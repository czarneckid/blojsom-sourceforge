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
package org.blojsom.plugin.common;

import org.blojsom.blog.Blog;
import org.blojsom.blog.Entry;
import org.blojsom.plugin.Plugin;
import org.blojsom.plugin.PluginException;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * StringUtilities plugin
 *
 * @author David Czarnecki
 * @version $Id: StringUtilitiesPlugin.java,v 1.2 2006-10-26 01:14:58 czarneckid Exp $
 * @since blojsom 3.0
 */
public class StringUtilitiesPlugin implements Plugin {

    private static final String BLOJSOM_PLUGIN_STRING_UTILITIES = "BLOJSOM_PLUGIN_STRING_UTILITIES";

    /**
     * Construct a new StringUtilities plugin
     */
    public StringUtilitiesPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error initializing the plugin
     */
    public void init() throws PluginException {
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param blog                {@link Blog} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws PluginException If there is an error processing the blog entries
     */
    public Entry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, Map context, Entry[] entries) throws PluginException {
        context.put(BLOJSOM_PLUGIN_STRING_UTILITIES, new StringUtilities());

        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws PluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws org.blojsom.plugin.PluginException
     *          If there is an error in finalizing this plugin
     */
    public void destroy() throws PluginException {
    }

    /**
     * Utility class for bundling string utility functions to make available to templates
     */
    public class StringUtilities {

        /**
         * Construct a new instance of StringUtilities
         */
        public StringUtilities() {
        }

        /**
         * Return an escaped string where &amp;, &lt;, &gt;, &quot;, and &apos; are converted to their HTML equivalents
         *
         * @param input Unescaped string
         * @return Escaped string containing HTML equivalents for &amp;, &lt;, &gt;, &quot;, and &apos;
         */
        public String escapeString(String input) {
            return BlojsomUtils.escapeString(input);
        }

        /**
         * Return an escaped string where &amp;, &lt;, &gt; are converted to their HTML equivalents
         *
         * @param input Unescaped string
         * @return Escaped string containing HTML equivalents for &amp;, &lt;, &gt;
         */
        public String escapeStringSimple(String input) {
            return BlojsomUtils.escapeStringSimple(input);
        }

        /**
         * Return an escaped string where &lt;, &gt; are converted to their HTML equivalents
         *
         * @param input Unescaped string
         * @return Escaped string containing HTML equivalents for &lt;, &gt;
         */
        public String escapeBrackets(String input) {
            return BlojsomUtils.escapeBrackets(input);
        }


        /**
         * Return a UTF-8 encoded string from the input
         *
         * @param input Input
         * @return Input that has been encoded using UTF-8
         */
        public String encodeStringUTF8(String input) {
            return BlojsomUtils.urlEncode(input);
        }

        /**
         * Decode a UTF-8 encoded string from the input
         *
         * @param input Input
         * @return Input that has been decoded using UTF-8
         */
        public String decodeStringUTF8(String input) {
            return BlojsomUtils.urlDecode(input);
        }

        /**
         * Parse a comma-separated list of values; also parses over internal spaces
         *
         * @param commaList Comma-separated list
         * @return Individual strings from the comma-separated list
         */
        public String[] parseCommaList(String commaList) {
            return BlojsomUtils.parseCommaList(commaList);
        }

        /**
         * Parse a comma-separated list of values
         *
         * @param commaList Comma-separated list
         * @return Individual strings from the comma-separated list
         */
        public String[] parseOnlyCommaList(String commaList) {
            return BlojsomUtils.parseOnlyCommaList(commaList);
        }

        /**
         * Parse a comma-separated list of values
         *
         * @param commaList Comma-separated list
         * @param trim If the contents of the array should be trimmed
         * @return Individual strings from the comma-separated list
         */
        public String[] parseOnlyCommaList(String commaList, boolean trim) {
            return BlojsomUtils.parseOnlyCommaList(commaList, trim);
        }

        /**
         * Parse a string into two separate strings based on the last comma in the input value
         *
         * @param value Input
         * @return Parsed string
         */
        public String[] parseLastComma(String value) {
            return BlojsomUtils.parseLastComma(value);
        }

        /**
         * Parse a delimited list of values
         *
         * @param delimitedList Delimited list
         * @param delimiter     Field Delimiter
         * @return Individual strings from the comma-separated list
         */
        public  String[] parseDelimitedList(String delimitedList, String delimiter) {
            return BlojsomUtils.parseDelimitedList(delimitedList, delimiter);
        }

        /**
         * Parse a delimited list of values
         *
         * @param delimitedList Delimited list
         * @param delimiter     Field Delimiter
         * @param trim If the contents of the array should be trimmed
         * @return Individual strings from the comma-separated list
         */
        public String[] parseDelimitedList(String delimitedList, String delimiter, boolean trim) {
            return BlojsomUtils.parseDelimitedList(delimitedList, delimiter, trim);
        }

    }
}
