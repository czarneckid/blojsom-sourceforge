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
package org.blojsom.plugin.security;

import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.BaseAdminPlugin;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This plugin performs authorization after prompting the user in a web form.
 *
 * @author Eric Broyles
 * @author David Czarnecki
 * @version $Id: FormAuthenticationPlugin.java,v 1.3 2008-07-07 19:54:12 czarneckid Exp $
 * @since blojsom 3.1
 */
public class FormAuthenticationPlugin extends BaseAdminPlugin {

    /**
     * The default initial page to display upon successful login.
     */
    private static final String DEFAULT_INITIAL_PAGE = "/html";

    /**
     * The page presented to the user for supplying login credentials.
     */
    private static final String LOGIN_PAGE = "login";

    private String initialPage;

    /**
     * Default constructor.
     */
    public FormAuthenticationPlugin() {
    }

    /**
     * Get the initial page to load on successful authentication.  This can be themed.
     *
     * @return Initail page to load on successful authentication
     */
    public String getInitialPage() {
        if (initialPage == null) {
            initialPage = DEFAULT_INITIAL_PAGE;
        }

        return initialPage;
    }

    /**
     * Set the initial page to load on successful authentication.  This can be themed.
     *
     * @param initialPage Initial page to load on successful authentication
     */
    public void setInitialPage(String initialPage) {
        this.initialPage = initialPage;
    }

    /**
     * Initialize the plugin
     *
     * @throws PluginException If there is an error on initialization
     */
    public void init() throws PluginException {
        _ignoreParams = new HashMap();
        _ignoreParams.put("username", "username");
        _ignoreParams.put("password", "password");
        _ignoreParams.put("submit", "submit");
        _ignoreParams.put("reset", "reset");
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param blog                {@link Blog} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws PluginException If there is an error processing the blog entries
     */
    public Entry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, Map context, Entry entries[]) throws PluginException {
        if (!authenticateUser(httpServletRequest, httpServletResponse, context, blog)) {
            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, LOGIN_PAGE);
        } else {
            String page = BlojsomUtils.getRequestValue(BlojsomConstants.PAGE_PARAM, httpServletRequest);
            if (!BlojsomUtils.checkNullOrBlank(page)) {
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, page);
            } else {
                // Don't specify a PAGE_PARAM to dispatch to the default template for the flavor
            }

            if (httpServletRequest.getSession().getAttribute(BlojsomConstants.REDIRECT_TO_PARAM) != null) {
                String redirectURL = (String) httpServletRequest.getSession().getAttribute(BlojsomConstants.REDIRECT_TO_PARAM);

                try {
                    httpServletRequest.getSession().removeAttribute(BlojsomConstants.REDIRECT_TO_PARAM);
                    httpServletResponse.sendRedirect(redirectURL);
                } catch (IOException e) {
                    _logger.error(e);
                }
            }
        }

        return entries;
    }
}
