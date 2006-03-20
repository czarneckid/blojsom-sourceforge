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
package org.blojsom.plugin.nofollow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Comment;
import org.blojsom.blog.Entry;
import org.blojsom.blog.Trackback;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NoFollow plugin adds support for the rel="nofollow" attribute on links added
 * to comments and trackbacks.
 *
 * @author David Czarnecki
 * @version $Id: NoFollowPlugin.java,v 1.2 2006-03-20 22:50:50 czarneckid Exp $
 * @since blojsom 3.0
 */
public class NoFollowPlugin implements Plugin {

    private Log _logger = LogFactory.getLog(NoFollowPlugin.class);

    private static final String HYPERLINK_REGEX = "<a\\s([^>]*\\s*href\\s*=[^>]*)>";
    private static final String ATTRIBUTE_REGEX = "[^=[\\p{Space}]]*\\s*=\\s*\"[^\"]*\"|[^=[\\p{Space}]]*\\s*=\\s*'[^']*'|[^=[\\p{Space}]]*\\s*=[^[\\p{Space}]]*";
    private static final String NOFOLLOW_REGEX = "\\s*nofollow\\s*";
    private static final String REL_ATTR_REGEX = "rel\\s*=";
    private static final String REL_NOFOLLOW = " rel=\"nofollow\"";

    /**
     * Construct a new instance of the NoFollow plugin
     */
    public NoFollowPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error initializing the plugin
     */
    public void init() throws PluginException {
    }

    /**
     * Take a string and add rel="nofollow" attributes to the &lt;a href/&gt; links
     * if they are not already on the links.
     *
     * @param text Text to look for hyperlinks
     * @return Text with rel="nofollow" attributes added to the hyperlinks
     */
    protected String noFollowFy(String text) {
        if (BlojsomUtils.checkNullOrBlank(text)) {
            return text;
        }
        
        StringBuffer updatedText = new StringBuffer();

        Pattern hyperlinkPattern = Pattern.compile(HYPERLINK_REGEX, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNICODE_CASE | Pattern.DOTALL);
        Matcher hyperlinkMatcher = hyperlinkPattern.matcher(text);

        Pattern attributePattern = Pattern.compile(ATTRIBUTE_REGEX, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNICODE_CASE | Pattern.DOTALL);
        Pattern relAttrPattern = Pattern.compile(REL_ATTR_REGEX, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNICODE_CASE | Pattern.DOTALL);
        Pattern noFollow = Pattern.compile(NOFOLLOW_REGEX, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNICODE_CASE | Pattern.DOTALL);

        Matcher noFollowMatcher;
        Matcher attributeMatcher;

        int lastIndex = 0;
        while (hyperlinkMatcher.find()) {
            updatedText.append(text.substring(lastIndex, hyperlinkMatcher.start()));
            String link = hyperlinkMatcher.group();
            attributeMatcher = attributePattern.matcher(link);

            StringBuffer updatedLink = new StringBuffer();
            boolean shouldAddAttr = true;

            while (attributeMatcher.find()) {
                String attr = attributeMatcher.group();

                Matcher relAttrMatcher = relAttrPattern.matcher(attr);
                while (relAttrMatcher.find()) {
                    noFollowMatcher = noFollow.matcher(attr);
                    if (!noFollowMatcher.matches()) {
                        int indexOfQuote = attr.lastIndexOf("\"");
                        if (indexOfQuote != -1) {
                            attr = attr.substring(0, indexOfQuote) + " nofollow\"";
                            shouldAddAttr = false;
                        }
                    }
                }

                updatedLink.append(attr);
            }

            if (shouldAddAttr) {
                updatedLink.append(REL_NOFOLLOW);
            }

            updatedLink.append((">"));
            updatedText.append(updatedLink);
            lastIndex = hyperlinkMatcher.end();
        }

        updatedText.append(text.substring(lastIndex));

        return updatedText.toString();
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
        for (int i = 0; i < entries.length; i++) {
            Entry entry = entries[i];

            Iterator commentsIterator = entry.getComments().iterator();
            String commentText;
            while (commentsIterator.hasNext()) {
                Comment blogComment = (Comment) commentsIterator.next();

                commentText = blogComment.getComment();
                commentText = noFollowFy(commentText);
                blogComment.setComment(commentText);
            }

            Iterator trackbacksIterator = entry.getTrackbacks().iterator();
            String trackbackText;
            while (trackbacksIterator.hasNext()) {
                Trackback trackback = (Trackback) trackbacksIterator.next();

                trackbackText = trackback.getExcerpt();
                trackbackText = noFollowFy(trackbackText);
                trackback.setExcerpt(trackbackText);
            }
        }

        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws PluginException If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws PluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws PluginException If there is an error in finalizing this plugin
     */
    public void destroy() throws PluginException {
    }
}