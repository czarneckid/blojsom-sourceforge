/**
 * Copyright (c) 2003-2006, David A. Czarnecki
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
package org.blojsom.upgrade;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.util.BlojsomConstants;
//import org.blojsom.util.BlojsomUtils;
import org.blojsom.blog.Blog;
import org.blojsom.blog.database.DatabaseBlog;
import org.blojsom2.util.BlojsomProperties;
import org.blojsom2.blog.BlojsomConfiguration;
import org.blojsom2.blog.BlojsomConfigurationException;
import org.blojsom2.blog.BlogUser;
import org.blojsom2.fetcher.BlojsomFetcher;
import org.blojsom2.fetcher.BlojsomFetcherException;
import org.blojsom2.BlojsomException;
//import org.blojsom2.util.BlojsomUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.InvalidPropertyException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Iterator;
import java.util.HashMap;

/**
 * Utility class to migrate from blojsom 2 to blojsom 3
 *
 * @author David Czarnecki
 * @since blojsom 3
 * @version $Id: Blojsom2ToBlojsom3Utility.java,v 1.3 2006-09-27 19:40:42 czarneckid Exp $
 */
public class Blojsom2ToBlojsom3Utility {

    private static Log _logger = LogFactory.getLog(Blojsom2ToBlojsom3Utility.class);

    private ServletConfig _servletConfig;
    private Fetcher _fetcher;

    private BlojsomFetcher _blojsom2Fetcher;
    private BlojsomConfiguration _blojsomConfiguration;
    private BlojsomProperties _blojsomProperties;

    private String _blojsom2Path;
    private String _blojsom3Path;

    /**
     * Construct a new instance of the blojsom 2 to blojsom 3 utility
     */
    public Blojsom2ToBlojsom3Utility() {
    }

    /**
     * Set the path to the blojsom 2 installation directory
     *
     * @param blojsom2Path blojsom 2 installation directory
     */
    public void setBlojsom2Path(String blojsom2Path) {
        _blojsom2Path = blojsom2Path;
    }

    /**
     * Set the path to the blojsom 3 installation directory
     *
     * @param blojsom3Path blojsom 3 installation directory
     */
    public void setBlojsom3Path(String blojsom3Path) {
        _blojsom3Path = blojsom3Path;
    }

    /**
     * Set the {@link Fetcher}
     *
     * @param fetcher {@link Fetcher}
     */
    public void setFetcher(Fetcher fetcher) {
        _fetcher = fetcher;
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
            _blojsom2Fetcher = (BlojsomFetcher) fetcherClass.newInstance();
            _blojsom2Fetcher.init(servletConfig, blojsomConfiguration);
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


    private void loadBlojsom2Configuration() {
        String blojsomPropertiesPath = _blojsom2Path + "/" + BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + "/blojsom.properties";
        try {
            _blojsomProperties = new BlojsomProperties();
            _blojsomProperties.load(new FileInputStream(blojsomPropertiesPath));
        } catch (IOException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            _blojsomProperties = null;
        }

        if (_blojsomProperties == null) {
            throw new FatalBeanException("Unable to load blojsom properties file: " + blojsomPropertiesPath);
        }

        try {
            _blojsomConfiguration = new BlojsomConfiguration(_servletConfig, org.blojsom.util.BlojsomUtils.propertiesToMap(_blojsomProperties));
        } catch (BlojsomConfigurationException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }
        }

        if (_blojsomConfiguration == null) {
            throw new FatalBeanException("Unable to construct blojsom configuration object");
        }

        try {
            configureFetcher(_servletConfig, _blojsomConfiguration);
        } catch (ServletException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FatalBeanException("Unable to construct blojsom 2 fetcher object", e);
        }
    }

    /**
     * Upgrade the blojsom 2 instance to blojsom 3
     */
    public void upgrade() {
        if (org.blojsom.util.BlojsomUtils.checkNullOrBlank(_blojsom2Path)) {
            throw new InvalidPropertyException(Blojsom2ToBlojsom3Utility.class, "blojsom2Path", "blojsom2Path property was null or blank");
        }

        if (org.blojsom.util.BlojsomUtils.checkNullOrBlank(_blojsom3Path)) {
            throw new InvalidPropertyException(Blojsom2ToBlojsom3Utility.class, "blojsom3Path", "blojsom3Path property was null or blank");
        }

        if (_logger.isDebugEnabled()) {
            _logger.debug("blojsom 2 path: " + _blojsom2Path);
            _logger.debug("blojsom 3 path: " + _blojsom3Path);
        }

        loadBlojsom2Configuration();

        // Migrate each blog
        String[] blojsom2IDs = _blojsomConfiguration.getBlojsomUsers();
        for (int i = 0; i < blojsom2IDs.length; i++) {
            String blojsom2ID = blojsom2IDs[i];
            Blog blog = null;

            // Try and load the blog in the blojsom 3 installation, otherwise, create a new blog
            try {
                blog = _fetcher.loadBlog(blojsom2ID);
            } catch (FetcherException e) {
                if (_logger.isInfoEnabled()) {
                    _logger.info(e);
                }

                blog = new DatabaseBlog();
                blog.setBlogId(blojsom2ID);
            }

            BlogUser blogUser = null;
            try {
                blogUser = _blojsomConfiguration.loadBlog(blojsom2ID);
            } catch (BlojsomException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error("Unable to load blojsom 2 blog ID: " + blojsom2ID);
                    _logger.error(e);
                }

                continue;
            }

            // Migrate the properties
            Map blojsom2BlogProperties = blogUser.getBlog().getBlogProperties();
            Properties blogProperties = org.blojsom2.util.BlojsomUtils.mapToProperties(blojsom2BlogProperties);
            blog.setProperties(org.blojsom.util.BlojsomUtils.propertiesToMap(blogProperties));
            blog.setPlugins(blogUser.getPluginChain());
            // Convert the flavors
            Map blojsom2BlogFlavors = blogUser.getFlavors();
            Map blojsom3FlavorToTemplate = new HashMap();
            Iterator flavorIterator = blojsom2BlogFlavors.keySet().iterator();
            while (flavorIterator.hasNext()) {
                String flavor = (String) flavorIterator.next();
                blojsom3FlavorToTemplate.put(flavor, blogUser.getFlavorToTemplate().get(flavor).toString() + ", " + blogUser.getFlavorToContentType().get(flavor).toString());
            }
            blog.setTemplates(blojsom3FlavorToTemplate);

            try {
                _fetcher.saveBlog(blog);
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            }
        }

        if (_logger.isDebugEnabled()) {
            _logger.debug("Finished upgrading blojsom 2 instance to blojsom 3!");
        }
    }
}
