/**
 * Copyright (c) 2003-2005, David A. Czarnecki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the "David A. Czarnecki" and "blojsom" nor the names of
 * its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Products derived from this software may not be called "blojsom",
 * nor may "blojsom" appear in their name, without prior written permission of
 * David A. Czarnecki.
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
import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;
import org.blojsom.blog.BlojsomConfiguration;
import org.blojsom.event.BlojsomEvent;
import org.blojsom.event.BlojsomListener;
import org.blojsom.plugin.BlojsomPlugin;
import org.blojsom.plugin.BlojsomPluginException;
import org.blojsom.plugin.comment.event.CommentResponseSubmissionEvent;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XSSFilterPlugin
 *
 * @author David Czarnecki
 * @version $Id: XSSFilterPlugin.java,v 1.2 2005-12-06 12:51:57 czarneckid Exp $
 * @since blojsom 2.27
 */
public class XSSFilterPlugin implements BlojsomPlugin, BlojsomListener {

    private Log _logger = LogFactory.getLog(XSSFilterPlugin.class);

    // Default set of balanced and unbalanced tags
    private static final String [] DEFAULT_ALLOWED_BALANCED_TAGS = {"b", "i", "blockquote", "pre", "ul", "li", "ol"};
    private static final String [] DEFAULT_ALLOWED_UNBALANCED_TAGS = {"br"};

    // Initialization parameters
    private static final String XSS_FILTER_ALLOWED_BALANCED_TAGS_IP = "plugin-xss-filter-allowed-balanced-tags";
    private static final String XSS_FILTER_ALLOWED_UNBALANCED_TAGS_IP = "plugin-xss-filter-allowed-unbalanced-tags";
    private static final String XSS_FILTER_ALLOW_LINKS_IP = "plugin-xss-filter-allow-links";

    // Context variables
    private static final String XSS_FILTER_ALLOWED_BALANCED_TAGS = "XSS_FILTER_ALLOWED_BALANCED_TAGS";
    private static final String XSS_FILTER_ALLOWED_UNBALANCED_TAGS = "XSS_FILTER_ALLOWED_UNBALANCED_TAGS";
    private static final String XSS_FILTER_ALLOW_LINKS = "XSS_FILTER_ALLOW_LINKS";

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

        _logger.debug("Initialized XSS Filter plugin");
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
        // Get the individual blog's initialization parameters
        Blog blog = user.getBlog();

        String allowedBalancedTagsIP = blog.getBlogProperty(XSS_FILTER_ALLOWED_BALANCED_TAGS_IP);
        String[] allowedBalancedTags = DEFAULT_ALLOWED_BALANCED_TAGS;
        if (!BlojsomUtils.checkNullOrBlank(allowedBalancedTagsIP)) {
            allowedBalancedTags = BlojsomUtils.parseCommaList(allowedBalancedTagsIP);
        }
        context.put(XSS_FILTER_ALLOWED_BALANCED_TAGS, allowedBalancedTags);

        String allowedUnbalancedTagsIP = blog.getBlogProperty(XSS_FILTER_ALLOWED_UNBALANCED_TAGS_IP);
        String[] allowedUnbalancedTags = DEFAULT_ALLOWED_UNBALANCED_TAGS;
        if (!BlojsomUtils.checkNullOrBlank(allowedUnbalancedTagsIP)) {
            allowedUnbalancedTags = BlojsomUtils.parseCommaList(allowedUnbalancedTagsIP);
        }
        context.put(XSS_FILTER_ALLOWED_UNBALANCED_TAGS, allowedUnbalancedTags);

        String allowLinksIP = blog.getBlogProperty(XSS_FILTER_ALLOW_LINKS_IP);
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
     * @since blojsom 2.24
     */
    public void processEvent(BlojsomEvent event) {
        if (event instanceof CommentResponseSubmissionEvent) {
            CommentResponseSubmissionEvent commentEvent = (CommentResponseSubmissionEvent) event;

            // Get the individual blog's initialization parameters
            Blog blog = commentEvent.getBlog().getBlog();
            String allowedBalancedTagsIP = blog.getBlogProperty(XSS_FILTER_ALLOWED_BALANCED_TAGS_IP);
            String[] allowedBalancedTags = DEFAULT_ALLOWED_BALANCED_TAGS;
            if (!BlojsomUtils.checkNullOrBlank(allowedBalancedTagsIP)) {
                allowedBalancedTags = BlojsomUtils.parseCommaList(allowedBalancedTagsIP);
            }

            String allowedUnbalancedTagsIP = blog.getBlogProperty(XSS_FILTER_ALLOWED_UNBALANCED_TAGS_IP);
            String[] allowedUnbalancedTags = DEFAULT_ALLOWED_UNBALANCED_TAGS;
            if (!BlojsomUtils.checkNullOrBlank(allowedUnbalancedTagsIP)) {
                allowedUnbalancedTags = BlojsomUtils.parseCommaList(allowedUnbalancedTagsIP);
            }

            String allowLinksIP = blog.getBlogProperty(XSS_FILTER_ALLOW_LINKS_IP);
            boolean allowLinks = true;
            if (!BlojsomUtils.checkNullOrBlank(allowLinksIP)) {
                allowLinks = Boolean.valueOf(allowLinksIP).booleanValue();
            }

            String commentText = commentEvent.getContent();
            commentText = BlojsomUtils.escapeStringSimple(commentText);

            if (commentText != null) {
                // Process balanced tags
                for (int i = 0; i < allowedBalancedTags.length; i++) {
                    String allowedBalancedTag = allowedBalancedTags[i];

                    commentText = replaceBalancedTag(commentText, allowedBalancedTag);
                }

                // Process unbalanced tags
                for (int i = 0; i < allowedUnbalancedTags.length; i++) {
                    String allowedUnbalancedTag = allowedUnbalancedTags[i];

                    commentText = replaceUnbalancedTag(commentText, allowedUnbalancedTag);
                }

                // Process links
                if (allowLinks) {
                    commentText = processLinks(commentText);
                }

                // Escaped brackets
                commentText = commentText.replaceAll("&amp;lt;", "&lt;");
                commentText = commentText.replaceAll("&amp;gt;", "&gt;");
                commentText = commentText.replaceAll("&amp;#", "&#");

                // Save the comment text
                commentEvent.setContent(commentText);
            }
        }
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
}