/**
 * Copyright (c) 2003, David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003 by Mark Lussier
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the "David A. Czarnecki" and "blojsom" nor the names of
 * its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Products derived from this software may not be called "blojsom",
 * nor may "blojsom" appear in their name, without prior written permission of
 * David A. Czarnecki.
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
import org.blojsom.blog.BlojsomConfiguration;
import org.blojsom.plugin.BlojsomPlugin;
import org.blojsom.plugin.BlojsomPluginException;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * BaseAdminPlugin
 *
 * @author David Czarnecki
 * @since blojsom 2.04
 * @version $Id: BaseAdminPlugin.java,v 1.1 2003-10-21 03:34:33 czarneckid Exp $
 */
public abstract class BaseAdminPlugin implements BlojsomPlugin, BlojsomConstants {

    protected static final Log _logger = LogFactory.getLog(BaseAdminPlugin.class);

    protected static final String BLOJSOM_ADMIN_PLUGIN_AUTHENTICATED_KEY = "org.blojsom.plugin.admin.Authenticated";
    protected static final String BLOJSOM_ADMIN_PLUGIN_USERNAME_PARAM = "username";
    protected static final String BLOJSOM_ADMIN_PLUGIN_PASSWORD_PARAM = "password";
    protected static final String ACTION_PARAM = "action";
    protected static final String ADMIN_ADMINISTRATION_PAGE = "/org/blojsom/plugin/admin/admin";
    protected static final String ADMIN_LOGIN_PAGE = "/org/blojsom/plugin/admin/admin-login";
    protected static final String LOGIN_ACTION = "login";
    protected static final String LOGOUT_ACTION = "logout";
    protected static final String PAGE_ACTION = "page";

    protected BlojsomConfiguration _blojsomConfiguration;

    /**
     *
     */
    public BaseAdminPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration {@link BlojsomConfiguration} information
     * @throws org.blojsom.plugin.BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws BlojsomPluginException {
        _blojsomConfiguration = blojsomConfiguration;
    }

    /**
     *
     * @param httpServletRequest
     * @param blog
     * @return
     */
    protected boolean authenticateUser(HttpServletRequest httpServletRequest, Blog blog) {
        HttpSession httpSession = httpServletRequest.getSession();

        // Check first to see if someone has requested to logout
        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);
        if (action != null && LOGOUT_ACTION.equals(action)) {
            httpSession.removeAttribute(BLOJSOM_ADMIN_PLUGIN_AUTHENTICATED_KEY);
        }

        // Otherwise, check for the authenticated key and if not authenticated, look for a "username" and "password" parameter
        if (httpSession.getAttribute(BLOJSOM_ADMIN_PLUGIN_AUTHENTICATED_KEY) == null) {
            String username = httpServletRequest.getParameter(BLOJSOM_ADMIN_PLUGIN_USERNAME_PARAM);
            String password = httpServletRequest.getParameter(BLOJSOM_ADMIN_PLUGIN_PASSWORD_PARAM);

            if (username == null || password == null || "".equals(username) || "".equals(password)) {
                _logger.debug("No username/password provided or username/password was empty.");
                return false;
            }

            // Check the username and password against the blog authorization
            if (blog.checkAuthorization(username, password)) {
                httpSession.setAttribute(BLOJSOM_ADMIN_PLUGIN_AUTHENTICATED_KEY, Boolean.TRUE);
                _logger.debug("Passed authentication for username: " + username);
                return true;
            } else {
                _logger.debug("Failed authentication for username: " + username);
                return false;
            }
        } else {
            return ((Boolean) httpSession.getAttribute(BLOJSOM_ADMIN_PLUGIN_AUTHENTICATED_KEY)).booleanValue();
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
