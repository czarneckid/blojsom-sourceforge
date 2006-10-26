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
package org.blojsom.plugin.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.*;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.weblogsping.WeblogsPingPlugin;
import org.blojsom.plugin.pingback.event.*;
import org.blojsom.plugin.pingback.PingbackPlugin;
import org.blojsom.plugin.trackback.event.*;
import org.blojsom.plugin.trackback.TrackbackPlugin;
import org.blojsom.plugin.common.ResponseConstants;
import org.blojsom.plugin.comment.event.*;
import org.blojsom.plugin.admin.event.EntryAddedEvent;
import org.blojsom.plugin.admin.event.ProcessEntryEvent;
import org.blojsom.plugin.admin.event.EntryDeletedEvent;
import org.blojsom.plugin.admin.event.EntryUpdatedEvent;
import org.blojsom.util.BlojsomMetaDataConstants;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.BlojsomConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * EditBlogEntriesPlugin
 *
 * @author David Czarnecki
 * @version $Id: EditBlogEntriesPlugin.java,v 1.16 2006-10-26 01:42:29 czarneckid Exp $
 * @since blojsom 3.0
 */
public class EditBlogEntriesPlugin extends BaseAdminPlugin {

    private Log _logger = LogFactory.getLog(EditBlogEntriesPlugin.class);

    // Pages
    private static final String EDIT_BLOG_ENTRIES_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blog-entries";
    private static final String EDIT_BLOG_ENTRIES_LIST_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blog-entries-list";
    private static final String EDIT_BLOG_ENTRY_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blog-entry";
    private static final String ADD_BLOG_ENTRY_PAGE = "/org/blojsom/plugin/admin/templates/admin-add-blog-entry";
    private static final String MANAGE_BLOG_ENTRIES_PAGE = "/org/blojsom/plugin/admin/templates/admin-manage-blog-entries";

    // Constants
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_LIST = "BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_LIST";
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_CATEGORY = "BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_CATEGORY";
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY = "BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY";
    private static final String BLOJSOM_PLUGIN_TOTAL_ENTRIES_PAGES = "BLOJSOM_PLUGIN_TOTAL_ENTRIES_PAGES";
    private static final String BLOJSOM_USER_OBJECT = "BLOJSOM_USER_OBJECT";

    // Localization constants
    private static final String FAILED_PERMISSION_EDIT_KEY = "failed.permission.edit.text";
    private static final String FAILED_RETRIEVE_BLOG_ENTRY_KEY = "failed.retrieve.entry.text";
    private static final String FAILED_DELETE_BLOG_ENTRY_KEY = "failed.delete.entry.text";
    private static final String FAILED_ADD_BLOG_ENTRY_KEY = "failed.add.entry.text";
    private static final String UPDATED_BLOG_ENTRY_KEY = "updated.blog.entry.text";
    private static final String DELETED_BLOG_ENTRY_KEY = "deleted.blog.entry.text";
    private static final String ADDED_BLOG_ENTRY_KEY = "added.blog.entry.text";
    private static final String DELETED_COMMENTS_KEY = "deleted.comments.text";
    private static final String APPROVED_COMMENTS_KEY = "approved.comments.text";
    private static final String DELETED_TRACKBACKS_KEY = "deleted.trackbacks.text";
    private static final String APPROVED_TRACKBACKS_KEY = "approved.trackbacks.text";
    private static final String APPROVED_PINGBACKS_KEY = "approved.pingbacks.text";
    private static final String DELETED_PINGBACKS_KEY = "deleted.pingbacks.text";
    private static final String BLANK_ENTRY_KEY = "blank.entry.text";
    private static final String INVALID_CATEGORYID_KEY = "invalid.categoryid.text";

    // Actions
    private static final String EDIT_BLOG_ENTRIES_ACTION = "edit-blog-entries";
    private static final String EDIT_BLOG_ENTRY_ACTION = "edit-blog-entry";
    private static final String UPDATE_BLOG_ENTRY_ACTION = "update-blog-entry";
    private static final String DELETE_BLOG_ENTRY_ACTION = "delete-blog-entry";
    private static final String NEW_BLOG_ENTRY_ACTION = "new-blog-entry";
    private static final String ADD_BLOG_ENTRY_ACTION = "add-blog-entry";
    private static final String DELETE_BLOG_COMMENTS = "delete-blog-comments";
    private static final String DELETE_BLOG_TRACKBACKS = "delete-blog-trackbacks";
    private static final String DELETE_BLOG_PINGBACKS = "delete-blog-pingbacks";
    private static final String APPROVE_BLOG_COMMENTS = "approve-blog-comments";
    private static final String APPROVE_BLOG_TRACKBACKS = "approve-blog-trackbacks";
    private static final String APPROVE_BLOG_PINGBACKS = "approve-blog-pingbacks";
    private static final String EDIT_ENTRIES_LIST = "edit-entries-list";
    private static final String DELETE_BLOG_ENTRY_LIST = "delete-blog-entry-list";

    // AJAX actions
    private static final String AJAX_DELETE_RESPONSE = "ajax-delete-response";
    private static final String AJAX_APPROVE_RESPONSE = "ajax-approve-response";
    private static final String AJAX_UNAPPROVE_RESPONSE = "ajax-unapprove-response";
    private static final String AJAX_MARK_SPAM_RESPONSE = "ajax-mark-spam-response";
    private static final String AJAX_UNMARK_SPAM_RESPONSE = "ajax-unmark-spam-response";

    // Form elements
    private static final String BLOG_CATEGORY_ID = "blog-category-id";
    private static final String BLOG_ENTRY_ID = "blog-entry-id";
    private static final String BLOG_ENTRY_TITLE = "blog-entry-title";
    private static final String BLOG_ENTRY_DESCRIPTION = "blog-entry-description";
    private static final String BLOG_COMMENT_ID = "blog-comment-id";
    private static final String BLOG_TRACKBACK_ID = "blog-trackback-id";
    private static final String BLOG_PINGBACK_ID = "blog-pingback-id";
    private static final String BLOG_ENTRY_PUBLISH_DATETIME = "blog-entry-publish-datetime";
    private static final String BLOG_TRACKBACK_URLS = "blog-trackback-urls";
    private static final String POST_SLUG = "post-slug";
    private static final String PING_BLOG_URLS = "ping-blog-urls";
    private static final String RESPONSE_TYPE = "response-type";
    private static final String RESPONSE_ID = "response-id";
    private static final String STATUS = "status";
    private static final String QUERY = "query";

    // Permissions
    private static final String EDIT_BLOG_ENTRIES_PERMISSION = "edit_blog_entries_permission";

    private Fetcher _fetcher;
    private EventBroadcaster _eventBroadcaster;

    /**
     * Default constructor.
     */
    public EditBlogEntriesPlugin() {
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
     * Delete a blog entry
     * @param httpServletRequest Request
     * @param blog {@link Blog}
     * @param context Context
     * @return <code>true</code> if the entry was deleted, <code>false</code> otherwise
     */
    protected boolean deleteBlogEntry(HttpServletRequest httpServletRequest, Blog blog, Map context) {
        String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
        Integer entryId;
        try {
            entryId = Integer.valueOf(blogEntryId);
        } catch (NumberFormatException e) {
            addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);

            return false;
        }

        try {
            Entry entryToDelete = _fetcher.newEntry();
            entryToDelete.setId(entryId);

            _fetcher.loadEntry(blog, entryToDelete);
            String title = entryToDelete.getTitle();
            _fetcher.deleteEntry(blog,entryToDelete);

            addOperationResultMessage(context, formatAdminResource(DELETED_BLOG_ENTRY_KEY, DELETED_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{title}));

            EntryDeletedEvent deleteEvent = new EntryDeletedEvent(this, new Date(), entryToDelete, blog);
            _eventBroadcaster.broadcastEvent(deleteEvent);
        } catch (FetcherException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            addOperationResultMessage(context, formatAdminResource(FAILED_DELETE_BLOG_ENTRY_KEY, FAILED_DELETE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
            return false;
        }

        return true;
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
        if (!authenticateUser(httpServletRequest, httpServletResponse, context, blog)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_LOGIN_PAGE);

            return entries;
        }

        String username = getUsernameFromSession(httpServletRequest, blog);
        if (!checkPermission(blog, null, username, EDIT_BLOG_ENTRIES_PERMISSION)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            addOperationResultMessage(context, getAdminResource(FAILED_PERMISSION_EDIT_KEY, FAILED_PERMISSION_EDIT_KEY, blog.getBlogAdministrationLocale()));

            return entries;
        }

        try {
            context.put(BLOJSOM_USER_OBJECT, _fetcher.loadUser(blog, username));
        } catch (FetcherException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }
        }

        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);
        String subAction = BlojsomUtils.getRequestValue(SUBACTION_PARAM, httpServletRequest);

        if (BlojsomUtils.checkNullOrBlank(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User did not request edit action");
            }
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
        } else if (PAGE_ACTION.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested edit blog entries page");
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRIES_PAGE);
        } else if (EDIT_BLOG_ENTRIES_ACTION.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested edit blog entries list page");
            }

            String blogCategoryId = BlojsomUtils.getRequestValue(BLOG_CATEGORY_ID, httpServletRequest);
            Integer categoryId;
            Category category = null;
            try {
                categoryId = Integer.valueOf(blogCategoryId);
            } catch (NumberFormatException e) {
                addOperationResultMessage(context, formatAdminResource(INVALID_CATEGORYID_KEY, INVALID_CATEGORYID_KEY, blog.getBlogAdministrationLocale(), new Object[] {blogCategoryId}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);

                return entries;
            }

            try {
                entries = _fetcher.loadAllEntriesForCategory(blog, categoryId);
                category = _fetcher.loadCategory(blog, categoryId);
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                entries = new Entry[0];
            }

            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_CATEGORY, category);
            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_LIST, entries);

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRIES_LIST_PAGE);
        } else if (EDIT_BLOG_ENTRY_ACTION.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested edit blog entry action");
            }

            String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
            Integer entryId;
            try {
                entryId = Integer.valueOf(blogEntryId);
            } catch (NumberFormatException e) {
                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);

                return entries;
            }

            try {
                Entry entry = _fetcher.newEntry();
                entry.setId(entryId);
                _fetcher.loadEntry(blog, entry);
                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY, entry);

                _eventBroadcaster.processEvent(new ProcessEntryEvent(this, new Date(), entry, blog, httpServletRequest, httpServletResponse, context));
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);

                return entries;
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);
        } else if (UPDATE_BLOG_ENTRY_ACTION.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested update blog entry action");
            }

            String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
            Integer entryId;
            try {
                entryId = Integer.valueOf(blogEntryId);
            } catch (NumberFormatException e) {
                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);

                return entries;
            }

            String blogCategoryId = BlojsomUtils.getRequestValue(BLOG_CATEGORY_ID, httpServletRequest);
            Integer categoryId;
            try {
                categoryId = Integer.valueOf(blogCategoryId);
            } catch (NumberFormatException e) {
                addOperationResultMessage(context, formatAdminResource(INVALID_CATEGORYID_KEY, INVALID_CATEGORYID_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogCategoryId}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);

                return entries;
            }

            String blogEntryDescription = BlojsomUtils.getRequestValue(BLOG_ENTRY_DESCRIPTION, httpServletRequest);
            String blogEntryTitle = BlojsomUtils.getRequestValue(BLOG_ENTRY_TITLE, httpServletRequest);
            if (BlojsomUtils.checkNullOrBlank(blogEntryTitle)) {
                blogEntryDescription = BlojsomUtils.LINE_SEPARATOR + blogEntryDescription;
            }

            String allowComments = BlojsomUtils.getRequestValue(BlojsomMetaDataConstants.BLOG_METADATA_COMMENTS_DISABLED, httpServletRequest);
            String allowTrackbacks = BlojsomUtils.getRequestValue(BlojsomMetaDataConstants.BLOG_METADATA_TRACKBACKS_DISABLED, httpServletRequest);
            String allowPingbacks = BlojsomUtils.getRequestValue(BlojsomMetaDataConstants.BLOG_METADATA_PINGBACKS_DISABLED, httpServletRequest);
            String blogTrackbackURLs = BlojsomUtils.getRequestValue(BLOG_TRACKBACK_URLS, httpServletRequest);
            String pingBlogURLS = BlojsomUtils.getRequestValue(PING_BLOG_URLS, httpServletRequest);
            String sendPingbacks = BlojsomUtils.getRequestValue(PingbackPlugin.PINGBACK_PLUGIN_METADATA_SEND_PINGBACKS, httpServletRequest);
            String status = BlojsomUtils.getRequestValue(STATUS, httpServletRequest);

            try {
                Entry entryToUpdate = _fetcher.newEntry();
                entryToUpdate.setId(entryId);
                _fetcher.loadEntry(blog, entryToUpdate);

                entryToUpdate.setTitle(blogEntryTitle);
                entryToUpdate.setDescription(blogEntryDescription);
                entryToUpdate.setBlogCategoryId(categoryId);

                Map entryMetaData = entryToUpdate.getMetaData();
                if (entryMetaData == null) {
                    entryMetaData = new HashMap();
                }

                if (!BlojsomUtils.checkNullOrBlank(allowComments)) {
                    entryToUpdate.setAllowComments(new Integer(0));
                } else {
                    entryToUpdate.setAllowComments(new Integer(1));
                }

                if (!BlojsomUtils.checkNullOrBlank(allowTrackbacks)) {
                    entryToUpdate.setAllowTrackbacks(new Integer(0));
                } else {
                    entryToUpdate.setAllowTrackbacks(new Integer(1));
                }

                if (!BlojsomUtils.checkNullOrBlank(allowPingbacks)) {
                    entryToUpdate.setAllowPingbacks(new Integer(0));
                } else {
                    entryToUpdate.setAllowPingbacks(new Integer(1));
                }

                if (BlojsomUtils.checkNullOrBlank(pingBlogURLS)) {
                    entryMetaData.put(WeblogsPingPlugin.NO_PING_WEBLOGS_METADATA, "true");
                } else {
                    entryMetaData.remove(WeblogsPingPlugin.NO_PING_WEBLOGS_METADATA);
                }

                if (!BlojsomUtils.checkNullOrBlank(sendPingbacks)) {
                    entryMetaData.put(PingbackPlugin.PINGBACK_PLUGIN_METADATA_SEND_PINGBACKS, "true");
                } else {
                    entryMetaData.remove(PingbackPlugin.PINGBACK_PLUGIN_METADATA_SEND_PINGBACKS);
                }

                String entryPublishDateTime = httpServletRequest.getParameter(BLOG_ENTRY_PUBLISH_DATETIME);
                if (!BlojsomUtils.checkNullOrBlank(entryPublishDateTime)) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                    try {
                        Date publishDateTime = simpleDateFormat.parse(entryPublishDateTime);
                        entryToUpdate.setDate(publishDateTime);
                        entryToUpdate.setModifiedDate(publishDateTime);
                    } catch (ParseException e) {
                    }
                } else {
                    entryToUpdate.setModifiedDate(new Date());
                }

                if (BlojsomUtils.checkNullOrBlank(status)) {
                    status = BlojsomMetaDataConstants.DRAFT_STATUS;
                }

                entryToUpdate.setStatus(status);
                entryToUpdate.setMetaData(entryMetaData);

                _eventBroadcaster.processEvent(new ProcessEntryEvent(this, new Date(), entryToUpdate, blog, httpServletRequest, httpServletResponse, context));

                _fetcher.saveEntry(blog, entryToUpdate);
                _fetcher.loadEntry(blog, entryToUpdate);

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Updated blog entry: " + entryToUpdate.getId());
                }

                StringBuffer entryLink = new StringBuffer();
                entryLink.append("<a href=\"").append(blog.getBlogURL()).append(entryToUpdate.getBlogCategory().getName()).append(entryToUpdate.getPostSlug()).append("\">").append(entryToUpdate.getEscapedTitle()).append("</a>");
                addOperationResultMessage(context, formatAdminResource(UPDATED_BLOG_ENTRY_KEY, UPDATED_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[] {entryLink.toString()}));

                EntryUpdatedEvent updateEvent = new EntryUpdatedEvent(this, new Date(), entryToUpdate, blog);
                _eventBroadcaster.broadcastEvent(updateEvent);

                // Send trackback pings
                if (!BlojsomUtils.checkNullOrBlank(blogTrackbackURLs)) {
                    sendTrackbackPings(blog, entryToUpdate, blogTrackbackURLs);
                }

                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);
                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY, entryToUpdate);
           } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[] {blogEntryId}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRIES_PAGE);
                entries = new Entry[0];
            }
        } else if (DELETE_BLOG_ENTRY_ACTION.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested delete blog entry action");
            }

            String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
            Integer entryId;
            try {
                entryId = Integer.valueOf(blogEntryId);
            } catch (NumberFormatException e) {
                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);

                return entries;
            }

            try {
                Entry entryToDelete = _fetcher.newEntry();
                entryToDelete.setId(entryId);

                _fetcher.loadEntry(blog, entryToDelete);
                String title = entryToDelete.getTitle();
                _fetcher.deleteEntry(blog,entryToDelete);

                addOperationResultMessage(context, formatAdminResource(DELETED_BLOG_ENTRY_KEY, DELETED_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{title}));

                EntryDeletedEvent deleteEvent = new EntryDeletedEvent(this, new Date(), entryToDelete, blog);
                _eventBroadcaster.broadcastEvent(deleteEvent);
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                addOperationResultMessage(context, formatAdminResource(FAILED_DELETE_BLOG_ENTRY_KEY, FAILED_DELETE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                entries = new Entry[0];
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRIES_PAGE);
        } else if (NEW_BLOG_ENTRY_ACTION.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested new blog entry action");
            }

            _eventBroadcaster.processEvent(new ProcessEntryEvent(this, new Date(), null, blog, httpServletRequest, httpServletResponse, context));

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADD_BLOG_ENTRY_PAGE);
        } else if (ADD_BLOG_ENTRY_ACTION.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested add blog entry action");
            }

            String blogCategoryId = BlojsomUtils.getRequestValue(BLOG_CATEGORY_ID, httpServletRequest);

            // Create a category for the blog if one doesn't exist
            if (BlojsomUtils.checkNullOrBlank(blogCategoryId)) {
                Category category = _fetcher.newCategory();
                category.setBlogId(blog.getId());
                category.setDescription("Uncategorized");
                category.setName("/uncategorized/");
                category.setParentCategoryId(null);

                try {
                    _fetcher.saveCategory(blog, category);
                    blogCategoryId = category.getId().toString();
                } catch (FetcherException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }

                    addOperationResultMessage(context, formatAdminResource(FAILED_ADD_BLOG_ENTRY_KEY, FAILED_ADD_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogCategoryId}));
                    httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);

                    return entries;
                }
            }

            Integer categoryId;
            try {
                categoryId = Integer.valueOf(blogCategoryId);
            } catch (NumberFormatException e) {
                addOperationResultMessage(context, formatAdminResource(INVALID_CATEGORYID_KEY, INVALID_CATEGORYID_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogCategoryId}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);

                return entries;
            }

            String blogEntryDescription = BlojsomUtils.getRequestValue(BLOG_ENTRY_DESCRIPTION, httpServletRequest);
            String blogEntryTitle = BlojsomUtils.getRequestValue(BLOG_ENTRY_TITLE, httpServletRequest);

            if (BlojsomUtils.checkNullOrBlank(blogEntryTitle) && BlojsomUtils.checkNullOrBlank(blogEntryDescription)) {
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADD_BLOG_ENTRY_PAGE);
                _eventBroadcaster.processEvent(new ProcessEntryEvent(this, new Date(), null, blog, httpServletRequest, httpServletResponse, context));

                addOperationResultMessage(context, getAdminResource(BLANK_ENTRY_KEY, BLANK_ENTRY_KEY, blog.getBlogAdministrationLocale()));

                return entries;
            }

            if (BlojsomUtils.checkNullOrBlank(blogEntryTitle)) {
                blogEntryDescription = BlojsomUtils.LINE_SEPARATOR + blogEntryDescription;
            }

            String allowComments = BlojsomUtils.getRequestValue(BlojsomMetaDataConstants.BLOG_METADATA_COMMENTS_DISABLED, httpServletRequest);
            String allowTrackbacks = BlojsomUtils.getRequestValue(BlojsomMetaDataConstants.BLOG_METADATA_TRACKBACKS_DISABLED, httpServletRequest);
            String allowPingbacks = BlojsomUtils.getRequestValue(BlojsomMetaDataConstants.BLOG_METADATA_PINGBACKS_DISABLED, httpServletRequest);
            String blogTrackbackURLs = BlojsomUtils.getRequestValue(BLOG_TRACKBACK_URLS, httpServletRequest);
            String pingBlogURLS = BlojsomUtils.getRequestValue(PING_BLOG_URLS, httpServletRequest);
            String postSlug = BlojsomUtils.getRequestValue(POST_SLUG, httpServletRequest);
            String sendPingbacks = BlojsomUtils.getRequestValue(PingbackPlugin.PINGBACK_PLUGIN_METADATA_SEND_PINGBACKS, httpServletRequest);
            String status = BlojsomUtils.getRequestValue(STATUS, httpServletRequest);

            Entry entry;
            entry = _fetcher.newEntry();

            entry.setBlogId(blog.getId());
            entry.setTitle(blogEntryTitle);
            entry.setBlogCategoryId(categoryId);
            entry.setDescription(blogEntryDescription);
            entry.setDate(new Date());
            entry.setModifiedDate(entry.getDate());
            entry.setBlogCategoryId(categoryId);

            if (!BlojsomUtils.checkNullOrBlank(postSlug)) {
                entry.setPostSlug(postSlug);
            }

            Map entryMetaData = new HashMap();
            username = (String) httpServletRequest.getSession().getAttribute(blog.getBlogAdminURL() + "_" + BLOJSOM_ADMIN_PLUGIN_USERNAME_KEY);
            entry.setAuthor(username);

            String entryPublishDateTime = httpServletRequest.getParameter(BLOG_ENTRY_PUBLISH_DATETIME);
            if (!BlojsomUtils.checkNullOrBlank(entryPublishDateTime)) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                try {
                    Date publishDateTime = simpleDateFormat.parse(entryPublishDateTime);
                    entry.setDate(publishDateTime);
                    entry.setModifiedDate(publishDateTime);
                } catch (ParseException e) {
                }
            }

            if (!BlojsomUtils.checkNullOrBlank(allowComments)) {
                entry.setAllowComments(new Integer(0));
            } else {
                entry.setAllowComments(new Integer(1));
            }

            if (!BlojsomUtils.checkNullOrBlank(allowTrackbacks)) {
                entry.setAllowTrackbacks(new Integer(0));
            } else {
                entry.setAllowTrackbacks(new Integer(1));
            }

            if (!BlojsomUtils.checkNullOrBlank(allowPingbacks)) {
                entry.setAllowPingbacks(new Integer(0));
            } else {
                entry.setAllowPingbacks(new Integer(1));
            }

            if (BlojsomUtils.checkNullOrBlank(pingBlogURLS)) {
                entryMetaData.put(WeblogsPingPlugin.NO_PING_WEBLOGS_METADATA, "true");
            } else {
                entryMetaData.remove(WeblogsPingPlugin.NO_PING_WEBLOGS_METADATA);
            }

            if (!BlojsomUtils.checkNullOrBlank(sendPingbacks)) {
                entryMetaData.put(PingbackPlugin.PINGBACK_PLUGIN_METADATA_SEND_PINGBACKS, "true");
            } else {
                entryMetaData.remove(PingbackPlugin.PINGBACK_PLUGIN_METADATA_SEND_PINGBACKS);
            }                       

            if (BlojsomUtils.checkNullOrBlank(status)) {
                status = BlojsomMetaDataConstants.DRAFT_STATUS;
            }

            entry.setStatus(status);
            entry.setMetaData(entryMetaData);

            try {
                _eventBroadcaster.processEvent(new ProcessEntryEvent(this, new Date(), entry, blog, httpServletRequest, httpServletResponse, context));

                _fetcher.saveEntry(blog, entry);
                _fetcher.loadEntry(blog, entry);

                StringBuffer entryLink = new StringBuffer();
                entryLink.append("<a href=\"").append(blog.getBlogURL()).append(entry.getBlogCategory().getName()).append(entry.getPostSlug()).append("\">").append(entry.getEscapedTitle()).append("</a>");
                addOperationResultMessage(context, formatAdminResource(ADDED_BLOG_ENTRY_KEY, ADDED_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{entryLink.toString()}));

                EntryAddedEvent addedEvent = new EntryAddedEvent(this, new Date(), entry, blog);
                _eventBroadcaster.broadcastEvent(addedEvent);
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                addOperationResultMessage(context, formatAdminResource(FAILED_ADD_BLOG_ENTRY_KEY, FAILED_ADD_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogCategoryId}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);

                return entries;
            }

            // Send trackback pings
            if (!BlojsomUtils.checkNullOrBlank(blogTrackbackURLs)) {
                sendTrackbackPings(blog, entry, blogTrackbackURLs);
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_ACTION);
            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY, entry);
        } else if (EDIT_ENTRIES_LIST.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested edit entries list action");
            }

            String page = BlojsomUtils.getRequestValue(BlojsomConstants.PAGE_NUMBER_PARAM, httpServletRequest);
            int pgNum;
            try {
                pgNum = Integer.parseInt(page);
            } catch (NumberFormatException e) {
                pgNum = 1;
            }

            if (pgNum < 1) {
                pgNum = 1;
            }

            try {
                if (DELETE_BLOG_ENTRY_LIST.equals(subAction)) {
                    deleteBlogEntry(httpServletRequest, blog, context);
                }

                String query = BlojsomUtils.getRequestValue(QUERY, httpServletRequest);
                if (!BlojsomUtils.checkNullOrBlank(query)) {
                    context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_LIST, _fetcher.findEntries(blog, query));
                    context.put(QUERY, BlojsomUtils.escapeString(query));
                } else {
                    context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_LIST, _fetcher.loadEntries(blog, 10, pgNum));
                }

                Integer totalEntries = _fetcher.countEntries(blog);
                int totalPages = (totalEntries.intValue() / 10);
                int totalRemaining = (totalEntries.intValue() % 10);
                if (totalRemaining > 0) {
                    totalPages += 1;
                }

                context.put(BLOJSOM_PLUGIN_TOTAL_ENTRIES_PAGES, new Integer(totalPages));
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            }

            context.put(BlojsomConstants.PAGE_NUMBER_PARAM, new Integer(pgNum));
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, MANAGE_BLOG_ENTRIES_PAGE);
        } else if (AJAX_DELETE_RESPONSE.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested AJAX delete response action");
            }

            String responseId = BlojsomUtils.getRequestValue(RESPONSE_ID, httpServletRequest);
            Integer responseID;
            try {
                responseID = Integer.valueOf(responseId);
            } catch (NumberFormatException e) {
                context.put(BlojsomConstants.BLOJSOM_AJAX_STATUS, BlojsomConstants.FAILURE);
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_AJAX_RESPONSE);

                return entries;
            }

            String responseType = BlojsomUtils.getRequestValue(RESPONSE_TYPE, httpServletRequest);
            try {
                if (Response.COMMENT_TYPE.equals(responseType)) {
                    Comment comment = _fetcher.newComment();
                    comment.setBlogId(blog.getId());
                    comment.setId(responseID);
                    _fetcher.loadComment(blog, comment);
                    _fetcher.deleteComment(blog, comment);

                    _eventBroadcaster.broadcastEvent(new CommentDeletedEvent(this, new Date(), comment, blog));

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("AJAX delete comment response complete: " + responseId);
                    }
                } else if (Response.TRACKBACK_TYPE.equals(responseType)) {
                    Trackback trackback = _fetcher.newTrackback();
                    trackback.setBlogId(blog.getId());
                    trackback.setId(responseID);
                    _fetcher.loadTrackback(blog, trackback);
                    _fetcher.deleteTrackback(blog, trackback);

                    _eventBroadcaster.broadcastEvent(new TrackbackDeletedEvent(this, new Date(), trackback, blog));

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("AJAX delete trackback response complete: " + responseId);
                    }
                } else if (Response.PINGBACK_TYPE.equals(responseType)) {
                    Pingback pingback = _fetcher.newPingback();
                    pingback.setBlogId(blog.getId());
                    pingback.setId(responseID);
                    _fetcher.loadPingback(blog, pingback);
                    _fetcher.deletePingback(blog, pingback);

                    _eventBroadcaster.broadcastEvent(new PingbackDeletedEvent(this, new Date(), pingback, blog));

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("AJAX delete pingback response complete: " + responseId);
                    }
                } else {
                    if (_logger.isErrorEnabled()) {
                        _logger.error("Unknown response type for AJAX delete response: " + responseType);
                    }
                }

                context.put(BlojsomConstants.BLOJSOM_AJAX_STATUS, BlojsomConstants.SUCCESS);

                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_AJAX_RESPONSE);
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                context.put(BlojsomConstants.BLOJSOM_AJAX_STATUS, BlojsomConstants.FAILURE);
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_AJAX_RESPONSE);

                entries = new Entry[0];
            }
        } else if (AJAX_APPROVE_RESPONSE.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested AJAX approve response action");
            }

            String responseId = BlojsomUtils.getRequestValue(RESPONSE_ID, httpServletRequest);
            Integer responseID;
            try {
                responseID = Integer.valueOf(responseId);
            } catch (NumberFormatException e) {
                context.put(BlojsomConstants.BLOJSOM_AJAX_STATUS, BlojsomConstants.FAILURE);
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_AJAX_RESPONSE);

                return entries;
            }

            String responseType = BlojsomUtils.getRequestValue(RESPONSE_TYPE, httpServletRequest);
            try {
                if (Response.COMMENT_TYPE.equals(responseType)) {
                    Comment comment = _fetcher.newComment();
                    comment.setBlogId(blog.getId());
                    comment.setId(responseID);
                    _fetcher.loadComment(blog, comment);
                    comment.setStatus(ResponseConstants.APPROVED_STATUS);
                    _fetcher.saveComment(blog, comment);

                    _eventBroadcaster.broadcastEvent(new CommentApprovedEvent(this, new Date(), comment, blog));

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("AJAX approve comment response complete: " + responseId);
                    }
                } else if (Response.TRACKBACK_TYPE.equals(responseType)) {
                    Trackback trackback = _fetcher.newTrackback();
                    trackback.setBlogId(blog.getId());
                    trackback.setId(responseID);
                    _fetcher.loadTrackback(blog, trackback);
                    trackback.setStatus(ResponseConstants.APPROVED_STATUS);
                    _fetcher.saveTrackback(blog, trackback);

                    _eventBroadcaster.broadcastEvent(new TrackbackApprovedEvent(this, new Date(), trackback, blog));

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("AJAX approve trackback response complete: " + responseId);
                    }
                } else if (Response.PINGBACK_TYPE.equals(responseType)) {
                    Pingback pingback = _fetcher.newPingback();
                    pingback.setBlogId(blog.getId());
                    pingback.setId(responseID);
                    _fetcher.loadPingback(blog, pingback);
                    pingback.setStatus(ResponseConstants.APPROVED_STATUS);
                    _fetcher.savePingback(blog, pingback);

                    _eventBroadcaster.broadcastEvent(new PingbackApprovedEvent(this, new Date(), pingback, blog));

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("AJAX approve pingback response complete: " + responseId);
                    }
                } else {
                    if (_logger.isErrorEnabled()) {
                        _logger.error("Unknown response type for AJAX approve response: " + responseType);
                    }
                }

                context.put(BlojsomConstants.BLOJSOM_AJAX_STATUS, BlojsomConstants.SUCCESS);

                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_AJAX_RESPONSE);
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                context.put(BlojsomConstants.BLOJSOM_AJAX_STATUS, BlojsomConstants.FAILURE);
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_AJAX_RESPONSE);

                entries = new Entry[0];
            }
        } else if (AJAX_UNAPPROVE_RESPONSE.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested AJAX unapprove response action");
            }

            String responseId = BlojsomUtils.getRequestValue(RESPONSE_ID, httpServletRequest);
            Integer responseID;
            try {
                responseID = Integer.valueOf(responseId);
            } catch (NumberFormatException e) {
                context.put(BlojsomConstants.BLOJSOM_AJAX_STATUS, BlojsomConstants.FAILURE);
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_AJAX_RESPONSE);

                return entries;
            }

            String responseType = BlojsomUtils.getRequestValue(RESPONSE_TYPE, httpServletRequest);
            try {
                if (Response.COMMENT_TYPE.equals(responseType)) {
                    Comment comment = _fetcher.newComment();
                    comment.setBlogId(blog.getId());
                    comment.setId(responseID);
                    _fetcher.loadComment(blog, comment);
                    comment.setStatus(ResponseConstants.NEW_STATUS);
                    _fetcher.saveComment(blog, comment);

                    _eventBroadcaster.broadcastEvent(new CommentUnapprovedEvent(this, new Date(), comment, blog));

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("AJAX unapprove comment response complete: " + responseId);
                    }
                } else if (Response.TRACKBACK_TYPE.equals(responseType)) {
                    Trackback trackback = _fetcher.newTrackback();
                    trackback.setBlogId(blog.getId());
                    trackback.setId(responseID);
                    _fetcher.loadTrackback(blog, trackback);
                    trackback.setStatus(ResponseConstants.NEW_STATUS);
                    _fetcher.saveTrackback(blog, trackback);

                    _eventBroadcaster.broadcastEvent(new TrackbackUnapprovedEvent(this, new Date(), trackback, blog));

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("AJAX unapprove trackback response complete: " + responseId);
                    }
                } else if (Response.PINGBACK_TYPE.equals(responseType)) {
                    Pingback pingback = _fetcher.newPingback();
                    pingback.setBlogId(blog.getId());
                    pingback.setId(responseID);
                    _fetcher.loadPingback(blog, pingback);
                    pingback.setStatus(ResponseConstants.NEW_STATUS);
                    _fetcher.savePingback(blog, pingback);

                    _eventBroadcaster.broadcastEvent(new PingbackUnapprovedEvent(this, new Date(), pingback, blog));

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("AJAX unapprove pingback response complete: " + responseId);
                    }
                } else {
                    if (_logger.isErrorEnabled()) {
                        _logger.error("Unknown response type for AJAX unapprove response: " + responseType);
                    }
                }

                context.put(BlojsomConstants.BLOJSOM_AJAX_STATUS, BlojsomConstants.SUCCESS);

                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_AJAX_RESPONSE);
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                context.put(BlojsomConstants.BLOJSOM_AJAX_STATUS, BlojsomConstants.FAILURE);
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_AJAX_RESPONSE);

                entries = new Entry[0];
            }
        } else if (AJAX_MARK_SPAM_RESPONSE.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested AJAX mark spam response action");
            }

            String responseId = BlojsomUtils.getRequestValue(RESPONSE_ID, httpServletRequest);
            Integer responseID;
            try {
                responseID = Integer.valueOf(responseId);
            } catch (NumberFormatException e) {
                context.put(BlojsomConstants.BLOJSOM_AJAX_STATUS, BlojsomConstants.FAILURE);
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_AJAX_RESPONSE);

                return entries;
            }

            String responseType = BlojsomUtils.getRequestValue(RESPONSE_TYPE, httpServletRequest);
            try {
                if (Response.COMMENT_TYPE.equals(responseType)) {
                    Comment comment = _fetcher.newComment();
                    comment.setBlogId(blog.getId());
                    comment.setId(responseID);
                    _fetcher.loadComment(blog, comment);
                    comment.setStatus(ResponseConstants.SPAM_STATUS);
                    _fetcher.saveComment(blog, comment);

                    _eventBroadcaster.broadcastEvent(new CommentMarkedSpamEvent(this, new Date(), comment, blog));

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("AJAX mark comment spam response complete: " + responseId);
                    }
                } else if (Response.TRACKBACK_TYPE.equals(responseType)) {
                    Trackback trackback = _fetcher.newTrackback();
                    trackback.setBlogId(blog.getId());
                    trackback.setId(responseID);
                    _fetcher.loadTrackback(blog, trackback);
                    trackback.setStatus(ResponseConstants.SPAM_STATUS);
                    _fetcher.saveTrackback(blog, trackback);

                    _eventBroadcaster.broadcastEvent(new TrackbackMarkedSpamEvent(this, new Date(), trackback, blog));

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("AJAX mark trackback spam response complete: " + responseId);
                    }
                } else if (Response.PINGBACK_TYPE.equals(responseType)) {
                    Pingback pingback = _fetcher.newPingback();
                    pingback.setBlogId(blog.getId());
                    pingback.setId(responseID);
                    _fetcher.loadPingback(blog, pingback);
                    pingback.setStatus(ResponseConstants.SPAM_STATUS);
                    _fetcher.savePingback(blog, pingback);

                    _eventBroadcaster.broadcastEvent(new PingbackMarkedSpamEvent(this, new Date(), pingback, blog));

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("AJAX mark pingback spam response complete: " + responseId);
                    }
                } else {
                    if (_logger.isErrorEnabled()) {
                        _logger.error("Unknown response type for AJAX mark spam response: " + responseType);
                    }
                }

                context.put(BlojsomConstants.BLOJSOM_AJAX_STATUS, BlojsomConstants.SUCCESS);

                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_AJAX_RESPONSE);
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                context.put(BlojsomConstants.BLOJSOM_AJAX_STATUS, BlojsomConstants.FAILURE);
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_AJAX_RESPONSE);

                entries = new Entry[0];
            }
        } else if (AJAX_UNMARK_SPAM_RESPONSE.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested AJAX unmark spam response action");
            }

            String responseId = BlojsomUtils.getRequestValue(RESPONSE_ID, httpServletRequest);
            Integer responseID;
            try {
                responseID = Integer.valueOf(responseId);
            } catch (NumberFormatException e) {
                context.put(BlojsomConstants.BLOJSOM_AJAX_STATUS, BlojsomConstants.FAILURE);
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_AJAX_RESPONSE);

                return entries;
            }

            String responseType = BlojsomUtils.getRequestValue(RESPONSE_TYPE, httpServletRequest);
            try {
                if (Response.COMMENT_TYPE.equals(responseType)) {
                    Comment comment = _fetcher.newComment();
                    comment.setBlogId(blog.getId());
                    comment.setId(responseID);
                    _fetcher.loadComment(blog, comment);
                    comment.setStatus(ResponseConstants.NEW_STATUS);
                    _fetcher.saveComment(blog, comment);

                    _eventBroadcaster.broadcastEvent(new CommentUnmarkedSpamEvent(this, new Date(), comment, blog));

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("AJAX unmark comment spam response complete: " + responseId);
                    }
                } else if (Response.TRACKBACK_TYPE.equals(responseType)) {
                    Trackback trackback = _fetcher.newTrackback();
                    trackback.setBlogId(blog.getId());
                    trackback.setId(responseID);
                    _fetcher.loadTrackback(blog, trackback);
                    trackback.setStatus(ResponseConstants.NEW_STATUS);
                    _fetcher.saveTrackback(blog, trackback);

                    _eventBroadcaster.broadcastEvent(new TrackbackUnmarkedSpamEvent(this, new Date(), trackback, blog));

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("AJAX unmark trackback spam response complete: " + responseId);
                    }
                } else if (Response.PINGBACK_TYPE.equals(responseType)) {
                    Pingback pingback = _fetcher.newPingback();
                    pingback.setBlogId(blog.getId());
                    pingback.setId(responseID);
                    _fetcher.loadPingback(blog, pingback);
                    pingback.setStatus(ResponseConstants.NEW_STATUS);
                    _fetcher.savePingback(blog, pingback);

                    _eventBroadcaster.broadcastEvent(new PingbackUnmarkedSpamEvent(this, new Date(), pingback, blog));

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("AJAX unmark pingback spam response complete: " + responseId);
                    }
                } else {
                    if (_logger.isErrorEnabled()) {
                        _logger.error("Unknown response type for AJAX mark spam response: " + responseType);
                    }
                }

                context.put(BlojsomConstants.BLOJSOM_AJAX_STATUS, BlojsomConstants.SUCCESS);

                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_AJAX_RESPONSE);
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                context.put(BlojsomConstants.BLOJSOM_AJAX_STATUS, BlojsomConstants.FAILURE);
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_AJAX_RESPONSE);

                entries = new Entry[0];
            }
        } else if (DELETE_BLOG_COMMENTS.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested delete comments action");
            }

            String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
            Integer entryId;
            try {
                entryId = Integer.valueOf(blogEntryId);
            } catch (NumberFormatException e) {
                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);

                return entries;
            }

            Entry entry;
            try {
                entry = _fetcher.loadEntry(blog, entryId);
                String[] blogCommentIDs = httpServletRequest.getParameterValues(BLOG_COMMENT_ID);

                if (blogCommentIDs != null && blogCommentIDs.length > 0) {
                    for (int i = 0; i < blogCommentIDs.length; i++) {
                        String blogCommentID = blogCommentIDs[i];
                        Comment[] blogComments = entry.getCommentsAsArray();
                        for (int j = 0; j < blogComments.length; j++) {
                            Comment blogComment = blogComments[j];
                            if (blogComment.getId().equals(Integer.valueOf(blogCommentID))) {
                                try {
                                    _fetcher.deleteComment(blog, blogComment);

                                    _eventBroadcaster.broadcastEvent(new CommentDeletedEvent(this, new Date(), blogComment, blog));
                                } catch (FetcherException e) {
                                    if (_logger.isErrorEnabled()) {
                                        _logger.error(e);
                                    }
                                }
                            }
                        }
                    }

                    addOperationResultMessage(context, formatAdminResource(DELETED_COMMENTS_KEY, DELETED_COMMENTS_KEY, blog.getBlogAdministrationLocale(), new Object[] {new Integer(blogCommentIDs.length)}));
                }

                _fetcher.loadEntry(blog, entry);

                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY, entry);
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                entries = new Entry[0];
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);
        } else if (APPROVE_BLOG_COMMENTS.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested approve comments action");
            }

            String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
            Integer entryId;
            try {
                entryId = Integer.valueOf(blogEntryId);
            } catch (NumberFormatException e) {
                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);

                return entries;
            }

            Entry entry;
            try {
                entry = _fetcher.loadEntry(blog, entryId);
                String[] blogCommentIDs = httpServletRequest.getParameterValues(BLOG_COMMENT_ID);

                if (blogCommentIDs != null && blogCommentIDs.length > 0) {
                    for (int i = 0; i < blogCommentIDs.length; i++) {
                        String blogCommentID = blogCommentIDs[i];
                        Comment[] blogComments = entry.getCommentsAsArray();
                        for (int j = 0; j < blogComments.length; j++) {
                            Comment blogComment = blogComments[j];
                            if (blogComment.getId().equals(Integer.valueOf(blogCommentID))) {
                                try {
                                    blogComment.setStatus(ResponseConstants.APPROVED_STATUS);
                                    _fetcher.saveComment(blog, blogComment);

                                    _eventBroadcaster.broadcastEvent(new CommentApprovedEvent(this, new Date(), blogComment, blog));
                                } catch (FetcherException e) {
                                    _logger.error(e);
                                }
                            }
                        }
                    }

                    addOperationResultMessage(context, formatAdminResource(APPROVED_COMMENTS_KEY, APPROVED_COMMENTS_KEY, blog.getBlogAdministrationLocale(), new Object[] {new Integer(blogCommentIDs.length)}));
                }

                _fetcher.loadEntry(blog, entry);

                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY, entry);
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                entries = new Entry[0];
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);
        } else if (DELETE_BLOG_TRACKBACKS.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested delete blog trackbacks action");
            }

            String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
            Integer entryId;
            try {
                entryId = Integer.valueOf(blogEntryId);
            } catch (NumberFormatException e) {
                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);

                return entries;
            }

            Entry entry;
            try {
                entry = _fetcher.loadEntry(blog, entryId);
                String[] blogTrackbackIDs = httpServletRequest.getParameterValues(BLOG_TRACKBACK_ID);

                if (blogTrackbackIDs != null && blogTrackbackIDs.length > 0) {
                    for (int i = 0; i < blogTrackbackIDs.length; i++) {
                        String blogTrackbackID = blogTrackbackIDs[i];
                        Trackback[] trackbacks = entry.getTrackbacksAsArray();
                        for (int j = 0; j < trackbacks.length; j++) {
                            Trackback trackback = trackbacks[j];
                            if (trackback.getId().equals(Integer.valueOf(blogTrackbackID))) {
                                try {
                                    _fetcher.deleteTrackback(blog, trackback);

                                    _eventBroadcaster.broadcastEvent(new TrackbackDeletedEvent(this, new Date(), trackback, blog));
                                } catch (FetcherException e) {
                                    if (_logger.isErrorEnabled()) {
                                        _logger.error(e);
                                    }
                                }
                            }
                        }
                    }

                    addOperationResultMessage(context, formatAdminResource(DELETED_TRACKBACKS_KEY, DELETED_TRACKBACKS_KEY, blog.getBlogAdministrationLocale(), new Object[] {new Integer(blogTrackbackIDs.length)}));
                }

                _fetcher.loadEntry(blog, entry);

                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY, entry);
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                entries = new Entry[0];
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);
        } else if (APPROVE_BLOG_TRACKBACKS.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested approve blog trackbacks action");
            }

            String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
            Integer entryId;
            try {
                entryId = Integer.valueOf(blogEntryId);
            } catch (NumberFormatException e) {
                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);

                return entries;
            }

            Entry entry;
            try {
                entry = _fetcher.loadEntry(blog, entryId);
                String[] blogTrackbackIDs = httpServletRequest.getParameterValues(BLOG_TRACKBACK_ID);

                if (blogTrackbackIDs != null && blogTrackbackIDs.length > 0) {
                    for (int i = 0; i < blogTrackbackIDs.length; i++) {
                        String blogTrackbackID = blogTrackbackIDs[i];
                        Trackback[] trackbacks = entry.getTrackbacksAsArray();
                        for (int j = 0; j < trackbacks.length; j++) {
                            Trackback trackback = trackbacks[j];
                            if (trackback.getId().equals(Integer.valueOf(blogTrackbackID))) {
                                try {
                                    trackback.setStatus(ResponseConstants.APPROVED_STATUS);
                                    _fetcher.saveTrackback(blog, trackback);

                                    _eventBroadcaster.broadcastEvent(new TrackbackApprovedEvent(this, new Date(), trackback, blog));
                                } catch (FetcherException e) {
                                    if (_logger.isErrorEnabled()) {
                                        _logger.error(e);
                                    }
                                }
                            }
                        }
                    }

                    addOperationResultMessage(context, formatAdminResource(APPROVED_TRACKBACKS_KEY, APPROVED_TRACKBACKS_KEY, blog.getBlogAdministrationLocale(), new Object[] {new Integer(blogTrackbackIDs.length)}));
                }

                _fetcher.loadEntry(blog, entry);

                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY, entry);
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                entries = new Entry[0];
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);
        } else if (DELETE_BLOG_PINGBACKS.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested delete blog pingbacks action");
            }

            String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
            Integer entryId;
            try {
                entryId = Integer.valueOf(blogEntryId);
            } catch (NumberFormatException e) {
                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);

                return entries;
            }

            Entry entry;
            try {
                entry = _fetcher.loadEntry(blog, entryId);
                String[] blogPingbackIDs = httpServletRequest.getParameterValues(BLOG_PINGBACK_ID);

                if (blogPingbackIDs != null && blogPingbackIDs.length > 0) {
                    for (int i = 0; i < blogPingbackIDs.length; i++) {
                        String blogPingbackID = blogPingbackIDs[i];
                        Pingback[] pingbacks = entry.getPingbacksAsArray();
                        for (int j = 0; j < pingbacks.length; j++) {
                            Pingback pingback = pingbacks[j];
                            if (pingback.getId().equals(Integer.valueOf(blogPingbackID))) {
                                try {
                                    _fetcher.deletePingback(blog, pingback);

                                    _eventBroadcaster.broadcastEvent(new PingbackDeletedEvent(this, new Date(), pingback, blog));
                                } catch (FetcherException e) {
                                    if (_logger.isErrorEnabled()) {
                                        _logger.error(e);
                                    }
                                }
                            }
                        }
                    }

                    addOperationResultMessage(context, formatAdminResource(DELETED_PINGBACKS_KEY, DELETED_PINGBACKS_KEY, blog.getBlogAdministrationLocale(), new Object[] {new Integer(blogPingbackIDs.length)}));
                }

                _fetcher.loadEntry(blog, entry);

                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY, entry);
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                entries = new Entry[0];
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);
        } else if (APPROVE_BLOG_PINGBACKS.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested approve blog pingbacks action");
            }

            String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
            Integer entryId;
            try {
                entryId = Integer.valueOf(blogEntryId);
            } catch (NumberFormatException e) {

                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);

                return entries;
            }

            Entry entry;
            try {
                entry = _fetcher.loadEntry(blog, entryId);
                String[] blogPingbackIDs = httpServletRequest.getParameterValues(BLOG_PINGBACK_ID);

                if (blogPingbackIDs != null && blogPingbackIDs.length > 0) {
                    for (int i = 0; i < blogPingbackIDs.length; i++) {
                        String blogPingbackID = blogPingbackIDs[i];
                        Pingback[] pingbacks = entry.getPingbacksAsArray();
                        for (int j = 0; j < pingbacks.length; j++) {
                            Pingback pingback = pingbacks[j];
                            if (pingback.getId().equals(Integer.valueOf(blogPingbackID))) {
                                try {
                                    pingback.setStatus(ResponseConstants.APPROVED_STATUS);
                                    _fetcher.savePingback(blog, pingback);

                                    _eventBroadcaster.broadcastEvent(new PingbackApprovedEvent(this, new Date(), pingback, blog));
                                } catch (FetcherException e) {
                                    if (_logger.isErrorEnabled()) {
                                        _logger.error(e);
                                    }
                                }
                            }
                        }
                    }

                    addOperationResultMessage(context, formatAdminResource(APPROVED_PINGBACKS_KEY, APPROVED_PINGBACKS_KEY, blog.getBlogAdministrationLocale(), new Object[] {new Integer(blogPingbackIDs.length)}));
                }

                _fetcher.loadEntry(blog, entry);

                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY, entry);
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                entries = new Entry[0];
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);
        }

        return entries;
    }

    /**
     * Send trackback pings to a comma-separated list of trackback URLs
     *
     * @param blog              Blog information
     * @param entry             Blog entry
     * @param blogTrackbackURLs Trackback URLs
     */
    protected void sendTrackbackPings(Blog blog, Entry entry, String blogTrackbackURLs) {
        // Build the URL parameters for the trackback ping URL
        StringBuffer trackbackPingURLParameters = new StringBuffer();
        try {
            StringBuffer entryLink = new StringBuffer(blog.getBlogURL()).append(entry.getBlogCategory().getName()).append(entry.getPostSlug());
            trackbackPingURLParameters.append("&").append(TrackbackPlugin.TRACKBACK_URL_PARAM).append("=").append(entryLink);
            trackbackPingURLParameters.append("&").append(TrackbackPlugin.TRACKBACK_TITLE_PARAM).append("=").append(URLEncoder.encode(entry.getTitle(), BlojsomConstants.UTF8));
            trackbackPingURLParameters.append("&").append(TrackbackPlugin.TRACKBACK_BLOG_NAME_PARAM).append("=").append(URLEncoder.encode(blog.getBlogName(), BlojsomConstants.UTF8));

            String excerpt = entry.getDescription().replaceAll("<.*?>", "");
            if (excerpt.length() > 255) {
                excerpt = excerpt.substring(0, 251);
                excerpt += "...";
            }
            trackbackPingURLParameters.append("&").append(TrackbackPlugin.TRACKBACK_EXCERPT_PARAM).append("=").append(URLEncoder.encode(excerpt, BlojsomConstants.UTF8));
        } catch (UnsupportedEncodingException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }
        }

        String[] trackbackURLs = BlojsomUtils.parseDelimitedList(blogTrackbackURLs, BlojsomConstants.WHITESPACE);
        if (trackbackURLs != null && trackbackURLs.length > 0) {
            for (int i = 0; i < trackbackURLs.length; i++) {
                String trackbackURL = trackbackURLs[i].trim();
                StringBuffer trackbackPingURL = new StringBuffer(trackbackURL);

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Automatically sending trackback ping to URL: " + trackbackPingURL.toString());
                }

                try {
                    URL trackbackUrl = new URL(trackbackPingURL.toString());

                    // Open a connection to the trackback URL and read its input
                    HttpURLConnection trackbackUrlConnection = (HttpURLConnection) trackbackUrl.openConnection();
                    trackbackUrlConnection.setRequestMethod("POST");
                    trackbackUrlConnection.setRequestProperty("Content-Encoding", BlojsomConstants.UTF8);
                    trackbackUrlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    trackbackUrlConnection.setRequestProperty("Content-Length", "" + trackbackPingURLParameters.length());
                    trackbackUrlConnection.setDoOutput(true);
                    trackbackUrlConnection.getOutputStream().write(trackbackPingURLParameters.toString().getBytes(BlojsomConstants.UTF8));
                    trackbackUrlConnection.connect();
                    BufferedReader trackbackStatus = new BufferedReader(new InputStreamReader(trackbackUrlConnection.getInputStream()));
                    String line;
                    StringBuffer status = new StringBuffer();
                    while ((line = trackbackStatus.readLine()) != null) {
                        status.append(line).append("\n");
                    }
                    trackbackUrlConnection.disconnect();

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Trackback status for ping to " + trackbackURL + ": " + status.toString());
                    }
                } catch (IOException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                }
            }
        }
    }
}
