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
package org.blojsom.plugin.comment;

import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.comment.event.CommentResponseSubmissionEvent;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.event.Listener;
import org.blojsom.event.Event;
import org.blojsom.event.EventBroadcaster;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * Math comment authenticator plugin
 *
 * @author David Czarnecki
 * @version $Id: MathCommentAuthenticationPlugin.java,v 1.3 2006-08-22 18:56:06 czarneckid Exp $
 * @since blojsom 3.0
 */
public class MathCommentAuthenticationPlugin extends CommentModerationPlugin implements Listener {

    private Log _logger = LogFactory.getLog(MathCommentAuthenticationPlugin.class);

    private static final String MATH_COMMENT_MODERATION_ENABLED = "math-comment-moderation-enabled";
    private static final String MATH_COMMENT_AUTHENTICATION_OPERATIONS_IP = "math-comment-authentication-operations";
    private static final String MATH_COMMENT_AUTHENTICATION_BOUND_IP = "math-comment-authentication-bound";

    private static final int AVAILABLE_OPERATIONS = 3;
    private static final int BOUND_DEFAULT = 10;

    public static final String BLOJSOM_MATH_AUTHENTICATOR_PLUGIN_ANSWER = "BLOJSOM_MATH_AUTHENTICATOR_PLUGIN_ANSWER";
    public static final String BLOJSOM_MATH_AUTHENTICATOR_PLUGIN_VALUE1 = "BLOJSOM_MATH_AUTHENTICATOR_PLUGIN_VALUE1";
    public static final String BLOJSOM_MATH_AUTHENTICATOR_PLUGIN_VALUE2 = "BLOJSOM_MATH_AUTHENTICATOR_PLUGIN_VALUE2";
    public static final String BLOJSOM_MATH_AUTHENTICATOR_PLUGIN_OPERATION = "BLOJSOM_MATH_AUTHENTICATOR_PLUGIN_OPERATION";
    public static final String BLOJSOM_MATH_AUTHENTICATOR_PLUGIN_ANSWER_CHECK_PARAM = "mathAnswerCheck";
    public static final String BLOJSOM_MATH_AUTHENTICATOR_PLUGIN_STATUS_MESSAGE = "BLOJSOM_MATH_AUTHENTICATOR_PLUGIN_STATUS_MESSAGE";

    private EventBroadcaster _eventBroadcaster;

    /**
     * Math comment authenticator plugin
     */
    public MathCommentAuthenticationPlugin() {
    }

    /**
     * Set the {@link EventBroadcaster} to use
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
        HttpSession httpSession = httpServletRequest.getSession();

        int bound = BOUND_DEFAULT;
        int availableOperations = AVAILABLE_OPERATIONS;
        String boundProperty = blog.getProperty(MATH_COMMENT_AUTHENTICATION_BOUND_IP);
        String availableOperationsProperty = blog.getProperty(MATH_COMMENT_AUTHENTICATION_OPERATIONS_IP);

        if (!BlojsomUtils.checkNullOrBlank(boundProperty)) {
            try {
                bound = Integer.parseInt(boundProperty);
            } catch (NumberFormatException e) {
            }
        }

        if (!BlojsomUtils.checkNullOrBlank(availableOperationsProperty)) {
            try {
                availableOperations = Integer.parseInt(availableOperationsProperty);
                if (availableOperations < 1 || availableOperations > AVAILABLE_OPERATIONS) {
                    availableOperations = AVAILABLE_OPERATIONS;
                } else {
                    availableOperations -= 1;
                }
            } catch (NumberFormatException e) {
            }
        }

        int operation = (int) (Math.random() * (availableOperations + 1));
        int value1 = (int) (Math.random() * bound);
        int value2 = (int) (Math.random() * bound);
        int answer;

        answer = getAnswerForOperation(value1, value2, operation);

        httpSession.setAttribute(BLOJSOM_MATH_AUTHENTICATOR_PLUGIN_VALUE1, new Integer(value1));
        httpSession.setAttribute(BLOJSOM_MATH_AUTHENTICATOR_PLUGIN_VALUE2, new Integer(value2));
        httpSession.setAttribute(BLOJSOM_MATH_AUTHENTICATOR_PLUGIN_ANSWER, new Integer(answer));
        httpSession.setAttribute(BLOJSOM_MATH_AUTHENTICATOR_PLUGIN_OPERATION, getOperatorForOperation(operation));

        return entries;
    }

    /**
     * Simple check to see if comment moderation is enabled
     * <p/>
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param blog                {@link Blog} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error in moderating a comment
     */
    protected void moderateComment(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, Map context, Entry[] entries) throws PluginException {
        if ("true".equalsIgnoreCase(blog.getProperty(COMMENT_MODERATION_ENABLED)) &&
                "true".equalsIgnoreCase(blog.getProperty(MATH_COMMENT_MODERATION_ENABLED))) {
            HttpSession httpSession = httpServletRequest.getSession();

            if ("y".equalsIgnoreCase(httpServletRequest.getParameter(CommentPlugin.COMMENT_PARAM)) && blog.getBlogCommentsEnabled().booleanValue()) {
                String mathAnswerCheck = BlojsomUtils.getRequestValue(BLOJSOM_MATH_AUTHENTICATOR_PLUGIN_ANSWER_CHECK_PARAM, httpServletRequest);

                boolean passedCheck = false;
                if (httpSession.getAttribute(BLOJSOM_MATH_AUTHENTICATOR_PLUGIN_ANSWER) != null) {
                    Integer mathAnswer = (Integer) httpSession.getAttribute(BLOJSOM_MATH_AUTHENTICATOR_PLUGIN_ANSWER);

                    try {
                        int mathAnswerCheckValue = Integer.parseInt(mathAnswerCheck);
                        int originalMathAnswerValue = mathAnswer.intValue();

                        if (mathAnswerCheckValue == originalMathAnswerValue) {
                            passedCheck = true;
                        }
                    } catch (NumberFormatException e) {
                    }
                }

                Map commentMetaData;
                if (context.containsKey(CommentPlugin.BLOJSOM_PLUGIN_COMMENT_METADATA)) {
                    commentMetaData = (Map) context.get(CommentPlugin.BLOJSOM_PLUGIN_COMMENT_METADATA);
                } else {
                    commentMetaData = new HashMap();
                }

                if (!passedCheck) {
                    commentMetaData.put(CommentPlugin.BLOJSOM_PLUGIN_COMMENT_METADATA_DESTROY, Boolean.TRUE);
                    httpServletRequest.setAttribute(BLOJSOM_MATH_AUTHENTICATOR_PLUGIN_STATUS_MESSAGE, "Failed math comment authentication check.");
                } else {
                    commentMetaData.put(CommentModerationPlugin.BLOJSOM_COMMENT_MODERATION_PLUGIN_APPROVED, Boolean.TRUE.toString());
                    httpServletRequest.setAttribute(BLOJSOM_MATH_AUTHENTICATOR_PLUGIN_STATUS_MESSAGE, "Passed math comment authentication check.");
                }

                context.put(CommentPlugin.BLOJSOM_PLUGIN_COMMENT_METADATA, commentMetaData);
            }
        }
    }

    /**
     * Return the result from the specified operation where 0 = addition, 1 = subtraction, 2 = multiplication
     *
     * @param value1 Value 1
     * @param value2 Value 2
     * @param operation Operation where 0 = addition, 1 = subtraction, 2 = multiplication
     * @return Value of operation
     */
    protected int getAnswerForOperation(int value1, int value2, int operation) {
        int answer;

        switch (operation) {
            case 0:
                {
                    answer = value1 + value2;
                    break;
                }
            case 1:
                {
                    answer = value1 - value2;
                    break;
                }
            case 2:
                {
                    answer = value1 * value2;
                    break;
                }
            default:
                {
                    answer = value1 + value2;
                }
        }

        return answer;
    }

    /**
     * Return the appropriate operator for the operation
     *
     * @param operation Operation where 0 = addition, 1 = subtraction, 2 = multiplication
     * @return + for addition, - for subtraction, and * for multiplication
     */
    protected String getOperatorForOperation(int operation) {
        switch (operation) {
            case 0:
                {
                    return "+";
                }
            case 1:
                {
                    return "-";
                }
            case 2:
                {
                    return "*";
                }
            default:
                {
                    return "+";
                }
        }
    }

    /**
     * Handle an event broadcast from another component
     *
     * @param event {@link Event} to be handled
     */
    public void handleEvent(Event event) {
    }

    /**
     * Process an event from another component
     *
     * @param event {@link Event} to be handled
     */
    public void processEvent(Event event) {
        if (event instanceof CommentResponseSubmissionEvent) {
            CommentResponseSubmissionEvent commentResponseSubmissionEvent = (CommentResponseSubmissionEvent) event;

            try {
                HashMap context = new HashMap();
                moderateComment(commentResponseSubmissionEvent.getHttpServletRequest(),
                        commentResponseSubmissionEvent.getHttpServletResponse(), commentResponseSubmissionEvent.getBlog(),
                        context,
                        new Entry[] {commentResponseSubmissionEvent.getEntry()});

                // Grab the comment metadata and populate the metadata for the current submission
                Map operationMetadata = (Map) context.get(CommentPlugin.BLOJSOM_PLUGIN_COMMENT_METADATA);
                Map commentMetadata = commentResponseSubmissionEvent.getMetaData();

                Iterator keys = operationMetadata.keySet().iterator();
                while (keys.hasNext()) {
                    Object key = keys.next();
                    commentMetadata.put(key.toString(), operationMetadata.get(key).toString());
                }
            } catch (PluginException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            }
        }
    }
}