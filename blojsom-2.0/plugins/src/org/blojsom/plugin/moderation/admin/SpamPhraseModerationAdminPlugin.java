package org.blojsom.plugin.moderation.admin;

import org.blojsom.plugin.admin.WebAdminPlugin;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Spam phrase moderation administration plugin
 *
 * @author David Czarnecki
 * @since blojsom 2.25
 * @version $Id: SpamPhraseModerationAdminPlugin.java,v 1.1 2005-05-06 20:59:03 czarneckid Exp $
 */
public class SpamPhraseModerationAdminPlugin extends WebAdminPlugin {

    private Log _logger = LogFactory.getLog(SpamPhraseModerationAdminPlugin.class);

    // Pages
    private static final String EDIT_SPAM_PHRASE_MODERATION_SETTINGS_PAGE = "/org/blojsom/plugin/moderation/admin/templates/admin-edit-spam-phrase-moderation-settings";

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
}