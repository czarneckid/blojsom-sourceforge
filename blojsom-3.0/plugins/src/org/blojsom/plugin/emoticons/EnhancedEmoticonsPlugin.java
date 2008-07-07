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
package org.blojsom.plugin.emoticons;

import org.blojsom.blog.Blog;
import org.blojsom.blog.Comment;
import org.blojsom.blog.Entry;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Enhanced Emoticons Plugin. This is slightly modified version of the built-in
 * Emoticon Plugin. It just adds the ability to configure the available
 * emoticons and the patterns they correspond to via the emoticons properties
 * file.
 *
 * @author David Czarnecki
 * @author Jan Wessely
 * @version $Id: EnhancedEmoticonsPlugin.java,v 1.7 2008-07-07 19:54:23 czarneckid Exp $
 * @since blojsom 3.0
 */
public class EnhancedEmoticonsPlugin implements Plugin {

    private static final String BLOJSOM_PLUGIN_METADATA_EMOTICONS_DISABLED = "emoticons-disabled";

    private static final String EMOTICONS_PARAM = "emoticons";
    private static final String EMOTICONS_PATTERN_POSTFIX = ".pattern";
    private static final String IMG_OPEN = "<img src=\"";
    private static final String IMG_CLOSE = "\"";
    private static final String IMG_ALT_START = " alt=\"";
    private static final String IMG_ALT_END = "\" />";
    private static final String EMOTICONS_CLASS = " class=\"emoticons\" ";

    private Map _emoticons;
    private boolean _parseComments = false;

    /**
     * Default constructor
     */
    public EnhancedEmoticonsPlugin() {
    }

    /**
     * Set the emoticons configuration data
     *
     * @param emoticons Emoticons configuration data
     */
    public void setEmoticons(Map emoticons) {
        _emoticons = emoticons;
    }

    /**
     * Set whether or not to parse comments for emoticons
     *
     * @param parseComments Parse comments
     */
    public void setParseComments(boolean parseComments) {
        _parseComments = parseComments;
    }

    /**
     * Initialize this plugin. This method only called when the plugin is
     * instantiated.
     *
     * @throws PluginException If there is an error initializing the plugin
     */
    public void init() throws PluginException {
    }

    /**
     * Read the list of available Emoticons and return it as a
     * {@link java.util.List}.
     *
     * @param emoticons list of available plugins from the emoticons properties file.
     * @return {@link java.util.List} of available emoticons.
     */
    private List parseEmoticons(String emoticons) {
        List list = new ArrayList();
        StringTokenizer tok = new StringTokenizer(emoticons, "\t\n\r\f,; ");

        while (tok.hasMoreTokens()) {
            list.add(tok.nextToken());
        }

        return list;
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param blog                {@link Blog} instance
     * @param context             Contextblojsom
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws PluginException If there is an error processing the blog entries
     */
    public org.blojsom.blog.Entry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                            Blog blog, Map context, org.blojsom.blog.Entry[] entries) throws PluginException {
        if (_emoticons == null) {
            return entries;
        }

        List availableEmoticons = parseEmoticons((String) _emoticons.get(EMOTICONS_PARAM));
        String blogBaseUrl = blog.getBlogBaseURL();

        for (int i = 0; i < entries.length; i++) {
            Entry entry = entries[i];

            if (!BlojsomUtils.checkMapForKey(entry.getMetaData(), BLOJSOM_PLUGIN_METADATA_EMOTICONS_DISABLED)) {
                String updatedDescription = entry.getDescription();
                Iterator iter = availableEmoticons.iterator();

                while (iter.hasNext()) {
                    String emoticon = (String) iter.next();
                    updatedDescription = replaceEmoticon(updatedDescription, emoticon, blogBaseUrl);
                }

                entry.setDescription(updatedDescription);

                if (_parseComments) {
                    Comment comments[] = entry.getCommentsAsArray();
                    for (int j = 0; j < comments.length; j++) {
                        Comment comment = comments[j];

                        String updatedCommentText = comment.getComment();
                        iter = availableEmoticons.iterator();

                        while (iter.hasNext()) {
                            String emoticon = (String) iter.next();
                            updatedCommentText = replaceEmoticon(updatedCommentText, emoticon, blogBaseUrl);
                        }

                        comment.setComment(updatedCommentText);
                    }
                }
            }
        }

        return entries;
    }

    /**
     * Replace the references in the description with the URL to the image for
     * the emoticon
     *
     * @param emoticonString Description string
     * @param emoticon       Emoticon name
     * @param url            Base URL for the blog
     * @return Updated description with emoticon references replaced with URLs
     *         to the images for the emoticons
     */
    private String replaceEmoticon(String emoticonString, String emoticon, String url) {
        String emoticonImage, emoticonPattern;
        emoticonImage = (String) _emoticons.get(emoticon);
        emoticonPattern = (String) _emoticons.get(emoticon + EMOTICONS_PATTERN_POSTFIX);

        if (!BlojsomUtils.checkNullOrBlank(emoticonImage)) {
            StringBuffer imageReference = new StringBuffer(IMG_OPEN);
            imageReference.append(url);
            imageReference.append(emoticonImage);
            imageReference.append(IMG_CLOSE);
            imageReference.append(EMOTICONS_CLASS);
            imageReference.append(IMG_ALT_START);
            imageReference.append(emoticonImage);
            imageReference.append(IMG_ALT_END);

            return BlojsomUtils.replace(emoticonString, emoticonPattern, imageReference.toString());
        }

        return emoticonString;
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