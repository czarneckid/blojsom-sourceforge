package org.ignition.blojsom.plugin.textile;

import java.util.StringTokenizer;


public class Textile implements TextileConstants {

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
        content = content.replaceAll(EXP_DOUBLEQUOTE_MATCH,EXP_DOUBLEQUOTE_REPLACE);


        /**
         * image qtag
         */
        content =content.replaceAll(EXP_IMAGE_QTAG_MATCH, EXP_IMAGE_QTAG_REPLACE);

        //# image with hyperlink
        //text = preg_replace("(<img.+ \\/>):(\\S+)","<a href=\"$2\">$1</a>",text);

        /**
         *  hyperlink qtag
         */
        content = content.replaceAll(EXP_HREF_QTAG_MATCH,EXP_HREF_QTAG_REPLACE);


        /**
         * loop through the array, replacing qtags with html
         */

        for(int x = 0; x < EXP_PHRASE_MODIFIER_SOURCETAGS.length; x++){
            content.replaceAll("(^|\\s|>)" + EXP_PHRASE_MODIFIER_SOURCETAGS[x] + "\\b(.+?)\\b([^\\w\\s]*?)"
                     + EXP_PHRASE_MODIFIER_SOURCETAGS[x] + "([^\\w\\s]{0,2})(\\s|$)?"
                    ,"$1<" + EXP_PHRASE_MODIFIER_REPLACETAGS[x] + ">$2$3</" + EXP_PHRASE_MODIFIER_REPLACETAGS[x] + ">$4");
        }




        /**
         * From the Origional Docs:
         * "some weird bs with underscores and \b word boundaries,
		 * so we'll do those on their own"
         */
        content = content.replaceAll(EXP_EMPHASIS_MATCH,EXP_EMPHASIS_REPLACE);
        content = content.replaceAll(EXP_ITALICS_MATCH,EXP_ITALICS_REPLACE);
        content = content.replaceAll(EXP_SUPERSCRIPT_MATCH,EXP_SUPERSCRIPT_REPLACE);


        /**
         * small problem with double quotes at the end of a string
         */
        content = content.replaceAll(EXP_EOL_DBL_QUOTES, " ");




        return content;

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
