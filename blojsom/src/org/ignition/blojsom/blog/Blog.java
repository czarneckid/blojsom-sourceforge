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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Blog
 *
 * @author David Czarnecki
 * @author Mark Lussier
 * @author Dan Morrill
 * @version $Id: Blog.java,v 1.49 2003-05-06 02:22:37 czarneckid Exp $
 */
public class Blog implements BlojsomConstants {

    private Log _logger = LogFactory.getLog(Blog.class);

    private String _blogHome;
    private String _blogName;
    private String _blogDescription;
    private String _blogURL;
    private String _blogBaseURL;
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
    private String _blogFetcher;
    private String _blogEntryMetaDataExtension;

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

        _blogBaseURL = blogConfiguration.getProperty(BLOG_BASE_URL_IP);
        if (_blogBaseURL == null) {
            _logger.error("No value supplied for blog-base-url");
            throw new BlojsomConfigurationException("No value supplied for blog-base-url");
        }
        if (_blogBaseURL.endsWith("/")) {
            _blogBaseURL = _blogBaseURL.substring(0, _blogBaseURL.length() - 1);
        }
        _blogProperties.put(BLOG_BASE_URL_IP, _blogBaseURL);

        _blogFileExtensions = BlojsomUtils.parseCommaList(blogConfiguration.getProperty(BLOG_FILE_EXTENSIONS_IP));
        _blogProperties.put(BLOG_FILE_EXTENSIONS_IP, _blogFileExtensions);

        _blogPropertiesExtensions = BlojsomUtils.parseCommaList(blogConfiguration.getProperty(BLOG_PROPERTIES_EXTENSIONS_IP));
        _blogProperties.put(BLOG_PROPERTIES_EXTENSIONS_IP, _blogPropertiesExtensions);

        _blogEntryMetaDataExtension = blogConfiguration.getProperty(BLOG_ENTRY_META_DATA_EXTENSION_IP);
        _blogProperties.put(BLOG_ENTRY_META_DATA_EXTENSION_IP, _blogEntryMetaDataExtension);

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
            commentsDirectoryRegex = ".*" + File.separator + "\\" + _blogCommentsDirectory;
        } else {
            commentsDirectoryRegex = ".*" + File.separator + _blogCommentsDirectory;
        }

        _blogTrackbackDirectory = blogConfiguration.getProperty(BLOG_TRACKBACK_DIRECTORY_IP);
        if ((_blogTrackbackDirectory == null) || ("".equals(_blogTrackbackDirectory))) {
            _blogTrackbackDirectory = DEFAULT_TRACKBACK_DIRECTORY;
        }
        _logger.debug("blojsom trackback directory: " + _blogTrackbackDirectory);
        _blogProperties.put(BLOG_TRACKBACK_DIRECTORY_IP, _blogTrackbackDirectory);

        String trackbackDirectoryRegex;
        if (_blogTrackbackDirectory.startsWith(".")) {
            trackbackDirectoryRegex = ".*" + File.separator + "\\" + _blogTrackbackDirectory;
        } else {
            trackbackDirectoryRegex = ".*" + File.separator + _blogTrackbackDirectory;
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
            _blogCommentsEnabled = Boolean.valueOf(true);
        } else {
            _blogCommentsEnabled = Boolean.valueOf(false);
        }
        _blogProperties.put(BLOG_COMMENTS_ENABLED_IP, _blogCommentsEnabled);

        String blogEmailEnabled = blogConfiguration.getProperty(BLOG_EMAIL_ENABLED_IP);
        if ("true".equalsIgnoreCase(blogEmailEnabled)) {
            _blogEmailEnabled = Boolean.valueOf(true);
        } else {
            _blogEmailEnabled =  Boolean.valueOf(false);
        }
        _blogProperties.put(BLOG_EMAIL_ENABLED_IP, _blogEmailEnabled);

        String blogFetcherClassName = blogConfiguration.getProperty(BLOG_FETCHER_IP);
        _blogFetcher = blogFetcherClassName;
        _blogProperties.put(BLOG_FETCHER_IP, blogFetcherClassName);

        _logger.info("blojsom home: " + _blogHome);
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
     * Base URL for the blog
     *
     * @return Blog base URL
     */
    public String getBlogBaseURL() {
        return _blogBaseURL;
    }

    /**
     * Set the bse URL for the blog
     *
     * @param blogBaseURL Base URL for the blog
     */
    public void setBlogBaseURL(String blogBaseURL) {
        _blogBaseURL = blogBaseURL;
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
     *
     * @return Blog entries to retrieve from the individual categories
     */
    public int getBlogDisplayEntries() {
        return _blogDisplayEntries;
    }

    /**
     * Set the number of blog entries that should be retrieved from individual categories
     *
     * @param blogDisplayEntries Number of blog entries that should be retrieved from individual categories
     */
    public void setBlogDisplayEntries(int blogDisplayEntries) {
        _blogDisplayEntries = blogDisplayEntries;
    }

    /**
     * Return the list of categories that should be mapped to the default category '/'
     *
     * @return List of categories
     */
    public String[] getBlogDefaultCategoryMappings() {
        return _blogDefaultCategoryMappings;
    }

    /**
     * Set the list of categories that should be mapped to the default category '/'
     *
     * @param blogDefaultCategoryMappings List of categories
     */
    public void setBlogDefaultCategoryMappings(String[] blogDefaultCategoryMappings) {
        _blogDefaultCategoryMappings = blogDefaultCategoryMappings;
    }

    /**
     * Set the Username/Password table used for blog authorization.
     * NOTE: This method can only be called once per Blog instances
     *
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
     *
     * @return Blog owner's e-mail
     */
    public String getBlogOwnerEmail() {
        return _blogOwnerEmail;
    }

    /**
     * Set the blog owner's e-mail address
     *
     * @param blogOwnerEmail New e-mail address for the blog owner
     */
    public void setBlogOwnerEmail(String blogOwnerEmail) {
        _blogOwnerEmail = blogOwnerEmail;
    }

    /**
     * Return the blog owner's name
     *
     * @return Blog owner's name
     */
    public String getBlogOwner() {
        return _blogOwner;
    }

    /**
     * Set the name of the blog owner
     *
     * @param blogOwner New blog owner's name
     */
    public void setBlogOwner(String blogOwner) {
        _blogOwner = blogOwner;
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
     * Get the directory where blog comments will be written to under the individual blog
     * category directories
     *
     * @return Blog comments directory
     */
    public String getBlogCommentsDirectory() {
        return _blogCommentsDirectory;
    }

    /**
     * Set the directory where blog comments will be written to under the individual blog
     * category directories
     *
     * @param blogCommentsDirectory Blog comments directory
     */
    public void setBlogCommentsDirectory(String blogCommentsDirectory) {
        _blogCommentsDirectory = blogCommentsDirectory;
    }

    /**
     * Get the list of directories that should be filtered when looking for categories
     *
     * @return Blog directory filter list
     */
    public String[] getBlogDirectoryFilter() {
        return _blogDirectoryFilter;
    }

    /**
     * Set the list of directories that should be filtered when looking for categories
     *
     * @param blogDirectoryFilter Blog directory filter list
     */
    public void setBlogDirectoryFilter(String[] blogDirectoryFilter) {
        _blogDirectoryFilter = blogDirectoryFilter;
    }

    /**
     * Get the directory where blog trackbacks will be written to under the individual blog
     * category directories
     *
     * @return Blog trackbacks directory
     */
    public String getBlogTrackbackDirectory() {
        return _blogTrackbackDirectory;
    }

    /**
     * Set the directory where blog trackbacks will be written to under the individual blog
     * category directories
     *
     * @param blogTrackbackDirectory Blog trackbacks directory
     */
    public void setBlogTrackbackDirectory(String blogTrackbackDirectory) {
        _blogTrackbackDirectory = blogTrackbackDirectory;
    }

    /**
     * Get the class name of the {@link org.ignition.blojsom.fetcher.BlojsomFetcher} used to fetch categories and entries
     * for this blog
     *
     * @return {@link org.ignition.blojsom.fetcher.BlojsomFetcher} class name
     */
    public String getBlogFetcher() {
        return _blogFetcher;
    }

    /**
     * Set the class name of the {@link org.ignition.blojsom.fetcher.BlojsomFetcher} used to fetch categories and entries
     * for this blog
     *
     * @param blogFetcher {@link org.ignition.blojsom.fetcher.BlojsomFetcher} class name
     */
    public void setBlogFetcher(String blogFetcher) {
        _blogFetcher = blogFetcher;
    }

    /**
     * Return whether or not comments are enabled
     *
     * @return Whether or not comments are enabled
     */
    public Boolean getBlogCommentsEnabled() {
        return _blogCommentsEnabled;
    }

    /**
     * Set whether or not comments are enabled
     *
     * @param blogCommentsEnabled Comments enabled value
     */
    public void setBlogCommentsEnabled(Boolean blogCommentsEnabled) {
        _blogCommentsEnabled = blogCommentsEnabled;
    }

    /**
     * Get whether or not email is enabled
     *
     * @return Whether or not email is enabled
     */
    public Boolean getBlogEmailEnabled() {
        return _blogEmailEnabled;
    }

    /**
     * Set whether or not email is enabled
     *
     * @param blogEmailEnabled Email enabled value
     */
    public void setBlogEmailEnabled(Boolean blogEmailEnabled) {
        _blogEmailEnabled = blogEmailEnabled;
    }

    /**
     *
     * @since blojsom 1.9
     * @return
     */
    public String getBlogEntryMetaDataExtension() {
        return _blogEntryMetaDataExtension;
    }

    /**
     *
     * @since blojsom 1.9
     * @param blogEntryMetaDataExtension
     */
    public void setBlogEntryMetaDataExtension(String blogEntryMetaDataExtension) {
        _blogEntryMetaDataExtension = blogEntryMetaDataExtension;
    }
}
