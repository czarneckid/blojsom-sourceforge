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
package org.blojsom.extension.atomapi;

import org.blojsom.blog.Blog;
import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;
import org.blojsom.util.BlojsomUtils;
import org.intabulas.sandler.elements.Author;
import org.intabulas.sandler.elements.Content;
import org.intabulas.sandler.elements.Entry;
import org.intabulas.sandler.elements.impl.AuthorImpl;
import org.intabulas.sandler.elements.impl.ContentImpl;
import org.intabulas.sandler.elements.impl.EntryImpl;

import java.util.Date;

/**
 * AtomUtils
 *
 * @author Mark Lussier
 * @since blojsom 2.0
 * @version $Id: AtomUtils.java,v 1.4 2003-09-10 19:15:27 intabulas Exp $
 */
public class AtomUtils {

    public static String generateNextNonce() {
        //@todo create algorithm format
        return BlojsomUtils.digestString("Blah", "SHA");

    }


    public static Entry fromBlogEntry(Blog blog, BlogUser user, BlogEntry blogentry) {
        Entry result = new EntryImpl();
        result.setTitle(blogentry.getEscapedTitle());
        result.setSummary(blogentry.getEscapedTitle());
        result.setCreated(blogentry.getDate());
        result.setIssued(blogentry.getDate());
        result.setModified(new Date(blogentry.getLastModified()));
        result.setId(blogentry.getEscapedLink());
        result.setLink(blogentry.getEscapedLink());
        Author author = new AuthorImpl();
        author.setName(blog.getBlogOwner());
        author.setEmail(blog.getBlogOwnerEmail());
        author.setUrl(blog.getBlogURL());
        result.setAuthor(author);

        Content content = new ContentImpl();
        content.setMimeType("text/html");
        content.setBody(blogentry.getEscapedDescription());
        result.addContent(content);
        return result;

    }

    public static Entry fromBlogEntrySearch(Blog blog, BlogUser user, BlogEntry blogentry) {
        Entry result = new EntryImpl();
        result.setTitle(blogentry.getEscapedTitle());
        result.setId(blog.getBlogBaseURL() + "/atomapi/" + user.getId() + "/?permalink=" + blogentry.getPermalink());
        return result;

    }


}
