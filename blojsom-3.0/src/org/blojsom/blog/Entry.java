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
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Entry
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: Entry.java,v 1.3 2006-04-05 00:46:32 czarneckid Exp $
 */
public interface Entry {

    /**
     * Get the entry ID
     *
     * @return Entry ID
     */
    Integer getId();

    /**
     * Set the entry ID
     *
     * @param id Entry ID
     */
    void setId(Integer id);

    /**
     * Get the blog ID
     *
     * @return Blog ID
     */
    String getBlogId();

    /**
     * Set the blog ID
     *
     * @param blogId Blog ID
     */
    void setBlogId(String blogId);

    /**
     * Get the blog category ID
     *
     * @return Blog category ID
     */
    public Integer getBlogCategoryId();

    /**
     * Set the blog category ID
     *
     * @param blogCategoryId Blog category ID
     */
    public void setBlogCategoryId(Integer blogCategoryId);

    /**
     * Date of the blog entry
     * <p/>
     * This value is constructed from the lastModified value of the file
     *
     * @return Date of the blog entry
     */
    Date getDate();

    /**
     * Date of this blog entry
     *
     * @param entryDate Date of the blog entry
     */
    void setDate(Date entryDate);

    /**
     * Return an RFC 822 style date
     *
     * @return Date formatted in RFC 822 format
     */
    String getRFC822Date();

    /**
     * Return an UTC style date
     *
     * @return Date formatted in UTC format
     */
    String getUTCDate();

    /**
     * Return an ISO 8601 style date
     * http://www.w3.org/TR/NOTE-datetime
     *
     * @return Date formatted in ISO 8601 format
     */
    String getISO8601Date();

    /**
     * Return the blog entry date formatted with a specified date format
     *
     * @param format Date format
     * @return <code>null</code> if the entry date or format is null, otherwise returns the entry date formatted to the specified format. If the format is invalid, returns <tt>entryDate.toString()</tt>
     */
    String getDateAsFormat(String format);

    /**
     * Return the blog entry date formatted with a specified date format
     *
     * @param format Date format
     * @param locale Locale for date formatting
     * @return <code>null</code> if the entry date or format is null, otherwise returns the entry date formatted to the specified format. If the format is invalid, returns <tt>entryDate.toString()</tt>
     */
    String getDateAsFormat(String format, Locale locale);

    /**
     * Title of the blog entry
     *
     * @return Blog title
     */
    String getTitle();

    /**
     * Set the title of the blog entry
     *
     * @param title Title for the blog entry
     */
    void setTitle(String title);

    /**
     * Title for the entry where the &lt;, &gt;, and &amp; characters are escaped
     *
     * @return Escaped entry title
     */
    String getEscapedTitle();

    /**
     * Description of the blog entry
     *
     * @return Blog entry description
     */
    String getDescription();

    /**
     * Escaped description of the blog entry
     * This method would be used for generating RSS feeds where the &lt;, &gt;, and &amp; characters are escaped
     *
     * @return Blog entry description where &amp;, &lt;, and &gt; have been escaped
     */
    String getEscapedDescription();

    /**
     * Set the description for the blog entry
     *
     * @param description Description for the blog entry
     */
    void setDescription(String description);

    /**
     * Category for the blog entry. This corresponds to the category directory name.
     *
     * @return Blog entry category
     */
    String getCategory();

    /**
     * Return the category name encoded.
     *
     * @return Category name encoded as UTF-8
     */
    String getEncodedCategory();

    /**
     * Determines whether or not this blog entry supports comments.
     *
     * @return <code>true</code> if the blog entry supports comments, <code>false</code> otherwise
     */
    Integer getAllowComments();

    /**
     * Whether or not comments are allowed
     *
     * @return <code>true</code> if comments are allowed, <code>false</code> otherwise
     */
    Boolean allowsComments();

    /**
     * Set whether comments are allowed
     *
     * @param allowComments <code>true</code> if comments are allowed, <code>false</code> otherwise
     */
    void setAllowComments(Integer allowComments);

    /**
     * Get the comments
     *
     * @return List of comments
     */
    List getComments();

    /**
     * Set the comments for this blog entry. The comments must be an <code>List</code>
     * of {@link org.blojsom.blog.Comment}. This method will not writeback or change the comments on disk.
     *
     * @param comments Comments for this entry
     */
    void setComments(List comments);

    /**
     * Get the comments as an array of {@link Comment} objects
     *
     * @return BlogComment[] array
     */
    Comment[] getCommentsAsArray();

    /**
     * Get the number of comments for this entry
     *
     * @return 0 if comments is <code>null</code>, or the number of comments otherwise, which could be 0
     */
    int getNumComments();

    /**
     * Determines whether or not this blog entry supports trackbacks.
     *
     * @return <code>true</code> if the blog entry supports trackbacks, <code>false</code> otherwise
     */
    Integer getAllowTrackbacks();

    /**
     * Whether or not trackbacks are allowed
     *
     * @return <code>true</code> if trackbacks are allowed, <code>false</code> otherwise
     */
    Boolean allowsTrackbacks();

    /**
     * Set whether trackbacks are allowed
     *
     * @param allowTrackbacks <code>true</code> if trackbacks are allowed, <code>false</code> otherwise
     */
    void setAllowTrackbacks(Integer allowTrackbacks);

    /**
     * Get the trackbacks
     *
     * @return List of trackbacks
     */
    List getTrackbacks();

    /**
     * Set the trackbacks for this blog entry. The trackbacks must be an <code>List</code>
     * of {@link org.blojsom.blog.Trackback}. This method will not writeback or change the trackbacks to disk.
     *
     * @param trackbacks Trackbacks for this entry
     */
    void setTrackbacks(List trackbacks);

    /**
     * Get the trackbacks as an array of Trackback objects
     *
     * @return Trackback[] array
     */
    Trackback[] getTrackbacksAsArray();

    /**
     * Get the number of trackbacks for this entry
     *
     * @return 0 if trackbacks is <code>null</code>, or the number of trackbacks otherwise, which could be 0
     */
    int getNumTrackbacks();

    /**
     * Get the {@link org.blojsom.blog.Category} object for this blog entry
     *
     * @return {@link org.blojsom.blog.Category} object
     */
    Category getBlogCategory();

    /**
     * Set the {@link org.blojsom.blog.Category} object for this blog entry
     *
     * @param blogCategory New {@link org.blojsom.blog.Category} object
     */
    void setBlogCategory(Category blogCategory);

    /**
     * Return meta data for this blog entry. This method may return <code>null</code>.
     *
     * @return Meta data
     */
    Map getMetaData();

    /**
     * Set the meta-data associated with this blog entry
     *
     * @param metaData Meta-data
     */
    void setMetaData(Map metaData);

    /**
     * Determines whether or not this blog entry supports pingbacks.
     *
     * @return <code>true</code> if the blog entry supports pingbacks, <code>false</code> otherwise
     */
    Integer getAllowPingbacks();

    /**
     * Whether or not pingbacks are allowed
     *
     * @return <code>true</code> if pingbacks are allowed, <code>false</code> otherwise
     */
    Boolean allowsPingbacks();

    /**
     * Set whether pingbacks are allowed
     *
     * @param allowPingbacks <code>true</code> if pingbacks are allowed, <code>false</code> otherwise
     */
    void setAllowPingbacks(Integer allowPingbacks);

    /**
     * Get the pingbacks for this entry
     *
     * @return List of {@link org.blojsom.blog.Pingback}
     */
    List getPingbacks();

    /**
     * Set the pingbacks for this blog entry. The pingbacks must be a <code>List</code>
     * of {@link org.blojsom.blog.Pingback}. This method will not writeback or change the pingbacks to disk.
     *
     * @param pingbacks {@link org.blojsom.blog.Pingback}s for this entry
     */
    void setPingbacks(List pingbacks);

    /**
     * Get the pingbacks as an array of {@link org.blojsom.blog.Pingback}s objects
     *
     * @return {@link org.blojsom.blog.Pingback}[] array
     */
    Pingback[] getPingbacksAsArray();

    /**
     * Get the number of pingbacks for this entry
     *
     * @return 0 if pingbacks is <code>null</code>, or the number of pingbacks otherwise, which could be 0
     */
    int getNumPingbacks();

    /**
     * Get the status
     *
     * @return Status
     */
    public String getStatus();

    /**
     * Set the status
     *
     * @param status Status
     */
    public void setStatus(String status);

    /**
     * Get the author
     *
     * @return Author
     */
    public String getAuthor();

    /**
     * Set the author
     *
     * @param author Author
     */
    public void setAuthor(String author);

    /**
     * Get the post slug
     *
     * @return Post slug
     */
    public String getPostSlug();

    /**
     * Set the post slug
     *
     * @param postSlug Post slug
     */
    public void setPostSlug(String postSlug);

    /**
     * Get the last modified date
     *
     * @return Last modified date
     */
    public Date getModifiedDate();

    /**
     * Set the last modified date
     *
     * @param modifiedDate Last modified date
     */
    public void setModifiedDate(Date modifiedDate);

    /**
     * Get the responses (comments, trackbacks, pingbacks)
     *
     * @return Responses (comments, trackbacks, pingbacks)
     */
    public List getResponses();

    /**
     * Get the responses (comments, trackbacks, pingbacks) matching some status
     *
     * @param status Status
     * @return Responses (comments, trackbacks, pingbacks) matching some status
     */
    public List getResponsesMatchingStatus(String status);
}
