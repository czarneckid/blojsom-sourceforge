/**
 * Copyright (c) 2003-2008, David A. Czarnecki
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
package org.blojsom.plugin.limiter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Comment;
import org.blojsom.blog.Entry;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * ConditionalGetPlugin
 *
 * @author David Czarnecki
 * @version $Id: ConditionalGetPlugin.java,v 1.4 2008-07-07 19:54:22 czarneckid Exp $
 * @since blojsom 3.0
 */
public class ConditionalGetPlugin implements Plugin {

    private Log _logger = LogFactory.getLog(ConditionalGetPlugin.class);

    private static final String IF_MODIFIED_SINCE_HEADER = "If-Modified-Since";
    private static final String IF_NONE_MATCH_HEADER = "If-None-Match";

    /**
     * Default constructor.
     */
    public ConditionalGetPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws PluginException If there is an error initializing the plugin
     */
    public void init() throws PluginException {
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param blog                {@link Blog} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws PluginException If there is an error processing the blog entries
     */
    public Entry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, Map context, Entry[] entries) throws PluginException {
        if (entries.length > 0) {
            long ifModifiedSinceHeader;
            try {
                ifModifiedSinceHeader = httpServletRequest.getDateHeader(IF_MODIFIED_SINCE_HEADER);
            } catch (Exception e) {
                ifModifiedSinceHeader = -1;
            }

            // Check first to see if neither of the headers we need to check are present
            if ((ifModifiedSinceHeader == -1) && (httpServletRequest.getHeader(IF_NONE_MATCH_HEADER) == null)) {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("No If-Modified-Since or If-None-Match HTTP headers present.");
                }
            } else {
                ArrayList datesToCheck = new ArrayList(5);
                Date[] dates;
                Comment[] comments;
                Comment comment;

                // If there are comments, add those to the dates to check, latest date first
                if (entries[0].getNumComments() > 0) {
                    comments = entries[0].getCommentsAsArray();
                    for (int i = comments.length - 1; i >= 0; i--) {
                        comment = comments[i];
                        datesToCheck.add(comment.getCommentDate());
                    }
                }

                // And add the date for the latest entry
                datesToCheck.add(entries[0].getDate());

                dates = (Date[]) datesToCheck.toArray(new Date[datesToCheck.size()]);

                // Cache the If-Modified-Since and If-None-Match headers since they will not change
                Date ifModifiedSinceDate = null;
                try {
                    ifModifiedSinceHeader = httpServletRequest.getDateHeader(IF_MODIFIED_SINCE_HEADER);
                } catch (Exception e) {
                    ifModifiedSinceHeader = -1;
                }

                if (ifModifiedSinceHeader != -1) {
                    ifModifiedSinceDate = new Date(ifModifiedSinceHeader);
                }

                String ifNoneMatchHeader = null;
                if (httpServletRequest.getHeader(IF_NONE_MATCH_HEADER) != null) {
                    ifNoneMatchHeader = httpServletRequest.getHeader(IF_NONE_MATCH_HEADER);
                }

                // Check the dates to see if anything matches
                Date latestEntryDate;
                for (int i = 0; i < dates.length; i++) {
                    latestEntryDate = dates[i];

                    if (ifModifiedSinceDate != null) {
                        if (latestEntryDate.toString().equals(ifModifiedSinceDate.toString())) {
                            if (_logger.isDebugEnabled()) {
                                _logger.debug("Returning 304 response based on If-Modified-Since header");
                            }
                            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                            break;
                        } else {
                            if (_logger.isDebugEnabled()) {
                                _logger.debug("Latest entry date/If-Modified-Since date: " + latestEntryDate.toString() + "/" + ifModifiedSinceDate.toString());
                            }
                        }
                    } else if (ifNoneMatchHeader != null) {
                        String calculatedIfNoneMatchHeader = "\"" + BlojsomUtils.digestString(BlojsomUtils.getISO8601Date(latestEntryDate)) + "\"";
                        if (ifNoneMatchHeader.equals(calculatedIfNoneMatchHeader)) {
                            if (_logger.isDebugEnabled()) {
                                _logger.debug("Returning 304 response based on If-None-Match header");
                            }
                            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                            break;
                        } else {
                            if (_logger.isDebugEnabled()) {
                                _logger.debug("Calculated ETag/If-None-Match ETag: " + calculatedIfNoneMatchHeader + "/" + ifNoneMatchHeader);
                            }
                        }
                    } else {
                        if (_logger.isDebugEnabled()) {
                            _logger.debug("No If-Modified-Since or If-None-Match HTTP headers present and not caught in initial check.");
                        }
                    }
                }
            }
        }

        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws PluginException If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws PluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws PluginException If there is an error in finalizing this plugin
     */
    public void destroy() throws PluginException {
    }
}
