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

import java.util.Date;
import java.util.Map;
import java.util.Locale;

/**
 * Comment
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: Comment.java,v 1.1 2006-03-20 21:31:13 czarneckid Exp $
 */
public interface Comment {

    /**
     *
     * @param id
     */
    void setId(Integer id);

    /**
     *
     * @return
     */
    public Integer getId();

    /**
     *
     * @param blogId
     */
    void setBlogId(String blogId);

    /**
     *
     * @return
     */
    String getBlogId();

    /**
     *
     * @return
     */
    public Integer getBlogEntryId();

    /**
     *
     * @param blogEntryId
     */
    public void setBlogEntryId(Integer blogEntryId);

    /**
     * Get the author of the comment
     *
     * @return Comment author
     */
    String getAuthor();

    /**
     * Set the author of the comment
     *
     * @param author Comment's new author
     */
    void setAuthor(String author);

    /**
     * Get the e-mail of the author of the comment
     *
     * @return Author's e-mail
     */
    String getAuthorEmail();

    /**
     * Set the e-mail of the author of the comment
     *
     * @param authorEmail Author's new e-mail
     */
    void setAuthorEmail(String authorEmail);

    /**
     * Get the URL of the author
     *
     * @return Author's URL
     */
    String getAuthorURL();

    /**
     * Set the URL for the author
     *
     * @param authorURL New URL for the author
     */
    void setAuthorURL(String authorURL);

    /**
     * Get the comment as a escaped string
     * @return Escaped Comment
     */
    String getEscapedComment();

    /**
     * Get the comment
     *
     * @return Comment
     */
    String getComment();

    /**
     * Set the new comment
     *
     * @param comment New comment
     */
    void setComment(String comment);

    /**
     * Get the date the comment was entered
     *
     * @return Comment date
     */
    Date getCommentDate();

    /**
     * Return an ISO 8601 style date
     * http://www.w3.org/TR/NOTE-datetime
     *
     * @return Date formatted in ISO 8601 format
     */
    String getISO8601Date();

    /**
     * Return an RFC 822 style date
     *
     * @return Date formatted in RFC 822 format
     */
    String getRFC822Date();

    /**
     * Get the trackback meta-data
     *
     * @return Meta-data as a {@link java.util.Map}
     */
    Map getMetaData();

    /**
     * Set the date for the comment
     *
     * @param commentDate Comment date
     */
    void setCommentDate(Date commentDate);

    /**
     * Set the trackback meta-data
     *
     * @param metaData {@link java.util.Map} containing meta-data for this comment
     */
    void setMetaData(Map metaData);

    /**
     * Return the comment date formatted with a specified date format
     *
     * @param format Date format
     * @return <code>null</code> if the comment date or format is null, otherwise returns the comment date
     *         formatted to the specified format. If the format is invalid, returns <tt>commentDate.toString()</tt>
     */
    String getDateAsFormat(String format);

    /**
     * Return the comment date formatted with a specified date format
     *
     * @param format Date format
     * @param locale Locale for date formatting
     * @return <code>null</code> if the entry date or format is null, otherwise returns the entry date formatted to the specified format. If the format is invalid, returns <tt>commentDate.toString()</tt>
     */
    String getDateAsFormat(String format, Locale locale);

    /**
     * Retrieve the {@link org.blojsom.blog.Entry} associated with this comment
     *
     * @return {@link org.blojsom.blog.Entry}
     */
    Entry getEntry();

    /**
     * Set the {@link org.blojsom.blog.Entry} associated with this comment
     *
     * @param blogEntry {@link org.blojsom.blog.Entry}
     */
    void setEntry(Entry entry);

    /**
     *
     * @return
     */
    public Integer getParentId();

    /**
     *
     * @param parentId
     */
    public void setParentId(Integer parentId);

    /**
     *
     * @return
     */
    public String getIp();

    /**
     *
     * @param ip
     */
    public void setIp(String ip);

    /**
     *
     * @return
     */
    public String getStatus();

    /**
     *
     * @param status
     */
    public void setStatus(String status);
}
