/**
 * Copyright (c) 2003, David A. Czarnecki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the "David A. Czarnecki" nor the names of
 * its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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
package org.ignition.blojsom.plugin.trackback;

import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.blog.Trackback;
import org.ignition.blojsom.plugin.BlojsomPlugin;
import org.ignition.blojsom.plugin.BlojsomPluginException;
import org.ignition.blojsom.util.BlojsomUtils;
import org.ignition.blojsom.util.BlojsomConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * TrackbackPlugin
 *
 * @author David Czarnecki
 */
public class TrackbackPlugin implements BlojsomPlugin {

    /**
     * Request parameter to indicate a trackback "tb"
     */
    private static final String TRACKBACK_PARAM = "tb";

    /**
     * Request parameter for the trackback "title"
     */
    private static final String TRACKBACK_TITLE_PARAM = "title";

    /**
     * Request parameter for the trackback "excerpt"
     */
    private static final String TRACKBACK_EXCERPT_PARAM = "excerpt";

    /**
     * Request parameter for the trackback "url"
     */
    private static final String TRACKBACK_URL_PARAM = "url";

    /**
     * Request parameter for the trackback "blog_name"
     */
    private static final String TRACKBACK_BLOG_NAME_PARAM = "blog_name";

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
    private static final String TRACKBACK_SUCCESS_PAGE = "trackback-success";

    /**
     * Trackback failure page
     */
    private static final String TRACKBACK_FAILURE_PAGE = "trackback-failure";

    private Log _logger = LogFactory.getLog(TrackbackPlugin.class);

    private String[] _blogFileExtensions;
    private String _blogHome;
    private String _blogTrackbackDirectory;

    /**
     * Default constructor
     */
    public TrackbackPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     * @param blogProperties Read-only properties for the Blog
     * @throws BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, HashMap blogProperties) throws BlojsomPluginException {
        _blogFileExtensions = (String[]) blogProperties.get(BlojsomConstants.BLOG_FILE_EXTENSIONS_IP);
        _blogHome = (String) blogProperties.get(BlojsomConstants.BLOG_HOME_IP);
        _blogTrackbackDirectory = (String) blogProperties.get(BlojsomConstants.BLOG_TRACKBACK_DIRECTORY_IP);
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
        String category = httpServletRequest.getPathInfo();
        if (category == null) {
            category = "/";
        } else if (!category.endsWith("/")) {
            category += "/";
        }
        String url = httpServletRequest.getParameter(TRACKBACK_URL_PARAM);
        String permalink = httpServletRequest.getParameter(BlojsomConstants.PERMALINK_PARAM);
        String title = httpServletRequest.getParameter(TRACKBACK_TITLE_PARAM);
        String excerpt = httpServletRequest.getParameter(TRACKBACK_EXCERPT_PARAM);
        String blogName = httpServletRequest.getParameter(TRACKBACK_BLOG_NAME_PARAM);
        String tb = httpServletRequest.getParameter(TRACKBACK_PARAM);

        if ((permalink != null) && (!"".equals(permalink)) && (tb != null) && ("y".equalsIgnoreCase(tb))) {
            if ((url == null) || ("".equals(url.trim()))) {
                context.put(BLOJSOM_TRACKBACK_RETURN_CODE, new Integer(1));
                context.put(BLOJSOM_TRACKBACK_MESSAGE, "No url parameter for trackback. url must be specified.");
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, TRACKBACK_FAILURE_PAGE);
                return entries;
            }

            if (title == null || "".equals(title)) {
                title = url;
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
            }
            if (!category.endsWith("/")) {
                category += "/";
            }
            Integer code = addTrackback(context, category, permalink, title, excerpt, url, blogName);
            context.put(BLOJSOM_TRACKBACK_RETURN_CODE, code);
            if (code.intValue() == 0) {
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, TRACKBACK_SUCCESS_PAGE);
            } else {
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, TRACKBACK_FAILURE_PAGE);
            }
        }

        return entries;
    }

    /**
     *
     * @param category
     * @param permalink
     * @param title
     * @param excerpt
     * @param url
     * @param blogName
     */
    private synchronized Integer addTrackback(Map context, String category, String permalink, String title,
                                              String excerpt, String url, String blogName) {
        Trackback trackback = new Trackback();
        trackback.setTitle(title);
        trackback.setExcerpt(excerpt);
        trackback.setUrl(url);
        trackback.setBlogName(blogName);
        trackback.setTrackbackDateLong(new Date().getTime());

        StringBuffer trackbackDirectory = new StringBuffer();
        String permalinkFilename = BlojsomUtils.getFilenameForPermalink(permalink, _blogFileExtensions);
        if (permalinkFilename == null) {
            _logger.debug("Invalid permalink trackback for: " + permalink);
            context.put(BLOJSOM_TRACKBACK_MESSAGE, "Invalid permalink trackback for: " + permalink);
            return new Integer(1);
        }

        trackbackDirectory.append(_blogHome);
        trackbackDirectory.append(BlojsomUtils.removeInitialSlash(category));
        File blogEntry = new File(trackbackDirectory.toString() + File.separator + permalink);
        if (!blogEntry.exists()) {
            _logger.error("Trying to create trackback for invalid blog entry: " + permalink);
            context.put(BLOJSOM_TRACKBACK_MESSAGE, "Trying to create trackback for invalid permalink");
            return new Integer(1);
        }
        trackbackDirectory.append(_blogTrackbackDirectory);
        trackbackDirectory.append(File.separator);
        trackbackDirectory.append(permalink);
        trackbackDirectory.append(File.separator);
        String trackbackFilename = trackbackDirectory.toString() + trackback.getTrackbackDateLong() + BlojsomConstants.TRACKBACK_EXTENSION;
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
            BufferedWriter bw = new BufferedWriter(new FileWriter(trackbackEntry));
            bw.write(trackback.getTitle());
            bw.newLine();
            bw.write(trackback.getExcerpt());
            bw.newLine();
            bw.write(trackback.getUrl());
            bw.newLine();
            bw.write(trackback.getBlogName());
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
