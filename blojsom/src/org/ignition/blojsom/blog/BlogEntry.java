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
import org.ignition.blojsom.util.BlojsomConstants;

import java.io.*;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * BlogEntry
 *
 * @author David Czarnecki
 * @version $Id: BlogEntry.java,v 1.25 2003-03-27 01:54:40 czarneckid Exp $
 */
public class BlogEntry implements BlojsomConstants {

    private Log _logger = LogFactory.getLog(BlogEntry.class);

    private String _title;
    private String _link;
    private String _description;
    private File _source;
    private String _category;
    private Date _entryDate;
    private long _lastModified;
    private String _commentsDirectory;
    private ArrayList _comments;
    private ArrayList _trackbacks;
    private String _trackbacksDirectory;

    /**
     * Create a new blog entry with no data
     */
    public BlogEntry() {
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
    public BlogEntry(String title, String link, String description, File source) {
        super();
        _title = title;
        _link = link;
        _description = description;
        _source = source;
        _entryDate = new Date(_source.lastModified());
    }

    /**
     * Date of the blog entry
     *
     * This value is constructed from the lastModified value of the file
     *
     * @return Date of the blog entry
     */
    public Date getDate() {
        return _entryDate;
    }

    /**
     * Return an RFC 822 style date
     *
     * @return Date formatted in RFC 822 format
     */
    public String getRFC822Date() {
        return BlojsomUtils.getRFC822Date(_entryDate);
    }

    /**
     * Return an ISO 8601 style date
     * http://www.w3.org/TR/NOTE-datetime
     *
     * @return Date formatted in ISO 8601 format
     */
    public String getISO8601Date() {
        return BlojsomUtils.getISO8601Date(_entryDate);
    }

    /**
     * Title of the blog entry
     *
     * @return Blog title
     */
    public String getTitle() {
        return _title;
    }

    /**
     * Set the title of the blog entry
     *
     * @param title Title for the blog entry
     */
    public void setTitle(String title) {
        _title = title;
    }

    /**
     * Title for the entry where the &lt;, &gt;, and &amp; characters are escaped
     *
     * @return Escaped entry title
     */
    public String getEscapedTitle() {
        return BlojsomUtils.escapeString(_title);
    }

    /**
     * Permalink for the blog entry
     *
     * @return Blog entry permalink
     */
    public String getLink() {
        return _link;
    }

    /**
     * Permalink for the blog entry where the &lt;, &gt;, and &amp; characters are escaped
     *
     * @return Blog entry permalink which has been escaped
     */
    public String getEscapedLink() {
        return BlojsomUtils.escapeString(_link);
    }

    /**
     * Set the permalink for the blog entry
     *
     * @param link Permalink for the blog entry
     */
    public void setLink(String link) {
        _link = link;
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
     * Description of the blog entry
     *
     * @return Blog entry description
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Escaped description of the blog entry
     * This method would be used for generating RSS feeds where the &lt;, &gt;, and &amp; characters are escaped
     *
     * @return Blog entry description where &amp;, &lt;, and &gt; have been escaped
     */
    public String getEscapedDescription() {
        return BlojsomUtils.escapeString(_description);
    }

    /**
     * Set the description for the blog entry
     *
     * @param description Description for the blog entry
     */
    public void setDescription(String description) {
        _description = description;
    }

    /**
     * Last modified time for the blog entry
     *
     * @return Blog entry last modified time
     */
    public long getLastModified() {
        return _lastModified;
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
     * Returns the contents of the file in a byte array
     *
     * @param file Input file
     * @return Byte array containing the file contents
     * @throws IOException If there is an error reading the contents of the file
     */
    private static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file: " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    /**
     * Reload the blog entry from disk
     *
     * The first line of the blog entry will be used as the title of the blog
     */
    public void reloadSource() throws IOException {
        byte[] fileContents = getBytesFromFile(_source);
        String lineSeparator = System.getProperty("line.separator");
        String description = new String(fileContents, "UTF-8");
        int titleIndex = description.indexOf(lineSeparator);
        if (titleIndex == -1) {
            _title = "";
            _description = description;
        } else {
            _title = description.substring(0, titleIndex);
            _description = description.substring(titleIndex + 1);
        }
        _entryDate = new Date(_source.lastModified());
        _lastModified = _source.lastModified();
    }

    /**
     * Category for the blog entry
     *
     * @return Blog entry category
     */
    public String getCategory() {
        return _category;
    }

    /**
     * Set the category for the blog entry
     *
     * @param category Category for the blog entry
     */
    public void setCategory(String category) {
        _category = category;
    }

    /**
     * Checks to see if the link to this entry is the same as the input entry
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (!(o instanceof BlogEntry)) {
            return false;
        }

        BlogEntry entry = (BlogEntry) o;
        if (_link.equals(entry.getLink())) {
            return true;
        }
        return false;
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
     * Get the comments
     *
     * @return ArrayList of comments
     */
    public ArrayList getComments() {
        return _comments;
    }

    /**
     * Set the comments for this blog entry. The comments must be an <code>ArrayList</code>
     * of {@link BlogComment}. This method will not writeback or change the comments
     *
     * @param comments Comments for this entry
     */
    public void setComments(ArrayList comments) {
        _comments = comments;
    }

    /**
     * Get the comments as an array of BlogComment objects
     *
     * @return BlogComment[] array
     */
    public BlogComment[] getCommentsAsArray() {
        if (_comments == null) {
            return null;
        } else {
            return (BlogComment[]) _comments.toArray(new BlogComment[_comments.size()]);
        }
    }

    /**
     * Get the number of comments for this entry
     *
     * @return 0 if comments is <code>null</code>, or the number of comments otherwise, which could be 0
     */
    public int getNumComments() {
        if (_comments == null) {
            return 0;
        } else {
            return _comments.size();
        }
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
    private BlogComment loadComment(File commentFile) {
        int commentSwitch = 0;
        BlogComment comment = new BlogComment();
        comment.setCommentDateLong(commentFile.lastModified());
        StringBuffer commentDescription = new StringBuffer();
        String commentLine;
        try {
            BufferedReader br = new BufferedReader(new FileReader(commentFile));
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
     * Get the trackbacks
     *
     * @return ArrayList of trackbacks
     */
    public ArrayList getTrackbacks() {
        return _trackbacks;
    }

    /**
     * Set the trackbacks for this blog entry. The trackbacks must be an <code>ArrayList</code>
     * of {@link Trackback}. This method will not writeback or change the trackbacks
     *
     * @param trackbacks Trackbacks for this entry
     */
    public void setTrackbacks(ArrayList trackbacks) {
        _trackbacks = trackbacks;
    }

    /**
     * Get the trackbacks as an array of Trackback objects
     *
     * @return Trackback[] array
     */
    public Trackback[] getTrackbacksAsArray() {
        if (_trackbacks == null) {
            return null;
        } else {
            return (Trackback[]) _trackbacks.toArray(new Trackback[_trackbacks.size()]);
        }
    }

    /**
     * Get the number of trackbacks for this entry
     *
     * @return 0 if trackbacks is <code>null</code>, or the number of trackbacks otherwise, which could be 0
     */
    public int getNumTrackbacks() {
        if (_trackbacks == null) {
            return 0;
        } else {
            return _trackbacks.size();
        }
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
    private Trackback loadTrackback(File trackbackFile) {
        int trackbackSwitch = 0;
        Trackback trackback = new Trackback();
        trackback.setTrackbackDateLong(trackbackFile.lastModified());
        String trackbackLine;
        try {
            BufferedReader br = new BufferedReader(new FileReader(trackbackFile));
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
}
