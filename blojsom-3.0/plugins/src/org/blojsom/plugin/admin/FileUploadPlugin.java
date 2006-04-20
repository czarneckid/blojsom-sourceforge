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

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.plugin.PluginException;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;

/**
 * FileUploadPlugin
 *
 * @author David Czarnecki
 * @version $Id: FileUploadPlugin.java,v 1.2 2006-04-20 15:40:44 czarneckid Exp $
 * @since blojsom 3.0
 */
public class FileUploadPlugin extends BaseAdminPlugin {

    private Log _logger = LogFactory.getLog(FileUploadPlugin.class);

    // Localization constants
    private static final String FAILED_PERMISSION_KEY = "file.upload.failed.permission.text";
    private static final String FAILED_RESOURCE_KEY = "file.upload.failed.resource.text";
    private static final String UNKNOWN_ERROR_KEY = "file.upload.unknown.error.text";
    private static final String SUCCESSFUL_UPLOAD_KEY = "successful.upload.text";
    private static final String INVALID_EXTENSION_KEY = "invalid.upload.extension.text";
    private static final String INVALID_TYPE_KEY = "invalid.upload.type.text";
    private static final String DELETED_FILES_KEY = "deleted.files.text";
    private static final String UPLOAD_LIMIT_KEY = "upload.limit.exceeded.text";

    private static final String RESOURCES_DIRECTORY_IP = "resources-directory";
    private static final String TEMPORARY_DIRECTORY_IP = "temporary-directory";
    private static final String DEFAULT_TEMPORARY_DIRECTORY = "/tmp";
    private static final String MAXIMUM_UPLOAD_SIZE_IP = "maximum-upload-size";
    private static final long DEFAULT_MAXIMUM_UPLOAD_SIZE = 100000;
    private static final String MAXIMUM_MEMORY_SIZE_IP = "maximum-memory-size";
    private static final int DEFAULT_MAXIMUM_MEMORY_SIZE = 50000;
    private static final String ACCEPTED_FILE_TYPES_IP = "accepted-file-types";
    private static final String[] DEFAULT_ACCEPTED_FILE_TYPES = {"image/jpeg", "image/gif", "image/png"};
    private static final String INVALID_FILE_EXTENSIONS_IP = "invalid-file-extensions";
    private static final String[] DEFAULT_INVALID_FILE_EXTENSIONS = {".jsp", ".jspf", ".jspi", ".jspx", ".php", ".cgi"};
    private static final String UPLOAD_QUOTA_ENABLED_IP = "upload-quota-enabled";
    private static final String UPLOAD_QUOTA_LIMIT_IP = "upload-quota-limit";
    private static final long DEFAULT_UPLOAD_QUOTA_LIMIT = 10485760;

    // Pages
    private static final String FILE_UPLOAD_PAGE = "/org/blojsom/plugin/admin/templates/admin-file-upload";

    // Constants
    private static final String PLUGIN_ADMIN_FILE_UPLOAD_FILES = "PLUGIN_ADMIN_FILE_UPLOAD_FILES";
    private static final String ACCEPTED_FILE_TYPES = "ACCEPTED_FILE_TYPES";
    private static final String INVALID_FILE_EXTENSIONS = "INVALID_FILE_EXTENSIONS";

    // Actions
    private static final String UPLOAD_FILE_ACTION = "upload-file";
    private static final String DELETE_UPLOAD_FILES = "delete-upload-files";

    // Form items
    private static final String FILE_TO_DELETE = "file-to-delete";

    // Permissions
    private static final String FILE_UPLOAD_PERMISSION = "file_upload_permission";

    private String _temporaryDirectory;
    private long _maximumUploadSize;
    private int _maximumMemorySize;
    private Map _acceptedFileTypes;
    private String _resourcesDirectory;
    private String[] _invalidFileExtensions;
    private boolean _uploadQuotaEnabled;
    private long _uploadQuotaLimit;
    private Properties _fileUploadProperties;

    /**
     * Default constructor.
     */
    public FileUploadPlugin() {
    }

    /**
     * Set the file upload properties
     *
     * @param fileUploadProperties File upload properties
     */
    public void setFileUploadProperties(Properties fileUploadProperties) {
        _fileUploadProperties = fileUploadProperties;
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error initializing the plugin
     */
    public void init() throws PluginException {
        super.init();

        _temporaryDirectory = _fileUploadProperties.getProperty(TEMPORARY_DIRECTORY_IP);
        if (BlojsomUtils.checkNullOrBlank(_temporaryDirectory)) {
            _temporaryDirectory = DEFAULT_TEMPORARY_DIRECTORY;
        }
        _logger.debug("Using temporary directory: " + _temporaryDirectory);

        try {
            _maximumUploadSize = Long.parseLong(_fileUploadProperties.getProperty(MAXIMUM_UPLOAD_SIZE_IP));
        } catch (NumberFormatException e) {
            _maximumUploadSize = DEFAULT_MAXIMUM_UPLOAD_SIZE;
        }
        _logger.debug("Using maximum upload size: " + _maximumUploadSize);

        try {
            _maximumMemorySize = Integer.parseInt(_fileUploadProperties.getProperty(MAXIMUM_MEMORY_SIZE_IP));
        } catch (NumberFormatException e) {
            _maximumMemorySize = DEFAULT_MAXIMUM_MEMORY_SIZE;
        }
        _logger.debug("Using maximum memory size: " + _maximumMemorySize);

        String acceptedFileTypes = _fileUploadProperties.getProperty(ACCEPTED_FILE_TYPES_IP);
        String[] parsedListOfTypes;
        if (BlojsomUtils.checkNullOrBlank(acceptedFileTypes)) {
            parsedListOfTypes = DEFAULT_ACCEPTED_FILE_TYPES;
        } else {
            parsedListOfTypes = BlojsomUtils.parseCommaList(acceptedFileTypes);
        }
        _acceptedFileTypes = new HashMap(parsedListOfTypes.length);
        for (int i = 0; i < parsedListOfTypes.length; i++) {
            String type = parsedListOfTypes[i];
            _acceptedFileTypes.put(type, type);
        }
        _logger.debug("Using accepted file types: " + BlojsomUtils.arrayOfStringsToString(parsedListOfTypes));

        _resourcesDirectory = _fileUploadProperties.getProperty(RESOURCES_DIRECTORY_IP);
        if (BlojsomUtils.checkNullOrBlank(_resourcesDirectory)) {
            _resourcesDirectory = BlojsomConstants.DEFAULT_RESOURCES_DIRECTORY;
        }

        _resourcesDirectory = BlojsomUtils.checkStartingAndEndingSlash(_resourcesDirectory);
        _logger.debug("Using resources directory: " + _resourcesDirectory);

        String invalidFileExtensionsProperty = _fileUploadProperties.getProperty(INVALID_FILE_EXTENSIONS_IP);
        if (BlojsomUtils.checkNullOrBlank(invalidFileExtensionsProperty)) {
            _invalidFileExtensions = DEFAULT_INVALID_FILE_EXTENSIONS;
        } else {
            _invalidFileExtensions = BlojsomUtils.parseCommaList(invalidFileExtensionsProperty);
        }
        _logger.debug("Using invalid file extensions: " + invalidFileExtensionsProperty);

        _uploadQuotaEnabled = Boolean.valueOf(_fileUploadProperties.getProperty(UPLOAD_QUOTA_ENABLED_IP)).booleanValue();
        if (_uploadQuotaEnabled) {
            String uploadQuotaLimit = _fileUploadProperties.getProperty(UPLOAD_QUOTA_LIMIT_IP);
            if (BlojsomUtils.checkNullOrBlank(uploadQuotaLimit)) {
                _uploadQuotaLimit = DEFAULT_UPLOAD_QUOTA_LIMIT;
            } else {
                try {
                    _uploadQuotaLimit = Long.parseLong(uploadQuotaLimit);
                    if (_uploadQuotaLimit <= 0) {
                        _uploadQuotaLimit = DEFAULT_UPLOAD_QUOTA_LIMIT;
                    }
                } catch (NumberFormatException e) {
                    _uploadQuotaLimit = DEFAULT_UPLOAD_QUOTA_LIMIT;
                }
            }

            _logger.debug("Upload limit enabled. Quota is : " + _uploadQuotaLimit + " bytes");
        }
    }

    /**
     * Return the size of a directory by getting the size of all its files and all of its directories files
     *
     * @param directory Directory whose size should be retrieved
     * @return Size of files in a directory or -1 if the original argument was not a directory
     */
    protected long getDirectorySize(File directory) {
        if (!directory.isDirectory()) {
            return -1;
        }

        long totalSize = 0;

        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];

            if (file.isDirectory()) {
                totalSize += getDirectorySize(file);
            } else {
                totalSize += file.length();
            }
        }

        return totalSize;
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
        if (!checkPermission(blog, null, username, FILE_UPLOAD_PERMISSION)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            addOperationResultMessage(context, getAdminResource(FAILED_PERMISSION_KEY, FAILED_PERMISSION_KEY, blog.getBlogAdministrationLocale()));

            return entries;
        }

        File resourceDirectory = new File(_servletConfig.getServletContext().getRealPath("/") + _resourcesDirectory + blog.getBlogId() + "/");

        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);
        if (BlojsomUtils.checkNullOrBlank(action)) {
            _logger.debug("User did not request edit action");

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
        } else if (PAGE_ACTION.equals(action)) {
            _logger.debug("User requested file upload page");

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, FILE_UPLOAD_PAGE);
        } else if (UPLOAD_FILE_ACTION.equals(action)) {
            _logger.debug("User requested file upload action");

            // Create a factory for disk-based file items
            DiskFileItemFactory factory = new DiskFileItemFactory();

            // Set factory constraints
            factory.setSizeThreshold(_maximumMemorySize);
            factory.setRepository(new File(_temporaryDirectory));

            ServletFileUpload upload = new ServletFileUpload(factory);

            // Set overall request size constraint
            upload.setSizeMax(_maximumUploadSize);

            try {
                List items = upload.parseRequest(httpServletRequest);
                Iterator itemsIterator = items.iterator();
                while (itemsIterator.hasNext()) {
                    FileItem item = (FileItem) itemsIterator.next();

                    // Check for the file upload form item
                    if (!item.isFormField()) {
                        String itemNameWithoutPath = BlojsomUtils.getFilenameFromPath(item.getName());

                        _logger.debug("Found file item: " + itemNameWithoutPath + " of type: " + item.getContentType());

                        // Is it one of the accepted file types?
                        String fileType = item.getContentType();
                        boolean isAcceptedFileType = _acceptedFileTypes.containsKey(fileType);

                        String extension = BlojsomUtils.getFileExtension(itemNameWithoutPath);
                        boolean isAcceptedFileExtension = true;
                        for (int i = 0; i < _invalidFileExtensions.length; i++) {
                            String invalidFileExtension = _invalidFileExtensions[i];
                            if (itemNameWithoutPath.indexOf(invalidFileExtension) != -1) {
                                isAcceptedFileExtension = false;
                                break;
                            }
                        }

                        if (_uploadQuotaEnabled) {
                            boolean overQuota = true;
                            long currentLimit = getDirectorySize(resourceDirectory);
                            if ((currentLimit != -1) && ((currentLimit + item.getSize() < _uploadQuotaLimit))) {
                                overQuota = false;
                            }

                            if (overQuota) {
                                _logger.error("Upload quota exceeded trying to upload file: " + itemNameWithoutPath);
                                addOperationResultMessage(context, formatAdminResource(UPLOAD_LIMIT_KEY, UPLOAD_LIMIT_KEY, blog.getBlogAdministrationLocale(), new Object[]{itemNameWithoutPath, new Long(_uploadQuotaLimit)}));

                                break;
                            }
                        }

                        // If so, upload the file to the resources directory
                        if (isAcceptedFileType && isAcceptedFileExtension) {
                            if (!resourceDirectory.exists()) {
                                if (!resourceDirectory.mkdirs()) {
                                    _logger.error("Unable to create resource directory for user: " + resourceDirectory.toString());
                                    addOperationResultMessage(context, getAdminResource(FAILED_RESOURCE_KEY, FAILED_RESOURCE_KEY, blog.getBlogAdministrationLocale()));
                                    return entries;
                                }
                            }

                            File resourceFile = new File(resourceDirectory, itemNameWithoutPath);
                            try {
                                item.write(resourceFile);
                            } catch (Exception e) {
                                _logger.error(e);
                                addOperationResultMessage(context, formatAdminResource(UNKNOWN_ERROR_KEY, UNKNOWN_ERROR_KEY, blog.getBlogAdministrationLocale(), new Object[]{e.getMessage()}));
                            }

                            String resourceURL = blog.getBlogBaseURL() + _resourcesDirectory + blog.getBlogId() + "/" + itemNameWithoutPath;

                            _logger.debug("Successfully uploaded resource file: " + resourceFile.toString());
                            addOperationResultMessage(context, formatAdminResource(SUCCESSFUL_UPLOAD_KEY, SUCCESSFUL_UPLOAD_KEY, blog.getBlogAdministrationLocale(), new Object[]{itemNameWithoutPath, resourceURL, itemNameWithoutPath}));
                        } else {
                            if (!isAcceptedFileExtension) {
                                _logger.error("Upload file does not have an accepted extension: " + extension);
                                addOperationResultMessage(context, formatAdminResource(INVALID_EXTENSION_KEY, INVALID_EXTENSION_KEY, blog.getBlogAdministrationLocale(), new Object[]{extension}));
                            } else {
                                _logger.error("Upload file is not an accepted type: " + itemNameWithoutPath + " of type: " + item.getContentType());
                                addOperationResultMessage(context, formatAdminResource(INVALID_TYPE_KEY, INVALID_TYPE_KEY, blog.getBlogAdministrationLocale(), new Object[]{item.getContentType()}));
                            }
                        }
                    }
                }
            } catch (FileUploadException e) {
                _logger.error(e);
                addOperationResultMessage(context, formatAdminResource(UNKNOWN_ERROR_KEY, UNKNOWN_ERROR_KEY, blog.getBlogAdministrationLocale(), new Object[]{e.getMessage()}));
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, FILE_UPLOAD_PAGE);
        } else if (DELETE_UPLOAD_FILES.equals(action)) {
            String[] filesToDelete = httpServletRequest.getParameterValues(FILE_TO_DELETE);
            int actualFilesDeleted = 0;
            if (filesToDelete != null && filesToDelete.length > 0) {
                File deletedFile;
                for (int i = 0; i < filesToDelete.length; i++) {
                    String fileToDelete = filesToDelete[i];
                    deletedFile = new File(resourceDirectory, fileToDelete);
                    if (!deletedFile.delete()) {
                        _logger.debug("Unable to delete resource file: " + deletedFile.toString());
                    } else {
                        actualFilesDeleted += 1;
                    }
                }

                addOperationResultMessage(context, formatAdminResource(DELETED_FILES_KEY, DELETED_FILES_KEY, blog.getBlogAdministrationLocale(), new Object[]{new Integer(actualFilesDeleted)}));
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, FILE_UPLOAD_PAGE);
        }

        // Create a list of files in the user's resource directory
        Map resourceFilesMap = null;
        if (resourceDirectory.exists()) {
            File[] resourceFiles = resourceDirectory.listFiles();

            if (resourceFiles != null) {
                resourceFilesMap = new HashMap(resourceFiles.length);
                for (int i = 0; i < resourceFiles.length; i++) {
                    File resourceFile = resourceFiles[i];
                    resourceFilesMap.put(resourceFile.getName(), resourceFile.getName());
                }
            }
        } else {
            resourceFilesMap = new HashMap();
        }

        resourceFilesMap = new TreeMap(resourceFilesMap);
        context.put(PLUGIN_ADMIN_FILE_UPLOAD_FILES, resourceFilesMap);
        context.put(ACCEPTED_FILE_TYPES, new TreeMap(_acceptedFileTypes));
        context.put(INVALID_FILE_EXTENSIONS, new TreeMap(BlojsomUtils.listToMap(BlojsomUtils.arrayToList(_invalidFileExtensions))));
        
        return entries;
    }
}
