<%@ page import="java.util.TreeMap,
                 org.ignition.blojsom.blog.Blog,
                 org.ignition.blojsom.util.BlojsomConstants,
                 java.util.Iterator,
                 org.ignition.blojsom.blog.BlogEntry"%>
<html>
<%
    Blog blogInformation = (Blog) request.getAttribute(BlojsomConstants.BLOJSOM_BLOG);
    BlogEntry[] entryArray = (BlogEntry[]) request.getAttribute(BlojsomConstants.BLOJSOM_ENTRIES);
    String[] blogCategories = (String[]) request.getAttribute(BlojsomConstants.BLOJSOM_CATEGORIES);
    String blogSiteURL = (String) request.getAttribute(BlojsomConstants.BLOJSOM_SITE_URL);
%>

<head>
<title><%= blogInformation.getBlogName() %></title>
</head>

<link rel="stylesheet" href="<%= blogSiteURL %>/main.css" />

<h1><a href="<%= blogInformation.getBlogURL() %>"><%= blogInformation.getBlogName() %></a></h1>
<h3><%= blogInformation.getBlogDescription() %></h3>

<body>
<%
    if (entryArray != null) {
        for (int i = 0; i < entryArray.length; i++) {
            BlogEntry blogEntry = entryArray[i];
%>
    <p class="weblogtitle"><%= blogEntry.getTitle() %></p><p class="weblogdateline"><%= blogEntry.getDate() %> [<a href="<%= blogInformation.getBlogURL()%><%= blogEntry.getCategory()%>">/<%= blogEntry.getCategory() %></a>]</p>
    <p><%= blogEntry.getDescription() %></p>
    <p class="weblogbottomline"><a href="<%= blogEntry.getLink() %>">Permalink</a></p>
<%
        }
    }
%>
<%
    for (int i = 0; i < blogCategories.length; i++) {
        String blogCategory = blogCategories[i];
%>
    <a href="<%= blogCategory %>"><%= blogCategory %></a><br />
<%
    }
%>
</body>

</html>