package org.ignition.blojsom.plugin.referer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ignition.blojsom.util.BlojsomUtils;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

import sun.util.calendar.Gregorian;

/**
 *
 * @author Mark Lussier
 */

public class BlogReferer {
    private String _url;
    private String _flavor;
    private Date _lastreferal;
    private int _count;
    private String _displayname;
    private boolean _istoday = false;

    private Log _logger = LogFactory.getLog(BlogReferer.class);

    public BlogReferer(String flavor, String url, Date date, int count) {
        _url = url;
        _lastreferal = date;
        _count = count;
        _flavor = flavor;
        _istoday = determineToday();
    }

    public String getFlavor() {
        return _flavor;
    }

    public void setFlavor(String flavor) {
        _flavor = flavor;
    }

    public String getUrl() {
        return _url;
    }

    public void setUrl(String url) {
        _url = url;
    }

    public Date getLastReferal() {
        return _lastreferal;
    }

    public boolean isToday() {
        return _istoday;

    }

    public void setLastReferal(Date lastreferal) {
        if (_lastreferal.compareTo(lastreferal) > 0) {
            _lastreferal =lastreferal;
            _istoday = determineToday();
        }
    }

    public int getRefererCount() {
        return _count;
    }

    public void increment() {
        _count += 1;
    }

    public void setCount(int count) {
        _count = count;
    }

    private boolean determineToday() {
        return ( RefererLogPlugin.getRefererDate( new Date()).equals(RefererLogPlugin.getRefererDate(_lastreferal)));
    }

}
