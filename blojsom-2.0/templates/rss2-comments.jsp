<?xml version="1.0"?>
<!-- name="generator" content="<%= request.getAttribute(BlojsomConstants.BLOJSOM_VERSION) %>" -->
<%@ page import="org.blojsom.blog.Blog,
                 org.blojsom.util.BlojsomConstants,
                 org.blojsom.blog.BlogEntry,
                 org.blojsom.util.BlojsomUtils,
                 java.util.ArrayList,
                 org.blojsom.blog.BlogComment"
                 session="false"%>
<%
    Blog blogInformation = (Blog) request.getAttribute(BlojsomConstants.BLOJSOM_BLOG);
    BlogEntry[] blogEntries = (BlogEntry[]) request.getAttribute(BlojsomConstants.BLOJSOM_ENTRIES);
    String blogSiteURL = (String) request.getAttribute(BlojsomConstants.BLOJSOM_SITE_URL);
%>

<rss version="2.0" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:wfw="http://wellformedweb.org/CommentAPI/">
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
			<pubDate><%= blogEntry.getRFC822Date() %></pubDate>
            <image>
              <url><%= blogSiteURL %>/favicon.ico</url>
              <title><%= blogInformation.getBlogName() %></title>
              <link><%= blogInformation.getBlogURL() %></link>
            </image>
            <docs>http://backend.userland.com/rss</docs>
            <generator><%= request.getAttribute(BlojsomConstants.BLOJSOM_VERSION) %></generator>
	        <dc:publisher><%= blogInformation.getBlogOwner()%></dc:publisher>
	        <dc:creator><%= blogInformation.getBlogOwnerEmail()%></dc:creator>


        <%
             ArrayList blogComments = blogEntry.getComments();
             if (blogComments != null) {
             for (int j = 0; j < blogComments.size(); j++) {
                   BlogComment blogComment = (BlogComment) blogComments.get(j);
        %>
            <item>
    		    <title><%= blogEntry.getEscapedTitle() %></title>
                <link><%= blogEntry.getEscapedLink() %></link>
                <description><%= blogComment.getComment() %></description>
			    <pubDate><%= blogComment.getRFC822Date() %></pubDate>
	            <dc:creator><%= blogComment.getAuthor()%></dc:creator>
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