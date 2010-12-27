<%@ page contentType="application/atom+xml; charset=UTF-8"
         import="org.blojsom.blog.Blog,
                 org.blojsom.util.BlojsomConstants,
                 org.blojsom.blog.BlogEntry"%>
<%
    Blog blogInformation = (Blog) request.getAttribute(BlojsomConstants.BLOJSOM_BLOG);
    BlogEntry[] blogEntries = (BlogEntry[]) request.getAttribute(BlojsomConstants.BLOJSOM_ENTRIES);
%>
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet href="<%= blogInformation.getBlogBaseURL() %>/atom.css" type="text/css"?>

<feed version="0.3"
    xmlns="http://purl.org/atom/ns#"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xml:lang="<%= blogInformation.getBlogLanguage() %>">

    <!-- required feed elements -->
    <title mode="escaped"><%= blogInformation.getBlogName() %></title>
    <link rel="alternate" type="text/html" href="<%= blogInformation.getBlogURL() %>"/>
    <modified><%= request.getAttribute(BlojsomConstants.BLOJSOM_DATE_UTC) %></modified>

    <!-- optional feed elements -->
    <info type="application/xhtml+xml" mode="xml">
        <div xmlns="http://www.w3.org/1999/xhtml">
        This is an Atom syndication feed. It is intended to be viewed in a news aggregator or syndicated to
        another site.  Please visit the <a href="http://intertwingly.net/wiki/pie/">Atom Project</a> for
        more information.
        </div>
    </info>

    <!-- in a single-author feed (like an individual weblog), put author at the feed level;
    in a multi-author feed (like a group weblog or a comments feed), put author at the entry level -->
    <author>
        <!-- required author elements -->
        <name><%= blogInformation.getBlogOwner() %></name>
        <!-- optional author elements -->
        <url><%= blogInformation.getBlogURL() %></url>
        <email><%= blogInformation.getBlogOwnerEmail() %></email>
    </author>

    <!-- optional feed elements -->
    <tagline><%= blogInformation.getBlogDescription().replaceAll("<.*?>","")%></tagline>
    <generator url="http://blojsom.sf.net" version="<%= request.getAttribute(BlojsomConstants.BLOJSOM_VERSION) %>">blojsom</generator>
    <copyright mode="escaped">Copyright &#169; <%= blogInformation.getBlogOwner() %></copyright>

    <%
        if (blogEntries != null) {
            for (int i = 0; i < blogEntries.length; i++) {
                BlogEntry blogEntry = blogEntries[i];
    %>
    <entry>
        <!-- entry required elements -->
        <title mode="escaped"><%= blogEntry.getEscapedTitle().replaceAll("<.*?>","")%></title>
        <link rel="alternate" type="text/html" href="<%= blogEntry.getLink() %>"/>
        <modified><%= blogEntry.getDateAsFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") %></modified>
        <issued><%= blogEntry.getISO8601Date() %></issued>
        <id>tag:<%= blogInformation.getBlogOwnerEmail() %>,<%= blogEntry.getDateAsFormat("yyyy-MM-dd") %>:<%= blogEntry.getEncodedCategory() %>.<%= blogEntry.getPermalink() %></id>

        <!-- entry optional elements -->
        <created><%= blogEntry.getDateAsFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") %></created>
        <content type="text/html" mode="escaped" xml:lang="<%= blogInformation.getBlogLanguage() %>" xml:base="<%= blogInformation.getBlogBaseURL() %>">
            <![CDATA[ <%= blogEntry.getDescription() %> ]]>
        </content>
    </entry>
    <%
            }
        }
    %>
</feed>
