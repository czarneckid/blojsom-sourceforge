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
package org.blojsom.plugin.admin.event;

import org.blojsom.blog.Blog;
import org.blojsom.event.Event;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

/**
 * AuthorizationEvent
 *
 * @author David Czarnecki
 * @version $Id: AuthorizationEvent.java,v 1.3 2008-07-07 19:54:11 czarneckid Exp $
 * @since blojsom 3.0
 */
public class AuthorizationEvent extends Event {

    protected HttpServletRequest _httpServletRequest;
    protected HttpServletResponse _httpServletResponse;
    protected Blog _blog;
    protected Map _context;
    protected Integer _blogUserID;

    /**
     * An event related to authorization for a given blog
     *
     * @param source              Source of the event
     * @param timestamp           Event timestamp
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param blog                {@link Blog}
     * @param context             Context for the given operation
     * @param blogUserID          Authorized user ID for the authorization event
     */
    public AuthorizationEvent(Object source, Date timestamp, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, Map context, Integer blogUserID) {
        super(source, timestamp);
        _httpServletRequest = httpServletRequest;
        _httpServletResponse = httpServletResponse;
        _blog = blog;
        _context = context;
        _blogUserID = blogUserID;
    }

    /**
     * Retrieve the request
     *
     * @return Request
     */
    public HttpServletRequest getHttpServletRequest() {
        return _httpServletRequest;
    }

    /**
     * Retrieve the response
     *
     * @return Response
     */
    public HttpServletResponse getHttpServletResponse() {
        return _httpServletResponse;
    }

    /**
     * Retrieve the {@link Blog}
     *
     * @return {@link Blog}
     */
    public Blog getBlog() {
        return _blog;
    }

    /**
     * Retrieve the context
     *
     * @return Context
     */
    public Map getContext() {
        return _context;
    }

    /**
     * Retrieve the authorized user ID
     *
     * @return Authorized user ID
     */
    public Integer getBlogUserID() {
        return _blogUserID;
    }
}