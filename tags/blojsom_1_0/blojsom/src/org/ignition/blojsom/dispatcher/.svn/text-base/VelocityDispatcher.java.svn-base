/**
 * Copyright (c) 2003, David A. Czarnecki
 * All rights reserved.
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
package org.ignition.blojsom.dispatcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import java.util.Map;
import java.util.Properties;
import java.io.*;

/**
 * VelocityDispatcher
 *
 * @author David Czarnecki (czarneckid@acm.org)
 */
public class VelocityDispatcher implements GenericDispatcher {

    private final static String BLOG_VELOCITY_PROPERTIES_IP = "velocity-properties";

    private Log _logger = LogFactory.getLog(VelocityDispatcher.class);

    /**
     * Create a new VelocityDispatcher
     */
    public VelocityDispatcher() {
    }

    /**
     * Initialization method for blojsom dispatchers
     *
     * @param servletConfig ServletConfig for obtaining any initialization parameters
     */
    public void init(ServletConfig servletConfig) {
        String velocityConfiguration = servletConfig.getInitParameter(BLOG_VELOCITY_PROPERTIES_IP);
        Properties velocityProperties = new Properties();
        InputStream is = servletConfig.getServletContext().getResourceAsStream(velocityConfiguration);
        try {
            velocityProperties.load(is);
            Velocity.init(velocityProperties);
        } catch (Exception e) {
            _logger.error(e);
        }
        _logger.debug("Initialized Velocity dispatcher");
    }

    /**
     * Dispatch a request and response. A context map is provided for the BlojsomServlet to pass
     * any required information for use by the dispatcher. The dispatcher is also
     * provided with the template for the requested flavor along with the content type for the
     * specific flavor. For example, the RSS flavor uses text/xml as its content type.
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @param context Context map
     * @param flavorTemplate Template to dispatch to for the requested flavor
     * @param flavorContentType Content type for the requested flavor
     * @throws IOException If there is an exception during IO
     * @throws ServletException If there is an exception in dispatching the request
     */
    public void dispatch(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map context, String flavorTemplate, String flavorContentType)
            throws IOException, ServletException {
        httpServletResponse.setContentType(flavorContentType);

        // Setup the VelocityContext
        OutputStreamWriter osw = new OutputStreamWriter(httpServletResponse.getOutputStream(), "UTF-8");
        VelocityContext velocityContext = new VelocityContext(context);
        try {
            Velocity.mergeTemplate(flavorTemplate, "UTF-8", velocityContext, osw);
        } catch (Exception e) {
            _logger.error(e);
        }

        osw.flush();
    }
}