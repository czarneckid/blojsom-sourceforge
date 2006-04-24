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
import org.blojsom.blog.Entry;
import org.blojsom.blog.User;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.PluginException;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * EditBlogsPlugin
 *
 * @author David Czarnecki
 * @version $Id: EditBlogsPlugin.java,v 1.5 2006-04-24 12:22:37 czarneckid Exp $
 * @since blojsom 3.0
 */
public class EditBlogsPlugin extends BaseAdminPlugin {

    private Log _logger = LogFactory.getLog(EditBlogsPlugin.class);

    // Pages
    private static final String EDIT_BLOG_USERS_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blogs";

    // Localization constants
    private static final String FAILED_DELETE_BLOGS_PERMISSION_KEY = "failed.delete.blogs.permission.text";
    private static final String FAILED_REMOVE_BLOG_CONFIGURATION_KEY = "failed.remove.blog.configuration.text";
    private static final String FAILED_REMOVE_BLOG_DIRECTORY_KEY = "failed.remove.blog.directory.text";
    private static final String FAILED_REMOVE_BLOG_RESOURCES_DIRECTORY_KEY = "failed.remove.blog.resources.text";
    private static final String DELETED_BLOG_KEY = "deleted.blog.text";

    private static final String FAILED_DELETE_PROTECTED_BLOG_KEY = "failed.delete.protected.blog.text";
    private static final String FAILED_ADD_BLOGS_PERMISSION_KEY = "failed.add.blogs.permission.text";
    private static final String MISSING_WEBLOG_ID_KEY = "missing.weblog.id.text";
    private static final String WEBLOG_ID_EXISTS_KEY = "weblog.id.exists.text";
    private static final String WEBLOG_DIRECTORY_EXISTS_KEY = "weblog.directory.exists.text";
    private static final String PASSWORDS_NOT_MATCHED_KEY = "passwords.not.matched.text";
    private static final String FAILED_BOOTSTRAP_DIRECTORY_COPY_KEY = "failed.bootstrap.directory.copy.text";
    private static final String FAILED_BLOG_DIRECTORY_CREATE_KEY = "failed.blog.directory.create.text";
    private static final String ADDED_NEW_WEBLOG_KEY = "added.new.blog.text";

    private static final String BLOJSOM_PLUGIN_EDIT_BLOGS = "BLOJSOM_PLUGIN_EDIT_BLOGS";

    // Actions
    private static final String DELETE_BLOG_USER_ACTION = "delete-blog-user";
    private static final String ADD_BLOG_USER_ACTION = "add-blog-user";

    // Form elements
    private static final String BLOG_ID = "blog-id";
    private static final String BLOG_LOGIN_ID = "blog-login-id";
    private static final String BLOG_USER_EMAIL = "blog-user-email";
    private static final String BLOG_USER_NAME = "blog-user-name";
    private static final String BLOG_USER_PASSWORD = "blog-user-password";
    private static final String BLOG_USER_PASSWORD_CHECK = "blog-user-password-check";

    // Permissions
    private static final String ADD_BLOG_PERMISSION = "add_blog";
    private static final String DELETE_BLOG_PERMISSION = "delete_blog";

    private Fetcher _fetcher;
    private Map _defaultBlogProperties;
    private Map _defaultTemplateProperties;
    private Map _defaultPluginProperties;
    private Properties _blojsomProperties;
    private Map _protectedBlogs;

    /**
     * Default constructor.
     */
    public EditBlogsPlugin() {
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
     * Set the default blog properties
     *
     * @param defaultBlogProperties Default blog properties
     */
    public void setDefaultBlogProperties(Map defaultBlogProperties) {
        _defaultBlogProperties = defaultBlogProperties;
    }

    /**
     * Set the default template properties
     *
     * @param defaultTemplateProperties Default template properties
     */
    public void setDefaultTemplateProperties(Map defaultTemplateProperties) {
        _defaultTemplateProperties = defaultTemplateProperties;
    }

    /**
     * Set the default plugin properties
     *
     * @param defaultPluginProperties Default plugin properties
     */
    public void setDefaultPluginProperties(Map defaultPluginProperties) {
        _defaultPluginProperties = defaultPluginProperties;
    }

    /**
     * Set the default blojsom properties
     *
     * @param blojsomProperties Default blojsom properties
     */
    public void setBlojsomProperties(Properties blojsomProperties) {
        _blojsomProperties = blojsomProperties;
    }

    /**
     * Set the protected blogs (cannot be deleted)
     *
     * @param protectedBlogs Map of protected blogs
     */
    public void setProtectedBlogs(Map protectedBlogs) {
        _protectedBlogs = protectedBlogs;
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

        String username = getUsernameFromSession(httpServletRequest, blog);

        try {
            context.put(BLOJSOM_PLUGIN_EDIT_BLOGS, _fetcher.loadBlogIDs());
        } catch (FetcherException e) {
            _logger.error(e);
        }

        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);
        if (BlojsomUtils.checkNullOrBlank(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User did not request edit action");
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
        } else if (PAGE_ACTION.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested edit blogs page");
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_USERS_PAGE);
        } else if (DELETE_BLOG_USER_ACTION.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested delete blog action");
            }

            // Check user is allowed to delete blogs
            if (!checkPermission(blog, null, username, DELETE_BLOG_PERMISSION)) {
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_USERS_PAGE);
                addOperationResultMessage(context, getAdminResource(FAILED_DELETE_BLOGS_PERMISSION_KEY, FAILED_DELETE_BLOGS_PERMISSION_KEY, blog.getBlogAdministrationLocale()));

                return entries;
            }

            String blogID = BlojsomUtils.getRequestValue(BLOG_ID, httpServletRequest);

            if (BlojsomUtils.checkNullOrBlank(blogID)) {
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_USERS_PAGE);
                return entries;
            } else if (_protectedBlogs.containsKey(blogID)) {
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_USERS_PAGE);
                addOperationResultMessage(context, formatAdminResource(FAILED_DELETE_PROTECTED_BLOG_KEY, FAILED_DELETE_PROTECTED_BLOG_KEY, blog.getBlogAdministrationLocale(), new Object[] {blogID}));

                return entries;
            } else {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Deleting blog: " + blogID);
                }

                try {
                    Blog blogToDelete = _fetcher.loadBlog(blogID);
                    _fetcher.deleteBlog(blogToDelete);
                } catch (FetcherException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }

                    addOperationResultMessage(context, formatAdminResource(FAILED_REMOVE_BLOG_CONFIGURATION_KEY, FAILED_REMOVE_BLOG_CONFIGURATION_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogID}));

                    return entries;
                }

                String blogsDirectoryPath = _blojsomProperties.getProperty(BlojsomConstants.BLOGS_DIRECTORY_IP, BlojsomConstants.DEFAULT_BLOGS_DIRECTORY);
                String resourcesDirectoryPath = _blojsomProperties.getProperty(BlojsomConstants.RESOURCES_DIRECTORY_IP, BlojsomConstants.DEFAULT_RESOURCES_DIRECTORY);

                File blogDirectory = new File(_servletConfig.getServletContext().getRealPath(BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY) + blogsDirectoryPath + blogID + "/");
                if (!BlojsomUtils.deleteDirectory(blogDirectory)) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error("Unable to remove blog directory: " + blogDirectory.toString());
                    }

                    addOperationResultMessage(context, formatAdminResource(FAILED_REMOVE_BLOG_DIRECTORY_KEY, FAILED_REMOVE_BLOG_DIRECTORY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogID}));
                } else {
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Removed blog directory: " + blogDirectory.toString());
                    }
                }

                File blogResourcesDirectory = new File(_servletConfig.getServletContext().getRealPath("/") + resourcesDirectoryPath + blogID + "/");
                if (!BlojsomUtils.deleteDirectory(blogResourcesDirectory)) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error("Unable to remove blog resource directory: " + blogResourcesDirectory.toString());
                    }

                    addOperationResultMessage(context, formatAdminResource(FAILED_REMOVE_BLOG_RESOURCES_DIRECTORY_KEY, FAILED_REMOVE_BLOG_RESOURCES_DIRECTORY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogID}));
                } else {
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Removed blog resource directory: " + blogResourcesDirectory.toString());
                    }
                }

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Wrote new blojsom configuration after deleting blog: " + blogID);
                }

                try {
                    context.put(BLOJSOM_PLUGIN_EDIT_BLOGS, _fetcher.loadBlogIDs());
                } catch (FetcherException e) {
                    _logger.error(e);
                }

                addOperationResultMessage(context, formatAdminResource(DELETED_BLOG_KEY, DELETED_BLOG_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogID}));
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_USERS_PAGE);
        } else if (ADD_BLOG_USER_ACTION.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested add blog action");
            }

            // Check user is allowed to add blogs
            if (!checkPermission(blog, null, username, ADD_BLOG_PERMISSION)) {
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_USERS_PAGE);
                addOperationResultMessage(context, getAdminResource(FAILED_ADD_BLOGS_PERMISSION_KEY, FAILED_ADD_BLOGS_PERMISSION_KEY, blog.getBlogAdministrationLocale()));

                return entries;
            }

            String blogID = BlojsomUtils.getRequestValue(BLOG_ID, httpServletRequest);

            if (BlojsomUtils.checkNullOrBlank(blogID)) { // Check that we got a blog user ID
                addOperationResultMessage(context, getAdminResource(MISSING_WEBLOG_ID_KEY, MISSING_WEBLOG_ID_KEY, blog.getBlogAdministrationLocale()));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_USERS_PAGE);

                return entries;
            } else { // Begin the process of adding a new user
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Adding new blog id: " + blogID);
                }

                try {
                    _fetcher.loadBlog(blogID);

                    addOperationResultMessage(context, formatAdminResource(WEBLOG_ID_EXISTS_KEY, WEBLOG_ID_EXISTS_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogID}));
                    httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_USERS_PAGE);

                    return entries;
                } catch (FetcherException e) {
                }

                Blog blogToAdd = _fetcher.newBlog();
                blogToAdd.setBlogId(blogID);

                File blogDirectory = new File(_servletConfig.getServletContext().getRealPath("/") + BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + blogID);
                if (blogDirectory.exists())
                { // Make sure that the blog user ID does not conflict with a directory underneath the installation directory
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Blog directory already exists for blog ID: " + blogID);
                    }

                    addOperationResultMessage(context, formatAdminResource(WEBLOG_DIRECTORY_EXISTS_KEY, WEBLOG_DIRECTORY_EXISTS_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogID}));
                    httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_USERS_PAGE);

                    return entries;
                } else { // Otherwise, check the authorization passwords match
                    String blogUserPassword = BlojsomUtils.getRequestValue(BLOG_USER_PASSWORD, httpServletRequest);
                    String blogUserPasswordCheck = BlojsomUtils.getRequestValue(BLOG_USER_PASSWORD_CHECK, httpServletRequest);

                    // Check to see that the password and password check are equal
                    if (!blogUserPassword.equals(blogUserPasswordCheck)) {
                        if (_logger.isDebugEnabled()) {
                            _logger.debug("User password does not equal password check");
                        }

                        addOperationResultMessage(context, getAdminResource(PASSWORDS_NOT_MATCHED_KEY, PASSWORDS_NOT_MATCHED_KEY, blog.getBlogAdministrationLocale()));
                        httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_USERS_PAGE);

                        return entries;
                    } else { // And if they do, initialize the user
                        // Setup the blog
                        blogToAdd.setProperties(_defaultBlogProperties);
                        blogToAdd.setTemplates(_defaultTemplateProperties);
                        blogToAdd.setPlugins(_defaultPluginProperties);

                        BlojsomUtils.resolveDynamicBaseAndBlogURL(httpServletRequest, blogToAdd, blogID);

                        try {
                            _fetcher.saveBlog(blogToAdd);
                        } catch (FetcherException e) {
                            if (_logger.isErrorEnabled()) {
                                _logger.error(e);
                            }

                            return entries;
                        }

                        String blogLoginID = BlojsomUtils.getRequestValue(BLOG_LOGIN_ID, httpServletRequest);
                        String blogUserEmail = BlojsomUtils.getRequestValue(BLOG_USER_EMAIL, httpServletRequest);
                        String blogUserName = BlojsomUtils.getRequestValue(BLOG_USER_NAME, httpServletRequest);

                        if (!BlojsomUtils.checkNullOrBlank(blogLoginID) && !BlojsomUtils.checkNullOrBlank(blogUserEmail)) {
                            User user = _fetcher.newUser();
                            user.setBlogId(blogToAdd.getBlogId());
                            user.setUserEmail(blogUserEmail);
                            Map userMetaData = new HashMap();
                            userMetaData.put("all_permissions_permission", "true");
                            user.setMetaData(userMetaData);
                            user.setUserLogin(blogLoginID);
                            if (!BlojsomUtils.checkNullOrBlank(blogUserName)) {
                                user.setUserName(blogUserName);
                            } else {
                                user.setUserName(blogLoginID);
                            }
                            user.setUserPassword(blogUserPassword);
                            user.setUserRegistered(new Date());
                            user.setUserStatus("new");

                            try {
                                _fetcher.saveUser(blogToAdd, user);
                            } catch (FetcherException e) {
                                if (_logger.isErrorEnabled()) {
                                    _logger.error(e);
                                }
                            }

                            String blogsDirectoryPath = _blojsomProperties.getProperty(BlojsomConstants.BLOGS_DIRECTORY_IP, BlojsomConstants.DEFAULT_BLOGS_DIRECTORY);
                            String bootstrapDirectoryPath = _blojsomProperties.getProperty(BlojsomConstants.BOOTSTRAP_DIRECTORY_IP, BlojsomConstants.DEFAULT_BOOTSTRAP_DIRECTORY);
                            String templatesDirectoryPath = _blojsomProperties.getProperty(BlojsomConstants.TEMPLATES_DIRECTORY_IP, BlojsomConstants.DEFAULT_TEMPLATES_DIRECTORY);
                            String resourcesDirectoryPath = _blojsomProperties.getProperty(BlojsomConstants.RESOURCES_DIRECTORY_IP, BlojsomConstants.DEFAULT_RESOURCES_DIRECTORY);

                            File bootstrapResourcesDirectory = new File(_servletConfig.getServletContext().getRealPath(BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY) + blogsDirectoryPath + bootstrapDirectoryPath + resourcesDirectoryPath);
                            File newBlogResourcesDirectory = new File(_servletConfig.getServletContext().getRealPath(resourcesDirectoryPath) + "/" + blogID);
                            File bootstrapTemplatesDirectory = new File(_servletConfig.getServletContext().getRealPath(BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY) + blogsDirectoryPath + bootstrapDirectoryPath + templatesDirectoryPath);
                            File newBlogTemplatesDirectory = new File(_servletConfig.getServletContext().getRealPath(BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY) + blogsDirectoryPath + blogID + templatesDirectoryPath);

                            try {
                                newBlogResourcesDirectory.mkdirs();
                                BlojsomUtils.copyDirectory(bootstrapResourcesDirectory, newBlogResourcesDirectory);
                            } catch (IOException e) {
                                if (_logger.isErrorEnabled()) {
                                    _logger.error(e);
                                }

                                addOperationResultMessage(context, getAdminResource(FAILED_BOOTSTRAP_DIRECTORY_COPY_KEY, FAILED_BOOTSTRAP_DIRECTORY_COPY_KEY, blog.getBlogAdministrationLocale()));
                                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_USERS_PAGE);

                                return entries;
                            }

                            try {
                                newBlogTemplatesDirectory.mkdirs();
                                BlojsomUtils.copyDirectory(bootstrapTemplatesDirectory, newBlogTemplatesDirectory);
                            } catch (IOException e) {
                                if (_logger.isErrorEnabled()) {
                                    _logger.error(e);
                                }

                                addOperationResultMessage(context, getAdminResource(FAILED_BLOG_DIRECTORY_CREATE_KEY, FAILED_BLOG_DIRECTORY_CREATE_KEY, blog.getBlogAdministrationLocale()));
                                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_USERS_PAGE);

                                return entries;
                            }

                            try {
                                context.put(BLOJSOM_PLUGIN_EDIT_BLOGS, _fetcher.loadBlogIDs());
                            } catch (FetcherException e) {
                                _logger.error(e);
                            }
                            
                            addOperationResultMessage(context, formatAdminResource(ADDED_NEW_WEBLOG_KEY, ADDED_NEW_WEBLOG_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogID}));
                            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_USERS_PAGE);
                        } else {
                            // User login ID or user e-mail is null or blank
                        }
                    }
                }
            }
        }

        return entries;
    }
}
