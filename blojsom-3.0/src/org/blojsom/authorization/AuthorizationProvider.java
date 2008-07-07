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
package org.blojsom.authorization;

import org.blojsom.ConfigurationException;
import org.blojsom.blog.Blog;
import org.blojsom.blog.User;

import java.util.Map;

/**
 * AuthorizationProvider
 *
 * @author David Czarnecki
 * @version $Id: AuthorizationProvider.java,v 1.6 2008-07-07 19:55:11 czarneckid Exp $
 * @since blojsom 3.0
 */
public interface AuthorizationProvider {

    /**
     * Initialization method for the authorization provider
     *
     * @throws ConfigurationException If there is an error initializing the provider
     */
    public void init() throws ConfigurationException;

    /**
     * Authorize a username and password for the given {@link Blog}
     *
     * @param blog                 {@link Blog}
     * @param authorizationContext {@link Map} to be used to provide other information for authorization. This will
     *                             change depending on the authorization provider.
     * @param userLogin            Username
     * @param password             Password
     * @throws AuthorizationException If there is an error authorizing the username and password
     */
    public void authorize(Blog blog, Map authorizationContext, String userLogin, String password) throws AuthorizationException;

    /**
     * Check a permission for the given {@link Blog}
     *
     * @param blog              {@link Blog}
     * @param permissionContext {@link Map} to be used to provide other information for permission check. This will
     *                          change depending on the authorization provider.
     * @param userLogin         Username
     * @param permission        Permission
     * @throws AuthorizationException If there is an error checking the permission for the username and permission
     */
    public void checkPermission(Blog blog, Map permissionContext, String userLogin, String permission) throws AuthorizationException;
}