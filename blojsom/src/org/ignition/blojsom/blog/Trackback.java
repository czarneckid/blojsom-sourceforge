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
 * Trackback
 *
 * @author David Czarnecki
 * @version $Id: Trackback.java,v 1.1 2003-03-18 03:07:43 czarneckid Exp $
 */
public class Trackback {

    private String _title;
    private String _excerpt;
    private String _url;
    private String _blogName;
    private long _trackbackDateLong;

    /**
     * Default constructor
     */
    public Trackback() {
    }

    /**
     * Trackback constructor to take a title, excerpt, url, and blog name
     *
     * @param title Title of the trackback
     * @param excerpt Excerpt from the trackback
     * @param url Url for the trackback
     * @param blogName Blog name of the trackback
     */
    public Trackback(String title, String excerpt, String url, String blogName) {
        _title = title;
        _excerpt = excerpt;
        _url = url;
        _blogName = blogName;
    }

    /**
     * Get the title of the trackback
     *
     * @return Trackback title
     */
    public String getTitle() {
        return _title;
    }

    /**
     * Set the title of the trackback
     *
     * @param title Trackback title
     */
    public void setTitle(String title) {
        _title = title;
    }

    /**
     * Get the excerpt of the trackback
     *
     * @return Trackback excerpt
     */
    public String getExcerpt() {
        return _excerpt;
    }

    /**
     * Set the excerpt of the trackback
     *
     * @param excerpt Trackback excerpt
     */
    public void setExcerpt(String excerpt) {
        _excerpt = excerpt;
    }

    /**
     * Get the url of the trackback
     *
     * @return Trackback url
     */
    public String getUrl() {
        return _url;
    }

    /**
     * Set the url of the trackback
     *
     * @param url Trackback url
     */
    public void setUrl(String url) {
        _url = url;
    }

    /**
     * Get the blog name of the trackback
     *
     * @return Trackback blog name
     */
    public String getBlogName() {
        return _blogName;
    }

    /**
     * Set the blog name of the trackback
     *
     * @param blogName Trackback blog name
     */
    public void setBlogName(String blogName) {
        _blogName = blogName;
    }

    /**
     * Set the date for the trackback
     *
     * @param trackbackDateLong Trackback date as a <code>long</code> value
     */
    public void setTrackbackDateLong(long trackbackDateLong) {
        _trackbackDateLong = trackbackDateLong;
    }

    /**
     * Get the date of the trackback
     *
     * @return Date of the trackback as a <code>long</code>
     */
    public long getTrackbackDateLong() {
        return _trackbackDateLong;
    }

    /**
     * Get the date of the trackback
     *
     * @return Date of the trackback as a <code>java.util.Date</code>
     */
    public Date getTrackbackDate() {
        return new Date(_trackbackDateLong);
    }
}
