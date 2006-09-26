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
package org.blojsom.plugin.moblog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.plugin.PluginException;
import org.blojsom.plugin.admin.event.EntryAddedEvent;
import org.blojsom.plugin.email.EmailConstants;
import org.blojsom.plugin.email.SimpleAuthenticator;
import org.blojsom.plugin.velocity.StandaloneVelocityPlugin;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomMetaDataConstants;
import org.blojsom.util.BlojsomUtils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.ConnectException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Moblog Plugin
 *
 * @author David Czarnecki
 * @author Mark Lussier
 * @version $Id: MoblogPlugin.java,v 1.5 2006-09-26 02:55:20 czarneckid Exp $
 * @since blojsom 3.0
 */
public class MoblogPlugin extends StandaloneVelocityPlugin {

    private Log _logger = LogFactory.getLog(MoblogPlugin.class);

    private static final String MOBLOG_ENTRY_TEMPLATE = "org/blojsom/plugin/moblog/moblog-plugin-template.vm";

    private static final String MOBLOG_SUBJECT = "MOBLOG_SUBJECT";
    private static final String MOBLOG_BODY_TEXT = "MOBLOG_BODY_TEXT";
    private static final String MOBLOG_IMAGES = "MOBLOG_IMAGES";
    private static final String MOBLOG_ATTACHMENTS = "MOBLOG_ATTACHMENTS";
    private static final String MOBLOG_ATTACHMENT = "MOBLOG_ATTACHMENT";
    private static final String MOBLOG_ATTACHMENT_URL = "MOBLOG_ATTACHMENT_URL";
    private static final String MOBLOG_IMAGE = "MOBLOG_IMAGE";
    private static final String MOBLOG_IMAGE_URL = "MOBLOG_IMAGE_URL";

    /**
     * Multipart/alternative mime-type
     */
    private static final String MULTIPART_ALTERNATIVE_MIME_TYPE = "multipart/alternative";

    /**
     * Text/html mime-type
     */
    private static final String TEXT_HTML_MIME_TYPE = "text/html";

    /**
     * Default mime-types for text
     */
    public static final String DEFAULT_TEXT_MIME_TYPES = "text/plain, text/html";

    /**
     * Default mime-types for images
     */
    public static final String DEFAULT_IMAGE_MIME_TYPES = "image/jpg, image/jpeg, image/gif, image/png";

    /**
     * Multipart mime-type
     */
    private static final String MULTIPART_TYPE = "multipart/*";

    /**
     * Default store
     */
    private static final String DEFAULT_MESSAGE_STORE = "pop3";

    /**
     * Default poll time (10 minutes)
     */
    private static final int DEFAULT_POLL_TIME = 720;

    /**
     * Moblog confifguration parameter for web.xml
     */
    public static final String PLUGIN_MOBLOG_CONFIGURATION_IP = "plugin-moblog";

    /**
     * Moblog configuration parameter for mailbox polling time (5 minutes)
     */
    public static final String PLUGIN_MOBLOG_POLL_TIME = "plugin-moblog-poll-time";

    /**
     * Moblog configuration parameter for message store provider
     */
    public static final String PLUGIN_MOBLOG_STORE_PROVIDER = "plugin-moblog-store-provider";

    /**
     * Default moblog authorization properties file which lists valid e-mail addresses who can moblog entries
     */
    public static final String DEFAULT_MOBLOG_AUTHORIZATION_FILE = "moblog-authorization.properties";

    /**
     * Configuration property for moblog authorization properties file to use
     */
    public static final String PROPERTY_AUTHORIZATION = "moblog-authorization";

    /**
     * Configuration property for mailhost
     */
    public static final String PROPERTY_HOSTNAME = "moblog-hostname";

    /**
     * Configuration property for mailbox user ID
     */
    public static final String PROPERTY_USERID = "moblog-userid";

    /**
     * Configuration property for mailbox user password
     */
    public static final String PROPERTY_PASSWORD = "moblog-password";

    /**
     * Configuration property for moblog category
     */
    public static final String PROPERTY_CATEGORY = "moblog-category";

    /**
     * Configuration property for whether or not moblog is enabled for this blog
     */
    public static final String PROPERTY_ENABLED = "moblog-enabled";

    /**
     * Configuration property for the secret word that must be present at the beginning of the subject
     */
    public static final String PLUGIN_MOBLOG_SECRET_WORD = "moblog-secret-word";

    /**
     * Configuration property for image mime-types
     */
    public static final String PLUGIN_MOBLOG_IMAGE_MIME_TYPES = "moblog-image-mime-types";

    /**
     * Configuration property for attachment mime-types
     */
    public static final String PLUGIN_MOBLOG_ATTACHMENT_MIME_TYPES = "moblog-attachment-mime-types";

    /**
     * Configuration property for text mime-types
     */
    public static final String PLUGIN_MOBLOG_TEXT_MIME_TYPES = "moblog-text-mime-types";

    /**
     * Configuration property for regular expression to ignore a certain portion of text
     */
    public static final String PLUGIN_MOBLOG_IGNORE_EXPRESSION = "moblog-ignore-expression";

    public static final String PLUGIN_MOBLOG_AUTHORIZED_ADDRESSES = "moblog-authorized-addresses";

    private int _pollTime;

    private Session _storeSession;
    private boolean _finished = false;
    private MailboxChecker _checker;
    private String _storeProvider;

    private EventBroadcaster _eventBroadcaster;
    private Fetcher _fetcher;

    /**
     * Set the {@link Fetcher}
     *
     * @param fetcher {@link Fetcher}
     */
    public void setFetcher(Fetcher fetcher) {
        _fetcher = fetcher;
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
     * Initialize this plugin. This method only called when the plugin is
     * instantiated.
     *
     * @throws PluginException If there is an error initializing the plugin
     */
    public void init() throws PluginException {
        super.init();

        String moblogPollTime = _servletConfig.getInitParameter(PLUGIN_MOBLOG_POLL_TIME);
        if (BlojsomUtils.checkNullOrBlank(moblogPollTime)) {
            _pollTime = DEFAULT_POLL_TIME;
        } else {
            try {
                _pollTime = Integer.parseInt(moblogPollTime);
            } catch (NumberFormatException e) {
                if (_logger.isErrorEnabled()) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error("Invalid time specified for: " + PLUGIN_MOBLOG_POLL_TIME);
                    }
                }
                _pollTime = DEFAULT_POLL_TIME;
            }
        }

        _storeProvider = _servletConfig.getInitParameter(PLUGIN_MOBLOG_STORE_PROVIDER);
        if (BlojsomUtils.checkNullOrBlank(_storeProvider)) {
            _storeProvider = DEFAULT_MESSAGE_STORE;
        }

        _checker = new MailboxChecker();
        _checker.setDaemon(true);

        String hostname = _servletConfig.getInitParameter(EmailConstants.SMTPSERVER_IP);
        if (hostname != null) {
            if (hostname.startsWith("java:comp/env")) {
                try {
                    Context context = new InitialContext();
                    _storeSession = (Session) context.lookup(hostname);
                } catch (NamingException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }

                    throw new PluginException(e);
                }
            } else {
                String username = _servletConfig.getInitParameter(EmailConstants.SMTPSERVER_USERNAME_IP);
                String password = _servletConfig.getInitParameter(EmailConstants.SMTPSERVER_PASSWORD_IP);

                Properties props = new Properties();
                props.put(EmailConstants.SESSION_NAME, hostname);
                if (BlojsomUtils.checkNullOrBlank(username) || BlojsomUtils.checkNullOrBlank(password)) {
                    _storeSession = Session.getInstance(props, null);
                } else {
                    _storeSession = Session.getInstance(props, new SimpleAuthenticator(username, password));
                }
            }
        }

        _checker.start();
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param blog                {@link org.blojsom.blog.Blog} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws PluginException If there is an error processing the blog entries
     */
    public Entry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, Map context, Entry[] entries) throws PluginException {
        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws PluginException If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws PluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws PluginException If there is an error in finalizing this plugin
     */
    public void destroy() throws PluginException {
        _finished = true;
    }

    /**
     * Thread that polls the mailboxes
     */
    private class MailboxChecker extends Thread {

        /**
         * Allocates a new <code>Thread</code> object. This constructor has
         * the same effect as <code>Thread(null, null,</code>
         * <i>gname</i><code>)</code>, where <b><i>gname</i></b> is
         * a newly generated name. Automatically generated names are of the
         * form <code>"Thread-"+</code><i>n</i>, where <i>n</i> is an integer.
         *
         * @see Thread#Thread(ThreadGroup,
         *      Runnable, String)
         */
        public MailboxChecker() {
            super();
        }

        /**
         * Perform the actual work of checking the POP3 mailbox configured for the blog user.
         *
         * @param mailbox Mailbox to be processed
         */
        private void processMailbox(Mailbox mailbox) {
            Folder folder = null;
            Store store = null;
            String subject;

            try {
                store = _storeSession.getStore(_storeProvider);
                store.connect(mailbox.getHostName(), mailbox.getUserId(), mailbox.getPassword());

                // -- Try to get hold of the default folder --
                folder = store.getDefaultFolder();
                if (folder == null) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error("Default folder is null.");
                    }
                    _finished = true;
                }

                // -- ...and its INBOX --
                folder = folder.getFolder(mailbox.getFolder());
                if (folder == null) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error("No POP3 folder called " + mailbox.getFolder());
                    }
                    _finished = true;
                }

                // -- Open the folder for read only --
                folder.open(Folder.READ_WRITE);

                // -- Get the message wrappers and process them --
                Message[] msgs = folder.getMessages();

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Found [" + msgs.length + "] messages");
                }

                for (int msgNum = 0; msgNum < msgs.length; msgNum++) {
                    String from = ((InternetAddress) msgs[msgNum].getFrom()[0]).getAddress();
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Processing message: " + msgNum);
                    }

                    if (!checkSender(mailbox, from)) {
                        if (_logger.isDebugEnabled()) {
                            _logger.debug("Unauthorized sender address: " + from);
                            _logger.debug("Deleting message: " + msgNum);
                        }

                        msgs[msgNum].setFlag(Flags.Flag.DELETED, true);
                    } else {
                        Message email = msgs[msgNum];
                        subject = email.getSubject();

                        StringBuffer description = new StringBuffer();
                        Part messagePart;
                        messagePart = email;
                        Pattern pattern = null;
                        List moblogImages = new ArrayList();
                        List moblogAttachments = new ArrayList();
                        Map moblogContext = new HashMap();

                        if (mailbox.getIgnoreExpression() != null) {
                            pattern = Pattern.compile(mailbox.getIgnoreExpression(), Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNICODE_CASE | Pattern.DOTALL);
                        }

                        if (subject == null) {
                            subject = "";
                        } else {
                            subject = subject.trim();
                        }

                        String secretWord = mailbox.getSecretWord();
                        if (secretWord != null) {
                            if (!subject.startsWith(secretWord)) {
                                if (_logger.isErrorEnabled()) {
                                    _logger.error("Message does not begin with secret word for user id: " + mailbox.getUserId());
                                }
                                msgs[msgNum].setFlag(Flags.Flag.DELETED, true);

                                continue;
                            } else {
                                subject = subject.substring(secretWord.length());
                            }
                        }

                        if (email.isMimeType(MULTIPART_TYPE)) {
                            // Check for multipart/alternative
                            String overallType = email.getContentType();
                            overallType = sanitizeContentType(overallType);

                            boolean isMultipartAlternative = false;
                            if (MULTIPART_ALTERNATIVE_MIME_TYPE.equals(overallType)) {
                                isMultipartAlternative = true;
                            }

                            Multipart mp = (Multipart) messagePart.getContent();
                            int count = mp.getCount();

                            for (int i = 0; i < count; i++) {
                                BodyPart bp = mp.getBodyPart(i);
                                String type = bp.getContentType();
                                if (type != null) {
                                    type = sanitizeContentType(type);
                                    Map imageMimeTypes = mailbox.getImageMimeTypes();
                                    Map attachmentMimeTypes = mailbox.getAttachmentMimeTypes();
                                    Map textMimeTypes = mailbox.getTextMimeTypes();

                                    // Check for multipart alternative as part of a larger e-mail block
                                    if (MULTIPART_ALTERNATIVE_MIME_TYPE.equals(type)) {
                                        Object mimeMultipartContent = bp.getContent();
                                        if (mimeMultipartContent instanceof MimeMultipart) {
                                            MimeMultipart mimeMultipart = (MimeMultipart) mimeMultipartContent;
                                            int mimeMultipartCount = mimeMultipart.getCount();
                                            for (int j = 0; j < mimeMultipartCount; j++) {
                                                BodyPart mimeMultipartBodyPart = mimeMultipart.getBodyPart(j);
                                                String mmpbpType = mimeMultipartBodyPart.getContentType();
                                                if (mmpbpType != null) {
                                                    mmpbpType = sanitizeContentType(mmpbpType);
                                                    if (TEXT_HTML_MIME_TYPE.equals(mmpbpType)) {
                                                        if (_logger.isDebugEnabled()) {
                                                            _logger.debug("Using HTML part of multipart/alternative: " + type);
                                                        }
                                                        InputStream is = bp.getInputStream();

                                                        BufferedReader reader = new BufferedReader(new InputStreamReader(is, BlojsomConstants.UTF8));
                                                        String thisLine;

                                                        while ((thisLine = reader.readLine()) != null) {
                                                            description.append(thisLine);
                                                            description.append(BlojsomConstants.LINE_SEPARATOR);
                                                        }

                                                        reader.close();
                                                        if (pattern != null) {
                                                            Matcher matcher = pattern.matcher(description);
                                                            if (!matcher.find() && !matcher.matches()) {
                                                                //entry.append(description);
                                                                moblogContext.put(MOBLOG_BODY_TEXT, description.toString());
                                                            }
                                                        } else {
                                                            //entry.append(description);
                                                            moblogContext.put(MOBLOG_BODY_TEXT, description.toString());
                                                        }
                                                    } else {
                                                        if (_logger.isDebugEnabled()) {
                                                            _logger.debug("Skipping non-HTML part of multipart/alternative block");
                                                        }
                                                    }
                                                } else {
                                                    if (_logger.isInfoEnabled()) {
                                                        _logger.info("Unknown mimetype for multipart/alternative block");
                                                    }
                                                }
                                            }
                                        } else {
                                            if (_logger.isDebugEnabled()) {
                                                _logger.debug("Multipart alternative block not instance of MimeMultipart");
                                            }
                                        }
                                    } else {
                                        if (imageMimeTypes.containsKey(type)) {
                                            if (_logger.isDebugEnabled()) {
                                                _logger.debug("Creating image of type: " + type);
                                            }
                                            String outputFilename = BlojsomUtils.digestString(bp.getFileName() + "-" + new Date().getTime());
                                            String extension = BlojsomUtils.getFileExtension(bp.getFileName());
                                            if (BlojsomUtils.checkNullOrBlank(extension)) {
                                                extension = "";
                                            }

                                            if (_logger.isDebugEnabled()) {
                                                _logger.debug("Writing to: " + mailbox.getOutputDirectory() + File.separator + outputFilename + "." + extension);
                                            }
                                            MoblogPluginUtils.saveFile(mailbox.getOutputDirectory() + File.separator + outputFilename, "." + extension, bp.getInputStream());

                                            String baseurl = mailbox.getBlogBaseURL();
                                            Map moblogImageInformation = new HashMap();
                                            moblogImageInformation.put(MOBLOG_IMAGE, outputFilename + "." + extension);
                                            moblogImageInformation.put(MOBLOG_IMAGE_URL, baseurl + mailbox.getUrlPrefix() + outputFilename + "." + extension);
                                            moblogImages.add(moblogImageInformation);
                                        } else if (attachmentMimeTypes.containsKey(type)) {
                                            if (_logger.isDebugEnabled()) {
                                                _logger.debug("Creating attachment of type: " + type);
                                            }
                                            String outputFilename = BlojsomUtils.digestString(bp.getFileName() + "-" + new Date().getTime());
                                            String extension = BlojsomUtils.getFileExtension(bp.getFileName());
                                            if (BlojsomUtils.checkNullOrBlank(extension)) {
                                                extension = "";
                                            }

                                            if (_logger.isDebugEnabled()) {
                                                _logger.debug("Writing to: " + mailbox.getOutputDirectory() + File.separator + outputFilename + "." + extension);
                                            }
                                            MoblogPluginUtils.saveFile(mailbox.getOutputDirectory() + File.separator + outputFilename, "." + extension, bp.getInputStream());

                                            String baseurl = mailbox.getBlogBaseURL();
                                            Map moblogAttachmentInformation = new HashMap();
                                            moblogAttachmentInformation.put(MOBLOG_ATTACHMENT, bp.getFileName());
                                            moblogAttachmentInformation.put(MOBLOG_ATTACHMENT_URL, baseurl + mailbox.getUrlPrefix() + outputFilename + "." + extension);
                                            moblogAttachments.add(moblogAttachmentInformation);
                                        } else if (textMimeTypes.containsKey(type)) {
                                            if ((isMultipartAlternative && (TEXT_HTML_MIME_TYPE.equals(type))) || !isMultipartAlternative)
                                            {
                                                if (_logger.isDebugEnabled()) {
                                                    _logger.debug("Using text part of type: " + type);
                                                }
                                                InputStream is = bp.getInputStream();

                                                BufferedReader reader = new BufferedReader(new InputStreamReader(is, BlojsomConstants.UTF8));
                                                String thisLine;

                                                while ((thisLine = reader.readLine()) != null) {
                                                    description.append(thisLine);
                                                    description.append(BlojsomConstants.LINE_SEPARATOR);
                                                }

                                                reader.close();
                                                if (pattern != null) {
                                                    Matcher matcher = pattern.matcher(description);
                                                    if (!matcher.find() && !matcher.matches()) {
                                                        moblogContext.put(MOBLOG_BODY_TEXT, description.toString());
                                                    }
                                                } else {
                                                    moblogContext.put(MOBLOG_BODY_TEXT, description.toString());
                                                }
                                            }
                                        } else {
                                            if (_logger.isInfoEnabled()) {
                                                _logger.info("Unknown mimetype for multipart: " + type);
                                            }
                                        }
                                    }
                                } else {
                                    if (_logger.isDebugEnabled()) {
                                        _logger.debug("Body part has no defined mime type. Skipping.");
                                    }
                                }
                            }
                        } else {
                            // Check for the message being one of the defined text mime types if it's not a multipart
                            Map textMimeTypes = mailbox.getTextMimeTypes();
                            String mimeType = email.getContentType();
                            if (mimeType != null) {
                                mimeType = sanitizeContentType(mimeType);
                            }

                            if ((mimeType != null) && (textMimeTypes.containsKey(mimeType))) {
                                InputStream is = email.getInputStream();

                                BufferedReader reader = new BufferedReader(new InputStreamReader(is, BlojsomConstants.UTF8));
                                String thisLine;

                                while ((thisLine = reader.readLine()) != null) {
                                    description.append(thisLine);
                                    description.append(BlojsomConstants.LINE_SEPARATOR);
                                }

                                reader.close();
                                if (pattern != null) {
                                    Matcher matcher = pattern.matcher(description);
                                    if (!matcher.find() && !matcher.matches()) {
                                        moblogContext.put(MOBLOG_BODY_TEXT, description.toString());
                                    }
                                } else {
                                    moblogContext.put(MOBLOG_BODY_TEXT, description.toString());
                                }
                            } else {
                                if (_logger.isInfoEnabled()) {
                                    _logger.info("Unknown mimetype: " + mimeType);
                                }
                            }
                        }

                        // Process subject to change category for moblog post
                        boolean categoryInSubject = false;
                        String categoryFromSubject = null;
                        if (subject.startsWith("[")) {
                            int startIndex = subject.indexOf("[");
                            if (startIndex != -1) {
                                int closingIndex = subject.indexOf("]", startIndex);
                                if (closingIndex != -1) {
                                    categoryFromSubject = subject.substring(startIndex + 1, closingIndex);
                                    subject = subject.substring(closingIndex + 1);
                                    categoryFromSubject = BlojsomUtils.normalize(categoryFromSubject);
                                    if (!categoryFromSubject.startsWith("/")) {
                                        categoryFromSubject = "/" + categoryFromSubject;
                                    }
                                    if (!categoryFromSubject.endsWith("/")) {
                                        categoryFromSubject += "/";
                                    }
                                    categoryInSubject = true;
                                    if (_logger.isInfoEnabled()) {
                                        _logger.info("Using category [" + categoryFromSubject + "] for entry: " + subject);
                                    }
                                }
                            }
                        }

                        String categoryID = categoryInSubject ? categoryFromSubject : mailbox.getCategoryId();

                        moblogContext.put(MOBLOG_SUBJECT, subject);
                        moblogContext.put(MOBLOG_IMAGES, moblogImages);
                        moblogContext.put(MOBLOG_ATTACHMENTS, moblogAttachments);

                        try {
                            Blog blog = _fetcher.loadBlog(mailbox.getBlogId());
                            String moblogText = mergeTemplate(MOBLOG_ENTRY_TEMPLATE, blog, moblogContext);

                            Entry entry;
                            entry = _fetcher.newEntry();

                            entry.setBlogCategoryId(Integer.valueOf(categoryID));
                            entry.setBlogId(mailbox.getId());
                            entry.setDate(new Date());
                            entry.setDescription(moblogText);
                            entry.setTitle(subject);
                            entry.setStatus(BlojsomMetaDataConstants.PUBLISHED_STATUS);

                            Map entryMetaData = new HashMap();
                            entryMetaData.put(BlojsomMetaDataConstants.BLOG_ENTRY_METADATA_AUTHOR_EXT, from);
                            entry.setMetaData(entryMetaData);

                            _fetcher.saveEntry(blog, entry);

                            msgs[msgNum].setFlag(Flags.Flag.DELETED, true);

                            _eventBroadcaster.broadcastEvent(new EntryAddedEvent(this, new Date(), entry, blog));
                        } catch (FetcherException e) {
                            if (_logger.isErrorEnabled()) {
                                _logger.error(e);
                            }
                        }
                    }
                }

                // Delete the messages
                try {
                    if (folder != null) {
                        folder.close(true);
                    }

                    if (store != null) {
                        store.close();
                    }
                } catch (MessagingException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                }
            } catch (ConnectException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            } catch (NoSuchProviderException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            } catch (MessagingException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            } catch (IOException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            } finally {
                try {
                    if (folder != null && folder.isOpen()) {
                        folder.close(true);
                    }

                    if (store != null) {
                        store.close();
                    }
                } catch (MessagingException e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }
                }
            }
        }

        /**
         * Process the moblog mailboxes for each user
         */
        public void run() {
            try {
                while (!_finished) {
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Moblog plugin waking up and looking for new messages");
                    }

                    String[] blogIDs = _fetcher.loadBlogIDs();
                    for (int i = 0; i < blogIDs.length; i++) {
                        String blogID = blogIDs[i];
                        Blog blog = _fetcher.loadBlog(blogID);

                        Mailbox mailbox = MoblogPluginUtils.readMailboxSettingsForBlog( _servletConfig, blog);
                        if (mailbox != null) {
                            if (mailbox.isEnabled()) {
                                if (_logger.isDebugEnabled()) {
                                    _logger.debug("Checking mailbox: " + mailbox.getUserId() + " for blog: " + mailbox.getBlogId());
                                }
                                processMailbox(mailbox);
                            }
                        }
                    }

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Moblog plugin off to take a nap");
                    }
                    sleep(_pollTime * 1000);
                }
            } catch (InterruptedException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            } catch (FetcherException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            }
        }

        /**
         * Check to see that the sender is an authorized user to moblog
         *
         * @param mailbox     Mailbox for user
         * @param fromAddress E-mail address of sender
         * @return <code>true</code> if the from address is specified as a valid poster to the moblog,
         *         <code>false</code> otherwise
         */
        private boolean checkSender(Mailbox mailbox, String fromAddress) {
            boolean result = false;
            Map authorizedAddresses = mailbox.getAuthorizedAddresses();

            if (authorizedAddresses.containsKey(fromAddress)) {
                result = true;
            }

            return result;
        }
    }

    /**
     * Return a content type up to the first ; character
     *
     * @param contentType Content type
     * @return Content type without any trailing information after a ;
     */
    protected String sanitizeContentType(String contentType) {
        int semicolonIndex = contentType.indexOf(";");
        if (semicolonIndex != -1) {
            contentType = contentType.substring(0, semicolonIndex);
        }

        return contentType.toLowerCase();
    }
}
