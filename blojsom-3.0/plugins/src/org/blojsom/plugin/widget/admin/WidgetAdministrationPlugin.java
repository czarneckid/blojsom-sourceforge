/**
 * Copyright (c) 2003-2007, David A. Czarnecki
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
package org.blojsom.plugin.widget.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.event.Event;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.event.Filter;
import org.blojsom.event.Listener;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.WebAdminPlugin;
import org.blojsom.plugin.widget.event.RegisterWidgetEvent;
import org.blojsom.plugin.widget.WidgetPlugin;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;

/**
 * WidgetAdministrationPlugin
 *
 * @author David Czarnecki
 * @version $Id: WidgetAdministrationPlugin.java,v 1.1 2007-03-26 18:17:19 czarneckid Exp $
 * @since blojsom 3.2
 */
public class WidgetAdministrationPlugin extends WebAdminPlugin implements Listener {

    private Log _logger = LogFactory.getLog(WidgetAdministrationPlugin.class);

    // Constants
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_WIDGETS_MAP = "BLOJSOM_PLUGIN_EDIT_BLOG_WIDGETS_MAP";
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_WIDGETS_AVAILABLE_WIDGETS = "BLOJSOM_PLUGIN_EDIT_BLOG_WIDGETS_AVAILABLE_WIDGETS";

    // Localization constants
    private static final String FAILED_WIDGET_PERMISSIONS_KEY = "failed.widget.permissions.text";
    private static final String SUCCESSFULLY_UPDATED_WIDGETS_KEY = "successfully.updated.widgets.text";
    private static final String FAILED_UPDATE_WIDGETS_KEY = "failed.update.widgets.text";

    // Pages
    private static final String WIDGET_CONFIGURATION_PAGE = "/org/blojsom/plugin/widget/admin/templates/admin-widget-configuration";

    // Permissions
    private static final String WIDGET_ADMIN_PERMISSION = "widget_admin_permission";

    // Actions
    private static final String UPDATE_WIDGETS_ACTION = "update-widgets";
    private static final String BLOJSOM_WIDGET_CHAIN = "blojsom-widget-chain";

    private EventBroadcaster _eventBroadcaster;
    private Fetcher _fetcher;
    private Map _widgets;

    /**
     * Create a new instance of the Widget administration plugin
     */
    public WidgetAdministrationPlugin() {
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
        super.init();

        _widgets = new HashMap();
        _eventBroadcaster.addListener(this, new Filter() {

            /**
             * Determines whether or not a particular event should be processed
             *
             * @param event {@link Event} to be processed
             * @return <code>true</code> if the event should be processed, <code>false</code> otherwise
             */
            public boolean processEvent(Event event) {
                return (event instanceof RegisterWidgetEvent);
            }
        });
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
        entries = super.process(httpServletRequest, httpServletResponse, blog, context, entries);

        String page = BlojsomUtils.getRequestValue(BlojsomConstants.PAGE_PARAM, httpServletRequest);

        String username = getUsernameFromSession(httpServletRequest, blog);
        if (!checkPermission(blog, null, username, WIDGET_ADMIN_PERMISSION)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            addOperationResultMessage(context, getAdminResource(FAILED_WIDGET_PERMISSIONS_KEY, FAILED_WIDGET_PERMISSIONS_KEY, blog.getBlogAdministrationLocale()));

            return entries;
        }

        if (ADMIN_LOGIN_PAGE.equals(page)) {
            return entries;
        } else {
            String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);

            Map widgetChain = new TreeMap(blog.getPlugins());

            // Iterate over the user's flavors and setup the widget chains
            Iterator flavorIterator = blog.getTemplates().keySet().iterator();
            String widgetsToRun;
            while (flavorIterator.hasNext()) {
                String flavor = (String) flavorIterator.next();
                widgetsToRun = blog.getProperty(WidgetPlugin.PLUGIN_WIDGETS + "-" + flavor);
                if (!BlojsomUtils.checkNullOrBlank(widgetsToRun)) {
                    widgetChain.put(flavor, widgetsToRun);
                } else {
                    widgetChain.put(flavor, "");
                }
            }

            // Check for the default flavor
            widgetsToRun = blog.getProperty(WidgetPlugin.PLUGIN_WIDGETS + "-default");
            if (BlojsomUtils.checkNullOrBlank(widgetsToRun)) {
                widgetsToRun = "";
            }
            widgetChain.put("default", widgetsToRun);
            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_WIDGETS_MAP, widgetChain);

            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_WIDGETS_AVAILABLE_WIDGETS, _widgets);
            
            if (UPDATE_WIDGETS_ACTION.equals(action)) {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("User requested modify widgets action");
                }

                // Update the internal widget chain map for the user per flavor
                flavorIterator = widgetChain.keySet().iterator();
                while (flavorIterator.hasNext()) {
                    String flavor = (String) flavorIterator.next();
                    widgetsToRun = BlojsomUtils.getRequestValue(flavor + "." + BLOJSOM_WIDGET_CHAIN, httpServletRequest);
                    if (BlojsomUtils.checkNullOrBlank(widgetsToRun)) {
                        widgetsToRun = "";
                    }
                    
                    blog.setProperty(WidgetPlugin.PLUGIN_WIDGETS + "-" + flavor, widgetsToRun);
                    widgetChain.put(flavor, widgetsToRun);
                }

                // Save the updated widget configuration
                try {
                    _fetcher.saveBlog(blog);
                    addOperationResultMessage(context, getAdminResource(SUCCESSFULLY_UPDATED_WIDGETS_KEY, SUCCESSFULLY_UPDATED_WIDGETS_KEY, blog.getBlogAdministrationLocale()));
                } catch (FetcherException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }

                    addOperationResultMessage(context, getAdminResource(FAILED_UPDATE_WIDGETS_KEY, FAILED_UPDATE_WIDGETS_KEY, blog.getBlogAdministrationLocale()));
                }

                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_WIDGETS_MAP, widgetChain);
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, WIDGET_CONFIGURATION_PAGE);
            }
        }

        return entries;
    }

    /**
     * Return the display name for the plugin
     *
     * @return Display name for the plugin
     */
    public String getDisplayName() {
        return "Widget Configuration plugin";
    }

    /**
     * Return the name of the initial editing page for the plugin
     *
     * @return Name of the initial editing page for the plugin
     */
    public String getInitialPage() {
        return WIDGET_CONFIGURATION_PAGE;
    }

    /**
     * Handle an event broadcast from another component
     *
     * @param event {@link Event} to be handled
     */
    public void handleEvent(Event event) {
        RegisterWidgetEvent registerWidgetEvent = (RegisterWidgetEvent) event;

        _widgets.put(registerWidgetEvent.getWidget(), registerWidgetEvent.getDisplayName());

        if (_logger.isDebugEnabled()) {
            _logger.debug("Registered widget: " + registerWidgetEvent.getWidget() +
                    "with display name: " + registerWidgetEvent.getDisplayName());
        }
    }

    /**
     * Process an event from another component
     *
     * @param event {@link Event} to be handled
     */
    public void processEvent(Event event) {
    }
}
