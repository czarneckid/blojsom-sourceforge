<%@ page contentType="text/html; charset=UTF-8"
         import="org.blojsom.blog.Blog,
                 org.blojsom.util.BlojsomConstants,
                 org.blojsom.blog.BlogEntry,
                 org.blojsom.blog.BlogCategory,
                 org.blojsom.blog.BlogComment,
                 org.blojsom.plugin.comment.CommentPlugin,
                 java.util.List"
		 session="false"%>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<%
            Blog blogInformation = (Blog) request.getAttribute(BlojsomConstants.BLOJSOM_BLOG);
            BlogEntry[] entryArray = (BlogEntry[]) request.getAttribute(BlojsomConstants.BLOJSOM_ENTRIES);
            BlogCategory[] blogCategories = (BlogCategory[]) request.getAttribute(BlojsomConstants.BLOJSOM_CATEGORIES);
            String blogSiteURL = (String) request.getAttribute(BlojsomConstants.BLOJSOM_SITE_URL);
            BlogCategory requestedCategory = (BlogCategory) request.getAttribute(BlojsomConstants.BLOJSOM_REQUESTED_CATEGORY);
            boolean blogCommentsEnabled = ((Boolean) request.getAttribute(BlojsomConstants.BLOJSOM_COMMENTS_ENABLED)).booleanValue();


            StringBuffer catStringBuf = new StringBuffer(20);
            String blogName = null;
            for (int j = 0; j < blogCategories.length; j++) {
                BlogCategory blogCategory = blogCategories[j];
                blogName = blogCategory.getName();
                if ((blogName == null) || (blogName.length() < 1)) {
                    blogName = blogCategory.getCategory();
                }
                catStringBuf.append("[<i><a href=").append(blogCategory.getCategoryURL());
                catStringBuf.append(">").append(blogName).append("</a></i>]");
            }
            String catString = catStringBuf.toString();
%>

    <head>
	<title><%= blogInformation.getBlogName() %></title>
	<link rel="stylesheet" href="<%= blogSiteURL %>/blojsom.css" />
    <link rel="SHORTCUT ICON" href="<%= blogSiteURL %>/favicon.ico" />
    <link rel="alternate" type="application/rss+xml" title="RSS" href="<%= blogInformation.getBlogURL() %>?flavor=rss" />
    <link rel="EditURL" type="application/rsd+xml" title="RSD" href="<%= blogInformation.getBlogURL() %>?flavor=rsd" />
    <script type="text/javascript">
    function reloadPreviewDiv() {
        var previewString = document.getElementById('commentText').value;
        document.getElementById('commentPreview').innerHTML = previewString;
    }
    </script>
    </head>

    <body>
	<h1><a href="<%= blogInformation.getBlogURL() %>"><%= blogInformation.getBlogName() %></a></h1>
	<h3><%= blogInformation.getBlogDescription() %></h3>

	<p>Available Categories: <%= catString %></p>
<%
            if (entryArray != null) {
                for (int i = 0; i < entryArray.length; i++) {
                BlogEntry blogEntry = entryArray[i];
%>
    <!-- CommentAPI AutoDiscovery -->
    <link rel="service.comment" type="text/xml" href="<%=blogInformation.getBlogBaseURL()%>/commentapi/<%= blogEntry.getId()%>" title="Comment Interface"/>


    <div class="entrystyle">
    <p class="weblogtitle"><%= blogEntry.getTitle() %> <span class="smalltext">[<a href="<%= blogEntry.getLink() %>">Permalink</a>]</span> </p>
    <p class="weblogdateline"><%= blogEntry.getDate() %></p>
    <p><%= blogEntry.getDescription() %></p>
    </div>

        <%
            String blogDescription =  blogEntry.getEscapedDescription();
            if ( blogDescription.length() > 255 ) {
                blogDescription = blogDescription.substring(0,252)+ "...";
            }
        %>
        <!-- Trackback Auto Discovery -->
<!--
    <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
             xmlns:dc="http://purl.org/dc/elements/1.1/"
             xmlns:trackback="http://madskills.com/public/xml/rss/module/trackback/">
        <rdf:Description
             rdf:about="<%= blogEntry.getLink()%>"
             dc:identifier="<%= blogEntry.getLink()%>"
             dc:title="<%=blogEntry.getTitle()%>"
             dc:subject="<%=requestedCategory.getCategoryURL()%>"
             dc:description="<%=blogDescription%>"
             dc:creator="<%= blogInformation.getBlogOwner() %>"
             dc:date="<%= blogEntry.getISO8601Date()%>"
             trackback:ping="<%= blogEntry.getLink()%>&tb=y" />
     </rdf:RDF>
-->


    <p class="weblogtitle">Comments on this entry</p><br/>
    <div class="entrystyle">
        <%
                    List blogComments = blogEntry.getComments();
                    if (blogComments != null) {
                    for (int j = 0; j < blogComments.size(); j++) {
                        BlogComment blogComment = (BlogComment) blogComments.get(j);
                        String commentAuthorLink;
                        if (blogComment.getAuthorEmail() != null && !"".equals(blogComment.getAuthorEmail())) {
                            commentAuthorLink = "<a href=\"mailto:" + blogComment.getAuthorEmail() + "\">" + blogComment.getAuthor() + "</a>";
                        } else {
                            commentAuthorLink = blogComment.getAuthor();
                        }
        %>
                <div class="commentstyle">
            Comment by: <%= commentAuthorLink %> -
                <a href="<%= blogComment.getAuthorURL() %>" rel="nofollow"><%= blogComment.getAuthorURL() %></a>
            <div class="weblogdateline">Left on: <%= blogComment.getCommentDate() %></div><br/>
            <%= blogComment.getComment() %><br />
        </div>

        <%
                    }
            }
        %>
        </div>

        <%
         if (blogCommentsEnabled && blogEntry.supportsComments()) {

         String commentAuthor = (String) request.getAttribute(CommentPlugin.BLOJSOM_COMMENT_PLUGIN_AUTHOR);
         String commentAuthorEmail = (String) request.getAttribute(CommentPlugin.BLOJSOM_COMMENT_PLUGIN_AUTHOR_EMAIL);
         String commentAuthorURL = (String) request.getAttribute(CommentPlugin.BLOJSOM_COMMENT_PLUGIN_AUTHOR_URL);
         String commentRememberMe = (String) request.getAttribute(CommentPlugin.BLOJSOM_COMMENT_PLUGIN_REMEMBER_ME);
         String initialAuthor = (null == commentAuthor) ? "" : commentAuthor;
         String initialAuthorEmail = (null == commentAuthorEmail) ? "" : commentAuthorEmail;
         String initialAuthorURL = (null == commentAuthorURL) ? "" : commentAuthorURL;
        %>

        <hr />
    <table>
        <form name="commentform" method="post" action=".">
            <input type="hidden" name="comment" value="y"/>
            <input type="hidden" name="page" value="comments"/>
            <input type="hidden" name="category" value="<%= requestedCategory.getCategory() %>"/><br />
            <input type="hidden" name="permalink" value="<%= blogEntry.getPermalink() %>"/> <br />

            <tr>
                <td>Author (<font color="red">*</font>):</td><td><input type="text" name="author" value="<%= initialAuthor %>"/></td>
            </tr>
            <tr>
                <td>E-mail:</td><td><input type="text" name="authorEmail" value="<%= initialAuthorEmail %>"/></td>
            </tr>
            <tr>
                <td>URL: </td><td><input type="text" name="authorURL" value="<%= initialAuthorURL %>"/></td>
            </tr>
            <tr>
                <td>Comment (<font color="red">*</font>):</td><td><textarea name="commentText" id="commentText" value="" rows="7" cols="55" onkeyup="reloadPreviewDiv();"></textarea></td>
            </tr>
            <tr>
                <td>Remember me?</td> <td><input type="checkbox" name="remember" <% if (commentRememberMe != null && !"".equals(commentRememberMe)) { %>CHECKED<% } %>/></td>
            </tr>
            <p />
            <tr>
                <td colspan="2"><input type="submit" name="submit" value="Submit Comment"/>
                <input type="reset" name="reset" value="Reset"/>
                </td>
            </tr>
            <tr></tr>
            <tr>
                <td colspan="2"><h4>Live Comment Preview</h4></td>
            </tr>
            <tr>
                <td colspan="2">
                    <div id="commentPreview">
                    </div>
                </td>
            </tr>
        </form>
    </table>
        <% } %>
<%
                }
            }
%>

<p />

<%
            if ((entryArray != null) && (entryArray.length > 0)) {
%>
	<p>Available Categories: <%= catString %></p>
<%
            }
%>

    <p />
    <a href="http://blojsom.sf.net"><img src="<%= blogSiteURL %>/powered-by-blojsom.gif" border="0" alt="Powered By blojsom"/></a>&nbsp;&nbsp;
    <a href="<%= requestedCategory.getCategoryURL() %>?flavor=rss"><img src="<%= blogSiteURL %>/xml.gif" border="0" alt="RSS Feed"/></a>&nbsp;
    <a href="<%= requestedCategory.getCategoryURL() %>?flavor=rss2"><img src="<%= blogSiteURL %>/rss.gif" border="0" alt="RSS2 Feed"/></a>&nbsp;
    <a href="<%= requestedCategory.getCategoryURL() %>?flavor=rdf"><img src="<%= blogSiteURL %>/rdf.gif" border="0" alt="RDF Feed"/></a>

    </body>
</html>
