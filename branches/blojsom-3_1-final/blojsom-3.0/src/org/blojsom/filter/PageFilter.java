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
 * Page filter supports URLs of the form <code>/[blojsom context]/[blojsom servlet mapping]/[blog ID]/[page name]/page/</code>. For example,
 * <code>/blojsom/blog/default/about/page/</code> would try and pull the <code>about.vm</code> template from the <code>default</code> blog's
 * <code>templates</code> directory.
 * <p></p>
 * Usage:<br/>
 * <pre>
 * <filter>
 *     <filter-name>PageFilter</filter-name>
 *     <filter-class>org.blojsom.filter.PageFilter</filter-class>
 *     <init-param>
 *         <param-name>use-root-blog-compatability</param-name>
 *         <param-value>false</param-value>
 *     </init-param>
 * </filter>
 *
 * <filter-mapping>
 *     <filter-name>PageFilter</filter-name></pre>
 *     <servlet-name>blojsom</servlet-name>
 * </filter-mapping>
 * </pre>
 *
 * For the page filter to be used, it must be mapped before the permalink filter.
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version PageFilter.java,v 1.1 2005/12/19 20:04:29 czarneckid Exp
 */
public class PageFilter implements Filter {

    private static final Log _logger = LogFactory.getLog(PageFilter.class);
    private static final String PAGE_PATHINFO = "/page/";
    private static final String PAGE_REGEX = PAGE_PATHINFO + "$";
    private static final Pattern PAGE_PATTERN = Pattern.compile(PAGE_REGEX, Pattern.UNICODE_CASE);
    private static final String USE_ROOT_BLOG_COMPATABILITY_IP = "use-root-blog-compatability";
    private static final boolean USE_ROOT_BLOG_COMPATABILITY_DEFAULT = false;

    private boolean _useRootBlogCompatability = USE_ROOT_BLOG_COMPATABILITY_DEFAULT;

    /**
     * Construct a new instance of the Feed filter
     */
    public PageFilter() {
    }

    /**
     * Initialize the filter
     *
     * @param filterConfig {@link FilterConfig}
     * @throws ServletException If there is an error initializing the filter
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        _useRootBlogCompatability = Boolean.valueOf(filterConfig.getInitParameter(USE_ROOT_BLOG_COMPATABILITY_IP)).booleanValue();

        _logger.debug("Initialized page filter (Root blog compatability: " + _useRootBlogCompatability + ")");
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

        _logger.debug("Handling page filter request: " + pathInfo);

        Matcher pageMatcher = PAGE_PATTERN.matcher(pathInfo);
        Map extraParameters;

        if (pageMatcher.find()) {
            int pageMatchIndex = pageMatcher.start();
            String blogIDPathInfo = null;

            if (!_useRootBlogCompatability) {
                int firstSlashAfterBlogID = pathInfo.substring(1).indexOf("/");
                blogIDPathInfo = pathInfo.substring(0, firstSlashAfterBlogID + 1) + "/";
                pathInfo = pathInfo.substring(firstSlashAfterBlogID + 1, pageMatchIndex);
            } else {
                pathInfo = pathInfo.substring(0, pageMatchIndex);
            }

            pageMatchIndex = uri.lastIndexOf(PAGE_PATHINFO);
            String URI = uri.substring(0, pageMatchIndex) + "/";
            pageMatchIndex = url.lastIndexOf(PAGE_PATHINFO);
            String URL = url.substring(0, pageMatchIndex) + "/";

            extraParameters = new HashMap();
            extraParameters.put("page", new String[]{pathInfo});
            pathInfo = "/";

            if (!_useRootBlogCompatability) {
                pathInfo = blogIDPathInfo;
            }

            hreq = new PagePermalinkRequst(hreq, extraParameters, URI, URL, pathInfo);
            _logger.debug("Handling pathinfo: " + pathInfo + " uri: " + URI + " url: " + URL);
        }

        chain.doFilter(hreq, response);
    }

    /**
     * Page request
     */
    public class PagePermalinkRequst extends HttpServletRequestWrapper {

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
        public PagePermalinkRequst(HttpServletRequest httpServletRequest, Map params, String uri, String url, String pathInfo) {
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
