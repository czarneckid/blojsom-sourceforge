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
package org.ignition.blojsom.extension.xmlrpc.handlers;

import org.ignition.blojsom.BlojsomException;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.extension.xmlrpc.BlojsomXMLRPCConstants;
import org.ignition.blojsom.fetcher.BlojsomFetcher;
import org.ignition.blojsom.util.BlojsomConstants;
import org.ignition.blojsom.util.BlojsomUtils;


/**
 * Abstract blojsom API handler
 *
 * @author Mark Lussier
 * @version $Id: AbstractBlojsomAPIHandler.java,v 1.8 2003-08-09 16:53:11 intabulas Exp $
 */
public abstract class AbstractBlojsomAPIHandler implements BlojsomConstants, BlojsomXMLRPCConstants {

    AbstractBlojsomAPIHandler() {
    }

    public static final int    AUTHORIZATION_EXCEPTION = 0001;
    public static final String AUTHORIZATION_EXCEPTION_MSG = "Invalid Username and/or Password";

    public static final int    UNKNOWN_EXCEPTION = 1000;
    public static final String UNKNOWN_EXCEPTION_MSG = "An error occured processing your request";

    public static final int    UNSUPPORTED_EXCEPTION = 1001;
    public static final String UNSUPPORTED_EXCEPTION_MSG = "Unsupported method - blojsom does not support this blogger concept";

    public static final int    INVALID_POSTID = 2000;
    public static final String INVALID_POSTID_MSG = "The entry postid you submitted is invalid";

    public static final int    NOBLOGS_EXCEPTION = 3000;
    public static final String NOBLOGS_EXCEPTION_MSG = "There are no categories defined for this blojsom";

    protected Blog _blog;
    protected BlojsomFetcher _fetcher;
    protected String _blogEntryExtension;

    /**
     * Attach a blog instance to the API Handler so that it can interact with the blog
     *
     * @param bloginstance an instance of Blog
     * @see org.ignition.blojsom.blog.Blog
     * @throws BlojsomException If there is an error setting the blog instance or properties for the handler
     */
    public abstract void setBlog(Blog bloginstance) throws BlojsomException;

    /**
     * Gets the name of API Handler. Used to bind to XML-RPC
     *
     * @return The API Name (ie: blogger)
     */
    public abstract String getName();

    /**
     * Set the {@link BlojsomFetcher} instance that will be used to fetch categories and entries
     *
     * @param fetcher {@link BlojsomFetcher} instance
     * @throws BlojsomException If there is an error in setting the fetcher
     */
    public abstract void setFetcher(BlojsomFetcher fetcher) throws BlojsomException;

    /**
     * Return a filename appropriate for the blog entry content
     *
     * @param content Blog entry content
     * @return Filename for the new blog entry
     */
    protected String getBlogEntryFilename(String content) {
        String hashable = content;

        if (content.length() > MAX_HASHABLE_LENGTH) {
            hashable = hashable.substring(0, MAX_HASHABLE_LENGTH);
        }

        String baseFilename = BlojsomUtils.digestString(hashable).toUpperCase();
        String filename = baseFilename + _blogEntryExtension;
        return filename;
    }
}

