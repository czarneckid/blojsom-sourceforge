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
import org.ignition.blojsom.util.BlojsomUtils;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

import sun.util.calendar.Gregorian;

/**
 * BlogReferer
 *
 * @author Mark Lussier
 * @version $Id: BlogReferer.java,v 1.2 2003-03-28 21:20:23 intabulas Exp $
 */
public class BlogReferer {
    private String _url;
    private String _flavor;
    private Date _lastreferal;
    private int _count;

    /**
     *
     */
    private boolean _istoday = false;

    /**
     *
     */
    private Log _logger = LogFactory.getLog(BlogReferer.class);

    /**
     *
     * @param flavor
     * @param url
     * @param date
     * @param count
     */
    public BlogReferer(String flavor, String url, Date date, int count) {
        _url = url;
        _lastreferal = date;
        _count = count;
        _flavor = flavor;
        _istoday = determineToday();
    }

    /**
     *
     * @return
     */
    public String getFlavor() {
        return _flavor;
    }

    /**
     *
     * @param flavor
     */
    public void setFlavor(String flavor) {
        _flavor = flavor;
    }

    /**
     *
     * @return
     */
    public String getUrl() {
        return _url;
    }

    /**
     *
     * @param url
     */
    public void setUrl(String url) {
        _url = url;
    }

    /**
     *
     * @return
     */
    public Date getLastReferal() {
        return _lastreferal;
    }

    /**
     *
     * @return
     */
    public boolean isToday() {
        return _istoday;

    }

    /**
     *
     * @param lastreferal
     */
    public void setLastReferal(Date lastreferal) {
        if (_lastreferal.compareTo(lastreferal) > 0) {
            _lastreferal = lastreferal;
            _istoday = determineToday();
        }
    }

    /**
     *
     * @return
     */
    public int getRefererCount() {
        return _count;
    }

    /**
     *
     */
    public void increment() {
        _count += 1;
    }

    /**
     *
     * @param count
     */
    public void setCount(int count) {
        _count = count;
    }

    /**
     * Determines if this referer has been seen TODAY
     * @return a boolean indicating if it was seend today
     */
    private boolean determineToday() {
        return (RefererLogPlugin.getRefererDate(new Date()).equals(RefererLogPlugin.getRefererDate(_lastreferal)));
    }

}
