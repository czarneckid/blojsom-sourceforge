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
package org.ignition.blojsom.plugin.macro;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.util.BlojsomUtils;
import org.ignition.blojsom.plugin.BlojsomPlugin;
import org.ignition.blojsom.plugin.BlojsomPluginException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Macro Expansion Plugin
 *
 * @author Mark Lussier
 * @version $Id: MacroExpansionPlugin.java,v 1.2 2003-04-19 02:44:57 czarneckid Exp $
 */
public class MacroExpansionPlugin implements BlojsomPlugin {

    private static final String BLOG_MACRO_CONFIGURATION_IP = "plugin-macros-expansion";

    private Log _logger = LogFactory.getLog(MacroExpansionPlugin.class);
    private Map _macros;

    /**
     * Regular expression to identify macros as $MACRO$ and DOES NOT ignore escaped $'s
     */
    private static final String MACRO_REGEX = "(\\$[^\\$]*\\$)";
    private Pattern _macro;

    /**
     * Default constructor. Compiles the macro regular expression pattern, $MACRO$
     */
    public MacroExpansionPlugin() {
        _macro = Pattern.compile(MACRO_REGEX);
    }

    /**
     * Load the macro mappings
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     */
    private void loadMacros(ServletConfig servletConfig) throws BlojsomPluginException {
        String macroConfiguration = servletConfig.getInitParameter(BLOG_MACRO_CONFIGURATION_IP);
        if (macroConfiguration == null || "".equals(macroConfiguration)) {
            throw new BlojsomPluginException("No value given for: " + BLOG_MACRO_CONFIGURATION_IP + " configuration parameter");
        }

        Properties macroProperties = new Properties();
        InputStream is = servletConfig.getServletContext().getResourceAsStream(macroConfiguration);
        try {
            macroProperties.load(is);
            is.close();
            Iterator handlerIterator = macroProperties.keySet().iterator();
            while (handlerIterator.hasNext()) {
                String keyword = (String) handlerIterator.next();
                _macros.put(keyword, macroProperties.get(keyword));
                _logger.info("Adding macro [" + keyword + "] with value of [" + macroProperties.get(keyword) + "]");
            }
        } catch (IOException e) {
            throw new BlojsomPluginException(e);
        }
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     * @param blog {@link Blog} instance
     * @throws org.ignition.blojsom.plugin.BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, Blog blog) throws BlojsomPluginException {
        _macros = new HashMap(10);

        loadMacros(servletConfig);
    }

    /**
     * Expand macro tokens in an entry
     *
     * @param content Entry to process
     * @return The macro expanded string
     */
    private String replaceMacros(String content) {
        Matcher _matcher = _macro.matcher(content);

        while (_matcher.find()) {
            String _token = _matcher.group();
            String _macro = _token.substring(1, _token.length() - 1).toLowerCase();
            if (_macros.containsKey(_macro)) {
                content = BlojsomUtils.replace(content, _token, (String) _macros.get(_macro));
            }
        }

        return content;
    }

    /**
     * Process the blog entries. Expands any macros in title and body.
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @param context Context
     * @param entries Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws org.ignition.blojsom.plugin.BlojsomPluginException If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                               Map context, BlogEntry[] entries) throws BlojsomPluginException {
        for (int i = 0; i < entries.length; i++) {
            BlogEntry entry = entries[i];
            entry.setTitle(replaceMacros(entry.getTitle()));
            entry.setDescription(replaceMacros(entry.getDescription()));
        }

        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws org.ignition.blojsom.plugin.BlojsomPluginException If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws BlojsomPluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws org.ignition.blojsom.plugin.BlojsomPluginException If there is an error in finalizing this plugin
     */
    public void destroy() throws BlojsomPluginException {
    }
}


