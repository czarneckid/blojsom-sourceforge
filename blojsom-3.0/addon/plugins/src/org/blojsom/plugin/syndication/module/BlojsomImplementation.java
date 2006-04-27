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
package org.blojsom.plugin.syndication.module;

import com.sun.syndication.feed.module.ModuleImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of module for parsing blojsom-specific information using ROME
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: BlojsomImplementation.java,v 1.2 2006-04-27 03:16:52 czarneckid Exp $
 */
public class BlojsomImplementation extends ModuleImpl implements Blojsom {

    private String _author;
    private String _technoratiTags;
    private String _postSlug;
    private boolean _allowsComments;
    private boolean _allowsTrackbacks;
    private boolean _allowsPingbacks;
    private List _comments;
    private List _trackbacks;
    private List _pingbacks;

    public BlojsomImplementation() {
        super(Blojsom.class, Blojsom.BLOJSOM_URI);
    }

    public Class getInterface() {
        return Blojsom.class;
    }

    public void copyFrom(Object object) {
        Blojsom blojsom = (Blojsom) object;

        setAuthor(blojsom.getAuthor());
        setTechnoratiTags(blojsom.getTechnoratiTags());
        setPostSlug(blojsom.getPostSlug());
        setAllowsComments(blojsom.getAllowsComments());
        setAllowsTrackbacks(blojsom.getAllowsTrackbacks());
        setAllowsPingbacks(blojsom.getAllowsPingbacks());

        // Process comments
        List comments = blojsom.getComments();
        List copiedComments = new ArrayList();
        for (int i = 0; i < comments.size(); i++) {
            SimpleComment comment = (SimpleComment) comments.get(i);
            SimpleComment copiedComment = new SimpleComment();
            copiedComment.setAuthor(comment.getAuthor());
            copiedComment.setAuthorEmail(comment.getAuthorEmail());
            copiedComment.setAuthorURL(comment.getAuthorURL());
            copiedComment.setComment(comment.getComment());
            copiedComment.setIp(comment.getIp());
            copiedComment.setCommentDate(comment.getCommentDate());
            copiedComment.setStatus(comment.getStatus());

            copiedComments.add(copiedComment);
        }
        setComments(copiedComments);

        // Process trackbacks
        List trackbacks = blojsom.getTrackbacks();
        List copiedTrackbacks = new ArrayList();
        for (int i = 0; i < trackbacks.size(); i++) {
            SimpleTrackback trackback = (SimpleTrackback) trackbacks.get(i);
            SimpleTrackback copiedTrackback = new SimpleTrackback();
            copiedTrackback.setBlogName(trackback.getBlogName());
            copiedTrackback.setExcerpt(trackback.getExcerpt());
            copiedTrackback.setIp(trackback.getIp());
            copiedTrackback.setStatus(trackback.getStatus());
            copiedTrackback.setTitle(trackback.getTitle());
            copiedTrackback.setTrackbackDate(trackback.getTrackbackDate());
            copiedTrackback.setUrl(trackback.getUrl());

            copiedTrackbacks.add(copiedTrackback);
        }
        setTrackbacks(copiedTrackbacks);

        // Process pingbacks
        List pingbacks = blojsom.getPingbacks();
        List copiedPingbacks = new ArrayList();
        for (int i = 0; i < pingbacks.size(); i++) {
            SimplePingback pingback = (SimplePingback) pingbacks.get(i);
            SimplePingback copiedPingback = new SimplePingback();
            copiedPingback.setBlogName(pingback.getBlogName());
            copiedPingback.setExcerpt(pingback.getExcerpt());
            copiedPingback.setIp(pingback.getIp());
            copiedPingback.setStatus(pingback.getStatus());
            copiedPingback.setTitle(pingback.getTitle());
            copiedPingback.setPingbackDate(pingback.getPingbackDate());
            copiedPingback.setUrl(pingback.getUrl());
            copiedPingback.setSourceURI(pingback.getSourceURI());
            copiedPingback.setTargetURI(pingback.getTargetURI());

            copiedPingbacks.add(copiedPingback);
        }
        setPingbacks(copiedPingbacks);
    }

    public String getAuthor() {
        return _author;
    }

    public void setAuthor(String author) {
        _author = author;
    }

    public String getTechnoratiTags() {
        return _technoratiTags;
    }

    public void setTechnoratiTags(String technoratiTags) {
        _technoratiTags = technoratiTags;
    }

    public String getPostSlug() {
        return _postSlug;
    }

    public void setPostSlug(String postSlug) {
        _postSlug = postSlug;
    }

    public boolean getAllowsComments() {
        return _allowsComments;
    }

    public void setAllowsComments(boolean allowsComments) {
        _allowsComments = allowsComments;
    }

    public boolean getAllowsTrackbacks() {
        return _allowsTrackbacks;
    }

    public void setAllowsTrackbacks(boolean allowsTrackbacks) {
        _allowsTrackbacks = allowsTrackbacks;
    }

    public boolean getAllowsPingbacks() {
        return _allowsPingbacks;
    }

    public void setAllowsPingbacks(boolean allowsPingbacks) {
        _allowsPingbacks = allowsPingbacks;
    }

    public List getComments() {
        return _comments;
    }

    public void setComments(List comments) {
        _comments = comments;
    }

    public List getTrackbacks() {
        return _trackbacks;
    }

    public void setTrackbacks(List trackbacks) {
        _trackbacks = trackbacks;
    }

    public List getPingbacks() {
        return _pingbacks;
    }

    public void setPingbacks(List pingbacks) {
        _pingbacks = pingbacks;
    }

    public String getUri() {
        return Blojsom.BLOJSOM_URI;
    }
}
