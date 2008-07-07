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
package org.blojsom.dispatcher.bsf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.bsf.BSFManager;
import org.apache.bsf.BSFException;
import org.apache.velocity.util.EnumerationIterator;
import org.blojsom.BlojsomException;
import org.blojsom.blog.Blog;
import org.blojsom.dispatcher.Dispatcher;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * BSFDispatcher
 *
 * @author David Czarnecki
 * @version $Id: BSFDispatcher.java,v 1.4 2008-07-07 19:54:28 czarneckid Exp $
 * @since blojsom 3.2
 */
public class BSFDispatcher implements Dispatcher {

    private Log _logger = LogFactory.getLog(BSFDispatcher.class);

    private ServletConfig _servletConfig;
    private Properties _blojsomProperties;

    private String _templatesDirectory;
    private String _blogsDirectory;
    private Map _scriptingEngines;

    /**
     * Default constructor
     */
    public BSFDispatcher() {
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
     * Set the scripting engines for the engine
     *
     * @param scriptingEngines Scripting engines (key = language, value = FQ engine class name)
     */
    public void setScriptingEngines(Map scriptingEngines) {
        _scriptingEngines = scriptingEngines;
    }

    /**
     * Initialization method for blojsom dispatchers
     *
     * @throws org.blojsom.BlojsomException If there is an error initializing the dispatcher
     */
    public void init() throws BlojsomException {
        _templatesDirectory = _blojsomProperties.getProperty(BlojsomConstants.TEMPLATES_DIRECTORY_IP, BlojsomConstants.DEFAULT_TEMPLATES_DIRECTORY);
        _blogsDirectory = _blojsomProperties.getProperty(BlojsomConstants.BLOGS_DIRECTORY_IP, BlojsomConstants.DEFAULT_BLOGS_DIRECTORY);

        if (_scriptingEngines != null && _scriptingEngines.size() > 0) {
            Iterator scriptingEngineIterator = _scriptingEngines.keySet().iterator();
            while (scriptingEngineIterator.hasNext()) {
                String engine = (String) scriptingEngineIterator.next();

                BSFManager.registerScriptingEngine(engine, (String) _scriptingEngines.get(engine), null);
            }
        }
    }

    /**
     * Read in template date
     *
     * @param reader Reader attached to the template
     * @return Template data
     */
    private String readTemplate(Reader reader) {
        BufferedReader br = new BufferedReader(reader);
        StringBuffer template = new StringBuffer(8192);
        String line;

        try {
            while ((line = br.readLine()) != null) {
                template.append(line).append(BlojsomConstants.LINE_SEPARATOR);
            }
        } catch (IOException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }
        }

        return template.toString();
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

        ServletContext servletContext = _servletConfig.getServletContext();

        if (!flavorTemplate.startsWith("/")) {
            flavorTemplate = '/' + flavorTemplate;
        }

        String flavorTemplateForPage = null;
        String pageParameter = BlojsomUtils.getRequestValue(BlojsomConstants.PAGE_PARAM, httpServletRequest, true);

        if (pageParameter != null) {
            flavorTemplateForPage = BlojsomUtils.getTemplateForPage(flavorTemplate, pageParameter);
            if (_logger.isDebugEnabled()) {
                _logger.debug("Retrieved template for page: " + flavorTemplateForPage);
            }
        }

        Writer responseWriter = httpServletResponse.getWriter();

        // Get the template language from the extension
        String templateLanguage = BlojsomUtils.getFileExtension(flavorTemplate);

        BSFManager manager = new BSFManager();

        // Populate the script context with context attributes from the blog
        populateContext(httpServletRequest, context);
        Iterator contextIterator = context.keySet().iterator();
        String contextKey;
        try {
            while (contextIterator.hasNext()) {
                contextKey = (String) contextIterator.next();
                manager.declareBean(contextKey, context.get(contextKey), context.get(contextKey).getClass());
            }

            manager.declareBean("out", responseWriter, Writer.class);
        } catch (BSFException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }
        }

        // Try and look for the original flavor template with page for the individual user
        if (flavorTemplateForPage != null) {
            String templateToLoad = BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + _blogsDirectory + blog.getBlogId() + _templatesDirectory + BlojsomUtils.removeInitialSlash(flavorTemplateForPage);

            if (servletContext.getResource(templateToLoad) != null) {
                InputStreamReader isr = new InputStreamReader(servletContext.getResourceAsStream(templateToLoad), BlojsomConstants.UTF8);

                String templateData = readTemplate(isr);
                try {
                    manager.exec(templateLanguage, "(blojsom)", 1, 1, templateData);
                } catch (BSFException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                }

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Dispatched to flavor page template for user: " + templateToLoad);
                }

                return;
            } else {
                templateToLoad = BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + BlojsomUtils.removeInitialSlash(_templatesDirectory) + BlojsomUtils.removeInitialSlash(flavorTemplateForPage);

                if (servletContext.getResource(templateToLoad) != null) {
                    // Otherwise, fallback and look for the flavor template with page without including any user information
                    InputStreamReader isr = new InputStreamReader(servletContext.getResourceAsStream(templateToLoad), BlojsomConstants.UTF8);

                    String templateData = readTemplate(isr);
                    try {
                        manager.exec(templateLanguage, "(blojsom)", 1, 1, templateData);
                    } catch (BSFException e) {
                        if (_logger.isErrorEnabled()) {
                            _logger.error(e);
                        }
                    }

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Dispatched to flavor page template for user: " + templateToLoad);
                    }

                    return;
                } else {
                    if (_logger.isErrorEnabled()) {
                        _logger.error("Unable to dispatch to flavor page template: " + templateToLoad);
                    }
                }
            }
        } else {
            // Otherwise, fallback and look for the flavor template for the individual user
            String templateToLoad = BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + _blogsDirectory + blog.getBlogId() + _templatesDirectory + BlojsomUtils.removeInitialSlash(flavorTemplate);

            if (servletContext.getResource(templateToLoad) != null) {
                InputStreamReader isr = new InputStreamReader(servletContext.getResourceAsStream(templateToLoad), BlojsomConstants.UTF8);

                String templateData = readTemplate(isr);
                try {
                    manager.exec(templateLanguage, "(blojsom)", 1, 1, templateData);
                } catch (BSFException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                }

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Dispatched to flavor template for user: " + templateToLoad);
                }

                return;
            } else {
                templateToLoad = BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + BlojsomUtils.removeInitialSlash(_templatesDirectory) + BlojsomUtils.removeInitialSlash(flavorTemplate);

                if (servletContext.getResource(templateToLoad) != null) {
                    // Otherwise, fallback and look for the flavor template without including any user information
                    InputStreamReader isr = new InputStreamReader(servletContext.getResourceAsStream(templateToLoad), BlojsomConstants.UTF8);

                    String templateData = readTemplate(isr);
                    try {
                        manager.exec(templateLanguage, "(blojsom)", 1, 1, templateData);
                    } catch (BSFException e) {
                        if (_logger.isErrorEnabled()) {
                            _logger.error(e);
                        }
                    }

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Dispatched to flavor template: " + templateToLoad);
                    }

                    return;
                } else {
                    if (_logger.isErrorEnabled()) {
                        _logger.error("Unable to dispatch to flavor template: " + templateToLoad);
                    }
                }
            }
        }

        responseWriter.flush();
    }
}

