/**
 * Copyright (c) 2003, David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003 by Mark Lussier
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the "David A. Czarnecki" and "blojsom" nor the names of
 * its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Products derived from this software may not be called "blojsom",
 * nor may "blojsom" appear in their name, without prior written permission of
 * David A. Czarnecki.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ignition.blojsom.util.BlojsomConstants;
import org.ignition.blojsom.util.BlojsomUtils;
import org.ignition.blojsom.BlojsomException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * BlogEntry
 *
 * @author David Czarnecki
 * @version $Id: BlogEntry.java,v 1.35 2003-05-31 18:40:45 czarneckid Exp $
 */
public abstract class BlogEntry implements BlojsomConstants {

    protected Log _logger = LogFactory.getLog(BlogEntry.class);

    protected String _title;
    protected String _link;
    protected String _description;
    protected String _category;
    protected Date _entryDate;
    protected long _lastModified;
    protected ArrayList _comments;
    protected ArrayList _trackbacks;
    protected BlogCategory _blogCategory;
    protected Map _metaData;

    /**
     * Create a new blog entry with no data
     */
    public BlogEntry() {
    }

    /**
     * Date of the blog entry
     *
     * This value is constructed from the lastModified value of the file
     *
     * @return Date of the blog entry
     */
    public Date getDate() {
        return _entryDate;
    }

    /**
     * Date of this blog entry
     *
     * @param entryDate Date of the blog entry
     */
    public void setDate(Date entryDate) {
        _entryDate = entryDate;
    }

    /**
     * Return an RFC 822 style date
     *
     * @return Date formatted in RFC 822 format
     */
    public String getRFC822Date() {
        return BlojsomUtils.getRFC822Date(_entryDate);
    }

    /**
     * Return an ISO 8601 style date
     * http://www.w3.org/TR/NOTE-datetime
     *
     * @return Date formatted in ISO 8601 format
     */
    public String getISO8601Date() {
        return BlojsomUtils.getISO8601Date(_entryDate);
    }

    /**
     * Title of the blog entry
     *
     * @return Blog title
     */
    public String getTitle() {
        return _title;
    }

    /**
     * Set the title of the blog entry
     *
     * @param title Title for the blog entry
     */
    public void setTitle(String title) {
        _title = title;
    }

    /**
     * Title for the entry where the &lt;, &gt;, and &amp; characters are escaped
     *
     * @return Escaped entry title
     */
    public String getEscapedTitle() {
        return BlojsomUtils.escapeString(_title);
    }

    /**
     * Permalink for the blog entry
     *
     * @return Blog entry permalink
     */
    public String getLink() {
        return _link;
    }

    /**
     * Permalink for the blog entry where the &lt;, &gt;, and &amp; characters are escaped
     *
     * @return Blog entry permalink which has been escaped
     */
    public String getEscapedLink() {
        return BlojsomUtils.escapeString(_link);
    }

    /**
     * Set the permalink for the blog entry
     *
     * @param link Permalink for the blog entry
     */
    public void setLink(String link) {
        _link = link;
    }

    /**
     * Description of the blog entry
     *
     * @return Blog entry description
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Escaped description of the blog entry
     * This method would be used for generating RSS feeds where the &lt;, &gt;, and &amp; characters are escaped
     *
     * @return Blog entry description where &amp;, &lt;, and &gt; have been escaped
     */
    public String getEscapedDescription() {
        return BlojsomUtils.escapeString(_description);
    }

    /**
     * Set the description for the blog entry
     *
     * @param description Description for the blog entry
     */
    public void setDescription(String description) {
        _description = description;
    }

    /**
     * Last modified time for the blog entry
     *
     * @return Blog entry last modified time
     */
    public long getLastModified() {
        return _lastModified;
    }

    /**
     * Returns the BlogId  for this entry
     *
     * @return Blog Id
     */
    public abstract String getId();

    /**
     * Return the permalink name for this blog entry
     *
     * @return Permalink name
     */
    public abstract String getPermalink();

    /**
     * Category for the blog entry. This corresponds to the category directory name.
     *
     * @return Blog entry category
     */
    public String getCategory() {
        return _category;
    }

    /**
     * Set the category for the blog entry. This corresponds to the category directory name.
     *
     * @param category Category for the blog entry
     */
    public void setCategory(String category) {
        _category = category;
    }

    /**
     * Checks to see if the link to this entry is the same as the input entry
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (!(o instanceof BlogEntry)) {
            return false;
        }

        BlogEntry entry = (BlogEntry) o;
        if (_link.equals(entry.getLink())) {
            return true;
        }
        return false;
    }

    /**
     * Determines whether or not this blog entry supports comments.
     *
     * @return <code>true</code> if the blog entry supports comments, <code>false</code> otherwise
     */
    public abstract boolean supportsComments();

    /**
     * Get the comments
     *
     * @return ArrayList of comments
     */
    public ArrayList getComments() {
        return _comments;
    }

    /**
     * Set the comments for this blog entry. The comments must be an <code>ArrayList</code>
     * of {@link BlogComment}. This method will not writeback or change the comments
     *
     * @param comments Comments for this entry
     */
    public void setComments(ArrayList comments) {
        _comments = comments;
    }

    /**
     * Get the comments as an array of BlogComment objects
     *
     * @return BlogComment[] array
     */
    public BlogComment[] getCommentsAsArray() {
        if (_comments == null) {
            return null;
        } else {
            return (BlogComment[]) _comments.toArray(new BlogComment[_comments.size()]);
        }
    }

    /**
     * Get the number of comments for this entry
     *
     * @return 0 if comments is <code>null</code>, or the number of comments otherwise, which could be 0
     */
    public int getNumComments() {
        if (_comments == null) {
            return 0;
        } else {
            return _comments.size();
        }
    }

    /**
     * Get the trackbacks
     *
     * @return ArrayList of trackbacks
     */
    public ArrayList getTrackbacks() {
        return _trackbacks;
    }

    /**
     * Set the trackbacks for this blog entry. The trackbacks must be an <code>ArrayList</code>
     * of {@link Trackback}. This method will not writeback or change the trackbacks
     *
     * @param trackbacks Trackbacks for this entry
     */
    public void setTrackbacks(ArrayList trackbacks) {
        _trackbacks = trackbacks;
    }

    /**
     * Get the trackbacks as an array of Trackback objects
     *
     * @return Trackback[] array
     */
    public Trackback[] getTrackbacksAsArray() {
        if (_trackbacks == null) {
            return null;
        } else {
            return (Trackback[]) _trackbacks.toArray(new Trackback[_trackbacks.size()]);
        }
    }

    /**
     * Get the number of trackbacks for this entry
     *
     * @return 0 if trackbacks is <code>null</code>, or the number of trackbacks otherwise, which could be 0
     */
    public int getNumTrackbacks() {
        if (_trackbacks == null) {
            return 0;
        } else {
            return _trackbacks.size();
        }
    }

    /**
     * Get the {@link BlogCategory} object for this blog entry
     *
     * @return {@link BlogCategory} object
     */
    public BlogCategory getBlogCategory() {
        return _blogCategory;
    }

    /**
     * Set the {@link BlogCategory} object for this blog entry
     *
     * @param blogCategory New {@link BlogCategory} object
     */
    public void setBlogCategory(BlogCategory blogCategory) {
        _blogCategory = blogCategory;
    }

    /**
     * Return meta data for this blog entry. This method may return <code>null</code>.
     *
     * @since blojsom 1.8
     * @return Meta data
     */
    public Map getMetaData() {
        return _metaData;
    }

    /**
     * Set the meta-data associated with this blog entry
     *
     * @param metaData Meta-data
     * @since blojsom 1.8
     */
    public void setMetaData(Map metaData) {
        _metaData = metaData;
    }

    /**
     * Return a string representation of the entry. The default implementation is to return
     * the blog entry title.
     *
     * @since blojsom 1.9
     * @return String representation of this entry
     */
    public String toString() {
        return _title;
    }

    /**
     * Set any attributes of the blog entry using data from the map.
     *
     * @param attributeMap Attributes
     */
    public void setAttributes(Map attributeMap) {
    }

    /**
     * Load a blog entry.
     *
     * @since blojsom 1.9
     * @param blog Blog
     * @throws BlojsomException If there is an error loading the entry
     */
    public abstract void loadEntry(Blog blog) throws BlojsomException;

    /**
     * Save the blog entry.
     *
     * @since blojsom 1.9
     * @param blog Blog
     * @throws BlojsomException If there is an error saving the entry
     */
    public abstract void saveEntry(Blog blog) throws BlojsomException;

    /**
     * Delete the blog entry.
     *
     * @since blojsom 1.9
     * @param blog Blog
     * @throws BlojsomException If there is an error deleting the entry
     */
    public abstract void deleteEntry(Blog blog) throws BlojsomException;
}
