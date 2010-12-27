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
package org.blojsom.blog.database;

import org.blojsom.blog.Entry;
import org.blojsom.blog.Trackback;
import org.blojsom.util.BlojsomUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * DatabaseTrackback
 *
 * @author David Czarnecki
 * @version $Id: DatabaseTrackback.java,v 1.8 2008-07-07 19:55:05 czarneckid Exp $
 * @since blojsom 3.0
 */
public class DatabaseTrackback implements Trackback, Serializable {

    protected Integer _id;
    protected Integer _blogId;
    protected Integer _blogEntryId;
    protected Entry _entry;

    protected String _title;
    protected String _excerpt;
    protected String _url;
    protected String _blogName;
    protected Date _trackbackDate;
    protected long _trackbackDateLong;
    protected Map _metaData;
    protected String _status;
    protected String _ip;

    /**
     * Create a new instance of the database trackback
     */
    public DatabaseTrackback() {
    }

    /**
     * Set the trackback ID
     *
     * @param id Trackback ID
     */
    public void setId(Integer id) {
        _id = id;
    }

    /**
     * Get the trackback ID
     *
     * @return Trackback ID
     */
    public Integer getId() {
        return _id;
    }

    /**
     * Set the blog ID
     *
     * @param blogId Blog ID
     */
    public void setBlogId(Integer blogId) {
        _blogId = blogId;
    }

    /**
     * Get the blog ID
     *
     * @return Blog ID
     */
    public Integer getBlogId() {
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
     * Retrieve the {@link BlogEntry} associated with this trackback
     *
     * @return {@link BlogEntry}
     */
    public Entry getEntry() {
        return _entry;
    }

    /**
     * Set the {@link BlogEntry} associated with this trackback
     *
     * @param blogEntry {@link BlogEntry}
     */
    public void setEntry(Entry entry) {
        _entry = entry;
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
     * Get the escaped title of the trackback
     *
     * @return Escaped title
     */
    public String getEscapedTitle() {
        return BlojsomUtils.escapeString(_title);
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
     * Get the excerpt as an escaped string
     *
     * @return Escaped excerpt
     */
    public String getEscapedExcerpt() {
        return BlojsomUtils.escapeString(_excerpt);
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
     * Get the escaped url of the trackback
     *
     * @return Escaped url
     */
    public String getEscapedUrl() {
        return BlojsomUtils.escapeString(_url);
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
     * Get the escaped blog name of the trackback
     *
     * @return Escaped blog name
     */
    public String getEscapedBlogName() {
        return BlojsomUtils.escapeString(_blogName);
    }

    /**
     * Get the trackback meta-data
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
        _trackbackDate = new Date(_trackbackDateLong);
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
     * Set the trackback meta-data
     *
     * @param metaData {@link Map} containing meta-data for this trackback
     */
    public void setMetaData(Map metaData) {
        _metaData = metaData;
    }

    /**
     * Return the trackback date formatted with a specified date format
     *
     * @param format Date format
     * @return <code>null</code> if the format is null, otherwise returns the trackback date formatted to
     *         the specified format. If the format is invalid, returns <tt>trackbackDate.toString()</tt>
     */
    public String getDateAsFormat(String format) {
        return getDateAsFormat(format, null);
    }

    /**
     * Return the trackback date formatted with a specified date format
     *
     * @param format Date format
     * @param locale Locale for date formatting
     * @return <code>null</code> if the entry date or format is null, otherwise returns the entry date formatted to the specified format. If the format is invalid, returns <tt>trackbackDate.toString()</tt>
     */
    public String getDateAsFormat(String format, Locale locale) {
        if (_trackbackDate == null || format == null) {
            return null;
        }

        SimpleDateFormat sdf;
        try {
            if (locale == null) {
                sdf = new SimpleDateFormat(format);
            } else {
                sdf = new SimpleDateFormat(format, locale);
            }

            return sdf.format(_trackbackDate);
        } catch (IllegalArgumentException e) {
            return _trackbackDate.toString();
        }
    }

    /**
     * Retrieve the date this trackback was created
     *
     * @return Date trackback was created
     */
    public Date getTrackbackDate() {
        return _trackbackDate;
    }

    /**
     * Set the trackback date
     *
     * @param trackbackDate Trackback date
     */
    public void setTrackbackDate(Date trackbackDate) {
        _trackbackDate = trackbackDate;
        _trackbackDateLong = _trackbackDate.getTime();
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
     * Get the IP
     *
     * @return IP
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
        return _trackbackDate;
    }

    /**
     * Get the response type
     *
     * @return Response type
     */
    public String getType() {
        return TRACKBACK_TYPE;
    }
}
