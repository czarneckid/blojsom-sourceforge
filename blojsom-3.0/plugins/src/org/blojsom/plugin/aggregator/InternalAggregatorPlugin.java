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
package org.blojsom.plugin.aggregator;

import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.blog.Category;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.BlojsomConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

/**
 * Internal Aggregator plugin
 * 
 * @author David Czarnecki
 * @since blojsom 3.1
 * @version $Id: InternalAggregatorPlugin.java,v 1.3 2006-10-26 21:02:50 czarneckid Exp $
 */
public class InternalAggregatorPlugin implements Plugin {

    private Log _logger = LogFactory.getLog(InternalAggregatorPlugin.class);

    private static final String BLOJSOM_PLUGIN_AGGREGATOR_BLOGS = "BLOJSOM_PLUGIN_AGGREGATOR_BLOGS";
    private static final int DEFAULT_PAGE_SIZE = 15;

    private Fetcher _fetcher;

    /**
     * Construct a new instance of the Internal Aggregator plugin
     */
    public InternalAggregatorPlugin() {
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
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws PluginException If there is an error initializing the plugin
     */
    public void init() throws PluginException {
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
        Category category = (Category) context.get(BlojsomConstants.BLOJSOM_REQUESTED_CATEGORY);

        String pgNum = BlojsomUtils.getRequestValue(BlojsomConstants.PAGE_NUMBER_PARAM, httpServletRequest);
        int page;
        try {
            page = Integer.parseInt(pgNum);
        } catch (NumberFormatException e) {
            page = 1;
        }

        int pageSize = blog.getBlogDisplayEntries();
        if (pageSize <= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        Map blogs = new HashMap();

        try {
            entries = _fetcher.loadEntries(pageSize, page, category, _fetcher.loadAllCategories(blog));
            for (int i = 0; i < entries.length; i++) {
                Entry entry = entries[i];
                if (!blogs.containsKey(entry.getBlogId())) {
                    blogs.put(entry.getBlogId(), _fetcher.loadBlog(entry.getBlogId()));
                }
            }
        } catch (FetcherException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }
        }

        context.put(BLOJSOM_PLUGIN_AGGREGATOR_BLOGS, blogs);

        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws PluginException If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws PluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws PluginException If there is an error in finalizing this plugin
     */
    public void destroy() throws PluginException {
    }
}
