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
package org.blojsom.plugin.common;

import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.blog.Category;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.util.BlojsomConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

/**
 * Count entries for category plugin
 *
 * @author David Czarnecki
 * @version $Id: CountEntriesForCategoryPlugin.java,v 1.2 2008-07-07 19:54:11 czarneckid Exp $
 * @since blojsom 3.2
 */
public class CountEntriesForCategoryPlugin implements Plugin {

    private Log _logger = LogFactory.getLog(CountEntriesForCategoryPlugin.class);

    private static final String PLUGIN_CONTEXT_ENTRIES_PER_CATEGORY = "PLUGIN_ENTRIES_PER_CATEGORY";

    private Fetcher _fetcher;

    /**
     * Create a new instance of the plugin
     */
    public CountEntriesForCategoryPlugin() {
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
        Map entriesPerCategory = new HashMap();

        try {
            Category[] categories = (Category[]) context.get(BlojsomConstants.BLOJSOM_ALL_CATEGORIES);
            if (categories != null && categories.length > 0) {
                for (int i = 0; i < categories.length; i++) {
                    Category category = categories[i];
                    entriesPerCategory.put(category.getId(), _fetcher.countEntriesForCategory(blog, category));
                }
            }
        } catch (FetcherException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }
        }

        context.put(PLUGIN_CONTEXT_ENTRIES_PER_CATEGORY, entriesPerCategory);

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
