/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.blojsom.event.pojo;

import org.blojsom.event.Event;
import org.blojsom.event.Filter;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author owen
 */
@Test
public class BasicFilterTest {

    public void testProcessEventPositive() {
        Event e = new BasicEvent(this);
        Filter f = new BasicFilter();
        Assert.assertTrue(f.processEvent(e));
    }

    public void testProcessEventNegative() {
        Event e = null;
        Filter f = new BasicFilter();
        Assert.assertFalse(f.processEvent(e));
    }
}
