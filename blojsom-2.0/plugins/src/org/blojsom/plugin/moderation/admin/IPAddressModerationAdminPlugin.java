package org.blojsom.plugin.moderation.admin;

import org.blojsom.plugin.admin.WebAdminPlugin;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * IP address moderation administration plugin
 *
 * @author David Czarnecki
 * @since blojsom 2.25
 * @version $Id: IPAddressModerationAdminPlugin.java,v 1.1 2005-05-06 20:59:03 czarneckid Exp $
 */
public class IPAddressModerationAdminPlugin extends WebAdminPlugin {

    private Log _logger = LogFactory.getLog(IPAddressModerationAdminPlugin.class);

    // Pages
    private static final String EDIT_IP_MODERATION_SETTINGS_PAGE = "/org/blojsom/plugin/moderation/admin/templates/admin-edit-ip-moderation-settings";

    /**
     * Create a new instance of the IP address moderation administration plugin
     */
    public IPAddressModerationAdminPlugin() {
    }

    /**
     * Return the display name for the plugin
     *
     * @return Display name for the plugin
     */
    public String getDisplayName() {
        return "IP Address Moderation plugin";
    }

    /**
     * Return the name of the initial editing page for the plugin
     *
     * @return Name of the initial editing page for the plugin
     */
    public String getInitialPage() {
        return EDIT_IP_MODERATION_SETTINGS_PAGE;
    }
}