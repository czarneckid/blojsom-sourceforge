/**
 * Copyright (c) 2003-2004, David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003-2004 by Mark Lussier
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
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.intabulas.sandler.authentication.DigestUtilities;
import org.intabulas.sandler.elements.Author;
import org.intabulas.sandler.elements.Content;
import org.intabulas.sandler.elements.Entry;
import org.intabulas.sandler.elements.Link;
import org.intabulas.sandler.elements.impl.AuthorImpl;
import org.intabulas.sandler.elements.impl.ContentImpl;
import org.intabulas.sandler.elements.impl.EntryImpl;
import org.intabulas.sandler.elements.impl.LinkImpl;

import java.util.Date;

/**
 * AtomUtils
 *
 * @author Mark Lussier
 * @since blojsom 2.0
 * @version $Id: AtomUtils.java,v 1.11 2004-01-11 03:58:35 czarneckid Exp $
 */
public class AtomUtils implements AtomConstants {

    /**
     * Generate a NONCE value based on the the current blog
     *
     * @param user BlogUser instance for the particular blog
     * @return String that is a SHA digest (in hex) of the NONCE value
     * todo Optimize the format we gen the nonce from
     */
    public static String generateNextNonce(BlogUser user) {
        String nonce = BlojsomUtils.getISO8601Date(new Date()) + ":" + user.getId() + ":" +
                user.getBlog().getBlogDescription();

        return DigestUtilities.digestString(nonce);
    }

    /**
     * Generate an Atom Entry object from a Blojsom BlogEntry object
     *
     * @param blog Blog instance
     * @param user BlogUser instance
     * @param blogentry BlogEntry to convert
     * @return Entry object populated from the BlogEntry
     */
    public static Entry fromBlogEntry(Blog blog, BlogUser user, BlogEntry blogentry) {
        Entry result = new EntryImpl();
        result.setTitle(blogentry.getEscapedTitle());
        result.setSummary(blogentry.getEscapedTitle());
        result.setCreated(blogentry.getDate());
        result.setIssued(blogentry.getDate());
        result.setModified(new Date(blogentry.getLastModified()));
        result.setId(blogentry.getEscapedLink());


        Link link = new LinkImpl();
        link.setType("text/html");
        link.setRelationship("alternate");
        link.setHref(blogentry.getEscapedLink());
        result.addLink(link);

        Author author = new AuthorImpl();
        author.setName(blog.getBlogOwner());
        author.setEmail(blog.getBlogOwnerEmail());
        author.setUrl(blog.getBlogURL());
        result.setAuthor(author);

        Content content = new ContentImpl();
        content.setMimeType(CONTENTTYPE_HTML);
        content.setBody(blogentry.getEscapedDescription());
        result.addContent(content);

        return result;
    }

    /**
     * Generates a slim Entry object (tile and id only) from a BlogEntry object
     *
     * @param blog Blog instance
     * @param user BlogUser instance
     * @param blogentry BlogEntry to convert
     * @param servletMapping Servlet mapping for the Atom API
     * @return Entry object populated from the BlogEntry
     */
    public static Entry fromBlogEntrySearch(Blog blog, BlogUser user, BlogEntry blogentry, String servletMapping) {
        Entry result = new EntryImpl();
        result.setTitle(blogentry.getEscapedTitle());
        result.setId(blog.getBlogBaseURL() + servletMapping + user.getId()
                + "/?" + BlojsomConstants.PERMALINK_PARAM + "=" + blogentry.getPermalink());

        return result;
    }
}
