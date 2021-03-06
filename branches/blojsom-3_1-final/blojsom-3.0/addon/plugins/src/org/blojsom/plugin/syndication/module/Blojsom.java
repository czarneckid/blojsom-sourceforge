/**
 * Copyright (c) 2003-2007, David A. Czarnecki
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
package org.blojsom.plugin.syndication.module;

import com.sun.syndication.feed.module.Module;

import java.util.List;

/**
 * Module for parsing blojsom-specific information using ROME
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: Blojsom.java,v 1.4 2007-01-17 02:35:06 czarneckid Exp $
 */
public interface Blojsom extends Module {

    public static final String BLOJSOM_URI = "http://rome.dev.java.net/module/blojsom/1.0";

    public String getAuthor();
    public void setAuthor(String author);

    public String getPostSlug();
    public void setPostSlug(String postSlug);

    public String getTechnoratiTags();
    public void setTechnoratiTags(String technoratiTags);

    public boolean getAllowsComments();
    public void setAllowsComments(boolean allowsComments);

    public boolean getAllowsTrackbacks();
    public void setAllowsTrackbacks(boolean allowsTrackbacks);

    public boolean getAllowsPingbacks();
    public void setAllowsPingbacks(boolean allowsPingbacks);

    public List getComments();
    public void setComments(List comments);

    public List getTrackbacks();
    public void setTrackbacks(List trackbacks);

    public List getPingbacks();
    public void setPingbacks(List pingbacks);

    public List getMetadata();
    public void setMetadata(List metadata);
}
