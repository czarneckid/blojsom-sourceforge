package org.ignition.blojsom.util;

/**
 * BlojsomConstants
 *
 * @author David Czarnecki
 */
public class BlojsomConstants {

    private BlojsomConstants() {
    }

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
     * Default flavor for blojsom if none is requested or the flavor requested is invalid
     */
    public static final String DEFAULT_FLAVOR_HTML = "html";

    /**
     * Value indicating all subdirectories under the blog home should be searched
     */
    public static final int INFINITE_BLOG_DEPTH = -1;
}
