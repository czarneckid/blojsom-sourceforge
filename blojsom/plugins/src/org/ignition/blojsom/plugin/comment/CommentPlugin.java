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
    public static final String COMMENT_TEXT_PARAM = "commentText";

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
     * @param comment Comment
     */
    public synchronized void addBlogComment(String category, String permalink, String author,
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
