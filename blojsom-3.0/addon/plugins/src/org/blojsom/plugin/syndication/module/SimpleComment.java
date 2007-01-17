/**
 * Copyright (c) 2003-2007, David A. Czarnecki
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
package org.blojsom.plugin.syndication.module;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Simple comment
 *
 * @author David Czarnecki
 * @version $Id: SimpleComment.java,v 1.3 2007-01-17 02:35:06 czarneckid Exp $
 * @since blojsom 3.0
 */
public class SimpleComment implements Cloneable {

    private String author;
    private String authorURL;
    private String authorEmail;
    private String comment;
    private Date commentDate;
    private String ip;
    private String status;
    private List metadata;

    public SimpleComment() {
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorURL() {
        return authorURL;
    }

    public void setAuthorURL(String authorURL) {
        this.authorURL = authorURL;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(Date commentDate) {
        this.commentDate = commentDate;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List getMetadata() {
        return metadata;
    }

    public void setMetadata(List metadata) {
        this.metadata = metadata;
    }

    public Object clone() throws CloneNotSupportedException {
        SimpleComment cloned = new SimpleComment();

        cloned.setAuthor(author);
        cloned.setAuthorEmail(authorEmail);
        cloned.setAuthorURL(authorURL);
        cloned.setComment(comment);
        cloned.setCommentDate(commentDate);
        cloned.setIp(ip);
        cloned.setStatus(status);

        // Process metadata
        List copiedMetadata = new ArrayList();
        if (metadata != null) {
            for (int i = 0; i < metadata.size(); i++) {
                Metadata metadataItem = (Metadata) metadata.get(i);
                Metadata copiedMetadataItem = new Metadata();
                copiedMetadataItem.setKey(metadataItem.getKey());
                copiedMetadataItem.setValue(metadataItem.getValue());

                copiedMetadata.add(copiedMetadataItem);
            }
        }
        cloned.setMetadata(copiedMetadata);

        return cloned;
    }
}
