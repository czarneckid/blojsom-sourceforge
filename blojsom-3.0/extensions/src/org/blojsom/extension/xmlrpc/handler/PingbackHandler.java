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
package org.blojsom.extension.xmlrpc.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.blojsom.blog.Entry;
import org.blojsom.blog.Pingback;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.common.ResponseConstants;
import org.blojsom.plugin.pingback.PingbackPlugin;
import org.blojsom.plugin.pingback.event.PingbackAddedEvent;
import org.blojsom.plugin.pingback.event.PingbackResponseSubmissionEvent;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pingback handler provides support for the <a href="http://www.hixie.ch/specs/pingback/pingback">Pingback 1.0</a>
 * specification.
 *
 * @author David Czarnecki
 * @version $Id: PingbackHandler.java,v 1.2 2006-03-23 17:26:51 czarneckid Exp $
 * @since blojsom 3.0
 */
public class PingbackHandler extends APIHandler {

    private static final Log _logger = LogFactory.getLog(PingbackHandler.class);

    private static final String TITLE_PATTERN = "<title>(.*)</title>";

    protected static final String API_NAME = "pingback";

    protected static final int PINGBACK_GENERIC_FAULT_CODE = 0;
    protected static final int PINGBACK_SOURCE_URI_NON_EXISTENT_CODE = 16;
    protected static final int PINGBACK_NO_LINK_TO_TARGET_URI_CODE = 17;
    protected static final int PINGBACK_TARGET_URI_NON_EXISTENT_CODE = 32;
    protected static final int PINGBACK_TARGET_URI_NOT_ENABLED_CODE = 33;
    protected static final int PINGBACK_ALREADY_REGISTERED_CODE = 48;
    protected static final int PINGBACK_ACCESS_DENIED_CODE = 49;
    protected static final int PINGBACK_UPSTREAM_SERVER_ERROR_CODE = 50;

    protected static final String PINGBACK_SOURCE_URI_METADATA = "pingback-source-uri";
    protected static final String PINGBACK_TARGET_URI_METADATA = "pingback-target-uri";

    /**
     * Construct a new Pingback handler
     */
    public PingbackHandler() {
    }

    /**
     * Gets the name of API Handler. Used to bind to XML-RPC
     *
     * @return The API Name (ie: pingback)
     */
    public String getName() {
        return API_NAME;
    }

    /**
     * Try to find the &lt;title&gt;&lt/title&gt; tags from the source text
     *
     * @param source Source URI text
     * @return Title of text or <code>null</code> if title tags are not found
     */
    protected String getTitleFromSource(String source) {
        String title = null;
        Pattern titlePattern = Pattern.compile(TITLE_PATTERN, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNICODE_CASE);
        Matcher titleMatcher = titlePattern.matcher(source);
        if (titleMatcher.find()) {
            title = titleMatcher.group(1);
        }

        return title;
    }

    /**
     * Try to extract an excerpt from the source text. Currently looks ahead 200 and ahead 200 characters from
     * the location of the targetURI within the source.
     *
     * @param source    Source URI text
     * @param targetURI Target URI from which to start the excerpt
     * @return Excerpt of text or <code>null</code> if we cannot find the targetURI
     */
    protected String getExcerptFromSource(String source, String targetURI) {
        String excerpt = null;

        int startOfTarget = source.indexOf(targetURI);
        if (startOfTarget != -1) {
            int startOfExcerpt = startOfTarget - 200;
            if (startOfExcerpt < 0) {
                startOfExcerpt = 0;
            }

            int endOfExcerpt = startOfTarget + 200;
            if (endOfExcerpt > source.length()) {
                endOfExcerpt = source.length();
            }

            excerpt = source.substring(startOfExcerpt, endOfExcerpt);
            excerpt = BlojsomUtils.stripHTML(excerpt);
            int firstSpace = excerpt.indexOf(' ') + 1;
            int lastSpace = excerpt.lastIndexOf(' ');
            if (-1 == lastSpace || lastSpace < firstSpace) lastSpace = excerpt.length();
            excerpt = excerpt.substring(firstSpace, lastSpace);
        }

        return excerpt;
    }

    /**
     * Notifies the server that a link has been added to sourceURI, pointing to targetURI.
     *
     * @param sourceURI The absolute URI of the post on the source page containing the link to the target site.
     * @param targetURI The absolute URI of the target of the link, as given on the source page.
     * @return
     */
    public String ping(String sourceURI, String targetURI) throws XmlRpcException {
        if (_logger.isDebugEnabled()) {
            _logger.debug("Pingback from: " + sourceURI + " to: " + targetURI);
        }

        if (BlojsomUtils.checkNullOrBlank(sourceURI)) {
            if (_logger.isErrorEnabled()) {
                _logger.error("Pingback must include a source URI");
            }

            throw new XmlRpcException(PINGBACK_SOURCE_URI_NON_EXISTENT_CODE, "Pingback must include a source URI");
        }

        // Fetch sourceURI to make sure there is a link to the targetURI
        StringBuffer sourcePage;
        try {
            URL source = new URL(sourceURI);
            HttpURLConnection sourceConnection = (HttpURLConnection) source.openConnection();
            sourceConnection.setRequestMethod("GET");
            sourceConnection.connect();
            BufferedReader sourceReader = new BufferedReader(new InputStreamReader(sourceConnection.getInputStream(), BlojsomConstants.UTF8));
            String line;
            sourcePage = new StringBuffer();

            while ((line = sourceReader.readLine()) != null) {
                sourcePage.append(line);
                sourcePage.append(BlojsomConstants.LINE_SEPARATOR);
            }
        } catch (IOException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new XmlRpcException(PINGBACK_GENERIC_FAULT_CODE, "Unable to retrieve source URI");
        }

        // Check that the sourceURI contains a link to the targetURI
        if (sourcePage.indexOf(targetURI) == -1) {
            if (_logger.isErrorEnabled()) {
                _logger.error("Target URI not found in Source URI");
            }

            throw new XmlRpcException(PINGBACK_NO_LINK_TO_TARGET_URI_CODE, "Target URI not found in source URI");
        }

        // Check targetURI exists and is a valid entry
        try {
            URL target = new URL(targetURI);
            HttpURLConnection httpURLConnection = (HttpURLConnection) target.openConnection();
            httpURLConnection.setRequestMethod("HEAD");
            httpURLConnection.connect();

            if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                if (_logger.isErrorEnabled()) {
                    _logger.error("Target URI does not exist");
                }

                throw new XmlRpcException(PINGBACK_TARGET_URI_NON_EXISTENT_CODE, "Target URI does not exist");
            }
        } catch (IOException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new XmlRpcException(PINGBACK_GENERIC_FAULT_CODE, "Unable to retrieve target URI");
        }

        String permalink = BlojsomUtils.getRequestValue(BlojsomConstants.PERMALINK_PARAM, _httpServletRequest);
        if (BlojsomUtils.checkNullOrBlank(permalink)) {
            _logger.error("Permalink is null or blank: " +  permalink);
            throw new XmlRpcException(PINGBACK_GENERIC_FAULT_CODE, "Unable to retrieve target URI");
        }

        // Check that the resource hasn't already been registered
        try {
            Pingback pingback = _fetcher.loadPingback(_blog, sourceURI, targetURI);
            if (pingback != null) {
                throw new XmlRpcException(PINGBACK_ALREADY_REGISTERED_CODE, "Pingback already registered");
            }
        } catch (FetcherException e) {
        }

        // Check the resource is pingback-enabled
        try {
            Entry entry = _fetcher.loadEntry(_blog, permalink);

            if (_blog.getBlogPingbacksEnabled().booleanValue() && entry.allowsPingbacks().booleanValue()) {
                // Record pingback
                Pingback pingback = _fetcher.newPingback();
                pingback.setBlogEntryId(entry.getId());
                pingback.setBlogId(_blog.getBlogId());
                pingback.setEntry(entry);
                pingback.setIp(_httpServletRequest.getRemoteAddr());
                pingback.setStatus(ResponseConstants.NEW_STATUS);

                Map pingbackMetaData = new HashMap();
                pingbackMetaData.put(PINGBACK_SOURCE_URI_METADATA, sourceURI);
                pingbackMetaData.put(PINGBACK_TARGET_URI_METADATA, targetURI);

                _eventBroadcaster.processEvent(new PingbackResponseSubmissionEvent(this, new Date(), _blog, _httpServletRequest, _httpServletResponse, getTitleFromSource(sourcePage.toString()), getTitleFromSource(sourcePage.toString()), sourceURI, getExcerptFromSource(sourcePage.toString(), targetURI), entry, pingbackMetaData));

                // Check to see if the trackback should be destroyed (not saved) automatically
                if (!pingbackMetaData.containsKey(PingbackPlugin.BLOJSOM_PLUGIN_PINGBACK_METADATA_DESTROY)) {

                    Integer status = addPingback(entry.getTitle(), getExcerptFromSource(sourcePage.toString(), targetURI), sourceURI, getTitleFromSource(sourcePage.toString()), pingbackMetaData, pingback);

                    if (status.intValue() != 0) {
                        throw new XmlRpcException(status.intValue(), "Unknown exception occurred");
                    } else {
                        _eventBroadcaster.broadcastEvent(new PingbackAddedEvent(this, new Date(), pingback, _blog));
                    }
                } else {
                    if (_logger.isInfoEnabled()) {
                        _logger.info("Pingback meta-data contained destroy key. Pingback was not saved");
                    }

                    throw new XmlRpcException(PINGBACK_ACCESS_DENIED_CODE, "Pingback meta-data contained destroy key. Pingback was not saved.");
                }
            } else {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Target URI does not support pingbacks");
                }

                throw new XmlRpcException(PINGBACK_TARGET_URI_NOT_ENABLED_CODE, "Target URI does not support pingbacks");
            }
        } catch (FetcherException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new XmlRpcException(PINGBACK_TARGET_URI_NON_EXISTENT_CODE, "Target URI does not exist");
        }

        // Update notification
        return "Registered pingback from: " + sourceURI + " to: " + targetURI;
    }

    /**
     * Add a pingback for a given blog ID
     *
     * @param title            Pingback title
     * @param excerpt          Pingback excerpt
     * @param url              Pingback URL
     * @param blogName         Pingback blog name
     * @param pingbackMetaData Pingback meta-data
     * @param pingback         {@link Pingback}
     * @return <code>0</code> if the pingback was registered, otherwise a fault code is returned
     */
    protected Integer addPingback(String title, String excerpt, String url, String blogName, Map pingbackMetaData, Pingback pingback) throws XmlRpcException {
        title = BlojsomUtils.escapeStringSimple(title);
        title = BlojsomUtils.stripLineTerminators(title, " ");
        pingback.setTitle(title);

        excerpt = BlojsomUtils.escapeStringSimple(excerpt);
        excerpt = BlojsomUtils.stripLineTerminators(excerpt, " ");
        pingback.setExcerpt(excerpt);

        url = BlojsomUtils.escapeStringSimple(url);
        url = BlojsomUtils.stripLineTerminators(url, " ");
        pingback.setUrl(url);

        blogName = BlojsomUtils.escapeStringSimple(blogName);
        blogName = BlojsomUtils.stripLineTerminators(blogName, " ");
        pingback.setBlogName(blogName);

        pingback.setTrackbackDate(new Date());
        pingback.setMetaData(pingbackMetaData);

        try {
            _fetcher.savePingback(_blog, pingback);
        } catch (FetcherException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }
            
            if (e.getCause() instanceof XmlRpcException) {
                throw (XmlRpcException) e.getCause();
            }
        }

        return new Integer(0);
    }
}