/**
 * Copyright (c) 2003-2007, David A. Czarnecki
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
package org.blojsom.plugin.pingback.event;

import org.blojsom.plugin.response.event.ResponseSubmissionEvent;
import org.blojsom.blog.Entry;
import org.blojsom.blog.Blog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

/**
 * Pingback response submission event
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: PingbackResponseSubmissionEvent.java,v 1.2 2007-01-17 02:35:13 czarneckid Exp $
 */
public class PingbackResponseSubmissionEvent extends ResponseSubmissionEvent {

    /**
     * Create a new instance of the pingback response submission event
     *
     * @param source              Source of event
     * @param timestamp           Time of event
     * @param blog                {@link Blog}
     * @param httpServletRequest  {@link HttpServletRequest}
     * @param httpServletResponse {@link HttpServletResponse}
     * @param submitter           Submitter (Blog name)
     * @param submitterItem1      Submitter data item 1 (Title)
     * @param submitterItem2      Submitter data item 2 (Source URI)
     * @param content             Pingback excerpt
     * @param entry               {@link Entry}
     * @param metaData            Pingback meta-data
     */
    public PingbackResponseSubmissionEvent(Object source, Date timestamp, Blog blog, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String submitter, String submitterItem1, String submitterItem2, String content, Entry entry, Map metaData) {
        super(source, timestamp, blog, httpServletRequest, httpServletResponse, submitter, submitterItem1, submitterItem2, content, entry, metaData);
    }
}
