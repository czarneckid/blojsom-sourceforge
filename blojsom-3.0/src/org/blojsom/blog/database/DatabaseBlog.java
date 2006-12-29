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
package org.blojsom.blog.database;

import org.blojsom.blog.Blog;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * DatabaseBlog
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: DatabaseBlog.java,v 1.6 2006-12-29 02:02:22 czarneckid Exp $
 */
public class DatabaseBlog implements Blog, Serializable {

    private Integer _id;
    private String _blogId;
    private Map _templates;
    private Map _plugins;
    private Map _properties;

    /**
     * Create a new instance of the database blog
     */
    public DatabaseBlog() {
    }

    /**
     * Retrieve the unique id
     *
     * @return Unique ID
     */
    public Integer getId() {
        return _id;
    }

    /**
     * Set the id
     *
     * @param id Unique ID
     */
    public void setId(Integer id) {
        _id = id;
    }

    /**
     * Retrieve the blog ID
     *
     * @return Blog ID
     */
    public String getBlogId() {
        return _blogId;
    }

    /**
     * Set the blog ID
     *
     * @param blogID Blog ID
     */
    public void setBlogId(String blogID) {
        _blogId = blogID;
    }

    /**
     * Get a map of the templates
     *
     * @return Map of the templates
     */
    public Map getTemplates() {
        return Collections.unmodifiableMap(_templates);
    }

    /**
     * Set the templates
     *
     * @param templates Map of the templates
     */
    public void setTemplates(Map templates) {
        _templates = templates;
    }

    /**
     * Get a map of the plugins
     *
     * @return Map of the plugins
     */
    public Map getPlugins() {
        return Collections.unmodifiableMap(_plugins);
    }

    /**
     * Set the plugins
     *
     * @param plugins Plugins
     */
    public void setPlugins(Map plugins) {
        _plugins = plugins;
    }

    /**
     * Get the properties for the blog
     *
     * @return Properties for the blog
     */
    public Map getProperties() {
        return _properties;
    }

    /**
     * Set the properties for the blog
     *
     * @param properties Blog properties
     */
    public void setProperties(Map properties) {
        _properties = properties;
    }

    /**
     * Get a named property
     *
     * @param property Property name
     * @return Value of property
     */
    public String getProperty(String property) {
        return (String) _properties.get(property);
    }

    /**
     * Get a named property from the blog
     *
     * @param property Name
     * @param fallback Fallback value
     * @param allowNullBlank Use the fallback property if <code>allowNullBlank</code> is <code>false</code>
     * @return Value of the property
     */
    public String getProperty(String property, String fallback, boolean allowNullBlank) {
        String value = (String) _properties.get(property);

        if (!allowNullBlank && BlojsomUtils.checkNullOrBlank(value)) {
            return fallback;
        }

        return value;
    }

    /**
     * Name of the blog
     *
     * @return Blog name
     */
    public String getBlogName() {
        return (String) _properties.get(BlojsomConstants.BLOG_NAME_IP);
    }

    /**
     * Returns the HTML escaped name of the blog
     *
     * @return Name of the blog that has been escaped
     */
    public String getEscapedBlogName() {
        return BlojsomUtils.escapeString((String) _properties.get(BlojsomConstants.BLOG_NAME_IP));

    }

    /**
     * Description of the blog
     *
     * @return Blog description
     */
    public String getBlogDescription() {
        return (String) _properties.get(BlojsomConstants.BLOG_DESCRIPTION_IP);
    }

    /**
     * Returns the HTML escaped description of the blog
     *
     * @return Description of the blog that has been escaped
     */
    public String getEscapedBlogDescription() {
        return BlojsomUtils.escapeString((String) _properties.get(BlojsomConstants.BLOG_DESCRIPTION_IP));
    }

    /**
     * URL for the blog
     *
     * @return Blog URL
     */
    public String getBlogURL() {
        return (String) _properties.get(BlojsomConstants.BLOG_URL_IP);
    }

    /**
     * Base admin URL for the blog
     *
     * @return Blog base admin URL
     */
    public String getBlogBaseAdminURL() {
        return (String) _properties.get(BlojsomConstants.BLOG_BASE_ADMIN_URL_IP);
    }

    /**
     * Admin URL for the blog
     *
     * @return Blog admin URL
     */
    public String getBlogAdminURL() {
        return (String) _properties.get(BlojsomConstants.BLOG_ADMIN_URL_IP);
    }

    /**
     * Base URL for the blog
     *
     * @return Blog base URL
     */
    public String getBlogBaseURL() {
        return (String) _properties.get(BlojsomConstants.BLOG_BASE_URL_IP);
    }

    /**
     * Language of the blog
     *
     * @return Blog language
     */
    public String getBlogLanguage() {
        String language = BlojsomConstants.BLOG_LANGUAGE_DEFAULT;

        if (_properties.containsKey(BlojsomConstants.BLOG_LANGUAGE_IP)) {
            language = (String) _properties.get(BlojsomConstants.BLOG_LANGUAGE_IP);
        }

        return language;
    }

    /**
     * Country of the blog
     *
     * @return Country for the blog
     */
    public String getBlogCountry() {
        String country = BlojsomConstants.BLOG_COUNTRY_DEFAULT;

        if (_properties.containsKey(BlojsomConstants.BLOG_COUNTRY_IP)) {
            country = (String) _properties.get(BlojsomConstants.BLOG_COUNTRY_IP);
        }

        return country;
    }

    /**
     * Return the number of blog entries to retrieve from the individual categories
     *
     * @return Blog entries to retrieve from the individual categories
     */
    public int getBlogDisplayEntries() {
        int displayEntries;

        try {
            displayEntries = Integer.parseInt((String) _properties.get(BlojsomConstants.BLOG_ENTRIES_DISPLAY_IP));
        } catch (NumberFormatException e) {
            displayEntries = BlojsomConstants.BLOG_ENTRIES_DISPLAY_DEFAULT;
        }

        return displayEntries;
    }

    /**
     * Return the blog owner's e-mail address
     *
     * @return Blog owner's e-mail
     */
    public String getBlogOwnerEmail() {
        return (String) _properties.get(BlojsomConstants.BLOG_OWNER_EMAIL);
    }

    /**
     * Return the blog owner's name
     *
     * @return Blog owner's name
     */
    public String getBlogOwner() {
        return (String) _properties.get(BlojsomConstants.BLOG_OWNER);
    }

    /**
     * Return whether or not comments are enabled
     *
     * @return Whether or not comments are enabled
     */
    public Boolean getBlogCommentsEnabled() {
        return Boolean.valueOf((String) _properties.get(BlojsomConstants.BLOG_COMMENTS_ENABLED_IP));
    }

    /**
     * Return whether or not trackbacks are enabled
     *
     * @return <code>true</code> if trackbacks are enabled, <code>false</code> otherwise
     */
    public Boolean getBlogTrackbacksEnabled() {
        return Boolean.valueOf((String) _properties.get(BlojsomConstants.BLOG_TRACKBACKS_ENABLED_IP));
    }

    /**
     * Return whether or not pingbacks are enabled
     *
     * @return <code>true</code> if pingbacks are enabled, <code>false</code> otherwise
     */
    public Boolean getBlogPingbacksEnabled() {
        return Boolean.valueOf((String) _properties.get(BlojsomConstants.BLOG_PINGBACKS_ENABLED_IP));
    }

    /**
     * Get whether or not email is enabled
     *
     * @return Whether or not email is enabled
     */
    public Boolean getBlogEmailEnabled() {
        return Boolean.valueOf((String) _properties.get(BlojsomConstants.BLOG_EMAIL_ENABLED_IP));
    }

    /**
     * Get the default flavor for this blog
     *
     * @return Default blog flavor
     */
    public String getBlogDefaultFlavor() {
        String defaultFlavor = BlojsomConstants.DEFAULT_FLAVOR_HTML;

        if (_properties.containsKey(BlojsomConstants.BLOG_DEFAULT_FLAVOR_IP)) {
            defaultFlavor = (String) _properties.get(BlojsomConstants.BLOG_DEFAULT_FLAVOR_IP);
        }

        return defaultFlavor;
    }

    /**
     * Is linear navigation enabled?
     *
     * @return <code>true</code> if linear navigation is enabled, <code>false</code> otherwise
     */
    public Boolean getLinearNavigationEnabled() {
        return Boolean.valueOf((String) _properties.get(BlojsomConstants.LINEAR_NAVIGATION_ENABLED_IP));
    }

    /**
     * Is XML-RPC enabled for this blog?
     *
     * @return <code>true</code> if XML-RPC is enabled, <code>false</code> otherwise
     */
    public Boolean getXmlrpcEnabled() {
        return Boolean.valueOf((String) _properties.get(BlojsomConstants.XMLRPC_ENABLED_IP));
    }

    /**
     * Retrieve the blog administration locale as a String
     *
     * @return String of blog administration locale
     */
    public String getBlogAdministrationLocaleAsString() {
        return (String) _properties.get(BlojsomConstants.BLOG_ADMINISTRATION_LOCALE_IP);
    }

    /**
     * Retrieve the blog administration locale as a {@link java.util.Locale} object
     *
     * @return {@link java.util.Locale} object for blog administration locale
     */
    public Locale getBlogAdministrationLocale() {
        String administrationLocale = (String) _properties.get(BlojsomConstants.BLOG_ADMINISTRATION_LOCALE_IP);
        if (!BlojsomUtils.checkNullOrBlank(administrationLocale)) {
            return BlojsomUtils.getLocaleFromString(administrationLocale);
        } else {
            return new Locale("en");
        }
    }

    /**
     * Retrive a {@link java.util.Locale} object from the blog's language and country settings
     *
     * @return {@link java.util.Locale} object from the blog's language and country settings
     */
    public Locale getBlogLocale() {
        return new Locale(getBlogLanguage(), getBlogCountry());
    }

    /**
     * Retrieve whether or not MD5 encrypted passwords are used
     *
     * @return <code>true</code> if encrypted passwords are used, <code>false</code> otherwise
     */
    public Boolean getUseEncryptedPasswords() {
        return Boolean.valueOf((String) _properties.get(BlojsomConstants.USE_ENCRYPTED_PASSWORDS));
    }

    /**
     * Retrieve the in-use password digest algorithm
     *
     * @return Password digest algorithm
     */
    public String getDigestAlgorithm() {
        String digestAlgorithm = BlojsomConstants.DEFAULT_DIGEST_ALGORITHM;

        if (_properties.containsKey(BlojsomConstants.DIGEST_ALGORITHM)) {
            digestAlgorithm = (String) _properties.get(BlojsomConstants.DIGEST_ALGORITHM);
        }

        return digestAlgorithm;
    }

    /**
     * Set the new name for the blog
     *
     * @param blogName Blog name
     */
    public void setBlogName(String blogName) {
        _properties.put(BlojsomConstants.BLOG_NAME_IP, blogName);
    }

    /**
     * Set the new description for the blog
     *
     * @param blogDescription Blog description
     */
    public void setBlogDescription(String blogDescription) {
        _properties.put(BlojsomConstants.BLOG_DESCRIPTION_IP, blogDescription);
    }

    /**
     * Set the new URL for the blog
     *
     * @param blogURL Blog URL
     */
    public void setBlogURL(String blogURL) {
        _properties.put(BlojsomConstants.BLOG_URL_IP, blogURL);
    }

    /**
     * Set the new admin URL for the blog
     *
     * @param blogAdminURL Blog admin URL
     */
    public void setAdminBlogURL(String blogAdminURL) {
        _properties.put(BlojsomConstants.BLOG_ADMIN_URL_IP, blogAdminURL);
    }

    /**
     * Set the new base URL for the blog
     *
     * @param blogBaseURL Blog base URL
     */
    public void setBlogBaseURL(String blogBaseURL) {
        _properties.put(BlojsomConstants.BLOG_BASE_URL_IP, blogBaseURL);
    }

    /**
     * Set the new 2 letter country code for the blog
     *
     * @param blogCountry Blog country code
     */
    public void setBlogCountry(String blogCountry) {
        _properties.put(BlojsomConstants.BLOG_COUNTRY_IP, blogCountry);
    }

    /**
     * Set the new 2 letter language code for the blog
     *
     * @param blogLanguage Blog language code
     */
    public void setBlogLanguage(String blogLanguage) {
        _properties.put(BlojsomConstants.BLOG_LANGUAGE_IP, blogLanguage);
    }

    /**
     * Set the number of entries to display at one time, where -1 indicates to display all entries
     *
     * @param blogDisplayEntries Blog display entries
     */
    public void setBlogDisplayEntries(int blogDisplayEntries) {
        _properties.put(BlojsomConstants.BLOG_ENTRIES_DISPLAY_IP, Integer.toString(blogDisplayEntries));
    }

    /**
     * Set the new blog owner name
     *
     * @param blogOwner Blog owner
     */
    public void setBlogOwner(String blogOwner) {
        _properties.put(BlojsomConstants.BLOG_OWNER, blogOwner);
    }

    /**
     * Set the new blog owner e-mail address
     *
     * @param blogOwnerEmail Blog owner e-mail
     */
    public void setBlogOwnerEmail(String blogOwnerEmail) {
        _properties.put(BlojsomConstants.BLOG_OWNER_EMAIL, blogOwnerEmail);
    }

    /**
     * Set whether blog comments are enabled
     *
     * @param blogCommentsEnabled <code>true</code> if comments are enabled, <code>false</code> otherwise
     */
    public void setBlogCommentsEnabled(Boolean blogCommentsEnabled) {
        _properties.put(BlojsomConstants.BLOG_COMMENTS_ENABLED_IP, blogCommentsEnabled.toString());
    }

    /**
     * Set whether emails are sent on blog comments and trackbacks
     *
     * @param blogEmailEnabled <code>true</code> if email of comments and trackbacks is enabled, <code>false</code> otherwise
     */
    public void setBlogEmailEnabled(Boolean blogEmailEnabled) {
        _properties.put(BlojsomConstants.BLOG_EMAIL_ENABLED_IP, blogEmailEnabled.toString());
    }

    /**
     * Set whether blog trackbacks are enabled
     *
     * @param blogTrackbacksEnabled <code>true</code> if trackbacks are enabled, <code>false</code> otherwise
     */
    public void setBlogTrackbacksEnabled(Boolean blogTrackbacksEnabled) {
        _properties.put(BlojsomConstants.BLOG_TRACKBACKS_ENABLED_IP, blogTrackbacksEnabled.toString());
    }

    /**
     * Set whether blog pingbacks are enabled
     *
     * @param blogPingbacksEnabled <code>true</code> if pingbacks are enabled, <code>false</code> otherwise
     */
    public void setBlogPingbacksEnabled(Boolean blogPingbacksEnabled) {
        _properties.put(BlojsomConstants.BLOG_PINGBACKS_ENABLED_IP, blogPingbacksEnabled.toString());
    }

    /**
     * Set the new default flavor for this blog
     *
     * @param blogDefaultFlavor New default blog flavor
     */
    public void setBlogDefaultFlavor(String blogDefaultFlavor) {
        _properties.put(BlojsomConstants.BLOG_DEFAULT_FLAVOR_IP, blogDefaultFlavor);
    }

    /**
     * Set whether or not linear navigation should be enabled
     *
     * @param linearNavigationEnabled <code>true</code> if linear navigation is enabled, <code>false</code> otherwise
     */
    public void setLinearNavigationEnabled(Boolean linearNavigationEnabled) {
        _properties.put(BlojsomConstants.LINEAR_NAVIGATION_ENABLED_IP, linearNavigationEnabled.toString());
    }

    /**
     * Set whether or not XML-RPC is enabled
     *
     * @param xmlrpcEnabled <code>true</code> if XML-RPC is enabled, <code>false</code> otherwise
     */
    public void setXmlrpcEnabled(Boolean xmlrpcEnabled) {
        _properties.put(BlojsomConstants.XMLRPC_ENABLED_IP, xmlrpcEnabled.toString());
    }

    /**
     * Set the locale used in the administration console
     *
     * @param blogAdministrationLocale Locale string of form <code>language_country_variant</code>
     */
    public void setBlogAdministrationLocale(String blogAdministrationLocale) {
        _properties.put(BlojsomConstants.BLOG_ADMINISTRATION_LOCALE_IP, blogAdministrationLocale);
    }

    /**
     * Set whether or not MD5 encrypted passwords are used
     *
     * @param useEncryptedPasswords <code>true</code> if MD5 passwords are used, <code>false</code> otherwise
     */
    public void setUseEncryptedPasswords(Boolean useEncryptedPasswords) {
        _properties.put(BlojsomConstants.USE_ENCRYPTED_PASSWORDS, useEncryptedPasswords.toString());
    }

    /**
     * Set the new admin URL for the blog
     *
     * @param blogAdminURL Blog admin URL
     */
    public void setBlogAdminURL(String blogAdminURL) {
        _properties.put(BlojsomConstants.BLOG_ADMIN_URL_IP, blogAdminURL);
    }

    /**
     * Set the new base admin URL for the blog
     *
     * @param blogBaseAdminURL Blog base admin URL
     */
    public void setBlogBaseAdminURL(String blogBaseAdminURL) {
        _properties.put(BlojsomConstants.BLOG_BASE_ADMIN_URL_IP, blogBaseAdminURL);
    }

    /**
     * Set the in-use password digest algorithm
     *
     * @param digestAlgorithm Digest algorithm
     */
    public void setDigestAlgorithm(String digestAlgorithm) {
        if (BlojsomUtils.checkNullOrBlank(digestAlgorithm)) {
            digestAlgorithm = BlojsomConstants.DEFAULT_DIGEST_ALGORITHM;
        }

        try {
            MessageDigest.getInstance(digestAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            digestAlgorithm = BlojsomConstants.DEFAULT_DIGEST_ALGORITHM;
        }

        _properties.put(BlojsomConstants.DIGEST_ALGORITHM, digestAlgorithm);
    }

    /**
     * Set a property for the blog
     *
     * @param name Property name
     * @param value Property value
     */
    public void setProperty(String name, String value) {
        if (name != null && value != null) {
            _properties.put(name, value);
        }
    }

    /**
     * Remove a property from the blog
     *
     * @param name Property name
     */
    public void removeProperty(String name) {
        if (!BlojsomUtils.checkNullOrBlank(name)) {
            _properties.remove(name);
        }
    }
}

