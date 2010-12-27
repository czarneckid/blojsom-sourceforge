/**
 * Copyright (c) 2003-2009, David A. Czarnecki
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

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * Simple trackback
 *
 * @author David Czarnecki
 * @version $Id: SimpleTrackback.java,v 1.4 2008-07-07 19:54:15 czarneckid Exp $
 * @since blojsom 3.0
 */
public class SimpleTrackback implements Cloneable {

    private String title;
    private String excerpt;
    private String url;
    private String blogName;
    private Date trackbackDate;
    private String ip;
    private String status;
    private List metadata;

    public SimpleTrackback() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBlogName() {
        return blogName;
    }

    public void setBlogName(String blogName) {
        this.blogName = blogName;
    }

    public Date getTrackbackDate() {
        return trackbackDate;
    }

    public void setTrackbackDate(Date trackbackDate) {
        this.trackbackDate = trackbackDate;
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
        SimpleTrackback cloned = new SimpleTrackback();

        cloned.setBlogName(blogName);
        cloned.setExcerpt(excerpt);
        cloned.setIp(ip);
        cloned.setStatus(status);
        cloned.setTitle(title);
        cloned.setTrackbackDate(trackbackDate);
        cloned.setUrl(url);

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
