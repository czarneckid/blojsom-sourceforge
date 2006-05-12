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
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.plugin.PluginException;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * EditBlogTemplatesPlugin
 *
 * @author David Czarnecki
 * @version $Id: EditBlogTemplatesPlugin.java,v 1.3 2006-05-12 15:53:03 czarneckid Exp $
 * @since blojsom 3.0
 */
public class EditBlogTemplatesPlugin extends BaseAdminPlugin {

    private Log _logger = LogFactory.getLog(EditBlogTemplatesPlugin.class);

    private static final String DEFAULT_ACCEPTED_TEMPLATE_EXTENSIONS = "vm";
    private static final String ACCEPTED_TEMPLATE_EXTENSIONS_INIT_PARAM = "accepted-template-extensions";

    // Pages
    private static final String EDIT_BLOG_TEMPLATES_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blog-templates";
    private static final String EDIT_BLOG_TEMPLATE_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blog-template";

    // Constants
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_TEMPLATES_TEMPLATE_FILES = "BLOJSOM_PLUGIN_EDIT_BLOG_TEMPLATES_TEMPLATE_FILES";
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_TEMPLATES_TEMPLATE_FILE = "BLOJSOM_PLUGIN_EDIT_BLOG_TEMPLATES_TEMPLATE_FILE";
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_TEMPLATES_TEMPLATE = "BLOJSOM_PLUGIN_EDIT_BLOG_TEMPLATES_TEMPLATE";
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_TEMPLATES_DIRECTORIES = "BLOJSOM_PLUGIN_EDIT_BLOG_TEMPLATES_DIRECTORIES";

    // Localization constants
    private static final String FAILED_EDIT_TEMPLATES_PERMISSION_KEY = "failed.edit.templates.permission.text";
    private static final String INVALID_TEMPLATE_PATH_KEY = "invalid.template.path.text";
    private static final String UNABLE_TO_LOAD_TEMPLATE_KEY = "unable.to.load.template.text";
    private static final String UNABLE_TO_DELETE_TEMPLATE_KEY = "unable.to.delete.template.text";
    private static final String UPDATED_TEMPLATE_KEY = "updated.template.text";
    private static final String NO_TEMPLATE_NAME_KEY = "no.template.name.text";
    private static final String INVALID_TEMPLATE_EXTENSION_KEY = "invalid.template.extension.text";
    private static final String TEMPLATE_DIRECTORY_NONEXISTENT_KEY = "template.directory.nonexistent.text";
    private static final String TEMPLATE_DIRECTORY_NOTSPECIFIED_KEY = "template.directory.notspecified.text";
    private static final String UNABLE_TO_ADD_TEMPLATE_DIRECTORY_KEY = "unable.to.add.template.directory.text";
    private static final String ADDED_TEMPLATE_DIRECTORY_KEY = "added.template.directory.text";
    private static final String CANNOT_REMOVE_TOP_TEMPLATE_DIRECTORY_KEY = "cannot.remove.top.template.directory.text";
    private static final String UNABLE_TO_DELETE_TEMPLATE_DIRECTORY_KEY = "unable.to.delete.template.directory.text";
    private static final String REMOVED_TEMPLATE_DIRECTORY_KEY = "removed.template.directory.text";
    private static final String DELETED_TEMPLATE_KEY = "deleted.template.text";

    // Actions
    private static final String ADD_BLOG_TEMPLATE_ACTION = "add-blog-template";
    private static final String DELETE_BLOG_TEMPLATE_ACTION = "delete-blog-template";
    private static final String EDIT_BLOG_TEMPLATES_ACTION = "edit-blog-template";
    private static final String UPDATE_BLOG_TEMPLATE_ACTION = "update-blog-template";
    private static final String ADD_TEMPLATE_DIRECTORY_ACTION = "add-template-directory";
    private static final String DELETE_TEMPLATE_DIRECTORY_ACTION = "delete-template-directory";

    // Form elements
    private static final String BLOG_TEMPLATE = "blog-template";
    private static final String BLOG_TEMPLATE_DATA = "blog-template-data";
    private static final String BLOG_TEMPLATE_DIRECTORY = "blog-template-directory";
    private static final String TEMPLATE_DIRECTORY_TO_ADD = "template-directory-to-add";

    // Permissions
    private static final String EDIT_BLOG_TEMPLATES_PERMISSION = "edit_blog_templates_permission";

    private Map _acceptedTemplateExtensions;
    private Properties _templateEditProperties;
    private Properties _blojsomProperties;
    private String _templatesDirectory;
    private String _blogsDirectory;

    /**
     * Default constructor.
     */
    public EditBlogTemplatesPlugin() {
    }

    /**
     * Set the template editing properties
     *
     * @param templateEditProperties Template editing properties
     */
    public void setTemplateEditProperties(Properties templateEditProperties) {
        _templateEditProperties = templateEditProperties;
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
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error initializing the plugin
     */
    public void init() throws PluginException {
        super.init();

        String acceptedTemplateExtensions = _templateEditProperties.getProperty(ACCEPTED_TEMPLATE_EXTENSIONS_INIT_PARAM);
        if (BlojsomUtils.checkNullOrBlank(acceptedTemplateExtensions)) {
            acceptedTemplateExtensions = DEFAULT_ACCEPTED_TEMPLATE_EXTENSIONS;
        }

        _acceptedTemplateExtensions = new HashMap();
        String[] templateExtensions = BlojsomUtils.parseCommaList(acceptedTemplateExtensions);
        for (int i = 0; i < templateExtensions.length; i++) {
            String templateExtension = templateExtensions[i];
            _acceptedTemplateExtensions.put(templateExtension, templateExtension);
        }

        _templatesDirectory = _blojsomProperties.getProperty(BlojsomConstants.TEMPLATES_DIRECTORY_IP, BlojsomConstants.DEFAULT_TEMPLATES_DIRECTORY);
        _blogsDirectory = _blojsomProperties.getProperty(BlojsomConstants.BLOGS_DIRECTORY_IP, BlojsomConstants.DEFAULT_BLOGS_DIRECTORY);
    }

    /**
     * Sanitize a filename
     *
     * @param blogTemplate Blog template filename
     * @return Sanitized filename or <code>null</code> if error in sanitizing
     */
    protected String sanitizeFilename(String blogTemplate) {
        String templateFilename = new File(blogTemplate).getName();
        int lastSeparator;
        blogTemplate = BlojsomUtils.normalize(blogTemplate);
        lastSeparator = blogTemplate.lastIndexOf(File.separator);
        if (lastSeparator == -1) {
            if (templateFilename != null) {
                return templateFilename;
            } else {
                return null;
            }
        } else {
            blogTemplate = blogTemplate.substring(0, lastSeparator + 1) + templateFilename;
        }

        return blogTemplate;
    }

    /**
     * Put the list of template files in the context
     *
     * @param templatesDirectory Templates directory
     * @param context            Context
     */
    protected void putTemplatesInContext(File templatesDirectory, Map context) {
        List templateFiles = new ArrayList();
        BlojsomUtils.listFilesInSubdirectories(templatesDirectory, templatesDirectory.getAbsolutePath(), templateFiles);
        File[] templates = (File[]) templateFiles.toArray(new File[templateFiles.size()]);
        Arrays.sort(templates);

        context.put(BLOJSOM_PLUGIN_EDIT_BLOG_TEMPLATES_TEMPLATE_FILES, templates);
    }

    /**
     * Put the list of template directories in the context
     *
     * @param templatesDirectory Templates directory
     * @param context            Context
     */
    protected void putTemplateDirectoriesInContext(File templatesDirectory, Map context) {
        List templateDirectories = new ArrayList();
        BlojsomUtils.listDirectoriesInSubdirectories(templatesDirectory, templatesDirectory.getAbsolutePath(), templateDirectories);
        File[] directories = (File[]) templateDirectories.toArray(new File[templateDirectories.size()]);
        Arrays.sort(directories);

        context.put(BLOJSOM_PLUGIN_EDIT_BLOG_TEMPLATES_DIRECTORIES, directories);
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
     * @throws PluginException If there is an error processing the blog entries
     */
    public Entry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, Map context, Entry[] entries) throws PluginException {
        if (!authenticateUser(httpServletRequest, httpServletResponse, context, blog)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_LOGIN_PAGE);

            return entries;
        }

        String username = getUsernameFromSession(httpServletRequest, blog);
        if (!checkPermission(blog, null, username, EDIT_BLOG_TEMPLATES_PERMISSION)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            addOperationResultMessage(context, getAdminResource(FAILED_EDIT_TEMPLATES_PERMISSION_KEY, FAILED_EDIT_TEMPLATES_PERMISSION_KEY, blog.getBlogAdministrationLocale()));

            return entries;
        }

        // Add list of templates to context
        File templatesDirectory = new File(_servletConfig.getServletContext().getRealPath("/") + BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + _blogsDirectory + blog.getBlogId() + _templatesDirectory);
        _logger.debug("Looking for templates in directory: " + templatesDirectory.toString());

        putTemplatesInContext(templatesDirectory, context);
        putTemplateDirectoriesInContext(templatesDirectory, context);

        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);
        if (BlojsomUtils.checkNullOrBlank(action)) {
            _logger.debug("User did not request edit action");
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
        } else if (PAGE_ACTION.equals(action)) {
            _logger.debug("User requested edit blog templates page");

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_TEMPLATES_PAGE);
        } else if (EDIT_BLOG_TEMPLATES_ACTION.equals(action)) {
            _logger.debug("User requested edit blog templates action");

            String blogTemplate = BlojsomUtils.getRequestValue(BLOG_TEMPLATE, httpServletRequest);
            if (BlojsomUtils.checkNullOrBlank(blogTemplate)) {
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_TEMPLATES_PAGE);

                return entries;
            }

            blogTemplate = sanitizeFilename(blogTemplate);
            if (blogTemplate == null) {
                addOperationResultMessage(context, getAdminResource(INVALID_TEMPLATE_PATH_KEY, INVALID_TEMPLATE_PATH_KEY, blog.getBlogAdministrationLocale()));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_TEMPLATES_PAGE);

                return entries;
            }

            File blogTemplateFile = new File(_servletConfig.getServletContext().getRealPath("/") + BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + _blogsDirectory + blog.getBlogId() + _templatesDirectory + blogTemplate);
            _logger.debug("Reading template file: " + blogTemplateFile.toString());

            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(blogTemplateFile), BlojsomConstants.UTF8));
                String input;
                StringBuffer template = new StringBuffer();

                while ((input = br.readLine()) != null) {
                    template.append(input);
                    template.append(BlojsomConstants.LINE_SEPARATOR);
                }

                br.close();

                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_TEMPLATES_TEMPLATE_FILE, blogTemplate);
                context.put(BLOJSOM_PLUGIN_EDIT_BLOG_TEMPLATES_TEMPLATE, BlojsomUtils.escapeString(template.toString()));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_TEMPLATE_PAGE);
            } catch (UnsupportedEncodingException e) {
                _logger.error(e);
                addOperationResultMessage(context, formatAdminResource(UNABLE_TO_LOAD_TEMPLATE_KEY, UNABLE_TO_LOAD_TEMPLATE_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogTemplate}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_TEMPLATES_PAGE);
            } catch (IOException e) {
                _logger.error(e);
                addOperationResultMessage(context, formatAdminResource(UNABLE_TO_LOAD_TEMPLATE_KEY, UNABLE_TO_LOAD_TEMPLATE_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogTemplate}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_TEMPLATES_PAGE);
            }
        } else if (UPDATE_BLOG_TEMPLATE_ACTION.equals(action)) {
            _logger.debug("User requested update blog template action");

            String blogTemplate = BlojsomUtils.getRequestValue(BLOG_TEMPLATE, httpServletRequest);
            if (BlojsomUtils.checkNullOrBlank(blogTemplate)) {
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_TEMPLATES_PAGE);

                return entries;
            }

            blogTemplate = sanitizeFilename(blogTemplate);
            if (blogTemplate == null) {
                addOperationResultMessage(context, getAdminResource(INVALID_TEMPLATE_PATH_KEY, INVALID_TEMPLATE_PATH_KEY, blog.getBlogAdministrationLocale()));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_TEMPLATES_PAGE);

                return entries;
            }

            String blogTemplateData = BlojsomUtils.getRequestValue(BLOG_TEMPLATE_DATA, httpServletRequest);
            File blogTemplateFile = new File(_servletConfig.getServletContext().getRealPath("/") + BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + _blogsDirectory + blog.getBlogId() + _templatesDirectory + blogTemplate);

            _logger.debug("Writing template file: " + blogTemplateFile.toString());

            try {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(blogTemplateFile), BlojsomConstants.UTF8));
                bw.write(blogTemplateData);
                bw.close();
            } catch (UnsupportedEncodingException e) {
                _logger.error(e);
                addOperationResultMessage(context, formatAdminResource(UNABLE_TO_DELETE_TEMPLATE_KEY, UNABLE_TO_DELETE_TEMPLATE_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogTemplate}));
            } catch (IOException e) {
                _logger.error(e);
                addOperationResultMessage(context, formatAdminResource(UNABLE_TO_DELETE_TEMPLATE_KEY, UNABLE_TO_DELETE_TEMPLATE_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogTemplate}));
            }

            addOperationResultMessage(context, formatAdminResource(UPDATED_TEMPLATE_KEY, UPDATED_TEMPLATE_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogTemplate}));

            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_TEMPLATES_TEMPLATE_FILE, blogTemplate);
            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_TEMPLATES_TEMPLATE, BlojsomUtils.escapeString(blogTemplateData));
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_TEMPLATE_PAGE);
        } else if (ADD_BLOG_TEMPLATE_ACTION.equals(action)) {
            _logger.debug("User requested add blog template action");

            String blogTemplate = BlojsomUtils.getRequestValue(BLOG_TEMPLATE, httpServletRequest);
            String blogTemplateDirectory = BlojsomUtils.getRequestValue(BLOG_TEMPLATE_DIRECTORY, httpServletRequest);

            if (BlojsomUtils.checkNullOrBlank(blogTemplate)) {
                addOperationResultMessage(context, getAdminResource(NO_TEMPLATE_NAME_KEY, NO_TEMPLATE_NAME_KEY, blog.getBlogAdministrationLocale()));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_TEMPLATES_PAGE);

                return entries;
            }

            blogTemplate = sanitizeFilename(blogTemplate);

            String templateName = BlojsomUtils.getFilenameFromPath(blogTemplate);
            String templateExtension = BlojsomUtils.getFileExtension(templateName);

            if (!_acceptedTemplateExtensions.containsKey(templateExtension)) {
                addOperationResultMessage(context, formatAdminResource(INVALID_TEMPLATE_EXTENSION_KEY, INVALID_TEMPLATE_EXTENSION_KEY, blog.getBlogAdministrationLocale(), new Object[]{templateExtension}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_TEMPLATES_PAGE);

                return entries;
            } else {
                blogTemplateDirectory = BlojsomUtils.normalize(blogTemplateDirectory);
                File addedTemplateDirectory = new File(templatesDirectory, blogTemplateDirectory);
                if (addedTemplateDirectory.exists()) {
                    context.put(BLOJSOM_PLUGIN_EDIT_BLOG_TEMPLATES_TEMPLATE_FILE, blogTemplateDirectory + File.separator + templateName);
                    context.put(BLOJSOM_PLUGIN_EDIT_BLOG_TEMPLATES_TEMPLATE, "");

                    httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_TEMPLATE_PAGE);
                } else {
                    addOperationResultMessage(context, getAdminResource(TEMPLATE_DIRECTORY_NONEXISTENT_KEY, TEMPLATE_DIRECTORY_NONEXISTENT_KEY, blog.getBlogAdministrationLocale()));
                }
            }
        } else if (ADD_TEMPLATE_DIRECTORY_ACTION.equals(action)) {
            _logger.debug("User requested add blog template directory action");

            String templateDirectoryToAdd = BlojsomUtils.getRequestValue(TEMPLATE_DIRECTORY_TO_ADD, httpServletRequest);
            String blogTemplateDirectory = BlojsomUtils.getRequestValue(BLOG_TEMPLATE_DIRECTORY, httpServletRequest);
            if (BlojsomUtils.checkNullOrBlank(templateDirectoryToAdd)) {
                addOperationResultMessage(context, getAdminResource(TEMPLATE_DIRECTORY_NOTSPECIFIED_KEY, TEMPLATE_DIRECTORY_NOTSPECIFIED_KEY, blog.getBlogAdministrationLocale()));
            } else {
                blogTemplateDirectory = BlojsomUtils.normalize(blogTemplateDirectory);
                templateDirectoryToAdd = BlojsomUtils.normalize(templateDirectoryToAdd);

                File newTemplateDirectory = new File(templatesDirectory, blogTemplateDirectory + File.separator + templateDirectoryToAdd);
                _logger.debug("Adding blog template directory: " + newTemplateDirectory.toString());

                if (!newTemplateDirectory.mkdir()) {
                    addOperationResultMessage(context, formatAdminResource(UNABLE_TO_ADD_TEMPLATE_DIRECTORY_KEY, UNABLE_TO_ADD_TEMPLATE_DIRECTORY_KEY, blog.getBlogAdministrationLocale(), new Object[]{templateDirectoryToAdd}));
                } else {
                    addOperationResultMessage(context, formatAdminResource(ADDED_TEMPLATE_DIRECTORY_KEY, ADDED_TEMPLATE_DIRECTORY_KEY, blog.getBlogAdministrationLocale(), new Object[]{templateDirectoryToAdd}));

                    putTemplateDirectoriesInContext(templatesDirectory, context);
                }
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_TEMPLATES_PAGE);
        } else if (DELETE_TEMPLATE_DIRECTORY_ACTION.equals(action)) {
            _logger.debug("User requested delete blog template directory action");

            String blogTemplateDirectory = BlojsomUtils.getRequestValue(BLOG_TEMPLATE_DIRECTORY, httpServletRequest);
            if (BlojsomUtils.checkNullOrBlank(blogTemplateDirectory)) {
                addOperationResultMessage(context, getAdminResource(CANNOT_REMOVE_TOP_TEMPLATE_DIRECTORY_KEY, CANNOT_REMOVE_TOP_TEMPLATE_DIRECTORY_KEY, blog.getBlogAdministrationLocale()));
            } else {
                blogTemplateDirectory = BlojsomUtils.normalize(blogTemplateDirectory);
                _logger.debug("Sanitized template directory: " + blogTemplateDirectory);
                File templateDirectoryToDelete = new File(templatesDirectory, blogTemplateDirectory);
                _logger.debug("Removing blog template directory: " + templateDirectoryToDelete);

                if (!BlojsomUtils.deleteDirectory(templateDirectoryToDelete, true)) {
                    addOperationResultMessage(context, formatAdminResource(UNABLE_TO_DELETE_TEMPLATE_DIRECTORY_KEY, UNABLE_TO_DELETE_TEMPLATE_DIRECTORY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogTemplateDirectory}));
                } else {
                    addOperationResultMessage(context, formatAdminResource(REMOVED_TEMPLATE_DIRECTORY_KEY, REMOVED_TEMPLATE_DIRECTORY_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogTemplateDirectory}));

                    putTemplateDirectoriesInContext(templatesDirectory, context);
                }
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_TEMPLATES_PAGE);
        } else if (DELETE_BLOG_TEMPLATE_ACTION.equals(action)) {
            _logger.debug("User requested delete blog template directory action");

            String blogTemplate = BlojsomUtils.getRequestValue(BLOG_TEMPLATE, httpServletRequest);
            if (BlojsomUtils.checkNullOrBlank(blogTemplate)) {
                addOperationResultMessage(context, getAdminResource(NO_TEMPLATE_NAME_KEY, NO_TEMPLATE_NAME_KEY, blog.getBlogAdministrationLocale()));
            }

            blogTemplate = sanitizeFilename(blogTemplate);
            File templateToDelete = new File(templatesDirectory, blogTemplate);
            _logger.debug("Deleting blog template: " + templateToDelete.toString());

            if (!templateToDelete.delete()) {
                addOperationResultMessage(context, formatAdminResource(UNABLE_TO_DELETE_TEMPLATE_KEY, UNABLE_TO_DELETE_TEMPLATE_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogTemplate}));
            } else {
                addOperationResultMessage(context, formatAdminResource(DELETED_TEMPLATE_KEY, DELETED_TEMPLATE_KEY, blog.getBlogAdministrationLocale(), new Object[]{blogTemplate}));

                putTemplatesInContext(templatesDirectory, context);
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_TEMPLATES_PAGE);
        }

        return entries;
    }
}
