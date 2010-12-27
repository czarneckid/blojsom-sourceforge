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
package org.blojsom.plugin.markup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Entry;
import org.blojsom.blog.Blog;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.event.Listener;
import org.blojsom.event.Event;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.event.ProcessEntryEvent;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Markup selection plugin allows an individual to select a markup filter to apply
 * to their blog entry.
 *
 * @author David Czarnecki
 * @version $Id: MarkupSelectionPlugin.java,v 1.3 2008-07-07 19:54:15 czarneckid Exp $
 * @since blojsom 3.0
 */
public class MarkupSelectionPlugin implements Plugin, Listener {

    private Log _logger = LogFactory.getLog(MarkupSelectionPlugin.class);

    private static final String PLUGIN_MARKUP_SELECTION_IP = "plugin-markup-selection";
    private static final String BLOJSOM_PLUGIN_MARKUP_SELECTIONS = "BLOJSOM_PLUGIN_MARKUP_SELECTIONS";
    private static final String MARKUP_SELECTION_TEMPLATE = "org/blojsom/plugin/markup/templates/admin-markup-selection-attachment.vm";
    private static final String MARKUP_SELECTIONS = "markup-selections";

    private EventBroadcaster _eventBroadcaster;
    private ServletConfig _servletConfig;

    private Map _markupSelections;

    /**
     * Create a new instance of the markup selection plugin
     */
    public MarkupSelectionPlugin() {
    }

    /**
     * Set the {@link EventBroadcaster} event broadcaster
     *
     * @param eventBroadcaster {@link EventBroadcaster}
     */
    public void setEventBroadcaster(EventBroadcaster eventBroadcaster) {
        _eventBroadcaster = eventBroadcaster;
    }

    /**
     * Set the {@link ServletConfig} for the fetcher to grab initialization parameters
     *
     * @param servletConfig {@link ServletConfig}
     */
    public void setServletConfig(ServletConfig servletConfig) {
        _servletConfig = servletConfig;
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error initializing the plugin
     */
    public void init() throws PluginException {
        String markupSelection = _servletConfig.getInitParameter(PLUGIN_MARKUP_SELECTION_IP);
        _markupSelections = new TreeMap();
        if (!BlojsomUtils.checkNullOrBlank(markupSelection)) {
            String[] markupTypes = BlojsomUtils.parseCommaList(markupSelection);
            for (int i = 0; i < markupTypes.length; i++) {
                String markupType = markupTypes[i];
                String[] markupNameAndKey = BlojsomUtils.parseDelimitedList(markupType, ":");
                if (markupNameAndKey != null && markupNameAndKey.length == 2) {
                    _markupSelections.put(markupNameAndKey[0], markupNameAndKey[1]);
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Added markup type and key: " + markupNameAndKey[0] + ":" + markupNameAndKey[1]);
                    }
                }
            }
        }

        _eventBroadcaster.addListener(this);
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param blog                {@link org.blojsom.blog.Blog} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws PluginException If there is an error processing the blog entries
     */
    public Entry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, Map context, Entry[] entries) throws PluginException {
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
            _logger.debug("Handling process blog entry event");

            if (!_markupSelections.isEmpty()) {
                ProcessEntryEvent processEntryEvent = (ProcessEntryEvent) event;
                Map templateAdditions = (Map) processEntryEvent.getContext().get("BLOJSOM_TEMPLATE_ADDITIONS");
                if (templateAdditions == null) {
                    templateAdditions = new TreeMap();
                }

                templateAdditions.put(getClass().getName(), "#parse('" + MARKUP_SELECTION_TEMPLATE + "')");
                processEntryEvent.getContext().put("BLOJSOM_TEMPLATE_ADDITIONS", templateAdditions);

                processEntryEvent.getContext().put(BLOJSOM_PLUGIN_MARKUP_SELECTIONS, Collections.unmodifiableMap(_markupSelections));

                String[] markupSelections = processEntryEvent.getHttpServletRequest().getParameterValues(MARKUP_SELECTIONS);
                Entry entry = processEntryEvent.getEntry();

                if (markupSelections != null && markupSelections.length > 0) {
                    // Remove the markup selections if the user selections the blank option
                    if (markupSelections.length == 1 && "".equals(markupSelections[0])) {
                        Iterator markupSelectionsIterator = _markupSelections.values().iterator();
                        while (markupSelectionsIterator.hasNext()) {
                            entry.getMetaData().remove(markupSelectionsIterator.next().toString());
                        }
                    } else {
                        // Otherwise, set the new markup selections
                        for (int i = 0; i < markupSelections.length; i++) {
                            String markupSelection = markupSelections[i];
                            entry.getMetaData().put(markupSelection, Boolean.TRUE.toString());
                        }
                    }
                }
            } else {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("No markup selections available");
                }
            }
        }
    }
}