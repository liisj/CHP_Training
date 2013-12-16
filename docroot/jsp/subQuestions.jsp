<%@ include file="/jsp/init.jsp" %>
<%@ page import="org.json.simple.JSONObject" %>

<portlet:resourceURL id="getSubQuestions" var="getSubQuestions">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:resourceURL>
<portlet:actionURL var="materialsURL">
	<portlet:param name="jspPage" value="/jsp/materialsList.jsp"/>
	<portlet:param name="actionName" value="materials"/>
</portlet:actionURL>
<portlet:actionURL var="subQuestionsURL">
	<portlet:param name="jspPage" value="/jsp/subQuestions.jsp"/>
	<portlet:param name="actionName" value="subQuestions"/>
</portlet:actionURL>
<portlet:actionURL var="diagnosisURL">
	<portlet:param name="jspPage" value="/jsp/diagnosis.jsp"/>
	<portlet:param name="actionName" value="diagnosis"/>
</portlet:actionURL>
<%
String questionId = (String) request.getAttribute("question_id");
String title = (String) request.getAttribute("title");
%>

<html>
<head>
<script type="text/javascript">
$(document).ready(function() {
	q_index = 0;
	console.log("question_id: "+'<%=questionId%>');
	var params = {"question_id" : '<%=questionId%>'};
	var request = jQuery.getJSON('<%=getSubQuestions%>', params);
	request.done(function(data) {
		displayQuestions(data);
	});
});

$(document).on("click",".nextBtn",function(){
	q_index += 1;
	var params = {"question_id" : $(this).attr("question_id")};
	var request = $.getJSON('<%=getSubQuestions%>', params);
	request.done(function(data) {
		displayQuestions(data);
	});
});

function displayQuestions(data) {
	console.log(data);
	
	var mainDiv = document.getElementById("subQuestionsBody");
	var titleSpan = $("<span>");
	titleSpan
		.html(data.title)
		.addClass("title")
		.appendTo(mainDiv);
	
	var treatmentText = data.treatment;
	if (treatmentText != null) {
		var subsection = $("<div>");
		subsection
			.addClass("treatmentText")
			.html(treatmentText)
			.appendTo(mainDiv);
		return;
	}
	
	var accordionWrap = $("<div>");
	accordionWrap
		.addClass("accordionUI")
		.appendTo(mainDiv);
	
	var subsection = $("<div>");
	subsection
		.attr("id","questionSet_" + q_index)
		.appendTo(accordionWrap);
	
	for (var i in data.questions) {
		
		// Question
		var qh = $("<h3>");
		qh
			.html(data.questions[i].question)
			.appendTo(subsection);
		
		// Description how to measure symptom
		
		var qAnsDiv = $("<div>");
		qAnsDiv
			.appendTo(subsection);
		var qAnsP = $("<p>");
		qAnsP
			.html(data.questions[i].description)
			.appendTo(qAnsDiv);
		$("<p>").appendTo(qAnsP);
		
		// Yes/No radio buttons
		var ans1 = $("<input>");
		ans1
			.attr("type","radio")
			.attr("name","ans_" + i)
			.attr("id","ans_" + i + "1")
			.attr("question_id",data.questions[i].id)
			.appendTo(qAnsP);
		var ansLbl1 = $("<label>");
		ansLbl1
			.attr("for","ans_" + i + "1")
			.text("Yes")
			.appendTo(qAnsP);
		$("<p>").appendTo(qAnsP);
		var ans2 = $("<input>");
		ans2
			.attr("type","radio")
			.attr("name","ans_" + i)
			.attr("id","ans_" + i + "2")
			.attr("question_id",data.questions[i].id)
			.appendTo(qAnsP);
		var ansLbl2 = $("<label>");
		ansLbl2
			.attr("for","ans_" + i + "2")
			.text("No")
			.appendTo(qAnsP);
	}
	$("#questionSet_" + q_index).accordion();
			
	var nextBtn = $("<button>");
	nextBtn
		.attr("question_id", data.next)
		.addClass("nextBtn")
		.text("Next")
		.appendTo(subsection)
		.button();
}

</script>
</head>
<body>
<div class="trainingBody">
<div class="contentBody contentSmall" id="subQuestionsBody"></div>
</div>
</body>
</html>