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
package org.blojsom.plugin.moderation.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.WebAdminPlugin;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spam phrase moderation administration plugin
 *
 * @author David Czarnecki
 * @version $Id: SpamPhraseModerationAdminPlugin.java,v 1.3 2007-01-17 02:35:12 czarneckid Exp $
 * @since blojsom 3.0
 */
public class SpamPhraseModerationAdminPlugin extends WebAdminPlugin {

    private Log _logger = LogFactory.getLog(SpamPhraseModerationAdminPlugin.class);

    private static final String SPAM_PHRASE_BLACKLIST_IP = "spam-phrase-blacklist";

    // Localization constants
    private static final String FAILED_SPAM_PHRASE_PERMISSION_KEY = "failed.spam.phrase.permission.text";
    private static final String ADDED_SPAM_PHRASE_KEY = "added.spam.phrase.text";
    private static final String SPAM_PHRASE_ALREADY_ADDED_KEY = "spam.phrase.already.added.text";
    private static final String DELETED_SPAM_PHRASE_KEY = "deleted.spam.phrase.text";
    private static final String NO_SPAM_PHRASES_TO_DELETE_KEY = "no.spam.phrases.to.delete.text";

    // Context
    private static final String BLOJSOM_PLUGIN_SPAM_PHRASES = "BLOJSOM_PLUGIN_SPAM_PHRASES";

    // Pages
    private static final String EDIT_SPAM_PHRASE_MODERATION_SETTINGS_PAGE = "/org/blojsom/plugin/moderation/admin/templates/admin-edit-spam-phrase-moderation-settings";

    // Form itmes
    private static final String SPAM_PHRASE = "spam-phrase";

    // Actions
    private static final String ADD_SPAM_PHRASE_ACTION = "add-spam-phrase";
    private static final String DELETE_SPAM_PHRASE_ACTION = "delete-spam-phrase";

    // Permissions
    private static final String SPAM_PHRASE_MODERATION_PERMISSION = "spam_phrase_moderation";

    private Fetcher _fetcher;

    /**
     * Create a new instance of the spam phrase moderation administration plugin
     */
    public SpamPhraseModerationAdminPlugin() {
    }

    /**
     * Return the display name for the plugin
     *
     * @return Display name for the plugin
     */
    public String getDisplayName() {
        return "Spam Phrase Moderation plugin";
    }

    /**
     * Return the name of the initial editing page for the plugin
     *
     * @return Name of the initial editing page for the plugin
     */
    public String getInitialPage() {
        return EDIT_SPAM_PHRASE_MODERATION_SETTINGS_PAGE;
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
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error initializing the plugin
     */
    public void init() throws PluginException {
        super.init();
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

        String username = getUsernameFromSession(httpServletRequest, blog);
        if (!checkPermission(blog, null, username, SPAM_PHRASE_MODERATION_PERMISSION)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_ADMINISTRATION_PAGE);
            addOperationResultMessage(context, getAdminResource(FAILED_SPAM_PHRASE_PERMISSION_KEY, FAILED_SPAM_PHRASE_PERMISSION_KEY, blog.getBlogAdministrationLocale()));

            return entries;
        }

        if (ADMIN_LOGIN_PAGE.equals(page)) {
            return entries;
        } else {
            String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);
            List spamPhrases = loadSpamPhrases(blog);
            String spamPhrase = BlojsomUtils.getRequestValue(SPAM_PHRASE, httpServletRequest);

            if (ADD_SPAM_PHRASE_ACTION.equals(action)) {
                if (!spamPhrases.contains(spamPhrase)) {
                    spamPhrases.add(spamPhrase);
                    blog.setProperty(SPAM_PHRASE_BLACKLIST_IP, BlojsomUtils.listToString(spamPhrases, "\n"));

                    try {
                        _fetcher.saveBlog(blog);
                    } catch (FetcherException e) {
                        if (_logger.isErrorEnabled()) {
                            _logger.error(e);
                        }
                    }

                    addOperationResultMessage(context, formatAdminResource(ADDED_SPAM_PHRASE_KEY, ADDED_SPAM_PHRASE_KEY, blog.getBlogAdministrationLocale(), new Object[] {spamPhrase}));
                } else {
                    addOperationResultMessage(context, formatAdminResource(SPAM_PHRASE_ALREADY_ADDED_KEY, SPAM_PHRASE_ALREADY_ADDED_KEY, blog.getBlogAdministrationLocale(), new Object[] {spamPhrase}));
                }
            } else if (DELETE_SPAM_PHRASE_ACTION.equals(action)) {
                String[] spamPhrasesToDelete = httpServletRequest.getParameterValues(SPAM_PHRASE);
                if (spamPhrasesToDelete != null && spamPhrasesToDelete.length > 0) {
                    for (int i = 0; i < spamPhrasesToDelete.length; i++) {
                        spamPhrases.set(Integer.parseInt(spamPhrasesToDelete[i]), null);
                    }

                    spamPhrases = BlojsomUtils.removeNullValues(spamPhrases);
                    blog.setProperty(SPAM_PHRASE_BLACKLIST_IP, BlojsomUtils.listToString(spamPhrases, "\n"));

                    try {
                        _fetcher.saveBlog(blog);
                    } catch (FetcherException e) {
                        if (_logger.isErrorEnabled()) {
                            _logger.error(e);
                        }
                    }

                    addOperationResultMessage(context, formatAdminResource(DELETED_SPAM_PHRASE_KEY, DELETED_SPAM_PHRASE_KEY, blog.getBlogAdministrationLocale(), new Object[] {new Integer(spamPhrasesToDelete.length)}));
                } else {
                    addOperationResultMessage(context, getAdminResource(NO_SPAM_PHRASES_TO_DELETE_KEY, NO_SPAM_PHRASES_TO_DELETE_KEY, blog.getBlogAdministrationLocale()));
                }
            }

            context.put(BLOJSOM_PLUGIN_SPAM_PHRASES, spamPhrases);
        }

        return entries;
    }

    /**
     * Load the list of spam phrases from the blog
     *
     * @param blog {@link blog}
     * @return List of spam phrases
     */
    protected List loadSpamPhrases(Blog blog) {
        ArrayList spamPhrases = new ArrayList(25);

        String spamPhrasesValues = blog.getProperty(SPAM_PHRASE_BLACKLIST_IP);
        if (!BlojsomUtils.checkNullOrBlank(spamPhrasesValues)) {
            try {
                StringReader stringReader = new StringReader(spamPhrasesValues);
                BufferedReader br = new BufferedReader(stringReader);
                String phrase;

                while ((phrase = br.readLine()) != null) {
                    spamPhrases.add(phrase);
                }

                br.close();
            } catch (IOException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            }
        }

        return spamPhrases;
    }
}