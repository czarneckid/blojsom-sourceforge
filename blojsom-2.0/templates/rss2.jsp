<?xml version="1.0"?>
<!-- name="generator" content="<%= request.getAttribute(BlojsomConstants.BLOJSOM_VERSION) %>" -->
<%@ page import="org.blojsom.blog.Blog,
                 org.blojsom.util.BlojsomConstants,
                 org.blojsom.blog.BlogEntry,
                 org.blojsom.util.BlojsomUtils"
                 session="false"%>
<%
    Blog blogInformation = (Blog) request.getAttribute(BlojsomConstants.BLOJSOM_BLOG);
    BlogEntry[] blogEntries = (BlogEntry[]) request.getAttribute(BlojsomConstants.BLOJSOM_ENTRIES);
    String blogSiteURL = (String) request.getAttribute(BlojsomConstants.BLOJSOM_SITE_URL);
    boolean blogCommentsEnabled = true;
    if (request.getAttribute(BlojsomConstants.BLOJSOM_COMMENTS_ENABLED) != null) {
        blogCommentsEnabled = ((Boolean) request.getAttribute(BlojsomConstants.BLOJSOM_COMMENTS_ENABLED)).booleanValue();
    }


%>
<rss version="2.0" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:wfw="http://wellformedweb.org/CommentAPI/">
  <channel>
    <title><%= blogInformation.getBlogName() %></title>
    <link><%= blogInformation.getBlogURL() %></link>
    <description><%= blogInformation.getBlogDescription() %></description>
    <language><%= blogInformation.getBlogLanguage() %></language>
    <image>
       <url><%= blogSiteURL %>/favicon.ico</url>
       <title><%= blogInformation.getBlogName() %></title>
       <link><%= blogInformation.getBlogURL() %></link>
    </image>
    <docs>http://backend.userland.com/rss</docs>
    <generator>blojsom</generator>
	<dc:publisher><%= blogInformation.getBlogOwner()%></dc:publisher>
	<dc:creator><%= blogInformation.getBlogOwnerEmail()%></dc:creator>
    <%
        if (blogEntries != null) {
            for (int i = 0; i < blogEntries.length; i++) {
                BlogEntry blogEntry = blogEntries[i];
    %>
    	<item>
    		<title><%= blogEntry.getEscapedTitle() %></title>
    		<link><%= blogEntry.getEscapedLink() %></link>
    		<description><%= blogEntry.getEscapedDescription() %></description>
            <guid isPermaLink="true"><%= blogEntry.getEscapedLink() %></guid>
			<pubDate><%= blogEntry.getRFC822Date() %></pubDate>
            <% if (blogCommentsEnabled && blogEntry.supportsComments() && !BlojsomUtils.checkMapForKey(blogEntry.getMetaData(), "blog-entry-comments-disabled")) { %>
			  <wfw:comment><%= blogInformation.getBlogBaseURL()%>/commentapi/<%= blogEntry.getId()%></wfw:comment>
              <wfw:commentRss><%= blogEntry.getEscapedLink()%>&amp;page=comments&amp;flavor=rss</wfw:commentRss>
            <% } %>
    	</item>
    <%
            }
        }
    %>
   </channel>
</rss>