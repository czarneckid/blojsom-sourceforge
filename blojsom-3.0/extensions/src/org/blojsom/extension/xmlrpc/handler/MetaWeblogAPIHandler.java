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
import org.blojsom.BlojsomException;
import org.blojsom.authorization.AuthorizationException;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Category;
import org.blojsom.blog.Entry;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.admin.event.EntryAddedEvent;
import org.blojsom.plugin.admin.event.EntryDeletedEvent;
import org.blojsom.plugin.admin.event.EntryUpdatedEvent;
import org.blojsom.util.BlojsomMetaDataConstants;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.BlojsomConstants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

/**
 * MetaWeblogAPIHandler
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: MetaWeblogAPIHandler.java,v 1.4 2006-09-06 01:22:22 czarneckid Exp $
 */
public class MetaWeblogAPIHandler extends APIHandler {

    private Log _logger = LogFactory.getLog(MetaWeblogAPIHandler.class);

    private static final String METAWEBLOG_ACCEPTED_TYPES_IP = "blojsom-extension-metaweblog-accepted-types";

    /**
     * Blogger API "blogid" key
     */
    private static final String MEMBER_BLOGID = "blogid";

    /**
     * Blogger API "blogName" key
     */
    private static final String MEMBER_BLOGNAME = "blogName";

    /**
     * MetaWeblog API "description" key
     */
    private static final String MEMBER_DESCRIPTION = "description";

    /**
     * MetaWeblog API "htmlUrl" key
     */
    private static final String MEMBER_HTML_URL = "htmlUrl";

    /**
     * MetaWeblog API "rssUrl" key
     */
    private static final String MEMBER_RSS_URL = "rssUrl";

    /**
     * MetaWeblog API "title" key
     */
    private static final String MEMBER_TITLE = "title";

    /**
     * MetaWeblog API "link" key
     */
    private static final String MEMBER_LINK = "link";

    /**
     * MetaWeblog API "name" key
     */
    private static final String MEMBER_NAME = "name";

    /**
     * MetaWeblog API "type" key
     */
    private static final String MEMBER_TYPE = "type";

    /**
     * MetaWeblog API "bits" key
     */
    private static final String MEMBER_BITS = "bits";

    /**
     * MetaWeblog API "permaLink" key
     */
    private static final String MEMBER_PERMALINK = "permaLink";

    /**
     * MetaWeblog API "dateCreated" key
     */
    private static final String MEMBER_DATE_CREATED = "dateCreated";

    /**
     * MetaWeblog API "categories" key
     */
    private static final String MEMBER_CATEGORIES = "categories";

    /**
     * MetaWeblog API "postid" key
     */
    private static final String MEMBER_POSTID = "postid";

    /**
     * MetaWeblog API "url" key
     */
    private static final String MEMBER_URL = "url";

    private static final String METAWEBLOG_API_PERMISSION = "post_via_metaweblog_api_permission";

    private static final String API_PREFIX = "metaWeblog";

    private String _uploadDirectory;
    private HashMap _acceptedMimeTypes;
    private String _staticURLPrefix;

    /**
     * Create a new instance of the MetaWeblog API handler
     */
    public MetaWeblogAPIHandler() {
    }

    /**
     * Retrieve the API handler name
     *
     * @return API handler name "metaWeblog"
     */
    public String getName() {
        return API_PREFIX;
    }

    /**
     * Set the {@link Blog}
     *
     * @param blog {@link Blog}
     */
    public void setBlog(Blog blog) {
        super.setBlog(blog);

        _uploadDirectory = _servletConfig.getServletContext().getRealPath(_properties.getProperty(BlojsomConstants.RESOURCES_DIRECTORY_IP, BlojsomConstants.DEFAULT_RESOURCES_DIRECTORY));
        if (BlojsomUtils.checkNullOrBlank(_uploadDirectory)) {
            if (_logger.isErrorEnabled()) {
                _logger.error("Unable to obtain path to resources directory");
            }
        }

        if (!_uploadDirectory.endsWith("/")) {
            _uploadDirectory += "/";
        }

        _acceptedMimeTypes = new HashMap(3);
        String acceptedMimeTypes = _blog.getProperty(METAWEBLOG_ACCEPTED_TYPES_IP);
        if (acceptedMimeTypes != null && !"".equals(acceptedMimeTypes)) {
            String[] types = BlojsomUtils.parseCommaList(acceptedMimeTypes);
            for (int i = 0; i < types.length; i++) {
                String type = types[i];
                type = type.toLowerCase();
                _acceptedMimeTypes.put(type, type);
            }
        }

        _staticURLPrefix = _properties.getProperty(BlojsomConstants.RESOURCES_DIRECTORY_IP, BlojsomConstants.DEFAULT_RESOURCES_DIRECTORY);
        if (!_staticURLPrefix.endsWith("/")) {
            _staticURLPrefix += "/";
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
            checkXMLRPCPermission(userid, METAWEBLOG_API_PERMISSION);

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
     * Authenticates a user and returns the categories available in the blojsom
     *
     * @param blogid   Dummy Value for Blojsom
     * @param userid   Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @return Blog category list
     * @throws XmlRpcException If there are no categories or the user was not authenticated correctly
     */
    public Object getCategories(String blogid, String userid, String password) throws Exception {
        _logger.debug("getCategories() Called =====[ SUPPORTED ]=====");
        _logger.debug("     BlogId: " + blogid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: *********");

        try {
            _authorizationProvider.authorize(_blog, null, userid, password);
            checkXMLRPCPermission(userid, METAWEBLOG_API_PERMISSION);

            Hashtable result;

            Category[] categories = _fetcher.loadAllCategories(_blog);

            if (categories != null) {
                result = new Hashtable(categories.length);

                for (int x = 0; x < categories.length; x++) {
                    Hashtable catlist = new Hashtable(3);
                    Category category = categories[x];

                    String description;
                    if (!BlojsomUtils.checkNullOrBlank(category.getDescription())) {
                        description = category.getDescription();
                    } else {
                        description = category.getName();
                    }

                    catlist.put(MEMBER_DESCRIPTION, description);
                    catlist.put(MEMBER_HTML_URL, _blog.getBlogURL() + category.getName());
                    catlist.put(MEMBER_RSS_URL, _blog.getBlogURL() + category.getName() + "?flavor=rss2");

                    result.put(Integer.toString(category.getId().intValue()), catlist);
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
     * Makes a new post to a designated blog. Optionally, will publish the blog after making the post
     *
     * @param blogid   Unique identifier of the blog the post will be added to
     * @param userid   Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @param struct   Contents of the post
     * @param publish  If true, the blog will be published immediately after the post is made
     * @return Post ID of the added entry
     * @throws XmlRpcException If the user was not authenticated correctly or if there was an I/O exception
     */
    public String newPost(String blogid, String userid, String password, Hashtable struct, boolean publish) throws Exception {
        _logger.debug("newPost() Called ===========[ SUPPORTED ]=====");
        _logger.debug("     BlogId: " + blogid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: *********");
        _logger.debug("    Publish: " + publish);

        if (struct.containsKey(MEMBER_CATEGORIES)) {
            Vector categories = (Vector) struct.get(MEMBER_CATEGORIES);
            if (categories.size() > 0) {
                blogid = (String) categories.get(0);
            }
        }

        try {
            _authorizationProvider.authorize(_blog, null, userid, password);
            checkXMLRPCPermission(userid, METAWEBLOG_API_PERMISSION);

            Integer blogID;
            try {
                blogID = Integer.valueOf(blogid);
            } catch (NumberFormatException e) {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
            }

            String result;

            Hashtable postcontent;
            postcontent = struct;

            String title = (String) postcontent.get(MEMBER_TITLE);
            String description = (String) postcontent.get(MEMBER_DESCRIPTION);
            Date dateCreated = (Date) postcontent.get(MEMBER_DATE_CREATED);

            try {
                Category category = _fetcher.loadCategory(_blog, blogID);
                Entry entry = _fetcher.newEntry();

                entry.setBlogId(_blog.getBlogId());
                entry.setBlogCategoryId(category.getId());
                if (dateCreated == null) {
                    entry.setDate(new Date());
                } else {
                    entry.setDate(dateCreated);
                }
                entry.setModifiedDate(entry.getDate());
                entry.setTitle(title);
                entry.setDescription(description);
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
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
            }

            return result;
        } catch (AuthorizationException e) {
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     * Edits a given post. Optionally, will publish the blog after making the edit
     *
     * @param postid   Unique identifier of the post to be changed
     * @param userid   Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @param struct   Contents of the post
     * @param publish  If true, the blog will be published immediately after the post is made
     * @return <code>true</code> if the entry was edited, <code>false</code> otherwise
     * @throws XmlRpcException If the user was not authenticated correctly, if there was an I/O exception,
     *                         or if the entry permalink ID is invalid
     */
    public boolean editPost(String postid, String userid, String password, Hashtable struct, boolean publish) throws Exception {
        _logger.debug("editPost() Called ========[ SUPPORTED ]=====");
        _logger.debug("     PostId: " + postid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: *********");
        _logger.debug("    Publish: " + publish);

        try {
            _authorizationProvider.authorize(_blog, null, userid, password);
            checkXMLRPCPermission(userid, METAWEBLOG_API_PERMISSION);

            boolean result;

            Integer postID;
            try {
                postID = Integer.valueOf(BlojsomUtils.removeSlashes(postid));
            } catch (NumberFormatException e) {
                throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
            }

            try {
                Entry entryToEdit = _fetcher.loadEntry(_blog, postID);

                Hashtable postcontent;
                postcontent = struct;

                String title = (String) postcontent.get(MEMBER_TITLE);
                String description = (String) postcontent.get(MEMBER_DESCRIPTION);
                Date dateCreated = (Date) postcontent.get(MEMBER_DATE_CREATED);

                entryToEdit.setTitle(title);
                entryToEdit.setDescription(description);
                if (dateCreated != null) {
                    entryToEdit.setDate(dateCreated);
                    entryToEdit.setModifiedDate(dateCreated);
                } else {
                    entryToEdit.setModifiedDate(new Date());
                }

                if (publish) {
                    entryToEdit.setStatus(BlojsomMetaDataConstants.PUBLISHED_STATUS);
                } else {
                    entryToEdit.setStatus(BlojsomMetaDataConstants.DRAFT_STATUS);
                }

                _fetcher.saveEntry(_blog, entryToEdit);

                result = true;

                _eventBroadcaster.broadcastEvent(new EntryUpdatedEvent(this, new Date(), entryToEdit, _blog));
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
            }

            return result;
        } catch (AuthorizationException e) {
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     * Retrieves a given post from the blog
     *
     * @param postid   Unique identifier of the post to be changed
     * @param userid   Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @return Structure containing the minimal attributes for the MetaWeblog API getPost() method: title, link, and description
     * @throws XmlRpcException If the user was not authenticated correctly, if there was an I/O exception,
     *                         or if the entry permalink ID is invalid
     */
    public Object getPost(String postid, String userid, String password) throws Exception {
        _logger.debug("getPost() Called =========[ SUPPORTED ]=====");
        _logger.debug("     PostId: " + postid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: *********");

        try {
            _authorizationProvider.authorize(_blog, null, userid, password);
            checkXMLRPCPermission(userid, METAWEBLOG_API_PERMISSION);

            Integer postID;
            try {
                postID = Integer.valueOf(postid);
            } catch (NumberFormatException e) {
                throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
            }

            try {
                Entry entry = _fetcher.loadEntry(_blog, postID);

                Hashtable postcontent = new Hashtable();
                postcontent.put(MEMBER_TITLE, entry.getTitle());
                postcontent.put(MEMBER_LINK, _blog.getBlogURL() + entry.getCategory() + entry.getPostSlug());
                postcontent.put(MEMBER_DESCRIPTION, entry.getDescription());
                postcontent.put(MEMBER_DATE_CREATED, entry.getDate());
                postcontent.put(MEMBER_PERMALINK, _blog.getBlogURL() + entry.getCategory() + entry.getPostSlug());
                postcontent.put(MEMBER_POSTID, Integer.toString(entry.getId().intValue()));

                Vector postCategories = new Vector(1);
                postCategories.add(Integer.toString(entry.getBlogCategoryId().intValue()));
                postcontent.put(MEMBER_CATEGORIES, postCategories);

                return postcontent;
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

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
     * @throws XmlRpcException
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
            checkXMLRPCPermission(userid, METAWEBLOG_API_PERMISSION);

            Integer postID;
            try {
                postID = Integer.valueOf(BlojsomUtils.removeSlashes(postid));
            } catch (NumberFormatException e) {
                throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
            }

            try {
                Entry entryToDelete = _fetcher.loadEntry(_blog, postID);
                _fetcher.deleteEntry(_blog, entryToDelete);

                result = true;
                _eventBroadcaster.broadcastEvent(new EntryDeletedEvent(this, new Date(), entryToDelete, _blog));
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
            }
        } catch (AuthorizationException e) {
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }

        return result;
    }

    /**
     * Retrieves a set of recent posts to the blog
     *
     * @param blogid        Unique identifier of the blog the post will be added to
     * @param userid        Login for a MetaWeblog user who has permission to post to the blog
     * @param password      Password for said username
     * @param numberOfPosts Number of posts to be retrieved from the blog
     * @return Array of structures containing the minimal attributes for the MetaWeblog API getPost() method: title, link, and description
     * @throws Exception If the user was not authenticated correctly
     */
    public Object getRecentPosts(String blogid, String userid, String password, int numberOfPosts) throws Exception {
        _logger.debug("getRecentPosts() Called =========[ SUPPORTED ]=====");
        _logger.debug("     BlogId: " + blogid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: *********");

        Vector recentPosts = new Vector();

        try {
            _authorizationProvider.authorize(_blog, null, userid, password);
            checkXMLRPCPermission(userid, METAWEBLOG_API_PERMISSION);

            Entry[] entries;
            try {
                entries = _fetcher.loadEntriesForCategory(_blog, Integer.valueOf(blogid), new Integer(numberOfPosts));
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
                    entrystruct.put(MEMBER_LINK, _blog.getBlogURL() + entry.getCategory() + entry.getPostSlug());
                    entrystruct.put(MEMBER_DESCRIPTION, entry.getDescription());
                    entrystruct.put(MEMBER_DATE_CREATED, entry.getDate());
                    entrystruct.put(MEMBER_PERMALINK, _blog.getBlogURL() + entry.getCategory() + entry.getPostSlug());
                    entrystruct.put(MEMBER_POSTID, Integer.toString(entry.getId().intValue()));

                    Vector postCategories = new Vector(1);
                    postCategories.add(Integer.toString(entry.getBlogCategoryId().intValue()));
                    entrystruct.put(MEMBER_CATEGORIES, postCategories);

                    recentPosts.add(entrystruct);
                }
            }

            return recentPosts;
        } catch (AuthorizationException e) {
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     * Uploads an object to the blog to a specified directory
     *
     * @param blogid   Unique identifier of the blog the post will be added to
     * @param userid   Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @param struct   Upload structure defined by the MetaWeblog API
     * @return Structure containing a link to the uploaded media object
     * @throws XmlRpcException If the user was not authenticated correctly, if there was an I/O exception,
     *                         or if the MIME type of the upload object is not accepted
     */
    public Object newMediaObject(String blogid, String userid, String password, Hashtable struct) throws Exception {
        _logger.debug("newMediaObject() Called =[ SUPPORTED ]=====");
        _logger.debug("     BlogId: " + blogid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: *********");

        try {
            _authorizationProvider.authorize(_blog, null, userid, password);
            checkXMLRPCPermission(userid, METAWEBLOG_API_PERMISSION);

            String name = (String) struct.get(MEMBER_NAME);
            name = BlojsomUtils.getFilenameFromPath(name);
            _logger.debug("newMediaObject name: " + name);
            String type = (String) struct.get(MEMBER_TYPE);
            _logger.debug("newMediaObject type: " + type);
            byte[] bits = (byte[]) struct.get(MEMBER_BITS);

            File uploadDirectory = new File(_uploadDirectory);
            if (!uploadDirectory.exists()) {
                if (_logger.isErrorEnabled()) {
                    _logger.error("Upload directory does not exist: " + uploadDirectory.toString());
                }

                throw new XmlRpcException(UNKNOWN_EXCEPTION, "Upload directory does not exist: " + uploadDirectory.toString());
            }

            if (_acceptedMimeTypes.containsKey(type.toLowerCase())) {
                try {
                    File uploadDirectoryForUser = new File(uploadDirectory, _blog.getBlogId());
                    if (!uploadDirectoryForUser.exists()) {
                        if (!uploadDirectoryForUser.mkdir()) {
                            if (_logger.isErrorEnabled()) {
                                _logger.error("Could not create upload directory for user: " + uploadDirectoryForUser.toString());
                            }

                            throw new XmlRpcException(UNKNOWN_EXCEPTION, "Could not create upload directory for user: " + _blog.getBlogId());
                        }
                    }

                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(uploadDirectoryForUser, name)));
                    bos.write(bits);
                    bos.close();

                    Hashtable returnStruct = new Hashtable(1);
                    String mediaURL = _blog.getBlogBaseURL() + _staticURLPrefix + _blog.getBlogId() + "/" + name;
                    returnStruct.put(MEMBER_URL, mediaURL);

                    return returnStruct;
                } catch (IOException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }

                    throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
                }
            } else {
                throw new XmlRpcException(UNKNOWN_EXCEPTION, "MIME type not accepted. Received MIME type: " + type);
            }
        } catch (BlojsomException e) {
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
     * @return
     * @throws XmlRpcException
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
     * @return
     * @throws XmlRpcException
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
