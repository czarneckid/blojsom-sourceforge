<%@ page contentType="text/html; charset=UTF-8"
         import="org.blojsom.blog.Blog,
		 org.blojsom.util.BlojsomConstants,
		 org.blojsom.blog.BlogEntry,
		 org.blojsom.blog.BlogCategory,
                 java.util.Map,
                 java.util.HashMap,
                 org.blojsom.plugin.referer.RefererLogPlugin,
                 java.util.Iterator,
                 org.blojsom.plugin.referer.BlogRefererGroup,
                 org.blojsom.plugin.referer.BlogReferer,
                 org.blojsom.plugin.calendar.BlogCalendar,
                 org.blojsom.plugin.calendar.AbstractCalendarPlugin,
                 org.blojsom.plugin.calendar.VelocityHelper,
                 org.blojsom.util.BlojsomUtils"
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
    boolean blogCommentsEnabled = true;
    if (request.getAttribute(BlojsomConstants.BLOJSOM_COMMENTS_ENABLED) != null) {
        blogCommentsEnabled = ((Boolean) request.getAttribute(BlojsomConstants.BLOJSOM_COMMENTS_ENABLED)).booleanValue();
    }
    boolean blogTrackbacksEnabled = true;
    if (request.getAttribute("BLOJSOM_TRACKBACK_PLUGIN_ENABLED") != null) {
        blogTrackbacksEnabled = ((Boolean) request.getAttribute("BLOJSOM_TRACKBACK_PLUGIN_ENABLED")).booleanValue();
    }

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
    </head>

    <body>

<!-- Search Box Code -->
<div id="searchbox">
<form class="searchform" method="post" action="<%= blogInformation.getBlogURL() %>">
<p class="searchtext">
Search:&nbsp;&nbsp;<input size="14" type="text" name="query" value=""/>&nbsp;
<input class="searchimage" type="image" src="<%= blogSiteURL %>/search.png" value="submit" alt="Search"/>
</p>
</form>
</div>


	<h1><a href="<%= blogInformation.getBlogURL() %>"><%= blogInformation.getBlogName() %></a></h1>
	<h3><%= blogInformation.getBlogDescription() %></h3>

	<p>Available Categories: <%= catString %></p>
<table class="mastertable">
<tr>
<td class="tablemaxwidth">

<%
	if (entryArray != null) {
	    for (int i = 0; i < entryArray.length; i++) {
		BlogEntry blogEntry = entryArray[i];
%>
		<div class="entrystyle">
		<p class="weblogtitle"><%= blogEntry.getTitle() %> <span class="smalltext">[<a href="<%= blogEntry.getLink() %>" title="Permalink to this blojsom entry">Permalink</a>]</span></p>
		<p class="weblogdateline"><%= blogEntry.getDate() %><br />
        <%
            String categoryName;
            if (blogEntry.getBlogCategory().getName() != null) {
                categoryName = blogEntry.getBlogCategory().getName();
            } else {
                categoryName = blogEntry.getCategory();
            }
        %>
        Category [<%= categoryName %>]</p>
		<p><%= blogEntry.getDescription() %></p>
		</div>
        <p class="weblogbottomline">
        <%
            Map entryMetaData;
            entryMetaData = blogEntry.getMetaData();
            if (entryMetaData != null) {
                if (entryMetaData.containsKey("blog-entry-author")) {
        %>
                    Posted by: <b><%= entryMetaData.get("blog-entry-author") %></b><br />
        <%
                }
            }
        %>
        <% if (blogCommentsEnabled && blogEntry.supportsComments() && !BlojsomUtils.checkMapForKey(blogEntry.getMetaData(), "blog-entry-comments-disabled")) { %>
        Comments [<a href="<%= blogEntry.getLink() %>&amp;page=comments"><%= blogEntry.getNumComments() %></a>] |
        <% } %>
        <% if (blogTrackbacksEnabled && blogEntry.supportsTrackbacks() && !BlojsomUtils.checkMapForKey(blogEntry.getMetaData(), "blog-entry-trackbacks-disabled")) { %>
        Trackbacks [<a href="<%= blogEntry.getLink() %>&amp;page=trackback"><%= blogEntry.getNumTrackbacks() %></a>]
        <% } %>
        </p>

        <% if ( entryArray.length == 1 ) {



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
    <% } %>


<%
	    }
	}
%>
</td>
<td class="tablemaxpxwidth">&nbsp;</td>

<td valign="top" align="center" width="200">

<%
    BlogCalendar blogCalendar = (BlogCalendar)request.getAttribute(AbstractCalendarPlugin.BLOJSOM_CALENDAR);
    VelocityHelper vtlHelper = (VelocityHelper )request.getAttribute(AbstractCalendarPlugin.BLOJSOM_CALENDAR_VTLHELPER);
%>


<div class="calendarbox">
<table class="calendartable"><tr><td colspan="7" class="calendarcaption"><b><%= blogCalendar.getCaption()%></b></td></tr>

<!-- DOW Name Row -->
<tr>

<%
     String[] dowNames = blogCalendar.getShortDayOfWeekNames();
     for (int x = 0; x < dowNames.length; x++) {
     %><td width="19" class="calendarcolumn"><%= dowNames[x]%>&nbsp;</td><%
     }%>
</tr>


<tr>
    <%= vtlHelper.getCalendarRow(1,"calendarcolumn")%>
</tr>
<tr>
    <%= vtlHelper.getCalendarRow(2,"calendarcolumn")%>
</tr>
<tr>
    <%= vtlHelper.getCalendarRow(3,"calendarcolumn")%>
</tr>
<tr>
    <%= vtlHelper.getCalendarRow(4,"calendarcolumn")%>
</tr>
<tr>
    <%= vtlHelper.getCalendarRow(5,"calendarcolumn")%>
</tr>
<tr>
    <%= vtlHelper.getCalendarRow(6,"calendarcolumn")%>
</tr>

<tr>
<td colspan="7" class="calendarcolumn"><%= vtlHelper.getPreviousMonth()%>&nbsp;&nbsp;&nbsp;<%= vtlHelper.getToday()%>&nbsp;&nbsp;&nbsp;<%= vtlHelper.getNextMonth()%></td>
</tr>
</table>
</div>



</td>
</tr>
</table >


<%
    if ((entryArray != null) && (entryArray.length > 0)) {
%>
	<p>Available Categories: <%= catString %></p>
<%
    }
%>

    <p>
    <a href="http://blojsom.sf.net"><img src="<%= blogSiteURL %>/powered-by-blojsom.gif" border="0" alt="Powered By blojsom"/></a>&nbsp;&nbsp;
    <a href="<%= requestedCategory.getCategoryURL() %>?flavor=rss"><img src="<%= blogSiteURL %>/xml.gif" border="0" alt="RSS Feed"/></a>&nbsp;
    <a href="<%= requestedCategory.getCategoryURL() %>?flavor=rss2"><img src="<%= blogSiteURL %>/rss.gif" border="0" alt="RSS2 Feed"/></a>&nbsp;
    <a href="<%= requestedCategory.getCategoryURL() %>?flavor=rdf"><img src="<%= blogSiteURL %>/rdf.gif" border="0" alt="RDF Feed"/></a>
    <p>


<!-- Optional Code if you are using the referer plugin -->
<% Map  refererGroups = (HashMap)request.getAttribute(RefererLogPlugin.REFERER_CONTEXT_NAME);

    if( refererGroups != null ) {
        Iterator _rgi  = refererGroups.keySet().iterator();
        while ( _rgi.hasNext() ) {
            String groupKey = (String)_rgi.next();
            BlogRefererGroup group = (BlogRefererGroup)refererGroups.get(groupKey);
            if (group.isHitCounter()) {
            %> <p class="weblogtitle2"><%=groupKey%> hits:&nbsp;<a href="<%= blogInformation.getBlogURL()%>?&amp;page=referers" title="Referer History"><%= group.getReferralCount()%></a></p><p/><%
            } else{
            %> <p class="weblogtitle2"><%=groupKey%> referers today&nbsp;<span class="refererhistory">(<a href="<%= blogInformation.getBlogURL()%>?&amp;page=referers" title="Referer History"><%= group.getReferralCount()%> overall</a>)</span></p>
               <p class="weblogdateline"> <%
                Iterator _gri  = group.keySet().iterator();
                while ( _gri.hasNext() ) {
                    String refererKey = (String)_gri.next();
                    BlogReferer referer= (BlogReferer)group.get(refererKey);
                    if( referer.isToday()) {
                      %><a href="<%= refererKey %>"><%= refererKey %></a>&nbsp;(<%= referer.getCount() %>)<br/><%
                    }
                }
               %></p><%

        }
        }
    }
%>


    </body>
</html>
