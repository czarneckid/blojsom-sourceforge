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

import java.io.File;
import java.io.IOException;
import java.util.*;

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
    private long _blogReloadCheck;
    private int _blogDepth;

    private Map _blogEntryMap;
    private Map _blogCalendarMap;

    private Thread _blogUpdaterThread;

    /**
     * Blog constructor
     *
     * @param blogHome Directory where the blog entries will be stored
     * @param blogName Name of the blog
     * @param blogDescription Detailed description of the blog
     * @param blogURL URL for the blog
     * @param blogLanguage Language of the blog
     * @param blogReloadCheck Time specified in ms when entries should be reloaded from disk
     * @param blogFileExtensions File extensions to look for when checking for blog entries
     * @param blogPropertiesExtensions File extensions to look for in category directories for meta-data
     * @param blogDepth Number of directory-levels to traverse in looking for blog entries
     */
    public Blog(String blogHome, String blogName, String blogDescription, String blogURL, String blogLanguage,
                long blogReloadCheck, String[] blogFileExtensions, String[] blogPropertiesExtensions, int blogDepth) {
        _blogHome = blogHome;
        _blogName = blogName;
        _blogDescription = blogDescription;
        _blogURL = blogURL;
        _blogLanguage = blogLanguage;
        _blogReloadCheck = blogReloadCheck;
        _blogFileExtensions = blogFileExtensions;
        _blogPropertiesExtensions = blogPropertiesExtensions;
        _blogDepth = blogDepth;

        _blogEntryMap = new TreeMap();
        _blogCalendarMap = new TreeMap();
        if (_blogReloadCheck != -1) {
            _blogUpdaterThread = new Thread(new BlogUpdater());
            if (!_blogUpdaterThread.isAlive()) {
                _blogUpdaterThread.setDaemon(true);
                _blogUpdaterThread.start();
            }
        } else {
            _logger.info("blojsom: blog reloading disabled");
            recursiveBlogBuilder(-1, _blogHome);
        }
    }

    /**
     * Build an in-memory representation for the blog
     *
     * @param blogDepth Blog depth to recurse to; -1 indicates infinite depth
     * @param blogDirectory Current working directory for the blog
     */
    private void recursiveBlogBuilder(int blogDepth, String blogDirectory) {
        blogDepth++;
        _logger.debug("Working in directory: " + blogDirectory + " at blog depth: " + blogDepth);
        if (_blogDepth != INFINITE_BLOG_DEPTH) {
            if (blogDepth == _blogDepth) {
                _logger.debug("Reached maximum blog depth: " + blogDepth);
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
	    _logger.debug("loading " + categoryPropertyFiles.length + " props files for '" + blogCategory.getCategory());
            Properties dirProps = new Properties();
            for (int i = 0; i < categoryPropertyFiles.length; i++) {
                try {
                    dirProps.load(new java.io.FileInputStream(categoryPropertyFiles[i]));
                } catch (IOException ex) {
                    _logger.debug("Failed loading properties from: " + categoryPropertyFiles[i].toString());
                    continue;
                }
            }

            blogCategory.setMetaData(dirProps);
        }

        // look for blog entries in this directory
        File[] entries = blog.listFiles(BlojsomUtils.getExtensionsFilter(_blogFileExtensions));
        if (entries == null) {
            _logger.debug("No blog entries in blog directory: " + blogDirectory);
            _blogEntryMap.put(blogCategory, new TreeMap(BlojsomUtils.FILE_TIME_COMPARATOR));
        } else {
            Map entryMap;
            Map calendarMap;

            // Get the category-based map
            if (!_blogEntryMap.containsKey(blogCategory)) {
                entryMap = new TreeMap(BlojsomUtils.FILE_TIME_COMPARATOR);
            } else {
                entryMap = (Map) _blogEntryMap.get(blogCategory);
                _blogEntryMap.remove(blogCategory);
            }

            _logger.debug("Adding " + entries.length + " entries to the blog");
            for (int i = 0; i < entries.length; i++) {
                File entry = entries[i];

                String blogCalendarKey = BlojsomUtils.getDateKey(new Date(entry.lastModified()));
                blogCalendarKey = blogCategory + blogCalendarKey;

                if (!_blogCalendarMap.containsKey(blogCalendarKey)) {
                    calendarMap = new TreeMap(BlojsomUtils.FILE_TIME_COMPARATOR);
                } else {
                    calendarMap = (Map) _blogCalendarMap.get(blogCalendarKey);
                    _blogCalendarMap.remove(blogCalendarKey);
                }

                BlogEntry blogEntry;
                if (!entryMap.containsKey(entry)) {
                    blogEntry = new BlogEntry();
                    blogEntry.setSource(entry);
                    blogEntry.setCategory(BlojsomUtils.removeInitialSlash(categoryKey));
                    blogEntry.setLink(_blogURL + BlojsomUtils.removeInitialSlash(categoryKey) + "?permalink=" + entry.getName());
                    blogEntry.reloadSource();
                    entryMap.put(entry, blogEntry);
                    calendarMap.put(entry, blogEntry);
                    _logger.debug("Adding initial blog entry: " + entry.toString() + " in blog category: " + categoryKey);
                } else {
                    blogEntry = (BlogEntry) entryMap.get(entry);
                    if (entry.lastModified() > blogEntry.getLastModified()) {
                        entryMap.remove(entry);
                        calendarMap.remove(entry);
                        blogEntry = new BlogEntry();
                        blogEntry.setSource(entry);
                        blogEntry.setCategory(BlojsomUtils.removeInitialSlash(categoryKey));
                        blogEntry.setLink(_blogURL + BlojsomUtils.removeInitialSlash(categoryKey) + "?permalink=" + entry.getName());
                        blogEntry.reloadSource();
                        entryMap.put(entry, blogEntry);
                        calendarMap.put(entry, blogEntry);
                        _logger.debug("Blog entry updated on disk: " + entry.toString());
                    }
                }
                _blogCalendarMap.put(blogCalendarKey, calendarMap);
                _logger.debug("Added " + calendarMap.size() + " entries to calendar map under key: " + blogCalendarKey);
            }
            blogCategory.setNumberOfEntries(entryMap.size());
            _blogEntryMap.put(blogCategory, entryMap);
            _logger.debug("Added " + entryMap.size() + " entries to the blog map under key: " + blogCategory);
        }

        if (directories == null) {
            return;
        } else {
            for (int i = 0; i < directories.length; i++) {
                File directory = directories[i];
                recursiveBlogBuilder(blogDepth, directory.toString());
            }
        }
    }

    /**
     * Removes any blog categories that have disappeared while the blog is running
     */
    private void cleanupBlogCategories() {
        Iterator categoryIterator = _blogEntryMap.keySet().iterator();
        ArrayList deletedCategories = new ArrayList();
        while (categoryIterator.hasNext()) {
            BlogCategory blogCategory = (BlogCategory) categoryIterator.next();
            File blogDirectory = new File(_blogHome + blogCategory.getCategory());
            if (!blogDirectory.exists()) {
                deletedCategories.add(blogCategory);
            }
        }

        for (int i = 0; i < deletedCategories.size(); i++) {
            BlogCategory deletedCategory = (BlogCategory) deletedCategories.get(i);
            String deletedCategoryName = deletedCategory.getCategory();

            _blogEntryMap.remove(deletedCategory);
            _logger.debug("Removed blog entry category: " + deletedCategoryName);

            Iterator calendarMapIterator = _blogCalendarMap.keySet().iterator();
            while (calendarMapIterator.hasNext()) {
                String calendarMapKey = (String) calendarMapIterator.next();
                if (calendarMapKey.startsWith(deletedCategoryName)) {
                    _logger.debug("Removed blog calendar category: " + deletedCategoryName);
                    _blogCalendarMap.remove(calendarMapKey);
                }
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
        return _blogEntryMap.containsKey(category);
    }

    /**
     * Return the blog entries for a blog category
     *
     * @param requestedCategory Requested category
     * @return Blog entrie
     */
    private Object getCategoryEntries(BlogCategory requestedCategory) {
        return _blogEntryMap.get(requestedCategory);
    }

    /**
     * Retrieve a permalink entry from the entries for a given category
     *
     * @param requestedCategory Requested category
     * @param permalink Permalink entry requested
     * @return Blog entry array containing the single requested permalink entry (possibly null), or null if the permalink entry was not found
     */
    public BlogEntry[] getPermalinkEntry(BlogCategory requestedCategory, String permalink) {
        BlogEntry[] entryArray;
        Map entriesForCategory = (Map) getCategoryEntries(requestedCategory);

        boolean foundEntry = false;
        entryArray = new BlogEntry[1];
        _logger.debug("Permalink entry: " + requestedCategory.getCategory() + permalink);
        Iterator entryIterator = entriesForCategory.keySet().iterator();
        while (entryIterator.hasNext() && !foundEntry) {
            File entryKey = (File) entryIterator.next();
            if (entryKey.getName().endsWith(permalink)) {
                foundEntry = true;
                entryArray[0] = (BlogEntry) entriesForCategory.get(entryKey);
                // Check if entry deleted from disk
                if (entryArray[0] == null) {
                    entryArray = null;
                    entriesForCategory.remove(entryKey);
                }
            }
        }

        if (foundEntry == false) {
            _logger.warn("Permalink entry: " + permalink + " gone");
            entryArray = null;
        }

        return entryArray;
    }

    /**
     * Retrieve all of the entries for a requested category
     *
     * @param requestedCategory Requested category
     * @return Blog entry array containing the list of blog entries for the requested category
     */
    public BlogEntry[] getEntriesForCategory(BlogCategory requestedCategory) {
        BlogEntry[] entryArray;
        ArrayList entryList = new ArrayList();
        Map entriesForCategory = (Map) getCategoryEntries(requestedCategory);

        Iterator entryIterator = entriesForCategory.keySet().iterator();
        while (entryIterator.hasNext()) {
            Object entryKey = entryIterator.next();
            if (entriesForCategory.get(entryKey) != null) {
                entryList.add(entriesForCategory.get(entryKey));
            } else {
                // Entry deleted from disk
                entriesForCategory.remove(entryKey);
            }
        }
        entryArray = (BlogEntry[]) entryList.toArray(new BlogEntry[entryList.size()]);

        return entryArray;
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
     * @return Blog entry array containing the list of blog entries for the given category, year, month, and day
     */
    public BlogEntry[] getEntriesForDate(BlogCategory requestedCategory, String year, String month, String day) {
        String blogCalendarKey = requestedCategory.getCategory() + year + month + day;
        _logger.debug("Looking for entries by date under key: " + blogCalendarKey);
        BlogEntry[] entryArray;
        ArrayList entryList = new ArrayList();
        Map entriesForDate = null;

        // Search for the calendar key in the calendar map
        Iterator calendarKeyIterator = _blogCalendarMap.keySet().iterator();
        while (calendarKeyIterator.hasNext()) {
            String calendarKey = (String) calendarKeyIterator.next();
            if (calendarKey.startsWith(blogCalendarKey)) {
                entriesForDate = (Map) _blogCalendarMap.get(calendarKey);
            }
        }

        if (entriesForDate != null) {
            Iterator entryIterator = entriesForDate.keySet().iterator();
            while (entryIterator.hasNext()) {
                Object entryKey = entryIterator.next();
                if (entriesForDate.containsKey(entryKey)) {
                    entryList.add(entriesForDate.get(entryKey));
                } else {
                    entriesForDate.remove(entryKey);
                }
            }
            entryArray = (BlogEntry[]) entryList.toArray(new BlogEntry[entryList.size()]);

            return entryArray;
        }

        return null;
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
     * Return the time (in ms) when blog entries will be reloaded from disk
     *
     * @return Time (ms) when to reload the blog entries
     */
    public long getBlogReloadCheck() {
        return _blogReloadCheck;
    }

    /**
     * Set the time (in ms) when blog entries should be reloaded from disk. -1
     * indicates that blog entries should not be reloaded
     *
     * @param _blogReloadCheck Time (ms) when to reload blog entries
     */
    public void setBlogReloadCheck(long _blogReloadCheck) {
        this._blogReloadCheck = _blogReloadCheck;
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
     * Return a list of categories for the blog that are appropriate in a hyperlink
     *
     * @return List of BlogCategory objects
     */
    public BlogCategory[] getBlogCategories() {
        ArrayList categoryList = new ArrayList();
        Iterator categoryIterator = _blogEntryMap.keySet().iterator();
        while (categoryIterator.hasNext()) {
            categoryList.add(categoryIterator.next());
        }
        return (BlogCategory[]) (categoryList.toArray(new BlogCategory[categoryList.size()]));
    }


    /**
     * Updates the in-memory blog representation for blojsom
     */
    private class BlogUpdater implements Runnable {

        /**
         * Create a new BlogUpdater
         */
        public BlogUpdater() {
        }

        /**
         * Reloads the blog entries and cleans up the blog categories from disk
         */
        public void run() {
            while (true) {
                _logger.debug("Reloading blog from BlogUpdater");
                recursiveBlogBuilder(-1, _blogHome);
                cleanupBlogCategories();

                try {
                    Thread.sleep(_blogReloadCheck);
                } catch (InterruptedException e) {
                    _logger.error(e);
                    break;
                }
            }
        }
    }
}
