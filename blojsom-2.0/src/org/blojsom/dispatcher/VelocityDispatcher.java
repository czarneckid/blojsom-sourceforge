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
package org.blojsom.dispatcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.blojsom.BlojsomException;
import org.blojsom.blog.BlogUser;
import org.blojsom.blog.BlojsomConfiguration;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;
import java.util.Properties;

/**
 * VelocityDispatcher
 *
 * @author David Czarnecki
 * @version $Id: VelocityDispatcher.java,v 1.4 2003-08-22 04:41:55 czarneckid Exp $
 */
public class VelocityDispatcher implements GenericDispatcher {

    private final static String BLOG_VELOCITY_PROPERTIES_IP = "velocity-properties";

    private Log _logger = LogFactory.getLog(VelocityDispatcher.class);
    private String _baseConfigurationDirectory;
    private String _templatesDirectory;

    /**
     * Create a new VelocityDispatcher
     */
    public VelocityDispatcher() {
    }

    /**
     * Initialization method for blojsom dispatchers
     *
     * @param servletConfig ServletConfig for obtaining any initialization parameters
     * @param blojsomConfiguration BlojsomConfiguration for blojsom-specific configuration information
     * @throws BlojsomException If there is an error initializing the dispatcher
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws BlojsomException {
        _baseConfigurationDirectory = blojsomConfiguration.getBaseConfigurationDirectory();
        _templatesDirectory = blojsomConfiguration.getBlojsomPropertyAsString(TEMPLATES_DIRECTORY_IP);
        if (_templatesDirectory == null || "".equals(_templatesDirectory)) {
            _templatesDirectory = DEFAULT_TEMPLATES_DIRECTORY;
        }
        _logger.debug("Using templates directory: " + _templatesDirectory);

        String velocityConfiguration = servletConfig.getInitParameter(BLOG_VELOCITY_PROPERTIES_IP);
        Properties velocityProperties = new Properties();
        InputStream is = servletConfig.getServletContext().getResourceAsStream(velocityConfiguration);

        try {
            velocityProperties.load(is);
            Velocity.init(velocityProperties);
            is.close();
        } catch (Exception e) {
            _logger.error(e);
        }

        _logger.debug("Initialized Velocity dispatcher");
    }

    /**
     * Dispatch a request and response. A context map is provided for the BlojsomServlet to pass
     * any required information for use by the dispatcher. The dispatcher is also
     * provided with the template for the requested flavor along with the content type for the
     * specific flavor.
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @param user {@link BlogUser} instance
     * @param context Context map
     * @param flavorTemplate Template to dispatch to for the requested flavor
     * @param flavorContentType Content type for the requested flavor
     * @throws IOException If there is an exception during IO
     * @throws ServletException If there is an exception in dispatching the request
     */
    public void dispatch(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse,
                         BlogUser user,
                         Map context,
                         String flavorTemplate,
                         String flavorContentType)
            throws IOException, ServletException {
        httpServletResponse.setContentType(flavorContentType);

        StringWriter sw = new StringWriter();
        String flavorTemplateForPage = null;

        if (BlojsomUtils.getRequestValue(PAGE_PARAM, httpServletRequest) != null) {
            flavorTemplateForPage = BlojsomUtils.getTemplateForPage(flavorTemplate, BlojsomUtils.getRequestValue(PAGE_PARAM, httpServletRequest));
            _logger.debug("Retrieved template for page: " + flavorTemplateForPage);
        }

        // Setup the VelocityContext
        VelocityContext velocityContext = new VelocityContext(context);

        if (flavorTemplateForPage != null) {
            // Try and look for the flavor page template for the individual user
            String templateToLoad = _baseConfigurationDirectory + user.getId() + _templatesDirectory + flavorTemplateForPage;
            if (!Velocity.templateExists(templateToLoad)) {
                _logger.error("Could not find flavor page template for user: " + templateToLoad);
                templateToLoad = flavorTemplateForPage;
                if (!Velocity.templateExists(templateToLoad)) {
                    _logger.error("Could not find default flavor page template: " + templateToLoad);
                    return;
                }
            }

            try {
                Velocity.mergeTemplate(templateToLoad, UTF8, velocityContext, sw);
            } catch (Exception e) {
                _logger.error(e);
            }
            _logger.debug("Dispatched to flavor page template: " + templateToLoad);
        } else {
            // Otherwise, fallback and look for the flavor template for the individual user
            String templateToLoad = _baseConfigurationDirectory + user.getId() + _templatesDirectory + flavorTemplate;
            if (!Velocity.templateExists(templateToLoad)) {
                _logger.error("Could not find flavor template for user: " + templateToLoad);
                templateToLoad = flavorTemplate;
                if (!Velocity.templateExists(templateToLoad)) {
                    _logger.error("Could not find default flavor template: " + templateToLoad);
                    return;
                }
            }

            try {
                Velocity.mergeTemplate(templateToLoad, UTF8, velocityContext, sw);
            } catch (Exception e) {
                _logger.error(e);
            }
            _logger.debug("Dispatched to flavor template: " + templateToLoad);
        }

        // We need that content length, especially for RSS feeds
        String content = sw.toString();
        byte[] contentBytes = null;
        try {
            contentBytes = content.getBytes(UTF8);
        } catch (UnsupportedEncodingException e) {
            _logger.error(e);
        }
        httpServletResponse.addIntHeader("Content-Length", (contentBytes == null ? content.length() : contentBytes.length));

        OutputStreamWriter osw = new OutputStreamWriter(httpServletResponse.getOutputStream(), UTF8);
        osw.write(content);
        osw.flush();
    }
}