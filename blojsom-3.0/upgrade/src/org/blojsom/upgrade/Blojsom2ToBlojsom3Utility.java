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
package org.blojsom.upgrade;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.fetcher.Fetcher;

/**
 * Utility class to migrate from blojsom 2 to blojsom 3
 *
 * @author David Czarnecki
 * @since blojsom 3
 * @version $Id: Blojsom2ToBlojsom3Utility.java,v 1.1 2006-09-27 01:59:47 czarneckid Exp $
 */
public class Blojsom2ToBlojsom3Utility {

    private static Log _logger = LogFactory.getLog(Blojsom2ToBlojsom3Utility.class);

    private Fetcher _fetcher;
    private String _blojsom2Path;

    /**
     * Construct a new instance of the blojsom 2 to blojsom 3 utility
     */
    public Blojsom2ToBlojsom3Utility() {
    }

    /**
     * Set the path to the blojsom 2 installation directory
     *
     * @param blojsom2Path blojsom 2 installation directory
     */
    public void setBlojsom2Path(String blojsom2Path) {
        _blojsom2Path = blojsom2Path;
    }

    /**
     * Set the {@link Fetcher}
     *
     * @param fetcher {@link Fetcher}
     */
    public void setFetcher(Fetcher fetcher) {
        _fetcher = fetcher;
    }

    /**
     * Upgrade the blojsom 2 instance to blojsom 3
     */
    public void upgrade() {
        if (_logger.isDebugEnabled()) {
            _logger.debug("Finished upgrading blojsom 2 instance to blojsom 3!");
        }
    }
}
