<%@ page import="org.ignition.blojsom.blog.Blog,
		 org.ignition.blojsom.util.BlojsomConstants,
		 org.ignition.blojsom.blog.BlogEntry,
		 org.ignition.blojsom.blog.BlogCategory"
		 session="false"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<%
    Blog blogInformation = (Blog) request.getAttribute(BlojsomConstants.BLOJSOM_BLOG);
    BlogEntry[] entryArray = (BlogEntry[]) request.getAttribute(BlojsomConstants.BLOJSOM_ENTRIES);
    BlogCategory[] blogCategories = (BlogCategory[]) request.getAttribute(BlojsomConstants.BLOJSOM_CATEGORIES);
    String blogSiteURL = (String) request.getAttribute(BlojsomConstants.BLOJSOM_SITE_URL);
    BlogCategory requestedCategory = (BlogCategory) request.getAttribute(BlojsomConstants.BLOJSOM_REQUESTED_CATEGORY);
    boolean blogCommentsEnabled = ((Boolean) request.getAttribute(BlojsomConstants.BLOJSOM_COMMENTS_ENABLED)).booleanValue();

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
    </head>

    <body>
	<h1><a href="<%= blogInformation.getBlogURL() %>">
		<%= blogInformation.getBlogName() %>
	</a></h1>

	<big><i><%= blogInformation.getBlogDescription() %></i></big>

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
        <% if (blogCommentsEnabled && blogEntry.supportsComments()) { %>
        <p class="weblogbottomline">Comments [<a href="<%= blogEntry.getLink() %>&page=comments"><%= blogEntry.getNumComments() %></a>]</p>
        <% } %>
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
