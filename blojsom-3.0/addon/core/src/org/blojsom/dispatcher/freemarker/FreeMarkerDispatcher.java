/**
 * Copyright (c) 2003-2007, David A. Czarnecki
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
package org.blojsom.dispatcher.freemarker;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.util.EnumerationIterator;
import org.blojsom.BlojsomException;
import org.blojsom.blog.Blog;
import org.blojsom.dispatcher.Dispatcher;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;

/**
 * FreeMarkerDispatcher
 * 
 * @author Dsvid Czarnecki
 * @version $Id: FreeMarkerDispatcher.java,v 1.3 2007-03-22 00:43:07 czarneckid Exp $
 * @since blojsom 3.0
 */
public class FreeMarkerDispatcher implements Dispatcher {

    private Log _logger = LogFactory.getLog(FreeMarkerDispatcher.class);

    private Properties _freemarkerProperties;
    private Properties _blojsomProperties;
    private ServletConfig _servletConfig;

    private String _templatesDirectory;
    private String _blogsDirectory;

    /**
     * Default constructor.
     */
    public FreeMarkerDispatcher() {
    }

    /**
     * Set the Freemarker properties for use by the dispatcher
     *
     * @param freemarkerProperties Properties for Freemarker configuration
     */
    public void setFreemarkerProperties(Properties freemarkerProperties) {
        _freemarkerProperties = freemarkerProperties;
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
     * Set paths appropriate for loading FreeMarker templates
     * 
     * @param blogID Blog ID
     * @param freemarkerConfiguration Freemarker configuration
     */
    private void setTemplatePath(String blogID, Configuration freemarkerConfiguration) {
        ServletContext servletContext = _servletConfig.getServletContext();

        try {
            StringBuffer templatePath = new StringBuffer(servletContext.getRealPath(BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY));
            templatePath.append(_blogsDirectory).append(blogID).append(_templatesDirectory);
            FileTemplateLoader fileTemplateLoaderUser = new FileTemplateLoader(new File(templatePath.toString()));

            ClassTemplateLoader classTemplateLoader = new ClassTemplateLoader(getClass(), "");

            templatePath = new StringBuffer();
            templatePath.append(servletContext.getRealPath(BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY));
            templatePath.append(BlojsomUtils.removeInitialSlash(_templatesDirectory));
            File globalTemplateDirectory = new File(templatePath.toString());

            TemplateLoader[] loaders;
            if (globalTemplateDirectory.exists()) {
                FileTemplateLoader fileTemplateLoaderGlobal = new FileTemplateLoader(globalTemplateDirectory);
                loaders = new TemplateLoader[] {fileTemplateLoaderUser, fileTemplateLoaderGlobal,
                                                classTemplateLoader};
            } else {
                loaders = new TemplateLoader[] {fileTemplateLoaderUser, classTemplateLoader};
            }

            MultiTemplateLoader multiTemplateLoader = new MultiTemplateLoader(loaders);
            freemarkerConfiguration.setTemplateLoader(multiTemplateLoader);
        } catch (IOException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }
        }
    }

    /**
     * Populate the context with the request and session attributes
     *
     * @param httpServletRequest Request
     * @param context            Context
     */
    private void populateContext(HttpServletRequest httpServletRequest, Map context) {
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

        // Configure FreeMarker with the loaded properties
        Configuration freemarkerConfiguration = Configuration.getDefaultConfiguration();
        if (_freemarkerProperties != null) {
            try {
                freemarkerConfiguration.setSettings(_freemarkerProperties);
            } catch (TemplateException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            }
        }

        populateContext(httpServletRequest, context);
        
        setTemplatePath(blog.getBlogId(), freemarkerConfiguration);

        BeansWrapper wrapper = new BeansWrapper();
        wrapper.setExposureLevel(BeansWrapper.EXPOSE_PROPERTIES_ONLY);
        wrapper.setSimpleMapWrapper(true);
        freemarkerConfiguration.setObjectWrapper(wrapper);

        Writer responseWriter = httpServletResponse.getWriter();
        String flavorTemplateForPage = null;
        String pageParameter = BlojsomUtils.getRequestValue(BlojsomConstants.PAGE_PARAM, httpServletRequest, true);

        if (pageParameter != null) {
            flavorTemplateForPage = BlojsomUtils.getTemplateForPage(flavorTemplate, pageParameter);
            if (_logger.isDebugEnabled()) {
                _logger.debug("Retrieved template for page: " + flavorTemplateForPage);
            }
        }

        if (flavorTemplateForPage != null) {
            // Try and look for the flavor page template for the individual user
            try {
                Template template = freemarkerConfiguration.getTemplate(flavorTemplateForPage);
                template.setEncoding(BlojsomConstants.UTF8);
                template.process(context, responseWriter);
            } catch (Exception e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                return;
            }

            if (_logger.isDebugEnabled()) {
                _logger.debug("Dispatched to flavor page template: " + flavorTemplateForPage);
            }
        } else {
            // Otherwise, fallback and look for the flavor template for the individual user
            try {
                Template template = freemarkerConfiguration.getTemplate(flavorTemplate);
                template.setEncoding(BlojsomConstants.UTF8);
                template.process(context, responseWriter);
            } catch (Exception e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                return;
            }

            if (_logger.isDebugEnabled()) {
                _logger.debug("Dispatched to flavor template: " + flavorTemplate);
            }
        }

        responseWriter.flush();
    }
}
