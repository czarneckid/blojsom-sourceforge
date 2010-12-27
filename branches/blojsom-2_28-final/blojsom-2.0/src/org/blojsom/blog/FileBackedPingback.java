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
package org.blojsom.blog;

import org.blojsom.BlojsomException;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.BlojsomProperties;
import org.blojsom.util.BlojsomConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;

import java.io.*;
import java.util.Properties;

/**
 * FileBackedPingback
 *
 * @author David Czarnecki
 * @since blojsom 2.26
 * @version $Id: FileBackedPingback.java,v 1.3 2005-09-18 20:58:18 czarneckid Exp $
 */
public class FileBackedPingback extends Pingback {

    private transient Log _logger = LogFactory.getLog(FileBackedPingback.class);

    protected static final int PINGBACK_GENERIC_FAULT_CODE = 0;
    protected static final int PINGBACK_SOURCE_URI_NON_EXISTENT_CODE = 16;
    protected static final int PINGBACK_NO_LINK_TO_TARGET_URI_CODE = 17;
    protected static final int PINGBACK_TARGET_URI_NON_EXISTENT_CODE = 32;
    protected static final int PINGBACK_TARGET_URI_NOT_ENABLED_CODE = 33;
    protected static final int PINGBACK_ALREADY_REGISTERED_CODE = 48;
    protected static final int PINGBACK_ACCESS_DENIED_CODE = 49;
    protected static final int PINGBACK_UPSTREAM_SERVER_ERROR_CODE = 50;

    protected transient File _source;

    /**
     * Default constructor
     */
    public FileBackedPingback() {
        super();
    }

    /**
     * Pingback constructor to take a title, excerpt, url, and blog name
     *
     * @param title    Title of the pingback
     * @param excerpt  Excerpt from the pingback
     * @param url      Url for the pingback
     * @param blogName Blog name of the pingback
     */
    public FileBackedPingback(String title, String excerpt, String url, String blogName) {
        super(title, excerpt, url, blogName);
    }

    /**
     * Retrieve the source file for this pingback
     *
     * @return {@link File} backing this pingback
     */
    public File getSource() {
        return _source;
    }

    /**
     * Set the source file for this pingback
     *
     * @param source {@link File} backing this pingback
     */
    public void setSource(File source) {
        _source = source;
    }

    /**
     * Load the pingback
     *
     * @param blogUser {@link BlogUser}
     * @since blojsom 2.26
     */
    public void load(BlogUser blogUser) throws BlojsomException {
        if (_source == null) {
            throw new BlojsomException("No source file set for this pingback");
        }

        int pingbackSwitch = 0;
        setTrackbackDateLong(_source.lastModified());
        StringBuffer pingbackExcerpt = new StringBuffer();
        String pingbackLine;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(_source), blogUser.getBlog().getBlogFileEncoding()));
            while (((pingbackLine = br.readLine()) != null) && (pingbackSwitch < 4)) {
                switch (pingbackSwitch) {
                    case 0:
                        {
                            if (!"".equals(pingbackLine.trim())) {
                                setTitle(pingbackLine);
                            }
                            pingbackSwitch++;
                            break;
                        }
                    case 1:
                        {
                            if (!"".equals(pingbackLine.trim())) {
                                setUrl(pingbackLine);
                            }
                            pingbackSwitch++;
                            break;
                        }
                    case 2:
                        {
                            if (!"".equals(pingbackLine.trim())) {
                                setBlogName(pingbackLine);
                            }
                            pingbackSwitch++;
                            break;
                        }
                    default:
                        {
                            pingbackExcerpt.append(pingbackLine).append(BlojsomUtils.LINE_SEPARATOR);
                        }
                }
            }

            setExcerpt(pingbackExcerpt.toString());
            setId(_source.getName());
            br.close();

            // Load pingback meta-data if available
            File pingbackMetaData = new File(BlojsomUtils.getFilename(_source.toString()) + ".meta");
            if (pingbackMetaData.exists()) {
                _logger.debug("Loading pingback meta-data: " + pingbackMetaData.toString());
                Properties pingbackMetaDataProperties = new BlojsomProperties(true);
                FileInputStream fis = new FileInputStream(pingbackMetaData);
                pingbackMetaDataProperties.load(fis);
                fis.close();
                setMetaData(BlojsomUtils.propertiesToMap(pingbackMetaDataProperties));
            }
        } catch (IOException e) {
            _logger.error(e);
        }
    }

    /**
     * Save the pingback
     *
     * @param blogUser {@link BlogUser}
     * @since blojsom 2.26
     */
    public void save(BlogUser blogUser) throws BlojsomException {
        if (_blogEntry == null) {
            throw new BlojsomException("Blog entry for this pingback not available");
        }

        Blog blog = blogUser.getBlog();
        long originalTimestamp = -1;
        originalTimestamp = _trackbackDateLong;

        StringBuffer pingbackDirectory = new StringBuffer();
        String permalinkFilename = _blogEntry.getPermalink();
        permalinkFilename = BlojsomUtils.urlDecode(permalinkFilename);
        if (permalinkFilename == null) {
            _logger.error("Invalid permalink pingback for: " + permalinkFilename);

            throw new BlojsomException("Invalid permalink pingback for: " + permalinkFilename, new XmlRpcException(PINGBACK_TARGET_URI_NON_EXISTENT_CODE, "Target URI does not exist"));
        }

        pingbackDirectory.append(blog.getBlogHome());
        pingbackDirectory.append(BlojsomUtils.removeInitialSlash(_blogEntry.getCategory()));
        File blogEntry = new File(pingbackDirectory.toString() + File.separator + permalinkFilename);
        _logger.debug("Directory: " + blogEntry.toString());
        if (!blogEntry.exists()) {
            _logger.error("Trying to create pingback for invalid blog entry: " + permalinkFilename);

            throw new BlojsomException("Trying to create pingback for invalid blog entry: " + permalinkFilename, new XmlRpcException(PINGBACK_TARGET_URI_NON_EXISTENT_CODE, "Target URI does not exist"));
        }

        pingbackDirectory.append(blog.getBlogPingbacksDirectory());
        pingbackDirectory.append(File.separator);
        pingbackDirectory.append(permalinkFilename);
        pingbackDirectory.append(File.separator);
        String pingbackFilename = pingbackDirectory.toString() + _id + BlojsomConstants.PINGBACK_EXTENSION;

        File pingbackDir = new File(pingbackDirectory.toString());
        if (!pingbackDir.exists()) {
            if (!pingbackDir.mkdirs()) {
                _logger.error("Could not create directory for pingbacks: " + pingbackDirectory);

                throw new BlojsomException("Could not create directory for pingbacks: " + pingbackDirectory, new XmlRpcException(PINGBACK_ACCESS_DENIED_CODE, "Access denied"));
            }
        }

        File pingbackEntry = new File(pingbackFilename);
        if (pingbackEntry.exists()) {
            _logger.debug("Pingback already registered");

            throw new BlojsomException("Pingback already registered", new XmlRpcException(PINGBACK_ALREADY_REGISTERED_CODE, "Pingback already registered"));
        }

        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pingbackEntry), blog.getBlogFileEncoding()));
            bw.write(BlojsomUtils.nullToBlank(getTitle()).trim());
            bw.newLine();
            bw.write(BlojsomUtils.nullToBlank(getUrl()).trim());
            bw.newLine();
            bw.write(BlojsomUtils.nullToBlank(getBlogName()).trim());
            bw.newLine();
            bw.write(BlojsomUtils.nullToBlank(getExcerpt()).trim());
            bw.newLine();
            bw.close();

            _logger.debug("Added pingback: " + pingbackFilename);

            Properties pingbackMetaDataProperties = BlojsomUtils.mapToProperties(_metaData, BlojsomConstants.UTF8);
            String pingbackMetaDataFilename = BlojsomUtils.getFilename(pingbackEntry.toString()) + BlojsomConstants.DEFAULT_METADATA_EXTENSION;
            FileOutputStream fos = new FileOutputStream(new File(pingbackMetaDataFilename));
            pingbackMetaDataProperties.store(fos, null);
            fos.close();

            _logger.debug("Wrote pingback meta-data: " + pingbackMetaDataFilename);

            _source = pingbackEntry;

            if (originalTimestamp != -1) {
                _source.setLastModified(originalTimestamp);
                File pingbackMetaData = new File(pingbackMetaDataFilename);
                pingbackMetaData.setLastModified(originalTimestamp);
            }
        } catch (IOException e) {
            _logger.error(e);

            throw new BlojsomException(e.getMessage(), new XmlRpcException(PINGBACK_GENERIC_FAULT_CODE, "Unknown exception occurred"));
        }
    }

    /**
     * Delete the pingback
     *
     * @param blogUser {@link BlogUser}
     * @since blojsom 2.26
     */
    public void delete(BlogUser blogUser) throws BlojsomException {
        Blog blog = blogUser.getBlog();

        if (_source == null || _blogEntry == null) {
            throw new BlojsomException("No source file found to delete pingback");
        }

        _logger.debug("Deleting pingback " + _source.getAbsolutePath());

        if (!_source.delete()) {
            throw new BlojsomException("Unable to delete pingback: " + getId());
        }

        // Delete meta-data
        File metaFile = new File(blog.getBlogHome() + _blogEntry.getCategory() + blog.getBlogPingbacksDirectory()
                + File.separatorChar + _blogEntry.getPermalink() + File.separatorChar + BlojsomUtils.getFilename(_source.getName())
                + blog.getBlogEntryMetaDataExtension());
        if (metaFile.exists()) {
            metaFile.delete();
        }
    }
}