/**
 * Copyright (c) 2003-2004, David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003-2004 by Mark Lussier
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
package org.blojsom.extension.xmlrpc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpc;
import org.apache.xmlrpc.XmlRpcServer;
import org.blojsom.BlojsomException;
import org.blojsom.blog.BlogUser;
import org.blojsom.extension.xmlrpc.handlers.AbstractBlojsomAPIHandler;
import org.blojsom.fetcher.BlojsomFetcherException;
import org.blojsom.servlet.BlojsomBaseServlet;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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
 * <p/>
 * This servlet uses the Jakarta XML-RPC Library (http://ws.apache.org/xmlrpc)
 * 
 * @author Mark Lussier
 * @version $Id: BlojsomXMLRPCServlet.java,v 1.8 2004-04-19 18:06:49 intabulas Exp $
 */
public class BlojsomXMLRPCServlet extends BlojsomBaseServlet implements BlojsomXMLRPCConstants {

    private Log _logger = LogFactory.getLog(BlojsomXMLRPCServlet.class);
    private Map _xmlrpcServers;

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
        _xmlrpcServers = new HashMap(_blojsomConfiguration.getBlogUsers().size());

        String templateConfiguration = servletConfig.getInitParameter(BLOG_XMLRPC_CONFIGURATION_IP);
        Properties handlerMapProperties = new Properties();
        InputStream is = servletConfig.getServletContext().getResourceAsStream(templateConfiguration);
        try {
            handlerMapProperties.load(is);
            is.close();
            Iterator userIterator = _blojsomConfiguration.getBlogUsers().keySet().iterator();
            String user;
            BlogUser blogUser;

            // Check for the default XML-RPC handler
            String defaultXMLRPCHandler = handlerMapProperties.getProperty(DEFAULT_XMLRPC_HANDLER_KEY);
            handlerMapProperties.remove(DEFAULT_XMLRPC_HANDLER_KEY);

            // Instantiate an XML-RPC server and separate handler instances for each user
            while (userIterator.hasNext()) {
                user = (String) userIterator.next();
                blogUser = (BlogUser) _blojsomConfiguration.getBlogUsers().get(user);

                Iterator handlerIterator = handlerMapProperties.keySet().iterator();
                XmlRpcServer xmlRpcServer = new XmlRpcServer();

                while (handlerIterator.hasNext()) {
                    String handlerName = (String) handlerIterator.next();
                    String handlerClassName = handlerMapProperties.getProperty(handlerName);
                    Class handlerClass = Class.forName(handlerClassName);
                    AbstractBlojsomAPIHandler handler = (AbstractBlojsomAPIHandler) handlerClass.newInstance();
                    handler.setBlogUser(blogUser);
                    handler.setFetcher(_fetcher);
                    handler.setConfiguration(_blojsomConfiguration);
                    xmlRpcServer.addHandler(handler.getName(), handler);
                    if (defaultXMLRPCHandler != null && defaultXMLRPCHandler.equals(handlerName)) {
                        xmlRpcServer.addHandler(DEFAULT_XMLRPC_HANDLER_KEY, handler);
                        _logger.debug("Added default XML-RPC handler: " + handlerClass + " for user: " + user);
                    }
                    _logger.debug("Added [" + handler.getName() + "] API Handler : " + handlerClass + " for user: " + user);

                }

                _xmlrpcServers.put(user, xmlRpcServer);
            }
        } catch (InstantiationException e) {
            _logger.error(e);
        } catch (IllegalAccessException e) {
            _logger.error(e);
        } catch (ClassNotFoundException e) {
            _logger.error(e);
        } catch (IOException e) {
            _logger.error(e);
        } catch (BlojsomException e) {
            _logger.error(e);
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
        XmlRpc.setEncoding(UTF8);

        configureBlojsom(servletConfig);
        configureAuthorization(servletConfig);
        configureAPIHandlers(servletConfig);

        _logger.debug("blojsom XML-RPC: All Your Blog Are Belong To Us");
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
            httpServletRequest.setCharacterEncoding(UTF8);
        } catch (UnsupportedEncodingException e) {
            _logger.error(e);
        }

        // Determine the appropriate user from the URL
        String user = httpServletRequest.getPathInfo();
        if (BlojsomUtils.checkNullOrBlank(user) || "/".equals(user)) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested user not found: " + user);
            return;
        }

        user = BlojsomUtils.removeInitialSlash(user);

        // Make sure that the user exists in the system
        XmlRpcServer xmlRpcServer = (XmlRpcServer) _xmlrpcServers.get(user);
        if (xmlRpcServer == null) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested user not found: " + user);
            return;
        }

        byte[] result = xmlRpcServer.execute(httpServletRequest.getInputStream());
        String content = new String(result, UTF8);
        httpServletResponse.setContentType("text/xml;charset=UTF-8");
        httpServletResponse.setContentLength(content.length());
        OutputStreamWriter osw = new OutputStreamWriter(httpServletResponse.getOutputStream(), UTF8);
        osw.write(content);
        osw.flush();
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
