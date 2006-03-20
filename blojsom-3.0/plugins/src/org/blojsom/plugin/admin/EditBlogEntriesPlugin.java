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
import org.blojsom.plugin.pingback.event.PingbackDeletedEvent;
import org.blojsom.plugin.pingback.event.PingbackApprovedEvent;
import org.blojsom.plugin.trackback.event.TrackbackDeletedEvent;
import org.blojsom.plugin.trackback.event.TrackbackApprovedEvent;
import org.blojsom.plugin.common.ResponseConstants;
import org.blojsom.plugin.comment.event.CommentDeletedEvent;
import org.blojsom.plugin.comment.event.CommentApprovedEvent;
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

/**
 * EditBlogEntriesPlugin
 *
 * @author David Czarnecki
 * @version $Id: EditBlogEntriesPlugin.java,v 1.1 2006-03-20 21:30:44 czarneckid Exp $
 * @since blojsom 3.0
 */
public class EditBlogEntriesPlugin extends BaseAdminPlugin {

    private Log _logger = LogFactory.getLog(EditBlogEntriesPlugin.class);

    // Pages
    private static final String EDIT_BLOG_ENTRIES_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blog-entries";
    private static final String EDIT_BLOG_ENTRIES_LIST_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blog-entries-list";
    private static final String EDIT_BLOG_ENTRY_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blog-entry";
    private static final String ADD_BLOG_ENTRY_PAGE = "/org/blojsom/plugin/admin/templates/admin-add-blog-entry";

    // Constants
    protected static final String BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_LIST = "BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_LIST";
    protected static final String BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_CATEGORY = "BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_CATEGORY";
    protected static final String BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY = "BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY";

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

    // Form elements
    protected static final String BLOG_CATEGORY_ID = "blog-category-id";
    protected static final String BLOG_CATEGORY_NAME = "blog-category-name";
    protected static final String BLOG_ENTRY_ID = "blog-entry-id";
    protected static final String BLOG_ENTRY_TITLE = "blog-entry-title";
    protected static final String BLOG_ENTRY_DESCRIPTION = "blog-entry-description";
    protected static final String BLOG_COMMENT_ID = "blog-comment-id";
    protected static final String BLOG_TRACKBACK_ID = "blog-trackback-id";
    protected static final String BLOG_PINGBACK_ID = "blog-pingback-id";
    protected static final String BLOG_ENTRY_PUBLISH_DATETIME = "blog-entry-publish-datetime";
    protected static final String BLOG_TRACKBACK_URLS = "blog-trackback-urls";
    protected static final String BLOG_ENTRY_PROPOSED_NAME = "blog-entry-proposed-name";
    protected static final String PING_BLOG_URLS = "ping-blog-urls";
    protected static final String UPDATED_BLOG_CATEGORY_NAME = "updated-blog-category-name";

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

        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);
        if (BlojsomUtils.checkNullOrBlank(action)) {
            _logger.debug("User did not request edit action");
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
        } else if (PAGE_ACTION.equals(action)) {
            _logger.debug("User requested edit blog entries page");

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRIES_PAGE);
        } else if (EDIT_BLOG_ENTRIES_ACTION.equals(action)) {
            _logger.debug("User requested edit blog entries list page");

            String blogCategoryId = BlojsomUtils.getRequestValue(BLOG_CATEGORY_ID, httpServletRequest);
            Integer categoryId;
            Category category = null;
            try {
                categoryId = Integer.valueOf(blogCategoryId);
            } catch (NumberFormatException e) {
                _logger.error(e);

                categoryId = new Integer(0);
            }

            try {
                entries = _fetcher.loadAllEntriesForCategory(blog, categoryId);
                category = _fetcher.loadCategory(blog, categoryId);

                if (entries != null) {
                    _logger.debug("Retrieved " + entries.length + " entries from category: " + blogCategoryId);
                } else {
                    _logger.debug("No entries found in category: " + blogCategoryId);
                }
            } catch (FetcherException e) {
                _logger.error(e);

                entries = new Entry[0];
            }

            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_CATEGORY, category);
            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_LIST, entries);

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRIES_LIST_PAGE);
        } else if (EDIT_BLOG_ENTRY_ACTION.equals(action)) {
            _logger.debug("User requested edit blog entry action");

            String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
            Integer entryId = null;
            try {
                entryId = Integer.valueOf(blogEntryId);
            } catch (NumberFormatException e) {
                _logger.error(e);

                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                entries = new Entry[0];
            }
            _logger.debug("Blog entry id: " + blogEntryId);

            try {
                Entry entry = _fetcher.newEntry();
                entry.setId(entryId);
                _fetcher.loadEntry(blog, entry);
                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY, entry);

                _eventBroadcaster.processEvent(new ProcessEntryEvent(this, new Date(), entry, blog, httpServletRequest, httpServletResponse, context));
            } catch (FetcherException e) {
                _logger.error(e);

                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                entries = new Entry[0];
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);
        } else if (UPDATE_BLOG_ENTRY_ACTION.equals(action)) {
            _logger.debug("User requested update blog entry action");

            String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
            Integer entryId = null;
            try {
                entryId = Integer.valueOf(blogEntryId);
            } catch (NumberFormatException e) {
                _logger.error(e);

                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                entries = new Entry[0];
            }
            _logger.debug("Blog entry id: " + blogEntryId);

            String blogCategoryId = BlojsomUtils.getRequestValue(BLOG_CATEGORY_ID, httpServletRequest);
            Integer categoryId;
            try {
                categoryId = Integer.valueOf(blogCategoryId);
            } catch (NumberFormatException e) {
                _logger.error(e);

                categoryId = new Integer(0);
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
            //String pingBlogURLS = BlojsomUtils.getRequestValue(PING_BLOG_URLS, httpServletRequest);

            // XXX
            //String sendPingbacks = BlojsomUtils.getRequestValue(PingbackPlugin.PINGBACK_PLUGIN_METADATA_SEND_PINGBACKS, httpServletRequest);

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

                // XXX
                /*
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
                */

                String entryPublishDateTime = httpServletRequest.getParameter(BLOG_ENTRY_PUBLISH_DATETIME);
                if (!BlojsomUtils.checkNullOrBlank(entryPublishDateTime)) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                    try {
                        Date publishDateTime = simpleDateFormat.parse(entryPublishDateTime);
                        _logger.debug("Publishing blog entry at: " + publishDateTime.toString());
                        entryToUpdate.setDate(publishDateTime);
                        entryToUpdate.setModifiedDate(publishDateTime);
                    } catch (ParseException e) {
                        _logger.error(e);
                    }
                } else {
                    entryToUpdate.setModifiedDate(new Date());
                }

                entryToUpdate.setMetaData(entryMetaData);

                _eventBroadcaster.processEvent(new ProcessEntryEvent(this, new Date(), entryToUpdate, blog, httpServletRequest, httpServletResponse, context));

                _fetcher.saveEntry(blog, entryToUpdate);
                _fetcher.loadEntry(blog, entryToUpdate);

                _logger.debug("Updated blog entry: " + entryToUpdate.getId());

                StringBuffer entryLink = new StringBuffer();
                entryLink.append("<a href=\"").append(blog.getBlogURL()).append(entryToUpdate.getBlogCategory().getName()).append("?permalink=").append(entryToUpdate.getTitle()).append("\">").append(entryToUpdate.getTitle()).append("</a>");
                addOperationResultMessage(context, formatAdminResource(UPDATED_BLOG_ENTRY_KEY, UPDATED_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[] {entryLink.toString()}));

                EntryUpdatedEvent updateEvent = new EntryUpdatedEvent(this, new Date(), entryToUpdate, blog);
                _eventBroadcaster.broadcastEvent(updateEvent);

                // Send trackback pings
                // XXX
                if (!BlojsomUtils.checkNullOrBlank(blogTrackbackURLs)) {
                    //sendTrackbackPings(blog, entryToUpdate, blogTrackbackURLs);
                }

                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);
                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY, entryToUpdate);
           } catch (FetcherException e) {
                _logger.error(e);

                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[] {blogEntryId}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRIES_PAGE);
                entries = new Entry[0];
            } catch (org.blojsom.BlojsomException e) {
                _logger.error(e);

                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[] {blogEntryId}));
                entries = new Entry[0];
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRIES_PAGE);
            }
        } else if (DELETE_BLOG_ENTRY_ACTION.equals(action)) {
            _logger.debug("User requested delete blog entry action");

            String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
            Integer entryId = null;
            try {
                entryId = Integer.valueOf(blogEntryId);
            } catch (NumberFormatException e) {
                _logger.error(e);

                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                entries = new Entry[0];
            }
            _logger.debug("Blog entry id: " + blogEntryId);

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
                _logger.error(e);

                addOperationResultMessage(context, formatAdminResource(FAILED_DELETE_BLOG_ENTRY_KEY, FAILED_DELETE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                entries = new Entry[0];
            } catch (org.blojsom.BlojsomException e) {
                _logger.error(e);

                addOperationResultMessage(context, formatAdminResource(FAILED_DELETE_BLOG_ENTRY_KEY, FAILED_DELETE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                entries = new Entry[0];
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRIES_PAGE);
        } else if (NEW_BLOG_ENTRY_ACTION.equals(action)) {
            _logger.debug("User requested new blog entry action");

            _eventBroadcaster.processEvent(new ProcessEntryEvent(this, new Date(), null, blog, httpServletRequest, httpServletResponse, context));

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADD_BLOG_ENTRY_PAGE);
        } else if (ADD_BLOG_ENTRY_ACTION.equals(action)) {
            _logger.debug("User requested add blog entry action");
            String blogCategoryId = BlojsomUtils.getRequestValue(BLOG_CATEGORY_ID, httpServletRequest);
            Integer categoryId;
            try {
                categoryId = Integer.valueOf(blogCategoryId);
            } catch (NumberFormatException e) {
                _logger.error(e);

                categoryId = new Integer(0);
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
            //String pingBlogURLS = BlojsomUtils.getRequestValue(PING_BLOG_URLS, httpServletRequest);

            // XXX
            //String sendPingbacks = BlojsomUtils.getRequestValue(PingbackPlugin.PINGBACK_PLUGIN_METADATA_SEND_PINGBACKS, httpServletRequest);

            Entry entry;
            entry = _fetcher.newEntry();

            entry.setBlogId(blog.getBlogId());
            entry.setTitle(blogEntryTitle);
            entry.setBlogCategoryId(categoryId);
            entry.setDescription(blogEntryDescription);
            entry.setDate(new Date());
            entry.setModifiedDate(entry.getDate());
            entry.setBlogCategoryId(categoryId);

            Map entryMetaData = new HashMap();
            username = (String) httpServletRequest.getSession().getAttribute(blog.getBlogAdminURL() + "_" + BLOJSOM_ADMIN_PLUGIN_USERNAME_KEY);
            entry.setAuthor(username);

            String entryPublishDateTime = httpServletRequest.getParameter(BLOG_ENTRY_PUBLISH_DATETIME);
            if (!BlojsomUtils.checkNullOrBlank(entryPublishDateTime)) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                try {
                    Date publishDateTime = simpleDateFormat.parse(entryPublishDateTime);
                    _logger.debug("Publishing blog entry at: " + publishDateTime.toString());
                    entry.setDate(publishDateTime);
                    entry.setModifiedDate(publishDateTime);
                } catch (ParseException e) {
                    _logger.error(e);
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

            // XXX
            /*
            if (BlojsomUtils.checkNullOrBlank(pingBlogURLS)) {
                entryMetaData.put(WeblogsPingPlugin.NO_PING_WEBLOGS_METADATA, "true");
            }

            if (!BlojsomUtils.checkNullOrBlank(sendPingbacks)) {
                entryMetaData.put(PingbackPlugin.PINGBACK_PLUGIN_METADATA_SEND_PINGBACKS, "true");
            }
            */

            entry.setMetaData(entryMetaData);

            try {
                _eventBroadcaster.processEvent(new ProcessEntryEvent(this, new Date(), entry, blog, httpServletRequest, httpServletResponse, context));

                _fetcher.saveEntry(blog, entry);
                _fetcher.loadEntry(blog, entry);

                StringBuffer entryLink = new StringBuffer();
                entryLink.append("<a href=\"").append(blog.getBlogURL()).append(entry.getBlogCategory().getName()).append(entry.getTitle()).append("\">").append(entry.getTitle()).append("</a>");
                addOperationResultMessage(context, formatAdminResource(ADDED_BLOG_ENTRY_KEY, ADDED_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{entryLink.toString()}));

                EntryAddedEvent addedEvent = new EntryAddedEvent(this, new Date(), entry, blog);
                _eventBroadcaster.broadcastEvent(addedEvent);
            } catch (org.blojsom.BlojsomException e) {
                _logger.error(e);
                addOperationResultMessage(context, formatAdminResource(FAILED_ADD_BLOG_ENTRY_KEY, FAILED_ADD_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogCategoryId}));
            }

            // XXX
            // Send trackback pings
            if (!BlojsomUtils.checkNullOrBlank(blogTrackbackURLs)) {
                //sendTrackbackPings(blog, entry, blogTrackbackURLs);
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_ACTION);
            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY, entry);
        } else if (DELETE_BLOG_COMMENTS.equals(action)) {
            _logger.debug("User requested delete comments action");

            String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
            Integer entryId;
            try {
                entryId = Integer.valueOf(blogEntryId);
            } catch (NumberFormatException e) {
                _logger.error(e);

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
                                    _logger.error(e);
                                }
                            }
                        }
                    }

                    addOperationResultMessage(context, formatAdminResource(DELETED_COMMENTS_KEY, DELETED_COMMENTS_KEY, blog.getBlogAdministrationLocale(), new Object[] {new Integer(blogCommentIDs.length)}));
                }

                _fetcher.loadEntry(blog, entry);

                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY, entry);
            } catch (FetcherException e) {
                _logger.error(e);

                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                entries = new Entry[0];
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);
        } else if (APPROVE_BLOG_COMMENTS.equals(action)) {
            _logger.debug("User requested approve comments action");

            String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
            Integer entryId;
            try {
                entryId = Integer.valueOf(blogEntryId);
            } catch (NumberFormatException e) {
                _logger.error(e);

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
                _logger.error(e);

                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                entries = new Entry[0];
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);
        } else if (DELETE_BLOG_TRACKBACKS.equals(action)) {
            _logger.debug("User requested delete blog trackbacks action");

            String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
            Integer entryId;
            try {
                entryId = Integer.valueOf(blogEntryId);
            } catch (NumberFormatException e) {
                _logger.error(e);

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
                                    _logger.error(e);
                                }
                            }
                        }
                    }

                    addOperationResultMessage(context, formatAdminResource(DELETED_TRACKBACKS_KEY, DELETED_TRACKBACKS_KEY, blog.getBlogAdministrationLocale(), new Object[] {new Integer(blogTrackbackIDs.length)}));
                }

                _fetcher.loadEntry(blog, entry);

                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY, entry);
            } catch (FetcherException e) {
                _logger.error(e);

                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                entries = new Entry[0];
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);
        } else if (APPROVE_BLOG_TRACKBACKS.equals(action)) {
            _logger.debug("User requested approve blog trackbacks action");

            String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
            Integer entryId;
            try {
                entryId = Integer.valueOf(blogEntryId);
            } catch (NumberFormatException e) {
                _logger.error(e);

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
                                    _logger.error(e);
                                }
                            }
                        }
                    }

                    addOperationResultMessage(context, formatAdminResource(APPROVED_TRACKBACKS_KEY, APPROVED_TRACKBACKS_KEY, blog.getBlogAdministrationLocale(), new Object[] {new Integer(blogTrackbackIDs.length)}));
                }

                _fetcher.loadEntry(blog, entry);

                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY, entry);
            } catch (FetcherException e) {
                _logger.error(e);

                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                entries = new Entry[0];
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);
        } else if (DELETE_BLOG_PINGBACKS.equals(action)) {
            _logger.debug("User requested delete blog pingbacks action");

            String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
            Integer entryId;
            try {
                entryId = Integer.valueOf(blogEntryId);
            } catch (NumberFormatException e) {
                _logger.error(e);

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
                                    _logger.error(e);
                                }
                            }
                        }
                    }

                    addOperationResultMessage(context, formatAdminResource(DELETED_PINGBACKS_KEY, DELETED_PINGBACKS_KEY, blog.getBlogAdministrationLocale(), new Object[] {new Integer(blogPingbackIDs.length)}));
                }

                _fetcher.loadEntry(blog, entry);

                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY, entry);
            } catch (FetcherException e) {
                _logger.error(e);

                addOperationResultMessage(context, formatAdminResource(FAILED_RETRIEVE_BLOG_ENTRY_KEY, FAILED_RETRIEVE_BLOG_ENTRY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogEntryId}));
                entries = new Entry[0];
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);
        } else if (APPROVE_BLOG_PINGBACKS.equals(action)) {
            _logger.debug("User requested approve blog pingbacks action");

            String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
            Integer entryId;
            try {
                entryId = Integer.valueOf(blogEntryId);
            } catch (NumberFormatException e) {
                _logger.error(e);

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
                                    _logger.error(e);
                                }
                            }
                        }
                    }

                    addOperationResultMessage(context, formatAdminResource(APPROVED_PINGBACKS_KEY, APPROVED_PINGBACKS_KEY, blog.getBlogAdministrationLocale(), new Object[] {new Integer(blogPingbackIDs.length)}));
                }

                _fetcher.loadEntry(blog, entry);

                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY, entry);
            } catch (FetcherException e) {
                _logger.error(e);

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
    /*
    protected void sendTrackbackPings(Blog blog, BlogEntry entry, String blogTrackbackURLs) {
        // Build the URL parameters for the trackback ping URL
        StringBuffer trackbackPingURLParameters = new StringBuffer();
        try {
            trackbackPingURLParameters.append("&").append(TrackbackPlugin.TRACKBACK_URL_PARAM).append("=").append(entry.getLink());
            trackbackPingURLParameters.append("&").append(TrackbackPlugin.TRACKBACK_TITLE_PARAM).append("=").append(URLEncoder.encode(entry.getTitle(), UTF8));
            trackbackPingURLParameters.append("&").append(TrackbackPlugin.TRACKBACK_BLOG_NAME_PARAM).append("=").append(URLEncoder.encode(blog.getBlogName(), UTF8));

            String excerpt = entry.getDescription().replaceAll("<.*?>", "");
            if (excerpt.length() > 255) {
                excerpt = excerpt.substring(0, 251);
                excerpt += "...";
            }
            trackbackPingURLParameters.append("&").append(TrackbackPlugin.TRACKBACK_EXCERPT_PARAM).append("=").append(URLEncoder.encode(excerpt, UTF8));
        } catch (UnsupportedEncodingException e) {
            _logger.error(e);
        }

        String[] trackbackURLs = BlojsomUtils.parseDelimitedList(blogTrackbackURLs, WHITESPACE);
        if (trackbackURLs != null && trackbackURLs.length > 0) {
            for (int i = 0; i < trackbackURLs.length; i++) {
                String trackbackURL = trackbackURLs[i].trim();
                StringBuffer trackbackPingURL = new StringBuffer(trackbackURL);

                _logger.debug("Automatically sending trackback ping to URL: " + trackbackPingURL.toString());

                try {
                    URL trackbackUrl = new URL(trackbackPingURL.toString());

                    // Open a connection to the trackback URL and read its input
                    HttpURLConnection trackbackUrlConnection = (HttpURLConnection) trackbackUrl.openConnection();
                    trackbackUrlConnection.setRequestMethod("POST");
                    trackbackUrlConnection.setRequestProperty("Content-Encoding", UTF8);
                    trackbackUrlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    trackbackUrlConnection.setRequestProperty("Content-Length", "" + trackbackPingURLParameters.length());
                    trackbackUrlConnection.setDoOutput(true);
                    trackbackUrlConnection.getOutputStream().write(trackbackPingURLParameters.toString().getBytes(UTF8));
                    trackbackUrlConnection.connect();
                    BufferedReader trackbackStatus = new BufferedReader(new InputStreamReader(trackbackUrlConnection.getInputStream()));
                    String line;
                    StringBuffer status = new StringBuffer();
                    while ((line = trackbackStatus.readLine()) != null) {
                        status.append(line).append("\n");
                    }
                    trackbackUrlConnection.disconnect();

                    _logger.debug("Trackback status for ping to " + trackbackURL + ": " + status.toString());
                } catch (IOException e) {
                    _logger.error(e);
                }
            }
        }
    }
    */
}
