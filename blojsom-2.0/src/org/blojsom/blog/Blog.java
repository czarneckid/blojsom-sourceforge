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
package org.blojsom.blog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import java.io.File;
import java.util.*;

/**
 * Blog
 *
 * @author David Czarnecki
 * @author Mark Lussier
 * @author Dan Morrill
 * @version $Id: Blog.java,v 1.7 2003-10-23 00:17:34 czarneckid Exp $
 */
public class Blog implements BlojsomConstants {

    private Log _logger = LogFactory.getLog(Blog.class);

    private String _blogHome;
    private String _blogName;
    private String _blogDescription;
    private String _blogURL;
    private String _blogBaseURL;
    private String _blogCountry;
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
    private Boolean _blogTrackbacksEnabled;
    private String _blogTrackbackDirectory;
    private String _blogEntryMetaDataExtension;
    private String _blogFileEncoding;

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

        _blogCountry = blogConfiguration.getProperty(BLOG_COUNTRY_IP);
        if (_blogCountry == null) {
            _logger.warn("No value supplied for blog-country. Defaulting to: " + BLOG_COUNTRY_DEFAULT);
            _blogCountry = BLOG_COUNTRY_DEFAULT;
        }
        _blogProperties.put(BLOG_COUNTRY_IP, _blogCountry);

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
        _blogProperties.put(BLOG_DEFAULT_CATEGORY_MAPPING_IP, blogDefaultCategoryMapping);

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
        _blogCommentsEnabled = Boolean.valueOf(blogCommentsEnabled);
        _blogProperties.put(BLOG_COMMENTS_ENABLED_IP, _blogCommentsEnabled);

        String blogTrackbacksEnabled = blogConfiguration.getProperty(BLOG_TRACKBACKS_ENABLED_IP);
        _blogTrackbacksEnabled = Boolean.valueOf(blogTrackbacksEnabled);
        _blogProperties.put(BLOG_TRACKBACKS_ENABLED_IP, _blogTrackbacksEnabled);

        String blogEmailEnabled = blogConfiguration.getProperty(BLOG_EMAIL_ENABLED_IP);
        if ("true".equalsIgnoreCase(blogEmailEnabled)) {
            _blogEmailEnabled = Boolean.valueOf(true);
        } else {
            _blogEmailEnabled = Boolean.valueOf(false);
        }
        _blogProperties.put(BLOG_EMAIL_ENABLED_IP, _blogEmailEnabled);

        String blogFileEncoding = blogConfiguration.getProperty(BLOG_FILE_ENCODING_IP);
        if (blogFileEncoding == null || "".equals(blogFileEncoding)) {
            blogFileEncoding = UTF8;
        }
        _blogFileEncoding = blogFileEncoding;
        _blogProperties.put(BLOG_FILE_ENCODING_IP, blogFileEncoding);

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
     * Return the directory where blog entries are stored
     *
     * @return Blog home directory
     */
    public String getBlogHome() {
        return _blogHome;
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
     * Return the list of blog properties file extensions
     *
     * @return Blog proprties extensions
     */
    public String[] getBlogPropertiesExtensions() {
        return _blogPropertiesExtensions;
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
     * Name of the blog
     *
     * @return Blog name
     */
    public String getBlogName() {
        return _blogName;
    }

    /**
     * Returns the HTML escaped name of the blog
     *
     * @since blojsom 1.9.6
     * @return Name of the blog that has been escaped
     */
    public String getEscapedBlogName() {
        return BlojsomUtils.escapeString(_blogName);
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
     * Returns the HTML escaped description of the blog
     *
     * @since blojsom 1.9.6
     * @return Description of the blog that has been escaped
     */
    public String getEscapedBlogDescription() {
        return BlojsomUtils.escapeString(_blogDescription);
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
     * Base URL for the blog
     *
     * @return Blog base URL
     */
    public String getBlogBaseURL() {
        return _blogBaseURL;
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
     * Country of the blog
     *
     * @since blojsom 1.9.5
     * @return Country for the blog
     */
    public String getBlogCountry() {
        return _blogCountry;
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
     * Return the list of categories that should be mapped to the default category '/'
     *
     * @return List of categories
     */
    public String[] getBlogDefaultCategoryMappings() {
        return _blogDefaultCategoryMappings;
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
     * Returns the authorization map for this blog
     *
     * @return Map of authorization usernames/passwords
     */
    public Map getAuthorization() {
        return Collections.unmodifiableMap(_authorization);
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
     * Return the blog owner's name
     *
     * @return Blog owner's name
     */
    public String getBlogOwner() {
        return _blogOwner;
    }

    /**
     * Returns a read-only view of the properties for this blog
     *
     * @return Map of blog properties
     */
    public Map getBlogProperties() {
        return Collections.unmodifiableMap(_blogProperties);
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
     * Get the list of directories that should be filtered when looking for categories
     *
     * @return Blog directory filter list
     */
    public String[] getBlogDirectoryFilter() {
        return _blogDirectoryFilter;
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
     * Return whether or not comments are enabled
     *
     * @return Whether or not comments are enabled
     */
    public Boolean getBlogCommentsEnabled() {
        return _blogCommentsEnabled;
    }

    /**
     * Return whether or not trackbacks are enabled
     *
     * @since blojsom 1.9.5
     * @return <code>true</code> if trackbacks are enabled, <code>false</code> otherwise
     */
    public Boolean getBlogTrackbacksEnabled() {
        return _blogTrackbacksEnabled;
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
     * Get the file extension for blog entry meta-data
     *
     * @since blojsom 1.9
     * @return Meta-data file extension
     */
    public String getBlogEntryMetaDataExtension() {
        return _blogEntryMetaDataExtension;
    }

    /**
     * Get the file encoding for blog entries
     *
     * @since blojsom 1.9
     * @return File encoding
     */
    public String getBlogFileEncoding() {
        return _blogFileEncoding;
    }

    /**
     * Return a named property from the blog properties
     *
     * @since blojsom 1.9
     * @param propertyName Name of the property to retrieve
     * @return Property value as a string or <code>null</code> if the property is not found
     */
    public String getBlogProperty(String propertyName) {
        if (_blogProperties.containsKey(propertyName)) {
            return _blogProperties.get(propertyName).toString();
        }

        return null;
    }

    /**
     * Set the new name for the blog
     *
     * @param blogName Blog name
     */
    public void setBlogName(String blogName) {
        _blogName = blogName;
        _blogProperties.put(BLOG_NAME_IP, blogName);
    }

    /**
     * Set the new description for the blog
     *
     * @param blogDescription Blog description
     */
    public void setBlogDescription(String blogDescription) {
        _blogDescription = blogDescription;
        _blogProperties.put(BLOG_DESCRIPTION_IP, blogDescription);
    }

    /**
     * Set the new URL for the blog
     *
     * @param blogURL Blog URL
     */
    public void setBlogURL(String blogURL) {
        _blogURL = blogURL;
        _blogProperties.put(BLOG_URL_IP, blogURL);
    }

    /**
     * Set the new base URL for the blog
     *
     * @param blogBaseURL Blog base URL
     */
    public void setBlogBaseURL(String blogBaseURL) {
        _blogBaseURL = blogBaseURL;
        _blogProperties.put(BLOG_BASE_URL_IP, blogBaseURL);
    }

    /**
     * Set the new 2 letter country code for the blog
     *
     * @param blogCountry Blog country code
     */
    public void setBlogCountry(String blogCountry) {
        _blogCountry = blogCountry;
        _blogProperties.put(BLOG_COUNTRY_IP, blogCountry);
    }

    /**
     * Set the new 2 letter language code for the blog
     *
     * @param blogLanguage Blog language code
     */
    public void setBlogLanguage(String blogLanguage) {
        _blogLanguage = blogLanguage;
        _blogProperties.put(BLOG_LANGUAGE_IP, blogLanguage);
    }

    /**
     * Set the depth to which blojsom should look for directories, where -1 indicates infinite depth search
     *
     * @param blogDepth Blog directory depth
     */
    public void setBlogDepth(int blogDepth) {
        _blogDepth = blogDepth;
        _blogProperties.put(BLOG_DEPTH_IP, new Integer(blogDepth));
    }

    /**
     * Set the number of entries to display at one time, where -1 indicates to display all entries
     *
     * @param blogDisplayEntries Blog display entries
     */
    public void setBlogDisplayEntries(int blogDisplayEntries) {
        _blogDisplayEntries = blogDisplayEntries;
        _blogProperties.put(BLOG_ENTRIES_DISPLAY_IP, new Integer(blogDisplayEntries));
    }

    /**
     * Set the new default blog category mappings
     *
     * @param blogDefaultCategoryMappings Blog default category mappings
     */
    public void setBlogDefaultCategoryMappings(String[] blogDefaultCategoryMappings) {
        _blogDefaultCategoryMappings = blogDefaultCategoryMappings;
        _blogProperties.put(BLOG_DEFAULT_CATEGORY_MAPPING_IP, blogDefaultCategoryMappings);
    }

    /**
     * Set the new blog owner name
     *
     * @param blogOwner Blog owner
     */
    public void setBlogOwner(String blogOwner) {
        _blogOwner = blogOwner;
        _blogProperties.put(BLOG_OWNER, blogOwner);
    }

    /**
     * Set the new blog owner e-mail address
     *
     * @param blogOwnerEmail Blog owner e-mail
     */
    public void setBlogOwnerEmail(String blogOwnerEmail) {
        _blogOwnerEmail = blogOwnerEmail;
        _blogProperties.put(BLOG_OWNER_EMAIL, blogOwnerEmail);
    }

    /**
     * Set whether blog comments are enabled
     *
     * @param blogCommentsEnabled <code>true</code> if comments are enabled, <code>false</code> otherwise
     */
    public void setBlogCommentsEnabled(Boolean blogCommentsEnabled) {
        _blogCommentsEnabled = blogCommentsEnabled;
        _blogProperties.put(BLOG_COMMENTS_ENABLED_IP, blogCommentsEnabled);
    }

    /**
     * Set whether emails are sent on blog comments and trackbacks
     *
     * @param blogEmailEnabled <code>true</code> if email of comments and trackbacks is enabled, <code>false</code> otherwise
     */
    public void setBlogEmailEnabled(Boolean blogEmailEnabled) {
        _blogEmailEnabled = blogEmailEnabled;
        _blogProperties.put(BLOG_EMAIL_ENABLED_IP, blogEmailEnabled);
    }

    /**
     * Set whether blog trackbacks are enabled
     *
     * @param blogTrackbacksEnabled <code>true</code> if trackbacks are enabled, <code>false</code> otherwise
     */
    public void setBlogTrackbacksEnabled(Boolean blogTrackbacksEnabled) {
        _blogTrackbacksEnabled = blogTrackbacksEnabled;
        _blogProperties.put(BLOG_TRACKBACKS_ENABLED_IP, blogTrackbacksEnabled);
    }

    /**
     * Set the new blog file encoding
     *
     * @param blogFileEncoding Blog file encoding
     */
    public void setBlogFileEncoding(String blogFileEncoding) {
        _blogFileEncoding = blogFileEncoding;
        _blogProperties.put(BLOG_FILE_ENCODING_IP, blogFileEncoding);
    }

    /**
     * Set the new blog default category mapping for a particular flavor
     *
     * @param flavorKey Flavor key (must end in blog-default-category-mapping)
     * @param blogDefaultCategoryMapping New blog category mapping
     */
    public void setBlogDefaultCategoryMappingForFlavor(String flavorKey, String blogDefaultCategoryMapping) {
        if (flavorKey.endsWith(BLOG_DEFAULT_CATEGORY_MAPPING_IP)) {
            _blogProperties.put(flavorKey, blogDefaultCategoryMapping);
        }
    }
}
