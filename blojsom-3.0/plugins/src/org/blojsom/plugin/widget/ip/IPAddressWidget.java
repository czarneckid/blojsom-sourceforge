/**
 * Copyright (c) 2003-2008, David A. Czarnecki
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
package org.blojsom.plugin.widget.ip;

import org.blojsom.event.Event;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.event.Filter;
import org.blojsom.event.Listener;
import org.blojsom.plugin.widget.event.ProcessWidgetRequest;
import org.blojsom.plugin.widget.event.RegisterWidgetEvent;

import java.util.Date;

/**
 * IP Address widget
 *
 * @author David Czarnecki
 * @version $Id: IPAddressWidget.java,v 1.2 2008-07-07 19:54:16 czarneckid Exp $
 * @since blojsom 3.2
 */
public class IPAddressWidget implements Listener {

    private static final String IP_WIDGET = "ip-widget";

    private EventBroadcaster _eventBroadcaster;

    /**
     * Create a new instance of the IP address widget
     */
    public IPAddressWidget() {
    }

    /**
     * Set the {@link EventBroadcaster}
     *
     * @param eventBroadcaster {@link EventBroadcaster}
     */
    public void setEventBroadcaster(EventBroadcaster eventBroadcaster) {
        _eventBroadcaster = eventBroadcaster;
    }

    /**
     * Initialize the widget
     */
    public void init() {
        _eventBroadcaster.addListener(this, new Filter() {
            /**
             * Determines whether or not a particular event should be processed
             *
             * @param event {@link Event} to be processed
             * @return <code>true</code> if the event should be processed, <code>false</code> otherwise
             */
            public boolean processEvent(Event event) {
                if (event instanceof ProcessWidgetRequest) {
                    ProcessWidgetRequest processWidgetRequest = (ProcessWidgetRequest) event;

                    if (IP_WIDGET.equals(processWidgetRequest.getWidget())) {
                        return true;
                    }
                }

                return false;
            }
        });

        _eventBroadcaster.broadcastEvent(new RegisterWidgetEvent(this, new Date(), IP_WIDGET, "Display Request IP Address"));
    }

    /**
     * Handle an event broadcast from another component
     *
     * @param event {@link Event} to be handled
     */
    public void handleEvent(Event event) {
    }

    /**
     * Process an event from another component
     *
     * @param event {@link Event} to be handled
     */
    public void processEvent(Event event) {
        if (event instanceof ProcessWidgetRequest) {
            ProcessWidgetRequest processWidgetRequest = (ProcessWidgetRequest) event;

            processWidgetRequest.setWidgetTemplate("IP address: " + processWidgetRequest.getRequest().getRemoteAddr());
        }
    }
}
