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

/**
 * BlogCategory
 *
 * @author David Czarnecki
 */
public class BlogCategory implements Comparable {

    private Log _logger = LogFactory.getLog(BlogCategory.class);

    private String _categoryURL;
    private String _category;
    private int _numOfEntries = 0;

    /**
     * Create a new BlogCategory
     *
     * @param category Category name
     * @param categoryURL Category URL
     */
    public BlogCategory(String category, String categoryURL) {
        _category = category;
        _categoryURL = categoryURL;
    }

    /**
     * Return the URL for this category
     *
     * @return Category URL
     */
    public String getCategoryURL() {
        return _categoryURL;
    }

    /**
     * Set a new URL for this category
     *
     * @param _categoryURL Category URL
     */
    public void setCategoryURL(String _categoryURL) {
        this._categoryURL = _categoryURL;
    }

    /**
     * Return the category name
     *
     * @return Category name
     */
    public String getCategory() {
        return _category;
    }

    /**
     * Set a new name for this category
     *
     * @param _category Category name
     */
    public void setCategory(String _category) {
        this._category = _category;
    }

    /**
     * Return the number of entries in this category
     *
     * @return Number of category entries
     */
    public int getNumberOfEntries() {
        return _numOfEntries;
    }

    /**
     * Set the number of entries in this category
     *
     * @param _numOfEntries Number of category entries
     */
    public void setNumberOfEntries(int _numOfEntries) {
        this._numOfEntries = _numOfEntries;
    }

    /**
     * Checks to see if this category is equal to the input category
     *
     * @param obj Input category
     * @return <code>true</code> if the category name and category URL are equal, <code>false</code> otherwise
     */
    public boolean equals(Object obj) {
        BlogCategory otherCategory = (BlogCategory) obj;
        return ((_category.equals(otherCategory._category)) && (_categoryURL.equals(otherCategory._categoryURL)));
    }

    /**
     * Compare the current category to the input category
     *
     * @param o Input category
     * @return
     */
    public int compareTo(Object o) {
        BlogCategory category = (BlogCategory) o;

        return _category.compareTo(category._category);
    }

    /**
     * Returns the category name
     *
     * @see #getCategory
     * @return Category name
     */
    public String toString() {
        return _category;
    }
}
