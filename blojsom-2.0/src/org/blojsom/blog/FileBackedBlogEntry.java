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
package org.blojsom.blog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.BlojsomProperties;
import org.blojsom.BlojsomException;

import java.io.*;
import java.util.*;

/**
 * FileBackedBlogEntry
 *
 * @author David Czarnecki
 * @version $Id: FileBackedBlogEntry.java,v 1.9 2003-11-29 16:20:42 czarneckid Exp $
 * @since blojsom 1.8
 */
public class FileBackedBlogEntry extends BlogEntry {

    private Log _logger = LogFactory.getLog(FileBackedBlogEntry.class);

    private static final String SOURCE_ATTRIBUTE = "blog-entry-source";

    private File _source;
    private String _commentsDirectory;
    private String _trackbacksDirectory;
    private String _blogFileEncoding;

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
     * Set the file encoding to use when loading the blog entry
     *
     * @since blojsom 1.9
     * @param blogFileEncoding File encoding
     */
    public void setBlogFileEncoding(String blogFileEncoding) {
        _blogFileEncoding = blogFileEncoding;
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
        return _category + '?' + PERMALINK_PARAM + '=' + BlojsomUtils.urlEncode(_source.getName());
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
    protected void reloadSource() throws IOException {
        boolean hasLoadedTitle = false;
        String lineSeparator = System.getProperty("line.separator");

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(_source), _blogFileEncoding));
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
     * Determines whether or not this blog entry supports trackbacks.
     *
     * @since blojsom 2.05
     * @return <code>true</code> if the blog entry supports trackbacks, <code>false</code> otherwise
     */
    public boolean supportsTrackbacks() {
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
    protected void loadComments() {
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
        String separator = System.getProperty("line.separator");
        String commentLine;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(commentFile), _blogFileEncoding));
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
                            commentDescription.append(commentLine).append(separator);
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
    protected void loadTrackbacks() {
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
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(trackbackFile), _blogFileEncoding));
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
     * @param blog Blog information
     */
    protected void loadMetaData(Blog blog) {
        String blogHome = blog.getBlogHome();
        String blogEntryMetaDataExtension = blog.getBlogEntryMetaDataExtension();

        if (blogEntryMetaDataExtension == null || "".equals(blogEntryMetaDataExtension)) {
            return;
        }

        String entryFilename = BlojsomUtils.getFilename(_source.getName());
        File blogEntryMetaData = new File(blogHome + BlojsomUtils.removeInitialSlash(_category) + entryFilename + blogEntryMetaDataExtension);

        if (blogEntryMetaData.exists()) {
            try {
                Properties entryMetaData = new BlojsomProperties(blog.getBlogFileEncoding());
                FileInputStream fis = new FileInputStream(blogEntryMetaData);
                entryMetaData.load(fis);
                fis.close();

                Iterator keys = entryMetaData.keySet().iterator();
                String key;

                if (_metaData == null) {
                    _metaData = new HashMap(5);
                }

                while (keys.hasNext()) {
                    key = (String) keys.next();
                    _metaData.put(key, entryMetaData.getProperty(key));
                }

                _logger.debug("Loaded meta-data from: " + blogEntryMetaData.toString());
            } catch (IOException e) {
                _logger.error("Failed loading meta-data from: " + blogEntryMetaData.toString());
            }
        } else if (_metaData == null) {
            _metaData = new HashMap(5);
        }
    }

    /**
     * Store the meta data for the entry
     *
     * @since blojsom 1.9
     * @param blog Blog information
     */
    protected void saveMetaData(Blog blog) {
        String blogHome = blog.getBlogHome();
        String blogEntryMetaDataExtension = blog.getBlogEntryMetaDataExtension();

        if (blogEntryMetaDataExtension == null || "".equals(blogEntryMetaDataExtension) || _metaData == null) {
            return;
        }

        String entryFilename = BlojsomUtils.getFilename(_source.getName());
        File blogEntryMetaData = new File(blogHome + BlojsomUtils.removeInitialSlash(_category) + File.separator + entryFilename + blogEntryMetaDataExtension);

        try {
            Properties entryMetaData = new BlojsomProperties(blog.getBlogFileEncoding());
            FileOutputStream fos = new FileOutputStream(blogEntryMetaData);
            Iterator keys = _metaData.keySet().iterator();
            String key;

            while (keys.hasNext()) {
                key = (String) keys.next();
                entryMetaData.put(key, _metaData.get(key));
            }

            entryMetaData.store(fos, null);
            fos.close();

            _logger.debug("Saved meta-data to: " + blogEntryMetaData.toString());
        } catch (IOException e) {
            _logger.error("Failed saving meta-data to: " + blogEntryMetaData.toString(), e);
        }
    }

    /**
     * Load a blog entry.
     *
     * @since blojsom 1.9
     * @param blog Blog
     * @throws BlojsomException If there is an error loading the entry
     */
    public void load(Blog blog) throws BlojsomException {
        if (_source == null) {
            throw new BlojsomException("No source file set for this blog entry.");
        }

        try {
            reloadSource();
            if (blog.getBlogCommentsEnabled().booleanValue()) {
                loadComments();
                loadTrackbacks();
            }
            loadMetaData(blog);
        } catch (IOException e) {
            _logger.error(e);
            throw new BlojsomException(e);
        }
    }

    /**
     * Save the blog entry. This method does not write out the comments or trackbacks to disk.
     *
     * @since blojsom 1.9
     * @param blog Blog
     * @throws BlojsomException If there is an error saving the entry
     */
    public void save(Blog blog) throws BlojsomException {
        if (_source == null) {
            throw new BlojsomException("No source file set for this blog entry.");
        }

        try {
            long originalTimestamp = _source.lastModified();

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(_source.getAbsolutePath(), false), blog.getBlogFileEncoding()));
            if (_title == null || "".equals(_title)) {
                bw.write(BlojsomUtils.nullToBlank(_description));
                bw.close();
            } else {
                bw.write(BlojsomUtils.nullToBlank(_title));
                bw.newLine();
                bw.write(BlojsomUtils.nullToBlank(_description));
                bw.close();
            }

            // Preserve original timestamp of the blog entry if its available in the meta-data
            if (_metaData.containsKey(BLOG_ENTRY_METADATA_TIMESTAMP)) {
                try {
                    originalTimestamp = Long.parseLong((String)_metaData.get(BLOG_ENTRY_METADATA_TIMESTAMP));
                } catch (NumberFormatException e) {
                    _logger.error(e);
                }
            } else { // Otherwise, preserve original timestamp of the blog entry
                _metaData.put(BLOG_ENTRY_METADATA_TIMESTAMP, new Long(originalTimestamp));
            }

            _source.setLastModified(originalTimestamp);
            saveMetaData(blog);
        } catch (IOException e) {
            _logger.error(e);
            throw new BlojsomException(e);
        }
    }

    /**
     * Delete the blog entry.
     *
     * @since blojsom 1.9
     * @param blog Blog
     * @throws BlojsomException If there is an error deleting the entry
     */
    public void delete(Blog blog) throws BlojsomException {
        if (_source == null) {
            throw new BlojsomException("No source file set for this blog entry.");
        }

        _logger.debug("Deleting post " + _source.getAbsolutePath());

        if (!_source.delete()) {
            throw new BlojsomException("Unable to delete entry: " + getId());
        }

        // Delete comments
        File _comments = new File(blog.getBlogHome() + _category + blog.getBlogCommentsDirectory()
                + File.separatorChar + _source.getName() + File.separatorChar);
        BlojsomUtils.deleteDirectory(_comments);

        // Delete trackbacks
        File _trackbacks = new File(blog.getBlogHome() + _category + blog.getBlogTrackbackDirectory()
                + File.separatorChar + _source.getName() + File.separatorChar);
        BlojsomUtils.deleteDirectory(_trackbacks);

        // Delete meta-data
        File metaFile = new File(blog.getBlogHome() + _category + BlojsomUtils.getFilename(_source.getName())
                + blog.getBlogEntryMetaDataExtension());
        if (metaFile.exists()) {
            metaFile.delete();
        }
    }

    /**
     * Set any attributes of the blog entry using data from the map.
     *
     * @param attributeMap Attributes
     */
    public void setAttributes(Map attributeMap) {
        if (attributeMap.containsKey(SOURCE_ATTRIBUTE)) {
            _source = (File) attributeMap.get(SOURCE_ATTRIBUTE);
        }
    }
}
