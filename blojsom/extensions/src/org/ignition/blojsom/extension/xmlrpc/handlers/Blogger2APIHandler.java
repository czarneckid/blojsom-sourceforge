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
package org.ignition.blojsom.extension.xmlrpc.handlers;

import org.ignition.blojsom.util.BlojsomConstants;
import org.ignition.blojsom.util.BlojsomUtils;
import org.ignition.blojsom.extension.xmlrpc.BlojsomXMLRPCConstants;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.blog.BlogCategory;
import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.fetcher.BlojsomFetcher;
import org.ignition.blojsom.fetcher.BlojsomFetcherException;
import org.ignition.blojsom.BlojsomException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import java.io.File;

/**
 * Blogger2APIHandler
 *
 * Blogger 2 details can be found at http://www.blogger.com/developers/api/documentation20.html
 *
 * @author David Czarnecki
 * @since blojsom 1.9.3
 * @version $Id: Blogger2APIHandler.java,v 1.1 2003-06-23 02:28:08 czarneckid Exp $
 */
public class Blogger2APIHandler extends AbstractBlojsomAPIHandler implements BlojsomConstants, BlojsomXMLRPCConstants {

    private Log _logger = LogFactory.getLog(Blogger2APIHandler.class);

    private static final String API_PREFIX = "blogger2";

    private static final String FETCHER_PERMALINK = "FETCHER_PERMALINK";
    private static final String FETCHER_FLAVOR = "FETCHER_FLAVOR";
    private static final String FETCHER_NUM_POSTS_INTEGER = "FETCHER_NUM_POSTS_INTEGER";
    private static final String FETCHER_CATEGORY = "FETCHER_CATEGORY";

    private static final String LOGIN_USERNAME = "username";
    private static final String LOGIN_PASSWORD = "password";
    private static final String LOGIN_APPKEY = "appkey";
    private static final String LOGIN_CLIENTID = "clientID";
    private static final String LOGIN_TOKEN = "token";

    private static final String POST_POSTID = "postID";
    private static final String POST_BLOGID = "blogID";
    private static final String POST_BODY = "body";
    private static final String POST_DATECREATED = "dateCreated";
    private static final String POST_POSTOPTIONS = "postOptions";

    private static final String BLOG_BLOGID = "blogID";
    private static final String BLOG_URL = "url";
    private static final String BLOG_BLOGNAME = "blogName";

    private static final String ACTIONS_DOPUBLISH = "doPublish";
    private static final String ACTIONS_MAKEDRAFT = "makeDraft";
    private static final String ACTIONS_SYNDICATE = "syndicate";
    private static final String ACTIONS_ALLOWCOMMENTS = "allowComments";

    private static final String USER_USERNAME = "username";
    private static final String USER_FIRSTNAME = "firstName";
    private static final String USER_LASTNAME = "lastName";
    private static final String USER_EMAIL = "email";
    private static final String USER_PID = "pid";

    private static final String POSTOPTIONS_CATEGORIES = "categories";
    private static final String POSTOPTIONS_TITLE = "title";
    private static final String POSTOPTIONS_PERMALINKURL = "permalinkUrl";
    private static final String POSTOPTIONS_RELATEDURL = "relatedUrl";
    private static final String POSTOPTIONS_CONVERTLINEBREAKS = "convertLineBreaks";

    private static final String FILTERS_NUMOFPOSTS = "numOfPosts";

    private Blog _blog;
    private BlojsomFetcher _fetcher;
    private String _blogEntryExtension;

    /**
     * Default constructor
     */
    public Blogger2APIHandler() {
    }

    /**
     * Attach a blog instance to the API Handler so that it can interact with the blog
     *
     * @param bloginstance an instance of Blog
     * @see Blog
     */
    public void setBlog(Blog bloginstance) {
        _blog = bloginstance;
        _blogEntryExtension = _blog.getBlogProperty(BLOG_XMLRPC_ENTRY_EXTENSION_IP);
        if (_blogEntryExtension == null || "".equals(_blogEntryExtension)) {
            _blogEntryExtension = DEFAULT_BLOG_XMLRPC_ENTRY_EXTENSION;
        }
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
     * Set the {@link BlojsomFetcher} instance that will be used to fetch categories and entries
     *
     * @param fetcher {@link BlojsomFetcher} instance
     */
    public void setFetcher(BlojsomFetcher fetcher) {
        _fetcher = fetcher;
    }

    /**
     *
     * @param login
     * @param post
     * @param actions
     * @return
     * @throws XmlRpcException
     */
    public String newPost(Hashtable login, Hashtable post, Hashtable actions) throws XmlRpcException {
        _logger.debug("newPost called. SUPPORTED");

        if (checkAuthorization(login)) {
            String category = (String) post.get(POST_BLOGID);
            String username = (String) login.get(LOGIN_USERNAME);
            String result = null;

            category = BlojsomUtils.normalize(category);

            // Quick verify that the category is valid
            File blogCategory = getBlogCategoryDirectory(category);
            if (blogCategory.exists() && blogCategory.isDirectory()) {
                String content = (String) post.get(POST_BODY);
                String hashable = content;

                if (content.length() > MAX_HASHABLE_LENGTH) {
                    hashable = hashable.substring(0, MAX_HASHABLE_LENGTH);
                }

                String baseFilename = BlojsomUtils.digestString(hashable).toUpperCase();
                String filename = baseFilename + _blogEntryExtension;
                String outputfile = blogCategory.getAbsolutePath() + File.separator + filename;
                String postid = category + "?" + PERMALINK_PARAM + "=" + filename;

                try {
                    File sourceFile = new File(outputfile);
                    BlogEntry entry = _fetcher.newBlogEntry();
                    Map attributeMap = new HashMap();
                    Map blogEntryMetaData = new HashMap();

                    attributeMap.put(SOURCE_ATTRIBUTE, sourceFile);
                    entry.setAttributes(attributeMap);
                    entry.setCategory(category);
                    entry.setDescription(content);
                    blogEntryMetaData.put(BLOG_METADATA_ENTRY_AUTHOR, username);
                    entry.setMetaData(blogEntryMetaData);
                    entry.save(_blog);

                    result = postid;
                    return result;
                } catch (BlojsomException e) {
                    _logger.error(e);
                    throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
                }

            } else {
                _logger.error("Invalid category directory: " + category);
                throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
            }
        } else {
            _logger.error("Failed to authenticate [" + getUsernameAndPassword(login) + "]");
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     *
     * @param login
     * @param post
     * @param actions
     * @return
     * @throws XmlRpcException
     */
    public Boolean editPost(Hashtable login, Hashtable post, Hashtable actions) throws XmlRpcException {
        _logger.debug("editPost called. SUPPORTED");

        if (checkAuthorization(login)) {
            String permalink = (String) post.get(POST_POSTID);
            String category = (String) post.get(POST_BLOGID);
            String content = (String) post.get(POST_BODY);

            category = BlojsomUtils.normalize(category);
            Map fetchMap = new HashMap();
            BlogCategory blogCategory = _fetcher.newBlogCategory();
            blogCategory.setCategory(category);
            blogCategory.setCategoryURL(_blog.getBlogURL() + category);
            fetchMap.put(FETCHER_CATEGORY, blogCategory);
            fetchMap.put(FETCHER_PERMALINK, permalink);

            try {
                BlogEntry[] entries = _fetcher.fetchEntries(fetchMap);
                if (entries != null && entries.length > 0) {
                    BlogEntry entry = entries[0];
                    try {
                        entry.setTitle(null);
                        entry.setDescription(content);
                        entry.save(_blog);

                        return Boolean.TRUE;
                    } catch (BlojsomException e) {
                        _logger.error(e);
                        throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
                    }
                } else {
                    throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
                }
            } catch (BlojsomFetcherException e) {
                _logger.error(e);
                throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
            }
        } else {
            _logger.error("Failed to authenticate [" + getUsernameAndPassword(login) + "]");
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     *
     * @param login
     * @param post
     * @param actions
     * @return
     * @throws XmlRpcException
     */
    public Boolean deletePost(Hashtable login, Hashtable post, Hashtable actions) throws XmlRpcException {
        _logger.debug("deletePost called. SUPPORTED");

        Boolean result = Boolean.FALSE;
        if (checkAuthorization(login)) {
            String permalink = (String) post.get(POST_POSTID);
            String category = (String) post.get(POST_BLOGID);

            category = BlojsomUtils.normalize(category);

            Map fetchMap = new HashMap();
            BlogCategory blogCategory = _fetcher.newBlogCategory();
            blogCategory.setCategory(category);
            blogCategory.setCategoryURL(_blog.getBlogURL() + category);
            fetchMap.put(FETCHER_CATEGORY, blogCategory);
            fetchMap.put(FETCHER_PERMALINK, permalink);
            BlogEntry[] _entries = new BlogEntry[0];
            try {
                _entries = _fetcher.fetchEntries(fetchMap);
            } catch (BlojsomFetcherException e) {
                _logger.error(e);
                throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
            }

            if (_entries != null && _entries.length > 0) {
                try {
                    _entries[0].delete(_blog);
                } catch (BlojsomException e) {
                    _logger.error(e);
                    throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
                }

                result = Boolean.TRUE;
            } else {
                _logger.error("Invalid postID: " + permalink);
                throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
            }

        } else {
            _logger.error("Failed to authenticate [" + getUsernameAndPassword(login) + "]");
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }

        return result;
    }

    /**
     *
     * @param login
     * @param post
     * @return
     * @throws XmlRpcException
     */
    public Object getPost(Hashtable login, Hashtable post) throws XmlRpcException {
        _logger.debug("getPost called. SUPPORTED");

        if (checkAuthorization(login)) {

            String category = (String) post.get(POST_BLOGID);
            String permalink = (String) post.get(POST_POSTID);

            category = BlojsomUtils.normalize(category);

            Map fetchMap = new HashMap();
            BlogCategory blogCategory = _fetcher.newBlogCategory();
            blogCategory.setCategory(category);
            blogCategory.setCategoryURL(_blog.getBlogURL() + category);
            fetchMap.put(FETCHER_CATEGORY, blogCategory);
            fetchMap.put(FETCHER_PERMALINK, permalink);
            BlogEntry[] _entries = new BlogEntry[0];
            try {
                _entries = _fetcher.fetchEntries(fetchMap);
            } catch (BlojsomFetcherException e) {
                _logger.error(e);
                throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
            }

            if (_entries != null && _entries.length > 0) {
                Hashtable postStructure = new Hashtable(5);
                Hashtable postOptions = new Hashtable(5);
                BlogEntry entry = _entries[0];
                postStructure.put(POST_POSTID, entry.getId());
                postStructure.put(POST_BLOGID, entry.getCategory());
                postStructure.put(POST_BODY, entry.getTitle() + LINE_SEPARATOR + entry.getDescription());
                postStructure.put(POST_DATECREATED, entry.getDate());
                Vector categories = new Vector();
                categories.add(entry.getCategory());
                postOptions.put(POSTOPTIONS_CATEGORIES, categories);
                postOptions.put(POSTOPTIONS_TITLE, entry.getEscapedTitle());
                postOptions.put(POSTOPTIONS_PERMALINKURL, entry.getEscapedLink());
                postOptions.put(POSTOPTIONS_RELATEDURL, entry.getEscapedLink());
                postOptions.put(POSTOPTIONS_CONVERTLINEBREAKS, "false");
                postStructure.put(POST_POSTOPTIONS, postOptions);

                return postStructure;
            } else {
                _logger.error("Invalid postID: " + permalink);
                throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
            }
        } else {
            _logger.error("Failed to authenticate [" + getUsernameAndPassword(login) + "]");
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     *
     * @param login
     * @param blogID
     * @param filters
     * @return
     * @throws XmlRpcException
     */
    public Object getPosts(Hashtable login, String blogID, Hashtable filters) throws XmlRpcException {
        _logger.debug("getPosts called. SUPPORTED");

        if (checkAuthorization(login)) {
            String category = BlojsomUtils.normalize(blogID);

            // Quick verify that the categories are valid
            File blogCategoryFile = new File(_blog.getBlogHome() + BlojsomUtils.removeInitialSlash(category));
            if (blogCategoryFile.exists() && blogCategoryFile.isDirectory()) {

                Vector recentPosts = new Vector();
                String requestedCategory = BlojsomUtils.removeInitialSlash(category);
                BlogEntry[] entries;
                Map fetchMap = new HashMap();
                BlogCategory blogCategory = _fetcher.newBlogCategory();
                blogCategory.setCategory(category);
                blogCategory.setCategoryURL(_blog.getBlogURL() + requestedCategory);
                Integer numposts = (Integer) filters.get(FILTERS_NUMOFPOSTS);

                try {
                    if (requestedCategory == null || "".equals(requestedCategory)) {
                        fetchMap.put(FETCHER_FLAVOR, DEFAULT_FLAVOR_HTML);
                        fetchMap.put(FETCHER_NUM_POSTS_INTEGER, numposts);
                        entries = _fetcher.fetchEntries(fetchMap);
                    } else {
                        fetchMap.put(FETCHER_CATEGORY, blogCategory);
                        fetchMap.put(FETCHER_NUM_POSTS_INTEGER, numposts);
                        entries = _fetcher.fetchEntries(fetchMap);
                    }

                    if (entries != null && entries.length > 0) {
                        for (int x = 0; x < entries.length; x++) {
                            BlogEntry entry = entries[x];
                            Hashtable entrystruct = new Hashtable(5);
                            entrystruct.put(POST_POSTID, entry.getId());
                            entrystruct.put(POST_BLOGID, entry.getCategory());
                            entrystruct.put(POST_BODY, entry.getTitle() + LINE_SEPARATOR + entry.getDescription());
                            entrystruct.put(POST_DATECREATED, entry.getDate());
                            Hashtable postOptions = new Hashtable();
                            Vector categories = new Vector();
                            categories.add(entry.getCategory());
                            postOptions.put(POSTOPTIONS_CATEGORIES, categories);
                            postOptions.put(POSTOPTIONS_TITLE, entry.getEscapedTitle());
                            postOptions.put(POSTOPTIONS_PERMALINKURL, entry.getEscapedLink());
                            postOptions.put(POSTOPTIONS_RELATEDURL, entry.getEscapedLink());
                            postOptions.put(POSTOPTIONS_CONVERTLINEBREAKS, "false");
                            entrystruct.put(POST_POSTOPTIONS, postOptions);
                            recentPosts.add(entrystruct);
                        }
                    }

                    return recentPosts;
                } catch (BlojsomFetcherException e) {
                    _logger.error(e);
                    throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
                }
            } else {
                _logger.error("Invalid category directory: " + category);
                throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
            }
        } else {
            _logger.error("Failed to authenticate [" + getUsernameAndPassword(login) + "]");
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     *
     * @param login
     * @return
     * @throws XmlRpcException
     */
    public Object getBlogs(Hashtable login) throws XmlRpcException {
        _logger.debug("getBlogs called. SUPPORTED");
        if (checkAuthorization(login)) {
            Vector result = new Vector();
            BlogCategory[] categories = null;

            try {
                categories = _fetcher.fetchCategories(null);
            } catch (BlojsomFetcherException e) {
                _logger.error(e);
            }

            if (categories != null) {
                for (int i = 0; i < categories.length; i++) {
                    BlogCategory category = categories[i];
                    Hashtable blogInfo = new Hashtable(3);

                    String blogID = category.getCategory();
                    if (blogID.length() > 1) {
                        blogID = BlojsomUtils.removeInitialSlash(blogID);
                    }

                    String description = "";
                    Map categoryMetaData = category.getMetaData();
                    if (categoryMetaData != null && categoryMetaData.containsKey(NAME_KEY)) {
                        description = (String) categoryMetaData.get(NAME_KEY);
                    } else {
                        description = blogID;
                    }

                    blogInfo.put(BLOG_BLOGID, blogID);
                    blogInfo.put(BLOG_URL, category.getCategoryURL());
                    blogInfo.put(BLOG_BLOGNAME, description);

                    result.add(blogInfo);
                }
            } else {
                throw new XmlRpcException(NOBLOGS_EXCEPTION, NOBLOGS_EXCEPTION_MSG);
            }

            return result;
        } else {
            _logger.error("Failed to authenticate [" + getUsernameAndPassword(login) + "]");
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     *
     * @param login
     * @param blogID
     * @param templateName
     * @return
     * @throws XmlRpcException
     */
    public Object getTemplate(Hashtable login, String blogID, String templateName) throws XmlRpcException {
        throw new XmlRpcException(UNSUPPORTED_EXCEPTION, UNSUPPORTED_EXCEPTION_MSG);
    }

    /**
     *
     * @param login
     * @param blogID
     * @param template
     * @return
     * @throws XmlRpcException
     */
    public Boolean setTemplate(Hashtable login, String blogID, Vector template) throws XmlRpcException {
        throw new XmlRpcException(UNSUPPORTED_EXCEPTION, UNSUPPORTED_EXCEPTION_MSG);
    }

    /**
     *
     * @param login
     * @param blogID
     * @return
     * @throws XmlRpcException
     */
    public Object getTemplates(Hashtable login, String blogID) throws XmlRpcException {
        throw new XmlRpcException(UNSUPPORTED_EXCEPTION, UNSUPPORTED_EXCEPTION_MSG);
    }

    /**
     *
     * @param login
     * @return
     * @throws XmlRpcException
     */
    public Object getUserInfo(Hashtable login) throws XmlRpcException {
        _logger.debug("getUserInfo called. SUPPORTED");
        if (checkAuthorization(login)) {
            Hashtable user = new Hashtable(5);

            user.put(USER_USERNAME, login.get(LOGIN_USERNAME));
            String ownerName = _blog.getBlogOwner();
            int _split = ownerName.indexOf(" ");
            if (_split > 0) {
                user.put(USER_FIRSTNAME, ownerName.substring(0, _split));
                user.put(USER_LASTNAME, ownerName.substring(_split + 1));
            } else {
                user.put(USER_FIRSTNAME, "blojsom");
                user.put(USER_LASTNAME, ownerName);
            }
            user.put(USER_EMAIL, _blog.getBlogOwnerEmail());
            user.put(USER_PID, new Integer(1));

            return user;
        } else {
            _logger.error("Failed to authenticate [" + getUsernameAndPassword(login) + "]");
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     *
     * @param login
     * @param user
     * @return
     * @throws XmlRpcException
     */
    public Boolean setUserInfo(Hashtable login, Hashtable user) throws XmlRpcException {
        throw new XmlRpcException(UNSUPPORTED_EXCEPTION, UNSUPPORTED_EXCEPTION_MSG);
    }

    /**
     *
     * @param login
     * @param blogID
     * @return
     * @throws XmlRpcException
     */
    public Object getBlogCategories(Hashtable login, String blogID) throws XmlRpcException {
        _logger.debug("getBlogCategories called. SUPPORTED");
        if (checkAuthorization(login)) {
            Vector result = new Vector();

            BlogCategory[] categories = null;
            try {
                categories = _fetcher.fetchCategories(null);
            } catch (BlojsomFetcherException e) {
                _logger.error(e);
            }

            if (categories != null) {
                for (int i = 0; i < categories.length; i++) {
                    BlogCategory category = categories[i];
                    result.add(category.getCategory());
                }
            } else {
                throw new XmlRpcException(NOBLOGS_EXCEPTION, NOBLOGS_EXCEPTION_MSG);
            }

            return result;
        } else {
            _logger.error("Failed to authenticate [" + getUsernameAndPassword(login) + "]");
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     *
     * @param login
     * @param blogID
     * @param categories
     * @return
     * @throws XmlRpcException
     */
    public Object setBlogCategories(Hashtable login, String blogID, Vector categories) throws XmlRpcException {
        _logger.debug("setBlogCategories called. UNSUPPORTED");
        throw new XmlRpcException(UNSUPPORTED_EXCEPTION, UNSUPPORTED_EXCEPTION_MSG);
    }

    /**
     *
     * @param login
     * @param blogID
     * @return
     * @throws XmlRpcException
     */
    public Boolean publish(Hashtable login, String blogID) throws XmlRpcException {
        _logger.debug("publish called. UNSUPPORTED");
        throw new XmlRpcException(UNSUPPORTED_EXCEPTION, UNSUPPORTED_EXCEPTION_MSG);
    }

    /**
     *
     * @param login
     * @return
     */
    private boolean checkAuthorization(Hashtable login) {
        String username = (String) login.get(LOGIN_USERNAME);
        String password = (String) login.get(LOGIN_PASSWORD);
        _logger.debug("Checking authorization for: " + username + "/" + password);

        if (username == null || "".equals(username) || password == null || "".equals(password)) {
            return false;
        } else {
            return _blog.checkAuthorization(username, password);
        }
    }

    /**
     *
     * @param login
     * @return
     */
    private String getUsernameAndPassword(Hashtable login) {
        String username = (String) login.get(LOGIN_USERNAME);
        String password = (String) login.get(LOGIN_PASSWORD);

        return username + "/" + password;
    }

    /**
     * Get the blog category. If the category exists, return the
     * appropriate directory, otherwise return the "root" of this blog.
     *
     * @param categoryName Category name
     * @return A directory into which a blog entry can be placed
     */
    protected File getBlogCategoryDirectory(String categoryName) {
        File blogCategory = new File(_blog.getBlogHome() + BlojsomUtils.removeInitialSlash(categoryName));
        if (blogCategory.exists() && blogCategory.isDirectory()) {
            return blogCategory;
        } else {
            return new File(_blog.getBlogHome() + "/");
        }
    }
}
