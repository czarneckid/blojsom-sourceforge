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
package org.ignition.blojsom.plugin.textile;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Textile
 *
 * @author Mark Lussier
 * @version $Id: Textile.java,v 1.3 2003-05-27 01:25:09 intabulas Exp $
 */
public class Textile implements TextileConstants {

    /**
     *
     */
    public Textile() {
    }


    public String process(String content) {

        /**
         * Turn any incoming ampersands into a dummy character for now.
         * This uses a negative lookahead for alphanumerics followed by a semicolon,
         * implying an incoming html entity, to be skipped
         */
        //text = preg_replace("&(?![#a-zA-Z0-9]+;)","x%x%",text);
        content = content.replaceAll(EXP_AMPERSAND, EXP_AMPERSAND_REPLACE);

        /**
         * unentify angle brackets and ampersands
         */
        content = replace(content, "&gt;", ">");
        content = replace(content, "&lt;", "<");
        content = replace(content, "&amp;", "&");




        /**
         *  zap carriage returns
         * @todo optimize
         */
        content = replace(content, "\r\n", "\n");

        /**
         * zap tabs
         * @todo optimize
         */
        content = replace(content, "\t", "");


        /**
         * trim each line
         */
        StringBuffer splitBuffer = new StringBuffer();
        StringTokenizer tokenizer = new StringTokenizer(content, "\n", true);
        while (tokenizer.hasMoreTokens()) {
            splitBuffer.append(tokenizer.nextToken().trim());
            splitBuffer.append("\n");
        }

        content = splitBuffer.toString();

        //### Find and replace quick tags


        /**
         * double equal signs mean <notextile>
         */
        content = content.replaceAll(EXP_DOUBLEQUOTE_MATCH, EXP_DOUBLEQUOTE_REPLACE);


        /**
         * image qtag
         */
        content = content.replaceAll(EXP_IMAGE_QTAG_MATCH, EXP_IMAGE_QTAG_REPLACE);

        //# image with hyperlink
        //text = preg_replace("(<img.+ \\/>):(\\S+)","<a href=\"$2\">$1</a>",text);

        /**
         *  hyperlink qtag
         */
        content = content.replaceAll(EXP_HREF_QTAG_MATCH, EXP_HREF_QTAG_REPLACE);


        /**
         * loop through the array, replacing qtags with html
         */

        for (int x = 0; x < EXP_PHRASE_MODIFIER_SOURCETAGS.length; x++) {
            content.replaceAll("(^|\\s|>)" + EXP_PHRASE_MODIFIER_SOURCETAGS[x] + "\\b(.+?)\\b([^\\w\\s]*?)"
                               + EXP_PHRASE_MODIFIER_SOURCETAGS[x] + "([^\\w\\s]{0,2})(\\s|$)?"
                               , "$1<" + EXP_PHRASE_MODIFIER_REPLACETAGS[x] + ">$2$3</" + EXP_PHRASE_MODIFIER_REPLACETAGS[x] + ">$4");
        }







        /**
         * From the Origional Docs:
         * "some weird bs with underscores and \b word boundaries,
         * so we'll do those on their own"
         */
        content = content.replaceAll(EXP_EMPHASIS_MATCH, EXP_EMPHASIS_REPLACE);
        content = content.replaceAll(EXP_ITALICS_MATCH, EXP_ITALICS_REPLACE);
        content = content.replaceAll(EXP_SUPERSCRIPT_MATCH, EXP_SUPERSCRIPT_REPLACE);


        /**
         * small problem with double quotes at the end of a string
         */
        content = content.replaceAll(EXP_EOL_DBL_QUOTES, " ");


        String[] glyphMatches = {EXP_SINGLE_CLOSING,
                                 EXP_SINGLE_OPENING,
                                 EXP_DOUBLE_CLOSING,
                                 EXP_DOUBLE_OPENING,
                                 EXP_ELLIPSES,
                                 EXP_3UPPER_ACCRONYM,
                                 EXP_3UPPERCASE_CAPS,
                                 EXP_EM_DASH,
                                 EXP_EN_DASH,
                                 EXP_EN_DECIMAL_DASH,
                                 EXP_DIMENSION_SIGN,
                                 EXP_TRADEMARK,
                                 EXP_REGISTERED,
                                 EXP_COPYRIGHT};


        String[] glyphReplacement = {REPLACE_SINGLE_CLOSING,
                                     REPLACE_SINGLE_OPENING,
                                     REPLACE_DOUBLE_CLOSING,
                                     REPLACE_DOUBLE_OPENING,
                                     REPLACE_ELLIPSES,
                                     REPLACE_3UPPER_ACCRONYM,
                                     REPLACE_3UPPERCASE_CAPS,
                                     REPLACE_EM_DASH,
                                     REPLACE_EN_DASH,
                                     REPLACE_EN_DECIMAL_DASH,
                                     REPLACE_DIMENSION_SIGN,
                                     REPLACE_TRADEMARK,
                                     REPLACE_REGISTERED,
                                     REPLACE_COPYRIGHT};


        boolean ishtml = Pattern.compile(EXP_ISHTML).matcher(content).find();
        boolean inpreservation = false;

        if (!ishtml) {
            content = arrayReplaceAll(content, glyphMatches, glyphReplacement);
        } else {
            String[] segments = splitContent(EXP_ISHTML, content);

            StringBuffer segmentBuffer = new StringBuffer();
            for (int x = 0; x < segments.length; x++) {
                //  # matches are off if we're between <code>, <pre> etc.
                if (segments[x].toLowerCase().matches(EXP_STARTPRESERVE)) {
                    inpreservation = true;
                } else if (segments[x].toLowerCase().matches(EXP_ENDPRESERVE)) {
                    inpreservation = true;
                }

                if (!Pattern.compile(EXP_ISHTML).matcher(segments[x]).find() && !inpreservation) {
                    segments[x] = arrayReplaceAll(segments[x], glyphMatches, glyphReplacement);
                }

                //# convert htmlspecial if between <code>
                if (inpreservation) {
                    segments[x] = htmlSpecialChars(segments[x], MODE_ENT_NOQUOTES);
                    segments[x] = replace(segments[x], "&lt;pre&gt;", "<pre>");
                    segments[x] = replace(segments[x], "&lt;code&gt;", "<code>");
                    segments[x] = replace(segments[x], "&lt;notextile&gt;", "<notextile>");
                }

                segmentBuffer.append(segments[x]);

            }

            content = segmentBuffer.toString();

        }


        //### Block level formatting

        //# deal with forced breaks; this is going to be a problem between
        //#  <pre> tags, but we'll clean them later

        content = content.replaceAll(EXP_FORCESLINEBREAKS, REPLACE_FORCESLINEBREAK);

        //# might be a problem with lists
        content = replace(content, "l><br />", "l>\n");


        String[] blockMatches = {EXP_BULLETED_LIST,
                                 EXP_NUMERIC_LIST,
                                 EXP_BLOCKQUOTE,
                                 EXP_HEADER_WITHCLASS,
                                 EXP_HEADER,
                                 EXP_PARA_WITHCLASS,
                                 EXP_PARA,
                                 EXP_REMAINING_PARA};

        String[] blockReplace = {REPLACE_BULLETED_LIST,
                                 REPLACE_NUMERIC_LIST,
                                 REPLACE_BLOCKQUOTE,
                                 REPLACE_HEADER_WITHCLASS,
                                 REPLACE_HEADER,
                                 REPLACE_PARA_WITHCLASS,
                                 REPLACE_PARA,
                                 REPLACE_REMAINING_PARA};


        StringBuffer blockBuffer = new StringBuffer();
        String list = "";
        content += " \n";

        boolean inpre = false;
        //# split the text into an array by newlines
        StringTokenizer blockTokenizer = new StringTokenizer(content, "\n", false);

        while (blockTokenizer.hasMoreTokens()) {
            String line = blockTokenizer.nextToken();

            //#make sure the line isn't blank
            if (!line.matches("^$")) {

                //# matches are off if we're between <pre> or <code> tags
                if (line.toLowerCase().indexOf("<pre>") > -1) {
                    inpre = true;
                }

                //# deal with block replacements first, then see if we're in a list
                if (!inpre) {
                    line = arrayReplaceAll(line, blockMatches, blockReplace);
                }

                //# kill any br tags that slipped in earlier
                if (inpre) {
                    line = replace(line, "<br />", "\n");
                    line = replace(line, "<br/>", "\n");
                }
                //# matches back on after </pre>
                if (line.toLowerCase().indexOf("</pre>") > -1) {
                    inpre = false;
                }

                //# at the beginning of a list, $line switches to a value
                boolean islist = Pattern.compile(EXP_LISTSTART).matcher(line).find();
                boolean islistline = Pattern.compile(EXP_LISTSTART + list).matcher(line).find();
                if (list.length() == 0 && islist) {
                    line = line.replaceAll(EXP_MATCHLIST, REPLACE_MATCHLIST);
                    list = line.substring(2, 3);

                    //# at the end of a list, $line switches to empty
                } else if (list.length() > 0 && !islistline) {
                    line = line.replaceAll(EXP_ENDMATCHLIST, "</" + list + REPLACE_ENDMATCHLIST);
                    list = "";
                }
            }
            // push each line to a new array once it's processed
            blockBuffer.append(line);
            blockBuffer.append("\n");

        }

        content = blockBuffer.toString();


        //#clean up <notextile>
        content = content.replaceAll("<\\/?notextile>", "");

        //# clean up liu and lio
        content = content.replaceAll("<(\\/?)li(u|o)>", "<$1li>");

        //# turn the temp char back to an ampersand entity
        content = replace(content,"x%x%","&#38;");

        //# Newline linebreaks, just for markup tidiness
        content= replace(content,"<br />","<br />\n");


        return content;

    }

    /**
     * An implementation of the PHP htmlspecialchars()
     * @param content
     * @param mode
     * @return
     */
    private String htmlSpecialChars(String content, int mode) {

        content = replace(content, "&", "&amp;");


        if (mode != MODE_ENT_NOQUOTES) {
            content = replace(content, "\"", "&quot;");
        }
        if (mode == MODE_ENT_QUOTES) {
            content = replace(content, "'", "&#039;");
        }
        content = replace(content, "<", "&lt;");
        content = replace(content, ">", "&gt;");
        return content;

    }


    private String[] splitContent(String matchexp, String content) {

        int startAt = 0;
        List tempList = new ArrayList();

        Pattern pattern = Pattern.compile(matchexp);

        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            tempList.add(content.substring(startAt, matcher.start()));
            tempList.add(matcher.group());
            startAt = matcher.end();
        }

        tempList.add(content.substring(startAt));

        String[] result = new String[tempList.size()];

        for (int i = 0; i < result.length; i++) {
            result[i] = (String) tempList.get(i);
        }

        return result;

    }


    /**
     *
     * @param content
     * @param matches
     * @param replaces
     * @return
     */
    private String arrayReplaceAll(String content, String[] matches, String[] replaces) {

        String result = content;

        for (int x = 0; x < matches.length; x++) {
            result = result.replaceAll(matches[x], replaces[x]);
        }

        return result;
    }


    /**
     * Replace any occurances of a string pattern within a string with a different string.
     *
     * @param str The source string.  This is the string that will be searched and have the replacements
     * @param pattern The pattern to look for in str
     * @param replace The string to insert in the place of <i>pattern</i>
     * @return String with replace occurences
     */
    private static String replace(String str, String pattern, String replace) {
        if (str == null || "".equals(str)) {
            return str;
        }

        if (replace == null) {
            return str;
        }

        if ("".equals(pattern)) {
            return str;
        }

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


}
