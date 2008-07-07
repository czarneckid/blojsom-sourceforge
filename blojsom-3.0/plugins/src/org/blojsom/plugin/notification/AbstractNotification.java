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
package org.blojsom.plugin.notification;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractNotification implements Notification {

    private Log _logger;

    protected String charEncoding;

    protected URL emailTemplate;

    protected String mailServer;

    protected String contentType;

    /**
     * Create a new abstract notification
     *
     * @param emailTemplate E-mail template
     * @param charEncoding  Character encoding
     * @param mailServer    Mail server name
     * @param contentType   Content type
     */
    public AbstractNotification(URL emailTemplate, String charEncoding, String mailServer, String contentType) {
        _logger = LogFactory.getLog(AbstractNotification.class);
        if (emailTemplate == null) {
            throw new IllegalArgumentException("The emailTemplate argument cannot be null.  Cannot send notification email.");
        } else {
            this.charEncoding = charEncoding;
            this.emailTemplate = emailTemplate;
            this.mailServer = mailServer;
            this.contentType = contentType;
        }
    }

    /**
     * Send the e-mail
     *
     * @throws EmailException If there is an error sending the e-mail
     */
    public void send() throws EmailException {
        try {
            HtmlEmail mail = new HtmlEmail();
            if (getRecipients() != null) {
                String recipient;
                for (Iterator iter = getRecipients().iterator(); iter.hasNext(); mail
                    .addTo(recipient))
                    recipient = (String) iter.next();

            }
            if (getCarbonCopyRecipients() != null) {
                String cc;
                for (Iterator iter = getCarbonCopyRecipients().iterator(); iter.hasNext(); mail
                    .addHeader("Cc", cc))
                    cc = (String) iter.next();

            }
            if (getBlindCarbonCopyRecipients() != null) {
                String bcc;
                for (Iterator iter = getBlindCarbonCopyRecipients().iterator(); iter.hasNext(); mail
                    .addHeader("Bcc", bcc))
                    bcc = (String) iter.next();

            }
            mail.setFrom(getSender());
            mail.setSubject(getSubject());
            mail.setHtmlMsg(getMessage());
            mail.setHostName(mailServer);
            mail.send();
            _logger.info((new StringBuffer()).append("Email sent to ").append(getRecipients().toString()).toString());
        } catch (Exception e) {
            throw new EmailException("Could not send the email", e);
        }
    }

    /**
     * Retrieve the sender
     *
     * @return Sender
     */
    protected abstract String getSender();

    /**
     * Retrieve the list of recipients
     *
     * @return Recipients
     */
    protected abstract List getRecipients();

    /**
     * Retrieve the e-mail message content
     *
     * @return E-mail message
     * @throws Exception If there is an error getting the content
     */
    protected abstract String getMessage() throws Exception;

    /**
     * Retrieve the subject
     *
     * @return Subject
     */
    protected abstract String getSubject();

    /**
     * Retrieve the list of CC recipients
     *
     * @return List of CC recipients
     */
    protected abstract List getCarbonCopyRecipients();

    /**
     * Retrieve the list of BCC recipients
     *
     * @return List of BCC recipients
     */
    protected abstract List getBlindCarbonCopyRecipients();

    /**
     * Encode regex characters in a string
     *
     * @param string Input
     * @return Encoded regex characters
     */
    protected String encodeRegexCharacters(String string) {
        return string.replaceAll("\\$", "&#36;");
    }

    /**
     * Retrieve the e-mail template
     *
     * @return E-mail template
     */
    protected URL getEmailTemplate() {
        return emailTemplate;
    }
}
