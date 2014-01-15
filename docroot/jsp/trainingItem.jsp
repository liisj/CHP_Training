<%@ include file="/jsp/init.jsp" %>

<portlet:resourceURL id="getMaterial" var="getMaterial">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:resourceURL>

<%
String matId = (String) request.getAttribute("mat_id");
%>

<html>
<head>
<script type="text/javascript">
$(document).ready(function() {
	
	console.log('<%=matId%>');
	var request = $.getJSON('<%=getMaterial%>', {"category_id": '<%=matId%>'});
	request.done(function(data) {
		console.log(data);
		$("#materialTitle").html(data["material_title"]);
		$("#materialBody").html(data["material_text"]);
	});
});
</script>
</head>
<body>
<div class="trainingBody">
<div class="contentBody contentSmall">
<div id="materialTitle" class="title">
</div>
<div id="materialBody">
</div>
</div>
</div>
</body>
</html>