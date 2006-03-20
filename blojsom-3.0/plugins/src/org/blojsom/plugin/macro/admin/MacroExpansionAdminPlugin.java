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
package org.blojsom.plugin.macro.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.WebAdminPlugin;
import org.blojsom.plugin.macro.MacroExpansionUtilities;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

/**
 * Macro Expansion Admin Plugin
 *
 * @author David Czarnecki
 * @version $Id: MacroExpansionAdminPlugin.java,v 1.2 2006-03-20 22:50:44 czarneckid Exp $
 * @since blojsom 3.0
 */
public class MacroExpansionAdminPlugin extends WebAdminPlugin {

    private Log _logger = LogFactory.getLog(MacroExpansionAdminPlugin.class);

    private static final String EDIT_MACRO_EXPANSION_SETTINGS_PAGE = "/org/blojsom/plugin/macro/admin/templates/admin-edit-macro-expansion-settings";

    private static final String BLOJSOM_PLUGIN_EDIT_MACRO_EXPANSION_MACROS = "BLOJSOM_PLUGIN_EDIT_MACRO_EXPANSION_MACROS";

    // Localization constants
    private static final String FAILED_MACRO_ADMIN_PERMISSION_KEY = "failed.macro.admin.permission.text";
    private static final String DELETED_MACROS_KEY = "deleted.macros.text";
    private static final String NO_MACROS_SELECTED_TO_DELETE_KEY = "no.macros.selected.to.delete.text";
    private static final String MISSING_MACRO_PARAMETERS_KEY = "missing.macro.parameters.text";
    private static final String ADDED_MACRO_KEY = "added.macro.text";
    private static final String MACRO_EXISTS_KEY = "macro.exists.text";

    // Form items
    private static final String MACRO_SHORT_NAME = "macro-short-name";
    private static final String MACRO_EXPANSION = "macro-expansion";
    private static final String MACROS = "macros";

    // Actions
    private static final String ADD_MACRO_ACTION = "add-macro";
    private static final String DELETE_SELECTED_MACROS_ACTION = "delete-selected-macros";

    // Permissions
    private static final String MACRO_EXPANSION_ADMIN_PERMISSION = "macro_expansion_admin_permission";

    private Fetcher _fetcher;

    /**
     * Default constructor
     */
    public MacroExpansionAdminPlugin() {
    }

    /**
     * Return the display name for the plugin
     *
     * @return Display name for the plugin
     */
    public String getDisplayName() {
        return "Macro Expansion plugin";
    }

    /**
     * Return the name of the initial editing page for the plugin
     *
     * @return Name of the initial editing page for the plugin
     */
    public String getInitialPage() {
        return EDIT_MACRO_EXPANSION_SETTINGS_PAGE;
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
        if (!checkPermission(blog, null, username, MACRO_EXPANSION_ADMIN_PERMISSION)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            addOperationResultMessage(context, getAdminResource(FAILED_MACRO_ADMIN_PERMISSION_KEY, FAILED_MACRO_ADMIN_PERMISSION_KEY, blog.getBlogAdministrationLocale()));

            return entries;
        }

        if (ADMIN_LOGIN_PAGE.equals(page)) {
            return entries;
        } else {
            String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);
            Map macros = MacroExpansionUtilities.readMacros(blog);
            Map updatedBlogProperties = new HashMap(blog.getProperties());

            if (DELETE_SELECTED_MACROS_ACTION.equals(action)) {
                String[] macrosToDelete = httpServletRequest.getParameterValues(MACROS);
                if (macrosToDelete != null && macrosToDelete.length > 0) {
                    for (int i = 0; i < macrosToDelete.length; i++) {
                        String macro = macrosToDelete[i];
                        macros.remove(macro);
                        updatedBlogProperties.remove(MacroExpansionUtilities.PLUGIN_MACRO_PREFIX + macro);
                    }

                    blog.setProperties(updatedBlogProperties);

                    try {
                        _fetcher.saveBlog(blog);
                    } catch (FetcherException e) {
                        if (_logger.isErrorEnabled()) {
                            if (_logger.isErrorEnabled()) {
                                _logger.error(e);
                            }
                        }
                    }

                    addOperationResultMessage(context, formatAdminResource(DELETED_MACROS_KEY, DELETED_MACROS_KEY, blog.getBlogAdministrationLocale(), new Object[]{new Integer(macrosToDelete.length)}));
                } else {
                    addOperationResultMessage(context, getAdminResource(NO_MACROS_SELECTED_TO_DELETE_KEY, NO_MACROS_SELECTED_TO_DELETE_KEY, blog.getBlogAdministrationLocale()));
                }
            } else if (ADD_MACRO_ACTION.equals(action)) {
                String macroShortName = BlojsomUtils.getRequestValue(MACRO_SHORT_NAME, httpServletRequest);
                String macroExpansion = BlojsomUtils.getRequestValue(MACRO_EXPANSION, httpServletRequest);

                if (BlojsomUtils.checkNullOrBlank(macroShortName) || BlojsomUtils.checkNullOrBlank(macroExpansion)) {
                    addOperationResultMessage(context, getAdminResource(MISSING_MACRO_PARAMETERS_KEY, MISSING_MACRO_PARAMETERS_KEY, blog.getBlogAdministrationLocale()));
                } else {
                    if (!macros.containsKey(macroShortName)) {
                        macros.put(macroShortName, macroExpansion);
                        updatedBlogProperties.put(MacroExpansionUtilities.PLUGIN_MACRO_PREFIX + macroShortName, macroExpansion);

                        blog.setProperties(updatedBlogProperties);

                        try {
                            _fetcher.saveBlog(blog);
                        } catch (FetcherException e) {
                            if (_logger.isErrorEnabled()) {
                                _logger.error(e);
                            }
                        }

                        addOperationResultMessage(context, formatAdminResource(ADDED_MACRO_KEY, ADDED_MACRO_KEY, blog.getBlogAdministrationLocale(), new Object[]{macroShortName}));
                    } else {
                        addOperationResultMessage(context, formatAdminResource(MACRO_EXISTS_KEY, MACRO_EXISTS_KEY, blog.getBlogAdministrationLocale(), new Object[]{macroShortName}));
                    }
                }
            }

            context.put(BLOJSOM_PLUGIN_EDIT_MACRO_EXPANSION_MACROS, macros);
        }

        return entries;
    }
}