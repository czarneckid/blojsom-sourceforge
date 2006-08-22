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
package org.blojsom.plugin.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.event.Event;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.event.Listener;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.event.ProcessEntryEvent;
import org.blojsom.plugin.comment.event.CommentResponseSubmissionEvent;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XSSFilterPlugin
 *
 * @author David Czarnecki
 * @version $Id: XSSFilterPlugin.java,v 1.3 2006-08-22 18:54:31 czarneckid Exp $
 * @since blojsom 3.0
 */
public class XSSFilterPlugin implements Plugin, Listener {

    private Log _logger = LogFactory.getLog(XSSFilterPlugin.class);

    // Default set of balanced and unbalanced tags
    private static final String [] DEFAULT_ALLOWED_BALANCED_TAGS = {"b", "strong", "i", "em", "u", "s", "blockquote", "pre", "ul", "li", "ol"};
    private static final String [] DEFAULT_ALLOWED_UNBALANCED_TAGS = {"br", "img"};

    // Initialization parameters
    private static final String XSS_FILTER_ALLOWED_BALANCED_TAGS_IP = "plugin-xss-filter-allowed-balanced-tags";
    private static final String XSS_FILTER_ALLOWED_UNBALANCED_TAGS_IP = "plugin-xss-filter-allowed-unbalanced-tags";
    private static final String XSS_FILTER_ALLOW_LINKS_IP = "plugin-xss-filter-allow-links";
    private static final String XSS_FILTER_PROCESS_ENTRIES_IP = "plugin-xss-filter-process-entries";

    // Context variables
    private static final String XSS_FILTER_ALLOWED_BALANCED_TAGS = "XSS_FILTER_ALLOWED_BALANCED_TAGS";
    private static final String XSS_FILTER_ALLOWED_UNBALANCED_TAGS = "XSS_FILTER_ALLOWED_UNBALANCED_TAGS";
    private static final String XSS_FILTER_ALLOW_LINKS = "XSS_FILTER_ALLOW_LINKS";

    private EventBroadcaster _eventBroadcaster;

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
        // Get the individual blog's initialization parameters
        String allowedBalancedTagsIP = blog.getProperty(XSS_FILTER_ALLOWED_BALANCED_TAGS_IP);
        String[] allowedBalancedTags = DEFAULT_ALLOWED_BALANCED_TAGS;
        if (!BlojsomUtils.checkNullOrBlank(allowedBalancedTagsIP)) {
            allowedBalancedTags = BlojsomUtils.parseCommaList(allowedBalancedTagsIP);
        }
        context.put(XSS_FILTER_ALLOWED_BALANCED_TAGS, allowedBalancedTags);

        String allowedUnbalancedTagsIP = blog.getProperty(XSS_FILTER_ALLOWED_UNBALANCED_TAGS_IP);
        String[] allowedUnbalancedTags = DEFAULT_ALLOWED_UNBALANCED_TAGS;
        if (!BlojsomUtils.checkNullOrBlank(allowedUnbalancedTagsIP)) {
            allowedUnbalancedTags = BlojsomUtils.parseCommaList(allowedUnbalancedTagsIP);
        }
        context.put(XSS_FILTER_ALLOWED_UNBALANCED_TAGS, allowedUnbalancedTags);

        String allowLinksIP = blog.getProperty(XSS_FILTER_ALLOW_LINKS_IP);
        Boolean allowLinks = Boolean.TRUE;
        if (!BlojsomUtils.checkNullOrBlank(allowLinksIP)) {
            allowLinks = Boolean.valueOf(allowLinksIP);
        }
        context.put(XSS_FILTER_ALLOW_LINKS, allowLinks);

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
        if (event instanceof CommentResponseSubmissionEvent) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("Processing comment response submission event");
            }
            CommentResponseSubmissionEvent commentEvent = (CommentResponseSubmissionEvent) event;

            String commentText = commentEvent.getContent();
            commentText = processContent(commentText, commentEvent.getBlog());

            // Save the processed comment text
            commentEvent.setContent(commentText);
        } else if (event instanceof ProcessEntryEvent) {
            ProcessEntryEvent entryEvent = (ProcessEntryEvent) event;
            Blog blog = entryEvent.getBlog();

            // Check to see if we should process entries through the XSS filter
            if (Boolean.valueOf(blog.getProperty(XSS_FILTER_PROCESS_ENTRIES_IP)).booleanValue()) {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Processing process blog entry event");
                }
                if (entryEvent.getEntry() != null) {
                    String entryText = entryEvent.getEntry().getDescription();
                    entryText = processContent(entryText, entryEvent.getBlog());

                    // Save the processed entry text
                    entryEvent.getEntry().setDescription(entryText);
                }
            }
        }
    }

    /**
     * Internal method to process any string content through the various routines
     *
     * @param content Content
     * @param blog    {@link Blog} information}
     * @return Processed content
     */
    protected String processContent(String content, Blog blog) {
        // Get the individual blog's initialization parameters
        String allowedBalancedTagsIP = blog.getProperty(XSS_FILTER_ALLOWED_BALANCED_TAGS_IP);
        String[] allowedBalancedTags = DEFAULT_ALLOWED_BALANCED_TAGS;
        if (!BlojsomUtils.checkNullOrBlank(allowedBalancedTagsIP)) {
            allowedBalancedTags = BlojsomUtils.parseCommaList(allowedBalancedTagsIP);
        }

        String allowedUnbalancedTagsIP = blog.getProperty(XSS_FILTER_ALLOWED_UNBALANCED_TAGS_IP);
        String[] allowedUnbalancedTags = DEFAULT_ALLOWED_UNBALANCED_TAGS;
        if (!BlojsomUtils.checkNullOrBlank(allowedUnbalancedTagsIP)) {
            allowedUnbalancedTags = BlojsomUtils.parseCommaList(allowedUnbalancedTagsIP);
        }

        String allowLinksIP = blog.getProperty(XSS_FILTER_ALLOW_LINKS_IP);
        boolean allowLinks = true;
        if (!BlojsomUtils.checkNullOrBlank(allowLinksIP)) {
            allowLinks = Boolean.valueOf(allowLinksIP).booleanValue();
        }

        content = BlojsomUtils.escapeStringSimple(content);

        if (content != null) {
            // Process balanced tags
            for (int i = 0; i < allowedBalancedTags.length; i++) {
                String allowedBalancedTag = allowedBalancedTags[i];

                content = replaceBalancedTag(content, allowedBalancedTag);
            }

            // Process unbalanced tags
            for (int i = 0; i < allowedUnbalancedTags.length; i++) {
                String allowedUnbalancedTag = allowedUnbalancedTags[i];

                content = replaceUnbalancedTag(content, allowedUnbalancedTag);
            }

            // Process links
            if (allowLinks) {
                content = processLinks(content);
            }

            content = processImgTags(content);

            // Escaped brackets
            content = content.replaceAll("&amp;lt;", "&lt;");
            content = content.replaceAll("&amp;gt;", "&gt;");
            content = content.replaceAll("&amp;#", "&#");
        }

        return content;
    }

    /**
     * Replace balanced tags
     *
     * @param input Input
     * @param tag   Tag
     * @return String where the &lt;<code>tag</code>&gt; and &lt;<code>/tag</code>&gt; have been replaced appropriately
     */
    private String replaceBalancedTag(String input, String tag) {
        Pattern openingPattern = Pattern.compile("&lt;" + tag + "&gt;", Pattern.CASE_INSENSITIVE);
        Pattern closingPattern = Pattern.compile("&lt;/" + tag + "&gt;", Pattern.CASE_INSENSITIVE);

        Matcher openingMatcher = openingPattern.matcher(input);
        input = openingMatcher.replaceAll("<" + tag + ">");

        Matcher closingMatcher = closingPattern.matcher(input);
        input = closingMatcher.replaceAll("</" + tag + ">");

        return input;
    }

    /**
     * Replace unbalanced tags
     *
     * @param input Input
     * @param tag   Tag
     * @return String where the &lt;<code>tag /</code>&gt; have been replaced appropriately
     */
    private String replaceUnbalancedTag(String input, String tag) {
        Pattern unbalancedPattern = Pattern.compile("&lt;" + tag + "\\s*/*&gt;", Pattern.CASE_INSENSITIVE);

        Matcher unbalancedMatcher = unbalancedPattern.matcher(input);
        input = unbalancedMatcher.replaceAll("<" + tag + " />");

        return input;
    }

    /**
     * Process &lt;a href .../&gt; links
     *
     * @param input Input
     * @return String where the &lt;a href .../&gt; links have been processed appropriately
     */
    private String processLinks(String input) {
        Pattern openingLinkPattern = Pattern.compile("&lt;a href=.*?&gt;", Pattern.CASE_INSENSITIVE);
        Pattern closingLinkPattern = Pattern.compile("&lt;/a&gt;", Pattern.CASE_INSENSITIVE);

        Matcher closingMatcher = closingLinkPattern.matcher(input);
        input = closingMatcher.replaceAll("</a>");

        Matcher openingMatcher = openingLinkPattern.matcher(input);
        while (openingMatcher.find()) {
            int start = openingMatcher.start();
            int end = openingMatcher.end();
            String link = input.substring(start, end);
            link = "<" + link.substring(4, link.length() - 4) + ">";
            input = input.substring(0, start) + link + input.substring(end, input.length());
            openingMatcher = openingLinkPattern.matcher(input);
        }

        return input;
    }

    /**
     * Process &lt;img ... /&gt; tags
     *
     * @param input Input
     * @return String where the &lt;img ... /&gt; links have been processed appropriately
     */
    private String processImgTags(String input) {
        Pattern imgPattern = Pattern.compile("(&lt;)(\\s*img\\s?.*?\\s*/*)(&gt;)", Pattern.CASE_INSENSITIVE);
        Matcher imgMatcher = imgPattern.matcher(input);

        // Replace all occurrences of pattern in input
        StringBuffer buffer = new StringBuffer();
        while (imgMatcher.find()) {
            // Insert replacement
            imgMatcher.appendReplacement(buffer, "<" + imgMatcher.group(2) + ">");
        }

        imgMatcher.appendTail(buffer);

        return buffer.toString();
    }
}