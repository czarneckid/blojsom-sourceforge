<?xml version="1.0" encoding="UTF-8"?>
<%@ page contentType="text/xml; charset=UTF-8"
         import="org.blojsom.blog.Blog,
                 org.blojsom.util.BlojsomConstants,
                 org.blojsom.blog.BlogEntry,
                 org.blojsom.util.BlojsomUtils,
                 org.blojsom.blog.BlogComment,
                 java.util.List"
         session="false"%>
<!-- name="generator" content="<%= request.getAttribute(BlojsomConstants.BLOJSOM_VERSION) %>" -->
<%
    Blog blogInformation = (Blog) request.getAttribute(BlojsomConstants.BLOJSOM_BLOG);
    BlogEntry[] blogEntries = (BlogEntry[]) request.getAttribute(BlojsomConstants.BLOJSOM_ENTRIES);
%>

<rss version="0.92" xmlns:wfw="http://wellformedweb.org/CommentAPI/">
  <channel>
    <%
        if (blogEntries != null) {
            for (int i = 0; i < blogEntries.length; i++) {
                BlogEntry blogEntry = blogEntries[i];
    %>
    		<title><%= blogEntry.getEscapedTitle() %></title>
    		<link><%= blogEntry.getEscapedLink() %></link>
    		<description><%= blogEntry.getEscapedDescription() %></description>
            <language><%= blogInformation.getBlogLanguage() %></language>


        <%
             List blogComments = blogEntry.getComments();
             if (blogComments != null) {
             for (int j = 0; j < blogComments.size(); j++) {
                   BlogComment blogComment = (BlogComment) blogComments.get(j);
        %>
            <item>
    		        <title><%= blogEntry.getEscapedTitle() %></title>
            		<link><%= blogEntry.getEscapedLink() %></link>
                    <description><%= blogComment.getComment() %></description>
           </item>
        <%
               }
            }
        %>


    <%
            }
        }
    %>

   </channel>
</rss>