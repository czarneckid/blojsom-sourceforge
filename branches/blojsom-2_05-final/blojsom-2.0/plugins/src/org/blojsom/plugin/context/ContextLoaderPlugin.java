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
package org.blojsom.plugin.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;
import org.blojsom.blog.BlojsomConfiguration;
import org.blojsom.plugin.BlojsomPlugin;
import org.blojsom.plugin.BlojsomPluginException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * ContextLoaderPlugin
 * 
 * @version $Id: ContextLoaderPlugin.java,v 1.1 2003-11-30 21:12:45 intabulas Exp $
 */
public class ContextLoaderPlugin implements BlojsomPlugin {

    /** web.xml init-param name */
    private static final String CONTEXT_CONFIG_IP = "plugin-contextloader";

    /** Extension of a Properties File */
    private static final String EXTENSION_PROPERTIES = ".properties";


    private Map _contextManager;

    /** Logger instance */
    private Log _logger = LogFactory.getLog(ContextLoaderPlugin.class);

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     * 
     * @param servletConfig        Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration {@link org.blojsom.blog.BlojsomConfiguration} information
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws BlojsomPluginException {
        String contextConfiguration = servletConfig.getInitParameter(CONTEXT_CONFIG_IP);
        if (contextConfiguration == null || "".equals(contextConfiguration)) {
            throw new BlojsomPluginException("No value given for: " + CONTEXT_CONFIG_IP + " configuration parameter");
        }

        String[] users = blojsomConfiguration.getBlojsomUsers();
        _contextManager = new HashMap(users.length);

        // For each user, try to load the contextloader properties file
        for (int i = 0; i < users.length; i++) {
            String user = users[i];
            Properties contextProperties = new Properties();
            String configurationFile = blojsomConfiguration.getBaseConfigurationDirectory() + user + '/' + contextConfiguration;
            InputStream is = servletConfig.getServletContext().getResourceAsStream(configurationFile);
            if (is == null) {
                _logger.info("No context loder configuration file found: " + configurationFile);
            } else {
                try {

                    _logger.info("Loading " + configurationFile);
                    // Load the properties
                    contextProperties.load(is);
                    is.close();

                    Map contextMap = new HashMap(contextProperties.size());

                    Iterator ksi = contextProperties.keySet().iterator();
                    while (ksi.hasNext()) {
                        String key = (String) ksi.next();
                        String value = contextProperties.getProperty(key);

                        if (value.endsWith(EXTENSION_PROPERTIES)) {
                            String filename = blojsomConfiguration.getBaseConfigurationDirectory() + user + '/' + value;
                            _logger.info("Processing " + filename);
                            InputStream loaderis = servletConfig.getServletContext().getResourceAsStream(filename);
                            Map propertiesContent = loadPropertiesFile(loaderis);
                            if (propertiesContent != null) {
                                contextMap.put(key, propertiesContent);
                            }
                        }


                    }

                    if (contextMap.size() > 0) {
                        _contextManager.put(user, contextMap);
                    }
                } catch (IOException e) {
                    _logger.error(e.getMessage(),e);
                    throw new BlojsomPluginException(e);
                }
            }
        }


    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     * 
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws BlojsomPluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     * 
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error in finalizing this plugin
     */
    public void destroy() throws BlojsomPluginException {
    }

    /**
     * Process the blog entries
     * 
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param user                {@link org.blojsom.blog.BlogUser} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlogUser user, Map context, BlogEntry[] entries) throws BlojsomPluginException {

        String userId = user.getId();
        Map usersContext = (Map) _contextManager.get(userId);
        if (usersContext != null) {
            Iterator ksi = usersContext.keySet().iterator();
            while (ksi.hasNext()) {
                String key = (String) ksi.next();
                Object value = usersContext.get(key);
                context.put(key, value);
            }
        }

        return entries;
    }


    /**
     *
     */
    private Map loadPropertiesFile(InputStream is) {
        Map result = null;
        Properties contextproperties = new Properties();

        try {
            contextproperties.load(is);
            is.close();

            result = new HashMap(contextproperties.size());
            Iterator ksi = contextproperties.keySet().iterator();
            while (ksi.hasNext()) {
                String key = (String) ksi.next();
                String value = contextproperties.getProperty(key);
                result.put(key, value);
            }
        } catch (IOException e) {
            _logger.error(e);
        }

        return result;
    }

}
