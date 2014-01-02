<%@ include file="/jsp/init.jsp" %>

<portlet:defineObjects/>

<%
PortletURL sendFormURL = renderResponse.createActionURL();
sendFormURL.setParameter(ActionRequest.ACTION_NAME, "sendForm");
sendFormURL.setParameter("jspPage", "/jsp/trainingFront.jsp");	
%>

<html>
<head>
<script type="text/javascript">
$(document).ready(function() {
	generalSymptomIndex = 1;
	diagnosisIndex = 1;
	addSubDiagnosis("subDiagnoses");
	$(".addSymptom").button();
	$("#saveBtn").button();
	$("#generalAddSympBtn").click();
	$(".addDiagnosis").button();
});

$(document).on("click",".addSymptom", function() {
	addSymptomField($(this).attr("div"), $(this).attr("type"), 
			$(this).attr("diagnosisIndex"), $(this).attr("symptomIndex"));
	var symptomIndex = parseInt($(this).attr("symptomIndex")) + 1;
	$(this).attr("symptomIndex", symptomIndex);
});

$(document).on("click",".addDiagnosis", function() {
	addSubDiagnosis($(this).attr("div"));
});

$(document).on("click","#saveBtn", function() {
	document.forms['addForm'].submit();
});

function addSymptomField(divId, type, currentDiag, symptomIndex) {
	var index;
	if (type == "general") {
		index = generalSymptomIndex;
		generalSymptomIndex += 1;
	}
	else if (type == "sub") {
		index = currentDiag + "_" + symptomIndex;
	}
	var form = document.getElementById(divId);
	var nameInLbl = $("<label>");
	nameInLbl
		.text("Symptom name: ")
		.attr("for", type + "Symptom_" + index)
		.addClass(type + "Symptom")
		.appendTo(form);
	var nameIn = $("<input>");
	nameIn
		.attr("type","text")
		.attr("id", type + "Symptom_" + index)
		.attr("name", type + "Symptom_" + index)
		.addClass(type + "Symptom")
		.appendTo(form);
	var descrInLbl = $("<label>");
	descrInLbl
		.text("Description: ")
		.attr("for", type + "Descr_" + index)
		.addClass(type + "Descr")
		.appendTo(form);
	var descrIn = $("<input>");
	descrIn
		.attr("type","text")
		.attr("id", type + "Descr_" + index)
		.addClass(type + "Descr")
		.appendTo(form);
	$("<p>").appendTo(form);
}

function addSubDiagnosis(divId) {
	var superDiv = document.getElementById(divId);
	var surroundDiv = $("<div>");
	surroundDiv
		.addClass("surroundDiv")
		.appendTo(superDiv);
	var nameBox = $("<div>");
	nameBox
		.addClass("box")
		.appendTo(surroundDiv);
	var nameInLbl = $("<label>");
	nameInLbl
		.text("Diagnosis name: ")
		.attr("for", "diagnosis_" + diagnosisIndex)
		.appendTo(nameBox);
	var nameIn = $("<input>");
	nameIn
		.attr("type","text")
		.attr("id", "diagnosis_" + diagnosisIndex)
		.appendTo(nameBox);
	var symptomBox = $("<div>");
	symptomBox
		.addClass("box")
		.appendTo(surroundDiv)
		.html("Symptoms");
	var symptomDiv = $("<div>");
	symptomDiv
		.attr("id", "symptomDiv_" + diagnosisIndex)
		.appendTo(symptomBox);
	var addSympBtn = $("<span>");
	addSympBtn
		.addClass("addSymptom")
		.attr("type", "sub")
		.attr("diagnosisIndex", diagnosisIndex)
		.attr("symptomIndex", 1)
		.attr("div","symptomDiv_" + diagnosisIndex)
		.appendTo(symptomBox)
		.html("New symptom")
		.button();
	addSympBtn.click();
	var nrLbl = $("<label>");
	$("<p>").appendTo(symptomBox);
	nrLbl
		.attr("for","symptomNr_" + diagnosisIndex)
		.text("How many symptoms need to be present to give this diagnosis? ")
		.appendTo(symptomBox);
	var nrIn = $("<input>");
	nrIn
		.attr("id","symptomNr_" + diagnosisIndex)
		.attr("type", "text")
		.appendTo(symptomBox);
	diagnosisIndex += 1;
	
}
</script>
</head>
<body>
<div class="trainingBody">
<div class="contentBody">
<span class="title">Add new guidelines</span><p/>
<form name="addForm" method="POST" action='<%=sendFormURL.toString()%>'>
<div id="formDiv">
<div class="box">
General diagnosis class: <input type="text" name="general">
</div>
<div class="box">
<div id="generalSymptoms">
<span>Symptoms</span><p/>
</div>
<span class="addSymptom" id="generalAddSympBtn" div="generalSymptoms" type="general">New symptom</span>
<p/>
<label for="generalNr">How many symptoms need to be present to give this diagnosis? </label>
<input type="text" id="generalNr"/>
</div>
Sub-diagnoses<p/>
<div id="subDiagnoses"></div>
<span class="addDiagnosis" div="subDiagnoses">New diagnosis</span>
<span id="saveBtn">Save</span>
</div>
</form>
</div>
</div>
</body>
</html>