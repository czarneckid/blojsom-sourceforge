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
package org.ignition.blojsom.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.util.BlojsomConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic Referer Plugin
 * <p />
 * This plugin will log to a file all the http referer headers it encounters. It dumps this
 * log to what ever file you set in the <i>referer-log</i>
 * init-param in <i>web.xml</i>. If no file is setup, it will dump it to the log as a backup
 *
 * @author Mark Lussier
 * @version $Id: RefererLogPlugin.java,v 1.13 2003-03-24 22:21:03 intabulas Exp $
 */
public class RefererLogPlugin implements BlojsomPlugin {

    /**
     * HTTP Header for Referer Information
     */
    private static final String HEADER_REFERER = "referer";
    private static final String REFERER_LOG_IP = "referer-log";
    private static final String REFERER_CONTEXT_NAME = "REFERER_HISTORY";
    private static final String BUG_FIX = "null";

    /**
     * Fully qualified filename to write referers to
     */
    private String _refererlog = null;

    /**
     * Contains the blog url to filter referes from sub-category entries
     */
    private String _blogurlfilter = null;

    /**
     * Referer History and Count
     */
    private Map _refererhistory = null;

    /**
     * Current referer list (will be persisted on destroy())
     */
    private List _referlist = null;

    /**
     * Logger instance
     */
    private Log _logger = LogFactory.getLog(RefererLogPlugin.class);

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     * @param blogProperties Read-only properties for the Blog
     * @throws BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, HashMap blogProperties) throws BlojsomPluginException {

        _blogurlfilter = (String) blogProperties.get(BlojsomConstants.BLOG_URL_IP);

        _refererlog = servletConfig.getInitParameter(REFERER_LOG_IP);
        if (_refererlog == null || "".equals(_refererlog)) {
            throw new BlojsomPluginException("No value given for: " + REFERER_LOG_IP + " configuration parameter");
        }

        _refererhistory = new HashMap(20);
        _referlist = new ArrayList(25);

        loadRefererLog(_refererlog);
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest Request
     * @param context Context
     * @param entries Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws BlojsomPluginException If there is an error processing the blog entries
I  close     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest, Map context, BlogEntry[] entries) throws BlojsomPluginException {
        String _referer = httpServletRequest.getHeader(HEADER_REFERER);

        if ((_referer != null) && (!_referer.startsWith(_blogurlfilter))) {
            _logger.info("HTTP Referer is " + _referer);

            if (_refererhistory.containsKey(_referer)) {
                int _count = ((Integer) _refererhistory.get(_referer)).intValue();
                _refererhistory.put(_referer, new Integer(_count + 1));
            } else {
                _refererhistory.put(_referer, new Integer(1));
            }
            _referlist.add(_referer);
        }
        context.put(REFERER_CONTEXT_NAME, _refererhistory);

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
     * Loads the saved referer log from disk after a blojsom restart.
     *
     * @param refererlog Fully qualified path to the refer log file
     */
    protected void loadRefererLog(String refererlog) {
        File _refererfile = new File(refererlog);

        if (_refererfile.exists()) {

            try {
                BufferedReader _br = new BufferedReader(new FileReader(_refererfile));
                String _entry = null;

                while (((_entry = _br.readLine()) != null)) {
                    if (!_entry.equals(BUG_FIX) && (!_entry.startsWith(_blogurlfilter))) {
                        if (_refererhistory.containsKey(_entry)) {
                            int _count = ((Integer) _refererhistory.get(_entry)).intValue();
                            _refererhistory.put(_entry, new Integer(_count + 1));
                        } else {
                            _refererhistory.put(_entry, new Integer(1));
                        }
                    }
                }
                _br.close();
            } catch (IOException e) {
                _logger.error(e);
            }
        }
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws BlojsomPluginException If there is an error in finalizing this plugin
     */
    public void destroy() throws BlojsomPluginException {
        // Writer referer cache out to disk
        _logger.info("Writing referer list to " + _refererlog);

        StringBuffer _referers;
        if (_referlist.size() > 0) {
            _referers = new StringBuffer();
            for (int x = 0; x < _referlist.size(); x++) {
                _referers.append((String) _referlist.get(x)).append("\n");
            }
            try {
                FileOutputStream _fos = new FileOutputStream(_refererlog, true);
                _fos.write(_referers.toString().getBytes());
                _fos.close();
            } catch (IOException e) {
                _logger.error(e);
            }
        }
    }
}
