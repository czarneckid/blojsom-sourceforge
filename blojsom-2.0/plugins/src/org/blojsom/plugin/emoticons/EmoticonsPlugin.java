/**
 * Copyright (c) 2003, David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003 by Mark Lussier
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
package org.blojsom.plugin.emoticons;

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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * EmoticonsPlugin
 *
 * @author David Czarnecki
 * @version $Id: EmoticonsPlugin.java,v 1.2 2003-08-22 04:40:32 czarneckid Exp $
 */
public class EmoticonsPlugin implements BlojsomPlugin {

    private static final String EMOTICONS_CONFIGURATION_IP = "plugin-emoticons";

    private static final String HAPPY = ":)";
    private static final String HAPPY_PARAM = "happy";

    private static final String SAD = ":(";
    private static final String SAD_PARAM = "sad";

    private static final String GRIN = ":D";
    private static final String GRIN_PARAM = "grin";

    private static final String LOVE = "<3";
    private static final String LOVE_PARAM = "love";

    private static final String MISCHIEF = ";7)";
    private static final String MISCHIEF_PARAM = "mischief";

    private static final String COOL = "])";
    private static final String COOL_PARAM = "cool";

    private static final String DEVIL = "})";
    private static final String DEVIL_PARAM = "devil";

    private static final String SILLY = ":P";
    private static final String SILLY_PARAM = "silly";

    private static final String ANGRY = ">(";
    private static final String ANGRY_PARAM = "angry";

    private static final String LAUGH = "(D";
    private static final String LAUGH_PARAM = "laugh";

    private static final String WINK = ";)";
    private static final String WINK_PARAM = "wink";

    private static final String BLUSH = "*^_^*";
    private static final String BLUSH_PARAM = "blush";

    private static final String CRY = ":'(";
    private static final String CRY_PARAM = "cry";

    private static final String CONFUSED = "`:|";
    private static final String CONFUSED_PARAM = "confused";

    private static final String SHOCKED = ":O";
    private static final String SHOCKED_PARAM = "shocked";

    private static final String PLAIN = ":|";
    private static final String PLAIN_PARAM = "plain";

    private static final String IMG_OPEN = "<img src=\"";
    private static final String IMG_CLOSE = "\"";
    private static final String IMG_ALT_START = " alt=\"";
    private static final String IMG_ALT_END = "\" />";

    private Log _logger = LogFactory.getLog(EmoticonsPlugin.class);

    private Map _emoticonsMap;

    /**
     * Default constructor
     */
    public EmoticonsPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration {@link org.blojsom.blog.BlojsomConfiguration} information
     * @throws org.blojsom.plugin.BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws org.blojsom.plugin.BlojsomPluginException {
        String emoticonsConfiguration = servletConfig.getInitParameter(EMOTICONS_CONFIGURATION_IP);
        if (emoticonsConfiguration == null || "".equals(emoticonsConfiguration)) {
            throw new BlojsomPluginException("No value given for: " + EMOTICONS_CONFIGURATION_IP + " configuration parameter");
        }

        String[] users = blojsomConfiguration.getBlojsomUsers();
        _emoticonsMap = new HashMap(users.length);
        for (int i = 0; i < users.length; i++) {
            String user = users[i];
            Properties emoticonsProperties = new Properties();
            String configurationFile = blojsomConfiguration.getBaseConfigurationDirectory() + user + '/' + emoticonsConfiguration;
            InputStream is = servletConfig.getServletContext().getResourceAsStream(configurationFile);
            if (is == null) {
                _logger.info("No emoticons configuration file found: " + configurationFile);
            } else {
                try {
                    emoticonsProperties.load(is);
                    is.close();
                    _emoticonsMap.put(user, BlojsomUtils.propertiesToMap(emoticonsProperties));
                } catch (IOException e) {
                    _logger.error(e);
                    throw new BlojsomPluginException(e);
                }
            }
        }
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @param user {@link org.blojsom.blog.BlogUser} instance
     * @param context Context
     * @param entries Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws org.blojsom.plugin.BlojsomPluginException If there is an error processing the blog entries
     */
    public org.blojsom.blog.BlogEntry[] process(HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse,
                               BlogUser user,
                               Map context,
                               org.blojsom.blog.BlogEntry[] entries) throws org.blojsom.plugin.BlojsomPluginException {
        if (!_emoticonsMap.containsKey(user.getId())) {
            return entries;
        }

        String blogBaseUrl = user.getBlog().getBlogBaseURL();
        Map emoticonsForUser = (Map) _emoticonsMap.get(user.getId());
        for (int i = 0; i < entries.length; i++) {
            BlogEntry entry = entries[i];
            String updatedDescription = entry.getDescription();
            updatedDescription = replaceEmoticon(emoticonsForUser, updatedDescription, HAPPY, HAPPY_PARAM, blogBaseUrl);
            updatedDescription = replaceEmoticon(emoticonsForUser, updatedDescription, SAD, SAD_PARAM, blogBaseUrl);
            updatedDescription = replaceEmoticon(emoticonsForUser, updatedDescription, GRIN, GRIN_PARAM, blogBaseUrl);
            updatedDescription = replaceEmoticon(emoticonsForUser, updatedDescription, LOVE, LOVE_PARAM, blogBaseUrl);
            updatedDescription = replaceEmoticon(emoticonsForUser, updatedDescription, MISCHIEF, MISCHIEF_PARAM, blogBaseUrl);
            updatedDescription = replaceEmoticon(emoticonsForUser, updatedDescription, COOL, COOL_PARAM, blogBaseUrl);
            updatedDescription = replaceEmoticon(emoticonsForUser, updatedDescription, DEVIL, DEVIL_PARAM, blogBaseUrl);
            updatedDescription = replaceEmoticon(emoticonsForUser, updatedDescription, SILLY, SILLY_PARAM, blogBaseUrl);
            updatedDescription = replaceEmoticon(emoticonsForUser, updatedDescription, ANGRY, ANGRY_PARAM, blogBaseUrl);
            updatedDescription = replaceEmoticon(emoticonsForUser, updatedDescription, LAUGH, LAUGH_PARAM, blogBaseUrl);
            updatedDescription = replaceEmoticon(emoticonsForUser, updatedDescription, WINK, WINK_PARAM, blogBaseUrl);
            updatedDescription = replaceEmoticon(emoticonsForUser, updatedDescription, BLUSH, BLUSH_PARAM, blogBaseUrl);
            updatedDescription = replaceEmoticon(emoticonsForUser, updatedDescription, CRY, CRY_PARAM, blogBaseUrl);
            updatedDescription = replaceEmoticon(emoticonsForUser, updatedDescription, CONFUSED, CONFUSED_PARAM, blogBaseUrl);
            updatedDescription = replaceEmoticon(emoticonsForUser, updatedDescription, SHOCKED, SHOCKED_PARAM, blogBaseUrl);
            updatedDescription = replaceEmoticon(emoticonsForUser, updatedDescription, PLAIN, PLAIN_PARAM, blogBaseUrl);
            entry.setDescription(updatedDescription);
        }
        return entries;
    }

    /**
     * Replace the references in the description with the URL to the image for the emoticon
     *
     * @param emoticonString Description string
     * @param emoticon Emoticon characters
     * @param emoticonParam Emoticon property name
     * @param url Base URL for the blog
     * @return Updated description with emoticon references replaced with URLs to the images for the emoticons
     */
    private String replaceEmoticon(Map emoticonsForUser, String emoticonString, String emoticon, String emoticonParam, String url) {
        String emoticonImage;
        emoticonImage = (String) emoticonsForUser.get(emoticonParam);
        if (emoticonImage != null && !"".equals(emoticonImage)) {
            StringBuffer imageReference = new StringBuffer(IMG_OPEN);
            imageReference.append(url);
            imageReference.append(emoticonImage);
            imageReference.append(IMG_CLOSE);
            imageReference.append(IMG_ALT_START);
            imageReference.append(emoticonImage);
            imageReference.append(IMG_ALT_END);
            return BlojsomUtils.replace(emoticonString, emoticon, imageReference.toString());
        }

        return emoticonString;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws org.blojsom.plugin.BlojsomPluginException If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws BlojsomPluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws org.blojsom.plugin.BlojsomPluginException If there is an error in finalizing this plugin
     */
    public void destroy() throws BlojsomPluginException {
    }
}
