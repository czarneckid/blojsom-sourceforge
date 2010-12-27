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
package org.blojsom.plugin.weather;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.weather.beans.NWSInformation;
import org.blojsom.plugin.weather.beans.WeatherInformation;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * WeatherPlugin
 *
 * @author David Czarnecki
 * @author Mark Lussier
 * @version $Id: WeatherPlugin.java,v 1.5 2008-07-07 19:54:15 czarneckid Exp $
 * @since blojsom 3.0
 */
public class WeatherPlugin implements Plugin {

    private Log _logger = LogFactory.getLog(WeatherPlugin.class);

    /**
     * Weather station code initialization parameter
     */
    public static final String PROPERTY_WEATHER_CODE = "weather-station-code";

    /**
     * Weather provider initialization parameter
     */
    public static final String PROPERTY_WEATHER_PROVIDER = "weather-provider";

    /**
     * Default weather provider from National Weather Service
     */
    public static final String DEFAULT_WEATHER_PROVIDER = "org.blojsom.plugin.weather.beans.NWSInformation";

    /**
     * Default Weather Station Code - Albany International Airport - Albany, NY USA
     */
    public static final String DEFAULT_WEATHER_CODE = "ALB";

    private Map _weatherInformation;

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error initializing the plugin
     */
    public void init() throws PluginException {
        _weatherInformation = new WeakHashMap();
    }

    /**
     * Read the {@link Weather} settings for a blog
     *
     * @param blog {@link Blog}
     * @return {@link Weather} settings for the blog
     */
    protected Weather readWeatherSettingsForBlog(Blog blog) {
        Weather weather = new Weather();

        String stationCode = blog.getProperty(WeatherPlugin.PROPERTY_WEATHER_CODE);
        if (BlojsomUtils.checkNullOrBlank(stationCode)) {
            stationCode = WeatherPlugin.DEFAULT_WEATHER_CODE;
        }

        String providerClass = blog.getProperty(WeatherPlugin.PROPERTY_WEATHER_PROVIDER);
        if (BlojsomUtils.checkNullOrBlank(providerClass)) {
            providerClass = WeatherPlugin.DEFAULT_WEATHER_PROVIDER;
        }

        weather.setStationCode(stationCode);
        weather.setProviderClass(providerClass);

        return weather;
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param blog                {@link Blog} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws PluginException If there is an error processing the blog entries
     */
    public Entry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, Map context, Entry[] entries) throws PluginException {
        try {
            WeatherInformation weatherInformation = (WeatherInformation) _weatherInformation.get(blog.getBlogId());

            if (weatherInformation == null) {
                Weather weather = readWeatherSettingsForBlog(blog);
                WeatherFetcher weatherFetcher = new WeatherFetcher();
                WeatherInformation weatherInformationForBlog = new NWSInformation(weather.getStationCode());

                weatherFetcher.retrieveForecast(weatherInformationForBlog);

                _weatherInformation.put(blog.getBlogId(), weatherInformationForBlog);
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Put weather information for " + blog.getBlogId() + " in cache");
                }
            } else {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Retrieved weather information for " + blog.getBlogId() + " from cache");
                }
            }

            context.put(WeatherConstants.BLOJSOM_WEATHER_INFORMATION, weatherInformation);
        } catch (IOException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }
        }

        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws PluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error in finalizing this plugin
     */
    public void destroy() throws PluginException {
    }
}
