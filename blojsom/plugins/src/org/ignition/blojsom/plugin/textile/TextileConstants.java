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

/**
 * Textile Constants
 *
 * @author Mark Lussier
 * @version $Id: TextileConstants.java,v 1.2 2003-05-26 23:06:49 intabulas Exp $
 */
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
