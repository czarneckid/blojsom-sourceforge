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
package org.ignition.blojsom.plugin.calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ignition.blojsom.util.BlojsomUtils;
import org.ignition.blojsom.util.BlojsomConstants;

import java.text.DateFormatSymbols;
import java.util.*;

/**
 * BlogCalendar
 *
 * @author Mark Lussier
 * @version $Id: BlogCalendar.java,v 1.4 2003-03-27 05:47:16 intabulas Exp $
 */
public class BlogCalendar {

    private Log _logger = LogFactory.getLog(BlogCalendar.class);


    private Calendar _calendar;
    private Calendar _today;
    private DateFormatSymbols _symbols;
    private Locale _locale;
    private Boolean[] _dayswithentry;
    private String[] _shortdownames;
    private String _blogURL;
    private int currentmonth;
    private int currentyear;

    // [Row][Col]
    private String[][] visualcalendar = new String[6][7];

    /**
     *
     * @param calendar
     */
    public BlogCalendar(Calendar calendar, String blogurl) {
        this(calendar, blogurl, Locale.getDefault());
    }

    public BlogCalendar(Calendar calendar, String blogurl, Locale locale) {
        _locale = locale;
        _calendar = calendar;
        _today = new GregorianCalendar(_locale);
        _today.setTime(new Date());
        _symbols = new DateFormatSymbols(_locale);
        _blogURL = blogurl;

        currentmonth = calendar.get(Calendar.MONTH) + 1;// Damm Java!
        currentyear = calendar.get(Calendar.YEAR);


        _dayswithentry = new Boolean[_calendar.getMaximum(Calendar.DAY_OF_MONTH)];
        Arrays.fill(_dayswithentry, Boolean.FALSE);

        _shortdownames = new String[7];
        String[] downames = _symbols.getShortWeekdays();
        for (int x = 0; x < _shortdownames.length; x++) {
            _shortdownames[x] = downames[x + 1];
        }

    }

    public String getCaption() {
        return BlojsomUtils.getFormattedDate(_calendar.getTime(), BlojsomConstants.BLOJSOM_CALENDAR_FORMAT);
    }

    public int getFirstDayOfMonth() {
        _calendar.set(Calendar.DAY_OF_MONTH, 1);
        return _calendar.get(Calendar.DAY_OF_WEEK);
    }

    public int getDaysInMonth() {
        return _calendar.getMaximum(Calendar.DAY_OF_MONTH);
    }

    public void setEntryForDOM(int dom) {
        _dayswithentry[dom - 1] = new Boolean(true);
    }

    public void removetEntryForDOM(int dom) {
        _dayswithentry[dom - 1] = new Boolean(false);
    }

    public boolean dayHasEntry(int dom) {
        Boolean hasEntry = _dayswithentry[dom - 1];
        return hasEntry.booleanValue();
    }

    public Boolean[] getEntryDates() {
        return _dayswithentry;
    }

    public String getMonthName(int month) {
        return getMonthNames()[month];
    }

    public String[] getMonthNames() {
        return _symbols.getMonths();
    }

    public String getShortMonthName(int month) {
        return getShortMonthNames()[month];
    }

    public String[] getShortMonthNames() {
        return _symbols.getShortMonths();
    }

    public String getDayOfWeekName(int dow) {
        return getDayOfWeekNames()[dow];
    }

    public String[] getDayOfWeekNames() {
        return _symbols.getWeekdays();
    }

    public String getShortDayOfWeekName(int dow) {
        return _shortdownames[dow - 1];
    }

    public String[] getShortDayOfWeekNames() {
        return _shortdownames;
    }


    // == Render Helper

    public void buildCalendar() {
        int fdow = getFirstDayOfMonth() - 1;
        int ldom = getDaysInMonth();
        int dowoffset = 0;
        for (int x = 0; x < 6; x++) {
            for (int y = 0; y < 7; y++) {
                if ((x == 0 && y < fdow) || (dowoffset >= ldom)) {
                    visualcalendar[x][y] = "&nbsp;";
                } else {
                    dowoffset += 1;
                    if (!dayHasEntry(dowoffset)) {
                        visualcalendar[x][y] = new Integer(dowoffset).toString();
                    } else {
                        StringBuffer _url = new StringBuffer("<a href=\"");
                        _url.append(BlojsomUtils.getCalendarNavigationUrl(_blogURL, currentmonth, dowoffset, currentyear));
                        _url.append("\">").append(dowoffset).append("</a>");
                        visualcalendar[x][y] = _url.toString();
                    }
                }
            }
        }

    }


    public String getVisualCalendarRow(int row, String clazz) {
        StringBuffer result = new StringBuffer();
        for (int x = 0; x < 7; x++) {
            result.append("<td class=\"").append(clazz).append("\">").append(visualcalendar[row - 1][x]).append("</td>");
        }
        return result.toString();
    }


    public String getVisualToday() {
        StringBuffer result = new StringBuffer();
        result.append("<a href=\"").append(_blogURL).append("\">Today</a>");
        return result.toString();
    }

    public String getVisualPreviousMonth() {
        StringBuffer result = new StringBuffer();
        _calendar.add(Calendar.MONTH, -1);
        result.append("<a href=\"");
        result.append(BlojsomUtils.getCalendarNavigationUrl(_blogURL,
                (_calendar.get(Calendar.MONTH) + 1),
                -1, _calendar.get(Calendar.YEAR)));
        result.append("\"> <&nbsp;&nbsp;");
        result.append(getShortMonthName(_calendar.get(Calendar.MONTH)));
        result.append("</a>");
        _calendar.add(Calendar.MONTH, 1);
        return result.toString();
    }

    public String getVisualNextMonth() {
        StringBuffer result = new StringBuffer();
        _calendar.add(Calendar.MONTH, 1);

        if ((_calendar.get(Calendar.MONTH) < (_today.get(Calendar.MONTH)+1)) &&
                (_calendar.get(Calendar.YEAR) <= _today.get(Calendar.YEAR))) {
            result.append("<a href=\"");
            result.append(BlojsomUtils.getCalendarNavigationUrl(_blogURL,
                    (_calendar.get(Calendar.MONTH) + 1),
                    -1, _calendar.get(Calendar.YEAR)));
            result.append("\">");
            result.append(getShortMonthName(_calendar.get(Calendar.MONTH)));
            result.append("&nbsp;&nbsp;> </a>");
            _calendar.add(Calendar.MONTH, -1);
        } else {
            result.append(result.append(getShortMonthName(_calendar.get(Calendar.MONTH))));
        }
        return result.toString();
    }


}

