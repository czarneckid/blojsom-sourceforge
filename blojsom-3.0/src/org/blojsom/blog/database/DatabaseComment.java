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
package org.blojsom.blog.database;

import org.blojsom.blog.Comment;
import org.blojsom.blog.Entry;
import org.blojsom.util.BlojsomUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;
import java.text.SimpleDateFormat;

/**
 * DatabaseComment
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: DatabaseComment.java,v 1.6 2006-09-05 17:30:26 czarneckid Exp $
 */
public class DatabaseComment implements Comment, Serializable {

    protected Integer _id;
    protected String _blogId;
    protected Integer _blogEntryId;
    protected Entry _entry;

    protected String _author;
    protected String _authorEmail;
    protected String _authorURL;
    protected String _comment;
    protected Date _commentDate;
    protected Map _metaData;
    protected Integer _parentId;
    protected String _status;
    protected String _ip;

    /**
     * Create a new instance of the database comment
     */
    public DatabaseComment() {
    }

    /**
     * Get the id of this blog comment
     *
     * @return Id
     */
    public Integer getId() {
        return _id;
    }

    /**
     * Set the id of this blog comment. This method can only be called if the id has not been set.
     *
     * @param id New id
     */
    public void setId(Integer id) {
        if (_id == null) {
            _id = id;
        }
    }

    /**
     * Set the blog ID
     *
     * @param blogId Blog ID
     */
    public void setBlogId(String blogId) {
        _blogId = blogId;
    }

    /**
     * Get the blog ID
     *
     * @return Blog ID
     */
    public String getBlogId() {
        return _blogId;
    }

    /**
     * Get the blog entry ID
     *
     * @return Blog entry ID
     */
    public Integer getBlogEntryId() {
        return _blogEntryId;
    }

    /**
     * Set the blog entry ID
     *
     * @param blogEntryId Blog entry ID
     */
    public void setBlogEntryId(Integer blogEntryId) {
        _blogEntryId = blogEntryId;
    }

    /**
     * Get the {@link Entry}
     *
     * @return {@link Entry}
     */
    public Entry getEntry() {
        return _entry;
    }

    /**
     * Set the {@link Entry}
     *
     * @param entry {@link Entry}
     */
    public void setEntry(Entry entry) {
        _entry = entry;
        _blogEntryId = _entry.getId();
    }

    /**
     * Get the author of the comment
     *
     * @return Comment author
     */
    public String getAuthor() {
        return _author;
    }

    /**
     * Get the author as an escaped string
     *
     * @return Escaped author
     */
    public String getEscapedAuthor() {
        return BlojsomUtils.escapeString(_author);
    }

    /**
     * Set the author of the comment
     *
     * @param author Comment's new author
     */
    public void setAuthor(String author) {
        _author = author;
    }

    /**
     * Get the e-mail of the author of the comment
     *
     * @return Author's e-mail
     */
    public String getAuthorEmail() {
        return _authorEmail;
    }

    /**
     * Get the escaped e-mail of the author of the comment
     *
     * @return Escaped author e-mail
     */
    public String getEscapedAuthorEmail() {
        return BlojsomUtils.escapeString(_authorEmail);
    }

    /**
     * Set the e-mail of the author of the comment
     *
     * @param authorEmail Author's new e-mail
     */
    public void setAuthorEmail(String authorEmail) {
        _authorEmail = authorEmail;
    }

    /**
     * Get the URL of the author
     *
     * @return Author's URL
     */
    public String getAuthorURL() {
        return _authorURL;
    }

    /**
     * Get the escaped URL of the author
     *
     * @return Escaped URL
     */
    public String getEscapedAuthorURL() {
        return BlojsomUtils.escapeString(_authorURL);
    }

    /**
     * Set the URL for the author
     *
     * @param authorURL New URL for the author
     */
    public void setAuthorURL(String authorURL) {
        _authorURL = authorURL;
    }

    /**
     * Get the comment as a escaped string
     * @return Escaped Comment
     */
    public String getEscapedComment() {
        return BlojsomUtils.escapeString(_comment);
    }

    /**
     * Get the comment
     *
     * @return Comment
     */
    public String getComment() {
        return _comment;
    }

    /**
     * Set the new comment
     *
     * @param comment New comment
     */
    public void setComment(String comment) {
        _comment = comment;
    }

    /**
     * Get the date the comment was entered
     *
     * @return Comment date
     */
    public Date getCommentDate() {
        return _commentDate;
    }

    /**
     * Return an ISO 8601 style date
     * http://www.w3.org/TR/NOTE-datetime
     *
     * @return Date formatted in ISO 8601 format
     */
    public String getISO8601Date() {
        return BlojsomUtils.getISO8601Date(_commentDate);
    }

    /**
     * Return an RFC 822 style date
     *
     * @return Date formatted in RFC 822 format
     */
    public String getRFC822Date() {
        return BlojsomUtils.getRFC822Date(_commentDate);
    }

    /**
     * Get the comment meta-data
     *
     * @return Meta-data as a {@link Map}
     */
    public Map getMetaData() {
        if (_metaData == null) {
            return new HashMap();
        }

        return _metaData;
    }

    /**
     * Set the date for the comment
     *
     * @param commentDate Comment date
     */
    public void setCommentDate(Date commentDate) {
        _commentDate = commentDate;
    }

    /**
     * Set the comment meta-data
     *
     * @param metaData {@link Map} containing meta-data for this comment
     */
    public void setMetaData(Map metaData) {
        _metaData = metaData;
    }

    /**
     * Return the comment date formatted with a specified date format
     *
     * @param format Date format
     * @return <code>null</code> if the comment date or format is null, otherwise returns the comment date
     *         formatted to the specified format. If the format is invalid, returns <tt>commentDate.toString()</tt>
     */
    public String getDateAsFormat(String format) {
        return getDateAsFormat(format, null);
    }

    /**
     * Return the comment date formatted with a specified date format
     *
     * @param format Date format
     * @param locale Locale for date formatting
     * @return <code>null</code> if the entry date or format is null, otherwise returns the entry date formatted to the specified format. If the format is invalid, returns <tt>commentDate.toString()</tt>
     */
    public String getDateAsFormat(String format, Locale locale) {
        if (_commentDate == null || format == null) {
            return null;
        }

        SimpleDateFormat sdf;
        try {
            if (locale == null) {
                sdf = new SimpleDateFormat(format);
            } else {
                sdf = new SimpleDateFormat(format, locale);
            }

            return sdf.format(_commentDate);
        } catch (IllegalArgumentException e) {
            return _commentDate.toString();
        }
    }

    /**
     * Get the comment parent ID
     *
     * @return Comment parent ID
     */
    public Integer getParentId() {
        return _parentId;
    }

    /**
     * Set the comment parent ID
     *
     * @param parentId Comment parent ID
     */
    public void setParentId(Integer parentId) {
        _parentId = parentId;
    }

    /**
     * Get the IP
     *
     * @return IP
     */
    public String getIp() {
        return _ip;
    }

    /**
     * Set the IP
     *
     * @param ip IP
     */
    public void setIp(String ip) {
        _ip = ip;
    }

    /**
     * Get the status
     *
     * @return Status
     */
    public String getStatus() {
        return _status;
    }

    /**
     * Set the status
     *
     * @param status Status
     */
    public void setStatus(String status) {
        _status = status;
    }

    /**
     * Retrieve the date for this object
     *
     * @return Date
     */
    public Date getDate() {
        return _commentDate;
    }

    /**
     * Get the response type
     *
     * @return Response type
     */
    public String getType() {
        return COMMENT_TYPE;
    }
}
