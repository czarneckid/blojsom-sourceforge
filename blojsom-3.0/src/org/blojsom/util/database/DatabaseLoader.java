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
package org.blojsom.util.database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * Database loader
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: DatabaseLoader.java,v 1.3 2006-05-01 03:02:37 czarneckid Exp $
 */
public class DatabaseLoader {

    private Log _logger = LogFactory.getLog(DatabaseLoader.class);

    private static final String DEFAULT_DETECT_BLOJSOM_SQL = "show tables;";

    private String _dbScript;
    private SessionFactory _sessionFactory;
    private ServletConfig _servletConfig;
    private String _detectBlojsomSQL;

    /**
     * Create a new instance of the Database loader
     */
    public DatabaseLoader() {
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
     * Set the DB script to initialize and load the database
     *
     * @param dbScript DB script to initialize and load the database
     */
    public void setDbScript(String dbScript) {
        _dbScript = dbScript;
    }

    /**
     * Set the {@link ServletConfig}
     *
     * @param servletConfig {@link ServletConfig}
     */
    public void setServletConfig(ServletConfig servletConfig) {
        _servletConfig = servletConfig;
    }

    /**
     * SQL to detect blojsom
     *
     * @param detectBlojsomSQL SQL to detect blojsom
     */
    public void setDetectBlojsomSQL(String detectBlojsomSQL) {
        _detectBlojsomSQL = detectBlojsomSQL;
    }

    /**
     * Initalize the blojsom database
     */
    public void init() {
        if (_dbScript == null) {
            if (_logger.isErrorEnabled()) {
                _logger.error("No database creation script defined");
            }

            return;
        }

        if (BlojsomUtils.checkNullOrBlank(_detectBlojsomSQL)) {
            _detectBlojsomSQL = DEFAULT_DETECT_BLOJSOM_SQL;
        }

        Session session = _sessionFactory.openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            if (_logger.isInfoEnabled()) {
                _logger.info("About to create blojsom database");
            }

            SQLQuery sqlQuery = session.createSQLQuery(_detectBlojsomSQL);
            List tables = sqlQuery.list();

            if (tables.size() > 0) {
                if (_logger.isInfoEnabled()) {
                    _logger.info("blojsom database already created");
                }
            } else {
                Connection sqlConnection = session.connection();

                InputStream is = _servletConfig.getServletContext().getResourceAsStream(_dbScript);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                String input;
                StringBuffer sql = new StringBuffer();

                while ((input = bufferedReader.readLine()) != null) {
                    if (!input.startsWith("--") && !"".equals(input.trim())) {
                        if (!input.endsWith(";")) {
                            sql.append(input).append(" ");
                        } else {
                            sql.append(input).append("\n");
                        }
                    }
                }

                if (_logger.isInfoEnabled()) {
                    _logger.info("Read in sql script");
                }

                bufferedReader = new BufferedReader(new StringReader(sql.toString()));
                PreparedStatement preparedStatement;

                 while ((input = bufferedReader.readLine()) != null) {
                    preparedStatement = sqlConnection.prepareStatement(input);
                    preparedStatement.execute();
                }

               if (_logger.isInfoEnabled()) {
                   _logger.info("Finised blojsom database creation");
               }
            }

            tx.commit();
        } catch (Exception e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }

            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
    }
}
