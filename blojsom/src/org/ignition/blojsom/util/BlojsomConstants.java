/**
 * Copyright (c) 2003, David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003 by Mark Lussier
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
package org.ignition.blojsom.util;

/**
 * BlojsomConstants
 *
 * @author David Czarnecki
 * @author Mark Lussier
 * @author Dan Morrill
 * @version $Id: BlojsomConstants.java,v 1.58 2003-05-15 00:44:53 czarneckid Exp $
 */
public interface BlojsomConstants {

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
     * Key under which the blog site will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_SITE_URL = "BLOJSOM_SITE_URL";


    /**
     * Key under which the permalink value will be placed. This is used to allow templates
     * to generate trackback auto-discovery fragments.
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_PERMALINK= "BLOJSOM_PERMALINK";


    /**
     * Key under which the requested category will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_REQUESTED_CATEGORY = "BLOJSOM_REQUESTED_CATEGORY";

    /**
     * Key under which whether or not comments are enabled will be placed
     * (example: on the request for the JSP dispatcher)
     */
    public static final String BLOJSOM_COMMENTS_ENABLED = "BLOJSOM_COMMENTS_ENABLED";

    /**
     * Key under which whether or not email messages will be sent to the blog author
     */
    public static final String BLOJSOM_EMAIL_ENABLED = "BLOJSOM_EMAIL_ENABLED";

    /**
     * Key under which the lastmodified date of the blog will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_LAST_MODIFIED = "BLOJSOM_LAST_MODIFIED";

    /**
     * Default class for fetching blog entries
     */
    public static final String BLOG_DEFAULT_FETCHER = "org.ignition.blojsom.fetcher.StandardFetcher";

    /**
     * UTF-8 encoding
     */
    public static final String UTF8 = "UTF-8";

    /**
     * Default flavor for blojsom if none is requested or the flavor requested is invalid
     */
    public static final String DEFAULT_FLAVOR_HTML = "html";

    /**
     * Default directory for adding comments
     */
    public static final String DEFAULT_COMMENTS_DIRECTORY = ".comments";

    /**
     * Reserved file extension for blojsom comments
     */
    public static final String COMMENT_EXTENSION = ".cmt";

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
     * Request parameter for the "category"
     */
    public static final String CATEGORY_PARAM = "category";

    /**
     * Default directory for adding trackbacks
     */
    public static final String DEFAULT_TRACKBACK_DIRECTORY = ".trackbacks";

    /**
     * Reserved file extension for blojsom trackbacks
     */
    public static final String TRACKBACK_EXTENSION = ".tb";

    /**
     * Value indicating all subdirectories under the blog home should be searched
     */
    public static final int INFINITE_BLOG_DEPTH = -1;

    /**
     * The properties file key that denotes a blog category description
     */
    public static final String DESCRIPTION_KEY = "blojsom.description";

    /**
     * The properties file key that denotes a blog category name (different from the directory name)
     */
    public static final String NAME_KEY = "blojsom.name";

    /**
     * Default language for blog if none supplied (en)
     */
    public static final String BLOG_LANGUAGE_DEFAULT = "en";

    /**
     * Default number of blog entries to display (-1 indicates all entries will be displayed)
     */
    public static final int BLOG_ENTRIES_DISPLAY_DEFAULT = -1;

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
    public static final String RFC_822_DATE_FORMAT = "EEE, d MMM yyyy kk:mm:ss Z";

    /**
     * ISO 8601 style date format
     * ISO 8601 [W3CDTF] date format (used in rdf flavor)
     */
    public static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'kk:mm:ssZ";

    /**
     * If a entry is longer that this length, then when any content hashing is performed, it is
     * truncated to this size. NOTE: This only truncates for hash.
     */
    public static final int MAX_HASHABLE_LENGTH = 300;


    /**
     * Entry Meta Data Key for Poster
     */
    public static final String BLOG_METADATA_ENTRY_AUTHOR ="blog-entry-author";

    /**
     * Entry MetaData File Header
     */
    public static final String BLOG_METADATA_HEADER = "blojsom entry metadata";

    // Blog initialization parameters from blojsom.properties
    static final String BLOG_HOME_IP = "blog-home";
    static final String BLOG_NAME_IP = "blog-name";
    static final String BLOG_DEPTH_IP = "blog-directory-depth";
    static final String BLOG_LANGUAGE_IP = "blog-language";
    static final String BLOG_DESCRIPTION_IP = "blog-description";
    static final String BLOG_URL_IP = "blog-url";
    static final String BLOG_BASE_URL_IP = "blog-base-url";
    static final String BLOG_FILE_EXTENSIONS_IP = "blog-file-extensions";
    static final String BLOG_PROPERTIES_EXTENSIONS_IP = "blog-properties-extensions";
    static final String BLOG_ENTRIES_DISPLAY_IP = "blog-entries-display";
    static final String BLOG_DEFAULT_CATEGORY_MAPPING_IP = "blog-default-category-mapping";
    static final String BLOG_DIRECTORY_FILTER_IP = "blog-directory-filter";
    static final String BLOG_AUTHORIZATION_IP = "blog-authorization";
    static final String BLOG_OWNER = "blog-owner";
    static final String BLOG_OWNER_EMAIL = "blog-owner-email";
    static final String BLOG_COMMENTS_DIRECTORY_IP = "blog-comments-directory";
    static final String BLOG_TRACKBACK_DIRECTORY_IP = "blog-trackbacks-directory";
    static final String BLOG_COMMENTS_ENABLED_IP = "blog-comments-enabled";
    static final String BLOG_EMAIL_ENABLED_IP = "blog-email-enabled";
    static final String BLOJSOM_PLUGIN_CHAIN = "blojsom-plugin-chain";
    static final String BLOG_FETCHER_IP = "blog-fetcher";
    static final String BLOG_ENTRY_META_DATA_EXTENSION_IP = "blog-entry-meta-data-extension";
    static final String BLOG_FILE_ENCODING_IP = "blog-file-encoding";
}
