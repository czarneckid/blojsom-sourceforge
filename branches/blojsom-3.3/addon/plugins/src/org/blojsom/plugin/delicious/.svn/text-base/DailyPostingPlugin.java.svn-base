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
package org.blojsom.plugin.delicious;

import del.icio.us.Delicious;
import del.icio.us.DeliciousUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.event.EntryAddedEvent;
import org.blojsom.plugin.velocity.StandaloneVelocityPlugin;
import org.blojsom.util.BlojsomMetaDataConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.*;

/**
 * Plugin to post links from your <a href="http://del.icio.us">del.icio.us</a> account to your blog.
 *
 * @author David Czarnecki
 * @version $Id: DailyPostingPlugin.java,v 1.4 2008-07-07 19:54:10 czarneckid Exp $
 * @since blojsom 3.0
 */
public class DailyPostingPlugin extends StandaloneVelocityPlugin {

    private Log _logger = LogFactory.getLog(DailyPostingPlugin.class);

    // Plugin configuration parameters
    private static final String DAILY_POSTING_POLL_TIME_IP = "daily-posting-poll-time";
    private static final int DAILY_POSTING_POLL_TIME_DEFAULT = (1000 * 60 * 60);
    private int _pollTime = DAILY_POSTING_POLL_TIME_DEFAULT;

    // Template
    private static final String DAILY_POSTING_TEMPLATE = "org/blojsom/plugin/delicious/daily-posting-template.vm";

    // Context variables
    private static final String DAILY_POSTING_USERNAME = "DAILY_POSTING_USERNAME";
    private static final String DAILY_POSTING_POSTS = "DAILY_POSTING_POSTS";

    // Individual configuration parameters
    private static final String DAILY_POSTING_USERNAME_IP = "daily-posting-username";
    private static final String DAILY_POSTING_PASSWORD_IP = "daily-posting-password";
    private static final String DAILY_POSTING_CATEGORY_IP = "daily-posting-category";
    private static final String DAILY_POSTING_HOUR_IP = "daily-posting-hour";
    private static final String DAILY_POSTING_TITLE_IP = "daily-posting-title";
    private static final String DAILY_POSTING_AUTHOR_IP = "daily-posting-author";
    private static final String DAILY_POSTING_TITLE_DEFAULT = "del.icio.us links for {0}";

    private boolean _finished = false;
    private DeliciousChecker _checker;

    private String _proxyHost = null;
    private String _proxyPort = null;

    private Fetcher _fetcher;
    private ServletConfig _servletConfig;
    private EventBroadcaster _eventBroadcaster;

    /**
     * Create a new instance of the daily posting plugin
     */
    public DailyPostingPlugin() {
    }

    /**
     * Set the {@link Fetcher}
     *
     * @param fetcher {@link Fetcher}
     */
    public void setFetcher(Fetcher fetcher) {
        _fetcher = fetcher;
    }

    /**
     * Set the {@link ServletConfig} for the fetcher to grab initialization parameters
     *
     * @param servletConfig {@link ServletConfig}
     */
    public void setServletConfig(ServletConfig servletConfig) {
        _servletConfig = servletConfig;
    }

    /**
     * Set the {@link EventBroadcaster}
     *
     * @param eventBroadcaster {@link EventBroadcaster}
     */
    public void setEventBroadcaster(EventBroadcaster eventBroadcaster) {
        _eventBroadcaster = eventBroadcaster;
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error initializing the plugin
     */
    public void init() throws PluginException {
        super.init();

        String pollTime = _servletConfig.getInitParameter(DAILY_POSTING_POLL_TIME_IP);
        if (BlojsomUtils.checkNullOrBlank(pollTime)) {
            _pollTime = DAILY_POSTING_POLL_TIME_DEFAULT;
        } else {
            try {
                _pollTime = Integer.parseInt(pollTime);
                if (_pollTime < DAILY_POSTING_POLL_TIME_DEFAULT) {
                    _pollTime = DAILY_POSTING_POLL_TIME_DEFAULT;
                }
            } catch (NumberFormatException e) {
                _pollTime = DAILY_POSTING_POLL_TIME_DEFAULT;
            }
        }

        try {
            _proxyHost = System.getProperty("http.proxyHost");
            _proxyPort = System.getProperty("http.proxyPort");
        } catch (Exception e) {
            _logger.error(e);
        }

        _checker = new DeliciousChecker();
        _checker.setDaemon(true);
        _checker.start();
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param blog                {@link Blog} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws PluginException If there is an error processing the blog entries
     */
    public Entry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, Map context, Entry[] entries) throws PluginException {
        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws PluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error in finalizing this plugin
     */
    public void destroy() throws PluginException {
        _finished = true;
    }

    /**
     * Checker thread for posting to del.icio.us
     */
    private class DeliciousChecker extends Thread {

        /**
         * Allocates a new <code>Thread</code> object. This constructor has
         * the same effect as <code>Thread(null, null,</code>
         * <i>gname</i><code>)</code>, where <b><i>gname</i></b> is
         * a newly generated name. Automatically generated names are of the
         * form <code>"Thread-"+</code><i>n</i>, where <i>n</i> is an integer.
         *
         * @see Thread#Thread(ThreadGroup,
         *      Runnable, String)
         */
        public DeliciousChecker() {
            super();
        }

        /**
         * If this thread was constructed using a separate
         * <code>Runnable</code> run object, then that
         * <code>Runnable</code> object's <code>run</code> method is called;
         * otherwise, this method does nothing and returns.
         * <p/>
         * Subclasses of <code>Thread</code> should override this method.
         *
         * @see Thread#start()
         * @see Thread#stop()
         * @see Thread#Thread(ThreadGroup,
         *      Runnable, String)
         * @see Runnable#run()
         */
        public void run() {
            try {
                while (!_finished) {
                    String[] blogIDs = _fetcher.loadBlogIDs();

                    Blog blog;
                    String blogID;

                    for (int i = 0; i < blogIDs.length; i++) {
                        blogID = blogIDs[i];

                        try {
                            blog = _fetcher.loadBlog(blogID);

                            String postingCategory = blog.getProperty(DAILY_POSTING_CATEGORY_IP);
                            String deliciousUsername = blog.getProperty(DAILY_POSTING_USERNAME_IP);
                            String deliciousPassword = blog.getProperty(DAILY_POSTING_PASSWORD_IP);
                            String postingHour = blog.getProperty(DAILY_POSTING_HOUR_IP);
                            String postTitle = blog.getProperty(DAILY_POSTING_TITLE_IP);
                            String postingAuthor = blog.getProperty(DAILY_POSTING_AUTHOR_IP);

                            if (BlojsomUtils.checkNullOrBlank(postTitle)) {
                                postTitle = DAILY_POSTING_TITLE_DEFAULT;
                            }

                            if (BlojsomUtils.checkNullOrBlank(postingCategory) ||
                                BlojsomUtils.checkNullOrBlank(deliciousPassword) ||
                                BlojsomUtils.checkNullOrBlank(deliciousUsername) ||
                                BlojsomUtils.checkNullOrBlank(postingHour)) {
                            } else {
                                Date now = new Date();
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(now);
                                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

                                try {
                                    int hourToPost = Integer.parseInt(postingHour);
                                    if (hourToPost == currentHour) {
                                        Delicious delicious = new Delicious(deliciousUsername, deliciousPassword);
                                        if (_proxyHost != null && _proxyPort != null) {
                                            delicious.setProxyConfiguration(_proxyHost, Integer.parseInt(_proxyPort));
                                        }

                                        List posts = delicious.getPostsForDate(null, now);
                                        if (posts.size() > 0) {
                                            HashMap deliciousContext = new HashMap();
                                            deliciousContext.put(DAILY_POSTING_USERNAME, deliciousUsername);
                                            deliciousContext.put(DAILY_POSTING_POSTS, posts);

                                            String renderedLinkTemplate = mergeTemplate(DAILY_POSTING_TEMPLATE, blog, deliciousContext);

                                            // Create the blog entry
                                            String nowAsString = DeliciousUtils.getDeliciousDate(now);
                                            postingCategory = BlojsomUtils.normalize(postingCategory);

                                            Entry entry;
                                            entry = _fetcher.newEntry();

                                            String title = MessageFormat.format(postTitle, new Object[]{nowAsString, deliciousUsername});
                                            entry.setBlogId(blog.getId());
                                            entry.setTitle(title);
                                            entry.setDescription(renderedLinkTemplate);
                                            entry.setDate(new Date());
                                            entry.setStatus(BlojsomMetaDataConstants.NEW_STATUS);
                                            entry.setBlogCategoryId(Integer.valueOf(postingCategory));
                                            try {
                                                if (_fetcher.loadUser(blog, postingAuthor) != null) {
                                                    entry.setAuthor(postingAuthor);
                                                }
                                            } catch (FetcherException e) {
                                            }

                                            _fetcher.saveEntry(blog, entry);

                                            _eventBroadcaster.broadcastEvent(new EntryAddedEvent(this, new Date(), entry, blog));
                                            if (_logger.isDebugEnabled()) {
                                                _logger.debug("Posted del.icio.us links for: " + blog.getBlogId() + " using: " + deliciousUsername);
                                            }
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                }
                            }
                        } catch (FetcherException e) {
                            if (_logger.isErrorEnabled()) {
                                _logger.error(e);
                            }
                        }
                    }

                    sleep(_pollTime);
                }
            } catch (InterruptedException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            }
        }
    }
}