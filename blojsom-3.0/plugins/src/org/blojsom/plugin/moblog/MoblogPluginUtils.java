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
package org.blojsom.plugin.moblog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Moblog Plugin Utils
 *
 * @author David Czarnecki
 * @version $Id: MoblogPluginUtils.java,v 1.5 2007-01-17 02:35:11 czarneckid Exp $
 * @since blojsom 3.0
 */
public class MoblogPluginUtils {

    private static Log _logger = LogFactory.getLog(MoblogPluginUtils.class);

    /**
     * Read in the mailbox settings for a given blog
     *
     * @param blog {@link Blog}
     * @return {@link Mailbox} populated with settings and authorized e-mail addresses or <code>null</code> if there
     *         was an error reading any configuration information
     */
    public static Mailbox readMailboxSettingsForBlog(ServletConfig servletConfig, Blog blog) {
        Mailbox mailbox = new Mailbox();
        mailbox.setEnabled(false);

        mailbox.setId(blog.getId());
        
        String blogID = blog.getBlogId();
        mailbox.setBlogId(blogID);
        mailbox.setBlogBaseURL(blog.getBlogBaseURL());

        String hostname = blog.getProperty(MoblogPlugin.PROPERTY_HOSTNAME);
        if (!BlojsomUtils.checkNullOrBlank(hostname)) {
            mailbox.setHostName(hostname);
        } else {
            mailbox.setEnabled(false);
        }

        String userid = blog.getProperty(MoblogPlugin.PROPERTY_USERID);
        if (!BlojsomUtils.checkNullOrBlank(userid)) {
            mailbox.setUserId(userid);
        } else {
            mailbox.setEnabled(false);
        }

        String password = blog.getProperty(MoblogPlugin.PROPERTY_PASSWORD);
        if (!BlojsomUtils.checkNullOrBlank(password)) {
            mailbox.setPassword(password);
        } else {
            mailbox.setEnabled(false);            
        }

        mailbox.setUrlPrefix(BlojsomConstants.DEFAULT_RESOURCES_DIRECTORY + blogID + "/");
        String resourceUrl = servletConfig.getServletContext().getRealPath(mailbox.getUrlPrefix());
        mailbox.setOutputDirectory(resourceUrl);

        String blogCategoryID = blog.getProperty(MoblogPlugin.PROPERTY_CATEGORY);
        mailbox.setCategoryId(blogCategoryID);

        Boolean enabled = Boolean.valueOf(blog.getProperty(MoblogPlugin.PROPERTY_ENABLED));
        mailbox.setEnabled(enabled.booleanValue());

        String[] types;

        // Extract the image mime types
        String imageMimeTypes = blog.getProperty(MoblogPlugin.PLUGIN_MOBLOG_IMAGE_MIME_TYPES);
        if (BlojsomUtils.checkNullOrBlank(imageMimeTypes)) {
            imageMimeTypes = MoblogPlugin.DEFAULT_IMAGE_MIME_TYPES;
        }
        if (!BlojsomUtils.checkNullOrBlank(imageMimeTypes)) {
            types = BlojsomUtils.parseCommaList(imageMimeTypes);
            if (types.length > 0) {
                Map imageTypesMap = new HashMap();
                for (int i = 0; i < types.length; i++) {
                    String type = types[i];
                    imageTypesMap.put(type, type);
                }
                mailbox.setImageMimeTypes(imageTypesMap);
            }
        }

        // Extract the attachment mime types
        String attachmentMimeTypes = blog.getProperty(MoblogPlugin.PLUGIN_MOBLOG_ATTACHMENT_MIME_TYPES);
        if (!BlojsomUtils.checkNullOrBlank(attachmentMimeTypes)) {
            types = BlojsomUtils.parseCommaList(attachmentMimeTypes);
            if (types.length > 0) {
                Map attachmentTypesMap = new HashMap();
                for (int i = 0; i < types.length; i++) {
                    String type = types[i];
                    attachmentTypesMap.put(type, type);
                }
                mailbox.setAttachmentMimeTypes(attachmentTypesMap);
            }
        } else {
            mailbox.setAttachmentMimeTypes(new HashMap());
        }

        // Extract the text mime types
        String textMimeTypes = blog.getProperty(MoblogPlugin.PLUGIN_MOBLOG_TEXT_MIME_TYPES);
        if (BlojsomUtils.checkNullOrBlank(textMimeTypes)) {
            textMimeTypes = MoblogPlugin.DEFAULT_TEXT_MIME_TYPES;
        }

        if (!BlojsomUtils.checkNullOrBlank(textMimeTypes)) {
            types = BlojsomUtils.parseCommaList(textMimeTypes);
            if (types.length > 0) {
                Map textTypesMap = new HashMap();
                for (int i = 0; i < types.length; i++) {
                    String type = types[i];
                    textTypesMap.put(type, type);
                }
                mailbox.setTextMimeTypes(textTypesMap);
            }
        }

        // Extract the secret word
        String secretWord = blog.getProperty(MoblogPlugin.PLUGIN_MOBLOG_SECRET_WORD);
        if (BlojsomUtils.checkNullOrBlank(secretWord)) {
            mailbox.setSecretWord(null);
        } else {
            mailbox.setSecretWord(secretWord);
        }

        // Configure authorized email addresses for moblog posting
        String authorizedAddresses = blog.getProperty(MoblogPlugin.PLUGIN_MOBLOG_AUTHORIZED_ADDRESSES);
        if (!BlojsomUtils.checkNullOrBlank(authorizedAddresses)) {
            String[] addresses = BlojsomUtils.parseCommaList(authorizedAddresses);
            mailbox.setAuthorizedAddresses(BlojsomUtils.arrayOfStringsToMap(addresses));
        } else {
            mailbox.setAuthorizedAddresses(new HashMap());
        }

        // Configure ignore regular expression
        String ignoreExpression = blog.getProperty(MoblogPlugin.PLUGIN_MOBLOG_IGNORE_EXPRESSION);
        if (BlojsomUtils.checkNullOrBlank(ignoreExpression)) {
            mailbox.setIgnoreExpression(null);
        } else {
            mailbox.setIgnoreExpression(ignoreExpression);
        }

        return mailbox;
    }

    /**
     * Save a file to disk
     *
     * @param filename  Base filename
     * @param extension File extension
     * @param input     Input from which to read and write a file
     * @return # of bytes written to disk
     * @throws IOException If there is an error writing the file
     */
    public static int saveFile(String filename, String extension, InputStream input) throws IOException {
        int count = 0;
        if (filename == null) {
            return count;
        }

        // Do not overwrite existing file
        File file = new File(filename + extension);
        for (int i = 0; file.exists(); i++) {
            file = new File(filename + i + extension);
        }
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        BufferedInputStream bis = new BufferedInputStream(input);
        int aByte;
        while ((aByte = bis.read()) != -1) {
            bos.write(aByte);
            count++;
        }

        bos.flush();
        bos.close();
        bis.close();

        return count;
    }
}