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
import java.util.Locale;
import java.util.Map;

/**
 * @author David Czarnecki
 * @version $Id: Trackback.java,v 1.5 2006-09-26 02:55:21 czarneckid Exp $
 * @since blojsom 3.0
 */
public interface Trackback extends Response {

    /**
     * Set the trackback ID
     *
     * @param id Trackback ID
     */
    void setId(Integer id);

    /**
     * Get the trackback ID
     *
     * @return Trackback ID
     */
    public Integer getId();

    /**
     * Set the blog ID
     *
     * @param blogId Blog ID
     */
    void setBlogId(Integer blogId);

    /**
     * Get the blog ID
     *
     * @return Blog ID
     */
    Integer getBlogId();

    /**
     * Get the blog entry ID
     *
     * @return Blog entry ID
     */
    public Integer getBlogEntryId();

    /**
     * Set the blog entry ID
     *
     * @param blogEntryId Blog entry ID
     */
    public void setBlogEntryId(Integer blogEntryId);

    /**
     * Get the title of the trackback
     *
     * @return Trackback title
     */
    String getTitle();

    /**
     * Get the escaped title of the trackback
     *
     * @return Escaped title
     */
    String getEscapedTitle();

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
     * Get the excerpt as an escaped string
     *
     * @return Escaped excerpt
     */
    String getEscapedExcerpt();

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
     * Get the escaped url of the trackback
     *
     * @return Escaped url
     */
    String getEscapedUrl();

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
     * Get the escaped blog name of the trackback
     *
     * @return Escaped blog name
     */
    String getEscapedBlogName();


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
     * Retrieve the {@link Entry} associated with this trackback
     *
     * @return {@link Entry}
     */
    Entry getEntry();

    /**
     * Set the {@link Entry} associated with this trackback
     *
     * @param blogEntry {@link Entry}
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
}
