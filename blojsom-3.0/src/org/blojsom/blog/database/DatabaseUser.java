/**
 * Copyright (c) 2003-2006, David A. Czarnecki
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
package org.blojsom.blog.database;

import org.blojsom.blog.User;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;

/**
 * DatabaseUser
 *
 * @author David Czarnecki
 * @version $Id: DatabaseUser.java,v 1.4 2006-05-13 14:09:56 czarneckid Exp $
 * @since blojsom 3.0
 */
public class DatabaseUser implements User, Serializable {

    protected Integer _id;
    protected String _blogId;
    protected String _userLogin;
    protected String _userPassword;
    protected String _userName;
    protected String _userEmail;
    protected String _userStatus;
    protected Date _userRegistered;
    protected Map _metaData;

    /**
     * Create a new instance of the database user
     */
    public DatabaseUser() {
    }

    /**
     * Get the user ID
     *
     * @return User ID
     */
    public Integer getId() {
        return _id;
    }

    /**
     * Set the user ID
     *
     * @param id User ID
     */
    public void setId(Integer id) {
        _id = id;
    }

    /**
     * Get the blog ID
     *
     * @return Blog ID
     */
    public String getBlogId() {
        return _blogId;
    }

    /**
     * Set the blog ID
     *
     * @param blogId Blog ID
     */
    public void setBlogId(String blogId) {
        _blogId = blogId;
    }

    /**
     * Get the user login
     *
     * @return User login
     */
    public String getUserLogin() {
        return _userLogin;
    }

    /**
     * Set the user login
     *
     * @param userLogin User login
     */
    public void setUserLogin(String userLogin) {
        _userLogin = userLogin;
    }

    /**
     * Get the password
     *
     * @return Password
     */
    public String getUserPassword() {
        return _userPassword;
    }

    /**
     * Set the password
     *
     * @param userPassword Password
     */
    public void setUserPassword(String userPassword) {
        _userPassword = userPassword;
    }

    /**
     * Get the user name
     *
     * @return User name
     */
    public String getUserName() {
        return _userName;
    }

    /**
     * Set the user name
     *
     * @param userName User name
     */
    public void setUserName(String userName) {
        _userName = userName;
    }

    /**
     * Get the user e-mail
     *
     * @return User e-mail
     */
    public String getUserEmail() {
        return _userEmail;
    }

    /**
     * Set the user e-mail
     *
     * @param userEmail User e-mail
     */
    public void setUserEmail(String userEmail) {
        _userEmail = userEmail;
    }

    /**
     * Get the user registered date
     *
     * @return User registered date
     */
    public Date getUserRegistered() {
        return _userRegistered;
    }

    /**
     * Set the user registered date
     *
     * @param userRegistered User registered date
     */
    public void setUserRegistered(Date userRegistered) {
        _userRegistered = userRegistered;
    }

    /**
     * Get the user status
     *
     * @return User status
     */
    public String getUserStatus() {
        return _userStatus;
    }

    /**
     * Set the user status
     *
     * @param userStatus User status
     */
    public void setUserStatus(String userStatus) {
        _userStatus = userStatus;
    }

    /**
     * Get the meta-data
     *
     * @return Meta-data
     */
    public Map getMetaData() {
        if (_metaData == null) {
            return new HashMap();
        }

        return _metaData;
    }


    /**
     * Set the meta-data
     *
     * @param metaData Meta-data
     */
    public void setMetaData(Map metaData) {
        _metaData = metaData;
    }
}
