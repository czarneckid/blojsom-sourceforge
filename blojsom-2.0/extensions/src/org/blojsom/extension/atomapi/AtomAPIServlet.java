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
package org.blojsom.extension.atomapi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.BlojsomException;
import org.blojsom.blog.Blog;
import org.blojsom.blog.BlogCategory;
import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;
import org.blojsom.fetcher.BlojsomFetcher;
import org.blojsom.fetcher.BlojsomFetcherException;
import org.blojsom.servlet.BlojsomBaseServlet;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.BlojsomMetaDataConstants;
import org.intabulas.sandler.Sandler;
import org.intabulas.sandler.api.SearchResults;
import org.intabulas.sandler.api.impl.SearchResultsImpl;
import org.intabulas.sandler.authentication.AtomAuthentication;
import org.intabulas.sandler.elements.Entry;
import org.intabulas.sandler.exceptions.FeedMarshallException;
import org.intabulas.sandler.introspection.Introspection;
import org.intabulas.sandler.introspection.impl.IntrospectionImpl;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

/**
 * AtomAPIServlet
 *
 * Implementation of J.C. Gregorio's AtomAPI
 * <a href="http://bitworking.org/rfc/draft-gregorio-08.xml">http://bitworking.org/rfc/draft-gregorio-08.xml</a>
 *
 * @author Mark Lussier
 * @since blojsom 2.0
 * @version $Id: AtomAPIServlet.java,v 1.24 2004-01-05 22:29:58 czarneckid Exp $
 */
public class AtomAPIServlet extends BlojsomBaseServlet implements BlojsomConstants, BlojsomMetaDataConstants, AtomConstants {

    /**
     * Logger instance
     */
    private Log _logger = LogFactory.getLog(AtomAPIServlet.class);


    /**
     * Default constructor
     */
    public AtomAPIServlet() {
    }


    /**
     * Initialize the blojsom AtomAPI servlet
     *
     * @param servletConfig Servlet configuration information
     * @throws ServletException If there is an error initializing the servlet
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        configureBlojsom(servletConfig);
        configureAuthorization(servletConfig);

        _logger.info("AtomAPI initialized");
    }


    /**
     * Is the request from an authorized poster to this blog?
     *
     * @param httpServletRequest Request
     * @return a boolean indicating if the user was authorized or not
     */
    private boolean isAuthorized(Blog blog, HttpServletRequest httpServletRequest, String verb) {
        boolean result = false;

        if (httpServletRequest.getHeader(ATOMHEADER_AUTHORIZATION) != null) {
            AtomAuthentication auth = new AtomAuthentication(httpServletRequest.getHeader(ATOMHEADER_AUTHORIZATION));
            Map authMap = blog.getAuthorization();
            if (authMap.containsKey(auth.getUsername())) {
                result = auth.authenticate((String) authMap.get(auth.getUsername()), verb);
            } else {
                _logger.info("Unable to locate user [" + auth.getUsername() + "] in authorization table");
            }
//            if (result) {
//                String sanityCheck = httpServletRequest.getHeader(HEADER_AUTHORIZATION);
//                if ((auth != null)) {
//                    result = sanityCheck.startsWith(ATOM_AUTH_PREFIX);
//                }
//            }
            if (!result) {
                _logger.info("Unable to authenticate user [" + auth.getUsername() + "]");
            }
        }
        return result;
    }


    /**
     * Send back failed authorization response
     *
     * @param httpServletResponse Response
     * @param user BlogUser instance
     */
    private void sendAuthenticationRequired(HttpServletResponse httpServletResponse, BlogUser user) {
        httpServletResponse.setContentType(CONTENTTYPE_HTML);

        // Generate a NextNonce and add it to the header
        String nonce = AtomUtils.generateNextNonce(user);
        String relm = MessageFormat.format(AUTHENTICATION_REALM, new Object[]{nonce});

        // send the NextNonce as part of a WWW-Authenticate header
        httpServletResponse.setHeader(HEADER_WWWAUTHENTICATE, relm);

        httpServletResponse.setStatus(401);
    }


    /**
     * Is this an AtomAPI search request?
     *
     * @param request Request
     * @return <code>true</code> if the request is a search request, <code>false</code> otherwise
     */
    private boolean isSearchRequest(HttpServletRequest request) {
        Map paramMap = request.getParameterMap();

        // Looks for the existence of specific params and also checks the QueryString for a name only param
        return (paramMap.containsKey(KEY_ATOMALL) || paramMap.containsKey(KEY_ATOMLAST) || paramMap.containsKey("start-range")
                || (request.getQueryString().indexOf(KEY_ATOMALL) != -1));
    }


    /**
     * Process the Search request
     *
     * @param request Request
     * @param category Blog category
     * @param blog Blog instance
     * @param blogUser BlogUser instance
     * @return the search result as a String
     *
     * todo just hable BlogUser since we can get the Blog instance from it
     */
    private String processSearchRequest(HttpServletRequest request, String category, Blog blog, BlogUser blogUser) {
        String result = null;
        Map paramMap = request.getParameterMap();
        int numPosts = -1;
        // Did they specify how many entries?
        if (paramMap.containsKey(KEY_ATOMLAST)) {
            try {
                numPosts = Integer.parseInt(((String []) paramMap.get(KEY_ATOMLAST))[0]);
            } catch (NumberFormatException e) {
                numPosts = -1;
            }
        }

        Map fetchMap = new HashMap();
        BlogCategory blogCategory = _fetcher.newBlogCategory();
        blogCategory.setCategory(category);
        blogCategory.setCategoryURL(blog.getBlogURL() + category);
        fetchMap.put(BlojsomFetcher.FETCHER_CATEGORY, blogCategory);
        fetchMap.put(BlojsomFetcher.FETCHER_NUM_POSTS_INTEGER, new Integer(numPosts));
        try {
            BlogEntry[] entries = _fetcher.fetchEntries(fetchMap, blogUser);

            if (entries != null && entries.length > 0) {
                SearchResults searchResult = new SearchResultsImpl();
                for (int x = 0; x < entries.length; x++) {
                    BlogEntry entry = entries[x];
                    Entry atomentry = AtomUtils.fromBlogEntrySearch(blog, blogUser, entry, ATOM_SERVLETMAPPING);
                    searchResult.addEntry(atomentry);
                }

                result = searchResult.toString();
            }
        } catch (BlojsomFetcherException e) {
            _logger.error(e.getLocalizedMessage(), e);
        }

        return result;
    }


    /**
     * Handle a Delete Entry message
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @throws ServletException If there is an error processing the request
     * @throws IOException If there is an error during I/O
     */
    protected void doDelete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        Blog blog = null;
        BlogUser blogUser = null;

        String permalink = BlojsomUtils.getRequestValue(PERMALINK_PARAM, httpServletRequest);
        String category = BlojsomUtils.getCategoryFromPath(httpServletRequest.getPathInfo());
        String user = BlojsomUtils.getUserFromPath(httpServletRequest.getPathInfo());

        _logger.info("AtomAPI Delete Called ================================================");
        _logger.info("       Path: " + httpServletRequest.getPathInfo());
        _logger.info("       User: " + user);
        _logger.info("   Category: " + category);
        _logger.info("  Permalink: " + permalink);

        if (BlojsomUtils.checkNullOrBlank(user)) {
            user = _blojsomConfiguration.getDefaultUser();
        }
        blogUser = (BlogUser) _blojsomConfiguration.getBlogUsers().get(user);
        blog = blogUser.getBlog();

        if (isAuthorized(blog, httpServletRequest, "DELETE")) {
            _logger.info("Fetching " + permalink);
            Map fetchMap = new HashMap();
            BlogCategory blogCategory = _fetcher.newBlogCategory();
            blogCategory.setCategory(category);
            blogCategory.setCategoryURL(blog.getBlogURL() + category);
            fetchMap.put(BlojsomFetcher.FETCHER_CATEGORY, blogCategory);
            fetchMap.put(BlojsomFetcher.FETCHER_PERMALINK, permalink);
            try {
                BlogEntry[] entries = _fetcher.fetchEntries(fetchMap, blogUser);
                if (entries != null && entries.length > 0) {
                    BlogEntry entry = entries[0];
                    entry.delete(blogUser);
                }

                // Okay now we generate a new NextOnce value, just for saftey sake and shove in into the response
                String nonce = AtomUtils.generateNextNonce(blogUser);
                httpServletResponse.setHeader(ATOMHEADER_AUTHENTICATION_INFO, ATOM_TOKEN_NEXTNONCE + nonce + "\"");
                httpServletResponse.setStatus(200);
            } catch (BlojsomFetcherException e) {
                _logger.error(e.getLocalizedMessage(), e);
                httpServletResponse.setStatus(404);
            } catch (BlojsomException e) {
                _logger.error(e.getLocalizedMessage(), e);
                httpServletResponse.setStatus(404);
            }
        } else {
            sendAuthenticationRequired(httpServletResponse, blogUser);
        }
    }


    /**
     * Creates an AtomAPI Introspection response
     *
     * @param blog Blog Instance
     * @param user BlogUser Instance
     * @return URL appropriate for introspection
     *
     * todo just hable BlogUser since we can get the Blog instance from it
     */
    private String createIntrospectionResponse(Blog blog, BlogUser user) {
        String atomuri = blog.getBlogBaseURL() + ATOM_SERVLETMAPPING + user.getId() + "/";

        Introspection introspection = new IntrospectionImpl();
        introspection.setSearchUrl(atomuri);
        introspection.setCreateUrl(atomuri);

        return introspection.toString();
    }


    /**
     * Process a Get Entry message
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @throws ServletException If there is an error processing the request
     * @throws IOException If there is an error during I/O
     */
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        Blog blog = null;
        BlogUser blogUser = null;
        String blogEntryExtension;

        String permalink = BlojsomUtils.getRequestValue(PERMALINK_PARAM, httpServletRequest);
        String category = BlojsomUtils.getCategoryFromPath(httpServletRequest.getPathInfo());
        String user = BlojsomUtils.getUserFromPath(httpServletRequest.getPathInfo());

        if (BlojsomUtils.checkNullOrBlank(user)) {
            user = _blojsomConfiguration.getDefaultUser();
        }
        blogUser = (BlogUser) _blojsomConfiguration.getBlogUsers().get(user);
        blog = blogUser.getBlog();
        blogEntryExtension = blog.getBlogProperty(BLOG_ATOMAPI_ENTRY_EXTENSION_IP);
        if (BlojsomUtils.checkNullOrBlank(blogEntryExtension)) {
            blogEntryExtension = DEFAULT_BLOG_ATOMAPI_ENTRY_EXTENSION;
        }

        _logger.info("AtomAPI GET Called ==================================================");
        _logger.info("       Path: " + httpServletRequest.getPathInfo());
        _logger.info("       User: " + user);
        _logger.info("   Category: " + category);
        _logger.info("  Permalink: " + permalink);
        _logger.info("      Query: " + httpServletRequest.getQueryString());

        boolean hasParams = ((httpServletRequest.getParameterMap().size() > 0) || httpServletRequest.getQueryString() != null);

        // NOTE: Assumes that the getPathInfo() returns only category data
        
        //if (isAuthorized(blog, httpServletRequest)) {

        String content = null;
        if (!hasParams) {
            content = createIntrospectionResponse(blog, blogUser);
            httpServletResponse.setContentType(CONTENTTYPE_XML);

        } else if (isSearchRequest(httpServletRequest)) {
            httpServletResponse.setContentType(CONTENTTYPE_XML);
            if (isSearchRequest(httpServletRequest)) {
                content = processSearchRequest(httpServletRequest, category, blog, blogUser);
            }
        } else {
            _logger.info("Fetching " + permalink);
            Map fetchMap = new HashMap();
            BlogCategory blogCategory = _fetcher.newBlogCategory();
            blogCategory.setCategory(category);
            blogCategory.setCategoryURL(blog.getBlogURL() + category);
            fetchMap.put(BlojsomFetcher.FETCHER_CATEGORY, blogCategory);
            fetchMap.put(BlojsomFetcher.FETCHER_PERMALINK, permalink);
            try {
                BlogEntry[] entries = _fetcher.fetchEntries(fetchMap, blogUser);

                if (entries != null && entries.length > 0) {
                    BlogEntry entry = entries[0];
                    Entry atomentry = AtomUtils.fromBlogEntry(blog, blogUser, entry);
                    content = atomentry.toString();
                    httpServletResponse.setContentType(CONTENTTYPE_ATOM);
                }
            } catch (BlojsomFetcherException e) {
                _logger.error(e);
                httpServletResponse.setStatus(404);
            }
        }

        if (content != null) {
            String nonce = AtomUtils.generateNextNonce(blogUser);
            httpServletResponse.setHeader(ATOMHEADER_AUTHENTICATION_INFO, ATOM_TOKEN_NEXTNONCE + nonce + "\"");
            httpServletResponse.setStatus(200);
            httpServletResponse.setContentLength(content.length());
            OutputStreamWriter osw = new OutputStreamWriter(httpServletResponse.getOutputStream(), UTF8);
            osw.write(content);
            osw.flush();
        } else {
            httpServletResponse.setStatus(404);
        }
//
//        } else {
//            sendAuthenticationRequired(httpServletResponse);
//        }
    }

    /**
     * Handle a Post Entry request
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @throws ServletException If there is an error processing the request
     * @throws IOException If there is an error during I/O
     */
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        Blog blog = null;
        BlogUser blogUser = null;
        String blogEntryExtension = DEFAULT_BLOG_ATOMAPI_ENTRY_EXTENSION;

        String permalink = BlojsomUtils.getRequestValue(PERMALINK_PARAM, httpServletRequest);
        String category = BlojsomUtils.getCategoryFromPath(httpServletRequest.getPathInfo());
        String user = BlojsomUtils.getUserFromPath(httpServletRequest.getPathInfo());

        _logger.info("AtomAPI POST Called =================================================");
        _logger.info("       Path: " + httpServletRequest.getPathInfo());
        _logger.info("       User: " + user);
        _logger.info("   Category: " + category);
        _logger.info("  Permalink: " + permalink);

        if (BlojsomUtils.checkNullOrBlank(user)) {
            user = _blojsomConfiguration.getDefaultUser();
        }
        blogUser = (BlogUser) _blojsomConfiguration.getBlogUsers().get(user);
        blog = blogUser.getBlog();
        blogEntryExtension = blog.getBlogProperty(BLOG_ATOMAPI_ENTRY_EXTENSION_IP);
        if (BlojsomUtils.checkNullOrBlank(blogEntryExtension)) {
            blogEntryExtension = DEFAULT_BLOG_ATOMAPI_ENTRY_EXTENSION;
        }

        if (isAuthorized(blog, httpServletRequest, "POST")) {

            // Quick verify that the category is valid
            File blogCategory = getBlogCategoryDirectory(blog, category);
            if (blogCategory.exists() && blogCategory.isDirectory()) {

                try {
                    Entry atomEntry = Sandler.unmarshallEntry(httpServletRequest.getInputStream());

                    String filename = getBlogEntryFilename(atomEntry.getContent(0).getBody(), blogEntryExtension);
                    String outputfile = blogCategory.getAbsolutePath() + File.separator + filename;

                    File sourceFile = new File(outputfile);
                    BlogEntry entry = _fetcher.newBlogEntry();
                    Map attributeMap = new HashMap();
                    Map blogEntryMetaData = new HashMap();
                    attributeMap.put(SOURCE_ATTRIBUTE, sourceFile);
                    entry.setAttributes(attributeMap);
                    entry.setCategory(category);
                    entry.setDescription(atomEntry.getContent(0).getBody());
                    entry.setDate(atomEntry.getCreated());
                    entry.setTitle(atomEntry.getTitle().getBody());

                    if (atomEntry.getAuthor() != null) {
                        blogEntryMetaData.put(BLOG_ENTRY_METADATA_AUTHOR, atomEntry.getAuthor().getName());
                    } else {
                        blogEntryMetaData.put(BLOG_ENTRY_METADATA_AUTHOR, "AtomAPI");
                    }
                    blogEntryMetaData.put(BLOG_ENTRY_METADATA_TIMESTAMP, new Long(new Date().getTime()).toString());
                    entry.setMetaData(blogEntryMetaData);
                    entry.save(blogUser);

                    httpServletResponse.setContentType(CONTENTTYPE_HTML);
                    httpServletResponse.setHeader(HEADER_LOCATION, entry.getEscapedLink());

                    String nonce = AtomUtils.generateNextNonce(blogUser);
                    httpServletResponse.setHeader(ATOMHEADER_AUTHENTICATION_INFO, ATOM_TOKEN_NEXTNONCE + nonce + "\"");
                    httpServletResponse.setStatus(201);
                } catch (FeedMarshallException e) {
                    _logger.error(e.getLocalizedMessage(), e);
                    httpServletResponse.setStatus(404);
                } catch (BlojsomException e) {
                    _logger.error(e);
                    httpServletResponse.setStatus(404);
                }
            }
        } else {
            sendAuthenticationRequired(httpServletResponse, blogUser);
        }
    }

    /**
     * Handle a Put Entry request
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @throws ServletException If there is an error processing the request
     * @throws IOException If there is an error during I/O
     */
    protected void doPut(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        Blog blog = null;
        BlogUser blogUser = null;

        String permalink = BlojsomUtils.getRequestValue(PERMALINK_PARAM, httpServletRequest);
        String category = BlojsomUtils.getCategoryFromPath(httpServletRequest.getPathInfo());
        String user = BlojsomUtils.getUserFromPath(httpServletRequest.getPathInfo());

        _logger.info("AtomAPI PUT Called ==================================================");
        _logger.info("       Path: " + httpServletRequest.getPathInfo());
        _logger.info("       User: " + user);
        _logger.info("   Category: " + category);
        _logger.info("  Permalink: " + permalink);

        if (BlojsomUtils.checkNullOrBlank(user)) {
            user = _blojsomConfiguration.getDefaultUser();
        }
        blogUser = (BlogUser) _blojsomConfiguration.getBlogUsers().get(user);
        blog = blogUser.getBlog();

        if (isAuthorized(blog, httpServletRequest, "PUT")) {

            Map fetchMap = new HashMap();
            BlogCategory blogCategory = _fetcher.newBlogCategory();
            blogCategory.setCategory(category);
            blogCategory.setCategoryURL(blog.getBlogURL() + category);
            fetchMap.put(BlojsomFetcher.FETCHER_CATEGORY, blogCategory);
            fetchMap.put(BlojsomFetcher.FETCHER_PERMALINK, permalink);
            try {
                BlogEntry[] entries = _fetcher.fetchEntries(fetchMap, blogUser);

                if (entries != null && entries.length > 0) {

                    Entry atomEntry = Sandler.unmarshallEntry(httpServletRequest.getInputStream());

                    BlogEntry entry = entries[0];
                    Map blogEntryMetaData = entry.getMetaData();
                    entry.setCategory(category);
                    entry.setDescription(atomEntry.getContent(0).getBody());
                    entry.setTitle(atomEntry.getTitle().getBody());
                    if (atomEntry.getAuthor() != null) {
                        blogEntryMetaData.put(BLOG_ENTRY_METADATA_AUTHOR, atomEntry.getAuthor().getName());
                    }
                    entry.setMetaData(blogEntryMetaData);
                    entry.save(blogUser);

                    String nonce = AtomUtils.generateNextNonce(blogUser);
                    httpServletResponse.setHeader(ATOMHEADER_AUTHENTICATION_INFO, ATOM_TOKEN_NEXTNONCE + nonce + "\"");

                    httpServletResponse.setStatus(204);
                } else {
                    _logger.info("Unable to fetch " + permalink);
                }
            } catch (BlojsomFetcherException e) {
                _logger.error(e);
                httpServletResponse.setStatus(404);
            } catch (BlojsomException e) {
                _logger.error(e);
                httpServletResponse.setStatus(404);
            } catch (FeedMarshallException e) {
                _logger.error(e);
                httpServletResponse.setStatus(404);
            }
        } else {
            sendAuthenticationRequired(httpServletResponse, blogUser);
        }
    }


    /**
     * Called when removing the servlet from the servlet container
     */
    public void destroy() {
        try {
            _fetcher.destroy();
        } catch (BlojsomFetcherException e) {
            _logger.error(e);
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
    protected File getBlogCategoryDirectory(Blog blog, String categoryName) {
        File blogCategory = new File(blog.getBlogHome() + BlojsomUtils.removeInitialSlash(categoryName));
        if (blogCategory.exists() && blogCategory.isDirectory()) {
            return blogCategory;
        } else {
            return new File(blog.getBlogHome() + "/");
        }
    }


    /**
     * Return a filename appropriate for the blog entry content
     *
     * @param content Blog entry content
     * @return Filename for the new blog entry
     */
    protected String getBlogEntryFilename(String content, String extension) {
        String hashable = content;

        if (content.length() > MAX_HASHABLE_LENGTH) {
            hashable = hashable.substring(0, MAX_HASHABLE_LENGTH);
        }

        String baseFilename = BlojsomUtils.digestString(hashable).toUpperCase();
        String filename = baseFilename + extension;
        return filename;
    }
}

