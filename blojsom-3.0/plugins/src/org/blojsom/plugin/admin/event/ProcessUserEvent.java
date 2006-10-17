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

import org.blojsom.blog.Blog;
import org.blojsom.blog.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

/**
 * Process user event contains information about a user with hooks for retrieving the servlet request,
 * response, and the current plugin execution context.
 *
 * @author David Czarnecki
 * @since blojsom 3.1
 * @version $Id: ProcessUserEvent.java,v 1.1 2006-10-17 16:20:37 czarneckid Exp $
 */
public class ProcessUserEvent extends UserEvent {

    protected HttpServletRequest _httpServletRequest;
    protected HttpServletResponse _httpServletResponse;
    protected Map _context;

    /**
     * Create a new event indicating something happened with a {@link User} in the system.
     *
     * @param source              Source of the event
     * @param timestamp           Event timestamp
     * @param user                {@link User}
     * @param blog                {@link Blog}
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param context             Context
     */
    public ProcessUserEvent(Object source, Date timestamp, User user, Blog blog, HttpServletRequest httpServletRequest,
                            HttpServletResponse httpServletResponse, Map context) {
        super(source, timestamp, user, blog);

        _httpServletRequest = httpServletRequest;
        _httpServletResponse = httpServletResponse;
        _context = context;
    }

    /**
     * Retrieve the servlet request
     *
     * @return {@link HttpServletRequest} Request
     */
    public HttpServletRequest getHttpServletRequest() {
        return _httpServletRequest;
    }

    /**
     * Retrieve the servlet response
     *
     * @return {@link HttpServletResponse} Response
     */
    public HttpServletResponse getHttpServletResponse() {
        return _httpServletResponse;
    }

    /**
     * Retrieve the plugin execution context
     *
     * @return Context map
     */
    public Map getContext() {
        return _context;
    }
}
