/**
 * Copyright (c) 2003, David A. Czarnecki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the "David A. Czarnecki" nor the names of
 * its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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

import java.util.Date;

/**
 * BlogComment
 *
 * @author David Czarnecki
 * @version $Id: BlogComment.java,v 1.1 2003-03-04 03:31:29 czarneckid Exp $
 */
public class BlogComment {

    private String _author;
    private String _authorEmail;
    private String _authorURL;
    private String _comment;
    private Date _commentDate;

    /**
     * Default constructor
     */
    public BlogComment() {
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
     * Set the URL for the author
     *
     * @param authorURL New URL for the author
     */
    public void setAuthorURL(String authorURL) {
        _authorURL = authorURL;
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
     * Set the date for the comment
     *
     * @param commentDate Comment date
     */
    public void setCommentDate(Date commentDate) {
        _commentDate = commentDate;
    }
}
