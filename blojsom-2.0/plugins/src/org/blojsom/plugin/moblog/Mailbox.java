/**
 * Copyright (c) 2003-2004, David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003-2004 by Mark Lussier
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
package org.blojsom.plugin.moblog;

import org.blojsom.blog.BlogUser;

/**
 * Mailbox Definition
 * This file is a container for everything the thread will need to connect to and to store into
 *
 * @author Mark Lussier
 * @version $Id: Mailbox.java,v 1.3 2004-04-26 02:43:04 intabulas Exp $
 * @since blojsom 2.14
 */
public class Mailbox {

    private String _hostName = null;
    private String _userId = null;
    private String _password = null;
    private String _folder = "INBOX";
    private String _outputDirectory;
    private String _entriesDirectory;
    private String _categoryName;
    private String _urlPrefix;
    private boolean _enabled;
    private BlogUser _user;


    /**
     * 
     */
    public Mailbox() {
    }

    /**
     * @param hostname
     * @param userid
     * @param password
     */
    public Mailbox(String hostname, String userid, String password) {
        _hostName = hostname;
        _userId = userid;
        _password = password;
    }

    /**
     * @return
     */
    public String getHostName() {
        return _hostName;
    }

    /**
     * @param hostName
     */
    public void setHostName(String hostName) {
        _hostName = hostName;
    }

    /**
     * @return
     */
    public String getUserId() {
        return _userId;
    }

    /**
     * @param userId
     */
    public void setUserId(String userId) {
        _userId = userId;
    }

    /**
     * @return
     */
    public String getPassword() {
        return _password;
    }

    /**
     * @param password
     */
    public void setPassword(String password) {
        _password = password;
    }

    /**
     * @return
     */
    public String getFolder() {
        return _folder;
    }

    /**
     * @param folder
     */
    public void setFolder(String folder) {
        _folder = folder;
    }

    /**
     * @return
     */
    public String getOutputDirectory() {
        return _outputDirectory;
    }

    /**
     * @param outputDirectory
     */
    public void setOutputDirectory(String outputDirectory) {
        _outputDirectory = outputDirectory;
    }

    /**
     * @return
     */
    public String getUrlPrefix() {
        return _urlPrefix;
    }

    /**
     * @param urlPrefix
     */
    public void setUrlPrefix(String urlPrefix) {
        _urlPrefix = urlPrefix;
    }

    /**
     * @return
     */
    public String getEntriesDirectory() {
        return _entriesDirectory;
    }

    /**
     * @param entriesDirectory
     */
    public void setEntriesDirectory(String entriesDirectory) {
        _entriesDirectory = entriesDirectory;
    }

    /**
     * @return
     */
    public String getCategoryName() {
        return _categoryName;
    }

    /**
     * @param categoryName
     */
    public void setCategoryName(String categoryName) {
        _categoryName = categoryName;
    }

    /**
     * @return
     */
    public boolean isEnabled() {
        return _enabled;
    }

    /**
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        _enabled = enabled;
    }

    /**
     * @return
     */
    public BlogUser getBlogUser() {
        return _user;
    }

    /**
     * @param user
     */
    public void setBlogUser(BlogUser user) {
        this._user = user;
    }
}
