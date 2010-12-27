package org.ignition.blojsom.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ignition.blojsom.blog.Blog;
import org.ignition.blojsom.blog.BlogEntry;
import org.ignition.blojsom.dispatcher.GenericDispatcher;
import org.ignition.blojsom.util.BlojsomConstants;
import org.ignition.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * BlojsomServlet
 *
 * @author David Czarnecki (czarneckid@acm.org)
 */
public class BlojsomServlet extends HttpServlet {

    private final static String BLOG_HOME_IP = "blog-home";
    private final static String BLOG_NAME_IP = "blog-name";
    private final static String BLOG_DEPTH_IP = "blog-directory-depth";
    private final static String BLOG_RELOAD_CHECK_IP = "blog-reload-check";
    private final static String BLOG_LANGUAGE_IP = "blog-language";
    private final static String BLOG_DESCRIPTION_IP = "blog-description";
    private final static String BLOG_URL_IP = "blog-url";
    private final static String BLOG_FILE_EXTENSIONS_IP = "blog-file-extensions";
    private final static String BLOG_FLAVOR_CONFIGURATION_IP = "blog-flavor-configuration";
    private final static String BLOG_TEMPLATE_MAP_PROPERTIES_IP = "template-map-properties";

    private final static String FLAVOR_PARAM = "flavor";

    private String _blogHome;
    private String _blogLanguage;
    private String _blogDescription;
    private String _blogName;
    private int _blogDepth;
    private String _blogURL;
    private String[] _blogFileExtensions;
    private TreeMap _blogEntryMap;
    private Blog _blog;
    private long _blogReloadCheck;

    private HashMap _flavorToTemplateMap;
    private HashMap _flavorToContentTypeMap;
    private HashMap _flavors;
    private HashMap _templateDispatchers;

    private Thread _blogUpdaterThread;

    private Log _logger = LogFactory.getLog(BlojsomServlet.class);

    /**
     * Create a new blojsom servlet instance
     */
    public BlojsomServlet() {
    }

    /**
     * Called when removing the servlet from the servlet container
     */
    public void destroy() {
        super.destroy();
    }

    /**
     * Configure the blog
     *
     * @param servletConfig Servlet configuration information
     */
    private void configureBlog(ServletConfig servletConfig) {
        _blogHome = servletConfig.getInitParameter(BLOG_HOME_IP);
        _blogLanguage = servletConfig.getInitParameter(BLOG_LANGUAGE_IP);
        _blogDescription = servletConfig.getInitParameter(BLOG_DESCRIPTION_IP);
        _blogName = servletConfig.getInitParameter(BLOG_NAME_IP);
        _blogDepth = Integer.parseInt(servletConfig.getInitParameter(BLOG_DEPTH_IP));
        _blogReloadCheck = Long.parseLong(servletConfig.getInitParameter(BLOG_RELOAD_CHECK_IP));
        _blogURL = servletConfig.getInitParameter(BLOG_URL_IP);
        if (!_blogURL.endsWith("/")) {
            _blogURL += "/";
        }
        _blogFileExtensions = BlojsomUtils.parseCommaList(servletConfig.getInitParameter(BLOG_FILE_EXTENSIONS_IP));
        _blog = new Blog(_blogName, _blogDescription, _blogURL, _blogLanguage);
        _blogEntryMap = new TreeMap();
    }

    /**
     * Configure the flavors for the blog which map flavor values like "html" and "rss" to
     * the proper template and content type
     *
     * @param servletConfig Servlet configuration information
     */
    private void configureFlavors(ServletConfig servletConfig) {
        _flavors = new HashMap();
        _flavorToTemplateMap = new HashMap();
        _flavorToContentTypeMap = new HashMap();

        String flavorConfiguration = servletConfig.getInitParameter(BLOG_FLAVOR_CONFIGURATION_IP);
        Properties flavorProperties = new Properties();
        InputStream is = servletConfig.getServletContext().getResourceAsStream(flavorConfiguration);
        try {
            flavorProperties.load(is);
            Iterator flavorIterator = flavorProperties.keySet().iterator();
            while (flavorIterator.hasNext()) {
                String flavor = (String) flavorIterator.next();
                String[] flavorMapping = BlojsomUtils.parseCommaList(flavorProperties.getProperty(flavor));
                _flavors.put(flavor, flavor);
                _flavorToTemplateMap.put(flavor, flavorMapping[0]);
                _flavorToContentTypeMap.put(flavor, flavorMapping[1]);
            }
        } catch (IOException e) {
            _logger.error(e);
        }
    }

    /**
     * Configure the dispatchers that blojsom will use when passing a request/response on to a
     * particular template
     *
     * @param servletConfig Servlet configuration information
     */
    private void configureDispatchers(ServletConfig servletConfig) {
        String templateConfiguration = servletConfig.getInitParameter(BLOG_TEMPLATE_MAP_PROPERTIES_IP);
        _templateDispatchers = new HashMap();
        Properties templateMapProperties = new Properties();
        InputStream is = servletConfig.getServletContext().getResourceAsStream(templateConfiguration);
        try {
            templateMapProperties.load(is);
            Iterator templateIterator = templateMapProperties.keySet().iterator();
            while (templateIterator.hasNext()) {
                String templateExtension = (String) templateIterator.next();
                String templateDispatcherClass = templateMapProperties.getProperty(templateExtension);
                Class dispatcherClass = Class.forName(templateDispatcherClass);
                GenericDispatcher dispatcher = (GenericDispatcher) dispatcherClass.newInstance();
                dispatcher.init(servletConfig);
                _templateDispatchers.put(templateExtension, dispatcher);
                _logger.debug("Added template dispatcher: " + templateDispatcherClass);
            }
        } catch (InstantiationException e) {
            _logger.error(e);
        } catch (IllegalAccessException e) {
            _logger.error(e);
        } catch (ClassNotFoundException e) {
            _logger.error(e);
        } catch (IOException e) {
            _logger.error(e);
        }
    }

    /**
     * Initialize blojsom: configure blog, configure flavors, configure dispatchers
     *
     * @param servletConfig Servlet configuration information
     * @throws ServletException If there is an error initializing blojsom
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        configureBlog(servletConfig);
        configureFlavors(servletConfig);
        configureDispatchers(servletConfig);

        _logger.info("blojsom home: " + _blogHome);
        if (_blogReloadCheck != -1) {
            _blogUpdaterThread = new Thread(new BlogUpdater());
            if (!_blogUpdaterThread.isAlive()) {
                _blogUpdaterThread.setDaemon(true);
                _blogUpdaterThread.start();
            }
        } else {
            _logger.info("blojsom: blog reloading disabled");
            recursiveBlogBuilder(-1, _blogHome);
        }
    }

    /**
     * Build an in-memory representation for the blog
     *
     * @param blogDepth Blog depth to recurse to; -1 indicates infinite depth
     * @param blogDirectory Current working directory for the blog
     */
    private void recursiveBlogBuilder(int blogDepth, String blogDirectory) {
        blogDepth++;
        _logger.debug("Working in directory: " + blogDirectory + " at blog depth: " + blogDepth);
        if (_blogDepth != BlojsomConstants.INFINITE_BLOG_DEPTH) {
            if (blogDepth == _blogDepth) {
                _logger.debug("Reached maximum blog depth: " + blogDepth);
                return;
            }
        }

        File blog = new File(blogDirectory);
        File[] directories = blog.listFiles(BlojsomUtils.getDirectoryFilter());

        String categoryKey = BlojsomUtils.getBlogCategory(_blogHome, blogDirectory);
        if (!categoryKey.endsWith("/")) {
            categoryKey += "/";
        }

        // look for blog entries in this directory
        File[] entries = blog.listFiles(BlojsomUtils.getExtensionsFilter(_blogFileExtensions));
        if (entries == null) {
            _logger.debug("No blog entries in blog directory: " + blogDirectory);
            _blogEntryMap.put(categoryKey, null);
        } else {
            TreeMap entryMap;
            if (_blogEntryMap.get(categoryKey) == null) {
                entryMap = new TreeMap(BlojsomUtils.FILE_TIME_COMPARATOR);
            } else {
                entryMap = (TreeMap) _blogEntryMap.get(categoryKey);
            }
            _logger.debug("Adding " + entries.length + " entries to the blog");
            for (int i = 0; i < entries.length; i++) {
                File entry = entries[i];
                BlogEntry blogEntry;
                if (!entryMap.containsKey(entry)) {
                    blogEntry = new BlogEntry();
                    blogEntry.setSource(entry);
                    blogEntry.setCategory(BlojsomUtils.removeInitialSlash(categoryKey));
                    blogEntry.setLink(_blogURL + BlojsomUtils.removeInitialSlash(categoryKey) + "?permalink=" + entry.getName());
                    blogEntry.reloadSource();
                    entryMap.put(entry, blogEntry);
                    _logger.debug("Adding initial blog entry: " + entry.toString() + " in blog category: " + categoryKey);
                } else {
                    blogEntry = (BlogEntry) entryMap.get(entry);
                    if (entry.lastModified() > blogEntry.getLastModified()) {
                        entryMap.remove(entry);
                        blogEntry = new BlogEntry();
                        blogEntry.setSource(entry);
                        blogEntry.setCategory(BlojsomUtils.removeInitialSlash(categoryKey));
                        blogEntry.setLink(_blogURL + BlojsomUtils.removeInitialSlash(categoryKey) + "?permalink=" + entry.getName());
                        blogEntry.reloadSource();
                        entryMap.put(entry, blogEntry);
                        _logger.debug("Blog entry updated on disk: " + entry.toString());
                    }
                }
            }
            _logger.debug("Added " + entryMap.size() + " entries to the blog");
            _blogEntryMap.put(categoryKey, entryMap);
        }

        if (directories == null) {
            return;
        } else {
            for (int i = 0; i < directories.length; i++) {
                File directory = directories[i];
                recursiveBlogBuilder(blogDepth, directory.toString());
            }
        }
    }

    /**
     * Removes any blog categories that have disappeared while the blog is running
     */
    private void cleanupBlogCategories() {
        Iterator categoryIterator = _blogEntryMap.keySet().iterator();
        ArrayList deletedCategories = new ArrayList();
        while (categoryIterator.hasNext()) {
            String category = (String) categoryIterator.next();
            File blogDirectory = new File(_blogHome + category);
            if (!blogDirectory.exists()) {
                _logger.debug("Blog directory deleted: " + blogDirectory.toString());
                deletedCategories.add(category);
            }
        }
        for (int i = 0; i < deletedCategories.size(); i++) {
            _blogEntryMap.remove(deletedCategories.get(i));
        }
    }

    /**
     * Service a request to blojsom
     *
     * @param httpServletRequest Request
     * @param httpServletResponse Response
     * @throws ServletException If there is an error processing the request
     * @throws IOException If there is an error in IO
     */
    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        // Determine user requested category
        String requestedCategory = httpServletRequest.getPathInfo();

        // Determine permalink
        String permalink = httpServletRequest.getParameter("permalink");
        if (permalink != null) {
            _logger.debug("Permalink request for: " + permalink);
        }

        if (requestedCategory == null) {
            requestedCategory = "/";
        } else if (!requestedCategory.endsWith("/")) {
            requestedCategory += "/";
        }
        _logger.debug("User requested category: " + requestedCategory);

        String flavor = httpServletRequest.getParameter(FLAVOR_PARAM);
        if (flavor == null) {
            flavor = BlojsomConstants.DEFAULT_FLAVOR_HTML;
        } else {
            if (_flavors.get(flavor) == null) {
                flavor = BlojsomConstants.DEFAULT_FLAVOR_HTML;
            }
        }

        TreeMap entriesForCategory = (TreeMap) _blogEntryMap.get(requestedCategory);
        if (entriesForCategory == null) {
            entriesForCategory = (TreeMap) _blogEntryMap.get("/");
        }

        HashMap context = new HashMap();
        context.put(BlojsomConstants.BLOJSOM_BLOG, _blog);
        BlogEntry[] entryArray;
        ArrayList entryList = new ArrayList();
        if (permalink == null) {
            entryArray = new BlogEntry[entriesForCategory.size()];
            Iterator entryIterator = entriesForCategory.keySet().iterator();
            while (entryIterator.hasNext()) {
                Object entryKey = entryIterator.next();
                if (entriesForCategory.get(entryKey) != null) {
                    entryList.add(entriesForCategory.get(entryKey));
                } else {
                    // Entry deleted from disk
                    entriesForCategory.remove(entryKey);
                }
            }
            entryArray = (BlogEntry[]) entryList.toArray(new BlogEntry[entryList.size()]);
        } else {
            boolean foundEntry = false;
            entryArray = new BlogEntry[1];
            _logger.debug("Permalink entry: " + requestedCategory + permalink);
            Iterator entryIterator = entriesForCategory.keySet().iterator();
            while (entryIterator.hasNext() && !foundEntry) {
                File entryKey = (File) entryIterator.next();
                if (entryKey.getName().endsWith(permalink)) {
                    foundEntry = true;
                    entryArray[0] = (BlogEntry) entriesForCategory.get(entryKey);
                    // Check if entry deleted from disk
                    if (entryArray[0] == null) {
                        entryArray = null;
                        entriesForCategory.remove(entryKey);
                    }
                }
            }
            if (foundEntry == false) {
                _logger.warn("Permalink entry: " + permalink + " gone");
                entryArray = null;
            }
        }
        context.put(BlojsomConstants.BLOJSOM_ENTRIES, entryArray);
        context.put(BlojsomConstants.BLOJSOM_CATEGORIES, BlojsomUtils.getBlogCategories(_blogURL, _blogEntryMap));

        // Forward the request on to the template for the requested flavor
        String flavorTemplate;
        if (_flavorToTemplateMap.get(flavor) == null) {
            flavorTemplate = (String) _flavorToTemplateMap.get(BlojsomConstants.DEFAULT_FLAVOR_HTML);
        } else {
            flavorTemplate = (String) _flavorToTemplateMap.get(flavor);
        }

        String flavorContentType = (String) _flavorToContentTypeMap.get(flavor);

        String templateExtension = BlojsomUtils.getFileExtension(flavorTemplate);
        _logger.debug("Template extension: " + templateExtension);

        GenericDispatcher dispatcher = (GenericDispatcher) _templateDispatchers.get(templateExtension);
        dispatcher.dispatch(httpServletRequest, httpServletResponse, context, flavorTemplate, flavorContentType);
    }

    /**
     * Updates the in-memory blog representation for blojsom
     */
    private class BlogUpdater implements Runnable {

        /**
         * Create a new BlogUpdater
         */
        public BlogUpdater() {
        }

        /**
         * Reloads the blog entries and cleans up the blog categories from disk
         */
        public void run() {
            while (true) {
                _logger.debug("Reloading blog from BlogUpdater");
                recursiveBlogBuilder(-1, _blogHome);
                cleanupBlogCategories();

                try {
                    Thread.sleep(_blogReloadCheck);
                } catch (InterruptedException e) {
                    _logger.error(e);
                    break;
                }
            }
        }
    }
}
