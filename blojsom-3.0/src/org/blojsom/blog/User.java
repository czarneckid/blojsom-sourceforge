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
package org.blojsom.blog;

import java.util.Date;
import java.util.Map;

/**
 * User
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: User.java,v 1.1 2006-03-20 21:31:13 czarneckid Exp $
 */
public interface User {

    /**
     *
     * @return
     */
    public Integer getId();

    /**
     *
     * @param id
     */
    public void setId(Integer id);

    /**
     *
     * @return
     */
    public String getBlogId();

    /**
     *
     * @param blogId
     */
    public void setBlogId(String blogId);

    /**
     *
     * @return
     */
    public String getUserLogin();

    /**
     *
     * @param userLogin
     */
    public void setUserLogin(String userLogin);

    /**
     *
     * @return
     */
    public String getUserPassword();

    /**
     *
     * @param userPassword
     */
    public void setUserPassword(String userPassword);

    /**
     *
     * @return
     */
    public String getUserName();

    /**
     *
     * @param userName
     */
    public void setUserName(String userName);

    /**
     *
     * @return
     */
    public String getUserEmail();

    /**
     *
     * @param userEmail
     */
    public void setUserEmail(String userEmail);

    /**
     *
     * @return
     */
    public Date getUserRegistered();

    /**
     *
     * @param userRegistered
     */
    public void setUserRegistered(Date userRegistered);

    /**
     *
     * @return
     */
    public String getUserStatus();

    /**
     *
     * @param userStatus
     */
    public void setUserStatus(String userStatus);

    /**
     *
     * @return
     */
    public Map getMetaData();

    /**
     * 
     * @param metaData
     */
    public void setMetaData(Map metaData);
}
