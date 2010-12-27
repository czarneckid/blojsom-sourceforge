<?xml version="1.0" encoding="UTF-8"?>
<%@ page contentType="text/xml; charset=UTF-8"
         import="org.blojsom.blog.Blog,
                 org.blojsom.util.BlojsomConstants,
                 org.blojsom.blog.BlogUser"%>

<%
    Blog blogInformation = (Blog) request.getAttribute(BlojsomConstants.BLOJSOM_BLOG);
    String blogUser = (String) request.getAttribute(BlojsomConstants.BLOJSOM_USER);
%>

<rsd version="1.0" xmlns="http://archipelago.phrasewise.com/rsd">
    <service>
        <engineName><%= request.getAttribute(BlojsomConstants.BLOJSOM_VERSION) %></engineName>
        <engineLink>http://blojsom.sf.net</engineLink>
        <homePageLink><%= blogInformation.getBlogURL() %></homePageLink>
        <apis>
            <api name="blogger" preferred="true" apiLink="<%= blogInformation.getBlogBaseURL() %>/xmlrpc/<%= blogUser %>/" blogID=""/>
            <api name="metaWeblog" preferred="false" apiLink="<%= blogInformation.getBlogBaseURL() %>/xmlrpc/<%= blogUser %>/" blogID=""/>
        </apis>
    </service>
</rsd>