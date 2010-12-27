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
package org.blojsom.plugin.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.event.Event;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.event.Listener;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.event.ProcessEntryEvent;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * FileAttachmentPlugin-The idea behind this plugin is taken from RSSEnclosure plugin.
 *
 * @author Sopan Shewale
 * @version 1.0
 * @since Blojsom Version 3.0
 */
public class FileAttachmentPlugin implements Plugin, Listener {

    private Log _logger = LogFactory.getLog(FileAttachmentPlugin.class);
    private static final String FILE_ATTACHMENT_TEMPLATE = "org/blojsom/plugin/common/templates/admin-file-attachment.vm";
    private static final String FILE_ATTACHMENT = "FILE_ATTACHMENT";
    private static final String FILE_ATTACHMENT_URL = "FILE_ATTACHMENT_URL";

    private static final String METADATA_FILE_ATTACHMENT = "file-attachment";

    protected EventBroadcaster _eventBroadcaster;
    protected ServletConfig _servletConfig;
    protected Properties _blojsomProperties;
    protected String _resourcesDirectory;


    /**
     * Default constructor
     */
    public FileAttachmentPlugin() {
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
     * Set the {@link ServletConfig}
     *
     * @param servletConfig {@link ServletConfig}
     */
    public void setServletConfig(ServletConfig servletConfig) {
        _servletConfig = servletConfig;
    }

    /**
     * Set the {@link EventBroadcaster}
     *
     * @param eventBroadcaster {@link EventBroadcaster}
     */
    public void setEventBroadcaster(EventBroadcaster eventBroadcaster) {
        _eventBroadcaster = eventBroadcaster;
    }


    /**
     * Initialize this plugin. This method only called when the plugin is
     * instantiated.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error initializing the plugin
     */
    public void init() throws PluginException {
        _resourcesDirectory = _blojsomProperties.getProperty(BlojsomConstants.RESOURCES_DIRECTORY_IP, BlojsomConstants.DEFAULT_RESOURCES_DIRECTORY);
        _eventBroadcaster.addListener(this);

        _logger.debug("Initialized File Attachment plugin");
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
        ServletContext servletContext = _servletConfig.getServletContext();
        for (int i = 0; i < entries.length; i++) {
            Entry entry = entries[i];
            if (BlojsomUtils.checkMapForKey(entry.getMetaData(), METADATA_FILE_ATTACHMENT)) {
                String attachmentName = (String) entry.getMetaData().get(METADATA_FILE_ATTACHMENT);
                File attachment = new File(servletContext.getRealPath("/") + _resourcesDirectory + blog.getBlogId() + "/" + attachmentName);
                if (attachment.exists()) {
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Adding file-attachment to entry for file: " + attachmentName);
                    }
                    StringBuffer attachmentElement = new StringBuffer();
                    String attachmentname = attachment.getName();
                    String url = blog.getBlogBaseURL() + _resourcesDirectory + blog.getBlogId() + "/" + attachmentname;
                    attachmentElement.append("<a href=\"");
                    attachmentElement.append(url);
                    attachmentElement.append("\">");
                    attachmentElement.append(attachmentname);
                    attachmentElement.append("</a>");
                    entry.getMetaData().put(METADATA_FILE_ATTACHMENT, attachmentElement.toString());
                    _logger.debug("Added file attachment: " + attachmentElement.toString());
                }
            }
        }
        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws PluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error in finalizing this plugin
     */
    public void destroy() throws PluginException {
    }

    /**
     * Handle an event broadcast from another component
     *
     * @param event {@link org.blojsom.event.Event} to be handled
     */
    public void handleEvent(Event event) {
    }

    /**
     * Process an event from another component
     *
     * @param event {@link org.blojsom.event.Event} to be handled
     */
    public void processEvent(Event event) {
        if (event instanceof ProcessEntryEvent) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("Handling process blog entry event");
            }
            ProcessEntryEvent processBlogEntryEvent = (ProcessEntryEvent) event;
            String blogId = processBlogEntryEvent.getBlog().getBlogId();

            Map templateAdditions = (Map) processBlogEntryEvent.getContext().get("BLOJSOM_TEMPLATE_ADDITIONS");
            if (templateAdditions == null) {
                templateAdditions = new TreeMap();
            }

            templateAdditions.put(getClass().getName(), "#parse('" + FILE_ATTACHMENT_TEMPLATE + "')");
            processBlogEntryEvent.getContext().put("BLOJSOM_TEMPLATE_ADDITIONS", templateAdditions);

            // Create a list of files in the user's resource directory
            File resourceDirectory = new File(_servletConfig.getServletContext().getRealPath("/") + _resourcesDirectory + blogId + "/");
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
            // Preserve the current file attachment if none submitted
            if (processBlogEntryEvent.getEntry() != null) {
                String currentAttachment = (String) processBlogEntryEvent.getEntry().getMetaData().get(METADATA_FILE_ATTACHMENT);
                String currentAttachmentURL = (String) processBlogEntryEvent.getEntry().getMetaData().get(FILE_ATTACHMENT_URL);

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Current Attachments: " + currentAttachment);
                }
                processBlogEntryEvent.getContext().put(FILE_ATTACHMENT, currentAttachment);
                processBlogEntryEvent.getContext().put(FILE_ATTACHMENT_URL, BlojsomUtils.escapeBrackets(currentAttachmentURL));

            }
            String fileAttachment = BlojsomUtils.getRequestValue(METADATA_FILE_ATTACHMENT, processBlogEntryEvent.getHttpServletRequest());
            String fileAttachmentURL = BlojsomUtils.getRequestValue(FILE_ATTACHMENT_URL, processBlogEntryEvent.getHttpServletRequest());

            if (!BlojsomUtils.checkNullOrBlank(fileAttachmentURL) && (processBlogEntryEvent.getEntry() != null)) {
                processBlogEntryEvent.getEntry().getMetaData().put(fileAttachmentURL, fileAttachmentURL);
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Added/updated File Attachment (explict): " + fileAttachmentURL);
                }
            } else {
                if (processBlogEntryEvent.getEntry() != null) {
                    processBlogEntryEvent.getEntry().getMetaData().remove(FILE_ATTACHMENT_URL);
                }
                if (!BlojsomUtils.checkNullOrBlank(fileAttachment) && processBlogEntryEvent.getEntry() != null) {
                    processBlogEntryEvent.getEntry().getMetaData().put(METADATA_FILE_ATTACHMENT, fileAttachment);
                    processBlogEntryEvent.getContext().put(FILE_ATTACHMENT, fileAttachment);


                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Added/updated File Attachment: " + BlojsomUtils.getFilenameFromPath(fileAttachment));
                    }
                } else {
                    if (processBlogEntryEvent.getEntry() != null) {
                        processBlogEntryEvent.getEntry().getMetaData().remove(METADATA_FILE_ATTACHMENT);
                    }
                    processBlogEntryEvent.getContext().remove(FILE_ATTACHMENT);
                }
            }

            resourceFilesMap = new TreeMap(resourceFilesMap);
            processBlogEntryEvent.getContext().put("PLUGIN_FILE_ATTACHMENT_FILES", resourceFilesMap);
        }
    }


    /**
     * File Attachment
     *
     * @author Sopan Shewale
     */
    public class FileAttachment {
        private String url;
        private String name;

        /**
         * Contruct an File Attachment
         *
         * @param url  URL to retrive the attached file
         * @param name retrive the name of the File
         */
        public FileAttachment(String url, String name) {
            this.url = url;
            this.name = name;

        }

        /**
         * get the URL for the attachment
         *
         * @return URL for the attachment
         */
        public String getUrl() {
            return url;

        }

        /**
         * Get the name of the attachment
         *
         * @return Name of the attachment
         */
        public String getName() {
            return name;

        }

    }


}
