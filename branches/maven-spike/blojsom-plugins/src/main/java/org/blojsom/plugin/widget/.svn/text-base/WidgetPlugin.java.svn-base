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
package org.blojsom.plugin.widget;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.widget.event.ProcessWidgetRequest;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.BlojsomConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Widget plugin
 *
 * @author David Czarnecki
 * @version $Id: WidgetPlugin.java,v 1.2 2008-07-07 19:54:28 czarneckid Exp $
 * @since blojsom 3.2
 */
public class WidgetPlugin implements Plugin {

    private Log _logger = LogFactory.getLog(WidgetPlugin.class);

    private static final String BLOJSOM_WIDGET_TEMPLATES = "BLOJSOM_WIDGET_TEMPLATES";
    private static final String BLOJSOM_WIDGET_TEMPLATES_BY_WIDGET = "BLOJSOM_WIDGET_TEMPLATES_BY_WIDGET";
    public static final String PLUGIN_WIDGETS = "plugin-widgets";

    private EventBroadcaster _eventBroadcaster;

    /**
     * Construct a new instance of the Widget plugin
     */
    public WidgetPlugin() {
    }

    /**
     * Set the {@link EventBroadcaster}
     *
     * @param eventBroadcaster {@link EventBroadcaster}
     */
    public void setEventBroadcaster(EventBroadcaster eventBroadcaster) {
        _eventBroadcaster = eventBroadcaster;
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
        String currentFlavor = (String) context.get(BlojsomConstants.BLOJSOM_REQUESTED_FLAVOR);
        String widgetsWithFlavor = PLUGIN_WIDGETS + "-" + currentFlavor;
        String widgetsToRun = blog.getProperty(widgetsWithFlavor);

        // Try to look for "plugin-widgets-<current requested flavor>" first and fallback to a default set of widgets
        if (BlojsomUtils.checkNullOrBlank(widgetsToRun)) {
            widgetsToRun = blog.getProperty(PLUGIN_WIDGETS + "-default");
        }

        if (!BlojsomUtils.checkNullOrBlank(widgetsToRun)) {
            String[] widgets = BlojsomUtils.parseOnlyCommaList(widgetsToRun, true);
            ArrayList widgetTemplates = new ArrayList(widgets.length);
            Map widgetTemplatesByWidget = new HashMap(widgets.length);

            for (int i = 0; i < widgets.length; i++) {
                String widget = widgets[i];

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Sending process widget request for widget: " + widget);
                }

                ProcessWidgetRequest processWidgetRequest = new ProcessWidgetRequest(this, new Date(), widget,
                    httpServletRequest, httpServletResponse, blog, context, "");

                _eventBroadcaster.processEvent(processWidgetRequest);

                widgetTemplates.add(processWidgetRequest.getWidgetTemplate());
                widgetTemplatesByWidget.put(widget, processWidgetRequest.getWidgetTemplate());
            }

            context.put(BLOJSOM_WIDGET_TEMPLATES, widgetTemplates);
            context.put(BLOJSOM_WIDGET_TEMPLATES_BY_WIDGET, widgetTemplatesByWidget);
        }

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
