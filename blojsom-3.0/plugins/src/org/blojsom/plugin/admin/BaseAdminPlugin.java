/**
 * Copyright (c) 2003-2008, David A. Czarnecki
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
import org.blojsom.authorization.AuthorizationProvider;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.permission.PermissionChecker;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.resources.ResourceManager;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * BaseAdminPlugin
 *
 * @author David Czarnecki
 * @version $Id: BaseAdminPlugin.java,v 1.5 2008-07-07 19:54:12 czarneckid Exp $
 * @since blojsom 3.0
 */
public class BaseAdminPlugin implements Plugin, PermissionedPlugin {

    protected static final Log _logger = LogFactory.getLog(BaseAdminPlugin.class);

    // Constants
    protected static final String BLOJSOM_ADMIN_PLUGIN_AUTHENTICATED_KEY = "org.blojsom.plugin.admin.Authenticated";
    protected static final String BLOJSOM_ADMIN_PLUGIN_USERNAME_KEY = "org.blojsom.plugin.admin.Username";
    protected static final String BLOJSOM_ADMIN_PLUGIN_USERNAME = "BLOJSOM_ADMIN_PLUGIN_USERNAME";
    protected static final String BLOJSOM_ADMIN_PLUGIN_USERNAME_PARAM = "username";
    protected static final String BLOJSOM_ADMIN_PLUGIN_PASSWORD_PARAM = "password";
    protected static final String ACTION_PARAM = "action";
    protected static final String SUBACTION_PARAM = "subaction";
    protected static final String BLOJSOM_ADMIN_PLUGIN_OPERATION_RESULT = "BLOJSOM_ADMIN_PLUGIN_OPERATION_RESULT";
    protected static final String BLOJSOM_USER_AUTHENTICATED = "BLOJSOM_USER_AUTHENTICATED";
    protected static final String BLOJSOM_ADMIN_MESSAGES_RESOURCE = "org.blojsom.plugin.admin.resources.messages";
    protected static final String BLOJSOM_PERMISSION_CHECKER = "BLOJSOM_PERMISSION_CHECKER";
    protected static final String PLUGIN_ADMIN_INHERIT_APACHE_CREDENTIALS = "plugin-admin-inherit-apache-credentials";

    // Localization constants
    protected static final String LOGIN_ERROR_TEXT_KEY = "login.error.text";

    // Pages
    protected static final String ADMIN_ADMINISTRATION_PAGE = "/org/blojsom/plugin/admin/templates/admin";
    protected static final String ADMIN_LOGIN_PAGE = "/org/blojsom/plugin/admin/templates/admin-login";
    protected static final String ADMIN_AJAX_RESPONSE = "/org/blojsom/plugin/admin/templates/admin-ajax-response";

    // Actions
    protected static final String LOGIN_ACTION = "login";
    protected static final String LOGOUT_ACTION = "logout";
    protected static final String PAGE_ACTION = "page";

    protected ServletConfig _servletConfig;
    protected AuthorizationProvider _authorizationProvider;
    protected ResourceManager _resourceManager;
    protected Map _ignoreParams;

    /**
     * Default constructor.
     */
    public BaseAdminPlugin() {
    }

    /**
     * Set the {@link ServletConfig} for the fetcher to grab initialization parameters
     *
     * @param servletConfig {@link ServletConfig}
     */
    public void setServletConfig(ServletConfig servletConfig) {
        _servletConfig = servletConfig;
    }

    /**
     * Set the authorization provider for use by this plugin
     *
     * @param authorizationProvider {@link AuthorizationProvider}
     */
    public void setAuthorizationProvider(AuthorizationProvider authorizationProvider) {
        _authorizationProvider = authorizationProvider;
    }

    /**
     * Set the resource manager for use by this plugin
     *
     * @param resourceManager {@link ResourceManager}
     */
    public void setResourceManager(ResourceManager resourceManager) {
        _resourceManager = resourceManager;
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws PluginException If there is an error initializing the plugin
     */
    public void init() throws PluginException {
        _ignoreParams = new HashMap();
        _ignoreParams.put(BLOJSOM_ADMIN_PLUGIN_USERNAME_PARAM, BLOJSOM_ADMIN_PLUGIN_USERNAME_PARAM);
        _ignoreParams.put(BLOJSOM_ADMIN_PLUGIN_PASSWORD_PARAM, BLOJSOM_ADMIN_PLUGIN_PASSWORD_PARAM);
        _ignoreParams.put("submit", "submit");
        _ignoreParams.put("reset", "reset");
    }

    /**
     * Authenticate the user if their authentication session variable is not present
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param context             Context
     * @param blog                {@link Blog} information
     * @return <code>true</code> if the user is authenticated, <code>false</code> otherwise
     */
    protected boolean authenticateUser(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map context, Blog blog) {
        BlojsomUtils.setNoCacheControlHeaders(httpServletResponse);
        HttpSession httpSession = httpServletRequest.getSession();
        boolean logout = false;

        // Check first to see if someone has requested to logout
        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);
        if (action != null && LOGOUT_ACTION.equals(action)) {
            httpSession.removeAttribute(blog.getBlogAdminURL() + "_" + BLOJSOM_ADMIN_PLUGIN_AUTHENTICATED_KEY);
            httpSession.removeAttribute(BLOJSOM_USER_AUTHENTICATED);
            httpSession.removeAttribute(BlojsomConstants.REDIRECT_TO_PARAM);
            logout = true;
        }

        StringBuffer redirectURL = new StringBuffer();
        redirectURL.append(httpServletRequest.getRequestURI());
        if (!redirectURL.toString().endsWith("/")) {
            redirectURL.append("/");
        }
        if (httpServletRequest.getParameterMap().size() > 0) {
            redirectURL.append("?");
            redirectURL.append(BlojsomUtils.convertRequestParams(httpServletRequest, _ignoreParams));
        }

        if (Boolean.valueOf(blog.getProperty(PLUGIN_ADMIN_INHERIT_APACHE_CREDENTIALS)).booleanValue() && !BlojsomUtils.checkNullOrBlank(httpServletRequest.getRemoteUser())) {
            String remoteUsername = httpServletRequest.getRemoteUser();
            _logger.debug("Retrieved remote_user from server: " + remoteUsername);

            httpSession.setAttribute(blog.getBlogAdminURL() + "_" + BLOJSOM_ADMIN_PLUGIN_AUTHENTICATED_KEY, Boolean.TRUE);
            httpSession.setAttribute(blog.getBlogAdminURL() + "_" + BLOJSOM_ADMIN_PLUGIN_USERNAME_KEY, remoteUsername);
            httpSession.setAttribute(BLOJSOM_ADMIN_PLUGIN_USERNAME, remoteUsername);
            httpSession.setAttribute(BLOJSOM_USER_AUTHENTICATED, Boolean.TRUE);
            _logger.debug("Passed remote_user authentication for username: " + remoteUsername);
        }

        // Otherwise, check for the authenticated key and if not authenticated, look for a "username" and "password" parameter
        if (httpSession.getAttribute(blog.getBlogAdminURL() + "_" + BLOJSOM_ADMIN_PLUGIN_AUTHENTICATED_KEY) == null) {
            String username = httpServletRequest.getParameter(BLOJSOM_ADMIN_PLUGIN_USERNAME_PARAM);
            String password = httpServletRequest.getParameter(BLOJSOM_ADMIN_PLUGIN_PASSWORD_PARAM);

            if (username == null || password == null || "".equals(username) || "".equals(password)) {
                _logger.debug("No username/password provided or username/password was empty");
                _logger.debug("Setting redirect_to attribute to: " + redirectURL.toString());
                if (!logout) {
                    httpServletRequest.getSession().setAttribute(BlojsomConstants.REDIRECT_TO_PARAM, redirectURL.toString());
                }

                return false;
            }

            // Check the username and password against the blog authorization
            try {
                _authorizationProvider.authorize(blog, null, username, password);
                httpSession.setAttribute(blog.getBlogAdminURL() + "_" + BLOJSOM_ADMIN_PLUGIN_AUTHENTICATED_KEY, Boolean.TRUE);
                httpSession.setAttribute(blog.getBlogAdminURL() + "_" + BLOJSOM_ADMIN_PLUGIN_USERNAME_KEY, username);
                httpSession.setAttribute(BLOJSOM_ADMIN_PLUGIN_USERNAME, username);
                httpSession.setAttribute(BLOJSOM_USER_AUTHENTICATED, Boolean.TRUE);
                _logger.debug("Passed authentication for username: " + username);

                return true;
            } catch (AuthorizationException e) {
                _logger.debug("Failed authentication for username: " + username);
                addOperationResultMessage(context, formatAdminResource(LOGIN_ERROR_TEXT_KEY, LOGIN_ERROR_TEXT_KEY, blog.getBlogAdministrationLocale(), new Object[]{username}));
                _logger.debug("Setting redirect_to attribute to: " + redirectURL.toString());
                if (!logout) {
                    httpServletRequest.getSession().setAttribute(BlojsomConstants.REDIRECT_TO_PARAM, redirectURL.toString());
                }

                return false;
            }
        } else {
            context.put(BLOJSOM_PERMISSION_CHECKER, new PermissionChecker(blog, _authorizationProvider, context));

            return ((Boolean) httpSession.getAttribute(blog.getBlogAdminURL() + "_" + BLOJSOM_ADMIN_PLUGIN_AUTHENTICATED_KEY)).booleanValue();
        }
    }

    /**
     * Retrieve the current authorized username for this session
     *
     * @param httpServletRequest Request
     * @param blog               {@link Blog}
     * @return Authorized username for this session or <code>null</code> if no user is currently authorized
     */
    protected String getUsernameFromSession(HttpServletRequest httpServletRequest, Blog blog) {
        return (String) httpServletRequest.getSession().getAttribute(blog.getBlogAdminURL() + "_" + BLOJSOM_ADMIN_PLUGIN_USERNAME_KEY);
    }

    /**
     * Check the permission for a given username and permission
     *
     * @param blog              {@link Blog} information
     * @param permissionContext {@link java.util.Map} containing context information for checking permission
     * @param username          Username
     * @param permission        Permission
     * @return <code>true</code> if the username has the required permission, <code>false</code> otherwise
     */
    public boolean checkPermission(Blog blog, Map permissionContext, String username, String permission) {
        try {
            _authorizationProvider.checkPermission(blog, permissionContext, username, permission);
        } catch (AuthorizationException e) {
            _logger.error(e);
            return false;
        }

        return true;
    }

    /**
     * Adds a message to the context under the <code>BLOJSOM_ADMIN_PLUGIN_OPERATION_RESULT</code> key
     *
     * @param context Context
     * @param message Message to add
     */
    protected void addOperationResultMessage(Map context, String message) {
        context.put(BLOJSOM_ADMIN_PLUGIN_OPERATION_RESULT, message);
    }

    /**
     * Retrieve a resource from the administration resource bundle
     *
     * @param resourceID   ID of resource to retrieve
     * @param fallbackText Text to use as fallback if resource ID is not found
     * @param locale       {@link Locale} to use when retrieving resource
     * @return Text from administration resource bundle given by <code>resourceID</code> or <code>fallbackText</code> if the resource ID is not found
     */
    protected String getAdminResource(String resourceID, String fallbackText, Locale locale) {
        return _resourceManager.getString(resourceID, BLOJSOM_ADMIN_MESSAGES_RESOURCE, fallbackText, locale);
    }

    /**
     * Retrieve a resource from the administration resource bundle and pass it through the {@link ResourceManager#format(String, Object[])} method
     *
     * @param resourceID   ID of resource to retrieve
     * @param fallbackText Text to use as fallback if resource ID is not found
     * @param locale       {@link Locale} to use when retrieving resource
     * @param arguments    Arguments for {@link ResourceManager#format(String, Object[])}
     * @return Text from administration resource bundle given by <code>resourceID</code> formatted appropriately or <code>fallbackText</code> if the resource ID could not be formatted
     */
    protected String formatAdminResource(String resourceID, String fallbackText, Locale locale, Object[] arguments) {
        String resourceText = getAdminResource(resourceID, fallbackText, locale);

        String formattedText = _resourceManager.format(resourceText, arguments);
        if (formattedText == null) {
            formattedText = fallbackText;
        }

        return formattedText;
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
        } else {
            String page = BlojsomUtils.getRequestValue(BlojsomConstants.PAGE_PARAM, httpServletRequest);
            if (!BlojsomUtils.checkNullOrBlank(page)) {
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, page);
            } else {
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            }

            if (httpServletRequest.getSession().getAttribute(BlojsomConstants.REDIRECT_TO_PARAM) != null) {
                String redirectURL = (String) httpServletRequest.getSession().getAttribute(BlojsomConstants.REDIRECT_TO_PARAM);

                try {
                    httpServletRequest.getSession().removeAttribute(BlojsomConstants.REDIRECT_TO_PARAM);
                    httpServletResponse.sendRedirect(redirectURL);
                } catch (IOException e) {
                    _logger.error(e);
                }
            }
        }

        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws PluginException If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws PluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws PluginException If there is an error in finalizing this plugin
     */
    public void destroy() throws PluginException {
    }
}
