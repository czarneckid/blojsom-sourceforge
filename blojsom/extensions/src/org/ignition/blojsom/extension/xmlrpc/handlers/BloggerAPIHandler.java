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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.blog.BlogCategory;
import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.blog.FileBackedBlogEntry;
import org.ignition.blojsom.fetcher.BlojsomFetcher;
import org.ignition.blojsom.util.BlojsomConstants;
import org.ignition.blojsom.util.BlojsomUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Blojsom XML-RPC Handler for the Blogger v1.0 API
 *
 * Blogger API spec can be found at http://plant.blogger.com/api/index.html
 *
 * @author Mark Lussier
 * @version $Id: BloggerAPIHandler.java,v 1.9 2003-04-17 02:35:36 czarneckid Exp $
 */
public class BloggerAPIHandler extends AbstractBlojsomAPIHandler implements BlojsomConstants {

    public static final String API_PREFIX = "blogger";

    private static final String FETCHER_CATEGORY_STRING = "FETCHER_CATEGORY_STRING";
    private static final String FETCHER_PERMALINK = "FETCHER_PERMALINK";
    private static final String FETCHER_FLAVOR = "FETCHER_FLAVOR";
    private static final String FETCHER_NUM_POSTS_INTEGER = "FETCHER_NUM_POSTS_INTEGER";
    private static final String FETCHER_CATEGORY = "FETCHER_CATEGORY";

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

    private Blog _blog;

    private BlojsomFetcher _fetcher;

    private Log _logger = LogFactory.getLog(BloggerAPIHandler.class);

    /**
     * Default constructor
     */
    public BloggerAPIHandler() {
    }

    /**
     * Gets the Name of API Handler. Used to Bind to XML-RPC
     *
     * @return The API Name (ie: blogger)
     */
    public String getName() {
        return API_PREFIX;
    }

    /**
     * Attach a Blog instance to the API Handler so that it can interact with the blog
     *
     * @param bloginstance an instance of Blog
     * @see org.ignition.blojsom.blog.Blog
     */
    public void setBlog(Blog bloginstance) {
        _blog = bloginstance;
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
     * Delete a Post
     *
     * @param appkey Unique identifier/passcode of the application sending the post
     * @param postid Unique identifier of the post to be changed
     * @param userid Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @param publish Ignored
     * @throws XmlRpcException
     * @return <code>true</code> if the entry was delete, <code>false</code> otherwise
     */
    public boolean deletePost(String appkey, String postid, String userid, String password, boolean publish) throws Exception {
        _logger.debug("deletePost() Called =====[ UNSUPPORTED ]=====");
        _logger.debug("     Appkey: " + appkey);
        _logger.debug("     PostId: " + postid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: " + password);


        boolean result = false;

        if (_blog.checkAuthorization(userid, password)) {

            String category;
            String permalink;
            String match = "?" + PERMALINK_PARAM + "=";

            int pos = postid.indexOf(match);
            if (pos != -1) {
                category = postid.substring(0, pos);
                permalink = postid.substring(pos + match.length());

                HashMap fetchMap = new HashMap();
                fetchMap.put(FETCHER_CATEGORY_STRING, category);
                fetchMap.put(FETCHER_PERMALINK, permalink);
                BlogEntry[] _entries = _fetcher.fetchEntries(fetchMap);

                if (_entries != null && _entries.length > 0) {
                    FileBackedBlogEntry _entry = (FileBackedBlogEntry) _entries[0];
                    System.out.println("Deleting post " + _entry.getSource().getAbsolutePath());
                    result = _entry.getSource().delete();
                    // @todo Delete Comment and Trackbacks as well
                } else {
                    throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
                }
            }
        } else {
            _logger.error("Failed to authenticate user [" + userid + "] with password [" + password + "]");
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }

        return result;
    }

    /**
     * Edits the main or archive index template of a given blog (NOT IMPLEMENTED)
     *
     * @param appkey Unique identifier/passcode of the application sending the post
     * @param blogid Unique identifier of the blog the post will be added to
     * @param userid Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @param template The text for the new template (usually mostly HTML). Must contain opening and closing <Blogger> tags, since they're needed to publish
     * @param templateType Determines which of the blog's templates will be returned. Currently, either "main" or "archiveIndex"
     * @throws XmlRpcException
     * @return
     */
    public boolean setTemplate(String appkey, String blogid, String userid, String password, String template, String templateType) throws Exception {
        _logger.debug("setTemplate() Called =====[ UNSUPPORTED ]=====");
        _logger.debug("     Appkey: " + appkey);
        _logger.debug("     BlogId: " + blogid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: " + password);
        _logger.debug("   Template: " + template);
        _logger.debug("       Type: " + templateType);

        throw new XmlRpcException(UNSUPPORTED_EXCEPTION, UNSUPPORTED_EXCEPTION_MSG);
    }

    /**
     * Returns the main or archive index template of a given blog (NOT IMPLEMENTED)
     *
     * @param appkey Unique identifier/passcode of the application sending the post
     * @param blogid Unique identifier of the blog the post will be added to
     * @param userid Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @param templateType Determines which of the blog's templates will be returned. Currently, either "main" or "archiveIndex"
     * @throws XmlRpcException
     * @return
     */
    public String getTemplate(String appkey, String blogid, String userid, String password, String templateType) throws Exception {
        _logger.debug("getTemplate() Called =====[ UNSUPPORTED ]=====");
        _logger.debug("     Appkey: " + appkey);
        _logger.debug("     BlogId: " + blogid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: " + password);
        _logger.debug("       Type: " + templateType);

        throw new XmlRpcException(UNSUPPORTED_EXCEPTION, UNSUPPORTED_EXCEPTION_MSG);
    }

    /**
     * Authenticates a user and returns basic user info (name, email, userid, etc.). (NOT IMPLEMENTED)
     *
     * @param appkey Unique identifier/passcode of the application sending the post
     * @param userid Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @throws XmlRpcException
     * @return
     */
    public String getUserInfo(String appkey, String userid, String password) throws Exception {
        _logger.debug("getUserInfo() Called =====[ UNSUPPORTED ]=====");
        _logger.debug("     Appkey: " + appkey);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: " + password);

        throw new XmlRpcException(UNSUPPORTED_EXCEPTION, UNSUPPORTED_EXCEPTION_MSG);
    }

    /**
     * Returns information on all the blogs a given user is a member of
     *
     * @param appkey Unique identifier/passcode of the application sending the post
     * @param userid Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @throws XmlRpcException If there are no categories or the user was not authenticated correctly
     * @return Blog category list
     */
    public Object getUsersBlogs(String appkey, String userid, String password) throws Exception {
        _logger.debug("getUsersBlogs() Called ===[ SUPPORTED ]=======");
        _logger.debug("     Appkey: " + appkey);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: " + password);

        if (_blog.checkAuthorization(userid, password)) {
            Vector result = new Vector();

            BlogCategory[] _categories = _fetcher.fetchCategories(null);

            if (_categories != null) {
                for (int x = 0; x < _categories.length; x++) {
                    Hashtable _bloglist = new Hashtable(3);
                    BlogCategory _category = _categories[x];

                    String _blogid = _category.getCategory();
                    if (_blogid.length() > 1) {
                        _blogid = BlojsomUtils.removeInitialSlash(_blogid);
                    }

                    String _description = "";
                    HashMap _metadata = _category.getMetaData();
                    if (_metadata != null && _metadata.containsKey(NAME_KEY)) {
                        _description = (String) _metadata.get(NAME_KEY);
                    } else {
                        _description = _blogid;
                    }

                    _bloglist.put(MEMBER_URL, _category.getCategoryURL());
                    _bloglist.put(MEMBER_BLOGID, _blogid);
                    _bloglist.put(MEMBER_BLOGNAME, _description);

                    result.add(_bloglist);
                }
            } else {
                throw new XmlRpcException(NOBLOGS_EXCEPTION, NOBLOGS_EXCEPTION_MSG);
            }

            return result;
        } else {
            _logger.error("Failed to authenticate user [" + userid + "] with password [" + password + "]");
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     * Edits a given post. Optionally, will publish the blog after making the edit
     *
     * @param appkey Unique identifier/passcode of the application sending the post
     * @param postid Unique identifier of the post to be changed
     * @param userid Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @param content Contents of the post
     * @param publish If true, the blog will be published immediately after the post is made
     * @throws XmlRpcException If the user was not authenticated correctly, if there was an I/O exception,
     * or if the entry permalink ID is invalid
     * @return <code>true</code> if the entry was edited, <code>false</code> otherwise
     */
    public boolean editPost(String appkey, String postid, String userid, String password, String content, boolean publish) throws Exception {
        _logger.debug("editPost() Called ========[ SUPPORTED ]=====");
        _logger.debug("     Appkey: " + appkey);
        _logger.debug("     PostId: " + postid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: " + password);
        _logger.debug("    Publish: " + publish);
        _logger.debug("     Content:\n " + content);

        if (_blog.checkAuthorization(userid, password)) {

            boolean result = false;

            String category;
            String permalink;
            String match = "?" + PERMALINK_PARAM + "=";

            int pos = postid.indexOf(match);
            if (pos != -1) {
                category = postid.substring(0, pos);
                permalink = postid.substring(pos + match.length());

                HashMap fetchMap = new HashMap();
                fetchMap.put(FETCHER_CATEGORY_STRING, category);
                fetchMap.put(FETCHER_PERMALINK, permalink);
                BlogEntry[] _entries = _fetcher.fetchEntries(fetchMap);

                if (_entries != null && _entries.length > 0) {
                    FileBackedBlogEntry _entry = (FileBackedBlogEntry) _entries[0];
                    try {
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(_entry.getSource().getAbsolutePath(), false), UTF8));
                        bw.write(content);
                        bw.close();
                        result = true;
                    } catch (IOException e) {
                        throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
                    }
                } else {
                    throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
                }
            }

            return result;
        } else {
            _logger.error("Failed to authenticate user [" + userid + "] with password [" + password + "]");
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     * Makes a new post to a designated blog. Optionally, will publish the blog after making the post
     *
     * @param appkey Unique identifier/passcode of the application sending the post
     * @param blogid Unique identifier of the blog the post will be added to
     * @param userid Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @param content Contents of the post
     * @param publish If true, the blog will be published immediately after the post is made
     * @throws XmlRpcException If the user was not authenticated correctly or if there was an I/O exception
     * @return Post ID of the added entry
     */
    public String newPost(String appkey, String blogid, String userid, String password, String content, boolean publish) throws Exception {
        _logger.debug("newPost() Called ===========[ SUPPORTED ]=====");
        _logger.debug("     Appkey: " + appkey);
        _logger.debug("     BlogId: " + blogid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: " + password);
        _logger.debug("    Publish: " + publish);
        _logger.debug("     Content:\n " + content);

        if (_blog.checkAuthorization(userid, password)) {
            String result = null;

            //Quick verify that the categories are valid
            File blogCategory = new File(_blog.getBlogHome() + BlojsomUtils.removeInitialSlash(blogid));
            if (blogCategory.exists() && blogCategory.isDirectory()) {

                String hashable = content;

                if (content.length() > MAX_HASHABLE_LENGTH) {
                    hashable = hashable.substring(0, MAX_HASHABLE_LENGTH);
                }

                String filename = BlojsomUtils.digestString(hashable).toUpperCase() + ".txt";
                String outputfile = blogCategory.getAbsolutePath() + File.separator + filename;
                String postid = blogid + "?" + PERMALINK_PARAM + "=" + filename;

                try {
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputfile, false), UTF8));
                    bw.write(content);
                    bw.close();
                    result = postid;
                } catch (IOException e) {
                    throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
                }
            }

            return result;
        } else {
            _logger.error("Failed to authenticate user [" + userid + "] with password [" + password + "]");
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     * Get a list of recent posts for a blojsom category
     *
     * @param appkey Unique identifier/passcode of the application sending the post
     * @param blogid Unique identifier of the blog the post will be added to
     * @param userid Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @param numposts Number of Posts to Retrieve
     * @throws XmlRpcException If the user was not authenticated correctly
     * @return Recent posts to the blog
     */
    public Object getRecentPosts(String appkey, String blogid, String userid, String password, int numposts) throws Exception {
        _logger.debug("getRecentPosts() Called ===========[ SUPPORTED ]=====");
        _logger.debug("     Appkey: " + appkey);
        _logger.debug("     BlogId: " + blogid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: " + password);
        _logger.debug("     Number: " + numposts);

        Vector recentPosts = new Vector();

        if (_blog.checkAuthorization(userid, password)) {

            //Quick verify that the categories are valid
            File blogCategoryFile = new File(_blog.getBlogHome() + BlojsomUtils.removeInitialSlash(blogid));
            if (blogCategoryFile.exists() && blogCategoryFile.isDirectory()) {

                String requestedCategory = BlojsomUtils.removeInitialSlash(blogid);
                BlogCategory blogCategory = new BlogCategory(blogid, _blog.getBlogFileExtensions() + requestedCategory);

                BlogEntry[] entries;
                HashMap fetchMap = new HashMap();

                if (requestedCategory == null || "".equals(requestedCategory)) {
                    fetchMap.put(FETCHER_FLAVOR, DEFAULT_FLAVOR_HTML);
                    fetchMap.put(FETCHER_NUM_POSTS_INTEGER, new Integer(numposts));
                    entries = _fetcher.fetchEntries(fetchMap);
                } else {
                    fetchMap.put(FETCHER_CATEGORY, blogCategory);
                    fetchMap.put(FETCHER_NUM_POSTS_INTEGER, new Integer(numposts));
                    entries = _fetcher.fetchEntries(fetchMap);
                }

                if (entries != null && entries.length > 0) {
                    for (int x = 0; x < entries.length; x++) {
                        FileBackedBlogEntry entry = (FileBackedBlogEntry) entries[x];
                        Hashtable entrystruct = new Hashtable();
                        entrystruct.put(MEMBER_POSTID, blogid + "?" + PERMALINK_PARAM + "=" + entry.getSource().getName());
                        entrystruct.put(MEMBER_BLOGID, blogid);
                        entrystruct.put(MEMBER_TITLE, entry.getEscapedTitle());
                        entrystruct.put(MEMBER_URL, entry.getEscapedLink());
                        entrystruct.put(MEMBER_CONTENT, entry.getTitle() + "\n" + entry.getDescription());
                        entrystruct.put(MEMBER_DATECREATED, entry.getISO8601Date());
                        entrystruct.put(MEMBER_AUTHORNAME, _blog.getBlogOwner());
                        entrystruct.put(MEMBER_AUTHOREMAIL, _blog.getBlogOwnerEmail());
                        recentPosts.add(entrystruct);
                    }
                }
            }

            return recentPosts;
        } else {
            _logger.error("Failed to authenticate user [" + userid + "] with password [" + password + "]");
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }
}
