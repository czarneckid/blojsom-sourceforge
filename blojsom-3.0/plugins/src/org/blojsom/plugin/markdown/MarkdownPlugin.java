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
package org.blojsom.plugin.markdown;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

/**
 * MarkdownPlugin
 * <p/>
 * To use the Markdown plugin, you will need to download the Markdown tool from
 * <a href="http://daringfireball.net/projects/markdown/">John Gruber's Markdown site</a>.
 *
 * @author David Czarnecki
 * @version $Id: MarkdownPlugin.java,v 1.2 2006-03-20 22:50:45 czarneckid Exp $
 * @since blojsom 3.0
 */
public class MarkdownPlugin implements Plugin {

    private Log _logger = LogFactory.getLog(MarkdownPlugin.class);

    /**
     * Metadata key to identify a Markdown post
     */
    private static final String METADATA_RUN_MARKDOWN = "run-markdown";

    /**
     * Extension of Markdown post
     */
    private static final String MARKDOWN_EXTENSION = ".markdown";

    /**
     * Initialization parameter for the command to start a Markdown session
     */
    private static final String PLUGIN_MARKDOWN_EXECUTION_IP = "plugin-markdown-execution";

    private String _markdownExecution;
    private ServletConfig _servletConfig;

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws PluginException If there is an error initializing the plugin
     */
    public void init() throws PluginException {
        _markdownExecution = _servletConfig.getInitParameter(PLUGIN_MARKDOWN_EXECUTION_IP);

        if (BlojsomUtils.checkNullOrBlank(_markdownExecution)) {
            if (_logger.isErrorEnabled()) {
                _logger.error("No Markdown execution string provided. Use initialization parameter: " + PLUGIN_MARKDOWN_EXECUTION_IP);
            }
        }
    }

    /**
     * Set the {@link ServletConfig}
     *
     * @param servletConfig {@link ServletConfig}
     */
    public void setServletConfig(ServletConfig servletConfig) {
        _servletConfig = servletConfig;
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
        if (!BlojsomUtils.checkNullOrBlank(_markdownExecution)) {
            for (int i = 0; i < entries.length; i++) {
                Entry entry = entries[i];

                if ((entry.getPostSlug().endsWith(MARKDOWN_EXTENSION) || BlojsomUtils.checkMapForKey(entry.getMetaData(), METADATA_RUN_MARKDOWN)))
                {
                    try {
                        Process process = Runtime.getRuntime().exec(_markdownExecution);
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), BlojsomConstants.UTF8));
                        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), BlojsomConstants.UTF8));
                        bw.write(entry.getDescription());
                        bw.close();
                        String input;
                        StringBuffer collectedDescription = new StringBuffer();

                        while ((input = br.readLine()) != null) {
                            collectedDescription.append(input).append(BlojsomConstants.LINE_SEPARATOR);
                        }

                        entry.setDescription(collectedDescription.toString());
                        br.close();
                    } catch (IOException e) {
                        if (_logger.isErrorEnabled()) {
                            _logger.error(e);
                        }
                    }
                }
            }
        }

        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws PluginException If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws PluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws PluginException If there is an error in finalizing this plugin
     */
    public void destroy() throws PluginException {
    }
}