<?xml version="1.0" ?>
<%@ page import="org.ignition.blojsom.blog.Blog,
                 org.ignition.blojsom.util.BlojsomConstants"%>

<%
    Blog blogInformation = (Blog) request.getAttribute(BlojsomConstants.BLOJSOM_BLOG);
%>

<rsd version="1.0" xmlns="http://archipelago.phrasewise.com/rsd">
    <service>
        <engineName>blojsom 1.9</engineName>
        <engineLink>http://blojsom.sf.net</engineLink>
        <homePageLink><%= blogInformation.getBlogURL() %></homePageLink>
        <apis>
            <api name="blogger" preferred="true" apiLink="<%= blogInformation.getBlogBaseURL() %>/xmlrpc/" blogID=""/>
            <api name="metaWeblog" preferred="false" apiLink="<%= blogInformation.getBlogBaseURL() %>/xmlrpc" blogID=""/>
        </apis>
    </service>
</rsd>