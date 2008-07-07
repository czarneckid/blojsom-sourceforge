/**
 * Copyright (c) 2003-2008, David A. Czarnecki
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
package org.blojsom.plugin.footnote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Footnote Expansion Plugin
 *
 * @author Mark Lussier
 * @author David Czarnecki
 * @version $Id: FootnotePlugin.java,v 1.5 2008-07-07 19:54:32 czarneckid Exp $
 * @since blojsom 3.0
 */
public class FootnotePlugin implements Plugin {

    private static final String FOOTNOTE_METADATA = "footnote";
    private static final String REGEX_FOOTNOTE = "\\[(\\d+)\\]";
    private static final String FOOTNOTE_LINKAGE_FORMAT = "[{0}] {1}";
    private static final String FOOTNOTES_PROCESSED_METADATA = "footnotes-processed";

    private Log _logger = LogFactory.getLog(FootnotePlugin.class);

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error initializing the plugin
     */
    public void init() throws PluginException {
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
        Pattern footnotePattern = Pattern.compile(REGEX_FOOTNOTE);

        for (int i = 0; i < entries.length; i++) {
            Entry entry = entries[i];
            String content = entry.getDescription();
            Matcher matcher = footnotePattern.matcher(content);
            StringBuffer modifiedContent = new StringBuffer();
            Map footnotes = new TreeMap(new FootnotePlugin.FootnoteComparator());

            while (matcher.find()) {
                int footnoteIndex;

                try {
                    footnoteIndex = Integer.parseInt(matcher.group(1));
                    if (BlojsomUtils.checkMapForKey(entry.getMetaData(), FOOTNOTE_METADATA + "-" + footnoteIndex)) {
                        footnotes.put(Integer.toString(footnoteIndex), entry.getMetaData().get(FOOTNOTE_METADATA + "-" + footnoteIndex));
                    }

                    matcher.appendReplacement(modifiedContent, "<sup id=\"footnoteref-" + footnoteIndex + "\"><a href=\"#footnote-" + footnoteIndex + "\">" + footnoteIndex + "</a></sup>");
                } catch (NumberFormatException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error("Footnote index in post is not a valid integer [" + matcher.group(1) + "]");
                    }
                }
            }

            matcher.appendTail(modifiedContent);

            if (!footnotes.isEmpty() && !BlojsomUtils.checkMapForKey(entry.getMetaData(), FOOTNOTES_PROCESSED_METADATA)) {
                modifiedContent.append("<br/><br/>");
                modifiedContent.append("<div class=\"footnote\">");
                modifiedContent.append("<hr/>");
                modifiedContent.append("<ol>");
                Iterator footnotesIterator = footnotes.keySet().iterator();

                while (footnotesIterator.hasNext()) {
                    String footnoteIndex = (String) footnotesIterator.next();
                    modifiedContent.append("<li id=\"footnote-").append(footnoteIndex).append("\"><p>");
                    modifiedContent.append(MessageFormat.format(FOOTNOTE_LINKAGE_FORMAT, new Object[]{footnoteIndex, entry.getMetaData().get(FOOTNOTE_METADATA + "-" + footnoteIndex)}));
                    modifiedContent.append("<a href=\"#footnoteref-").append(footnoteIndex).append("\" class=\"footnoteBackLink\" title=\"Jump back to footnote ").append(footnoteIndex).append(" in the text.\">");
                    modifiedContent.append("&#8617;</a></p></li>");
                }

                modifiedContent.append("</ol>");
                modifiedContent.append("</div>");

                entry.getMetaData().put(FOOTNOTES_PROCESSED_METADATA, "true");
            }

            entry.setDescription(modifiedContent.toString());
        }

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
     * Provides a Comparator for numerically sorting footnotes
     */
    private class FootnoteComparator implements Comparator {

        /**
         * Standard implementation of the compare method for Integer keys
         *
         * @param element1 first Object to be compared
         * @param element2 second Object to be compared
         * @return a negative integer, zero, or a positive integer as the first
         *         argument is less than, equal to, or greater than the second.
         */
        public int compare(Object element1, Object element2) {
            Integer next = Integer.decode((String) element1);
            Integer previous = Integer.decode((String) element2);

            return next.compareTo(previous);
        }
    }
}
