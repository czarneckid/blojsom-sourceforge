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
package org.blojsom.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Category;
import org.blojsom.blog.Comment;
import org.blojsom.blog.Entry;
import org.blojsom.dispatcher.Dispatcher;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * BlojsomServlet
 *
 * @author David Czarnecki
 * @version $Id: BlojsomServlet.java,v 1.9 2006-08-16 15:25:17 czarneckid Exp $
 * @since blojsom 3.0
 */
public class BlojsomServlet extends HttpServlet {

    protected Log _logger = LogFactory.getLog(BlojsomServlet.class);

    protected String[] BLOJSOM_CONFIGURATION_FILES = {"blojsom.xml"};
    protected ClassPathXmlApplicationContext _classPathXmlApplicationContext;

    /**
     * Initialize blojsom
     *
     * @param servletConfig {@link ServletConfig}
     * @throws ServletException If there is an error initializing blojsom
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        ServletConfigFactoryBean.setServletConfig(servletConfig);

        try {
            _classPathXmlApplicationContext = new ClassPathXmlApplicationContext(BLOJSOM_CONFIGURATION_FILES);
        } catch (BeansException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new ServletException(e);
        }

        servletConfig.getServletContext().setAttribute(BlojsomConstants.BLOJSOM_APPLICATION_CONTEXT, _classPathXmlApplicationContext);

        if (_logger.isDebugEnabled()) {
            _logger.debug("blojsom: All Your Blog Are Belong To Us");
        }
    }

    /**
     * Handle requests made to blojsom
     *
     * @param httpServletRequest  {@link HttpServletRequest} request
     * @param httpServletResponse {@link HttpServletResponse} response
     * @throws ServletException If there is an error serving the request
     * @throws IOException      If there is an error serving the request
     */
    protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        try {
            httpServletRequest.setCharacterEncoding(BlojsomConstants.UTF8);
        } catch (UnsupportedEncodingException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }
        }

        // Make sure that we have a request URI ending with a / otherwise we need to
        // redirect so that the browser can handle relative link generation
        if (!httpServletRequest.getRequestURI().endsWith("/")) {
            StringBuffer redirectURL = new StringBuffer();
            redirectURL.append(httpServletRequest.getRequestURI());
            redirectURL.append("/");
            if (httpServletRequest.getParameterMap().size() > 0) {
                redirectURL.append("?");
                redirectURL.append(BlojsomUtils.convertRequestParams(httpServletRequest));
            }

            if (_logger.isDebugEnabled()) {
                _logger.debug("Redirecting the user to: " + redirectURL.toString());
            }

            httpServletResponse.sendRedirect(redirectURL.toString());

            return;
        }

        Properties blojsomDefaultProperties = (Properties) _classPathXmlApplicationContext.getBean("defaultProperties");

        // Check for an overriding id
        String blogId = httpServletRequest.getParameter(BlojsomConstants.BLOG_ID_PARAM);
        if (BlojsomUtils.checkNullOrBlank(blogId)) {
            String blogIdFromPath = BlojsomUtils.getBlogFromPath(httpServletRequest.getPathInfo());
            if (blogIdFromPath == null) {
                blogId = blojsomDefaultProperties.getProperty(BlojsomConstants.DEFAULT_BLOG_IP);
            } else {
                blogId = blogIdFromPath;
            }
        }

        if (BlojsomUtils.checkNullOrBlank(blogId)) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Blog ID not specified");

            return;
        }

        Fetcher fetcher = (Fetcher) _classPathXmlApplicationContext.getBean("fetcher");

        Blog blog;
        try {
            blog = fetcher.loadBlog(blogId);
        } catch (FetcherException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            // Try and use the default blog ID if a blog ID is specified and unable to load the blog
            String defaultBlogId = blojsomDefaultProperties.getProperty(BlojsomConstants.DEFAULT_BLOG_IP);
            if (BlojsomUtils.checkNullOrBlank(defaultBlogId)) {
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Unable to load blog ID: " + blogId);

                return;
            } else {
                try {
                    blog = fetcher.loadBlog(defaultBlogId);
                } catch (FetcherException e1) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }

                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Unable to load blog ID: " + defaultBlogId);

                    return;
                }
            }
        }

        if ("true".equals(blog.getProperty(BlojsomConstants.USE_DYNAMIC_BLOG_URLS))) {
            BlojsomUtils.resolveDynamicBaseAndBlogURL(httpServletRequest, blog, blogId);
        }

        // Determine the requested flavor
        String flavor = httpServletRequest.getParameter(BlojsomConstants.FLAVOR_PARAM);
        if (BlojsomUtils.checkNullOrBlank(flavor)) {
            flavor = blog.getProperty(BlojsomConstants.BLOG_DEFAULT_FLAVOR_IP);
            if (blog.getTemplates().get(flavor) == null) {
                flavor = BlojsomConstants.DEFAULT_FLAVOR_HTML;
            }
        } else {
            if (blog.getTemplates().get(flavor) == null) {
                flavor = blog.getProperty(BlojsomConstants.BLOG_DEFAULT_FLAVOR_IP);
                if (blog.getTemplates().get(flavor) == null) {
                    flavor = BlojsomConstants.DEFAULT_FLAVOR_HTML;
                }
            }
        }

        // Setup the initial context for the fetcher, plugins, and finally the dispatcher
        HashMap context = new HashMap();

        // Setup the resource manager in the context
        context.put(BlojsomConstants.RESOURCE_MANAGER_CONTEXT_KEY, _classPathXmlApplicationContext.getBean("resourceManager"));
        context.put(BlojsomConstants.BLOJSOM_REQUESTED_FLAVOR, flavor);

        Entry[] entries = null;
        Category[] categories = null;

        try {
            categories = fetcher.fetchCategories(httpServletRequest, httpServletResponse, blog, flavor, context);
            entries = fetcher.fetchEntries(httpServletRequest, httpServletResponse, blog, flavor, context);
        } catch (FetcherException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }
        }

        String[] pluginChain;
        Map plugins = blog.getPlugins();

        // Check to see if the user would like to override the plugin chain
        if (httpServletRequest.getParameter(BlojsomConstants.PLUGINS_PARAM) != null) {
            pluginChain = BlojsomUtils.parseCommaList(httpServletRequest.getParameter(BlojsomConstants.PLUGINS_PARAM));
        } else {
            if (plugins.containsKey(flavor) && !BlojsomUtils.checkNullOrBlank(((String) plugins.get(flavor)).trim())) {
                pluginChain = BlojsomUtils.parseOnlyCommaList((String) plugins.get(flavor), true);
            } else {
                pluginChain = BlojsomUtils.parseOnlyCommaList((String) plugins.get("default"), true);
            }
        }

        // Invoke the plugins in the order in which they were specified
        if ((entries != null) && (pluginChain != null) && (pluginChain.length > 0)) {
            for (int i = 0; i < pluginChain.length; i++) {
                String plugin = pluginChain[i];
                try {
                    Plugin pluginToExecute = (Plugin) _classPathXmlApplicationContext.getBean(plugin);
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("blojsom plugin execution: " + pluginToExecute.getClass().getName());
                    }
                    try {
                        entries = pluginToExecute.process(httpServletRequest, httpServletResponse, blog, context, entries);
                        pluginToExecute.cleanup();
                    } catch (PluginException e) {
                        if (_logger.isErrorEnabled()) {
                            _logger.error(e);
                        }
                    }
                } catch (BeansException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error("Plugin not available: " + plugin);
                    }
                }
            }
        } else {
            if (_logger.isDebugEnabled()) {
                _logger.debug("No entries available for plugins to process or no plugins specified for flavor");
            }
        }

        String blogdate = null;
        String blogISO8601Date = null;
        String blogUTCDate = null;
        Date blogDateObject = null;

        boolean sendLastModified = true;
        if (httpServletRequest.getParameter(BlojsomConstants.OVERRIDE_LASTMODIFIED_PARAM) != null) {
            sendLastModified = Boolean.getBoolean(httpServletRequest.getParameter(BlojsomConstants.OVERRIDE_LASTMODIFIED_PARAM));
        }

        // If we have entries, construct a last modified on the most recent entry
        // Additionally, set the blog date
        if (sendLastModified) {
            if ((entries != null) && (entries.length > 0)) {
                Entry _entry = entries[0];
                long _lastmodified;

                if (_entry.getNumComments() > 0) {
                    Comment _comment = _entry.getCommentsAsArray()[_entry.getNumComments() - 1];
                    _lastmodified = _comment.getCommentDate().getTime();
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Adding last-modified header for most recent entry comment");
                    }
                } else {
                    _lastmodified = _entry.getDate().getTime();
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Adding last-modified header for most recent blog entry");
                    }
                }

                // Check for the Last-Modified object from one of the plugins
                if (context.containsKey(BlojsomConstants.BLOJSOM_LAST_MODIFIED)) {
                    Long lastModified = (Long) context.get(BlojsomConstants.BLOJSOM_LAST_MODIFIED);
                    if (lastModified.longValue() > _lastmodified) {
                        _lastmodified = lastModified.longValue();
                    }
                }

                // Generates an ETag header based on the string value of LastModified as an ISO8601 Format
                String etagLastModified = BlojsomUtils.getISO8601Date(new Date(_lastmodified));
                httpServletResponse.addHeader(BlojsomConstants.HTTP_ETAG, "\"" + BlojsomUtils.digestString(etagLastModified) + "\"");

                httpServletResponse.addDateHeader(BlojsomConstants.HTTP_LASTMODIFIED, _lastmodified);
                blogdate = entries[0].getRFC822Date();
                blogISO8601Date = entries[0].getISO8601Date();
                blogDateObject = entries[0].getDate();
                blogUTCDate = BlojsomUtils.getUTCDate(entries[0].getDate());
            } else {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Adding last-modified header for current date");
                }

                Date today = new Date();
                blogdate = BlojsomUtils.getRFC822Date(today);
                blogISO8601Date = BlojsomUtils.getISO8601Date(today);
                blogUTCDate = BlojsomUtils.getUTCDate(today);
                blogDateObject = today;
                httpServletResponse.addDateHeader(BlojsomConstants.HTTP_LASTMODIFIED, today.getTime());
                // Generates an ETag header based on the string value of LastModified as an ISO8601 Format
                httpServletResponse.addHeader(BlojsomConstants.HTTP_ETAG, "\"" + BlojsomUtils.digestString(blogISO8601Date) + "\"");
            }
        }

        context.put(BlojsomConstants.BLOJSOM_DATE, blogdate);
        context.put(BlojsomConstants.BLOJSOM_DATE_ISO8601, blogISO8601Date);
        context.put(BlojsomConstants.BLOJSOM_DATE_OBJECT, blogDateObject);
        context.put(BlojsomConstants.BLOJSOM_DATE_UTC, blogUTCDate);

        // Finish setting up the context for the dispatcher
        context.put(BlojsomConstants.BLOJSOM_BLOG, blog);
        context.put(BlojsomConstants.BLOJSOM_ENTRIES, entries);
        context.put(BlojsomConstants.BLOJSOM_CATEGORIES, categories);
        context.put(BlojsomConstants.BLOJSOM_VERSION, BlojsomConstants.BLOJSOM_VERSION_NUMBER);
        context.put(BlojsomConstants.BLOJSOM_BLOG_ID, blog.getBlogId());
        context.put(BlojsomConstants.BLOJSOM_SITE_URL, blog.getBlogBaseURL());

        context.put(BlojsomConstants.BLOJSOM_BLOG_ID, blog.getBlogId());

        String templateAndType = (String) blog.getTemplates().get(flavor);
        String[] templateData = BlojsomUtils.parseOnlyCommaList(templateAndType, true);
        String templateExtension = BlojsomUtils.getFileExtension(templateData[0]);

        Dispatcher dispatcher = null;
        try {
            dispatcher = (org.blojsom.dispatcher.Dispatcher) _classPathXmlApplicationContext.getBean(templateExtension);
        } catch (BeansException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to retrieve dispatcher for template extension: " + templateExtension);
        }
        dispatcher.dispatch(httpServletRequest, httpServletResponse, blog, context, templateData[0], templateData[1]);
    }

    /**
     * Take blojsom out of service
     */
    public void destroy() {
        super.destroy();

        _classPathXmlApplicationContext.destroy();

        if (_logger.isDebugEnabled()) {
            _logger.debug("blojsom destroyed");
        }
    }
}
