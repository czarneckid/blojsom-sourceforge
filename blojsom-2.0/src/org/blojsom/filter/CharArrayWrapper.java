package org.blojsom.filter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.CharArrayWriter;
import java.io.PrintWriter;

/**
 * A response wrapper that takes everything the client
 * would normally output and saves it in one big
 * character array.
 * <p></p>
 * Taken from More Servlets and JavaServer Pages
 * from Prentice Hall and Sun Microsystems Press,
 * <a href="http://www.moreservlets.com/">http://www.moreservlets.com/</a>.
 * &copy; 2002 Marty Hall; may be freely used or adapted.
 *
 * @author Marty Hall
 * @author David Czarnecki
 * @version $Id: CharArrayWrapper.java,v 1.4 2005-02-11 17:40:25 czarneckid Exp $
 * @since blojsom 2.24  
 */
public class CharArrayWrapper extends HttpServletResponseWrapper {

    private CharArrayWriter charWriter;

    /**
     * Initializes wrapper.
     * <p></p>
     * First, this constructor calls the parent
     * constructor. That call is crucial so that the response
     * is stored and thus setHeader, setStatus, addCookie,
     * and so forth work normally.
     * <p></p>
     * Second, this constructor creates a CharArrayWriter
     * that will be used to accumulate the response.
     */
    public CharArrayWrapper(HttpServletResponse response) {
        super(response);
        charWriter = new CharArrayWriter();
    }

    /**
     * When servlets or JSP pages ask for the Writer,
     * don't give them the real one. Instead, give them
     * a version that writes into the character array.
     * The filter needs to send the contents of the
     * array to the client (perhaps after modifying it).
     */
    public PrintWriter getWriter() {
        return (new PrintWriter(charWriter));
    }

    /**
     * Get a String representation of the entire buffer.
     * <p></p>
     * Be sure <b>not</b> to call this method multiple times
     * on the same wrapper. The API for CharArrayWriter
     * does not guarantee that it "remembers" the previous
     * value, so the call is likely to make a new String
     * every time.
     */
    public String toString() {
        return (charWriter.toString());
    }

    /**
     * Get the underlying character array.
     */
    public char[] toCharArray() {
        return (charWriter.toCharArray());
    }
}