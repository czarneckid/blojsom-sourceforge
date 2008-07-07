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
package org.blojsom.plugin.widget.event;

import org.blojsom.blog.Blog;
import org.blojsom.event.Event;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Date;

/**
 * ProcessWidgetRequest
 *
 * @author David Czarnecki
 * @version $Id: ProcessWidgetRequest.java,v 1.2 2008-07-07 19:54:26 czarneckid Exp $
 * @since blojsom 3.2
 */
public class ProcessWidgetRequest extends Event {

    protected String _widget;
    protected HttpServletRequest _request;
    protected HttpServletResponse _response;
    protected Blog _blog;
    protected Map _context;
    protected String _widgetTemplate;

    /**
     * ProcessWidgetRequest
     *
     * @param source         Source
     * @param timestamp      Timestamp
     * @param widget         Widget
     * @param request        {@link HttpServletRequest}
     * @param response       {@link HttpServletResponse}
     * @param blog           {@link Blog}
     * @param context        Context
     * @param widgetTemplate Initial template
     */
    public ProcessWidgetRequest(Object source, Date timestamp, String widget, HttpServletRequest request, HttpServletResponse response,
                                Blog blog, Map context, String widgetTemplate) {
        super(source, timestamp);

        _widget = widget;
        _request = request;
        _response = response;
        _blog = blog;
        _context = context;
        _widgetTemplate = widgetTemplate;
    }

    /**
     * Get the name of the widget
     *
     * @return Widget name
     */
    public String getWidget() {
        return _widget;
    }

    /**
     * Get the request
     *
     * @return {@link HttpServletRequest} request
     */
    public HttpServletRequest getRequest() {
        return _request;
    }

    /**
     * Get the response
     *
     * @return {@link HttpServletResponse} response
     */
    public HttpServletResponse getResponse() {
        return _response;
    }

    /**
     * Get the blog
     *
     * @return {@link Blog} information
     */
    public Blog getBlog() {
        return _blog;
    }

    /**
     * Get the context
     *
     * @return Context
     */
    public Map getContext() {
        return _context;
    }

    /**
     * Get the widget template
     *
     * @return Widget template
     */
    public String getWidgetTemplate() {
        return _widgetTemplate;
    }

    /**
     * Set the widget template
     *
     * @param widgetTemplate Widget template
     */
    public void setWidgetTemplate(String widgetTemplate) {
        _widgetTemplate = widgetTemplate;
    }
}
