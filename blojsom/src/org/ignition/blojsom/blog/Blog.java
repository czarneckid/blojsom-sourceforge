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
package org.ignition.blojsom.blog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ignition.blojsom.util.BlojsomConstants;
import org.ignition.blojsom.util.BlojsomUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Blog
 *
 * @author David Czarnecki
 * @author Mark Lussier
 * @author Dan Morrill
 * @version $Id: Blog.java,v 1.40 2003-03-31 03:46:08 czarneckid Exp $
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
    private String[] _blogDirectoryFilter;
    private String _blogOwner;
    private String _blogOwnerEmail;
    private String _blogCommentsDirectory;
    private Boolean _blogCommentsEnabled;
    private Boolean _blogEmailEnabled;
    private String _blogTrackbackDirectory;

    private HashMap _blogProperties;

    private Map _authorization = null;

    /**
     * Create a blog with the supplied configuration properties
     *
     * @param blogConfiguration Blog configuration properties
     * @throws BlojsomConfigurationException If there is an error configuring the blog
     */
    public Blog(Properties blogConfiguration) throws BlojsomConfigurationException {
        _blogProperties = new HashMap();

        // Load the blog properties with all the keys/values even though some will be overridden
        Iterator keyIterator = blogConfiguration.keySet().iterator();
        while (keyIterator.hasNext()) {
            String key = (String) keyIterator.next();
            String propertyValue = blogConfiguration.getProperty(key);
            _blogProperties.put(key, propertyValue);
        }

        _blogHome = blogConfiguration.getProperty(BLOG_HOME_IP);
        if (_blogHome == null) {
            _logger.error("No value supplied for blog-home");
            throw new BlojsomConfigurationException("No valued supplied for blog-home");
        }
        if (!_blogHome.endsWith("/")) {
            _blogHome += "/";
        }
        _blogProperties.put(BLOG_HOME_IP, _blogHome);

        _blogLanguage = blogConfiguration.getProperty(BLOG_LANGUAGE_IP);
        if (_blogLanguage == null) {
            _logger.warn("No value supplied for blog-language. Defaulting to: " + BLOG_LANGUAGE_DEFAULT);
            _blogLanguage = BLOG_LANGUAGE_DEFAULT;
        }
        _blogProperties.put(BLOG_LANGUAGE_IP, _blogLanguage);

        _blogDescription = blogConfiguration.getProperty(BLOG_DESCRIPTION_IP);
        if (_blogDescription == null) {
            _logger.warn("No value supplied for blog-description");
            _blogDescription = "";
        }
        _blogProperties.put(BLOG_DESCRIPTION_IP, _blogDescription);

        _blogName = blogConfiguration.getProperty(BLOG_NAME_IP);
        if (_blogName == null) {
            _logger.warn("No value supplied for blog-name");
            _blogName = "";
        }
        _blogProperties.put(BLOG_NAME_IP, _blogName);

        _blogDepth = Integer.parseInt(blogConfiguration.getProperty(BLOG_DEPTH_IP, Integer.toString(INFINITE_BLOG_DEPTH)));
        _blogProperties.put(BLOG_DEPTH_IP, new Integer(_blogDepth));

        _blogURL = blogConfiguration.getProperty(BLOG_URL_IP);
        if (_blogURL == null) {
            _logger.error("No value supplied for blog-url");
            throw new BlojsomConfigurationException("No value supplied for blog-url");
        }
        if (!_blogURL.endsWith("/")) {
            _blogURL += "/";
        }
        _blogProperties.put(BLOG_URL_IP, _blogURL);

        _blogFileExtensions = BlojsomUtils.parseCommaList(blogConfiguration.getProperty(BLOG_FILE_EXTENSIONS_IP));
        _blogProperties.put(BLOG_FILE_EXTENSIONS_IP, _blogFileExtensions);

        _blogPropertiesExtensions = BlojsomUtils.parseCommaList(blogConfiguration.getProperty(BLOG_PROPERTIES_EXTENSIONS_IP));
        _blogProperties.put(BLOG_PROPERTIES_EXTENSIONS_IP, _blogPropertiesExtensions);

        _blogDisplayEntries = Integer.parseInt(blogConfiguration.getProperty(BLOG_ENTRIES_DISPLAY_IP, Integer.toString(BLOG_ENTRIES_DISPLAY_DEFAULT)));
        _blogProperties.put(BLOG_ENTRIES_DISPLAY_IP, new Integer(_blogDisplayEntries));

        String blogDefaultCategoryMapping = blogConfiguration.getProperty(BLOG_DEFAULT_CATEGORY_MAPPING_IP);
        if (blogDefaultCategoryMapping == null || "".equals(blogDefaultCategoryMapping)) {
            _blogDefaultCategoryMappings = null;
            _logger.debug("No mapping supplied for the default category '/'");
        } else {
            _blogDefaultCategoryMappings = BlojsomUtils.parseCommaList(blogDefaultCategoryMapping);
            _logger.debug(_blogDefaultCategoryMappings.length + " directories mapped to the default category '/'");
            if (_blogDefaultCategoryMappings.length == 0) {
                _blogDefaultCategoryMappings = null;
            }
        }
        _blogProperties.put(BLOG_DEFAULT_CATEGORY_MAPPING_IP, _blogDefaultCategoryMappings);

        _blogCommentsDirectory = blogConfiguration.getProperty(BLOG_COMMENTS_DIRECTORY_IP);
        if ((_blogCommentsDirectory == null) || ("".equals(_blogCommentsDirectory))) {
            _blogCommentsDirectory = DEFAULT_COMMENTS_DIRECTORY;
        }
        _logger.debug("blojsom comments directory: " + _blogCommentsDirectory);
        _blogProperties.put(BLOG_COMMENTS_DIRECTORY_IP, _blogCommentsDirectory);

        String commentsDirectoryRegex;
        if (_blogCommentsDirectory.startsWith(".")) {
            commentsDirectoryRegex = ".*/\\" + _blogCommentsDirectory;
        } else {
            commentsDirectoryRegex = ".*/" + _blogCommentsDirectory;
        }

        _blogTrackbackDirectory = blogConfiguration.getProperty(BLOG_TRACKBACK_DIRECTORY_IP);
        if ((_blogTrackbackDirectory == null) || ("".equals(_blogTrackbackDirectory))) {
            _blogTrackbackDirectory = DEFAULT_TRACKBACK_DIRECTORY;
        }
        _logger.debug("blojsom trackback directory: " + _blogTrackbackDirectory);
        _blogProperties.put(BLOG_TRACKBACK_DIRECTORY_IP, _blogTrackbackDirectory);

        String trackbackDirectoryRegex;
        if (_blogTrackbackDirectory.startsWith(".")) {
            trackbackDirectoryRegex = ".*/\\" + _blogTrackbackDirectory;
        } else {
            trackbackDirectoryRegex = ".*/" + _blogTrackbackDirectory;
        }

        String blogDirectoryFilter = blogConfiguration.getProperty(BLOG_DIRECTORY_FILTER_IP);
        // Add the blog comments directory to the blog directory filter
        if (blogDirectoryFilter == null) {
            blogDirectoryFilter = commentsDirectoryRegex + ", " + trackbackDirectoryRegex;
        } else {
            blogDirectoryFilter = blogDirectoryFilter + ", " + commentsDirectoryRegex + ", " + trackbackDirectoryRegex;
        }

        _blogDirectoryFilter = BlojsomUtils.parseCommaList(blogDirectoryFilter);
        for (int i = 0; i < _blogDirectoryFilter.length; i++) {
            _logger.debug("blojsom to filter: " + _blogDirectoryFilter[i]);
        }
        _logger.debug("blojsom filtering " + _blogDirectoryFilter.length + " directories");
        _blogProperties.put(BLOG_DIRECTORY_FILTER_IP, _blogDirectoryFilter);

        _blogOwner = blogConfiguration.getProperty(BLOG_OWNER);
        _blogProperties.put(BLOG_OWNER, _blogOwner);

        _blogOwnerEmail = blogConfiguration.getProperty(BLOG_OWNER_EMAIL);
        _blogProperties.put(BLOG_OWNER_EMAIL, _blogOwnerEmail);

        String blogCommentsEnabled = blogConfiguration.getProperty(BLOG_COMMENTS_ENABLED_IP);
        if ("true".equalsIgnoreCase(blogCommentsEnabled)) {
            _blogCommentsEnabled = new Boolean(true);
        } else {
            _blogCommentsEnabled = new Boolean(false);
        }
        _blogProperties.put(BLOG_COMMENTS_ENABLED_IP, _blogCommentsEnabled);


        String blogEmailEnabled = blogConfiguration.getProperty(BLOG_EMAIL_ENABLED_IP);
        if ("true".equalsIgnoreCase(blogEmailEnabled)) {
            _blogEmailEnabled = new Boolean(true);
        } else {
            _blogEmailEnabled = new Boolean(false);
        }
        _blogProperties.put(BLOG_EMAIL_ENABLED_IP, _blogEmailEnabled);


        _logger.info("blojsom home: " + _blogHome);
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
        File[] directories;
        if (_blogDirectoryFilter == null) {
            directories = blog.listFiles(BlojsomUtils.getDirectoryFilter());
        } else {
            directories = blog.listFiles(BlojsomUtils.getDirectoryFilter(_blogDirectoryFilter));
        }

        String categoryKey = BlojsomUtils.getBlogCategory(_blogHome, blogDirectory);
        if (!categoryKey.endsWith("/")) {
            categoryKey += "/";
        }

        BlogCategory blogCategory = new BlogCategory(categoryKey, _blogURL + BlojsomUtils.removeInitialSlash(categoryKey));
        blogCategory.loadMetaData(_blogHome, _blogPropertiesExtensions);
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
     * Check to see if a username and password is valid for this blog
     * @param username Username of the user
     * @param password Password for the Username
     * @return True if the user is authenticated
     */
    public boolean checkAuthorization(String username, String password) {
        boolean result = false;

        if (_authorization != null) {
            if (_authorization.containsKey(username)) {
                result = password.equals(_authorization.get(username));
            }
        }

        return result;
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
     * or <code>BlogEntry[0]</code> if the permalink entry was not found
     */
    public BlogEntry[] getPermalinkEntry(BlogCategory requestedCategory, String permalink) {
        String category = BlojsomUtils.removeInitialSlash(requestedCategory.getCategory());
        String permalinkEntry = _blogHome + category + permalink;
        File blogFile = new File(permalinkEntry);
        if (!blogFile.exists()) {
            return new BlogEntry[0];
        } else {
            BlogEntry[] entryArray = new BlogEntry[1];
            BlogEntry blogEntry = new BlogEntry();
            blogEntry.setSource(blogFile);
            blogEntry.setCategory(category);
            blogEntry.setLink(_blogURL + category + "?" + PERMALINK_PARAM + "=" + BlojsomUtils.urlEncode(blogFile.getName()));
            try {
                blogEntry.reloadSource();
            } catch (IOException e) {
                return new BlogEntry[0];
            }
            blogEntry.setCommentsDirectory(_blogCommentsDirectory);
            if (_blogCommentsEnabled.booleanValue()) {
                blogEntry.loadComments();
            }
            blogEntry.loadTrackbacks();
            entryArray[0] = blogEntry;
            return entryArray;
        }
    }

    /**
     * Retrieve a permalink entry from the entries for a given category
     *
     * @param category Requested category as a String
     * @param permalink Permalink entry requested
     * @return Blog entry array containing the single requested permalink entry,
     * or <code>BlogEntry[0]</code> if the permalink entry was not found
     */
    public BlogEntry[] getPermalinkEntry(String category, String permalink) {
        String permalinkEntry = _blogHome + BlojsomUtils.removeInitialSlash(category) + permalink;
        File blogFile = new File(permalinkEntry);
        if (!blogFile.exists()) {
            return new BlogEntry[0];
        } else {
            BlogEntry[] entryArray = new BlogEntry[1];
            BlogEntry blogEntry = new BlogEntry();
            blogEntry.setSource(blogFile);
            blogEntry.setCategory(category);
            blogEntry.setLink(_blogURL + category + "?" + PERMALINK_PARAM + "=" + BlojsomUtils.urlEncode(blogFile.getName()));
            try {
                blogEntry.reloadSource();
            } catch (IOException e) {
                return new BlogEntry[0];
            }
            blogEntry.setCommentsDirectory(_blogCommentsDirectory);
            if (_blogCommentsEnabled.booleanValue()) {
                blogEntry.loadComments();
            }
            blogEntry.loadTrackbacks();
            entryArray[0] = blogEntry;
            return entryArray;
        }
    }

    /**
     * Retrieve all of the entries for a requested category. Uses the value of the blog display entries
     * to pass to getEntriesForCategory(BlogCategory, int)
     *
     * @param requestedCategory Requested category
     * @return Blog entry array containing the list of blog entries for the requested category,
     * or <code>null</code> if there are no entries for the category
     */
    public BlogEntry[] getEntriesForCategory(BlogCategory requestedCategory) {
        return getEntriesForCategory(requestedCategory, _blogDisplayEntries);
    }

    /**
     * Retrieve all of the entries for a requested category
     *
     * @param requestedCategory Requested category
     * @param maxBlogEntries Maximum number of blog entries to retrieve from a blog category
     * @return Blog entry array containing the list of blog entries for the requested category,
     * or <code>BlogEntry[0]</code> if there are no entries for the category
     */
    public BlogEntry[] getEntriesForCategory(BlogCategory requestedCategory, int maxBlogEntries) {
        BlogEntry[] entryArray;
        File blogCategory = new File(_blogHome + BlojsomUtils.removeInitialSlash(requestedCategory.getCategory()));
        File[] entries = blogCategory.listFiles(BlojsomUtils.getRegularExpressionFilter(_blogFileExtensions));
        String category = BlojsomUtils.removeInitialSlash(requestedCategory.getCategory());
        if (entries == null) {
            _logger.debug("No blog entries in blog directory: " + blogCategory);
            return new BlogEntry[0];
        } else {
            Arrays.sort(entries, BlojsomUtils.FILE_TIME_COMPARATOR);
            BlogEntry blogEntry;
            int entryCounter;
            if (maxBlogEntries == -1) {
                entryCounter = entries.length;
            } else {
                entryCounter = (maxBlogEntries > entries.length) ? entries.length : maxBlogEntries;
            }
            entryArray = new BlogEntry[entryCounter];
            for (int i = 0; i < entryCounter; i++) {
                File entry = entries[i];
                blogEntry = new BlogEntry();
                blogEntry.setSource(entry);
                blogEntry.setCategory(category);
                blogEntry.setLink(_blogURL + category + "?" + PERMALINK_PARAM + "=" + BlojsomUtils.urlEncode(entry.getName()));
                try {
                    blogEntry.reloadSource();
                } catch (IOException e) {
                    _logger.error(e);
                }
                blogEntry.setCommentsDirectory(_blogCommentsDirectory);
                if (_blogCommentsEnabled.booleanValue()) {
                    blogEntry.loadComments();
                }
                blogEntry.loadTrackbacks();
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
     * @param flavor Flavor
     * @param year Year to retrieve entries for
     * @param month Month to retrieve entries for in 2-digit format (01-12)
     * @param day Day to retrieve entries for in 2-digit format (01-31)
     * @return Blog entry array containing the list of blog entries for the given category, year, month, and day,
     * or <code>BlogEntry[0]</code> if there are no entries
     */
    public BlogEntry[] getEntriesForDate(BlogCategory requestedCategory, String flavor, String year, String month, String day) {
        BlogEntry[] blogEntries = null;
        ArrayList updatedEntryList = new ArrayList();
        String requestedDateKey = year + month + day;

        if ((requestedCategory == null) || ("/".equals(requestedCategory.getCategory()))) {
            blogEntries = getEntriesAllCategories(flavor);
        } else {
            blogEntries = getEntriesForCategory(requestedCategory, -1);
        }

        if (blogEntries == null) {
            return new BlogEntry[0];
        }

        for (int i = 0; i < blogEntries.length; i++) {
            BlogEntry blogEntry = blogEntries[i];
            String blogDateKey = BlojsomUtils.getDateKey(blogEntry.getDate());
            if (blogDateKey.startsWith(requestedDateKey)) {
                updatedEntryList.add(blogEntry);
            }
        }

        if (updatedEntryList.size() == 0) {
            return new BlogEntry[0];
        } else {
            return (BlogEntry[]) updatedEntryList.toArray(new BlogEntry[updatedEntryList.size()]);
        }
    }

    /**
     * Convenience method to retrive entries for the categories, using the values set for
     * the default category mapping and the configured number of blog entries to retrieve
     * from each category
     *
     * @param flavor Requested flavor
     * @return Blog entry array containing the list of blog entries for the categories
     * or <code>BlogEntry[0]</code> if there are no entries
     */
    public BlogEntry[] getEntriesAllCategories(String flavor) {
        return getEntriesAllCategories(flavor, _blogDisplayEntries);
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
    public BlogEntry[] getEntriesAllCategories(String flavor, int maxBlogEntries) {
        if (flavor.equals(DEFAULT_FLAVOR_HTML)) {
            return getEntriesAllCategories(_blogDefaultCategoryMappings, maxBlogEntries);
        } else {
            String flavorMappingKey = flavor + "." + BLOG_DEFAULT_CATEGORY_MAPPING_IP;
            String categoryMappingForFlavor = (String) _blogProperties.get(flavorMappingKey);
            String[] categoryMappingsForFlavor = null;
            if (categoryMappingForFlavor != null) {
                _logger.debug("Using category mappings for flavor: " + flavor);
                categoryMappingsForFlavor = BlojsomUtils.parseCommaList(categoryMappingForFlavor);
            } else {
                _logger.debug("Fallback to default category mappings for flavor: " + flavor);
                categoryMappingsForFlavor = _blogDefaultCategoryMappings;
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
     * @param blogHome New directory to use to look for blog entries
     */
    public void setBlogHome(String blogHome) {
        _blogHome = blogHome;
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
     * @param blogFileExtensions New list of blog file extensions
     */
    public void setBlogFileExtensions(String[] blogFileExtensions) {
        _blogFileExtensions = blogFileExtensions;
    }

    /**
     * Return the list of blog properties file extensions
     *
     * @return Blog proprties extensions
     */
    public String[] getBlogPropertiesExtensions() {
        return _blogPropertiesExtensions;
    }

    /**
     * Set the list of blog properties file extensions to look for
     *
     * @param blogPropertiesExtensions New list of blog properties file extensions
     */
    public void setBlogPropertiesExtensions(String[] blogPropertiesExtensions) {
        _blogPropertiesExtensions = blogPropertiesExtensions;
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
     * @param blogDepth Blog depth
     */
    public void setBlogDepth(int blogDepth) {
        _blogDepth = blogDepth;
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
     * @param blogName Name for the blog
     */
    public void setBlogName(String blogName) {
        _blogName = blogName;
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
     * @param blogDescription Description for the blog
     */
    public void setBlogDescription(String blogDescription) {
        _blogDescription = blogDescription;
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
     * @param blogURL URL for the blog
     */
    public void setBlogURL(String blogURL) {
        _blogURL = blogURL;
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
     * @param blogLanguage Language for the blog
     */
    public void setBlogLanguage(String blogLanguage) {
        _blogLanguage = blogLanguage;
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
     * @param blogDisplayEntries Number of blog entries that should be retrieved from individual categories
     */
    public void setBlogDisplayEntries(int blogDisplayEntries) {
        _blogDisplayEntries = blogDisplayEntries;
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
     * @param blogDefaultCategoryMappings List of categories
     */
    public void setBlogDefaultCategoryMappings(String[] blogDefaultCategoryMappings) {
        _blogDefaultCategoryMappings = blogDefaultCategoryMappings;
    }


    /**
     * Set the Username/Password table used for blog authorization.
     * NOTE: This method can only be called once per Blog instances
     * @param authorization HashMap of Usernames and Passwords
     * @return  True is the authorization table was assigned, otherwise false
     */
    public boolean setAuthorization(Map authorization) {
        boolean result = false;
        if (_authorization == null) {
            _authorization = authorization;
            result = true;
        }

        return result;
    }


    /**
     * Return the blog owner's e-mail address
     * @return Blog owner's e-mail
     */
    public String getBlogOwnerEmail() {
        return _blogOwnerEmail;
    }

    /**
     * Set the blog owner's e-mail address
     * @param blogOwnerEmail New e-mail address for the blog owner
     */
    public void setBlogOwnerEmail(String blogOwnerEmail) {
        _blogOwnerEmail = blogOwnerEmail;
    }

    /**
     * Return the blog owner's name
     * @return Blog owner's name
     */
    public String getBlogOwner() {
        return _blogOwner;
    }

    /**
     * Set the name of the blog owner
     * @param blogOwner New blog owner's name
     */
    public void setBlogOwner(String blogOwner) {
        _blogOwner = blogOwner;
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
    public BlogCategory[] getBlogCategoryHierarchy(BlogCategory currentCategory) {
        if (currentCategory.getCategory().equals("/")) {
            return null;
        }

        StringTokenizer slashTokenizer = new StringTokenizer(currentCategory.getCategory(), "/");
        String previousCategoryName = "/";
        ArrayList categoryList = new ArrayList();
        ArrayList sanitizedCategoryList = new ArrayList();
        BlogCategory category;

        while (slashTokenizer.hasMoreTokens()) {
            previousCategoryName += slashTokenizer.nextToken() + "/";
            if (!previousCategoryName.equals(currentCategory.getCategory())) {
                category = new BlogCategory(previousCategoryName, _blogURL + BlojsomUtils.removeInitialSlash(previousCategoryName));
                category.loadMetaData(_blogHome, _blogPropertiesExtensions);
                categoryList.add(category);
            }
        }

        recursiveCategoryBuilder(-1, _blogHome + BlojsomUtils.removeInitialSlash(currentCategory.getCategory()), categoryList);
        for (int i = 0; i < categoryList.size(); i++) {
            category = (BlogCategory) categoryList.get(i);
            if (!category.getCategory().equals(currentCategory.getCategory())) {
                _logger.debug(category.getCategory());
                sanitizedCategoryList.add(category);
            }
        }

        BlogCategory rootCategory = new BlogCategory("/", _blogURL);
        rootCategory.loadMetaData(_blogHome, _blogPropertiesExtensions);
        sanitizedCategoryList.add(0, rootCategory);

        if (sanitizedCategoryList.size() > 0) {
            return (BlogCategory[]) sanitizedCategoryList.toArray(new BlogCategory[sanitizedCategoryList.size()]);
        } else {
            return null;
        }
    }

    /**
     * Returns a read-only view of the properties for this blog
     *
     * @return HashMap of blog properties
     */
    public HashMap getBlogProperties() {
        return _blogProperties;
    }

    /**
     * Return whether or not blog comments are enabled
     *
     * @return <code>true</code> if comments are enabled, <code>false</code> otherwise
     */
    public Boolean areCommentsEnabled() {
        return _blogCommentsEnabled;
    }
}
