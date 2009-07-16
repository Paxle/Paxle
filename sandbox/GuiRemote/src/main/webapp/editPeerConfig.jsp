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
	
		<p>
			<p>
				<img src='<s:property value="peerUrl" />/configurations/<s:property value="bundleId" />/<s:property value="pid" />/icon?size=16'/>
				<strong><s:property value="peerConfigData['name']" /></strong>&nbsp;[Bundle <a name='bundleID'><s:property value="bundleId" /></a>]<br/>
				<div class="descriptionText"><s:property value="peerConfigData['description']" /></div>
			</p>
		</p>
		<div>The current configuration of this service</div>			

		<s:actionerror />			
		<s:form action="PeerConfigEdit">
			<s:hidden name="peerUrl" />
			<s:hidden name="pid" />
			<s:hidden name="bundleId" />
			<s:iterator value="properties" status="stat">
				<s:if test="type=='Boolean'">
					<s:radio list="#{true:'yes', false: 'no'}"
						label="%{name}" 
						name="properties[%{#stat.index}].value"
						tooltip="%{description}"
						javascriptTooltip="true" />
				</s:if>
				<s:else>
					<s:if test="options != null">
						<s:select list="options"
							multiple="%{#isMulti =:[#this==0 ? false : true], #isMulti(cardinality)}"
							size="%{#size =:[#this==0 ? 1 : #this>5 ? 5 : #this ], #size(cardinality)}"
							label="%{name}"
							name="properties[%{#stat.index}].value" 
							tooltip="%{description}"
							javascriptTooltip="true" />
					</s:if>
					<s:else>
						<s:if test="size > 1">
							<s:iterator value="value" status="statsValues">
								<s:textfield 
									label="%{name}"
									name="properties[%{#stat.index}].value[%{#statsValues.index}]" 
									tooltip="%{description}"
									javascriptTooltip="true" />
							</s:iterator>
						</s:if>
						<s:else>
							<s:textfield 
								label="%{name}"
								name="properties[%{#stat.index}].value" 
								tooltip="%{description}"
								javascriptTooltip="true" />
						</s:else>
					</s:else>
				</s:else>
			</s:iterator>
			<s:submit value="Update"/>
		</s:form>
</div>
</body>
</html>