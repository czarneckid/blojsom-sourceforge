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
 * Neither the name of the "David A. Czarnecki" nor the names of
 * its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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
package org.ignition.blojsom.extension.xmlrpc.handlers;

import org.ignition.blojsom.util.BlojsomConstants;
import org.ignition.blojsom.util.BlojsomUtils;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.blog.BlogCategory;
import org.ignition.blojsom.blog.BlogEntry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;

import java.util.Hashtable;
import java.util.HashMap;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;


/**
 * LiveJournal XML-RPC Handler for the LiveJournal API
 *
 * LiveJournal API pec can be found at http://www.livejournal.com/doc/server/ljp.csp.xml-rpc.protocol.html
 *
 * @author Mark Lussier
 * @version $Id: LiveJournalAPIHandler.java,v 1.1 2003-02-26 22:03:32 intabulas Exp $
 */
public class LiveJournalAPIHandler extends AbstractBlojsomAPIHandler implements BlojsomConstants {

    public static final String API_PREFIX = "LJ.XMLRPC";

    private Blog _blog;
    private Log _logger = LogFactory.getLog(LiveJournalAPIHandler.class);

    /**
     *
     */
    public LiveJournalAPIHandler() {
    }


    /**
     * Gets the Name of API Handler. Used to Bind to XML-RPC
     *
     * @return The API Name (ie: blogger)
     */
    public String getName() {
        return API_PREFIX;
    }

    /**
     * Attach a Blog instance to the API Handler so that it can interact with the blog
     * @param bloginstance an instance of Blog
     * @see org.ignition.blojsom.blog.Blog
     */
    public void setBlog(Blog bloginstance) {
        _blog = bloginstance;
    }


    public Object login(Object struct) {
        Hashtable  result = new Hashtable(3);



        return result;
    }



    /*
login - validate user's password and get base information needed for client to function
postevent - The most important mode, this is how a user actually submits a new log entry to the server.
editevent - Edit or delete a user's past journal entry
editfriendgroups - Edit the user's defined groups of friends.
editfriends - Add, edit, or delete friends from the user's friends list.
getevents - Download parts of the user's journal.
getfriends - Returns a list of which other LiveJournal users this user lists as their friend.
friendof - Returns a list of which other LiveJournal users list this user as their friend.
getfriendgroups - Retrieves a list of the user's defined groups of friends.
getdaycounts - This mode retrieves the number of journal entries per day.
syncitems - Returns a list of all the items that have been created or updated for a user.
checkfriends - Checks to see if your friends list has been updated since a specified time.
consolecommand - Run an administrative command.

    */


}
