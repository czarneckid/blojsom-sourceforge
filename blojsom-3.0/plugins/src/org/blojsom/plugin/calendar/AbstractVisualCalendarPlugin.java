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
package org.blojsom.plugin.calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.PluginException;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * AbstractVisualCalendarPlugin
 *
 * @author David Czarnecki
 * @author Mark Lussier
 * @version $Id: AbstractVisualCalendarPlugin.java,v 1.4 2006-07-07 01:37:01 czarneckid Exp $
 * @since blojsom 3.0
 */
public abstract class AbstractVisualCalendarPlugin extends AbstractCalendarPlugin {

    private Log _logger = LogFactory.getLog(AbstractVisualCalendarPlugin.class);

    protected BlogCalendar _blogCalendar;

    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param user                {@link BlogUser} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws PluginException If there is an error processing the blog entries
     */
    public Entry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, Map context, Entry[] entries) throws PluginException {
        entries = super.process(httpServletRequest, httpServletResponse, blog, context, entries);

        Locale locale = (Locale) context.get(BLOJSOM_CALENDAR_LOCALE);
        BlogCalendar blogCalendar = (BlogCalendar) context.get(BLOJSOM_CALENDAR);
        Calendar entrycalendar = Calendar.getInstance(locale);

        Date startDate = BlojsomUtils.getFirstDateOfYearMonth(locale, blogCalendar.getCurrentYear(), blogCalendar.getCurrentMonth());
        Date endDate = BlojsomUtils.getLastDateOfYearMonth(locale, blogCalendar.getCurrentYear(), blogCalendar.getCurrentMonth());
        Date now = new Date();

        if (startDate.before(now)) {
            if (endDate.after(now)) {
                endDate = now;
            }
            
            try {
                Entry[] entriesForMonth = _fetcher.findEntriesBetweenDates(blog, startDate, endDate);
                for (int i = 0; i < entriesForMonth.length; i++) {
                    Entry entry = entriesForMonth[i];
                    entrycalendar.setTime(entry.getDate());
                    int entrymonth = entrycalendar.get(Calendar.MONTH);
                    int entryyear = entrycalendar.get(Calendar.YEAR);

                    // If the entry is is the same month and the same year, then flag that date as having a entry
                    if ((entrymonth == blogCalendar.getCurrentMonth()) && (entryyear == blogCalendar.getCurrentYear())) {
                        blogCalendar.setEntryForDOM(entrycalendar.get(Calendar.DAY_OF_MONTH));
                    }
                }
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            }
        }

        context.put(BLOJSOM_CALENDAR, blogCalendar);

        return entries;
    }
}
