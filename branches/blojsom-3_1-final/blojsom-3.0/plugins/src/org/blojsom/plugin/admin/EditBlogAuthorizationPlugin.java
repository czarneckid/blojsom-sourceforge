/**
 * Copyright (c) 2003-2007, David A. Czarnecki
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
import org.blojsom.blog.database.DatabaseUser;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.event.AuthorizationAddedEvent;
import org.blojsom.plugin.admin.event.AuthorizationDeletedEvent;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * EditBlogAuthorizationPlugin
 *
 * @author David Czarnecki
 * @version $Id: EditBlogAuthorizationPlugin.java,v 1.10 2007-01-17 02:35:04 czarneckid Exp $
 * @since blojsom 3.0
 */
public class EditBlogAuthorizationPlugin extends BaseAdminPlugin {

    private Log _logger = LogFactory.getLog(EditBlogAuthorizationPlugin.class);

    // Localization constants
    private static final String FAILED_AUTHORIZATION_PERMISSION_KEY = "failed.authorization.permission.text";
    private static final String FAILED_OTHER_AUTHORIZATION_PERMISSION_KEY = "failed.other.authorization.permission.text";
    private static final String SUCCESSFUL_AUTHORIZATION_UPDATE_KEY = "successful.authorization.update.key";
    private static final String SUCCESSFUL_AUTHORIZATION_DELETE_KEY = "successful.authorization.delete.key";
    private static final String UNSUCCESSFUL_AUTHORIZATION_UPDATE_KEY = "unsuccessful.authorization.update.key";
    private static final String UNSUCCESSFUL_AUTHORIZATION_DELETE_KEY = "unsuccessful.authorization.delete.key";
    private static final String PASSWORD_CHECK_FAILED_KEY = "password.check.failed.text";
    private static final String MISSING_PARAMETERS_KEY = "missing.parameters.text";
    private static final String MISSING_BLOG_ID_KEY = "no.blog.id.delete.text";
    private static final String USER_LOGIN_EXISTS_KEY = "user.login.exists.text";

    // Pages
    private static final String EDIT_BLOG_AUTHORIZATIONS_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blog-authorizations";
    private static final String EDIT_BLOG_AUTHORIZATION_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blog-authorization";

    // Constants
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_USERS = "BLOJSOM_PLUGIN_EDIT_BLOG_USERS";
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_USER = "BLOJSOM_PLUGIN_EDIT_BLOG_USER";
    private static final String NEW_USER_STATUS = "new";

    // Actions
    private static final String ADD_BLOG_AUTHORIZATION_ACTION = "add-blog-authorization";
    private static final String MODIFY_BLOG_AUTHORIZATION_ACTION = "modify-blog-authorization";
    private static final String DELETE_BLOG_AUTHORIZATION_ACTION = "delete-blog-authorization";
    private static final String EDIT_BLOG_AUTHORIZATION = "edit-blog-authorization";

    // Form elements
    private static final String BLOG_USER_ID = "blog-user-id";
    private static final String BLOG_LOGIN_ID = "blog-login-id";
    private static final String BLOG_USER_NAME = "blog-user-name";
    private static final String BLOG_USER_PASSWORD = "blog-user-password";
    private static final String BLOG_USER_PASSWORD_CHECK = "blog-user-password-check";
    private static final String BLOG_USER_EMAIL = "blog-user-email";
    private static final String BLOG_PERMISSIONS = "blog-permissions";

    // Permissions
    private static final String ADD_BLOG_AUTHORIZATION_PERMISSIONS_PERMISSION = "add_blog_authorization_permissions_permission";
    private static final String EDIT_BLOG_AUTHORIZATION_PERMISSION = "edit_blog_authorization_permission";
    private static final String EDIT_OTHER_USERS_AUTHORIZATION_PERMISSION = "edit_other_users_authorization_permission";

    private Fetcher _fetcher;
    private EventBroadcaster _eventBroadcaster;

    /**
     * Default constructor
     */
    public EditBlogAuthorizationPlugin() {
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
        if (!checkPermission(blog, null, username, EDIT_BLOG_AUTHORIZATION_PERMISSION)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            addOperationResultMessage(context, getAdminResource(FAILED_AUTHORIZATION_PERMISSION_KEY, FAILED_AUTHORIZATION_PERMISSION_KEY, blog.getBlogAdministrationLocale()));

            return entries;
        }

        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);
        if (BlojsomUtils.checkNullOrBlank(action)) {
            _logger.debug("User did not request edit authorization action");
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
        } else if (PAGE_ACTION.equals(action)) {
            _logger.debug("User requested edit blog authorization page");

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_AUTHORIZATIONS_PAGE);
        } else if (ADD_BLOG_AUTHORIZATION_ACTION.equals(action) || MODIFY_BLOG_AUTHORIZATION_ACTION.equals(action)) {
            if (ADD_BLOG_AUTHORIZATION_ACTION.equals(action)) {
                _logger.debug("User requested add authorization action");
            } else {
                _logger.debug("User requested modify authorization action");
            }

            String blogUserID = BlojsomUtils.getRequestValue(BLOG_USER_ID, httpServletRequest);
            String blogLoginID = BlojsomUtils.getRequestValue(BLOG_LOGIN_ID, httpServletRequest);
            String blogUserName = BlojsomUtils.getRequestValue(BLOG_USER_NAME, httpServletRequest);
            String blogUserPassword = BlojsomUtils.getRequestValue(BLOG_USER_PASSWORD, httpServletRequest);
            String blogUserPasswordCheck = BlojsomUtils.getRequestValue(BLOG_USER_PASSWORD_CHECK, httpServletRequest);
            String blogUserEmail = BlojsomUtils.getRequestValue(BLOG_USER_EMAIL, httpServletRequest);
            String blogUserPermissions = BlojsomUtils.getRequestValue(BLOG_PERMISSIONS, httpServletRequest);

            if (!BlojsomUtils.checkNullOrBlank(blogUserID)) {
                if (BlojsomUtils.checkNullOrBlank(blogUserEmail)) {
                    blogUserEmail = "";
                }

                if (!checkPermission(blog, null, username, EDIT_OTHER_USERS_AUTHORIZATION_PERMISSION)) {
                    httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_AUTHORIZATIONS_PAGE);
                    addOperationResultMessage(context, getAdminResource(FAILED_OTHER_AUTHORIZATION_PERMISSION_KEY, FAILED_OTHER_AUTHORIZATION_PERMISSION_KEY, blog.getBlogAdministrationLocale()));

                    context.put(BLOJSOM_PLUGIN_EDIT_BLOG_USERS, _fetcher.getUsers(blog));

                    return entries;
                }

                boolean modifyingPassword = true;

                if (ADD_BLOG_AUTHORIZATION_ACTION.equals(action) && (BlojsomUtils.checkNullOrBlank(blogUserPassword) || BlojsomUtils.checkNullOrBlank(blogUserPasswordCheck)))
                {
                    addOperationResultMessage(context, getAdminResource(MISSING_PARAMETERS_KEY, MISSING_PARAMETERS_KEY, blog.getBlogAdministrationLocale()));
                    _logger.debug("Missing parameters from the request to complete add/modify authorization action");

                    httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_AUTHORIZATIONS_PAGE);
                    context.put(BLOJSOM_PLUGIN_EDIT_BLOG_USERS, _fetcher.getUsers(blog));

                    return entries;
                } else if (MODIFY_BLOG_AUTHORIZATION_ACTION.equals(action) && BlojsomUtils.checkNullOrBlank(blogUserPassword) && BlojsomUtils.checkNullOrBlank(blogUserPasswordCheck)) {
                    modifyingPassword = false;
                } else if (MODIFY_BLOG_AUTHORIZATION_ACTION.equals(action) && !blogUserPassword.equals(blogUserPasswordCheck)) {
                    addOperationResultMessage(context, getAdminResource(PASSWORD_CHECK_FAILED_KEY, PASSWORD_CHECK_FAILED_KEY, blog.getBlogAdministrationLocale()));
                    _logger.debug("Password and password check not equal for add/modify authorization action");

                    httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_AUTHORIZATIONS_PAGE);
                    context.put(BLOJSOM_PLUGIN_EDIT_BLOG_USERS, _fetcher.getUsers(blog));

                    return entries;
                }

                if (ADD_BLOG_AUTHORIZATION_ACTION.equals(action) && (!blogUserPassword.equals(blogUserPasswordCheck))) {
                    addOperationResultMessage(context, getAdminResource(PASSWORD_CHECK_FAILED_KEY, PASSWORD_CHECK_FAILED_KEY, blog.getBlogAdministrationLocale()));
                    _logger.debug("Password and password check not equal for add/modify authorization action");

                    httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_AUTHORIZATIONS_PAGE);
                    context.put(BLOJSOM_PLUGIN_EDIT_BLOG_USERS, _fetcher.getUsers(blog));

                    return entries;
                }

                if (blog.getUseEncryptedPasswords().booleanValue()) {
                    blogUserPassword = BlojsomUtils.digestString(blogUserPassword, blog.getDigestAlgorithm());
                }

                String[] permissions = null;
                if (!BlojsomUtils.checkNullOrBlank(blogUserPermissions)) {
                    permissions = BlojsomUtils.parseOnlyCommaList(blogUserPermissions, true);
                }

                User user = null;
                if (ADD_BLOG_AUTHORIZATION_ACTION.equals(action)) {
                    try {
                        _fetcher.loadUser(blog, blogLoginID);

                        addOperationResultMessage(context, formatAdminResource(USER_LOGIN_EXISTS_KEY, USER_LOGIN_EXISTS_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogLoginID}));
                        httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_AUTHORIZATIONS_PAGE);
                        context.put(BLOJSOM_PLUGIN_EDIT_BLOG_USERS, _fetcher.getUsers(blog));

                        return entries;
                    } catch (FetcherException e) {
                    }

                    user = new DatabaseUser();
                    user.setBlogId(blog.getId());
                    user.setUserEmail(blogUserEmail);
                    user.setUserLogin(blogLoginID);
                    user.setUserName(blogUserName);
                    user.setUserPassword(blogUserPassword);
                    user.setUserRegistered(new Date());
                    user.setUserStatus(NEW_USER_STATUS);
                    if (permissions != null) {
                        Map userMetaData = new HashMap();
                        for (int i = 0; i < permissions.length; i++) {
                            String permission = permissions[i];
                            if (permission.endsWith(BlojsomConstants.PERMISSION_SUFFIX) && checkPermission(blog, null, username, ADD_BLOG_AUTHORIZATION_PERMISSIONS_PERMISSION))
                            {
                                userMetaData.put(permission, Boolean.TRUE.toString());
                            }
                        }

                        user.setMetaData(userMetaData);
                    }
                } else {
                    try {
                        user = _fetcher.loadUser(blog, Integer.valueOf(blogUserID));
                        user.setUserEmail(blogUserEmail);
                        if (modifyingPassword) {
                            user.setUserPassword(blogUserPassword);
                        }

                        user.setUserName(blogUserName);
                    } catch (FetcherException e) {
                        if (_logger.isErrorEnabled()) {
                            _logger.error(e);
                        }
                    }
                }

                try {
                    _fetcher.saveUser(blog, user);

                    addOperationResultMessage(context, formatAdminResource(SUCCESSFUL_AUTHORIZATION_UPDATE_KEY, SUCCESSFUL_AUTHORIZATION_UPDATE_KEY, blog.getBlogAdministrationLocale(), new Object[]{user.getUserLogin()}));
                    _eventBroadcaster.processEvent(new AuthorizationAddedEvent(this, new Date(), httpServletRequest, httpServletResponse, blog, context, user.getId()));
                } catch (FetcherException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }

                    addOperationResultMessage(context, formatAdminResource(UNSUCCESSFUL_AUTHORIZATION_UPDATE_KEY, UNSUCCESSFUL_AUTHORIZATION_UPDATE_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogLoginID}));
                }
            } else {
                addOperationResultMessage(context, getAdminResource(MISSING_PARAMETERS_KEY, MISSING_PARAMETERS_KEY, blog.getBlogAdministrationLocale()));
                _logger.debug("Missing parameters from the request to complete add/modify authorization action");
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_AUTHORIZATIONS_PAGE);
        } else if (DELETE_BLOG_AUTHORIZATION_ACTION.equals(action)) {
            _logger.debug("User requested delete authorization action");

            // Load the current authorized user's ID for checking against the incoming blog user ID
            String authorizedUserID;
            try {
                User currentAuthorizedUser = _fetcher.loadUser(blog, username);
                authorizedUserID = currentAuthorizedUser.getId().toString();

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Edit blog authorization authenticated user ID: " + authorizedUserID);
                }
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                addOperationResultMessage(context, getAdminResource(FAILED_OTHER_AUTHORIZATION_PERMISSION_KEY, FAILED_OTHER_AUTHORIZATION_PERMISSION_KEY, blog.getBlogAdministrationLocale()));
                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_USERS, _fetcher.getUsers(blog));

                return entries;
            }

            String blogUserID = BlojsomUtils.getRequestValue(BLOG_USER_ID, httpServletRequest);
            if (!BlojsomUtils.checkNullOrBlank(blogUserID)) {
                if ((authorizedUserID.equals(blogUserID)) || !checkPermission(blog, null, username, EDIT_OTHER_USERS_AUTHORIZATION_PERMISSION))
                {
                    httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_AUTHORIZATIONS_PAGE);
                    addOperationResultMessage(context, getAdminResource(FAILED_OTHER_AUTHORIZATION_PERMISSION_KEY, FAILED_OTHER_AUTHORIZATION_PERMISSION_KEY, blog.getBlogAdministrationLocale()));
                    context.put(BLOJSOM_PLUGIN_EDIT_BLOG_USERS, _fetcher.getUsers(blog));

                    return entries;
                }

                try {
                    Integer userID = Integer.valueOf(blogUserID);
                    try {
                        User user = _fetcher.loadUser(blog, userID);
                        _fetcher.deleteUser(blog, userID);

                        if (_logger.isDebugEnabled()) {
                            _logger.debug("Removed user: " + blogUserID + " from blog: " + blog.getBlogId());
                        }

                        addOperationResultMessage(context, formatAdminResource(SUCCESSFUL_AUTHORIZATION_DELETE_KEY, SUCCESSFUL_AUTHORIZATION_DELETE_KEY, blog.getBlogAdministrationLocale(), new Object[]{user.getUserLogin()}));
                        _eventBroadcaster.processEvent(new AuthorizationDeletedEvent(this, new Date(), httpServletRequest, httpServletResponse, blog, context, userID));
                    } catch (FetcherException e) {
                        addOperationResultMessage(context, formatAdminResource(UNSUCCESSFUL_AUTHORIZATION_DELETE_KEY, UNSUCCESSFUL_AUTHORIZATION_DELETE_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogUserID}));

                        if (_logger.isErrorEnabled()) {
                            _logger.error(e);
                        }
                    }
                } catch (NumberFormatException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }

                    addOperationResultMessage(context, formatAdminResource(UNSUCCESSFUL_AUTHORIZATION_DELETE_KEY, UNSUCCESSFUL_AUTHORIZATION_DELETE_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogUserID}));
                }
            } else {
                addOperationResultMessage(context, getAdminResource(MISSING_BLOG_ID_KEY, MISSING_BLOG_ID_KEY, blog.getBlogAdministrationLocale()));
                _logger.debug("No blog user id to delete from authorization");
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_AUTHORIZATIONS_PAGE);
        } else if (EDIT_BLOG_AUTHORIZATION.equals(action)) {
            _logger.debug("User requested edit authorization action");

            String userID = BlojsomUtils.getRequestValue(BLOG_USER_ID, httpServletRequest);
            if (!BlojsomUtils.checkNullOrBlank(userID)) {
                try {
                    User user = _fetcher.loadUser(blog, Integer.valueOf(userID));

                    context.put(BLOJSOM_PLUGIN_EDIT_BLOG_USER, user);
                    httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_AUTHORIZATION_PAGE);
                } catch (FetcherException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                }
            } else {
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_AUTHORIZATIONS_PAGE);
            }
        }

        context.put(BLOJSOM_PLUGIN_EDIT_BLOG_USERS, _fetcher.getUsers(blog));

        return entries;
    }
}
