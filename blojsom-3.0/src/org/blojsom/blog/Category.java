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

/**
 * Category
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: Category.java,v 1.2 2006-03-21 02:40:40 czarneckid Exp $
 */
public interface Category {

    /**
     * Get the category ID
     *
     * @return Category ID
     */
    Integer getId();

    /**
     * Set the category ID
     *
     * @param id Category ID
     */
    void setId(Integer id);

    /**
     * Get the blog parent category ID
     *
     * @return Parent category ID
     */
    Integer getParentCategoryId();

    /**
     * Se the parent category ID
     *
     * @param parentCategoryId Parent category ID
     */
    void setParentCategoryId(Integer id);

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
     * Return the category name
     *
     * @return Category name
     */
    String getName();

    /**
     * Return the category name encoded for a link
     *
     * @return Category name encoded as UTF-8 with preserved "/" and "+" characters
     */
    String getEncodedName();

    /**
     * Set a new name for this category
     *
     * @param name Category name
     */
    void setName(String name);

    /**
     * Sets the description of this category
     *
     * @param desc The new description of the category
     */
    void setDescription(String desc);

    /**
     * Retrieves the description of this category
     *
     * @return The description of the category
     */
    String getDescription();

    /**
     * Set the meta-data associated with this category
     *
     * @param metadata The map to be associated with the category as meta-data
     */
    void setMetaData(Map metadata);

    /**
     * Retrieves the meta-data associated with this category
     *
     * @return The properties associated with the category as meta-data, or null if no metadata exists
     */
    Map getMetaData();

    /**
     * Returns the parent category of this category. Defaults to "/"
     *
     * @return {@link org.blojsom.blog.Category} containing the parent category
     */
    Category getParentCategory();

    /**
     * Sets the parent category of this category
     *
     * @param cateogory {@link org.blojsom.blog.Category} that represents the parent category
     */
    void setParentCategory(Category category);
}
