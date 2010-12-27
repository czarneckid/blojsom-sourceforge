/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.blojsom.event.pojo;

import java.util.Date;
import org.blojsom.event.Event;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author owen
 */
@Test
public class BasicEventTest {

    public void testConstructor() {
        Event e = new BasicEvent(this);
        Assert.assertEquals(e.getSource(), this);
        Assert.assertTrue(e.getTimestamp().before(new Date()));
        Assert.assertFalse(e.isEventHandled());
    }

    public void testOverloadedConstructor() {
        Date d = new Date();
        Event e = new BasicEvent(this, d);
        Assert.assertEquals(e.getSource(), this);
        Assert.assertTrue(e.getTimestamp().equals(d));
        Assert.assertFalse(e.isEventHandled());
    }
}
