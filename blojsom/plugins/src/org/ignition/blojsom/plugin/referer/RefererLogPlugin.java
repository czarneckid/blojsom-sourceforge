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
package org.ignition.blojsom.plugin.referer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.plugin.referer.BlogReferer;
import org.ignition.blojsom.plugin.referer.BlogRefererGroup;
import org.ignition.blojsom.plugin.BlojsomPlugin;
import org.ignition.blojsom.plugin.BlojsomPluginException;
import org.ignition.blojsom.util.BlojsomConstants;
import org.ignition.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Generic Referer Plugin
 *
 * @author Mark Lussier
 * @version $Id: RefererLogPlugin.java,v 1.13 2003-04-19 02:44:57 czarneckid Exp $
 */
public class RefererLogPlugin implements BlojsomPlugin {

    /**
     * HTTP Header for Referer Information
     */
    private static final String HEADER_REFERER = "referer";

    /**
     * web.xml init-param name
     */
    private static final String REFERER_CONFIG_IP = "plugin-referer";

    /**
     * Key under which the "REFERER_HISTORY" groups will be place into the context
     * (example: on the request for the JSPDispatcher)
     */
    public static final String REFERER_CONTEXT_NAME = "REFERER_HISTORY";

    /**
     * Key under which the "REFERER_MAX_LENGTH" will be placed into the context
     * (example: on the request for the JSPDispatcher)
     */
    public static final String REFERER_CONTEXT_MAX_LENGTH = "REFERER_MAX_LENGTH";

    /**
     * Header written to the refer log file
     */
    private static final String REFERER_LOG_HEADER = "blojsom referer log";

    /**
     * Hit counter key
     */
    public static final String HITCOUNTER_KEY  = ".hitcounter";

    /**
     * Format used to store last refer date for a given url
     */
    public static final String REFERER_DATE_FORMAT = "yyyy-MM-dd";

    private static final int FIELD_FLAVOR = 0;
    private static final int FIELD_DATE = 1;
    private static final int FIELD_COUNT = 2;

    /**
     * Fully qualified filename to write referers to
     */
    private String _refererlog = null;

    /**
     * Contains the blog url to filter referes from sub-category entries
     */
    private String _blogurlfilter = null;


    /**
     * Logger instance
     */
    private Log _logger = LogFactory.getLog(RefererLogPlugin.class);

    private Map _referergroups;

    private static final int REFERER_MAX_LENGTH_DEFAULT = 40;

    private int _referermaxlength = REFERER_MAX_LENGTH_DEFAULT;

    private static final String REFERER_LOG_FILE = "referer-filename";

    private static final String REFERER_MAX_LENGTH = "referer-display-size";

    private static final String REFERER_HIT_COUNTS = "hit-count-flavors";

    private List _hitcountflavors;


    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig Servlet config object for the plugin to retrieve any initialization parameters
     * @param blog {@link Blog} instance
     * @throws org.ignition.blojsom.plugin.BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, Blog blog) throws BlojsomPluginException {

        _referergroups = new HashMap(5);

        _blogurlfilter = blog.getBlogURL();

        _hitcountflavors = new ArrayList(5);

        String refererConfiguration = servletConfig.getInitParameter(REFERER_CONFIG_IP);
        if (refererConfiguration == null || "".equals(refererConfiguration)) {
            throw new BlojsomPluginException("No value given for: " + REFERER_CONFIG_IP + " configuration parameter");
        }

        Properties refererProperties = new Properties();
        InputStream is = servletConfig.getServletContext().getResourceAsStream(refererConfiguration);
        try {
            refererProperties.load(is);
            is.close();
            String maxlength = refererProperties.getProperty(REFERER_MAX_LENGTH);
            if (maxlength != null) {
                try {
                    _referermaxlength = Integer.parseInt(maxlength);
                } catch (NumberFormatException e) {
                    _referermaxlength = REFERER_MAX_LENGTH_DEFAULT;
                }
            }

            String hitcounters = refererProperties.getProperty(REFERER_HIT_COUNTS);
            if (hitcounters != null) {
                String[] _hitflavors = BlojsomUtils.parseCommaList(hitcounters);
                for (int x = 0; x < _hitflavors.length; x++) {
                    _hitcountflavors.add(_hitflavors[x]);
                }
                _logger.info("Hit count flavors = " + _hitcountflavors.size());
            }

            _refererlog = refererProperties.getProperty(REFERER_LOG_FILE);
        } catch (IOException e) {
            throw new BlojsomPluginException(e);
        }

        loadRefererLog(_refererlog);
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
        String referer = httpServletRequest.getHeader(HEADER_REFERER);
        String flavor = httpServletRequest.getParameter(BlojsomConstants.FLAVOR_PARAM);

        if (flavor == null) {
            flavor = BlojsomConstants.DEFAULT_FLAVOR_HTML;
        }

        if (_hitcountflavors.contains(flavor)) {
            _logger.debug("[HitCounter] flavor=" + flavor + " - referer=" + referer);

            BlogRefererGroup group;
            if (_referergroups.containsKey(flavor)) {
                group = (BlogRefererGroup) _referergroups.get(flavor);
            } else {
                group = new BlogRefererGroup(true);
            }
            group.addHitCount(new Date(), 1);
            _referergroups.put(flavor, group);

        } else if ((referer != null) && (!referer.startsWith(_blogurlfilter))) {
            _logger.debug("[Referer] flavor=" + flavor + " - referer=" + referer);

            BlogRefererGroup group;
            if (_referergroups.containsKey(flavor)) {
                group = (BlogRefererGroup) _referergroups.get(flavor);
            } else {
                group = new BlogRefererGroup(_hitcountflavors.contains(flavor));
            }
            group.addReferer(flavor, referer, new Date());
            _referergroups.put(flavor, group);

        }

        context.put(REFERER_CONTEXT_NAME, _referergroups);
        context.put(REFERER_CONTEXT_MAX_LENGTH, new Integer(_referermaxlength));

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
    private void loadRefererLog(String refererlog) {

        File _refererfile = new File(refererlog);

        if (_refererfile.exists()) {

            Properties _refererproperties = new Properties();

            try {
                InputStream is = new FileInputStream(_refererfile);
                _refererproperties.load(is);
                is.close();

                Enumeration _refererenum = _refererproperties.keys();
                while (_refererenum.hasMoreElements()) {
                    String _key = (String) _refererenum.nextElement();
                    String[] _details = BlojsomUtils.parseDelimitedList(_key, ".");

                    String _flavor = _details[FIELD_FLAVOR];
                    String _url = (String)_refererproperties.get( _key);

                    _logger.info("Loading [" + _url + "] " + "Flavor [" + _flavor + "] " + "Date ["
                                  + _details[FIELD_DATE] + "] " + "Count [" + _details[FIELD_COUNT] + "]");

                    BlogRefererGroup _group;
                    if (_referergroups.containsKey(_flavor)) {
                        _group = (BlogRefererGroup) _referergroups.get(_flavor);
                    } else {
                        _group = new BlogRefererGroup(_hitcountflavors.contains(_flavor));
                    }

                    if (_hitcountflavors.contains(_flavor)) {
                        _group.addHitCount(getDateFromReferer(_details[FIELD_DATE]), Integer.parseInt(_details[FIELD_COUNT]));
                    } else {
                        _group.addReferer(_flavor, _url, getDateFromReferer(_details[FIELD_DATE]), Integer.parseInt(_details[FIELD_COUNT]));
                    }
                    _referergroups.put(_flavor, _group);
                }

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

        Properties _refererproperties = new Properties();

        Iterator _groupiterator = _referergroups.keySet().iterator();
        while (_groupiterator.hasNext()) {
            String groupflavor = (String) _groupiterator.next();
            BlogRefererGroup group = (BlogRefererGroup) _referergroups.get(groupflavor);
            if (group.isHitCounter()) {

                _refererproperties.put(groupflavor + "." + getRefererDate(group.getLastReferralDate()) + "." + group.getReferralCount(), HITCOUNTER_KEY);
                //_refererproperties.put(groupflavor + ".hitcounter", getRefererDate(group.getLastReferralDate()) + "," + group.getReferralCount());
            } else {
                Iterator _flavoriterator = group.keySet().iterator();
                while (_flavoriterator.hasNext()) {
                    String flavorkey = (String) _flavoriterator.next();
                    BlogReferer referer = (BlogReferer) group.get(flavorkey);

                    _refererproperties.put(groupflavor +"." + getRefererDate(referer.getLastReferral()) + "." + referer.getCount(),
                                           referer.getUrl());
                }
            }
        }

        try {
            FileOutputStream _fos = new FileOutputStream(_refererlog, false);
            _refererproperties.store(_fos, REFERER_LOG_HEADER);
            _fos.close();
        } catch (IOException e) {
            _logger.error(e);
        }
    }

    /**
     * Converts a string date in the form of yyyy-MM-dd to a Date
     *
     * @param rfcdate String in yyyy-MM-dd format
     * @return the Date
     */
    private static Date getDateFromReferer(String rfcdate) {
        Date result = null;
        SimpleDateFormat sdf = new SimpleDateFormat(REFERER_DATE_FORMAT);
        try {
            result = sdf.parse(rfcdate);
        } catch (ParseException e) {
            result = new Date();
        }
        return result;
    }

    /**
     * Converts a Date into a String for writing to the referer log
     * 
     * @param date Date to write
     * @return String verion of date in the format of yyyy-MM-dd
     */
    public static String getRefererDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(REFERER_DATE_FORMAT);
        return sdf.format(date);
    }
}
