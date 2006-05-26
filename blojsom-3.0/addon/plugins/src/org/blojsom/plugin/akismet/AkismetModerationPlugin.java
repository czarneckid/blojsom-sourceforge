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

import net.sf.akismet.Akismet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.event.Event;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.event.Listener;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.comment.CommentModerationPlugin;
import org.blojsom.plugin.comment.CommentPlugin;
import org.blojsom.plugin.comment.event.CommentResponseSubmissionEvent;
import org.blojsom.plugin.comment.event.CommentMarkedSpamEvent;
import org.blojsom.plugin.comment.event.CommentUnmarkedSpamEvent;
import org.blojsom.plugin.pingback.event.PingbackResponseSubmissionEvent;
import org.blojsom.plugin.pingback.event.PingbackMarkedSpamEvent;
import org.blojsom.plugin.pingback.event.PingbackUnmarkedSpamEvent;
import org.blojsom.plugin.pingback.PingbackPlugin;
import org.blojsom.plugin.response.event.ResponseSubmissionEvent;
import org.blojsom.plugin.trackback.TrackbackModerationPlugin;
import org.blojsom.plugin.trackback.TrackbackPlugin;
import org.blojsom.plugin.trackback.event.TrackbackResponseSubmissionEvent;
import org.blojsom.plugin.trackback.event.TrackbackUnmarkedSpamEvent;
import org.blojsom.plugin.trackback.event.TrackbackMarkedSpamEvent;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Akismet moderation plugin
 *
 * @author David Czarnecki
 * @version $Id: AkismetModerationPlugin.java,v 1.6 2006-05-26 21:56:56 czarneckid Exp $
 * @since blojsom 3.0
 */
public class AkismetModerationPlugin implements Plugin, Listener {

    private Log _logger = LogFactory.getLog(AkismetModerationPlugin.class);

    // Initialization parameters
    private static final String AKISMET_PLUGIN_API_KEY_IP = "akismet-plugin-api-key";
    private static final String AKISMET_PLUGIN_DELETE_SPAM_IP = "akismet-plugin-delete-spam";
    private static final String AKISMET_PLUGIN_AUTOMATIC_APPROVAL_IP = "akismet-plugin-automatic-approval";
    private static final boolean DELETE_SPAM_DEFAULT = false;

    // Context variables
    private static final String AKISMET_PLUGIN_RESPONSE = "AKISMET_PLUGIN_RESPONSE";

    private EventBroadcaster _eventBroadcaster;

    /**
     * Construct a new instance of the Akismet moderation plugin
     */
    public AkismetModerationPlugin() {
    }

    /**
     * Set the {@link org.blojsom.event.EventBroadcaster} event broadcaster
     *
     * @param eventBroadcaster {@link org.blojsom.event.EventBroadcaster}
     */
    public void setEventBroadcaster(EventBroadcaster eventBroadcaster) {
        _eventBroadcaster = eventBroadcaster;
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error initializing the plugin
     */
    public void init() throws PluginException {
        _eventBroadcaster.addListener(this);
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
        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws PluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error in finalizing this plugin
     */
    public void destroy() throws PluginException {
    }

    /**
     * Handle an event broadcast from another component
     *
     * @param event {@link org.blojsom.event.Event} to be handled
     */
    public void handleEvent(Event event) {
    }

    /**
     * Process an event from another component
     *
     * @param event {@link org.blojsom.event.Event} to be handled
     */
    public void processEvent(Event event) {
        if (event instanceof ResponseSubmissionEvent) {
            ResponseSubmissionEvent responseSubmissionEvent = (ResponseSubmissionEvent) event;
            Blog blog = responseSubmissionEvent.getBlog();

            String akismetAPIKey = blog.getProperty(AKISMET_PLUGIN_API_KEY_IP);
            if (BlojsomUtils.checkNullOrBlank(akismetAPIKey)) {
                if (_logger.isInfoEnabled()) {
                    _logger.info("No Akismet API key provided for blog property: " + AKISMET_PLUGIN_API_KEY_IP);
                }
            } else {
                Akismet akismet = new Akismet(akismetAPIKey, blog.getBlogURL());
                String responseType = Akismet.COMMENT_TYPE_BLANK;

                if (responseSubmissionEvent instanceof CommentResponseSubmissionEvent) {
                    responseType = Akismet.COMMENT_TYPE_COMMENT;
                } else if (responseSubmissionEvent instanceof TrackbackResponseSubmissionEvent) {
                    responseType = Akismet.COMMENT_TYPE_TRACKBACK;
                } else if (responseSubmissionEvent instanceof PingbackResponseSubmissionEvent) {
                    responseType = Akismet.COMMENT_TYPE_PINGBACK;
                }

                // Check the content from Akismet
                HttpServletRequest httpServletRequest = responseSubmissionEvent.getHttpServletRequest();
                Map metaData = responseSubmissionEvent.getMetaData();

                StringBuffer entryLink = new StringBuffer().append(blog.getBlogURL())
                        .append(responseSubmissionEvent.getEntry().getCategory())
                        .append(responseSubmissionEvent.getEntry().getPostSlug());

                boolean isSpam = akismet.commentCheck(httpServletRequest.getRemoteAddr(), httpServletRequest.getHeader("User-Agent"), httpServletRequest.getHeader("Referer"), entryLink.toString(), responseType, responseSubmissionEvent.getSubmitter(), responseSubmissionEvent.getSubmitterItem1(), responseSubmissionEvent.getSubmitterItem2(), responseSubmissionEvent.getContent(), null);

                metaData.put(AKISMET_PLUGIN_RESPONSE, Boolean.valueOf(isSpam).toString());

                // If Akismet identifies the content as spam, process for moderation or deletion accordingly
                if (isSpam) {
                    boolean deleteSpam = DELETE_SPAM_DEFAULT;

                    String deleteSpamValue = responseSubmissionEvent.getBlog().getProperty(AKISMET_PLUGIN_DELETE_SPAM_IP);
                    if (!BlojsomUtils.checkNullOrBlank(deleteSpamValue)) {
                        deleteSpam = Boolean.valueOf(deleteSpamValue).booleanValue();
                    }

                    if (!deleteSpam) {
                        if (_logger.isDebugEnabled()) {
                            _logger.debug("Marking response for moderation");
                        }
                    } else {
                        if (_logger.isDebugEnabled()) {
                            _logger.debug("Marking response for automatic deletion");
                        }
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
                    } else if (responseSubmissionEvent instanceof PingbackResponseSubmissionEvent) {
                        if (!deleteSpam) {
                            metaData.put(PingbackPlugin.BLOJSOM_PINGBACK_PLUGIN_APPROVED, Boolean.FALSE.toString());
                        } else {
                            metaData.put(PingbackPlugin.BLOJSOM_PLUGIN_PINGBACK_METADATA_DESTROY, Boolean.TRUE);
                        }
                    }
                } else {
                    boolean automaticApproval = Boolean.valueOf(blog.getProperty(AKISMET_PLUGIN_AUTOMATIC_APPROVAL_IP)).booleanValue();
                    if (automaticApproval) {
                        if (responseSubmissionEvent instanceof CommentResponseSubmissionEvent) {
                            if (!metaData.containsKey(CommentModerationPlugin.BLOJSOM_COMMENT_MODERATION_PLUGIN_APPROVED)
                                    && !"false".equals(metaData.get(CommentModerationPlugin.BLOJSOM_COMMENT_MODERATION_PLUGIN_APPROVED))
                                    && !metaData.containsKey(CommentPlugin.BLOJSOM_PLUGIN_COMMENT_METADATA_DESTROY)) {
                                metaData.put(CommentModerationPlugin.BLOJSOM_COMMENT_MODERATION_PLUGIN_APPROVED, Boolean.TRUE.toString());
                            }
                        } else if (responseSubmissionEvent instanceof TrackbackResponseSubmissionEvent) {
                            if (!metaData.containsKey(TrackbackModerationPlugin.BLOJSOM_TRACKBACK_MODERATION_PLUGIN_APPROVED)
                                    && !"false".equals(metaData.get(TrackbackModerationPlugin.BLOJSOM_TRACKBACK_MODERATION_PLUGIN_APPROVED))
                                    && !metaData.containsKey(TrackbackPlugin.BLOJSOM_PLUGIN_TRACKBACK_METADATA_DESTROY)) {
                                metaData.put(TrackbackModerationPlugin.BLOJSOM_TRACKBACK_MODERATION_PLUGIN_APPROVED, Boolean.TRUE.toString());
                            }
                        } else if (responseSubmissionEvent instanceof PingbackResponseSubmissionEvent) {
                            if (!metaData.containsKey(PingbackPlugin.BLOJSOM_PINGBACK_PLUGIN_APPROVED)
                                    && !"false".equals(metaData.get(PingbackPlugin.BLOJSOM_PINGBACK_PLUGIN_APPROVED))
                                    && !metaData.containsKey(PingbackPlugin.BLOJSOM_PLUGIN_PINGBACK_METADATA_DESTROY)) {
                                metaData.put(PingbackPlugin.BLOJSOM_PINGBACK_PLUGIN_APPROVED, Boolean.TRUE.toString());
                            }
                        }
                    }
                }
            }
        } else if (event instanceof CommentMarkedSpamEvent) {
            CommentMarkedSpamEvent commentMarkedSpamEvent = (CommentMarkedSpamEvent) event;
            Blog blog = commentMarkedSpamEvent.getBlog();

            String akismetAPIKey = blog.getProperty(AKISMET_PLUGIN_API_KEY_IP);
            if (BlojsomUtils.checkNullOrBlank(akismetAPIKey)) {
                if (_logger.isInfoEnabled()) {
                    _logger.info("No Akismet API key provided for blog property: " + AKISMET_PLUGIN_API_KEY_IP);
                }
            } else {
                Akismet akismet = new Akismet(akismetAPIKey, blog.getBlogURL());
                String responseType = Akismet.COMMENT_TYPE_COMMENT;
                StringBuffer permalink = new StringBuffer();
                permalink.append(blog.getBlogURL()).append("/")
                        .append(commentMarkedSpamEvent.getEntry().getBlogCategory().getName())
                        .append(commentMarkedSpamEvent.getEntry().getPostSlug());

                akismet.submitSpam(commentMarkedSpamEvent.getComment().getIp(),
                        null, null, permalink.toString(), responseType, commentMarkedSpamEvent.getComment().getAuthor(),
                        commentMarkedSpamEvent.getComment().getAuthorEmail(),
                        commentMarkedSpamEvent.getComment().getAuthorURL(),
                        commentMarkedSpamEvent.getComment().getComment(), null);
            }
        } else if (event instanceof CommentUnmarkedSpamEvent) {
            CommentUnmarkedSpamEvent commentUnmarkedSpamEvent = (CommentUnmarkedSpamEvent) event;
            Blog blog = commentUnmarkedSpamEvent.getBlog();

            String akismetAPIKey = blog.getProperty(AKISMET_PLUGIN_API_KEY_IP);
            if (BlojsomUtils.checkNullOrBlank(akismetAPIKey)) {
                if (_logger.isInfoEnabled()) {
                    _logger.info("No Akismet API key provided for blog property: " + AKISMET_PLUGIN_API_KEY_IP);
                }
            } else {
                Akismet akismet = new Akismet(akismetAPIKey, blog.getBlogURL());
                String responseType = Akismet.COMMENT_TYPE_COMMENT;
                StringBuffer permalink = new StringBuffer();
                permalink.append(blog.getBlogURL()).append("/")
                        .append(commentUnmarkedSpamEvent.getEntry().getBlogCategory().getName())
                        .append(commentUnmarkedSpamEvent.getEntry().getPostSlug());

                akismet.submitHam(commentUnmarkedSpamEvent.getComment().getIp(),
                        null, null, permalink.toString(), responseType, commentUnmarkedSpamEvent.getComment().getAuthor(),
                        commentUnmarkedSpamEvent.getComment().getAuthorEmail(),
                        commentUnmarkedSpamEvent.getComment().getAuthorURL(),
                        commentUnmarkedSpamEvent.getComment().getComment(), null);
            }
        } else if (event instanceof TrackbackMarkedSpamEvent) {
            TrackbackMarkedSpamEvent trackbackMarkedSpamEvent = (TrackbackMarkedSpamEvent) event;
            Blog blog = trackbackMarkedSpamEvent.getBlog();

            String akismetAPIKey = blog.getProperty(AKISMET_PLUGIN_API_KEY_IP);
            if (BlojsomUtils.checkNullOrBlank(akismetAPIKey)) {
                if (_logger.isInfoEnabled()) {
                    _logger.info("No Akismet API key provided for blog property: " + AKISMET_PLUGIN_API_KEY_IP);
                }
            } else {
                Akismet akismet = new Akismet(akismetAPIKey, blog.getBlogURL());
                String responseType = Akismet.COMMENT_TYPE_TRACKBACK;
                StringBuffer permalink = new StringBuffer();
                permalink.append(blog.getBlogURL()).append("/")
                        .append(trackbackMarkedSpamEvent.getEntry().getBlogCategory().getName())
                        .append(trackbackMarkedSpamEvent.getEntry().getPostSlug());

                akismet.submitSpam(trackbackMarkedSpamEvent.getTrackback().getIp(),
                        null, null, permalink.toString(), responseType, trackbackMarkedSpamEvent.getTrackback().getTitle(),
                        trackbackMarkedSpamEvent.getTrackback().getBlogName(),
                        trackbackMarkedSpamEvent.getTrackback().getUrl(),
                        trackbackMarkedSpamEvent.getTrackback().getExcerpt(), null);
            }
        } else if (event instanceof TrackbackUnmarkedSpamEvent) {
            TrackbackUnmarkedSpamEvent trackbackUnmarkedSpamEvent = (TrackbackUnmarkedSpamEvent) event;
            Blog blog = trackbackUnmarkedSpamEvent.getBlog();

            String akismetAPIKey = blog.getProperty(AKISMET_PLUGIN_API_KEY_IP);
            if (BlojsomUtils.checkNullOrBlank(akismetAPIKey)) {
                if (_logger.isInfoEnabled()) {
                    _logger.info("No Akismet API key provided for blog property: " + AKISMET_PLUGIN_API_KEY_IP);
                }
            } else {
                Akismet akismet = new Akismet(akismetAPIKey, blog.getBlogURL());
                String responseType = Akismet.COMMENT_TYPE_TRACKBACK;
                StringBuffer permalink = new StringBuffer();
                permalink.append(blog.getBlogURL()).append("/")
                        .append(trackbackUnmarkedSpamEvent.getEntry().getBlogCategory().getName())
                        .append(trackbackUnmarkedSpamEvent.getEntry().getPostSlug());

                akismet.submitHam(trackbackUnmarkedSpamEvent.getTrackback().getIp(),
                        null, null, permalink.toString(), responseType, trackbackUnmarkedSpamEvent.getTrackback().getTitle(),
                        trackbackUnmarkedSpamEvent.getTrackback().getBlogName(),
                        trackbackUnmarkedSpamEvent.getTrackback().getUrl(),
                        trackbackUnmarkedSpamEvent.getTrackback().getExcerpt(), null);
            }
        } else if (event instanceof PingbackMarkedSpamEvent) {
            PingbackMarkedSpamEvent pingbackMarkedSpamEvent = (PingbackMarkedSpamEvent) event;
            Blog blog = pingbackMarkedSpamEvent.getBlog();

            String akismetAPIKey = blog.getProperty(AKISMET_PLUGIN_API_KEY_IP);
            if (BlojsomUtils.checkNullOrBlank(akismetAPIKey)) {
                if (_logger.isInfoEnabled()) {
                    _logger.info("No Akismet API key provided for blog property: " + AKISMET_PLUGIN_API_KEY_IP);
                }
            } else {
                Akismet akismet = new Akismet(akismetAPIKey, blog.getBlogURL());
                String responseType = Akismet.COMMENT_TYPE_PINGBACK;
                StringBuffer permalink = new StringBuffer();
                permalink.append(blog.getBlogURL()).append("/")
                        .append(pingbackMarkedSpamEvent.getEntry().getBlogCategory().getName())
                        .append(pingbackMarkedSpamEvent.getEntry().getPostSlug());

                akismet.submitSpam(pingbackMarkedSpamEvent.getPingback().getIp(),
                        null, null, permalink.toString(), responseType, pingbackMarkedSpamEvent.getPingback().getTitle(),
                        pingbackMarkedSpamEvent.getPingback().getBlogName(),
                        pingbackMarkedSpamEvent.getPingback().getUrl(),
                        pingbackMarkedSpamEvent.getPingback().getExcerpt(), null);
            }
        } else if (event instanceof PingbackUnmarkedSpamEvent) {
            PingbackUnmarkedSpamEvent pingbackUnmarkedSpamEvent = (PingbackUnmarkedSpamEvent) event;
            Blog blog = pingbackUnmarkedSpamEvent.getBlog();

            String akismetAPIKey = blog.getProperty(AKISMET_PLUGIN_API_KEY_IP);
            if (BlojsomUtils.checkNullOrBlank(akismetAPIKey)) {
                if (_logger.isInfoEnabled()) {
                    _logger.info("No Akismet API key provided for blog property: " + AKISMET_PLUGIN_API_KEY_IP);
                }
            } else {
                Akismet akismet = new Akismet(akismetAPIKey, blog.getBlogURL());
                String responseType = Akismet.COMMENT_TYPE_PINGBACK;
                StringBuffer permalink = new StringBuffer();
                permalink.append(blog.getBlogURL()).append("/")
                        .append(pingbackUnmarkedSpamEvent.getEntry().getBlogCategory().getName())
                        .append(pingbackUnmarkedSpamEvent.getEntry().getPostSlug());

                akismet.submitHam(pingbackUnmarkedSpamEvent.getPingback().getIp(),
                        null, null, permalink.toString(), responseType, pingbackUnmarkedSpamEvent.getPingback().getTitle(),
                        pingbackUnmarkedSpamEvent.getPingback().getBlogName(),
                        pingbackUnmarkedSpamEvent.getPingback().getUrl(),
                        pingbackUnmarkedSpamEvent.getPingback().getExcerpt(), null);
            }
        }
    }
}
