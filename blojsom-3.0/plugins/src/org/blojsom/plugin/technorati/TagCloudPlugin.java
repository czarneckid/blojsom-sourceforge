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
package org.blojsom.plugin.technorati;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Tag Cloud plugin
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: TagCloudPlugin.java,v 1.2 2006-03-20 22:51:00 czarneckid Exp $
 */
public class TagCloudPlugin implements Plugin {

    private Log _logger = LogFactory.getLog(TagCloudPlugin.class);

    private static final String TAG_QUERY_PARAM = "tq";
    private static final String BLOJSOM_PLUGIN_TAG_CLOUD_MAP = "BLOJSOM_PLUGIN_TAG_CLOUD_MAP";
    private static final int MIN_FONTSIZE = 1;
    private static final int MAX_FONTSIZE = 10;

    /**
     * Create a new instance of the tag cloud plugin
     */
    public TagCloudPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error initializing the plugin
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
        TreeMap tagMap = new TreeMap();
        String tagQuery = BlojsomUtils.getRequestValue(TAG_QUERY_PARAM, httpServletRequest);
        ArrayList entriesMatchingTagQuery = new ArrayList(10);
        Integer maxTagCount = new Integer(1);

        for (int i = 0; i < entries.length; i++) {
            Entry entry = entries[i];

            if (BlojsomUtils.checkMapForKey(entry.getMetaData(), TechnoratiTagsPlugin.METADATA_TECHNORATI_TAGS)) {
                String[] tags = BlojsomUtils.parseOnlyCommaList((String) entry.getMetaData().get(TechnoratiTagsPlugin.METADATA_TECHNORATI_TAGS));
                String tag;
                if (tags != null && tags.length > 0) {
                    for (int j = 0; j < tags.length; j++) {
                        tag = tags[j].trim();
                        if (!BlojsomUtils.checkNullOrBlank(tagQuery)) {
                            if (tagQuery.equals(tag)) {
                                entriesMatchingTagQuery.add(entries[i]);
                            }
                        }

                        if (tagMap.containsKey(tag)) {
                            Integer tagCount = (Integer) tagMap.get(tag);
                            tagCount = new Integer(tagCount.intValue() + 1);
                            if (tagCount.intValue() > maxTagCount.intValue()) {
                                maxTagCount = new Integer(tagCount.intValue());
                            }

                            tagMap.put(tag, tagCount);
                        } else {
                            tagMap.put(tag, new Integer(1));
                        }
                    }
                }
            }
        }

        Iterator tagKeyIterator = tagMap.keySet().iterator();
        while (tagKeyIterator.hasNext()) {
            String tag = (String) tagKeyIterator.next();
            Integer tagCount = (Integer) tagMap.get(tag);
            int tagRank = rankTagPerEntries(tagCount.intValue(), 1, maxTagCount.intValue());

            if (_logger.isDebugEnabled()) {
                _logger.debug("Tag rank for " + tag + " tag: " + tagRank);
            }
            tagMap.put(tag, new Integer(tagRank));
        }

        context.put(BLOJSOM_PLUGIN_TAG_CLOUD_MAP, tagMap);

        if (!BlojsomUtils.checkNullOrBlank(tagQuery) && entriesMatchingTagQuery.size() > 0) {
            return (Entry[]) entriesMatchingTagQuery.toArray(new Entry[entriesMatchingTagQuery.size()]);
        }

        return entries;
    }

    /**
     * Calculate a scaled ranking for a given tag count and number of entries
     *
     * @param tagCount Total count for a given tag
     * @param minTagCount Minimum number of tags
     * @param maxTagCount Maximum number of tags
     * @return Ranked distribution between 1 and TAG_DISTRIBUTION_MAX
     */
    private int rankTagPerEntries(int tagCount, int minTagCount, int maxTagCount) {
        if (minTagCount == maxTagCount) {
            return MAX_FONTSIZE;
        }

        double scaledCount = (double) (tagCount - minTagCount) / (maxTagCount - minTagCount);
        double scaledSize = scaledCount * (MAX_FONTSIZE - MIN_FONTSIZE) + MIN_FONTSIZE;

        return (int) Math.ceil(scaledSize);
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