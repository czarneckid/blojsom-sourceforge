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
import org.ignition.blojsom.BlojsomException;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.blog.BlogCategory;
import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.extension.xmlrpc.BlojsomXMLRPCConstants;
import org.ignition.blojsom.fetcher.BlojsomFetcher;
import org.ignition.blojsom.util.BlojsomConstants;
import org.ignition.blojsom.util.BlojsomUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Blojsom XML-RPC Handler for the MetaWeblog API
 *
 * MetaWeblog API pec can be found at http://www.xmlrpc.com/metaWeblogApi
 *
 * @author Mark Lussier
 * @version $Id: MetaWeblogAPIHandler.java,v 1.31 2003-07-06 22:25:39 czarneckid Exp $
 */
public class MetaWeblogAPIHandler extends AbstractBlojsomAPIHandler implements BlojsomConstants, BlojsomXMLRPCConstants {

    private static final String FETCHER_CATEGORY = "FETCHER_CATEGORY";
    private static final String FETCHER_PERMALINK = "FETCHER_PERMALINK";

    private static final String MEMBER_DESCRIPTION = "description";
    private static final String MEMBER_HTML_URL = "htmlUrl";
    private static final String MEMBER_RSS_URL = "rssUrl";
    private static final String MEMBER_TITLE = "title";
    private static final String MEMBER_LINK = "link";

    public static final String API_PREFIX = "metaWeblog";

    private Blog _blog;
    private BlojsomFetcher _fetcher;
    private String _blogEntryExtension;

    private Log _logger = LogFactory.getLog(MetaWeblogAPIHandler.class);

    /**
     * Default constructor
     */
    public MetaWeblogAPIHandler() {
    }

    /**
     * Gets the name of API Handler. Used to bind to XML-RPC
     *
     * @return The API Name (ie: metaWeblog)
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
        _blogEntryExtension = _blog.getBlogProperty(BLOG_XMLRPC_ENTRY_EXTENSION_IP);
        if (_blogEntryExtension == null || "".equals(_blogEntryExtension)) {
            _blogEntryExtension = DEFAULT_BLOG_XMLRPC_ENTRY_EXTENSION;
        }
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
     * Authenticates a user and returns the categories available in the blojsom
     *
     * @param blogid Dummy Value for Blojsom
     * @param userid Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @throws XmlRpcException If there are no categories or the user was not authenticated correctly
     * @return Blog category list
     */
    public Object getCategories(String blogid, String userid, String password) throws Exception {
        _logger.debug("getCategories() Called =====[ SUPPORTED ]=====");
        _logger.debug("     BlogId: " + blogid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: " + password);

        if (_blog.checkAuthorization(userid, password)) {
            Hashtable result;

            BlogCategory[] categories = _fetcher.fetchCategories(null);

            if (categories != null) {
                result = new Hashtable(categories.length);

                for (int x = 0; x < categories.length; x++) {
                    Hashtable catlist = new Hashtable(3);
                    BlogCategory category = categories[x];

                    String categoryId = category.getCategory();
                    if (categoryId.length() > 1) {
                        categoryId = BlojsomUtils.removeInitialSlash(categoryId);
                    }

                    String description = "No Category Metadata Found";
                    Map metadata = category.getMetaData();
                    if (metadata != null && metadata.containsKey(DESCRIPTION_KEY)) {
                        description = (String) metadata.get(DESCRIPTION_KEY);
                    }

                    catlist.put(MEMBER_DESCRIPTION, description);
                    catlist.put(MEMBER_HTML_URL, category.getCategoryURL());
                    catlist.put(MEMBER_RSS_URL, category.getCategoryURL() + "?flavor=rss");

                    result.put(categoryId, catlist);
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
     * @param postid Unique identifier of the post to be changed
     * @param userid Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @param struct Contents of the post
     * @param publish If true, the blog will be published immediately after the post is made
     * @throws XmlRpcException If the user was not authenticated correctly, if there was an I/O exception,
     * or if the entry permalink ID is invalid
     * @return <code>true</code> if the entry was edited, <code>false</code> otherwise
     */
    public boolean editPost(String postid, String userid, String password, Hashtable struct, boolean publish) throws Exception {
        _logger.debug("editPost() Called ========[ SUPPORTED ]=====");
        _logger.debug("     PostId: " + postid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: " + password);
        _logger.debug("    Publish: " + publish);

        if (_blog.checkAuthorization(userid, password)) {
            boolean result = false;

            String category;
            String permalink;
            String match = "?" + PERMALINK_PARAM + "=";

            int pos = postid.indexOf(match);
            if (pos != -1) {
                category = postid.substring(0, pos);
                category = BlojsomUtils.normalize(category);
                permalink = postid.substring(pos + match.length());

                BlogCategory blogCategory = _fetcher.newBlogCategory();
                blogCategory.setCategory(category);
                blogCategory.setCategoryURL(_blog.getBlogURL() + category);

                Map fetchMap = new HashMap();
                fetchMap.put(FETCHER_CATEGORY, blogCategory);
                fetchMap.put(FETCHER_PERMALINK, permalink);
                BlogEntry[] entries = _fetcher.fetchEntries(fetchMap);

                if (entries != null && entries.length > 0) {
                    BlogEntry entry = entries[0];

                    try {
                        Hashtable postcontent = struct;

                        String title = (String) postcontent.get(MEMBER_TITLE);
                        String description = (String) postcontent.get(MEMBER_DESCRIPTION);

                        if (title == null) {
                            title = "No Title";
                        }

                        String hashable = description;

                        if (description.length() > MAX_HASHABLE_LENGTH) {
                            hashable = hashable.substring(0, MAX_HASHABLE_LENGTH);
                        }

                        entry.setTitle(title);
                        entry.setDescription(description);
                        entry.save(_blog);
                        result = true;
                    } catch (BlojsomException e) {
                        _logger.error(e);
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
     * @param blogid Unique identifier of the blog the post will be added to
     * @param userid Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @param struct Contents of the post
     * @param publish If true, the blog will be published immediately after the post is made
     * @throws XmlRpcException If the user was not authenticated correctly or if there was an I/O exception
     * @return Post ID of the added entry
     */
    public String newPost(String blogid, String userid, String password, Hashtable struct, boolean publish) throws Exception {
        _logger.debug("newPost() Called ===========[ SUPPORTED ]=====");
        _logger.debug("     BlogId: " + blogid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: " + password);
        _logger.debug("    Publish: " + publish);

        blogid = BlojsomUtils.normalize(blogid);

        if (_blog.checkAuthorization(userid, password)) {
            String result = null;

            //Quick verify that the categories are valid
            File blogCategory = getBlogCategoryDirectory(blogid);
            if (blogCategory.exists() && blogCategory.isDirectory()) {

                Hashtable postcontent = struct;

                String title = (String) postcontent.get(MEMBER_TITLE);
                String description = (String) postcontent.get(MEMBER_DESCRIPTION);

                if (title == null) {
                    title = "No Title";
                }

                String hashable = description;

                if (description.length() > MAX_HASHABLE_LENGTH) {
                    hashable = hashable.substring(0, MAX_HASHABLE_LENGTH);
                }

                String baseFilename = BlojsomUtils.digestString(hashable).toUpperCase();
                String filename = baseFilename + _blogEntryExtension;
                String outputfile = blogCategory.getAbsolutePath() + File.separator + filename;
                String postid = blogid + "?" + PERMALINK_PARAM + "=" + filename;

                StringBuffer post = new StringBuffer();
                post.append(title).append("\n").append(description);

                try {
                    File sourceFile = new File(outputfile);
                    BlogEntry entry = _fetcher.newBlogEntry();
                    HashMap attributeMap = new HashMap();
                    HashMap blogEntryMetaData = new HashMap();

                    attributeMap.put(SOURCE_ATTRIBUTE, sourceFile);
                    entry.setAttributes(attributeMap);
                    entry.setCategory(blogid);
                    entry.setDescription(post.toString());
                    blogEntryMetaData.put(BLOG_METADATA_ENTRY_AUTHOR, userid);
                    entry.setMetaData(blogEntryMetaData);
                    entry.save(_blog);
                    result = postid;
                } catch (BlojsomException e) {
                    _logger.error(e);
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
     * (NOT IMPLEMENTED)
     *
     * @param postid
     * @param userid
     * @param password
     * @return
     * @throws XmlRpcException
     */
    public Object getPost(String postid, String userid, String password) throws Exception {
        _logger.debug("getPost() Called =========[ SUPPORTED ]=====");
        _logger.debug("     PostId: " + postid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: " + password);

        throw new XmlRpcException(UNSUPPORTED_EXCEPTION, UNSUPPORTED_EXCEPTION_MSG);
    }

    /**
     * (NOT IMPLEMENTED)
     *
     * @param blogid
     * @param userid
     * @param password
     * @param struct
     * @return
     * @throws XmlRpcException
     */
    public Object newMediaObject(String blogid, String userid, String password, Object struct) throws Exception {
        _logger.debug("newMediaObject() Called =[ UNSUPPORTED ]=====");
        _logger.debug("     BlogId: " + blogid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: " + password);

        throw new XmlRpcException(UNSUPPORTED_EXCEPTION, UNSUPPORTED_EXCEPTION_MSG);
    }

    /**
     * Get the blog category. If the category exists, return the
     * appropriate directory, otherwise return the "root" of this blog.
     *
     * @since blojsom 1.9
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