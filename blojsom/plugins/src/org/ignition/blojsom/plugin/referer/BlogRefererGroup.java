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

import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Set;

/**
 * BlogRefererGroup
 *
 * @author Mark Lussier
 * @version $Id: BlogRefererGroup.java,v 1.2 2003-03-28 21:20:20 intabulas Exp $
 */
public class BlogRefererGroup {

    private Map _groups;
    private int _grouptotal = 0;

    /**
     *
     */
    public BlogRefererGroup() {
        _groups = new HashMap(25);
    }

    /**
     *
     * @param flavor
     * @param url
     * @param date
     */
    public void addReferer(String flavor, String url, Date date) {
        if (_groups.containsKey(url)) {
            BlogReferer br = (BlogReferer) _groups.get(url);
            br.increment();
            br.setLastReferal(date);
        } else {
            _groups.put(url, new BlogReferer(flavor, url, date, 1));
        }

        _grouptotal += 1;

    }

    /**
     *
     * @param flavor
     * @param url
     * @param date
     * @param total
     */
    public void addReferer(String flavor, String url, Date date, int total) {
        if (_groups.containsKey(url)) {
            BlogReferer br = (BlogReferer) _groups.get(url);
            br.setLastReferal(date);
            br.setCount(total);
        } else {
            _groups.put(url, new BlogReferer(flavor, url, date, total));
        }

        _grouptotal += total;

    }

    /**
     *
     * @return
     */
    public int size() {
        return _groups.size();
    }

    /**
     *
     * @param key
     * @return
     */
    public Object get(Object key) {
        return _groups.get(key);
    }

    /**
     *
     * @return
     */
    public Set keySet() {
        return _groups.keySet();
    }

    /**
     *
     * @return
     */
    public int getRefererCount() {
        return _grouptotal;
    }


}
