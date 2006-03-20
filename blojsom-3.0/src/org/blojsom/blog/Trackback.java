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

import java.util.Map;
import java.util.Locale;
import java.util.Date;

/**
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: Trackback.java,v 1.1 2006-03-20 21:31:13 czarneckid Exp $
 */
public interface Trackback {

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
     * Get the title of the trackback
     *
     * @return Trackback title
     */
    String getTitle();

    /**
     * Set the title of the trackback
     *
     * @param title Trackback title
     */
    void setTitle(String title);

    /**
     * Get the excerpt of the trackback
     *
     * @return Trackback excerpt
     */
    String getExcerpt();

    /**
     * Set the excerpt of the trackback
     *
     * @param excerpt Trackback excerpt
     */
    void setExcerpt(String excerpt);

    /**
     * Get the url of the trackback
     *
     * @return Trackback url
     */
    String getUrl();

    /**
     * Set the url of the trackback
     *
     * @param url Trackback url
     */
    void setUrl(String url);

    /**
     * Get the blog name of the trackback
     *
     * @return Trackback blog name
     */
    String getBlogName();

    /**
     * Get the trackback meta-data
     *
     * @return Meta-data as a {@link java.util.Map}
     */
    Map getMetaData();

    /**
     * Set the blog name of the trackback
     *
     * @param blogName Trackback blog name
     */
    void setBlogName(String blogName);

    /**
     * Set the trackback meta-data
     *
     * @param metaData {@link java.util.Map} containing meta-data for this trackback
     */
    void setMetaData(Map metaData);

    /**
     * Return the trackback date formatted with a specified date format
     *
     * @param format Date format
     * @return <code>null</code> if the format is null, otherwise returns the trackback date formatted to
     *         the specified format. If the format is invalid, returns <tt>trackbackDate.toString()</tt>
     */
    String getDateAsFormat(String format);

    /**
     * Return the trackback date formatted with a specified date format
     *
     * @param format Date format
     * @param locale Locale for date formatting
     * @return <code>null</code> if the entry date or format is null, otherwise returns the entry date formatted to the specified format. If the format is invalid, returns <tt>trackbackDate.toString()</tt>
     */
    String getDateAsFormat(String format, Locale locale);

    /**
     * Retrieve the {@link org.blojsom.blog.Entry} associated with this trackback
     *
     * @return {@link org.blojsom.blog.Entry}
     */
    Entry getEntry();

    /**
     * Set the {@link org.blojsom.blog.Entry} associated with this trackback
     *
     * @param blogEntry {@link org.blojsom.blog.Entry}
     */
    void setEntry(Entry blogEntry);

    /**
     * Retrieve the date this trackback was created
     *
     * @return Date trackback was created
     */
    Date getTrackbackDate();

    /**
     * Set the trackback date
     *
     * @param trackbackDate Trackback date
     */
    void setTrackbackDate(Date trackbackDate);

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
