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
package org.blojsom.plugin.language;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.event.Event;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.event.Listener;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.event.ProcessEntryEvent;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.TreeMap;

/**
 * Language selection plugin allows you to attach a language attribute to a blog entry.
 *
 * @author David Czarnecki
 * @version $Id: LanguageSelectionPlugin.java,v 1.4 2008-07-07 19:54:17 czarneckid Exp $
 * @since blojsom 3.0
 */
public class LanguageSelectionPlugin implements Plugin, Listener {

    private Log _logger = LogFactory.getLog(LanguageSelectionPlugin.class);

    private static final String LANGUAGE_SELECTION_TEMPLATE = "org/blojsom/plugin/language/templates/admin-language-selection.vm";

    private static final String BLOJSOM_JVM_LANGUAGES = "BLOJSOM_JVM_LANGUAGES";
    private static final String BLOJSOM_PLUGIN_CURRENT_LANGUAGE_SELECTION = "BLOJSOM_PLUGIN_CURRENT_LANGUAGE_SELECTION";
    private static final String METADATA_LANGUAGE = "language";

    private EventBroadcaster _eventBroadcaster;

    /**
     * Create a new instance of the language selection plugin
     */
    public LanguageSelectionPlugin() {
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
            if (_logger.isDebugEnabled()) {
                _logger.debug("Handling process blog entry event");
            }
            ProcessEntryEvent processBlogEntryEvent = (ProcessEntryEvent) event;

            String language = BlojsomUtils.getRequestValue(METADATA_LANGUAGE, processBlogEntryEvent.getHttpServletRequest());
            Map context = processBlogEntryEvent.getContext();

            Map templateAdditions = (Map) processBlogEntryEvent.getContext().get("BLOJSOM_TEMPLATE_ADDITIONS");
            if (templateAdditions == null) {
                templateAdditions = new TreeMap();
            }

            templateAdditions.put(getClass().getName(), "#parse('" + LANGUAGE_SELECTION_TEMPLATE + "')");
            processBlogEntryEvent.getContext().put("BLOJSOM_TEMPLATE_ADDITIONS", templateAdditions);

            context.put(BLOJSOM_JVM_LANGUAGES, BlojsomUtils.getLanguagesForSystem(processBlogEntryEvent.getBlog().getBlogAdministrationLocale()));

            // Preserve the current language selection if none submitted
            if (processBlogEntryEvent.getEntry() != null) {
                String currentLanguage = (String) processBlogEntryEvent.getEntry().getMetaData().get(METADATA_LANGUAGE);
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Current language: " + currentLanguage);
                }
                processBlogEntryEvent.getContext().put(BLOJSOM_PLUGIN_CURRENT_LANGUAGE_SELECTION, currentLanguage);
            }

            if (!BlojsomUtils.checkNullOrBlank(language)) {
                processBlogEntryEvent.getEntry().getMetaData().put(METADATA_LANGUAGE, language);
                processBlogEntryEvent.getContext().put(BLOJSOM_PLUGIN_CURRENT_LANGUAGE_SELECTION, language);
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Added/updated language: " + language);
                }
            } else {
                if (processBlogEntryEvent.getEntry() != null) {
                    processBlogEntryEvent.getEntry().getMetaData().remove(METADATA_LANGUAGE);
                }
                processBlogEntryEvent.getContext().remove(BLOJSOM_PLUGIN_CURRENT_LANGUAGE_SELECTION);
            }
        }
    }
}