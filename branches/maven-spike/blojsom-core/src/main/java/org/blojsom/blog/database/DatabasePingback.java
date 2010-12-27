/**
 * Copyright (c) 2003-2009, David A. Czarnecki
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
package org.blojsom.blog.database;

import org.blojsom.blog.Pingback;
import org.blojsom.util.BlojsomUtils;

/**
 * DatabasePingback
 *
 * @author David Czarnecki
 * @version $Id: DatabasePingback.java,v 1.7 2008-07-07 19:55:05 czarneckid Exp $
 * @since blojsom 3.0
 */
public class DatabasePingback extends DatabaseTrackback implements Pingback {

    private String _sourceURI;
    private String _targetURI;

    /**
     * Create a new instance of the database pingback
     */
    public DatabasePingback() {
    }

    /**
     * Get the source URI
     *
     * @return Source URI
     */
    public String getSourceURI() {
        return _sourceURI;
    }

    /**
     * Get the escaped source URI
     *
     * @return Escaped source URI
     */
    public String getEscapedSourceURI() {
        return BlojsomUtils.escapeString(_sourceURI);
    }

    /**
     * Set the source URI
     *
     * @param sourceURI Source URI
     */
    public void setSourceURI(String sourceURI) {
        _sourceURI = sourceURI;
    }

    /**
     * Get the target URI
     *
     * @return Target URI
     */
    public String getTargetURI() {
        return _targetURI;
    }

    /**
     * Get the escaped target URI
     *
     * @return Escaped target URI
     */
    public String getEscapedTargetURI() {
        return BlojsomUtils.escapeString(_targetURI);
    }

    /**
     * Set the target URI
     *
     * @param targetURI Target URI
     */
    public void setTargetURI(String targetURI) {
        _targetURI = targetURI;
    }

    /**
     * Get the response type
     *
     * @return Response type
     */
    public String getType() {
        return PINGBACK_TYPE;
    }
}
