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
package org.blojsom.plugin.admin;

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
import org.blojsom.util.BlojsomMetaDataConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * EditBlogEntriesPlugin
 *
 * @author czarnecki
 * @since blojsom 2.05
 * @version $Id: EditBlogEntriesPlugin.java,v 1.13 2003-12-30 23:24:09 czarneckid Exp $
 */
public class EditBlogEntriesPlugin extends BaseAdminPlugin {

    private Log _logger = LogFactory.getLog(EditBlogEntriesPlugin.class);

    // XML-RPC constants
    public static final String BLOG_XMLRPC_ENTRY_EXTENSION_IP = "blog-xmlrpc-entry-extension";
    /**
     * Default file extension for blog entries written via XML-RPC
     */
    public static final String DEFAULT_BLOG_XMLRPC_ENTRY_EXTENSION = ".txt";

    // Pages
    private static final String EDIT_BLOG_ENTRIES_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blog-entries";
    private static final String EDIT_BLOG_ENTRIES_LIST_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blog-entries-list";
    private static final String EDIT_BLOG_ENTRY_PAGE = "/org/blojsom/plugin/admin/templates/admin-edit-blog-entry";
    private static final String ADD_BLOG_ENTRY_PAGE = "/org/blojsom/plugin/admin/templates/admin-add-blog-entry";

    // Constants
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_LIST = "BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_LIST";
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_CATEGORY = "BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_CATEGORY";
    private static final String BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY = "BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY";

    // Actions
    private static final String EDIT_BLOG_ENTRIES_ACTION = "edit-blog-entries";
    private static final String EDIT_BLOG_ENTRY_ACTION = "edit-blog-entry";
    private static final String UPDATE_BLOG_ENTRY_ACTION = "update-blog-entry";
    private static final String DELETE_BLOG_ENTRY_ACTION = "delete-blog-entry";
    private static final String NEW_BLOG_ENTRY_ACTION = "new-blog-entry";
    private static final String ADD_BLOG_ENTRY_ACTION = "add-blog-entry";

    // Form elements
    private static final String BLOG_CATEGORY_NAME = "blog-category-name";
    private static final String BLOG_ENTRY_ID = "blog-entry-id";
    private static final String BLOG_ENTRY_TITLE = "blog-entry-title";
    private static final String BLOG_ENTRY_DESCRIPTION = "blog-entry-description";

    private BlojsomFetcher _fetcher;

    /**
     * Default constructor.
     */
    public EditBlogEntriesPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration {@link BlojsomConfiguration} information
     * @throws org.blojsom.plugin.BlojsomPluginException If there is an error initializing the plugin
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
     * @throws BlojsomPluginException If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlogUser user, Map context, BlogEntry[] entries) throws BlojsomPluginException {
        if (!authenticateUser(httpServletRequest, httpServletResponse, context, user.getBlog())) {
            httpServletRequest.setAttribute(PAGE_PARAM, ADMIN_LOGIN_PAGE);

            return entries;
        }

        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);
        if (BlojsomUtils.checkNullOrBlank(action)) {
            _logger.debug("User did not request edit action");
            httpServletRequest.setAttribute(PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
        } else if (PAGE_ACTION.equals(action)) {
            _logger.debug("User requested edit blog entries page");

            httpServletRequest.setAttribute(PAGE_PARAM, EDIT_BLOG_ENTRIES_PAGE);
        } else if (EDIT_BLOG_ENTRIES_ACTION.equals(action)) {
            _logger.debug("User requested edit blog entries list page");

            String blogCategoryName = BlojsomUtils.getRequestValue(BLOG_CATEGORY_NAME, httpServletRequest);
            blogCategoryName = BlojsomUtils.normalize(blogCategoryName);

            BlogCategory category;
            category = _fetcher.newBlogCategory();
            category.setCategory(blogCategoryName);
            category.setCategoryURL(user.getBlog().getBlogURL() + BlojsomUtils.removeInitialSlash(blogCategoryName));

            Map fetchMap = new HashMap();
            fetchMap.put(BlojsomFetcher.FETCHER_CATEGORY, category);
            fetchMap.put(BlojsomFetcher.FETCHER_NUM_POSTS_INTEGER, new Integer(-1));
            try {
                entries = _fetcher.fetchEntries(fetchMap, user);
                if (entries != null) {
                    _logger.debug("Retrieved " + entries.length + " entries from category: " + blogCategoryName);
                    Arrays.sort(entries, BlojsomUtils.FILE_TIME_COMPARATOR);
                } else {
                    _logger.debug("No entries found in category: " + blogCategoryName);
                }
            } catch (BlojsomFetcherException e) {
                _logger.error(e);
                entries = new BlogEntry[0];
            }

            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_CATEGORY, blogCategoryName);
            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_LIST, entries);
            httpServletRequest.setAttribute(PAGE_PARAM, EDIT_BLOG_ENTRIES_LIST_PAGE);
        } else if (EDIT_BLOG_ENTRY_ACTION.equals(action)) {
            _logger.debug("User requested edit blog entry action");

            String blogCategoryName = BlojsomUtils.getRequestValue(BLOG_CATEGORY_NAME, httpServletRequest);
            blogCategoryName = BlojsomUtils.normalize(blogCategoryName);
            String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
            _logger.debug("Blog entry id: " + blogEntryId);

            BlogCategory category;
            category = _fetcher.newBlogCategory();
            category.setCategory(blogCategoryName);
            category.setCategoryURL(user.getBlog().getBlogURL() + BlojsomUtils.removeInitialSlash(blogCategoryName));

            Map fetchMap = new HashMap();
            fetchMap.put(BlojsomFetcher.FETCHER_CATEGORY, category);
            fetchMap.put(BlojsomFetcher.FETCHER_PERMALINK, blogEntryId);
            try {
                entries = _fetcher.fetchEntries(fetchMap, user);
                if (entries != null) {
                    _logger.debug("Retrieved " + entries.length + " entries from category: " + blogCategoryName);
                    context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_ENTRY, entries[0]);
                } else {
                    _logger.debug("No entries found in category: " + blogCategoryName);
                }
            } catch (BlojsomFetcherException e) {
                _logger.error(e);
                addOperationResultMessage(context, "Unable to retrieve blog entry: " + blogEntryId);
                entries = new BlogEntry[0];
            }

            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_CATEGORY, blogCategoryName);
            httpServletRequest.setAttribute(PAGE_PARAM, EDIT_BLOG_ENTRY_PAGE);
        } else if (UPDATE_BLOG_ENTRY_ACTION.equals(action)) {
            _logger.debug("User requested update blog entry action");

            String blogCategoryName = BlojsomUtils.getRequestValue(BLOG_CATEGORY_NAME, httpServletRequest);
            blogCategoryName = BlojsomUtils.normalize(blogCategoryName);
            String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
            String blogEntryDescription = BlojsomUtils.getRequestValue(BLOG_ENTRY_DESCRIPTION, httpServletRequest);
            String blogEntryTitle = BlojsomUtils.getRequestValue(BLOG_ENTRY_TITLE, httpServletRequest);
            _logger.debug("Blog entry id: " + blogEntryId);

            BlogCategory category;
            category = _fetcher.newBlogCategory();
            category.setCategory(blogCategoryName);
            category.setCategoryURL(user.getBlog().getBlogURL() + BlojsomUtils.removeInitialSlash(blogCategoryName));

            Map fetchMap = new HashMap();
            fetchMap.put(BlojsomFetcher.FETCHER_CATEGORY, category);
            fetchMap.put(BlojsomFetcher.FETCHER_PERMALINK, blogEntryId);
            try {
                entries = _fetcher.fetchEntries(fetchMap, user);
                if (entries != null) {
                    _logger.debug("Retrieved " + entries.length + " entries from category: " + blogCategoryName);
                    BlogEntry entryToUpdate = entries[0];
                    entryToUpdate.setTitle(blogEntryTitle);
                    entryToUpdate.setDescription(blogEntryDescription);
                    entryToUpdate.save(user);
                    _logger.debug("Updated blog entry: " + entryToUpdate.getLink());
                    addOperationResultMessage(context, "Updated blog entry: " + blogEntryId);
                } else {
                    _logger.debug("No entries found in category: " + blogCategoryName);
                }
            } catch (BlojsomFetcherException e) {
                _logger.error(e);
                addOperationResultMessage(context, "Unable to retrieve blog entry: " + blogEntryId);
                entries = new BlogEntry[0];
            } catch (BlojsomException e) {
                _logger.error(e);
                addOperationResultMessage(context, "Unable to retrieve blog entry: " + blogEntryId);
                entries = new BlogEntry[0];
            }

            httpServletRequest.setAttribute(PAGE_PARAM, EDIT_BLOG_ENTRIES_PAGE);
        } else if (DELETE_BLOG_ENTRY_ACTION.equals(action)) {
            _logger.debug("User requested delete blog entry action");

            String blogCategoryName = BlojsomUtils.getRequestValue(BLOG_CATEGORY_NAME, httpServletRequest);
            blogCategoryName = BlojsomUtils.normalize(blogCategoryName);
            String blogEntryId = BlojsomUtils.getRequestValue(BLOG_ENTRY_ID, httpServletRequest);
            _logger.debug("Blog entry id: " + blogEntryId);

            BlogCategory category;
            category = _fetcher.newBlogCategory();
            category.setCategory(blogCategoryName);
            category.setCategoryURL(user.getBlog().getBlogURL() + BlojsomUtils.removeInitialSlash(blogCategoryName));

            Map fetchMap = new HashMap();
            fetchMap.put(BlojsomFetcher.FETCHER_CATEGORY, category);
            fetchMap.put(BlojsomFetcher.FETCHER_PERMALINK, blogEntryId);
            try {
                entries = _fetcher.fetchEntries(fetchMap, user);
                if (entries != null) {
                    _logger.debug("Retrieved " + entries.length + " entries from category: " + blogCategoryName);
                    entries[0].delete(user);
                    addOperationResultMessage(context, "Deleted blog entry: " + blogEntryId);
                } else {
                    _logger.debug("No entries found in category: " + blogCategoryName);
                }
            } catch (BlojsomFetcherException e) {
                _logger.error(e);
                addOperationResultMessage(context, "Unable to delete blog entry: " + blogEntryId);
                entries = new BlogEntry[0];
            } catch (BlojsomException e) {
                _logger.error(e);
                addOperationResultMessage(context, "Unable to delete blog entry: " + blogEntryId);
                entries = new BlogEntry[0];
            }

            httpServletRequest.setAttribute(PAGE_PARAM, EDIT_BLOG_ENTRIES_PAGE);
        } else if (NEW_BLOG_ENTRY_ACTION.equals(action)) {
            _logger.debug("User requested new blog entry action");

            String blogCategoryName = BlojsomUtils.getRequestValue(BLOG_CATEGORY_NAME, httpServletRequest);
            blogCategoryName = BlojsomUtils.normalize(blogCategoryName);

            context.put(BLOJSOM_PLUGIN_EDIT_BLOG_ENTRIES_CATEGORY, blogCategoryName);
            httpServletRequest.setAttribute(PAGE_PARAM, ADD_BLOG_ENTRY_PAGE);
        } else if (ADD_BLOG_ENTRY_ACTION.equals(action)) {
            _logger.debug("User requested add blog entry action");

            String blogCategoryName = BlojsomUtils.getRequestValue(BLOG_CATEGORY_NAME, httpServletRequest);
            blogCategoryName = BlojsomUtils.normalize(blogCategoryName);
            if (!blogCategoryName.endsWith("/")) {
                blogCategoryName += "/";
            }
            String blogEntryDescription = BlojsomUtils.getRequestValue(BLOG_ENTRY_DESCRIPTION, httpServletRequest);
            String blogEntryTitle = BlojsomUtils.getRequestValue(BLOG_ENTRY_TITLE, httpServletRequest);

            BlogCategory category;
            category = _fetcher.newBlogCategory();
            category.setCategory(blogCategoryName);
            category.setCategoryURL(user.getBlog().getBlogURL() + BlojsomUtils.removeInitialSlash(blogCategoryName));

            BlogEntry entry;
            entry = _fetcher.newBlogEntry();
            entry.setTitle(blogEntryTitle);
            entry.setCategory(blogCategoryName);
            entry.setDescription(blogEntryDescription);

            Map entryMetaData = new HashMap();
            String username = (String) httpServletRequest.getSession().getAttribute(user.getBlog().getBlogURL() + "_" + BLOJSOM_ADMIN_PLUGIN_USERNAME_KEY);
            entryMetaData.put(BlojsomMetaDataConstants.BLOG_ENTRY_METADATA_AUTHOR, username);
            entryMetaData.put(BlojsomMetaDataConstants.BLOG_ENTRY_METADATA_TIMESTAMP, new Long(new Date().getTime()).toString());
            entry.setMetaData(entryMetaData);

            String blogEntryExtension = user.getBlog().getBlogProperty(BLOG_XMLRPC_ENTRY_EXTENSION_IP);
            if (BlojsomUtils.checkNullOrBlank(blogEntryExtension)) {
                blogEntryExtension = DEFAULT_BLOG_XMLRPC_ENTRY_EXTENSION;
            }

            String filename = getBlogEntryFilename(blogEntryDescription, blogEntryExtension);
            File blogFilename = new File(user.getBlog().getBlogHome() + BlojsomUtils.removeInitialSlash(blogCategoryName) + filename);
            _logger.debug("New blog entry file: " + blogFilename.toString());

            Map attributeMap = new HashMap();
            attributeMap.put(BlojsomMetaDataConstants.SOURCE_ATTRIBUTE, blogFilename);
            entry.setAttributes(attributeMap);

            try {
                entry.save(user);
                addOperationResultMessage(context, "Added blog entry: " + entry.getId());
            } catch (BlojsomException e) {
                _logger.error(e);
                addOperationResultMessage(context, "Unable to add blog entry to category: " + blogCategoryName);
            }

            httpServletRequest.setAttribute(PAGE_PARAM, EDIT_BLOG_ENTRIES_PAGE);
        }

        return entries;
    }

    /**
     * Return a filename appropriate for the blog entry content
     *
     * @param content Blog entry content
     * @param blogEntryExtension Extension to be used for the blog entry filename
     * @return Filename for the new blog entry
     */
    protected String getBlogEntryFilename(String content, String blogEntryExtension) {
        String hashable = content;

        if (content.length() > MAX_HASHABLE_LENGTH) {
            hashable = hashable.substring(0, MAX_HASHABLE_LENGTH);
        }

        String baseFilename = BlojsomUtils.digestString(hashable).toUpperCase();
        String filename = baseFilename + blogEntryExtension;
        return filename;
    }
}
