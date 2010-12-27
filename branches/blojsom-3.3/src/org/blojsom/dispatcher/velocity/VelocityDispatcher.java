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
package org.blojsom.dispatcher.velocity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.util.EnumerationIterator;
import org.blojsom.dispatcher.Dispatcher;
import org.blojsom.blog.Blog;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;

/**
 * Velocity dispatcher
 *
 * @author David Czarnecki
 * @version $Id: VelocityDispatcher.java,v 1.7 2008-07-07 19:55:13 czarneckid Exp $
 * @since blojsom 3.0
 */
public class VelocityDispatcher implements Dispatcher {

    private Log _logger = LogFactory.getLog(VelocityDispatcher.class);

    private static final String BLOJSOM_RENDER_TOOL = "BLOJSOM_RENDER_TOOL";

    private Properties _velocityProperties;
    private ServletConfig _servletConfig;
    private Properties _blojsomProperties;

    private String _templatesDirectory;
    private String _blogsDirectory;

    /**
     * Create a new instance of the Velocity dispatcher
     */
    public VelocityDispatcher() {
    }

    /**
     * Initialization method for blojsom dispatchers
     *
     * @throws org.blojsom.BlojsomException If there is an error initializing the dispatcher
     */
    public void init() throws org.blojsom.BlojsomException {
        _templatesDirectory = _blojsomProperties.getProperty(BlojsomConstants.TEMPLATES_DIRECTORY_IP, BlojsomConstants.DEFAULT_TEMPLATES_DIRECTORY);
        _blogsDirectory = _blojsomProperties.getProperty(BlojsomConstants.BLOGS_DIRECTORY_IP, BlojsomConstants.DEFAULT_BLOGS_DIRECTORY);
    }

    /**
     * Set the Velocity properties for use by the dispatcher
     *
     * @param velocityProperties Properties for Velocity configuration
     */
    public void setVelocityProperties(Properties velocityProperties) {
        _velocityProperties = velocityProperties;
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
     * Populate the Velocity context with the request and session attributes
     *
     * @param httpServletRequest Request
     * @param context            Context
     */
    protected void populateVelocityContext(HttpServletRequest httpServletRequest, Map context) {
        EnumerationIterator iterator = new EnumerationIterator(httpServletRequest.getAttributeNames());
        while (iterator.hasNext()) {
            Object key = iterator.next();
            Object value = httpServletRequest.getAttribute(key.toString());
            context.put(key, value);
        }

        HttpSession httpSession = httpServletRequest.getSession(false);
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
     * Remove references from the Velocity context
     *
     * @param velocityContext {@link VelocityContext}
     */
    protected void destroyVelocityContext(VelocityContext velocityContext) {
        // Make sure no objects are referenced in the context after they're finished
        Object[] contextKeys = velocityContext.getKeys();
        for (int i = 0; i < contextKeys.length; i++) {
            Object contextKey = contextKeys[i];
            velocityContext.remove(contextKey);
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
        ServletContext servletContext = _servletConfig.getServletContext();

        // Create the Velocity Engine
        VelocityEngine velocityEngine = new VelocityEngine();
        try {
            Properties updatedProperties = (Properties) _velocityProperties.clone();
            updatedProperties.put(VelocityEngine.FILE_RESOURCE_LOADER_PATH, servletContext.getRealPath(BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + _blogsDirectory + blog.getBlogId() + _templatesDirectory) + ", " + servletContext.getRealPath(BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + _templatesDirectory));
            velocityEngine.init(updatedProperties);
        } catch (Exception e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            return;
        }

        Writer responseWriter = httpServletResponse.getWriter();
        String flavorTemplateForPage = null;
        String pageParameter = BlojsomUtils.getRequestValue(BlojsomConstants.PAGE_PARAM, httpServletRequest, true);

        if (pageParameter != null) {
            flavorTemplateForPage = BlojsomUtils.getTemplateForPage(flavorTemplate, pageParameter);

            if (_logger.isDebugEnabled()) {
                _logger.debug("Retrieved template for page: " + flavorTemplateForPage);
            }
        }

        // Setup the VelocityContext
        populateVelocityContext(httpServletRequest, context);
        VelocityContext velocityContext = new VelocityContext(context);
        velocityContext.put(BLOJSOM_RENDER_TOOL, new BlojsomRenderTool(velocityEngine, velocityContext));

        if (flavorTemplateForPage != null) {
            // Try and look for the flavor page template for the individual user
            if (!velocityEngine.templateExists(flavorTemplateForPage)) {
                if (_logger.isErrorEnabled()) {
                    _logger.error("Could not find flavor page template for user: " + flavorTemplateForPage);
                }

                responseWriter.flush();
                destroyVelocityContext(velocityContext);

                return;
            } else {
                try {
                    velocityEngine.mergeTemplate(flavorTemplateForPage, BlojsomConstants.UTF8, velocityContext, responseWriter);
                } catch (Exception e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }

                    responseWriter.flush();
                    destroyVelocityContext(velocityContext);

                    return;
                }
            }

            _logger.debug("Dispatched to flavor page template: " + flavorTemplateForPage);
        } else {
            // Otherwise, fallback and look for the flavor template for the individual user
            if (!velocityEngine.templateExists(flavorTemplate)) {
                if (_logger.isErrorEnabled()) {
                    _logger.error("Could not find flavor template for user: " + flavorTemplate);
                }

                responseWriter.flush();
                destroyVelocityContext(velocityContext);

                return;
            } else {
                try {
                    velocityEngine.mergeTemplate(flavorTemplate, BlojsomConstants.UTF8, velocityContext, responseWriter);
                } catch (Exception e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }

                    responseWriter.flush();
                    destroyVelocityContext(velocityContext);

                    return;
                }
            }

            if (_logger.isDebugEnabled()) {
                _logger.debug("Dispatched to flavor template: " + flavorTemplate);
            }
        }

        responseWriter.flush();
        destroyVelocityContext(velocityContext);
    }

    /**
     * Blojsom render tool mimics the functionality of the Velocity render tool to parse VTL markup added to a
     * template
     */
    public class BlojsomRenderTool {

        private static final String LOG_TAG = "BlojsomRenderTool";

        private VelocityEngine _velocityEngine;
        private VelocityContext _velocityContext;

        /**
         * Create a new instance of the render tool
         *
         * @param velocityEngine  {@link VelocityEngine}
         * @param velocityContext {@link VelocityContext}
         */
        public BlojsomRenderTool(VelocityEngine velocityEngine, VelocityContext velocityContext) {
            _velocityEngine = velocityEngine;
            _velocityContext = velocityContext;
        }

        /**
         * Evaluate a string containing VTL markup
         *
         * @param template VTL markup
         * @return Processed VTL or <code>null</code> if an error in evaluation
         */
        public String evaluate(String template) {
            if (BlojsomUtils.checkNullOrBlank(template)) {
                return null;
            }

            StringWriter sw = new StringWriter();
            boolean success = false;

            try {
                if (_velocityEngine == null) {
                    success = Velocity.evaluate(_velocityContext, sw, LOG_TAG, template);
                } else {
                    success = _velocityEngine.evaluate(_velocityContext, sw, LOG_TAG, template);
                }
            } catch (ParseErrorException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            } catch (MethodInvocationException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            } catch (ResourceNotFoundException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            } catch (IOException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            }

            if (success) {
                return sw.toString();
            }

            return null;
        }
    }
}
