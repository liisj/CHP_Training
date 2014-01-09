<%@ include file="/jsp/init.jsp" %>
<%@ page import="org.json.simple.JSONObject" %>

<portlet:resourceURL id="getSubQuestions" var="getSubQuestions">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:resourceURL>
<portlet:resourceURL id="getFirstQuestionBox" var="getFirstQuestionBox">
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

var diagnosisTitle = "";

$(document).ready(function() {
	q_index = 0;
	console.log("question_id: "+'<%=questionId%>');
	var params = {"topic" : '<%=questionId%>'};
	var request = jQuery.getJSON('<%=getFirstQuestionBox%>', params);
	request.done(function(data) {
		displayQuestions(data);
	});
});

$(document).on("click",".nextBtn",function(){
	q_index += 1;
	
	var yes_count = 0;
	var answerBtns = document.getElementsByName("ans_" + $(this).attr("questionSet"));
	for (var i in answerBtns) {
		if (answerBtns[i].id == "ans_" + $(this).attr("questionSet") + "_1" && 
				answerBtns[i].checked) {
			yes_count += 1;
		}
	}
	
	console.log("yes_count: " + yes_count);
	var params = {
			"questionbox" : $(this).attr("questionbox_id"),
			"yes_count" : yes_count};
	var request = $.getJSON('<%=getSubQuestions%>', params);
	request.done(function(data) {
		displayQuestions(data);
	});
});

function displayQuestions(data) {
	console.log(data);
	
	var mainDiv = document.getElementById("subQuestionsBody");
	
	if (data.action == "treatment") {
		
		var surroundDiv = $("<div>");
		surroundDiv
			.addClass("treatmentArea")
			.appendTo(mainDiv);
		
		var treatmentTitle = $("<div>");
		treatmentTitle
			.addClass("treatmentTitle")
			.html();
		
		var treatmentText = data.treatment.description;
		var subsection = $("<div>");
		subsection
			.addClass("treatmentText")
			.html(treatmentText)
			.appendTo(surroundDiv);
		return;
	}
	
	if (data.topic != null) {
		var titleSpan = $("<span>");
		titleSpan
			.html(data.topic.title)
			.addClass("title")
			.appendTo(mainDiv);
	}
	
	var accordionWrap = $("<div>");
	accordionWrap
		.addClass("accordionUI")
		.appendTo(mainDiv);
	
	var askDiv = $("<div>");
	askDiv
		.addClass("ask")
		.appendTo(accordionWrap)
		.html("Does the patient exhibit any of the following symptoms?");
	
	var subsection = $("<div>");
	subsection
		.attr("id","questionSet_" + q_index)
		.appendTo(accordionWrap);
	
	var questions = data.questionbox.questions;
	for (var i in questions) {
		
		// Question
		var qh = $("<h3>");
		qh
			.html(questions[i].question)
			.appendTo(subsection);
		
		// Description how to measure symptom
		
		var qAnsDiv = $("<div>");
		qAnsDiv
			.appendTo(subsection);
		var qAnsP = $("<p>");
		qAnsP
			.html(questions[i].details.replace(new RegExp("[<>]", "g"),""))
			.appendTo(qAnsDiv);
		$("<p>").appendTo(qAnsP);
		
		// Yes/No radio buttons
		var ans1 = $("<input>");
		ans1
			.attr("type","radio")
			.attr("name","ans_" + q_index)
			.attr("id","ans_" + q_index + "_1")
			.attr("questionSet",q_index)
			.appendTo(qAnsP);
		var ansLbl1 = $("<label>");
		ansLbl1
			.attr("for","ans_" + q_index + "_1")
			.text("Yes")
			.appendTo(qAnsP);
		$("<p>").appendTo(qAnsP);
		var ans2 = $("<input>");
		ans2
			.attr("type","radio")
			.attr("name","ans_" + q_index)
			.attr("id","ans_" + q_index + "_2")
			.attr("questionSet",q_index)
			.appendTo(qAnsP);
		var ansLbl2 = $("<label>");
		ansLbl2
			.attr("for","ans_" + q_index + "_2")
			.text("No")
			.appendTo(qAnsP);
	}
	$("#questionSet_" + q_index).accordion();
			
	var nextBtn = $("<button>");
	nextBtn
		.attr("questionSet",q_index)
		.attr("questionbox_id", data.questionbox.id)
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