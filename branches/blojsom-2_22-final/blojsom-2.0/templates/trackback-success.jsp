<%@ page contentType="text/xml; charset=UTF-8"
         import="org.blojsom.plugin.trackback.TrackbackPlugin"%>
<?xml version="1.0" encoding="UTF-8"?>
<%
    Integer blojsomTrackbackReturnCode = (Integer) request.getAttribute(TrackbackPlugin.BLOJSOM_TRACKBACK_RETURN_CODE);
%>
<response>
<error><%= blojsomTrackbackReturnCode.intValue() %></error>
</response>