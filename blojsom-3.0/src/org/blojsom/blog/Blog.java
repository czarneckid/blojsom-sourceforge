/**
 * Copyright (c) 2003-2007, David A. Czarnecki
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
package org.blojsom.blog;

import java.util.Map;
import java.util.Locale;

/**
 * Blog
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: Blog.java,v 1.7 2007-01-17 02:35:16 czarneckid Exp $
 */
public interface Blog {

    /**
     * Retrieve the unique id
     *
     * @return Unique ID
     */
    public Integer getId();

    /**
     * Set the id
     *
     * @param id Unique ID
     */
    public void setId(Integer id);

    /**
     * Retrieve the blog ID
     *
     * @return Blog ID
     */
    public String getBlogId();

    /**
     * Set the blog ID
     *
     * @param blogID Blog ID
     */
    public void setBlogId(String blogID);

    /**
     * Get a map of the templates
     *
     * @return Map of the templates
     */
    public Map getTemplates();

    /**
     * Set the templates
     *
     * @param templates Map of the templates
     */
    public void setTemplates(Map templates);

    /**
     * Get a map of the plugins
     *
     * @return Map of the plugins
     */
    public Map getPlugins();

    /**
     * Set the plugins
     *
     * @param plugins Plugins
     */
    public void setPlugins(Map plugins);

    /**
     * Get the properties for the blog
     *
     * @return Properties for the blog
     */
    public Map getProperties();

    /**
     * Set the properties for the blog
     *
     * @param properties Blog properties
     */
    public void setProperties(Map properties);

    /**
     * Name of the blog
     *
     * @return Blog name
     */
    String getBlogName();

    /**
     * Returns the HTML escaped name of the blog
     *
     * @return Name of the blog that has been escaped
     */
    String getEscapedBlogName();

    /**
     * Description of the blog
     *
     * @return Blog description
     */
    String getBlogDescription();

    /**
     * Returns the HTML escaped description of the blog
     *
     * @return Description of the blog that has been escaped
     */
    String getEscapedBlogDescription();

    /**
     * URL for the blog
     *
     * @return Blog URL
     */
    String getBlogURL();

    /**
     * Admin URL for the blog
     *
     * @return Blog admin URL
     */
    String getBlogAdminURL();

    /**
     * Base admin URL for the blog
     *
     * @return Blog base admin URL
     */
    String getBlogBaseAdminURL();

    /**
     * Base URL for the blog
     *
     * @return Blog base URL
     */
    String getBlogBaseURL();

    /**
     * Language of the blog
     *
     * @return Blog language
     */
    String getBlogLanguage();

    /**
     * Country of the blog
     *
     * @return Country for the blog
     */
    String getBlogCountry();

    /**
     * Return the number of blog entries to retrieve from the individual categories
     *
     * @return Blog entries to retrieve from the individual categories
     */
    int getBlogDisplayEntries();

    /**
     * Return the blog owner's e-mail address
     *
     * @return Blog owner's e-mail
     */
    String getBlogOwnerEmail();

    /**
     * Return the blog owner's name
     *
     * @return Blog owner's name
     */
    String getBlogOwner();

    /**
     * Return whether or not comments are enabled
     *
     * @return Whether or not comments are enabled
     */
    Boolean getBlogCommentsEnabled();

    /**
     * Return whether or not trackbacks are enabled
     *
     * @return <code>true</code> if trackbacks are enabled, <code>false</code> otherwise
     */
    Boolean getBlogTrackbacksEnabled();

    /**
     * Return whether or not pingbacks are enabled
     *
     * @return <code>true</code> if pingbacks are enabled, <code>false</code> otherwise
     */
    Boolean getBlogPingbacksEnabled();

    /**
     * Get whether or not email is enabled
     *
     * @return Whether or not email is enabled
     */
    Boolean getBlogEmailEnabled();

    /**
     * Get the default flavor for this blog
     *
     * @return Default blog flavor
     */
    String getBlogDefaultFlavor();

    /**
     * Is linear navigation enabled?
     *
     * @return <code>true</code> if linear navigation is enabled, <code>false</code> otherwise
     */
    Boolean getLinearNavigationEnabled();

    /**
     * Is XML-RPC enabled for this blog?
     *
     * @return <code>true</code> if XML-RPC is enabled, <code>false</code> otherwise
     */
    Boolean getXmlrpcEnabled();

    /**
     * Retrieve the blog administration locale as a String
     *
     * @return String of blog administration locale
     */
    String getBlogAdministrationLocaleAsString();

    /**
     * Retrieve the blog administration locale as a {@link java.util.Locale} object
     *
     * @return {@link java.util.Locale} object for blog administration locale
     */
    Locale getBlogAdministrationLocale();

    /**
     * Retrive a {@link java.util.Locale} object from the blog's language and country settings
     *
     * @return {@link java.util.Locale} object from the blog's language and country settings
     */
    Locale getBlogLocale();

    /**
     * Retrieve whether or not MD5 encrypted passwords are used
     *
     * @return <code>true</code> if encrypted passwords are used, <code>false</code> otherwise
     */
    Boolean getUseEncryptedPasswords();

    /**
     * Retrieve the in-use password digest algorithm
     *
     * @return Password digest algorithm
     */
    String getDigestAlgorithm();

    /**
     * Get a named property from the blog
     *
     * @param property Name
     * @return Value of the property
     */
    String getProperty(String property);

    /**
     * Get a named property from the blog
     *
     * @param property Name
     * @param fallback Fallback value
     * @param allowNullBlank Use the fallback property if <code>allowNullBlank</code> is <code>false</code>
     * @return Value of the property
     */
    String getProperty(String property, String fallback, boolean allowNullBlank);

    /**
     * Set the new name for the blog
     *
     * @param blogName Blog name
     */
    void setBlogName(String blogName);

    /**
     * Set the new description for the blog
     *
     * @param blogDescription Blog description
     */
    void setBlogDescription(String blogDescription);

    /**
     * Set the new URL for the blog
     *
     * @param blogURL Blog URL
     */
    void setBlogURL(String blogURL);

    /**
     * Set the new admin URL for the blog
     *
     * @param blogAdminURL Blog admin URL
     */
    void setAdminBlogURL(String blogAdminURL);

    /**
     * Set the new base URL for the blog
     *
     * @param blogBaseURL Blog base URL
     */
    void setBlogBaseURL(String blogBaseURL);

    /**
     * Set the new 2 letter country code for the blog
     *
     * @param blogCountry Blog country code
     */
    void setBlogCountry(String blogCountry);

    /**
     * Set the new 2 letter language code for the blog
     *
     * @param blogLanguage Blog language code
     */
    void setBlogLanguage(String blogLanguage);

    /**
     * Set the number of entries to display at one time, where -1 indicates to display all entries
     *
     * @param blogDisplayEntries Blog display entries
     */
    void setBlogDisplayEntries(int blogDisplayEntries);

    /**
     * Set the new blog owner name
     *
     * @param blogOwner Blog owner
     */
    void setBlogOwner(String blogOwner);

    /**
     * Set the new blog owner e-mail address
     *
     * @param blogOwnerEmail Blog owner e-mail
     */
    void setBlogOwnerEmail(String blogOwnerEmail);

    /**
     * Set whether blog comments are enabled
     *
     * @param blogCommentsEnabled <code>true</code> if comments are enabled, <code>false</code> otherwise
     */
    void setBlogCommentsEnabled(Boolean blogCommentsEnabled);

    /**
     * Set whether emails are sent on blog comments and trackbacks
     *
     * @param blogEmailEnabled <code>true</code> if email of comments and trackbacks is enabled, <code>false</code> otherwise
     */
    void setBlogEmailEnabled(Boolean blogEmailEnabled);

    /**
     * Set whether blog trackbacks are enabled
     *
     * @param blogTrackbacksEnabled <code>true</code> if trackbacks are enabled, <code>false</code> otherwise
     */
    void setBlogTrackbacksEnabled(Boolean blogTrackbacksEnabled);

    /**
     * Set whether blog pingbacks are enabled
     *
     * @param blogPingbacksEnabled <code>true</code> if pingbacks are enabled, <code>false</code> otherwise
     */
    void setBlogPingbacksEnabled(Boolean blogPingbacksEnabled);

    /**
     * Set the new default flavor for this blog
     *
     * @param blogDefaultFlavor New default blog flavor
     */
    void setBlogDefaultFlavor(String blogDefaultFlavor);

    /**
     * Set whether or not linear navigation should be enabled
     *
     * @param linearNavigationEnabled <code>true</code> if linear navigation is enabled, <code>false</code> otherwise
     */
    void setLinearNavigationEnabled(Boolean linearNavigationEnabled);

    /**
     * Set whether or not XML-RPC is enabled
     *
     * @param xmlrpcEnabled <code>true</code> if XML-RPC is enabled, <code>false</code> otherwise
     */
    void setXmlrpcEnabled(Boolean xmlrpcEnabled);

    /**
     * Set the locale used in the administration console
     *
     * @param blogAdministrationLocale Locale string of form <code>language_country_variant</code>
     */
    void setBlogAdministrationLocale(String blogAdministrationLocale);

    /**
     * Set whether or not MD5 encrypted passwords are used
     *
     * @param useEncryptedPasswords <code>true</code> if MD5 passwords are used, <code>false</code> otherwise
     */
    void setUseEncryptedPasswords(Boolean useEncryptedPasswords);

    /**
     * Set the new admin URL for the blog
     *
     * @param blogAdminURL Blog admin URL
     */
    void setBlogAdminURL(String blogAdminURL);

    /**
     * Set the new base admin URL for the blog
     *
     * @param blogBaseAdminURL Blog base admin URL
     */
    void setBlogBaseAdminURL(String blogBaseAdminURL);

    /**
     * Set the in-use password digest algorithm
     *
     * @param digestAlgorithm Digest algorithm
     */
    void setDigestAlgorithm(String digestAlgorithm);

    /**
     * Set a property for the blog
     *
     * @param name Property name
     * @param value Property value
     */
    void setProperty(String name, String value);

    /**
     * Remove a property from the blog
     *
     * @param name Property name
     */
    void removeProperty(String name);
}
