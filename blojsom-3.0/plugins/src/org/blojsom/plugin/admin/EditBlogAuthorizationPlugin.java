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
import org.blojsom.authorization.AuthorizationException;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.blog.User;
import org.blojsom.blog.database.DatabaseUser;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.event.AuthorizationAddedEvent;
import org.blojsom.plugin.admin.event.AuthorizationDeletedEvent;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.hibernate.*;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * EditBlogAuthorizationPlugin
 *
 * @author David Czarnecki
 * @version $Id: EditBlogAuthorizationPlugin.java,v 1.1 2006-03-20 21:30:44 czarneckid Exp $
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

    // Pages
    private static final String EDIT_BLOG_AUTHORIZATION_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blog-authorization";

    // Constants
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_USERS = "BLOJSOM_PLUGIN_EDIT_BLOG_USERS";
    private static final String NEW_USER_STATUS = "new";

    // Actions
    private static final String ADD_BLOG_AUTHORIZATION_ACTION = "add-blog-authorization";
    private static final String MODIFY_BLOG_AUTHORIZATION_ACTION = "modify-blog-authorization";
    private static final String DELETE_BLOG_AUTHORIZATION_ACTION = "delete-blog-authorization";

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

    private SessionFactory _sessionFactory;
    private EventBroadcaster _eventBroadcaster;

    /**
     * Default constructor
     */
    public EditBlogAuthorizationPlugin() {
    }

    /**
     * Set the {@link SessionFactory}
     *
     * @param sessionFactory {@link SessionFactory}
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        _sessionFactory = sessionFactory;
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
     *
     * @param blog
     * @return
     */
    protected User[] getUsersForBlog(Blog blog) {
        Session session = _sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        Criteria userCriteria = session.createCriteria(org.blojsom.blog.database.DatabaseUser.class);
        userCriteria.add(Restrictions.eq("blogId", blog.getBlogId()));
        userCriteria.addOrder(Order.asc("userLogin"));

        List userList = userCriteria.list();

        tx.commit();
        session.close();

        try {
            return (DatabaseUser[]) userList.toArray(new DatabaseUser[userList.size()]);
        } catch (Exception e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            return new DatabaseUser[0];
        }
    }

    /**
     *
     * @param blog
     * @param userID
     * @throws AuthorizationException
     */
    protected void deleteUserFromBlog(Blog blog, Integer userID) throws AuthorizationException {
        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            User userToDelete = (DatabaseUser) session.load(DatabaseUser.class, userID);
            if (!userToDelete.getBlogId().equals(blog.getBlogId())) {
                tx.commit();
                session.close();

                throw new AuthorizationException("User ID: " + userID + " not from current blog: " + blog.getBlogId());
            }

            session.delete(userToDelete);

            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new AuthorizationException("Unable to delete user ID: " + userID + " from blog: " + blog.getBlogId(), e);
        }
    }

    /**
     *
     * @param blog
     * @param userID
     * @return
     * @throws AuthorizationException
     */
    protected User loadUserFromBlog(Blog blog, Integer userID) throws AuthorizationException {
        if (userID == null) {
            return new DatabaseUser();
        } else {
            try {
                Session session = _sessionFactory.openSession();
                Transaction tx = session.beginTransaction();

                User user = (DatabaseUser) session.load(DatabaseUser.class, userID);
                if (!user.getBlogId().equals(blog.getBlogId())) {
                    tx.commit();
                    session.close();

                    throw new AuthorizationException("User ID: " + userID + " not from current blog: " + blog.getBlogId());
                }

                tx.commit();
                session.close();

                return user;
            } catch (HibernateException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                throw new AuthorizationException("Unable to load user ID: " + userID + " from blog: " + blog.getBlogId(), e);
            }
        }
    }

    /**
     *
     * @param blog
     * @param user
     * @return
     * @throws AuthorizationException
     */
    protected User saveUserToBlog(Blog blog, User user) throws AuthorizationException {
        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            if (user.getId() == null) {
                Criteria userCriteria = session.createCriteria(org.blojsom.blog.database.DatabaseUser.class);
                userCriteria.add(Restrictions.eq("userLogin", user.getUserLogin()));
                userCriteria.add(Restrictions.eq("blogId", blog.getBlogId()));

                if (userCriteria.uniqueResult() != null) {
                    tx.commit();
                    session.close();

                    throw new AuthorizationException("User login already exists: " + user.getUserLogin());
                }
            }

            session.saveOrUpdate(user);

            tx.commit();
            session.close();

            return user;
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new AuthorizationException("Unable to save user login: " + user.getUserLogin() + " to blog: " + blog.getBlogId(), e);
        }
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

            if (!BlojsomUtils.checkNullOrBlank(blogUserID) && !BlojsomUtils.checkNullOrBlank(blogUserPassword) && !BlojsomUtils.checkNullOrBlank(blogUserPasswordCheck))
            {
                if (blogUserPassword.equals(blogUserPasswordCheck)) {
                    if (BlojsomUtils.checkNullOrBlank(blogUserEmail)) {
                        blogUserEmail = "";
                    }

                    if ((!username.equals(blogUserID)) && !checkPermission(blog, null, username, EDIT_OTHER_USERS_AUTHORIZATION_PERMISSION))
                    {
                        httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_AUTHORIZATION_PAGE);
                        addOperationResultMessage(context, getAdminResource(FAILED_OTHER_AUTHORIZATION_PERMISSION_KEY, FAILED_OTHER_AUTHORIZATION_PERMISSION_KEY, blog.getBlogAdministrationLocale()));

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
                        user = new DatabaseUser();
                        user.setBlogId(blog.getBlogId());
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
                                if (permission.endsWith("_permission") && checkPermission(blog, null, username, ADD_BLOG_AUTHORIZATION_PERMISSIONS_PERMISSION)) {
                                    userMetaData.put(permission, Boolean.TRUE.toString());
                                }
                            }

                            user.setMetaData(userMetaData);
                        }
                    } else {
                        try {
                            user = loadUserFromBlog(blog, Integer.valueOf(blogUserID));
                            user.setUserEmail(blogUserEmail);
                            user.setUserPassword(blogUserPassword);
                        } catch (AuthorizationException e) {
                            if (_logger.isErrorEnabled()) {
                                _logger.error(e);
                            }
                        }
                    }

                    try {
                        saveUserToBlog(blog, user);

                        addOperationResultMessage(context, formatAdminResource(SUCCESSFUL_AUTHORIZATION_UPDATE_KEY, SUCCESSFUL_AUTHORIZATION_UPDATE_KEY, blog.getBlogAdministrationLocale(), new Object[]{user.getUserLogin()}));
                        _eventBroadcaster.processEvent(new AuthorizationAddedEvent(this, new Date(), httpServletRequest, httpServletResponse, blog, context, user.getId()));
                    } catch (AuthorizationException e) {
                        if (_logger.isErrorEnabled()) {
                            _logger.error(e);
                        }

                        addOperationResultMessage(context, formatAdminResource(UNSUCCESSFUL_AUTHORIZATION_UPDATE_KEY, UNSUCCESSFUL_AUTHORIZATION_UPDATE_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogLoginID}));
                    }
                } else {
                    addOperationResultMessage(context, getAdminResource(PASSWORD_CHECK_FAILED_KEY, PASSWORD_CHECK_FAILED_KEY, blog.getBlogAdministrationLocale()));
                    _logger.debug("Password and password check not equal for add/modify authorization action");
                }
            } else {
                addOperationResultMessage(context, getAdminResource(MISSING_PARAMETERS_KEY, MISSING_PARAMETERS_KEY, blog.getBlogAdministrationLocale()));
                _logger.debug("Missing parameters from the request to complete add/modify authorization action");
            }
        } else if (DELETE_BLOG_AUTHORIZATION_ACTION.equals(action)) {
            _logger.debug("User requested delete authorization action");

            String blogUserID = BlojsomUtils.getRequestValue(BLOG_USER_ID, httpServletRequest);
            if (!BlojsomUtils.checkNullOrBlank(blogUserID)) {
                if ((!username.equals(blogUserID)) && !checkPermission(blog, null, username, EDIT_OTHER_USERS_AUTHORIZATION_PERMISSION))
                {
                    httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_AUTHORIZATION_PAGE);
                    addOperationResultMessage(context, getAdminResource(FAILED_OTHER_AUTHORIZATION_PERMISSION_KEY, FAILED_OTHER_AUTHORIZATION_PERMISSION_KEY, blog.getBlogAdministrationLocale()));

                    return entries;
                }

                try {
                    Integer userID = Integer.valueOf(blogUserID);
                    try {
                        deleteUserFromBlog(blog, userID);

                        if (_logger.isDebugEnabled()) {
                            _logger.debug("Removed user: " + blogUserID + " from blog: " + blog.getBlogId());
                        }

                        addOperationResultMessage(context, formatAdminResource(SUCCESSFUL_AUTHORIZATION_DELETE_KEY, SUCCESSFUL_AUTHORIZATION_DELETE_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogUserID}));
                        _eventBroadcaster.processEvent(new AuthorizationDeletedEvent(this, new Date(), httpServletRequest, httpServletResponse, blog, context, userID));
                    } catch (AuthorizationException e) {
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
        }

        context.put(BLOJSOM_PLUGIN_EDIT_BLOG_USERS, getUsersForBlog(blog));
        httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_AUTHORIZATION_PAGE);

        return entries;
    }
}
