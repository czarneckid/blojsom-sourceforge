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
package org.blojsom.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.BlojsomException;
import org.blojsom.blog.BlogUser;
import org.blojsom.blog.BlojsomConfiguration;
import org.blojsom.blog.BlojsomConfigurationException;
import org.blojsom.fetcher.BlojsomFetcher;
import org.blojsom.fetcher.BlojsomFetcherException;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * BlojsomBaseServlet
 *
 * @author David Czarnecki
 * @since blojsom 2.0
 * @version $Id: BlojsomBaseServlet.java,v 1.5 2003-08-22 04:41:10 czarneckid Exp $
 */
public class BlojsomBaseServlet extends HttpServlet implements BlojsomConstants {

    private Log _logger = LogFactory.getLog(BlojsomBaseServlet.class);

    protected String _baseConfigurationDirectory;
    protected String _defaultUser;
    protected Map _users;
    protected BlojsomFetcher _fetcher;
    protected BlojsomConfiguration _blojsomConfiguration;


    /**
     * Configure the {@link BlojsomFetcher} that will be used to fetch categories and
     * entries
     *
     * @param servletConfig Servlet configuration information
     * @param blojsomConfiguration blojsom properties
     * @throws javax.servlet.ServletException If the {@link BlojsomFetcher} class could not be loaded and/or initialized
     */
    protected void configureFetcher(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws ServletException {
        String fetcherClassName = blojsomConfiguration.getFetcherClass();
        try {
            Class fetcherClass = Class.forName(fetcherClassName);
            _fetcher = (BlojsomFetcher) fetcherClass.newInstance();
            _fetcher.init(servletConfig);
            _logger.info("Added blojsom fetcher: " + fetcherClassName);
        } catch (ClassNotFoundException e) {
            _logger.error(e);
            throw new ServletException(e);
        } catch (InstantiationException e) {
            _logger.error(e);
            throw new ServletException(e);
        } catch (IllegalAccessException e) {
            _logger.error(e);
            throw new ServletException(e);
        } catch (BlojsomFetcherException e) {
            _logger.error(e);
            throw new ServletException(e);
        }
    }

    /**
     * Configure the global configuration information and initialize the users for this blog
     *
     * @param servletConfig Servlet configuration information
     */
    protected void configureBlojsom(ServletConfig servletConfig) throws ServletException {
        try {
            // Configure blojsom's properties
            Properties configurationProperties = BlojsomUtils.loadProperties(servletConfig, BLOJSOM_CONFIGURATION_IP, true);

            _blojsomConfiguration = new BlojsomConfiguration(servletConfig, BlojsomUtils.propertiesToMap(configurationProperties));
            _baseConfigurationDirectory = _blojsomConfiguration.getBaseConfigurationDirectory();
            _users = _blojsomConfiguration.getBlogUsers();
            _defaultUser = _blojsomConfiguration.getDefaultUser();

            // Configure the fetcher for use by this blog
            configureFetcher(servletConfig, _blojsomConfiguration);
        } catch (BlojsomConfigurationException e) {
            _logger.error(e);
            throw new ServletException(e);
        } catch (BlojsomException e) {
            _logger.error(e);
            throw new ServletException(e);
        }
    }

    /**
     * Configure the authorization table (user id's and and passwords) for each user
     *
     * @param servletConfig Servlet configuration information
     */
    protected void configureAuthorization(ServletConfig servletConfig) throws ServletException {
        String authorizationConfiguration = servletConfig.getInitParameter(BLOG_AUTHORIZATION_IP);
        if (authorizationConfiguration == null || "".equals(authorizationConfiguration)) {
            _logger.error("No authorization configuration file specified");
            throw new ServletException("No authorization configuration file specified");
        }

        // Load the authorization properties files for the individual users
        Iterator usersIterator = _users.keySet().iterator();
        BlogUser blogUser;
        Properties authorizationProperties;
        while (usersIterator.hasNext()) {
            String user = (String) usersIterator.next();
            blogUser = (BlogUser) _users.get(user);

            InputStream is = servletConfig.getServletContext().getResourceAsStream(_baseConfigurationDirectory + user + '/' + authorizationConfiguration);
            authorizationProperties = new Properties();
            try {
                authorizationProperties.load(is);
                is.close();
                blogUser.getBlog().setAuthorization(BlojsomUtils.propertiesToMap(authorizationProperties));
                _users.put(user, blogUser);
                _logger.debug("Added authorization information for user: " + user);
            } catch (IOException e) {
                _logger.error(e);
                throw new ServletException(e);
            }
        }
    }
}
