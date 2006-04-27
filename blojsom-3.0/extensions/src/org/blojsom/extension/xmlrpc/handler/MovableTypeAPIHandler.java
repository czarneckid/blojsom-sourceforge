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
package org.blojsom.extension.xmlrpc.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.blojsom.authorization.AuthorizationException;
import org.blojsom.blog.Category;
import org.blojsom.blog.Entry;
import org.blojsom.blog.Trackback;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.trackback.TrackbackPlugin;
import org.blojsom.util.BlojsomUtils;

import java.util.Hashtable;
import java.util.Vector;

/**
 * MovableType API handler
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: MovableTypeAPIHandler.java,v 1.2 2006-04-27 20:03:00 czarneckid Exp $
 */
public class MovableTypeAPIHandler extends APIHandler {

    private Log _logger = LogFactory.getLog(MovableTypeAPIHandler.class);

    private static final String MEMBER_DATECREATED = "dateCreated";
    private static final String MEMBER_USERID = "userid";
    private static final String MEMBER_POSTID = "postid";
    private static final String MEMBER_TITLE = "title";
    private static final String MEMBER_CATEGORYID = "categoryId";
    private static final String MEMBER_CATEGORYNAME = "categoryName";
    private static final String MEMBER_ISPRIMARY = "isPrimary";
    private static final String MEMBER_KEY = "key";
    private static final String MEMBER_LABEL = "label";
    private static final String MEMBER_PING_TITLE = "pingTitle";
    private static final String MEMBER_PING_URL = "pingURL";
    private static final String MEMBER_PING_IP = "pingIP";

    private static final String API_PREFIX = "mt";

    private static final String MOVABLETYPE_API_PERMISSION = "post_via_movabletype_api_permission";

    /**
     * Construct a new <a href="http://www.movabletype.org/docs/mtmanual_programmatic.html">MovableType API</a> handler
     */
    public MovableTypeAPIHandler() {
    }

    /**
     * Gets the name of API Handler. Used to bind to XML-RPC
     *
     * @return The API Name (ie: blogger)
     */
    public String getName() {
        return API_PREFIX;
    }

    /**
     * Returns a bandwidth-friendly list of the most recent posts in the system.
     *
     * @param blogID        Blog ID
     * @param username      Username
     * @param password      Password
     * @param numberOfPosts Number of titles to retrieve
     * @return Bandwidth-friendly list of the most recent posts in the system
     * @throws Exception If there is an error retrieving post titles
     */
    public Object getRecentPostTitles(String blogID, String username, String password, int numberOfPosts) throws Exception {
        _logger.debug("getRecentPostTitles() Called ===========[ SUPPORTED ]=====");
        _logger.debug("     BlogId: " + blogID);
        _logger.debug("     UserId: " + username);
        _logger.debug("   Password: *********");
        _logger.debug("   Numposts: " + numberOfPosts);

        Vector recentPosts = new Vector();

        try {
            _authorizationProvider.authorize(_blog, null, username, password);
            checkXMLRPCPermission(username, MOVABLETYPE_API_PERMISSION);

            Entry[] entries;
            try {
                entries = _fetcher.loadEntriesForCategory(_blog, Integer.valueOf(blogID), new Integer(numberOfPosts));
            } catch (FetcherException e) {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
            } catch (NumberFormatException e) {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
            }

            if (entries != null && entries.length > 0) {
                for (int x = 0; x < entries.length; x++) {
                    Entry entry = entries[x];
                    Hashtable entrystruct = new Hashtable();

                    entrystruct.put(MEMBER_TITLE, entry.getTitle());
                    entrystruct.put(MEMBER_USERID, entry.getAuthor());
                    entrystruct.put(MEMBER_DATECREATED, entry.getDate());
                    entrystruct.put(MEMBER_POSTID, Integer.toString(entry.getId().intValue()));

                    recentPosts.add(entrystruct);
                }
            }

            return recentPosts;
        } catch (AuthorizationException e) {
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     * Returns a list of all categories defined in the weblog.
     *
     * @param blogID   Blog ID
     * @param username Username
     * @param password Password
     * @return List of all categories defined in the weblog
     * @throws Exception If there is an error getting the category list
     */
    public Object getCategoryList(String blogID, String username, String password) throws Exception {
        _logger.debug("getCategories() Called =====[ SUPPORTED ]=====");
        _logger.debug("     BlogId: " + blogID);
        _logger.debug("     UserId: " + username);
        _logger.debug("   Password: *********");

        try {
            _authorizationProvider.authorize(_blog, null, username, password);
            checkXMLRPCPermission(username, MOVABLETYPE_API_PERMISSION);

            Vector result;

            Category[] categories = _fetcher.loadAllCategories(_blog);

            if (categories != null) {
                result = new Vector(categories.length);

                for (int x = 0; x < categories.length; x++) {
                    Hashtable catlist = new Hashtable(3);
                    Category category = categories[x];

                    String description;
                    if (!BlojsomUtils.checkNullOrBlank(category.getDescription())) {
                        description = category.getDescription();
                    } else {
                        description = category.getName();
                    }

                    catlist.put(MEMBER_CATEGORYID, Integer.toString(category.getId().intValue()));
                    catlist.put(MEMBER_CATEGORYNAME, description);

                    result.add(catlist);
                }
            } else {
                throw new XmlRpcException(NOBLOGS_EXCEPTION, NOBLOGS_EXCEPTION_MSG);
            }

            return result;
        } catch (AuthorizationException e) {
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     * Returns a list of all categories to which the post is assigned. Since we only support
     * single categories at the moment, just return a single structure.
     *
     * @param postID   Post ID
     * @param username Username
     * @param password Password
     * @return An array of structs containing String categoryName, String categoryId, and boolean isPrimary
     */
    public Object getPostCategories(String postID, String username, String password) throws Exception {
        _logger.debug("getPost() Called =========[ SUPPORTED ]=====");
        _logger.debug("     PostId: " + postID);
        _logger.debug("     UserId: " + username);
        _logger.debug("   Password: *********");

        Vector result = new Vector();

        try {
            _authorizationProvider.authorize(_blog, null, username, password);
            checkXMLRPCPermission(username, MOVABLETYPE_API_PERMISSION);

            Integer postIDForEntry;
            try {
                postIDForEntry = Integer.valueOf(postID);
            } catch (NumberFormatException e) {
                throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
            }

            try {
                Entry entry = _fetcher.loadEntry(_blog, postIDForEntry);

                Hashtable categoryContent = new Hashtable();

                String description;
                if (!BlojsomUtils.checkNullOrBlank(entry.getBlogCategory().getDescription())) {
                    description = entry.getBlogCategory().getDescription();
                } else {
                    description = entry.getBlogCategory().getName();
                }

                categoryContent.put(MEMBER_CATEGORYID, entry.getBlogCategoryId());
                categoryContent.put(MEMBER_CATEGORYNAME, description);
                categoryContent.put(MEMBER_ISPRIMARY, Boolean.TRUE);

                result.add(categoryContent);
            } catch (FetcherException e) {
                throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
            }

            return result;
        } catch (AuthorizationException e) {
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     * Sets the categories for a post.
     *
     * @param postID     Post ID
     * @param username   Username
     * @param password   Password
     * @param categories Array of structs containing String categoryId and boolean isPrimary
     * @return <code>true</code> if categories set for a post
     * @throws Exception If there is an error setting the categories for a post
     */
    public boolean setPostCategories(String postID, String username, String password, Vector categories) throws Exception {
        throw new XmlRpcException(UNSUPPORTED_EXCEPTION, UNSUPPORTED_EXCEPTION_MSG);
    }

    /**
     * Retrieve information about the XML-RPC methods supported by the server.
     *
     * @return Array of method names supported by the server
     * @throws Exception If there is an error retrieving the list of supported XML-RPC methods.
     */
    public Object supportedMethods() throws Exception {
        Vector result = new Vector();

        result.add("blogger.newPost");
        result.add("blogger.editPost");
        result.add("blogger.getPost");
        result.add("blogger.deletePost");
        result.add("blogger.getRecentPosts");
        result.add("blogger.getUsersBlogs");
        result.add("blogger.getUserInfo");
        result.add("metaWeblog.getUsersBlogs");
        result.add("metaWeblog.getCategories");
        result.add("metaWeblog.newPost");
        result.add("metaWeblog.editPost");
        result.add("metaWeblog.getPost");
        result.add("metaWeblog.deletePost");
        result.add("metaWeblog.getRecentPosts");
        result.add("metaWeblog.newMediaObject");
        result.add("mt.getRecentPostTitles");
        result.add("mt.getCategoryList");
        result.add("mt.getPostCategories");
        result.add("mt.supportedMethods");
        result.add("mt.supportedTextFilters");
        result.add("mt.getTrackbackPings");

        return result;
    }

    /**
     * Retrieve information about the text formatting plugins supported by the server.
     *
     * @return An array of structs containing String key and String label. key is the
     *         unique string identifying a text formatting plugin, and label is the readable
     *         description to be displayed to a user
     * @throws Exception If there is an error retrieving the list of plugins
     */
    public Object supportedTextFilters() throws Exception {
        // Return an empty list as we need to figure out a way to determine supported formatting plugins
        return new Vector();
    }

    /**
     * Retrieve the list of TrackBack pings posted to a particular entry
     *
     * @param postID Post ID
     * @return An array of structs containing String pingTitle (the title of the entry sent
     *         in the ping), String pingURL (the URL of the entry), and String pingIP (the IP address
     *         of the host that sent the ping)
     * @throws Exception If there is an error retrieving trackbacks for an entry
     */
    public Object getTrackbackPings(String postID) throws Exception {
        _logger.debug("getTrackbackPings() Called =========[ SUPPORTED ]=====");
        _logger.debug("     PostId: " + postID);

        Vector trackbackPings = new Vector();

        Integer postIDForEntry;
        try {
            postIDForEntry = Integer.valueOf(postID);
        } catch (NumberFormatException e) {
            throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
        }

        try {
            Entry entry = _fetcher.loadEntry(_blog, postIDForEntry);
            Trackback[] trackbacks = entry.getTrackbacksAsArray();

            for (int i = 0; i < trackbacks.length; i++) {
                Hashtable trackbackInformation = new Hashtable(3);

                trackbackInformation.put(MEMBER_PING_TITLE, trackbacks[i].getTitle());
                trackbackInformation.put(MEMBER_PING_URL, trackbacks[i].getUrl());
                if (BlojsomUtils.checkMapForKey(trackbacks[i].getMetaData(), TrackbackPlugin.BLOJSOM_TRACKBACK_PLUGIN_METADATA_IP))
                {
                    trackbackInformation.put(MEMBER_PING_IP, trackbacks[i].getMetaData().get(TrackbackPlugin.BLOJSOM_TRACKBACK_PLUGIN_METADATA_IP));
                } else {
                    trackbackInformation.put(MEMBER_PING_IP, "");
                }

                trackbackPings.add(trackbackInformation);
            }

            return trackbackPings;
        } catch (FetcherException e) {
            throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
        }
    }

    /**
     * Publish (rebuild) all of the static files related to an entry from your weblog. Equivalent to saving an entry in the system (but without the ping)
     *
     * @param postID   Post ID
     * @param username Username
     * @param password Password
     * @return <code>true</code> if post published
     * @throws Exception If there is an error publishing the post
     */
    public boolean publishPost(String postID, String username, String password) throws Exception {
        throw new XmlRpcException(UNSUPPORTED_EXCEPTION, UNSUPPORTED_EXCEPTION_MSG);
    }

}
