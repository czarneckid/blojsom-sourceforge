/**
 * Copyright (c) 2003-2007, David A. Czarnecki
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
package org.blojsom.authorization.ldap;

import netscape.ldap.*;
import netscape.ldap.factory.JSSESocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.ConfigurationException;
import org.blojsom.authorization.AuthorizationException;
import org.blojsom.authorization.database.DatabaseAuthorizationProvider;
import org.blojsom.blog.Blog;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import java.util.Enumeration;
import java.util.Map;

/**
 * LDAPAuthorizationProvider
 * <p></p>
 * This implementation authenticates a user against an LDAP server.  The user
 * name must be the same as that of their LDAP user (uid).  There are two ways
 * to configure this in terms of the accepted users.  The first is where only
 * the blog owner can edit the blog.  To use this technique, delete the
 * authorization.properties file from the user's blog directory.  The lack of
 * this file tells the authorization logic to use the blog owner as the UID for
 * LDAP authentication.  The second way provides multiple user editing of a
 * blog.  This second way utilizes the authorization.properties file's user
 * names (it ignores passwords and other data).  Incoming authorization requests
 * have the user name checked to see if it is listed in the
 * authorization.properties file (indicating a user who is allowed to edit this
 * blog).  If it is in the list, this username is used as the LDAP UID.  This
 * class/implementation requires LDAP protocol version 3.  You must set the
 * configuration values defined by the BlojsomConstants:
 * BLOG_LDAP_AUTHORIZATION_SERVER_IP, BLOG_LDAP_AUTHORIZATION_DN_IP, and
 * BLOG_LDAP_AUTHORIZATION_PORT_IP (optional).
 * <p></p>
 * Note, this implementation currently requires the Mozilla LDAP Java SDK.  See
 * http://www.mozilla.org/directory/.
 *
 * @author David Czarnecki
 * @author Christopher Bailey
 * @version $Id: LDAPAuthorizationProvider.java,v 1.5 2007-01-17 01:15:46 czarneckid Exp $
 * @since blojsom 3.0
 */
public class LDAPAuthorizationProvider extends DatabaseAuthorizationProvider {

    private static final String BLOG_LDAP_AUTHORIZATION_SERVER_IP = "blog-ldap-authorization-server";
    private static final String BLOG_LDAP_AUTHORIZATION_PORT_IP = "blog-ldap-authorization-port";
    private static final String BLOG_LDAP_AUTHORIZATION_DN_IP = "blog-ldap-authorization-dn";
    private static final String BLOG_LDAP_AUTHORIZATION_UID_IP = "blog-ldap-authorization-uid";
    private static final String BLOG_LDAP_AUTHORIZATION_BINDING_USER_IP = "blog-ldap-authorization-bindinguser";
    private static final String BLOG_LDAP_AUTHORIZATION_BINDING_PASSWORD_IP = "blog-ldap-authorization-bindingpassword";
    private static final String BLOG_LDAP_AUTHORIZATION_USE_SSL = "blog-ldap-authorization-use-ssl";

    private static final String UID_DEFAULT = "uid";

    private Log _logger = LogFactory.getLog(LDAPAuthorizationProvider.class);
    private String _ldapServer;
    private int _ldapPort = 389;
    private String _ldapDN;
    private String _uidAttributeName = UID_DEFAULT;

    private String _bindingUser = null;
    private String _bindingPassword = null;
    private ServletConfig _servletConfig;
    private boolean _useSSL = false;

    /**
     * Default constructor
     */
    public LDAPAuthorizationProvider() {
    }

    /**
     * Set the {@link ServletConfig} for the fetcher to grab initialization parameters
     *
     * @param servletConfig {@link ServletConfig}
     */
    public void setServletConfig(ServletConfig servletConfig) {
        _servletConfig = servletConfig;
    }

    /**
     * Initialization method for the authorization provider
     *
     * @throws ConfigurationException If there is an error initializing the provider
     */
    public void init() throws ConfigurationException {
        super.init();

        _ldapServer = _servletConfig.getInitParameter(BLOG_LDAP_AUTHORIZATION_SERVER_IP);
        _ldapDN = _servletConfig.getInitParameter(BLOG_LDAP_AUTHORIZATION_DN_IP);
        String port = _servletConfig.getInitParameter(BLOG_LDAP_AUTHORIZATION_PORT_IP);
        if (!BlojsomUtils.checkNullOrBlank(_servletConfig.getInitParameter(BLOG_LDAP_AUTHORIZATION_UID_IP))) {
            _uidAttributeName = _servletConfig.getInitParameter(BLOG_LDAP_AUTHORIZATION_UID_IP);
        }

        if (!BlojsomUtils.checkNullOrBlank(_servletConfig.getInitParameter(BLOG_LDAP_AUTHORIZATION_USE_SSL))) {
            String bool = _servletConfig.getInitParameter(BLOG_LDAP_AUTHORIZATION_USE_SSL);
            _useSSL = Boolean.valueOf(bool).booleanValue();
        }

        // We don't setup a credentions map here, because with LDAP, you can't
        // obtain the user's passwords, you can only check/authenticate against
        // the LDAP server.  Instead, check each time in the authorize method.
        if (BlojsomUtils.checkNullOrBlank(_ldapServer)) {
            String msg = "No LDAP authorization server specified.";
            if (_logger.isErrorEnabled()) {
                _logger.error(msg);
            }

            throw new ConfigurationException(msg);
        }

        if (BlojsomUtils.checkNullOrBlank(_ldapDN)) {
            String msg = "No LDAP authorization DN specified.";
            if (_logger.isErrorEnabled()) {
                _logger.error(msg);
            }

            throw new ConfigurationException(msg);
        }

        if (!BlojsomUtils.checkNullOrBlank(port)) {
            try {
                _ldapPort = Integer.valueOf(port).intValue();
                if ((0 > _ldapPort) || (_ldapPort > 65535)) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error("LDAP port is not in valid range [0,65535].");
                    }

                    throw new NumberFormatException();
                }
            } catch (NumberFormatException nfe) {
                String msg = "Invalid LDAP port '" + port + "' specified.";
                if (_logger.isErrorEnabled()) {
                    _logger.error(msg);
                }

                throw new ConfigurationException(msg);
            }
        }

        _bindingUser = _servletConfig.getInitParameter(BLOG_LDAP_AUTHORIZATION_BINDING_USER_IP);
        _bindingPassword = _servletConfig.getInitParameter(BLOG_LDAP_AUTHORIZATION_BINDING_PASSWORD_IP);

        if (_logger.isDebugEnabled()) {
            _logger.debug("LDAP Authorization Provider server: " + _ldapServer);
            _logger.debug("LDAP Authorization Provider port: " + _ldapPort);
            _logger.debug("LDAP Authorization Provider DN: " + _ldapDN);
            _logger.debug("LDAP Authorization Provider UID: " + _uidAttributeName);
            _logger.debug("LDAP Authorization Provider binding user: " + _bindingUser);
            _logger.debug("LDAP Authorization Provider binding password: **********");
            _logger.debug("LDAP Authorization Provider UseSSL: " + _useSSL);

            _logger.debug("Initialized LDAP authorization provider");
        }
    }

    /**
     * Authorize a username and password for the given {@link Blog}
     *
     * @param blog                 {@link Blog}
     * @param authorizationContext {@link Map} to be used to provide other information for authorization. This will
     *                             change depending on the authorization provider. This parameter is not used in this implementation.
     * @param username             Username.  In this implementation, this value must match that of the blog user's ID.
     * @param password             Password
     * @throws AuthorizationException If there is an error authorizing the username and password
     */
    public void authorize(Blog blog, Map authorizationContext, String username, String password) throws AuthorizationException {
        String dn = getDN(username);

        if (BlojsomUtils.checkNullOrBlank(_ldapServer) || BlojsomUtils.checkNullOrBlank(dn)) {
            String msg = "Authorization failed for blog: " + blog.getBlogId() + " for username: " + username + "; " + "LDAP not properly configured";
            if (_logger.isErrorEnabled()) {
                _logger.error(msg);
            }

            throw new AuthorizationException(msg);
        }

        try {
            LDAPConnection ldapConnection;

            if (_useSSL) {
                JSSESocketFactory ldapSocketFactory = new JSSESocketFactory();
                ldapConnection = new LDAPConnection(ldapSocketFactory);
            } else {
                ldapConnection = new LDAPConnection();
            }

            // Connect to the directory server
            ldapConnection.connect(_ldapServer, _ldapPort);

            if (blog.getUseEncryptedPasswords().booleanValue()) {
                password = BlojsomUtils.digestString(password, blog.getDigestAlgorithm());
            }

            // Use simple authentication. The first argument
            // specifies the version of the LDAP protocol used.
            ldapConnection.authenticate(3, dn, password);

            ldapConnection.disconnect();
            if (_logger.isDebugEnabled()) {
                _logger.debug("Successfully authenticated user '" + username + "' via LDAP.");
            }
        } catch (LDAPException e) {
            String reason;
            switch (e.getLDAPResultCode()) {
                // The DN does not correspond to any existing entry
                case LDAPException.NO_SUCH_OBJECT:
                    reason = "The specified user does not exist: " + dn;
                    break;
                    // The password is incorrect
                case LDAPException.INVALID_CREDENTIALS:
                    reason = "Invalid password";
                    break;
                    // Some other error occurred
                default:
                    reason = "Failed to authenticate as " + dn + ", " + e;
                    break;
            }

            String msg = "Authorization failed for blog: " + blog.getBlogId() + " for username: " + username + "; " + reason;

            if (_logger.isErrorEnabled()) {
                _logger.error(msg);
            }

            throw new AuthorizationException(msg);
        }
    }

    /**
     * Get the DN for a given username
     *
     * @param username Username
     * @return DN for a given username or <code>null</code> if there is an exception in lookup
     */
    protected String getDN(String username) {
        try {
            LDAPConnection ldapConnection;

            if (_useSSL) {
                JSSESocketFactory ldapSocketFactory = new JSSESocketFactory();
                ldapConnection = new LDAPConnection(ldapSocketFactory);
            } else {
                ldapConnection = new LDAPConnection();
            }

            // Connect to the directory server
            ldapConnection.connect(_ldapServer, _ldapPort);

            // Authenticate with the server
            if (!BlojsomUtils.checkNullOrBlank(_bindingUser) && !BlojsomUtils.checkNullOrBlank(_bindingPassword)) {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Using LDAP authentication for LDAP connection");
                }

                ldapConnection.authenticate(3, _bindingUser, _bindingPassword);
            }

            // Search for the dn of the user given the username (uid).
            String[] attrs = {};
            LDAPSearchResults res = ldapConnection.search(_ldapDN, LDAPv2.SCOPE_SUB, "(" + _uidAttributeName + "=" + username + ")", attrs, true);

            if (!res.hasMoreElements()) {
                // No such user.
                if (_logger.isDebugEnabled()) {
                    _logger.debug("User '" + username + "' does not exist in LDAP directory.");
                }

                ldapConnection.disconnect();

                return null;
            }

            String dn = res.next().getDN();
            ldapConnection.disconnect();
            if (_logger.isDebugEnabled()) {
                _logger.debug("Successfully got user DN '" + dn + "' via LDAP.");
            }

            return dn;
        } catch (LDAPException e) {
            // Some exception occurred above; the search for the dn failed.
            return null;
        }
    }

    /**
     * Get a specific attribute value for a given username
     *
     * @param username  Username
     * @param attribute Attribute
     * @return attribute value for a given username or <code>null</code> if there is an exception in lookup
     */
    protected String getAttribute(String username, String attribute) {
        LDAPConnection ldapConnection = null;
        String value = null;

        try {
            // Connect to the server.
            if (_useSSL) {
                JSSESocketFactory ldapSocketFactory = new JSSESocketFactory();
                ldapConnection = new LDAPConnection(ldapSocketFactory);
            } else {
                ldapConnection = new LDAPConnection();
            }

            ldapConnection.connect(_ldapServer, _ldapPort);

            // Authenticate with the server
            if (!BlojsomUtils.checkNullOrBlank(_bindingUser) && !BlojsomUtils.checkNullOrBlank(_bindingPassword)) {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Using LDAP authentication for LDAP connection");
                }

                ldapConnection.authenticate(3, _bindingUser, _bindingPassword);
            }

            // Send the search request.
            String attrs[] = {attribute};
            LDAPSearchResults res = ldapConnection.search(_ldapDN, LDAPConnection.SCOPE_SUB, "(" + _uidAttributeName + "=" + username + ")", attrs, false);

            // Iterate through and print out the results.
            while (res.hasMoreElements()) {
                // Get the next directory entry.
                LDAPEntry findEntry = null;

                try {
                    findEntry = res.next();
                } catch (LDAPException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error("Error: " + e.toString());
                    }

                    continue;
                }

                // Print the DN of the entry.
                if (_logger.isDebugEnabled()) {
                    _logger.debug(findEntry.getDN());
                }

                // Get the attributes of the entry
                LDAPAttributeSet findAttrs = findEntry.getAttributeSet();
                Enumeration enumAttrs = findAttrs.getAttributes();

                if (_logger.isDebugEnabled()) {
                    _logger.debug("\tAttributes: ");
                }

                // Loop on attributes
                while (enumAttrs.hasMoreElements()) {
                    LDAPAttribute anAttr = (LDAPAttribute) enumAttrs.nextElement();
                    String attrName = anAttr.getName();
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("\t\t" + attrName);
                    }

                    // Loop on values for this attribute
                    Enumeration enumVals = anAttr.getStringValues();
                    if (enumVals != null) {
                        while (enumVals.hasMoreElements()) {
                            String aVal = (String) enumVals.nextElement();
                            value = aVal;

                            if (_logger.isDebugEnabled()) {
                                _logger.debug("\t\t\t" + aVal);
                            }
                        }
                    }
                }
            }
        } catch (LDAPException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error("Error: " + e.toString());
            }
        }

        // Done, so disconnect
        if ((ldapConnection != null) && ldapConnection.isConnected()) {
            try {
                ldapConnection.disconnect();
            } catch (LDAPException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error("Error: " + e.toString());
                }
            }
        }

        return value;
    }

    /**
     * Return the LDAP server name
     *
     * @return LDAP server name
     */
    protected String getServer() {
        return _ldapServer;
    }

    /**
     * Return the LDAP server port
     *
     * @return LDAP server port
     */
    protected int getPort() {
        return _ldapPort;
    }

    /**
     * Return the LDAP base DN
     *
     * @return LDAP base DN
     */
    protected String getBaseDN() {
        return _ldapDN;
    }
}
