/**
 * Copyright (c) 2003-2005, David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003-2005 by Mark Lussier
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
package org.blojsom.plugin.calendar;

import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;
import org.blojsom.plugin.BlojsomPluginException;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;

/**
 * AbstractVisualCalendarPlugin
 *
 * @author Mark Lussier
 * @version $Id: AbstractVisualCalendarPlugin.java,v 1.6 2005-01-05 02:31:36 czarneckid Exp $
 */
public abstract class AbstractVisualCalendarPlugin extends AbstractCalendarPlugin {

    protected BlogCalendar _blogCalendar;

    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param user                {@link BlogUser} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws BlojsomPluginException If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse,
                               BlogUser user,
                               Map context,
                               BlogEntry[] entries) throws BlojsomPluginException {

        entries = super.process(httpServletRequest, httpServletResponse, user, context, entries);
        Locale locale = (Locale) context.get(BLOJSOM_CALENDAR_LOCALE);
        BlogCalendar blogCalendar = (BlogCalendar) context.get(BLOJSOM_CALENDAR);

        Calendar entrycalendar = Calendar.getInstance(locale);
        if (entries != null && entries.length > 0) {
            for (int x = 0; x < entries.length; x++) {
                BlogEntry entry = entries[x];
                entrycalendar.setTime(entry.getDate());
                int entrymonth = entrycalendar.get(Calendar.MONTH);
                int entryyear = entrycalendar.get(Calendar.YEAR);
                // If the Entry is is the same month and the same year, then flag that date as having a Entry
                if ((entrymonth == blogCalendar.getCurrentMonth()) && (entryyear == blogCalendar.getCurrentYear())) {
                    blogCalendar.setEntryForDOM(entrycalendar.get(Calendar.DAY_OF_MONTH));
                }
            }

            if (context.containsKey(BlojsomConstants.BLOJSOM_PERMALINK)) {
                ArrayList files = new ArrayList();
                Date today = new Date();
                BlojsomUtils.visitFilesAndDirectories(today, user.getBlog().getBlogFileExtensions(), user.getBlog().getBlogDirectoryFilter(), new File(user.getBlog().getBlogHome()), files);
                Calendar checkCalendar = Calendar.getInstance(locale);
                for (int i = 0; i < files.size(); i++) {
                    File entryOnDisk = (File) files.get(i);
                    checkCalendar.setTimeInMillis(entryOnDisk.lastModified());
                    int entrymonth = checkCalendar.get(Calendar.MONTH);
                    int entryyear = checkCalendar.get(Calendar.YEAR);
                    if ((entrymonth == blogCalendar.getCurrentMonth()) && (entryyear == blogCalendar.getCurrentYear())) {
                        blogCalendar.setEntryForDOM(checkCalendar.get(Calendar.DAY_OF_MONTH));
                    }
                }
            }
        }

        context.put(BLOJSOM_CALENDAR, blogCalendar);
        return entries;
    }

}
