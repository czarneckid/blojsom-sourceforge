/**
 * Copyright (c) 2003-2005, David A. Czarnecki
 * All rights reserved.
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
package org.blojsom.plugin.admin.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.BlogUser;
import org.blojsom.event.BlojsomEvent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

/**
 * ProcessRequestEvent
 *
 * @author David Czarnecki
 * @since blojsom 2.26
 * @version $Id: ProcessRequestEvent.java,v 1.2 2005-06-14 14:21:29 czarneckid Exp $
 */
public class ProcessRequestEvent extends BlojsomEvent {

    protected Log _logger = LogFactory.getLog(ProcessRequestEvent.class);

    protected HttpServletRequest _httpServletRequest;
    protected HttpServletResponse _httpServletResponse;
    protected Map _context;
    protected BlogUser _blogUser;

    /**
     * Create a new instance of the process event request
     *
     * @param source Event source
     * @param timestamp Timestamp of event
     * @param blogUser {@link BlogUser} information
     * @param httpServletRequest Servlet request
     * @param httpServletResponse Servlet response
     * @param context Context
     */
    public ProcessRequestEvent(Object source, Date timestamp, BlogUser blogUser,
                               HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                               Map context) {
        super(source, timestamp);
        _blogUser = blogUser;
        _httpServletRequest = httpServletRequest;
        _httpServletResponse = httpServletResponse;
        _context = context;
    }

    /**
     * Retrieve the servlet request
     *
     * @return {@link HttpServletRequest}
     */
    public HttpServletRequest getHttpServletRequest() {
        return _httpServletRequest;
    }

    /**
     * Retrieve the servlet response
     *
     * @return {@link HttpServletResponse}
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

    /**
     * Retrieve the {@link BlogUser} associated with the event
     *
     * @return {@link BlogUser}
     */
    public BlogUser getBlogUser() {
        return _blogUser;
    }
}