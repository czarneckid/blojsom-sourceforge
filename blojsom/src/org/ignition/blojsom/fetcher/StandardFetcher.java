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
package org.ignition.blojsom.fetcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ignition.blojsom.blog.*;
import org.ignition.blojsom.util.BlojsomConstants;
import org.ignition.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * StandardFetcher
 *
 * @author David Czarnecki
 * @since blojsom 1.8
 * @version $Id: StandardFetcher.java,v 1.16 2003-05-26 19:10:34 czarneckid Exp $
 */
public class StandardFetcher implements BlojsomFetcher, BlojsomConstants {

    private Log _logger = LogFactory.getLog(StandardFetcher.class);
    private Blog _blog;

    private static final String FETCHER_CATEGORY = "FETCHER_CATEGORY";
    private static final String FETCHER_PERMALINK = "FETCHER_PERMALINK";
    private static final String FETCHER_NUM_POSTS_INTEGER = "FETCHER_NUM_POSTS_INTEGER";
    private static final String FETCHER_FLAVOR = "FETCHER_FLAVOR";

    private static final String STANDARD_FETCHER_CATEGORY = "STANDARD_FETCHER_CATEGORY";

    /**
     * Default constructor
     */
    public StandardFetcher() {
    }

    /**
     * Initialize this fetcher. This method only called when the fetcher is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     * @param blog {@link Blog} instance
     * @throws BlojsomFetcherException If there is an error initializing the fetcher
     */
    public void init(ServletConfig servletConfig, Blog blog) throws BlojsomFetcherException {
        _blog = blog;
        _logger.debug("Initialized standard fetcher");
    }

    /**
     * Initialize this fetcher. This method only called when the fetcher is instantiated.
     *
     * @param blog {@link Blog} instance
     * @throws BlojsomFetcherException If there is an error initializing the fetcher
     */
    public void init(Blog blog) throws BlojsomFetcherException {
        init(null, blog);
    }

    /**
     * Retrieve a permalink entry from the entries for a given category
     *
     * @param requestedCategory Requested category
     * @param permalink Permalink entry requested
     * @return Blog entry array containing the single requested permalink entry,
     * or <code>BlogEntry[0]</code> if the permalink entry was not found
     */
    protected BlogEntry[] getPermalinkEntry(BlogCategory requestedCategory, String permalink) {
        String category = BlojsomUtils.removeInitialSlash(requestedCategory.getCategory());
        String permalinkEntry = _blog.getBlogHome() + category + permalink;
        File blogFile = new File(permalinkEntry);
        if (!blogFile.exists()) {
            return new BlogEntry[0];
        } else {
            BlogEntry[] entryArray = new BlogEntry[1];
            FileBackedBlogEntry blogEntry = new FileBackedBlogEntry();
            FileBackedBlogCategory blogCategory = new FileBackedBlogCategory(requestedCategory.getCategory(), _blog.getBlogURL() + BlojsomUtils.removeInitialSlash(category));
            blogEntry.setSource(blogFile);
            blogEntry.setCategory(category);
            blogEntry.setLink(_blog.getBlogURL() + category + "?" + PERMALINK_PARAM + "=" + BlojsomUtils.urlEncode(blogFile.getName()));
            blogEntry.setBlogFileEncoding(_blog.getBlogFileEncoding());
            try {
                blogEntry.reloadSource();
                blogCategory.loadMetaData(_blog.getBlogHome(), _blog.getBlogPropertiesExtensions());
                blogEntry.loadMetaData(_blog.getBlogHome(), _blog.getBlogEntryMetaDataExtension());
            } catch (IOException e) {
                return new BlogEntry[0];
            }
            blogEntry.setBlogCategory(blogCategory);
            blogEntry.setCommentsDirectory(_blog.getBlogCommentsDirectory());
            blogEntry.setTrackbacksDirectory(_blog.getBlogTrackbackDirectory());
            if (_blog.getBlogCommentsEnabled().booleanValue()) {
                blogEntry.loadComments();
            }
            blogEntry.loadTrackbacks();
            entryArray[0] = blogEntry;
            return entryArray;
        }
    }

    /**
     * Retrieve all of the entries for a requested category
     *
     * @param requestedCategory Requested category
     * @param maxBlogEntries Maximum number of blog entries to retrieve from a blog category
     * @return Blog entry array containing the list of blog entries for the requested category,
     * or <code>BlogEntry[0]</code> if there are no entries for the category
     */
    protected BlogEntry[] getEntriesForCategory(BlogCategory requestedCategory, int maxBlogEntries) {
        BlogEntry[] entryArray;
        File blogCategory = new File(_blog.getBlogHome() + BlojsomUtils.removeInitialSlash(requestedCategory.getCategory()));
        File[] entries = blogCategory.listFiles(BlojsomUtils.getRegularExpressionFilter(_blog.getBlogFileExtensions()));
        String category = BlojsomUtils.removeInitialSlash(requestedCategory.getCategory());
        if (entries == null) {
            _logger.debug("No blog entries in blog directory: " + blogCategory);
            return new BlogEntry[0];
        } else {
            Arrays.sort(entries, BlojsomUtils.FILE_TIME_COMPARATOR);
            FileBackedBlogEntry blogEntry;
            int entryCounter;
            if (maxBlogEntries == -1) {
                entryCounter = entries.length;
            } else {
                entryCounter = (maxBlogEntries > entries.length) ? entries.length : maxBlogEntries;
            }
            entryArray = new BlogEntry[entryCounter];
            for (int i = 0; i < entryCounter; i++) {
                File entry = entries[i];
                blogEntry = new FileBackedBlogEntry();
                FileBackedBlogCategory blogCategoryForEntry = new FileBackedBlogCategory(requestedCategory.getCategory(), _blog.getBlogURL() + category);
                blogEntry.setSource(entry);
                blogEntry.setCategory(category);
                blogEntry.setLink(_blog.getBlogURL() + category + "?" + PERMALINK_PARAM + "=" + BlojsomUtils.urlEncode(entry.getName()));
                blogEntry.setBlogFileEncoding(_blog.getBlogFileEncoding());
                try {
                    blogEntry.reloadSource();
                    blogCategoryForEntry.loadMetaData(_blog.getBlogHome(), _blog.getBlogPropertiesExtensions());
                    blogEntry.loadMetaData(_blog.getBlogHome(), _blog.getBlogEntryMetaDataExtension());
                } catch (IOException e) {
                    _logger.error(e);
                }
                blogEntry.setCommentsDirectory(_blog.getBlogCommentsDirectory());
                blogEntry.setTrackbacksDirectory(_blog.getBlogTrackbackDirectory());
                blogEntry.setBlogCategory(blogCategoryForEntry);
                if (_blog.getBlogCommentsEnabled().booleanValue()) {
                    blogEntry.loadComments();
                }
                blogEntry.loadTrackbacks();
                entryArray[i] = blogEntry;
            }
            return entryArray;
        }
    }

    /**
     * Retrive entries for the categories, using the values set for
     * the default category mapping and the configured number of blog entries to retrieve
     * from each category
     *
     * @param flavor Requested flavor
     * @param maxBlogEntries Maximum number of entries to retrieve per category
     * @return Blog entry array containing the list of blog entries for the categories
     * or <code>BlogEntry[0]</code> if there are no entries
     */
    protected BlogEntry[] getEntriesAllCategories(String flavor, int maxBlogEntries) {
        if (flavor.equals(DEFAULT_FLAVOR_HTML)) {
            return getEntriesAllCategories(_blog.getBlogDefaultCategoryMappings(), maxBlogEntries);
        } else {
            String flavorMappingKey = flavor + "." + BLOG_DEFAULT_CATEGORY_MAPPING_IP;
            String categoryMappingForFlavor = (String) _blog.getBlogProperties().get(flavorMappingKey);
            String[] categoryMappingsForFlavor = null;
            if (categoryMappingForFlavor != null) {
                _logger.debug("Using category mappings for flavor: " + flavor);
                categoryMappingsForFlavor = BlojsomUtils.parseCommaList(categoryMappingForFlavor);
            } else {
                _logger.debug("Fallback to default category mappings for flavor: " + flavor);
                categoryMappingsForFlavor = _blog.getBlogDefaultCategoryMappings();
            }
            return getEntriesAllCategories(categoryMappingsForFlavor, maxBlogEntries);
        }
    }

    /**
     * Retrieve entries for all the categories in the blog. This method will the parameter
     * <code>maxBlogEntries</code> to limit the entries it retrieves from each of the categories.
     * Entries from the categories are sorted based on file time.
     *
     * @param categoryFilter If <code>null</code>, a list of all the categories is retrieved, otherwise only
     * the categories in the list will be used to search for entries
     * @param maxBlogEntries Maximum number of blog entries to retrieve from each category
     * @return Blog entry array containing the list of blog entries for the categories
     * or <code>BlogEntry[0]</code> if there are no entries
     */
    protected BlogEntry[] getEntriesAllCategories(String[] categoryFilter, int maxBlogEntries) {
        BlogCategory[] blogCategories = null;

        if (categoryFilter == null) {
            blogCategories = getBlogCategories();
        } else {
            blogCategories = new BlogCategory[categoryFilter.length];
            for (int i = 0; i < categoryFilter.length; i++) {
                String category = BlojsomUtils.removeInitialSlash(categoryFilter[i]);
                if ("".equals(category)) {
                    blogCategories[i] = new BlogCategory("/", _blog.getBlogURL() + category);
                } else {
                    blogCategories[i] = new BlogCategory(category, _blog.getBlogURL() + category);
                }
            }
        }

        if (blogCategories == null) {
            return new BlogEntry[0];
        } else {
            ArrayList blogEntries = new ArrayList();
            for (int i = 0; i < blogCategories.length; i++) {
                BlogCategory blogCategory = blogCategories[i];
                BlogEntry[] entriesForCategory = getEntriesForCategory(blogCategory, -1);
                if (entriesForCategory != null) {
                    Arrays.sort(entriesForCategory, BlojsomUtils.FILE_TIME_COMPARATOR);
                    if (maxBlogEntries != -1) {
                        int entryCounter = (maxBlogEntries >= entriesForCategory.length) ? entriesForCategory.length : maxBlogEntries;
                        for (int j = 0; j < entryCounter; j++) {
                            BlogEntry blogEntry = entriesForCategory[j];
                            blogEntries.add(blogEntry);
                        }
                    } else {
                        for (int j = 0; j < entriesForCategory.length; j++) {
                            BlogEntry blogEntry = entriesForCategory[j];
                            blogEntries.add(blogEntry);
                        }
                    }
                }
            }

            BlogEntry[] entries = (BlogEntry[]) blogEntries.toArray(new BlogEntry[blogEntries.size()]);
            Arrays.sort(entries, BlojsomUtils.FILE_TIME_COMPARATOR);
            return entries;
        }
    }

    /**
     * Determine the blog category based on the request
     *
     * @param httpServletRequest Request
     * @return {@link BlogCategory} of the requested category
     */
    protected BlogCategory getBlogCategory(HttpServletRequest httpServletRequest) {
        // Determine the user requested category
        String requestedCategory = httpServletRequest.getPathInfo();
        _logger.debug("blojsom path info: " + requestedCategory);

        if (requestedCategory == null) {
            requestedCategory = "/";
        } else if (!requestedCategory.endsWith("/")) {
            requestedCategory += "/";
        }

        _logger.debug("User requested category: " + requestedCategory);
        FileBackedBlogCategory category = new FileBackedBlogCategory(requestedCategory, _blog.getBlogURL() + BlojsomUtils.removeInitialSlash(requestedCategory));

        // We might also want to pass the flavor so that we can also have flavor-based category meta-data
        category.loadMetaData(_blog.getBlogHome(), _blog.getBlogPropertiesExtensions());

        return category;
    }

    /**
     * Fetch a set of {@link BlogEntry} objects.
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @param flavor Flavor
     * @param context Context
     * @return Blog entries retrieved for the particular request
     * @throws BlojsomFetcherException If there is an error retrieving the blog entries for the request
     */
    public BlogEntry[] fetchEntries(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    String flavor,
                                    Map context) throws BlojsomFetcherException {
        BlogCategory category = (BlogCategory) context.get(STANDARD_FETCHER_CATEGORY);
        context.remove(STANDARD_FETCHER_CATEGORY);

        // Determine if a permalink has been requested
        String permalink = httpServletRequest.getParameter(PERMALINK_PARAM);
        if (permalink != null) {
            permalink = BlojsomUtils.getFilenameForPermalink(permalink, _blog.getBlogFileExtensions());
            permalink = BlojsomUtils.urlDecode(permalink);
            if (permalink == null) {
                _logger.error("Permalink request for invalid permalink: " + httpServletRequest.getParameter(PERMALINK_PARAM));
            } else {
                _logger.debug("Permalink request for: " + permalink);
            }
        }

        // Check for a permalink entry request
        if (permalink != null) {
            context.put(BLOJSOM_PERMALINK, permalink);
            return getPermalinkEntry(category, permalink);
        } else {
            if (category.getCategory().equals("/")) {
                return getEntriesAllCategories(flavor, -1);
            } else {
                return getEntriesForCategory(category, -1);
            }
        }
    }

    /**
     * Fetch a set of {@link BlogEntry} objects. This method is intended to be used for other
     * components such as the XML-RPC handlers that cannot generate servlet request and
     * response objects, but still need to be able to fetch entries. Implementations of this
     * method <b>must</b> be explicit about the exact parameter names and types that are
     * expected to return an appropriate set of {@link BlogEntry} objects. The following
     * table describes the parameters accepted by this method and their return value. The value
     * for <code>fetchParameters</code> may represent the keys and data types that should be
     * present in the <code>fetchParameters</code> map to return the proper data.
     * <p />
     * <table border="1">
     * <th><code>fetchParameters</code> value</th> <th>Return value</th>
     * <tr>
     * <td>"FETCHER_CATEGORY" (<code>BlogCategory</code>) and "FETCHER_PERMALINK" (<code>String</code>)</td> <td>return a single <code>BlogEntry</code> for the requested permalink</td>
     * </tr>
     * <tr>
     * <td>"FETCHER_CATEGORY" (<code>BlogCategory</code>) and "FETCHER_NUM_POSTS_INTEGER" (<code>Integer</code>)</td> <td>return entries for the requested category up to the value indicated by the number of entries</td>
     * </tr>
     * <tr>
     * <td>"FETCHER_FLAVOR" (<code>String</code>) and "FETCHER_NUM_POSTS_INTEGER" (<code>Integer</code>)</td> <td>return all entries for the default category ("/") for the requested flavor up to the value indicated by the number of entries</td>
     * </tr>
     * </table>
     *
     * @param fetchParameters Parameters which will be used to retrieve blog entries
     * @return Blog entries retrieved for the particular request
     * @throws BlojsomFetcherException If there is an error retrieving the blog entries for the request
     */
    public BlogEntry[] fetchEntries(Map fetchParameters) throws BlojsomFetcherException {
        if (fetchParameters.containsKey(FETCHER_CATEGORY) && fetchParameters.containsKey(FETCHER_PERMALINK)) {
            return getPermalinkEntry((BlogCategory) fetchParameters.get(FETCHER_CATEGORY), (String) fetchParameters.get(FETCHER_PERMALINK));
        } else if (fetchParameters.containsKey(FETCHER_FLAVOR) && fetchParameters.containsKey(FETCHER_NUM_POSTS_INTEGER)) {
            return getEntriesAllCategories((String) fetchParameters.get(FETCHER_FLAVOR), ((Integer) fetchParameters.get(FETCHER_NUM_POSTS_INTEGER)).intValue());
        } else if (fetchParameters.containsKey(FETCHER_CATEGORY) && fetchParameters.containsKey(FETCHER_NUM_POSTS_INTEGER)) {
            return getEntriesForCategory((BlogCategory) fetchParameters.get(FETCHER_CATEGORY), ((Integer) fetchParameters.get(FETCHER_NUM_POSTS_INTEGER)).intValue());
        }

        return new BlogEntry[0];
    }

    /**
     * Build a list of blog categories recursively
     *
     * @param blogDepth Depth at which the current iteration is running
     * @param blogDirectory Directory in which the current iteration is running
     * @param categoryList Dynamic list of categories that gets added to as it explores directories
     */
    protected void recursiveCategoryBuilder(int blogDepth, String blogDirectory, ArrayList categoryList) {
        blogDepth++;
        if (_blog.getBlogDepth() != INFINITE_BLOG_DEPTH) {
            if (blogDepth == _blog.getBlogDepth()) {
                return;
            }
        }

        File blog = new File(blogDirectory);
        File[] directories;
        if (_blog.getBlogDirectoryFilter() == null) {
            directories = blog.listFiles(BlojsomUtils.getDirectoryFilter());
        } else {
            directories = blog.listFiles(BlojsomUtils.getDirectoryFilter(_blog.getBlogDirectoryFilter()));
        }

        String categoryKey = BlojsomUtils.getBlogCategory(_blog.getBlogHome(), blogDirectory);
        if (!categoryKey.endsWith("/")) {
            categoryKey += "/";
        }

        FileBackedBlogCategory blogCategory = new FileBackedBlogCategory(categoryKey, _blog.getBlogURL() + BlojsomUtils.removeInitialSlash(categoryKey));
        blogCategory.loadMetaData(_blog.getBlogHome(), _blog.getBlogPropertiesExtensions());
        categoryList.add(blogCategory);

        if (directories == null) {
            return;
        } else {
            for (int i = 0; i < directories.length; i++) {
                File directory = directories[i];
                recursiveCategoryBuilder(blogDepth, directory.toString(), categoryList);
            }
        }
    }

    /**
     * Return a list of categories for the blog that are appropriate in a hyperlink
     *
     * @return List of BlogCategory objects
     */
    protected BlogCategory[] getBlogCategories() {
        ArrayList categoryList = new ArrayList();
        recursiveCategoryBuilder(-1, _blog.getBlogHome(), categoryList);
        return (BlogCategory[]) (categoryList.toArray(new BlogCategory[categoryList.size()]));
    }

    /**
     * Return a list of categories up the category hierarchy from the current category. If
     * the "/" category is requested, <code>null</code> is returned. Up the hierarchy, only
     * the parent categories are returned. Down the hierarchy from the current category, all
     * children are returned while obeying the <code>blog-directory-depth</code> parameter.
     * @param currentCategory Current category in the blog category hierarchy
     *
     * @return List of blog categories or <code>null</code> if "/" category is requested or there
     * are no sub-categories
     */
    protected BlogCategory[] getBlogCategoryHierarchy(BlogCategory currentCategory) {
        if (currentCategory.getCategory().equals("/")) {
            return null;
        }

        StringTokenizer slashTokenizer = new StringTokenizer(currentCategory.getCategory(), "/");
        String previousCategoryName = "/";
        ArrayList categoryList = new ArrayList();
        ArrayList sanitizedCategoryList = new ArrayList();
        BlogCategory category;
        FileBackedBlogCategory fileBackedCategory;

        while (slashTokenizer.hasMoreTokens()) {
            previousCategoryName += slashTokenizer.nextToken() + "/";
            if (!previousCategoryName.equals(currentCategory.getCategory())) {
                fileBackedCategory = new FileBackedBlogCategory(previousCategoryName, _blog.getBlogURL() + BlojsomUtils.removeInitialSlash(previousCategoryName));
                fileBackedCategory.loadMetaData(_blog.getBlogHome(), _blog.getBlogPropertiesExtensions());
                categoryList.add(fileBackedCategory);
            }
        }

        recursiveCategoryBuilder(-1, _blog.getBlogHome() + BlojsomUtils.removeInitialSlash(currentCategory.getCategory()), categoryList);
        for (int i = 0; i < categoryList.size(); i++) {
            category = (BlogCategory) categoryList.get(i);
            if (!category.getCategory().equals(currentCategory.getCategory())) {
                _logger.debug(category.getCategory());
                sanitizedCategoryList.add(category);
            }
        }

        FileBackedBlogCategory rootCategory = new FileBackedBlogCategory("/", _blog.getBlogURL());
        rootCategory.loadMetaData(_blog.getBlogHome(), _blog.getBlogPropertiesExtensions());
        sanitizedCategoryList.add(0, rootCategory);

        if (sanitizedCategoryList.size() > 0) {
            return (BlogCategory[]) sanitizedCategoryList.toArray(new BlogCategory[sanitizedCategoryList.size()]);
        } else {
            return null;
        }
    }

    /**
     * Fetch a set of {@link BlogCategory} objects
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @param flavor Flavor
     * @param context Context
     * @return Blog categories retrieved for the particular request
     * @throws BlojsomFetcherException If there is an error retrieving the blog categories for the request
     */
    public BlogCategory[] fetchCategories(HttpServletRequest httpServletRequest,
                                          HttpServletResponse httpServletResponse,
                                          String flavor,
                                          Map context) throws BlojsomFetcherException {
        BlogCategory[] categories;
        BlogCategory category = getBlogCategory(httpServletRequest);

        context.put(STANDARD_FETCHER_CATEGORY, category);
        context.put(BLOJSOM_REQUESTED_CATEGORY, category);

        if (category.getCategory().equals("/")) {
            categories = getBlogCategories();
        } else {
            categories = getBlogCategoryHierarchy(category);
        }

        return categories;
    }

    /**
     * Fetch a set of {@link BlogCategory} objects. This method is intended to be used for other
     * components such as the XML-RPC handlers that cannot generate servlet request and
     * response objects, but still need to be able to fetch categories. Implementations of this
     * method <b>must</b> be explicit about the exact parameter names and types that are
     * expected to return an appropriate set of {@link BlogCategory} objects. The following
     * table describes the parameters accepted by this method and their return value. The value
     * for <code>fetchParameters</code> may represent the keys and data types that should be
     * present in the <code>fetchParameters</code> map to return the proper data.
     * <p />
     * <table border="1">
     * <th><code>fetchParameters</code> value</th> <th>Return value</th>
     * <tr>
     * <td><code>null</code></td> <td>return all categories</td>
     * </tr>
     * <tr>
     * <td>"FETCHER_CATEGORY" (<code>BlogCategory</code>)</td> <td>Up the hierarchy, only
     * the parent categories are returned. Down the hierarchy from the current category, all
     * children are returned while obeying the <code>blog-directory-depth</code> parameter.</td>
     * </tr>
     * </table>
     *
     * @param fetchParameters Parameters which will be used to retrieve blog entries
     * @return Blog categories retrieved for the particular request
     * @throws BlojsomFetcherException If there is an error retrieving the blog categories for the request
     */
    public BlogCategory[] fetchCategories(Map fetchParameters) throws BlojsomFetcherException {
        if (fetchParameters == null) {
            return getBlogCategories();
        } else if (fetchParameters.containsKey(FETCHER_CATEGORY)) {
            BlogCategory category = (BlogCategory) fetchParameters.get(FETCHER_CATEGORY);
            if (category.getCategory().equals("/")) {
                return getBlogCategories();
            } else {
                return getBlogCategoryHierarchy(category);
            }
        }

        return new BlogCategory[0];
    }

    /**
     * Called when {@link org.ignition.blojsom.servlet.BlojsomServlet} is taken out of service
     *
     * @throws BlojsomFetcherException If there is an error in finalizing this fetcher
     */
    public void destroy() throws BlojsomFetcherException {
    }
}
