package org.ignition.blojsom.plugin.textile;


public interface TextileConstants {


    public static final String EXP_AMPERSAND = "&(?![#a-zA-Z0-9]+;)";
    public static final String EXP_AMPERSAND_REPLACE = "x%x%";


    public static final String EXP_DOUBLEQUOTE_MATCH = "(^|\\s)==(.*)==([^[:alnum:]]{0,2})(\\s|$)";
    public static final String EXP_DOUBLEQUOTE_REPLACE = "$1<notextile>$2</notextile>$3$4";

    public static final String EXP_IMAGE_QTAG_MATCH = "!([^\\s\\(=]+?)\\s?(\\(([^\\)]+?)\\))?!";
    public static final String EXP_IMAGE_QTAG_REPLACE = "<img src=\"$1\" alt=\"$3\" />";


    public static final String EXP_HREF_QTAG_MATCH = "\"([^\"\\(]+)\\s?(\\(([^\\)]+)\\))?\":(\\S+?)([^\\w\\s\\/;]|[1-9]*?)(\\s|$)";
    public static final String EXP_HREF_QTAG_REPLACE = "$1<a href=\\\"$4$5\\\" title=\\\"$3\\\">$2</a>$6";


    public static final String[] EXP_PHRASE_MODIFIER_SOURCETAGS = {"\\*\\*", "\\*", "\\?\\?", "-", "\\+", "~", "@"};
    public static final String[] EXP_PHRASE_MODIFIER_REPLACETAGS = {"b", "strong", "cite", "del", "ins", "sub", "code"};
    public static final String EXP_PHRASE_MODIFIER = "";

    public static final String EXP_ITALICS_MATCH = "(^|\\s)__(.*?)__([^\\w\\s]{0,2})(\\s|$)?";
    public static final String EXP_ITALICS_REPLACE = "$1<i>$2</i>$3";

    public static final String EXP_EMPHASIS_MATCH = "(^|\\s)_(.*?)_([^\\w\\s]{0,2})(\\s|$)?";
    public static final String EXP_EMPHASIS_REPLACE = "$1<em>$2</em>$3";

    public static final String EXP_SUPERSCRIPT_MATCH = "(^|\\s)\\^(.*?)\\^(\\s|$)?";
    public static final String EXP_SUPERSCRIPT_REPLACE = "$1<sup>$2</sup>$3";

    public static final String EXP_EOL_DBL_QUOTES = "\"$";


}
