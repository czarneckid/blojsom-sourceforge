package org.blojsom.plugin.xpath;

import org.apache.commons.jxpath.JXPathContext;
import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;
import org.blojsom.blog.BlojsomConfiguration;
import org.blojsom.plugin.BlojsomPlugin;
import org.blojsom.plugin.BlojsomPluginException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * XPathPlugin
 * @author Mark Lussier
 * @version $Id: XPathPlugin.java,v 1.1 2003-09-24 03:19:44 intabulas Exp $
 */

public class XPathPlugin implements BlojsomPlugin {

    private static final String XPATH_PARAM = "xpath";

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration {@link org.blojsom.blog.BlojsomConfiguration} information
     * @throws org.blojsom.plugin.BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws BlojsomPluginException {

        JXPathContext.newContext(BlogEntry.class);

    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @param user {@link org.blojsom.blog.BlogUser} instance
     * @param context Context
     * @param entries Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws BlojsomPluginException If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlogUser user, Map context, BlogEntry[] entries) throws BlojsomPluginException {

        BlogEntry[] results = entries;
        String xpath = httpServletRequest.getParameter(XPATH_PARAM);


        if (xpath != null) {
            BlogEntryWrapper entryWrapper = new BlogEntryWrapper(entries);
            JXPathContext xpathcontext = JXPathContext.newContext(entryWrapper);
            Iterator entryIterator = xpathcontext.iterate(xpath);
            List foundEntries = new ArrayList();
            while (entryIterator.hasNext()) {
                BlogEntry entry = (BlogEntry) entryIterator.next();
                foundEntries.add(entry);
            }

            if (foundEntries.size() == 0) {
                results = entries;
            } else {
                results = (BlogEntry[]) foundEntries.toArray();
            }

        } else {
            results = entries;
        }


        return results;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws BlojsomPluginException If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws BlojsomPluginException {

    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws BlojsomPluginException If there is an error in finalizing this plugin
     */
    public void destroy() throws BlojsomPluginException {
    }
}


