package org.ignition.blojsom.plugin.textile;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: lussiema
 * Date: May 15, 2003
 * Time: 1:41:27 PM
 * To change this template use Options | File Templates.
 */
public class TestTextilePlugin implements TextileConstants {


    public static void main(String[] args) {


        String[] glyphs = {
            "([^\\s[{<])?\\'(?(1)|(?=\\s|s\\b))"
        };


        String[] replace = {
            "$1&#8217;$2"
        };

        String[] glyph_search = {
            "([^\\']*)\\'([^\\']*)",
            "\\'", // single opening
            "([^\\']*)\\\"([^\\\"]*)", // # double closing
            "\"", // double opening
            "\\b( )?\\.{3}", // # ellipsis
            "\\b([A-Z][A-Z0-9]{2,})\\b(\\(([^\\)]+)\\))", // # 3+ uppercase acronym
            "(^|[^\"][>\\s])([A-Z][A-Z0-9 ]{2,})([^<a-z0-9]|$)", // # 3+ uppercase caps
            "\\s?--\\s?", // # em dash
            "\\s-\\s", // # en dash
            "(\\d+)-(\\d+)", // # en dash
            "(\\d+) ?x ?(\\d+)", //# dimension sign
            "\\b ?(\\((tm|TM)\\))", // trademark
            "\\b ?(\\([rR]\\))", // # registered
            "\\b ?(\\([cC]\\))" // # registered
        };


        String[] glyph_replace = {
            "$1&#8217;$2", //# single closing
            "&#8216;", //# single opening
            "$1&#8221;", //# double closing
            "&#8220;", //# double opening
            "$1&#8230;", //# ellipsis
            "<acronym title=\"$2\">$1</acronym>", //# 3+ uppercase acronym
            "$1<span class=\"caps\">$2</span>$3", //# 3+ uppercase caps
            "&#8212;", //# em dash
            " &#8211; ", //# en dash
            "$1&#8211;$2", //# en dash
            "$1&#215;$2", //# dimension sign
            "&#8482;", //# trademark
            "&#174;", //# registered
            "&#169;"                //# copyright
        };


        String[] test_values = {
            "So Mark's", //# single closing
            "So 'steve'", //# single opening
            "So Welcome\"", //# double closing
            "So \"Welcome", //# double opening
            "Hmm...", //# ellipsis
            "WTF(What The Fsck)", //# 3+ uppercase acronym
            " my own WTF tgas", //# 3+ uppercase caps
            "-- Wow", //# em dash
            "-w?", //# en dash
            "111-111", //# en dash
            "4 x 4", //# dimension sign
            "blojsim(tm)", //# trademark
            "blojsim(r)", //# registered
            "Copyright(c) blojsim"                //# copyright
        };

        String[] blockfind = {
            "^\\s?\\*\\s(.*)$", // bulleted list *
            "^\\s?#\\s(.*)$", //numeric list #
            "^bq\\. (.*)", // blockquote bq.
            "^h(\\d)\\(([\\w]+)\\)\\.\\s(.*)",
            "^h(\\d)\\. (.*)", // plain header hn.
            "^p\\(([\\w]+)\\)\\.\\s(.*)", // para p(class).  w/ css class
            "^p\\. (.*)", // plain paragraph
            "^([^\\t ]+.*)"						// remaining plain paragraph
        };

        String[] blockreplace = {
            "\t<liu>$1</liu>",
            "\t<lio>$1</lio>",
            "\t<blockquote>$1</blockquote>",
            "\t<h$1 class=\"$2\">$3</h$1>",
            "\t<h$1>$2</h$1>",
            "\t<p class=\"$1\">$2</p>",
            "\t<p>$1</p>",
            "\t<p>$1</p>"
        };


        String[] blocktest = {
            "* Test the terrordone\n", // bulleted list *
            "# Whoopy", //numeric list #
            "bq. Testing", // blockquote bq.
            "h1(blah). Testing", // header hn(class).  w/ css class
            "h1. Testing", // plain header hn.
            "p(testing). Blah", // para p(class).  w/ css class
            "p. lagh", // plain paragraph
            "sdasdasdasd"						// remaining plain paragraph
        };



String f = "h2. This is a title\n" +
"\n" +
"h3. This is a subhead\n" +
"\n" +
"This is some text of dubious character. Isn't the use of \"quotes\" just lazy writing -- and theft of 'intellectual property' besides? I think the time has come to see a block quote.\n" +
"\n" +
"bq. This is a block quote. I'll admit it's not the most exciting block quote ever devised.\n" +
"\n" +
"Well, that went well. How about we insert an <a href=\"/\" title=\"watch out\">old-fashioned hypertext link</a>? Will the quote marks in the tags get messed up? No!\n" +
"\n" +
"\"This is a link (optional title)\":http://www.textism.com\n" +
"\n" +
"An image:\n" +
"\n" +
"!/common/textist.gif(optional alt text)!\n" +
"\n" +
"# Librarians rule\n" +
"# Yes they do\n" +
"# But you knew that\n" +
"\n" +
"Some more text of dubious character. Here is a noisome string of CAPITAL letters. Here is something we want to _emphasize_.\n" +
"That was a linebreak. And something to indicate *strength*. Of course I could use <em>my own HTML tags</em> if I <strong>felt</strong> like it.\n" +
"\n" +
"h3. Coding\n" +
"\n" +
"This <code>is some code, \"isn't it\"</code>. Watch those quote marks! Now for some preformatted text:\n" +
"\n" +
"<pre>\n" +
"<code>\n" +
"\t$text = str_replace(\"<p>%::%</p>\",\"\",$text);\n" +
"\t$text = str_replace(\"%::%</p>\",\"\",$text);\n" +
"\t$text = str_replace(\"%::%\",\"\",$text);\n" +
"\n" +
"</code>\n" +
"</pre>\n" +
"\n" +
"This isn't code.\n" +
"\n" +
"\n" +
"So you see, my friends:\n" +
"\n" +
"* The time is now\n" +
"* The time is not later\n" +
"* The time is not yesterday\n" +
"* We must act\n" +
"\n";

String f2=        "\n<code>\n" +
        "\t$text = str_replace(\"<p>%::%</p>\",\"\",$text);\n" +
        "\t$text = str_replace(\"%::%</p>\",\"\",$text);\n" +
        "\t$text = str_replace(\"%::%\",\"\",$text);\n" +
        "\n" +
        "</code>\n" +
        "</pre>\n" +
        "\n" +
        "This isn't code.\n" +
        "\n" +
        "\n" +
        "So you see, my friends:\n" +
        "\n" +
        "* The time is now\n" +
        "* The time is not later\n" +
        "* The time is not yesterday\n" +
        "* We must act\n" ;



        Textile _textile = new Textile();
        System.out.println(_textile.process(f2));


    }
}

