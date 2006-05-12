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
package org.blojsom.util;

/**
 * BlojsomConstants
 *
 * @author David Czarnecki
 * @author Mark Lussier
 * @since blojsom 3.0
 * @version $Id: BlojsomConstants.java,v 1.12 2006-05-12 17:46:37 czarneckid Exp $
 */
public interface BlojsomConstants {

    /**
     * blojsom version
     */
    public static final String BLOJSOM_VERSION_NUMBER = "blojsom v3.0 (M2)";

    /**
     * Key under which blog information will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_BLOG = "BLOJSOM_BLOG";

    /**
     * Key under which the blog entries will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_ENTRIES = "BLOJSOM_ENTRIES";

    /**
     * Key under which the blog categories will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_CATEGORIES = "BLOJSOM_CATEGORIES";

    /**
     * Key under which all the blog categories will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_ALL_CATEGORIES = "BLOJSOM_ALL_CATEGORIES";

    /**
     * Key under which the date (RFC 822 format) of the blog will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_DATE = "BLOJSOM_DATE";

    /**
     * Key under which the date (ISO 8601 format) of the blog will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_DATE_ISO8601 = "BLOJSOM_DATE_ISO8601";

    /**
     * Key under which the date object of the blog will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_DATE_OBJECT = "BLOJSOM_DATE_OBJECT";

    /**
     * Key under which the date (UTC format) of the blog will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_DATE_UTC = "BLOJSOM_DATE_UTC";

    /**
     * Key under which the blog site will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_SITE_URL = "BLOJSOM_SITE_URL";

    /**
     * Key under which the permalink value will be placed. This is used to allow templates
     * to generate trackback auto-discovery fragments.
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_PERMALINK = "BLOJSOM_PERMALINK";

    /**
     * Key under which the next entry after the permalink value will be placed. This is used to allow templates
     * to generate linear post navigation links.
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_PERMALINK_NEXT_ENTRY = "BLOJSOM_PERMALINK_NEXT_ENTRY";

    /**
     * Key under which the previous entry after the permalink value will be placed. This is used to allow templates
     * to generate linear post navigation links.
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_PERMALINK_PREVIOUS_ENTRY = "BLOJSOM_PERMALINK_PREVIOUS_ENTRY";

    /**
     * Key under which the requested category will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_REQUESTED_CATEGORY = "BLOJSOM_REQUESTED_CATEGORY";

    /**
     * Key under which the lastmodified date of the blog will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_LAST_MODIFIED = "BLOJSOM_LAST_MODIFIED";

    /**
     * Key under which the blog id will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_BLOG_ID = "BLOJSOM_BLOG_ID";

    /**
     * Key under which the blojsom version string will be placed
     * (example: on the request for the JSP dispatcher)
     */
    public static final String BLOJSOM_VERSION = "BLOJSOM_VERSION";

    /**
     * Key under which the blojsom requested flavor string will be placed
     * (example: in the context for the Velocity dispatcher)
     */
    public static final String BLOJSOM_REQUESTED_FLAVOR = "BLOJSOM_REQUESTED_FLAVOR";

    /**
     * Key under which the plugins will be placed
     * (example: in the context for the Velocity dispatcher)
     */
    public static final String BLOJSOM_PLUGINS = "BLOJSOM_PLUGINS";

    /**
     * Key under which the resource manager will be placed
     * (example: in the context for the Velocity dispatcher)
     */
    public static final String RESOURCE_MANAGER_CONTEXT_KEY = "BLOJSOM_RESOURCE_MANAGER";

    public static final String BLOJSOM_APPLICATION_CONTEXT = "BLOJSOM_APPLICATION_CONTEXT";
    public static final String BLOJSOM_XMLRPC_APPLICATION_CONTEXT = "BLOJSOM_XMLRPC_APPLICATION_CONTEXT";
    public static final String BLOJSOM_COMMENTAPI_APPLICATION_CONTEXT = "BLOJSOM_COMMENTAPI_APPLICATION_CONTEXT";

    /**
     * UTF-8 encoding
     */
    public static final String UTF8 = "UTF-8";

    /**
     * Default flavor for blojsom if none is requested or the flavor requested is invalid
     */
    public static final String DEFAULT_FLAVOR_HTML = "html";

    /**
     * Request parameter for the requested "flavor"
     */
    public static final String FLAVOR_PARAM = "flavor";

    /**
     * Request parameter for a "permalink"
     */
    public static final String PERMALINK_PARAM = "permalink";

    /**
     * Request parameter for the "plugins"
     */
    public static final String PLUGINS_PARAM = "plugins";

    /**
     * Request parameter for the "page"
     */
    public static final String PAGE_PARAM = "page";

    /**
     * Request parameter value for the archive page
     */
    public static final String PAGE_PARAM_ARCHIVE = "archive";

    /**
     * Request parameter value for not toggling LastModfied and ETag fromgetting generated
     */
    public static final String OVERRIDE_LASTMODIFIED_PARAM = "lastmodified";

    /**
     * Request parameter for the "category"
     */
    public static final String CATEGORY_PARAM = "category";

    /**
     * Request parameter for the "blog id"
     */
    public static final String BLOG_ID_PARAM = "blog_id";

    /**
     * Value indicating all subdirectories under the blog home should be searched
     */
    public static final int INFINITE_BLOG_DEPTH = -1;

    /**
     * Default language for blog if none supplied (en)
     */
    public static final String BLOG_LANGUAGE_DEFAULT = "en";

    /**
     * Default country for blog if none supplied (US)
     */
    public static final String BLOG_COUNTRY_DEFAULT = "US";

    /**
     * Default time zone (America/New_York)
     */
    public static final String BLOG_DEFAULT_TIMEZONE = "America/New_York";

    /**
     * Default number of blog entries to display 
     */
    public static final int BLOG_ENTRIES_DISPLAY_DEFAULT = 15;

    /**
     * HTTP Header Name representing the Last Modified Timstamp of the blog (GMT Based)
     */
    public static final String HTTP_LASTMODIFIED = "Last-Modified";

    /**
     * HTTP Header Name representing the ETag of the blog
     */
    public static final String HTTP_ETAG = "ETag";

    /**
     * RFC 822 style date format
     */
    public static final String RFC_822_DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z";

    /**
     * ISO 8601 style date format
     * ISO 8601 [W3CDTF] date format (used in rdf flavor)
     */
    public static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssz";

    /**
     * Short ISO 8601 style date format
     */
    public static final String SHORT_ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * UTC style date format
     */
    public static final String UTC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * If a entry is longer that this length, then when any content hashing is performed, it is
     * truncated to this size. NOTE: This only truncates for hash.
     */
    public static final int MAX_HASHABLE_LENGTH = 300;

    /**
     * Line separator for the system
     */
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static final String DEFAULT_DIGEST_ALGORITHM = "MD5";

    public static final String WHITESPACE = " \t\n\f\r";

    public static final String REDIRECT_TO_PARAM = "redirect_to";

    public static final String PAGE_NUMBER_PARAM = "pg_num";

    public static final String BLOJSOM_AJAX_STATUS = "BLOJSOM_AJAX_STATUS";
    public static final String SUCCESS = "success";
    public static final String FAILURE = "failure";

    /**
     * Various HTTP caching headers
     */
    public static final String PRAGMA_HTTP_HEADER = "Pragma";
    public static final String CACHE_CONTROL_HTTP_HEADER = "Cache-Control";
    public static final String NO_CACHE_HTTP_HEADER_VALUE = "no-cache";

    // Blojsom properties
    public static final String DEFAULT_BLOG_IP = "default-blog";
    public static final String TEMPLATES_DIRECTORY_IP = "templates-directory";
    public static final String RESOURCES_DIRECTORY_IP = "resources-directory";
    public static final String DEFAULT_CONFIGURATION_BASE_DIRECTORY = "/WEB-INF/";
    public static final String BLOGS_DIRECTORY_IP = "blogs-directory";
    public static final String DEFAULT_BLOGS_DIRECTORY = "/blogs/";
    public static final String BOOTSTRAP_DIRECTORY_IP = "bootstrap-directory";
    public static final String DEFAULT_BOOTSTRAP_DIRECTORY = "/bootstrap/";
    public static final String DEFAULT_TEMPLATES_DIRECTORY = "/templates/";
    public static final String DEFAULT_RESOURCES_DIRECTORY = "/resources/";
    public static final String INSTALLED_LOCALES_IP = "installed-locales";

    // Blog properties
    public static final String BLOG_NAME_IP = "blog-name";
    public static final String BLOG_DEPTH_IP = "blog-directory-depth";
    public static final String BLOG_LANGUAGE_IP = "blog-language";
    public static final String BLOG_COUNTRY_IP = "blog-country";
    public static final String BLOG_DESCRIPTION_IP = "blog-description";
    public static final String BLOG_URL_IP = "blog-url";
    public static final String BLOG_ADMIN_URL_IP = "blog-admin-url"; 
    public static final String BLOG_BASE_URL_IP = "blog-base-url";
    public static final String BLOG_ENTRIES_DISPLAY_IP = "blog-entries-display";
    public static final String BLOG_AUTHORIZATION_IP = "blog-authorization";
    public static final String BLOG_OWNER = "blog-owner";
    public static final String BLOG_OWNER_EMAIL = "blog-owner-email";
    public static final String BLOG_COMMENTS_ENABLED_IP = "blog-comments-enabled";
    public static final String BLOG_TRACKBACKS_ENABLED_IP = "blog-trackbacks-enabled";
    public static final String BLOG_PINGBACKS_ENABLED_IP = "blog-pingbacks-enabled"; 
    public static final String BLOG_EMAIL_ENABLED_IP = "blog-email-enabled";
    public static final String BLOJSOM_PLUGIN_CHAIN = "blojsom-plugin-chain";
    public static final String BLOG_DEFAULT_FLAVOR_IP = "blog-default-flavor";
    public static final String LINEAR_NAVIGATION_ENABLED_IP = "linear-navigation-enabled";
    public static final String XMLRPC_ENABLED_IP = "xmlrpc-enabled";
    public static final String BLOG_ADMINISTRATION_LOCALE_IP = "blog-administration-locale";
    public static final String USE_ENCRYPTED_PASSWORDS = "use-encrypted-passwords";
    public static final String DIGEST_ALGORITHM = "digest-algorithm";
    public static final String RECURSIVE_CATEGORIES = "recursive-categories";
    public static final String PREFERRED_SYNDICATION_FLAVOR = "preferred-syndication-flavor";
    public static final String USE_DYNAMIC_BLOG_URLS = "use-dynamic-blog-urls";
    public static final String RECENT_COMMENTS_COUNT = "recent-comments-count";
    public static final int DEFAULT_RECENT_COMMENTS_COUNT = 5;
    public static final String RECENT_TRACKBACKS_COUNT = "recent-trackbacks-count";
    public static final int DEFAULT_RECENT_TRACKBACKS_COUNT = 5;
    public static final String RECENT_PINGBACKS_COUNT = "recent-pingbacks-count";
    public static final int DEFAULT_RECENT_PINGBACKS_COUNT = 5;
    public static final String DEFAULT_POST_CATEGORY = "default-post-category";
    public static final String USE_RICHTEXT_EDITOR = "use-richtext-editor";
    
}

