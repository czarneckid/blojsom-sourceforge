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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Arrays;

/**
 * BlogCalendar
 *
 * @author Mark Lussier
 * @version $Id: BlogCalendar.java,v 1.1 2003-03-27 02:07:42 intabulas Exp $
 */
public class BlogCalendar {

    private Log _logger = LogFactory.getLog(BlogCalendar.class);

    private Calendar _calendar;
    private Date _today;
    private DateFormatSymbols _symbols;
    private Locale _local;
    private Boolean[] _dayswithentry;


    /**
     *
     * @param calendar
     */
    public BlogCalendar(Calendar calendar) {
        this(calendar, Locale.getDefault());
    }

    public BlogCalendar(Calendar calendar, Locale locale) {
        _local = locale;
        _calendar = calendar;
        _today = new Date();
        _symbols = new DateFormatSymbols(_local);


        _dayswithentry = new Boolean[_calendar.getMaximum(Calendar.DAY_OF_MONTH)];
        Arrays.fill(_dayswithentry, Boolean.FALSE);
    }

    public String getCaption() {
        return BlojsomUtils.getFormattedDate(_calendar.getTime(), BlojsomConstants.BLOJSOM_CALENDAR_FORMAT);
    }

    public int getFirstDayOfMonth() {
       _calendar.set( Calendar.DAY_OF_MONTH, 1 );
        return _calendar.get( Calendar.DAY_OF_WEEK);
    }

    public int getDaysInMonth() {
        return _calendar.getMaximum(Calendar.DAY_OF_MONTH);
    }

    public void setEntryForDOM( int dom ) {
        _dayswithentry[dom - 1] = new Boolean(true);
    }

    public void removetEntryForDOM( int dom ) {
        _dayswithentry[dom - 1] = new Boolean(false);
    }

    public boolean dayHasEntry( int dom ) {
        return ((Boolean)_dayswithentry[dom-1]).booleanValue();
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
        return getShortDayOfWeekNames()[dow];
    }

    public String[] getShortDayOfWeekNames() {
        return _symbols.getShortWeekdays();
    }


}

