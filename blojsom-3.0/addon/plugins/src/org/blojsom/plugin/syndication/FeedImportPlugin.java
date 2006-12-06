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
package org.blojsom.plugin.syndication;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.module.content.ContentModuleImpl;
import com.sun.syndication.feed.module.content.ContentModule;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.*;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.technorati.TechnoratiTagsPlugin;
import org.blojsom.plugin.syndication.module.*;
import org.blojsom.plugin.admin.WebAdminPlugin;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomMetaDataConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Feed import plugin
 *
 * @author David Czarnecki
 * @version $Id: FeedImportPlugin.java,v 1.8 2006-12-06 02:41:58 czarneckid Exp $
 * @since blojsom 3.0
 */
public class FeedImportPlugin extends WebAdminPlugin {

    private Log _logger = LogFactory.getLog(FeedImportPlugin.class);

    // Localization constants
    private static final String FAILED_FEED_IMPORT_PERMISSION_KEY = "failed.feed.import.permission.text";
    private static final String FAILED_FEED_IMPORT_ERROR_KEY = "failed.feed.import.error.text";
    private static final String FAILED_FEED_IMPORT_IO_KEY = "failed.feed.import.io.text";

    // Pages
    private static final String FEED_IMPORT_PAGE = "/org/blojsom/plugin/syndication/templates/feed-import";

    // Permissions
    private static final String FEED_IMPORT_PERMISSION = "feed_import_permission";

    // Form items
    private static final String IMPORT_URL = "import-url";

    // Actions
    private static final String FEED_IMPORT_ACTION = "feed-import";

    private Fetcher _fetcher;

    /**
     * Construct a new instance of the Feed import plugin
     */
    public FeedImportPlugin() {
    }

    /**
     * Return the display name for the plugin
     *
     * @return Display name for the plugin
     */
    public String getDisplayName() {
        return "Feed Import plugin";
    }

    /**
     * Return the name of the initial editing page for the plugin
     *
     * @return Name of the initial editing page for the plugin
     */
    public String getInitialPage() {
        return FEED_IMPORT_PAGE;
    }

    /**
     * Set the {@link Fetcher}
     *
     * @param fetcher {@link Fetcher}
     */
    public void setFetcher(Fetcher fetcher) {
        _fetcher = fetcher;
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param blog                {@link Blog} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws PluginException If there is an error processing the blog entries
     */
    public Entry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, Map context, Entry[] entries) throws PluginException {
        entries = super.process(httpServletRequest, httpServletResponse, blog, context, entries);

        String page = BlojsomUtils.getRequestValue(BlojsomConstants.PAGE_PARAM, httpServletRequest);
        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);

        String username = getUsernameFromSession(httpServletRequest, blog);
        if (!checkPermission(blog, null, username, FEED_IMPORT_PERMISSION)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            addOperationResultMessage(context, getAdminResource(FAILED_FEED_IMPORT_PERMISSION_KEY, FAILED_FEED_IMPORT_PERMISSION_KEY, blog.getBlogAdministrationLocale()));

            return entries;
        }

        if (ADMIN_LOGIN_PAGE.equals(page)) {
            return entries;
        } else if (FEED_IMPORT_ACTION.equals(action)) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User requested feed import action");
            }

            String importLocation = BlojsomUtils.getRequestValue(IMPORT_URL, httpServletRequest);
            if (!BlojsomUtils.checkNullOrBlank(importLocation)) {
                try {
                    URL importURL = new URL(importLocation);
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

                            Category category;
                            category = _fetcher.newCategory();
                            String categoryName = "/";
                            if (entryCategories.size() > 0) {
                                categoryName = ((SyndCategory) entryCategories.get(0)).getName();
                                categoryName = categoryName.replaceAll("[+]", " ");
                                if (categoryName != null) {
                                    if (!categoryName.startsWith("/")) {
                                        categoryName = "/" + categoryName;
                                    }

                                    if (!categoryName.endsWith("/")) {
                                        categoryName += "/";
                                    }
                                }
                            }

                            try {
                                category = _fetcher.loadCategory(blog, categoryName);
                                if (category == null) {
                                    category = _fetcher.newCategory();
                                    category.setBlogId(blog.getId());
                                    category.setName(categoryName);
                                    category.setDescription(categoryName.replaceAll("/", " "));
                                }
                            } catch (FetcherException e) {
                                if (_logger.isErrorEnabled()) {
                                    _logger.error(e);
                                }

                                category.setBlogId(blog.getId());
                                category.setName(categoryName);
                            }

                            try {
                                _fetcher.saveCategory(blog, category);
                                _fetcher.loadCategory(blog, category);
                            } catch (FetcherException e) {
                                _logger.error(e);
                                statusMessage.append(e.getMessage()).append("<br />");
                            }

                            Entry newEntry;
                            newEntry = _fetcher.newEntry();

                            newEntry.setBlogId(blog.getId());
                            newEntry.setTitle(entry.getTitle());
                            newEntry.setBlogCategoryId(category.getId());
                            newEntry.setBlogCategory(category);
                            newEntry.setDescription(entry.getDescription().getValue());
                            newEntry.setDate(entry.getPublishedDate());
                            newEntry.setModifiedDate(entry.getUpdatedDate());
                            newEntry.setStatus(BlojsomMetaDataConstants.PUBLISHED_STATUS);

                            BlojsomImplementation blojsomImplementation = (BlojsomImplementation) entry.getModule(Blojsom.BLOJSOM_URI);
                            if (blojsomImplementation != null) {
                                if (blojsomImplementation.getAuthor() != null) {
                                    newEntry.setAuthor(blojsomImplementation.getAuthor());
                                }

                                if (blojsomImplementation.getTechnoratiTags() != null) {
                                    Map metadata = newEntry.getMetaData();
                                    metadata.put(TechnoratiTagsPlugin.METADATA_TECHNORATI_TAGS, blojsomImplementation.getTechnoratiTags());
                                    newEntry.setMetaData(metadata);
                                }

                                if (blojsomImplementation.getPostSlug() != null) {
                                    newEntry.setPostSlug(blojsomImplementation.getPostSlug());
                                }

                                // Handle responses
                                if (blojsomImplementation.getAllowsComments()) {
                                    newEntry.setAllowComments(new Integer(1));
                                } else {
                                    newEntry.setAllowComments(new Integer(0));
                                }

                                if (blojsomImplementation.getAllowsTrackbacks()) {
                                    newEntry.setAllowTrackbacks(new Integer(1));
                                } else {
                                    newEntry.setAllowTrackbacks(new Integer(0));
                                }

                                if (blojsomImplementation.getAllowsPingbacks()) {
                                    newEntry.setAllowPingbacks(new Integer(1));
                                } else {
                                    newEntry.setAllowPingbacks(new Integer(0));
                                }

                                if (blojsomImplementation.getMetadata() != null && blojsomImplementation.getMetadata().size() > 0) {
                                    Map metadataForEntry = newEntry.getMetaData();
                                    List metadataItems = blojsomImplementation.getMetadata();
                                    for (int j = 0; j < metadataItems.size(); j++) {
                                        Metadata metadata = (Metadata) metadataItems.get(j);

                                        if (metadata.getKey() != null) {
                                            metadataForEntry.put(metadata.getKey(), metadata.getValue());
                                        }
                                    }

                                    newEntry.setMetaData(metadataForEntry);
                                }
                            }

                            ContentModuleImpl contentModule = (ContentModuleImpl) entry.getModule(ContentModule.URI);
                            if (contentModule != null) {
                                List encodeds = contentModule.getEncodeds();
                                if (encodeds != null && encodeds.size() > 0) {
                                    newEntry.setDescription("");

                                    StringBuffer description = new StringBuffer();
                                    for (int j = 0; j < encodeds.size(); j++) {
                                        String encodedContent = (String) encodeds.get(j);
                                        description.append(encodedContent).append(BlojsomConstants.LINE_SEPARATOR);
                                    }

                                    newEntry.setDescription(description.toString());
                                }
                            }

                            try {
                                _fetcher.saveEntry(blog, newEntry);
                                _fetcher.loadEntry(blog, newEntry);

                                if (blojsomImplementation != null) {
                                    if (blojsomImplementation.getComments() != null && blojsomImplementation.getComments().size() > 0) {
                                        List comments = blojsomImplementation.getComments();
                                        for (int j = 0; j < comments.size(); j++) {
                                            SimpleComment simpleComment = (SimpleComment) comments.get(j);
                                            Comment comment = _fetcher.newComment();

                                            comment.setAuthor(simpleComment.getAuthor());
                                            comment.setAuthorEmail(simpleComment.getAuthorEmail());
                                            comment.setAuthorURL(simpleComment.getAuthorURL());
                                            comment.setComment(simpleComment.getComment());
                                            comment.setCommentDate(simpleComment.getCommentDate());
                                            comment.setIp(simpleComment.getIp());
                                            comment.setStatus(simpleComment.getStatus());
                                            comment.setBlogId(blog.getId());
                                            comment.setBlogEntryId(newEntry.getId());

                                            if (simpleComment.getMetadata() != null && simpleComment.getMetadata().size() > 0) {
                                                Map metadataForComment = comment.getMetaData();
                                                List metadataItems = simpleComment.getMetadata();
                                                for (int k = 0; k < metadataItems.size(); k++) {
                                                    Metadata metadata = (Metadata) metadataItems.get(k);

                                                    if (metadata.getKey() != null) {
                                                        metadataForComment.put(metadata.getKey(), metadata.getValue());
                                                    }
                                                }

                                                comment.setMetaData(metadataForComment);
                                            }

                                            _fetcher.saveComment(blog, comment);
                                        }
                                    }

                                    if (blojsomImplementation.getTrackbacks() != null && blojsomImplementation.getTrackbacks().size() > 0) {
                                        List trackbacks = blojsomImplementation.getTrackbacks();
                                        for (int j = 0; j < trackbacks.size(); j++) {
                                            SimpleTrackback simpleTrackback = (SimpleTrackback) trackbacks.get(j);
                                            Trackback trackback = _fetcher.newTrackback();

                                            trackback.setBlogName(simpleTrackback.getBlogName());
                                            trackback.setExcerpt(simpleTrackback.getExcerpt());
                                            trackback.setUrl(simpleTrackback.getUrl());
                                            trackback.setTitle(simpleTrackback.getTitle());
                                            trackback.setIp(simpleTrackback.getIp());
                                            trackback.setTrackbackDate(simpleTrackback.getTrackbackDate());
                                            trackback.setStatus(simpleTrackback.getStatus());
                                            trackback.setBlogEntryId(newEntry.getId());
                                            trackback.setBlogId(blog.getId());

                                            if (simpleTrackback.getMetadata() != null && simpleTrackback.getMetadata().size() > 0) {
                                                Map metadataForTrackback = trackback.getMetaData();
                                                List metadataItems = simpleTrackback.getMetadata();
                                                for (int k = 0; k < metadataItems.size(); k++) {
                                                    Metadata metadata = (Metadata) metadataItems.get(k);

                                                    if (metadata.getKey() != null) {
                                                        metadataForTrackback.put(metadata.getKey(), metadata.getValue());
                                                    }
                                                }

                                                trackback.setMetaData(metadataForTrackback);
                                            }

                                            _fetcher.saveTrackback(blog, trackback);
                                        }
                                    }

                                    if (blojsomImplementation.getPingbacks() != null && blojsomImplementation.getPingbacks().size() > 0) {
                                        List pingbacks = blojsomImplementation.getPingbacks();
                                        for (int j = 0; j < pingbacks.size(); j++) {
                                            SimplePingback simplePingback = (SimplePingback) pingbacks.get(j);
                                            Pingback pingback = _fetcher.newPingback();

                                            pingback.setBlogName(simplePingback.getBlogName());
                                            pingback.setExcerpt(simplePingback.getExcerpt());
                                            pingback.setUrl(simplePingback.getUrl());
                                            pingback.setTitle(simplePingback.getTitle());
                                            pingback.setIp(simplePingback.getIp());
                                            pingback.setTrackbackDate(simplePingback.getPingbackDate());
                                            pingback.setStatus(simplePingback.getStatus());
                                            pingback.setSourceURI(simplePingback.getSourceURI());
                                            pingback.setTargetURI(simplePingback.getTargetURI());
                                            pingback.setBlogEntryId(newEntry.getId());
                                            pingback.setBlogId(blog.getId());

                                            if (simplePingback.getMetadata() != null && simplePingback.getMetadata().size() > 0) {
                                                Map metadataForPingback = pingback.getMetaData();
                                                List metadataItems = simplePingback.getMetadata();
                                                for (int k = 0; k < metadataItems.size(); k++) {
                                                    Metadata metadata = (Metadata) metadataItems.get(k);

                                                    if (metadata.getKey() != null) {
                                                        metadataForPingback.put(metadata.getKey(), metadata.getValue());
                                                    }
                                                }

                                                pingback.setMetaData(metadataForPingback);
                                            }

                                            _fetcher.savePingback(blog, pingback);
                                        }
                                    }
                                }
                            } catch (FetcherException e) {
                                _logger.error(e);
                                statusMessage.append(e.getMessage()).append("<br />");
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
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }

                    addOperationResultMessage(context, formatAdminResource(FAILED_FEED_IMPORT_ERROR_KEY, FAILED_FEED_IMPORT_ERROR_KEY, blog.getBlogAdministrationLocale(), new Object[]{e.getMessage()}));
                } catch (IOException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }

                    addOperationResultMessage(context, formatAdminResource(FAILED_FEED_IMPORT_IO_KEY, FAILED_FEED_IMPORT_IO_KEY, blog.getBlogAdministrationLocale(), new Object[]{e.getMessage()}));
                } catch (RuntimeException e) {
                }
            }
        }

        httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, FEED_IMPORT_PAGE);

        return entries;
    }
}
