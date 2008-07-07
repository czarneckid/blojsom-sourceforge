/**
 * Copyright (c) 2003-2008, David A. Czarnecki
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
package org.blojsom.plugin.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.event.ProcessRequestEvent;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * ThemeSwitcherPlugin
 *
 * @author David Czarnecki
 * @version $Id: ThemeSwitcherPlugin.java,v 1.4 2008-07-07 19:54:12 czarneckid Exp $
 * @since blojsom 3.0
 */
public class ThemeSwitcherPlugin extends WebAdminPlugin {

    private Log _logger = LogFactory.getLog(ThemeSwitcherPlugin.class);

    // Localization constants
    private static final String FAILED_PERMISSION_KEY = "failed.theme.switch.permission.text";
    private static final String NONE_SELECTED_KEY = "no.theme.flavor.selected.text";
    private static final String ADMIN_FLAVOR_PROTECTED_KEY = "admin.flavor.protected.text";
    private static final String FAILED_THEME_TEMPLATE_COPY_KEY = "failed.theme.template.copy.text";
    private static final String FAILED_FLAVOR_WRITE_KEY = "failed.flavor.write.text";
    private static final String THEME_SWITCHED_KEY = "theme.switched.text";

    // Pages
    private static final String THEME_SWITCHER_SETTINGS_PAGE = "/org/blojsom/plugin/admin/templates/admin-theme-switcher-settings";

    // Context variables
    private static final String THEME_SWITCHER_PLUGIN_AVAILABLE_THEMES = "THEME_SWITCHER_PLUGIN_AVAILABLE_THEMES";
    private static final String THEME_SWITCHER_PLUGIN_FLAVORS = "THEME_SWITCHER_PLUGIN_FLAVORS";
    private static final String THEME_SWITCHER_PLUGIN_DEFAULT_FLAVOR = "THEME_SWITCHER_PLUGIN_DEFAULT_FLAVOR";
    private static final String CURRENT_HTML_THEME = "CURRENT_HTML_THEME";

    // Actions
    private static final String SWITCH_THEME_ACTION = "switch-theme";

    // Form items
    private static final String THEME = "theme";
    private static final String FLAVOR = "flavor-name";

    // Permissions
    private static final String SWITCH_THEME_PERMISSION = "switch_theme_permission";
    private static final String DEFAULT_THEMES_DIRECTORY = "/themes/";
    private static final String THEMES_DIRECTORY_IP = "themes-directory";

    private String _themesDirectory;
    private Properties _blojsomProperties;
    private Fetcher _fetcher;
    private EventBroadcaster _eventBroadcaster;

    /**
     * Default constructor
     */
    public ThemeSwitcherPlugin() {
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
     * Set the {@link Fetcher}
     *
     * @param fetcher {@link Fetcher}
     */
    public void setFetcher(Fetcher fetcher) {
        _fetcher = fetcher;
    }

    /**
     * Set the default blojsom properties
     *
     * @param blojsomProperties Default blojsom properties
     */
    public void setBlojsomProperties(Properties blojsomProperties) {
        _blojsomProperties = blojsomProperties;
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error initializing the plugin
     */
    public void init() throws PluginException {
        super.init();

        _themesDirectory = _blojsomProperties.getProperty(THEMES_DIRECTORY_IP);
        if (BlojsomUtils.checkNullOrBlank(_themesDirectory)) {
            _themesDirectory = DEFAULT_THEMES_DIRECTORY;
        }

        _themesDirectory = BlojsomUtils.checkStartingAndEndingSlash(_themesDirectory);
    }

    /**
     * Return the display name for the plugin
     *
     * @return Display name for the plugin
     */
    public String getDisplayName() {
        return "Theme Switcher plugin";
    }

    /**
     * Return the name of the initial editing page for the plugin
     *
     * @return Name of the initial editing page for the plugin
     */
    public String getInitialPage() {
        return THEME_SWITCHER_SETTINGS_PAGE;
    }

    /**
     * Retrieve the list of directories (theme names) from the themes installation directory
     *
     * @return List of theme names available
     */
    protected String[] getAvailableThemes() {
        ArrayList themes = new ArrayList(0);

        File themesDirectory = new File(_servletConfig.getServletContext().getRealPath("/") + BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + _themesDirectory);

        if (themesDirectory.exists() && themesDirectory.isDirectory()) {
            File[] themesInstalled = themesDirectory.listFiles(BlojsomUtils.getDirectoryFilter());
            if (themesInstalled != null && themesInstalled.length > 0) {
                for (int i = 0; i < themesInstalled.length; i++) {
                    File installedTheme = themesInstalled[i];
                    themes.add(installedTheme.getName());
                }
            }
        }

        String[] availableThemes = (String[]) themes.toArray(new String[themes.size()]);
        Arrays.sort(availableThemes);

        return availableThemes;
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
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error processing the blog entries
     */
    public Entry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, Map context, Entry[] entries) throws PluginException {
        entries = super.process(httpServletRequest, httpServletResponse, blog, context, entries);
        String page = BlojsomUtils.getRequestValue(BlojsomConstants.PAGE_PARAM, httpServletRequest);

        String username = getUsernameFromSession(httpServletRequest, blog);
        if (!checkPermission(blog, null, username, SWITCH_THEME_PERMISSION)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            addOperationResultMessage(context, getAdminResource(FAILED_PERMISSION_KEY, FAILED_PERMISSION_KEY, blog.getBlogAdministrationLocale()));

            return entries;
        }

        if (ADMIN_LOGIN_PAGE.equals(page)) {
            return entries;
        } else {
            String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);

            context.put(THEME_SWITCHER_PLUGIN_AVAILABLE_THEMES, getAvailableThemes());
            context.put(THEME_SWITCHER_PLUGIN_FLAVORS, new TreeMap(blog.getTemplates()));
            context.put(THEME_SWITCHER_PLUGIN_DEFAULT_FLAVOR, blog.getBlogDefaultFlavor());
            String currentHtmlFlavor = (String) blog.getTemplates().get(BlojsomConstants.DEFAULT_FLAVOR_HTML);
            currentHtmlFlavor = currentHtmlFlavor.substring(0, currentHtmlFlavor.indexOf('.'));
            context.put(CURRENT_HTML_THEME, currentHtmlFlavor);

            if (SWITCH_THEME_ACTION.equals(action)) {
                String theme = BlojsomUtils.getRequestValue(THEME, httpServletRequest);
                String flavor = BlojsomUtils.getRequestValue(FLAVOR, httpServletRequest);

                if (BlojsomUtils.checkNullOrBlank(theme) || BlojsomUtils.checkNullOrBlank(flavor)) {
                    addOperationResultMessage(context, getAdminResource(NONE_SELECTED_KEY, NONE_SELECTED_KEY, blog.getBlogAdministrationLocale()));
                    return entries;
                }

                if ("admin".equalsIgnoreCase(flavor)) {
                    addOperationResultMessage(context, getAdminResource(ADMIN_FLAVOR_PROTECTED_KEY, ADMIN_FLAVOR_PROTECTED_KEY, blog.getBlogAdministrationLocale()));
                    return entries;
                }

                File copyFromTemplatesDirectory = new File(_servletConfig.getServletContext().getRealPath("/") +
                    BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + _themesDirectory + theme +
                    "/" + _blojsomProperties.getProperty(BlojsomConstants.TEMPLATES_DIRECTORY_IP));

                File[] templateFiles = copyFromTemplatesDirectory.listFiles();
                String mainTemplate = null;

                if (templateFiles != null && templateFiles.length > 0) {
                    for (int i = 0; i < templateFiles.length; i++) {
                        File templateFile = templateFiles[i];
                        if (!templateFile.isDirectory()) {
                            if (templateFile.getName().startsWith(theme + ".")) {
                                mainTemplate = templateFile.getName();
                            }
                        }
                    }
                }

                File copyToTemplatesDirectory = new File(_servletConfig.getServletContext().getRealPath("/") +
                    BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY +
                    _blojsomProperties.getProperty(BlojsomConstants.BLOGS_DIRECTORY_IP) + blog.getBlogId() +
                    "/" + _blojsomProperties.getProperty(BlojsomConstants.TEMPLATES_DIRECTORY_IP));

                try {
                    BlojsomUtils.copyDirectory(copyFromTemplatesDirectory, copyToTemplatesDirectory);
                } catch (IOException e) {
                    _logger.error(e);
                    addOperationResultMessage(context, getAdminResource(FAILED_THEME_TEMPLATE_COPY_KEY, FAILED_THEME_TEMPLATE_COPY_KEY, blog.getBlogAdministrationLocale()));
                }

                File copyFromResourcesDirectory = new File(_servletConfig.getServletContext().getRealPath("/") +
                    BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + _themesDirectory + theme + "/" +
                    _blojsomProperties.getProperty(BlojsomConstants.RESOURCES_DIRECTORY_IP));
                File copyToResourcesDirectory = new File(_servletConfig.getServletContext().getRealPath("/") +
                    _blojsomProperties.getProperty(BlojsomConstants.RESOURCES_DIRECTORY_IP) + blog.getBlogId() + "/");

                try {
                    BlojsomUtils.copyDirectory(copyFromResourcesDirectory, copyToResourcesDirectory);
                } catch (IOException e) {
                    _logger.error(e);
                    addOperationResultMessage(context, getAdminResource(FAILED_THEME_TEMPLATE_COPY_KEY, FAILED_THEME_TEMPLATE_COPY_KEY, blog.getBlogAdministrationLocale()));
                }

                try {
                    if (mainTemplate == null) {
                        mainTemplate = (String) blog.getTemplates().get(flavor);

                        _logger.debug("No main template supplied for " + theme + " theme. Using existing template for flavor: " + mainTemplate);
                    } else {
                        if (BlojsomConstants.DEFAULT_FLAVOR_HTML.equals(flavor)) {
                            mainTemplate += ", " + "text/html;charset=UTF-8";
                        }
                    }

                    Map templates = new HashMap(blog.getTemplates());
                    templates.put(flavor, mainTemplate);
                    blog.setTemplates(templates);

                    _fetcher.saveBlog(blog);
                } catch (FetcherException e) {
                    _logger.error(e);
                    addOperationResultMessage(context, getAdminResource(FAILED_FLAVOR_WRITE_KEY, FAILED_FLAVOR_WRITE_KEY, blog.getBlogAdministrationLocale()));

                    return entries;
                }

                currentHtmlFlavor = (String) blog.getTemplates().get(BlojsomConstants.DEFAULT_FLAVOR_HTML);
                currentHtmlFlavor = currentHtmlFlavor.substring(0, currentHtmlFlavor.indexOf('.'));
                context.put(CURRENT_HTML_THEME, currentHtmlFlavor);

                addOperationResultMessage(context, formatAdminResource(THEME_SWITCHED_KEY, THEME_SWITCHED_KEY, blog.getBlogAdministrationLocale(), new Object[]{theme, flavor}));
                _eventBroadcaster.processEvent(new ProcessRequestEvent(this, new Date(), blog, httpServletRequest, httpServletResponse, context));
            } else {
                _eventBroadcaster.processEvent(new ProcessRequestEvent(this, new Date(), blog, httpServletRequest, httpServletResponse, context));

                context.put(THEME_SWITCHER_PLUGIN_AVAILABLE_THEMES, getAvailableThemes());
            }
        }

        return entries;
    }
}