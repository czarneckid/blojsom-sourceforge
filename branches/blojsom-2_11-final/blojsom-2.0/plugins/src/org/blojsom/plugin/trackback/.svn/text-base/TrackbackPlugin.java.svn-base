/**
 * Copyright (c) 2003-2004, David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003-2004 by Mark Lussier
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
package org.blojsom.plugin.trackback;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.*;
import org.blojsom.plugin.BlojsomPluginException;
import org.blojsom.plugin.common.IPBanningPlugin;
import org.blojsom.plugin.email.EmailUtils;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;
import java.util.Map;
import java.util.Calendar;
import java.util.HashMap;

/**
 * TrackbackPlugin
 *
 * @author David Czarnecki
 * @version $Id: TrackbackPlugin.java,v 1.12 2004-02-06 20:11:37 czarneckid Exp $
 */
public class TrackbackPlugin extends IPBanningPlugin implements BlojsomConstants {

    /**
     * Default prefix for trackback e-mail notification
     */
    private static final String DEFAULT_TRACKBACK_PREFIX = "[blojsom] Trackback on: ";

    /**
     * Initialization parameter for e-mail prefix
     */
    private static final String TRACKBACK_PREFIX_IP = "plugin-trackback-email-prefix";

    /**
     * Initialization parameter for the throttling of trackbacks from IP addresses
     */
    private static final String TRACKBACK_THROTTLE_MINUTES_IP = "plugin-trackback-throttle";

    /**
     * Default throttle value for trackbacks from a particular IP address
     */
    private static final int TRACKBACK_THROTTLE_DEFAULT_MINUTES = 5;

    /**
     * Request parameter to indicate a trackback "tb"
     */
    private static final String TRACKBACK_PARAM = "tb";

    /**
     * Request parameter for the trackback "title"
     */
    public static final String TRACKBACK_TITLE_PARAM = "title";

    /**
     * Request parameter for the trackback "excerpt"
     */
    public static final String TRACKBACK_EXCERPT_PARAM = "excerpt";

    /**
     * Request parameter for the trackback "url"
     */
    public static final String TRACKBACK_URL_PARAM = "url";

    /**
     * Request parameter for the trackback "blog_name"
     */
    public static final String TRACKBACK_BLOG_NAME_PARAM = "blog_name";

    /**
     * Key under which the indicator this plugin is "live" will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_TRACKBACK_PLUGIN_ENABLED = "BLOJSOM_TRACKBACK_PLUGIN_ENABLED";

    /**
     * Key under which the trackback return code will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_TRACKBACK_RETURN_CODE = "BLOJSOM_TRACKBACK_RETURN_CODE";

    /**
     * Key under which the trackback error message will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_TRACKBACK_MESSAGE = "BLOJSOM_TRACKBACK_MESSAGE";

    /**
     * Trackback success page
     */
    private static final String TRACKBACK_SUCCESS_PAGE = "/trackback-success";

    /**
     * Trackback failure page
     */
    private static final String TRACKBACK_FAILURE_PAGE = "/trackback-failure";

    private Map _ipAddressTrackbackTimes;

    private Log _logger = LogFactory.getLog(TrackbackPlugin.class);

    /**
     * Default constructor
     */
    public TrackbackPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig        Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration {@link org.blojsom.blog.BlojsomConfiguration} information
     * @throws BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws BlojsomPluginException {
        super.init(servletConfig, blojsomConfiguration);

        _ipAddressTrackbackTimes = new HashMap(10);
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param user                {@link BlogUser} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws BlojsomPluginException If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse,
                               BlogUser user,
                               Map context,
                               BlogEntry[] entries) throws BlojsomPluginException {
        Blog blog = user.getBlog();
        context.put(BLOJSOM_TRACKBACK_PLUGIN_ENABLED, blog.getBlogTrackbacksEnabled());
        if (!blog.getBlogTrackbacksEnabled().booleanValue()) {
            _logger.debug("blog trackbacks not enabled for user: " + user.getId());
            return entries;
        }

        String[] _blogFileExtensions;
        String _blogHome;
        String _blogTrackbackDirectory;
        Boolean _blogEmailEnabled;
        Boolean _blogTrackbacksEnabled;
        String _blogUrlPrefix;
        String _blogFileEncoding;
        String _emailPrefix;

        _blogFileExtensions = blog.getBlogFileExtensions();
        _blogHome = blog.getBlogHome();
        _blogTrackbackDirectory = blog.getBlogTrackbackDirectory();
        _blogEmailEnabled = blog.getBlogEmailEnabled();
        _blogTrackbacksEnabled = blog.getBlogTrackbacksEnabled();
        _blogUrlPrefix = blog.getBlogURL();
        _blogFileEncoding = blog.getBlogFileEncoding();
        _emailPrefix = blog.getBlogProperty(TRACKBACK_PREFIX_IP);
        if (_emailPrefix == null) {
            _emailPrefix = DEFAULT_TRACKBACK_PREFIX;
        }

        if (entries.length == 0) {
            return entries;
        }

        if (!_blogTrackbacksEnabled.booleanValue()) {
            return entries;
        }

        String bannedIPListParam = blog.getBlogProperty(BANNED_IP_ADDRESSES_IP);
        String[] bannedIPList;
        if (bannedIPListParam == null) {
            bannedIPList = null;
            _logger.info("Blog configuration parameter not supplied for: " + BANNED_IP_ADDRESSES_IP);
        } else {
            bannedIPList = BlojsomUtils.parseCommaList(bannedIPListParam);
        }

        // Check for a trackback from a banned IP address
        String remoteIPAddress = httpServletRequest.getRemoteAddr();
        if (isIPBanned(bannedIPList, remoteIPAddress)) {
            _logger.debug("Attempted trackback from banned IP address: " + remoteIPAddress);
            return entries;
        }

        String category = httpServletRequest.getPathInfo();
        if (category == null) {
            category = "/";
        } else if (!category.endsWith("/")) {
            category += "/";
        }

        if (category.startsWith("/" + user.getId() + "/")) {
            category = BlojsomUtils.getCategoryFromPath(category);
        }

        String url = httpServletRequest.getParameter(TRACKBACK_URL_PARAM);
        String permalink = httpServletRequest.getParameter(PERMALINK_PARAM);
        String title = httpServletRequest.getParameter(TRACKBACK_TITLE_PARAM);
        String excerpt = httpServletRequest.getParameter(TRACKBACK_EXCERPT_PARAM);
        String blogName = httpServletRequest.getParameter(TRACKBACK_BLOG_NAME_PARAM);
        String tb = httpServletRequest.getParameter(TRACKBACK_PARAM);

        String entryTitle = entries[0].getTitle();

        if ((permalink != null) && (!"".equals(permalink)) && (tb != null) && ("y".equalsIgnoreCase(tb))) {
            if ((url == null) || ("".equals(url.trim()))) {
                context.put(BLOJSOM_TRACKBACK_RETURN_CODE, new Integer(1));
                context.put(BLOJSOM_TRACKBACK_MESSAGE, "No url parameter for trackback. url must be specified.");
                httpServletRequest.setAttribute(PAGE_PARAM, TRACKBACK_FAILURE_PAGE);

                return entries;
            }

            // Check for trackback throttling
            String trackbackThrottleValue = blog.getBlogProperty(TRACKBACK_THROTTLE_MINUTES_IP);
            if (!BlojsomUtils.checkNullOrBlank(trackbackThrottleValue)) {
                int trackbackThrottleMinutes;

                try {
                    trackbackThrottleMinutes = Integer.parseInt(trackbackThrottleValue);
                } catch (NumberFormatException e) {
                    trackbackThrottleMinutes = TRACKBACK_THROTTLE_DEFAULT_MINUTES;
                }
                _logger.debug("Trackback throttling enabled at: " + trackbackThrottleMinutes + " minutes");

                remoteIPAddress = httpServletRequest.getRemoteAddr();
                if (_ipAddressTrackbackTimes.containsKey(remoteIPAddress)) {
                    Calendar currentTime = Calendar.getInstance();
                    Calendar timeOfLastTrackback = (Calendar) _ipAddressTrackbackTimes.get(remoteIPAddress);
                    long timeDifference = currentTime.getTimeInMillis() - timeOfLastTrackback.getTimeInMillis();

                    long differenceInMinutes = timeDifference / (60 * 1000);
                    if (differenceInMinutes < trackbackThrottleMinutes) {
                        _logger.debug("Trackback throttle enabled. Comment from IP address: " + remoteIPAddress + " in less than " + trackbackThrottleMinutes + " minutes");

                        context.put(BLOJSOM_TRACKBACK_RETURN_CODE, new Integer(1));
                        context.put(BLOJSOM_TRACKBACK_MESSAGE, "Trackback throttling enabled.");
                        httpServletRequest.setAttribute(PAGE_PARAM, TRACKBACK_FAILURE_PAGE);

                        return entries;
                    } else {
                        _logger.debug("Trackback throttle enabled. Resetting date of last comment to current time");
                        _ipAddressTrackbackTimes.put(remoteIPAddress, currentTime);
                    }
                } else {
                    Calendar calendar = Calendar.getInstance();
                    _ipAddressTrackbackTimes.put(remoteIPAddress, calendar);
                }
            }

            url = url.trim();

            if (BlojsomUtils.checkNullOrBlank(title)) {
                title = url;
            } else {
                title = title.trim();
            }

            if (excerpt == null) {
                excerpt = "";
            } else {
                if (excerpt.length() >= 255) {
                    excerpt = excerpt.substring(0, 252);
                    excerpt += "...";
                }
            }

            if (blogName == null) {
                blogName = "";
            } else {
                blogName = blogName.trim();
            }

            if (!category.endsWith("/")) {
                category += "/";
            }

            Integer code = addTrackback(context, category, permalink, title, excerpt, url, blogName,
                    _blogFileExtensions, _blogHome, _blogTrackbackDirectory,
                    _blogFileEncoding);

            // For persisting the Last-Modified time
            httpServletRequest.getSession().setAttribute(BLOJSOM_LAST_MODIFIED, new Long(new Date().getTime()));

            if (_blogEmailEnabled.booleanValue()) {
                sendTrackbackEmail(entryTitle, title, category, permalink, url, excerpt, blogName, context,
                        _blogUrlPrefix, _emailPrefix);
            }

            context.put(BLOJSOM_TRACKBACK_RETURN_CODE, code);
            if (code.intValue() == 0) {
                httpServletRequest.setAttribute(PAGE_PARAM, TRACKBACK_SUCCESS_PAGE);
            } else {
                httpServletRequest.setAttribute(PAGE_PARAM, TRACKBACK_FAILURE_PAGE);
            }
        }

        return entries;
    }

    /**
     * Add a trackback to the permalink entry
     *
     * @param category  Category where the permalink exists
     * @param permalink Permalink
     * @param title     Trackback title
     * @param excerpt   Excerpt for the trackback (not more than 255 characters in length)
     * @param url       URL for the trackback
     * @param blogName  Name of the blog making the trackback
     */
    private Integer addTrackback(Map context, String category, String permalink, String title,
                                 String excerpt, String url, String blogName,
                                 String[] blogFileExtensions, String blogHome,
                                 String blogTrackbackDirectory, String blogFileEncoding) {
        Trackback trackback = new Trackback();
        excerpt = BlojsomUtils.escapeMetaAndLink(excerpt);
        trackback.setTitle(title);
        trackback.setExcerpt(excerpt);
        trackback.setUrl(url);
        trackback.setBlogName(blogName);
        trackback.setTrackbackDateLong(new Date().getTime());

        StringBuffer trackbackDirectory = new StringBuffer();
        String permalinkFilename = BlojsomUtils.getFilenameForPermalink(permalink, blogFileExtensions);
        permalinkFilename = BlojsomUtils.urlDecode(permalinkFilename);
        if (permalinkFilename == null) {
            _logger.debug("Invalid permalink trackback for: " + permalink);
            context.put(BLOJSOM_TRACKBACK_MESSAGE, "Invalid permalink trackback for: " + permalink);
            return new Integer(1);
        }

        trackbackDirectory.append(blogHome);
        trackbackDirectory.append(BlojsomUtils.removeInitialSlash(category));
        File blogEntry = new File(trackbackDirectory.toString() + File.separator + permalinkFilename);
        if (!blogEntry.exists()) {
            _logger.error("Trying to create trackback for invalid blog entry: " + permalink);
            context.put(BLOJSOM_TRACKBACK_MESSAGE, "Trying to create trackback for invalid permalink: " + category + permalinkFilename);
            return new Integer(1);
        }
        trackbackDirectory.append(blogTrackbackDirectory);
        trackbackDirectory.append(File.separator);
        trackbackDirectory.append(permalinkFilename);
        trackbackDirectory.append(File.separator);
        String trackbackFilename = trackbackDirectory.toString() + trackback.getTrackbackDateLong() + TRACKBACK_EXTENSION;
        File trackbackDir = new File(trackbackDirectory.toString());
        if (!trackbackDir.exists()) {
            if (!trackbackDir.mkdirs()) {
                _logger.error("Could not create directory for trackbacks: " + trackbackDirectory);
                context.put(BLOJSOM_TRACKBACK_MESSAGE, "Could not create directory for trackbacks");
                return new Integer(1);
            }
        }

        File trackbackEntry = new File(trackbackFilename);
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(trackbackEntry), blogFileEncoding));
            bw.write(BlojsomUtils.nullToBlank(trackback.getTitle()).trim());
            bw.newLine();
            bw.write(BlojsomUtils.nullToBlank(trackback.getExcerpt()).trim());
            bw.newLine();
            bw.write(BlojsomUtils.nullToBlank(trackback.getUrl()).trim());
            bw.newLine();
            bw.write(BlojsomUtils.nullToBlank(trackback.getBlogName()).trim());
            bw.newLine();
            bw.close();
            _logger.debug("Added trackback: " + trackbackFilename);
        } catch (IOException e) {
            _logger.error(e);
            context.put(BLOJSOM_TRACKBACK_MESSAGE, "I/O error on trackback write.");
            return new Integer(1);
        }

        return new Integer(0);
    }


    /**
     * Send Trackback Email to Blog Author
     *
     * @param entryTitle Blog Entry title for this Trackback
     * @param title      title of trackback entry
     * @param category   catagory for trackbacked entry
     * @param permalink  permalink for trackbacked entry
     * @param url        URL of site tracking back
     * @param excerpt    excerpt of trackback post
     * @param blogName   Title of trackbacking blog
     * @param context    Context
     */
    private void sendTrackbackEmail(String entryTitle, String title, String category, String permalink, String url,
                                    String excerpt, String blogName, Map context,
                                    String blogUrlPrefix, String emailPrefix) {

        StringBuffer _trackback = new StringBuffer();
        _trackback.append("Trackback on: ").append(blogUrlPrefix).append(BlojsomUtils.removeInitialSlash(category));
        _trackback.append("?permalink=").append(permalink).append("&page=trackback").append("\n");

        _trackback.append("\n==[ Trackback ]==========================================================").append("\n\n");

        if (title != null && !title.equals("")) {
            _trackback.append("Title    : ").append(title).append("\n");
        }
        if (url != null && !url.equals("")) {
            _trackback.append("Url      : ").append(url).append("\n");
        }
        if (blogName != null && !blogName.equals("")) {
            _trackback.append("Blog Name: ").append(blogName).append("\n");
        }
        if (excerpt != null && !excerpt.equals("")) {
            _trackback.append("Excerpt  : ").append(excerpt).append("\n");
        }

        EmailUtils.notifyBlogAuthor(emailPrefix + entryTitle, _trackback.toString(), context);
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
