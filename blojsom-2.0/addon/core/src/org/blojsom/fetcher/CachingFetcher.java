/**
 * Copyright (c) 2003-2004, David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003-2004 by Mark Lussier
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
package org.blojsom.fetcher;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.BlojsomException;
import org.blojsom.blog.*;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Properties;

/**
 * CachingFetcher
 * 
 * @author David Czarnecki
 * @version $Id: CachingFetcher.java,v 1.11 2004-07-30 03:38:06 czarneckid Exp $
 * @since blojsom 2.01
 */
public class CachingFetcher extends StandardFetcher {

    private Log _logger = LogFactory.getLog(CachingFetcher.class);

    /**
     * Default refresh period for refreshing the cache (5 minutes)
     */
    private static final int DEFAULT_CACHE_REFRESH = 300;

    /**
     * Initialization parameter for web.xml
     */
    private static final String OSCACHE_PROPERTIES_IP = "oscache-properties";

    /**
     * Parameter for blog.properties for user to control cache refresh period
     */
    private static final String CACHING_FETCHER_REFRESH = "caching-fetcher-refresh";

    /**
     * Default location for oscache.properties
     */
    private static final String OSCACHE_PROPERTIES_DEFAULT = "/WEB-INF/oscache.properties";

    /**
     * Internal key for cache for flavor
     */
    private static final String FLAVOR_KEY = "__FLAVOR__";

    /**
     * Internal key for cache for category
     */
    private static final String CATEGORY_KEY = "__CATEGORY__";

    protected static GeneralCacheAdministrator _cache;

    /**
     * Default constructor
     */
    public CachingFetcher() {
    }

    /**
     * Initialize this fetcher. This method only called when the fetcher is instantiated.
     * 
     * @param servletConfig        Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration blojsom configuration information
     * @throws BlojsomFetcherException If there is an error initializing the fetcher
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws BlojsomFetcherException {
        super.init(servletConfig, blojsomConfiguration);

        String oscachePropertiesIP = servletConfig.getInitParameter(OSCACHE_PROPERTIES_IP);
        if (BlojsomUtils.checkNullOrBlank(oscachePropertiesIP)) {
            oscachePropertiesIP = OSCACHE_PROPERTIES_DEFAULT;
        }

        try {
            Properties oscacheProperties = BlojsomUtils.loadProperties(servletConfig, oscachePropertiesIP);
            if (_cache == null) {
                _cache = new GeneralCacheAdministrator(oscacheProperties);
            }
            _logger.debug("Initialized caching fetcher");
        } catch (BlojsomException e) {
            _logger.error(e);
            throw new BlojsomFetcherException(e);
        }
    }

    /**
     * Fetch a set of {@link org.blojsom.blog.BlogEntry} objects.
     * 
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param user                {@link BlogUser} instance
     * @param flavor              Flavor
     * @param context             Context
     * @return Blog entries retrieved for the particular request
     * @throws BlojsomFetcherException If there is an error retrieving the blog entries for the request
     */
    public BlogEntry[] fetchEntries(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlogUser user, String flavor, Map context) throws BlojsomFetcherException {
        BlogCategory category = (BlogCategory) context.get(STANDARD_FETCHER_CATEGORY);
        context.remove(STANDARD_FETCHER_CATEGORY);
        int blogDirectoryDepth = ((Integer) context.get(STANDARD_FETCHER_DEPTH)).intValue();
        context.remove(STANDARD_FETCHER_DEPTH);
        Blog blog = user.getBlog();

        // Check to see if the requested flavor should be ignored
        if (_ignoreFlavors.indexOf(flavor) != -1) {
            return new BlogEntry[0];
        }

        // Determine if a permalink has been requested
        String permalink = httpServletRequest.getParameter(PERMALINK_PARAM);
        if (permalink != null) {
            permalink = BlojsomUtils.getFilenameForPermalink(permalink, blog.getBlogFileExtensions());
            permalink = BlojsomUtils.urlDecode(permalink);
            if (permalink == null) {
                _logger.error("Permalink request for invalid permalink: " + httpServletRequest.getParameter(PERMALINK_PARAM));
            } else {
                _logger.debug("Permalink request for: " + permalink);
            }
        }

        // Check for a permalink entry request
        if (permalink != null) {
            context.put(BLOJSOM_PERMALINK, permalink);

            String cacheRefresh = user.getBlog().getBlogProperty(CACHING_FETCHER_REFRESH);
            int refreshPeriod;
            if (BlojsomUtils.checkNullOrBlank(cacheRefresh)) {
                refreshPeriod = DEFAULT_CACHE_REFRESH;
            }
            try {
                refreshPeriod = Integer.parseInt(cacheRefresh);
            } catch (NumberFormatException e) {
                refreshPeriod = DEFAULT_CACHE_REFRESH;
            }

            BlogEntry[] permalinkEntry = getPermalinkEntry(user, category, permalink);

            if (blog.getLinearNavigationEnabled().booleanValue()) {
                BlogEntry[] allEntries;
                
                try {
                    allEntries = (BlogEntry[]) _cache.getFromCache(user.getId() + FLAVOR_KEY + flavor, refreshPeriod);
                    _logger.debug("Returned entries from cache for user/flavor: " + user.getId() + " / " + flavor);
                } catch (NeedsRefreshException e) {
                    allEntries = (BlogEntry[]) e.getCacheContent();

                    if (allEntries == null) {
                        allEntries = getEntriesAllCategories(user, flavor, -1, blogDirectoryDepth);
                        _cache.putInCache(user.getId() + FLAVOR_KEY + flavor, allEntries);
                    } else {
                        _cache.cancelUpdate(user.getId() + FLAVOR_KEY + flavor);
                        Thread allCategoriesFetcherThread = new Thread(new AllCategoriesFetcherThread(user, flavor, blogDirectoryDepth));
                        allCategoriesFetcherThread.start();

                        _logger.debug("Returning from all categories fetcher thread for key: " + user.getId() + FLAVOR_KEY + flavor);
                    }
                }

                if (permalinkEntry.length > 0 && allEntries.length > 0) {
                    String permalinkId = permalinkEntry[0].getId();
                    for (int i = 0; i < allEntries.length; i++) {
                        BlogEntry blogEntry = allEntries[i];
                        String blogEntryId = blogEntry.getId();
                        if (blogEntryId != null && blogEntryId.equals(permalinkId)) {
                            if ((i-1) >= 0) {
                                context.put(BLOJSOM_PERMALINK_NEXT_ENTRY, allEntries[i-1]);
                            } else {
                                context.put(BLOJSOM_PERMALINK_NEXT_ENTRY, null);
                            }

                            if ((i+1) < allEntries.length) {
                                context.put(BLOJSOM_PERMALINK_PREVIOUS_ENTRY, allEntries[i+1]);
                            } else {
                                context.put(BLOJSOM_PERMALINK_PREVIOUS_ENTRY, null);
                            }

                            break;
                        }
                    }
                }
            }

            return permalinkEntry;
        } else {
            BlogEntry[] entries;

            String cacheRefresh = user.getBlog().getBlogProperty(CACHING_FETCHER_REFRESH);
            int refreshPeriod;
            if (BlojsomUtils.checkNullOrBlank(cacheRefresh)) {
                refreshPeriod = DEFAULT_CACHE_REFRESH;
            }
            try {
                refreshPeriod = Integer.parseInt(cacheRefresh);
            } catch (NumberFormatException e) {
                refreshPeriod = DEFAULT_CACHE_REFRESH;
            }

            if (category.getCategory().equals("/")) {
                try {
                    entries = (BlogEntry[]) _cache.getFromCache(user.getId() + FLAVOR_KEY + flavor, refreshPeriod);
                    _logger.debug("Returned entries from cache for user/flavor: " + user.getId() + " / " + flavor);

                    return entries;
                } catch (NeedsRefreshException e) {
                    entries = (BlogEntry[]) e.getCacheContent();
                    if (entries == null) {
                        entries = getEntriesAllCategories(user, flavor, -1, blogDirectoryDepth);
                        _cache.putInCache(user.getId() + FLAVOR_KEY + flavor, entries);
                    } else {
                        _cache.cancelUpdate(user.getId() + FLAVOR_KEY + flavor);
                        Thread allCategoriesFetcherThread = new Thread(new AllCategoriesFetcherThread(user, flavor, blogDirectoryDepth));
                        allCategoriesFetcherThread.start();

                        _logger.debug("Returning from all categories fetcher thread for key: " + user.getId() + FLAVOR_KEY + flavor);
                    }

                    return entries;
                }
            } else {
                try {
                    entries = (BlogEntry[]) _cache.getFromCache(user.getId() + CATEGORY_KEY + category.getCategory(), refreshPeriod);
                    _logger.debug("Returned entries from cache for user/category: " + user.getId() + " / " + category.getCategory());

                    return entries;
                } catch (NeedsRefreshException e) {
                    entries = (BlogEntry[]) e.getCacheContent();
                    if (entries == null) {
                        entries = getEntriesForCategory(user, category, -1);
                        _cache.putInCache(user.getId() + CATEGORY_KEY + category.getCategory(), entries);
                    } else {
                        _cache.cancelUpdate(user.getId() + CATEGORY_KEY + category.getCategory());
                        Thread singleCategoryFetcherThread = new Thread(new SingleCategoryFetcherThread(user, category));
                        singleCategoryFetcherThread.start();

                        _logger.debug("Returning from single category fetcher thread for key: " + user.getId() + CATEGORY_KEY + category.getCategory());
                    }

                    return entries;
                }
            }
        }
    }

    /**
     * AllCategoriesFetcherThread
     *
     * @since blojsom 2.05
     */
    private class AllCategoriesFetcherThread implements Runnable {

        private BlogUser _user;
        private String _flavor;
        private int _blogDirectoryDepth;

        /**
         * Default constructor.
         *
         * @param user               Blog user
         * @param flavor             Flavor
         * @param blogDirectoryDepth Blog directory depth
         */
        public AllCategoriesFetcherThread(BlogUser user, String flavor, int blogDirectoryDepth) {
            _user = user;
            _flavor = flavor;
            _blogDirectoryDepth = blogDirectoryDepth;
        }

        /**
         * If this thread was constructed using a separate
         * <code>Runnable</code> run object, then that
         * <code>Runnable</code> object's <code>run</code> method is called;
         * otherwise, this method does nothing and returns.
         * <p/>
         * Subclasses of <code>Thread</code> should override this method.
         *
         * @see Thread#start()
         * @see Thread#stop()
         * @see Thread#Thread(ThreadGroup,
                *      Runnable, String)
         * @see Runnable#run()
         */
        public void run() {
            BlogEntry[] entries = getEntriesAllCategories(_user, _flavor, -1, _blogDirectoryDepth);
            _cache.flushEntry(_user.getId() + FLAVOR_KEY + _flavor);
            _cache.putInCache(_user.getId() + FLAVOR_KEY + _flavor, entries);

            return;
        }
    }

    /**
     * SingleCategoryFetcherThread
     *
     * @since blojsom 2.05
     */
    private class SingleCategoryFetcherThread implements Runnable {

        private BlogUser _user;
        private BlogCategory _category;

        /**
         * Default constructor.
         *
         * @param user     Blog user
         * @param category Blog category
         */
        public SingleCategoryFetcherThread(BlogUser user, BlogCategory category) {
            _user = user;
            _category = category;
        }

        /**
         * If this thread was constructed using a separate
         * <code>Runnable</code> run object, then that
         * <code>Runnable</code> object's <code>run</code> method is called;
         * otherwise, this method does nothing and returns.
         * <p/>
         * Subclasses of <code>Thread</code> should override this method.
         *
         * @see Thread#start()
         * @see Thread#stop()
         * @see Thread#Thread(ThreadGroup,
                *      Runnable, String)
         * @see Runnable#run()
         */
        public void run() {
            BlogEntry[] entries = getEntriesForCategory(_user, _category, -1);
            _cache.flushEntry(_user.getId() + CATEGORY_KEY + _category.getCategory());
            _cache.putInCache(_user.getId() + CATEGORY_KEY + _category.getCategory(), entries);

            return;
        }
    }
}
