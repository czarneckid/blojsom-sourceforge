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

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.FileItem;
import org.blojsom.blog.BlojsomConfiguration;
import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;
import org.blojsom.plugin.BlojsomPluginException;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.BlojsomException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.io.File;

/**
 * FileUploadPlugin
 * 
 * @author czarnecki
 * @version $Id: FileUploadPlugin.java,v 1.8 2003-12-23 01:57:00 czarneckid Exp $
 * @since blojsom 2.05
 */
public class FileUploadPlugin extends BaseAdminPlugin {

    private Log _logger = LogFactory.getLog(FileUploadPlugin.class);

    private static final String PLUGIN_ADMIN_UPLOAD_IP = "plugin-admin-upload";
    private static final String TEMPORARY_DIRECTORY_IP = "temporary-directory";
    private static final String DEFAULT_TEMPORARY_DIRECTORY = "/tmp";
    private static final String MAXIMUM_UPLOAD_SIZE_IP = "maximum-upload-size";
    private static final long DEFAULT_MAXIMUM_UPLOAD_SIZE = 100000;
    private static final String MAXIMUM_MEMORY_SIZE_IP = "maximum-memory-size";
    private static final int DEFAULT_MAXIMUM_MEMORY_SIZE = 50000;
    private static final String ACCEPTED_FILE_TYPES_IP = "accepted-file-types";
    private static final String[] DEFAULT_ACCEPTED_FILE_TYPES = {"image/jpeg", "image/gif", "image/png"};
    private static final String RESOURCES_DIRECTORY_IP = "resources-directory";
    private static final String DEFAULT_RESOURCES_DIRECTORY = "/resources/";

    // Pages
    private static final String FILE_UPLOAD_PAGE = "/org/blojsom/plugin/admin/templates/admin-file-upload";

    // Actions
    private static final String UPLOAD_FILE_ACTION = "upload-file";

    private String _temporaryDirectory;
    private long _maximumUploadSize;
    private int _maximumMemorySize;
    private Map _acceptedFileTypes;
    private String _resourcesDirectory;

    /**
     * Default constructor.
     */
    public FileUploadPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     * 
     * @param servletConfig        Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration {@link org.blojsom.blog.BlojsomConfiguration} information
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws BlojsomPluginException {
        super.init(servletConfig, blojsomConfiguration);

        try {
            Properties configurationProperties = BlojsomUtils.loadProperties(servletConfig, PLUGIN_ADMIN_UPLOAD_IP, true);
            _temporaryDirectory = configurationProperties.getProperty(TEMPORARY_DIRECTORY_IP);
            if (_temporaryDirectory == null || "".equals(_temporaryDirectory)) {
                _temporaryDirectory = DEFAULT_TEMPORARY_DIRECTORY;
            }
            _logger.debug("Using temporary directory: " + _temporaryDirectory);

            try {
                _maximumUploadSize = Long.parseLong(configurationProperties.getProperty(MAXIMUM_UPLOAD_SIZE_IP));
            } catch (NumberFormatException e) {
                _maximumUploadSize = DEFAULT_MAXIMUM_UPLOAD_SIZE;
            }
            _logger.debug("Using maximum upload size: " + _maximumUploadSize);

            try {
                _maximumMemorySize = Integer.parseInt(configurationProperties.getProperty(MAXIMUM_MEMORY_SIZE_IP));
            } catch (NumberFormatException e) {
                _maximumMemorySize = DEFAULT_MAXIMUM_MEMORY_SIZE;
            }
            _logger.debug("Using maximum memory size: " + _maximumMemorySize);

            String acceptedFileTypes = configurationProperties.getProperty(ACCEPTED_FILE_TYPES_IP);
            String[] parsedListOfTypes;
            if (acceptedFileTypes == null || "".equals(acceptedFileTypes)) {
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

            _resourcesDirectory = configurationProperties.getProperty(RESOURCES_DIRECTORY_IP);
            if (_resourcesDirectory == null || "".equals(_resourcesDirectory)) {
                _resourcesDirectory = DEFAULT_RESOURCES_DIRECTORY;
            }

            _resourcesDirectory = BlojsomUtils.checkStartingAndEndingSlash(_resourcesDirectory);
            _logger.debug("Using resources directory: " + _resourcesDirectory);
        } catch (BlojsomException e) {
            _logger.error(e);
            throw new BlojsomPluginException(e);
        }
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
        if (!authenticateUser(httpServletRequest, httpServletResponse, user.getBlog())) {
            httpServletRequest.setAttribute(PAGE_PARAM, ADMIN_LOGIN_PAGE);

            return entries;
        }

        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);
        if (BlojsomUtils.checkNullOrBlank(action)) {
            _logger.debug("User did not request edit action");

            httpServletRequest.setAttribute(PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
        } else if (PAGE_ACTION.equals(action)) {
            _logger.debug("User requested file upload page");

            httpServletRequest.setAttribute(PAGE_PARAM, FILE_UPLOAD_PAGE);
        } else if (UPLOAD_FILE_ACTION.equals(action)) {
            _logger.debug("User requested file upload action");

            // Create a new disk file upload and set its parameters
            DiskFileUpload diskFileUpload = new DiskFileUpload();
            diskFileUpload.setRepositoryPath(_temporaryDirectory);
            diskFileUpload.setSizeThreshold(_maximumMemorySize);
            diskFileUpload.setSizeMax(_maximumUploadSize);

            try {
                List items = diskFileUpload.parseRequest(httpServletRequest);
                Iterator itemsIterator = items.iterator();
                while (itemsIterator.hasNext()) {
                    FileItem item = (FileItem) itemsIterator.next();

                    // Check for the file upload form item
                    if (!item.isFormField()) {
                        _logger.debug("Found file item: " + item.getName() + " of type: " + item.getContentType());

                        // Is it one of the accepted file types?
                        String fileType = item.getContentType();
                        boolean isAcceptedFileType = _acceptedFileTypes.containsKey(fileType);

                        // If so, upload the file to the resources directory
                        if (isAcceptedFileType) {
                            File resourceDirectory = new File(_blojsomConfiguration.getInstallationDirectory() +
                                    _resourcesDirectory + user.getId() + "/");
                            if (!resourceDirectory.exists()) {
                                if (!resourceDirectory.mkdirs()) {
                                    _logger.error("Unable to create resource directory for user: " + resourceDirectory.toString());
                                    return entries;
                                }
                            }

                            File resourceFile = new File(_blojsomConfiguration.getInstallationDirectory() +
                                    _resourcesDirectory + user.getId() + "/" + item.getName());
                            try {
                                item.write(resourceFile);
                            } catch (Exception e) {
                                _logger.error(e);
                            }
                            _logger.debug("Successfully uploaded resource file: " + resourceFile.toString());
                        } else {
                            _logger.error("Upload file is not an accepted type: " + item.getName() + " of type: " + item.getContentType());
                        }
                    }
                }
            } catch (FileUploadException e) {
                _logger.error(e);
            }

            httpServletRequest.setAttribute(PAGE_PARAM, FILE_UPLOAD_PAGE);
        }

        return entries;
    }
}
