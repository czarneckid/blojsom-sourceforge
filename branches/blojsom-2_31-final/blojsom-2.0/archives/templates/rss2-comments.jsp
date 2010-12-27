<?xml version="1.0" encoding="UTF-8"?>
<%@ page contentType="text/xml; charset=UTF-8"
         import="org.blojsom.blog.Blog,
                 org.blojsom.util.BlojsomConstants,
                 org.blojsom.blog.BlogEntry,
                 org.blojsom.util.BlojsomUtils,
                 java.util.List,
                 org.blojsom.blog.BlogComment"
         session="false"%>
<!-- name="generator" content="<%= request.getAttribute(BlojsomConstants.BLOJSOM_VERSION) %>" -->
<%
    Blog blogInformation = (Blog) request.getAttribute(BlojsomConstants.BLOJSOM_BLOG);
    BlogEntry[] blogEntries = (BlogEntry[]) request.getAttribute(BlojsomConstants.BLOJSOM_ENTRIES);
%>

<rss version="2.0">
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
            <docs>http://blogs.law.harvard.edu/tech/rss</docs>
            <generator><%= request.getAttribute(BlojsomConstants.BLOJSOM_VERSION) %></generator>
            <pubDate><%= request.getAttribute(BlojsomConstants.BLOJSOM_DATE) %></pubDate>
            <category><%= blogEntry.getEncodedCategory() %></category>
	        <managingEditor><%= request.getAttribute(BlojsomConstants.BLOG_OWNER_EMAIL) %></managingEditor>
            <webMaster><%= request.getAttribute(BlojsomConstants.BLOG_OWNER_EMAIL) %></webMaster>
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
			    <pubDate><%= blogComment.getRFC822Date() %></pubDate>
	            <author><%= blogComment.getAuthorEmail() %></author>
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