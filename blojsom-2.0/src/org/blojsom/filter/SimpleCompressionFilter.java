package org.blojsom.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.util.BlojsomConstants;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

/**
 * Filter that compresses output with gzip (assuming that browser supports gzip).
 * <p></p>
 * Taken from More Servlets and JavaServer Pages
 * from Prentice Hall and Sun Microsystems Press,
 * <a href="http://www.moreservlets.com/">http://www.moreservlets.com/</a>.
 * &copy; 2002 Marty Hall; may be freely used or adapted.
 *
 * @author Marty Hall
 * @author David Czarnecki
 * @version $Id: SimpleCompressionFilter.java,v 1.1 2005-02-11 17:40:37 czarneckid Exp $
 * @since blojsom 2.24
 */
public class SimpleCompressionFilter implements Filter {

    private Log _logger = LogFactory.getLog(SimpleCompressionFilter.class);
    private FilterConfig config;

    /**
     * If browser does not support gzip, invoke resource
     * normally. If browser <i>does</i> support gzip,
     * set the Content-Encoding response header and
     * invoke resource with a wrapped response that
     * collects all the output. Extract the output
     * and write it into a gzipped byte array. Finally,
     * write that array to the client's output stream.
     */
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        if (!isGzipSupported(req)) {
            // Invoke resource normally.
            chain.doFilter(req, res);
        } else {
            _logger.debug("GZIP supported, compressing.");

            // Tell browser we are sending it gzipped data.
            res.setHeader("Content-Encoding", "gzip");

            // Invoke resource, accumulating output in the wrapper.
            CharArrayWrapper responseWrapper =
                    new CharArrayWrapper(res);
            chain.doFilter(req, responseWrapper);

            // Get character array representing output.
            char[] responseChars = responseWrapper.toCharArray();

            // Make a writer that compresses data and puts
            // it into a byte array.
            ByteArrayOutputStream byteStream =
                    new ByteArrayOutputStream();
            GZIPOutputStream zipOut =
                    new GZIPOutputStream(byteStream);
            OutputStreamWriter tempOut =
                    new OutputStreamWriter(zipOut, BlojsomConstants.UTF8);

            // Compress original output and put it into byte array.
            tempOut.write(responseChars);

            // Gzip streams must be explicitly closed.
            tempOut.close();

            // Update the Content-Length header.
            res.setContentLength(byteStream.size());

            // Send compressed result to client.
            OutputStream realOut = res.getOutputStream();
            byteStream.writeTo(realOut);
            byteStream.flush();
            byteStream.close();
        }
    }

    /**
     * Store the FilterConfig object in case subclasses want it.
     */
    public void init(FilterConfig config)
            throws ServletException {
        this.config = config;
    }

    /**
     * Retrieve the {@link FilterConfig} object
     *
     * @return {@link FilterConfig}
     */
    protected FilterConfig getFilterConfig() {
        return (config);
    }

    /**
     * Called when filter taken out of service
     */
    public void destroy() {
    }

    /**
     * Check to see if gzip is supported by the client
     *
     * @param req Request
     * @return <code>true</code> if the client supports gzip compression
     */
    private boolean isGzipSupported(HttpServletRequest req) {
        String browserEncodings =
                req.getHeader("Accept-Encoding");
        return ((browserEncodings != null) &&
                (browserEncodings.indexOf("gzip") != -1));
    }
}