/**
 * Copyright (c) 2003-2004 , David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003-2004  by Mark Lussier
 * Adapted code from Chris Nokleberg (http://sixlegs.com/)
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
package org.blojsom.listener.event;

import org.blojsom.listener.BlojsomListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

/**
 * SimpleBlojsomEventBroadcaster.
 * <p></p>
 * Events are broadcast to each listener in a separate thread so that the broadcaster is not a bottleneck.
 * No defined order is set for how each listener will receive an event, so you should not assume any order
 * in listeners being called. No steps are taken to ensure a listener does not receive an event if it is
 * removed at the same time an event is being broadcast.
 *
 * @author David Czarnecki
 * @version $Id: SimpleBlojsomEventBroadcaster.java,v 1.1 2004-08-26 01:57:45 czarneckid Exp $
 * @since blojsom 2.18
 */
public class SimpleBlojsomEventBroadcaster implements BlojsomEventBroadcaster {

    private static final Log _logger = LogFactory.getLog(SimpleBlojsomEventBroadcaster.class);
    private Set _listeners;

    /**
     * Default constructor.
     */
    public SimpleBlojsomEventBroadcaster() {
        _listeners = new HashSet();
    }

    /**
     * Add a listener to this event broadcaster
     *
     * @param listener {@link BlojsomListener}
     */
    public void addListener(BlojsomListener listener) {
        _listeners.add(listener);
        _logger.debug("Added listener: " + listener.getClass().getName());
    }

    /**
     * Remove a listener from this event broadcaster
     *
     * @param listener {@link BlojsomListener}
     */
    public void removeListener(BlojsomListener listener) {
        _listeners.remove(listener);
        _logger.debug("Removed listener: " + listener.getClass().getName());        
    }

    /**
     * Broadcast an event to all listeners
     *
     * @param event {@link BlojsomEvent} to be broadcast to all listeners
     */
    public void broadcastEvent(BlojsomEvent event) {
        Thread eventBroadcaster = new Thread(new AsynchronousEventBroadcaster(event));
        eventBroadcaster.start();
    }

    /**
     * Thread to handle broadcasting an event to registered listeners.
     */
    private class AsynchronousEventBroadcaster implements Runnable {

        private BlojsomEvent _event;

        public AsynchronousEventBroadcaster(BlojsomEvent event) {
            _event = event;
        }

        /**
         * Iterates over the set of {@link BlojsomListener} registered with this broadcaster and calls
         * the {@link BlojsomListener#handleEvent(BlojsomEvent)} method with the
         * {@link BlojsomEvent}.
         */
        public void run() {
            Iterator listenerIterator = _listeners.iterator();
            while (listenerIterator.hasNext()) {
                BlojsomListener listener = (BlojsomListener) listenerIterator.next();
                listener.handleEvent(_event);
            }
        }
    }
}