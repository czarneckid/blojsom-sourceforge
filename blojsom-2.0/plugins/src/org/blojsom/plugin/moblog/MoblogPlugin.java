/**
 * Copyright (c) 2003-2004, David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003-2004 by Mark Lussier
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the "David A. Czarnecki" and "blojsom" nor the names of
 * its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Products derived from this software may not be called "blojsom",
 * nor may "blojsom" appear in their name, without prior written permission of
 * David A. Czarnecki.
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
import org.blojsom.BlojsomException;
import org.blojsom.blog.*;
import org.blojsom.fetcher.BlojsomFetcher;
import org.blojsom.fetcher.BlojsomFetcherException;
import org.blojsom.plugin.BlojsomPlugin;
import org.blojsom.plugin.BlojsomPluginException;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomMetaDataConstants;
import org.blojsom.util.BlojsomUtils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.ConnectException;
import java.util.*;

/**
 * Moblog Plugin
 *
 * @author David Czarnecki
 * @author Mark Lussier
 * @version $Id: MoblogPlugin.java,v 1.2 2004-04-26 00:49:17 intabulas Exp $
 * @since blojsom 2.14
 */
public class MoblogPlugin implements BlojsomPlugin {

    /* */
    private static final String BLOG_MOBLOG_CONFIGURATION_IP = "plugin-moblog";
    /* */
    private static final String DEFAULT_MOBLOG_AUTHORIZATION_FILE = "moblog-authorization.properties";
    /* */
    private static final String PROPERTY_AUTHORIZATION = "moblog-authorization";
    /* */
    private static final String PROPERTY_HOSTNAME = "moblog-hostname";
    /* */
    private static final String PROPERTY_USERID = "moblog-userid";
    /* */
    private static final String PROPERTY_PASSWORD = "moblog-password";
    /* */
    private static final String PROPERTY_CATEGORY = "moblog-category";
    /* */
    private static final String PROPERTY_ENABLED = "moblog-enabled";


    private Log _logger = LogFactory.getLog(MoblogPlugin.class);

    private Map _authorizationMap;
    private String _urlPrefix = null;
    private static final int SLEEP_TIME = 100;

    private Session _popSession;
    private boolean _finished = false;
    private MailboxChecker _checker;

    private BlojsomFetcher _fetcher;


    /**
     * Configuires the list of valid Moblog posters for each users blog
     *
     * @param servletConfig        Servlet configuration information
     * @param blojsomConfiguration {@link org.blojsom.blog.BlojsomConfiguration} Information
     * @param user                 the User Id for this Authorzation List
     * @param authFile             the file that contains this users authorization List;
     */
    private void configureAuthorization(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration, String user, String authFile) {

        if (authFile != null) {
            String authorizationFile = blojsomConfiguration.getBaseConfigurationDirectory() + user + "/" + authFile;
            InputStream ais = servletConfig.getServletContext().getResourceAsStream(authorizationFile);
            if (ais == null) {
                _logger.info("No moblog-authorization configuration file found: " + authorizationFile);
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(ais));
                String thisLine;
                List authList = new ArrayList(5); // Seems like a safe #
                try {
                    while ((thisLine = reader.readLine()) != null) {
                        if (!thisLine.startsWith("#")) {
                            authList.add(thisLine);
                            //_logger.debug("Adding [" + thisLine + "] to the " + user + "moblog list");
                        }
                    }
                    ais.close();
                } catch (IOException e) {
                    _logger.error(e.getMessage(), e);
                }
                _authorizationMap.put(user, authList);
            }

        }

    }


    /**
     * Initialize this plugin. This method only called when the plugin is
     * instantiated.
     *
     * @param servletConfig        Servlet config object for the plugin to retrieve any
     *                             initialization parameters
     * @param blojsomConfiguration {@link org.blojsom.blog.BlojsomConfiguration}
     *                             information
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error
     *          initializing the plugin
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration
            blojsomConfiguration) throws BlojsomPluginException {

        String fetcherClassName = blojsomConfiguration.getFetcherClass();
        try {
            Class fetcherClass = Class.forName(fetcherClassName);
            _fetcher = (BlojsomFetcher) fetcherClass.newInstance();
            _fetcher.init(servletConfig, blojsomConfiguration);
            _logger.info("Added blojsom fetcher: " + fetcherClassName);
        } catch (ClassNotFoundException e) {
            _logger.error(e);
            throw new BlojsomPluginException(e);
        } catch (InstantiationException e) {
            _logger.error(e);
            throw new BlojsomPluginException(e);
        } catch (IllegalAccessException e) {
            _logger.error(e);
            throw new BlojsomPluginException(e);
        } catch (BlojsomFetcherException e) {
            _logger.error(e);
            throw new BlojsomPluginException(e);
        }


        String mobogConfiguration = servletConfig.getInitParameter(BLOG_MOBLOG_CONFIGURATION_IP);

        _checker = new MailboxChecker();

        _urlPrefix = blojsomConfiguration.getResourceDirectory();

        _authorizationMap = new HashMap(blojsomConfiguration.getBlogUsers().size());

        Iterator userIterator = blojsomConfiguration.getBlogUsers().keySet().iterator();
        while (userIterator.hasNext()) {
            String user = (String) userIterator.next();
            BlogUser blogUser = (BlogUser) blojsomConfiguration.getBlogUsers().get(user);


            Properties mobogProperties = new Properties();
            String configurationFile = blojsomConfiguration.getBaseConfigurationDirectory() + user + "/" + mobogConfiguration;

            InputStream is = servletConfig.getServletContext().getResourceAsStream(configurationFile);
            if (is == null) {
                _logger.info("No moblog configuration file found: " + configurationFile);
            } else {
                try {
                    mobogProperties.load(is);

                    /**
                     * Configure each blogs authorized users
                     */
                    String authFile = mobogProperties.getProperty(PROPERTY_AUTHORIZATION, DEFAULT_MOBLOG_AUTHORIZATION_FILE);
                    configureAuthorization(servletConfig, blojsomConfiguration, user, authFile);


                    if (mobogProperties.size() > 0) {
                        Mailbox mailbox = new Mailbox();

                        mailbox.seBlogtUser(blogUser);

                        String hostname = mobogProperties.getProperty(PROPERTY_HOSTNAME);
                        if (hostname != null) {
                            mailbox.setHostName(hostname);
                        }
                        String userid = mobogProperties.getProperty(PROPERTY_USERID);
                        if (userid != null) {
                            mailbox.setUserId(userid);
                        }
                        String password = mobogProperties.getProperty(PROPERTY_PASSWORD);
                        if (password != null) {
                            mailbox.setPassword(password);
                        }


                        mailbox.setUrlPrefix(BlojsomUtils.removeTrailingSlash(_urlPrefix) + "/" + user + "/");
                        String resourceUrl = blojsomConfiguration.getQualifiedResourceDirectory();
                        mailbox.setOutputDirectory(resourceUrl + File.separator + user);

                        String blogCategoryName = mobogProperties.getProperty(PROPERTY_CATEGORY);
                        blogCategoryName = BlojsomUtils.normalize(blogCategoryName);
                        if (!blogCategoryName.endsWith("/")) {
                            blogCategoryName += "/";
                        }

                        mailbox.setCategoryName(blogCategoryName);
                        mailbox.setEntriesDirectory(blogUser.getBlog().getBlogURL() + BlojsomUtils.removeInitialSlash(blogCategoryName));


                        Boolean enabled = Boolean.valueOf(mobogProperties.getProperty(PROPERTY_ENABLED, "false"));
                        mailbox.setEnabled(enabled.booleanValue());


                        _checker.addMailbox(mailbox);
                    }

                    is.close();
                } catch (IOException e) {
                    _logger.error(e);
                    throw new BlojsomPluginException(e);
                }
            }
        }


        _popSession = Session.getDefaultInstance(System.getProperties(), null);


        _checker.start();
        _logger.debug("Started moblog plugin.");
    }


    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param user                {@link org.blojsom.blog.BlogUser} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws BlojsomPluginException If there is an error processing the blog
     *                                entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse, BlogUser user, Map context,
                               BlogEntry[] entries) throws BlojsomPluginException {
        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws BlojsomPluginException If there is an error performing cleanup
     *                                for this plugin
     */
    public void cleanup() throws BlojsomPluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws BlojsomPluginException If there is an error in finalizing this
     *                                plugin
     */
    public void destroy() throws BlojsomPluginException {
        _finished = true;
    }


    /**
     * Thread that polls the mail box's
     */
    private class MailboxChecker extends Thread {

        private List _pollingQueue;

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
            _pollingQueue = new ArrayList(2);
        }

        public void addMailbox(Mailbox mailbox) {
            if (!_pollingQueue.contains(mailbox)) {
                _pollingQueue.add(mailbox);
            }

        }


        private void processMailbox(Mailbox mailbox) {
            Folder folder = null;
            Store store = null;
            String subject = null;
            try {
                store = _popSession.getStore("pop3");
                store.connect(mailbox.getHostName(), mailbox.getUserId(), mailbox.getPassword());

                // -- Try to get hold of the default folder --
                folder = store.getDefaultFolder();
                if (folder == null) {
                    _logger.error("Default folder is null.");
                    _finished = true;
                }

                // -- ...and its INBOX --
                folder = folder.getFolder(mailbox.getFolder());
                if (folder == null) {
                    _logger.error("No POP3 folder called " + mailbox.getFolder());
                    _finished = true;
                }

                // -- Open the folder for read only --
                folder.open(Folder.READ_WRITE);

                // -- Get the message wrappers and process them --
                Message[] msgs = folder.getMessages();

                _logger.debug("Found [" + msgs.length + "] messages");

                for (int msgNum = 0; msgNum < msgs.length; msgNum++) {
                    String from = ((InternetAddress)
                            msgs[msgNum].getFrom()[0]).getAddress();
                    _logger.debug("Processing Message #" + msgNum + "\n");
                    if (!checkSender(mailbox.getBlogUser().getId(), from)) {
                        _logger.debug("Unauthorized sender address: " + from);
                        _logger.debug("Deleting message " + msgNum);
                        msgs[msgNum].setFlag(Flags.Flag.DELETED, true);
                    } else {
                        Message email = msgs[msgNum];
                        subject = email.getSubject();

                        StringBuffer entry = new StringBuffer();
                        StringBuffer description = new StringBuffer();
                        Part messagePart = email;
                        if (subject == null) {
                            subject = "";
                        }
                        //entry.append(subject).append("\n\n");
                        if (email.isMimeType("multipart/*")) {
                            Multipart mp = (Multipart)
                                    messagePart.getContent();
                            int count = mp.getCount();
                            for (int i = 0; i < count; i++) {
                                BodyPart bp = mp.getBodyPart(i);
                                /* Handle JPEG's */
                                if (bp.isMimeType("image/jpeg") || bp.isMimeType("image/jpg")) {
                                    _logger.debug("Creating Image");
                                    InputStream is = bp.getInputStream();
                                    byte[] jpgFile = new
                                            byte[is.available()];
                                    is.read(jpgFile, 0, is.available());
                                    is.close();
                                    String outputFilename =
                                            BlojsomUtils.digestString(bp.getFileName() + "-" + new Date().getTime());
                                    _logger.debug("Writing to: " + mailbox.getOutputDirectory() + File.separator +
                                            outputFilename + ".jpg");
                                    FileOutputStream fos = new
                                            FileOutputStream(new File(mailbox.getOutputDirectory() + File.separator + outputFilename + ".jpg"));
                                    fos.write(jpgFile);
                                    fos.close();
                                    String baseurl = mailbox.getBlogUser().getBlog().getBlogBaseURL();
                                    entry.append("<p /><img src =\"").append(baseurl).append(mailbox.getUrlPrefix()).append(outputFilename + ".jpg").append("\" border=\"0\"/ > ");
                                    /* Handle PNG's */
                                } else if (bp.isMimeType("image/png")) {
                                    _logger.debug("Creating Image");
                                    InputStream is = bp.getInputStream();
                                    byte[] pngFile = new
                                            byte[is.available()];
                                    is.read(pngFile, 0, is.available());
                                    is.close();
                                    String outputFilename =
                                            BlojsomUtils.digestString(bp.getFileName() + "-" + new Date().getTime());
                                    _logger.debug("Writing to: " + mailbox.getOutputDirectory() + File.separator +
                                            outputFilename + ".png");
                                    FileOutputStream fos = new
                                            FileOutputStream(new File(mailbox.getOutputDirectory() + File.separator + outputFilename + ".png"));
                                    fos.write(pngFile);
                                    fos.close();

                                    String baseurl = mailbox.getBlogUser().getBlog().getBlogBaseURL();

                                    entry.append("<p /><img src =\"").append(baseurl).append(mailbox.getUrlPrefix()).append(outputFilename + ".png").append("\" border=\"0\"/ > ");
                                } else if (bp.isMimeType("image/gif")) {
                                    _logger.debug("Creating Image");
                                    InputStream is = bp.getInputStream();
                                    byte[] gifFile = new
                                            byte[is.available()];
                                    is.read(gifFile, 0, is.available());
                                    is.close();
                                    String outputFilename =
                                            BlojsomUtils.digestString(bp.getFileName() + "-" + new Date().getTime());
                                    _logger.debug("Writing to: " + mailbox.getOutputDirectory() + File.separator +
                                            outputFilename + ".gif");
                                    FileOutputStream fos = new
                                            FileOutputStream(new File(mailbox.getOutputDirectory() + File.separator + outputFilename + ".gif"));
                                    fos.write(gifFile);
                                    fos.close();

                                    String baseurl = mailbox.getBlogUser().getBlog().getBlogBaseURL();

                                    entry.append("<p /><img src =\"").append(baseurl).append(mailbox.getUrlPrefix()).append(outputFilename + ".gif").append("\" border=\"0\"/ > ");
                                } else if (bp.isMimeType("text/plain")) {
                                    InputStream is = bp.getInputStream();

                                    BufferedReader reader = new
                                            BufferedReader(new InputStreamReader(is));
                                    String thisLine;

                                    while ((thisLine = reader.readLine()) !=
                                            null) {
                                        description.append(thisLine);

                                        description.append(BlojsomConstants.LINE_SEPARATOR);
                                    }

                                    reader.close();
                                    entry.append(description);
                                } else {
                                    _logger.debug("Unknown mimetype " + bp.getContentType());
                                }
                            }
                        }


                        String filename = BlojsomUtils.digestString(entry.toString());
                        filename += ".txt";


                        BlogCategory category;
                        category = _fetcher.newBlogCategory();
                        category.setCategory(mailbox.getCategoryName());
                        category.setCategoryURL(mailbox.getEntriesDirectory());


                        BlogUser blogUser = mailbox.getBlogUser();

                        File blogCategory = getBlogCategoryDirectory(blogUser.getBlog(), mailbox.getCategoryName());


                        if (blogCategory.exists() && blogCategory.isDirectory()) {

                            String outputfile = blogCategory.getAbsolutePath() + File.separator + filename;

                            try {
                                File sourceFile = new File(outputfile);
                                BlogEntry blogEntry;
                                blogEntry = _fetcher.newBlogEntry();

                                Map attributeMap = new HashMap();
                                Map blogEntryMetaData = new HashMap();
                                attributeMap.put(BlojsomMetaDataConstants.SOURCE_ATTRIBUTE, sourceFile);
                                blogEntry.setAttributes(attributeMap);


                                blogEntry.setTitle(subject);
                                blogEntry.setCategory(mailbox.getCategoryName());
                                blogEntry.setDescription(entry.toString());
                                blogEntryMetaData.put(BlojsomMetaDataConstants.BLOG_ENTRY_METADATA_TIMESTAMP, new Long(new Date().getTime()).toString());
                                blogEntryMetaData.put(BlojsomMetaDataConstants.BLOG_ENTRY_METADATA_AUTHOR_EXT, from);
                                blogEntry.setMetaData(blogEntryMetaData);
                                blogEntry.save(mailbox.getBlogUser());

                                msgs[msgNum].setFlag(Flags.Flag.DELETED, true);
                            } catch (BlojsomException e) {
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
                    _logger.error(e);
                }

            } catch (ConnectException e) {
                _logger.error(e);
            } catch (NoSuchProviderException e) {
                _logger.error(e);
            } catch (MessagingException e) {
                _logger.error(e);
            } catch (IOException e) {
                _logger.error(e);
            } finally {
                try {
                    if (folder != null && folder.isOpen()) {
                        folder.close(true);
                    }

                    if (store != null) {
                        store.close();
                    }
                } catch (MessagingException e) {
                    _logger.error(e);
                }
            }

        }

        /**
         *
         */
        public void run() {
            try {
                while (!_finished) {
                    _logger.debug("Moblog Plugin waking up and looking for new messages");


                    int cnt = _pollingQueue.size();
                    for (int x = 0; x < cnt; x++) {
                        Mailbox mailbox = (Mailbox) _pollingQueue.get(x);
                        if (mailbox.isEnabled()) {
                            _logger.debug("  Checking " + mailbox.getHostName());
                            processMailbox(mailbox);
                        }
                    }

                    _logger.debug("Moblog Plugin off to take a nap");
                    sleep(SLEEP_TIME * 1000);
                }
            } catch (InterruptedException e) {
                _logger.error(e);
            }

        }

        /**
         * @param fromAddress
         * @return
         */
        private boolean checkSender(String key, String fromAddress) {
            boolean result = false;
            if (_authorizationMap.containsKey(key)) {
                List list = (ArrayList) _authorizationMap.get(key);
                result = list.contains(fromAddress);
            }

            return result;
        }
    }


    /**
     * Get the blog category. If the category exists, return the
     * appropriate directory, otherwise return the "root" of this blog.
     *
     * @param categoryName Category name
     * @return A directory into which a blog entry can be placed
     * @since blojsom 2.14
     */
    protected File getBlogCategoryDirectory(Blog blog, String categoryName) {
        File blogCategory = new File(blog.getBlogHome() + BlojsomUtils.removeInitialSlash(categoryName));
        if (blogCategory.exists() && blogCategory.isDirectory()) {
            return blogCategory;
        } else {
            return new File(blog.getBlogHome() + "/");
        }
    }

}
