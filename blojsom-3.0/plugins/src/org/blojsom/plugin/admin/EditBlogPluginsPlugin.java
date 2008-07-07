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
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * EditBlogPluginsPlugin
 *
 * @author David Czarnecki
 * @version $Id: EditBlogPluginsPlugin.java,v 1.5 2008-07-07 19:54:12 czarneckid Exp $
 * @since blojsom 3.0
 */
public class EditBlogPluginsPlugin extends BaseAdminPlugin {

    private Log _logger = LogFactory.getLog(EditBlogPluginsPlugin.class);

    // Pages
    private static final String EDIT_BLOG_PLUGINS_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blog-plugins";

    // Constants
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_PLUGINS_MAP = "BLOJSOM_PLUGIN_EDIT_BLOG_PLUGINS_MAP";
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_PLUGINS_AVAILABLE_PLUGINS = "BLOJSOM_PLUGIN_EDIT_BLOG_PLUGINS_AVAILABLE_PLUGINS";

    // Localization constants
    private static final String FAILED_EDIT_PLUGINS_PERMISSION_KEY = "failed.edit.plugins.permission.text";
    private static final String SUCCESSFULLY_UPDATED_PLUGINS_KEY = "successfully.updated.plugins.text";
    private static final String FAILED_UPDATE_PLUGINS_KEY = "failed.updated.plugins.text";

    // Actions
    private static final String MODIFY_PLUGIN_CHAINS = "modify-plugin-chains";

    // Permissions
    private static final String EDIT_BLOG_PLUGINS_PERMISSION = "edit_blog_plugins_permission";

    private Fetcher _fetcher;

    /**
     * Default constructor
     */
    public EditBlogPluginsPlugin() {
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
        if (!checkPermission(blog, null, username, EDIT_BLOG_PLUGINS_PERMISSION)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            addOperationResultMessage(context, getAdminResource(FAILED_EDIT_PLUGINS_PERMISSION_KEY, FAILED_EDIT_PLUGINS_PERMISSION_KEY, blog.getBlogAdministrationLocale()));

            return entries;
        }

        context.put(BLOJSOM_PLUGIN_EDIT_BLOG_PLUGINS_MAP, new TreeMap(blog.getPlugins()));

        // Add the list of available plugins
        ApplicationContext applicationContext = (ApplicationContext) _servletConfig.getServletContext().getAttribute(BlojsomConstants.BLOJSOM_APPLICATION_CONTEXT);
        if (applicationContext != null) {
            Map pluginBeans = applicationContext.getBeansOfType(Plugin.class);
            String[] pluginNames = applicationContext.getBeanNamesForType(Plugin.class);
            for (int i = 0; i < pluginNames.length; i++) {
                String pluginName = pluginNames[i];
                Object plugin = pluginBeans.get(pluginName);
                if (plugin.getClass().getName().indexOf("admin") != -1) {
                    pluginBeans.remove(pluginName);
                }
            }

            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_PLUGINS_AVAILABLE_PLUGINS, new TreeMap(pluginBeans));
        }

        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);
        if (BlojsomUtils.checkNullOrBlank(action)) {
            _logger.debug("User did not request edit action");

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
        } else if (PAGE_ACTION.equals(action)) {
            _logger.debug("User requested edit blog plugins page");

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_PLUGINS_PAGE);
        } else if (MODIFY_PLUGIN_CHAINS.equals(action)) {
            _logger.debug("User requested modify blog plugins action");

            Map pluginChain = new TreeMap(blog.getPlugins());

            // Iterate over the user's flavors and update the plugin chains
            Iterator flavorIterator = blog.getTemplates().keySet().iterator();
            String updatedFlavor;
            while (flavorIterator.hasNext()) {
                String flavor = (String) flavorIterator.next();
                updatedFlavor = BlojsomUtils.getRequestValue(flavor + "." + BlojsomConstants.BLOJSOM_PLUGIN_CHAIN, httpServletRequest);
                if (!BlojsomUtils.checkNullOrBlank(updatedFlavor)) {
                    pluginChain.put(flavor, updatedFlavor);
                } else {
                    pluginChain.put(flavor, "");
                }
            }

            // Check for the default flavor
            updatedFlavor = BlojsomUtils.getRequestValue("default." + BlojsomConstants.BLOJSOM_PLUGIN_CHAIN, httpServletRequest);
            pluginChain.put("default", updatedFlavor);

            // Update the internal plugin chain map for the user
            blog.setPlugins(pluginChain);

            // Write out the updated plugin configuration file
            try {
                _fetcher.saveBlog(blog);
                addOperationResultMessage(context, getAdminResource(SUCCESSFULLY_UPDATED_PLUGINS_KEY, SUCCESSFULLY_UPDATED_PLUGINS_KEY, blog.getBlogAdministrationLocale()));
            } catch (FetcherException e) {
                _logger.error(e);
                addOperationResultMessage(context, getAdminResource(FAILED_UPDATE_PLUGINS_KEY, FAILED_UPDATE_PLUGINS_KEY, blog.getBlogAdministrationLocale()));
            }

            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_PLUGINS_MAP, new TreeMap(blog.getPlugins()));
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_PLUGINS_PAGE);
        }

        return entries;
    }
}
