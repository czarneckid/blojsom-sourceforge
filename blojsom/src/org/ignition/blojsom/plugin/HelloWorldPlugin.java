/**
 * Copyright (c) 2003, David A. Czarnecki
 * All rights reserved.
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

import org.ignition.blojsom.blog.BlogEntry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import java.util.HashMap;

/**
 * HelloWorldPlugin
 *
 * @author David Czarnecki
 * @version $Id: HelloWorldPlugin.java,v 1.1 2003-02-25 20:40:59 czarneckid Exp $
 */
public class HelloWorldPlugin implements BlojsomPlugin {

    private Log _logger = LogFactory.getLog(HelloWorldPlugin.class);

    /**
     * Initializes the HelloWorldPlugin
     *
     * @param servletConfig Servlet configuration
     * @param blogProperties Blog properties
     * @throws BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, HashMap blogProperties) throws BlojsomPluginException {
    }

    /**
     * Changes all of the entries' descriptions to "Hello World"
     *
     * @param entries Blog entries
     * @return Blog entries where each description has been set to "Hello World"
     * @throws BlojsomPluginException If there is an error processing the entries
     */
    public BlogEntry[] process(BlogEntry[] entries) throws BlojsomPluginException {
        if (entries == null) {
            return null;
        }

        for (int i = 0; i < entries.length; i++) {
            BlogEntry entry = entries[i];
            entry.setDescription("Hello World!");
        }

        return entries;
    }

    /**
     * Perform any cleanup
     *
     * @throws BlojsomPluginException If there is an error in cleanup
     */
    public void cleanup() throws BlojsomPluginException {
    }
}
