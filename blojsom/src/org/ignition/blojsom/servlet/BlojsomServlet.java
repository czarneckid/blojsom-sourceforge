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
package org.ignition.blojsom.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.blog.BlogCategory;
import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.dispatcher.GenericDispatcher;
import org.ignition.blojsom.util.BlojsomConstants;
import org.ignition.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * BlojsomServlet
 *
 * @author David Czarnecki
 */
public class BlojsomServlet extends HttpServlet implements BlojsomConstants {

    // Blog initialization parameters from blojsom.properties
    private final static String BLOG_HOME_IP = "blog-home";
    private final static String BLOG_NAME_IP = "blog-name";
    private final static String BLOG_DEPTH_IP = "blog-directory-depth";
    private final static String BLOG_LANGUAGE_IP = "blog-language";
    private final static String BLOG_DESCRIPTION_IP = "blog-description";
    private final static String BLOG_URL_IP = "blog-url";
    private final static String BLOG_FILE_EXTENSIONS_IP = "blog-file-extensions";
    private final static String BLOG_PROPERTIES_EXTENSIONS_IP = "blog-properties-extensions";
    private static final String BLOG_ENTRIES_DISPLAY_IP = "blog-entries-display";
    private static final String BLOG_DEFAULT_CATEGORY_MAPPING_IP = "blog-default-category-mapping";

    // BlojsomServlet initialization properties from web.xml
    private final static String BLOG_FLAVOR_CONFIGURATION_IP = "blog-flavor-configuration";
    private final static String BLOG_DISPATCHER_MAP_CONFIGURATION_IP = "dispatcher-map-configuration";
    private final static String BLOG_CONFIGURATION_IP = "blog-configuration";

    private Blog _blog;

    private Map _flavorToTemplateMap;
    private Map _flavorToContentTypeMap;
    private Map _flavors;
    private Map _templateDispatchers;

    private Log _logger = LogFactory.getLog(BlojsomServlet.class);

    /**
     * Create a new blojsom servlet instance
     */
    public BlojsomServlet() {
    }

    /**
     * Called when removing the servlet from the servlet container
     */
    public void destroy() {
        super.destroy();
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

            String blogHome = configurationProperties.getProperty(BLOG_HOME_IP);
            if (blogHome == null) {
                _logger.error("No value supplied for blog-home");
                throw new ServletException("No valued supplied for blog-home");
            }
            if (!blogHome.endsWith("/")) {
                blogHome += "/";
            }

            String blogLanguage = configurationProperties.getProperty(BLOG_LANGUAGE_IP);
            if (blogLanguage == null) {
                _logger.warn("No value supplied for blog-language. Defaulting to: " + BLOG_LANGUAGE_DEFAULT);
                blogLanguage = BLOG_LANGUAGE_DEFAULT;
            }

            String blogDescription = configurationProperties.getProperty(BLOG_DESCRIPTION_IP);
            if (blogDescription == null) {
                _logger.warn("No value supplied for blog-description");
                blogDescription = "";
            }

            String blogName = configurationProperties.getProperty(BLOG_NAME_IP);
            if (blogName == null) {
                _logger.warn("No value supplied for blog-name");
                blogName = "";
            }

            int blogDepth = Integer.parseInt(configurationProperties.getProperty(BLOG_DEPTH_IP, Integer.toString(INFINITE_BLOG_DEPTH)));

            String blogURL = configurationProperties.getProperty(BLOG_URL_IP);
            if (blogURL == null) {
                _logger.error("No value supplied for blog-url");
                throw new ServletException("No value supplied for blog-url");
            }
            if (!blogURL.endsWith("/")) {
                blogURL += "/";
            }

            // The following parameters will either be removed or changed
            String[] blogFileExtensions = BlojsomUtils.parseCommaList(configurationProperties.getProperty(BLOG_FILE_EXTENSIONS_IP));
            String[] blogPropertiesExtensions = BlojsomUtils.parseCommaList(configurationProperties.getProperty(BLOG_PROPERTIES_EXTENSIONS_IP));
            _blog = new Blog(blogHome, blogName, blogDescription, blogURL, blogLanguage,
                    blogFileExtensions, blogPropertiesExtensions, blogDepth);

            int blogDisplayEntries = Integer.parseInt(configurationProperties.getProperty(BLOG_ENTRIES_DISPLAY_IP, Integer.toString(BLOG_ENTRIES_DISPLAY_DEFAULT)));
            _blog.setBlogDisplayEntries(blogDisplayEntries);

            String blogDefaultCategoryMapping = configurationProperties.getProperty(BLOG_DEFAULT_CATEGORY_MAPPING_IP);
            String[] blogDefaultCategoriesMap;
            if (blogDefaultCategoryMapping == null) {
                blogDefaultCategoriesMap = null;
                _logger.debug("No mapping supplied for the default category '/'");
            } else {
                blogDefaultCategoriesMap = BlojsomUtils.parseCommaList(blogDefaultCategoryMapping);
                _logger.debug(blogDefaultCategoriesMap.length + " directories mapped to the default category '/'");
                if (blogDefaultCategoriesMap.length == 0) {
                    blogDefaultCategoriesMap = null;
                }
            }
            _blog.setBlogDefaultCategoryMappings(blogDefaultCategoriesMap);
        } catch (IOException e) {
            _logger.error(e);
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
     * Initialize blojsom: configure blog, configure flavors, configure dispatchers
     *
     * @param servletConfig Servlet configuration information
     * @throws ServletException If there is an error initializing blojsom
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        configureBlog(servletConfig);
        configureFlavors(servletConfig);
        configureDispatchers(servletConfig);

        _logger.info("blojsom home: " + _blog.getBlogHome());
    }

    /**
     * Service a request to blojsom
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @throws ServletException If there is an error processing the request
     * @throws IOException If there is an error in IO
     */
    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        String blogSiteURL = BlojsomUtils.getBlogSiteURL(httpServletRequest.getRequestURL().toString(), httpServletRequest.getServletPath());
        _logger.debug("blojsom URL: " + blogSiteURL);

        // Determine the user requested category
        String requestedCategory = httpServletRequest.getPathInfo();

        if (requestedCategory == null) {
            requestedCategory = "/";
        } else if (!requestedCategory.endsWith("/")) {
            requestedCategory += "/";
        }

        _logger.debug("User requested category: " + requestedCategory);
        BlogCategory category = new BlogCategory(requestedCategory, _blog.getBlogURL() + BlojsomUtils.removeInitialSlash(requestedCategory));

        // Determine if a permalink has been requested
        String permalink = httpServletRequest.getParameter("permalink");
        if (permalink != null) {
            _logger.debug("Permalink request for: " + permalink);
        }

        // Determine a calendar-based request
        String year = null;
        String month = null;
        String day = null;

        year = httpServletRequest.getParameter(YEAR_PARAM);
        if (year != null) {

            // Must be a 4 digit year
            if (year.length() != 4) {
                year = null;
            } else {
                month = httpServletRequest.getParameter(MONTH_PARAM);
                if (month == null) {
                    month = "";
                }
                day = httpServletRequest.getParameter(DAY_PARAM);
                if (day == null) {
                    day = "";
                }
            }
            _logger.debug("Calendar-based request for: " + requestedCategory + year + month + day);
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
            // Check to see if we have requested entries by calendar
            if (year != null) {
                entries = _blog.getEntriesForDate(category, year, month, day);
            // Check for the default category
            } else if (requestedCategory.equals("/")) {
                entries = _blog.getEntriesAllCategories();
            // Check for the requested category
            } else {
                entries = _blog.getEntriesForCategory(category);
            }
        }

        // Setup the context for the dispatcher
        HashMap context = new HashMap();
        context.put(BLOJSOM_BLOG, _blog);
        context.put(BLOJSOM_SITE_URL, blogSiteURL);
        context.put(BLOJSOM_ENTRIES, entries);
        if (requestedCategory.equals("/")) {
            context.put(BLOJSOM_CATEGORIES, _blog.getBlogCategories());
        } else {
            context.put(BLOJSOM_CATEGORIES, _blog.getBlogCategoryHierarchy(category));
        }
        context.put(BLOJSOM_REQUESTED_CATEGORY, requestedCategory);

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
