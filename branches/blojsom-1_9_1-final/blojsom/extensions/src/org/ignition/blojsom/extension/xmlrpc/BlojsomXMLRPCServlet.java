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
package org.ignition.blojsom.extension.xmlrpc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpc;
import org.apache.xmlrpc.XmlRpcServer;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.blog.BlojsomConfigurationException;
import org.ignition.blojsom.extension.xmlrpc.handlers.AbstractBlojsomAPIHandler;
import org.ignition.blojsom.fetcher.BlojsomFetcher;
import org.ignition.blojsom.fetcher.BlojsomFetcherException;
import org.ignition.blojsom.util.BlojsomConstants;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


/**
 * Blojsom XML-RPC Servlet
 *
 * This servlet uses the Jakarta XML-RPC Library (http://ws.apache.org/xmlrpc)
 *
 * @author Mark Lussier
 * @version $Id: BlojsomXMLRPCServlet.java,v 1.15 2003-05-26 18:16:16 czarneckid Exp $
 */
public class BlojsomXMLRPCServlet extends HttpServlet implements BlojsomConstants, BlojsomXMLRPCConstants {

    private Log _logger = LogFactory.getLog(BlojsomXMLRPCServlet.class);

    protected Blog _blog = null;
    private BlojsomFetcher _fetcher;

    XmlRpcServer _xmlrpc;

    /**
     * Construct a new Blojsom XML-RPC servlet instance
     */
    public BlojsomXMLRPCServlet() {
    }

    /**
     * Configure the XML-RPC API Handlers
     *
     * @param servletConfig Servlet configuration information
     */
    private void configureAPIHandlers(ServletConfig servletConfig) {
        String templateConfiguration = servletConfig.getInitParameter(BLOG_XMLRPC_CONFIGURATION_IP);
        Properties handlerMapProperties = new Properties();
        InputStream is = servletConfig.getServletContext().getResourceAsStream(templateConfiguration);
        try {
            handlerMapProperties.load(is);
            is.close();
            Iterator handlerIterator = handlerMapProperties.keySet().iterator();
            while (handlerIterator.hasNext()) {
                String handlerName = (String) handlerIterator.next();
                String handlerClassName = handlerMapProperties.getProperty(handlerName);
                Class handlerClass = Class.forName(handlerClassName);
                AbstractBlojsomAPIHandler handler = (AbstractBlojsomAPIHandler) handlerClass.newInstance();
                handler.setBlog(_blog);
                handler.setFetcher(_fetcher);
                _xmlrpc.addHandler(handler.getName(), handler);
                _logger.debug("Added [" + handler.getName() + "] API Handler : " + handlerClass);
            }
        } catch (InstantiationException e) {
            _logger.error(e);
        } catch (IllegalAccessException e) {
            _logger.error(e);
        } catch (ClassNotFoundException e) {
            _logger.error(e);
        } catch (IOException e) {
            _logger.error(e);
        }
    }

    /**
     * Configure the authorization table blog (user id's and and passwords)
     *
     * @param servletConfig Servlet configuration information
     */
    private void configureAuthorization(ServletConfig servletConfig) {
        Map _authorization = new HashMap();

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
                _authorization.put(userid, password);
            }

            if (!_blog.setAuthorization(_authorization)) {
                _logger.error("Authorization table could not be assigned");
            }

        } catch (IOException e) {
            _logger.error(e);
        }
    }

    /**
     * Load blojsom configuration information
     *
     * @param servletConfig Servlet configuration information
     * @param filename blojsom configuration file to be loaded
     */
    private void processBlojsomConfiguration(ServletConfig servletConfig, String filename) {
        Properties _configuration = new Properties();
        InputStream _cis = servletConfig.getServletContext().getResourceAsStream(filename);

        try {
            _configuration.load(_cis);
            _cis.close();
            _blog = new Blog(_configuration);
        } catch (IOException e) {
            _logger.error(e);
        } catch (BlojsomConfigurationException e) {
            _logger.error(e);
        }
    }

    /**
     * Configure the {@link BlojsomFetcher} that will be used to fetch categories and
     * entries
     *
     * @param servletConfig Servlet configuration information
     * @throws ServletException If the {@link BlojsomFetcher} class could not be loaded and/or initialized
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
     * Initialize the blojsom XML-RPC servlet
     *
     * @param servletConfig Servlet configuration information
     * @throws ServletException If there is an error initializing the servlet
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        String _cfgfile = servletConfig.getInitParameter(BLOG_CONFIGURATION_IP);

        if (_cfgfile == null || _cfgfile.equals("")) {
            _logger.info("blojsom configuration not specified, using " + DEFAULT_BLOJSOM_CONFIGURATION);
            _cfgfile = DEFAULT_BLOJSOM_CONFIGURATION;
        }

        _xmlrpc = new XmlRpcServer();
        XmlRpc.setEncoding(UTF8);

        processBlojsomConfiguration(servletConfig, _cfgfile);
        configureAuthorization(servletConfig);
        configureFetcher(servletConfig);
        configureAPIHandlers(servletConfig);

        _logger.info("blojsom home is [" + _blog.getBlogHome() + "]");
    }

    /**
     * Service an XML-RPC request by passing the request to the proper handler
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @throws ServletException If there is an error processing the request
     * @throws IOException If there is an error during I/O
     */
    protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        try {
            httpServletRequest.setCharacterEncoding(UTF8);
        } catch (UnsupportedEncodingException e) {
            _logger.error(e);
        }

        byte[] result = _xmlrpc.execute(httpServletRequest.getInputStream());
        String content = new String(result, UTF8);
        httpServletResponse.setContentType("text/xml;chartset=UTF-8");
        httpServletResponse.setContentLength(content.length());
        OutputStreamWriter osw = new OutputStreamWriter(httpServletResponse.getOutputStream(), UTF8);
        osw.write(content);
        osw.flush();
    }

    /**
     * Called when removing the servlet from the servlet container
     */
    public void destroy() {
    }
}
