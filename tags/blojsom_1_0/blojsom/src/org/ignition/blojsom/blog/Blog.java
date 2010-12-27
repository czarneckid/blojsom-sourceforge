/**
 * Copyright (c) 2003, David A. Czarnecki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the "David A. Czarnecki" nor the names of
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
package org.ignition.blojsom.blog;

/**
 * Blog
 *
 * @author David Czarnecki
 */
public class Blog {

    private String _blogName;
    private String _blogDescription;
    private String _blogURL;
    private String _blogLanguage;

    /**
     * Blog constructor
     *
     * @param blogName Name of the blog
     * @param blogDescription Detailed description of the blog
     * @param blogURL URL for the blog
     * @param blogLanguage Language of the blog
     */
    public Blog(String blogName, String blogDescription, String blogURL, String blogLanguage) {
        this._blogName = blogName;
        this._blogDescription = blogDescription;
        this._blogURL = blogURL;
        this._blogLanguage = blogLanguage;
    }

    /**
     * Name of the blog
     *
     * @return Blog name
     */
    public String getBlogName() {
        return _blogName;
    }

    /**
     * Set the name of the blog
     *
     * @param _blogName Name for the blog
     */
    public void setBlogName(String _blogName) {
        this._blogName = _blogName;
    }

    /**
     * Description of the blog
     *
     * @return Blog description
     */
    public String getBlogDescription() {
        return _blogDescription;
    }

    /**
     * Set the description for the blog
     *
     * @param _blogDescription Description for the blog
     */
    public void setBlogDescription(String _blogDescription) {
        this._blogDescription = _blogDescription;
    }

    /**
     * URL for the blog
     *
     * @return Blog URL
     */
    public String getBlogURL() {
        return _blogURL;
    }

    /**
     * Set the URL for the blog
     *
     * @param _blogURL URL for the blog
     */
    public void setBlogURL(String _blogURL) {
        this._blogURL = _blogURL;
    }

    /**
     * Language of the blog
     *
     * @return Blog language
     */
    public String getBlogLanguage() {
        return _blogLanguage;
    }

    /**
     * Set the language for the blog
     *
     * @param _blogLanguage Language for the blog
     */
    public void setBlogLanguage(String _blogLanguage) {
        this._blogLanguage = _blogLanguage;
    }
}
