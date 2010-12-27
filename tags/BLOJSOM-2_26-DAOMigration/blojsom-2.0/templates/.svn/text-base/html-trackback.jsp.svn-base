<%@ page contentType="text/html; charset=UTF-8"
         import="org.blojsom.util.BlojsomConstants,
                 org.blojsom.blog.*,
                 java.util.List"
		 session="false"%>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<%
            Blog blogInformation = (Blog) request.getAttribute(BlojsomConstants.BLOJSOM_BLOG);
            BlogEntry[] entryArray = (BlogEntry[]) request.getAttribute(BlojsomConstants.BLOJSOM_ENTRIES);
            BlogCategory[] blogCategories = (BlogCategory[]) request.getAttribute(BlojsomConstants.BLOJSOM_CATEGORIES);
            String blogSiteURL = (String) request.getAttribute(BlojsomConstants.BLOJSOM_SITE_URL);
            BlogCategory requestedCategory = (BlogCategory) request.getAttribute(BlojsomConstants.BLOJSOM_REQUESTED_CATEGORY);

            StringBuffer catStringBuf = new StringBuffer(20);
            String blogName = null;
            for (int j = 0; j < blogCategories.length; j++) {
                BlogCategory blogCategory = blogCategories[j];
                blogName = blogCategory.getName();
                if ((blogName == null) || (blogName.length() < 1)) {
                    blogName = blogCategory.getCategory();
                }
                catStringBuf.append("[<i><a href=").append(blogCategory.getCategoryURL());
                catStringBuf.append(">").append(blogName).append("</a></i>]");
            }
            String catString = catStringBuf.toString();
%>

    <head>
	<title><%= blogInformation.getBlogName() %></title>
	<link rel="stylesheet" href="<%= blogSiteURL %>/blojsom.css" />
    <link rel="SHORTCUT ICON" href="<%= blogSiteURL %>/favicon.ico" />
    <link rel="alternate" type="application/rss+xml" title="RSS" href="<%= blogInformation.getBlogURL() %>?flavor=rss" />
    <link rel="EditURL" type="application/rsd+xml" title="RSD" href="<%= blogInformation.getBlogURL() %>?flavor=rsd" />

    </head>

    <body>
	<h1><a href="<%= blogInformation.getBlogURL() %>">
		<%= blogInformation.getBlogName() %>
	</a></h1>

	<h3><%= blogInformation.getBlogDescription() %></h3>

	<p><b>Available Categories: </b><%= catString %></p>
<%
            if (entryArray != null) {
                for (int i = 0; i < entryArray.length; i++) {
                BlogEntry blogEntry = entryArray[i];
%>
		<div class="entrystyle">
		<p class="weblogtitle"><%= blogEntry.getTitle() %>
		    <span class="smalltext">
			[<a href="<%= blogEntry.getLink() %>">Permalink</a>]
		    </span>
		</p>
		<p class="weblogdateline"><%= blogEntry.getDate() %></p>
		<p><%= blogEntry.getDescription() %></p>
		</div>

        <%
            String blogDescription =  blogEntry.getEscapedDescription();
            if ( blogDescription.length() > 255 ) {
                blogDescription = blogDescription.substring(0,252)+ "...";
            }
        %>
        <!-- Trackback Auto Discovery -->

<!--
    <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
             xmlns:dc="http://purl.org/dc/elements/1.1/"
             xmlns:trackback="http://madskills.com/public/xml/rss/module/trackback/">
        <rdf:Description
             rdf:about="<%= blogEntry.getLink()%>"
             dc:identifier="<%= blogEntry.getLink()%>"
             dc:title="<%=blogEntry.getTitle()%>"
             dc:subject="<%=requestedCategory.getCategoryURL()%>"
             dc:description="<%=blogDescription%>"
             dc:creator="<%= blogInformation.getBlogOwner() %>"
             dc:date="<%= blogEntry.getISO8601Date()%>"
             trackback:ping="<%= blogEntry.getLink()%>&tb=y" />
     </rdf:RDF>
-->


        <p class="weblogtitle">Trackback for this entry</p>
        <p class="weblogdateline"><%= blogEntry.getLink() %>&amp;tb=y</p>
        <%
            List blogTrackbacks = blogEntry.getTrackbacks();
            if (blogTrackbacks != null) {
            for (int j = 0; j < blogTrackbacks.size(); j++) {
                Trackback trackback = (Trackback) blogTrackbacks.get(j);
                String urlName;
                if (trackback.getBlogName() != null) {
                    urlName = trackback.getBlogName();
                } else {
                    urlName = trackback.getUrl();
                }
                String trackbackTitle;
                if (trackback.getTitle() != null) {
                    trackbackTitle = trackback.getTitle();
                } else {
                    trackbackTitle = "";
                }
                String trackbackExcerpt;
                if (trackback.getExcerpt() != null) {
                    trackbackExcerpt = trackback.getExcerpt();
                } else {
                    trackbackExcerpt = "";
                }
        %>
        <div class="commentstyle">
            Trackback from: <a href="<%= trackback.getUrl() %>" rel="nofollow"><%= urlName %></a>
            <div class="weblogdateline">Left on: <%= trackback.getTrackbackDate() %></div><br/>
            Title: <%= trackbackTitle %><br />
            <%= trackbackExcerpt %><br />
        </div>

        <%
                    }
            }
        %>


<%
                }
            }
%>

<p />

<%
            if ((entryArray != null) && (entryArray.length > 0)) {
%>
	<p><b>Available Categories: </b><%= catString %></p>
<%
            }
%>

    <p />
    <a href="http://blojsom.sf.net"><img src="<%= blogSiteURL %>/powered-by-blojsom.gif" border="0" alt="Powered By blojsom"/></a>&nbsp;&nbsp;
    <a href="<%= requestedCategory.getCategoryURL() %>?flavor=rss"><img src="<%= blogSiteURL %>/xml.gif" border="0" alt="RSS Feed"/></a>&nbsp;
    <a href="<%= requestedCategory.getCategoryURL() %>?flavor=rss2"><img src="<%= blogSiteURL %>/rss.gif" border="0" alt="RSS2 Feed"/></a>&nbsp;
    <a href="<%= requestedCategory.getCategoryURL() %>?flavor=rdf"><img src="<%= blogSiteURL %>/rdf.gif" border="0" alt="RDF Feed"/></a>

    </body>
</html>
