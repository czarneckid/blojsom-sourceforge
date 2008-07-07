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
package org.blojsom.filter;

import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.*;

/**
 * FeedFilter
 *
 * @author David Czarnecki
 * @version $Id: FeedFilter.java,v 1.3 2008-07-07 19:55:12 czarneckid Exp $
 * @since blojsom 3.0
 */
public class FeedFilter implements Filter {

    private static final Log _logger = LogFactory.getLog(FeedFilter.class);

    private static final String FEED_WITH_TYPE_REGEX = "/feed/(.+)/$";
    private static final Pattern FEED_WITH_TYPE_PATTERN = Pattern.compile(FEED_WITH_TYPE_REGEX, Pattern.UNICODE_CASE);
    private static final String FEED_NO_TYPE_REGEX = "/feed/$";
    private static final Pattern FEED_NO_TYPE_PATTERN = Pattern.compile(FEED_NO_TYPE_REGEX, Pattern.UNICODE_CASE);
    private static final String DEFAULT_FEED_TYPE = "rss2";
    private static final String DEFAULT_FEED_TYPE_IP = "default-feed-type";
    private static final String PERMALINK_EXTENSIONS_IP = "permalink-extensions";
    private static final String[] DEFAULT_PERMALINK_EXTENSIONS = {".html", ".txt"};

    private String _defaultFeedType = DEFAULT_FEED_TYPE;
    private String[] _permalinkExtensions;

    /**
     * Construct a new instance of the Feed filter
     */
    public FeedFilter() {
    }

    /**
     * Initialize the filter
     *
     * @param filterConfig {@link FilterConfig}
     * @throws ServletException If there is an error initializing the filter
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        String defaultFeedType = filterConfig.getInitParameter(DEFAULT_FEED_TYPE_IP);
        if (!BlojsomUtils.checkNullOrBlank(defaultFeedType)) {
            _defaultFeedType = defaultFeedType;
        }

        _permalinkExtensions = DEFAULT_PERMALINK_EXTENSIONS;
        String permalinkExtensions = filterConfig.getInitParameter(PERMALINK_EXTENSIONS_IP);
        if (!BlojsomUtils.checkNullOrBlank(permalinkExtensions)) {
            _permalinkExtensions = BlojsomUtils.parseOnlyCommaList(permalinkExtensions, true);
        }

        _logger.debug("Initialized feed filter using default feed type: " + _defaultFeedType);
    }

    /**
     * Remove the filter from service
     */
    public void destroy() {
    }

    /**
     * Process the request.
     * <p/>
     * Processes requests of the form
     * <ul>
     * </ul>
     *
     * @param request  {@link ServletRequest}
     * @param response {@link ServletResponse}
     * @param chain    {@link FilterChain} to execute
     * @throws IOException      If there is an error executing the filter
     * @throws ServletException If there is an error executing the filter
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        request.setCharacterEncoding(BlojsomConstants.UTF8);

        HttpServletRequest hreq = (HttpServletRequest) request;
        String uri = hreq.getRequestURI();
        StringBuffer url = hreq.getRequestURL();
        String pathInfo = hreq.getPathInfo();
        if (BlojsomUtils.checkNullOrBlank(pathInfo)) {
            pathInfo = "/";
        }

        _logger.debug("Handling feed filter request: " + pathInfo);

        Matcher feedWithTypeMatcher = FEED_WITH_TYPE_PATTERN.matcher(pathInfo);
        Matcher feedNoTypeMatcher = FEED_NO_TYPE_PATTERN.matcher(pathInfo);
        Map extraParameters;

        if (feedWithTypeMatcher.find()) {
            String feedType = feedWithTypeMatcher.group(1);

            extraParameters = new HashMap();
            extraParameters.put("flavor", new String[]{feedType});

            String feedTypeSubstring = "/feed/" + feedType + "/";
            int feedTypeIndex = pathInfo.lastIndexOf(feedTypeSubstring) + 1;
            String pathinfo = pathInfo.substring(0, feedTypeIndex);
            boolean matchedPermalink = false;
            for (int i = 0; i < _permalinkExtensions.length; i++) {
                String defaultPermalinkExtension = _permalinkExtensions[i];

                if (pathinfo.endsWith(defaultPermalinkExtension + "/")) {
                    matchedPermalink = true;
                    break;
                }
            }

            feedTypeIndex = uri.lastIndexOf(feedTypeSubstring);
            String URI = uri.substring(0, feedTypeIndex);
            feedTypeIndex = url.lastIndexOf(feedTypeSubstring);
            String URL = url.substring(0, feedTypeIndex);

            if (matchedPermalink) {
                pathinfo = pathinfo.substring(0, pathinfo.length() - 1);
            } else {
                URI += "/";
                URL += "/";
            }

            _logger.debug("Handling feed type: " + feedType + " with path info: " + pathinfo + " URI: " + URI + " URL: " + URL);
            hreq = new FeedPermalinkRequst(hreq, extraParameters, URI, URL, pathinfo);
        } else if (feedNoTypeMatcher.find()) {
            extraParameters = new HashMap();
            extraParameters.put("flavor", new String[]{_defaultFeedType});

            String feedTypeSubstring = "/feed/";
            int feedTypeIndex = pathInfo.lastIndexOf(feedTypeSubstring) + 1;
            String pathinfo = pathInfo.substring(0, feedTypeIndex);
            boolean matchedPermalink = false;
            for (int i = 0; i < DEFAULT_PERMALINK_EXTENSIONS.length; i++) {
                String defaultPermalinkExtension = DEFAULT_PERMALINK_EXTENSIONS[i];

                if (pathinfo.endsWith(defaultPermalinkExtension + "/")) {
                    matchedPermalink = true;
                    break;
                }
            }

            feedTypeIndex = uri.lastIndexOf(feedTypeSubstring);
            String URI = uri.substring(0, feedTypeIndex);
            feedTypeIndex = url.lastIndexOf(feedTypeSubstring);
            String URL = url.substring(0, feedTypeIndex);

            if (matchedPermalink) {
                pathinfo = pathinfo.substring(0, pathinfo.length() - 1);
            } else {
                URI += "/";
                URL += "/";
            }

            _logger.debug("Handling default feed type: " + _defaultFeedType + " with path info: " + pathinfo + " URI: " + URI + " URL: " + URL);
            hreq = new FeedPermalinkRequst(hreq, extraParameters, URI, URL, pathinfo);
        }

        chain.doFilter(hreq, response);
    }

    /**
     * Feed request
     */
    public class FeedPermalinkRequst extends HttpServletRequestWrapper {

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
        public FeedPermalinkRequst(HttpServletRequest httpServletRequest, Map params, String uri, String url, String pathInfo) {
            super(httpServletRequest);

            Map updatedParams = new HashMap(httpServletRequest.getParameterMap());
            Iterator keys = params.keySet().iterator();
            while (keys.hasNext())

            {
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
