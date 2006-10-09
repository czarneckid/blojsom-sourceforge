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
package org.blojsom.extension.xmlrpc.handler;

import org.apache.xmlrpc.XmlRpcException;
import org.blojsom.authorization.AuthorizationException;
import org.blojsom.authorization.AuthorizationProvider;
import org.blojsom.blog.Blog;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.fetcher.Fetcher;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Properties;

/**
 * API Handler
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: APIHandler.java,v 1.3 2006-10-09 19:40:59 czarneckid Exp $
 */
public abstract class APIHandler {

    protected static final int AUTHORIZATION_EXCEPTION = 1;
    protected static final String AUTHORIZATION_EXCEPTION_MSG = "Invalid username and/or password";

    protected static final int UNKNOWN_EXCEPTION = 1000;
    protected static final String UNKNOWN_EXCEPTION_MSG = "An error occured processing your request";

    protected static final int UNSUPPORTED_EXCEPTION = 1001;
    protected static final String UNSUPPORTED_EXCEPTION_MSG = "Unsupported method";

    protected static final int INVALID_POSTID = 2000;
    protected static final String INVALID_POSTID_MSG = "The entry postid you submitted is invalid";

    protected static final int NOBLOGS_EXCEPTION = 3000;
    protected static final String NOBLOGS_EXCEPTION_MSG = "There are no categories defined";

    protected static final int PERMISSION_EXCEPTION = 4000;
    protected static final String PERMISSION_EXCEPTION_MSG = "User does not have permission to use this XML-RPC method";

    protected static final String ALL_XMLRPC_EDIT_PERMISSION = "all_xmlrpc_edit_permission";

    protected AuthorizationProvider _authorizationProvider;
    protected Fetcher _fetcher;
    protected Blog _blog;
    protected HttpServletRequest _httpServletRequest;
    protected HttpServletResponse _httpServletResponse;
    protected EventBroadcaster _eventBroadcaster;
    protected Properties _properties;
    protected ServletConfig _servletConfig;

    /**
     * Set the {@link AuthorizationProvider}
     *
     * @param authorizationProvider {@link AuthorizationProvider}
     */
    public void setAuthorizationProvider(AuthorizationProvider authorizationProvider) {
        _authorizationProvider = authorizationProvider;
    }

    /**
     * Set the {@link Fetcher}
     *
     * @param fetcher {@link Fetcher}
     */
    public void setFetcher(Fetcher fetcher) {
        _fetcher = fetcher;
    }

    /**
     * Set the {@link Blog}
     *
     * @param blog {@link Blog}
     */
    public void setBlog(Blog blog) {
        _blog = blog;
    }

    /**
     * Set the {@link HttpServletRequest}
     *
     * @param httpServletRequest {@link HttpServletRequest}
     */
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        _httpServletRequest = httpServletRequest;
    }

    /**
     * Set the {@link HttpServletResponse}
     *
     * @param httpServletResponse {@link HttpServletResponse}
     */
    public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
        _httpServletResponse = httpServletResponse;
    }

    /**
     * Set the {@link EventBroadcaster}
     *
     * @param eventBroadcaster {@link EventBroadcaster}
     */
    public void setEventBroadcaster(EventBroadcaster eventBroadcaster) {
        _eventBroadcaster = eventBroadcaster;
    }

    /**
     * Set the {@link ServletConfig}
     *
     * @param servletConfig {@link ServletConfig}
     */
    public void setServletConfig(ServletConfig servletConfig) {
        _servletConfig = servletConfig;
    }

    /**
     * Set the properties for the handler
     *
     * @param properties Handler properties
     */
    public void setProperties(Properties properties) {
        _properties = properties;
    }

    /**
     * Retrieve the API handler name
     *
     * @return API handler name
     */
    public abstract String getName();

    /**
     * Check XML-RPC permissions for a given username
     *
     * @param username Username
     * @param permission Permisison to check
     * @throws org.apache.xmlrpc.XmlRpcException If the username does not have the required permission
     */
    protected void checkXMLRPCPermission(String username, String permission) throws XmlRpcException {
        try {
            _authorizationProvider.checkPermission(_blog, new HashMap(), username, permission);
        } catch (AuthorizationException e) {
            throw new XmlRpcException(PERMISSION_EXCEPTION, PERMISSION_EXCEPTION_MSG);
        }
    }
}
