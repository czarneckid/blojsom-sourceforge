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
package org.blojsom.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

/**
 * Page Entries filter
 *
 * @author David Czarnecki
 * @version $Id: SkipEntriesFilter.java,v 1.3 2008-07-07 19:55:12 czarneckid Exp $
 * @since blojsom 3.0
 */
public class SkipEntriesFilter implements Filter {

    private static final Log LOGGER = LogFactory.getLog(SkipEntriesFilter.class);
    private static final String PAGE_WITH_NUMBER_REGEX = "/skip/(.+)/$";
    private static final Pattern PAGE_WITH_NUMBER_PATTERN = Pattern.compile(PAGE_WITH_NUMBER_REGEX, Pattern.UNICODE_CASE);

    /**
     * Construct a new instance of the page entries filter
     */
    public SkipEntriesFilter() {
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        servletRequest.setCharacterEncoding(BlojsomConstants.UTF8);

        HttpServletRequest hreq = (HttpServletRequest) servletRequest;
        String uri = hreq.getRequestURI();
        StringBuffer url = hreq.getRequestURL();
        String pathInfo = hreq.getPathInfo();
        if (BlojsomUtils.checkNullOrBlank(pathInfo)) {
            pathInfo = "/";
        }

        LOGGER.debug("Handling skip entries request: " + pathInfo);

        Matcher pageNumberMatcher = PAGE_WITH_NUMBER_PATTERN.matcher(pathInfo);
        Map extraParameters;

        if (pageNumberMatcher.find()) {
            String pageNumber = pageNumberMatcher.group(1);

            extraParameters = new HashMap();
            extraParameters.put(BlojsomConstants.PAGE_NUMBER_PARAM, new String[]{pageNumber});

            String pageNumberSubstring = "/skip/" + pageNumber + "/";
            int pageNumberIndex = pathInfo.lastIndexOf(pageNumberSubstring) + 1;
            String pathinfo = pathInfo.substring(0, pageNumberIndex);
            pageNumberIndex = uri.lastIndexOf(pageNumberSubstring);
            String URI = uri.substring(0, pageNumberIndex);
            pageNumberIndex = url.lastIndexOf(pageNumberSubstring);
            String URL = url.substring(0, pageNumberIndex);

            LOGGER.debug("Handling skip entries page: " + pageNumber + " with path info: " + pathinfo + " URI: " + URI + " URL: " + URL);
            hreq = new SkipEntriesPermalinkRequest(hreq, extraParameters, URI, URL, pathinfo);
        }

        filterChain.doFilter(hreq, servletResponse);
    }

    public void destroy() {
    }

    /**
     * Page number request
     */
    public class SkipEntriesPermalinkRequest extends HttpServletRequestWrapper {

        private Map params;
        private String uri;
        private String url;
        private String pathInfo;

        /**
         * @param httpServletRequest
         * @param params
         * @param uri
         * @param url
         * @param pathInfo
         */
        public SkipEntriesPermalinkRequest(HttpServletRequest httpServletRequest, Map params, String uri, String url, String pathInfo) {
            super(httpServletRequest);

            Map updatedParams = new HashMap(httpServletRequest.getParameterMap());
            Iterator keys = params.keySet().iterator();
            while (keys.hasNext()) {
                Object o = keys.next();
                updatedParams.put(o, params.get(o));
            }
            this.params = Collections.unmodifiableMap(updatedParams);
            this.uri = uri;
            this.url = url;
            this.pathInfo = pathInfo;
        }

        /**
         * Return the request URI
         *
         * @return Request URI
         */
        public String getRequestURI() {
            return uri;
        }

        /**
         * Return the request URL
         *
         * @return Request URL
         */
        public StringBuffer getRequestURL() {
            return new StringBuffer(url);
        }

        /**
         * Return the path information
         *
         * @return Path information
         */
        public String getPathInfo() {
            return pathInfo;
        }

        /**
         * Retrieve a named parameter
         *
         * @param name Parameter to retrieve
         * @return Parameter value or <code>null</code> if the parameter is not found
         */
        public String getParameter(String name) {
            String[] values = getParameterValues(name);
            return (values != null) ? values[0] : null;
        }

        /**
         * Retrieve the map of parameters
         *
         * @return Parameter map
         */
        public Map getParameterMap() {
            return params;
        }

        /**
         * Retrieve the parameter names
         *
         * @return {@link java.util.Enumeration} of parameter names
         */
        public Enumeration getParameterNames() {
            return Collections.enumeration(params.keySet());
        }

        /**
         * Retrieve a parameter value as a <code>String[]</code>
         *
         * @param name Parameter name
         * @return Parameter value as <code>String[]</code> or <code>null</code> if the parameter is not found
         */
        public String[] getParameterValues(String name) {
            return (String[]) params.get(name);
        }

        /**
         * Set the path information for the request
         *
         * @param pathInfo New path information
         */
        public void setPathInfo(String pathInfo) {
            this.pathInfo = pathInfo;
        }
    }
}
