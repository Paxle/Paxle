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
	<h2>Config-Management</h2>
		<table>
		<tr>
			<th>&nbsp;</th>
			<th>Name</th>
			<th>Description</th>
		</tr>
		
		<s:iterator value="peerConfigData">
			<tr><th class="sub" colspan="3"><s:property value="bundleName" /><tt></tt></th></tr>
			<s:iterator value="configs">
			<tr>
				<td><img src='<s:property value="peerUrl" />/configurations/<s:property value="bundleId" />/<s:property value="pid" />/icon?size=16'/></td>
				<td>
					<a name='<s:property value="pid" />' 
					   href='<s:url action="PeerConfigView">
	            			<s:param name="peerUrl" value="peerUrl"/>
	            			<s:param name="bundleId" value="bundleId"/>
	            			<s:param name="pid" value="pid"/>
	        		   </s:url>'><s:property value="name" /></a><br/>
					<tt>[<s:property value="pid" />]</tt>
				</td>
				<td class="descriptionText"><s:property value="description" /></td>
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