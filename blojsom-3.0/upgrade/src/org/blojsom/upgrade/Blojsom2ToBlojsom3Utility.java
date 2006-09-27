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
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.blojsom2.util.BlojsomProperties;
import org.blojsom2.blog.BlojsomConfiguration;
import org.blojsom2.blog.BlojsomConfigurationException;
import org.blojsom2.fetcher.BlojsomFetcher;
import org.blojsom2.fetcher.BlojsomFetcherException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.InvalidPropertyException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Utility class to migrate from blojsom 2 to blojsom 3
 *
 * @author David Czarnecki
 * @since blojsom 3
 * @version $Id: Blojsom2ToBlojsom3Utility.java,v 1.2 2006-09-27 16:48:04 czarneckid Exp $
 */
public class Blojsom2ToBlojsom3Utility {

    private static Log _logger = LogFactory.getLog(Blojsom2ToBlojsom3Utility.class);

    private ServletConfig _servletConfig;
    private Fetcher _fetcher;
    private BlojsomFetcher _blojsom2Fetcher;

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

    /**
     * Upgrade the blojsom 2 instance to blojsom 3
     */
    public void upgrade() {
        if (BlojsomUtils.checkNullOrBlank(_blojsom2Path)) {
            throw new InvalidPropertyException(Blojsom2ToBlojsom3Utility.class, "blojsom2Path", "blojsom2Path property was null or blank");
        }

        if (BlojsomUtils.checkNullOrBlank(_blojsom3Path)) {
            throw new InvalidPropertyException(Blojsom2ToBlojsom3Utility.class, "blojsom3Path", "blojsom3Path property was null or blank");
        }

        if (_logger.isDebugEnabled()) {
            _logger.debug("blojsom 2 path: " + _blojsom2Path);
            _logger.debug("blojsom 3 path: " + _blojsom3Path);
        }

        String blojsomPropertiesPath = _blojsom2Path + "/" + BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + "/blojsom.properties";
        BlojsomProperties blojsomProperties = null;
        try {
            blojsomProperties = new BlojsomProperties();
            blojsomProperties.load(new FileInputStream(blojsomPropertiesPath));
        } catch (IOException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            blojsomProperties = null;
        }

        if (blojsomProperties == null) {
            throw new FatalBeanException("Unable to load blojsom properties file: " + blojsomPropertiesPath);
        }

        BlojsomConfiguration blojsomConfiguration = null;
        try {
            blojsomConfiguration = new BlojsomConfiguration(_servletConfig, BlojsomUtils.propertiesToMap(blojsomProperties));
        } catch (BlojsomConfigurationException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }
        }

        if (blojsomConfiguration == null) {
            throw new FatalBeanException("Unable to construct blojsom configuration object");
        }

        try {
            configureFetcher(_servletConfig, blojsomConfiguration);
        } catch (ServletException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FatalBeanException("Unable to construct blojsom 2 fetcher object", e);
        }

        if (_logger.isDebugEnabled()) {
            _logger.debug("Finished upgrading blojsom 2 instance to blojsom 3!");
        }
    }
}
