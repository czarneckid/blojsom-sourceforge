/**
 * Copyright (c) 2003, David A. Czarnecki
 * All rights reserved.
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
package org.ignition.blojsom.blog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ignition.blojsom.util.BlojsomConstants;
import org.ignition.blojsom.util.BlojsomUtils;

import java.util.*;
import java.io.IOException;
import java.io.File;

/**
 * Blog
 *
 * @author David Czarnecki
 * @author Dan Morrill
 */
public class Blog implements BlojsomConstants {

    private Log _logger = LogFactory.getLog(Blog.class);

    private String _blogHome;
    private String _blogName;
    private String _blogDescription;
    private String _blogURL;
    private String _blogLanguage;
    private String[] _blogFileExtensions;
    private String[] _blogPropertiesExtensions;
    private int _blogDepth;
    private int _blogDisplayEntries;
    private String[] _blogDefaultCategoryMappings;

    /**
     * Blog constructor
     *
     * @param blogHome Directory where the blog entries will be stored
     * @param blogName Name of the blog
     * @param blogDescription Detailed description of the blog
     * @param blogURL URL for the blog
     * @param blogLanguage Language of the blog
     * @param blogFileExtensions File extensions to look for when checking for blog entries
     * @param blogPropertiesExtensions File extensions to look for in category directories for meta-data
     * @param blogDepth Number of directory-levels to traverse in looking for blog entries
     */
    public Blog(String blogHome, String blogName, String blogDescription, String blogURL, String blogLanguage,
                String[] blogFileExtensions, String[] blogPropertiesExtensions, int blogDepth) {
        _blogHome = blogHome;
        _blogName = blogName;
        _blogDescription = blogDescription;
        _blogURL = blogURL;
        _blogLanguage = blogLanguage;
        _blogFileExtensions = blogFileExtensions;
        _blogPropertiesExtensions = blogPropertiesExtensions;
        _blogDepth = blogDepth;
    }

    /**
     * Build a list of blog categories recursively
     *
     * @param blogDepth Depth at which the current iteration is running
     * @param blogDirectory Directory in which the current iteration is running
     * @param categoryList Dynamic list of categories that gets added to as it explores directories
     */
    private void recursiveCategoryBuilder(int blogDepth, String blogDirectory, ArrayList categoryList) {
        blogDepth++;
        if (_blogDepth != INFINITE_BLOG_DEPTH) {
            if (blogDepth == _blogDepth) {
                return;
            }
        }

        File blog = new File(blogDirectory);
        File[] directories = blog.listFiles(BlojsomUtils.getDirectoryFilter());

        String categoryKey = BlojsomUtils.getBlogCategory(_blogHome, blogDirectory);
        if (!categoryKey.endsWith("/")) {
            categoryKey += "/";
        }

        BlogCategory blogCategory = new BlogCategory(categoryKey, _blogURL + BlojsomUtils.removeInitialSlash(categoryKey));

        // Load properties file for category (if present)
        File[] categoryPropertyFiles = blog.listFiles(BlojsomUtils.getExtensionsFilter(_blogPropertiesExtensions));
        if ((categoryPropertyFiles != null) && (categoryPropertyFiles.length > 0)) {
            Properties dirProps = new Properties();
            for (int i = 0; i < categoryPropertyFiles.length; i++) {
                try {
                    dirProps.load(new java.io.FileInputStream(categoryPropertyFiles[i]));
                } catch (IOException ex) {
                    _logger.warn("Failed loading properties from: " + categoryPropertyFiles[i].toString());
                    continue;
                }
            }

            blogCategory.setMetaData(dirProps);
        }

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
     * Check to see if a blog category contains any entries
     *
     * @param category Requested category
     * @return <code>true</code> if the category has entries, <code>false</code> otherwise
     */
    public boolean checkCategoryHasEntries(BlogCategory category) {
        File blogCategory = new File(_blogHome + BlojsomUtils.removeInitialSlash(category.getCategory()));
        return blogCategory.exists();
    }

    /**
     * Retrieve a permalink entry from the entries for a given category
     *
     * @param requestedCategory Requested category
     * @param permalink Permalink entry requested
     * @return Blog entry array containing the single requested permalink entry,
     * or <code>null</code> if the permalink entry was not found
     */
    public BlogEntry[] getPermalinkEntry(BlogCategory requestedCategory, String permalink) {
        String category = BlojsomUtils.removeInitialSlash(requestedCategory.getCategory());
        String permalinkEntry = _blogHome + category + permalink;
        File blogFile = new File(permalinkEntry);
        if (!blogFile.exists()) {
            return null;
        } else {
            BlogEntry[] entryArray = new BlogEntry[1];
            BlogEntry blogEntry = new BlogEntry();
            blogEntry.setSource(blogFile);
            blogEntry.setCategory(category);
            blogEntry.setLink(_blogURL + category + "?permalink=" + blogFile.getName());
            blogEntry.reloadSource();
            entryArray[0] = blogEntry;
            return entryArray;
        }
    }

    /**
     * Retrieve all of the entries for a requested category
     *
     * @param requestedCategory Requested category
     * @return Blog entry array containing the list of blog entries for the requested category,
     * or <code>null</code> if there are no entries for the category
     */
    public BlogEntry[] getEntriesForCategory(BlogCategory requestedCategory) {
        BlogEntry[] entryArray;
        File blogCategory = new File(_blogHome + BlojsomUtils.removeInitialSlash(requestedCategory.getCategory()));
        File[] entries = blogCategory.listFiles(BlojsomUtils.getExtensionsFilter(_blogFileExtensions));
        String category = BlojsomUtils.removeInitialSlash(requestedCategory.getCategory());
        if (entries == null) {
            _logger.debug("No blog entries in blog directory: " + blogCategory);
            return null;
        } else {
            Arrays.sort(entries, BlojsomUtils.FILE_TIME_COMPARATOR);
            entryArray = new BlogEntry[entries.length];
            BlogEntry blogEntry;
            for (int i = 0; i < entries.length; i++) {
                File entry = entries[i];
                blogEntry = new BlogEntry();
                blogEntry.setSource(entry);
                blogEntry.setCategory(category);
                blogEntry.setLink(_blogURL + category + "?permalink=" + entry.getName());
                blogEntry.reloadSource();
                entryArray[i] = blogEntry;
            }
            return entryArray;
        }
    }

    /**
     * Retrieve all of the entries for a requested category that fall under a given date. A partial
     * date may be given such that if only a year is given, it would retrieve all entries under the
     * given category for that year. If a year and a month are give, it would retrieve all entries
     * under the given category for that year and month. If a year, month, and day are given, it would
     * retrieve all entries under the given category for that year, month, and day.
     *
     * @param requestedCategory Requested category
     * @param year Year to retrieve entries for
     * @param month Month to retrieve entries for
     * @param day Day to retrieve entries for
     * @return Blog entry array containing the list of blog entries for the given category, year, month, and day,
     * or <code>null</code> if there are no entries
     */
    public BlogEntry[] getEntriesForDate(BlogCategory requestedCategory, String year, String month, String day) {
        BlogEntry[] blogEntries = getEntriesForCategory(requestedCategory);
        ArrayList updatedEntryList = new ArrayList();
        String requestedDateKey = year + month + day;

        if (blogEntries == null) {
            return null;
        }

        for (int i = 0; i < blogEntries.length; i++) {
            BlogEntry blogEntry = blogEntries[i];
            String blogDateKey = BlojsomUtils.getDateKey(blogEntry.getDate());
            if (blogDateKey.startsWith(requestedDateKey)) {
                updatedEntryList.add(blogEntry);
            }
        }

        if (updatedEntryList.size() == 0) {
            return null;
        } else {
            return (BlogEntry[]) updatedEntryList.toArray(new BlogEntry[updatedEntryList.size()]);
        }
    }

    /**
     * Convenience method to retrive entries for the categories, using the values set for
     * the default category mapping and the maximum number of blog entries to retrieve
     * from each category
     *
     * @return Blog entry array containing the list of blog entries for the categories
     * or <code>null</code> if there are no entries
     */
    public BlogEntry[] getEntriesAllCategories() {
        return getEntriesAllCategories(_blogDefaultCategoryMappings, _blogDisplayEntries);
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
     * or <code>null</code> if there are no entries
     */
    public BlogEntry[] getEntriesAllCategories(String[] categoryFilter, int maxBlogEntries) {
        BlogCategory[] blogCategories = null;

        if (categoryFilter == null) {
            blogCategories = getBlogCategories();
        } else {
            blogCategories = new BlogCategory[categoryFilter.length];
            for (int i = 0; i < categoryFilter.length; i++) {
                String category = BlojsomUtils.removeInitialSlash(categoryFilter[i]);
                blogCategories[i] = new BlogCategory(category, _blogURL + category);
            }
        }

        if (blogCategories == null) {
            return null;
        } else {
            ArrayList blogEntries = new ArrayList();
            for (int i = 0; i < blogCategories.length; i++) {
                BlogCategory blogCategory = blogCategories[i];
                BlogEntry[] entriesForCategory = getEntriesForCategory(blogCategory);
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
     * Return the directory where blog entries are stored
     *
     * @return Blog home directory
     */
    public String getBlogHome() {
        return _blogHome;
    }

    /**
     * Set the directory where blog entries are stored
     *
     * @param _blogHome New directory to use to look for blog entries
     */
    public void setBlogHome(String _blogHome) {
        this._blogHome = _blogHome;
    }

    /**
     * Return the list of blog file extensions
     *
     * @return Blog file extensions
     */
    public String[] getBlogFileExtensions() {
        return _blogFileExtensions;
    }

    /**
     * Set the list of blog file extensions to look for
     *
     * @param _blogFileExtensions New list of blog file extensions
     */
    public void setBlogFileExtensions(String[] _blogFileExtensions) {
        this._blogFileExtensions = _blogFileExtensions;
    }

    /**
     * Return the depth to which blog entries will be searched
     *
     * @return Blog depth
     */
    public int getBlogDepth() {
        return _blogDepth;
    }

    /**
     * Set the septh to which blog entries will be searched
     *
     * @param _blogDepth Blog depth
     */
    public void setBlogDepth(int _blogDepth) {
        this._blogDepth = _blogDepth;
    }

    /**
     * Name of the blog
     *
     * @return Blog name
     */
    public String getBlogName() {
        return _blogName;
    }

    /**
     * Set the name of the blog
     *
     * @param _blogName Name for the blog
     */
    public void setBlogName(String _blogName) {
        this._blogName = _blogName;
    }

    /**
     * Description of the blog
     *
     * @return Blog description
     */
    public String getBlogDescription() {
        return _blogDescription;
    }

    /**
     * Set the description for the blog
     *
     * @param _blogDescription Description for the blog
     */
    public void setBlogDescription(String _blogDescription) {
        this._blogDescription = _blogDescription;
    }

    /**
     * URL for the blog
     *
     * @return Blog URL
     */
    public String getBlogURL() {
        return _blogURL;
    }

    /**
     * Set the URL for the blog
     *
     * @param _blogURL URL for the blog
     */
    public void setBlogURL(String _blogURL) {
        this._blogURL = _blogURL;
    }

    /**
     * Language of the blog
     *
     * @return Blog language
     */
    public String getBlogLanguage() {
        return _blogLanguage;
    }

    /**
     * Set the language for the blog
     *
     * @param _blogLanguage Language for the blog
     */
    public void setBlogLanguage(String _blogLanguage) {
        this._blogLanguage = _blogLanguage;
    }

    /**
     * Return the number of blog entries to retrieve from the individual categories
     * @return Blog entries to retrieve from the individual categories
     */
    public int getBlogDisplayEntries() {
        return _blogDisplayEntries;
    }

    /**
     * Set the number of blog entries that should be retrieved from individual categories
     * @param _blogDisplayEntries Number of blog entries that should be retrieved from individual categories
     */
    public void setBlogDisplayEntries(int _blogDisplayEntries) {
        this._blogDisplayEntries = _blogDisplayEntries;
    }

    /**
     * Return the list of categories that should be mapped to the default category '/'
     * @return List of categories
     */
    public String[] getBlogDefaultCategoryMappings() {
        return _blogDefaultCategoryMappings;
    }

    /**
     * Set the list of categories that should be mapped to the default category '/'
     * @param _blogDefaultCategoryMappings List of categories
     */
    public void setBlogDefaultCategoryMappings(String[] _blogDefaultCategoryMappings) {
        this._blogDefaultCategoryMappings = _blogDefaultCategoryMappings;
    }

    /**
     * Return a list of categories for the blog that are appropriate in a hyperlink
     *
     * @return List of BlogCategory objects
     */
    public BlogCategory[] getBlogCategories() {
        ArrayList categoryList = new ArrayList();
        recursiveCategoryBuilder(-1, _blogHome, categoryList);
        return (BlogCategory[]) (categoryList.toArray(new BlogCategory[categoryList.size()]));
    }
}
