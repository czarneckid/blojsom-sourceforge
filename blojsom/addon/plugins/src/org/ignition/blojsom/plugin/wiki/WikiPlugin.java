/*
 * Copyright 1999-2002 by General Electric Company,
 * 3135 Easton Turnpike Fairfield, CT 06431, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of General Electric Company.
 */
package org.ignition.blojsom.plugin.wiki;

import org.ignition.blojsom.plugin.BlojsomPlugin;
import org.ignition.blojsom.plugin.BlojsomPluginException;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.blog.BlogEntry;
import org.radeox.engine.context.RenderContext;
import org.radeox.engine.context.BaseRenderContext;
import org.radeox.engine.RenderEngine;
import org.radeox.EngineManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * WikiPlugin
 * 
 * @author David Czarnecki 
 * @version $Id: WikiPlugin.java,v 1.1 2003-06-17 04:51:12 czarneckid Exp $
 */
public class WikiPlugin implements BlojsomPlugin {

    private Log _logger = LogFactory.getLog(WikiPlugin.class);
    private static final String WIKI_EXTENSION = ".wiki";

    RenderEngine _engine;
    RenderContext _context;

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     * @param blog {@link Blog} instance
     * @throws BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, Blog blog) throws BlojsomPluginException {
        _engine = EngineManager.getInstance();
        _context = new BaseRenderContext();
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @param context Context
     * @param entries Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws BlojsomPluginException If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                               Map context, BlogEntry[] entries) throws BlojsomPluginException {
        for (int i = 0; i < entries.length; i++) {
            BlogEntry entry = entries[i];
            if (entry.getId().endsWith(WIKI_EXTENSION)) {
                String description = entry.getDescription();
                entry.setDescription(_engine.render(description, _context));
            }
        }

        return entries;
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
