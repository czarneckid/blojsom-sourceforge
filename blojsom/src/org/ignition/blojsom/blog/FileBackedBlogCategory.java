/**
 * Copyright (c) 2003, David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003 by Mark Lussier
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
package org.ignition.blojsom.blog;

import org.ignition.blojsom.util.BlojsomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * FileBackedBlogCategory
 *
 * @author David Czarnecki
 * @version $Id: FileBackedBlogCategory.java,v 1.2 2003-04-19 14:38:30 czarneckid Exp $
 */
public class FileBackedBlogCategory extends BlogCategory {

    private Log _logger = LogFactory.getLog(FileBackedBlogCategory.class);

    /**
     * Create a new FileBackedBlogCategory.
     */
    public FileBackedBlogCategory() {
        super();
    }

    /**
     * Create a new FileBackedBlogCategory.
     *
     * @param category Category name
     * @param categoryURL Category URL
     */
    public FileBackedBlogCategory(String category, String categoryURL) {
        super(category, categoryURL);
    }

    /**
     * Load the meta data for the category
     *
     * @param blogHome Directory where blog entries are stored
     * @param propertiesExtensions List of file extensions to use when looking for category properties
     */
    public void loadMetaData(String blogHome, String[] propertiesExtensions) {
        File blog = new File(blogHome + BlojsomUtils.removeInitialSlash(_category));

        // Load properties file for category (if present)
        File[] categoryPropertyFiles = blog.listFiles(BlojsomUtils.getExtensionsFilter(propertiesExtensions));
        if ((categoryPropertyFiles != null) && (categoryPropertyFiles.length > 0)) {
            Properties dirProps = new Properties();
            for (int i = 0; i < categoryPropertyFiles.length; i++) {
                try {
                    FileInputStream _fis = new FileInputStream(categoryPropertyFiles[i]);
                    dirProps.load( _fis);
                    _fis.close();
                } catch (IOException ex) {
                    _logger.warn("Failed loading properties from: " + categoryPropertyFiles[i].toString());
                    continue;
                }
            }

            setMetaData(dirProps);
        }
    }
}
