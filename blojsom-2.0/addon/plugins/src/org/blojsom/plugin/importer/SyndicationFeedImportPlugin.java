/**
 * Copyright (c) 2003-2005, David A. Czarnecki
 * All rights reserved.
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
package org.blojsom.plugin.importer;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.BlojsomException;
import org.blojsom.blog.BlogCategory;
import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;
import org.blojsom.blog.BlojsomConfiguration;
import org.blojsom.fetcher.BlojsomFetcher;
import org.blojsom.fetcher.BlojsomFetcherException;
import org.blojsom.plugin.BlojsomPluginException;
import org.blojsom.plugin.admin.WebAdminPlugin;
import org.blojsom.util.BlojsomMetaDataConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Syndication feed importer plugin
 *
 * @author David Czarnecki
 * @version $Id: SyndicationFeedImportPlugin.java,v 1.2 2005-06-16 20:06:38 czarneckid Exp $
 * @since blojsom 2.26
 */
public class SyndicationFeedImportPlugin extends WebAdminPlugin {

    private Log _logger = LogFactory.getLog(SyndicationFeedImportPlugin.class);

    // Pages
    private static final String FEED_IMPORTER_PAGE = "/org/blojsom/plugin/importer/admin/templates/feed-import-settings";

    // Form items
    private static final String IMPORT_URL_PARAM = "import-url";

    // Actions
    private static final String IMPORT_SYNDICATION_FEED_ACTION = "import-syndication-feed";

    // Permissions
    private static final String IMPORT_SYNDICATION_FEED_PERMISSION = "import_syndication_feed";

    private BlojsomFetcher _fetcher;

    /**
     * Create a new instance of the syndication feed import plugin
     */
    public SyndicationFeedImportPlugin() {
    }

    /**
     * Return the display name for the plugin
     *
     * @return Display name for the plugin
     */
    public String getDisplayName() {
        return "Syndication Feed Import plugin";
    }

    /**
     * Return the name of the initial editing page for the plugin
     *
     * @return Name of the initial editing page for the plugin
     */
    public String getInitialPage() {
        return FEED_IMPORTER_PAGE;
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig        Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration {@link org.blojsom.blog.BlojsomConfiguration} information
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws BlojsomPluginException {
        super.init(servletConfig, blojsomConfiguration);

        String fetcherClassName = blojsomConfiguration.getFetcherClass();
        try {
            Class fetcherClass = Class.forName(fetcherClassName);
            _fetcher = (BlojsomFetcher) fetcherClass.newInstance();
            _fetcher.init(servletConfig, blojsomConfiguration);
            _logger.info("Added blojsom fetcher: " + fetcherClassName);
        } catch (ClassNotFoundException e) {
            _logger.error(e);
            throw new BlojsomPluginException(e);
        } catch (InstantiationException e) {
            _logger.error(e);
            throw new BlojsomPluginException(e);
        } catch (IllegalAccessException e) {
            _logger.error(e);
            throw new BlojsomPluginException(e);
        } catch (BlojsomFetcherException e) {
            _logger.error(e);
            throw new BlojsomPluginException(e);
        }
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param user                {@link org.blojsom.blog.BlogUser} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlogUser user, Map context, BlogEntry[] entries) throws BlojsomPluginException {
        entries = super.process(httpServletRequest, httpServletResponse, user, context, entries);
        String page = BlojsomUtils.getRequestValue(PAGE_PARAM, httpServletRequest);

        if (ADMIN_LOGIN_PAGE.equals(page)) {
            return entries;
        }

        String username = getUsernameFromSession(httpServletRequest, user.getBlog());
        if (!checkPermission(user, null, username, IMPORT_SYNDICATION_FEED_PERMISSION)) {
            httpServletRequest.setAttribute(PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            addOperationResultMessage(context, "You are not allowed to import syndication feeds");

            return entries;
        }

        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);

        if (IMPORT_SYNDICATION_FEED_ACTION.equals(action)) {
            String importURLLocation = BlojsomUtils.getRequestValue(IMPORT_URL_PARAM, httpServletRequest);
            if (!BlojsomUtils.checkNullOrBlank(importURLLocation)) {
                try {
                    URL importURL = new URL(importURLLocation);
                    SyndFeedInput input = new SyndFeedInput();
                    SyndFeed feed = input.build(new XmlReader(importURL));
                    SyndEntry entry;

                    List feedEntries = feed.getEntries();

                    StringBuffer statusMessage = new StringBuffer();

                    if (feedEntries.size() > 0) {
                        statusMessage.append("<p>");

                        for (int i = 0; i < feedEntries.size(); i++) {
                            entry = (SyndEntry) feedEntries.get(i);

                            List entryCategories = entry.getCategories();

                            BlogCategory blogCategory;
                            blogCategory = _fetcher.newBlogCategory();
                            String categoryName = "/";
                            if (entryCategories.size() > 0) {
                                categoryName = ((SyndCategory) entryCategories.get(0)).getName();
                                if (categoryName != null) {
                                    if (!categoryName.startsWith("/")) {
                                        categoryName = "/" + categoryName;
                                    }

                                    if (!categoryName.endsWith("/")) {
                                        categoryName += "/";
                                    }
                                }
                            }

                            blogCategory.setCategory(categoryName);
                            blogCategory.setCategoryURL(user.getBlog().getBlogURL() + BlojsomUtils.removeInitialSlash(categoryName));

                            try {
                                blogCategory.save(user);
                            } catch (BlojsomException e) {
                                _logger.error(e);
                                statusMessage.append(e.getMessage() + "<br />");
                            }

                            BlogEntry blogEntry;
                            blogEntry = _fetcher.newBlogEntry();
                            blogEntry.setTitle(entry.getTitle());
                            blogEntry.setCategory(categoryName);
                            blogEntry.setDescription(entry.getDescription().getValue());
                            blogEntry.setBlogCategory(blogCategory);

                            Map entryMetaData = new HashMap();
                            entryMetaData.put(BlojsomMetaDataConstants.BLOG_ENTRY_METADATA_AUTHOR, entry.getAuthor());
                            entryMetaData.put(BlojsomMetaDataConstants.BLOG_ENTRY_METADATA_TIMESTAMP, Long.toString(entry.getPublishedDate().getTime()));

                            blogEntry.setMetaData(entryMetaData);

                            try {
                                blogEntry.save(user);
                            } catch (BlojsomException e) {
                                _logger.error(e);
                                statusMessage.append(e.getMessage() + "<br />");
                            }
                        }

                        statusMessage.append(("</p>"));
                    }

                    String status = "Successfully imported " + feedEntries.size() + " entries";
                    if (statusMessage.length() > 0) {
                        status += "<br /> " + statusMessage.toString();
                    }

                    addOperationResultMessage(context, status);
                } catch (FeedException e) {
                    _logger.error(e);

                    addOperationResultMessage(context, "Error in feed: " + e.getMessage());
                } catch (IOException e) {
                    _logger.error(e);

                    addOperationResultMessage(context, "Error in I/O: " + e.getMessage());
                }
            }
        }

        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws BlojsomPluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error in finalizing this plugin
     */
    public void destroy() throws BlojsomPluginException {
    }
}