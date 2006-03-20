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
package org.blojsom.plugin.trackback;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.util.BlojsomConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AutoTrackbackPlugin
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: AutoTrackbackPlugin.java,v 1.1 2006-03-20 21:31:01 czarneckid Exp $
 */
public class AutoTrackbackPlugin implements Plugin {

    private Log _logger = LogFactory.getLog(AutoTrackbackPlugin.class);

    private static final int REGEX_OPTIONS = Pattern.DOTALL | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE;
    private static final Pattern RDF_OUTER_PATTERN = Pattern.compile("(<rdf:RDF.*?</rdf:RDF>).*?", REGEX_OPTIONS);
    private static final Pattern RDF_INNER_PATTERN = Pattern.compile("(<rdf:Description.*/>)", REGEX_OPTIONS);
    private static final Pattern DC_IDENTIFIER_PATTERN = Pattern.compile("dc:identifier=\"(.*)\"");
    private static final Pattern TRACKBACK_PING_PATTERN = Pattern.compile("trackback:ping=\"(.*)\"");
    private static final Pattern HREF_PATTERN = Pattern.compile("<\\s*a.*?href\\s*=\\s*\"(([^\"]+).*?)\"\\s*>", REGEX_OPTIONS);

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws org.blojsom.plugin.PluginException If there is an error initializing the plugin
     */
    public void init() throws PluginException {
    }

    /**
     * Perform the trackback autodiscovery process
     *
     * @param blog Blog information
     * @param blogEntry Blog entry
     */
    private void trackbackAutodiscovery(Blog blog, Entry blogEntry) {
        try {
            // Build the URL parameters for the trackback ping URL
            StringBuffer trackbackPingURLParameters = new StringBuffer();
            trackbackPingURLParameters.append("&").append(TrackbackPlugin.TRACKBACK_URL_PARAM).append("=").append(blogEntry.getId());
            trackbackPingURLParameters.append("&").append(TrackbackPlugin.TRACKBACK_TITLE_PARAM).append("=").append(URLEncoder.encode(blogEntry.getTitle(), BlojsomConstants.UTF8));
            trackbackPingURLParameters.append("&").append(TrackbackPlugin.TRACKBACK_BLOG_NAME_PARAM).append("=").append(URLEncoder.encode(blog.getBlogName(), BlojsomConstants.UTF8));

            String excerpt = blogEntry.getDescription().replaceAll("<.*?>", "");
            if (excerpt.length() > 255) {
                excerpt = excerpt.substring(0, 251);
                excerpt += "...";
            }
            trackbackPingURLParameters.append("&").append(TrackbackPlugin.TRACKBACK_EXCERPT_PARAM).append("=").append(URLEncoder.encode(excerpt, BlojsomConstants.UTF8));
            
            // Extract all the HREF links from the blog description
            Matcher hrefMatcher = HREF_PATTERN.matcher(blogEntry.getDescription());
            while (hrefMatcher.find()) {

                // If we have a group count of 2, the inner group will be the http:// reference
                // Read the entire contents of the URL into a buffer
                if (hrefMatcher.groupCount() == 2) {
                    String hyperlink = hrefMatcher.group(1);
                    _logger.debug("Found hyperlink: " + hyperlink);
                    BufferedReader br;
                    URL hyperlinkURL = new URL(hyperlink);
                    br = new BufferedReader(new InputStreamReader(hyperlinkURL.openStream()));
                    String html;
                    StringBuffer contents = new StringBuffer();
                    while ((html = br.readLine()) != null) {
                        contents.append(html).append("\n");
                    }

                    // Look for the Auto Trackback RDF in the HTML
                    Matcher rdfOuterMatcher = RDF_OUTER_PATTERN.matcher(contents.toString());
                    while (rdfOuterMatcher.find()) {
                        _logger.debug("Found outer RDF text in hyperlink");
                        for (int i = 0; i < rdfOuterMatcher.groupCount(); i++) {
                            String outerRdfText = rdfOuterMatcher.group(i);

                            // Look for the inner RDF description
                            Matcher rdfInnerMatcher = RDF_INNER_PATTERN.matcher(outerRdfText);
                            while (rdfInnerMatcher.find()) {
                                _logger.debug("Found inner RDF text in hyperlink");
                                for (int j = 0; j < rdfInnerMatcher.groupCount(); j++) {
                                    String innerRdfText = rdfInnerMatcher.group(j);

                                    // Look for a dc:identifier attribute which matches the current hyperlink
                                    Matcher dcIdentifierMatcher = DC_IDENTIFIER_PATTERN.matcher(innerRdfText);
                                    if (dcIdentifierMatcher.find()) {
                                        String dcIdentifier = dcIdentifierMatcher.group(1);

                                        // If we find a match, send a trackback ping to the
                                        if (dcIdentifier.equals(hyperlink)) {
                                            _logger.debug("Matched dc:identifier to hyperlink");
                                            Matcher trackbackPingMatcher = TRACKBACK_PING_PATTERN.matcher(innerRdfText);
                                            if (trackbackPingMatcher.find()) {
                                                StringBuffer trackbackPingURL = new StringBuffer(trackbackPingMatcher.group(1));

                                                _logger.debug("Automatically sending trackback ping to URL: " + trackbackPingURL.toString());
                                                URL trackbackUrl = new URL(trackbackPingURL.toString());

                                                // Open a connection to the trackback URL and read its input
                                                HttpURLConnection trackbackUrlConnection = (HttpURLConnection) trackbackUrl.openConnection();
                                                trackbackUrlConnection.setRequestMethod("POST");
                                                trackbackUrlConnection.setRequestProperty("Content-Encoding", BlojsomConstants.UTF8);
                                                trackbackUrlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                                trackbackUrlConnection.setRequestProperty("Content-Length", "" + trackbackPingURLParameters.length());
                                                trackbackUrlConnection.setDoOutput(true);
                                                trackbackUrlConnection.getOutputStream().write(trackbackPingURLParameters.toString().getBytes(BlojsomConstants.UTF8));
                                                trackbackUrlConnection.connect();
                                                BufferedReader trackbackStatus = new BufferedReader(new InputStreamReader(trackbackUrlConnection.getInputStream()));
                                                String line;
                                                StringBuffer status = new StringBuffer();
                                                while ((line = trackbackStatus.readLine()) != null) {
                                                    status.append(line).append("\n");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            _logger.error(e);
        }
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
        for (int i = 0; i < entries.length; i++) {
            Entry entry = entries[i];
            if (entry.getMetaData() != null) {
                Map entryMetaData = entry.getMetaData();
                if (entryMetaData.containsKey("auto-trackback") && !entryMetaData.containsKey("auto-trackback-complete")) {
                    trackbackAutodiscovery(blog, entry);
                    entryMetaData.put("auto-trackback-complete", "true");
                }
            } else {
                _logger.debug("Skipping blog entry for autotrackback: " + entry.getId());
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
