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
package org.ignition.blojsom.util;

import org.ignition.blojsom.blog.BlogEntry;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileFilter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * BlojsomUtils
 *
 * @author David Czarnecki
 * @version $Id: BlojsomUtils.java,v 1.30 2003-03-21 04:07:39 czarneckid Exp $
 */
public class BlojsomUtils implements BlojsomConstants {

    private BlojsomUtils() {
    }

    /**
     * Return a file filter which only returns directories
     *
     * @return File filter appropriate for filtering only directories
     */
    public static FileFilter getDirectoryFilter() {
        return new FileFilter() {
            public boolean accept(File pathname) {
                return (pathname.isDirectory());
            }
        };
    }

    /**
     * Return a file filter which only returns directories that are not one of a list
     * of excluded directories
     *
     * @param excludedDirectories List of directories to exclude
     * @return File filter appropriate for filtering only directories
     */
    public static FileFilter getDirectoryFilter(final String[] excludedDirectories) {
        return new FileFilter() {
            public boolean accept(File pathname) {
                if (!pathname.isDirectory()) {
                    return false;
                } else {
                    for (int i = 0; i < excludedDirectories.length; i++) {
                        String excludedDirectory = excludedDirectories[i];
                        if (pathname.toString().matches(excludedDirectory)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        };
    }

    /**
     * Return a date in RFC 822 style
     *
     * @param date Date
     * @return Date formatted as RFC 822
     */
    public static String getRFC822Date(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(RFC_822_DATE_FORMAT);
        return sdf.format(date);
    }


    /**
     * Return a date in ISO 8601 style
     * http://www.w3.org/TR/NOTE-datetime
     *
     * @param date Date
     * @return Date formatted as ISO 8601
     */
    public static String getISO8601Date(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_DATE_FORMAT);
        return sdf.format(date);
    }

    /**
     * Return a file filter which takes a list of regular expressions to look for
     *
     * @param expressions List of regular expressions for files to retrieve
     * @return File filter appropriate for filtering out a set of files based on regular expressions
     */
    public static FileFilter getRegularExpressionFilter(final String[] expressions) {
        return new FileFilter() {
            public boolean accept(File pathname) {
                for (int i = 0; i < expressions.length; i++) {
                    if (pathname.isDirectory()) {
                        return false;
                    }
                    String expression = expressions[i];
                    if (pathname.getName().matches(expression)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * Return a file filter which takes a list of file extensions to look for
     *
     * @param extensions List of file extensions
     * @return File filter appropriate for filtering out a set of file extensions
     */
    public static FileFilter getExtensionsFilter(final String[] extensions) {
        return new FileFilter() {
            public boolean accept(File pathname) {
                for (int i = 0; i < extensions.length; i++) {
                    if (pathname.isDirectory()) {
                        return false;
                    }
                    String extension = extensions[i];
                    if (pathname.getName().endsWith(extension)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * Return a file filter which takes a single file extension to look for
     *
     * @param extension File extension
     * @return File filter appropriate for filtering out a single file extension
     */
    public static FileFilter getExtensionFilter(final String extension) {
        return getExtensionsFilter(new String[]{extension});
    }

    /**
     * Parse a comma-separated list of values; also parses over internal spaces
     *
     * @param commaList Comma-separated list
     * @return Individual strings from the comma-separated list
     */
    public static String[] parseCommaList(String commaList) {
        StringTokenizer tokenizer = new StringTokenizer(commaList, ", ");
        ArrayList list = new ArrayList();
        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken());
        }
        if (list.size() == 0) {
            return new String[]{};
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    /**
     * Convert the request parameters to a string
     *
     * @param request Servlet request
     * @return Request parameters in the form &amp;name=value
     */
    public static String convertRequestParams(HttpServletRequest request) {
        Enumeration paramNames = request.getParameterNames();
        StringBuffer buffer = new StringBuffer();
        while (paramNames.hasMoreElements()) {
            String name = (String) paramNames.nextElement();
            String value = request.getParameter(name);
            try {
                buffer.append(URLEncoder.encode(name, "UTF-8")).append("=").append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
            }
            if (paramNames.hasMoreElements()) {
                buffer.append("&");
            }
        }
        return buffer.toString();
    }


    /**
     * Strip off the blog home directory for a requested blog category
     *
     * @param blogHome Blog home value
     * @param requestedCategory Requested blog category
     * @return Blog category only
     */
    public static String getBlogCategory(String blogHome,
                                         String requestedCategory) {
        requestedCategory = requestedCategory.replace('\\', '/');
        int indexOfBlogHome = requestedCategory.indexOf(blogHome);
        if (indexOfBlogHome == -1) {
            return "";
        }
        indexOfBlogHome += blogHome.length();
        String returnCategory = requestedCategory.substring(indexOfBlogHome);
        returnCategory = removeInitialSlash(returnCategory);
        return "/" + returnCategory;
    }

    /**
     * Return a URL to the main blog site without the servlet path requested
     *
     * @param blogURL URL for the blog
     * @param servletPath Servlet path under which the blog is placed
     * @return URL to the blog up to the servlet path
     */
    public static String getBlogSiteURL(String blogURL, String servletPath) {
        if (servletPath == null || "".equals(servletPath)) {
            return blogURL;
        }
        int servletPathIndex = blogURL.indexOf(servletPath, 7);
        if (servletPathIndex == -1) {
            return blogURL;
        }

        return blogURL.substring(0, servletPathIndex);
    }

    /**
     * Return an escaped string where &amp;, &lt;, and &gt; are converted to their HTML equivalents
     *
     * @param input Unescaped string
     * @return Escaped string containing HTML equivalents for &amp;, &lt;, and &gt;
     */
    public static String escapeString(String input) {
        if (input == null) {
            return null;
        }

        String unescaped = replace(input, "&", "&amp;");
        unescaped = replace(unescaped, "<", "&lt;");
        unescaped = replace(unescaped, ">", "&gt;");
        return unescaped;
    }

    /**
     * Replace any occurances of a string pattern within a string with a different string.
     *
     * @param str The source string.  This is the string that will be searched and have the replacements
     * @param pattern The pattern to look for in str
     * @param replace The string to insert in the place of <i>pattern</i>
     * @return String with replace occurences
     */
    public static String replace(String str, String pattern, String replace) {
        int s = 0;
        int e = 0;
        StringBuffer result = new StringBuffer();

        while ((e = str.indexOf(pattern, s)) >= 0) {
            result.append(str.substring(s, e));
            result.append(replace);
            s = e + pattern.length();
        }
        result.append(str.substring(s));
        return result.toString();
    }

    /**
     * Return the file extension for a given filename or <code>null</code> if no file extension
     * is present
     *
     * @param filename Filename
     * @return File extension without the . or <code>null</code> if no file extension is present
     */
    public static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex == -1) {
            return null;
        } else {
            return filename.substring(dotIndex + 1);
        }
    }

    /**
     * Return a string of "YYYYMMDD"
     *
     * @param date Date from which to extract "key"
     * @return String of "YYYYMMDD"
     */
    public static String getDateKey(Date date) {
        StringBuffer value = new StringBuffer();
        Calendar calendar = Calendar.getInstance();
        long l = 0;

        calendar.setTime(date);
        value.append(calendar.get(Calendar.YEAR));
        // month and date need to be 2 digits; otherwise it is
        // impossible to distinguish between e.g. November (11)
        // and January (1) when using the date as a prefix
        l = calendar.get(Calendar.MONTH) + 1;
        if (l < 10) {
            value.append("0");
        }
        value.append(l);
        l = calendar.get(Calendar.DAY_OF_MONTH);
        if (l < 10) {
            value.append("0");
        }
        value.append(l);
        // highest possible values above are 12 and 31, so no need to
        // be generic & handle arbitrary-length digits

        return value.toString();
    }

    /**
     * Remove the initial "/" from a string
     *
     * @param input Input string
     * @return Input string without initial "/" removed or <code>null</code> if the input was null
     */
    public static String removeInitialSlash(String input) {
        if (input == null) {
            return null;
        }

        if (!input.startsWith("/")) {
            return input;
        } else {
            return input.substring(1);
        }
    }


    /**
     * Extracts the first line in a given string, otherwise returns the first n bytes
     * @param input String from which to extract the first line
     * @param length Number of bytes to  return if line seperator isnot found
     * @return the first line of the  string
     */
    public static String getFirstLine(String input, int length) {
        String result;
        String lineSeparator = System.getProperty("line.separator");
        int titleIndex = input.indexOf(lineSeparator);
        if (titleIndex == -1) {
            result = input.substring(0, length) + "...";
        } else {
            result = input.substring(0, titleIndex);
        }
        return result;
    }

    /**
     * Return the template name for a particular page
     *
     * @param flavorTemplate Flavor template filename
     * @param Requested page
     * @return
     */
    public static final String getTemplateForPage(String flavorTemplate, String page) {
        int dotIndex = flavorTemplate.lastIndexOf(".");
        if (dotIndex == -1) {
            return flavorTemplate + "-" + page;
        } else {
            StringBuffer newTemplate = new StringBuffer();
            newTemplate.append(flavorTemplate.substring(0, dotIndex));
            newTemplate.append("-");
            newTemplate.append(page);
            newTemplate.append(".");
            newTemplate.append(flavorTemplate.substring(dotIndex + 1, flavorTemplate.length()));
            return newTemplate.toString();
        }
    }

    /**
     * Tries to retrieve a given key using getParameter(key) and if not available, will
     * use getAttribute(key) from the servlet request
     *
     * @param key Parameter to retrieve
     * @param httpServletRequest Request
     * @return Value of the key as a string, or <code>null</code> if there is no parameter/attribute
     */
    public static final String getRequestValue(String key, HttpServletRequest httpServletRequest) {
        if (httpServletRequest.getParameter(key) != null) {
            return httpServletRequest.getParameter(key);
        } else if (httpServletRequest.getAttribute(key) != null) {
            return httpServletRequest.getAttribute(key).toString();
        }

        return null;
    }

    /**
     * Return only the filename of a permalink request
     *
     * @param permalink Permalink request
     * @param blogEntryExtensions Regex for blog entries so that we only pickup requests for valid blog entries
     * @return Filename portion of permalink request
     */
    public static final String getFilenameForPermalink(String permalink, String[] blogEntryExtensions) {
        if (permalink == null) {
            return null;
        }

        boolean matchesExtension = false;
        for (int i = 0; i < blogEntryExtensions.length; i++) {
            String blogEntryExtension = blogEntryExtensions[i];
            if (permalink.matches(blogEntryExtension)) {
                matchesExtension = true;
                break;
            }
        }
        if (!matchesExtension) {
            return null;
        }

        int indexOfSlash = permalink.lastIndexOf("/");
        if (indexOfSlash == -1) {
            return permalink;
        } else {
            String sanitizedPermalink = permalink.substring(indexOfSlash + 1, permalink.length());
            if (sanitizedPermalink.startsWith("..")) {
                sanitizedPermalink = sanitizedPermalink.substring(2, sanitizedPermalink.length());
            } else if (sanitizedPermalink.startsWith(".")) {
                sanitizedPermalink = sanitizedPermalink.substring(1, sanitizedPermalink.length());
            }

            return sanitizedPermalink;
        }
    }

    /**
     * Return a comparator that uses a file's last modified time to order the files. If the
     * files have the same last modified time, the file's names are compared to order the
     * files.
     */
    public static Comparator FILE_TIME_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            File f1;
            File f2;

            if ((o1 instanceof BlogEntry) && (o2 instanceof BlogEntry)) {
                f1 = ((BlogEntry) o1).getSource();
                f2 = ((BlogEntry) o2).getSource();
            } else {
                f1 = (File) o1;
                f2 = (File) o2;
            }

            if (f1.lastModified() > f2.lastModified()) {
                return -1;
            } else if (f1.lastModified() < f2.lastModified()) {
                return 1;
            } else {
                return f1.getName().compareTo(f2.getName());
            }
        }
    };

    /**
     * Return a comparator that uses a file's last modified time to order the files in ascending order.
     * If the files have the same last modified time, the file's names are compared to order the
     * files.
     */
    public static Comparator FILE_TIME_ASCENDING_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            File f1;
            File f2;

            if ((o1 instanceof BlogEntry) && (o2 instanceof BlogEntry)) {
                f1 = ((BlogEntry) o1).getSource();
                f2 = ((BlogEntry) o2).getSource();
            } else {
                f1 = (File) o1;
                f2 = (File) o2;
            }

            if (f1.lastModified() > f2.lastModified()) {
                return 1;
            } else if (f1.lastModified() < f2.lastModified()) {
                return -1;
            } else {
                return f1.getName().compareTo(f2.getName());
            }
        }
    };

    /**
     * Return a comparator to sort by name
     */
    public static Comparator FILE_NAME_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;

            return s1.compareTo(s2);
        }
    };

    static final byte HEX_DIGITS[] = {
        (byte) '0', (byte) '1', (byte) '2', (byte) '3',
        (byte) '4', (byte) '5', (byte) '6', (byte) '7',
        (byte) '8', (byte) '9', (byte) 'a', (byte) 'b',
        (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'
    };


    /**
     * Performs an MD5 Digest onthe given String content
     *
     * @param data Content to digest
     * @return The Hash as Hex String
     */
    public static String digestString(String data) {
        try {
            MessageDigest _md = MessageDigest.getInstance("MD5");
            _md.update(data.getBytes());
            byte[] _digest = _md.digest();
            String _ds = toHexString(_digest, 0, _digest.length);
            return _ds;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * Convert Byte Array to Hex Value
     * @param buf Byte Array to convert to Hex Value
     * @param offset Starting Offset for Conversion
     * @param length Length to convery
     * @param value Hex Value
     */
    private static void toHexValue(byte[] buf, int offset, int length, int value) {
        do {
            buf[offset + --length] = HEX_DIGITS[value & 0x0f];
            value >>>= 4;
        } while (value != 0 && length > 0);

        while (--length >= 0) {
            buf[offset + length] = HEX_DIGITS[0];
        }
    }


    /**
     * Convert a Byte  Array to a Hex String
     * @param buf Byte Array to convert to Hex String
     * @param offset Starting Offset for Conversion
     * @param length Length to convery
     * @return A Hex String representing the byte array
     */
    public static String toHexString(byte[] buf, int offset, int length) {
        byte[] buf1 = new byte[length * 2];
        for (int i = 0; i < length; i++) {
            toHexValue(buf1, i * 2, 2, buf[i + offset]);
        }
        return new String(buf1);
    }


}
