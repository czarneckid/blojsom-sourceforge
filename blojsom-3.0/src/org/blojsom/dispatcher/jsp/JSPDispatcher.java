/**
 * Copyright (c) 2003-2006, David A. Czarnecki
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
package org.blojsom.dispatcher.jsp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.BlojsomException;
import org.blojsom.dispatcher.Dispatcher;
import org.blojsom.blog.Blog;
import org.blojsom.filter.PermalinkFilter;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * JSPDispatcher
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: JSPDispatcher.java,v 1.2 2006-03-21 16:32:21 czarneckid Exp $
 */
public class JSPDispatcher implements Dispatcher {

    private Log _logger = LogFactory.getLog(JSPDispatcher.class);

    private ServletContext _context;
    private ServletConfig _servletConfig;
    private Properties _blojsomProperties;
    private String _templatesDirectory;
    private String _blogsDirectory;

    /**
     * Create a new JSPDispatcher
     */
    public JSPDispatcher() {
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
     * Set the {@link ServletConfig}
     *
     * @param servletConfig {@link ServletConfig}
     */
    public void setServletConfig(ServletConfig servletConfig) {
        _servletConfig = servletConfig;
    }

    /**
     * Initialization method for blojsom dispatchers
     *
     * @param servletConfig        ServletConfig for obtaining any initialization parameters
     * @param blojsomConfiguration BlojsomConfiguration for blojsom-specific configuration information
     * @throws BlojsomException If there is an error initializing the dispatcher
     */
    public void init() throws BlojsomException {
        _templatesDirectory = _blojsomProperties.getProperty(BlojsomConstants.TEMPLATES_DIRECTORY_IP, BlojsomConstants.DEFAULT_TEMPLATES_DIRECTORY);
        _blogsDirectory = _blojsomProperties.getProperty(BlojsomConstants.BLOGS_DIRECTORY_IP, BlojsomConstants.DEFAULT_BLOGS_DIRECTORY);
        _context = _servletConfig.getServletContext();
    }

    /**
     * Dispatch a request and response. A context map is provided for the BlojsomServlet to pass
     * any required information for use by the dispatcher. The dispatcher is also
     * provided with the template for the requested flavor along with the content type for the
     * specific flavor.
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param blog                {@link Blog} instance
     * @param context             Context map
     * @param flavorTemplate      Template to dispatch to for the requested flavor
     * @param flavorContentType   Content type for the requested flavor
     * @throws IOException      If there is an exception during IO
     * @throws ServletException If there is an exception in dispatching the request
     */
    public void dispatch(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, Map context, String flavorTemplate, String flavorContentType) throws IOException, ServletException {
        httpServletResponse.setContentType(flavorContentType);

        if (!flavorTemplate.startsWith("/")) {
            flavorTemplate = '/' + flavorTemplate;
        }

        String flavorTemplateForPage = null;
        if (BlojsomUtils.getRequestValue(BlojsomConstants.PAGE_PARAM, httpServletRequest) != null) {
            flavorTemplateForPage = BlojsomUtils.getTemplateForPage(flavorTemplate, BlojsomUtils.getRequestValue(BlojsomConstants.PAGE_PARAM, httpServletRequest));

            if (_logger.isDebugEnabled()) {
                _logger.debug("Retrieved template for page: " + flavorTemplateForPage);
            }
        }

        // Populate the request with context attributes from the blog
        Iterator contextIterator = context.keySet().iterator();
        while (contextIterator.hasNext()) {
            String contextKey = (String) contextIterator.next();
            httpServletRequest.setAttribute(contextKey, context.get(contextKey));
        }

        if (httpServletRequest instanceof PermalinkFilter.PermalinkRequest) {
            PermalinkFilter.PermalinkRequest permalinkRequest = (PermalinkFilter.PermalinkRequest) httpServletRequest;
            permalinkRequest.setPathInfo(null);
        }

        // Try and look for the original flavor template with page for the individual user
        if (flavorTemplateForPage != null) {
            String templateToLoad = BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + _blogsDirectory + blog.getBlogId() + _templatesDirectory + BlojsomUtils.removeInitialSlash(flavorTemplateForPage);
            if (_context.getResource(templateToLoad) != null) {
                httpServletRequest.getRequestDispatcher(templateToLoad).forward(httpServletRequest, httpServletResponse);
                httpServletResponse.getWriter().flush();

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Dispatched to flavor page template for user: " + templateToLoad);
                }

            } else {
                templateToLoad = BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + BlojsomUtils.removeInitialSlash(_templatesDirectory) + BlojsomUtils.removeInitialSlash(flavorTemplateForPage);
                if (_context.getResource(templateToLoad) != null) {
                    // Otherwise, fallback and look for the flavor template with page without including any user information
                    httpServletRequest.getRequestDispatcher(templateToLoad).forward(httpServletRequest, httpServletResponse);
                    httpServletResponse.getWriter().flush();

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Dispatched to flavor page template: " + templateToLoad);
                    }

                } else {
                    if (_logger.isErrorEnabled()) {
                        _logger.error("Unable to dispatch to flavor page template: " + templateToLoad);
                    }
                }
            }
        } else {
            // Otherwise, fallback and look for the flavor template for the individual user
            String templateToLoad = BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + _blogsDirectory + blog.getBlogId() + _templatesDirectory + BlojsomUtils.removeInitialSlash(flavorTemplate);
            if (_context.getResource(templateToLoad) != null) {
                httpServletRequest.getRequestDispatcher(templateToLoad).forward(httpServletRequest, httpServletResponse);
                httpServletResponse.getWriter().flush();

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Dispatched to flavor template for user: " + templateToLoad);
                }

            } else {
                templateToLoad = BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + BlojsomUtils.removeInitialSlash(_templatesDirectory) + BlojsomUtils.removeInitialSlash(flavorTemplate);
                if (_context.getResource(templateToLoad) != null) {
                    // Otherwise, fallback and look for the flavor template without including any user information
                    httpServletRequest.getRequestDispatcher(templateToLoad).forward(httpServletRequest, httpServletResponse);
                    httpServletResponse.getWriter().flush();
                    
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Dispatched to flavor template: " + templateToLoad);
                    }
                } else {
                    if (_logger.isErrorEnabled()) {
                        _logger.error("Unable to dispatch to flavor template: " + templateToLoad);
                    }
                }
            }
        }
    }
}