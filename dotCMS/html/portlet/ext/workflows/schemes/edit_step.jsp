<%@page import="com.dotmarketing.util.UtilMethods"%>
<%@page
	import="com.dotmarketing.portlets.workflows.business.WorkflowAPI"%>
<%@page import="com.dotmarketing.portlets.workflows.model.WorkflowStep"%>
<%@page
	import="com.dotmarketing.portlets.workflows.model.WorkflowScheme"%>
<%@page import="com.dotmarketing.business.APILocator"%>
<%@page import="java.util.List"%>
<%@page import="com.liferay.portal.language.LanguageUtil"%>

<%
	WorkflowAPI wapi = APILocator.getWorkflowAPI();
	String stepId = request.getParameter("stepId");
	WorkflowStep step = wapi.findStep(stepId);
	WorkflowScheme scheme = wapi.findScheme(step.getSchemeId());



%>

<div dojoType="dijit.form.Form" id="addEditStepForm" jsId="addEditStepForm" encType="multipart/form-data" action="/DotAjaxDirector/com.dotmarketing.portlets.workflows.ajax.WfStepAjax" method="POST">
	
	<!-- START Listing Results -->
	<table class="listingTable tableNintyPercent" style="width:90%">
			<input type="hidden" name="cmd" value="reorder">
			<input type="hidden" name="stepId" id="stepId" value="<%=UtilMethods.webifyString(step.getId())%>">
			
			<input type="hidden" name="schemeId" value="<%=UtilMethods.webifyString(scheme.getId())%>">
			<tr>
	
				<th colspan="2"><%=LanguageUtil.get(pageContext, "Workflow-Step")%>
	
				</th>
			</tr>
			<tr>
				<td align="right"><%=LanguageUtil.get(pageContext, "Name")%>:</td>
				<td><input type="text" name="stepName" id=""stepName""
					dojoType="dijit.form.ValidationTextBox"  required="true" 
					value="<%=UtilMethods.webifyString(step.getName())%>"
					maxlength="255">
				</td>
			</tr>
			<tr>
			
				<td align="right"><%=LanguageUtil.get(pageContext, "Order")%>:</td>
				<td><input type="text" name="stepOrder" style="width:50px;"
					id="stepOrder" dojoType="dijit.form.ValidationTextBox" regExp="\d+"
					
					value="">
				</td>
			</tr>
			<tr>
				<td align="right"><%=LanguageUtil.get(pageContext, "Resolve-Task")%>:</td>
				<td><input type="checkbox" name="stepResolved"
					id="stepResolved" dojoType="dijit.form.CheckBox" value="true"
					<%=(step.isResolved()) ? "checked='true'" : ""%>>
				</td>
			</tr>
		</table>
			
		<div class="buttonRow">
			<button dojoType="dijit.form.Button" onClick='stepAdmin.editStep()' iconClass="saveIcon"
				type="button" id="editStepBtn">
				<%=UtilMethods.escapeSingleQuotes(LanguageUtil.get(pageContext, "save"))%>
			</button> &nbsp; &nbsp;
	
			<button dojoType="dijit.form.Button"
				onClick='stepAdmin.hideEdit()' iconClass="cancelIcon"
				type="button">
				<%=UtilMethods.escapeSingleQuotes(LanguageUtil.get(pageContext, "cancel"))%>
			</button>
		</div>
</div>