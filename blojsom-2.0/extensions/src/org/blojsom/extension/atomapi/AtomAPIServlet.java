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
package org.blojsom.extension.atomapi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.Base64;
import org.blojsom.fetcher.BlojsomFetcher;
import org.blojsom.fetcher.BlojsomFetcherException;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.blog.Blog;
import org.blojsom.blog.BlojsomConfigurationException;
import org.blojsom.blog.*;
import org.blojsom.fetcher.BlojsomFetcher;
import org.blojsom.fetcher.BlojsomFetcherException;
import org.blojsom.BlojsomException;
import org.blojsom.servlet.BlojsomBaseServlet;
import org.intabulas.sandler.elements.Entry;

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
 * <a href="http://bitworking.org/rfc/draft-gregorio-05.html">http://bitworking.org/rfc/draft-gregorio-08.html</a>
 *
 * @author Mark Lussier
 * @since blojsom 2.0
 * @version $Id: AtomAPIServlet.java,v 1.1 2003-09-07 17:56:09 intabulas Exp $
 */
public class AtomAPIServlet extends BlojsomBaseServlet implements BlojsomConstants, AtomConstants {

    private static final String FETCHER_PERMALINK = "FETCHER_PERMALINK";
    private static final String FETCHER_FLAVOR = "FETCHER_FLAVOR";
    private static final String FETCHER_NUM_POSTS_INTEGER = "FETCHER_NUM_POSTS_INTEGER";
    private static final String FETCHER_CATEGORY = "FETCHER_CATEGORY";


    private static final String BLOG_CONFIGURATION_IP = "blog-configuration";
    private static final String DEFAULT_BLOJSOM_CONFIGURATION = "/WEB-INF/blojsom.properties";

    private static final String CONTENTTYPE_ATOM = "application/atom+xml";

    private Log _logger = LogFactory.getLog(AtomAPIServlet.class);

    protected Blog _blog = null;
    protected BlogUser _blogUser;
    private BlojsomFetcher _fetcher;
    protected String _blogEntryExtension;

    /**
     * Public Constructor
     */
    public AtomAPIServlet() {
    }

    /**
     * Attach a Blog instance to the API Handler so that it can interact with the blog
     *
     * @param blogUser an instance of BlogUser
     * @see org.blojsom.blog.BlogUser
     * @throws org.blojsom.BlojsomException If there is an error setting the blog instance or properties for the handler
     */
    public void setBlogUser(BlogUser blogUser) throws BlojsomException {
        _blogUser = blogUser;
        _blog = _blogUser.getBlog();
        _blogEntryExtension = _blog.getBlogProperty(BLOG_ATOMAPI_ENTRY_EXTENSION_IP);
        if (_blogEntryExtension == null || "".equals(_blogEntryExtension)) {
            _blogEntryExtension = DEFAULT_BLOG_ATOMAPI_ENTRY_EXTENSION;
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
        configureBlojsom(servletConfig);
        configureAuthorization(servletConfig);

        _logger.info("AtomAPI initialized, home is [" + _blog.getBlogHome() + ']');

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
                    _logger.info("Unable to authenticate user [" + username + "] w/password [" + password + ']');
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
        String user = BlojsomUtils.getUserFromPath(httpServletRequest.getPathInfo());

        if ( user == null || "".equals(user)) {
             //@todo
        } else {
            try {
                setBlogUser((BlogUser)_users.get(user));
            } catch (BlojsomException e) {
                _logger.error(e.getLocalizedMessage(), e);
            }
        }


        Map fetchMap = new HashMap();
        BlogCategory blogCategory = _fetcher.newBlogCategory();
        blogCategory.setCategory(category);
        blogCategory.setCategoryURL(_blog.getBlogURL() + category);
        fetchMap.put(FETCHER_CATEGORY, blogCategory);
        fetchMap.put(FETCHER_PERMALINK, permalink);
        try {
            BlogEntry[] entries = _fetcher.fetchEntries(fetchMap, _blogUser);

            if (entries != null && entries.length > 0) {
                BlogEntry entry = entries[0];
                Entry atomentry = AtomUtils.fromBlogEntry(_blog, entry);
                String content = atomentry.toString();
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

