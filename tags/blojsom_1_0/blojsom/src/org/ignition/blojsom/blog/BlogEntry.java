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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
     * Permalink for the blog entry
     *
     * @return Blog entry permalink
     */
    public String getLink() {
        return _link;
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
     * This method would be used for generating RSS feeds where &lt; and &gt; need to be
     * escaped
     *
     * @return Blog entry description where &lt; and &gt; have been escaped
     */
    public String getEscapedDescription() {
        return BlojsomUtils.escapeBrackets(_description);
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
     * Reload the blog entry from disk
     *
     * The first line of the blog entry will be used as the title of the blog
     */
    public void reloadSource() {
        boolean hasLoadedTitle = false;
        try {
            BufferedReader br = new BufferedReader(new FileReader(_source));
            String line;
            StringBuffer description = new StringBuffer();
            while ((line = br.readLine()) != null) {
                if (!hasLoadedTitle) {
                    _title = line;
                    hasLoadedTitle = true;
                } else {
                    description.append(line);
                }
            }
            br.close();
            _description = description.toString();
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
}
