/**
 * Copyright (c) 2003-2005, David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003-2005 by Mark Lussier
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
package org.blojsom.plugin.footnote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;
import org.blojsom.blog.BlojsomConfiguration;
import org.blojsom.plugin.BlojsomPlugin;
import org.blojsom.plugin.BlojsomPluginException;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Footnote Expansion Plugin
 *
 * @author Mark Lussier
 * @author David Czarnecki
 * @version $Id: FootnotePlugin.java,v 1.4 2005-07-27 02:26:10 czarneckid Exp $
 */
public class FootnotePlugin implements BlojsomPlugin {

    private static final String FOOTNOTE_METADATA = "footnote";
    private static final String REGEX_FOOTNOTE = "\\[(\\d+)\\]";
    private static final String FOOTNOTE_LINKAGE_FORMAT = "[{0}] {1}";

    private Log _logger = LogFactory.getLog(FootnotePlugin.class);

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig        Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration {@link org.blojsom.blog.BlojsomConfiguration} information
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws BlojsomPluginException {
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
        Pattern footnotePattern = Pattern.compile(REGEX_FOOTNOTE);

        for (int i = 0; i < entries.length; i++) {
            BlogEntry entry = entries[i];
            String content = entry.getDescription();
            Matcher matcher = footnotePattern.matcher(content);
            StringBuffer modifiedContent = new StringBuffer();
            Map footnotes = new TreeMap();

            while (matcher.find()) {
                int footnoteIndex = -1;

                try {
                    footnoteIndex = Integer.parseInt(matcher.group(1));
                    if (BlojsomUtils.checkMapForKey(entry.getMetaData(), FOOTNOTE_METADATA + "-" + footnoteIndex)) {
                        footnotes.put(Integer.toString(footnoteIndex), entry.getMetaData().get(FOOTNOTE_METADATA + "-" + footnoteIndex));
                    }

                    matcher.appendReplacement(modifiedContent, "<sup id=\"footnoteref-" + footnoteIndex + "\"><a href=\"#footnote-" + footnoteIndex + "\">" + footnoteIndex + "</a></sup>");
                } catch (NumberFormatException e) {
                    _logger.error("Footnote index in post is not a valid integer [" + matcher.group(1) + "]");
                }
            }

            matcher.appendTail(modifiedContent);

            if (!footnotes.isEmpty()) {
                modifiedContent.append("<br/><br/>");
                modifiedContent.append("<div class=\"footnote\">");
                modifiedContent.append("<hr/>");
                modifiedContent.append("<ol>");
                Iterator footnotesIterator = footnotes.keySet().iterator();
                while (footnotesIterator.hasNext()) {
                    String footnoteIndex = (String) footnotesIterator.next();
                    modifiedContent.append("<li id=\"footnote-" + footnoteIndex + "\"><p>");
                    modifiedContent.append(MessageFormat.format(FOOTNOTE_LINKAGE_FORMAT, new Object[] {footnoteIndex, entry.getMetaData().get(FOOTNOTE_METADATA + "-" + footnoteIndex)}));
                    modifiedContent.append("<a href=\"#footnoteref-" + footnoteIndex + "\" class=\"footnoteBackLink\" title=\"Jump back to footnote " + footnoteIndex + " in the text.\">");
                    modifiedContent.append("&#8617;</a></p></li>");
                }
                modifiedContent.append("</ol>");
                modifiedContent.append("</div>");
            }

            entry.setDescription(modifiedContent.toString());
        }

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

}
