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
package org.ignition.blojsom.plugin.lastmodified;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.blog.BlogComment;
import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.plugin.BlojsomPlugin;
import org.ignition.blojsom.plugin.BlojsomPluginException;
import org.ignition.blojsom.util.BlojsomConstants;
import org.ignition.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

/**
 * LastModifiedPlugin
 *
 * @author David Czarnecki
 * @since blojsom 1.9.4
 */
public class LastModifiedPlugin implements BlojsomPlugin, BlojsomConstants {

    private Log _logger = LogFactory.getLog(LastModifiedPlugin.class);

    /**
     * Default constructor.
     */
    public LastModifiedPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     * @param blog {@link Blog} instance
     * @throws BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, Blog blog) throws BlojsomPluginException {
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @param context Context
     * @param entries Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws BlojsomPluginException If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                               Map context, BlogEntry[] entries) throws BlojsomPluginException {

        String blogdate = null;
        String blogISO8601Date = null;
        String blogUTCDate = null;
        Date blogDateObject = null;

        // If we have entries, construct a last modified on the most recent entry
        // Additionally, set the blog date
        if ((entries != null) && (entries.length > 0)) {
            BlogEntry _entry = entries[0];
            long _lastmodified;

            if (_entry.getNumComments() > 0) {
                BlogComment _comment = _entry.getCommentsAsArray()[_entry.getNumComments() - 1];
                _lastmodified = _comment.getCommentDateLong();
                _logger.debug("Adding last-modified header for most recent entry comment");
            } else {
                _lastmodified = _entry.getLastModified();
                _logger.debug("Adding last-modified header for most recent blog entry");
            }

            // Check for the Last-Modified object from one of the plugins
            if (httpServletRequest.getSession().getAttribute(BLOJSOM_LAST_MODIFIED) != null) {
                Long lastModified = (Long) httpServletRequest.getSession().getAttribute(BLOJSOM_LAST_MODIFIED);
                if (lastModified.longValue() > _lastmodified) {
                    _lastmodified = lastModified.longValue();
                }
            }

            // Generates an ETag header based on the string value of LastModified as an ISO8601 Format
            String etagLastModified = BlojsomUtils.getISO8601Date(new Date(_lastmodified));
            httpServletResponse.addHeader(HTTP_ETAG, "\"" + BlojsomUtils.digestString(etagLastModified) + "\"");

            httpServletResponse.addDateHeader(HTTP_LASTMODIFIED, _lastmodified);
            blogdate = entries[0].getRFC822Date();
            blogISO8601Date = entries[0].getISO8601Date();
            blogDateObject = entries[0].getDate();
            blogUTCDate = BlojsomUtils.getUTCDate(entries[0].getDate());
        } else {
            _logger.debug("Adding last-modified header for current date");
            Date today = new Date();
            blogdate = BlojsomUtils.getRFC822Date(today);
            blogISO8601Date = BlojsomUtils.getISO8601Date(today);
            blogUTCDate = BlojsomUtils.getUTCDate(today);
            blogDateObject = today;
            httpServletResponse.addDateHeader(HTTP_LASTMODIFIED, today.getTime());
            // Generates an ETag header based on the string value of LastModified as an ISO8601 Format
            httpServletResponse.addHeader(HTTP_ETAG, "\"" + BlojsomUtils.digestString(blogISO8601Date) + "\"");
        }

        context.put(BLOJSOM_DATE, blogdate);
        context.put(BLOJSOM_DATE_ISO8601, blogISO8601Date);
        context.put(BLOJSOM_DATE_OBJECT, blogDateObject);
        context.put(BLOJSOM_DATE_UTC, blogUTCDate);

        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws BlojsomPluginException If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws BlojsomPluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws BlojsomPluginException If there is an error in finalizing this plugin
     */
    public void destroy() throws BlojsomPluginException {
    }
}
