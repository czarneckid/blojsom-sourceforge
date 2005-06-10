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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.BlojsomException;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.BlojsomProperties;
import org.blojsom.util.BlojsomConstants;

import java.io.*;
import java.util.Properties;

/**
 * FileBackedTrackback
 *
 * @author David Czarnecki
 * @version $Id: FileBackedTrackback.java,v 1.1 2005-06-10 02:16:24 czarneckid Exp $
 * @since blojsom 2.26
 */
public class FileBackedTrackback extends Trackback {

    private Log _logger = LogFactory.getLog(FileBackedTrackback.class);

    protected File _source;

    /**
     * Default constructor
     */
    public FileBackedTrackback() {
        super();
    }

    /**
     * Trackback constructor to take a title, excerpt, url, and blog name
     *
     * @param title    Title of the trackback
     * @param excerpt  Excerpt from the trackback
     * @param url      Url for the trackback
     * @param blogName Blog name of the trackback
     */
    public FileBackedTrackback(String title, String excerpt, String url, String blogName) {
        super(title, excerpt, url, blogName);
    }

    /**
     * Retrieve the source file for this trackback
     *
     * @return {@link File} backing this trackback
     */
    public File getSource() {
        return _source;
    }

    /**
     * Set the source file for this trackback
     *
     * @param source {@link File} backing this trackback
     */
    public void setSource(File source) {
        _source = source;
    }

    /**
     * Load the trackback
     *
     * @param blogUser {@link BlogUser}
     * @since blojsom 2.26
     */
    public void load(BlogUser blogUser) throws BlojsomException {
        if (_source == null) {
            throw new BlojsomException("No source file set for this trackback");
        }

        int trackbackSwitch = 0;
        setTrackbackDateLong(_source.lastModified());
        String trackbackLine;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(_source), blogUser.getBlog().getBlogFileEncoding()));
            while (((trackbackLine = br.readLine()) != null) && (trackbackSwitch < 4)) {
                switch (trackbackSwitch) {
                    case 0:
                        {
                            if (!"".equals(trackbackLine.trim())) {
                                setTitle(trackbackLine);
                            }
                            trackbackSwitch++;
                            break;
                        }
                    case 1:
                        {
                            if (!"".equals(trackbackLine.trim())) {
                                setExcerpt(trackbackLine);
                            }
                            trackbackSwitch++;
                            break;
                        }
                    case 2:
                        {
                            if (!"".equals(trackbackLine.trim())) {
                                setUrl(trackbackLine);
                            }
                            trackbackSwitch++;
                            break;
                        }
                    case 3:
                        {
                            if (!"".equals(trackbackLine.trim())) {
                                setBlogName(trackbackLine);
                            }
                            trackbackSwitch++;
                            break;
                        }
                }
            }

            setId(_source.getName());
            br.close();

            // Load trackback meta-data if available
            File trackbackMetaData = new File(BlojsomUtils.getFilename(_source.toString()) + ".meta");
            if (trackbackMetaData.exists()) {
                _logger.debug("Loading trackback meta-data: " + trackbackMetaData.toString());
                Properties trackbackMetaDataProperties = new BlojsomProperties();
                FileInputStream fis = new FileInputStream(trackbackMetaData);
                trackbackMetaDataProperties.load(fis);
                fis.close();
                setMetaData(BlojsomUtils.propertiesToMap(trackbackMetaDataProperties));
            }
        } catch (IOException e) {
            _logger.error(e);
        }
    }

    /**
     * Save the trackback
     *
     * @param blogUser {@link BlogUser}
     * @since blojsom 2.26
     */
    public void save(BlogUser blogUser) throws BlojsomException {
        if (_blogEntry == null) {
            throw new BlojsomException("Blog entry for this trackback not available");
        }

        Blog blog = blogUser.getBlog();
        long originalTimestamp = -1;
        File trackbackEntry;

        if (_source == null) {
            StringBuffer trackbackDirectory = new StringBuffer();
            String permalinkFilename = _blogEntry.getPermalink();
            permalinkFilename = BlojsomUtils.urlDecode(permalinkFilename);

            if (permalinkFilename == null) {
                _logger.debug("Invalid permalink trackback for: " + permalinkFilename);

                throw new BlojsomException("Invalid permalink trackback for: " + permalinkFilename);
            }

            trackbackDirectory.append(blog.getBlogHome());
            trackbackDirectory.append(BlojsomUtils.removeInitialSlash(_blogEntry.getCategory()));
            File blogEntry = new File(trackbackDirectory.toString() + File.separator + permalinkFilename);
            if (!blogEntry.exists()) {
                _logger.error("Trying to create trackback for invalid blog entry: " + permalinkFilename);

                throw new BlojsomException("Trying to create trackback for invalid blog entry: " + permalinkFilename);
            }

            trackbackDirectory.append(blog.getBlogTrackbackDirectory());
            trackbackDirectory.append(File.separator);
            trackbackDirectory.append(permalinkFilename);
            trackbackDirectory.append(File.separator);
            String trackbackFilename = trackbackDirectory.toString() + getTrackbackDateLong() + BlojsomConstants.TRACKBACK_EXTENSION;
            setId(getTrackbackDateLong() + BlojsomConstants.TRACKBACK_EXTENSION);
            File trackbackDir = new File(trackbackDirectory.toString());
            if (!trackbackDir.exists()) {
                if (!trackbackDir.mkdirs()) {
                    _logger.error("Could not create directory for trackbacks: " + trackbackDirectory);

                    throw new BlojsomException("Could not create directory for trackbacks: " + trackbackDirectory);
                }
            }
            trackbackEntry = new File(trackbackFilename);
        } else {
            trackbackEntry = _source;
            originalTimestamp = _source.lastModified();
        }

        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(trackbackEntry), blog.getBlogFileEncoding()));
            bw.write(BlojsomUtils.nullToBlank(getTitle()).trim());
            bw.newLine();
            bw.write(BlojsomUtils.nullToBlank(getExcerpt()).trim());
            bw.newLine();
            bw.write(BlojsomUtils.nullToBlank(getUrl()).trim());
            bw.newLine();
            bw.write(BlojsomUtils.nullToBlank(getBlogName()).trim());
            bw.newLine();
            bw.close();
            _logger.debug("Added trackback: " + trackbackEntry.getName());

            Properties trackbackMetaDataProperties = BlojsomUtils.mapToProperties(_metaData, BlojsomConstants.UTF8);
            String trackbackMetaDataFilename = BlojsomUtils.getFilename(trackbackEntry.toString()) + BlojsomConstants.DEFAULT_METADATA_EXTENSION;
            FileOutputStream fos = new FileOutputStream(new File(trackbackMetaDataFilename));
            trackbackMetaDataProperties.store(fos, null);
            fos.close();
            _logger.debug("Wrote trackback meta-data: " + trackbackMetaDataFilename);

            _source = trackbackEntry;

            if (originalTimestamp != -1) {
                _source.setLastModified(originalTimestamp);
                File trackbackMetaData = new File(trackbackMetaDataFilename);
                trackbackMetaData.setLastModified(originalTimestamp);
            }
        } catch (IOException e) {
            _logger.error(e);

            throw new BlojsomException(e);
        }
    }

    /**
     * Delete the trackback
     *
     * @param blogUser {@link BlogUser}
     * @since blojsom 2.26
     */
    public void delete(BlogUser blogUser) throws BlojsomException {
        Blog blog = blogUser.getBlog();

        if (_source == null || _blogEntry == null) {
            throw new BlojsomException("No source file found to delete trackback");
        }

        _logger.debug("Deleting trackback " + _source.getAbsolutePath());

        if (!_source.delete()) {
            throw new BlojsomException("Unable to delete trackback: " + getId());
        }

        // Delete meta-data
        File metaFile = new File(blog.getBlogHome() + _blogEntry.getCategory() + blog.getBlogTrackbackDirectory()
                + File.separatorChar + _blogEntry.getPermalink() + File.separatorChar + BlojsomUtils.getFilename(_source.getName())
                + blog.getBlogEntryMetaDataExtension());
        if (metaFile.exists()) {
            metaFile.delete();
        }
    }
}