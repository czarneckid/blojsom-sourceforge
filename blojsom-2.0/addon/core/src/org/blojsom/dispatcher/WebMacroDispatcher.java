/**
 * Copyright (c) 2003-2005, David A. Czarnecki
 * All rights reserved.
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
package org.blojsom.dispatcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.util.EnumerationIterator;
import org.blojsom.BlojsomException;
import org.blojsom.blog.BlogUser;
import org.blojsom.blog.BlojsomConfiguration;
import org.blojsom.util.BlojsomUtils;
import org.webmacro.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * WebMacro dispatcher
 *
 * @author David Czarnecki
 * @since blojsom 2.28
 * @version $Id: WebMacroDispatcher.java,v 1.3 2005-11-05 18:02:08 czarneckid Exp $
 */
public class WebMacroDispatcher implements BlojsomDispatcher {

    private static final String BLOG_WEBMACRO_PROPERTIES_IP = "webmacro-properties";
    private static final String DEFAULT_WEBMACRO_PROPERTIES = "/WEB-INF/WebMacro.properties";

    private Log _logger = LogFactory.getLog(WebMacroDispatcher.class);

    private String _templatesDirectory;
    private String _installationDirectory;
    private String _baseConfigurationDirectory;

    private Properties _webMacroProperties;


    /**
     * Construct a new instance of the WebMacro dispatcher
     */
    public WebMacroDispatcher() {
    }

    /**
     * Initialization method for blojsom dispatchers
     *
     * @param servletConfig        ServletConfig for obtaining any initialization parameters
     * @param blojsomConfiguration BlojsomConfiguration for blojsom-specific configuration information
     * @throws org.blojsom.BlojsomException If there is an error initializing the dispatcher
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws BlojsomException {
        _baseConfigurationDirectory = blojsomConfiguration.getBaseConfigurationDirectory();
        _templatesDirectory = blojsomConfiguration.getTemplatesDirectory();
        _installationDirectory = blojsomConfiguration.getInstallationDirectory();

        _logger.debug("Using templates directory: " + _templatesDirectory);

        String webMacroConfiguration = servletConfig.getInitParameter(BLOG_WEBMACRO_PROPERTIES_IP);
        if (BlojsomUtils.checkNullOrBlank(webMacroConfiguration)) {
            webMacroConfiguration = DEFAULT_WEBMACRO_PROPERTIES;
        }

        _webMacroProperties = new Properties();
        InputStream is = servletConfig.getServletContext().getResourceAsStream(webMacroConfiguration);

        try {
            _webMacroProperties.load(is);
            is.close();
        } catch (Exception e) {
            _logger.error(e);
        }

        _logger.debug("Initialized WebMacro dispatcher");
    }

    /**
     * Return a path appropriate for the WebMacro file resource loader for a given blog
     *
     * @param blogID Blog ID
     * @return blojsom installation directory + base configuration directory + Blog ID + templates directory
     */
    protected String getWebMacroTemplatePathForBlog(String blogID) {
        StringBuffer templatePathForBlog = new StringBuffer();
        templatePathForBlog.append(_installationDirectory);
        templatePathForBlog.append(BlojsomUtils.removeInitialSlash(_baseConfigurationDirectory));
        templatePathForBlog.append(blogID).append("/");
        templatePathForBlog.append(BlojsomUtils.removeInitialSlash(_templatesDirectory));

        return templatePathForBlog.toString();
    }

    /**
     * Return a path appropriate for the WebMacro file resource loader
     *
     * @return blojsom installation directory + base configuration directory + templates directory
     */
    protected String getWebMacroGlobalTemplatePath() {
        StringBuffer templatePath = new StringBuffer();

        templatePath.append(_installationDirectory);
        templatePath.append(BlojsomUtils.removeInitialSlash(_baseConfigurationDirectory));
        templatePath.append(BlojsomUtils.removeInitialSlash(_templatesDirectory));

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
     * @param user                {@link org.blojsom.blog.BlogUser} instance
     * @param context             Context map
     * @param flavorTemplate      Template to dispatch to for the requested flavor
     * @param flavorContentType   Content type for the requested flavor
     * @throws java.io.IOException            If there is an exception during IO
     * @throws javax.servlet.ServletException If there is an exception in dispatching the request
     */
    public void dispatch(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlogUser user, Map context, String flavorTemplate, String flavorContentType) throws IOException, ServletException {
        httpServletResponse.setContentType(flavorContentType);

        try {
            Properties updatedWebMacroProperties = (Properties) _webMacroProperties.clone();
            updatedWebMacroProperties.setProperty("TemplateLoaderPath.1", "file:" + getWebMacroTemplatePathForBlog(user.getId()));
            updatedWebMacroProperties.setProperty("TemplateLoaderPath.2", "file:" + getWebMacroGlobalTemplatePath());

            WM wm = new WM(updatedWebMacroProperties);
            Context wmContext = wm.getContext();
            populateWebMacroContext(httpServletRequest, context);
            wmContext.setMap(context);

            String flavorTemplateForPage = null;
            String pageParameter = BlojsomUtils.getRequestValue(PAGE_PARAM, httpServletRequest, true);

            if (pageParameter != null) {
                flavorTemplateForPage = BlojsomUtils.getTemplateForPage(flavorTemplate, pageParameter);
                _logger.debug("Retrieved template for page: " + flavorTemplateForPage);
            }

            if (BlojsomUtils.checkNullOrBlank(flavorTemplateForPage)) {
                try {
                    wm.writeTemplate(flavorTemplate, httpServletResponse.getOutputStream(), wmContext);
                } catch (ResourceException e) {
                    _logger.error(e);
                } catch (PropertyException e) {
                    _logger.error(e);
                }

                _logger.debug("Dispatched to flavor template: " + flavorTemplate);
            } else {
                try {
                    wm.writeTemplate(flavorTemplateForPage, httpServletResponse.getOutputStream(), wmContext);
                } catch (ResourceException e) {
                    _logger.error(e);
                } catch (PropertyException e) {
                    _logger.error(e);
                }

                _logger.debug("Dispatched to flavor page template: " + flavorTemplateForPage);

            }

            httpServletResponse.getOutputStream().flush();
        } catch (InitException e) {
            _logger.error(e);
        }
    }
}
