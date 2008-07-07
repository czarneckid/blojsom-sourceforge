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

import org.apache.commons.mail.Email;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.web.context.ServletContextAware;

import javax.mail.MessagingException;
import javax.servlet.ServletContext;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public abstract class AbstractVelocityEmailNotification extends AbstractNotification implements ServletContextAware {

    private VelocityEngine engine;

    private VelocityContext context;

    private String velocityPropsPath;

    private static final Logger logger = Logger.getLogger(AbstractVelocityEmailNotification.class);

    /**
     * Construct a new abstract velocity e-mail notification
     *
     * @param email         E-mail message
     * @param emailTemplate E-mail template
     * @throws MessagingException If there is an error constructing the e-mail notification
     */
    public AbstractVelocityEmailNotification(Email email, URL emailTemplate) throws MessagingException {
        super(emailTemplate, "UTF-8", email.getHostName(), "text/html");
        engine = new VelocityEngine();
        try {
            engine.init();
        } catch (Exception e) {
            throw new MessagingException("Could not initialize the templating engine; cannot send the email.", e);
        }
        context = new VelocityContext();
    }

    /**
     * Construct a new abstract velocity e-mail notification
     *
     * @param emailTemplate E-mail template
     * @param charEncoding  Character encoding
     * @param mailServer    Mail server
     * @param contentType   Content type
     * @throws Exception If there is an error constructing the e-mail notification
     */
    public AbstractVelocityEmailNotification(URL emailTemplate, String charEncoding, String mailServer, String contentType) throws Exception {
        super(emailTemplate, charEncoding, mailServer, contentType);
        engine = new VelocityEngine();
        engine.init();
        context = new VelocityContext();
    }

    /**
     * Construct a new abstract velocity e-mail notification
     *
     * @param emailTemplate E-mail template
     * @param charEncoding  Character encoding
     * @param mailServer    Mail server
     * @param contentType   Content type
     * @param velocityProps Velocity properties
     * @throws Exception If there is an error constructing the e-mail notification
     */
    public AbstractVelocityEmailNotification(URL emailTemplate, String charEncoding, String mailServer, String contentType, Properties velocityProps) throws Exception {
        super(emailTemplate, charEncoding, mailServer, contentType);
        engine = new VelocityEngine();
        engine.init(velocityProps);
        context = new VelocityContext();
    }

    /**
     * Construct a new abstract velocity e-mail notification
     *
     * @param emailTemplate     E-mail template
     * @param charEncoding      Character encoding
     * @param mailServer        Mail server
     * @param contentType       Content type
     * @param velocityPropsPath Velocity properties path
     * @throws Exception If there is an error constructing the e-mail notification
     */
    public AbstractVelocityEmailNotification(URL emailTemplate, String charEncoding, String mailServer, String contentType, String velocityPropsPath) throws Exception {
        super(emailTemplate, charEncoding, mailServer, contentType);
        engine = new VelocityEngine();
        context = new VelocityContext();
        this.velocityPropsPath = velocityPropsPath;
    }

    /**
     * Get the e-mail message content evaluated
     *
     * @return E-mail message content
     * @throws Exception If there is an error evaluating the template
     */
    protected String getMessage() throws Exception {
        StringWriter writer = new StringWriter();
        Reader template = new InputStreamReader(getEmailTemplate().openStream());
        try {
            engine.evaluate(context, writer, "VelocityEmail", template);
            return writer.toString();
        } finally {
            if (template != null) {
                template.close();
            }
        }
    }

    /**
     * Put a name and value in the context
     *
     * @param name  Name
     * @param value Value
     */
    public void put(String name, Object value) {
        context.put(name, value);
    }

    /**
     * Put a map of name/value pairs in the context
     *
     * @param values Map of name/value pairs
     */
    public void putAll(Map values) {
        java.util.Map.Entry entry;
        for (Iterator itr = values.entrySet().iterator(); itr.hasNext(); context.put(entry.getKey()
            .toString(), entry.getValue()))
            entry = (java.util.Map.Entry) itr.next();

    }

    /**
     * Get a value from the context
     *
     * @param key Key
     * @return Value for the given key
     */
    public Object get(String key) {
        return context.get(key);
    }

    /**
     * Set the servlet context
     *
     * @param servletContext {@link ServletContext}
     */
    public void setServletContext(ServletContext servletContext) {
        if (velocityPropsPath != null) {
            Properties velociProps = new Properties();
            java.io.InputStream is = servletContext.getResourceAsStream(velocityPropsPath);
            try {
                if (is == null)
                    throw new IllegalStateException((new StringBuffer()).append("The Velocity properties file ").append(velocityPropsPath).append(" does not exist").toString());
                velociProps.load(is);
                engine.init(velociProps);
            } catch (Exception e) {
                IllegalStateException ise = new IllegalStateException((new StringBuffer()).append("Unable to initialize Velocity: ").append(e.getMessage()).toString());
                ise.initCause(e);
                throw ise;
            }
        }
    }

}
