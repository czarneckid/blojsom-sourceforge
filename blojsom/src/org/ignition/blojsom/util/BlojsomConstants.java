/**
 * Copyright (c) 2003, David A. Czarnecki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the "David A. Czarnecki" nor the names of
 * its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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
 */
public interface BlojsomConstants {

    /**
     * Value under which blog information will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_BLOG = "BLOJSOM_BLOG";

    /**
     * Value under which the blog entries will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_ENTRIES = "BLOJSOM_ENTRIES";

    /**
     * Value under which the blog categories will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_CATEGORIES = "BLOJSOM_CATEGORIES";

    /**
     * Value under which the date of the blog will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_DATE = "BLOJSOM_DATE";

    /**
     * Value under which the blog site will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_SITE_URL = "BLOJSOM_SITE_URL";

    /**
     * Value under which the requested category will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_REQUESTED_CATEGORY = "BLOJSOM_REQUESTED_CATEGORY";

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
     * Request parameter for the "year"
     */
    public static final String YEAR_PARAM = "year";

    /**
     * Request parameter for the "month"
     */
    public static final String MONTH_PARAM = "month";

    /**
     * Request parameter for the "day"
     */
    public static final String DAY_PARAM = "day";

    /**
     * Request parameter for the "query"
     */
    public static final String QUERY_PARAM = "query";

    /**
     * Request parameter for the "plugins"
     */
    public static final String PLUGINS_PARAM = "plugins";

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
     * RFC 822 style date format
     */
    public static final String RFC_822_DATE_FORMAT = "EEE, d MMM yyyy kk:mm:ss Z";

    /**
     * ISO 8601 style date format
     * ISO 8601 [W3CDTF] date format (used in rdf flavor)
     */
    public static final String ISO_8601_DATE_FORMAT = "yyyy-MM-d'T'kk:mm:ssZZZZZ";

    /**
     * If a entry is longer that this length, then when any content hashing is performed, it is
     * truncated to this size. NOTE: This only truncates for hash.
     */
    public static final int MAX_HASHABLE_LENGTH = 300;


    // Blog initialization parameters from blojsom.properties
    final static String BLOG_HOME_IP = "blog-home";
    final static String BLOG_NAME_IP = "blog-name";
    final static String BLOG_DEPTH_IP = "blog-directory-depth";
    final static String BLOG_LANGUAGE_IP = "blog-language";
    final static String BLOG_DESCRIPTION_IP = "blog-description";
    final static String BLOG_URL_IP = "blog-url";
    final static String BLOG_FILE_EXTENSIONS_IP = "blog-file-extensions";
    final static String BLOG_PROPERTIES_EXTENSIONS_IP = "blog-properties-extensions";
    static final String BLOG_ENTRIES_DISPLAY_IP = "blog-entries-display";
    static final String BLOG_DEFAULT_CATEGORY_MAPPING_IP = "blog-default-category-mapping";
    static final String BLOG_DIRECTORY_FILTER_IP = "blog-directory-filter";
    static final String BLOG_AUTHORIZATION_IP = "blog-authorization";

    static final String BLOG_OWNER = "blog-owner";
    static final String BLOG_OWNER_EMAIL = "blog-owner-email";
    static final String BLOJSOM_PLUGIN_CHAIN = "blojsom-plugin-chain";
}
