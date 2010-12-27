/**
 * Copyright (c) 2003-2007, David A. Czarnecki
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
package org.blojsom.plugin.pingback.event;

import org.blojsom.blog.Blog;
import org.blojsom.blog.Pingback;
import org.blojsom.plugin.admin.event.EntryEvent;

import java.util.Date;

/**
 * Pingback event
 *
 * @author David Czarnecki
 * @version $Id: PingbackEvent.java,v 1.2 2007-01-17 02:35:13 czarneckid Exp $
 * @since blojsom 3.0
 */
public class PingbackEvent extends EntryEvent {

    protected Pingback _pingback;

    /**
     * Create a new event indicating something happened with a {@link Pingback} in the system.
     *
     * @param source    Source of the event
     * @param timestamp Event timestamp
     * @param trackback {@link Pingback}
     * @param blog      {@link Blog}
     */
    public PingbackEvent(Object source, Date timestamp, Pingback pingback, Blog blog) {
        super(source, timestamp, pingback.getEntry(), blog);

        _pingback = pingback;
    }

    /**
     * Retrieve the {@link Pingback} associated with the event
     *
     * @return {@link Pingback}
     */
    public Pingback getPingback() {
        return _pingback;
    }
}
