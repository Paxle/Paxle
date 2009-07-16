<%@taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
	<%@ include file="headers.jsp" %>
	<title>Index</title>
</head>
<body>
	<%@ include file="top.jsp" %>
	<div id="main">
		<h2><s:text name="peer.add.title" /></h2>

		<s:actionerror />
		<s:fielderror />
		
		<s:form action="AddPeer" validate="true">
			<s:textfield label="%{getText('peer.name')}" title="%{getText('peer.name.desc')}" name="name" required="true"></s:textfield>
			<s:textfield label="%{getText('peer.address')}" title="%{getText('peer.address.desc')}" name="address" required="true"></s:textfield>
			<s:submit align="center"/>
		</s:form>
	</div>
</body>
</html>
