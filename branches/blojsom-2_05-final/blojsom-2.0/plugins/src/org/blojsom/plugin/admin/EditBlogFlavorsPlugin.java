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

import org.blojsom.plugin.BlojsomPluginException;
import org.blojsom.blog.BlojsomConfiguration;
import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;
import org.blojsom.util.BlojsomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.ArrayList;
import java.io.File;

/**
 * EditBlogFlavorsPlugin
 * 
 * @author czarnecki
 * @since blojsom 2.05
 * @version $Id: EditBlogFlavorsPlugin.java,v 1.1 2003-12-04 03:52:22 czarneckid Exp $
 */
public class EditBlogFlavorsPlugin extends BaseAdminPlugin {

    private static final Log _logger = LogFactory.getLog(EditBlogFlavorsPlugin.class);

    private String _templatesDirectory;
    private String _installationDirectory;
    private String _baseConfigurationDirectory;

    // Pages
    private static final String ADD_BLOG_FLAVOR_PAGE = "/org/blojsom/plugin/admin/templates/admin-add-blog-flavor";

    // Constants
    private static final String DEFAULT_MIME_TYPE = "text/html";
    private static final String DEFAULT_CHARACTER_SET = UTF8;
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_FLAVORS_TEMPLATE_FILES = "BLOJSOM_PLUGIN_EDIT_BLOG_FLAVORS_TEMPLATE_FILES";

    // Actions
    private static final String ADD_BLOG_FLAVOR_ACTION = "add-blog-flavor";

    // Form elements
    private static final String FLAVOR_NAME = "flavor-name";
    private static final String FLAVOR_MIME_TYPE = "flavor-mime-type";
    private static final String FLAVOR_CHARACTER_SET = "flavor-character-set";
    private static final String BLOG_TEMPLATE = "blog-template";

    /**
     * Default constructor.
     */
    public EditBlogFlavorsPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig        Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration {@link org.blojsom.blog.BlojsomConfiguration} information
     * @throws org.blojsom.plugin.BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws BlojsomPluginException {
        super.init(servletConfig, blojsomConfiguration);

        _templatesDirectory = blojsomConfiguration.getTemplatesDirectory();
        _installationDirectory = blojsomConfiguration.getInstallationDirectory();
        _baseConfigurationDirectory = blojsomConfiguration.getBaseConfigurationDirectory();
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
     * @throws BlojsomPluginException If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlogUser user, Map context, BlogEntry[] entries) throws BlojsomPluginException {
        entries = super.process(httpServletRequest, httpServletResponse, user, context, entries);

        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);
        if (action == null || "".equals(action)) {
            _logger.debug("User did not request edit action");
            httpServletRequest.setAttribute(PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
        } else if (PAGE_ACTION.equals(action)) {
            _logger.debug("User requested add blog flavor page");

            File templatesDirectory = new File(_installationDirectory + BlojsomUtils.removeInitialSlash(_baseConfigurationDirectory) +
                    user.getId() + _templatesDirectory);
            _logger.debug("Looking for templates in directory: " + templatesDirectory.toString());

            File[] templates = templatesDirectory.listFiles();
            ArrayList templatesList = new ArrayList(templates.length);
            for (int i = 0; i < templates.length; i++) {
                File template = templates[i];
                if (template.isFile()) {
                    templatesList.add(template.getName());
                    _logger.debug("Added template: " + template.getName());
                }
            }

            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_FLAVORS_TEMPLATE_FILES, templatesList);

            httpServletRequest.setAttribute(PAGE_PARAM, ADD_BLOG_FLAVOR_PAGE);
        } else if (ADD_BLOG_FLAVOR_ACTION.equals(action)) {
            _logger.debug("User requested add blog flavor action");

            String flavorName = BlojsomUtils.getRequestValue(FLAVOR_NAME, httpServletRequest);
            if (flavorName == null || "".equals(flavorName)) {
                _logger.debug("No flavor name specified");
                return entries;
            }

            String blogTemplate = BlojsomUtils.getRequestValue(BLOG_TEMPLATE, httpServletRequest);
            if (blogTemplate == null || "".equals(blogTemplate)) {
                _logger.debug("No blog template specified");
                return entries;
            }

            String flavorMimeType = BlojsomUtils.getRequestValue(FLAVOR_MIME_TYPE, httpServletRequest);
            if (flavorMimeType == null || "".equals(flavorMimeType)) {
                flavorMimeType = DEFAULT_MIME_TYPE;
                _logger.debug("Flavor MIME type not specified. Using default: " + flavorMimeType);
            }
            String flavorCharacterSet = BlojsomUtils.getRequestValue(FLAVOR_CHARACTER_SET, httpServletRequest);
            if (flavorCharacterSet == null || "".equals(flavorCharacterSet)) {
                flavorCharacterSet = DEFAULT_CHARACTER_SET;
                _logger.debug("Flavor character set not specified. Using default: " + flavorCharacterSet);
            }

            Map flavorMapForUser = user.getFlavors();
            if (flavorMapForUser.containsKey(flavorName)) {
                _logger.debug("Flavor already exists: " + flavorName);
                return entries;
            } else {
                Map flavorTemplatesForUser = user.getFlavorToTemplate();
                Map flavorContentTypesForUser = user.getFlavorToContentType();

                flavorMapForUser.put(flavorName, flavorName);
                flavorTemplatesForUser.put(flavorName, blogTemplate);
                flavorContentTypesForUser.put(flavorName, flavorMimeType + ";" + flavorCharacterSet);
                _logger.debug("Successfully added flavor: " + flavorName + " using template: " + blogTemplate + " with content type: " + flavorMimeType + ";" + flavorCharacterSet);

                httpServletRequest.setAttribute(PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            }

        }
        return entries;
    }
}
