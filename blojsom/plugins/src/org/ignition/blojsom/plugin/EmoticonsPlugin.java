package org.ignition.blojsom.plugin;

import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * EmoticonsPlugin
 *
 * @author David Czarnecki
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
    private static final String IMG_CLOSE = "\" />";

    private Properties _emoticonsProperties;

    /**
     * Default constructor
     */
    public EmoticonsPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     * @param blogProperties Read-only properties for the Blog
     * @throws BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, HashMap blogProperties) throws BlojsomPluginException {
        String emoticonsConfiguration = servletConfig.getInitParameter(EMOTICONS_CONFIGURATION_IP);
        if (emoticonsConfiguration == null || "".equals(emoticonsConfiguration)) {
            throw new BlojsomPluginException("No value given for: " + EMOTICONS_CONFIGURATION_IP + " configuration parameter");
        }
        _emoticonsProperties = new Properties();
        InputStream is = servletConfig.getServletContext().getResourceAsStream(emoticonsConfiguration);
        try {
            _emoticonsProperties.load(is);
        } catch (IOException e) {
            throw new BlojsomPluginException(e);
        }
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest Request
     * @param context Context
     * @param entries Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws BlojsomPluginException If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest, Map context, BlogEntry[] entries) throws BlojsomPluginException {
        String blogSiteURL = BlojsomUtils.getBlogSiteURL(httpServletRequest.getRequestURL().toString(), httpServletRequest.getServletPath());
        if (blogSiteURL.endsWith("/")) {
            blogSiteURL = blogSiteURL.substring(0, blogSiteURL.length() - 1);
        }

        for (int i = 0; i < entries.length; i++) {
            BlogEntry entry = entries[i];
            String updatedDescription = entry.getDescription();
            updatedDescription = replaceEmoticon(updatedDescription, HAPPY, HAPPY_PARAM, blogSiteURL);
            updatedDescription = replaceEmoticon(updatedDescription, SAD, SAD_PARAM, blogSiteURL);
            updatedDescription = replaceEmoticon(updatedDescription, GRIN, GRIN_PARAM, blogSiteURL);
            updatedDescription = replaceEmoticon(updatedDescription, LOVE, LOVE_PARAM, blogSiteURL);
            updatedDescription = replaceEmoticon(updatedDescription, MISCHIEF, MISCHIEF_PARAM, blogSiteURL);
            updatedDescription = replaceEmoticon(updatedDescription, COOL, COOL_PARAM, blogSiteURL);
            updatedDescription = replaceEmoticon(updatedDescription, DEVIL, DEVIL_PARAM, blogSiteURL);
            updatedDescription = replaceEmoticon(updatedDescription, SILLY, SILLY_PARAM, blogSiteURL);
            updatedDescription = replaceEmoticon(updatedDescription, ANGRY, ANGRY_PARAM, blogSiteURL);
            updatedDescription = replaceEmoticon(updatedDescription, LAUGH, LAUGH_PARAM, blogSiteURL);
            updatedDescription = replaceEmoticon(updatedDescription, WINK, WINK_PARAM, blogSiteURL);
            updatedDescription = replaceEmoticon(updatedDescription, BLUSH, BLUSH_PARAM, blogSiteURL);
            updatedDescription = replaceEmoticon(updatedDescription, CRY, CRY_PARAM, blogSiteURL);
            updatedDescription = replaceEmoticon(updatedDescription, CONFUSED, CONFUSED_PARAM, blogSiteURL);
            updatedDescription = replaceEmoticon(updatedDescription, SHOCKED, SHOCKED_PARAM, blogSiteURL);
            updatedDescription = replaceEmoticon(updatedDescription, PLAIN, PLAIN_PARAM, blogSiteURL);
            entry.setDescription(updatedDescription);
        }
        return entries;
    }

    /**
     *
     * @param emoticonString
     * @param emoticon
     * @param emoticonParam
     * @param url
     * @return
     */
    private String replaceEmoticon(String emoticonString, String emoticon, String emoticonParam, String url) {
        String emoticonImage;
        emoticonImage = _emoticonsProperties.getProperty(emoticonParam);
        if (emoticonImage != null && !"".equals(emoticonImage)) {
            StringBuffer imageReference = new StringBuffer(IMG_OPEN);
            imageReference.append(url);
            imageReference.append(emoticonImage);
            imageReference.append(IMG_CLOSE);
            return BlojsomUtils.replace(emoticonString, emoticon, imageReference.toString());
        }

        return emoticonString;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws BlojsomPluginException If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws BlojsomPluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws BlojsomPluginException If there is an error in finalizing this plugin
     */
    public void destroy() throws BlojsomPluginException {
    }
}
