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
package org.blojsom.blog;

import java.util.Date;
import java.util.Map;

/**
 * User
 *
 * @author David Czarnecki
 * @version $Id: User.java,v 1.5 2008-07-07 19:55:08 czarneckid Exp $
 * @since blojsom 3.0
 */
public interface User {

    /**
     * Get the user ID
     *
     * @return User ID
     */
    Integer getId();

    /**
     * Set the user ID
     *
     * @param id User ID
     */
    void setId(Integer id);

    /**
     * Get the blog ID
     *
     * @return Blog ID
     */
    Integer getBlogId();

    /**
     * Set the blog ID
     *
     * @param blogId Blog ID
     */
    void setBlogId(Integer blogId);

    /**
     * Get the user login
     *
     * @return User login
     */
    String getUserLogin();

    /**
     * Set the user login
     *
     * @param userLogin User login
     */
    void setUserLogin(String userLogin);

    /**
     * Get the password
     *
     * @return Password
     */
    String getUserPassword();

    /**
     * Set the password
     *
     * @param userPassword Password
     */
    void setUserPassword(String userPassword);

    /**
     * Get the user name
     *
     * @return User name
     */
    String getUserName();

    /**
     * Set the user name
     *
     * @param userName User name
     */
    void setUserName(String userName);

    /**
     * Get the user e-mail
     *
     * @return User e-mail
     */
    String getUserEmail();

    /**
     * Set the user e-mail
     *
     * @param userEmail User e-mail
     */
    void setUserEmail(String userEmail);

    /**
     * Get the user registered date
     *
     * @return User registered date
     */
    Date getUserRegistered();

    /**
     * Set the user registered date
     *
     * @param userRegistered User registered date
     */
    void setUserRegistered(Date userRegistered);

    /**
     * Get the user status
     *
     * @return User status
     */
    String getUserStatus();

    /**
     * Set the user status
     *
     * @param userStatus User status
     */
    void setUserStatus(String userStatus);

    /**
     * Get the meta-data
     *
     * @return Meta-data
     */
    Map getMetaData();

    /**
     * Set the meta-data
     *
     * @param metaData Meta-data
     */
    void setMetaData(Map metaData);
}
