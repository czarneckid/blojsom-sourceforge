package org.blojsom.plugin.admin;

import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;
import org.blojsom.blog.BlojsomConfiguration;
import org.blojsom.blog.Blog;
import org.blojsom.plugin.BlojsomPluginException;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.BlojsomProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.io.*;

/**
 * EditBlogCategoriesPlugin
 *
 * @author czarnecki
 * @version $Id: EditBlogCategoriesPlugin.java,v 1.1 2003-11-03 04:14:00 czarneckid Exp $
 */
public class EditBlogCategoriesPlugin extends BaseAdminPlugin {

    private static Log _logger = LogFactory.getLog(EditBlogCategoriesPlugin.class);

    private static final String EDIT_BLOG_CATEGORIES_PAGE = "/org/blojsom/plugin/admin/admin-edit-blog-categories";

    // Actions
    private static final String ADD_BLOG_CATEGORY_ACTION = "add-blog-category";
    private static final String DELETE_BLOG_CATEGORY_ACTION = "delete-blog-category";
    private static final String EDIT_BLOG_CATEGORY_ACTION = "edit-blog-category";

    // Form elements
    private static final String BLOG_CATEGORY_NAME = "blog-category-name";
    private static final String BLOG_CATEGORY_META_DATA = "blog-category-meta-data";

    /**
     * Default constructor
     */
    public EditBlogCategoriesPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration {@link BlojsomConfiguration} information
     * @throws BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws BlojsomPluginException {
        super.init(servletConfig, blojsomConfiguration);
    }


    /**
     * Process the blog entries
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @param user {@link BlogUser} instance
     * @param context Context
     * @param entries Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws BlojsomPluginException If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlogUser user, Map context, BlogEntry[] entries) throws BlojsomPluginException {
        entries = super.process(httpServletRequest, httpServletResponse, user, context, entries);
        Blog blog = user.getBlog();

        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);
        if (action == null || "".equals(action)) {
            _logger.debug("User did not request edit action");
            httpServletRequest.setAttribute(PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
        } else if (PAGE_ACTION.equals(action)) {
            _logger.debug("User requested edit categories page");
            httpServletRequest.setAttribute(PAGE_PARAM, EDIT_BLOG_CATEGORIES_PAGE);
        } else if (DELETE_BLOG_CATEGORY_ACTION.equals(action)) {
            String category = BlojsomUtils.getRequestValue(BLOG_CATEGORY_NAME, httpServletRequest);
            _logger.debug("User request blog category delete action");
            _logger.debug("Normalized category name: " + BlojsomUtils.normalize(category));

            httpServletRequest.setAttribute(PAGE_PARAM, EDIT_BLOG_CATEGORIES_PAGE);
        } else if (EDIT_BLOG_CATEGORY_ACTION.equals(action)) {
            String blogCategoryName = BlojsomUtils.getRequestValue(BLOG_CATEGORY_NAME, httpServletRequest);
            _logger.debug("Editing blog category: " + blogCategoryName);

            httpServletRequest.setAttribute(PAGE_PARAM, EDIT_BLOG_CATEGORIES_PAGE);
        } else if (ADD_BLOG_CATEGORY_ACTION.equals(action)) {
            String blogCategoryName = BlojsomUtils.getRequestValue(BLOG_CATEGORY_NAME, httpServletRequest);
            // Check for blank or null category
            if (blogCategoryName == null || "".equals(blogCategoryName)) {
                httpServletRequest.setAttribute(PAGE_PARAM, EDIT_BLOG_CATEGORIES_PAGE);
                return entries;
            }
            blogCategoryName = BlojsomUtils.normalize(blogCategoryName);
            _logger.debug("Adding blog category: " + blogCategoryName);

            String blogCategoryMetaData = BlojsomUtils.getRequestValue(BLOG_CATEGORY_META_DATA, httpServletRequest);
            if (blogCategoryMetaData == null) {
                blogCategoryMetaData = "";
            }
            _logger.debug("Adding blog category meta-data: " + blogCategoryMetaData);

            // Separate the blog category meta-data into key/value pairs
            BufferedReader br = new BufferedReader(new StringReader(blogCategoryMetaData));
            String input;
            String[] splitInput;
            BlojsomProperties categoryMetaData = new BlojsomProperties(blog.getBlogFileEncoding());
            try {
                while ((input = br.readLine()) != null) {
                    splitInput = input.split("=");
                    if (splitInput.length == 2) {
                        categoryMetaData.put(splitInput[0], splitInput[1]);
                    }
                }
            } catch (IOException e) {
                _logger.error(e);
            }

            File newBlogCategory = new File(blog.getBlogHome() + "/" + user.getId() + "/" + BlojsomUtils.removeInitialSlash(blogCategoryName));
            if (!newBlogCategory.mkdirs()) {
                _logger.error("Unable to add new blog category: " + blogCategoryName);
            } else {
                _logger.debug("Created blog directory: " + newBlogCategory.toString());
            }

            File newBlogProperties = new File(newBlogCategory.getAbsolutePath() + "/blojsom.properties");
            try {
                FileOutputStream fos = new FileOutputStream(newBlogProperties);
                categoryMetaData.store(fos, null);
                fos.close();
                _logger.debug("Wrote blog properties to: " + newBlogProperties.toString());
            } catch (IOException e) {
                _logger.error(e);
            }

            _logger.debug("Successfully added new blog category: " + blogCategoryName);
            httpServletRequest.setAttribute(PAGE_PARAM, EDIT_BLOG_CATEGORIES_PAGE);
        }

        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws BlojsomPluginException If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws BlojsomPluginException {
        super.cleanup();
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws BlojsomPluginException If there is an error in finalizing this plugin
     */
    public void destroy() throws BlojsomPluginException {
        super.destroy();
    }
}
