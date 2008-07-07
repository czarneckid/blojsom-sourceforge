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
package org.blojsom.plugin.registration;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.blojsom.blog.Blog;
import org.blojsom.blog.User;
import org.blojsom.plugin.notification.AbstractVelocityEmailNotification;
import org.blojsom.plugin.notification.AbstractNotification;

import javax.mail.Address;
import javax.mail.MessagingException;
import java.net.URL;
import java.util.*;

/**
 * RegistrationNotification
 *
 * @author Eric Broyles
 * @version $Id: RegistrationNotification.java,v 1.3 2008-07-07 19:54:18 czarneckid Exp $
 */
public class RegistrationNotification extends AbstractVelocityEmailNotification {

    private Email email;

    /**
     * Create a new registration notification
     *
     * @param email         E-mail message
     * @param emailTemplate URL for template
     * @param user          {@link User}
     * @param blog          {@link Blog}
     * @throws MessagingException If there is an error setting the e-mail content
     */
    public RegistrationNotification(Email email, URL emailTemplate, User user, Blog blog) throws MessagingException {
        super(email, emailTemplate);
        setEmail(email);
        if (!"".equals(user.getUserName())) put("name", user.getUserName());
        else put("name", user.getUserLogin());
        put("subject", email.getSubject());
        put("blogName", blog.getBlogName());
        put("username", user.getUserLogin());
        put("password", user.getUserPassword());
        put("blogUrl", blog.getBlogURL());
        put("privacyUrl", blog.getProperty("privacy-policy-url", (new StringBuffer()).append(blog.getBlogURL()).append("/privacy.html").toString(), false));
        put("blogOwner", blog.getBlogOwner());
        put("activationUrl", (new StringBuffer()).append(blog.getBlogURL()).append("?plugins=registration&action=activate&username=").append(user.getUserLogin())
            .toString());
        try {
            email.setContent(getMessage(), "text/html");
        } catch (Exception e) {
            throw new MessagingException("Failed to set the content of the message. ", e);
        }
    }

    /**
     * @see AbstractNotification#getBlindCarbonCopyRecipients()
     */
    protected List getBlindCarbonCopyRecipients() {
        try {
            return convertElementsToString(getEmail().getMimeMessage().getRecipients(javax.mail.internet.MimeMessage.RecipientType.BCC));
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * @see AbstractNotification#getBlindCarbonCopyRecipients()
     */
    protected List getCarbonCopyRecipients() {
        try {
            return convertElementsToString(getEmail().getMimeMessage().getRecipients(javax.mail.internet.MimeMessage.RecipientType.CC));
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * @see AbstractNotification#getRecipients()
     */
    protected List getRecipients() {
        try {
            return convertElementsToString(getEmail().getMimeMessage().getRecipients(javax.mail.internet.MimeMessage.RecipientType.TO));
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * @see AbstractNotification#getSender()
     */
    protected String getSender() {
        try {
            return getEmail().getFromAddress().getAddress();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @see AbstractNotification#getSubject()
     */
    protected String getSubject() {
        return getEmail().getSubject();
    }

    /**
     * Retrieve the e-mail message
     *
     * @return E-mail message
     */
    public Email getEmail() {
        return email;
    }

    /**
     * Set the e-mail message
     *
     * @param email E-mail message
     */
    public void setEmail(Email email) {
        this.email = email;
    }

    /**
     * Send the e-mail
     *
     * @throws EmailException If there is an error sending the e-mail
     */
    public void send() throws EmailException {
        getEmail().send();
    }

    /**
     * Convert a list of addresses into the list
     *
     * @param addresses Array of addresses
     * @return List of addresses
     */
    protected List convertElementsToString(Address addresses[]) {
        if (addresses != null) {
            List addressList = Arrays.asList(addresses);
            List stringList = new ArrayList(addressList.size());
            Address address;
            for (Iterator iter = addressList.iterator(); iter.hasNext(); stringList.add(address
                .toString()))
                address = (Address) iter.next();

            return stringList;
        } else {
            return Collections.EMPTY_LIST;
        }
    }
}
