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
package org.blojsom.plugin.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.comment.CommentModerationPlugin;
import org.blojsom.plugin.comment.CommentPlugin;
import org.blojsom.plugin.pingback.PingbackPlugin;
import org.blojsom.plugin.trackback.TrackbackModerationPlugin;
import org.blojsom.plugin.trackback.TrackbackPlugin;
import org.blojsom.plugin.weblogsping.WeblogsPingPlugin;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Properties;

/**
 * EditBlogPropertiesPlugin
 *
 * @author David Czarnecki
 * @version $Id: EditBlogPropertiesPlugin.java,v 1.11 2007-01-17 02:35:05 czarneckid Exp $
 * @since blojsom 3.0
 */
public class EditBlogPropertiesPlugin extends BaseAdminPlugin {

    private static Log _logger = LogFactory.getLog(EditBlogPropertiesPlugin.class);

    private static final String EDIT_BLOG_PROPERTIES_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blog-properties";

    // Localization constants
    private static final String FAILED_EDIT_PROPERTIES_PERMISSION_KEY = "failed.edit.properties.permission.text";
    private static final String UPDATED_BLOG_PROPERTIES_KEY = "updated.blog.properties.text";
    private static final String FAILED_SAVE_BLOG_PROPERTIES_KEY = "failed.save.blog.properties.text";
    private static final String BLOG_PROPERTY_HAS_VALUE_KEY = "blog.property.has.value.text";
    private static final String BLOG_PROPERTY_NOT_FOUND_KEY = "blog.property.not.found.text";

    // Actions
    private static final String EDIT_BLOG_PROPERTIES_ACTION = "edit-blog-properties";
    private static final String CHECK_BLOG_PROPERTY_ACTION = "check-blog-property";
    private static final String SET_BLOG_PROPERTY_ACTION = "set-blog-property";

    private static final String BLOJSOM_INSTALLED_LOCALES = "BLOJSOM_INSTALLED_LOCALES";
    private static final String BLOJSOM_JVM_LANGUAGES = "BLOJSOM_JVM_LANGUAGES";
    private static final String BLOJSOM_JVM_COUNTRIES = "BLOJSOM_JVM_COUNTRIES";
    private static final String BLOJSOM_JVM_TIMEZONES = "BLOJSOM_JVM_TIMEZONES";

    // Permissions
    private static final String EDIT_BLOG_PROPERTIES_PERMISSION = "edit_blog_properties_permission";
    private static final String SET_ARBITRARY_PROPERTIES_PERMISSION = "set_arbitrary_properties_permission";
    private static final String CHECK_ARBITRARY_PROPERTIES_PERMISSION = "check_arbitrary_properties_permission";

    // Form items
    private static final String INDIVIDUAL_BLOG_PROPERTY = "individual-blog-property";
    private static final String INDIVIDUAL_BLOG_PROPERTY_VALUE = "individual-blog-property-value";

    private Properties _blojsomProperties;
    private Fetcher _fetcher;

    /**
     * Default constructor.
     */
    public EditBlogPropertiesPlugin() {
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
     * Set the default properties
     *
     * @param properties Default properties
     */
    public void setBlojsomProperties(Properties blojsomProperties) {
        _blojsomProperties = blojsomProperties;
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
        if (!authenticateUser(httpServletRequest, httpServletResponse, context, blog)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_LOGIN_PAGE);

            return entries;
        }

        String username = getUsernameFromSession(httpServletRequest, blog);
        if (!checkPermission(blog, null, username, EDIT_BLOG_PROPERTIES_PERMISSION)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            addOperationResultMessage(context, getAdminResource(FAILED_EDIT_PROPERTIES_PERMISSION_KEY, FAILED_EDIT_PROPERTIES_PERMISSION_KEY, blog.getBlogAdministrationLocale()));

            return entries;
        }

        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);
        if (BlojsomUtils.checkNullOrBlank(action)) {
            _logger.debug("User did not request edit action");
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
        } else if (PAGE_ACTION.equals(action)) {
            _logger.debug("User requested edit page");
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_PROPERTIES_PAGE);
        } else if (EDIT_BLOG_PROPERTIES_ACTION.equals(action)) {
            _logger.debug("User requested edit action");

            String blogPropertyValue = BlojsomUtils.getRequestValue(BlojsomConstants.BLOG_NAME_IP, httpServletRequest);
            blog.setBlogName(blogPropertyValue);
            blogPropertyValue = BlojsomUtils.getRequestValue(BlojsomConstants.BLOG_DESCRIPTION_IP, httpServletRequest);
            blog.setBlogDescription(blogPropertyValue);
            blogPropertyValue = BlojsomUtils.getRequestValue(BlojsomConstants.BLOG_COUNTRY_IP, httpServletRequest);
            blog.setBlogCountry(blogPropertyValue);
            blogPropertyValue = BlojsomUtils.getRequestValue(BlojsomConstants.BLOG_LANGUAGE_IP, httpServletRequest);
            blog.setBlogLanguage(blogPropertyValue);
            blogPropertyValue = BlojsomUtils.getRequestValue(BlojsomConstants.BLOG_ADMINISTRATION_LOCALE_IP, httpServletRequest);
            blog.setBlogAdministrationLocale(blogPropertyValue);
            blogPropertyValue = BlojsomUtils.getRequestValue("blog-timezone-id", httpServletRequest);
            if (BlojsomUtils.checkNullOrBlank(blogPropertyValue)) {
                blogPropertyValue = BlojsomConstants.BLOG_DEFAULT_TIMEZONE;
            }
            blog.setProperty("blog-timezone-id", blogPropertyValue);
            blogPropertyValue = BlojsomUtils.getRequestValue("blog-display-entries", httpServletRequest);
            try {
                int blogDisplayEntries = Integer.parseInt(blogPropertyValue);
                blog.setBlogDisplayEntries(blogDisplayEntries);
            } catch (NumberFormatException e) {
                _logger.error("Blog display entries parameter invalid.", e);
            }
            blogPropertyValue = BlojsomUtils.getRequestValue(BlojsomConstants.BLOG_OWNER, httpServletRequest);
            blog.setBlogOwner(blogPropertyValue);
            blogPropertyValue = BlojsomUtils.getRequestValue(BlojsomConstants.BLOG_OWNER_EMAIL, httpServletRequest);
            blog.setBlogOwnerEmail(blogPropertyValue);
            blogPropertyValue = BlojsomUtils.getRequestValue(BlojsomConstants.BLOG_COMMENTS_ENABLED_IP, httpServletRequest);
            blog.setBlogCommentsEnabled(Boolean.valueOf(blogPropertyValue));
            blogPropertyValue = BlojsomUtils.getRequestValue(BlojsomConstants.BLOG_TRACKBACKS_ENABLED_IP, httpServletRequest);
            blog.setBlogTrackbacksEnabled(Boolean.valueOf(blogPropertyValue));
            blogPropertyValue = BlojsomUtils.getRequestValue(BlojsomConstants.BLOG_EMAIL_ENABLED_IP, httpServletRequest);
            blog.setBlogEmailEnabled(Boolean.valueOf(blogPropertyValue));
            blogPropertyValue = BlojsomUtils.getRequestValue(BlojsomConstants.BLOG_DEFAULT_FLAVOR_IP, httpServletRequest);
            blog.setBlogDefaultFlavor(blogPropertyValue);
            blogPropertyValue = BlojsomUtils.getRequestValue(BlojsomConstants.LINEAR_NAVIGATION_ENABLED_IP, httpServletRequest);
            blog.setLinearNavigationEnabled(Boolean.valueOf(blogPropertyValue));
            blogPropertyValue = BlojsomUtils.getRequestValue(BlojsomConstants.BLOG_URL_IP, httpServletRequest);
            blog.setBlogURL(blogPropertyValue);
            blogPropertyValue = BlojsomUtils.getRequestValue(BlojsomConstants.BLOG_BASE_URL_IP, httpServletRequest);
            blog.setBlogBaseURL(blogPropertyValue);
            blogPropertyValue = BlojsomUtils.getRequestValue(BlojsomConstants.DEFAULT_POST_CATEGORY, httpServletRequest);
            blog.setProperty(BlojsomConstants.DEFAULT_POST_CATEGORY, blogPropertyValue);
            blogPropertyValue = BlojsomUtils.getRequestValue(BlojsomConstants.USE_DYNAMIC_BLOG_URLS, httpServletRequest);
            if (!BlojsomUtils.checkNullOrBlank(blogPropertyValue)) {
                blog.setProperty(BlojsomConstants.USE_DYNAMIC_BLOG_URLS, "true");
            } else {
                blog.setProperty(BlojsomConstants.USE_DYNAMIC_BLOG_URLS, "false");
            }            

            // Comment plugin properties
            blogPropertyValue = BlojsomUtils.getRequestValue(CommentPlugin.COMMENT_AUTOFORMAT_IP, httpServletRequest);
            blog.setProperty(CommentPlugin.COMMENT_AUTOFORMAT_IP, blogPropertyValue);
            blogPropertyValue = BlojsomUtils.getRequestValue(CommentPlugin.COMMENT_PREFIX_IP, httpServletRequest);
            blog.setProperty(CommentPlugin.COMMENT_PREFIX_IP, blogPropertyValue);
            blogPropertyValue = BlojsomUtils.getRequestValue(CommentPlugin.COMMENT_COOKIE_EXPIRATION_DURATION_IP, httpServletRequest);
            blog.setProperty(CommentPlugin.COMMENT_COOKIE_EXPIRATION_DURATION_IP, blogPropertyValue);
            blogPropertyValue = BlojsomUtils.getRequestValue(CommentPlugin.COMMENT_THROTTLE_MINUTES_IP, httpServletRequest);
            blog.setProperty(CommentPlugin.COMMENT_THROTTLE_MINUTES_IP, blogPropertyValue);
            blogPropertyValue = BlojsomUtils.getRequestValue(CommentPlugin.COMMENT_DAYS_EXPIRATION_IP, httpServletRequest);
            blog.setProperty(CommentPlugin.COMMENT_DAYS_EXPIRATION_IP, blogPropertyValue);
            blogPropertyValue = BlojsomUtils.getRequestValue(CommentModerationPlugin.COMMENT_MODERATION_ENABLED, httpServletRequest);
            blog.setProperty(CommentModerationPlugin.COMMENT_MODERATION_ENABLED, blogPropertyValue);

            // Trackback plugin properties
            blogPropertyValue = BlojsomUtils.getRequestValue(TrackbackPlugin.TRACKBACK_THROTTLE_MINUTES_IP, httpServletRequest);
            blog.setProperty(TrackbackPlugin.TRACKBACK_THROTTLE_MINUTES_IP, blogPropertyValue);
            blogPropertyValue = BlojsomUtils.getRequestValue(TrackbackPlugin.TRACKBACK_PREFIX_IP, httpServletRequest);
            blog.setProperty(TrackbackPlugin.TRACKBACK_PREFIX_IP, blogPropertyValue);
            blogPropertyValue = BlojsomUtils.getRequestValue(TrackbackPlugin.TRACKBACK_DAYS_EXPIRATION_IP, httpServletRequest);
            blog.setProperty(TrackbackPlugin.TRACKBACK_DAYS_EXPIRATION_IP, blogPropertyValue);
            blogPropertyValue = BlojsomUtils.getRequestValue(TrackbackModerationPlugin.TRACKBACK_MODERATION_ENABLED, httpServletRequest);
            blog.setProperty(TrackbackModerationPlugin.TRACKBACK_MODERATION_ENABLED, blogPropertyValue);

            // Pingback properties
            blogPropertyValue = BlojsomUtils.getRequestValue(BlojsomConstants.BLOG_PINGBACKS_ENABLED_IP, httpServletRequest);
            blog.setBlogPingbacksEnabled(Boolean.valueOf(blogPropertyValue));
            blogPropertyValue = BlojsomUtils.getRequestValue(PingbackPlugin.PINGBACK_PREFIX_IP, httpServletRequest);
            blog.setProperty(PingbackPlugin.PINGBACK_PREFIX_IP, blogPropertyValue);
            blogPropertyValue = BlojsomUtils.getRequestValue(PingbackPlugin.PINGBACK_MODERATION_ENABLED, httpServletRequest);
            blog.setProperty(PingbackPlugin.PINGBACK_MODERATION_ENABLED, blogPropertyValue);

            // Weblogs Ping plugin properties
            blogPropertyValue = BlojsomUtils.getRequestValue(WeblogsPingPlugin.BLOG_PING_URLS_IP, httpServletRequest);
            String[] pingURLs = BlojsomUtils.parseDelimitedList(blogPropertyValue, BlojsomUtils.WHITESPACE);
            if (pingURLs != null && pingURLs.length > 0) {
                blog.setProperty(WeblogsPingPlugin.BLOG_PING_URLS_IP, BlojsomUtils.arrayOfStringsToString(pingURLs, " "));
            } else {
                blog.setProperty(WeblogsPingPlugin.BLOG_PING_URLS_IP, "");
            }

            // XML-RPC settings
            blogPropertyValue = BlojsomUtils.getRequestValue(BlojsomConstants.XMLRPC_ENABLED_IP, httpServletRequest);
            blog.setXmlrpcEnabled(Boolean.valueOf(blogPropertyValue));

            try {
                _fetcher.saveBlog(blog);

                addOperationResultMessage(context, getAdminResource(UPDATED_BLOG_PROPERTIES_KEY, UPDATED_BLOG_PROPERTIES_KEY, blog.getBlogAdministrationLocale()));
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                addOperationResultMessage(context, getAdminResource(FAILED_SAVE_BLOG_PROPERTIES_KEY, FAILED_SAVE_BLOG_PROPERTIES_KEY, blog.getBlogAdministrationLocale()));
            }

            // Request that we go back to the edit blog properties page
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_PROPERTIES_PAGE);
        } else if (SET_BLOG_PROPERTY_ACTION.equals(action)) {
            _logger.debug("User requested set blog property action");

            if (!checkPermission(blog, null, username, SET_ARBITRARY_PROPERTIES_PERMISSION)) {
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
                addOperationResultMessage(context, getAdminResource(FAILED_EDIT_PROPERTIES_PERMISSION_KEY, FAILED_EDIT_PROPERTIES_PERMISSION_KEY, blog.getBlogAdministrationLocale()));

                return entries;
            }

            String blogProperty = BlojsomUtils.getRequestValue(INDIVIDUAL_BLOG_PROPERTY, httpServletRequest);
            if (!BlojsomUtils.checkNullOrBlank(blogProperty)) {
                String blogPropertyValue = BlojsomUtils.getRequestValue(INDIVIDUAL_BLOG_PROPERTY_VALUE, httpServletRequest);
                if (blogPropertyValue == null) {
                    blogPropertyValue = "";
                }

                blog.setProperty(blogProperty, blogPropertyValue);

                try {
                    _fetcher.saveBlog(blog);

                    addOperationResultMessage(context, getAdminResource(UPDATED_BLOG_PROPERTIES_KEY, UPDATED_BLOG_PROPERTIES_KEY, blog.getBlogAdministrationLocale()));
                } catch (FetcherException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }

                    addOperationResultMessage(context, getAdminResource(FAILED_SAVE_BLOG_PROPERTIES_KEY, FAILED_SAVE_BLOG_PROPERTIES_KEY, blog.getBlogAdministrationLocale()));
                }
            }

            // Request that we go back to the edit blog properties page
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_PROPERTIES_PAGE);
        } else if (CHECK_BLOG_PROPERTY_ACTION.equals(action)) {
            _logger.debug("User requested check blog property action");

            if (!checkPermission(blog, null, username, CHECK_ARBITRARY_PROPERTIES_PERMISSION)) {
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
                addOperationResultMessage(context, getAdminResource(FAILED_EDIT_PROPERTIES_PERMISSION_KEY, FAILED_EDIT_PROPERTIES_PERMISSION_KEY, blog.getBlogAdministrationLocale()));

                return entries;
            }

            String blogProperty = BlojsomUtils.getRequestValue(INDIVIDUAL_BLOG_PROPERTY, httpServletRequest);

            if (!BlojsomUtils.checkNullOrBlank(blogProperty)) {
                if (blog.getProperty(blogProperty) != null) {
                    addOperationResultMessage(context, formatAdminResource(BLOG_PROPERTY_HAS_VALUE_KEY, BLOG_PROPERTY_HAS_VALUE_KEY, blog.getBlogAdministrationLocale(), new Object[] {blogProperty, blog.getProperty(blogProperty)}));
                } else {
                    addOperationResultMessage(context, formatAdminResource(BLOG_PROPERTY_NOT_FOUND_KEY, BLOG_PROPERTY_NOT_FOUND_KEY, blog.getBlogAdministrationLocale(), new Object[] {blogProperty}));
                }
            }

            // Request that we go back to the edit blog properties page
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, EDIT_BLOG_PROPERTIES_PAGE);
        }

        String installedLocales = _blojsomProperties.getProperty(BlojsomConstants.INSTALLED_LOCALES_IP);
        if (installedLocales != null) {
            context.put(BLOJSOM_INSTALLED_LOCALES, BlojsomUtils.parseCommaList(installedLocales));
        }
        context.put(BLOJSOM_JVM_LANGUAGES, BlojsomUtils.getLanguagesForSystem(blog.getBlogAdministrationLocale()));
        context.put(BLOJSOM_JVM_COUNTRIES, BlojsomUtils.getCountriesForSystem(blog.getBlogAdministrationLocale()));
        context.put(BLOJSOM_JVM_TIMEZONES, BlojsomUtils.getTimeZonesForSystem(blog.getBlogAdministrationLocale()));

        return entries;
    }
}
