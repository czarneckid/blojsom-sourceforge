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
 * Neither the name of the "David A. Czarnecki" nor the names of
 * its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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
import org.apache.xmlrpc.XmlRpcServer;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.blog.BlojsomConfigurationException;
import org.ignition.blojsom.extension.xmlrpc.handlers.AbstractBlojsomAPIHandler;
import org.ignition.blojsom.util.BlojsomConstants;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;


/**
 * Blojsom XML-RPC Servlet
 *
 * This servlet uses the Jakarta XML-RPC Library (http://ws.apache.org/xmlrpc)
 *
 * @author Mark Lussier
 * @version $Id: BlojsomXMLRPCServlet.java,v 1.3 2003-03-01 19:29:09 intabulas Exp $
 */
public class BlojsomXMLRPCServlet extends HttpServlet implements BlojsomConstants {
    private static final String BLOG_CONFIGURATION_IP = "blog-configuration";
    private static final String DEFAULT_BLOJSOM_CONFIGURATION = "/WEB-INF/blojsom.properties";
    private static final String BLOG_XMLRPC_CONFIGURATION_IP  = "blog-xmlrpc-configuration";

    private Log _logger = LogFactory.getLog(BlojsomXMLRPCServlet.class);

    protected Blog _blog = null;

    XmlRpcServer _xmlrpc;

    protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        byte[] result = _xmlrpc.execute(httpServletRequest.getInputStream());
        httpServletResponse.setContentType("text/xml");
        httpServletResponse.setContentLength(result.length);
        OutputStream out = httpServletResponse.getOutputStream();
        out.write(result);
        out.flush();
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
            Iterator handlerIterator = handlerMapProperties.keySet().iterator();
            while (handlerIterator.hasNext()) {
                String handlerName = (String) handlerIterator.next();
                String handlerClassName = handlerMapProperties.getProperty(handlerName);
                Class handlerClass = Class.forName(handlerClassName);
                AbstractBlojsomAPIHandler handler = (AbstractBlojsomAPIHandler) handlerClass.newInstance();
                handler.setBlog( _blog );
                _xmlrpc.addHandler( handler.getName(), handler);
                _logger.debug("Added ["+ handler.getName() +"] API Handler : " + handlerClass);
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


    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);



        String _cfgfile = servletConfig.getInitParameter(BLOG_CONFIGURATION_IP);

        if (_cfgfile == null || _cfgfile.equals("")) {
            _logger.info("blojsom  configuration not specified, using " + DEFAULT_BLOJSOM_CONFIGURATION);
            _cfgfile = DEFAULT_BLOJSOM_CONFIGURATION;
        }


        processBlojsomCongfiguration(servletConfig.getServletContext(), _cfgfile);
        configureAuthorization(servletConfig);

        _logger.info("Blojsom home is [" + _blog.getBlogHome() + "]");


        _xmlrpc = new XmlRpcServer();

        configureAPIHandlers(servletConfig);

    }


    public void destroy() {
    }

    public void processBlojsomCongfiguration(ServletContext context, String filename) {

        Properties _configuration = new Properties();
        InputStream _cis = context.getResourceAsStream(filename);

        try {
            _configuration.load(_cis);
            _blog = new Blog(_configuration);
        } catch (IOException e) {
            _logger.error(e);
        } catch (BlojsomConfigurationException e) {
            _logger.error(e);
        }

    }

}
