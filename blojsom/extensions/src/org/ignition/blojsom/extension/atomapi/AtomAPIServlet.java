/**
 * Copyright (c) 2003, David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003 by Mark Lussier
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the "David A. Czarnecki" and "blojsom" nor the names of
 * its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Products derived from this software may not be called "blojsom",
 * nor may "blojsom" appear in their name, without prior written permission of
 * David A. Czarnecki.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ignition.blojsom.extension.atomapi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.Base64;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.blog.BlogCategory;
import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.blog.BlojsomConfigurationException;
import org.ignition.blojsom.fetcher.BlojsomFetcher;
import org.ignition.blojsom.fetcher.BlojsomFetcherException;
import org.ignition.blojsom.util.BlojsomConstants;
import org.ignition.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * AtomAPIServlet
 *
 * Implementation of J.C. Gregorio's AtomAPI
 * <a href="http://bitworking.org/rfc/draft-gregorio-04.html">http://bitworking.org/rfc/draft-gregorio-04.html</a>
 *
 * @author Mark Lussier
 * @version $Id: AtomAPIServlet.java,v 1.1 2003-07-18 01:20:55 czarneckid Exp $
 */
public class AtomAPIServlet extends HttpServlet implements BlojsomConstants, AtomConstants {

    private static final String FETCHER_PERMALINK = "FETCHER_PERMALINK";
    private static final String FETCHER_FLAVOR = "FETCHER_FLAVOR";
    private static final String FETCHER_NUM_POSTS_INTEGER = "FETCHER_NUM_POSTS_INTEGER";
    private static final String FETCHER_CATEGORY = "FETCHER_CATEGORY";


    private static final String BLOG_CONFIGURATION_IP = "blog-configuration";
    private static final String DEFAULT_BLOJSOM_CONFIGURATION = "/WEB-INF/blojsom.properties";

    private static final String CONTENTTYPE_ATOM = "application/not-atom+xml";

    private Log _logger = LogFactory.getLog(AtomAPIServlet.class);

    protected Blog _blog = null;

    private BlojsomFetcher _fetcher;

    /**
     * Public Constructor
     */
    public AtomAPIServlet() {
    }

    /**
     * Configure the authorization table blog (user id's and and passwords)
     *
     * @param servletConfig Servlet configuration information
     */
    private void configureAuthorization(ServletConfig servletConfig) {
        Map authorization = new HashMap();

        String authConfiguration = servletConfig.getInitParameter(BLOG_AUTHORIZATION_IP);
        Properties authProperties = new Properties();
        InputStream is = servletConfig.getServletContext().getResourceAsStream(authConfiguration);
        try {
            authProperties.load(is);
            is.close();
            Iterator authIterator = authProperties.keySet().iterator();
            while (authIterator.hasNext()) {
                String userid = (String) authIterator.next();
                String password = authProperties.getProperty(userid);
                authorization.put(userid, password);
            }

            if (!_blog.setAuthorization(authorization)) {
                _logger.error("Authorization table could not be assigned");
            }

        } catch (IOException e) {
            _logger.error(e);
        }
    }

    /**
     * Load blojsom configuration information
     *
     * @param context Servlet context
     * @param filename blojsom configuration file to be loaded
     */
    public void processBlojsomCongfiguration(ServletContext context, String filename) {
        Properties configuration = new Properties();
        InputStream cis = context.getResourceAsStream(filename);

        try {
            configuration.load(cis);
            cis.close();
            _blog = new Blog(configuration);
        } catch (IOException e) {
            _logger.error(e);
        } catch (BlojsomConfigurationException e) {
            _logger.error(e);
        }
    }


    /**
     * Configure the {@link org.ignition.blojsom.fetcher.BlojsomFetcher} that will be used to fetch categories and
     * entries
     *
     * @param servletConfig Servlet configuration information
     * @throws ServletException If the {@link org.ignition.blojsom.fetcher.BlojsomFetcher} class could not be loaded and/or initialized
     */
    private void configureFetcher(ServletConfig servletConfig) throws ServletException {
        String fetcherClassName = _blog.getBlogFetcher();
        if ((fetcherClassName == null) || "".equals(fetcherClassName)) {
            fetcherClassName = BLOG_DEFAULT_FETCHER;
        }

        try {
            Class fetcherClass = Class.forName(fetcherClassName);
            _fetcher = (BlojsomFetcher) fetcherClass.newInstance();
            _fetcher.init(servletConfig, _blog);
            _logger.info("Added blojsom fetcher: " + fetcherClassName);
        } catch (ClassNotFoundException e) {
            _logger.error(e);
            throw new ServletException(e);
        } catch (InstantiationException e) {
            _logger.error(e);
            throw new ServletException(e);
        } catch (IllegalAccessException e) {
            _logger.error(e);
            throw new ServletException(e);
        } catch (BlojsomFetcherException e) {
            _logger.error(e);
            throw new ServletException(e);
        }
    }


    /**
     * Initialize the blojsom AtomAPI servlet
     *
     * @param servletConfig Servlet configuration information
     * @throws javax.servlet.ServletException If there is an error initializing the servlet
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        String cfgfile = servletConfig.getInitParameter(BLOG_CONFIGURATION_IP);

        if (cfgfile == null || cfgfile.equals("")) {
            _logger.info("blojsom configuration not specified, using " + DEFAULT_BLOJSOM_CONFIGURATION);
            cfgfile = DEFAULT_BLOJSOM_CONFIGURATION;
        }

        processBlojsomCongfiguration(servletConfig.getServletContext(), cfgfile);
        processBlojsomCongfiguration(servletConfig.getServletContext(), cfgfile);
        configureAuthorization(servletConfig);
        configureFetcher(servletConfig);

        _logger.info("AtomAPI initialized, home is [" + _blog.getBlogHome() + "]");

    }


    /**
     * Decode the HTTP Authorization Header
     * @param httpServletRequest
     * @return
     */
    private String extractAuthorization(HttpServletRequest httpServletRequest) {
        String result = null;
        String auth = httpServletRequest.getHeader(HEADER_AUTHORIZATION);

        if ((auth != null)) {
            if (auth.startsWith(BASE64_AUTH_PREFIX)) {
                auth = auth.substring(BASE64_AUTH_PREFIX.length());
            }
            result = new String(Base64.decode(auth.getBytes()));
        }
        return result;
    }


    /**
     *
     * @param httpServletRequest Request
     * @return
     */
    private boolean isAuthorized(HttpServletRequest httpServletRequest) {
        boolean result = false;
        String realmAuth = extractAuthorization(httpServletRequest);
        if (realmAuth != null && !"".equals(realmAuth)) {
            int pos = realmAuth.indexOf(":");
            if (pos > 0) {
                String username = realmAuth.substring(0, pos);
                String password = realmAuth.substring(pos + 1);
                result = _blog.checkAuthorization(username, password);
                if (!result) {
                    _logger.info("Unable to authenticate user [" + username + "] w/password [" + password + "]");
                }

            }

        }
        return result;
    }


    private void sendAuthenticationRequired(HttpServletResponse httpServletResponse) {
        httpServletResponse.setContentType("text/html");
        httpServletResponse.setHeader(HEADER_AUTHCHALLENGE, AUTHENTICATION_RELM);
        httpServletResponse.setStatus(401);
    }


    /**
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @throws ServletException If there is an error processing the request
     * @throws IOException If there is an error during I/O
     */
    protected void doDelete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {

        _logger.info("AtomAPI Delete Called =====[ SUPPORTED ]=====");
        _logger.info("       Path: " + httpServletRequest.getPathInfo());

        if (isAuthorized(httpServletRequest)) {


        } else {
            sendAuthenticationRequired(httpServletResponse);
        }

    }


    /**
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @throws ServletException If there is an error processing the request
     * @throws IOException If there is an error during I/O
     */
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {


        _logger.info("AtomAPI GET Called =====[ SUPPORTED ]=====");
        _logger.info("       Path: " + httpServletRequest.getPathInfo());

        // NOTE: Assumes that the getPathInfo() returns only category data

        String permalink = BlojsomUtils.getRequestValue(PERMALINK_PARAM, httpServletRequest);
        String category = BlojsomUtils.normalize(httpServletRequest.getPathInfo());

        Map fetchMap = new HashMap();
        BlogCategory blogCategory = _fetcher.newBlogCategory();
        blogCategory.setCategory(category);
        blogCategory.setCategoryURL(_blog.getBlogURL() + category);
        fetchMap.put(FETCHER_CATEGORY, blogCategory);
        fetchMap.put(FETCHER_PERMALINK, permalink);
        try {
            BlogEntry[] _entries = _fetcher.fetchEntries(fetchMap);

            if (_entries != null && _entries.length > 0) {
                BlogEntry entry = _entries[0];
                // AtomEntry converts a BlogEntry to an Atom Entry XML stream..
                // VERY messy right now and it will be refactored to be bidi
                AtomEntry atom = new AtomEntry(_blog, entry);
                String content = atom.getAsString();
                httpServletResponse.setContentType(CONTENTTYPE_ATOM);
                httpServletResponse.setStatus(200);
                httpServletResponse.setContentLength(content.length());
                OutputStreamWriter osw = new OutputStreamWriter(httpServletResponse.getOutputStream(), "UTF-8");
                osw.write(content);
                osw.flush();

            }
        } catch (BlojsomFetcherException e) {
            _logger.error(e);
            httpServletResponse.setStatus(404);
        }


    }

    /**
     * Handle HTTP POST
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @throws ServletException If there is an error processing the request
     * @throws IOException If there is an error during I/O
     */
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {

        _logger.info("AtomAPI POST Called =====[ SUPPORTED ]=====");
        _logger.info("       Path: " + httpServletRequest.getPathInfo());

        if (isAuthorized(httpServletRequest)) {

        } else {
            sendAuthenticationRequired(httpServletResponse);
        }


    }

    /**
     * Handle HTTP PUT
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @throws ServletException If there is an error processing the request
     * @throws IOException If there is an error during I/O
     */
    protected void doPut(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {

        _logger.info("AtomAPI PUT Called =====[ SUPPORTED ]=====");
        _logger.info("       Path: " + httpServletRequest.getPathInfo());

        if (isAuthorized(httpServletRequest)) {

        } else {
            sendAuthenticationRequired(httpServletResponse);
        }


    }


    /**
     * Called when removing the servlet from the servlet container
     */
    public void destroy() {
        try {
            _fetcher.destroy();
        } catch (BlojsomFetcherException e) {
            _logger.error(e);
        }

    }

}

