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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.io.File;
import java.io.IOException;

/**
 * BlogCategory
 *
 * @author David Czarnecki
 * @version $Id: BlogCategory.java,v 1.9 2003-03-23 19:30:45 czarneckid Exp $
 */
public class BlogCategory implements Comparable {

    private Log _logger = LogFactory.getLog(BlogCategory.class);

    private String _categoryURL;
    private String _category;
    private Map _metadata = null;
    private String _description = null;
    private String _name = null;


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
     * @param categoryURL Category URL
     */
    public void setCategoryURL(String categoryURL) {
        _categoryURL = categoryURL;
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
     * @param category Category name
     */
    public void setCategory(String category) {
        _category = category;
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
     * @see #getCategory()
     * @return Category name
     */
    public String toString() {
        return _category;
    }

    /**
     * Sets the description of this category
     *
     * @param desc The new description of the category
     */
    public void setDescription(String desc) {
        _description = desc;
        if (_metadata == null) {
            _metadata = new HashMap(5);
        }
        _metadata.put(BlojsomConstants.DESCRIPTION_KEY, _description);
    }

    /**
     * Retrieves the description of this category
     *
     * @return The description of the category
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Sets the name of this category
     *
     * @param name The new name of the category
     */
    public void setName(String name) {
        _name = name;
        if (_metadata == null) {
            _metadata = new HashMap(5);
        }
        _metadata.put(BlojsomConstants.NAME_KEY, _name);
    }

    /**
     * Retrieves the name of this category
     *
     * @return The name of the category
     */
    public String getName() {
        return _name;
    }

    /**
     * Sets the meta-data associated with this category
     *
     * @param data The properties to be associated with the category as meta-data
     */
    public void setMetaData(Properties data) {
        String s = null;
        Enumeration keys = data.keys();
        Enumeration vals = data.elements();
        if (_metadata == null) {
            _metadata = new HashMap(5);
        }
        while (keys.hasMoreElements()) {
            _metadata.put(keys.nextElement(), vals.nextElement());
        }

        s = (String) _metadata.get(BlojsomConstants.DESCRIPTION_KEY);
        if ((s != null) && (!"".equals(s))) {
            _description = s;
        }

        s = (String) _metadata.get(BlojsomConstants.NAME_KEY);
        if ((s != null) && (!"".equals(s))) {
            _name = s;
        }
    }

    /**
     * Retrieves the meta-data associated with this category
     *
     * @return The properties associated with the category as meta-data, or null if no metadata exists
     */
    public HashMap getMetaData() {
        return (HashMap) _metadata;
    }

    /**
     * Load the meta data for the category
     *
     * @param blogHome Directory where blog entries are stored
     * @param propertiesExtensions List of file extensions to use when looking for category properties
     */
    public void loadMetaData(String blogHome, String[] propertiesExtensions) {
        File blog = new File(blogHome + BlojsomUtils.removeInitialSlash(_category));

        // Load properties file for category (if present)
        File[] categoryPropertyFiles = blog.listFiles(BlojsomUtils.getExtensionsFilter(propertiesExtensions));
        if ((categoryPropertyFiles != null) && (categoryPropertyFiles.length > 0)) {
            Properties dirProps = new Properties();
            for (int i = 0; i < categoryPropertyFiles.length; i++) {
                try {
                    dirProps.load(new java.io.FileInputStream(categoryPropertyFiles[i]));
                } catch (IOException ex) {
                    _logger.warn("Failed loading properties from: " + categoryPropertyFiles[i].toString());
                    continue;
                }
            }

            setMetaData(dirProps);
        }
    }
}
