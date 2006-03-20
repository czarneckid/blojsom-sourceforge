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
package org.blojsom.plugin.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Entry;
import org.blojsom.blog.Blog;
import org.blojsom.event.Event;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.event.Listener;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.event.ProcessEntryEvent;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.BlojsomConstants;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Properties;

/**
 * RSSEnclosurePlugin
 *
 * @author David Czarnecki
 * @version $Id: RSSEnclosurePlugin.java,v 1.1 2006-03-20 21:30:54 czarneckid Exp $
 * @since blojsom 3.0
 */
public class RSSEnclosurePlugin implements Plugin, Listener {

    private Log _logger = LogFactory.getLog(RSSEnclosurePlugin.class);

    private static final String RSS_ENCLOSURE_TEMPLATE = "org/blojsom/plugin/common/templates/admin-rss-enclosure-attachment.vm";
    private static final String RSS_ENCLOSURE_ATTACHMENT = "RSS_ENCLOSURE_ATTACHMENT";
    private static final String RSS_ENCLOSURE_URL_ITEM = "RSS_ENCLOSURE_URL_ITEM";
    private static final String RSS_ENCLOSURE_LENGTH_ITEM = "RSS_ENCLOSURE_LENGTH_ITEM";
    private static final String RSS_ENCLOSURE_TYPE_ITEM = "RSS_ENCLOSURE_TYPE_ITEM";
    private static final String RSS_ENCLOSURE_LINK_TEMPLATE = "<enclosure url=\"{0}\" length=\"{1}\" type=\"{2}\" />";

    protected static final String MIME_TYPE_XMPEGURL = "audio/x-mpegurl m3u";
    protected static final String MIME_TYPE_XMPEG = "audio/x-mpeg mp1 mp2 mp3 mpa mpega";

    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    public static final String METADATA_RSS_ENCLOSURE = "rss-enclosure";
    public static final String METADATA_ESS_ENCLOSURE_OBJECT = "rss-enclosure-object";

    public static final String RSS_ENCLOSURE_URL = "rss-enclosure-url";
    public static final String RSS_ENCLOSURE_LENGTH = "rss-enclosure-length";
    public static final String RSS_ENCLOSURE_TYPE = "rss-enclosure-type";

    protected EventBroadcaster _eventBroadcaster;
    protected ServletConfig _servletConfig;
    protected Properties _blojsomProperties;

    protected String _resourcesDirectory;

    /**
     * Default constructor
     */
    public RSSEnclosurePlugin() {
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

        _logger.debug("Initialized RSS enclosures plugin");
    }

    /**
     * Add additional mime types to the map
     *
     * @param mimeTypes Mime types map
     */
    protected void addAdditionalMimeTypes(MimetypesFileTypeMap mimeTypes) {
        mimeTypes.addMimeTypes(MIME_TYPE_XMPEGURL);
        mimeTypes.addMimeTypes(MIME_TYPE_XMPEG);
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
            if (BlojsomUtils.checkMapForKey(entry.getMetaData(), RSS_ENCLOSURE_URL) && BlojsomUtils.checkMapForKey(entry.getMetaData(), RSS_ENCLOSURE_LENGTH) && BlojsomUtils.checkMapForKey(entry.getMetaData(), RSS_ENCLOSURE_TYPE))
            {
                String rssEnclosureURL = (String) entry.getMetaData().get(RSS_ENCLOSURE_URL);
                long rssEnclosureLength = -1;
                try {
                    rssEnclosureLength = Long.parseLong((String) entry.getMetaData().get(RSS_ENCLOSURE_LENGTH));
                } catch (NumberFormatException e) {
                    _logger.error(e);
                }
                String rssEnclosureType = (String) entry.getMetaData().get(RSS_ENCLOSURE_TYPE);

                String rssEnclosure = MessageFormat.format(RSS_ENCLOSURE_LINK_TEMPLATE, new Object[]{rssEnclosureURL, Long.toString(rssEnclosureLength), rssEnclosureType});
                RSSEnclosure rssEnclosureObject = new RSSEnclosure(rssEnclosureURL, rssEnclosureLength, rssEnclosureType);

                entry.getMetaData().put(METADATA_RSS_ENCLOSURE, rssEnclosure);
                entry.getMetaData().put(METADATA_ESS_ENCLOSURE_OBJECT, rssEnclosureObject);
                _logger.debug("Added explicit enclosure: " + rssEnclosure);
            } else {
                if (BlojsomUtils.checkMapForKey(entry.getMetaData(), METADATA_RSS_ENCLOSURE)) {
                    String enclosureName = BlojsomUtils.getFilenameFromPath((String) entry.getMetaData().get(METADATA_RSS_ENCLOSURE));
                    File enclosure = new File(servletContext.getRealPath("/") + _resourcesDirectory + blog.getBlogId() + "/" + enclosureName);
                    if (enclosure.exists()) {
                        _logger.debug("Adding enclosure to entry for file: " + enclosureName);

                        MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
                        addAdditionalMimeTypes(mimetypesFileTypeMap);
                        String type = mimetypesFileTypeMap.getContentType(enclosure);

                        StringBuffer enclosureElement = new StringBuffer();
                        String url = blog.getBlogBaseURL() + _resourcesDirectory + blog.getBlogId() + "/" + enclosure.getName();
                        enclosureElement.append("<enclosure url=\"");
                        enclosureElement.append(url);
                        enclosureElement.append("\" length=\"");
                        enclosureElement.append(enclosure.length());
                        enclosureElement.append("\" type=\"");
                        if (BlojsomUtils.checkNullOrBlank(type)) {
                            type = DEFAULT_MIME_TYPE;
                        }
                        enclosureElement.append(type);
                        enclosureElement.append("\" />");

                        RSSEnclosure rssEnclosure = new RSSEnclosure(url, enclosure.length(), type);
                        entry.getMetaData().put(METADATA_RSS_ENCLOSURE, enclosureElement.toString());
                        entry.getMetaData().put(METADATA_ESS_ENCLOSURE_OBJECT, rssEnclosure);
                        _logger.debug("Added enclosure: " + enclosureElement.toString());
                    }
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
            _logger.debug("Handling process blog entry event");

            ProcessEntryEvent processBlogEntryEvent = (ProcessEntryEvent) event;
            String blogId = processBlogEntryEvent.getBlog().getBlogId();

            Map templateAdditions = (Map) processBlogEntryEvent.getContext().get("BLOJSOM_TEMPLATE_ADDITIONS");
            if (templateAdditions == null) {
                templateAdditions = new TreeMap();
            }

            templateAdditions.put(getClass().getName(), "#parse('" + RSS_ENCLOSURE_TEMPLATE + "')");
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

            // Preserve the current rss enclosure if none submitted
            if (processBlogEntryEvent.getEntry() != null) {
                String currentEnclosure = (String) processBlogEntryEvent.getEntry().getMetaData().get(METADATA_RSS_ENCLOSURE);
                String currentEnclosureURL = (String) processBlogEntryEvent.getEntry().getMetaData().get(RSS_ENCLOSURE_URL);
                String currentEnclosureLength = (String) processBlogEntryEvent.getEntry().getMetaData().get(RSS_ENCLOSURE_LENGTH);
                String currentEnclosureType = (String) processBlogEntryEvent.getEntry().getMetaData().get(RSS_ENCLOSURE_TYPE);

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Current enclosure: " + currentEnclosure);
                }
                processBlogEntryEvent.getContext().put(RSS_ENCLOSURE_ATTACHMENT, currentEnclosure);
                processBlogEntryEvent.getContext().put(RSS_ENCLOSURE_URL_ITEM, currentEnclosureURL);
                processBlogEntryEvent.getContext().put(RSS_ENCLOSURE_LENGTH_ITEM, currentEnclosureLength);
                processBlogEntryEvent.getContext().put(RSS_ENCLOSURE_TYPE_ITEM, currentEnclosureType);
            }

            String rssEnclosure = BlojsomUtils.getRequestValue(METADATA_RSS_ENCLOSURE, processBlogEntryEvent.getHttpServletRequest());
            String rssEnclosureURL = BlojsomUtils.getRequestValue(RSS_ENCLOSURE_URL, processBlogEntryEvent.getHttpServletRequest());
            String rssEnclosureLength = BlojsomUtils.getRequestValue(RSS_ENCLOSURE_LENGTH, processBlogEntryEvent.getHttpServletRequest());
            String rssEnclosureType = BlojsomUtils.getRequestValue(RSS_ENCLOSURE_TYPE, processBlogEntryEvent.getHttpServletRequest());

            if (!BlojsomUtils.checkNullOrBlank(rssEnclosureURL) && (processBlogEntryEvent.getEntry() != null)) {
                processBlogEntryEvent.getEntry().getMetaData().put(RSS_ENCLOSURE_URL, rssEnclosureURL);
                if (BlojsomUtils.checkNullOrBlank(rssEnclosureLength) && BlojsomUtils.checkNullOrBlank(rssEnclosureType)) {
                    String[] enclosureProperties = discoverEnclosureProperties(rssEnclosureURL);
                    rssEnclosureLength = enclosureProperties[0];
                    rssEnclosureType = enclosureProperties[1];
                }

                processBlogEntryEvent.getEntry().getMetaData().put(RSS_ENCLOSURE_LENGTH, rssEnclosureLength);
                processBlogEntryEvent.getEntry().getMetaData().put(RSS_ENCLOSURE_TYPE, rssEnclosureType);
                processBlogEntryEvent.getContext().put(RSS_ENCLOSURE_URL_ITEM, rssEnclosureURL);
                processBlogEntryEvent.getContext().put(RSS_ENCLOSURE_LENGTH_ITEM, rssEnclosureLength);
                processBlogEntryEvent.getContext().put(RSS_ENCLOSURE_TYPE_ITEM, rssEnclosureType);

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Added/updated RSS enclosure (explict): " + rssEnclosureURL);
                }
            } else {
                if (processBlogEntryEvent.getEntry() != null) {
                    processBlogEntryEvent.getEntry().getMetaData().remove(RSS_ENCLOSURE_URL);
                    processBlogEntryEvent.getEntry().getMetaData().remove(RSS_ENCLOSURE_TYPE);
                    processBlogEntryEvent.getEntry().getMetaData().remove(RSS_ENCLOSURE_LENGTH);
                }

                processBlogEntryEvent.getContext().remove(RSS_ENCLOSURE_URL_ITEM);
                processBlogEntryEvent.getContext().remove(RSS_ENCLOSURE_TYPE_ITEM);
                processBlogEntryEvent.getContext().remove(RSS_ENCLOSURE_LENGTH_ITEM);

                if (!BlojsomUtils.checkNullOrBlank(rssEnclosure) && processBlogEntryEvent.getEntry() != null) {
                    processBlogEntryEvent.getEntry().getMetaData().put(METADATA_RSS_ENCLOSURE, rssEnclosure);
                    processBlogEntryEvent.getContext().put(RSS_ENCLOSURE_ATTACHMENT, rssEnclosure);

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Added/updated RSS enclosure: " + BlojsomUtils.getFilenameFromPath(rssEnclosure));
                    }
                } else {
                    if (processBlogEntryEvent.getEntry() != null) {
                        processBlogEntryEvent.getEntry().getMetaData().remove(METADATA_RSS_ENCLOSURE);
                    }
                    processBlogEntryEvent.getContext().remove(RSS_ENCLOSURE_ATTACHMENT);
                }
            }

            resourceFilesMap = new TreeMap(resourceFilesMap);
            processBlogEntryEvent.getContext().put("PLUGIN_RSS_ENCLOSURE_FILES", resourceFilesMap);
        }
    }

    /**
     * Discover the content length and content type for an enclosure URL
     *
     * @param rssEnclosureURL URL for enclosure
     * @return String array containing the enclosure's content length and content type
     */
    protected String[] discoverEnclosureProperties(String rssEnclosureURL) {
        String[] enclosureProperties = new String[]{"", ""};

        try {
            if (!rssEnclosureURL.toLowerCase().startsWith("http://")) {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("RSS enclosure URL not an HTTP-accessible resource");
                }
            } else {
                URL enclosure = new URL(rssEnclosureURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) enclosure.openConnection();
                httpURLConnection.setRequestMethod("HEAD");
                httpURLConnection.connect();

                enclosureProperties[0] = Integer.toString(httpURLConnection.getContentLength());
                enclosureProperties[1] = httpURLConnection.getContentType();

                httpURLConnection.disconnect();
            }
        } catch (Exception e) {
            if (_logger.isErrorEnabled()) {
                _logger.error("Error retrieving enclosure properties", e);
            }
        }

        return enclosureProperties;
    }

    /**
     * RSS Enclosure
     *
     * @author David Czarnecki
     */
    public class RSSEnclosure {

        private String url;
        private long length;
        private String type;

        /**
         * Construct an RSS enclosure
         *
         * @param url    URL to retrieve enclosure
         * @param length Length of enclosure
         * @param type   Type of enclosure
         */
        public RSSEnclosure(String url, long length, String type) {
            this.url = url;
            this.length = length;
            this.type = type;
        }

        /**
         * Get the URL for the enclosure
         *
         * @return URL for enclosure
         */
        public String getUrl() {
            return url;
        }

        /**
         * Get the length of the enclosure
         *
         * @return Length of enclosure
         */
        public long getLength() {
            return length;
        }

        /**
         * Get the type of the enclosure
         *
         * @return Type of enclosure (e.g. audio/mpeg)
         */
        public String getType() {
            return type;
        }
    }
}

