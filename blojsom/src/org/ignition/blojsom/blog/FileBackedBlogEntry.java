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
package org.ignition.blojsom.blog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ignition.blojsom.util.BlojsomUtils;

import java.io.*;
import java.util.*;

/**
 * FileBackedBlogEntry
 *
 * @author David Czarnecki
 * @version $Id: FileBackedBlogEntry.java,v 1.3 2003-05-07 02:27:56 czarneckid Exp $
 * @since blojsom 1.8
 */
public class FileBackedBlogEntry extends BlogEntry {

    private Log _logger = LogFactory.getLog(FileBackedBlogEntry.class);

    private File _source;
    private String _commentsDirectory;
    private String _trackbacksDirectory;

    /**
     * Create a new blog entry with no data
     */
    public FileBackedBlogEntry() {
        _commentsDirectory = DEFAULT_COMMENTS_DIRECTORY;
        _trackbacksDirectory = DEFAULT_TRACKBACK_DIRECTORY;
    }

    /**
     * Create a new blog entry
     *
     * @param title Entry title
     * @param link Permalink to the entry
     * @param description Entry description
     * @param source File containing the blog entry
     */
    public FileBackedBlogEntry(String title, String link, String description, File source) {
        super();
        _title = title;
        _link = link;
        _description = description;
        _source = source;
        _entryDate = new Date(_source.lastModified());
    }

    /**
     * File containing the blog entry
     *
     * @return Blog entry file
     */
    public File getSource() {
        return _source;
    }

    /**
     * Set the file of the blog entry
     *
     * @param source File for the blog entry
     */
    public void setSource(File source) {
        _source = source;
    }

    /**
     * Returns the BlogId  for this entry
     *
     * @return Blog Id
     */
    public String getId() {
        return _category + "?" + PERMALINK_PARAM + "=" + BlojsomUtils.urlEncode(_source.getName());
    }

    /**
     * Return the permalink name for this blog entry
     *
     * @return Permalink name
     */
    public String getPermalink() {
        return BlojsomUtils.urlEncode(_source.getName());
    }

    /**
     * Reload the blog entry from disk
     *
     * The first line of the blog entry will be used as the title of the blog
     */
    public void reloadSource() throws IOException {
        boolean hasLoadedTitle = false;
        String lineSeparator = System.getProperty("line.separator");

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(_source), UTF8));
            String line;
            StringBuffer description = new StringBuffer();
            while ((line = br.readLine()) != null) {
                if (!hasLoadedTitle) {
                    _title = line;
                    hasLoadedTitle = true;
                } else {
                    description.append(line);
                    description.append(lineSeparator);
                }
            }
            br.close();
            _description = description.toString();
            _entryDate = new Date(_source.lastModified());
            _lastModified = _source.lastModified();
        } catch (IOException e) {
            _logger.error(e);
        }
    }

    /**
     * Determines whether or not this blog entry supports comments by testing to see if
     * the blog entry is writable.
     *
     * @return <code>true</code> if the blog entry is writable, <code>false</code> otherwise
     */
    public boolean supportsComments() {
        return _source.canWrite();
    }

    /**
     * Set the directory for comments
     *
     * @param commentsDirectory Comments directory
     */
    public void setCommentsDirectory(String commentsDirectory) {
        _commentsDirectory = commentsDirectory;
    }

    /**
     * Convenience method to load the comments for this blog entry. A blog entry can have
     */
    public void loadComments() {
        if (supportsComments()) {
            String commentsDirectoryPath;
            if (_source.getParent() == null) {
                commentsDirectoryPath = File.separator + _commentsDirectory + File.separator + _source.getName();
            } else {
                commentsDirectoryPath = _source.getParent() + File.separator + _commentsDirectory + File.separator + _source.getName();
            }
            File commentsDirectory = new File(commentsDirectoryPath);
            File[] comments = commentsDirectory.listFiles(BlojsomUtils.getExtensionFilter(COMMENT_EXTENSION));
            if ((comments != null) && (comments.length > 0)) {
                _logger.debug("Adding " + comments.length + " comments to blog entry: " + getPermalink());
                Arrays.sort(comments, BlojsomUtils.FILE_TIME_ASCENDING_COMPARATOR);
                _comments = new ArrayList(comments.length);
                for (int i = 0; i < comments.length; i++) {
                    File comment = comments[i];
                    _comments.add(loadComment(comment));
                }
            }
        } else {
            _logger.debug("Blog entry does not support comments");
        }
    }

    /**
     * Load a comment for this blog entry from disk
     * Comments must always have the form:<br />
     * author<br>
     * author e-mail address<br />
     * author url<br />
     * everything else after is the comment
     *
     * @param commentFile Comment file
     * @return BlogComment Blog comment loaded from disk
     */
    protected BlogComment loadComment(File commentFile) {
        int commentSwitch = 0;
        BlogComment comment = new BlogComment();
        comment.setCommentDateLong(commentFile.lastModified());
        StringBuffer commentDescription = new StringBuffer();
        String commentLine;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(commentFile), UTF8));
            while ((commentLine = br.readLine()) != null) {
                switch (commentSwitch) {
                    case 0:
                        {
                            comment.setAuthor(commentLine);
                            commentSwitch++;
                            break;
                        }
                    case 1:
                        {
                            comment.setAuthorEmail(commentLine);
                            commentSwitch++;
                            break;
                        }
                    case 2:
                        {
                            comment.setAuthorURL(commentLine);
                            commentSwitch++;
                            break;
                        }
                    default:
                        {
                            commentDescription.append(commentLine);
                        }
                }
            }
            comment.setComment(commentDescription.toString());
            comment.setCommentDate(new Date(commentFile.lastModified()));
            br.close();
        } catch (IOException e) {
            _logger.error(e);
        }
        return comment;
    }

    /**
     * Set the directory for trackbacks
     *
     * @param trackbacksDirectory Trackbacks directory
     */
    public void setTrackbacksDirectory(String trackbacksDirectory) {
        _trackbacksDirectory = trackbacksDirectory;
    }

    /**
     * Convenience method to load the trackbacks for this blog entry.
     */
    public void loadTrackbacks() {
        String trackbacksDirectoryPath;
        if (_source.getParent() == null) {
            trackbacksDirectoryPath = File.separator + _trackbacksDirectory + File.separator + _source.getName();
        } else {
            trackbacksDirectoryPath = _source.getParent() + File.separator + _trackbacksDirectory + File.separator + _source.getName();
        }
        File trackbacksDirectory = new File(trackbacksDirectoryPath);
        File[] trackbacks = trackbacksDirectory.listFiles(BlojsomUtils.getExtensionFilter(TRACKBACK_EXTENSION));
        if ((trackbacks != null) && (trackbacks.length > 0)) {
            _logger.debug("Adding " + trackbacks.length + " trackbacks to blog entry: " + getPermalink());
            Arrays.sort(trackbacks, BlojsomUtils.FILE_TIME_ASCENDING_COMPARATOR);
            _trackbacks = new ArrayList(trackbacks.length);
            for (int i = 0; i < trackbacks.length; i++) {
                File trackbackFile = trackbacks[i];
                _trackbacks.add(loadTrackback(trackbackFile));
            }
        }
    }

    /**
     * Load a trackback for this blog entry from disk
     * Trackbacks must always have the form:<br />
     * title<br>
     * excerpt<br />
     * url<br />
     * blog_name
     *
     * @param trackbackFile Trackback file
     * @return Trackback Trackback loaded from disk
     */
    protected Trackback loadTrackback(File trackbackFile) {
        int trackbackSwitch = 0;
        Trackback trackback = new Trackback();
        trackback.setTrackbackDateLong(trackbackFile.lastModified());
        String trackbackLine;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(trackbackFile), UTF8));
            while (((trackbackLine = br.readLine()) != null) && (trackbackSwitch < 4)) {
                switch (trackbackSwitch) {
                    case 0:
                        {
                            if (!"".equals(trackbackLine.trim())) {
                                trackback.setTitle(trackbackLine);
                            }
                            trackbackSwitch++;
                            break;
                        }
                    case 1:
                        {
                            if (!"".equals(trackbackLine.trim())) {
                                trackback.setExcerpt(trackbackLine);
                            }
                            trackbackSwitch++;
                            break;
                        }
                    case 2:
                        {
                            if (!"".equals(trackbackLine.trim())) {
                                trackback.setUrl(trackbackLine);
                            }
                            trackbackSwitch++;
                            break;
                        }
                    case 3:
                        {
                            if (!"".equals(trackbackLine.trim())) {
                                trackback.setBlogName(trackbackLine);
                            }
                            trackbackSwitch++;
                            break;
                        }
                }
            }
            br.close();
        } catch (IOException e) {
            _logger.error(e);
        }
        return trackback;
    }

    /**
     * Load the meta data for the entry
     *
     * @since blojsom 1.9
     * @param blogHome Directory where blog entries are stored
     * @param blogEntryMetaDataExtension File extension to use for the blog entry meta-data
     */
    public void loadMetaData(String blogHome, String blogEntryMetaDataExtension) {
        if (blogEntryMetaDataExtension == null || "".equals(blogEntryMetaDataExtension)) {
            return;
        }

        String entryFilename = BlojsomUtils.getFilename(_source.getName());
        File blogEntryMetaData = new File(blogHome + BlojsomUtils.removeInitialSlash(_category) + entryFilename + blogEntryMetaDataExtension);

        if (blogEntryMetaData.exists()) {
            try {
                Properties entryMetaData = new Properties();
                FileInputStream fis = new FileInputStream(blogEntryMetaData);
                entryMetaData.load(fis);
                fis.close();

                Enumeration keys = entryMetaData.keys();

                String key;
                if (_metaData == null) {
                    _metaData = new HashMap(5);
                }

                while (keys.hasMoreElements()) {
                    key = (String) keys.nextElement();
                    _metaData.put(key, entryMetaData.getProperty(key));
                }

                _logger.debug("Loaded meta-data from: " + blogEntryMetaData.toString());
            } catch (IOException e) {
                _logger.error("Failed loading meta-data from: " + blogEntryMetaData.toString());
            }
        }
    }
}
