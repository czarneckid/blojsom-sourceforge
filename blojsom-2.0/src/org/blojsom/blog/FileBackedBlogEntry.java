/**
 * Copyright (c) 2003-2005 , David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003-2005  by Mark Lussier
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
 * @version $Id: FileBackedBlogEntry.java,v 1.29 2005-06-23 19:03:08 czarneckid Exp $
 * @since blojsom 1.8
 */
public class FileBackedBlogEntry extends BlogEntry {

    protected transient Log _logger = LogFactory.getLog(FileBackedBlogEntry.class);

    protected transient File _source;
    protected transient String _commentsDirectory;
    protected transient String _trackbacksDirectory;
    protected transient String _pingbacksDirectory;
    protected transient String _blogFileEncoding;

    /**
     * Create a new blog entry with no data
     */
    public FileBackedBlogEntry() {
        _commentsDirectory = DEFAULT_COMMENTS_DIRECTORY;
        _trackbacksDirectory = DEFAULT_TRACKBACK_DIRECTORY;
        _pingbacksDirectory = DEFAULT_PINGBACKS_DIRECTORY;
        _blogFileEncoding = UTF8;
    }

    /**
     * Create a new blog entry
     *
     * @param title       Entry title
     * @param link        Permalink to the entry
     * @param description Entry description
     * @param source      File containing the blog entry
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
     * @param blogFileEncoding File encoding
     * @since blojsom 1.9
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
        String startingSlash = "";
        if (!_category.startsWith("/")) {
            startingSlash = "/";
        }

        return startingSlash + BlojsomUtils.urlEncodeForLink(_category) + '?' + PERMALINK_PARAM + '=' + BlojsomUtils.urlEncode(_source.getName());
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
     * <p/>
     * The first line of the blog entry will be used as the title of the blog
     *
     * @param blog Blog information
     * @throws IOException If there is an error loading the blog entry
     */
    protected void reloadSource(Blog blog) throws IOException {
        boolean hasLoadedTitle = false;
        String lineSeparator = LINE_SEPARATOR;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(_source), blog.getBlogFileEncoding()));
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
     * @return <code>true</code> if the blog entry supports trackbacks, <code>false</code> otherwise
     * @since blojsom 2.05
     */
    public boolean supportsTrackbacks() {
        return _source.canWrite();
    }

    /**
     * Determines whether or not this blog entry supports pingbacks.
     *
     * @return <code>true</code> if the blog entry supports pingbacks, <code>false</code> otherwise
     * @since blojsom 2.23
     */
    public boolean supportsPingbacks() {
        return _source.canWrite() && (!BlojsomUtils.checkMapForKey(_metaData, BLOG_METADATA_PINGBACKS_DISABLED));
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
     *
     * @param blogUser {@link BlogUser} information
     */
    protected void loadComments(BlogUser blogUser) {
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
                BlogComment blogComment;
                for (int i = 0; i < comments.length; i++) {
                    File comment = comments[i];
                    try {
                        blogComment = loadComment(comment, blogUser.getBlog().getBlogFileEncoding(), blogUser);
                        _comments.add(blogComment);
                    } catch (BlojsomException e) {
                        _logger.error(e);
                    }
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
     * @param commentFile      Comment file
     * @param blogFileEncoding Encoding for blog files
     * @param blogUser {@link BlogUser}
     * @return BlogComment Blog comment loaded from disk
     */
    protected BlogComment loadComment(File commentFile, String blogFileEncoding, BlogUser blogUser) throws BlojsomException {
        FileBackedBlogComment comment = new FileBackedBlogComment();
        comment.setSource(commentFile);
        comment.setBlogEntry(this);
        comment.load(blogUser);

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
     *
     * @param blogUser {@link BlogUser} information
     */
    protected void loadTrackbacks(BlogUser blogUser) {
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
            Trackback trackback;
            for (int i = 0; i < trackbacks.length; i++) {
                File trackbackFile = trackbacks[i];
                try {
                    trackback = loadTrackback(trackbackFile, blogUser.getBlog().getBlogFileEncoding(), blogUser);
                    _trackbacks.add(trackback);
                } catch (BlojsomException e) {
                    _logger.error(e);
                }
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
     * @param trackbackFile    Trackback file
     * @param blogFileEncoding Encoding for blog files
     * @param blogUser {@link BlogUser} information
     * @return Trackback Trackback loaded from disk
     */
    protected Trackback loadTrackback(File trackbackFile, String blogFileEncoding, BlogUser blogUser) throws BlojsomException {
        FileBackedTrackback trackback = new FileBackedTrackback();

        trackback.setSource(trackbackFile);
        trackback.setBlogEntry(this);
        trackback.load(blogUser);

        return trackback;
    }

    /**
     * Set the directory for pingbacks
     *
     * @param pingbacksDirectory Pingbacks directory
     * @since blojsom 2.23
     */
    public void setPingbacksDirectory(String pingbacksDirectory) {
        _pingbacksDirectory = pingbacksDirectory;
    }

    /**
     * Convenience method to load the pingbacks for this blog entry.
     *
     * @param blogUser {@link BlogUser} information
     * @since blojsom 2.23
     */
    protected void loadPingbacks(BlogUser blogUser) {
        String pingbacksDirectoryPath;
        if (_source.getParent() == null) {
            pingbacksDirectoryPath = File.separator + _pingbacksDirectory + File.separator + _source.getName();
        } else {
            pingbacksDirectoryPath = _source.getParent() + File.separator + _pingbacksDirectory + File.separator + _source.getName();
        }

        File pingbacksDirectory = new File(pingbacksDirectoryPath);
        File[] pingbacks = pingbacksDirectory.listFiles(BlojsomUtils.getExtensionFilter(PINGBACK_EXTENSION));
        if ((pingbacks != null) && (pingbacks.length > 0)) {
            _logger.debug("Adding " + pingbacks.length + " pingbacks to blog entry: " + getPermalink());
            Arrays.sort(pingbacks, BlojsomUtils.FILE_TIME_ASCENDING_COMPARATOR);
            _pingbacks = new ArrayList(pingbacks.length);
            Pingback pingback;
            for (int i = 0; i < pingbacks.length; i++) {
                File pingbackFile = pingbacks[i];
                try {
                    pingback = loadPingback(pingbackFile, blogUser.getBlog().getBlogFileEncoding(), blogUser);
                    _pingbacks.add(pingback);
                } catch (BlojsomException e) {
                    _logger.error(e);
                }
            }
        }
    }

    /**
     * Load a pingback for this blog entry from disk
     * Pingbacks must always have the form:<br />
     * title<br>
     * url<br />
     * blog_name<br/>
     * excerpt<br />
     *
     * @param pingbackFile    Pingback file
     * @param blogFileEncoding Encoding for blog files
     * @param blogUser {@link BlogUser}
     * @return {@link Pingback} loaded from disk
     * @since blojsom 2.23
     */
    protected Pingback loadPingback(File pingbackFile, String blogFileEncoding, BlogUser blogUser) throws BlojsomException {
        FileBackedPingback pingback = new FileBackedPingback();

        pingback.setSource(pingbackFile);
        pingback.setBlogEntry(this);
        pingback.load(blogUser);

        return pingback;
    }

    /**
     * Load the meta data for the entry
     *
     * @param blog Blog information
     * @since blojsom 1.9
     */
    protected void loadMetaData(Blog blog) {
        String blogHome = blog.getBlogHome();
        String blogEntryMetaDataExtension = blog.getBlogEntryMetaDataExtension();

        if (BlojsomUtils.checkNullOrBlank(blogEntryMetaDataExtension)) {
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

                // Preserve the original timestamp is that metadata is available
                if (_metaData.containsKey(BLOG_ENTRY_METADATA_TIMESTAMP)) {
                    try {
                        long originalTimestamp = Long.parseLong((String) _metaData.get(BLOG_ENTRY_METADATA_TIMESTAMP));
                        _entryDate = new Date(originalTimestamp);
                        _lastModified = originalTimestamp;
                    } catch (NumberFormatException e) {
                        _logger.error(e);
                    }
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
     * @param blog Blog information
     * @since blojsom 1.9
     */
    protected void saveMetaData(Blog blog) {
        String blogHome = blog.getBlogHome();
        String blogEntryMetaDataExtension = blog.getBlogEntryMetaDataExtension();

        if (BlojsomUtils.checkNullOrBlank(blogEntryMetaDataExtension) || _metaData == null) {
            return;
        }

        String entryFilename = BlojsomUtils.getFilename(_source.getName());
        File blogEntryMetaData = new File(blogHome + BlojsomUtils.removeInitialSlash(_category) + File.separator + entryFilename + blogEntryMetaDataExtension);

        try {
            Properties entryMetaData = BlojsomUtils.mapToProperties(_metaData, UTF8);
            FileOutputStream fos = new FileOutputStream(blogEntryMetaData);
            entryMetaData.store(fos, null);
            fos.close();

            _logger.debug("Saved meta-data to: " + blogEntryMetaData.toString());
        } catch (IOException e) {
            _logger.error("Failed saving meta-data to: " + blogEntryMetaData.toString(), e);
        }
    }

    /**
     * Load the blog category information for this entry
     *
     * @param blogUser {@link BlogUser} information
     * @since blojsom 2.23
     */
    protected void loadBlogCategory(BlogUser blogUser) {
        if (_category != null) {
            Blog blog = blogUser.getBlog();
            FileBackedBlogCategory blogCategoryForEntry;

            blogCategoryForEntry = new FileBackedBlogCategory();
            blogCategoryForEntry.setCategory(_category);
            blogCategoryForEntry.setCategoryURL(blog.getBlogURL() + BlojsomUtils.removeInitialSlash(_category));

            try {
                blogCategoryForEntry.load(blogUser);
            } catch (BlojsomException e) {
                _logger.error(e);
            }


            if ("/".equals(_category)) {
                setLink(blog.getBlogURL() + '?' + PERMALINK_PARAM + '=' + BlojsomUtils.urlEncode(_source.getName()));
            } else {
                setLink(blog.getBlogURL() + BlojsomUtils.urlEncodeForLink(_category.substring(0, _category.length() - 1)) + "/?" + PERMALINK_PARAM + '=' + BlojsomUtils.urlEncode(_source.getName()));
            }

            _blogCategory = blogCategoryForEntry;
        }
    }

    /**
     * Load a blog entry.
     *
     * @param blogUser User information
     * @throws BlojsomException If there is an error loading the entry
     * @since blojsom 1.9
     */
    public void load(BlogUser blogUser) throws BlojsomException {
        Blog blog = blogUser.getBlog();

        if (_source == null) {
            throw new BlojsomException("No source file set for this blog entry.");
        }

        try {
            reloadSource(blog);
            loadBlogCategory(blogUser);
            if (blog.getBlogCommentsEnabled().booleanValue()) {
                loadComments(blogUser);
                loadTrackbacks(blogUser);
                loadPingbacks(blogUser);
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
     * @param blogUser User information
     * @throws BlojsomException If there is an error saving the entry
     * @since blojsom 1.9
     */
    public void save(BlogUser blogUser) throws BlojsomException {
        Blog blog = blogUser.getBlog();

        if (_source == null) {
            String category = BlojsomUtils.normalize(_category);
            category = BlojsomUtils.removeInitialSlash(_category);
            String proposedFilename = BlojsomUtils.getBlogEntryFilename(_title, _description);
            String fileExtension = blog.getBlogProperty(BLOG_DEFAULT_FILE_EXTENSION_IP);
            if (BlojsomUtils.checkNullOrBlank(fileExtension)) {
                fileExtension = DEFAULT_ENTRY_EXTENSION;
            }

            File blogFilename = new File(blog.getBlogHome() + category + proposedFilename + fileExtension);
            int fileTag = 1;
            while (blogFilename.exists()) {
                blogFilename = new File(blog.getBlogHome() + category + proposedFilename + "-" + fileTag + fileExtension);
                fileTag++;
            }
            _logger.debug("Created blog entry file: " + blogFilename.toString());

            _source = blogFilename;
        }

        try {
            long originalTimestamp = _source.lastModified();

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(_source.getAbsolutePath(), false), blog.getBlogFileEncoding()));
            if (BlojsomUtils.checkNullOrBlank(_title)) {
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
                    originalTimestamp = Long.parseLong((String) _metaData.get(BLOG_ENTRY_METADATA_TIMESTAMP));
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
     * @param blogUser User information
     * @throws BlojsomException If there is an error deleting the entry
     * @since blojsom 1.9
     */
    public void delete(BlogUser blogUser) throws BlojsomException {
        Blog blog = blogUser.getBlog();

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

         // Delete pingbacks
        File _pingbacks = new File(blog.getBlogHome() + _category + blog.getBlogPingbacksDirectory()
                + File.separatorChar + _source.getName() + File.separatorChar);
        BlojsomUtils.deleteDirectory(_pingbacks);

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
