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
package org.blojsom.plugin.technorati;

import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.event.Event;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.event.Listener;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.event.ProcessEntryEvent;
import org.blojsom.plugin.velocity.StandaloneVelocityPlugin;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Technorati tags plugin
 *
 * @author David Czarnecki
 * @version $Id: TechnoratiTagsPlugin.java,v 1.6 2008-07-07 19:54:16 czarneckid Exp $
 * @since blojsom 3.0
 */
public class TechnoratiTagsPlugin extends StandaloneVelocityPlugin implements Listener {

    private static final String TECHNORATI_TAGS_TEMPLATE = "org/blojsom/plugin/technorati/templates/admin-technorati-tags.vm";
    private static final String TECHNORATI_TAG_LINK_TEMPLATE = "org/blojsom/plugin/technorati/templates/technorati-tag-link.vm";
    private static final String TECHNORATI_TAGS = "TECHNORATI_TAGS";
    private static final String TECHNORATI_TAG_LINKS = "TECHNORATI_TAG_LINKS";

    private EventBroadcaster _eventBroadcaster;

    public static final String METADATA_TECHNORATI_TAGS = "technorati-tags";

    /**
     * Create a new instance of the Technorati tag plugin
     */
    public TechnoratiTagsPlugin() {
    }

    /**
     * Set the {@link org.blojsom.event.EventBroadcaster} event broadcaster
     *
     * @param eventBroadcaster {@link org.blojsom.event.EventBroadcaster}
     */
    public void setEventBroadcaster(EventBroadcaster eventBroadcaster) {
        _eventBroadcaster = eventBroadcaster;
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error initializing the plugin
     */
    public void init() throws PluginException {
        super.init();

        _eventBroadcaster.addListener(this);
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
        for (int i = 0; i < entries.length; i++) {
            Entry entry = entries[i];
            Map entryMetaData = entry.getMetaData();

            if (BlojsomUtils.checkMapForKey(entryMetaData, METADATA_TECHNORATI_TAGS)) {
                String[] tags = BlojsomUtils.parseOnlyCommaList((String) entryMetaData.get(METADATA_TECHNORATI_TAGS));
                if (tags != null && tags.length > 0) {
                    ArrayList tagLinks = new ArrayList(tags.length);
                    String tagLinkTemplate = mergeTemplate(TECHNORATI_TAG_LINK_TEMPLATE, blog, new HashMap());
                    for (int j = 0; j < tags.length; j++) {
                        String tag = tags[j].trim();

                        tagLinks.add(MessageFormat.format(tagLinkTemplate, new Object[]{BlojsomUtils.urlEncode(tag),BlojsomUtils.escapeString(tag)}));
                    }

                    entryMetaData.put(TECHNORATI_TAG_LINKS, tagLinks.toArray(new String[tagLinks.size()]));
                }
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

    /**
     * Handle an event broadcast from another component
     *
     * @param event {@link org.blojsom.event.Event} to be handled
     */
    public void handleEvent(Event event) {
    }

    /**
     * Process an event from another component
     *
     * @param event {@link org.blojsom.event.Event} to be handled
     */
    public void processEvent(Event event) {
        if (event instanceof ProcessEntryEvent) {
            ProcessEntryEvent processBlogEntryEvent = (ProcessEntryEvent) event;

            String technoratiTags = BlojsomUtils.getRequestValue(METADATA_TECHNORATI_TAGS, processBlogEntryEvent.getHttpServletRequest());
            if (processBlogEntryEvent.getEntry() != null) {
                String savedTechnoratiTags = (String) processBlogEntryEvent.getEntry().getMetaData().get(METADATA_TECHNORATI_TAGS);
                if (savedTechnoratiTags != null) {
                    if (technoratiTags == null) {
                        // Request parameter not available, save old set of tags
                        technoratiTags = savedTechnoratiTags;
                        // Request parameter blank, so throw away set of tags
                    } else if ("".equals(technoratiTags.trim())) {
                        technoratiTags = "";
                    }
                }

                if ("".equals(technoratiTags)) {
                    processBlogEntryEvent.getEntry().getMetaData().remove(METADATA_TECHNORATI_TAGS);
                } else {
                    processBlogEntryEvent.getEntry().getMetaData().put(METADATA_TECHNORATI_TAGS, technoratiTags);
                }
            }

            Map context = processBlogEntryEvent.getContext();

            Map templateAdditions = (Map) processBlogEntryEvent.getContext().get("BLOJSOM_TEMPLATE_ADDITIONS");
            if (templateAdditions == null) {
                templateAdditions = new TreeMap();
            }

            templateAdditions.put(getClass().getName(), "#parse('" + TECHNORATI_TAGS_TEMPLATE + "')");
            processBlogEntryEvent.getContext().put("BLOJSOM_TEMPLATE_ADDITIONS", templateAdditions);

            context.put(TECHNORATI_TAGS, technoratiTags);

            processBlogEntryEvent.getContext().put(TECHNORATI_TAGS, technoratiTags);
        }
    }
}