package org.ignition.blojsom.plugin;

import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.util.BlojsomConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * SimpleSearchPlugin
 *
 * @author David Czarnecki
 */
public class SimpleSearchPlugin implements BlojsomPlugin, BlojsomConstants {

    private Log _logger = LogFactory.getLog(SimpleSearchPlugin.class);

    /**
     *
     */
    public SimpleSearchPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     * @param blogProperties Read-only properties for the Blog
     * @throws BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, HashMap blogProperties) throws BlojsomPluginException {
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest Request
     * @param entries Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws BlojsomPluginException If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest, BlogEntry[] entries) throws BlojsomPluginException {
        String query = httpServletRequest.getParameter(QUERY_PARAM);
        if (query == null) {
            return entries;
        }

        ArrayList entriesMatchingQuery = new ArrayList(5);

        for (int i = 0; i < entries.length; i++) {
            BlogEntry entry = entries[i];
            if ((entry.getTitle().indexOf(query) != -1) || ((entry.getDescription().indexOf(query) != -1))) {
                entriesMatchingQuery.add(entry);
            }
        }

        if (entriesMatchingQuery.size() == 0) {
            return null;
        } else {
            return (BlogEntry[]) entriesMatchingQuery.toArray(new BlogEntry[entriesMatchingQuery.size()]);
        }
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws BlojsomPluginException If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws BlojsomPluginException {
    }
}
