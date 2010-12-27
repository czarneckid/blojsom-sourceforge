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
package org.blojsom.plugin.velocity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.blojsom.blog.Blog;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.util.BlojsomConstants;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

/**
 * StandalongVelocityPlugin
 *
 * @author David Czarnecki
 * @version $Id: StandaloneVelocityPlugin.java,v 1.5 2008-07-07 19:54:25 czarneckid Exp $
 * @since blojsom 3.0
 */
public abstract class StandaloneVelocityPlugin implements Plugin {

    protected Log _logger = LogFactory.getLog(StandaloneVelocityPlugin.class);

    protected Properties _velocityProperties;
    protected ServletConfig _servletConfig;

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig        Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration {@link org.blojsom.blog.BlojsomConfiguration} information
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error initializing the plugin
     */
    public void init() throws PluginException {
    }

    /**
     * Set the Velocity properties
     *
     * @param velocityProperties Velocity properties
     */
    public void setVelocityProperties(Properties velocityProperties) {
        _velocityProperties = velocityProperties;
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
     * Merge a given template for the user with the appropriate context
     *
     * @param template Template
     * @param blog     {@link Blog} information
     * @param context  Context with objects for use in the template
     * @return Merged template or <code>null</code> if there was an error setting properties, loading the template, or merging
     *         the template
     */
    protected String mergeTemplate(String template, Blog blog, Map context) {
        ServletContext servletContext = _servletConfig.getServletContext();

        // Create the Velocity Engine
        VelocityEngine velocityEngine = new VelocityEngine();

        try {
            Properties updatedVelocityProperties = (Properties) _velocityProperties.clone();
            updatedVelocityProperties.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, servletContext.getRealPath("/WEB-INF/"
                + "blogs/" + blog.getBlogId() + "/templates/") + ", " + servletContext.getRealPath("/WEB-INF/templates/"));
            velocityEngine.init(updatedVelocityProperties);
        } catch (Exception e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            return null;
        }

        StringWriter writer = new StringWriter();

        // Setup the VelocityContext
        VelocityContext velocityContext = new VelocityContext(context);

        if (!velocityEngine.templateExists(template)) {
            if (_logger.isErrorEnabled()) {
                _logger.error("Could not find template for user: " + template);
            }

            return null;
        } else {
            try {
                velocityEngine.mergeTemplate(template, BlojsomConstants.UTF8, velocityContext, writer);
            } catch (Exception e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                return null;
            }
        }

        if (_logger.isDebugEnabled()) {
            _logger.debug("Merged template: " + template);
        }

        return writer.toString();
    }
}