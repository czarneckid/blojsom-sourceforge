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
package org.blojsom.plugin.akismet;

import org.blojsom.plugin.BlojsomPlugin;
import org.blojsom.plugin.BlojsomPluginException;
import org.blojsom.plugin.trackback.event.TrackbackResponseSubmissionEvent;
import org.blojsom.plugin.trackback.TrackbackModerationPlugin;
import org.blojsom.plugin.trackback.TrackbackPlugin;
import org.blojsom.plugin.comment.event.CommentResponseSubmissionEvent;
import org.blojsom.plugin.comment.CommentModerationPlugin;
import org.blojsom.plugin.comment.CommentPlugin;
import org.blojsom.plugin.response.event.ResponseSubmissionEvent;
import org.blojsom.blog.BlojsomConfiguration;
import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;
import org.blojsom.blog.Blog;
import org.blojsom.event.BlojsomListener;
import org.blojsom.event.BlojsomEvent;
import org.blojsom.util.BlojsomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import net.sf.akismet.Akismet;

/**
 * Akismet moderation plugin
 *
 * @author David Czarnecki
 * @version $Id: AkismetModerationPlugin.java,v 1.2 2006-01-19 17:28:48 czarneckid Exp $
 * @since blojsom 2.29
 */
public class AkismetModerationPlugin implements BlojsomPlugin, BlojsomListener {

    private Log _logger = LogFactory.getLog(AkismetModerationPlugin.class);

    // Initialization parameters
    private static final String AKISMET_PLUGIN_API_KEY_IP = "akismet-plugin-api-key";
    private static final String AKISMET_PLUGIN_DELETE_SPAM_IP = "akismet-plugin-delete-spam";
    private static final String AKISMET_PLUGIN_AUTOMATIC_APPROVAL_IP = "akismet-plugin-automatic-approval";
    private static final boolean DELETE_SPAM_DEFAULT = false;

    // Context variables
    private static final String AKISMET_PLUGIN_RESPONSE = "AKISMET_PLUGIN_RESPONSE";

    /**
     * Construct a new instance of the Akismet moderation plugin
     */
    public AkismetModerationPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig        Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration {@link org.blojsom.blog.BlojsomConfiguration} information
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws BlojsomPluginException {
        blojsomConfiguration.getEventBroadcaster().addListener(this);
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param user                {@link org.blojsom.blog.BlogUser} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlogUser user, Map context, BlogEntry[] entries) throws BlojsomPluginException {
        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws BlojsomPluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error in finalizing this plugin
     */
    public void destroy() throws BlojsomPluginException {
    }

    /**
     * Handle an event broadcast from another component
     *
     * @param event {@link org.blojsom.event.BlojsomEvent} to be handled
     */
    public void handleEvent(BlojsomEvent event) {
    }

    /**
     * Process an event from another component
     *
     * @param event {@link org.blojsom.event.BlojsomEvent} to be handled
     */
    public void processEvent(BlojsomEvent event) {
        if (event instanceof ResponseSubmissionEvent) {
            ResponseSubmissionEvent responseSubmissionEvent = (ResponseSubmissionEvent) event;
            Blog blog = responseSubmissionEvent.getBlog().getBlog();

            String akismetAPIKey = blog.getBlogProperty(AKISMET_PLUGIN_API_KEY_IP);
            if (BlojsomUtils.checkNullOrBlank(akismetAPIKey)) {
                _logger.info("No Akismet API key provided for blog property: " + AKISMET_PLUGIN_API_KEY_IP);
            } else {
                Akismet akismet = new Akismet(akismetAPIKey, blog.getBlogURL());
                String responseType = Akismet.COMMENT_TYPE_BLANK;

                if (responseSubmissionEvent instanceof CommentResponseSubmissionEvent) {
                    responseType = Akismet.COMMENT_TYPE_COMMENT;
                } else if (responseSubmissionEvent instanceof TrackbackResponseSubmissionEvent) {
                    responseType = Akismet.COMMENT_TYPE_TRACKBACK;
                }

                // Check the content from Akismet
                HttpServletRequest httpServletRequest = responseSubmissionEvent.getHttpServletRequest();
                Map metaData = responseSubmissionEvent.getMetaData();

                boolean isSpam = akismet.commentCheck(httpServletRequest.getRemoteAddr(), httpServletRequest.getHeader("User-Agent"),
                        httpServletRequest.getHeader("Referer"), responseSubmissionEvent.getBlogEntry().getLink(),
                        responseType, responseSubmissionEvent.getSubmitter(), responseSubmissionEvent.getSubmitterItem1(),
                        responseSubmissionEvent.getSubmitterItem2(), responseSubmissionEvent.getContent(), null);

                metaData.put(AKISMET_PLUGIN_RESPONSE, Boolean.valueOf(isSpam));

                // If Akismet identifies the content as spam, process for moderation or deletion accordingly
                if (isSpam) {
                    boolean deleteSpam = DELETE_SPAM_DEFAULT;

                    String deleteSpamValue = responseSubmissionEvent.getBlog().getBlog().getBlogProperty(AKISMET_PLUGIN_DELETE_SPAM_IP);
                    if (!BlojsomUtils.checkNullOrBlank(deleteSpamValue)) {
                        deleteSpam = Boolean.valueOf(deleteSpamValue).booleanValue();
                    }

                    if (!deleteSpam) {
                        _logger.debug("Marking response for moderation");
                    } else {
                        _logger.debug("Marking response for automatic deletion");
                    }

                    if (responseSubmissionEvent instanceof CommentResponseSubmissionEvent) {
                        if (!deleteSpam) {
                            metaData.put(CommentModerationPlugin.BLOJSOM_COMMENT_MODERATION_PLUGIN_APPROVED, Boolean.FALSE.toString());
                        } else {
                            metaData.put(CommentPlugin.BLOJSOM_PLUGIN_COMMENT_METADATA_DESTROY, Boolean.TRUE);
                        }
                    } else if (responseSubmissionEvent instanceof TrackbackResponseSubmissionEvent) {
                        if (!deleteSpam) {
                            metaData.put(TrackbackModerationPlugin.BLOJSOM_TRACKBACK_MODERATION_PLUGIN_APPROVED, Boolean.FALSE.toString());
                        } else {
                            metaData.put(TrackbackPlugin.BLOJSOM_PLUGIN_TRACKBACK_METADATA_DESTROY, Boolean.TRUE);
                        }
                    }
                } else {
                    boolean automaticApproval = Boolean.valueOf(blog.getBlogProperty(AKISMET_PLUGIN_AUTOMATIC_APPROVAL_IP)).booleanValue();
                    if (automaticApproval) {
                        if (responseSubmissionEvent instanceof CommentResponseSubmissionEvent) {
                            metaData.put(CommentModerationPlugin.BLOJSOM_COMMENT_MODERATION_PLUGIN_APPROVED, Boolean.TRUE.toString());
                        } else if (responseSubmissionEvent instanceof TrackbackResponseSubmissionEvent) {
                            metaData.put(TrackbackModerationPlugin.BLOJSOM_TRACKBACK_MODERATION_PLUGIN_APPROVED, Boolean.TRUE.toString());
                        }
                    }
                }
            }
        }
    }
}
