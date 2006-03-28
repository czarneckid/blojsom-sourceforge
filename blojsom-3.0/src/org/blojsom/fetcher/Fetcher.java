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
package org.blojsom.fetcher;

import org.blojsom.blog.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;

/**
 * Fetcher
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: Fetcher.java,v 1.7 2006-03-28 01:24:42 czarneckid Exp $
 */
public interface Fetcher {

    /**
     * Initialize this fetcher. This method only called when the fetcher is instantiated.
     *
     * @throws FetcherException If there is an error initializing the fetcher
     */
    public void init() throws FetcherException;

    /**
     * Return a new {@link Entry} instance
     *
     * @return Blog entry instance
     */
    public Entry newEntry();

    /**
     * Return a new {@link Comment} instance
     *
     * @return {@link Comment}
     */
    public Comment newComment();

    /**
     * Return a new {@link Trackback} instance
     *
     * @return {@link Trackback}
     */
    public Trackback newTrackback();

    /**
     * Return a new {@link Pingback} instance
     *
     * @return {@link Pingback}
     */
    public Pingback newPingback();

    /**
     * Return a new {@link Category} instance
     *
     * @return {@link Category}
     */
    public Category newCategory();

    /**
     * Return a new {@link Blog} instance
     *
     * @return {@link Blog}
     */
    public Blog newBlog();

    /**
     * Return a new {@link User} instance
     *
     * @return {@link User}
     */
    public User newUser();

    /**
     * Load the blog IDs
     *
     * @return List of blog IDs
     * @throws FetcherException If there is an error loading the blog IDs
     */
    public String[] loadBlogIDs() throws FetcherException;

    /**
     *
     * @param blogId
     * @return
     * @throws FetcherException
     */
    public Blog loadBlog(String blogId) throws FetcherException;

    /**
     * 
     * @param blog
     * @throws FetcherException
     */
    public void saveBlog(Blog blog) throws FetcherException;

    /**
     * Delete a blog
     *
     * @param blog {@link Blog}
     * @throws FetcherException If there is an error deleting the blog
     */
    public void deleteBlog(Blog blog) throws FetcherException;

    /**
     * Fetch a set of {@link Entry} objects.
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @param blog {@link Blog} instance
     * @param flavor Flavor
     * @param context Context
     * @return Blog entries retrieved for the particular request
     * @throws FetcherException If there is an error retrieving the blog entries for the request
     */
    public Entry[] fetchEntries(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Blog blog,
                                String flavor,
                                Map context) throws FetcherException;

    /**
     *
     * @param blog
     * @param categoryId
     * @return
     * @throws FetcherException
     */
    public Entry[] loadAllEntriesForCategory(Blog blog, Integer categoryId) throws FetcherException;

    /**
     *
     * @param blog
     * @param categoryId
     * @param limit
     * @return
     * @throws FetcherException
     */
    public Entry[] loadEntriesForCategory(Blog blog, Integer categoryId, Integer limit) throws FetcherException;

    /**
     *
     * @param blog
     * @param entryId
     * @return
     * @throws FetcherException
     */
    public Entry loadEntry(Blog blog, Integer entryId) throws FetcherException;

    /**
     * Load an {@link Entry} given a post slug
     *
     * @param blog {@link Blog}
     * @param postSlug Post slug
     * @return {@link Entry} for the given post slug
     * @throws FetcherException If an entry for the blog and post slug cannot be found
     */
    public Entry loadEntry(Blog blog, String postSlug) throws FetcherException;

    /**
     * Fetch a set of {@link Category} objects
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @param blog {@link Blog} instance
     * @param flavor Flavor
     * @param context Context
     * @return Blog categories retrieved for the particular request
     * @throws FetcherException If there is an error retrieving the blog categories for the request
     */
    public Category[] fetchCategories(HttpServletRequest httpServletRequest,
                                      HttpServletResponse httpServletResponse,
                                      Blog blog,
                                      String flavor,
                                      Map context) throws FetcherException;

    /**
     *
     * @param blog
     * @return
     * @throws FetcherException
     */
    public Category[] loadAllCategories(Blog blog) throws FetcherException;

    /**
     *
     * @param blog
     * @param categoryId
     * @return
     * @throws FetcherException
     */
    public Category loadCategory(Blog blog, Integer categoryId) throws FetcherException;

    /**
     *
     * @param blog
     * @param entry
     * @throws FetcherException
     */
    public void saveEntry(Blog blog, Entry entry) throws FetcherException;

    /**
     *
     * @param blog
     * @param entry
     * @throws FetcherException
     */
    public void loadEntry(Blog blog, Entry entry) throws FetcherException;

    /**
     *
     * @param blog
     * @param entry
     * @throws FetcherException
     */
    public void deleteEntry(Blog blog, Entry entry) throws FetcherException;

    /**
     *
     * @param blog
     * @param category
     * @throws FetcherException
     */
    public void saveCategory(Blog blog, Category category) throws FetcherException;

    /**
     *
     * @param blog
     * @param category
     * @throws FetcherException
     */
    public void loadCategory(Blog blog, Category category) throws FetcherException;

    /**
     *
     * @param blog
     * @param category
     * @throws FetcherException
     */
    public void deleteCategory(Blog blog, Category category) throws FetcherException;

    /**
     *
     * @param blog
     * @param comment
     * @throws FetcherException
     */
    public void saveComment(Blog blog, Comment comment) throws FetcherException;

    /**
     *
     * @param blog
     * @param comment
     * @throws FetcherException
     */
    public void loadComment(Blog blog, Comment comment) throws FetcherException;

    /**
     *
     * @param blog
     * @param comment
     * @throws FetcherException
     */
    public void deleteComment(Blog blog, Comment comment) throws FetcherException;

    /**
     * Load the recent comments for a blog
     *
     * @param blog {@link Blog}
     * @throws FetcherException If there is an error retrieving the recent comments
     */
    public List loadRecentComments(Blog blog) throws FetcherException;

    /**
     *
     * @param blog
     * @param trackback
     * @throws FetcherException
     */
    public void saveTrackback(Blog blog, Trackback trackback) throws FetcherException;

    /**
     *
     * @param blog
     * @param trackback
     * @throws FetcherException
     */
    public void loadTrackback(Blog blog, Trackback trackback) throws FetcherException;

    /**
     *
     * @param blog
     * @param trackback
     * @throws FetcherException
     */
    public void deleteTrackback(Blog blog, Trackback trackback) throws FetcherException;

    /**
     *
     * @param blog
     * @param pingback
     * @throws FetcherException
     */
    public void savePingback(Blog blog, Pingback pingback) throws FetcherException;

    /**
     *
     * @param blog
     * @param pingback
     * @throws FetcherException
     */
    public void loadPingback(Blog blog, Pingback pingback) throws FetcherException;

    /**
     * Load a pingback given the source URI and target URI
     *
     * @param blog {@link Blog}
     * @param sourceURI Source URI
     * @param targetURI Target URI
     * @return {@link Pingback} given the source and target URIs or <code>null</code> if not found
     * @throws FetcherException If there was an erorr loading the pingback
     */
    public Pingback loadPingback(Blog blog, String sourceURI, String targetURI) throws FetcherException;

    /**
     *
     * @param blog
     * @param pingback
     * @throws FetcherException
     */
    public void deletePingback(Blog blog, Pingback pingback) throws FetcherException;

    /**
     * Retrieve the users for a given blog
     *
     * @param blog {@link Blog}
     * @return List of {@link User}s for a blog
     */
    public User[] getUsers(Blog blog);

    /**
     * Load a {@link User} from a blog
     *
     * @param blog      {@link Blog}
     * @param userLogin Login ID
     * @throws FetcherException If there is an error loading the {@link User} from the blog
     */
    public User loadUser(Blog blog, String userLogin) throws FetcherException;

    /**
     * Load a given {@link User} from a blog given their ID
     *
     * @param blog {@link Blog}
     * @param userID User ID
     * @return {@link User}
     * @throws FetcherException If there is an error loading the user
     */
    public User loadUser(Blog blog, Integer userID) throws FetcherException;

    /**
     * Save a given {@link User} to the blog
     *
     * @param blog {@link Blog}
     * @param user {@link User}
     * @return {@link User}
     * @throws FetcherException If there is an error saving the user to the blog
     */
    public User saveUser(Blog blog, User user) throws FetcherException;

    /**
     * Delete a given user from a blog
     *
     * @param blog {@link Blog}
     * @param userID User ID
     * @throws FetcherException If there is an error deleting the user from the blog
     */
    public void deleteUser(Blog blog, Integer userID) throws FetcherException;

    /**
     * Called when {@link org.blojsom.servlet.BlojsomServlet} is taken out of service
     *
     * @throws FetcherException If there is an error in finalizing this fetcher
     */
    public void destroy() throws FetcherException;
}
