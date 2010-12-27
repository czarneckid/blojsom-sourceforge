/**
 * Copyright (c) 2003-2009, David A. Czarnecki
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
package org.blojsom.plugin.calendar;

import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.fetcher.Fetcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Date;

/**
 * AbstractCalendarPlugin is a base plugin that is used by the various calendar plugins
 * to filter content.
 *
 * @author David Czarnecki
 * @author Mark Lussier
 * @version $Id: AbstractCalendarPlugin.java,v 1.6 2008-07-07 19:54:09 czarneckid Exp $
 * @since blojsom 3.0
 */
public abstract class AbstractCalendarPlugin implements Plugin {

    protected static final String BLOJSOM_CALENDAR_LOCALE = "BLOJSOM_CALENDAR_LOCALE";

    /**
     * Request parameter for the "year"
     */
    protected static final String YEAR_PARAM = "year";

    /**
     * Request parameter for the "month"
     */
    protected static final String MONTH_PARAM = "month";

    /**
     * Request parameter for the "day"
     */
    protected static final String DAY_PARAM = "day";

    /**
     * Format String for Calendar Month
     * (Example: March 2003)
     */
    protected static final String BLOJSOM_CALENDAR_FORMAT = "MMMMM yyyy";

    /**
     * Short Format String for Previous/Next Calendar Month(s)
     * (Example: Mar)
     */
    protected static final String BLOJSOM_CALENDAR_SHORTFORMAT = "MMM";

    protected static final String BLOJSOM_FILTER_START_DATE = "BLOJSOM_FILTER_START_DATE";
    protected static final String BLOJSOM_FILTER_END_DATE = "BLOJSOM_FILTER_END_DATE";

    /**
     * Key under which the blog calendar will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_CALENDAR = "BLOJSOM_CALENDAR";

    /**
     * Key under which the blog calendar vtl helper will be placed
     * (example: on the request for the JSPDispatcher)
     */
    public static final String BLOJSOM_CALENDAR_VTLHELPER = "BLOJSOM_CALENDAR_VTLHELPER";

    protected Fetcher _fetcher;

    /**
     * Set the {@link Fetcher}
     *
     * @param fetcher {@link Fetcher}
     */
    public void setFetcher(Fetcher fetcher) {
        _fetcher = fetcher;
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws PluginException If there is an error initializing the plugin
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
        Locale locale = Locale.getDefault();

        // If blog-language is set in blojsom.properties, use it instead
        String localeLanguage = blog.getBlogLanguage();

        // If no locale is configured, use the system default
        if (localeLanguage != null) {
            locale = new Locale(localeLanguage);
        }
        context.put(BLOJSOM_CALENDAR_LOCALE, locale);

        String requestedDateKey;
        String calendarUrl = blog.getBlogURL();
        calendarUrl = BlojsomUtils.addTrailingSlash(calendarUrl);

        // Default to the Current Month and Year
        Calendar calendar = Calendar.getInstance(locale);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Determine a calendar-based request
        String year = null;
        String month = null;
        String day = null;

        year = httpServletRequest.getParameter(YEAR_PARAM);
        if (year != null) {

            // Must be a 4 digit year
            if (year.length() != 4) {
                year = null;
            } else {
                try {
                    currentYear = Integer.parseInt(year);
                    calendar.set(Calendar.YEAR, currentYear);

                    Date startDate = BlojsomUtils.getFirstDateOfYear(locale, currentYear);
                    Date endDate = BlojsomUtils.getLastDateOfYear(locale, currentYear);
                    context.put(BLOJSOM_FILTER_START_DATE, startDate);
                    context.put(BLOJSOM_FILTER_END_DATE, endDate);
                } catch (NumberFormatException e) {
                    year = "";
                }

                month = httpServletRequest.getParameter(MONTH_PARAM);

                if (month == null) {
                    month = "";
                } else if (month.length() < 2) {
                    month = "0" + month;
                }

                if (!month.equals("")) {
                    try {
                        currentMonth = Integer.parseInt(month) - 1; // Damm Sun!
                        calendar.set(Calendar.MONTH, currentMonth);

                        Date startDate = BlojsomUtils.getFirstDateOfYearMonth(locale, currentYear, currentMonth);
                        Date endDate = BlojsomUtils.getLastDateOfYearMonth(locale, currentYear, currentMonth);
                        context.put(BLOJSOM_FILTER_START_DATE, startDate);
                        context.put(BLOJSOM_FILTER_END_DATE, endDate);
                    } catch (NumberFormatException e) {
                        month = "";
                    }
                }

                day = httpServletRequest.getParameter(DAY_PARAM);
                if (day == null) {
                    day = "";
                } else if (day.length() < 2) {
                    day = "0" + day;
                }

                if (!day.equals("")) {
                    try {
                        currentDay = Integer.parseInt(day);
                        if (currentDay > calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                            currentDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                        }

                        calendar.set(Calendar.DAY_OF_MONTH, currentDay);

                        Date startDate = BlojsomUtils.getFirstDateOfYearMonthDay(locale, currentYear, currentMonth, currentDay);
                        Date endDate = BlojsomUtils.getLastDateOfYearMonthDay(locale, currentYear, currentMonth, currentDay);
                        context.put(BLOJSOM_FILTER_START_DATE, startDate);
                        context.put(BLOJSOM_FILTER_END_DATE, endDate);
                    } catch (NumberFormatException e) {
                    }
                }
            }

            requestedDateKey = year + month + day;

        } else {
            requestedDateKey = null;
        }

        BlogCalendar blogCalendar = new BlogCalendar(calendar, calendarUrl, locale);
        blogCalendar.setRequestedDateKey(requestedDateKey);

        context.put(BLOJSOM_CALENDAR, blogCalendar);

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
