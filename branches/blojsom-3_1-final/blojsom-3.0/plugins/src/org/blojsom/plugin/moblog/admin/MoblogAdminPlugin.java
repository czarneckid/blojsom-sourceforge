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
package org.blojsom.plugin.moblog.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.WebAdminPlugin;
import org.blojsom.plugin.moblog.MoblogPlugin;
import org.blojsom.plugin.moblog.MoblogPluginUtils;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Moblog Admin Plugin
 *
 * @author David Czarnecki
 * @version $Id: MoblogAdminPlugin.java,v 1.2 2007-01-17 02:35:12 czarneckid Exp $
 * @since blojsom 3.0
 */
public class MoblogAdminPlugin extends WebAdminPlugin {

    private Log _logger = LogFactory.getLog(MoblogAdminPlugin.class);

    private static final String EDIT_MOBLOG_SETTINGS_PAGE = "/org/blojsom/plugin/moblog/admin/templates/admin-edit-moblog-settings";

    // Localization constants
    private static final String FAILED_MOBLOG_PERMISSIONS_KEY = "failed.moblog.permissions.text";
    private static final String UPDATED_MOBLOG_CONFIGURATION_KEY = "updated.moblog.configuration.text";
    private static final String FAILED_WRITE_MOBLOG_CONFIGURATION_KEY = "failed.write.moblog.configuration.text";
    private static final String ADDED_AUTHORIZED_EMAIL_KEY = "added.authorized.email.text";
    private static final String FAILED_ADD_EMAIL_KEY = "failed.add.email.text";
    private static final String NO_EMAIL_ADDRESS_KEY = "no.email.address.text";
    private static final String REMOVED_AUTHORIZED_EMAIL_KEY = "removed.authorized.email.text";
    private static final String FAILED_DELETE_EMAIL_KEY = "failed.delete.email.text";

    // Form itmes
    private static final String MOBLOG_ENABLED = "moblog-enabled";
    private static final String MOBLOG_HOSTNAME = "moblog-hostname";
    private static final String MOBLOG_USERID = "moblog-userid";
    private static final String MOBLOG_PASSWORD = "moblog-password";
    private static final String MOBLOG_CATEGORY = "moblog-category";
    private static final String MOBLOG_SECRET_WORD = "moblog-secret-word";
    private static final String MOBLOG_IMAGE_MIME_TYPES = "moblog-image-mime-types";
    private static final String MOBLOG_ATTACHMENT_MIME_TYPES = "moblog-attachment-mime-types";
    private static final String MOBLOG_TEXT_MIME_TYPES = "moblog-text-mime-types";
    private static final String MOBLOG_AUTHORIZED_ADDRESS = "moblog-authorized-address";
    private static final String MOBLOG_IGNORE_EXPRESSION = "moblog-ignore-expression";

    // Actions
    private static final String UPDATE_MOBLOG_SETTINGS_ACTIONS = "update-moblog-settings";
    private static final String ADD_AUTHORIZED_ADDRESS_ACTION = "add-authorized-address";
    private static final String DELETE_AUTHORIZED_ADDRESS_ACTION = "delete-authorized-address";

    private static final String MOBLOG_ADMIN_PERMISSION = "moblog_admin";

    private static final String BLOJSOM_PLUGIN_MOBLOG_MAILBOX = "BLOJSOM_PLUGIN_MOBLOG_MAILBOX";

    private Fetcher _fetcher;

    /**
     * Default constructor
     */
    public MoblogAdminPlugin() {
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
        return "Moblog plugin";
    }

    /**
     * Return the name of the initial editing page for the plugin
     *
     * @return Name of the initial editing page for the plugin
     */
    public String getInitialPage() {
        return EDIT_MOBLOG_SETTINGS_PAGE;
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
        entries = super.process(httpServletRequest, httpServletResponse, blog, context, entries);

        String page = BlojsomUtils.getRequestValue(BlojsomConstants.PAGE_PARAM, httpServletRequest);

        String username = getUsernameFromSession(httpServletRequest, blog);
        if (!checkPermission(blog, null, username, MOBLOG_ADMIN_PERMISSION)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            addOperationResultMessage(context, getAdminResource(FAILED_MOBLOG_PERMISSIONS_KEY, FAILED_MOBLOG_PERMISSIONS_KEY, blog.getBlogAdministrationLocale()));

            return entries;
        }

        if (ADMIN_LOGIN_PAGE.equals(page)) {
            return entries;
        } else {
            String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);

            if (UPDATE_MOBLOG_SETTINGS_ACTIONS.equals(action)) {
                boolean mailboxEnabled = Boolean.valueOf(BlojsomUtils.getRequestValue(MOBLOG_ENABLED, httpServletRequest)).booleanValue();
                blog.setProperty(MoblogPlugin.PROPERTY_ENABLED, Boolean.toString(mailboxEnabled));

                String hostname = BlojsomUtils.getRequestValue(MOBLOG_HOSTNAME, httpServletRequest);
                blog.setProperty(MoblogPlugin.PROPERTY_HOSTNAME, hostname);

                String userID = BlojsomUtils.getRequestValue(MOBLOG_USERID, httpServletRequest);
                blog.setProperty(MoblogPlugin.PROPERTY_USERID, userID);

                String password = BlojsomUtils.getRequestValue(MOBLOG_PASSWORD, httpServletRequest);
                blog.setProperty(MoblogPlugin.PROPERTY_PASSWORD, password);

                String category = BlojsomUtils.getRequestValue(MOBLOG_CATEGORY, httpServletRequest);
                blog.setProperty(MoblogPlugin.PROPERTY_CATEGORY, category);

                String textMimeTypeValue = BlojsomUtils.getRequestValue(MOBLOG_TEXT_MIME_TYPES, httpServletRequest);
                blog.setProperty(MoblogPlugin.PLUGIN_MOBLOG_TEXT_MIME_TYPES, textMimeTypeValue);

                String attachmentMimeTypeValue = BlojsomUtils.getRequestValue(MOBLOG_ATTACHMENT_MIME_TYPES, httpServletRequest);
                blog.setProperty(MoblogPlugin.PLUGIN_MOBLOG_ATTACHMENT_MIME_TYPES, attachmentMimeTypeValue);

                String imageMimeTypeValue = BlojsomUtils.getRequestValue(MOBLOG_IMAGE_MIME_TYPES, httpServletRequest);
                blog.setProperty(MoblogPlugin.PLUGIN_MOBLOG_IMAGE_MIME_TYPES, imageMimeTypeValue);

                String secretWord = BlojsomUtils.getRequestValue(MOBLOG_SECRET_WORD, httpServletRequest);
                blog.setProperty(MoblogPlugin.PLUGIN_MOBLOG_SECRET_WORD, secretWord);

                String ignoreExpression = BlojsomUtils.getRequestValue(MOBLOG_IGNORE_EXPRESSION, httpServletRequest);
                blog.setProperty(MoblogPlugin.PLUGIN_MOBLOG_IGNORE_EXPRESSION, ignoreExpression);

                try {
                    _fetcher.saveBlog(blog);
                    addOperationResultMessage(context, getAdminResource(UPDATED_MOBLOG_CONFIGURATION_KEY, UPDATED_MOBLOG_CONFIGURATION_KEY, blog.getBlogAdministrationLocale()));
                } catch (FetcherException e) {
                    _logger.error(e);
                    addOperationResultMessage(context, getAdminResource(FAILED_WRITE_MOBLOG_CONFIGURATION_KEY, FAILED_WRITE_MOBLOG_CONFIGURATION_KEY, blog.getBlogAdministrationLocale()));
                }
            } else if (ADD_AUTHORIZED_ADDRESS_ACTION.equals(action)) {
                String addressToAdd = BlojsomUtils.getRequestValue(MOBLOG_AUTHORIZED_ADDRESS, httpServletRequest);
                if (!BlojsomUtils.checkNullOrBlank(addressToAdd)) {
                    String authorizedAddresses = blog.getProperty(MoblogPlugin.PLUGIN_MOBLOG_AUTHORIZED_ADDRESSES);
                    String updatedAddresses;
                    Map addresses;

                    if (!BlojsomUtils.checkNullOrBlank(authorizedAddresses)) {
                        addresses = BlojsomUtils.arrayOfStringsToMap(BlojsomUtils.parseCommaList(authorizedAddresses));
                        addresses.put(addressToAdd, addressToAdd);
                        updatedAddresses = BlojsomUtils.getKeysAsStringList(addresses);
                    } else {
                        updatedAddresses = addressToAdd;
                    }

                    blog.setProperty(MoblogPlugin.PLUGIN_MOBLOG_AUTHORIZED_ADDRESSES, updatedAddresses);

                    try {
                        _fetcher.saveBlog(blog);
                        addOperationResultMessage(context, formatAdminResource(ADDED_AUTHORIZED_EMAIL_KEY, ADDED_AUTHORIZED_EMAIL_KEY, blog.getBlogAdministrationLocale(), new Object[] {addressToAdd}));
                    } catch (FetcherException e) {
                        _logger.error(e);
                        addOperationResultMessage(context, getAdminResource(FAILED_ADD_EMAIL_KEY, FAILED_ADD_EMAIL_KEY, blog.getBlogAdministrationLocale()));
                    }
                } else {
                    addOperationResultMessage(context, getAdminResource(NO_EMAIL_ADDRESS_KEY, NO_EMAIL_ADDRESS_KEY, blog.getBlogAdministrationLocale()));
                }
            } else if (DELETE_AUTHORIZED_ADDRESS_ACTION.equals(action)) {
                String addressToDelete = BlojsomUtils.getRequestValue(MOBLOG_AUTHORIZED_ADDRESS, httpServletRequest);
                if (!BlojsomUtils.checkNullOrBlank(addressToDelete)) {
                    String authorizedAddresses = blog.getProperty(MoblogPlugin.PLUGIN_MOBLOG_AUTHORIZED_ADDRESSES);
                    String updatedAddresses = "";
                    Map addresses;

                    if (!BlojsomUtils.checkNullOrBlank(authorizedAddresses)) {
                        addresses = BlojsomUtils.arrayOfStringsToMap(BlojsomUtils.parseCommaList(authorizedAddresses));
                        addresses.remove(addressToDelete);
                        updatedAddresses = BlojsomUtils.getKeysAsStringList(addresses);
                    }

                    blog.setProperty(MoblogPlugin. PLUGIN_MOBLOG_AUTHORIZED_ADDRESSES, updatedAddresses);

                    try {
                        _fetcher.saveBlog(blog);
                        addOperationResultMessage(context, formatAdminResource(REMOVED_AUTHORIZED_EMAIL_KEY, REMOVED_AUTHORIZED_EMAIL_KEY, blog.getBlogAdministrationLocale(), new Object[] {addressToDelete}));
                    } catch (FetcherException e) {
                        _logger.error(e);
                        addOperationResultMessage(context, getAdminResource(FAILED_DELETE_EMAIL_KEY, FAILED_DELETE_EMAIL_KEY, blog.getBlogAdministrationLocale()));
                    }
                } else {
                    addOperationResultMessage(context, getAdminResource(NO_EMAIL_ADDRESS_KEY, NO_EMAIL_ADDRESS_KEY, blog.getBlogAdministrationLocale()));
                }
            }

            context.put(BLOJSOM_PLUGIN_MOBLOG_MAILBOX, MoblogPluginUtils.readMailboxSettingsForBlog(_servletConfig, blog));
        }

        return entries;
    }
}