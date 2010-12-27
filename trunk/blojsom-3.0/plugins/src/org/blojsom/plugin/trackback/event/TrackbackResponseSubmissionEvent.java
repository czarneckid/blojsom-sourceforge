/**
 * Copyright (c) 2003-2009, David A. Czarnecki
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
package org.blojsom.plugin.trackback.event;

import org.blojsom.plugin.response.event.ResponseSubmissionEvent;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

/**
 * Trackback response submission event
 *
 * @author David Czarnecki
 * @version $Id: TrackbackResponseSubmissionEvent.java,v 1.3 2008-07-07 19:54:16 czarneckid Exp $
 * @since blojsom 3.0
 */
public class TrackbackResponseSubmissionEvent extends ResponseSubmissionEvent {

    /**
     * Create a new instance of the trackback response submission event
     *
     * @param source              Source of event
     * @param timestamp           Time of event
     * @param blog                {@link Blog}
     * @param httpServletRequest  {@link HttpServletRequest}
     * @param httpServletResponse {@link HttpServletResponse}
     * @param submitter           Submitter (blogName)
     * @param submitterItem1      Submitter data item 1 (title)
     * @param submitterItem2      Submitter data item 2 (URL)
     * @param content             Trackback excerpt
     * @param entry               {@link Entry}
     * @param metaData            Trackback meta-data
     */
    public TrackbackResponseSubmissionEvent(Object source, Date timestamp, Blog blog, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String submitter, String submitterItem1, String submitterItem2, String content, Entry entry, Map metaData) {
        super(source, timestamp, blog, httpServletRequest, httpServletResponse, submitter, submitterItem1, submitterItem2, content, entry, metaData);
    }
}