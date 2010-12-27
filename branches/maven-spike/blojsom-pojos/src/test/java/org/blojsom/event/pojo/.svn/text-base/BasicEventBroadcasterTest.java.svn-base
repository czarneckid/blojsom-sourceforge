/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.blojsom.event.pojo;

import org.blojsom.event.Event;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.event.Filter;
import org.blojsom.event.Listener;
import org.mockito.Mockito;
import org.testng.annotations.Test;

/**
 *
 * @author owen
 */
@Test
public class BasicEventBroadcasterTest {

    public void testAddListener() {
        Listener l = new BasicListener();
        EventBroadcaster eb = new BasicEventBroadcaster();
        eb.addListener(l);
    }

    public void testAddListenerNegative() {
        EventBroadcaster eb = new BasicEventBroadcaster();
        eb.addListener(null);
    }

    public void testOverloadAddListener() {
        Listener l = new BasicListener();
        Filter f = new BasicFilter();
        EventBroadcaster eb = new BasicEventBroadcaster();
        eb.addListener(l, f);
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testProcessMessage() {
        EventBroadcaster eb = new BasicEventBroadcaster();
        eb.processEvent(null);
    }

    public void testBroadcastMessage() {
        Event e = new BasicEvent(this);
        Listener l = Mockito.mock(Listener.class);
        EventBroadcaster eb = new BasicEventBroadcaster();
        eb.addListener(l);
        eb.broadcastEvent(e);
        Mockito.verify(l).handleEvent(e);
    }

    public void testRemoveListener() {
        Event e = new BasicEvent(this);
        Listener l = Mockito.mock(Listener.class);
        EventBroadcaster eb = new BasicEventBroadcaster();
        eb.addListener(l);
        eb.removeListener(l);
        Mockito.verifyZeroInteractions(l);
    }

    public void testRemoveListenerNegative() {
        Listener l = new BasicListener();
        EventBroadcaster eb = new BasicEventBroadcaster();
        eb.removeListener(l);
        eb.removeListener(null);
    }
}
