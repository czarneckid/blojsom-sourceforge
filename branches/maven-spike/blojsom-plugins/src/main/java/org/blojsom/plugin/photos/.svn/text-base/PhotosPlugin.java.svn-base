/**
 * Copyright (c) 2005-2006, Timothy Stone
 * All rights reserved.
 *
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the "Timothy Stone" nor the names of
 * its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
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
package org.blojsom.plugin.photos;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import javax.servlet.ServletConfig;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;

/**
 * Photos Plugin
 * A simple photo gallery plugin
 *
 * @author Timothy Stone
 * @version 1.0
 */
public class PhotosPlugin implements Plugin {

    private Log _logger = LogFactory.getLog(PhotosPlugin.class);
    private ServletConfig _servletConfig;
    private Properties _blojsomProperties;

    /**
     * Default constructor.
     */
    public PhotosPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws PluginException If there is an error initializing the plugin
     */
    @Override
    public void init() throws PluginException {
    }

    /**
     * Process the blog entries
     *
     * @param request  Request
     * @param response Response
     * @param blog                {@link Blog} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws BlojsomPluginException If there is an error processing the blog entries
     */
    @Override
    public Entry[] process(HttpServletRequest request,
            HttpServletResponse response,
            Blog blog,
            Map context,
            Entry[] entries) throws PluginException {

        String blogResourcePath = getBlogResourcePath(blog);

        for (int i = 0; i < entries.length; i++) {
            Entry entry = entries[i];
            ArrayList photos = new ArrayList();
            String photoAlbum = getPhotoAlbum(entry.getMetaData());
            String photoAlbumList = getPhotoAlbumList(entry.getMetaData());
            String photoAlbumThumbnail = getPhotoAlbumThumbnail(entry.getMetaData());
            String photoAlbumRealPath = getPhotoAlbumRealPath(blogResourcePath + BlojsomUtils.removeSlashes(photoAlbum));
            if (!BlojsomUtils.checkNullOrBlank(photoAlbumRealPath)) {
                if (!BlojsomUtils.checkNullOrBlank(photoAlbumList)) {
                    String[] filesInList = BlojsomUtils.parseCommaList(photoAlbumList);
                    for (int f = 0; f < filesInList.length; f++) {
                        photos.add(blogResourcePath + BlojsomUtils.addTrailingSlash(photoAlbum) + filesInList[f]);
                    }
                } else {
                    try {
                        File file = new File(photoAlbumRealPath);
                        if(file.exists()) {
                            if (file.isDirectory()) {
                                File[] files = file.listFiles(BlojsomUtils.getExtensionsFilter(new String[]{".jpeg", ".jpg", ".gif", ".png"}));
                                for (int f = 0; f < files.length; f++) {
                                    if (!(files[f].getName()).equalsIgnoreCase(photoAlbumThumbnail)) {
                                        photos.add(blogResourcePath + BlojsomUtils.addTrailingSlash(photoAlbum) + files[f].getName());
                                    }
                                }
                            }
                            if (file.isFile()) {
                                photos.add(blogResourcePath + photoAlbum);
                            }
                        }
                    } catch (NullPointerException npe) {
                        _logger.error("Photo album is 'null'.\n");
                    } catch (SecurityException se) {
                        _logger.error(se.getMessage());
                    }
                }
                if (photoAlbumThumbnail != null) {
                    photoAlbumThumbnail = blogResourcePath + BlojsomUtils.addTrailingSlash(photoAlbum) + photoAlbumThumbnail;
                    entry.getMetaData().put("photo-album-thumbnail", photoAlbumThumbnail);
                    _logger.debug("photoAlbumThumbnail is: " + photoAlbumThumbnail);
                }
                entry.getMetaData().put("photo-album", photos);
                _logger.debug("Photo album loaded with "
                        + photos.size() + ((photos.size() == 1) ? " photo." : " photos."));
            } else {
                _logger.debug("No photo album. Moving on...\n");
            }
        }
        return entries;
    }

    private String getBlogResourcePath(Blog blog) {
        String blogResourcePath = _blojsomProperties.getProperty("resources-directory") + blog.getBlogId();
        blogResourcePath = BlojsomUtils.addTrailingSlash(blogResourcePath);
        _logger.debug("blogResourcePath is: " + blogResourcePath);
        return blogResourcePath;
    }

    private String getPhotoAlbum(Map metaData) {
        String photoAlbum;
        if (BlojsomUtils.checkMapForKey(metaData, "photo-album")) {
            photoAlbum = ((String) metaData.get("photo-album")).trim();
        } else {
            photoAlbum = null;
        }
        _logger.debug("photoAlbum is: " + photoAlbum);
        return photoAlbum;
    }

    private String getPhotoAlbumList(Map metaData) {
        String photoAlbumList;
        if (BlojsomUtils.checkMapForKey(metaData, "photo-album-list")) {
            photoAlbumList = ((String) metaData.get("photo-album-list")).trim();
        } else {
            photoAlbumList = null;
        }
        _logger.debug("photoAlbumList is: " + photoAlbumList);
        return photoAlbumList;
    }

    private String getPhotoAlbumThumbnail(Map metaData) {
        String photoAlbumThumbnail;
        if (BlojsomUtils.checkMapForKey(metaData, "photo-album-thumbnail")) {
            photoAlbumThumbnail = ((String) metaData.get("photo-album-thumbnail")).trim();
        } else {
            photoAlbumThumbnail = null;
        }
        _logger.debug("photoAlbumThumbnail is: " + photoAlbumThumbnail);
        return photoAlbumThumbnail;
    }

    private String getPhotoAlbumRealPath(String photoAlbum) {
        String photoAlbumRealPath = _servletConfig.getServletContext().getRealPath(photoAlbum);
        _logger.debug("photoAlbumRealPath is: " + photoAlbumRealPath);
        return photoAlbumRealPath;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws PluginException If there is an error performing cleanup for this plugin
     */
    @Override
    public void cleanup() throws PluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws PluginException If there is an error in finalizing this plugin
     */
    @Override
    public void destroy() throws PluginException {
    }

    /**
     * @param servletContext the _servletContext to set
     */
    public void setServletConfig(ServletConfig servletConfig) {
        this._servletConfig = servletConfig;
    }

    /**
     * @param blojsomProperties the _blojsomProperties to set
     */
    public void setBlojsomProperties(Properties blojsomProperties) {
        this._blojsomProperties = blojsomProperties;
    }
}
