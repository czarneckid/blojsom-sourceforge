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
package org.ignition.blojsom.extension.xmlrpc.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.blog.BlogCategory;
import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.util.BlojsomConstants;
import org.ignition.blojsom.util.BlojsomUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Blojsom XML-RPC Handler for the Blogger v1.0 API
 *
 * Blogger API spec can be found at http://plant.blogger.com/api/index.html
 *
 * @author Mark Lussier
 * @version $Id: BlojsomBloggerAPIHandler.java,v 1.8 2003-03-13 02:09:40 czarneckid Exp $
 */
public class BlojsomBloggerAPIHandler extends AbstractBlojsomAPIHandler implements BlojsomConstants {

    public static final String API_PREFIX = "blogger";

    /** */
    public static final String MEMBER_URL = "url";
    /** */
    public static final String MEMBER_BLOGID = "blogid";
    /** */
    public static final String MEMBER_BLOGNAME = "blogName";

    private Blog _blog;
    private Log _logger = LogFactory.getLog(BlojsomBloggerAPIHandler.class);

    /**
     * Default constructor
     */
    public BlojsomBloggerAPIHandler() {
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
     * Edits the main or archive index template of a given blog
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
        _logger.info("setTemplate() Called =====[ UNSUPPORTED ]=====");
        _logger.info("     Appkey: " + appkey);
        _logger.info("     BlogId: " + blogid);
        _logger.info("     UserId: " + userid);
        _logger.info("   Password: " + password);
        _logger.info("   Template: " + template);
        _logger.info("       Type: " + templateType);

        throw new XmlRpcException(UNSUPPORTED_EXCEPTION, UNSUPPORTED_EXCEPTION_MSG);
    }

    /**
     * Returns the main or archive index template of a given blog
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
        _logger.info("getTemplate() Called =====[ UNSUPPORTED ]=====");
        _logger.info("     Appkey: " + appkey);
        _logger.info("     BlogId: " + blogid);
        _logger.info("     UserId: " + userid);
        _logger.info("   Password: " + password);
        _logger.info("       Type: " + templateType);

        throw new XmlRpcException(UNSUPPORTED_EXCEPTION, UNSUPPORTED_EXCEPTION_MSG);
    }

    /**
     * Authenticates a user and returns basic user info (name, email, userid, etc.)
     *
     * @param appkey Unique identifier/passcode of the application sending the post
     * @param userid Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @throws XmlRpcException
     * @return
     */
    public String getUserInfo(String appkey, String userid, String password) throws Exception {
        _logger.info("getUserInfo() Called =====[ UNSUPPORTED ]=====");
        _logger.info("     Appkey: " + appkey);
        _logger.info("     UserId: " + userid);
        _logger.info("   Password: " + password);

        throw new XmlRpcException(UNSUPPORTED_EXCEPTION, UNSUPPORTED_EXCEPTION_MSG);
    }

    /**
     * Returns information on all the blogs a given user is a member of
     *
     * @param appkey Unique identifier/passcode of the application sending the post
     * @param userid Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @throws XmlRpcException
     * @return
     */
    public Object getUsersBlogs(String appkey, String userid, String password) throws Exception {
        _logger.info("getUsersBlogs() Called ===[ SUPPORTED ]=======");
        _logger.info("     Appkey: " + appkey);
        _logger.info("     UserId: " + userid);
        _logger.info("   Password: " + password);

        if (_blog.checkAuthorization(userid, password)) {
            Vector result = new Vector();

            BlogCategory[] _categories = _blog.getBlogCategories();

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
     * @throws XmlRpcException
     * @return
     */
    public boolean editPost(String appkey, String postid, String userid, String password, String content, boolean publish) throws Exception {
        _logger.info("editPost() Called ========[ SUPPORTED ]=====");
        _logger.info("     Appkey: " + appkey);
        _logger.info("     PostId: " + postid);
        _logger.info("     UserId: " + userid);
        _logger.info("   Password: " + password);
        _logger.info("    Publish: " + publish);
        _logger.info("     Content:\n " + content);

        if (_blog.checkAuthorization(userid, password)) {

            boolean result = false;

            String category;
            String permalink;
            String match = "?" + PERMALINK_PARAM + "=";

            int pos = postid.indexOf(match);
            if (pos != -1) {
                category = postid.substring(0, pos);
                permalink = postid.substring(pos + match.length());

                BlogEntry[] _entries = _blog.getPermalinkEntry(category, permalink);
                if (_entries != null && _entries.length > 0) {
                    BlogEntry _entry = _entries[0];
                    try {
                        FileOutputStream _fos = new FileOutputStream(_entry.getSource().getAbsolutePath(), false);
                        _fos.write(content.getBytes());
                        _fos.close();
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
     * @throws XmlRpcException
     * @return
     */
    public String newPost(String appkey, String blogid, String userid, String password, String content, boolean publish) throws Exception {
        _logger.info("newPost() Called ===========[ SUPPORTED ]=====");
        _logger.info("     Appkey: " + appkey);
        _logger.info("     BlogId: " + blogid);
        _logger.info("     UserId: " + userid);
        _logger.info("   Password: " + password);
        _logger.info("    Publish: " + publish);
        _logger.info("     Content:\n " + content);

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
                String outputfile = blogCategory.getAbsolutePath() + "/" + filename;
                String postid = blogid + "?" + PERMALINK_PARAM + "=" + filename;

                try {
                    FileOutputStream _fos = new FileOutputStream(outputfile, false);
                    _fos.write(content.getBytes());
                    _fos.close();
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
}
