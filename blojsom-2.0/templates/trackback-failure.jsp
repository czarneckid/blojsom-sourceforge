<?xml version="1.0" encoding="UTF-8"?>
<%@ page contentType="text/xml; charset=UTF-8"
         import="org.blojsom.plugin.trackback.TrackbackPlugin"%>
<%
    Integer blojsomTrackbackReturnCode = (Integer) request.getAttribute(TrackbackPlugin.BLOJSOM_TRACKBACK_RETURN_CODE);
    String blojsomTrackbackMessage = (String) request.getAttribute(TrackbackPlugin.BLOJSOM_TRACKBACK_MESSAGE);
%>
<response>
<error><%= blojsomTrackbackReturnCode.intValue() %></error>
<message><%= blojsomTrackbackMessage %></message>
</response>