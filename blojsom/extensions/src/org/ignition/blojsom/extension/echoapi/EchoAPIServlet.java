package org.ignition.blojsom.extension.echoapi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.blog.BlojsomConfigurationException;
import org.ignition.blojsom.blog.BlogCategory;
import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.fetcher.BlojsomFetcher;
import org.ignition.blojsom.fetcher.BlojsomFetcherException;
import org.ignition.blojsom.util.BlojsomConstants;
import org.ignition.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * Implementation of J.C. Gregorio's EchoAPI
 * <a href="http://bitworking.org/rfc/draft-gregorio-03.html">http://bitworking.org/rfc/draft-gregorio-03.html</a>
 *
 * @author Mark Lussier
 */

public class EchoAPIServlet extends HttpServlet implements BlojsomConstants {

    private static final String FETCHER_PERMALINK = "FETCHER_PERMALINK";
    private static final String FETCHER_FLAVOR = "FETCHER_FLAVOR";
    private static final String FETCHER_NUM_POSTS_INTEGER = "FETCHER_NUM_POSTS_INTEGER";
    private static final String FETCHER_CATEGORY = "FETCHER_CATEGORY";


    private static final String BLOG_CONFIGURATION_IP = "blog-configuration";
    private static final String DEFAULT_BLOJSOM_CONFIGURATION = "/WEB-INF/blojsom.properties";

    private static final String HEADER_LOCATION = "Location";
    private static final String CONTENTTYPE_ECHO = "application/not-echo+xml";

    private Log _logger = LogFactory.getLog(EchoAPIServlet.class);

    protected Blog _blog = null;

    private BlojsomFetcher _fetcher;

    /**
     * Public Constructor
     */
    public EchoAPIServlet() {
    }

    /**
     * Configure the authorization table blog (user id's and and passwords)
     *
     * @param servletConfig Servlet configuration information
     */
    private void configureAuthorization(ServletConfig servletConfig) {
        Map _authorization = new HashMap();

        String authConfiguration = servletConfig.getInitParameter(BLOG_AUTHORIZATION_IP);
        Properties authProperties = new Properties();
        InputStream is = servletConfig.getServletContext().getResourceAsStream(authConfiguration);
        try {
            authProperties.load(is);
            is.close();
            Iterator authIterator = authProperties.keySet().iterator();
            while (authIterator.hasNext()) {
                String userid = (String) authIterator.next();
                String password = authProperties.getProperty(userid);
                _authorization.put(userid, password);
            }

            if (!_blog.setAuthorization(_authorization)) {
                _logger.error("Authorization table could not be assigned");
            }

        } catch (IOException e) {
            _logger.error(e);
        }
    }

    /**
     * Load blojsom configuration information
     *
     * @param context Servlet context
     * @param filename blojsom configuration file to be loaded
     */
    public void processBlojsomCongfiguration(ServletContext context, String filename) {
        Properties _configuration = new Properties();
        InputStream _cis = context.getResourceAsStream(filename);

        try {
            _configuration.load(_cis);
            _cis.close();
            _blog = new Blog(_configuration);
        } catch (IOException e) {
            _logger.error(e);
        } catch (BlojsomConfigurationException e) {
            _logger.error(e);
        }
    }


    /**
     * Configure the {@link org.ignition.blojsom.fetcher.BlojsomFetcher} that will be used to fetch categories and
     * entries
     *
     * @param servletConfig Servlet configuration information
     * @throws ServletException If the {@link org.ignition.blojsom.fetcher.BlojsomFetcher} class could not be loaded and/or initialized
     */
    private void configureFetcher(ServletConfig servletConfig) throws ServletException {
        String fetcherClassName = _blog.getBlogFetcher();
        if ((fetcherClassName == null) || "".equals(fetcherClassName)) {
            fetcherClassName = BLOG_DEFAULT_FETCHER;
        }

        try {
            Class fetcherClass = Class.forName(fetcherClassName);
            _fetcher = (BlojsomFetcher) fetcherClass.newInstance();
            _fetcher.init(servletConfig, _blog);
            _logger.info("Added blojsom fetcher: " + fetcherClassName);
        } catch (ClassNotFoundException e) {
            _logger.error(e);
            throw new ServletException(e);
        } catch (InstantiationException e) {
            _logger.error(e);
            throw new ServletException(e);
        } catch (IllegalAccessException e) {
            _logger.error(e);
            throw new ServletException(e);
        } catch (BlojsomFetcherException e) {
            _logger.error(e);
            throw new ServletException(e);
        }
    }


    /**
     * Initialize the blojsom EchoAPI servlet
     *
     * @param servletConfig Servlet configuration information
     * @throws javax.servlet.ServletException If there is an error initializing the servlet
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        String _cfgfile = servletConfig.getInitParameter(BLOG_CONFIGURATION_IP);

        if (_cfgfile == null || _cfgfile.equals("")) {
            _logger.info("blojsom configuration not specified, using " + DEFAULT_BLOJSOM_CONFIGURATION);
            _cfgfile = DEFAULT_BLOJSOM_CONFIGURATION;
        }

        processBlojsomCongfiguration(servletConfig.getServletContext(), _cfgfile);
        processBlojsomCongfiguration(servletConfig.getServletContext(), _cfgfile);
        configureAuthorization(servletConfig);
        configureFetcher(servletConfig);

        _logger.info("EchoAPI initialized, home is [" + _blog.getBlogHome() + "]");

    }


    /**
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    protected void doDelete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {

        _logger.info("EchoAPI Delete Called =====[ SUPPORTED ]=====");
        _logger.info("       Path: " + httpServletRequest.getPathInfo());

    }


    /**
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {


        _logger.info("EchoAPI GET Called =====[ SUPPORTED ]=====");
        _logger.info("       Path: " + httpServletRequest.getPathInfo());

        // NOTE: Assumes that the getPathInfo() returns only category data

        String permalink = BlojsomUtils.getRequestValue(PERMALINK_PARAM, httpServletRequest);
        String category = BlojsomUtils.normalize(httpServletRequest.getPathInfo());

        Map fetchMap = new HashMap();
        BlogCategory blogCategory = _fetcher.newBlogCategory();
        blogCategory.setCategory(category);
        blogCategory.setCategoryURL(_blog.getBlogURL() + category);
        fetchMap.put(FETCHER_CATEGORY, blogCategory);
        fetchMap.put(FETCHER_PERMALINK, permalink);
        try {
            BlogEntry[] _entries = _fetcher.fetchEntries(fetchMap);

            if (_entries != null && _entries.length > 0) {
                BlogEntry entry = _entries[0];
                // EchoEntry converts a BlogEntry to an Echo Entry XML stream..
                // VERY messy right now and it will be refactored to be bidi
                EchoEntry echo = new EchoEntry(_blog, entry);
                String content = echo.getAsString();
                httpServletResponse.setContentType(CONTENTTYPE_ECHO);
                httpServletResponse.setStatus(200);
                httpServletResponse.setContentLength(content.length());
                OutputStreamWriter osw = new OutputStreamWriter(httpServletResponse.getOutputStream(), "UTF-8");
                osw.write(content);
                osw.flush();

            }
        } catch (BlojsomFetcherException e) {
            _logger.error(e);
            httpServletResponse.setStatus(404);
        }


    }

    /**
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {

        _logger.info("EchoAPI POST Called =====[ SUPPORTED ]=====");
        _logger.info("       Path: " + httpServletRequest.getPathInfo());

    }

    /**
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    protected void doPut(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {

        _logger.info("EchoAPI PUT Called =====[ SUPPORTED ]=====");
        _logger.info("       Path: " + httpServletRequest.getPathInfo());
    }


    /**
     * Called when removing the servlet from the servlet container
     */
    public void destroy() {
        try {
            _fetcher.destroy();
        } catch (BlojsomFetcherException e) {
            _logger.error(e);
        }

    }

}

