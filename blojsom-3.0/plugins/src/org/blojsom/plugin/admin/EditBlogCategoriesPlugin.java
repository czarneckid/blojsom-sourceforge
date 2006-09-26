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
import org.blojsom.blog.Blog;
import org.blojsom.blog.Category;
import org.blojsom.blog.Entry;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.event.CategoryAddedEvent;
import org.blojsom.plugin.admin.event.CategoryDeletedEvent;
import org.blojsom.plugin.admin.event.CategoryUpdatedEvent;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.BlojsomConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * EditBlogCategoriesPlugin
 *
 * @author David Czarnecki
 * @version $Id: EditBlogCategoriesPlugin.java,v 1.4 2006-09-26 02:55:20 czarneckid Exp $
 * @since blojsom 3.0
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
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_CATEGORY = "BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_CATEGORY";
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_CATEGORY_METADATA = "BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_CATEGORY_METADATA";
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_ALL_CATEGORIES = "BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_ALL_CATEGORIES";

    // Actions
    private static final String ADD_BLOG_CATEGORY_ACTION = "add-blog-category";
    private static final String DELETE_BLOG_CATEGORY_ACTION = "delete-blog-category";
    private static final String EDIT_BLOG_CATEGORY_ACTION = "edit-blog-category";
    private static final String UPDATE_BLOG_CATEGORY_ACTION = "update-blog-category";

    // Form elements
    private static final String BLOG_CATEGORY_ID = "blog-category-id";
    private static final String BLOG_CATEGORY_PARENT_ID = "blog-category-parent-id";
    private static final String BLOG_CATEGORY_NAME = "blog-category-name";
    private static final String BLOG_CATEGORY_DESCRIPTION = "blog-category-description";
    private static final String BLOG_CATEGORY_META_DATA = "blog-category-meta-data";

    private static final String EDIT_BLOG_CATEGORIES_PERMISSION = "edit_blog_categories_permission";

    private Fetcher _fetcher;
    private EventBroadcaster _eventBroadcaster;

    /**
     * Default constructor.
     */
    public EditBlogCategoriesPlugin() {
    }

    /**
     * Set the {@link Fetcher}
     *
     * @param fetcher {@link Fetcher}
     */
    public void setFetcher(Fetcher fetcher) {
        _fetcher = fetcher;
    }

    /**
     * Set the {@link EventBroadcaster}
     *
     * @param eventBroadcaster {@link EventBroadcaster}
     */
    public void setEventBroadcaster(EventBroadcaster eventBroadcaster) {
        _eventBroadcaster = eventBroadcaster;
    }

    /**
     * Get the display name for a category prefering the description over the name
     *
     * @param category {@link Category}
     * @return Display name (Description if available, otherwise Name)
     */
    protected String getDisplayName(Category category) {
        if (!BlojsomUtils.checkNullOrBlank(category.getDescription())) {
            return category.getDescription();
        }

        return category.getName();
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param blog                {@link Blog} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws PluginException If there is an error processing the blog entries
     */
    public Entry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, Map context, Entry[] entries) throws PluginException {
        if (!authenticateUser(httpServletRequest, httpServletResponse, context, blog)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_LOGIN_PAGE);

            return entries;
        }

        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);
        try {
            Category[] allCategories = _fetcher.loadAllCategories(blog);
            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_ALL_CATEGORIES, allCategories);
        } catch (FetcherException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }
        }

        String username = getUsernameFromSession(httpServletRequest, blog);
        if (!checkPermission(blog, null, username, EDIT_BLOG_CATEGORIES_PERMISSION)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            addOperationResultMessage(context, getAdminResource(FAILED_PERMISSION_KEY, FAILED_PERMISSION_KEY, blog.getBlogAdministrationLocale()));

            return entries;
        }

        if (BlojsomUtils.checkNullOrBlank(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User did not request edit action");
            }
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
        } else if (PAGE_ACTION.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested edit categories page");
            }
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_CATEGORIES_PAGE);
        } else if (DELETE_BLOG_CATEGORY_ACTION.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User request blog category delete action");
            }
            String blogCategoryId = BlojsomUtils.getRequestValue(BLOG_CATEGORY_ID, httpServletRequest);
            if (_logger.isDebugEnabled()) {
                _logger.debug("Delting blog category: " + blogCategoryId);
            }

            try {
                Integer categoryID = Integer.valueOf(blogCategoryId);
                Category blogCategoryToDelete = _fetcher.newCategory();
                blogCategoryToDelete.setId(categoryID);
                _fetcher.loadCategory(blog, blogCategoryToDelete);
                _fetcher.deleteCategory(blog, blogCategoryToDelete);

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Deleted blog category: " + blogCategoryId);
                }
                addOperationResultMessage(context, formatAdminResource(DELETED_CATEGORY_KEY, DELETED_CATEGORY_KEY, blog.getBlogAdministrationLocale(), new Object[]{getDisplayName(blogCategoryToDelete)}));

                _eventBroadcaster.broadcastEvent(new CategoryDeletedEvent(this, new Date(), blogCategoryToDelete, blog));
            } catch (NumberFormatException e) {
                addOperationResultMessage(context, formatAdminResource(FAILED_DELETED_CATEGORY_KEY, FAILED_DELETED_CATEGORY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogCategoryId}));
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                addOperationResultMessage(context, formatAdminResource(FAILED_DELETED_CATEGORY_KEY, FAILED_DELETED_CATEGORY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogCategoryId}));
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_CATEGORIES_PAGE);
        } else if (EDIT_BLOG_CATEGORY_ACTION.equals(action)) {
            String blogCategoryId = BlojsomUtils.getRequestValue(BLOG_CATEGORY_ID, httpServletRequest);
            if (_logger.isDebugEnabled()) {
                _logger.debug("Editing blog category: " + blogCategoryId);
            }
            Category blogCategoryToEdit;

            try {
                Integer categoryID = Integer.valueOf(blogCategoryId);
                blogCategoryToEdit = _fetcher.newCategory();
                blogCategoryToEdit.setId(categoryID);
                _fetcher.loadCategory(blog, blogCategoryToEdit);

                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_CATEGORY, blogCategoryToEdit);

                StringBuffer categoryPropertiesString = new StringBuffer();
                Iterator keyIterator = blogCategoryToEdit.getMetaData().keySet().iterator();
                Object key;
                while (keyIterator.hasNext()) {
                    key = keyIterator.next();
                    categoryPropertiesString.append(key.toString()).append("=").append(blogCategoryToEdit.getMetaData().get(key)).append("\r\n");
                }

                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_CATEGORY_METADATA, categoryPropertiesString.toString());
            } catch (NumberFormatException e) {
                addOperationResultMessage(context, formatAdminResource(FAILED_LOAD_CATEGORY_KEY, FAILED_LOAD_CATEGORY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogCategoryId}));
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                addOperationResultMessage(context, formatAdminResource(FAILED_LOAD_CATEGORY_KEY, FAILED_LOAD_CATEGORY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogCategoryId}));
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_CATEGORY_PAGE);
        } else if (ADD_BLOG_CATEGORY_ACTION.equals(action) || UPDATE_BLOG_CATEGORY_ACTION.equals(action)) {
            boolean isUpdatingCategory = UPDATE_BLOG_CATEGORY_ACTION.equals(action);

            String blogCategoryName = BlojsomUtils.getRequestValue(BLOG_CATEGORY_NAME, httpServletRequest);
            // Check for blank or null category
            if (BlojsomUtils.checkNullOrBlank(blogCategoryName)) {
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_CATEGORIES_PAGE);
                addOperationResultMessage(context, getAdminResource(NO_CATEGORY_SPECIFIED_KEY, NO_CATEGORY_SPECIFIED_KEY, blog.getBlogAdministrationLocale()));

                return entries;
            }

            blogCategoryName = BlojsomUtils.normalize(blogCategoryName);
            blogCategoryName = BlojsomUtils.addSlashes(blogCategoryName);

            String blogCategoryId = BlojsomUtils.getRequestValue(BLOG_CATEGORY_ID, httpServletRequest);
            String blogCategoryParentId = BlojsomUtils.getRequestValue(BLOG_CATEGORY_PARENT_ID, httpServletRequest);
            String blogCategoryDescription = BlojsomUtils.getRequestValue(BLOG_CATEGORY_DESCRIPTION, httpServletRequest);

            if (!isUpdatingCategory) {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Adding blog category: " + blogCategoryName);
                }
            } else {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Updating blog category: " + blogCategoryName);
                }
            }

            String blogCategoryMetaData = BlojsomUtils.getRequestValue(BLOG_CATEGORY_META_DATA, httpServletRequest);
            if (blogCategoryMetaData == null) {
                blogCategoryMetaData = "";
            }

            if (!isUpdatingCategory) {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Adding blog category meta-data: " + blogCategoryMetaData);
                }
            }

            // Separate the blog category meta-data into key/value pairs
            BufferedReader br = new BufferedReader(new StringReader(blogCategoryMetaData));
            String input;
            String[] splitInput;
            Map categoryMetaData = new HashMap();
            try {
                while ((input = br.readLine()) != null) {
                    splitInput = input.split("=");
                    if (splitInput.length == 2) {
                        categoryMetaData.put(splitInput[0], splitInput[1]);
                    }
                }
            } catch (IOException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                addOperationResultMessage(context, getAdminResource(FAILED_CATEGORY_METADATA_READ_KEY, FAILED_CATEGORY_METADATA_READ_KEY, blog.getBlogAdministrationLocale()));
            }

            Integer parentCategoryID = new Integer(0);
            try {
                parentCategoryID = Integer.valueOf(blogCategoryParentId);
            } catch (NumberFormatException e) {
            }

            Category blogCategory;
            if (!isUpdatingCategory) {
                blogCategory = _fetcher.newCategory();
            } else {
                try {
                    blogCategory = _fetcher.newCategory();
                    Integer categoryID = Integer.valueOf(blogCategoryId);
                    blogCategory.setId(categoryID);

                    _fetcher.loadCategory(blog, blogCategory);
                } catch (NumberFormatException e) {
                    addOperationResultMessage(context, formatAdminResource(CATEGORY_CHANGE_FAILED_KEY, CATEGORY_CHANGE_FAILED_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogCategoryId}));
                    httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_CATEGORIES_PAGE);

                    return entries;
                } catch (FetcherException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }

                    addOperationResultMessage(context, formatAdminResource(CATEGORY_CHANGE_FAILED_KEY, CATEGORY_CHANGE_FAILED_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogCategoryId}));
                    httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_CATEGORIES_PAGE);

                    return entries;
                }
            }

            if (!BlojsomUtils.checkNullOrBlank(blogCategoryParentId)) {
                blogCategory.setParentCategoryId(parentCategoryID);
            } else {
                blogCategory.setParentCategoryId(null);
            }

            blogCategory.setName(blogCategoryName);
            blogCategory.setDescription(blogCategoryDescription);
            blogCategory.setBlogId(blog.getId());
            blogCategory.setMetaData(categoryMetaData);

            try {
                _fetcher.saveCategory(blog, blogCategory);

                if (!isUpdatingCategory) {
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Successfully added new blog category: " + blogCategoryName);
                    }

                    addOperationResultMessage(context, formatAdminResource(CATEGORY_ADD_SUCCESS_KEY, CATEGORY_ADD_SUCCESS_KEY, blog.getBlogAdministrationLocale(), new Object[]{getDisplayName(blogCategory)}));

                    _eventBroadcaster.broadcastEvent(new CategoryAddedEvent(this, new Date(), blogCategory, blog));
                } else {
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Successfully updated blog category: " + blogCategoryName);
                    }

                    addOperationResultMessage(context, formatAdminResource(CATEGORY_UPDATE_SUCCESS_KEY, CATEGORY_UPDATE_SUCCESS_KEY, blog.getBlogAdministrationLocale(), new Object[]{getDisplayName(blogCategory)}));

                    _eventBroadcaster.broadcastEvent(new CategoryUpdatedEvent(this, new Date(), blogCategory, blog));
                }
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                addOperationResultMessage(context, formatAdminResource(CATEGORY_CHANGE_FAILED_KEY, CATEGORY_CHANGE_FAILED_KEY, blog.getBlogAdministrationLocale(), new Object[]{getDisplayName(blogCategory)}));
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_CATEGORIES_PAGE);
        }

        try {
            Category[] allCategories = _fetcher.loadAllCategories(blog);
            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_CATEGORIES_ALL_CATEGORIES, allCategories);
        } catch (FetcherException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }
        }

        return entries;
    }
}
