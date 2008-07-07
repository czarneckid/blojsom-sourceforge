/**
 * Copyright (c) 2003-2008, David A. Czarnecki
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
import java.util.Date;

/**
 * Fetcher
 *
 * @author David Czarnecki
 * @version $Id: Fetcher.java,v 1.23 2008-07-07 19:55:09 czarneckid Exp $
 * @since blojsom 3.0
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
     * @return {@link Comment} comment
     */
    public Comment newComment();

    /**
     * Return a new {@link Trackback} instance
     *
     * @return {@link Trackback} trackback
     */
    public Trackback newTrackback();

    /**
     * Return a new {@link Pingback} instance
     *
     * @return {@link Pingback} pingback
     */
    public Pingback newPingback();

    /**
     * Return a new {@link Category} instance
     *
     * @return {@link Category} category
     */
    public Category newCategory();

    /**
     * Return a new {@link Blog} instance
     *
     * @return {@link Blog} blog
     */
    public Blog newBlog();

    /**
     * Return a new {@link User} instance
     *
     * @return {@link User} user
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
     * Load the {@link Blog} given the blog ID
     *
     * @param blogId Blog ID
     * @return {@link Blog} blog
     * @throws FetcherException If there is an error loading the blog
     */
    public Blog loadBlog(String blogId) throws FetcherException;

    /**
     * Load the {@link Blog} given the ID
     *
     * @param id ID
     * @return {@link Blog} blog
     * @throws FetcherException If there is an error loading the blog
     */
    public Blog loadBlog(Integer id) throws FetcherException;

    /**
     * Save a {@link Blog}
     *
     * @param blog {@link Blog}
     * @throws FetcherException If there is an error saving the blog
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
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param blog                {@link Blog} instance
     * @param flavor              Flavor
     * @param context             Context
     * @return Blog entries retrieved for the particular request
     * @throws FetcherException If there is an error retrieving the blog entries for the request
     */
    public Entry[] fetchEntries(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Blog blog,
                                String flavor,
                                Map context) throws FetcherException;

    /**
     * Load all the entries for a given category
     *
     * @param blog       {@link Blog}
     * @param categoryId Category ID
     * @return Blog entries for a given category
     * @throws FetcherException If there is an error loading the entries
     */
    public Entry[] loadAllEntriesForCategory(Blog blog, Integer categoryId) throws FetcherException;

    /**
     * Load all the entries for a given category
     *
     * @param blog       {@link Blog}
     * @param categoryId Category ID
     * @param limit      Limit on number of entries to return
     * @return Blog entries for a given category
     * @throws FetcherException If there is an error loading the entries
     */
    public Entry[] loadEntriesForCategory(Blog blog, Integer categoryId, Integer limit) throws FetcherException;

    /**
     * Load a set of entries using a given page size and page in which to retrieve the entries
     *
     * @param blog     {@link Blog}
     * @param pageSize Page size
     * @param page     Page
     * @return Blog entries
     * @throws FetcherException If there is an error loading the entries
     */
    public Entry[] loadEntries(Blog blog, int pageSize, int page) throws FetcherException;

    /**
     * Load a set of entries using a given page size and page in which to retrieve the entries
     *
     * @param pageSize          Page size
     * @param page              Page
     * @param specificCategory  Category
     * @param defaultCategories Default categories to use for requesting entries from the blogs
     * @return Blog entries
     * @throws FetcherException If there is an error loading the entries
     */
    public Entry[] loadEntries(int pageSize, int page, Category specificCategory, Category[] defaultCategories)
        throws FetcherException;

    /**
     * Find entries which have the search query in their title or description
     *
     * @param blog  {@link Blog}
     * @param query Search query
     * @return Blog entries which have the search query in their title or descirption
     * @throws FetcherException If there is an error searching through entries
     */
    public Entry[] findEntries(Blog blog, String query) throws FetcherException;

    /**
     * Find entries by a metadata key/value pair
     *
     * @param blog          {@link Blog}
     * @param metadataKey   Metadata key
     * @param metadataValue Metadata value
     * @param pre           If the search should use % before the metadata value (match anything before)
     * @param post          If the search should use % after the metadata value (match antthing after)
     * @return Entries matching metadata key and value using LIKE syntax for metadata value
     * @throws FetcherException If there is an error searching through entries
     */
    public Entry[] findEntriesByMetadataKeyValue(Blog blog, String metadataKey, String metadataValue,
                                                 boolean pre, boolean post) throws FetcherException;

    /**
     * Find entries with a given metadata key
     *
     * @param blog        {@link Blog}
     * @param metadataKey Metadata key
     * @return Entries with the given metadata key
     * @throws FetcherException If there is an error searching through entries
     */
    public Entry[] findEntriesWithMetadataKey(Blog blog, String metadataKey) throws FetcherException;

    /**
     * Find entries between a start and end date
     *
     * @param blog      {@link Blog}
     * @param startDate Start date
     * @param endDate   End date
     * @return Entries between a start and end date
     * @throws FetcherException If there is an error searching for entries between the dates
     */
    public Entry[] findEntriesBetweenDates(Blog blog, Date startDate, Date endDate) throws FetcherException;

    /**
     * Loads the previous entries of a specified entry
     *
     * @param blog               {@link Blog}
     * @param entry              {@link Entry}
     * @param numPreviousEntries Number of previous entries to retrieve
     * @return Array of entries before the given entry
     * @throws FetcherException If there is an error retrieving previous entries
     */
    public Entry[] loadPreviousEntries(Blog blog, Entry entry, int numPreviousEntries) throws FetcherException;

    /**
     * Count the number of entries for a blog
     *
     * @param blog {@link Blog}
     * @return Number of entries
     * @throws FetcherException If there is an error counting the blog entries
     */
    public Integer countEntries(Blog blog) throws FetcherException;

    /**
     * Count the number of entries for a blog category
     *
     * @param blog     {@link Blog}
     * @param category {@link Category}
     * @return Number of entries
     * @throws FetcherException If there is an error counting the blog entries in the category
     */
    public Integer countEntriesForCategory(Blog blog, Category category) throws FetcherException;

    /**
     * Load an {@link Entry} for a given entry ID
     *
     * @param blog    {@link Blog}
     * @param entryId Entry ID
     * @return {@link Entry} entry
     * @throws FetcherException If there is an error loading the entry
     */
    public Entry loadEntry(Blog blog, Integer entryId) throws FetcherException;

    /**
     * Load an {@link Entry} given a post slug
     *
     * @param blog     {@link Blog}
     * @param postSlug Post slug
     * @return {@link Entry} for the given post slug
     * @throws FetcherException If an entry for the blog and post slug cannot be found
     */
    public Entry loadEntry(Blog blog, String postSlug) throws FetcherException;

    /**
     * Fetch a set of {@link Category} objects
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param blog                {@link Blog} instance
     * @param flavor              Flavor
     * @param context             Context
     * @return Blog categories retrieved for the particular request
     * @throws FetcherException If there is an error retrieving the blog categories for the request
     */
    public Category[] fetchCategories(HttpServletRequest httpServletRequest,
                                      HttpServletResponse httpServletResponse,
                                      Blog blog,
                                      String flavor,
                                      Map context) throws FetcherException;

    /**
     * Load each {@link Category} for a given blog
     *
     * @param blog {@link Blog}
     * @return {@link Category} list for the blog
     * @throws FetcherException If there is an error loading the categories
     */
    public Category[] loadAllCategories(Blog blog) throws FetcherException;

    /**
     * Load the {@link Category} for a given category ID
     *
     * @param blog       {@link Blog}
     * @param categoryId Category ID
     * @return {@link Category} for the given category ID
     * @throws FetcherException If there is an error loading the category
     */
    public Category loadCategory(Blog blog, Integer categoryId) throws FetcherException;

    /**
     * Load the {@link Category} for a given category name
     *
     * @param blog {@link Blog}
     * @param name Category name
     * @return {@link Category} for the given category name
     * @throws FetcherException If there is an error loading the category
     */
    public Category loadCategory(Blog blog, String name) throws FetcherException;

    /**
     * Save a given {@link Entry}
     *
     * @param blog  {@link Blog}
     * @param entry {@link Entry} to save
     * @throws FetcherException If there is an error saving the entry
     */
    public void saveEntry(Blog blog, Entry entry) throws FetcherException;

    /**
     * Load a given {@link Entry}
     *
     * @param blog  {@link Blog}
     * @param entry {@link Entry} to load
     * @throws FetcherException If there is an error loading the entry
     */
    public void loadEntry(Blog blog, Entry entry) throws FetcherException;

    /**
     * Delete a given {@link Entry}
     *
     * @param blog  {@link Blog}
     * @param entry {@link Entry} to delete
     * @throws FetcherException If there is an error deleting the entry
     */
    public void deleteEntry(Blog blog, Entry entry) throws FetcherException;

    /**
     * Save a given {@link Category}
     *
     * @param blog     {@link Blog}
     * @param category {@link Category} to save
     * @throws FetcherException If there is an error saving the category
     */
    public void saveCategory(Blog blog, Category category) throws FetcherException;

    /**
     * Load a given {@link Category}
     *
     * @param blog     {@link Blog}
     * @param category {@link Category} to load
     * @throws FetcherException If there is an loading saving the category
     */
    public void loadCategory(Blog blog, Category category) throws FetcherException;

    /**
     * Delete a given {@link Category}
     *
     * @param blog     {@link Blog}
     * @param category {@link Category} to delete
     * @throws FetcherException If there is an error deleting the category
     */
    public void deleteCategory(Blog blog, Category category) throws FetcherException;

    /**
     * Save a given {@link Comment}
     *
     * @param blog    {@link Blog}
     * @param comment {@link Comment} to save
     * @throws FetcherException If there is an error saving the comment
     */
    public void saveComment(Blog blog, Comment comment) throws FetcherException;

    /**
     * Load a given {@link Comment}
     *
     * @param blog    {@link Blog}
     * @param comment {@link Comment} to load
     * @throws FetcherException If there is an error loading the comment
     */
    public void loadComment(Blog blog, Comment comment) throws FetcherException;

    /**
     * Delete a given {@link Comment}
     *
     * @param blog    {@link Blog}
     * @param comment {@link Comment} to delete
     * @throws FetcherException If there is an error deleting the comment
     */
    public void deleteComment(Blog blog, Comment comment) throws FetcherException;

    /**
     * Load the recent comments for a blog
     *
     * @param blog {@link Blog}
     * @return List of recent comment
     * @throws FetcherException If there is an error retrieving the recent comments
     */
    public List loadRecentComments(Blog blog) throws FetcherException;

    /**
     * Save a given {@link Trackback}
     *
     * @param blog      {@link Blog}
     * @param trackback {@link Trackback} to save
     * @throws FetcherException If there is an error saving the trackback
     */
    public void saveTrackback(Blog blog, Trackback trackback) throws FetcherException;

    /**
     * Load a given {@link Trackback}
     *
     * @param blog      {@link Blog}
     * @param trackback {@link Trackback} to load
     * @throws FetcherException If there is an error loading the trackback
     */
    public void loadTrackback(Blog blog, Trackback trackback) throws FetcherException;

    /**
     * Delete a given {@link Trackback}
     *
     * @param blog      {@link Blog}
     * @param trackback {@link Trackback} to delete
     * @throws FetcherException If there is an error deleting the trackback
     */
    public void deleteTrackback(Blog blog, Trackback trackback) throws FetcherException;

    /**
     * Load the recent trackbacks for a blog
     *
     * @param blog {@link Blog}
     * @return List of recent trackbacks
     * @throws FetcherException If there is an error retrieving the recent trackbacks
     */
    public List loadRecentTrackbacks(Blog blog) throws FetcherException;

    /**
     * Save a given {@link Pingback}
     *
     * @param blog     {@link Blog}
     * @param pingback {@link Pingback} to save
     * @throws FetcherException If there is an error saving the pingback
     */
    public void savePingback(Blog blog, Pingback pingback) throws FetcherException;

    /**
     * Load a given {@link Pingback}
     *
     * @param blog     {@link Blog}
     * @param pingback {@link Pingback} to load
     * @throws FetcherException If there is an error loading the pingback
     */
    public void loadPingback(Blog blog, Pingback pingback) throws FetcherException;

    /**
     * Load a pingback given the source URI and target URI
     *
     * @param blog      {@link Blog}
     * @param sourceURI Source URI
     * @param targetURI Target URI
     * @return {@link Pingback} given the source and target URIs or <code>null</code> if not found
     * @throws FetcherException If there was an erorr loading the pingback
     */
    public Pingback loadPingback(Blog blog, String sourceURI, String targetURI) throws FetcherException;

    /**
     * Delete a given {@link Pingback}
     *
     * @param blog     {@link Blog}
     * @param pingback {@link Pingback} to delete
     * @throws FetcherException If there is an error deleting the pingback
     */
    public void deletePingback(Blog blog, Pingback pingback) throws FetcherException;

    /**
     * Load the recent pingbacks for a blog
     *
     * @param blog {@link Blog}
     * @return List of recent pingbacks
     * @throws FetcherException If there is an error retrieving the recent pingbacks
     */
    public List loadRecentPingbacks(Blog blog) throws FetcherException;

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
     * @return {@link User} user
     * @throws FetcherException If there is an error loading the {@link User} from the blog
     */
    public User loadUser(Blog blog, String userLogin) throws FetcherException;

    /**
     * Load a given {@link User} from a blog given their ID
     *
     * @param blog   {@link Blog}
     * @param userID User ID
     * @return {@link User} user
     * @throws FetcherException If there is an error loading the user
     */
    public User loadUser(Blog blog, Integer userID) throws FetcherException;

    /**
     * Save a given {@link User} to the blog
     *
     * @param blog {@link Blog}
     * @param user {@link User}
     * @return {@link User} user
     * @throws FetcherException If there is an error saving the user to the blog
     */
    public User saveUser(Blog blog, User user) throws FetcherException;

    /**
     * Delete a given user from a blog
     *
     * @param blog   {@link Blog}
     * @param userID User ID
     * @throws FetcherException If there is an error deleting the user from the blog
     */
    public void deleteUser(Blog blog, Integer userID) throws FetcherException;

    /**
     * Find the responses (comments, trackbacks, pingbacks) for a given {@link Blog} matching one of a set of status codes
     *
     * @param blog   {@link Blog}
     * @param status List of status codes to search
     * @return List of responses (comments, trackbacks, pingbacks) matching one of a set of status codes
     * @throws FetcherException If there is an error loading the responses
     */
    public List findResponsesByStatus(Blog blog, String[] status) throws FetcherException;

    /**
     * Find the responses (comments, trackbacks, pingbacks) for a given {@link Blog} matching some query
     *
     * @param blog  {@link Blog}
     * @param query Query which will match on various items such as commenter name, e-mail, IP address, etc.
     * @return List of responses (comments, trackbacks, pingbacks) matching query
     * @throws FetcherException If there is an error loading the responses
     */
    public List findResponsesByQuery(Blog blog, String query) throws FetcherException;

    /**
     * Called when {@link org.blojsom.servlet.BlojsomServlet} is taken out of service
     *
     * @throws FetcherException If there is an error in finalizing this fetcher
     */
    public void destroy() throws FetcherException;
}
