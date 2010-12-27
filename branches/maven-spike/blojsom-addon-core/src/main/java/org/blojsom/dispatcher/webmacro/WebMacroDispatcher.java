/**
 * Copyright (c) 2003-2009, David A. Czarnecki
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
package org.blojsom.dispatcher.webmacro;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.util.EnumerationIterator;
import org.blojsom.BlojsomException;
import org.blojsom.blog.Blog;
import org.blojsom.dispatcher.Dispatcher;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.webmacro.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * WebMacro dispatcher
 *
 * @author David Czarnecki
 * @version $Id: WebMacroDispatcher.java,v 1.3 2008-07-07 19:54:16 czarneckid Exp $
 * @since blojsom 3.0
 */
public class WebMacroDispatcher implements Dispatcher {

    private Log _logger = LogFactory.getLog(WebMacroDispatcher.class);

    private Properties _webMacroProperties;
    private Properties _blojsomProperties;
    private ServletConfig _servletConfig;

    private String _templatesDirectory;
    private String _blogsDirectory;


    /**
     * Construct a new instance of the WebMacro dispatcher
     */
    public WebMacroDispatcher() {
    }

    /**
     * Set the WebMacro properties for use by the dispatcher
     *
     * @param webMacroProperties Properties for WebMacro configuration
     */
    public void setWebMacroProperties(Properties webMacroProperties) {
        _webMacroProperties = webMacroProperties;
    }

    /**
     * Set the properties in use by blojsom
     *
     * @param blojsomProperties Properties in use by blojsom
     */
    public void setBlojsomProperties(Properties blojsomProperties) {
        _blojsomProperties = blojsomProperties;
    }

    /**
     * Set the {@link javax.servlet.ServletConfig}
     *
     * @param servletConfig {@link javax.servlet.ServletConfig}
     */
    public void setServletConfig(ServletConfig servletConfig) {
        _servletConfig = servletConfig;
    }

    /**
     * Initialization method for blojsom dispatchers
     *
     * @throws org.blojsom.BlojsomException If there is an error initializing the dispatcher
     */
    public void init() throws BlojsomException {
        _templatesDirectory = _blojsomProperties.getProperty(BlojsomConstants.TEMPLATES_DIRECTORY_IP, BlojsomConstants.DEFAULT_TEMPLATES_DIRECTORY);
        _blogsDirectory = _blojsomProperties.getProperty(BlojsomConstants.BLOGS_DIRECTORY_IP, BlojsomConstants.DEFAULT_BLOGS_DIRECTORY);
    }

    /**
     * Return a path appropriate for the WebMacro file resource loader for a given blog
     *
     * @param blogID Blog ID
     * @return blojsom installation directory + base configuration directory + Blog ID + templates directory
     */
    protected String getWebMacroTemplatePathForBlog(String blogID) {
        StringBuffer templatePathForBlog = new StringBuffer();
        templatePathForBlog.append(_servletConfig.getServletContext().getRealPath(BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY));
        templatePathForBlog.append(_blogsDirectory);
        templatePathForBlog.append(blogID);
        templatePathForBlog.append(_templatesDirectory);

        return templatePathForBlog.toString();
    }

    /**
     * Return a path appropriate for the WebMacro file resource loader
     *
     * @return blojsom installation directory + base configuration directory + templates directory
     */
    protected String getWebMacroGlobalTemplatePath() {
        StringBuffer templatePath = new StringBuffer();

        templatePath.append(_servletConfig.getServletContext().getRealPath(BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY));
        templatePath.append(_templatesDirectory);

        return templatePath.toString();
    }

    /**
     * Populate the WebMacro context with the request and session attributes
     *
     * @param httpServletRequest Request
     * @param context            Context
     */
    protected void populateWebMacroContext(HttpServletRequest httpServletRequest, Map context) {
        EnumerationIterator iterator = new EnumerationIterator(httpServletRequest.getAttributeNames());
        while (iterator.hasNext()) {
            Object key = iterator.next();
            Object value = httpServletRequest.getAttribute(key.toString());
            context.put(key, value);
        }

        HttpSession httpSession = httpServletRequest.getSession();
        if (httpSession != null) {
            iterator = new EnumerationIterator(httpSession.getAttributeNames());
            while (iterator.hasNext()) {
                Object key = iterator.next();
                Object value = httpSession.getAttribute(key.toString());
                context.put(key, value);
            }
        }
    }

    /**
     * Dispatch a request and response. A context map is provided for the BlojsomServlet to pass
     * any required information for use by the dispatcher. The dispatcher is also
     * provided with the template for the requested flavor along with the content type for the
     * specific flavor.
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param blog                {@link Blog}
     * @param context             Context map
     * @param flavorTemplate      Template to dispatch to for the requested flavor
     * @param flavorContentType   Content type for the requested flavor
     * @throws java.io.IOException            If there is an exception during IO
     * @throws javax.servlet.ServletException If there is an exception in dispatching the request
     */
    public void dispatch(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, Map context, String flavorTemplate, String flavorContentType) throws IOException, ServletException {
        httpServletResponse.setContentType(flavorContentType);

        try {
            Properties updatedWebMacroProperties = (Properties) _webMacroProperties.clone();
            updatedWebMacroProperties.setProperty("TemplateLoaderPath.1", "file:" + getWebMacroTemplatePathForBlog(blog.getBlogId()));
            updatedWebMacroProperties.setProperty("TemplateLoaderPath.2", "file:" + getWebMacroGlobalTemplatePath());

            WM wm = new WM(updatedWebMacroProperties);
            Context wmContext = wm.getContext();
            populateWebMacroContext(httpServletRequest, context);
            wmContext.setMap(context);

            String flavorTemplateForPage = null;
            String pageParameter = BlojsomUtils.getRequestValue(BlojsomConstants.PAGE_PARAM, httpServletRequest, true);

            if (pageParameter != null) {
                flavorTemplateForPage = BlojsomUtils.getTemplateForPage(flavorTemplate, pageParameter);
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Retrieved template for page: " + flavorTemplateForPage);
                }
            }

            if (BlojsomUtils.checkNullOrBlank(flavorTemplateForPage)) {
                try {
                    wm.writeTemplate(flavorTemplate, httpServletResponse.getOutputStream(), wmContext);
                } catch (ResourceException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                } catch (PropertyException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                }

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Dispatched to flavor template: " + flavorTemplate);
                }
            } else {
                try {
                    wm.writeTemplate(flavorTemplateForPage, httpServletResponse.getOutputStream(), wmContext);
                } catch (ResourceException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                } catch (PropertyException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                }

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Dispatched to flavor page template: " + flavorTemplateForPage);
                }

            }

            httpServletResponse.getOutputStream().flush();
        } catch (InitException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }
        }
    }
}
