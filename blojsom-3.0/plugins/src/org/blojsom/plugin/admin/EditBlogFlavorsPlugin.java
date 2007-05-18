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
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.PluginException;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;

/**
 * EditBlogFlavorsPlugin
 *
 * @author David Czarnecki
 * @version $Id: EditBlogFlavorsPlugin.java,v 1.8 2007-05-18 01:07:46 czarneckid Exp $
 * @since blojsom 3.0
 */
public class EditBlogFlavorsPlugin extends BaseAdminPlugin {

    private Log _logger = LogFactory.getLog(EditBlogFlavorsPlugin.class);

    private static final String PROTECTED_FLAVORS_IP = "protected-flavors";
    private static final String DEFAULT_PROTECTED_FLAVORS = "admin";

    // Pages
    private static final String EDIT_BLOG_FLAVORS_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blog-flavors";
    private static final String EDIT_BLOG_FLAVOR_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blog-flavor";

    // Constants
    private static final String DEFAULT_MIME_TYPE = "text/html";
    private static final String DEFAULT_CHARACTER_SET = BlojsomConstants.UTF8;
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_FLAVORS_EXISTING = "BLOJSOM_PLUGIN_EDIT_BLOG_FLAVORS_EXISTING";
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_FLAVORS_TEMPLATE_FILES = "BLOJSOM_PLUGIN_EDIT_BLOG_FLAVORS_TEMPLATE_FILES";
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_FLAVORS_FLAVORS = "BLOJSOM_PLUGIN_EDIT_BLOG_FLAVORS_FLAVORS";

    private static final String FLAVOR_NAME_EDIT = "FLAVOR_NAME_EDIT";
    private static final String FLAVOR_TYPE_EDIT = "FLAVOR_TYPE_EDIT";
    private static final String FLAVOR_CHARACTER_SET_EDIT = "FLAVOR_CHARACTER_SET_EDIT";
    private static final String FLAVOR_TEMPLATE_EDIT = "FLAVOR_TEMPLATE_EDIT";

    // Localization constants
    private static final String FAILED_EDIT_FLAVOR_PERMISSION_KEY = "failed.edit.flavor.permission.text";
    private static final String NO_FLAVOR_SPECIFIED_KEY = "no.flavor.specified.text";
    private static final String NO_BLOG_TEMPLATE_SPECIFIED_KEY = "no.blog.template.specified.text";
    private static final String SUCCESSFULLY_ADDED_FLAVOR_KEY = "successfully.added.flavor.text";
    private static final String FAILED_UPDATE_FLAVOR_KEY = "failed.update.flavor.text";
    private static final String FAILED_DELETE_DEFAULT_FLAVOR_KEY = "failed.delete.default.flavor.text";
    private static final String FAILED_DELETE_PROTECTED_FLAVOR_KEY = "failed.delete.protected.flavor.text";
    private static final String SUCCESSFULLY_DELETED_FLAVOR_KEY = "successfully.deleted.flavor.text";

    // Actions
    private static final String ADD_BLOG_FLAVOR_ACTION = "add-blog-flavor";
    private static final String MODIFY_BLOG_FLAVOR_ACTION = "modify-blog-flavor";
    private static final String DELETE_BLOG_FLAVOR_ACTION = "delete-blog-flavor";
    private static final String EDIT_BLOG_FLAVOR_ACTION = "edit-blog-flavor";

    // Form elements
    private static final String FLAVOR_NAME = "flavor-name";
    private static final String FLAVOR_MIME_TYPE = "flavor-mime-type";
    private static final String FLAVOR_CHARACTER_SET = "flavor-character-set";
    private static final String BLOG_TEMPLATE = "blog-template";

    // Permissions
    private static final String EDIT_BLOG_FLAVORS_PERMISSION = "edit_blog_flavors_permission";

    private Fetcher _fetcher;
    private Properties _blojsomProperties;
    private String _templatesDirectory;
    private String _blogsDirectory;

    /**
     * Default constructor.
     */
    public EditBlogFlavorsPlugin() {
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
     * Set the default blojsom properties
     *
     * @param blojsomProperties Default blojsom properties
     */
    public void setBlojsomProperties(Properties blojsomProperties) {
        _blojsomProperties = blojsomProperties;
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws PluginException If there is an error initializing the plugin
     */
    public void init() throws PluginException {
        super.init();

        _templatesDirectory = _blojsomProperties.getProperty(BlojsomConstants.TEMPLATES_DIRECTORY_IP, BlojsomConstants.DEFAULT_TEMPLATES_DIRECTORY);
        _blogsDirectory = _blojsomProperties.getProperty(BlojsomConstants.BLOGS_DIRECTORY_IP, BlojsomConstants.DEFAULT_BLOGS_DIRECTORY);
    }

    /**
     * Add flavor information to the context
     *
     * @param blog {@link Blog}
     * @param context Context
     */
    protected void addFlavorInformationToContext(Blog blog, Map context) {
        // Put the available templates in the context for the edit flavors template
        File templatesDirectory = new File(_servletConfig.getServletContext().getRealPath("/") + BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + _blogsDirectory + blog.getBlogId() + _templatesDirectory);
        _logger.debug("Looking for templates in directory: " + templatesDirectory.toString());

        List templateFiles = new ArrayList();
        BlojsomUtils.listFilesInSubdirectories(templatesDirectory, templatesDirectory.getAbsolutePath(), templateFiles);
        File[] templates = (File[]) templateFiles.toArray(new File[templateFiles.size()]);
        Arrays.sort(templates);

        // Put the available flavors in the context for the edit flavors template
        context.put(BLOJSOM_PLUGIN_EDIT_BLOG_FLAVORS_FLAVORS, new TreeMap(blog.getTemplates()));
        context.put(BLOJSOM_PLUGIN_EDIT_BLOG_FLAVORS_TEMPLATE_FILES, templates);
        context.put(BLOJSOM_PLUGIN_EDIT_BLOG_FLAVORS_EXISTING, new TreeMap(blog.getTemplates()));
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
        if (!checkPermission(blog, null, username, EDIT_BLOG_FLAVORS_PERMISSION)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            addOperationResultMessage(context, getAdminResource(FAILED_EDIT_FLAVOR_PERMISSION_KEY, FAILED_EDIT_FLAVOR_PERMISSION_KEY, blog.getBlogAdministrationLocale()));

            return entries;
        }

        String protectedFlavors = blog.getProperty(PROTECTED_FLAVORS_IP);
        if (BlojsomUtils.checkNullOrBlank(protectedFlavors)) {
            protectedFlavors = DEFAULT_PROTECTED_FLAVORS;
        }

        if (protectedFlavors.indexOf(DEFAULT_PROTECTED_FLAVORS) == -1) {
            protectedFlavors = protectedFlavors + " " + DEFAULT_PROTECTED_FLAVORS;
        }

        addFlavorInformationToContext(blog, context);

        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);
        if (BlojsomUtils.checkNullOrBlank(action)) {
            _logger.debug("User did not request edit action");

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
        } else if (PAGE_ACTION.equals(action)) {
            _logger.debug("User requested add blog flavor page");

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_FLAVORS_PAGE);
        } else if (ADD_BLOG_FLAVOR_ACTION.equals(action) || MODIFY_BLOG_FLAVOR_ACTION.equals(action)) {
            _logger.debug("User requested add blog flavor action");

            String flavorName = BlojsomUtils.getRequestValue(FLAVOR_NAME, httpServletRequest);
            flavorName = (flavorName != null) ? (flavorName.replaceAll("\\W", "")) : null;
            if (BlojsomUtils.checkNullOrBlank(flavorName)) {
                _logger.debug("No flavor name specified");
                addOperationResultMessage(context, getAdminResource(NO_FLAVOR_SPECIFIED_KEY, NO_FLAVOR_SPECIFIED_KEY, blog.getBlogAdministrationLocale()));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_FLAVORS_PAGE);

                return entries;
            }

            String blogTemplate = BlojsomUtils.getRequestValue(BLOG_TEMPLATE, httpServletRequest);
            if (BlojsomUtils.checkNullOrBlank(blogTemplate)) {
                _logger.debug("No blog template specified");
                addOperationResultMessage(context, getAdminResource(NO_BLOG_TEMPLATE_SPECIFIED_KEY, NO_BLOG_TEMPLATE_SPECIFIED_KEY, blog.getBlogAdministrationLocale()));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_FLAVORS_PAGE);

                return entries;
            }

            String flavorMimeType = BlojsomUtils.getRequestValue(FLAVOR_MIME_TYPE, httpServletRequest);
            if (BlojsomUtils.checkNullOrBlank(flavorMimeType)) {
                flavorMimeType = DEFAULT_MIME_TYPE;
                _logger.debug("Flavor MIME type not specified. Using default: " + flavorMimeType);
            }

            String flavorCharacterSet = BlojsomUtils.getRequestValue(FLAVOR_CHARACTER_SET, httpServletRequest);
            if (BlojsomUtils.checkNullOrBlank(flavorCharacterSet)) {
                flavorCharacterSet = DEFAULT_CHARACTER_SET;
                _logger.debug("Flavor character set not specified. Using default: " + flavorCharacterSet);
            }

            Map templateMap = new HashMap(blog.getTemplates());
            templateMap.put(flavorName, blogTemplate + ", " + flavorMimeType + ";charset=" + flavorCharacterSet);
            blog.setTemplates(templateMap);

            _logger.debug("Successfully added flavor: " + flavorName + " using template: " + blogTemplate + " with content type: " + flavorMimeType + ";" + flavorCharacterSet);

            try {
                _fetcher.saveBlog(blog);
                addOperationResultMessage(context, formatAdminResource(SUCCESSFULLY_ADDED_FLAVOR_KEY, SUCCESSFULLY_ADDED_FLAVOR_KEY, blog.getBlogAdministrationLocale(), new Object[] {flavorName, blogTemplate, flavorMimeType, flavorCharacterSet}));
                _logger.debug("Successfully wrote flavor configuration file for blog: " + blog.getBlogId());

                addFlavorInformationToContext(blog, context);
            } catch (FetcherException e) {
                addOperationResultMessage(context, getAdminResource(FAILED_UPDATE_FLAVOR_KEY, FAILED_UPDATE_FLAVOR_KEY, blog.getBlogAdministrationLocale()));
                _logger.error(e);
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_FLAVORS_PAGE);
        } else if (DELETE_BLOG_FLAVOR_ACTION.equals(action)) {
            _logger.debug("User requested delete blog flavor action");

            String flavorName = BlojsomUtils.getRequestValue(FLAVOR_NAME, httpServletRequest);
            if (BlojsomUtils.checkNullOrBlank(flavorName)) {
                _logger.debug("No flavor name specified");
                addOperationResultMessage(context, getAdminResource(NO_FLAVOR_SPECIFIED_KEY, NO_FLAVOR_SPECIFIED_KEY, blog.getBlogAdministrationLocale()));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_FLAVORS_PAGE);

                return entries;
            }

            if (flavorName.equalsIgnoreCase(blog.getBlogDefaultFlavor())) {
                _logger.debug("Cannot delete the default flavor");
                addOperationResultMessage(context, getAdminResource(FAILED_DELETE_DEFAULT_FLAVOR_KEY, FAILED_DELETE_DEFAULT_FLAVOR_KEY, blog.getBlogAdministrationLocale()));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_FLAVORS_PAGE);

                return entries;
            }

            if (protectedFlavors.indexOf(flavorName) != -1) {
                _logger.debug("Cannot delete protected flavor: " + flavorName);
                addOperationResultMessage(context, formatAdminResource(FAILED_DELETE_PROTECTED_FLAVOR_KEY, FAILED_DELETE_PROTECTED_FLAVOR_KEY, blog.getBlogAdministrationLocale(), new Object[] {flavorName}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_FLAVORS_PAGE);

                return entries;
            }

            // Delete the flavor from the maps
            Map templateMap = new HashMap(blog.getTemplates());
            templateMap.remove(flavorName);
            blog.setTemplates(templateMap);

            Map pluginsMap = new HashMap(blog.getPlugins());
            pluginsMap.remove(flavorName);
            blog.setPlugins(pluginsMap);

            // Write out the flavor configuration file
            try {
                _fetcher.saveBlog(blog);
                _logger.debug("Successfully wrote flavor configuration file for blog: " + blog.getBlogId());
                addOperationResultMessage(context, formatAdminResource(SUCCESSFULLY_DELETED_FLAVOR_KEY, SUCCESSFULLY_DELETED_FLAVOR_KEY, blog.getBlogAdministrationLocale(), new Object[] {flavorName}));

                addFlavorInformationToContext(blog, context);
            } catch (FetcherException e) {
                addOperationResultMessage(context, getAdminResource(FAILED_UPDATE_FLAVOR_KEY, FAILED_UPDATE_FLAVOR_KEY, blog.getBlogAdministrationLocale()));
                _logger.error(e);
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_FLAVORS_PAGE);
        } else if (EDIT_BLOG_FLAVOR_ACTION.equals(action)) {
            String flavor = BlojsomUtils.getRequestValue(FLAVOR_NAME, httpServletRequest);

            context.put(FLAVOR_NAME_EDIT, flavor);

            Map templates = blog.getTemplates();
            String templateAndType = (String) templates.get(flavor);
            String[] templateType = BlojsomUtils.parseCommaList(templateAndType);
            String[] typeAndCharacterSet;
            if (templateType.length == 2) {
                typeAndCharacterSet = BlojsomUtils.parseDelimitedList(templateType[1], ";", true);
            } else {
                typeAndCharacterSet = new String[] {"text/html"};
            }

            context.put(FLAVOR_TYPE_EDIT, typeAndCharacterSet[0]);
            if (typeAndCharacterSet.length == 2) {
                context.put(FLAVOR_CHARACTER_SET_EDIT, typeAndCharacterSet[1].substring(typeAndCharacterSet[1].indexOf("=") + 1));
            } else {
                context.put(FLAVOR_CHARACTER_SET_EDIT, BlojsomUtils.UTF8);
            }
            context.put(FLAVOR_TEMPLATE_EDIT, templateType[0]);

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_FLAVOR_PAGE);
        }

        return entries;
    }
}
