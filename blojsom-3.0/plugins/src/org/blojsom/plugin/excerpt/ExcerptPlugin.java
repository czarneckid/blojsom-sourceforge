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
package org.blojsom.plugin.excerpt;

import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ExcerptPlugin
 *
 * @author David Czarnecki
 * @author Mark Lussier
 * @version $Id: ExcerptPlugin.java,v 1.2 2006-03-20 22:50:36 czarneckid Exp $
 * @since blojsom 3.0
 */
public class ExcerptPlugin implements Plugin {

    private static final String SHOWME_PARAM = "smm";

    private static final String EXCERPT_EXPRESSION = "(^|\\s).*<div class=\"excerpt\">(.*)</div>.*";
    private static final String SHOWME_START = "$2 &nbsp;<a class=\"smm\" href=\"";
    private static final String SHOWME_FINISH = "&amp;" + SHOWME_PARAM + "=y\">Read More</a>";

    /**
     * Default Constructor
     */
    public ExcerptPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error initializing the plugin
     */
    public void init() throws PluginException {
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
        String showme = BlojsomUtils.getRequestValue(SHOWME_PARAM, httpServletRequest);
        if (showme == null) {
            processEntries(blog, entries);
        }

        return entries;
    }

    /**
     * Process entries
     *
     * @param blog {@link Blog}
     * @param entries Array of {@link BlogEntry} objects
     */
    private void processEntries(Blog blog, Entry[] entries) {
        Pattern p = Pattern.compile(EXCERPT_EXPRESSION, Pattern.DOTALL);

        for (int i = 0; i < entries.length; i++) {
            Entry entry = entries[i];
            String originalDescription = entry.getDescription();
            Matcher m = p.matcher(originalDescription);
            if (m.matches()) {
                StringBuffer newDescription = new StringBuffer(m.replaceAll(SHOWME_START));
                newDescription.append(blog.getBlogURL()).append("?permalink=").append(entry.getPostSlug());
                newDescription.append(SHOWME_FINISH);
                entry.setDescription(newDescription.toString());
            }
        }
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws PluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error in finalizing this plugin
     */
    public void destroy() throws PluginException {
    }
}
