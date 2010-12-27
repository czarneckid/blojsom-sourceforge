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

import org.apache.velocity.util.StringUtils;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.blog.User;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.plugin.PluginException;
import org.blojsom.util.BlojsomConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * * View custom properties for a Blog user. Useful for collecting various
 * information from a user upon registration.
 * </p>
 *
 * @author Eric Broyles
 * @version $Id: ViewBlogUserPropertiesPlugin.java,v 1.3 2008-07-07 19:54:12 czarneckid Exp $
 */
public class ViewBlogUserPropertiesPlugin extends BaseAdminPlugin {

    /**
     * The suffix used to identify custom user properties.
     */
    protected static final String USER_PROPERTIES_SUFFIX = "_property";

    protected static final String BLOJSOM_PLUGIN_VIEW_USER_PROPERTIES_USER_MAP = "BLOJSOM_PLUGIN_VIEW_USER_PROPERTIES_USER_MAP";

    protected static final String VIEW_USER_PROPERTIES_PAGE = "/org/blojsom/plugin/admin/templates/admin-view-user-properties";

    protected static final String VIEW_USER_PROPERTIES_PERMISSION = "view_user_properties_permission";

    protected static final String FAILED_VIEW_PROPERTIES_KEY = "failed.view.user.properties.text";

    protected Fetcher _fetcher;

    /**
     * Construct a new instance of the View blog user properties plugin
     */
    public ViewBlogUserPropertiesPlugin() {
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
     * Read the properties for the specified user
     *
     * @param user User
     * @return Properties for the given user
     */
    protected Map readPropertiesForUser(User user) {
        Map properties = new TreeMap();
        Iterator keyIterator = user.getMetaData().keySet().iterator();

        while (keyIterator.hasNext()) {
            String propertyKey = (String) keyIterator.next();
            if (propertyKey.endsWith(USER_PROPERTIES_SUFFIX)) {
                // Camel case the property key and remove underscores for readability in the display
                properties.put(StringUtils.removeAndHump(propertyKey.replaceAll("_", " _"), "_"), user.getMetaData().get(propertyKey));
            }
        }
        properties.put("Name", user.getUserName());
        properties.put("Email", user.getUserEmail());
        properties.put("Status", user.getUserStatus());
        properties.put("Registered", user.getUserRegistered());

        return properties;
    }

    /**
     * Add the properties for the users in a blog to the context
     *
     * @param context Context
     * @param blog    {@link Blog}
     */
    protected void setupPropertiesInContext(Map context, Blog blog) {
        User[] users = _fetcher.getUsers(blog);
        TreeMap userIDs = new TreeMap();
        for (int i = 0; i < users.length; i++) {
            User userFromBlog = users[i];
            Map propertiesForUser = readPropertiesForUser(userFromBlog);

            userIDs.put(userFromBlog.getUserLogin(), propertiesForUser);
        }

        context.put(BLOJSOM_PLUGIN_VIEW_USER_PROPERTIES_USER_MAP, Collections
            .unmodifiableMap(userIDs));
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
        if (!checkPermission(blog, null, username, VIEW_USER_PROPERTIES_PERMISSION)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            addOperationResultMessage(context, getAdminResource(FAILED_VIEW_PROPERTIES_KEY, FAILED_VIEW_PROPERTIES_KEY, blog.getBlogAdministrationLocale()));

            return entries;
        }

        setupPropertiesInContext(context, blog);
        httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, VIEW_USER_PROPERTIES_PAGE);

        return entries;
    }
}
