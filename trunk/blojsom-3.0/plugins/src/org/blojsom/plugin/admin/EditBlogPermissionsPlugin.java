/**
 * Copyright (c) 2003-2009, David A. Czarnecki
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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Edit Blog Permissions plugin handles the adding and deleting of permissions for users of a given blog.
 *
 * @author David Czarnecki
 * @version $Id: EditBlogPermissionsPlugin.java,v 1.8 2008-07-07 19:54:12 czarneckid Exp $
 * @since blojsom 3.0
 */
public class EditBlogPermissionsPlugin extends BaseAdminPlugin {

    private Log _logger = LogFactory.getLog(EditBlogPermissionsPlugin.class);

    // Pages
    private static final String EDIT_BLOG_PERMISSIONS_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blog-permissions";

    // Constants
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_PERMISSIONS_USER_MAP = "BLOJSOM_PLUGIN_EDIT_BLOG_PERMISSIONS_USER_MAP";

    // Localization constants
    private static final String FAILED_PERMISSIONS_READ_KEY = "failed.read.permissions.text";
    private static final String FAILED_EDIT_PERMISSIONS_KEY = "failed.edit.permissions.text";
    private static final String PERMISSIONS_SAVED_KEY = "permissions.saved.text";
    private static final String ERROR_SAVING_PERMISSIONS_KEY = "error.saving.permissions.text";
    private static final String NO_PERMISSION_SPECIFIED_KEY = "no.permission.specified.text";
    private static final String NO_BLOG_USER_ID_PERMISSION_SPECIFIED_KEY = "no.blog.user.id.specified.permission.text";
    private static final String PERMISSION_DELETED_KEY = "permission.deleted.text";

    // Actions
    private static final String ADD_BLOG_PERMISSION_ACTION = "add-blog-permission";
    private static final String DELETE_BLOG_PERMISSION_ACTION = "delete-blog-permission";

    // Form elements
    private static final String BLOG_USER_ID = "blog-user-id";
    private static final String BLOG_PERMISSION = "blog-permission";

    // Permissions
    private static final String EDIT_BLOG_PERMISSIONS_PERMISSION = "edit_blog_permissions_permission";

    private Fetcher _fetcher;

    /**
     * Construct a new instance of the Edit Blog Permissions plugin
     */
    public EditBlogPermissionsPlugin() {
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
     * Read the permissions file for a given blog
     *
     * @param user User
     * @return Permissions for the given blog
     */
    protected Map readPermissionsForUser(User user) {
        Map permissions = new TreeMap();
        Iterator keyIterator = user.getMetaData().keySet().iterator();

        while (keyIterator.hasNext()) {
            String property = (String) keyIterator.next();
            if (property.endsWith(BlojsomConstants.PERMISSION_SUFFIX)) {
                permissions.put(property, user.getMetaData().get(property));
            }
        }

        return permissions;
    }

    /**
     * Add the permissions for the users in a blog to the context
     *
     * @param context Context
     * @param blog    {@link Blog}
     */
    protected void setupPermissionsInContext(Map context, Blog blog) {
        User[] users = _fetcher.getUsers(blog);
        TreeMap userIDs = new TreeMap();
        for (int i = 0; i < users.length; i++) {
            User userFromBlog = users[i];
            Map permissionsForUser = readPermissionsForUser(userFromBlog);

            userIDs.put(userFromBlog.getUserLogin(), permissionsForUser);
        }

        context.put(BLOJSOM_PLUGIN_EDIT_BLOG_PERMISSIONS_USER_MAP, Collections.unmodifiableMap(userIDs));
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
        if (!checkPermission(blog, null, username, EDIT_BLOG_PERMISSIONS_PERMISSION)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            addOperationResultMessage(context, getAdminResource(FAILED_EDIT_PERMISSIONS_KEY, FAILED_EDIT_PERMISSIONS_KEY, blog.getBlogAdministrationLocale()));

            return entries;
        }

        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);
        if (BlojsomUtils.checkNullOrBlank(action)) {
            _logger.debug("User did not request edit permission action");
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
        } else if (PAGE_ACTION.equals(action)) {
            _logger.debug("User requested edit blog permissions page");
        } else if (ADD_BLOG_PERMISSION_ACTION.equals(action)) {
            _logger.debug("User requested add permission action");

            String blogUserID = BlojsomUtils.getRequestValue(BLOG_USER_ID, httpServletRequest);
            if (!BlojsomUtils.checkNullOrBlank(blogUserID)) {
                String permissionToAdd = BlojsomUtils.getRequestValue(BLOG_PERMISSION, httpServletRequest);
                if (!BlojsomUtils.checkNullOrBlank(permissionToAdd) && (permissionToAdd.endsWith(BlojsomConstants.PERMISSION_SUFFIX))) {
                    User user;
                    try {
                        user = _fetcher.loadUser(blog, blogUserID);
                    } catch (FetcherException e) {
                        if (_logger.isErrorEnabled()) {
                            _logger.error(e);
                        }

                        httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
                        addOperationResultMessage(context, getAdminResource(FAILED_EDIT_PERMISSIONS_KEY, FAILED_EDIT_PERMISSIONS_KEY, blog.getBlogAdministrationLocale()));

                        return entries;
                    } catch (NumberFormatException e) {
                        if (_logger.isErrorEnabled()) {
                            _logger.error(e);
                        }

                        httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
                        addOperationResultMessage(context, getAdminResource(FAILED_EDIT_PERMISSIONS_KEY, FAILED_EDIT_PERMISSIONS_KEY, blog.getBlogAdministrationLocale()));

                        return entries;
                    }

                    String[] permissions = BlojsomUtils.parseOnlyCommaList(permissionToAdd, true);
                    for (int i = 0; i < permissions.length; i++) {
                        String permission = permissions[i];
                        if (permission.endsWith(BlojsomConstants.PERMISSION_SUFFIX)) {
                            user.getMetaData().put(permission, Boolean.TRUE.toString());
                        }
                    }

                    try {
                        _fetcher.saveUser(blog, user);

                        addOperationResultMessage(context, getAdminResource(PERMISSIONS_SAVED_KEY, PERMISSIONS_SAVED_KEY, blog.getBlogAdministrationLocale()));
                    } catch (FetcherException e) {
                        _logger.error(e);

                        addOperationResultMessage(context, getAdminResource(ERROR_SAVING_PERMISSIONS_KEY, ERROR_SAVING_PERMISSIONS_KEY, blog.getBlogAdministrationLocale()));
                    }
                } else {
                    addOperationResultMessage(context, getAdminResource(NO_PERMISSION_SPECIFIED_KEY, NO_PERMISSION_SPECIFIED_KEY, blog.getBlogAdministrationLocale()));
                }
            } else {
                addOperationResultMessage(context, getAdminResource(NO_BLOG_USER_ID_PERMISSION_SPECIFIED_KEY, NO_BLOG_USER_ID_PERMISSION_SPECIFIED_KEY, blog.getBlogAdministrationLocale()));
                _logger.debug("No blog user id specified");
            }
        } else if (DELETE_BLOG_PERMISSION_ACTION.equals(action)) {
            _logger.debug("User requested delete permission action");

            String blogUserID = BlojsomUtils.getRequestValue(BLOG_USER_ID, httpServletRequest);
            if (!BlojsomUtils.checkNullOrBlank(blogUserID)) {
                String permissionToDelete = BlojsomUtils.getRequestValue(BLOG_PERMISSION, httpServletRequest);
                if (!BlojsomUtils.checkNullOrBlank(permissionToDelete) && (permissionToDelete.endsWith(BlojsomConstants.PERMISSION_SUFFIX))) {
                    User user;
                    try {
                        user = _fetcher.loadUser(blog, blogUserID);
                    } catch (FetcherException e) {
                        if (_logger.isErrorEnabled()) {
                            _logger.error(e);
                        }

                        httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
                        addOperationResultMessage(context, getAdminResource(FAILED_EDIT_PERMISSIONS_KEY, FAILED_EDIT_PERMISSIONS_KEY, blog.getBlogAdministrationLocale()));

                        return entries;
                    } catch (NumberFormatException e) {
                        if (_logger.isErrorEnabled()) {
                            _logger.error(e);
                        }

                        httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
                        addOperationResultMessage(context, getAdminResource(FAILED_EDIT_PERMISSIONS_KEY, FAILED_EDIT_PERMISSIONS_KEY, blog.getBlogAdministrationLocale()));

                        return entries;
                    }

                    user.getMetaData().remove(permissionToDelete);

                    try {
                        _fetcher.saveUser(blog, user);

                        addOperationResultMessage(context, getAdminResource(PERMISSIONS_SAVED_KEY, PERMISSIONS_SAVED_KEY, blog.getBlogAdministrationLocale()));
                    } catch (FetcherException e) {
                        _logger.error(e);

                        addOperationResultMessage(context, getAdminResource(ERROR_SAVING_PERMISSIONS_KEY, ERROR_SAVING_PERMISSIONS_KEY, blog.getBlogAdministrationLocale()));
                    }
                } else {
                    addOperationResultMessage(context, getAdminResource(NO_PERMISSION_SPECIFIED_KEY, NO_PERMISSION_SPECIFIED_KEY, blog.getBlogAdministrationLocale()));
                }
            } else {
                addOperationResultMessage(context, getAdminResource(NO_BLOG_USER_ID_PERMISSION_SPECIFIED_KEY, NO_BLOG_USER_ID_PERMISSION_SPECIFIED_KEY, blog.getBlogAdministrationLocale()));
                _logger.debug("No blog user ID to delete from permissions");
            }
        }

        setupPermissionsInContext(context, blog);
        httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_PERMISSIONS_PAGE);

        return entries;
    }
}