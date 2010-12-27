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
package org.blojsom.extension.xmlrpc.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.blojsom.BlojsomException;
import org.blojsom.authorization.AuthorizationException;
import org.blojsom.blog.Category;
import org.blojsom.blog.Entry;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.admin.event.EntryAddedEvent;
import org.blojsom.plugin.admin.event.EntryDeletedEvent;
import org.blojsom.plugin.admin.event.EntryUpdatedEvent;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.BlojsomMetaDataConstants;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Blogger API handler
 *
 * @author David Czarnecki
 * @version $Id: BloggerAPIHandler.java,v 1.7 2008-07-07 19:54:24 czarneckid Exp $
 * @since blojsom 3.0
 */
public class BloggerAPIHandler extends APIHandler {

    private Log _logger = LogFactory.getLog(BloggerAPIHandler.class);

    /**
     * Blogger API "url" key
     */
    private static final String MEMBER_URL = "url";

    /**
     * Blogger API "blogid" key
     */
    private static final String MEMBER_BLOGID = "blogid";

    /**
     * Blogger APU "postid" key
     */
    private static final String MEMBER_POSTID = "postid";

    /**
     * Blogger API "blogName" key
     */
    private static final String MEMBER_BLOGNAME = "blogName";

    /**
     * Blogger API "title" key
     */
    private static final String MEMBER_TITLE = "title";

    /**
     * Blogger API "content" key
     */
    private static final String MEMBER_CONTENT = "content";

    /**
     * Blogger API "dateCreated" key
     */
    private static final String MEMBER_DATECREATED = "dateCreated";

    /**
     * Blogger API "authorName" key
     */
    private static final String MEMBER_AUTHORNAME = "authorName";

    /**
     * Blogger API "authorEmail" key
     */
    private static final String MEMBER_AUTHOREMAIL = "authorEmail";

    /**
     * Blogger API "nickname" key
     */
    private static final String MEMBER_NICKNAME = "nickname";

    /**
     * Blogger API "userid" key
     */
    private static final String MEMBER_USERID = "userid";

    /**
     * Blogger API "email" key
     */
    private static final String MEMBER_EMAIL = "email";

    /**
     * Blogger API "firstname" key
     */
    private static final String MEMBER_FIRSTNAME = "firstname";

    /**
     * Blogger API "lastname" key
     */
    private static final String MEMBER_LASTNAME = "lastname";

    private static final String TITLE_TAG_START = "<title>";
    private static final String TITLE_TAG_END = "</title>";

    private static final String API_PREFIX = "blogger";

    private static final String BLOGGER_API_PERMISSION = "post_via_blogger_api_permission";

    /**
     * Create a new instance of the Blogger API handler
     */
    public BloggerAPIHandler() {
    }

    /**
     * Retrieve the API handler name
     *
     * @return API handler name "blogger"
     */
    public String getName() {
        return API_PREFIX;
    }

    /**
     * Authenticates a user and returns basic user info (name, email, userid, etc.).
     *
     * @param appkey   Unique identifier/passcode of the application sending the post
     * @param userid   Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @return Basic user information (name, email, userid)
     * @throws org.apache.xmlrpc.XmlRpcException
     *          If there is an error
     */
    public Object getUserInfo(String appkey, String userid, String password) throws Exception {
        _logger.debug("getUserInfo() Called =====[ SUPPORTED ]=======");
        _logger.debug("     Appkey: " + appkey);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: *********");

        try {
            _authorizationProvider.authorize(_blog, null, userid, password);
            checkXMLRPCPermission(userid, BLOGGER_API_PERMISSION);

            Hashtable userinfo = new Hashtable();
            userinfo.put(MEMBER_EMAIL, _blog.getBlogOwnerEmail());
            userinfo.put(MEMBER_NICKNAME, userid);
            userinfo.put(MEMBER_USERID, "1");
            userinfo.put(MEMBER_URL, _blog.getBlogURL());

            String _ownerName = _blog.getBlogOwner();
            int _split = _ownerName.indexOf(" ");
            if (_split > 0) {
                userinfo.put(MEMBER_FIRSTNAME, _ownerName.substring(0, _split));
                userinfo.put(MEMBER_LASTNAME, _ownerName.substring(_split + 1));
            } else {
                userinfo.put(MEMBER_FIRSTNAME, "blojsom");
                userinfo.put(MEMBER_LASTNAME, _ownerName);
            }

            return userinfo;

        } catch (BlojsomException e) {
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     * Returns information on all the blogs a given user is a member of
     *
     * @param appkey   Unique identifier/passcode of the application sending the post
     * @param userid   Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @return Blog category list
     * @throws XmlRpcException If there are no categories or the user was not authenticated correctly
     */
    public Object getUsersBlogs(String appkey, String userid, String password) throws Exception {
        _logger.debug("getUsersBlogs() Called ===[ SUPPORTED ]=======");
        _logger.debug("     Appkey: " + appkey);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: *********");

        try {
            _authorizationProvider.authorize(_blog, null, userid, password);
            checkXMLRPCPermission(userid, BLOGGER_API_PERMISSION);

            Vector result = new Vector();

            Category[] categories = _fetcher.loadAllCategories(_blog);

            if (categories != null) {
                for (int x = 0; x < categories.length; x++) {
                    Hashtable _bloglist = new Hashtable(3);
                    Category category = categories[x];

                    String description;
                    if (!BlojsomUtils.checkNullOrBlank(category.getDescription())) {
                        description = category.getDescription();
                    } else {
                        description = category.getName();
                    }

                    _bloglist.put(MEMBER_URL, _blog.getBlogURL() + category.getName());
                    _bloglist.put(MEMBER_BLOGID, Integer.toString(category.getId().intValue()));
                    _bloglist.put(MEMBER_BLOGNAME, description);

                    result.add(_bloglist);
                }
            } else {
                throw new XmlRpcException(NOBLOGS_EXCEPTION, NOBLOGS_EXCEPTION_MSG);
            }

            return result;
        } catch (BlojsomException e) {
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     * Find a title in a content string delimited by &lt;title&gt;...&lt;/title&gt;
     *
     * @param content Content
     * @return Title found in content or <code>null</code> if the title was not present in &lt;title&gt; tags
     */
    private String findTitleInContent(String content) {
        String titleFromContent = null;
        int titleTagStartIndex = content.indexOf(TITLE_TAG_START);

        if (titleTagStartIndex != -1) {
            int titleTagEndIndex = content.indexOf(TITLE_TAG_END);

            if (titleTagEndIndex != -1 && (titleTagEndIndex > titleTagStartIndex)) {
                titleFromContent = content.substring(titleTagStartIndex + TITLE_TAG_START.length(), titleTagEndIndex);
            }
        }

        return titleFromContent;
    }

    /**
     * Makes a new post to a designated blog. Optionally, will publish the blog after making the post
     *
     * @param appkey   Unique identifier/passcode of the application sending the post
     * @param blogid   Unique identifier of the blog the post will be added to
     * @param userid   Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @param content  Contents of the post
     * @param publish  If true, the blog will be published immediately after the post is made
     * @return Post ID of the added entry
     * @throws XmlRpcException If the user was not authenticated correctly or if there was an I/O exception
     */
    public String newPost(String appkey, String blogid, String userid, String password, String content, boolean publish) throws Exception {
        _logger.debug("newPost() Called ===========[ SUPPORTED ]=====");
        _logger.debug("     Appkey: " + appkey);
        _logger.debug("     BlogId: " + blogid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: *********");
        _logger.debug("    Publish: " + publish);
        _logger.debug("     Content:\n " + content);

        try {
            _authorizationProvider.authorize(_blog, null, userid, password);
            checkXMLRPCPermission(userid, BLOGGER_API_PERMISSION);

            String result;
            String title = findTitleInContent(content);

            Integer blogID;
            try {
                blogID = Integer.valueOf(blogid);
            } catch (NumberFormatException e) {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
            }

            try {
                Category category = _fetcher.loadCategory(_blog, blogID);
                Entry entry = _fetcher.newEntry();

                if (title != null) {
                    content = BlojsomUtils.replace(content, TITLE_TAG_START + title + TITLE_TAG_END, "");
                    entry.setTitle(title);
                }

                entry.setBlogId(_blog.getId());
                entry.setBlogCategoryId(category.getId());
                entry.setDate(new Date());
                entry.setModifiedDate(entry.getDate());
                entry.setTitle(title);
                entry.setDescription(content);
                entry.setAuthor(userid);

                if (publish) {
                    entry.setStatus(BlojsomMetaDataConstants.PUBLISHED_STATUS);
                } else {
                    entry.setStatus(BlojsomMetaDataConstants.DRAFT_STATUS);
                }

                _fetcher.saveEntry(_blog, entry);

                result = Integer.toString(entry.getId().intValue());

                // Send out an add blog entry event
                _eventBroadcaster.broadcastEvent(new EntryAddedEvent(this, new Date(), entry, _blog));
            } catch (FetcherException e) {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
            }

            return result;
        } catch (BlojsomException e) {
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     * Edits a given post. Optionally, will publish the blog after making the edit
     *
     * @param appkey   Unique identifier/passcode of the application sending the post
     * @param postid   Unique identifier of the post to be changed
     * @param userid   Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @param content  Contents of the post
     * @param publish  If true, the blog will be published immediately after the post is made
     * @return <code>true</code> if the entry was edited, <code>false</code> otherwise
     * @throws XmlRpcException If the user was not authenticated correctly, if there was an I/O exception,
     *                         or if the entry permalink ID is invalid
     */
    public boolean editPost(String appkey, String postid, String userid, String password, String content, boolean publish) throws Exception {
        _logger.debug("editPost() Called ========[ SUPPORTED ]=====");
        _logger.debug("     Appkey: " + appkey);
        _logger.debug("     PostId: " + postid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: *********");
        _logger.debug("    Publish: " + publish);
        _logger.debug("     Content:\n " + content);

        try {
            _authorizationProvider.authorize(_blog, null, userid, password);
            checkXMLRPCPermission(userid, BLOGGER_API_PERMISSION);

            boolean result;

            Integer postID;
            try {
                postID = Integer.valueOf(postid);
            } catch (NumberFormatException e) {
                throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
            }

            try {
                Entry entryToEdit = _fetcher.loadEntry(_blog, postID);

                if (!userid.equals(entryToEdit.getAuthor())) {
                    checkXMLRPCPermission(userid, ALL_XMLRPC_EDIT_PERMISSION);
                }

                String title = findTitleInContent(content);
                if (title != null) {
                    content = BlojsomUtils.replace(content, TITLE_TAG_START + title + TITLE_TAG_END, "");
                    entryToEdit.setTitle(title);
                } else {
                    entryToEdit.setTitle("");
                }
                entryToEdit.setDescription(content);
                entryToEdit.setModifiedDate(new Date());

                if (publish) {
                    entryToEdit.setStatus(BlojsomMetaDataConstants.PUBLISHED_STATUS);
                } else {
                    entryToEdit.setStatus(BlojsomMetaDataConstants.DRAFT_STATUS);
                }

                _fetcher.saveEntry(_blog, entryToEdit);

                result = true;

                _eventBroadcaster.broadcastEvent(new EntryUpdatedEvent(this, new Date(), entryToEdit, _blog));
            } catch (FetcherException e) {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
            }

            return result;
        } catch (AuthorizationException e) {
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     * Get a particular post for a blojsom category
     *
     * @param appkey   Unique identifier/passcode of the application sending the post
     * @param postid   Unique identifier of the blog post
     * @param userid   Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @return Post to the blog
     * @throws XmlRpcException If the user was not authenticated correctly
     */
    public Object getPost(String appkey, String postid, String userid, String password) throws Exception {
        _logger.debug("getPost() Called ===========[ SUPPORTED ]=====");
        _logger.debug("     Appkey: " + appkey);
        _logger.debug("     PostId: " + postid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: *********");

        try {
            _authorizationProvider.authorize(_blog, null, userid, password);
            checkXMLRPCPermission(userid, BLOGGER_API_PERMISSION);

            Integer postID;
            try {
                postID = Integer.valueOf(postid);
            } catch (NumberFormatException e) {
                throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
            }

            try {
                Entry entry = _fetcher.loadEntry(_blog, postID);

                Hashtable entrystruct = new Hashtable();
                entrystruct.put(MEMBER_POSTID, Integer.toString(entry.getId().intValue()));
                entrystruct.put(MEMBER_BLOGID, Integer.toString(entry.getBlogCategory().getId().intValue()));
                entrystruct.put(MEMBER_TITLE, entry.getTitle());
                entrystruct.put(MEMBER_URL, _blog.getBlogURL() + entry.getCategory() + entry.getPostSlug());
                entrystruct.put(MEMBER_CONTENT, entry.getTitle() + BlojsomConstants.LINE_SEPARATOR + entry.getDescription());
                entrystruct.put(MEMBER_DATECREATED, entry.getDate());
                entrystruct.put(MEMBER_AUTHORNAME, _blog.getBlogOwner());
                entrystruct.put(MEMBER_AUTHOREMAIL, _blog.getBlogOwnerEmail());

                return entrystruct;
            } catch (FetcherException e) {
                throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
            }
        } catch (AuthorizationException e) {
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     * Delete a Post
     *
     * @param appkey   Unique identifier/passcode of the application sending the post
     * @param postid   Unique identifier of the post to be changed
     * @param userid   Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @param publish  Ignored
     * @return <code>true</code> if the entry was delete, <code>false</code> otherwise
     * @throws XmlRpcException If there is an error deleting the post
     */
    public boolean deletePost(String appkey, String postid, String userid, String password, boolean publish) throws Exception {
        _logger.debug("deletePost() Called =====[ SUPPORTED ]=====");
        _logger.debug("     Appkey: " + appkey);
        _logger.debug("     PostId: " + postid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: *********");

        boolean result;

        try {
            _authorizationProvider.authorize(_blog, null, userid, password);
            checkXMLRPCPermission(userid, BLOGGER_API_PERMISSION);

            Integer postID;
            try {
                postID = Integer.valueOf(postid);
            } catch (NumberFormatException e) {
                throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
            }

            try {
                Entry entryToDelete = _fetcher.loadEntry(_blog, postID);
                _fetcher.deleteEntry(_blog, entryToDelete);

                result = true;
                _eventBroadcaster.broadcastEvent(new EntryDeletedEvent(this, new Date(), entryToDelete, _blog));
            } catch (FetcherException e) {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
            }
        } catch (AuthorizationException e) {
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }

        return result;
    }

    /**
     * Get a list of recent posts for a blojsom category
     *
     * @param appkey   Unique identifier/passcode of the application sending the post
     * @param blogid   Unique identifier of the blog the post will be added to
     * @param userid   Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @param numposts Number of Posts to Retrieve
     * @return Recent posts to the blog
     * @throws XmlRpcException If the user was not authenticated correctly
     */
    public Object getRecentPosts(String appkey, String blogid, String userid, String password, int numposts) throws Exception {
        _logger.debug("getRecentPosts() Called ===========[ SUPPORTED ]=====");
        _logger.debug("     Appkey: " + appkey);
        _logger.debug("     BlogId: " + blogid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: *********");
        _logger.debug("   Numposts: " + numposts);

        Vector recentPosts = new Vector();

        try {
            _authorizationProvider.authorize(_blog, null, userid, password);
            checkXMLRPCPermission(userid, BLOGGER_API_PERMISSION);

            Entry[] entries;
            try {
                entries = _fetcher.loadEntriesForCategory(_blog, Integer.valueOf(blogid), new Integer(numposts));
            } catch (FetcherException e) {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
            } catch (NumberFormatException e) {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
            }

            if (entries != null && entries.length > 0) {
                for (int x = 0; x < entries.length; x++) {
                    Entry entry = entries[x];
                    Hashtable entrystruct = new Hashtable();
                    entrystruct.put(MEMBER_POSTID, Integer.toString(entry.getId().intValue()));
                    entrystruct.put(MEMBER_BLOGID, Integer.toString(entry.getBlogCategory().getId().intValue()));
                    entrystruct.put(MEMBER_TITLE, entry.getTitle());
                    entrystruct.put(MEMBER_URL, _blog.getBlogURL() + entry.getCategory() + entry.getPostSlug());
                    entrystruct.put(MEMBER_CONTENT, entry.getTitle() + BlojsomConstants.LINE_SEPARATOR + entry.getDescription());
                    entrystruct.put(MEMBER_DATECREATED, entry.getDate());
                    entrystruct.put(MEMBER_AUTHORNAME, _blog.getBlogOwner());
                    entrystruct.put(MEMBER_AUTHOREMAIL, _blog.getBlogOwnerEmail());
                    recentPosts.add(entrystruct);
                }
            }

            return recentPosts;
        } catch (AuthorizationException e) {
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     * Edits the main or archive index template of a given blog (NOT IMPLEMENTED)
     *
     * @param appkey       Unique identifier/passcode of the application sending the post
     * @param blogid       Unique identifier of the blog the post will be added to
     * @param userid       Login for a Blogger user who has permission to post to the blog
     * @param password     Password for said username
     * @param template     The text for the new template (usually mostly HTML). Must contain opening and closing <Blogger> tags, since they're needed to publish
     * @param templateType Determines which of the blog's templates will be returned. Currently, either "main" or "archiveIndex"
     * @return Not supported
     * @throws XmlRpcException Not supported
     */
    public boolean setTemplate(String appkey, String blogid, String userid, String password, String template, String templateType) throws Exception {
        _logger.debug("setTemplate() Called =====[ UNSUPPORTED ]=====");
        _logger.debug("     Appkey: " + appkey);
        _logger.debug("     BlogId: " + blogid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: *********");
        _logger.debug("   Template: " + template);
        _logger.debug("       Type: " + templateType);

        throw new XmlRpcException(UNSUPPORTED_EXCEPTION, UNSUPPORTED_EXCEPTION_MSG);
    }

    /**
     * Returns the main or archive index template of a given blog (NOT IMPLEMENTED)
     *
     * @param appkey       Unique identifier/passcode of the application sending the post
     * @param blogid       Unique identifier of the blog the post will be added to
     * @param userid       Login for a Blogger user who has permission to post to the blog
     * @param password     Password for said username
     * @param templateType Determines which of the blog's templates will be returned. Currently, either "main" or "archiveIndex"
     * @return Not supported
     * @throws XmlRpcException Not supported
     */
    public String getTemplate(String appkey, String blogid, String userid, String password, String templateType) throws Exception {
        _logger.debug("getTemplate() Called =====[ UNSUPPORTED ]=====");
        _logger.debug("     Appkey: " + appkey);
        _logger.debug("     BlogId: " + blogid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: *********");
        _logger.debug("       Type: " + templateType);

        throw new XmlRpcException(UNSUPPORTED_EXCEPTION, UNSUPPORTED_EXCEPTION_MSG);
    }
}
