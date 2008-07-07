/**
 * Copyright (c) 2003-2008, David A. Czarnecki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *     following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *     following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of "David A. Czarnecki" and "blojsom" nor the names of its contributors may be used to
 *     endorse or promote products derived from this software without specific prior written permission.
 * Products derived from this software may not be called "blojsom", nor may "blojsom" appear in their name,
 *     without prior written permission of David A. Czarnecki.
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
package org.blojsom.extension.xmlrpc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpc;
import org.apache.xmlrpc.XmlRpcServer;
import org.blojsom.blog.Blog;
import org.blojsom.extension.xmlrpc.handler.APIHandler;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.servlet.ServletConfigFactoryBean;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.Properties;


/**
 * Blojsom XML-RPC Servlet
 * <p/>
 * This servlet uses the Jakarta XML-RPC Library (http://ws.apache.org/xmlrpc)
 *
 * @author Mark Lussier
 * @author David Czarnecki
 * @version $Id: BlojsomXMLRPCServlet.java,v 1.7 2008-07-07 19:54:27 czarneckid Exp $
 * @since blojsom 3.0
 */
public class BlojsomXMLRPCServlet extends HttpServlet {

    protected Log _logger = LogFactory.getLog(BlojsomXMLRPCServlet.class);

    protected String[] BLOJSOM_CONFIGURATION_FILES = {"blojsom-xmlrpc.xml"};

    protected static final int XMLRPC_DISABLED = 4000;
    protected static final String XMLRPC_DISABLED_MESSAGE = "XML-RPC disabled for the requested blog";
    protected static final String XMLRPC_ACCEPTS_ONLY_POSTS_MESSAGE = "XML-RPC server only accepts POST requests.";

    protected ClassPathXmlApplicationContext _classPathXmlApplicationContext;

    /**
     * Construct a new Blojsom XML-RPC servlet instance
     */
    public BlojsomXMLRPCServlet() {
    }

    /**
     * Initialize the blojsom XML-RPC servlet
     *
     * @param servletConfig Servlet configuration information
     * @throws ServletException If there is an error initializing the servlet
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        ServletConfigFactoryBean.setServletConfig(servletConfig);

        _classPathXmlApplicationContext = new ClassPathXmlApplicationContext(BLOJSOM_CONFIGURATION_FILES);
        servletConfig.getServletContext().setAttribute(BlojsomConstants.BLOJSOM_XMLRPC_APPLICATION_CONTEXT, _classPathXmlApplicationContext);

        // Set the default encoding for the XmlRpc classes to UTF-8
        XmlRpc.setEncoding(BlojsomConstants.UTF8);

        if (_logger.isDebugEnabled()) {
            _logger.debug("blojsom XML-RPC: All Your Blog Are Belong To Us");
        }
    }

    /**
     * Configure the XML-RPC server for the given blog
     *
     * @param httpServletRequest  {@link HttpServletRequest}
     * @param httpServletResponse {@link HttpServletResponse}
     * @param blogId              Blog ID
     * @return {@link XmlRpcServer} instance with handlers configured for the given blog
     */
    protected XmlRpcServer configureXMLRPCServer(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String blogId) {
        XmlRpcServer xmlRpcServer = new XmlRpcServer();

        Fetcher fetcher = (Fetcher) _classPathXmlApplicationContext.getBean("fetcher");
        Properties defaultProperties = (Properties) _classPathXmlApplicationContext.getBean("defaultProperties");

        Blog blog;
        try {
            blog = fetcher.loadBlog(blogId);
        } catch (FetcherException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            return null;
        }

        if ("true".equals(blog.getProperty(BlojsomConstants.USE_DYNAMIC_BLOG_URLS))) {
            String servletPath = defaultProperties.getProperty(BlojsomXMLRPCConstants.SERVLET_PATH_IP);
            if (BlojsomUtils.checkNullOrBlank(servletPath)) {
                servletPath = BlojsomXMLRPCConstants.DEFAULT_SERVLET_PATH;
            }

            BlojsomUtils.resolveDynamicBaseAndBlogURL(httpServletRequest, blog, blogId, servletPath);
        }

        // Check to see if XML-RPC is enabled for the blog
        if (!blog.getXmlrpcEnabled().booleanValue()) {
            if (_logger.isErrorEnabled()) {
                _logger.error(XMLRPC_DISABLED_MESSAGE);
            }

            return null;
        }

        Properties xmlrpcProperties = (Properties) _classPathXmlApplicationContext.getBean("xmlrpcProperties");
        String defaultXMLRPCHandler = xmlrpcProperties.getProperty(BlojsomXMLRPCConstants.DEFAULT_XMLRPC_HANDLER_KEY);
        xmlrpcProperties.remove(BlojsomXMLRPCConstants.DEFAULT_XMLRPC_HANDLER_KEY);

        Iterator handlerIterator = xmlrpcProperties.keySet().iterator();
        while (handlerIterator.hasNext()) {
            String handlerName = (String) handlerIterator.next();
            APIHandler apiHandler = (APIHandler) _classPathXmlApplicationContext.getBean(handlerName);
            apiHandler.setProperties(defaultProperties);
            apiHandler.setHttpServletRequest(httpServletRequest);
            apiHandler.setHttpServletResponse(httpServletResponse);
            apiHandler.setBlog(blog);

            xmlRpcServer.addHandler(handlerName, apiHandler);

            if (defaultXMLRPCHandler != null && defaultXMLRPCHandler.equals(handlerName)) {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Added default XML-RPC handler: " + apiHandler.getClass().getName() + " for blog: " + blog.getBlogId());
                }
                xmlRpcServer.addHandler(BlojsomXMLRPCConstants.DEFAULT_XMLRPC_HANDLER_KEY, apiHandler);
            }

            if (_logger.isDebugEnabled()) {
                _logger.debug("Added [" + handlerName + "] API Handler : " + apiHandler.getClass().getName() + " for blog: " + blog.getBlogId());
            }
        }

        return xmlRpcServer;
    }

    /**
     * Service an XML-RPC request by passing the request to the proper handler
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @throws ServletException If there is an error processing the request
     * @throws IOException      If there is an error during I/O
     */
    protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        try {
            httpServletRequest.setCharacterEncoding(BlojsomConstants.UTF8);
        } catch (UnsupportedEncodingException e) {
            _logger.error(e);
        }

        if (!"post".equalsIgnoreCase(httpServletRequest.getMethod())) {
            httpServletResponse.setContentType("text/html; charset=UTF-8");
            httpServletResponse.setContentLength(XMLRPC_ACCEPTS_ONLY_POSTS_MESSAGE.length());
            httpServletResponse.setStatus(HttpURLConnection.HTTP_BAD_METHOD);
            PrintWriter printWriter = httpServletResponse.getWriter();
            printWriter.print(XMLRPC_ACCEPTS_ONLY_POSTS_MESSAGE);
            printWriter.flush();
        } else {
            // Determine the appropriate blog from the URL
            String blogId = BlojsomUtils.getBlogFromPath(httpServletRequest.getPathInfo());
            if (BlojsomUtils.checkNullOrBlank(blogId) || "/".equals(blogId)) {
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Unable to load blog ID");

                return;
            }

            // Make sure that the blog exists in the system
            XmlRpcServer xmlRpcServer = configureXMLRPCServer(httpServletRequest, httpServletResponse, blogId);
            if (xmlRpcServer == null) {
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Unable to load blog ID");

                return;
            }

            byte[] result = xmlRpcServer.execute(httpServletRequest.getInputStream());
            httpServletResponse.setContentType("text/xml; charset=UTF-8");
            httpServletResponse.setContentLength(result.length);
            OutputStream os = httpServletResponse.getOutputStream();
            os.write(result);
            os.flush();
        }
    }

    /**
     * Called when removing the servlet from the servlet container
     */
    public void destroy() {
        super.destroy();

        _classPathXmlApplicationContext.destroy();

        if (_logger.isDebugEnabled()) {
            _logger.debug("blojsom XML-RPC destroyed");
        }
    }
}
