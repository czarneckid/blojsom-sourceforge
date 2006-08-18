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

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleParser;
import com.sun.syndication.io.impl.DateParser;
import org.jdom.Element;
import org.jdom.Namespace;
import org.blojsom.util.BlojsomUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Blojsom parser for ROME
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: BlojsomParser.java,v 1.4 2006-08-18 17:24:12 czarneckid Exp $
 */
public class BlojsomParser implements ModuleParser {

    private static final Namespace BLOJSOM_NS = Namespace.getNamespace("blojsom", Blojsom.BLOJSOM_URI);

    public String getNamespaceUri() {
        return Blojsom.BLOJSOM_URI;
    }

    public Module parse(Element element) {
        boolean foundSomething = false;
        BlojsomImplementation blojsomModule = new BlojsomImplementation();

        Element e = element.getChild("author", BLOJSOM_NS);
        if (e != null) {
            foundSomething = true;
            blojsomModule.setAuthor(e.getText());
        }

        e = element.getChild("technoratiTags", BLOJSOM_NS);
        if (e != null) {
            foundSomething = true;
            blojsomModule.setTechnoratiTags(e.getText());
        }

        e = element.getChild("postSlug", BLOJSOM_NS);
        if (e != null) {
            foundSomething = true;
            blojsomModule.setPostSlug(BlojsomUtils.urlDecode(e.getText()));
        }

        e = element.getChild("allowsComments", BLOJSOM_NS);
        if (e != null) {
            foundSomething = true;
            blojsomModule.setAllowsComments(Boolean.valueOf(e.getText()).booleanValue());
        }

        e = element.getChild("allowsTrackbacks", BLOJSOM_NS);
        if (e != null) {
            foundSomething = true;
            blojsomModule.setAllowsTrackbacks(Boolean.valueOf(e.getText()).booleanValue());
        }

        e = element.getChild("allowsPingbacks", BLOJSOM_NS);
        if (e != null) {
            foundSomething = true;
            blojsomModule.setAllowsPingbacks(Boolean.valueOf(e.getText()).booleanValue());
        }

        e = element.getChild("comments", BLOJSOM_NS);
        if (e != null) {
            foundSomething = true;
            blojsomModule.setComments(parseComments(e));
        }

        e = element.getChild("trackbacks", BLOJSOM_NS);
        if (e != null) {
            foundSomething = true;
            blojsomModule.setTrackbacks(parseTrackbacks(e));
        }

        e = element.getChild("pingbacks", BLOJSOM_NS);
        if (e != null) {
            foundSomething = true;
            blojsomModule.setPingbacks(parsePingbacks(e));
        }

        e = element.getChild("metadataItems", BLOJSOM_NS);
        if (e != null) {
            foundSomething = true;
            blojsomModule.setMetadata(parseMetadata(e));
        }

        return (foundSomething) ? blojsomModule : null;
    }

    private List parseComments(Element e) {
        List comments = new ArrayList();
        List commentElements = e.getChildren("comment", BLOJSOM_NS);

        for (int i = 0; i < commentElements.size(); i++) {
            Element element = (Element) commentElements.get(i);
            if (element != null) {
                SimpleComment comment = new SimpleComment();

                Element author = element.getChild("commentAuthor", BLOJSOM_NS);
                if (author != null) {
                    comment.setAuthor(author.getText());
                }

                Element authorEmail = element.getChild("commentAuthorEmail", BLOJSOM_NS);
                if (authorEmail != null) {
                    comment.setAuthorEmail(authorEmail.getText());
                }

                Element authorURL = element.getChild("commentAuthorURL", BLOJSOM_NS);
                if (authorURL != null) {
                    comment.setAuthorURL(authorURL.getText());
                }

                Element commentText = element.getChild("commentText", BLOJSOM_NS);
                if (commentText != null) {
                    comment.setComment(commentText.getText());
                }

                Element commentDate = element.getChild("commentDate", BLOJSOM_NS);
                if (commentDate != null) {
                    comment.setCommentDate(DateParser.parseRFC822(commentDate.getText()));
                }

                Element status = element.getChild("commentStatus", BLOJSOM_NS);
                if (status != null) {
                    comment.setStatus(status.getText());
                }

                Element ip = element.getChild("commentIP", BLOJSOM_NS);
                if (ip != null) {
                    comment.setIp(ip.getText());
                }

                Element metadata = element.getChild("commentMetadata", BLOJSOM_NS);
                if (metadata != null) {
                    comment.setMetadata(parseMetadata(metadata));
                }

                comments.add(comment);
            }
        }

        return comments;
    }

    private List parseTrackbacks(Element e) {
        List trackbacks = new ArrayList();
        List trackbackElements = e.getChildren("trackback", BLOJSOM_NS);

        for (int i = 0; i < trackbackElements.size(); i++) {
            Element element = (Element) trackbackElements.get(i);
            if (element != null) {
                SimpleTrackback trackback = new SimpleTrackback();

                Element trackbackTitle = element.getChild("trackbackTitle", BLOJSOM_NS);
                if (trackbackTitle != null) {
                    trackback.setTitle(trackbackTitle.getText());
                }

                Element trackbackExcerpt = element.getChild("trackbackExcerpt", BLOJSOM_NS);
                if (trackbackExcerpt != null) {
                    trackback.setExcerpt(trackbackExcerpt.getText());
                }

                Element trackbackUrl = element.getChild("trackbackUrl", BLOJSOM_NS);
                if (trackbackUrl != null) {
                    trackback.setUrl(trackbackUrl.getText());
                }

                Element trackbackBlogName = element.getChild("trackbackBlogName", BLOJSOM_NS);
                if (trackbackBlogName != null) {
                    trackback.setBlogName(trackbackBlogName.getName());
                }

                Element trackbackIp = element.getChild("trackbackIP", BLOJSOM_NS);
                if (trackbackIp != null) {
                    trackback.setIp(trackbackIp.getText());
                }

                Element trackbackDate = element.getChild("trackbackDate", BLOJSOM_NS);
                if (trackbackDate != null) {
                    trackback.setTrackbackDate(DateParser.parseRFC822(trackbackDate.getText()));
                }

                Element trackbackStatus = element.getChild("trackbackStatus", BLOJSOM_NS);
                if (trackbackStatus != null) {
                    trackback.setStatus(trackbackStatus.getText());
                }

                Element metadata = element.getChild("trackbackMetadata", BLOJSOM_NS);
                if (metadata != null) {
                    trackback.setMetadata(parseMetadata(metadata));
                }

                trackbacks.add(trackback);
            }
        }

        return trackbacks;
    }

    private List parsePingbacks(Element e) {
        List pingbacks = new ArrayList();
        List pingbackElements = e.getChildren("pingback", BLOJSOM_NS);

        for (int i = 0; i < pingbackElements.size(); i++) {
            Element element = (Element) pingbackElements.get(i);
            if (element != null) {
                SimplePingback pingback = new SimplePingback();

                Element pingbackTitle = element.getChild("pingbackTitle", BLOJSOM_NS);
                if (pingbackTitle != null) {
                    pingback.setTitle(pingbackTitle.getText());
                }

                Element pingbackExcerpt = element.getChild("pingbackExcerpt", BLOJSOM_NS);
                if (pingbackExcerpt != null) {
                    pingback.setExcerpt(pingbackExcerpt.getText());
                }

                Element pingbackUrl = element.getChild("pingbackUrl", BLOJSOM_NS);
                if (pingbackUrl != null) {
                    pingback.setUrl(pingbackUrl.getText());
                }

                Element pingbackBlogName = element.getChild("pingbackBlogName", BLOJSOM_NS);
                if (pingbackBlogName != null) {
                    pingback.setBlogName(pingbackBlogName.getName());
                }

                Element pingbackIp = element.getChild("pingbackIP", BLOJSOM_NS);
                if (pingbackIp != null) {
                    pingback.setIp(pingbackIp.getText());
                }

                Element pingbackDate = element.getChild("pingbackDate", BLOJSOM_NS);
                if (pingbackDate != null) {
                    pingback.setPingbackDate(DateParser.parseRFC822(pingbackDate.getText()));
                }

                Element pingbackStatus = element.getChild("pingbackStatus", BLOJSOM_NS);
                if (pingbackStatus != null) {
                    pingback.setStatus(pingbackStatus.getText());
                }

                Element pingbackSourceURI = element.getChild("pingbackSourceURI", BLOJSOM_NS);
                if (pingbackSourceURI != null) {
                    pingback.setSourceURI(pingbackSourceURI.getText());
                }

                Element pingbackTargetURI = element.getChild("pingbackTargetURI", BLOJSOM_NS);
                if (pingbackTargetURI != null) {
                    pingback.setTargetURI(pingbackTargetURI.getText());
                }

                Element metadata = element.getChild("pingbackMetadata", BLOJSOM_NS);
                if (metadata != null) {
                    pingback.setMetadata(parseMetadata(metadata));
                }

                pingbacks.add(pingback);
            }
        }

        return pingbacks;
    }

    private List parseMetadata(Element e) {
        List metadata = new ArrayList();
        List metadataElements = e.getChildren("metadata", BLOJSOM_NS);

        for (int i = 0; i < metadataElements.size(); i++) {
            Element element = (Element) metadataElements.get(i);
            Metadata metadataItem = new Metadata();

            Element metadataKey = element.getChild("key", BLOJSOM_NS);
            if (metadataKey != null) {
                metadataItem.setKey(metadataKey.getText());
            }

            Element metadataValue = element.getChild("value", BLOJSOM_NS);
            if (metadataValue != null) {
                metadataItem.setValue(metadataValue.getText());
            }

            metadata.add(metadataItem);
        }
        
        return metadata;
    }
}

