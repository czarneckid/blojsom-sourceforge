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
import org.apache.xmlrpc.Base64;
import org.ignition.blojsom.BlojsomException;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.blog.BlogCategory;
import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.extension.xmlrpc.BlojsomXMLRPCConstants;
import org.ignition.blojsom.fetcher.BlojsomFetcher;
import org.ignition.blojsom.util.BlojsomConstants;
import org.ignition.blojsom.util.BlojsomUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Blojsom XML-RPC Handler for the MetaWeblog API
 *
 * MetaWeblog API pec can be found at http://www.xmlrpc.com/metaWeblogApi
 *
 * @author Mark Lussier
 * @version $Id: MetaWeblogAPIHandler.java,v 1.35 2003-07-13 01:01:05 czarneckid Exp $
 */
public class MetaWeblogAPIHandler extends AbstractBlojsomAPIHandler implements BlojsomConstants, BlojsomXMLRPCConstants {

    private static final String FETCHER_CATEGORY = "FETCHER_CATEGORY";
    private static final String FETCHER_PERMALINK = "FETCHER_PERMALINK";

    private static final String METAWEBLOG_UPLOAD_DIRECTORY_IP = "blojsom-extension-metaweblog-upload-directory";
    private static final String METAWEBLOG_ACCEPTED_TYPES_IP = "blojsom-extension-metaweblog-accepted-types";
    private static final String METAWEBLOG_STATIC_URL_PREFIX_IP = "blojsom-extension-metaweblog-static-url-prefix";

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
     * MetaWeblog API "url" key
     */
    private static final String MEMBER_URL = "url";

    public static final String API_PREFIX = "metaWeblog";

    private Blog _blog;
    private BlojsomFetcher _fetcher;
    private String _blogEntryExtension;
    private String _uploadDirectory;
    private HashMap _acceptedMimeTypes;
    private String _staticURLPrefix;

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
     * @throws BlojsomException If there is an error setting the blog instance or properties for the handler
     */
    public void setBlog(Blog bloginstance) throws BlojsomException {
        _blog = bloginstance;
        _blogEntryExtension = _blog.getBlogProperty(BLOG_XMLRPC_ENTRY_EXTENSION_IP);
        if (_blogEntryExtension == null || "".equals(_blogEntryExtension)) {
            _blogEntryExtension = DEFAULT_BLOG_XMLRPC_ENTRY_EXTENSION;
        }

        _uploadDirectory = bloginstance.getBlogProperty(METAWEBLOG_UPLOAD_DIRECTORY_IP);
        if (_uploadDirectory == null || "".equals(_uploadDirectory)) {
            _logger.error("No upload directory specified in property: " + METAWEBLOG_UPLOAD_DIRECTORY_IP);
            throw new BlojsomException("No upload directory specified in property: " + METAWEBLOG_UPLOAD_DIRECTORY_IP);
        }

        if (!_uploadDirectory.endsWith("/")) {
            _uploadDirectory += "/";
        }

        _acceptedMimeTypes = new HashMap(3);
        String acceptedMimeTypes = bloginstance.getBlogProperty(METAWEBLOG_ACCEPTED_TYPES_IP);
        if (acceptedMimeTypes != null && !"".equals(acceptedMimeTypes)) {
            String[] types = BlojsomUtils.parseCommaList(acceptedMimeTypes);
            for (int i = 0; i < types.length; i++) {
                String type = types[i];
                type = type.toLowerCase();
                _acceptedMimeTypes.put(type, type);
            }
        }

        _staticURLPrefix = bloginstance.getBlogProperty(METAWEBLOG_STATIC_URL_PREFIX_IP);
        if (_staticURLPrefix == null) {
            _logger.error("No static URL prefix specified in property: " + METAWEBLOG_STATIC_URL_PREFIX_IP);
            throw new BlojsomException("No static URL prefix specified in property: " + METAWEBLOG_STATIC_URL_PREFIX_IP);
        } else {
            _staticURLPrefix = BlojsomUtils.removeInitialSlash(_staticURLPrefix);
            if (!_staticURLPrefix.endsWith("/")) {
                _staticURLPrefix += "/";
            }
        }
    }

    /**
     * Set the {@link BlojsomFetcher} instance that will be used to fetch categories and entries
     *
     * @param fetcher {@link BlojsomFetcher} instance
     * @throws BlojsomException If there is an error in setting the fetcher
     */
    public void setFetcher(BlojsomFetcher fetcher) throws BlojsomException {
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
            } else {
                throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
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
     * Retrieves a given post from the blog
     *
     * @param postid Unique identifier of the post to be changed
     * @param userid Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @return Structure containing the minimal attributes for the MetaWeblog API getPost() method: title, link, and description
     * @throws XmlRpcException If the user was not authenticated correctly, if there was an I/O exception,
     * or if the entry permalink ID is invalid
     * @since blojsom 1.9.4
     */
    public Object getPost(String postid, String userid, String password) throws Exception {
        _logger.debug("getPost() Called =========[ SUPPORTED ]=====");
        _logger.debug("     PostId: " + postid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: " + password);

        if (_blog.checkAuthorization(userid, password)) {

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

                    Hashtable postcontent = new Hashtable(3);
                    postcontent.put(MEMBER_TITLE, entry.getTitle());
                    postcontent.put(MEMBER_LINK, entry.getPermalink());
                    postcontent.put(MEMBER_DESCRIPTION, entry.getDescription());

                    return postcontent;
                } else {
                    throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
                }
            } else {
                throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
            }
        } else {
            _logger.error("Failed to authenticate user [" + userid + "] with password [" + password + "]");
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
    }

    /**
     * Uploads an object to the blog to a specified directory
     *
     * @param blogid Unique identifier of the blog the post will be added to
     * @param userid Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @param struct Upload structure defined by the MetaWeblog API
     * @return Structure containing a link to the uploaded media object
     * @throws XmlRpcException If the user was not authenticated correctly, if there was an I/O exception,
     * or if the MIME type of the upload object is not accepted
     * @since blojsom 1.9.4
     */
    public Object newMediaObject(String blogid, String userid, String password, Hashtable struct) throws Exception {
        _logger.debug("newMediaObject() Called =[ SUPPORTED ]=====");
        _logger.debug("     BlogId: " + blogid);
        _logger.debug("     UserId: " + userid);
        _logger.debug("   Password: " + password);

        if (_blog.checkAuthorization(userid, password)) {
            String name = (String) struct.get(MEMBER_NAME);
            String type = (String) struct.get(MEMBER_TYPE);
            String bits = (String) struct.get(MEMBER_BITS);

            String extension = BlojsomUtils.getFileExtension(name);
            if (extension == null) {
                extension = "";
            }
            name = BlojsomUtils.normalize(name);
            name += "." + extension;
            byte[] decodedFile = Base64.decode(bits.getBytes());

            if (!_acceptedMimeTypes.containsKey(type.toLowerCase())) {
                try {
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(_uploadDirectory + name));
                    bos.write(decodedFile);
                    bos.close();

                    Hashtable returnStruct = new Hashtable(1);
                    String mediaURL = _blog.getBlogBaseURL() + _staticURLPrefix + name;
                    returnStruct.put(MEMBER_URL, mediaURL);

                    return returnStruct;
                } catch (IOException e) {
                    _logger.error(e);
                    throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
                }
            } else {
                _logger.error("MIME type not accepted. Received MIME type: " + type);
                throw new XmlRpcException(UNKNOWN_EXCEPTION, UNKNOWN_EXCEPTION_MSG);
            }
        } else {
            _logger.error("Failed to authenticate user [" + userid + "] with password [" + password + "]");
            throw new XmlRpcException(AUTHORIZATION_EXCEPTION, AUTHORIZATION_EXCEPTION_MSG);
        }
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