<?xml version="1.0"?>
<%@ page import="org.blojsom.blog.Blog,
                 org.blojsom.util.BlojsomConstants,
                 org.blojsom.blog.BlogEntry"%>
<%
    Blog blogInformation = (Blog) request.getAttribute(BlojsomConstants.BLOJSOM_BLOG);
    BlogEntry[] blogEntries = (BlogEntry[]) request.getAttribute(BlojsomConstants.BLOJSOM_ENTRIES);
%>
<feed xmlns="http://purl.org/atom/ns#"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xml:lang="$BLOJSOM_BLOG.getBlogLanguage()"
    version="0.2">

    <!-- feed required elements -->
    <title><%= blogInformation.getBlogName() %></title>
    <link><%= blogInformation.getBlogURL() %></link>
    <modified><%= request.getAttribute(BlojsomConstants.BLOJSOM_DATE_UTC) %></modified>

    <!-- feed optional elements -->
    <tagline><%= blogInformation.getBlogDescription().replaceAll("<.*?>","")%></tagline>
    <generator name="<%= request.getAttribute(BlojsomConstants.BLOJSOM_VERSION) %>">http://blojsom.sf.net</generator>
    <copyright><!-- Insert your copyright --></copyright>

    <%
        if (blogEntries != null) {
            for (int i = 0; i < blogEntries.length; i++) {
                BlogEntry blogEntry = blogEntries[i];
    %>
    <entry>
        <!-- entry required elements -->
        <title><%= blogEntry.getTitle().replaceAll("<.*?>","")%></title>
        <link><%= blogEntry.getLink() %></link>
        <id><%= blogEntry.getLink() %></id>
        <issued><%= blogEntry.getISO8601Date() %></issued>
        <modified><%= blogEntry.getDateAsFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") %></modified>

        <!-- entry optional elements -->
        <created><%= blogEntry.getDateAsFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") %></created>
        <author>
            <name><%= blogInformation.getBlogOwner() %></name>
            <url><%= blogInformation.getBlogURL() %></url>
            <email><%= blogInformation.getBlogOwnerEmail() %></email>
        </author>
        <content type="text/html" xml:lang="<%= blogInformation.getBlogLanguage() %>" mode="escaped">
            <![CDATA[ <%= blogEntry.getDescription() %> ]]>
        </content>
    </entry>
    <%
            }
        }
    %>
</feed>
