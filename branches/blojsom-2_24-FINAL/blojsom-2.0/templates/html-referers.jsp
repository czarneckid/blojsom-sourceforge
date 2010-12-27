<%@ page contentType="text/html; charset=UTF-8"
         import="org.blojsom.blog.Blog,
		 org.blojsom.util.BlojsomConstants,
                 java.util.Map,
                 java.util.HashMap,
                 org.blojsom.plugin.referer.RefererLogPlugin,
                 java.util.Iterator,
                 org.blojsom.plugin.referer.BlogRefererGroup,
                 org.blojsom.plugin.referer.BlogReferer"
		 session="false"%>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<%
    Blog blogInformation = (Blog) request.getAttribute(BlojsomConstants.BLOJSOM_BLOG);
    String blogSiteURL = (String) request.getAttribute(BlojsomConstants.BLOJSOM_SITE_URL);
%>

<title><%=blogInformation.getBlogName()%></title>
<link rel="stylesheet" href="<%= blogSiteURL %>/blojsom.css" />
<link rel="SHORTCUT ICON" href="<%= blogSiteURL %>/favicon.ico" />

</head>

<body>

<h1><a href="<%=blogInformation.getBlogURL()%>"><%=blogInformation.getBlogName()%></a></h1>
<h3><%=blogInformation.getBlogDescription()%></h3>

<p class="categorylist">Complete Referer History:</p>



<% Map  refererGroups = (HashMap)request.getAttribute(RefererLogPlugin.REFERER_CONTEXT_NAME);

    if( refererGroups != null ) {
        Iterator _rgi  = refererGroups.keySet().iterator();
        while ( _rgi.hasNext() ) {
            String groupKey = (String)_rgi.next();
            BlogRefererGroup group = (BlogRefererGroup)refererGroups.get(groupKey);
            if (group.isHitCounter()) {
            %> <p class="weblogtitle2"><%=groupKey%> hits:&nbsp;<%= group.getReferralCount()%></p><p/><%
            } else{
            %> <p class="weblogtitle2"><%=groupKey%> referers&nbsp;<span class="refererhistory">(<%= group.getReferralCount()%> total)</span></p>
               <p class="weblogdateline"> <%
                Iterator _gri  = group.keySet().iterator();
                while ( _gri.hasNext() ) {
                    String refererKey = (String)_gri.next();
                    BlogReferer referer= (BlogReferer)group.get(refererKey);
                    if( referer.isToday()) {
                      %><a href="<%= refererKey %>" rel="nofollow"><%= refererKey %></a>&nbsp;(<%= referer.getCount() %>)<br/><%
                    }
                }
               %></p><%

        }
        }
    }
%>

<p/>
<p>
<a href="http://blojsom.sf.net"><img src="$BLOJSOM_SITE_URL/powered-by-blojsom.gif"  alt="Powered By blojsom" border="0"/></a>&nbsp;&nbsp;
<a href="$BLOJSOM_REQUESTED_CATEGORY.getCategoryURL()?flavor=rss"><img src="$BLOJSOM_SITE_URL/xml.gif"  alt="RSS Feed" border="0"/></a>&nbsp;
<a href="$BLOJSOM_REQUESTED_CATEGORY.getCategoryURL()?flavor=rss2"><img src="$BLOJSOM_SITE_URL/rss.gif"  alt="RSS2 Feed" border="0"/></a>&nbsp;
<a href="$BLOJSOM_REQUESTED_CATEGORY.getCategoryURL()?flavor=rdf"><img src="$BLOJSOM_SITE_URL/rdf.gif"  alt="RDF Feed" border="0"/></a>
</p>


</body>

</html>