package org.ignition.blojsom.plugin.textile;

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


        String[] replace= {
            "$1&#8217;$2"
        };


        String[] block_find = {
          "^\\s?\\*\\s(.*)",            //# bulleted list *
          "^\\s?#\\s(.*)",            //# numeric list #
          "^bq\\. (.*)",              //# blockquote bq.
          "^h(\\d)\\(([\\w]+)\\)\\.\\s(.*)",  //# header hn(class).  w/ css class
          "^h(\\d)\\. (.*)",            //# plain header hn.
          "^p\\(([[:alnum:]]+)\\)\\.\\s(.*)",   //# para p(class).  w/ css class
          "^p\\. (.*)",             //# plain paragraph
          "^([^\\t ]+.*)"           //# remaining plain paragraph
          };

        String[] block_replace = {
          "\t<liu>$1</liu>",
          "\t<lio>$1</lio>",
          "\t<blockquote>$1</blockquote>",
          "\t<h$1 class=\"$2\">$3</h$1>",
          "\t<h$1>$2</h$1>",
          "\t<p class=\"$1\">$2</p>",
          "\t<p>$1</p>",
          "\t<p>$1</p>"
          };


        String[] test_values = {
          "* welcome to the terror dome",
          "# welcome to the terror dome",
          "bq. welcome to the terror dome",
          "h1(image). welcome to the terror dome",
          "h1. welcome to the terror dome",
          "p(image). welcome to  the terrordome",
          "p. welcome to the terror dome",
          "welcome to where time stands still\nno one leaves and no one will"
          };



        //# some weird bs with underscores and \b word boundaries,
        //#  so we'll do those on their own


        //# small problem with double quotes at the end of a string

        for ( int x = 0 ; x < replace.length;x++ ) {
            System.out.println("mark's house".replaceAll(glyphs[x],replace[x]));
        }


//        for ( int x = 0 ; x < test_values.length;x++ ) {
//            System.out.println("old: " +test_values[x]);
//            System.out.println("new: " +test_values[x].replaceAll(block_find[x],block_replace[x]));
//            System.out.println("\n");
//        }



    }
}
