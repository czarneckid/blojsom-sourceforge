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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ignition.blojsom.util.BlojsomUtils;

import java.io.*;
import java.util.Date;

/**
 * BlogEntry
 *
 * @author David Czarnecki
 */
public class BlogEntry {

    private Log _logger = LogFactory.getLog(BlogEntry.class);

    private String _title;
    private String _link;
    private String _description;
    private File _source;
    private String _category;
    private Date _entryDate;
    private long _lastModified;

    /**
     * Create a new blog entry with no data
     */
    public BlogEntry() {
    }

    /**
     * Create a new blog entry
     *
     * @param title Entry title
     * @param link Permalink to the entry
     * @param description Entry description
     * @param source File containing the blog entry
     */
    public BlogEntry(String title, String link, String description, File source) {
        _title = title;
        _link = link;
        _description = description;
        _source = source;
        _entryDate = new Date(_source.lastModified());
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
     * @param _title Title for the blog entry
     */
    public void setTitle(String _title) {
        this._title = _title;
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
     * @return
     */
    public String getEscapedLink() {
        return BlojsomUtils.escapeString(_link);
    }

    /**
     * Set the permalink for the blog entry
     *
     * @param _link Permalink for the blog entry
     */
    public void setLink(String _link) {
        this._link = _link;
    }

    /**
     * File containing the blog entry
     *
     * @return Blog entry file
     */
    public File getSource() {
        return _source;
    }

    /**
     * Set the file of the blog entry
     *
     * @param _source File for the blog entry
     */
    public void setSource(File _source) {
        this._source = _source;
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
        this._description = description;
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
     * Returns the contents of the file in a byte array
     *
     * @param file Input file
     * @return Byte array containing the file contents
     * @throws IOException If there is an error reading the contents of the file
     */
    private static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file: " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    /**
     * Reload the blog entry from disk
     *
     * The first line of the blog entry will be used as the title of the blog
     */
    public void reloadSource() {
        try {
            byte[] fileContents = getBytesFromFile(_source);
            String lineSeparator = System.getProperty("line.separator");
            String description = new String(fileContents);
            int titleIndex = description.indexOf(lineSeparator);
            if (titleIndex == -1) {
                _title = "";
                _description = description;
            } else {
                _title = description.substring(0, titleIndex);
                _description = description.substring(titleIndex + 1);
            }
            _entryDate = new Date(_source.lastModified());
            _lastModified = _source.lastModified();
        } catch (IOException e) {
            _logger.error(e);
        }
    }

    /**
     * Category for the blog entry
     *
     * @return Blog entry category
     */
    public String getCategory() {
        return _category;
    }

    /**
     * Set the category for the blog entry
     *
     * @param _category Category for the blog entry
     */
    public void setCategory(String _category) {
        this._category = _category;
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
        if (this.getLink().equals(entry.getLink())) {
            return true;
        }
        return false;
    }
}
