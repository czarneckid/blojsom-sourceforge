package org.ignition.blojsom.plugin.highlight;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.plugin.BlojsomPlugin;
import org.ignition.blojsom.plugin.BlojsomPluginException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Mark Lussier
 */

public class GoogleHighlightPlugin implements BlojsomPlugin {


    /**
     * HTTP Header for Referer Information
     */
    private static final String HEADER_REFERER = "referer";


    private static final String EXPRESSSION_GOOGLE = "^http:\\/\\/w?w?w?\\.?google.*";
    private static final String EXPRESSION_HTMLPREFIX = "(?<=>)([^<]+)?";
    private static final String EXPRESSION_HASTAGS = "<.+>";

    private static final String GOOGLE_QUERY = "^.*q=([^&]+)&?.*$";
    private static final String GOOGLE_CLEANQUOTES = "'/\'|\"/\"";

    private static final String HIGHLIGHT_PLAINTEXT = "<span class=\"searchhighlight\">$1</span>";
    private static final String HIGHLIGHT_HTML = "$1<span class=\"searchhighlight\">$2</span>";

    /**
     * Logger instance
     */
    private Log _logger = LogFactory.getLog(GoogleHighlightPlugin.class);


    public void init(ServletConfig servletConfig, Blog blog) throws BlojsomPluginException {

    }


    private String[] extractQueryTokens(String referer) {
        String[] result = null;
        Matcher matcher = Pattern.compile(GOOGLE_QUERY).matcher(referer);
        if (matcher.find()) {
            String _query = matcher.group(1);
            _query = _query.replaceAll(GOOGLE_CLEANQUOTES, "");
            StringTokenizer _st = new StringTokenizer(_query, "+, .", false);
            result = new String[_st.countTokens()];
            int cnt = 0;
            while (_st.hasMoreElements()) {
                result[cnt] = _st.nextToken();
                cnt += 1;
            }
        }

        return result;

    }

    public BlogEntry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                               Map context, BlogEntry[] entries) throws BlojsomPluginException {
        String referer = httpServletRequest.getHeader(HEADER_REFERER);

        if (referer != null && referer.matches(EXPRESSSION_GOOGLE)) {
            String[] searchwords = extractQueryTokens(referer);

            Pattern hasTags = Pattern.compile(EXPRESSION_HASTAGS);

            for (int x = 0; x < entries.length; x++) {
                BlogEntry entry = entries[x];
                Matcher matcher = hasTags.matcher(entry.getDescription());
                boolean isHtml = matcher.find();

                for (int y = 0; y < searchwords.length; y++) {
                    String word = searchwords[y];
                    if (isHtml) {
                        entry.setDescription(entry.getDescription().replaceAll("(\\b" + word + "\\b)", HIGHLIGHT_PLAINTEXT));
                    } else {
                        entry.setDescription(entry.getDescription().replaceAll(EXPRESSION_HTMLPREFIX + "(\\b" + word + "\\b)", HIGHLIGHT_HTML));
                    }

                }
            }
        }

        return entries;

    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws BlojsomPluginException If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws BlojsomPluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws BlojsomPluginException If there is an error in finalizing this plugin
     */
    public void destroy() throws BlojsomPluginException {
    }


}
