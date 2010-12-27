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
package org.blojsom.plugin.admin.event;

import org.blojsom.blog.BlogCategory;
import org.blojsom.blog.BlogUser;
import org.blojsom.event.BlojsomEvent;

import java.util.Date;

/**
 * BlogCategoryEvent
 *
 * @author David Czarnecki
 * @since blojsom 2.28
 * @version $Id: $
 */
public class BlogCategoryEvent extends BlojsomEvent {

    protected BlogCategory _blogCategory;
    protected BlogUser _blogUser;

    /**
     * Create a new event indicating something happened with a category in the system.
     *
     * @param source Source of the event
     * @param timestamp Event timestamp
     * @param blogCategory {@link BlogCategory}
     * @param blogUser {@link BlogUser}
     */
    public BlogCategoryEvent(Object source, Date timestamp, BlogCategory blogCategory, BlogUser blogUser) {
        super(source, timestamp);
        _blogCategory = blogCategory;
        _blogUser = blogUser;
    }

    /**
     * Retrieve the {@link BlogCategory} associated with this event
     *
     * @return {@link BlogCategory}
     */
    public BlogCategory getBlogCategory() {
        return _blogCategory;
    }

    /**
     * Retrieve the {@link BlogUser} associated with this event
     *
     * @return {@link BlogUser}
     */
    public BlogUser getBlogUser() {
        return _blogUser;
    }
}
