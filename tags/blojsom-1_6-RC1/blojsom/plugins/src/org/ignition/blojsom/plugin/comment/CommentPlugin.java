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
package org.ignition.blojsom.plugin.comment;

import org.ignition.blojsom.plugin.BlojsomPlugin;
import org.ignition.blojsom.plugin.BlojsomPluginException;
import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.blog.BlogComment;
import org.ignition.blojsom.util.BlojsomUtils;
import org.ignition.blojsom.util.BlojsomConstants;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

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
 * CommentPlugin
 *
 * @author David Czarnecki
 * @version $Id: CommentPlugin.java,v 1.5 2003-03-23 18:31:17 intabulas Exp $
 */
public class CommentPlugin implements BlojsomPlugin {

    /**
     * Request parameter for the "comment"
     */
    private static final String COMMENT_PARAM = "comment";

    /**
     * Request parameter for the "author"
     */
    private static final String AUTHOR_PARAM = "author";

    /**
     * Request parameter for the "authorEmail"
     */
    private static final String AUTHOR_EMAIL_PARAM = "authorEmail";

    /**
     * Request parameter for the "authorURL"
     */
    private static final String AUTHOR_URL_PARAM = "authorURL";

    /**
     * Request parameter for the "commentText"
     */
    private static final String COMMENT_TEXT_PARAM = "commentText";

    private Log _logger = LogFactory.getLog(CommentPlugin.class);
    private Boolean _blogCommentsEnabled;
    private String[] _blogFileExtensions;
    private String _blogHome;
    private String _blogCommentsDirectory;

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
        _blogCommentsEnabled = (Boolean) blogProperties.get(BlojsomConstants.BLOG_COMMENTS_ENABLED_IP);
        _blogCommentsDirectory = (String) blogProperties.get(BlojsomConstants.BLOG_COMMENTS_DIRECTORY_IP);
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
        // Comment handling
        if ("y".equalsIgnoreCase(httpServletRequest.getParameter(COMMENT_PARAM)) && _blogCommentsEnabled.booleanValue()) {
            String author = httpServletRequest.getParameter(AUTHOR_PARAM);
            String authorEmail = httpServletRequest.getParameter(AUTHOR_EMAIL_PARAM);
            String authorURL = httpServletRequest.getParameter(AUTHOR_URL_PARAM);
            String commentText = httpServletRequest.getParameter(COMMENT_TEXT_PARAM);
            String permalink = httpServletRequest.getParameter(BlojsomConstants.PERMALINK_PARAM);
            String category = httpServletRequest.getParameter(BlojsomConstants.CATEGORY_PARAM);
            if ((author != null && !"".equals(author)) && (commentText != null && !"".equals(commentText))
                    && (permalink != null && !"".equals(permalink)) && (category != null && !"".equals(category))) {
                if (!category.endsWith("/")) {
                    category += "/";
                }
                addBlogComment(category, permalink, author, authorEmail, authorURL, commentText);
            }
        }

        return entries;
    }

    /**
     * Add a comment to a particular blog entry
     *
     * @param category Blog entry category
     * @param permalink Blog entry permalink
     * @param author Comment author
     * @param authorEmail Comment author e-mail
     * @param authorURL Comment author URL
     * @param userComment Comment
     */
    private synchronized void addBlogComment(String category, String permalink, String author,
                                            String authorEmail, String authorURL, String userComment) {
        if (_blogCommentsEnabled.booleanValue()) {
            BlogComment comment = new BlogComment();
            comment.setAuthor(author);
            comment.setAuthorEmail(authorEmail);
            comment.setAuthorURL(authorURL);
            comment.setComment(userComment);
            comment.setCommentDate(new Date());

            StringBuffer commentDirectory = new StringBuffer();
            String permalinkFilename = BlojsomUtils.getFilenameForPermalink(permalink, _blogFileExtensions);
            if (permalinkFilename == null) {
                _logger.debug("Invalid permalink comment for: " + permalink);
                return;
            }
            commentDirectory.append(_blogHome);
            commentDirectory.append(BlojsomUtils.removeInitialSlash(category));
            File blogEntry = new File(commentDirectory.toString() + File.separator + permalink);
            if (!blogEntry.exists()) {
                _logger.error("Trying to create comment for invalid blog entry: " + permalink);
                return;
            }
            commentDirectory.append(_blogCommentsDirectory);
            commentDirectory.append(File.separator);
            commentDirectory.append(permalink);
            commentDirectory.append(File.separator);
            String commentFilename = commentDirectory.toString() + comment.getCommentDate().getTime() + BlojsomConstants.COMMENT_EXTENSION;
            File commentDir = new File(commentDirectory.toString());
            if (!commentDir.exists()) {
                if (!commentDir.mkdirs()) {
                    _logger.error("Could not create directory for comments: " + commentDirectory);
                    return;
                }
            }

            File commentEntry = new File(commentFilename);
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(commentEntry));
                bw.write(comment.getAuthor());
                bw.newLine();
                bw.write(comment.getAuthorEmail());
                bw.newLine();
                bw.write(comment.getAuthorURL());
                bw.newLine();
                bw.write(comment.getComment());
                bw.newLine();
                bw.close();
                _logger.debug("Added blog comment: " + commentFilename);
            } catch (IOException e) {
                _logger.error(e);
            }
        }
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
