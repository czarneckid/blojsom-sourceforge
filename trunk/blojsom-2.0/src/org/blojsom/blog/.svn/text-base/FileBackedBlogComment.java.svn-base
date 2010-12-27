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
package org.blojsom.blog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.BlojsomException;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.BlojsomProperties;
import org.blojsom.util.BlojsomConstants;

import java.io.*;
import java.util.Properties;
import java.util.Date;

/**
 * FileBackedBlogComment
 *
 * @author David Czarnecki
 * @version $Id: FileBackedBlogComment.java,v 1.4 2006-01-04 16:59:53 czarneckid Exp $
 * @since blojsom 2.26
 */
public class FileBackedBlogComment extends BlogComment {

    private transient Log _logger = LogFactory.getLog(FileBackedBlogComment.class);

    protected transient File _source;

    /**
     * Default constructor
     */
    public FileBackedBlogComment() {
        super();
    }

    /**
     * Retrieve the source file for this comment
     *
     * @return {@link File} backing this comment
     */
    public File getSource() {
        return _source;
    }

    /**
     * Set the source file for this comment
     *
     * @param source {@link File} backing this comment
     */
    public void setSource(File source) {
        _source = source;
    }

    /**
     * Load the blog comment
     *
     * @param blogUser {@link BlogUser}
     * @since blojsom 2.26
     */
    public void load(BlogUser blogUser) throws BlojsomException {
        if (_source == null) {
            throw new BlojsomException("No source file set for this comment");
        }

        int commentSwitch = 0;
        setCommentDateLong(_source.lastModified());
        StringBuffer commentDescription = new StringBuffer();
        String separator = BlojsomConstants.LINE_SEPARATOR;
        String commentLine;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(_source), blogUser.getBlog().getBlogFileEncoding()));
            while ((commentLine = br.readLine()) != null) {
                switch (commentSwitch) {
                    case 0:
                        {
                            setAuthor(commentLine);
                            commentSwitch++;
                            break;
                        }
                    case 1:
                        {
                            setAuthorEmail(commentLine);
                            commentSwitch++;
                            break;
                        }
                    case 2:
                        {
                            setAuthorURL(commentLine);
                            commentSwitch++;
                            break;
                        }
                    default:
                        {
                            commentDescription.append(commentLine).append(separator);
                        }
                }
            }

            setComment(commentDescription.toString());
            setCommentDate(new Date(_source.lastModified()));
            setId(_source.getName());
            br.close();

            // Load comment meta-data if available
            File commentMetaData = new File(BlojsomUtils.getFilename(_source.toString()) + ".meta");
            if (commentMetaData.exists()) {
                _logger.debug("Loading comment meta-data: " + commentMetaData.toString());
                Properties commentMetaDataProperties = new BlojsomProperties();
                FileInputStream fis = new FileInputStream(commentMetaData);
                commentMetaDataProperties.load(fis);
                fis.close();
                setMetaData(BlojsomUtils.propertiesToMap(commentMetaDataProperties));
            }
        } catch (IOException e) {
            _logger.error(e);
        }
    }

    /**
     * Save the blog comment
     *
     * @param blogUser {@link BlogUser}
     * @since blojsom 2.26
     */
    public void save(BlogUser blogUser) throws BlojsomException {
        if (_blogEntry == null) {
            throw new BlojsomException("Blog entry for this comment not available");
        }

        Blog blog = blogUser.getBlog();
        long originalTimestamp = -1;
        originalTimestamp = _commentDateLong;

        File commentEntry;
        
        if (_source == null) {
            StringBuffer commentDirectory = new StringBuffer();
            String permalinkFilename = _blogEntry.getPermalink();
            permalinkFilename = BlojsomUtils.urlDecode(permalinkFilename);
            if (permalinkFilename == null) {
                _logger.debug("Invalid permalink comment for: " + _blogEntry.getPermalink());

                throw new BlojsomException("Invalid permalink comment for: " + _blogEntry.getPermalink());
            }

            commentDirectory.append(blog.getBlogHome());
            commentDirectory.append(BlojsomUtils.removeInitialSlash(_blogEntry.getCategory()));
            File blogEntry = new File(commentDirectory.toString() + File.separator + permalinkFilename);
            if (!blogEntry.exists()) {
                _logger.error("Trying to create comment for invalid blog entry: " + _blogEntry.getPermalink());

                throw new BlojsomException("Trying to create comment for invalid blog entry: " + _blogEntry.getPermalink());
            }

            commentDirectory.append(blog.getBlogCommentsDirectory());
            commentDirectory.append(File.separator);
            commentDirectory.append(permalinkFilename);
            commentDirectory.append(File.separator);

            String commentHashable = _author + _comment;
            String hashedComment = BlojsomUtils.digestString(commentHashable).toUpperCase();
            String commentFilename = commentDirectory.toString() + hashedComment + BlojsomConstants.COMMENT_EXTENSION;

            setId(hashedComment + BlojsomConstants.COMMENT_EXTENSION);

            File commentDir = new File(commentDirectory.toString());
            if (!commentDir.exists()) {
                if (!commentDir.mkdirs()) {
                    _logger.error("Could not create directory for comments: " + commentDirectory);

                    throw new BlojsomException("Could not create directory for comments: " + commentDirectory);
                }
            }

            commentEntry = new File(commentFilename);
        } else {
            commentEntry = _source;
            originalTimestamp = _commentDateLong;
        }

        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(commentEntry), blog.getBlogFileEncoding()));
            bw.write(BlojsomUtils.nullToBlank(getAuthor()).trim());
            bw.newLine();
            bw.write(BlojsomUtils.nullToBlank(getAuthorEmail()).trim());
            bw.newLine();
            bw.write(BlojsomUtils.nullToBlank(getAuthorURL()).trim());
            bw.newLine();
            bw.write(BlojsomUtils.nullToBlank(getComment()).trim());
            bw.newLine();
            bw.close();
            _logger.debug("Added blog comment: " + _id);

            Properties commentMetaDataProperties = BlojsomUtils.mapToProperties(_metaData, BlojsomConstants.UTF8);
            String commentMetaDataFilename = BlojsomUtils.getFilename(commentEntry.toString()) + BlojsomConstants.DEFAULT_METADATA_EXTENSION;
            FileOutputStream fos = new FileOutputStream(new File(commentMetaDataFilename));
            commentMetaDataProperties.store(fos, null);
            fos.close();
            _logger.debug("Wrote comment meta-data: " + commentMetaDataFilename);

            _source = commentEntry;

            if (originalTimestamp != -1) {
                _source.setLastModified(originalTimestamp);
                File commentMetaData = new File(commentMetaDataFilename);
                commentMetaData.setLastModified(originalTimestamp);
            }
        } catch (IOException e) {
            _logger.error(e);

            throw new BlojsomException(e);
        }
    }

    /**
     * Delete the blog comment
     *
     * @param blogUser {@link BlogUser}
     * @since blojsom 2.26
     */
    public void delete(BlogUser blogUser) throws BlojsomException {
        Blog blog = blogUser.getBlog();

        if (_source == null || _blogEntry == null) {
            throw new BlojsomException("No source file found to delete comment");
        }

        _logger.debug("Deleting comment " + _source.getAbsolutePath());

        if (!_source.delete()) {
            throw new BlojsomException("Unable to delete commnent: " + getId());
        }

        // Delete meta-data
        File metaFile = new File(blog.getBlogHome() + _blogEntry.getCategory() + blog.getBlogCommentsDirectory()
                + File.separatorChar + _blogEntry.getPermalink() + File.separatorChar + BlojsomUtils.getFilename(_source.getName())
                + blog.getBlogEntryMetaDataExtension());
        if (metaFile.exists()) {
            metaFile.delete();
        } else {
            _logger.error("Unable to delete comment meta-data: " + metaFile);
        }
    }
}