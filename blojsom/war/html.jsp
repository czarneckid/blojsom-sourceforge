<%@ page import="java.util.TreeMap,
		 org.ignition.blojsom.blog.Blog,
		 org.ignition.blojsom.util.BlojsomConstants,
		 java.util.Iterator,
		 org.ignition.blojsom.blog.BlogEntry,
		 org.ignition.blojsom.blog.BlogCategory"
		 session="false"%>
<html>
<%
    Blog blogInformation = (Blog) request.getAttribute(BlojsomConstants.BLOJSOM_BLOG);
    BlogEntry[] entryArray = (BlogEntry[]) request.getAttribute(BlojsomConstants.BLOJSOM_ENTRIES);
    BlogCategory[] blogCategories = (BlogCategory[]) request.getAttribute(BlojsomConstants.BLOJSOM_CATEGORIES);
    String blogSiteURL = (String) request.getAttribute(BlojsomConstants.BLOJSOM_SITE_URL);

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
<%
		if ((i > 0) && ((i % 5) == 0)) {
%>
		    <div nowrap class="weblogbottomline">
			<b>Categories: </b><%= catString %>
		    </div>
<%
		}
	    }
	}
%>
<%
    if ((entryArray != null) && (entryArray.length > 0)) {
%>
	<p><b>Available Categories: </b><%= catString %></p>
<%
    }
%>

    <p />
    <img src="<%= blogSiteURL %>/powered-by-blojsom.gif" border="0" />

    </body>
</html>
