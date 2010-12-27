<%@ page import="org.ignition.blojsom.plugin.trackback.TrackbackPlugin"%>
<?xml version="1.0" encoding="iso-8859-1"?>
<%
    Integer blojsomTrackbackReturnCode = (Integer) request.getAttribute(TrackbackPlugin.BLOJSOM_TRACKBACK_RETURN_CODE);
%>
<response>
<error><%= blojsomTrackbackReturnCode.intValue() %></error>
</response>