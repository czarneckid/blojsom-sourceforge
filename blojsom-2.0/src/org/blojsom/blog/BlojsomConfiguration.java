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
package org.blojsom.blog;

import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.BlojsomConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * BlojsomConfiguration
 *
 * @author David Czarnecki
 * @since blojsom 2.0
 * @version $Id: BlojsomConfiguration.java,v 1.6 2003-09-12 02:08:59 czarneckid Exp $
 */
public class BlojsomConfiguration implements BlojsomConstants {

    private Log _logger = LogFactory.getLog(BlojsomConfiguration.class);

    private String _blojsomUsers;
    private String _defaultUser;
    private String _baseConfigurationDirectory;
    private String _fetcherClass;
    private String _installationDirectory;

    private Map _blogUsers;
    private Map _blojsomConfiguration;

    /**
     * Initialize the BlojsomConfiguration object
     *
     * @param servletConfig Servlet configuration information
     * @param blojsomConfiguration Map of loaded blojsom properties
     */
    public BlojsomConfiguration(ServletConfig servletConfig, Map blojsomConfiguration) throws BlojsomConfigurationException {
        _blojsomConfiguration = blojsomConfiguration;

        _installationDirectory = servletConfig.getServletContext().getRealPath("/");
        if (_installationDirectory == null || "".equals(_installationDirectory)) {
            _logger.error("No installation directory set for blojsom");
            throw new BlojsomConfigurationException("No installation directory set for blojsom");
        } else {
            if (!_installationDirectory.endsWith("/")) {
                _installationDirectory += "/";
            }
        }
        _logger.debug("Using installation directory" + _installationDirectory);

        _baseConfigurationDirectory = getBlojsomPropertyAsString(BLOJSOM_CONFIGURATION_BASE_DIRECTORY_IP);
        if (_baseConfigurationDirectory == null || "".equals(_baseConfigurationDirectory)) {
            _baseConfigurationDirectory = BLOJSOM_DEFAULT_CONFIGURATION_BASE_DIRECTORY;
        } else {
            if (!_baseConfigurationDirectory.startsWith("/")) {
                _baseConfigurationDirectory = '/' + _baseConfigurationDirectory;
            }
            if (!_baseConfigurationDirectory.endsWith("/")) {
                _baseConfigurationDirectory += "/";
            }
        }
        _logger.debug("Using base configuration directory: " + _baseConfigurationDirectory);

        _blojsomUsers = getBlojsomPropertyAsString(BLOJSOM_USERS_IP);
        String[] users = BlojsomUtils.parseCommaList(_blojsomUsers);
        InputStream is;
        if (users.length == 0) {
            _logger.error("No users defined for this blojsom blog");
            throw new BlojsomConfigurationException("No users defined for this blojsom blog");
        } else {
            _blogUsers = new HashMap(users.length);
            for (int i = 0; i < users.length; i++) {
                String user = users[i];
                BlogUser blogUser = new BlogUser();
                blogUser.setId(user);

                Properties userProperties = new Properties();
                is = servletConfig.getServletContext().getResourceAsStream(_baseConfigurationDirectory + user + '/' + BLOG_DEFAULT_PROPERTIES);
                try {
                    userProperties.load(is);
                    is.close();
                } catch (IOException e) {
                    _logger.error(e);
                    throw new BlojsomConfigurationException(e);
                }

                Blog userBlog = new Blog(userProperties);
                blogUser.setBlog(userBlog);

                _blogUsers.put(user, blogUser);
                _logger.debug("Added blojsom user: " + blogUser.getId());
            }

            // Determine and set the default user
            String defaultUser = getBlojsomPropertyAsString(BLOJSOM_DEFAULT_USER_IP);
            if (defaultUser == null || "".equals(defaultUser)) {
                _logger.error("No default user defined in configuration property: " + BLOJSOM_DEFAULT_USER_IP);
                throw new BlojsomConfigurationException("No default user defined in configuration property: " + BLOJSOM_DEFAULT_USER_IP);
            }

            if (!_blogUsers.containsKey(defaultUser)) {
                _logger.error("Default user does not match any of the registered blojsom users: " + defaultUser);
                throw new BlojsomConfigurationException("Default user does not match any of the registered blojsom users: " + defaultUser);
            }

            _defaultUser = defaultUser;
            _logger.debug("blojsom default user: " + _defaultUser);

            _fetcherClass = getBlojsomPropertyAsString(BLOJSOM_FETCHER_IP);
            if ((_fetcherClass == null) || "".equals(_fetcherClass)) {
                _fetcherClass = BLOG_DEFAULT_FETCHER;
            }
        }
    }

    /**
     * Returns an unmodifiable map of the blojsom configuration properties
     *
     * @return Unmodifiable map of the blojsom configuration properties
     */
    public Map getBlojsomConfiguration() {
        return Collections.unmodifiableMap(_blojsomConfiguration);
    }

    /**
     * Retrieve a blojsom property as a string
     *
     * @param propertyKey Property key
     * @return Value of blojsom property as a string or <code>null</code> if no property key is found
     */
    public String getBlojsomPropertyAsString(String propertyKey) {
        if (_blojsomConfiguration.containsKey(propertyKey)) {
            return (String) _blojsomConfiguration.get(propertyKey);
        }

        return null;
    }

    /**
     * Return a blojsom configuration property
     *
     * @param propertyKey Property key
     * @return Value of blojsom property
     */
    public Object getBlojsomProperty(String propertyKey) {
        return _blojsomConfiguration.get(propertyKey);
    }

    /**
     * Get the default user for this blojsom instance
     *
     * @return Default user
     */
    public String getDefaultUser() {
        return _defaultUser;
    }

    /**
     * Get the base directory for obtaining configuration information
     *
     * @return Configuration base directory (e.g. /WEB-INF)
     */
    public String getBaseConfigurationDirectory() {
        return _baseConfigurationDirectory;
    }

    /**
     * Get the classname of the fetcher used for this blojsom instance
     *
     * @return Fetcher classname
     */
    public String getFetcherClass() {
        return _fetcherClass;
    }

    /**
     * Get the installation directory for blojsom. This is the directory where the blojsom WAR file will
     * be unpacked.
     *
     * @since blojsom 2.01
     * @return Installation directory
     */
    public String getInstallationDirectory() {
        return _installationDirectory;
    }

    /**
     * Get the list of users for this blojsom instance returned as a String[]
     *
     * @return List of users as a String[]
     */
    public String[] getBlojsomUsers() {
        return BlojsomUtils.parseCommaList(_blojsomUsers);
    }

    /**
     * Get a map of the {@link BlogUser} objects
     *
     * @return Map of {@link BlogUser} objects
     */
    public Map getBlogUsers() {
        return _blogUsers;
    }
}
