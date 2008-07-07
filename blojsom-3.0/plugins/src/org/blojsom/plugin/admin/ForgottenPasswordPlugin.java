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
package org.blojsom.plugin.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.blog.User;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.email.EmailConstants;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;
import java.util.Random;

/**
 * Forgotten password plugin
 *
 * @author David Czarnecki
 * @version $Id: ForgottenPasswordPlugin.java,v 1.5 2008-07-07 19:54:12 czarneckid Exp $
 * @since blojsom 3.0
 */
public class ForgottenPasswordPlugin extends BaseAdminPlugin {

    private Log _logger = LogFactory.getLog(ForgottenPasswordPlugin.class);

    // Localization constants
    private static final String FAILED_PASSWORD_CHANGE_KEY = "failed.password.change.text";
    private static final String CONSTRUCTED_PASSWORD_EMAIL_KEY = "constructed.password.email.text";
    private static final String USERNAME_BLANK_KEY = "username.blank.text";

    private static final String FORGOTTEN_USERNAME_PARAM = "forgotten-username";
    private static final String FORGOTTEN_PASSWORD_PAGE = "forgotten-password";

    private Fetcher _fetcher;
    private String _mailServer;
    private String _mailServerUsername;
    private String _mailServerPassword;
    private Session _session;

    /**
     * Default constructor.
     */
    public ForgottenPasswordPlugin() {
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
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error initializing the plugin
     */
    public void init() throws PluginException {
        super.init();

        _mailServer = _servletConfig.getInitParameter(EmailConstants.SMTPSERVER_IP);

        if (_mailServer != null) {
            if (_mailServer.startsWith("java:comp/env")) {
                try {
                    Context context = new InitialContext();
                    _session = (Session) context.lookup(_mailServer);
                } catch (NamingException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }

                    throw new PluginException(e);
                }
            } else {
                _mailServerUsername = _servletConfig.getInitParameter(EmailConstants.SMTPSERVER_USERNAME_IP);
                _mailServerPassword = _servletConfig.getInitParameter(EmailConstants.SMTPSERVER_PASSWORD_IP);
            }
        } else {
            if (_logger.isErrorEnabled()) {
                _logger.error("Missing SMTP servername servlet initialization parameter: " + EmailConstants.SMTPSERVER_IP);
            }
        }
    }

    /**
     * Setup the comment e-mail
     *
     * @param blog  {@link Blog} information
     * @param user  {@link User}
     * @param email Email message
     * @throws EmailException If there is an error preparing the e-mail message
     */
    protected void setupEmail(Blog blog, User user, Email email) throws EmailException {
        email.setCharset(BlojsomConstants.UTF8);

        // If we have a mail session for the environment, use that
        if (_session != null) {
            email.setMailSession(_session);
        } else {
            // Otherwise, if there is a username and password for the mail server, use that
            if (!BlojsomUtils.checkNullOrBlank(_mailServerUsername) && !BlojsomUtils.checkNullOrBlank(_mailServerPassword)) {
                email.setHostName(_mailServer);
                email.setAuthentication(_mailServerUsername, _mailServerPassword);
            } else {
                email.setHostName(_mailServer);
            }
        }

        email.setFrom(blog.getBlogOwnerEmail(), "Blojsom Forgotten Password");

        String authorizedUserEmail = user.getUserEmail();
        if (BlojsomUtils.checkNullOrBlank(authorizedUserEmail)) {
            authorizedUserEmail = blog.getBlogOwnerEmail();
        }

        String authorizedUser = user.getUserName();
        if (BlojsomUtils.checkNullOrBlank(authorizedUser)) {
            authorizedUser = user.getUserLogin();
        }

        email.addTo(authorizedUserEmail, authorizedUser);
        email.setSentDate(new Date());
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
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error processing the blog entries
     */
    public Entry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, Map context, Entry[] entries) throws PluginException {
        String username = BlojsomUtils.getRequestValue(FORGOTTEN_USERNAME_PARAM, httpServletRequest);
        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);

        if (!BlojsomUtils.checkNullOrBlank(username)) {
            User user;

            try {
                user = _fetcher.loadUser(blog, username);

                HtmlEmail email = new HtmlEmail();
                setupEmail(blog, user, email);

                StringBuffer emailText = new StringBuffer("Here's your password: ");
                if (blog.getUseEncryptedPasswords().booleanValue()) {
                    // Otherwise we have to create a new password since the password is one-way encrypted with MD5
                    Random random = new Random(new Date().getTime() + System.currentTimeMillis());
                    int password = random.nextInt(Integer.MAX_VALUE);
                    String updatedPassword = Integer.toString(password);

                    user.setUserPassword(BlojsomUtils.digestString(updatedPassword));
                    _fetcher.saveUser(blog, user);
                    emailText.append(updatedPassword);
                } else {
                    emailText.append(user.getUserPassword());
                }

                email.setHtmlMsg(emailText.toString());
                email.setTextMsg(emailText.toString());

                String to = user.getUserName();
                if (BlojsomUtils.checkNullOrBlank(to)) {
                    to = user.getUserLogin();
                }

                email.setSubject("Forgotten password e-mail for " + to);

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Constructed forgotten password e-mail message for username: " + username);
                }

                email.send();

                addOperationResultMessage(context, formatAdminResource(CONSTRUCTED_PASSWORD_EMAIL_KEY, CONSTRUCTED_PASSWORD_EMAIL_KEY, blog.getBlogAdministrationLocale(), new Object[]{to}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_LOGIN_PAGE);
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                addOperationResultMessage(context, formatAdminResource(FAILED_PASSWORD_CHANGE_KEY, FAILED_PASSWORD_CHANGE_KEY, blog.getBlogAdministrationLocale(), new Object[]{username}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_LOGIN_PAGE);

                return entries;
            } catch (EmailException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                addOperationResultMessage(context, formatAdminResource(FAILED_PASSWORD_CHANGE_KEY, FAILED_PASSWORD_CHANGE_KEY, blog.getBlogAdministrationLocale(), new Object[]{username}));
                httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, ADMIN_LOGIN_PAGE);

                return entries;
            }
        } else {
            if (BlojsomUtils.checkNullOrBlank(action)) {
                addOperationResultMessage(context, getAdminResource(USERNAME_BLANK_KEY, USERNAME_BLANK_KEY, blog.getBlogAdministrationLocale()));
            }

            httpServletRequest.setAttribute(BlojsomConstants.PAGE_PARAM, FORGOTTEN_PASSWORD_PAGE);
        }

        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws PluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error in finalizing this plugin
     */
    public void destroy() throws PluginException {
    }
}