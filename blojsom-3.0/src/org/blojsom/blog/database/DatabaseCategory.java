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
package org.blojsom.blog.database;

import org.blojsom.blog.Category;
import org.blojsom.util.BlojsomUtils;

import java.io.Serializable;
import java.util.Map;

/**
 * DatabaseCategory
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: DatabaseCategory.java,v 1.1 2006-03-20 21:31:13 czarneckid Exp $
 */
public class DatabaseCategory implements Category, Serializable {

    private Integer _id;
    private Integer _parentCategoryId;
    private String _blogId;
    private Category _parentCategory;

    protected String _name;
    protected Map _metaData = null;
    protected String _description = null;

    /**
     *
     */
    public DatabaseCategory() {
    }

    /**
     *
     * @return
     */
    public Integer getId() {
        return _id;
    }

    /**
     *
     * @param id
     */
    public void setId(Integer id) {
        _id = id;
    }

    /**
     *
     * @return
     */
    public Integer getParentCategoryId() {
        return _parentCategoryId;
    }

    /**
     *
     * @param parentCategoryId
     */
    public void setParentCategoryId(Integer parentCategoryId) {
        _parentCategoryId = parentCategoryId;
    }

    /**
     *
     * @return
     */
    public String getBlogId() {
        return _blogId;
    }

    /**
     * 
     * @param blogId
     */
    public void setBlogId(String blogId) {
        _blogId = blogId;
    }

    /**
     * Set a new name for this category
     *
     * @param name Category name
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Return the category name
     *
     * @return Category name
     */
    public String getName() {
        return _name;
    }

    /**
     * Return the category name encoded for a link
     *
     * @return Category name encoded as UTF-8 with preserved "/" and "+" characters
     */
    public String getEncodedName() {
        return BlojsomUtils.urlEncodeForLink(_name);
    }

    /**
     * Sets the description of this category
     *
     * @param desc The new description of the category
     */
    public void setDescription(String description) {
        _description = description;
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
     * Set the meta-data associated with this category
     *
     * @param metaData The map to be associated with the category as meta-data
     */
    public void setMetaData(Map metaData) {
        _metaData = metaData;
    }

    /**
     * Retrieves the meta-data associated with this category
     *
     * @return The properties associated with the category as meta-data, or null if no metadata exists
     */
    public Map getMetaData() {
        return _metaData;
    }

    /**
     * Returns the parent category of this category. Defaults to "/"
     *
     * @return {@link org.blojsom.blog.Category} containing the parent category
     */
    public Category getParentCategory() {
        return _parentCategory;
    }

    /**
     * Sets the parent category of this category
     *
     * @param cateogory {@link org.blojsom.blog.Category} that represents the parent category
     */
    public void setParentCategory(Category parentCategory) {
        _parentCategory = parentCategory;
    }
}
