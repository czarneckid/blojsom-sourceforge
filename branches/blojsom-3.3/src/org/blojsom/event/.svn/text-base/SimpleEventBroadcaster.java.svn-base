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
package org.blojsom.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * SimpleEventBroadcaster.
 * <p></p>
 * Events are broadcast to each event in a separate thread so that the broadcaster is not a bottleneck.
 * No defined order is set for how each event will receive an event, so you should not assume any order
 * in listeners being called. No steps are taken to ensure a event does not receive an event if it is
 * removed at the same time an event is being broadcast.
 * <p></p>
 * The addition of the {@link #processEvent(Event)} method adds the capability for components to have an
 * event processed after the call instead of asynchronously as with the {@link #broadcastEvent(Event)} method.
 *
 * @author David Czarnecki
 * @version $Id: SimpleEventBroadcaster.java,v 1.3 2008-07-07 19:55:06 czarneckid Exp $
 * @since blojsom 3.0
 */
public class SimpleEventBroadcaster implements EventBroadcaster {

    private Log _logger = LogFactory.getLog(SimpleEventBroadcaster.class);

    private static Set _listeners;
    private static Map _listenerToHandler;

    /**
     * Default constructor.
     */
    public SimpleEventBroadcaster() {
        if (_listeners == null) {
            _listeners = new HashSet();
        } else {
            // @todo Can Spring support a singleton bean across servlets/application contexts?
            if (_logger.isDebugEnabled()) {
                _logger.debug("Using shared listeners map");
            }
        }

        if (_listenerToHandler == null) {
            _listenerToHandler = new HashMap();
        } else {
            // @todo Can Spring support a singleton bean across servlets/application contexts?
            if (_logger.isDebugEnabled()) {
                _logger.debug("Using shared listener to handler map");
            }
        }

        if (_logger.isDebugEnabled()) {
            _logger.debug("Initialized simple event broadcaster");
        }
    }

    /**
     * Add a event to this event broadcaster
     *
     * @param listener {@link Listener}
     */
    public void addListener(Listener listener) {
        EventHandler handler = new EventHandler(listener, new Filter() {
            /**
             * Determines whether or not a particular event should be processed
             *
             * @param event {@link Event} to be processed
             * @return <code>true</code> if the event should be processed, <code>false</code> otherwise
             */
            public boolean processEvent(Event event) {
                return true;
            }
        });

        if (!_listenerToHandler.containsKey(listener.getClass().getName())) {
            _listeners.add(handler);
            _listenerToHandler.put(listener.getClass().getName(), handler);
            if (_logger.isDebugEnabled()) {
                _logger.debug("Added event listener: " + listener.getClass().getName() + " with process all events filter");
            }
        }
    }

    /**
     * Add a event to this event broadcaster. Events are filtered using the {@link org.blojsom.event.Filter} instance
     * passed to this method.
     *
     * @param listener {@link Listener}
     * @param filter   {@link Filter} used to filter events
     */
    public void addListener(Listener listener, Filter filter) {
        EventHandler handler = new EventHandler(listener, filter);

        if (!_listenerToHandler.containsKey(listener.getClass().getName())) {
            _listeners.add(handler);
            _listenerToHandler.put(listener.getClass().getName(), handler);
            if (_logger.isDebugEnabled()) {
                _logger.debug("Added event listener: " + listener.getClass().getName() + " with filter: " + filter.getClass().getName());
            }
        }
    }

    /**
     * Remove a event from this event broadcaster
     *
     * @param listener {@link Listener}
     */
    public void removeListener(Listener listener) {
        if (_listenerToHandler.containsKey(listener.getClass().getName())) {
            EventHandler handler = (EventHandler) _listenerToHandler.get(listener.getClass().getName());
            _listeners.remove(handler);
            _listenerToHandler.remove(listener.getClass().getName());
        }

        if (_logger.isDebugEnabled()) {
            _logger.debug("Removed event listener: " + listener.getClass().getName());
        }
    }

    /**
     * Broadcast an event to all listeners
     *
     * @param event {@link Event} to be broadcast to all listeners
     */
    public void broadcastEvent(Event event) {
        Thread eventBroadcaster = new Thread(new AsynchronousEventBroadcaster(event));
        eventBroadcaster.setDaemon(true);
        eventBroadcaster.start();
    }

    /**
     * Process an event with all listeners
     *
     * @param event {@link Event} to be processed by all listeners
     */
    public void processEvent(Event event) {
        Iterator handlerIterator = _listeners.iterator();
        while (handlerIterator.hasNext()) {
            EventHandler eventHandler = (EventHandler) handlerIterator.next();
            if (eventHandler._filter.processEvent(event)) {
                eventHandler._listener.processEvent(event);
            }
        }
    }

    /**
     * Event handler helper class.
     */
    protected class EventHandler {

        protected Listener _listener;
        protected Filter _filter;

        /**
         * Create a new event handler with event and filter instances.
         *
         * @param listener {@link Listener}
         * @param filter   {@link Filter}
         */
        protected EventHandler(Listener listener, Filter filter) {
            _listener = listener;
            _filter = filter;
        }
    }

    /**
     * Thread to handle broadcasting an event to registered listeners.
     */
    private class AsynchronousEventBroadcaster implements Runnable {

        private Event _event;

        public AsynchronousEventBroadcaster(Event event) {
            _event = event;
        }

        /**
         * Iterates over the set of {@link EventHandler} registered with this broadcaster and calls
         * the {@link Listener#handleEvent(Event)} method with the
         * {@link Event}.
         */
        public void run() {
            Iterator handlerIterator = _listeners.iterator();
            while (handlerIterator.hasNext()) {
                EventHandler eventHandler = (EventHandler) handlerIterator.next();
                if (eventHandler._filter.processEvent(_event)) {
                    eventHandler._listener.handleEvent(_event);
                }
            }
        }
    }
}