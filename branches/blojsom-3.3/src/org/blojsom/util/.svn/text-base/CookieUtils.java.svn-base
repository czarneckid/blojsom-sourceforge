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
package org.blojsom.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * CookieUtils
 *
 * @author David Czarnecki
 * @version $Id: CookieUtils.java,v 1.6 2008-07-07 19:55:06 czarneckid Exp $
 * @since blojsom 3.0
 */
public class CookieUtils {

    /**
     * Return a cookie given a particular key
     *
     * @param httpServletRequest Request
     * @param cookieKey          Cookie key
     * @return <code>Cookie</code> of the requested key or <code>null</code> if no cookie
     *         under that name is found
     */
    public static Cookie getCookie(HttpServletRequest httpServletRequest, String cookieKey) {
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies == null) {
            return null;
        }

        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if (cookie.getName().equals(cookieKey)) {
                return cookie;
            }
        }

        return null;
    }

    /**
     * Add a cookie with a key and value to the response
     *
     * @param httpServletResponse Response
     * @param cookieExpiration    Cookie expiration (in seconds)
     * @param cookieKey           Cookie key
     * @param cookieValue         Cookie value
     */
    public static void addCookie(HttpServletResponse httpServletResponse, int cookieExpiration, String cookieKey, String cookieValue) {
        Cookie cookie = new Cookie(cookieKey, BlojsomUtils.urlEncode(cookieValue));
        cookie.setMaxAge(cookieExpiration);
        httpServletResponse.addCookie(cookie);
    }
}
