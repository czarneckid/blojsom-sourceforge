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
package org.ignition.blojsom.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ignition.blojsom.blog.*;
import org.ignition.blojsom.dispatcher.GenericDispatcher;
import org.ignition.blojsom.util.BlojsomConstants;
import org.ignition.blojsom.util.BlojsomUtils;
import org.ignition.blojsom.plugin.BlojsomPluginException;
import org.ignition.blojsom.plugin.BlojsomPlugin;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * BlojsomServlet
 *
 * @author David Czarnecki
 * @author Mark Lussier
 * @version $Id: BlojsomServlet.java,v 1.67 2003-04-13 15:56:03 intabulas Exp $
 */
public class BlojsomServlet extends HttpServlet implements BlojsomConstants {

    // BlojsomServlet initialization properties from web.xml
    private final static String BLOG_FLAVOR_CONFIGURATION_IP = "blog-flavor-configuration";
    private final static String BLOG_DISPATCHER_MAP_CONFIGURATION_IP = "dispatcher-map-configuration";
    private final static String BLOG_CONFIGURATION_IP = "blog-configuration";
    private final static String BLOG_PLUGIN_CONFIGURATION_IP = "blog-plugin-configuration";

    private Blog _blog;

    private Map _flavorToTemplateMap;
    private Map _flavorToContentTypeMap;
    private Map _flavors;
    private Map _templateDispatchers;
    private Map _plugins;
    private Map _pluginChainMap;

    private Log _logger = LogFactory.getLog(BlojsomServlet.class);

    /**
     * Create a new blojsom servlet instance
     */
    public BlojsomServlet() {
    }

    /**
     * Called when removing the servlet from the servlet container. Also calls the
     * {@link BlojsomPlugin#destroy} method for each of the plugins loaded by
     * blojsom
     */
    public void destroy() {
        super.destroy();
        Iterator pluginIteratorIterator = _plugins.keySet().iterator();
        while (pluginIteratorIterator.hasNext()) {
            String pluginName = (String) pluginIteratorIterator.next();
            BlojsomPlugin plugin = (BlojsomPlugin) _plugins.get(pluginName);
            try {
                plugin.destroy();
                _logger.debug("Removed blojsom plugin: " + plugin.getClass().getName());
            } catch (BlojsomPluginException e) {
                _logger.error(e);
            }
        }
        _logger.debug("blojsom destroyed");
    }

    /**
     * Configure the blog
     *
     * @param servletConfig Servlet configuration information
     */
    private void configureBlog(ServletConfig servletConfig) throws ServletException {
        String blojsomConfiguration = servletConfig.getInitParameter(BLOG_CONFIGURATION_IP);
        Properties configurationProperties = new Properties();
        InputStream is = servletConfig.getServletContext().getResourceAsStream(blojsomConfiguration);
        try {
            configurationProperties.load(is);
            _blog = new Blog(configurationProperties);
            is.close();
        } catch (IOException e) {
            _logger.error(e);
        } catch (BlojsomConfigurationException e) {
            _logger.error(e);
            throw new ServletException(e);
        }
    }

    /**
     * Configure the flavors for the blog which map flavor values like "html" and "rss" to
     * the proper template and content type
     *
     * @param servletConfig Servlet configuration information
     */
    private void configureFlavors(ServletConfig servletConfig) {
        _flavors = new HashMap();
        _flavorToTemplateMap = new HashMap();
        _flavorToContentTypeMap = new HashMap();

        String flavorConfiguration = servletConfig.getInitParameter(BLOG_FLAVOR_CONFIGURATION_IP);
        Properties flavorProperties = new Properties();
        InputStream is = servletConfig.getServletContext().getResourceAsStream(flavorConfiguration);
        try {
            flavorProperties.load(is);
            is.close();
            Iterator flavorIterator = flavorProperties.keySet().iterator();
            while (flavorIterator.hasNext()) {
                String flavor = (String) flavorIterator.next();
                String[] flavorMapping = BlojsomUtils.parseCommaList(flavorProperties.getProperty(flavor));
                _flavors.put(flavor, flavor);
                _flavorToTemplateMap.put(flavor, flavorMapping[0]);
                _flavorToContentTypeMap.put(flavor, flavorMapping[1]);
            }
        } catch (IOException e) {
            _logger.error(e);
        }
    }

    /**
     * Configure the authorization table blog (user id's and and passwords)
     *
     * @param servletConfig Servlet configuration information
     */
    private void configureAuthorization(ServletConfig servletConfig) {
        Map _authorization = new HashMap();

        String authConfiguration = servletConfig.getInitParameter(BLOG_AUTHORIZATION_IP);
        Properties authProperties = new Properties();
        InputStream is = servletConfig.getServletContext().getResourceAsStream(authConfiguration);
        try {
            authProperties.load(is);
            is.close();
            Iterator authIterator = authProperties.keySet().iterator();
            while (authIterator.hasNext()) {
                String userid = (String) authIterator.next();
                String password = authProperties.getProperty(userid);
                _authorization.put(userid, password);
            }

            if (!_blog.setAuthorization(_authorization)) {
                _logger.error("Authorization table could not be assigned");
            }

        } catch (IOException e) {
            _logger.error(e);
        }
    }


    /**
     * Configure the dispatchers that blojsom will use when passing a request/response on to a
     * particular template
     *
     * @param servletConfig Servlet configuration information
     */
    private void configureDispatchers(ServletConfig servletConfig) {
        String templateConfiguration = servletConfig.getInitParameter(BLOG_DISPATCHER_MAP_CONFIGURATION_IP);
        _templateDispatchers = new HashMap();
        Properties templateMapProperties = new Properties();
        InputStream is = servletConfig.getServletContext().getResourceAsStream(templateConfiguration);
        try {
            templateMapProperties.load(is);
            is.close();
            Iterator templateIterator = templateMapProperties.keySet().iterator();
            while (templateIterator.hasNext()) {
                String templateExtension = (String) templateIterator.next();
                String templateDispatcherClass = templateMapProperties.getProperty(templateExtension);
                Class dispatcherClass = Class.forName(templateDispatcherClass);
                GenericDispatcher dispatcher = (GenericDispatcher) dispatcherClass.newInstance();
                dispatcher.init(servletConfig);
                _templateDispatchers.put(templateExtension, dispatcher);
                _logger.debug("Added template dispatcher: " + templateDispatcherClass);
            }
        } catch (InstantiationException e) {
            _logger.error(e);
        } catch (IllegalAccessException e) {
            _logger.error(e);
        } catch (ClassNotFoundException e) {
            _logger.error(e);
        } catch (IOException e) {
            _logger.error(e);
        }
    }

    /**
     * Configure the plugins that blojsom will use
     *
     * @param servletConfig Servlet configuration information
     */
    private void configurePlugins(ServletConfig servletConfig) {
        String pluginConfiguration = servletConfig.getInitParameter(BLOG_PLUGIN_CONFIGURATION_IP);
        _plugins = new HashMap();
        _pluginChainMap = new HashMap();
        Properties pluginProperties = new Properties();
        InputStream is = servletConfig.getServletContext().getResourceAsStream(pluginConfiguration);
        try {
            pluginProperties.load(is);
            is.close();
            Iterator pluginIterator = pluginProperties.keySet().iterator();
            while (pluginIterator.hasNext()) {
                String plugin = (String) pluginIterator.next();
                if (plugin.indexOf(BLOJSOM_PLUGIN_CHAIN) != -1) {
                    _pluginChainMap.put(plugin, BlojsomUtils.parseCommaList(pluginProperties.getProperty(plugin)));
                    _logger.debug("Added plugin chain: " + plugin + "=" + pluginProperties.getProperty(plugin));
                } else {
                    String pluginClassName = pluginProperties.getProperty(plugin);
                    try {
                        Class pluginClass = Class.forName(pluginClassName);
                        BlojsomPlugin blojsomPlugin = (BlojsomPlugin) pluginClass.newInstance();
                        blojsomPlugin.init(servletConfig, _blog.getBlogProperties());
                        _plugins.put(plugin, blojsomPlugin);
                        _logger.info("Added blojsom plugin: " + pluginClassName);
                    } catch (BlojsomPluginException e) {
                        _logger.error(e);
                    } catch (InstantiationException e) {
                        _logger.error(e);
                    } catch (IllegalAccessException e) {
                        _logger.error(e);
                    } catch (ClassNotFoundException e) {
                        _logger.error(e);
                    }
                }
            }
        } catch (IOException e) {
            _logger.error(e);
        }
    }

    /**
     * Initialize blojsom: configure blog, configure flavors, configure dispatchers
     *
     * @param servletConfig Servlet configuration information
     * @throws ServletException If there is an error initializing blojsom
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        configureBlog(servletConfig);
        configureAuthorization(servletConfig);
        configureFlavors(servletConfig);
        configureDispatchers(servletConfig);
        configurePlugins(servletConfig);

        _logger.debug("blojsom: All Your Blog Are Belong To Us");
    }

    /**
     * Service a request to blojsom
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @throws ServletException If there is an error processing the request
     * @throws IOException If there is an error in IO
     */
    protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        try {
            httpServletRequest.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            _logger.error(e);
        }

        _logger.debug("blojsom servlet path: " + httpServletRequest.getServletPath());
        _logger.debug("blojsom request URI: " + httpServletRequest.getRequestURI());
        _logger.debug("blojsom request URL: " + httpServletRequest.getRequestURL().toString());
        _logger.debug("blojsom URL: " + _blog.getBlogBaseURL());

        // Make sure that we have a request URI ending with a / otherwise we need to
        // redirect so that the browser can handle relative link generation
        if (!httpServletRequest.getRequestURI().endsWith("/")) {
            StringBuffer redirectURL = new StringBuffer();
            redirectURL.append(httpServletRequest.getRequestURI());
            redirectURL.append("/");
            if (httpServletRequest.getParameterMap().size() > 0) {
                redirectURL.append("?");
                redirectURL.append(BlojsomUtils.convertRequestParams(httpServletRequest));
            }
            _logger.debug("Redirecting the user to: " + redirectURL.toString());
            httpServletResponse.sendRedirect(redirectURL.toString());
            return;
        }

        // Determine the user requested category
        String requestedCategory = httpServletRequest.getPathInfo();
        _logger.debug("blojsom path info: " + requestedCategory);

        if (requestedCategory == null) {
            requestedCategory = "/";
        } else if (!requestedCategory.endsWith("/")) {
            requestedCategory += "/";
        }

        _logger.debug("User requested category: " + requestedCategory);
        BlogCategory category = new BlogCategory(requestedCategory, _blog.getBlogURL() + BlojsomUtils.removeInitialSlash(requestedCategory));

        // We might also want to pass the flavor so that we can also have flavor-based category meta-data
        category.loadMetaData(_blog.getBlogHome(), _blog.getBlogPropertiesExtensions());

        // Determine if a permalink has been requested
        String permalink = httpServletRequest.getParameter(PERMALINK_PARAM);
        if (permalink != null) {
            permalink = BlojsomUtils.getFilenameForPermalink(permalink, _blog.getBlogFileExtensions());
            permalink = BlojsomUtils.urlDecode(permalink);
            if (permalink == null) {
                _logger.error("Permalink request for invalid permalink: " + httpServletRequest.getParameter(PERMALINK_PARAM));
            } else {
                _logger.debug("Permalink request for: " + permalink);
            }
        }

        // Determine the requested flavor
        String flavor = httpServletRequest.getParameter(FLAVOR_PARAM);
        if (flavor == null) {
            flavor = DEFAULT_FLAVOR_HTML;
        } else {
            if (_flavors.get(flavor) == null) {
                flavor = DEFAULT_FLAVOR_HTML;
            }
        }

        BlogEntry[] entries;

        // Check for a permalink entry request
        if (permalink != null) {
            entries = _blog.getPermalinkEntry(category, permalink);
        } else {
            if (requestedCategory.equals("/")) {
                entries = _blog.getEntriesAllCategories(flavor, -1);
            } else {
                entries = _blog.getEntriesForCategory(category, -1);
            }
        }

        String[] pluginChain = null;

        // Check to see if the user would like to override the plugin chain
        if (httpServletRequest.getParameter(PLUGINS_PARAM) != null) {
            pluginChain = BlojsomUtils.parseCommaList(httpServletRequest.getParameter(PLUGINS_PARAM));
        } else {
            String pluginChainMapKey = flavor + "." + BLOJSOM_PLUGIN_CHAIN;
            if (_pluginChainMap.containsKey(pluginChainMapKey)) {
                pluginChain = (String[]) _pluginChainMap.get(pluginChainMapKey);
            } else {
                pluginChain = (String[]) _pluginChainMap.get(BLOJSOM_PLUGIN_CHAIN);
            }
        }

        // Setup the initial context for the dispatcher
        HashMap context = new HashMap();

        // Invoke the plugins in the order in which they were specified
        if ((entries != null) && (pluginChain != null)) {
            for (int i = 0; i < pluginChain.length; i++) {
                String plugin = pluginChain[i];
                if (_plugins.containsKey(plugin)) {
                    BlojsomPlugin blojsomPlugin = (BlojsomPlugin) _plugins.get(plugin);
                    _logger.debug("blojsom plugin execution: " + blojsomPlugin.getClass().getName());
                    try {
                        entries = blojsomPlugin.process(httpServletRequest, httpServletResponse, context, entries);
                        blojsomPlugin.cleanup();
                    } catch (BlojsomPluginException e) {
                        _logger.error(e);
                    }
                } else {
                    _logger.error("No plugin loaded for: " + plugin);
                }
            }
        }

        String blogdate = null;
        String blogISO8601Date = null;
        Date blogDateObject = null;
        // If we have entries, construct a last modified on the most recent entry
        // Additionally, set the blog date
        if ((entries != null) && (entries.length > 0)) {
            BlogEntry _entry = entries[0];
            long _lastmodified;

            if ((httpServletRequest.getParameter(PAGE_PARAM) != null) && (_entry.getNumComments() > 0)) {
                BlogComment _comment = _entry.getCommentsAsArray()[_entry.getNumComments() - 1];
                _lastmodified = _comment.getCommentDateLong();
                _logger.debug("Adding last-modified header for most recent entry comment");
            } else {
                _lastmodified = _entry.getLastModified();
                _logger.debug("Adding last-modified header for most recent blog entry");
            }
            httpServletResponse.addDateHeader(HTTP_LASTMODIFIED, _lastmodified);
            blogdate = entries[0].getRFC822Date();
            blogISO8601Date = entries[0].getISO8601Date();
            blogDateObject = entries[0].getDate();
        } else {
            _logger.debug("Adding last-modified header for current date");
            Date today = new Date();
            blogdate = BlojsomUtils.getRFC822Date(today);
            blogISO8601Date = BlojsomUtils.getISO8601Date(today);
            blogDateObject = today;
        }

        // Finish setting up the context for the dispatcher
        context.put(BLOJSOM_BLOG, _blog);
        context.put(BLOJSOM_SITE_URL, _blog.getBlogBaseURL());
        context.put(BLOJSOM_ENTRIES, entries);
        context.put(BLOJSOM_DATE, blogdate);
        context.put(BLOJSOM_DATE_ISO8601, blogISO8601Date);
        context.put(BLOJSOM_DATE_OBJECT, blogDateObject);
        if (permalink != null) {
            context.put(BLOJSOM_PERMALINK, permalink);
        }

        if (requestedCategory.equals("/")) {
            context.put(BLOJSOM_CATEGORIES, _blog.getBlogCategories());
        } else {
            context.put(BLOJSOM_CATEGORIES, _blog.getBlogCategoryHierarchy(category));
        }
        context.put(BLOJSOM_REQUESTED_CATEGORY, category);
        context.put(BLOJSOM_COMMENTS_ENABLED, _blog.areCommentsEnabled());

        // Forward the request on to the template for the requested flavor
        String flavorTemplate;
        if (_flavorToTemplateMap.get(flavor) == null) {
            flavorTemplate = (String) _flavorToTemplateMap.get(DEFAULT_FLAVOR_HTML);
        } else {
            flavorTemplate = (String) _flavorToTemplateMap.get(flavor);
        }

        // Get the content type for the requested flavor
        String flavorContentType = (String) _flavorToContentTypeMap.get(flavor);

        String templateExtension = BlojsomUtils.getFileExtension(flavorTemplate);
        _logger.debug("Template extension: " + templateExtension);

        // Retrieve the appropriate dispatcher for the template
        GenericDispatcher dispatcher = (GenericDispatcher) _templateDispatchers.get(templateExtension);
        dispatcher.dispatch(httpServletRequest, httpServletResponse, context, flavorTemplate, flavorContentType);
    }
}
