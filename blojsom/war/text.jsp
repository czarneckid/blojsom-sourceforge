<%@ page import="org.ignition.blojsom.blog.BlogEntry,
                 org.ignition.blojsom.util.BlojsomConstants"
         contentType="text/plain" %>
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
