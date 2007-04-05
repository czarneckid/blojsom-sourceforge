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
package org.blojsom.plugin.showmore.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.WebAdminPlugin;
import org.blojsom.plugin.showmore.ShowMeMoreConfiguration;
import org.blojsom.plugin.showmore.ShowMeMorePlugin;
import org.blojsom.plugin.showmore.ShowMeMoreUtilities;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Show Me More administration plugin
 *
 * @author David Czarnecki
 * @version $Id: ShowMeMoreAdminPlugin.java,v 1.2 2007-04-05 01:19:39 czarneckid Exp $
 * @since blojsom 3.2
 */
public class ShowMeMoreAdminPlugin extends WebAdminPlugin {

    private Log _logger = LogFactory.getLog(ShowMeMoreAdminPlugin.class);

    // Localization constants
    private static final String FAILED_SHOWMEMORE_PERMISSION_KEY = "failed.showmemore.permission.text";
    private static final String FAILED_SHOWMEMORE_CONFIGURATION_SAVE_KEY = "failed.showmemore.configuration.save.text";
    private static final String SAVED_SHOWMEMORE_CONFIGURATION_KEY = "saved.showmemore.configuration.text";

    // Pages
    private static final String EDIT_SHOWMEMORE_SETTINGS_PAGE = "/org/blojsom/plugin/showmore/admin/templates/admin-edit-showmemore-settings";

    // Actions
    private static final String UPDATE_SHOWMEMORE_SETTINGS = "update-showmemore-settings";

    // Context attributes
    private static final String SHOWMEMORE_CONFIGURATION = "SHOWMEMORE_CONFIGURATION";

    // Permissions
    private static final String SHOWMEMORE_ADMIN_PERMISSION = "showmemore_admin_permission";

    private Fetcher _fetcher;

    /**
     * Default constructor
     */
    public ShowMeMoreAdminPlugin() {
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
     * Return the display name for the plugin
     *
     * @return Display name for the plugin
     */
    public String getDisplayName() {
        return "Show Me More plugin";
    }

    /**
     * Return the name of the initial editing page for the plugin
     *
     * @return Name of the initial editing page for the plugin
     */
    public String getInitialPage() {
        return EDIT_SHOWMEMORE_SETTINGS_PAGE;
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
        if (!checkPermission(blog, null, username, SHOWMEMORE_ADMIN_PERMISSION)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            addOperationResultMessage(context, getAdminResource(FAILED_SHOWMEMORE_PERMISSION_KEY, FAILED_SHOWMEMORE_PERMISSION_KEY, blog.getBlogAdministrationLocale()));

            return entries;
        }

        if (ADMIN_LOGIN_PAGE.equals(page)) {
            return entries;
        } else {
            String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);

            ShowMeMoreConfiguration showMeMoreConfiguration;
            if (UPDATE_SHOWMEMORE_SETTINGS.equals(action)) {
                String cutoffLength = BlojsomUtils.getRequestValue(ShowMeMorePlugin.ENTRY_LENGTH_CUTOFF, httpServletRequest);
                int entryLengthCutoff = ShowMeMorePlugin.ENTRY_TEXT_CUTOFF_DEFAULT;
                try {
                    entryLengthCutoff = Integer.parseInt(cutoffLength);
                } catch (NumberFormatException e) {
                }

                String entryTextCutoff = BlojsomUtils.getRequestValue(ShowMeMorePlugin.ENTRY_TEXT_CUTOFF, httpServletRequest);
                String showMoreText = BlojsomUtils.getRequestValue(ShowMeMorePlugin.SHOW_ME_MORE_TEXT, httpServletRequest);
                String textCutoffStart = BlojsomUtils.getRequestValue(ShowMeMorePlugin.ENTRY_TEXT_CUTOFF_START, httpServletRequest);
                String textCutoffEnd = BlojsomUtils.getRequestValue(ShowMeMorePlugin.ENTRY_TEXT_CUTOFF_END, httpServletRequest);
                showMeMoreConfiguration = new ShowMeMoreConfiguration(entryLengthCutoff, entryTextCutoff, showMoreText, textCutoffStart, textCutoffEnd);

                blog.setProperty(ShowMeMorePlugin.ENTRY_LENGTH_CUTOFF, Integer.toString(entryLengthCutoff));
                blog.setProperty(ShowMeMorePlugin.ENTRY_TEXT_CUTOFF, showMeMoreConfiguration.getTextCutoff());
                blog.setProperty(ShowMeMorePlugin.SHOW_ME_MORE_TEXT, showMeMoreConfiguration.getMoreText());
                blog.setProperty(ShowMeMorePlugin.ENTRY_TEXT_CUTOFF_START, showMeMoreConfiguration.getTextCutoffStart());
                blog.setProperty(ShowMeMorePlugin.ENTRY_TEXT_CUTOFF_END, showMeMoreConfiguration.getTextCutoffEnd());

                try {
                    _fetcher.saveBlog(blog);

                    addOperationResultMessage(context, getAdminResource(SAVED_SHOWMEMORE_CONFIGURATION_KEY,
                            SAVED_SHOWMEMORE_CONFIGURATION_KEY, blog.getBlogAdministrationLocale()));
                } catch (FetcherException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }

                    addOperationResultMessage(context, getAdminResource(FAILED_SHOWMEMORE_CONFIGURATION_SAVE_KEY,
                            FAILED_SHOWMEMORE_CONFIGURATION_SAVE_KEY, blog.getBlogAdministrationLocale()));
                }
            }

            showMeMoreConfiguration = ShowMeMoreUtilities.loadConfiguration(blog);
            context.put(SHOWMEMORE_CONFIGURATION, showMeMoreConfiguration);
        }

        return entries;
    }
}