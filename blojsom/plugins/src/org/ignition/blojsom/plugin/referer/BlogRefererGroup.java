package org.ignition.blojsom.plugin.referer;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Set;

/**
 *
 * @author Mark Lussier
 */ 

public class BlogRefererGroup {

    private Map _groups;
    private int _grouptotal = 0;

    public BlogRefererGroup() {
        _groups = new HashMap(25);
    }

    public void addReferer( String flavor, String url, Date date ) {
        if ( _groups.containsKey(url)) {
            BlogReferer _br = (BlogReferer)_groups.get(url);
            _br.increment();
            _br.setLastReferal(date);
        } else {
            _groups.put( url, new BlogReferer(flavor, url, date, 1));
        }

        _grouptotal  += 1;

    }

    public void addReferer( String flavor, String url, Date date, int total ) {
        if ( _groups.containsKey(url)) {
            BlogReferer _br = (BlogReferer)_groups.get(url);
            _br.setLastReferal(date);
            _br.setCount(total);
        } else {
            _groups.put( url, new BlogReferer(flavor, url, date, total));
        }

        _grouptotal  += total;

    }


    public int size() {
        return _groups.size();
    }

    public Object get(Object key) {
        return _groups.get(key);
    }

    public Set keySet() {
        return _groups.keySet();
    }

    public int getRefererCount() {
        return _grouptotal;
    }


}
