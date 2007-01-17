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
package org.blojsom.plugin.registration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.blog.User;
import org.blojsom.blog.database.DatabaseUser;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.BaseAdminPlugin;
import org.blojsom.plugin.notification.Notification;
import org.blojsom.util.BlojsomUtils;
import org.blojsom.util.resources.ResourceManager;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.util.*;

/**
 * This plugin allows users to register for an account on the blog. Can be
 * combined with various authorization plugins to force users to login before
 * they can access the blog.
 * 
 * @author Eric Broyles
 * @version $Id: RegistrationPlugin.java,v 1.2 2007-01-17 02:35:05 czarneckid Exp $
 */
public class RegistrationPlugin extends BaseAdminPlugin {
    private Log _logger;

    private static final String REGISTRATION_USERNAME_PARAM = "username";

    private static final String REGISTRATION_FULLNAME_PARAM = "fullname";

    private static final String REGISTRATION_EMAIL_PARAM = "email";

    private static final String REGISTRATION_PAGE = "registration";

    private static final String FAILED_REGISTRATION_KEY = "failed.registration.text";

    private static final String CONSTRUCTED_REGISTRATION_EMAIL_KEY = "constructed.registration.email.text";

    private static final String USERNAME_BLANK_KEY = "username.blank.text";

    private static final String REGISTRATION_ACTIVATION_SUCCESS_KEY = "registration.activation.successful.text";

    private static final String REGISTRATION_ACTIVATION_FAILED_KEY = "registration.activation.failed.text";

    private static final String REGISTRATION_ACTIVATION_ACTION = "activate";

    private static final String REGISTERED_STATUS = "registered";

    private static final String APPROVED_STATUS = "approved";

    private static final String REGISTRATION_USER_REQUIRED_METADATA_KEYS = "registration-user-required-metadata-keys";

    private static final String REGISTRATION_USER_OPTIONAL_METADATA_KEYS = "registration-user-optional-metadata-keys";

    private static final String REGISTRATION_MISSING_REQUIRED_VALUE_KEY = "registration.missing.required.value.text";

    protected static final String REGISTRATION_MESSAGES_RESOURCE = "org.blojsom.plugin.registration.messages";

    private Fetcher _fetcher;

    private String _mailServer;

    private String _mailServerUsername;

    private String _mailServerPassword;

    private Session _session;

    /**
     *
     */
    public RegistrationPlugin() {
        _logger = LogFactory.getLog(RegistrationPlugin.class);
    }

    /**
     * Set the Fetcher.  Usually injected by Spring.
     *
     * @param fetcher {@link Fetcher}
     */
    public void setFetcher(Fetcher fetcher) {
        _fetcher = fetcher;
    }

    /**
     * @see org.blojsom.plugin.admin.BaseAdminPlugin#init()
     */
    public void init() throws PluginException {
        super.init();
        _mailServer = _servletConfig.getServletContext().getInitParameter("smtp-server");
        if (_mailServer != null) {
            if (_mailServer.startsWith("java:comp/env")) {
                try {
                    Context context = new InitialContext();
                    _session = (Session) context.lookup(_mailServer);
                } catch (NamingException e) {
                    if (_logger.isErrorEnabled()) _logger.error(e);
                    throw new PluginException(e);
                }
            } else {
                _mailServerUsername = _servletConfig.getServletContext().getInitParameter("smtp-server-username");
                _mailServerPassword = _servletConfig.getServletContext().getInitParameter("smtp-server-password");
            }
        } else
        if (_logger.isErrorEnabled()) _logger.error("Missing SMTP servername servlet initialization parameter: smtp-server");
    }

    /**
     * Setup an email for the given Blog and User.  Sets the from address to the blog owner's
     * email address and the blog's name.  Sets the to address to the user's email address
     * and the blog owner's email address.
     *
     * @param blog {@link Blog}
     * @param user {@link User}
     * @param email E-mail message
     * @throws EmailException If there is an error setting e-mail attributes
     */
    protected void setupEmail(Blog blog, User user, Email email) throws EmailException {
        email.setCharset("UTF-8");
        if (_session != null) email.setMailSession(_session);
        else if (!BlojsomUtils.checkNullOrBlank(_mailServerUsername) && !BlojsomUtils.checkNullOrBlank(_mailServerPassword)) {
            email.setHostName(_mailServer);
            email.setAuthentication(_mailServerUsername, _mailServerPassword);
        } else {
            email.setHostName(_mailServer);
        }
        email.setFrom(blog.getBlogOwnerEmail(), blog.getBlogName());
        String authorizedUserEmail = user.getUserEmail();
        if (BlojsomUtils.checkNullOrBlank(authorizedUserEmail)) authorizedUserEmail = blog.getBlogOwnerEmail();
        String authorizedUser = user.getUserName();
        if (BlojsomUtils.checkNullOrBlank(authorizedUser)) authorizedUser = user.getUserLogin();
        email.addTo(authorizedUserEmail, authorizedUser);
        email.setSentDate(new Date());
    }

    /**
     * @see org.blojsom.plugin.admin.BaseAdminPlugin#process(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse,org.blojsom.blog.Blog,java.util.Map,org.blojsom.blog.Entry[])
     */
    public Entry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, Map context, Entry entries[]) throws PluginException {
        String username = BlojsomUtils.getRequestValue(REGISTRATION_USERNAME_PARAM, httpServletRequest);
        String fullname = BlojsomUtils.getRequestValue(REGISTRATION_FULLNAME_PARAM, httpServletRequest);
        String userEmail = BlojsomUtils.getRequestValue(REGISTRATION_EMAIL_PARAM, httpServletRequest);
        String action = BlojsomUtils.getRequestValue(ACTION_PARAM, httpServletRequest);
        if (action != null && REGISTRATION_ACTIVATION_ACTION.equals(action)) {
            User user;
            try {
                user = _fetcher.loadUser(blog, username);
                user.setUserStatus(APPROVED_STATUS);
                _fetcher.saveUser(blog, user);
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) _logger.error(e);
                addOperationResultMessage(context, formatRegistrationResource(REGISTRATION_ACTIVATION_FAILED_KEY, REGISTRATION_ACTIVATION_FAILED_KEY, blog.getBlogLocale(), new Object[]{username}));
                httpServletRequest.setAttribute(PAGE_ACTION, REGISTRATION_PAGE);
                return entries;
            }
            addOperationResultMessage(context, formatRegistrationResource(REGISTRATION_ACTIVATION_SUCCESS_KEY, REGISTRATION_ACTIVATION_SUCCESS_KEY, blog
                    .getBlogLocale(), new Object[]{user.getUserName(), user.getUserLogin()}));
            httpServletRequest.setAttribute("page", REGISTRATION_PAGE);
            return entries;
        }
        if (!BlojsomUtils.checkNullOrBlank(username)) {
            if (fullname == null || ("").equals(fullname)) {
                notifyOfMissingParameter("name", context, blog, httpServletRequest);
                return entries;
            }
            if (userEmail == null || ("").equals(userEmail)) {
                notifyOfMissingParameter("email address", context, blog, httpServletRequest);
                return entries;
            }
            try {
                Date now = new Date();
                User user = new DatabaseUser();
                user.setBlogId(blog.getId());
                user.setUserLogin(username);
                user.setUserName(fullname);
                user.setUserEmail(userEmail);
                user.setUserRegistered(now);
                user.setUserStatus(REGISTERED_STATUS);
                try {
                    addUserMetaData(httpServletRequest, blog, context, user, blog
                            .getProperty(REGISTRATION_USER_REQUIRED_METADATA_KEYS), true);
                } catch (MissingParameterException e) {
                    return entries;
                }
                addUserMetaData(httpServletRequest, blog, context, user, blog
                        .getProperty(REGISTRATION_USER_OPTIONAL_METADATA_KEYS), false);
                Random random = new Random(now.getTime() + System.currentTimeMillis());
                int password = random.nextInt(0x7fffffff);
                String updatedPassword = Integer.toString(password);

                if (blog.getUseEncryptedPasswords().booleanValue()) {
                    user.setUserPassword(BlojsomUtils.digestString(updatedPassword, blog.getDigestAlgorithm()));
                } else {
                    user.setUserPassword(updatedPassword);
                }
                _fetcher.saveUser(blog, user);

                if (blog.getUseEncryptedPasswords().booleanValue()) {
                    // Set the user's password to the plain text version so we can email it to them; don't save it this way
                    user.setUserPassword(updatedPassword);
                }

                Notification notification = constructEmail(blog, user, httpServletRequest
                        .getParameter("flavor"));
                notification.send();

                if (_logger.isDebugEnabled()) {
                    _logger.debug((new StringBuffer()).append("Constructed registration e-mail message for username: ").append(username).toString());
                }
                addOperationResultMessage(context, formatRegistrationResource(CONSTRUCTED_REGISTRATION_EMAIL_KEY, CONSTRUCTED_REGISTRATION_EMAIL_KEY, blog.getBlogLocale(), new Object[]{fullname, userEmail}));
                httpServletRequest.setAttribute(PAGE_ACTION, REGISTRATION_PAGE);
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
                notifyOfFailedRegistration(username, context, blog, httpServletRequest);
                return entries;
            } catch (EmailException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
                notifyOfFailedRegistration(username, context, blog, httpServletRequest);
                return entries;
            } catch (MessagingException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
                notifyOfFailedRegistration(username, context, blog, httpServletRequest);
                return entries;
            }
        } else {
            if (BlojsomUtils.checkNullOrBlank(action)) {
                addOperationResultMessage(context, getAdminResource(USERNAME_BLANK_KEY, USERNAME_BLANK_KEY, blog.getBlogLocale()));
            }
            httpServletRequest.setAttribute(PAGE_ACTION, REGISTRATION_PAGE);
        }
        return entries;
    }

    /**
     * Add metadata to the user.  If the metadata is required and it's not present
     * the user will be notified that required data is missing.
     *
     * @param httpServletRequest   the request
     * @param blog                 the Blog
     * @param context              Context
     * @param user                 the user who's registering
     * @param metaDataPropertyKeys the keys used to access the metadata values from the context
     * @param isRequired If the metadata is required
     * @throws MissingParameterException when a required parameter is missing
     */
    protected void addUserMetaData(HttpServletRequest httpServletRequest, Blog blog, Map context, User user, String metaDataPropertyKeys, boolean isRequired) throws MissingParameterException {
        if (metaDataPropertyKeys != null) {
            String keys[] = metaDataPropertyKeys.split(",");
            if (keys.length > 0) {
                Map metaData = new HashMap();
                for (int i = 0; i < keys.length; i++) {
                    String key = keys[i].trim();
                    try {
                        String formValue = BlojsomUtils.getRequestValue(key, httpServletRequest);
                        if (formValue != null && !"".equals(formValue)) {
                            metaData.put(key, formValue);
                        } else if (isRequired) {
                            notifyOfMissingParameter(key, context, blog, httpServletRequest);
                            throw new MissingParameterException();
                        }
                    } catch (NullPointerException e) {
                        if (_logger.isErrorEnabled()) _logger.error((new StringBuffer()).append("Property ").append(key)
                                .append(" was null").toString(), e);
                    }
                }

                user.setMetaData(metaData);
            }
        }
    }

    /**
     * @see org.blojsom.plugin.admin.BaseAdminPlugin#cleanup()
     */
    public void cleanup() throws PluginException {
    }

    /**
     * @see org.blojsom.plugin.admin.BaseAdminPlugin#destroy()
     */
    public void destroy() throws PluginException {
    }

    /**
     * Construct an email notification to the user with their registration details.
     *
     * @param blog {@link Blog}
     * @param user {@link User}
     * @param flavor Flavor
     * @return the Notification
     * @throws EmailException If there is an error setting e-mail attributes
     * @throws MessagingException If there is an error sending the e-mail
     */
    protected Notification constructEmail(Blog blog, User user, String flavor) throws EmailException, MessagingException {
        HtmlEmail email = new HtmlEmail();
        setupEmail(blog, user, email);
        String to = user.getUserName();
        if (BlojsomUtils.checkNullOrBlank(to)) {
            to = user.getUserLogin();
        }
        email.setSubject((new StringBuffer()).append("Registration details for ").append(to)
                .toString());
        String templatePath = (new StringBuffer()).append("/WEB-INF/themes/").append(flavor)
                .append("/templates/").append(flavor).append("-registration-email.vm").toString();
        java.net.URL emailTemplate;
        try {
            emailTemplate = _servletConfig.getServletContext().getResource(templatePath);
        } catch (MalformedURLException e) {
            throw new MessagingException("Cannot find email template", e);
        }
        Notification notification = new RegistrationNotification(email, emailTemplate, user, blog);
        email.buildMimeMessage();
        return notification;
    }

    /**
     * Retrieve a resource from the registration resource bundle and pass it through the {@link ResourceManager#format(String,Object[])} method
     *
     * @param resourceID   ID of resource to retrieve
     * @param fallbackText Text to use as fallback if resource ID is not found
     * @param locale       {@link Locale} to use when retrieving resource
     * @param arguments    Arguments for {@link ResourceManager#format(String,Object[])}
     * @return Text from administration resource bundle given by <code>resourceID</code> formatted appropriately or <code>fallbackText</code> if the resource ID could not be formatted
     */
    protected String formatRegistrationResource(String resourceID, String fallbackText, Locale locale, Object[] arguments) {
        String resourceText = getRegistrationResource(resourceID, fallbackText, locale);

        String formattedText = _resourceManager.format(resourceText, arguments);
        if (formattedText == null) {
            formattedText = fallbackText;
        }

        return formattedText;
    }


    /**
     * Retrieve a resource from the registration resource bundle
     *
     * @param resourceID   ID of resource to retrieve
     * @param fallbackText Text to use as fallback if resource ID is not found
     * @param locale       {@link Locale} to use when retrieving resource
     * @return Text from administration resource bundle given by <code>resourceID</code> or <code>fallbackText</code> if the resource ID is not found
     */
    protected String getRegistrationResource(String resourceID, String fallbackText, Locale locale) {
        return _resourceManager.getString(resourceID, REGISTRATION_MESSAGES_RESOURCE, fallbackText, locale);
    }

    /**
     * Notify the user that a required registration parameter is missing.
     *
     * @param parameterName      the name of the missing parameter
     * @param context            Context
     * @param blog               the Blog
     * @param httpServletRequest the request
     */
    protected void notifyOfMissingParameter(String parameterName, Map context, Blog blog, HttpServletRequest httpServletRequest) {
        addOperationResultMessage(context, formatRegistrationResource(REGISTRATION_MISSING_REQUIRED_VALUE_KEY, REGISTRATION_MISSING_REQUIRED_VALUE_KEY, blog.getBlogLocale(), new Object[]{parameterName}));
        httpServletRequest.setAttribute(PAGE_ACTION, REGISTRATION_PAGE);
    }

    /**
     * Notify the user that their registration has failed for some reason.
     *
     * @param username           the username of the user for which registration failed
     * @param context            Context
     * @param blog               the Blog
     * @param httpServletRequest the request
     */
    protected void notifyOfFailedRegistration(String username, Map context, Blog blog, HttpServletRequest httpServletRequest) {

        addOperationResultMessage(context, formatRegistrationResource(FAILED_REGISTRATION_KEY, FAILED_REGISTRATION_KEY, blog.getBlogLocale(),
				new Object[] { username }));
		httpServletRequest.setAttribute(PAGE_ACTION, REGISTRATION_PAGE);
		
	}

}
