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
import org.apache.xmlrpc.Base64;
import org.blojsom.BlojsomException;
import org.blojsom.blog.Blog;
import org.blojsom.blog.BlogCategory;
import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;
import org.blojsom.fetcher.BlojsomFetcherException;
import org.blojsom.servlet.BlojsomBaseServlet;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.intabulas.sandler.Sandler;
import org.intabulas.sandler.elements.Entry;
import org.intabulas.sandler.exceptions.FeedMarshallException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * AtomAPIServlet
 *
 * Implementation of J.C. Gregorio's AtomAPI
 * <a href="http://bitworking.org/rfc/draft-gregorio-08.xml">http://bitworking.org/rfc/draft-gregorio-08.xml</a>
 *
 * @author Mark Lussier
 * @since blojsom 2.0
 * @version $Id: AtomAPIServlet.java,v 1.8 2003-09-09 23:46:53 intabulas Exp $
 */
public class AtomAPIServlet extends BlojsomBaseServlet implements BlojsomConstants, AtomConstants {

    private static final String FETCHER_PERMALINK = "FETCHER_PERMALINK";
    private static final String FETCHER_CATEGORY = "FETCHER_CATEGORY";

    private static final String CONTENTTYPE_ATOM = "application/x.atom+xml";
    private static final String CONTENTTYPE_XML = "application/xml";

    private static final String COMMAND_COMMAND = "command";

    private Log _logger = LogFactory.getLog(AtomAPIServlet.class);

    private static final String ATOM_NAMESPACE="\"http://purl.org/atom/ns#\"";

    /**
     * Public Constructor
     */
    public AtomAPIServlet() {
    }


    /**
     * Initialize the blojsom AtomAPI servlet
     *
     * @param servletConfig Servlet configuration information
     * @throws javax.servlet.ServletException If there is an error initializing the servlet
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        configureBlojsom(servletConfig);
        configureAuthorization(servletConfig);

        _logger.info("AtomAPI initialized");

    }


    /**
     * Extract the Atom command from the post stream
     * @param pathInfo
     * @return
     */
    private String extractCommand(String pathInfo) {
        String result = null;
        int index = pathInfo.indexOf("command=");

        if ( index != -1) {
            result = pathInfo.substring(index);


        }

        return result;

    }


    /**
     * Decode the HTTP Authorization Header
     * @param httpServletRequest
     * @return
     */
    private String extractAuthorization(HttpServletRequest httpServletRequest) {
        String result = null;
        String auth = httpServletRequest.getHeader(HEADER_AUTHORIZATION);

        if ((auth != null)) {
            if (auth.startsWith(BASE64_AUTH_PREFIX)) {
                auth = auth.substring(BASE64_AUTH_PREFIX.length());
            }
            result = new String(Base64.decode(auth.getBytes()));
        }
        return result;
    }


    /**
     *
     * @param httpServletRequest Request
     * @return
     */
    private boolean isAuthorized(Blog blog, HttpServletRequest httpServletRequest) {
        boolean result = false;
        String realmAuth = extractAuthorization(httpServletRequest);
        if (realmAuth != null && !"".equals(realmAuth)) {
            int pos = realmAuth.indexOf(":");
            if (pos > 0) {
                String username = realmAuth.substring(0, pos);
                String password = realmAuth.substring(pos + 1);
                result = blog.checkAuthorization(username, password);
                if (!result) {
                    _logger.info("Unable to authenticate user [" + username + "] w/password [" + password + ']');
                }

            }

        }
        return result;
    }


    private void sendAuthenticationRequired(HttpServletResponse httpServletResponse) {
        httpServletResponse.setContentType("text/html");
        httpServletResponse.setHeader(HEADER_AUTHCHALLENGE, AUTHENTICATION_REALM);
        httpServletResponse.setStatus(401);
    }


    /**
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


        _logger.info("AtomAPI Delete Called =====[ SUPPORTED ]=====");
        _logger.info("       Path: " + httpServletRequest.getPathInfo());
        _logger.info("       User: " + user);
        _logger.info("   Category: " + category);
        _logger.info("  Permalink: " + permalink);

        if (user == null || "".equals(user)) {
            user = _defaultUser;
        }
        blogUser = (BlogUser) _users.get(user);
        blog = blogUser.getBlog();


        if (isAuthorized(blog, httpServletRequest)) {


        } else {
            sendAuthenticationRequired(httpServletResponse);
        }

    }


    private String createIntrospectionDocument(Blog blog, BlogUser user) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("<introspection xmlns=").append( ATOM_NAMESPACE).append(">");

        buffer.append("<create-entry>").append(blog.getBlogBaseURL() + "/atomapi/").append(user.getId()).append("/");
        buffer.append(COMMAND_COMMAND).append("=create").append("</create-entry>");

        buffer.append("<search-entries>").append(blog.getBlogBaseURL() + "/atomapi/").append(user.getId()).append("/");
        buffer.append(COMMAND_COMMAND).append("=search").append("</search-entries>");

        buffer.append("</introspection>");

        return buffer.toString();
    }


    /**
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

        String command = extractCommand(httpServletRequest.getPathInfo());

        if (user == null || "".equals(user)) {
            user = _defaultUser;
        }
        blogUser = (BlogUser) _users.get(user);
        blog = blogUser.getBlog();
        blogEntryExtension = blog.getBlogProperty(BLOG_ATOMAPI_ENTRY_EXTENSION_IP);
        if (blogEntryExtension == null || "".equals(blogEntryExtension)) {
            blogEntryExtension = DEFAULT_BLOG_ATOMAPI_ENTRY_EXTENSION;
        }


        _logger.info("AtomAPI GET Called =====[ SUPPORTED ]=====");
        _logger.info("       Path: " + httpServletRequest.getPathInfo());
        _logger.info("       User: " + user);
        _logger.info("   Category: " + category);
        _logger.info("  Permalink: " + permalink);
        _logger.info("    Command: " + command);



        // NOTE: Assumes that the getPathInfo() returns only category data
        
        if (isAuthorized(blog, httpServletRequest)) {

            String content = null;
            if ( command == null ) {
                content = createIntrospectionDocument(blog, blogUser);
                httpServletResponse.setContentType(CONTENTTYPE_XML);
            } else if (command.equalsIgnoreCase("search")) {
                httpServletResponse.setContentType(CONTENTTYPE_ATOM);

            }



            httpServletResponse.setStatus(200);
            httpServletResponse.setContentLength(content.length());
            OutputStreamWriter osw = new OutputStreamWriter(httpServletResponse.getOutputStream(), "UTF-8");
            osw.write(content);
            osw.flush();

//
//
//            Map fetchMap = new HashMap();
//            BlogCategory blogCategory = _fetcher.newBlogCategory();
//            blogCategory.setCategory(category);
//            blogCategory.setCategoryURL(blog.getBlogURL() + category);
//            fetchMap.put(FETCHER_CATEGORY, blogCategory);
//            fetchMap.put(FETCHER_PERMALINK, permalink);
//            try {
//                BlogEntry[] entries = _fetcher.fetchEntries(fetchMap, blogUser);
//
//                if (entries != null && entries.length > 0) {
//                    BlogEntry entry = entries[0];
//                    Entry atomentry = AtomUtils.fromBlogEntry(blog, entry);
//                    String content = atomentry.toString();
//                    httpServletResponse.setContentType(CONTENTTYPE_ATOM);
//                    httpServletResponse.setStatus(200);
//                    httpServletResponse.setContentLength(content.length());
//                    OutputStreamWriter osw = new OutputStreamWriter(httpServletResponse.getOutputStream(), "UTF-8");
//                    osw.write(content);
//                    osw.flush();
//                }
//            } catch (BlojsomFetcherException e) {
//                _logger.error(e);
//                httpServletResponse.setStatus(404);
//            }
        } else {
            sendAuthenticationRequired(httpServletResponse);
        }


    }

    /**
     * Handle HTTP POST
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


        _logger.info("AtomAPI POST Called =====[ SUPPORTED ]=====");
        _logger.info("       Path: " + httpServletRequest.getPathInfo());
        _logger.info("       User: " + user);
        _logger.info("   Category: " + category);
        _logger.info("  Permalink: " + permalink);

        if (user == null || "".equals(user)) {
            user = _defaultUser;
        }
        blogUser = (BlogUser) _users.get(user);
        blog = blogUser.getBlog();
        blogEntryExtension = blog.getBlogProperty(BLOG_ATOMAPI_ENTRY_EXTENSION_IP);
        if (blogEntryExtension == null || "".equals(blogEntryExtension)) {
            blogEntryExtension = DEFAULT_BLOG_ATOMAPI_ENTRY_EXTENSION;
        }


        if (isAuthorized(blog, httpServletRequest)) {

            String result = null;

            // Quick verify that the category is valid
            File blogCategory = getBlogCategoryDirectory(blog, category);
            if (blogCategory.exists() && blogCategory.isDirectory()) {

                try {
                    Entry atomEntry = Sandler.unmarshallEntry(httpServletRequest.getInputStream());

                    String filename = getBlogEntryFilename(atomEntry.getContent(0).getBody(), blogEntryExtension);
                    String outputfile = blogCategory.getAbsolutePath() + File.separator + filename;
                    String postid = category + "?" + PERMALINK_PARAM + "=" + filename;


                    File sourceFile = new File(outputfile);
                    BlogEntry entry = _fetcher.newBlogEntry();
                    Map attributeMap = new HashMap();
                    Map blogEntryMetaData = new HashMap();
                    attributeMap.put(SOURCE_ATTRIBUTE, sourceFile);
                    entry.setAttributes(attributeMap);
                    entry.setCategory(category);
                    entry.setDescription(atomEntry.getContent(0).getBody());
                    entry.setDate(atomEntry.getCreated());
                    entry.setTitle(atomEntry.getTitle());

                    blogEntryMetaData.put(BLOG_METADATA_ENTRY_AUTHOR, atomEntry.getAuthor().getName());
                    entry.setMetaData(blogEntryMetaData);
                    entry.save(blog);
                    result = postid;

                    httpServletResponse.setContentType("text/html");
                    httpServletResponse.setHeader(HEADER_LOCATION, entry.getEscapedLink());
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
            sendAuthenticationRequired(httpServletResponse);
        }


    }

    /**
     * Handle HTTP PUT
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


        _logger.info("AtomAPI PUT Called =====[ SUPPORTED ]=====");
        _logger.info("       Path: " + httpServletRequest.getPathInfo());
        _logger.info("       User: " + user);
        _logger.info("   Category: " + category);
        _logger.info("  Permalink: " + permalink);

        if (user == null || "".equals(user)) {
            user = _defaultUser;
        }
        blogUser = (BlogUser) _users.get(user);
        blog = blogUser.getBlog();

        if (isAuthorized(blog, httpServletRequest)) {

        } else {
            sendAuthenticationRequired(httpServletResponse);
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

