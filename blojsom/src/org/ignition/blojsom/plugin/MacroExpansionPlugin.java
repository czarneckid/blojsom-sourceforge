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
 * Neither the name of the "David A. Czarnecki" nor the names of
 * its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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
package org.ignition.blojsom.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * MacroExpansionPlugin
 *
 * @author Mark Lussier
 * @version $Id: MacroExpansionPlugin.java,v 1.2 2003-02-26 00:40:01 czarneckid Exp $
 */
public class MacroExpansionPlugin implements BlojsomPlugin {

    private static final String BLOG_MACRO_CONFIGURATION_IP = "blog-macros-expansion";

    private Log _logger = LogFactory.getLog(MacroExpansionPlugin.class);
    private Map _macros;

    /**
     * RegExt to identify Macros as $MACRO$ and DOES NOT ignore escaped $'s
     */
    private static final String MACRO_REGEX = "(\\$[^\\$]*\\$)";
    private Pattern _macro;

    public MacroExpansionPlugin() {
        _macro = Pattern.compile(MACRO_REGEX);
    }

    /**
     * Load the Macro Mappings
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     */
    private void loadMacros(ServletConfig servletConfig) {
        String macroConfiguration = servletConfig.getInitParameter(BLOG_MACRO_CONFIGURATION_IP);
        Properties macroProperties = new Properties();
        InputStream is = servletConfig.getServletContext().getResourceAsStream(macroConfiguration);
        try {
            macroProperties.load(is);
            Iterator handlerIterator = macroProperties.keySet().iterator();
            while (handlerIterator.hasNext()) {
                String keyword = (String) handlerIterator.next();
                _macros.put(keyword, macroProperties.get(keyword));
                _logger.info("Adding macro [" + keyword + "] with value of  [" + macroProperties.get(keyword) + "]");
            }
        } catch (IOException e) {
            _logger.error(e);
        }
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     * @param blogProperties Read-only properties for the Blog
     * @throws BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, HashMap blogProperties) throws BlojsomPluginException {
        _macros = new HashMap(10);

        loadMacros(servletConfig);
    }

    /**
     * Replace Macro Tokens with their values
     * @param content Entry to Process
     * @return The Macro expanded String
     * @todo Very Sub-Optimal
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
     * @param entries Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws BlojsomPluginException If there is an error processing the blog entries
     */
    public BlogEntry[] process(BlogEntry[] entries) throws BlojsomPluginException {
        if (entries == null) {
            return null;
        }

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
     * @throws BlojsomPluginException If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws BlojsomPluginException {
        _macros.clear();
    }
}
