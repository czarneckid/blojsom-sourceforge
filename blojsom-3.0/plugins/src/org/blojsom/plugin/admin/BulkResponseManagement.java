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
import org.blojsom.plugin.comment.event.CommentApprovedEvent;
import org.blojsom.plugin.comment.event.CommentDeletedEvent;
import org.blojsom.plugin.comment.event.CommentMarkedSpamEvent;
import org.blojsom.plugin.common.ResponseConstants;
import org.blojsom.plugin.pingback.event.PingbackApprovedEvent;
import org.blojsom.plugin.pingback.event.PingbackDeletedEvent;
import org.blojsom.plugin.pingback.event.PingbackMarkedSpamEvent;
import org.blojsom.plugin.trackback.event.TrackbackApprovedEvent;
import org.blojsom.plugin.trackback.event.TrackbackDeletedEvent;
import org.blojsom.plugin.trackback.event.TrackbackMarkedSpamEvent;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

/**
 * Bulk Response Management plugin
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: BulkResponseManagement.java,v 1.5 2006-09-26 02:55:20 czarneckid Exp $
 */
public class BulkResponseManagement extends BaseAdminPlugin {

    private Log _logger = LogFactory.getLog(BulkResponseManagement.class);

    private static final String BULK_RESPONSE_MANAGEMENT_PERMISSION = "bulk_response_management_permission";
    private static final String BLOJSOM_USER_OBJECT = "BLOJSOM_USER_OBJECT";

    private String [] DEFAULT_RESPONSE_STATUS_LIST = {ResponseConstants.NEW_STATUS, ResponseConstants.SPAM_STATUS};

    // Localization constants
    private static final String FAILED_BULK_PERMISSION_KEY = "failed.bulk.permission.text";
    private static final String SUCCESFUL_BULK_PROCESSING_KEY = "successful.bulk.processing.text";

    // Pages
    private static final String BULK_RESPONSE_MANAGEMENT_PAGE = "/org/blojsom/plugin/admin/templates/bulk-response-management";

    // Actions
    private static final String BULK_RESPONSE_MANAGEMENT_ACTION = "bulk-response-management";

    // Context attributes
    private static final String BULK_RESPONSES = "BULK_RESPONSES";

    // Form items
    private static final String DELETE_COMMENTS = "delete_comments";
    private static final String APPROVE_COMMENTS = "approve_comments";
    private static final String DELETE_TRACKBACKS = "delete_trackbacks";
    private static final String APPROVE_TRACKBACKS = "approve_trackbacks";
    private static final String DELETE_PINGBACKS = "delete_pingbacks";
    private static final String APPROVE_PINGBACKS = "approve_pingbacks";
    private static final String MARK_SPAM_COMMENTS = "mark_spam_comments";
    private static final String MARK_SPAM_TRACKBACKS = "mark_spam_trackbacks";
    private static final String MARK_SPAM_PINGBACKS = "mark_spam_pingbacks";
    private static final String QUERY = "query";

    private Fetcher _fetcher;
    private EventBroadcaster _eventBroadcaster;
    private String[] _responseStatusList = DEFAULT_RESPONSE_STATUS_LIST;

    /**
     * Create a new instance of the bulk response management plugin.
     */
    public BulkResponseManagement() {
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
     * Set the response status codes to search for in bulk management
     *
     * @param statusList Status list
     */
    public void setResponseStatusList(String[] statusList) {
        _responseStatusList = statusList;
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
        if (!checkPermission(blog, null, username, BULK_RESPONSE_MANAGEMENT_PERMISSION)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            addOperationResultMessage(context, getAdminResource(FAILED_BULK_PERMISSION_KEY, FAILED_BULK_PERMISSION_KEY, blog.getBlogAdministrationLocale()));

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
        if (BlojsomUtils.checkNullOrBlank(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User did not request edit action");
            }
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
        } else if (PAGE_ACTION.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested bulk response edit page");
            }
        } else if (BULK_RESPONSE_MANAGEMENT_ACTION.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested bulk response management action");
            }

            int commentsApproved = 0;
            int commentsDeleted = 0;
            int trackbacksApproved = 0;
            int trackbacksDeleted = 0;
            int pingbacksApproved = 0;
            int pingbacksDeleted = 0;

            Integer entityID;

            String[] markspamComments = BlojsomUtils.getRequestValues(MARK_SPAM_COMMENTS, httpServletRequest);
            for (int i = 0; i < markspamComments.length; i++) {
                try {
                    String item = markspamComments[i];
                    entityID = Integer.valueOf(item);
                    Comment comment = _fetcher.newComment();
                    comment.setBlogId(blog.getId());
                    comment.setId(entityID);

                    _fetcher.loadComment(blog, comment);

                    CommentMarkedSpamEvent commentMarkedSpamEvent = new CommentMarkedSpamEvent(this, new Date(), comment, blog);
                    _eventBroadcaster.broadcastEvent(commentMarkedSpamEvent);

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Marked comment as spam for comment ID: " + entityID);
                    }
                } catch (NumberFormatException e) {
                } catch (FetcherException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                }
            }

            String[] markspamTrackbacks = BlojsomUtils.getRequestValues(MARK_SPAM_TRACKBACKS, httpServletRequest);
            for (int i = 0; i < markspamTrackbacks.length; i++) {
                try {
                    String item = markspamTrackbacks[i];
                    entityID = Integer.valueOf(item);
                    Trackback trackback = _fetcher.newTrackback();
                    trackback.setBlogId(blog.getId());
                    trackback.setId(entityID);

                    _fetcher.loadTrackback(blog, trackback);

                    TrackbackMarkedSpamEvent trackbackMarkedSpamEvent = new TrackbackMarkedSpamEvent(this, new Date(), trackback, blog);
                    _eventBroadcaster.broadcastEvent(trackbackMarkedSpamEvent);

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Marked trackback as spam for trackback ID: " + entityID);
                    }
                } catch (NumberFormatException e) {
                } catch (FetcherException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                }
            }

            String[] markspamPingbacks = BlojsomUtils.getRequestValues(MARK_SPAM_PINGBACKS, httpServletRequest);
            for (int i = 0; i < markspamPingbacks.length; i++) {
                try {
                    String item = markspamPingbacks[i];
                    entityID = Integer.valueOf(item);
                    Pingback pingback = _fetcher.newPingback();
                    pingback.setBlogId(blog.getId());
                    pingback.setId(entityID);

                    _fetcher.loadPingback(blog, pingback);

                    PingbackMarkedSpamEvent pingbackMarkedSpamEvent = new PingbackMarkedSpamEvent(this, new Date(), pingback, blog);
                    _eventBroadcaster.broadcastEvent(pingbackMarkedSpamEvent);

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Marked pingback as spam for pingback ID: " + entityID);
                    }
                } catch (NumberFormatException e) {
                } catch (FetcherException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                }
            }

            String[] delete = BlojsomUtils.getRequestValues(DELETE_COMMENTS, httpServletRequest);
            for (int i = 0; i < delete.length; i++) {
                String item = delete[i];
                try {
                    entityID = Integer.valueOf(item);
                    Comment comment = _fetcher.newComment();
                    comment.setBlogId(blog.getId());
                    comment.setId(entityID);

                    _fetcher.loadComment(blog, comment);
                    _fetcher.deleteComment(blog, comment);
                    commentsDeleted++;

                    _eventBroadcaster.broadcastEvent(new CommentDeletedEvent(this, new Date(), comment, blog));
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Bulk delete comment ID: " + entityID);
                    }
                } catch (NumberFormatException e) {
                } catch (FetcherException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                }
            }

            String[] approve = BlojsomUtils.getRequestValues(APPROVE_COMMENTS, httpServletRequest);
            for (int i = 0; i < approve.length; i++) {
                String item = approve[i];
                try {
                    entityID = Integer.valueOf(item);
                    Comment comment = _fetcher.newComment();
                    comment.setBlogId(blog.getId());
                    comment.setId(entityID);

                    _fetcher.loadComment(blog, comment);
                    comment.setStatus(ResponseConstants.APPROVED_STATUS);
                    _fetcher.saveComment(blog, comment);
                    commentsApproved += 1;

                    _eventBroadcaster.broadcastEvent(new CommentApprovedEvent(this, new Date(), comment, blog));
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Bulk approve comment ID: " + entityID);
                    }
                } catch (NumberFormatException e) {
                } catch (FetcherException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                }
            }

            delete = BlojsomUtils.getRequestValues(DELETE_TRACKBACKS, httpServletRequest);
            for (int i = 0; i < delete.length; i++) {
                String item = delete[i];
                try {
                    entityID = Integer.valueOf(item);
                    Trackback trackback = _fetcher.newTrackback();
                    trackback.setBlogId(blog.getId());
                    trackback.setId(entityID);

                    _fetcher.loadTrackback(blog, trackback);
                    _fetcher.deleteTrackback(blog, trackback);
                    trackbacksDeleted += 1;

                    _eventBroadcaster.broadcastEvent(new TrackbackDeletedEvent(this, new Date(), trackback, blog));
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Bulk delete trackback ID: " + entityID);
                    }
                } catch (NumberFormatException e) {
                } catch (FetcherException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                }
            }

            approve = BlojsomUtils.getRequestValues(APPROVE_TRACKBACKS, httpServletRequest);
            for (int i = 0; i < approve.length; i++) {
                String item = approve[i];
                try {
                    entityID = Integer.valueOf(item);
                    Trackback trackback = _fetcher.newTrackback();
                    trackback.setBlogId(blog.getId());
                    trackback.setId(entityID);

                    _fetcher.loadTrackback(blog, trackback);
                    trackback.setStatus(ResponseConstants.APPROVED_STATUS);
                    _fetcher.saveTrackback(blog, trackback);
                    trackbacksApproved += 1;

                    _eventBroadcaster.broadcastEvent(new TrackbackApprovedEvent(this, new Date(), trackback, blog));
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Bulk approve trackback ID: " + entityID);
                    }
                } catch (NumberFormatException e) {
                } catch (FetcherException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                }
            }

            delete = BlojsomUtils.getRequestValues(DELETE_PINGBACKS, httpServletRequest);
            for (int i = 0; i < delete.length; i++) {
                String item = delete[i];
                try {
                    entityID = Integer.valueOf(item);
                    Pingback pingback = _fetcher.newPingback();
                    pingback.setBlogId(blog.getId());
                    pingback.setId(entityID);

                    _fetcher.loadPingback(blog, pingback);
                    _fetcher.deletePingback(blog, pingback);
                    pingbacksDeleted += 1;

                    _eventBroadcaster.broadcastEvent(new PingbackDeletedEvent(this, new Date(), pingback, blog));
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Bulk delete pingback ID: " + entityID);
                    }
                } catch (NumberFormatException e) {
                } catch (FetcherException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                }
            }

            approve = BlojsomUtils.getRequestValues(APPROVE_PINGBACKS, httpServletRequest);
            for (int i = 0; i < approve.length; i++) {
                String item = approve[i];
                try {
                    entityID = Integer.valueOf(item);
                    Pingback pingback = _fetcher.newPingback();
                    pingback.setBlogId(blog.getId());
                    pingback.setId(entityID);

                    _fetcher.loadPingback(blog, pingback);
                    pingback.setStatus(ResponseConstants.APPROVED_STATUS);
                    _fetcher.savePingback(blog, pingback);
                    pingbacksApproved += 1;

                    _eventBroadcaster.broadcastEvent(new PingbackApprovedEvent(this, new Date(), pingback, blog));
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Bulk approve pingback ID: " + entityID);
                    }
                } catch (NumberFormatException e) {
                } catch (FetcherException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                }
            }

            if (commentsApproved > 0 || commentsDeleted > 0 || trackbacksApproved > 0 || trackbacksDeleted > 0
                    || pingbacksApproved > 0 || pingbacksDeleted > 0) {
                addOperationResultMessage(context, formatAdminResource(SUCCESFUL_BULK_PROCESSING_KEY, SUCCESFUL_BULK_PROCESSING_KEY,
                    blog.getBlogAdministrationLocale(),
                    new Object[] {new Integer(commentsApproved), new Integer(commentsDeleted),
                            new Integer(trackbacksApproved), new Integer(trackbacksDeleted),
                            new Integer(pingbacksApproved), new Integer(pingbacksDeleted)}));
            }
        }

        String query = BlojsomUtils.getRequestValue(QUERY, httpServletRequest);

        // Put the responses on the request
        try {
            if (BlojsomUtils.checkNullOrBlank(query)) {
                context.put(BULK_RESPONSES, _fetcher.findResponsesByStatus(blog, _responseStatusList));
            } else {
                context.put(BULK_RESPONSES, _fetcher.findResponsesByQuery(blog, query));
                context.put(QUERY, BlojsomUtils.escapeString(query));
            }
        } catch (FetcherException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }
        }

        httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, BULK_RESPONSE_MANAGEMENT_PAGE);

        return entries;
    }
}
