/**
 * Copyright (c) 2003-2005, David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003-2005 by Mark Lussier
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
import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;
import org.blojsom.blog.Blog;
import org.blojsom.blog.BlojsomConfigurationException;
import org.blojsom.plugin.BlojsomPluginException;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.BlojsomProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Reload Blog Properties plugin.
 *
 * @author David Czarnecki
 * @version $Id: ReloadBlogPropertiesPlugin.java,v 1.5 2005-09-14 14:48:48 czarneckid Exp $
 * @since blojsom 2.17
 */
public class ReloadBlogPropertiesPlugin extends WebAdminPlugin {

    private Log _logger = LogFactory.getLog(ReloadBlogPropertiesPlugin.class);

    // Localization constants
    private static final String FAILED_PROPERTIES_LOAD_PERMISSION_KEY = "failed.properties.load.permission.text";
    private static final String RELOADED_PROPERTIES_KEY = "reloaded.properties.text";
    private static final String FAILED_PROPERTIES_LOAD_KEY = "failed.properties.load.text";

    // Permissions
    private static final String RELOAD_PROPERTIES_PERMISSION = "reload_properties";

    /**
     * Default constructor.
     */
    public ReloadBlogPropertiesPlugin() {
    }

    /**
     * Return the display name for the plugin
     *
     * @return Display name for the plugin
     */
    public String getDisplayName() {
        return "Reload Blog Properties plugin";
    }

    /**
     * Return the name of the initial editing page for the plugin
     *
     * @return Name of the initial editing page for the plugin
     */
    public String getInitialPage() {
        return "";
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param user                {@link org.blojsom.blog.BlogUser} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlogUser user, Map context, BlogEntry[] entries) throws BlojsomPluginException {
        entries = super.process(httpServletRequest, httpServletResponse, user, context, entries);

        String username = getUsernameFromSession(httpServletRequest, user.getBlog());
        if (!checkPermission(user, null, username, RELOAD_PROPERTIES_PERMISSION)) {
            httpServletRequest.setAttribute(PAGE_PARAM, ADMIN_LOGIN_PAGE);
            addOperationResultMessage(context, getAdminResource(FAILED_PROPERTIES_LOAD_PERMISSION_KEY, FAILED_PROPERTIES_LOAD_PERMISSION_KEY, user.getBlog().getBlogAdministrationLocale()));

            return entries;
        }

        String page = BlojsomUtils.getRequestValue(PAGE_PARAM, httpServletRequest);

        if (ADMIN_LOGIN_PAGE.equals(page)) {
            return entries;
        } else {
            try {
                Properties blogProperties = new BlojsomProperties();
                InputStream is = _servletConfig.getServletContext().getResourceAsStream(_blojsomConfiguration.getBaseConfigurationDirectory() + user.getId() + '/' + BLOG_DEFAULT_PROPERTIES);
                blogProperties.load(is);
                is.close();
                Blog blog = new Blog(blogProperties);
                user.setBlog(blog);
                addOperationResultMessage(context, formatAdminResource(RELOADED_PROPERTIES_KEY, RELOADED_PROPERTIES_KEY, user.getBlog().getBlogAdministrationLocale(), new Object[] {user.getId()}));
                httpServletRequest.setAttribute(PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            } catch (IOException e) {
                _logger.error(e);
                addOperationResultMessage(context, formatAdminResource(FAILED_PROPERTIES_LOAD_KEY, FAILED_PROPERTIES_LOAD_KEY, user.getBlog().getBlogAdministrationLocale(), new Object[] {user.getId()}));
            } catch (BlojsomConfigurationException e) {
                _logger.error(e);
                addOperationResultMessage(context, formatAdminResource(FAILED_PROPERTIES_LOAD_KEY, FAILED_PROPERTIES_LOAD_KEY, user.getBlog().getBlogAdministrationLocale(), new Object[] {user.getId()}));
            }
        }

        return entries;
    }
}