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
import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.blog.BlogCategory;
import org.ignition.blojsom.dispatcher.GenericDispatcher;
import org.ignition.blojsom.util.BlojsomConstants;
import org.ignition.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * BlojsomServlet
 *
 * @author David Czarnecki
 */
public class BlojsomServlet extends HttpServlet implements BlojsomConstants {

    private final static String BLOG_HOME_IP = "blog-home";
    private final static String BLOG_NAME_IP = "blog-name";
    private final static String BLOG_DEPTH_IP = "blog-directory-depth";
    private final static String BLOG_RELOAD_CHECK_IP = "blog-reload-check";
    private final static String BLOG_LANGUAGE_IP = "blog-language";
    private final static String BLOG_DESCRIPTION_IP = "blog-description";
    private final static String BLOG_URL_IP = "blog-url";
    private final static String BLOG_FILE_EXTENSIONS_IP = "blog-file-extensions";
    private final static String BLOG_FLAVOR_CONFIGURATION_IP = "blog-flavor-configuration";
    private final static String BLOG_TEMPLATE_MAP_PROPERTIES_IP = "template-map-properties";

    private String _blogHome;
    private String _blogLanguage;
    private String _blogDescription;
    private String _blogName;
    private int _blogDepth;
    private String _blogURL;
    private String[] _blogFileExtensions;
    private Blog _blog;
    private long _blogReloadCheck;

    private Map _blogEntryMap;
    private Map _blogCalendarMap;

    private Map _flavorToTemplateMap;
    private Map _flavorToContentTypeMap;
    private Map _flavors;
    private Map _templateDispatchers;

    private Thread _blogUpdaterThread;

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
    private void configureBlog(ServletConfig servletConfig) {
        _blogHome = servletConfig.getInitParameter(BLOG_HOME_IP);
        _blogLanguage = servletConfig.getInitParameter(BLOG_LANGUAGE_IP);
        _blogDescription = servletConfig.getInitParameter(BLOG_DESCRIPTION_IP);
        _blogName = servletConfig.getInitParameter(BLOG_NAME_IP);
        _blogDepth = Integer.parseInt(servletConfig.getInitParameter(BLOG_DEPTH_IP));
        _blogReloadCheck = Long.parseLong(servletConfig.getInitParameter(BLOG_RELOAD_CHECK_IP));
        _blogURL = servletConfig.getInitParameter(BLOG_URL_IP);
        if (!_blogURL.endsWith("/")) {
            _blogURL += "/";
        }
        _blogFileExtensions = BlojsomUtils.parseCommaList(servletConfig.getInitParameter(BLOG_FILE_EXTENSIONS_IP));
        _blog = new Blog(_blogName, _blogDescription, _blogURL, _blogLanguage);
        _blogEntryMap = new TreeMap();
        _blogCalendarMap = new TreeMap();
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
        String templateConfiguration = servletConfig.getInitParameter(BLOG_TEMPLATE_MAP_PROPERTIES_IP);
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

        _logger.info("blojsom home: " + _blogHome);
        if (_blogReloadCheck != -1) {
            _blogUpdaterThread = new Thread(new BlogUpdater());
            if (!_blogUpdaterThread.isAlive()) {
                _blogUpdaterThread.setDaemon(true);
                _blogUpdaterThread.start();
            }
        } else {
            _logger.info("blojsom: blog reloading disabled");
            recursiveBlogBuilder(-1, _blogHome);
        }
    }

    /**
     * Build an in-memory representation for the blog
     *
     * @param blogDepth Blog depth to recurse to; -1 indicates infinite depth
     * @param blogDirectory Current working directory for the blog
     */
    private void recursiveBlogBuilder(int blogDepth, String blogDirectory) {
        blogDepth++;
        _logger.debug("Working in directory: " + blogDirectory + " at blog depth: " + blogDepth);
        if (_blogDepth != INFINITE_BLOG_DEPTH) {
            if (blogDepth == _blogDepth) {
                _logger.debug("Reached maximum blog depth: " + blogDepth);
                return;
            }
        }

        File blog = new File(blogDirectory);
        File[] directories = blog.listFiles(BlojsomUtils.getDirectoryFilter());

        String categoryKey = BlojsomUtils.getBlogCategory(_blogHome, blogDirectory);
        if (!categoryKey.endsWith("/")) {
            categoryKey += "/";
        }
        BlogCategory blogCategory = new BlogCategory(categoryKey, _blogURL + BlojsomUtils.removeInitialSlash(categoryKey));

        // look for blog entries in this directory
        File[] entries = blog.listFiles(BlojsomUtils.getExtensionsFilter(_blogFileExtensions));
        if (entries == null) {
            _logger.debug("No blog entries in blog directory: " + blogDirectory);
            _blogEntryMap.put(blogCategory, new TreeMap(BlojsomUtils.FILE_TIME_COMPARATOR));
        } else {
            Map entryMap;
            Map calendarMap;

            // Get the category-based map
            if (!_blogEntryMap.containsKey(blogCategory)) {
                entryMap = new TreeMap(BlojsomUtils.FILE_TIME_COMPARATOR);
            } else {
                entryMap = (Map) _blogEntryMap.get(blogCategory);
                _blogEntryMap.remove(blogCategory);
            }

            _logger.debug("Adding " + entries.length + " entries to the blog");
            for (int i = 0; i < entries.length; i++) {
                File entry = entries[i];

                String blogCalendarKey = BlojsomUtils.getDateKey(new Date(entry.lastModified()));
                blogCalendarKey = blogCategory + blogCalendarKey;

                if (!_blogCalendarMap.containsKey(blogCalendarKey)) {
                    calendarMap = new TreeMap(BlojsomUtils.FILE_TIME_COMPARATOR);
                } else {
                    calendarMap = (Map) _blogCalendarMap.get(blogCalendarKey);
                    _blogCalendarMap.remove(blogCalendarKey);
                }

                BlogEntry blogEntry;
                if (!entryMap.containsKey(entry)) {
                    blogEntry = new BlogEntry();
                    blogEntry.setSource(entry);
                    blogEntry.setCategory(BlojsomUtils.removeInitialSlash(categoryKey));
                    blogEntry.setLink(_blogURL + BlojsomUtils.removeInitialSlash(categoryKey) + "?permalink=" + entry.getName());
                    blogEntry.reloadSource();
                    entryMap.put(entry, blogEntry);
                    calendarMap.put(entry, blogEntry);
                    _logger.debug("Adding initial blog entry: " + entry.toString() + " in blog category: " + categoryKey);
                } else {
                    blogEntry = (BlogEntry) entryMap.get(entry);
                    if (entry.lastModified() > blogEntry.getLastModified()) {
                        entryMap.remove(entry);
                        calendarMap.remove(entry);
                        blogEntry = new BlogEntry();
                        blogEntry.setSource(entry);
                        blogEntry.setCategory(BlojsomUtils.removeInitialSlash(categoryKey));
                        blogEntry.setLink(_blogURL + BlojsomUtils.removeInitialSlash(categoryKey) + "?permalink=" + entry.getName());
                        blogEntry.reloadSource();
                        entryMap.put(entry, blogEntry);
                        calendarMap.put(entry, blogEntry);
                        _logger.debug("Blog entry updated on disk: " + entry.toString());
                    }
                }
                _blogCalendarMap.put(blogCalendarKey, calendarMap);
                _logger.debug("Added " + calendarMap.size() + " entries to calendar map under key: " + blogCalendarKey);
            }
            blogCategory.setNumberOfEntries(entryMap.size());
            _blogEntryMap.put(blogCategory, entryMap);
            _logger.debug("Added " + entryMap.size() + " entries to the blog map under key: " + blogCategory);
        }

        if (directories == null) {
            return;
        } else {
            for (int i = 0; i < directories.length; i++) {
                File directory = directories[i];
                recursiveBlogBuilder(blogDepth, directory.toString());
            }
        }
    }

    /**
     * Removes any blog categories that have disappeared while the blog is running
     */
    private void cleanupBlogCategories() {
        Iterator categoryIterator = _blogEntryMap.keySet().iterator();
        ArrayList deletedCategories = new ArrayList();
        while (categoryIterator.hasNext()) {
            BlogCategory blogCategory = (BlogCategory) categoryIterator.next();
            File blogDirectory = new File(_blogHome + blogCategory.getCategory());
            if (!blogDirectory.exists()) {
                deletedCategories.add(blogCategory);
            }
        }

        for (int i = 0; i < deletedCategories.size(); i++) {
            BlogCategory deletedCategory = (BlogCategory) deletedCategories.get(i);
            String deletedCategoryName = deletedCategory.getCategory();

            _blogEntryMap.remove(deletedCategory);
            _logger.debug("Removed blog entry category: " + deletedCategoryName);

            Iterator calendarMapIterator = _blogCalendarMap.keySet().iterator();
            while (calendarMapIterator.hasNext()) {
                String calendarMapKey = (String) calendarMapIterator.next();
                if (calendarMapKey.startsWith(deletedCategoryName)) {
                    _logger.debug("Removed blog calendar category: " + deletedCategoryName);
                    _blogCalendarMap.remove(calendarMapKey);
                }
            }
        }
    }

    /**
     * Retrieve a permalink entry from the entries for a given category
     *
     * @param entriesForCategory Blog entries for a given category
     * @param requestedCategory Requested category
     * @param permalink Permalink entry requested
     * @return Blog entry array containing the single requested permalink entry (possibly null), or null if the permalink entry was not found
     */
    private BlogEntry[] getPermalinkEntry(Map entriesForCategory, String requestedCategory, String permalink) {
        BlogEntry[] entryArray;

        boolean foundEntry = false;
        entryArray = new BlogEntry[1];
        _logger.debug("Permalink entry: " + requestedCategory + permalink);
        Iterator entryIterator = entriesForCategory.keySet().iterator();
        while (entryIterator.hasNext() && !foundEntry) {
            File entryKey = (File) entryIterator.next();
            if (entryKey.getName().endsWith(permalink)) {
                foundEntry = true;
                entryArray[0] = (BlogEntry) entriesForCategory.get(entryKey);
                // Check if entry deleted from disk
                if (entryArray[0] == null) {
                    entryArray = null;
                    entriesForCategory.remove(entryKey);
                }
            }
        }

        if (foundEntry == false) {
            _logger.warn("Permalink entry: " + permalink + " gone");
            entryArray = null;
        }

        return entryArray;
    }

    /**
     * Retrieve all of the entries for a requested category
     *
     * @param entriesForCategory Entries for the requested category
     * @param requestedCategory Requested category
     * @return Blog entry array containing the list of blog entries for the requested category
     */
    private BlogEntry[] getEntriesForCategory(Map entriesForCategory) {
        BlogEntry[] entryArray;
        ArrayList entryList = new ArrayList();
        Iterator entryIterator = entriesForCategory.keySet().iterator();
        while (entryIterator.hasNext()) {
            Object entryKey = entryIterator.next();
            if (entriesForCategory.get(entryKey) != null) {
                entryList.add(entriesForCategory.get(entryKey));
            } else {
                // Entry deleted from disk
                entriesForCategory.remove(entryKey);
            }
        }
        entryArray = (BlogEntry[]) entryList.toArray(new BlogEntry[entryList.size()]);

        return entryArray;
    }

    /**
     * Retrieve all of the entries for a requested category that fall under a given date. A partial
     * date may be given such that if only a year is given, it would retrieve all entries under the
     * given category for that year. If a year and a month are give, it would retrieve all entries
     * under the given category for that year and month. If a year, month, and day are given, it would
     * retrieve all entries under the given category for that year, month, and day.
     *
     * @param requestedCategory Requested category
     * @param year Year to retrieve entries for
     * @param month Month to retrieve entries for
     * @param day Day to retrieve entries for
     * @return Blog entry array containing the list of blog entries for the given category, year, month, and day
     */
    private BlogEntry[] getEntriesForDate(String requestedCategory, String year, String month, String day) {
        String blogCalendarKey = requestedCategory + year + month + day;
        _logger.debug("Looking for entries by date under key: " + blogCalendarKey);
        BlogEntry[] entryArray;
        ArrayList entryList = new ArrayList();
        Map entriesForDate = null;

        // Search for the calendar key in the calendar map
        Iterator calendarKeyIterator = _blogCalendarMap.keySet().iterator();
        while (calendarKeyIterator.hasNext()) {
            String calendarKey = (String) calendarKeyIterator.next();
            if (calendarKey.startsWith(blogCalendarKey)) {
                entriesForDate = (Map) _blogCalendarMap.get(calendarKey);
            }
        }

        if (entriesForDate != null) {
            Iterator entryIterator = entriesForDate.keySet().iterator();
            while (entryIterator.hasNext()) {
                Object entryKey = entryIterator.next();
                if (entriesForDate.containsKey(entryKey)) {
                    entryList.add(entriesForDate.get(entryKey));
                } else {
                    entriesForDate.remove(entryKey);
                }
            }
            entryArray = (BlogEntry[]) entryList.toArray(new BlogEntry[entryList.size()]);

            return entryArray;
        }

        return null;
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
        String blogSiteURL = BlojsomUtils.getBlogSiteURL(_blogURL, httpServletRequest.getServletPath());

        // Determine the user requested category
        String requestedCategory = httpServletRequest.getPathInfo();

        if (requestedCategory == null) {
            requestedCategory = "/";
        } else if (!requestedCategory.endsWith("/")) {
            requestedCategory += "/";
        }

        _logger.debug("User requested category: " + requestedCategory);
        BlogCategory category = new BlogCategory(requestedCategory, _blogURL + BlojsomUtils.removeInitialSlash(requestedCategory));

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

        // Get the entries for the requested category
        Map entriesForCategory = (Map) _blogEntryMap.get(category);

        // Convert the entries from the map into an array
        BlogEntry[] entries;

        // Check first to see if the entries for the requested category is null
        // If so, there is no use looking for a permalink entry or a calendar-based entry in that category
        if (entriesForCategory == null) {
            entries = null;
        } else {
            // Otherwise, check for a permalink entry first
            if (permalink != null) {
                entries = getPermalinkEntry(entriesForCategory, requestedCategory, permalink);
            } else {
                // Check to see if we have requested entries by calendar
                if (year != null) {
                    entries = getEntriesForDate(requestedCategory, year, month, day);
                } else {
                    entries = getEntriesForCategory(entriesForCategory);
                }
            }
        }

        // Setup the context for the dispatcher
        HashMap context = new HashMap();
        context.put(BLOJSOM_BLOG, _blog);
        context.put(BLOJSOM_SITE_URL, blogSiteURL);
        context.put(BLOJSOM_ENTRIES, entries);
        context.put(BLOJSOM_CATEGORIES, BlojsomUtils.getBlogCategories(_blogURL, _blogEntryMap));

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

    /**
     * Updates the in-memory blog representation for blojsom
     */
    private class BlogUpdater implements Runnable {

        /**
         * Create a new BlogUpdater
         */
        public BlogUpdater() {
        }

        /**
         * Reloads the blog entries and cleans up the blog categories from disk
         */
        public void run() {
            while (true) {
                _logger.debug("Reloading blog from BlogUpdater");
                recursiveBlogBuilder(-1, _blogHome);
                cleanupBlogCategories();

                try {
                    Thread.sleep(_blogReloadCheck);
                } catch (InterruptedException e) {
                    _logger.error(e);
                    break;
                }
            }
        }
    }
}
