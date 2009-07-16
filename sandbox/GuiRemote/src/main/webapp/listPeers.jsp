<%@taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
	<%@ include file="headers.jsp" %>
	<title>Peer List</title>
</head>
<body>
	<%@ include file="top.jsp" %>
	<div id="main">
	<h2>Peer List:</h2>
	
	<!-- displaying the peer graph -->
	<img src="<s:url action="ViewChart"/>?varName=org.paxle.crawler%2Fppm"/>
	<img src="<s:url action="ViewChart"/>?varName=org.paxle.lucene-db%2Fdocs.known"/>
	
	
	<!-- displaying the peer list -->
	<s:if test="peers.size > 0">
		<table>
		<tr>
			<th>Name</th>
			<th>Address</th>
			<th>Actions</th>
		</tr>		
	      <s:iterator value="peers" status="rowstatus">
	        <tr>
	          <td><s:property value="name" /></td>
	          <td><s:property value="address" /></td>
	          <td>				
				<a href='<s:url action="ViewPeer">
	            	<s:param name="peerUrl" value="address"/>
	        	</s:url>'>Status</a> |
	        	<a href='<s:url action="ListPeerConfigList">
	            	<s:param name="peerUrl" value="address"/>
	        	</s:url>'>Config</a>
	          </td>
	        </tr>
	      </s:iterator>
	    </table>
	</s:if>
	<s:else>
		No Peers found!
	</s:else>	
    
    <a href="<s:url action="AddPeer"/>">Add Peer</a>
    </div>
</body>
</html>