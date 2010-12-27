<%@ page import="org.blojsom.blog.BlogEntry,
                 org.blojsom.util.BlojsomConstants,
                 org.blojsom.blog.Blog"
         contentType="text/plain" %>

<% Blog blogInformation = (Blog) request.getAttribute(BlojsomConstants.BLOJSOM_BLOG); %>
<%= blogInformation.getBlogName() %>
<%= blogInformation.getBlogURL() %>
<%= blogInformation.getBlogDescription() %>


<%
    BlogEntry[] entryArray = (BlogEntry[]) request.getAttribute(BlojsomConstants.BLOJSOM_ENTRIES);
    if (entryArray != null) {
        for (int i = 0; i < entryArray.length; i++) {
        BlogEntry blogEntry = entryArray[i];
%>
---
<%= blogEntry.getTitle() %>
<%= blogEntry.getDate() %>
<%= blogEntry.getDescription() %>
<%
        }
    }
%>
