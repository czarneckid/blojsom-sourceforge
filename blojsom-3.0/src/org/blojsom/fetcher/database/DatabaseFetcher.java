/**
 * Copyright (c) 2003-2006, David A. Czarnecki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *     following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *     following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of "David A. Czarnecki" and "blojsom" nor the names of its contributors may be used to
 *     endorse or promote products derived from this software without specific prior written permission.
 * Products derived from this software may not be called "blojsom", nor may "blojsom" appear in their name,
 *     without prior written permission of David A. Czarnecki.
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
package org.blojsom.fetcher.database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.*;
import org.blojsom.blog.database.*;
import org.blojsom.event.Event;
import org.blojsom.event.EventBroadcaster;
import org.blojsom.event.Listener;
import org.blojsom.fetcher.Fetcher;
import org.blojsom.fetcher.FetcherException;
import org.blojsom.util.BlojsomConstants;
import org.blojsom.util.BlojsomUtils;
import org.hibernate.*;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Database fetcher
 *
 * @author David Czarnecki
 * @version $Id: DatabaseFetcher.java,v 1.13 2006-04-05 00:40:53 czarneckid Exp $
 * @since blojsom 3.0
 */
public class DatabaseFetcher implements Fetcher, Listener {

    protected Log _logger = LogFactory.getLog(DatabaseFetcher.class);

    protected ServletConfig _servletConfig;
    protected EventBroadcaster _eventBroadcaster;
    protected SessionFactory _sessionFactory;
    protected Properties _blojsomProperties;

    /**
     * Create a new instance of the database fetcher
     */
    public DatabaseFetcher() {
    }

    /**
     * Set the {@link ServletConfig} for the fetcher to grab initialization parameters
     *
     * @param servletConfig {@link ServletConfig}
     */
    public void setServletConfig(ServletConfig servletConfig) {
        _servletConfig = servletConfig;
    }

    /**
     * Set the {@link EventBroadcaster} event broadcaster
     *
     * @param eventBroadcaster {@link EventBroadcaster}
     */
    public void setEventBroadcaster(EventBroadcaster eventBroadcaster) {
        _eventBroadcaster = eventBroadcaster;
    }

    /**
     * Set the {@link SessionFactory}
     *
     * @param sessionFactory {@link SessionFactory}
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        _sessionFactory = sessionFactory;
    }

    /**
     * Set the default blojsom properties
     *
     * @param blojsomProperties Default blojsom properties
     */
    public void setBlojsomProperties(Properties blojsomProperties) {
        _blojsomProperties = blojsomProperties;
    }

    /**
     * Initialize this fetcher. This method only called when the fetcher is instantiated.
     *
     * @throws org.blojsom.fetcher.FetcherException
     *          If there is an error initializing the fetcher
     */
    public void init() throws FetcherException {
        _eventBroadcaster.addListener(this);

        if (_logger.isDebugEnabled()) {
            _logger.debug("Initialized database fetcher");
        }
    }

    /**
     * Return a new {@link org.blojsom.blog.Entry} instance
     *
     * @return Blog entry instance
     */
    public Entry newEntry() {
        return new DatabaseEntry();
    }

    /**
     * Return a new {@link org.blojsom.blog.Comment} instance
     *
     * @return {@link org.blojsom.blog.Comment}
     */
    public Comment newComment() {
        return new DatabaseComment();
    }

    /**
     * Return a new {@link org.blojsom.blog.Trackback} instance
     *
     * @return {@link org.blojsom.blog.Trackback}
     */
    public Trackback newTrackback() {
        return new DatabaseTrackback();
    }

    /**
     * Return a new {@link org.blojsom.blog.Pingback} instance
     *
     * @return {@link org.blojsom.blog.Pingback}
     */
    public Pingback newPingback() {
        return new DatabasePingback();
    }

    /**
     * Return a new {@link org.blojsom.blog.Category} instance
     *
     * @return {@link org.blojsom.blog.Category}
     */
    public Category newCategory() {
        return new DatabaseCategory();
    }

    /**
     * Return a new {@link org.blojsom.blog.Blog} instance
     *
     * @return {@link org.blojsom.blog.Blog}
     */
    public Blog newBlog() {
        return new DatabaseBlog();
    }

    /**
     * Return a new {@link org.blojsom.blog.User} instance
     *
     * @return {@link org.blojsom.blog.User}
     */
    public User newUser() {
        return new DatabaseUser();
    }

    /**
     * Load the {@link Blog} given the blog ID
     *
     * @param blogId Blog ID
     * @return {@link Blog}
     * @throws FetcherException If there is an error loading the blog
     */
    public Blog loadBlog(String blogId) throws FetcherException {
        Session session = _sessionFactory.openSession();
        Transaction tx = null;

        Blog blog = null;

        try {
            tx = session.beginTransaction();
            blog = (Blog) session.load(DatabaseBlog.class, blogId);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }

            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        } finally {
            session.close();
        }

        return blog;
    }

    /**
     * Save a {@link Blog}
     *
     * @param blog {@link Blog}
     * @throws FetcherException If there is an error saving the blog
     */
    public void saveBlog(Blog blog) throws FetcherException {
        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            session.saveOrUpdate(blog);

            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException("Unable to save blog inforamtion: " + blog.getBlogId(), e);
        }
    }

    /**
     * Delete a blog
     *
     * @param blog {@link Blog}
     * @throws FetcherException If there is an error deleting the blog
     */
    public void deleteBlog(Blog blog) throws FetcherException {
        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            session.delete(blog);

            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException("Unable to delete blog inforamtion: " + blog.getBlogId(), e);
        }
    }

    /**
     * Load the blog IDs
     *
     * @return List of blog IDs
     * @throws org.blojsom.fetcher.FetcherException
     *          If there is an error loading the blog IDs
     */
    public String[] loadBlogIDs() throws FetcherException {
        String[] blogIDs;

        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            SQLQuery sqlQuery = session.createSQLQuery("select blog_id from blog");
            List blogIDList = sqlQuery.list();

            tx.commit();
            session.close();

            blogIDs = (String[]) blogIDList.toArray(new String[blogIDList.size()]);
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException("Unable to load blog IDs", e);
        }

        return blogIDs;
    }

    /**
     * Fetch a set of {@link org.blojsom.blog.Entry} objects.
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param blog                {@link org.blojsom.blog.Blog} instance
     * @param flavor              Flavor
     * @param context             Context
     * @return Blog entries retrieved for the particular request
     * @throws org.blojsom.fetcher.FetcherException
     *          If there is an error retrieving the blog entries for the request
     */
    public Entry[] fetchEntries(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, String flavor, Map context) throws FetcherException {
        Category category = (Category) context.get(BlojsomConstants.BLOJSOM_REQUESTED_CATEGORY);

        String ignoreFlavors = _blojsomProperties.getProperty("ignore-flavors");
        // Check to see if the requested flavor should be ignored
        if (ignoreFlavors.indexOf(flavor) != -1) {
            return new Entry[0];
        }

        // Determine if a permalink has been requested
        String permalink = httpServletRequest.getParameter(BlojsomConstants.PERMALINK_PARAM);
        if (permalink != null) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("Permalink request for: " + permalink);
            }
        }

        if (permalink != null) {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            Criteria permalinkCriteria = session.createCriteria(org.blojsom.blog.database.DatabaseEntry.class);
            permalinkCriteria.add(Restrictions.eq("blogId", blog.getBlogId()))
                    .add(Restrictions.eq("postSlug", BlojsomUtils.removeSlashes(permalink)));

            List permalinkEntryList = permalinkCriteria.list();

            if (permalinkEntryList.size() == 1) {
                DatabaseEntry entry = (DatabaseEntry) permalinkEntryList.get(0);
                context.put(BlojsomConstants.BLOJSOM_PERMALINK, entry.getId());

                permalinkCriteria = session.createCriteria(org.blojsom.blog.database.DatabaseEntry.class);
                permalinkCriteria.add(Restrictions.eq("blogId", blog.getBlogId()))
                        .add(Restrictions.gt("date", entry.getDate()))
                        .add(Restrictions.lt("date", new Date()));
                permalinkCriteria.addOrder(Order.desc("date"));
                permalinkCriteria.setMaxResults(1);

                List nextList = permalinkCriteria.list();
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Total entries after permalink: " + nextList.size());
                }

                if (nextList.size() == 1) {
                    context.put(BlojsomConstants.BLOJSOM_PERMALINK_NEXT_ENTRY, nextList.get(0));
                } else {
                    context.put(BlojsomConstants.BLOJSOM_PERMALINK_NEXT_ENTRY, null);
                }

                permalinkCriteria = session.createCriteria(org.blojsom.blog.database.DatabaseEntry.class);
                permalinkCriteria.add(Restrictions.eq("blogId", blog.getBlogId()))
                        .add(Restrictions.lt("date", entry.getDate()));
                permalinkCriteria.addOrder(Order.desc("date"));
                permalinkCriteria.setMaxResults(1);

                List prevList = permalinkCriteria.list();
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Total entries before permalink: " + prevList.size());
                }

                if (prevList.size() == 1) {
                    context.put(BlojsomConstants.BLOJSOM_PERMALINK_PREVIOUS_ENTRY, prevList.get(0));
                } else {
                    context.put(BlojsomConstants.BLOJSOM_PERMALINK_PREVIOUS_ENTRY, null);
                }
            }

            tx.commit();
            session.close();

            if (permalinkEntryList.size() > 0) {
                return new Entry[]{(Entry) permalinkEntryList.get(0)};
            } else {
                return (Entry[]) permalinkEntryList.toArray(new DatabaseEntry[permalinkEntryList.size()]);
            }
        } else {
            String pgNum = BlojsomUtils.getRequestValue(BlojsomConstants.PAGE_NUMBER_PARAM, httpServletRequest);
            int page;
            try {
                page = Integer.parseInt(pgNum);
                page -= 1;
                if (page < 0) {
                    page = 0;
                }
            } catch (NumberFormatException e) {
                page = 0;
            }

            page *= blog.getBlogDisplayEntries();

            if (category != null && !"/".equals(category.getName())) {
                Session session = _sessionFactory.openSession();
                Transaction tx = session.beginTransaction();

                Criteria entryCriteria = session.createCriteria(org.blojsom.blog.database.DatabaseEntry.class);
                entryCriteria.add(Restrictions.eq("blogId", blog.getBlogId()));
                entryCriteria.add(Restrictions.eq("blogCategoryId", category.getId()));
                entryCriteria.add(Restrictions.lt("date", new Date()));
                entryCriteria.addOrder(Order.desc("date"));
                entryCriteria.setMaxResults(blog.getBlogDisplayEntries());
                entryCriteria.setFirstResult(page);

                List entryList = entryCriteria.list();

                tx.commit();
                session.close();

                return (DatabaseEntry[]) entryList.toArray(new DatabaseEntry[entryList.size()]);
            } else {
                Session session = _sessionFactory.openSession();
                Transaction tx = session.beginTransaction();

                Criteria entryCriteria = session.createCriteria(org.blojsom.blog.database.DatabaseEntry.class);
                entryCriteria.add(Restrictions.eq("blogId", blog.getBlogId()));
                entryCriteria.add(Restrictions.lt("date", new Date()));
                entryCriteria.addOrder(Order.desc("date"));
                entryCriteria.setMaxResults(blog.getBlogDisplayEntries());
                entryCriteria.setFirstResult(page);

                List entryList = entryCriteria.list();

                tx.commit();
                session.close();

                return (DatabaseEntry[]) entryList.toArray(new DatabaseEntry[entryList.size()]);
            }
        }
    }

    /**
     * Load all the entries for a given category
     *
     * @param blog {@link Blog}
     * @param categoryId Category ID
     * @return Blog entries for a given category
     * @throws FetcherException If there is an error loading the entries
     */
    public Entry[] loadAllEntriesForCategory(Blog blog, Integer categoryId) throws FetcherException {
        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            Criteria entryCriteria = session.createCriteria(DatabaseEntry.class);
            entryCriteria.add(Restrictions.eq("blogId", blog.getBlogId()));
            entryCriteria.add(Restrictions.eq("blogCategoryId", categoryId));
            entryCriteria.addOrder(Order.desc("date"));

            List entryList = entryCriteria.list();

            tx.commit();
            session.close();

            return (DatabaseEntry[]) entryList.toArray(new DatabaseEntry[entryList.size()]);
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Load all the entries for a given category
     *
     * @param blog {@link Blog}
     * @param categoryId Category ID
     * @param limit Limit on number of entries to return
     * @return Blog entries for a given category
     * @throws FetcherException If there is an error loading the entries
     */
    public Entry[] loadEntriesForCategory(Blog blog, Integer categoryId, Integer limit) throws FetcherException {
        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            Criteria entryCriteria = session.createCriteria(DatabaseEntry.class);
            entryCriteria.add(Restrictions.eq("blogId", blog.getBlogId()));
            entryCriteria.add(Restrictions.eq("blogCategoryId", categoryId));
            entryCriteria.addOrder(Order.desc("date"));
            entryCriteria.setMaxResults(limit.intValue());

            List entryList = entryCriteria.list();

            tx.commit();
            session.close();

            return (DatabaseEntry[]) entryList.toArray(new DatabaseEntry[entryList.size()]);
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Load an {@link Entry} for a given entry ID
     *
     * @param blog {@link Blog}
     * @param entryId Entry ID
     * @return {@link Entry}
     * @throws FetcherException If there is an error loading the entry
     */
    public Entry loadEntry(Blog blog, Integer entryId) throws FetcherException {
        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            Criteria entryCriteria = session.createCriteria(DatabaseEntry.class);
            entryCriteria.add(Restrictions.eq("blogId", blog.getBlogId()))
                    .add(Restrictions.eq("id", entryId));

            Entry entry = (DatabaseEntry) entryCriteria.uniqueResult();

            tx.commit();
            session.close();

            return entry;
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Load an {@link Entry} given a post slug
     *
     * @param blog     {@link Blog}
     * @param postSlug Post slug
     * @return {@link Entry} for the given post slug
     * @throws org.blojsom.fetcher.FetcherException
     *          If an entry for the blog and post slug cannot be found
     */
    public Entry loadEntry(Blog blog, String postSlug) throws FetcherException {
        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            Criteria entryCriteria = session.createCriteria(DatabaseEntry.class);
            entryCriteria.add(Restrictions.eq("blogId", blog.getBlogId()))
                    .add(Restrictions.eq("postSlug", postSlug));

            Entry entry = (DatabaseEntry) entryCriteria.uniqueResult();

            tx.commit();
            session.close();

            if (entry == null) {
                throw new FetcherException("Entry could not be loaded with post slug: " + postSlug);
            }

            return entry;
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Determine the blog category based on the request
     *
     * @param httpServletRequest Request
     * @return {@link BlogCategory} of the requested category
     */
    protected String getBlogCategory(Blog blog, HttpServletRequest httpServletRequest) {
        // Determine the user requested category
        String requestedCategory;
        httpServletRequest.getPathInfo();
        String blogIdFromPath = BlojsomUtils.getBlogFromPath(httpServletRequest.getPathInfo());
        if (blogIdFromPath == null) {
            requestedCategory = httpServletRequest.getPathInfo();
        } else {
            if (blogIdFromPath.equals(blog.getBlogId())) {
                requestedCategory = BlojsomUtils.getCategoryFromPath(httpServletRequest.getPathInfo());
            } else {
                requestedCategory = httpServletRequest.getPathInfo();
            }
        }

        requestedCategory = BlojsomUtils.normalize(requestedCategory);
        if (_logger.isDebugEnabled()) {
            _logger.debug("blojsom path info: " + requestedCategory);
        }

        String categoryParameter = httpServletRequest.getParameter(BlojsomConstants.CATEGORY_PARAM);
        if (!(categoryParameter == null) && !("".equals(categoryParameter))) {
            categoryParameter = BlojsomUtils.normalize(categoryParameter);
            if (_logger.isDebugEnabled()) {
                _logger.debug("Category parameter override: " + categoryParameter);
            }
            requestedCategory = categoryParameter;
        }

        if (requestedCategory == null) {
            requestedCategory = "/";
        } else if (!requestedCategory.endsWith("/")) {
            requestedCategory += "/";
        }

        requestedCategory = BlojsomUtils.urlDecode(requestedCategory);
        if (_logger.isDebugEnabled()) {
            _logger.debug("User requested category: " + requestedCategory);
        }

        return requestedCategory;
    }

    /**
     * Fetch a set of {@link org.blojsom.blog.Category} objects
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param blog                {@link org.blojsom.blog.Blog} instance
     * @param flavor              Flavor
     * @param context             Context
     * @return Blog categories retrieved for the particular request
     * @throws org.blojsom.fetcher.FetcherException
     *          If there is an error retrieving the blog categories for the request
     */
    public Category[] fetchCategories(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Blog blog, String flavor, Map context) throws FetcherException {
        Category[] allCategoriesArray;

        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            // Get the requested category and put it in the context
            String requestedCategory = getBlogCategory(blog, httpServletRequest);
            Criteria requestedCategoryCriteria = session.createCriteria(DatabaseCategory.class);
            requestedCategoryCriteria.add(Restrictions.and(Restrictions.eq("name", BlojsomUtils.addSlashes(requestedCategory)), Restrictions.eq("blogId", blog.getBlogId())));
            DatabaseCategory dbCategory = (DatabaseCategory) requestedCategoryCriteria.uniqueResult();

            if (dbCategory != null) {
                context.put(BlojsomConstants.BLOJSOM_REQUESTED_CATEGORY, dbCategory);
            }

            // Get all categories and put them in the context
            Criteria categoryCriteria = session.createCriteria(DatabaseCategory.class);
            categoryCriteria.add(Restrictions.eq("blogId", blog.getBlogId()));
            categoryCriteria.addOrder(Order.asc("name"));

            List allCategories = categoryCriteria.list();
            allCategoriesArray = (DatabaseCategory[]) allCategories.toArray(new DatabaseCategory[allCategories.size()]);

            context.put(BlojsomConstants.BLOJSOM_ALL_CATEGORIES, allCategoriesArray);

            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }

        return allCategoriesArray;
    }

    /**
     * Load each {@link Category} for a given blog
     *
     * @param blog {@link Blog}
     * @return {@link Category} list for the blog
     * @throws FetcherException If there is an error loading the categories
     */
    public Category[] loadAllCategories(Blog blog) throws FetcherException {
        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            Criteria categoryCriteria = session.createCriteria(DatabaseCategory.class);
            categoryCriteria.add(Restrictions.eq("blogId", blog.getBlogId()));
            categoryCriteria.addOrder(Order.asc("name"));

            List allCategories = categoryCriteria.list();

            tx.commit();
            session.close();

            return (DatabaseCategory[]) allCategories.toArray(new DatabaseCategory[allCategories.size()]);
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Load the {@link Category} for a given category ID
     *
     * @param blog {@link Blog}
     * @param categoryId Category ID
     * @return {@link Category} for the given category ID
     * @throws FetcherException If there is an error loading the category
     */
    public Category loadCategory(Blog blog, Integer categoryId) throws FetcherException {
        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            Criteria categoryCriteria = session.createCriteria(DatabaseCategory.class);
            categoryCriteria.add(Restrictions.eq("blogId", blog.getBlogId()))
                    .add(Restrictions.eq("id", categoryId));

            Category category = (DatabaseCategory) categoryCriteria.uniqueResult();

            tx.commit();
            session.close();

            return category;
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Create a unique post slug
     *
     * @param blog {@link Blog}
     * @param entry {@link Entry}
     * @return Unique post slug
     */
    protected String createPostSlug(Blog blog, Entry entry) {
        String postSlug;

        if (BlojsomUtils.checkNullOrBlank(entry.getPostSlug())) {
            postSlug = BlojsomUtils.getPostSlug(entry.getTitle(), entry.getDescription());
        } else {
            postSlug = entry.getPostSlug();
        }

        int postSlugTag = 1;
        boolean postSlugOK = false;

        while (!postSlugOK) {
            try {
                loadEntry(blog, postSlug);
                postSlug += ("-" + postSlugTag++);
            } catch (FetcherException e) {
                postSlugOK = true;
            }
        }

        return postSlug;
    }

    /**
     * Save a given {@link Entry}
     *
     * @param blog {@link Blog}
     * @param entry {@link Entry} to save
     * @throws FetcherException If there is an error saving the entry
     */
    public void saveEntry(Blog blog, Entry entry) throws FetcherException {
        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            if (entry.getBlogId() == null) {
                entry.setBlogId(blog.getBlogId());
            }

            if (entry.getDate() == null) {
                entry.setDate(new Date());
            }

            if (entry.getModifiedDate() == null) {
                entry.setModifiedDate(entry.getDate());
            }

            if (entry.getId() == null) {
                entry.setPostSlug(createPostSlug(blog, entry));
                session.save(entry);
            } else {
                session.update(entry);
            }

            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Load a given {@link Entry}
     *
     * @param blog {@link Blog}
     * @param entry {@link Entry} to load
     * @throws FetcherException If there is an error loading the entry
     */
    public void loadEntry(Blog blog, Entry entry) throws FetcherException {
        if (entry.getId() == null) {
            throw new FetcherException("No ID associated with this entry");
        }

        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            session.load(entry, entry.getId());

            tx.commit();
            session.close();

            if (!blog.getBlogId().equals(entry.getBlogId())) {
                throw new FetcherException("Entry blog ID not associated with blog ID from call");
            }
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Delete a given {@link Entry}
     *
     * @param blog {@link Blog}
     * @param entry {@link Entry} to delete
     * @throws FetcherException If there is an error deleting the entry
     */
    public void deleteEntry(Blog blog, Entry entry) throws FetcherException {
        if (entry.getId() == null) {
            throw new FetcherException("No ID associated with this entry");
        }

        if (!blog.getBlogId().equals(entry.getBlogId())) {
            throw new FetcherException("Entry blog ID not associated with blog ID from call");
        }

        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            session.delete(entry);

            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Save a given {@link Category}
     *
     * @param blog {@link Blog}
     * @param category {@link Category} to save
     * @throws FetcherException If there is an error saving the category
     */
    public void saveCategory(Blog blog, Category category) throws FetcherException {
        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            if (category.getId() == null) {
                session.save(category);
            } else {
                session.update(category);
            }

            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Load a given {@link Category}
     *
     * @param blog {@link Blog}
     * @param category {@link Category} to load
     * @throws FetcherException If there is an loading saving the category
     */
    public void loadCategory(Blog blog, Category category) throws FetcherException {
        if (category.getId() == null) {
            throw new FetcherException("No ID associated with this category");
        }

        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            session.load(category, category.getId());

            tx.commit();
            session.close();

            if (!blog.getBlogId().equals(category.getBlogId())) {
                throw new FetcherException("Category blog ID not associated with blog ID from call");
            }
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Delete a given {@link Category}
     *
     * @param blog {@link Blog}
     * @param category {@link Category} to delete
     * @throws FetcherException If there is an error deleting the category
     */
    public void deleteCategory(Blog blog, Category category) throws FetcherException {
        if (category.getId() == null) {
            throw new FetcherException("No ID associated with this category");
        }

        if (!blog.getBlogId().equals(category.getBlogId())) {
            throw new FetcherException("Category blog ID not associated with blog ID from call");
        }

        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            session.delete(category);

            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Save a given {@link Comment}
     *
     * @param blog {@link Blog}
     * @param comment {@link Comment} to save
     * @throws FetcherException If there is an error saving the comment
     */
    public void saveComment(Blog blog, Comment comment) throws FetcherException {
        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            if (comment.getId() == null) {
                session.save(comment);
            } else {
                session.update(comment);
            }

            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Load a given {@link Comment}
     *
     * @param blog {@link Blog}
     * @param comment {@link Comment} to load
     * @throws FetcherException If there is an error loading the comment
     */
    public void loadComment(Blog blog, Comment comment) throws FetcherException {
        if (comment.getId() == null) {
            throw new FetcherException("No ID associated with this comment");
        }

        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            session.load(comment, comment.getId());

            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Delete a given {@link Comment}
     *
     * @param blog {@link Blog}
     * @param comment {@link Comment} to delete
     * @throws FetcherException If there is an error deleting the comment
     */
    public void deleteComment(Blog blog, Comment comment) throws FetcherException {
        if (comment.getId() == null) {
            throw new FetcherException("No ID associated with this comment");
        }

        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            session.delete(comment);

            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Load the recent comments for a blog
     *
     * @param blog  {@link Blog}
     * @throws FetcherException If there is an error retrieving the recent comments
     */
    public List loadRecentComments(Blog blog) throws FetcherException {
        List recentComments;

        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            Criteria commentsCriteria = session.createCriteria(org.blojsom.blog.database.DatabaseComment.class);
            commentsCriteria.add(Restrictions.eq("blogId", blog.getBlogId()))
                    .add(Restrictions.eq("status", "approved"))
                    .addOrder(Order.desc("commentDate"));

            String recentCommentsCount = blog.getProperty(BlojsomConstants.RECENT_COMMENTS_COUNT);
            int count;
            try {
                count = Integer.parseInt(recentCommentsCount);
            } catch (NumberFormatException e) {
                count = BlojsomConstants.DEFAULT_RECENT_COMMENTS_COUNT;
            }

            if (count > 0) {
                commentsCriteria.setMaxResults(count);
            }

            recentComments = commentsCriteria.list();

            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }

        return recentComments;
    }

    /**
     * Save a given {@link Trackback}
     *
     * @param blog {@link Blog}
     * @param trackback {@link Trackback} to save
     * @throws FetcherException If there is an error saving the trackback
     */
    public void saveTrackback(Blog blog, Trackback trackback) throws FetcherException {
        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            if (trackback.getId() == null) {
                session.save(trackback);
            } else {
                session.update(trackback);
            }

            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Load a given {@link Trackback}
     *
     * @param blog {@link Blog}
     * @param trackback {@link Trackback} to load
     * @throws FetcherException If there is an error loading the trackback
     */
    public void loadTrackback(Blog blog, Trackback trackback) throws FetcherException {
        if (trackback.getId() == null) {
            throw new FetcherException("No ID associated with this trackback");
        }

        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            session.load(trackback, trackback.getId());

            tx.commit();

            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Delete a given {@link Trackback}
     *
     * @param blog {@link Blog}
     * @param trackback {@link Trackback} to delete
     * @throws FetcherException If there is an error deleting the trackback
     */
    public void deleteTrackback(Blog blog, Trackback trackback) throws FetcherException {
        if (trackback.getId() == null) {
            throw new FetcherException("No ID associated with this trackback");
        }

        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            session.delete(trackback);

            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Load the recent trackbacks for a blog
     *
     * @param blog {@link Blog}
     * @throws FetcherException If there is an error retrieving the recent trackbacks
     */
    public List loadRecentTrackbacks(Blog blog) throws FetcherException {
        List recentTrackbacks;

        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            Criteria trackbacksCriteria = session.createCriteria(org.blojsom.blog.database.DatabaseTrackback.class);
            trackbacksCriteria.add(Restrictions.eq("blogId", blog.getBlogId()))
                    .add(Restrictions.eq("status", "approved"))
                    .addOrder(Order.desc("trackbackDate"));

            String recentTrackbacksCount = blog.getProperty(BlojsomConstants.RECENT_TRACKBACKS_COUNT);
            int count;
            try {
                count = Integer.parseInt(recentTrackbacksCount);
            } catch (NumberFormatException e) {
                count = BlojsomConstants.DEFAULT_RECENT_TRACKBACKS_COUNT;
            }

            if (count > 0) {
                trackbacksCriteria.setMaxResults(count);
            }

            recentTrackbacks = trackbacksCriteria.list();

            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }

        return recentTrackbacks;
    }

    /**
     * Save a given {@link Pingback}
     *
     * @param blog {@link Blog}
     * @param pingback {@link Pingback} to save
     * @throws FetcherException If there is an error saving the pingback
     */
    public void savePingback(Blog blog, Pingback pingback) throws FetcherException {
        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            if (pingback.getId() == null) {
                session.save(pingback);
            } else {
                session.update(pingback);
            }

            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Load a given {@link Pingback}
     *
     * @param blog {@link Blog}
     * @param pingback {@link Pingback} to load
     * @throws FetcherException If there is an error loading the pingback
     */
    public void loadPingback(Blog blog, Pingback pingback) throws FetcherException {
        if (pingback.getId() == null) {
            throw new FetcherException("No ID associated with this pingback");
        }

        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            session.load(pingback, pingback.getId());

            tx.commit();

            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Load a pingback given the source URI and target URI
     *
     * @param blog {@link Blog}
     * @param sourceURI Source URI
     * @param targetURI Target URI
     * @return {@link Pingback} given the source and target URIs or <code>null</code> if not found
     * @throws FetcherException If there was an erorr loading the pingback
     */
    public Pingback loadPingback(Blog blog, String sourceURI, String targetURI) throws FetcherException {
        Pingback pingback;

        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            Criteria pingbackCriteria = session.createCriteria(org.blojsom.blog.database.DatabasePingback.class);
            pingbackCriteria.add(Restrictions.eq("blogId", blog.getBlogId()))
                    .add(Restrictions.eq("sourceURI", sourceURI))
                    .add(Restrictions.eq("targetURI", targetURI));

            pingback = (Pingback) pingbackCriteria.uniqueResult();

            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }

        return pingback;
    }

    /**
     * Delete a given {@link Pingback}
     *
     * @param blog {@link Blog}
     * @param pingback {@link Pingback} to delete
     * @throws FetcherException If there is an error deleting the pingback
     */
    public void deletePingback(Blog blog, Pingback pingback) throws FetcherException {
        if (pingback.getId() == null) {
            throw new FetcherException("No ID associated with this pingback");
        }

        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            session.delete(pingback);

            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Load the recent pingbacks for a blog
     *
     * @param blog {@link Blog}
     * @throws FetcherException If there is an error retrieving the recent pingbacks
     */
    public List loadRecentPingbacks(Blog blog) throws FetcherException {
        List recentPingbacks;

        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            Criteria pingbacksCriteria = session.createCriteria(org.blojsom.blog.database.DatabasePingback.class);
            pingbacksCriteria.add(Restrictions.eq("blogId", blog.getBlogId()))
                    .add(Restrictions.eq("status", "approved"))
                    .addOrder(Order.desc("trackbackDate"));

            String recentPingbacksCount = blog.getProperty(BlojsomConstants.RECENT_PINGBACKS_COUNT);
            int count;
            try {
                count = Integer.parseInt(recentPingbacksCount);
            } catch (NumberFormatException e) {
                count = BlojsomConstants.DEFAULT_RECENT_PINGBACKS_COUNT;
            }

            if (count > 0) {
                pingbacksCriteria.setMaxResults(count);
            }

            recentPingbacks = pingbacksCriteria.list();

            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }

        return recentPingbacks;
    }

    /**
     * Load a {@link User} from a blog
     *
     * @param blog      {@link Blog}
     * @param userLogin Login ID
     * @throws FetcherException If there is an error loading the {@link User} from the blog
     */
    public User loadUser(Blog blog, String userLogin) throws FetcherException {
        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            Criteria userCriteria = session.createCriteria(DatabaseUser.class);
            userCriteria.add(Restrictions.eq("userLogin", userLogin)).add(Restrictions.eq("blogId", blog.getBlogId()));

            DatabaseUser user = (DatabaseUser) userCriteria.uniqueResult();

            tx.commit();
            session.close();

            if (user == null) {
                throw new FetcherException("Unable to load user login: " + userLogin + " for blog: " + blog.getBlogId());
            }

            return user;
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }
    }

    /**
     * Retrieve the users for a given blog
     *
     * @param blog {@link Blog}
     * @return List of {@link User}s for a blog
     */
    public User[] getUsers(Blog blog) {
        Session session = _sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        Criteria userCriteria = session.createCriteria(org.blojsom.blog.database.DatabaseUser.class);
        userCriteria.add(Restrictions.eq("blogId", blog.getBlogId()));
        userCriteria.addOrder(Order.asc("userLogin"));

        List userList = userCriteria.list();

        tx.commit();
        session.close();

        try {
            return (DatabaseUser[]) userList.toArray(new DatabaseUser[userList.size()]);
        } catch (Exception e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            return new DatabaseUser[0];
        }
    }

    /**
     * Load a given {@link User} from a blog given their ID
     *
     * @param blog   {@link Blog}
     * @param userID User ID
     * @return {@link User}
     * @throws FetcherException If there is an error loading the user
     */
    public User loadUser(Blog blog, Integer userID) throws FetcherException {
        if (userID == null) {
            return new DatabaseUser();
        } else {
            try {
                Session session = _sessionFactory.openSession();
                Transaction tx = session.beginTransaction();

                User user = (DatabaseUser) session.load(DatabaseUser.class, userID);
                if (!user.getBlogId().equals(blog.getBlogId())) {
                    tx.commit();
                    session.close();

                    throw new FetcherException("User ID: " + userID + " not from current blog: " + blog.getBlogId());
                }

                tx.commit();
                session.close();

                return user;
            } catch (HibernateException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }

                throw new FetcherException("Unable to load user ID: " + userID + " from blog: " + blog.getBlogId(), e);
            }
        }
    }

    /**
     * Save a given {@link User} to the blog
     *
     * @param blog {@link Blog}
     * @param user {@link User}
     * @return {@link User}
     * @throws FetcherException If there is an error saving the user to the blog
     */
    public User saveUser(Blog blog, User user) throws FetcherException {
        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            if (user.getId() == null) {
                session.save(user);
            } else {
                session.update(user);
            }

            tx.commit();
            session.close();

            return user;
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException("Unable to save user login: " + user.getUserLogin() + " to blog: " + blog.getBlogId(), e);
        }
    }

    /**
     * Delete a given user from a blog
     *
     * @param blog   {@link Blog}
     * @param userID User ID
     * @throws FetcherException If there is an error deleting the user from the blog
     */
    public void deleteUser(Blog blog, Integer userID) throws FetcherException {
        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            User userToDelete = (DatabaseUser) session.load(DatabaseUser.class, userID);
            if (!userToDelete.getBlogId().equals(blog.getBlogId())) {
                tx.commit();
                session.close();

                throw new FetcherException("User ID: " + userID + " not from current blog: " + blog.getBlogId());
            }

            session.delete(userToDelete);

            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException("Unable to delete user ID: " + userID + " from blog: " + blog.getBlogId(), e);
        }
    }

    /**
     * Load the responses (comments, trackbacks, pingbacks) for a given {@link Blog} matching one of a set of status codes
     *
     * @param blog {@link Blog}
     * @param status List of status codes to load
     * @return List of responses (comments, trackbacks, pingbacks) matching one of a set of status codes
     * @throws FetcherException If there is an error loading the responses
     */
    public List loadResponses(Blog blog, String[] status) throws FetcherException {
        List responses = new ArrayList();

        try {
            Session session = _sessionFactory.openSession();
            Transaction tx = session.beginTransaction();

            Criteria commentsCriteria = session.createCriteria(org.blojsom.blog.database.DatabaseComment.class);
            commentsCriteria.add(Restrictions.eq("blogId", blog.getBlogId()))
                    .add(Restrictions.in("status", status));

            responses.addAll(commentsCriteria.list());

            Criteria trackbacksCriteria = session.createCriteria(org.blojsom.blog.database.DatabaseTrackback.class);
            trackbacksCriteria.add(Restrictions.eq("blogId", blog.getBlogId()))
                    .add(Restrictions.in("status", status));

            responses.addAll(trackbacksCriteria.list());

            tx.commit();
            session.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            throw new FetcherException(e);
        }

        Collections.sort(responses, BlojsomUtils.RESPONSE_COMPARATOR);
        return responses;
    }

    /**
     * Handle an event broadcast from another component
     *
     * @param event {@link org.blojsom.event.Event} to be handled
     */
    public void handleEvent(Event event) {
    }

    /**
     * Process an event from another component
     *
     * @param event {@link org.blojsom.event.Event} to be handled
     */
    public void processEvent(Event event) {
    }

    /**
     * Called when {@link org.blojsom.servlet.BlojsomServlet} is taken out of service
     *
     * @throws org.blojsom.fetcher.FetcherException
     *          If there is an error in finalizing this fetcher
     */
    public void destroy() throws FetcherException {
        try {
            _sessionFactory.close();
        } catch (HibernateException e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }
        }

        if (_logger.isDebugEnabled()) {
            _logger.debug("Destroyed database fetcher");
        }
    }
}
