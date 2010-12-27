/**
 * Copyright (c) 2003-2006, David A. Czarnecki
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
package org.blojsom.plugin.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.*;
import org.blojsom.fetcher.BlojsomFetcher;
import org.blojsom.fetcher.BlojsomFetcherException;
import org.blojsom.plugin.BlojsomPluginException;
import org.blojsom.plugin.admin.event.UpdatedBlogCategoryEvent;
import org.blojsom.plugin.admin.event.AddedBlogCategoryEvent;
import org.blojsom.plugin.admin.event.DeletedBlogCategoryEvent;
import org.blojsom.util.BlojsomProperties;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.BlojsomException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Date;

/**
 * EditBlogCategoriesPlugin
 * 
 * @author czarnecki
 * @since blojsom 2.04
 * @version $Id: EditBlogCategoriesPlugin.java,v 1.24 2006-01-04 16:52:59 czarneckid Exp $
 */
public class EditBlogCategoriesPlugin extends BaseAdminPlugin {

    private static Log _logger = LogFactory.getLog(EditBlogCategoriesPlugin.class);

    // Localization constants
    private static final String FAILED_PERMISSION_KEY = "failed.permission.text";
    private static final String DELETED_CATEGORY_KEY = "deleted.category.text";
    private static final String FAILED_DELETED_CATEGORY_KEY = "failed.deleted.category.text";
    private static final String FAILED_LOAD_CATEGORY_KEY = "failed.load.category.text";
    private static final String NO_CATEGORY_SPECIFIED_KEY = "no.category.specified.text";
    private static final String FAILED_CATEGORY_METADATA_READ_KEY = "failed.category.metadata.read.text";
    private static final String CATEGORY_ADD_SUCCESS_KEY = "category.add.success.text";
    private static final String CATEGORY_UPDATE_SUCCESS_KEY = "category.update.success.text";
    private static final String CATEGORY_CHANGE_FAILED_KEY = "category.change.failed.text";

    // Pages
    private static final String EDIT_BLOG_CATEGORIES_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blog-categories";
    private static final String EDIT_BLOG_CATEGORY_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blog-category";

    // Constants
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_CATEGORY_NAME = "BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_CATEGORY_NAME";
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_CATEGORY_DESCRIPTION = "BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_CATEGORY_DESCRIPTION";
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_CATEGORY_METADATA = "BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_CATEGORY_METADATA";
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_ALL_CATEGORIES = "BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_ALL_CATEGORIES";

    // Actions
    private static final String ADD_BLOG_CATEGORY_ACTION = "add-blog-category";
    private static final String DELETE_BLOG_CATEGORY_ACTION = "delete-blog-category";
    private static final String EDIT_BLOG_CATEGORY_ACTION = "edit-blog-category";
    private static final String UPDATE_BLOG_CATEGORY_ACTION = "update-blog-category";

    // Form elements
    private static final String BLOG_CATEGORY_NAME = "blog-category-name";
    private static final String BLOG_CATEGORY_DESCRIPTION = "blog-category-description";
    private static final String BLOG_CATEGORY_META_DATA = "blog-category-meta-data";
    private static final String BLOG_CATEGORY_PARENT = "blog-category-parent";

    private static final String EDIT_BLOG_CATEGORIES_PERMISSION = "edit_blog_categories";

    private BlojsomFetcher _fetcher;

    /**
     * Default constructor.
     */
    public EditBlogCategoriesPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig        Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration {@link org.blojsom.blog.BlojsomConfiguration} information
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws BlojsomPluginException {
        super.init(servletConfig, blojsomConfiguration);

        String fetcherClassName = blojsomConfiguration.getFetcherClass();
        try {
            Class fetcherClass = Class.forName(fetcherClassName);
            _fetcher = (BlojsomFetcher) fetcherClass.newInstance();
            _fetcher.init(servletConfig, blojsomConfiguration);
            _logger.info("Added blojsom fetcher: " + fetcherClassName);
        } catch (ClassNotFoundException e) {
            _logger.error(e);
            throw new BlojsomPluginException(e);
        } catch (InstantiationException e) {
            _logger.error(e);
            throw new BlojsomPluginException(e);
        } catch (IllegalAccessException e) {
            _logger.error(e);
            throw new BlojsomPluginException(e);
        } catch (BlojsomFetcherException e) {
            _logger.error(e);
            throw new BlojsomPluginException(e);
        }
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
        if (!authenticateUser(httpServletRequest, httpServletResponse, context, user)) {
            httpServletRequest.setAttribute(PAGE_PARAM, ADMIN_LOGIN_PAGE);

            return entries;
        }

        Blog blog = user.getBlog();

        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);
        try {
            BlogCategory[] allCategories = _fetcher.fetchCategories(null, user);
            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_ALL_CATEGORIES, allCategories);
        } catch (BlojsomFetcherException e) {
            _logger.error(e);
        }

        String username = getUsernameFromSession(httpServletRequest, user.getBlog());
        if (!checkPermission(user, null, username, EDIT_BLOG_CATEGORIES_PERMISSION)) {
            httpServletRequest.setAttribute(PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            addOperationResultMessage(context, getAdminResource(FAILED_PERMISSION_KEY, FAILED_PERMISSION_KEY, user.getBlog().getBlogAdministrationLocale()));

            return entries;
        }

        if (BlojsomUtils.checkNullOrBlank(action)) {
            _logger.debug("User did not request edit action");
            httpServletRequest.setAttribute(PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
        } else if (PAGE_ACTION.equals(action)) {
            _logger.debug("User requested edit categories page");
            httpServletRequest.setAttribute(PAGE_PARAM, EDIT_BLOG_CATEGORIES_PAGE);
        } else if (DELETE_BLOG_CATEGORY_ACTION.equals(action)) {
            _logger.debug("User request blog category delete action");
            String blogCategoryName = BlojsomUtils.getRequestValue(BLOG_CATEGORY_NAME, httpServletRequest);
            blogCategoryName = BlojsomUtils.normalize(blogCategoryName);

            BlogCategory blogCategoryToDelete = _fetcher.newBlogCategory();
            blogCategoryToDelete.setCategory(blogCategoryName);
            try {
                blogCategoryToDelete.delete(user);

                 _logger.debug("Deleted blog category: " + blogCategoryName);
                addOperationResultMessage(context, formatAdminResource(DELETED_CATEGORY_KEY, DELETED_CATEGORY_KEY, user.getBlog().getBlogAdministrationLocale(), new Object[] {blogCategoryName}));

                _blojsomConfiguration.getEventBroadcaster().broadcastEvent(new DeletedBlogCategoryEvent(this, new Date(), blogCategoryToDelete, user));
            } catch (BlojsomException e) {
                _logger.error(e);

                _logger.error("Unable to delete blog category: " + blogCategoryName);
                addOperationResultMessage(context, formatAdminResource(FAILED_DELETED_CATEGORY_KEY, FAILED_DELETED_CATEGORY_KEY, user.getBlog().getBlogAdministrationLocale(), new Object[] {blogCategoryName}));
            }

            httpServletRequest.setAttribute(PAGE_PARAM, EDIT_BLOG_CATEGORIES_PAGE);
        } else if (EDIT_BLOG_CATEGORY_ACTION.equals(action)) {
            String blogCategoryName = BlojsomUtils.getRequestValue(BLOG_CATEGORY_NAME, httpServletRequest);
            blogCategoryName = BlojsomUtils.normalize(blogCategoryName);
            _logger.debug("Editing blog category: " + blogCategoryName);

            BlogCategory blogCategoryToEdit = _fetcher.newBlogCategory();
            blogCategoryToEdit.setCategory(blogCategoryName);
            try {
                blogCategoryToEdit.load(user);

                // Try and load the category description if available
                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_CATEGORY_DESCRIPTION, blogCategoryToEdit.getMetaData().get(NAME_KEY));

                StringBuffer categoryPropertiesString = new StringBuffer();
                Iterator keyIterator = blogCategoryToEdit.getMetaData().keySet().iterator();
                Object key;
                while (keyIterator.hasNext()) {
                    key = keyIterator.next();
                    categoryPropertiesString.append(key.toString()).append("=").append(blogCategoryToEdit.getMetaData().get(key)).append("\r\n");
                }

                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_CATEGORY_METADATA, categoryPropertiesString.toString());
            } catch (BlojsomException e) {
                _logger.error(e);

                addOperationResultMessage(context, formatAdminResource(FAILED_LOAD_CATEGORY_KEY, FAILED_LOAD_CATEGORY_KEY, user.getBlog().getBlogAdministrationLocale(), new Object[] {blogCategoryName}));
            }

            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_CATEGORY_NAME, blogCategoryName);
            httpServletRequest.setAttribute(PAGE_PARAM, EDIT_BLOG_CATEGORY_PAGE);
        } else if (ADD_BLOG_CATEGORY_ACTION.equals(action) || UPDATE_BLOG_CATEGORY_ACTION.equals(action)) {
            boolean isUpdatingCategory = UPDATE_BLOG_CATEGORY_ACTION.equals(action);

            String blogCategoryName = BlojsomUtils.getRequestValue(BLOG_CATEGORY_NAME, httpServletRequest);
            // Check for blank or null category
            if (BlojsomUtils.checkNullOrBlank(blogCategoryName)) {
                httpServletRequest.setAttribute(PAGE_PARAM, EDIT_BLOG_CATEGORIES_PAGE);
                addOperationResultMessage(context, getAdminResource(NO_CATEGORY_SPECIFIED_KEY, NO_CATEGORY_SPECIFIED_KEY, user.getBlog().getBlogAdministrationLocale()));

                return entries;
            }

            blogCategoryName = BlojsomUtils.normalize(blogCategoryName);

            String blogCategoryParent = BlojsomUtils.getRequestValue(BLOG_CATEGORY_PARENT, httpServletRequest);
            blogCategoryParent = BlojsomUtils.normalize(blogCategoryParent);

            String blogCategoryDescription = BlojsomUtils.getRequestValue(BLOG_CATEGORY_DESCRIPTION, httpServletRequest);

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

                addOperationResultMessage(context, getAdminResource(FAILED_CATEGORY_METADATA_READ_KEY, FAILED_CATEGORY_METADATA_READ_KEY, user.getBlog().getBlogAdministrationLocale()));
            }

            BlogCategory blogCategory = _fetcher.newBlogCategory();

            if (BlojsomUtils.checkNullOrBlank(blogCategoryParent)) {
                blogCategory.setCategory(BlojsomUtils.removeInitialSlash(blogCategoryName));
            } else {
                blogCategory.setCategory(BlojsomUtils.removeSlashes(blogCategoryParent) + "/" + BlojsomUtils.removeInitialSlash(blogCategoryName));
            }

            if (!BlojsomUtils.checkNullOrBlank(blogCategoryDescription)) {
                categoryMetaData.put(NAME_KEY, blogCategoryDescription);
            }

            blogCategory.setMetaData(categoryMetaData);

            try {
                blogCategory.save(user);

                if (!isUpdatingCategory) {
                    _logger.debug("Successfully added new blog category: " + blogCategoryName);
                    addOperationResultMessage(context, formatAdminResource(CATEGORY_ADD_SUCCESS_KEY, CATEGORY_ADD_SUCCESS_KEY, user.getBlog().getBlogAdministrationLocale(), new Object[] {blogCategoryName}));

                    _blojsomConfiguration.getEventBroadcaster().broadcastEvent(new AddedBlogCategoryEvent(this, new Date(), blogCategory, user));
                } else {
                    _logger.debug("Successfully updated blog category: " + blogCategoryName);
                    addOperationResultMessage(context, formatAdminResource(CATEGORY_UPDATE_SUCCESS_KEY, CATEGORY_UPDATE_SUCCESS_KEY, user.getBlog().getBlogAdministrationLocale(), new Object[] {blogCategoryName}));

                    _blojsomConfiguration.getEventBroadcaster().broadcastEvent(new UpdatedBlogCategoryEvent(this, new Date(), blogCategory, user));
                }
            } catch (BlojsomException e) {
                _logger.error(e);

                addOperationResultMessage(context, formatAdminResource(CATEGORY_CHANGE_FAILED_KEY, CATEGORY_CHANGE_FAILED_KEY, user.getBlog().getBlogAdministrationLocale(), new Object[] {blogCategoryName}));
            }

            httpServletRequest.setAttribute(PAGE_PARAM, EDIT_BLOG_CATEGORIES_PAGE);
        }

        try {
            BlogCategory[] allCategories = _fetcher.fetchCategories(null, user);
            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_ALL_CATEGORIES, allCategories);
        } catch (BlojsomFetcherException e) {
            _logger.error(e);
        }

        return entries;
    }
}
