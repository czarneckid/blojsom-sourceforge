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
import java.util.Iterator;
import java.io.*;

/**
 * EditBlogCategoriesPlugin
 * 
 * @author czarnecki
 * @version $Id: EditBlogCategoriesPlugin.java,v 1.2 2003-11-04 06:04:17 czarneckid Exp $
 */
public class EditBlogCategoriesPlugin extends BaseAdminPlugin {

    private static Log _logger = LogFactory.getLog(EditBlogCategoriesPlugin.class);

    // Pages
    private static final String EDIT_BLOG_CATEGORIES_PAGE = "/org/blojsom/plugin/admin/admin-edit-blog-categories";
    private static final String EDIT_BLOG_CATEGORY_PAGE = "/org/blojsom/plugin/admin/admin-edit-blog-category";

    // Constants
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_CATEGORY_NAME = "BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_CATEGORY_NAME";
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_CATEGORY_METADATA = "BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_CATEGORY_METADATA";

    // Actions
    private static final String ADD_BLOG_CATEGORY_ACTION = "add-blog-category";
    private static final String DELETE_BLOG_CATEGORY_ACTION = "delete-blog-category";
    private static final String EDIT_BLOG_CATEGORY_ACTION = "edit-blog-category";
    private static final String UPDATE_BLOG_CATEGORY_ACTION = "update-blog-category";

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
     * @param servletConfig        Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration {@link BlojsomConfiguration} information
     * @throws BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws BlojsomPluginException {
        super.init(servletConfig, blojsomConfiguration);
    }

    /**
     * Delete a directory (or file) and any subdirectories underneath the directory
     * 
     * @param directoryOrFile Directory or file to be deleted
     * @return <code>true</code> if the directory (or file) could be deleted, <code>falde</code> otherwise
     */
    private boolean deleteDirectory(File directoryOrFile) {
        if (directoryOrFile.isDirectory()) {
            File[] children = directoryOrFile.listFiles();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirectory(children[i]);
                if (!success) {
                    return false;
                }
            }
        }

        return directoryOrFile.delete();
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
            String blogCategoryName = BlojsomUtils.getRequestValue(BLOG_CATEGORY_NAME, httpServletRequest);
            _logger.debug("User request blog category delete action");
            _logger.debug("Normalized category name: " + BlojsomUtils.normalize(blogCategoryName));

            File existingBlogCategory = new File(blog.getBlogHome() + "/" + BlojsomUtils.removeInitialSlash(blogCategoryName));
            if (!deleteDirectory(existingBlogCategory)) {
                _logger.debug("Unable to delete blog category: " + existingBlogCategory.toString());
            } else {
                _logger.debug("Deleted blog category: " + existingBlogCategory.toString());
            }

            httpServletRequest.setAttribute(PAGE_PARAM, EDIT_BLOG_CATEGORIES_PAGE);
        } else if (EDIT_BLOG_CATEGORY_ACTION.equals(action)) {
            String blogCategoryName = BlojsomUtils.getRequestValue(BLOG_CATEGORY_NAME, httpServletRequest);
            _logger.debug("Editing blog category: " + blogCategoryName);

            File existingBlogCategory = new File(blog.getBlogHome() + "/" + BlojsomUtils.removeInitialSlash(blogCategoryName));
            _logger.debug("Retrieving blog properties from category directory: " + existingBlogCategory.toString());
            String[] propertiesExtensions = blog.getBlogPropertiesExtensions();
            File[] propertiesFiles = existingBlogCategory.listFiles(BlojsomUtils.getExtensionsFilter(propertiesExtensions));

            if (propertiesFiles != null && propertiesFiles.length > 0) {
                StringBuffer categoryPropertiesString = new StringBuffer();
                for (int i = 0; i < propertiesFiles.length; i++) {
                    File propertiesFile = propertiesFiles[i];
                    _logger.debug("Loading blog properties from file: " + propertiesFile.toString());
                    BlojsomProperties categoryProperties = new BlojsomProperties();
                    try {
                        FileInputStream fis = new FileInputStream(propertiesFile);
                        categoryProperties.load(fis);
                        fis.close();

                        Iterator keyIterator = categoryProperties.keySet().iterator();
                        Object key;
                        while (keyIterator.hasNext()) {
                            key = keyIterator.next();
                            categoryPropertiesString.append(key.toString()).append("=").append(categoryProperties.get(key)).append("\r\n");
                        }
                    } catch (IOException e) {
                        _logger.error(e);
                    }
                }

                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_CATEGORY_NAME, blogCategoryName);
                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_CATEGORY_METADATA, categoryPropertiesString.toString());
                httpServletRequest.setAttribute(PAGE_PARAM, EDIT_BLOG_CATEGORY_PAGE);
            }
        } else if (ADD_BLOG_CATEGORY_ACTION.equals(action) || UPDATE_BLOG_CATEGORY_ACTION.equals(action)) {
            boolean isUpdatingCategory = UPDATE_BLOG_CATEGORY_ACTION.equals(action);

            String blogCategoryName = BlojsomUtils.getRequestValue(BLOG_CATEGORY_NAME, httpServletRequest);
            // Check for blank or null category
            if (blogCategoryName == null || "".equals(blogCategoryName)) {
                httpServletRequest.setAttribute(PAGE_PARAM, EDIT_BLOG_CATEGORIES_PAGE);
                return entries;
            }
            blogCategoryName = BlojsomUtils.normalize(blogCategoryName);

            if (!isUpdatingCategory) {
                _logger.debug("Adding blog category: " + blogCategoryName);
            } else {
                _logger.debug("Updating blog category: " + blogCategoryName);
            }

            String blogCategoryMetaData = BlojsomUtils.getRequestValue(BLOG_CATEGORY_META_DATA, httpServletRequest);
            if (blogCategoryMetaData == null) {
                blogCategoryMetaData = "";
            }

            if (!isUpdatingCategory) {
                _logger.debug("Adding blog category meta-data: " + blogCategoryMetaData);
            }

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

            File newBlogCategory = new File(blog.getBlogHome() + "/" + BlojsomUtils.removeInitialSlash(blogCategoryName));
            if (!isUpdatingCategory) {
                if (!newBlogCategory.mkdirs()) {
                    _logger.error("Unable to add new blog category: " + blogCategoryName);
                } else {
                    _logger.debug("Created blog directory: " + newBlogCategory.toString());
                }
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

            if (!isUpdatingCategory) {
                _logger.debug("Successfully added new blog category: " + blogCategoryName);
            } else {
                _logger.debug("Successfully updated blog category: " + blogCategoryName);
            }
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
