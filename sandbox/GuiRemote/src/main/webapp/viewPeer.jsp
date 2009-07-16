<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<%@ include file="headers.jsp" %>	
	<title>View Peer Status</title>
</head>
<body>
<%@ include file="top.jsp" %>
<div id="main">
	<h2>System Monitoring</h2>
	<div class="summary"><s:iterator value="peerData">
		<div style="white-space:nowrap;">|&nbsp;<a href='#<s:property value="id" />'><s:property value="id"/></a>&nbsp;</div>
	</s:iterator>&nbsp;|
	</div>

	<table>
	<tr>
		<th>Name</th>
		<th>Type</th>
		<th>Date</th>
		<th>Value</th>
		<th>Description</th>
	</tr>
	<s:iterator value="peerData" status="rowstatus">
		<tr>
			<th class="sub" colspan="5"><a name='<s:property value="id" />'><s:property value="id" /></a></th>
		</tr>	
		<s:iterator value="variables">
		<tr>
			<td><tt><b><s:property value="id" /></b></tt></td>
			<td><tt><s:property value="type" /></tt></td>
			<td><tt><s:property value="timestamp" /></tt></td>
			<td><tt><s:property value="value" /></tt></td>
			<td><div class="descriptionText"><s:property value="description" /></div></td>
		</tr>
		</s:iterator>
		
	</s:iterator>
	</table>
	<div class="buttonBar">
		<a class="button" id="backButton" href='<s:url action="ListPeers"/>' >Back to overview</a>
	</div>	
</div>
</body>
</html>