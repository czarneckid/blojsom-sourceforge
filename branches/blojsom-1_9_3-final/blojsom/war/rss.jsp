<?xml version="1.0"?>
<!DOCTYPE rss PUBLIC "-//Netscape Communications//DTD RSS 0.91//EN" "http://my.netscape.com/publish/formats/rss-0.91.dtd">
<!-- name="generator" content="blojsom v1.9.2" -->
<%@ page import="org.ignition.blojsom.blog.Blog,
                 org.ignition.blojsom.util.BlojsomConstants,
                 org.ignition.blojsom.blog.BlogEntry"
                 session="false"%>
<%
    Blog blogInformation = (Blog) request.getAttribute(BlojsomConstants.BLOJSOM_BLOG);
    BlogEntry[] blogEntries = (BlogEntry[]) request.getAttribute(BlojsomConstants.BLOJSOM_ENTRIES);
%>
<rss version="0.92">
  <channel>
    <title><%= blogInformation.getBlogName() %></title>
    <link><%= blogInformation.getBlogURL() %></link>
    <description><%= blogInformation.getBlogDescription() %></description>
    <language><%= blogInformation.getBlogLanguage() %></language>
    <%
        if (blogEntries != null) {
            for (int i = 0; i < blogEntries.length; i++) {
                BlogEntry blogEntry = blogEntries[i];
    %>
    	<item>
    		<title><%= blogEntry.getEscapedTitle() %></title>
    		<link><%= blogEntry.getEscapedLink() %></link>
    		<description><%= blogEntry.getEscapedDescription() %></description>
			<wfw:comment xmlns:wfw="http://wellformedweb.org/CommentAPI/">
                 <%= blogInformation.getBlogBaseURL()%>/commentapi/<%= blogEntry.getId()%>
            </wfw:comment>
    	</item>
    <%
            }
        }
    %>
   </channel>
</rss>