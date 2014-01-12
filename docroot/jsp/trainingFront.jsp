<%@ include file="/jsp/init.jsp" %>
<!-- 
<portlet:defineObjects/>
<%
/*PortletURL searchURL = renderResponse.createresourceURL();
searchURL.setParameter(
ActionRequest.ACTION_NAME, "search");*/
%>
 -->

<portlet:resourceURL id="getCategories" var="getCategories">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:resourceURL>
<portlet:resourceURL id="getMaterialTitles" var="getMaterialTitles">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:resourceURL>
<portlet:resourceURL id="getTopQuestions" var="getTopQuestions">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:resourceURL>
<portlet:actionURL var="subCategoriesURL">
	<portlet:param name="jspPage" value="/jsp/subCategories.jsp"/>
	<portlet:param name="actionName" value="subCategories"/>
</portlet:actionURL>
<portlet:actionURL var="subQuestionsURL">
	<portlet:param name="jspPage" value="/jsp/subQuestions.jsp"/>
	<portlet:param name="actionName" value="subQuestions"/>
</portlet:actionURL>
<portlet:actionURL var="materialURL">
	<portlet:param name="jspPage" value="/jsp/trainingItem.jsp"/>
	<portlet:param name="actionName" value="materials"/>
</portlet:actionURL>
<portlet:actionURL var="addURL2">
	<portlet:param name="jspPage" value="/jsp/add2.jsp"/>
	<portlet:param name="actionName" value="add"/>
</portlet:actionURL>
<portlet:actionURL var="addURL">
	<portlet:param name="jspPage" value="/jsp/add.jsp"/>
	<portlet:param name="actionName" value="add2"/>
</portlet:actionURL>


<html>
<head>
<script type="text/javascript">


$(document).ready(function() {
	
	// Fill categories pane
	
	// Query the top level of categories
	var request = jQuery.getJSON('<%=getCategories%>');
	request.done(function(data1) {
		console.log("data1: ");
		console.log(data1);
		var categoryDiv = document.getElementById("categoriesAccordion");
		var keys = Object.keys(data1);
		for (var i in keys) {
			var key = parseInt(keys[i]);
			if (data1[key] != null) {
				var cat1 = $("<h3>");
				cat1
					.appendTo(categoryDiv)
					.html(data1[key])
					.attr("cat_id",key)
					.attr("id", "cat1_" + i)
					.attr("i",i)
					.addClass("catTitle");
				var innerDiv = $("<div>");
				innerDiv
					.appendTo(categoryDiv)
					.attr("id", "innerDiv1_" + i);
				
				// For each, query the second level of categories
				var params = {"index" : i, "category" : key};
				console.log("level 2 params");
				console.log(params);
				var subRequest = $.getJSON('<%=getCategories%>',params);
				subRequest.done(function(data2) {
					console.log("data2: ");
					console.log(data2);
					var innerDiv_super = document.getElementById("innerDiv1_" + data2.index);
					var keys2 = Object.keys(data2);
					for (var j in keys2) {
						var key2 = keys2[j];
						if (key2 != "index" && key2 != "material") {
							console.log("key2: " + key2);
							var cat2 = $("<h3>");
							cat2
								.appendTo(innerDiv_super)
								.html(data2[key2])
								.attr("cat_id",key2)
								.attr("id", "cat2_" + j)
								.attr("j",j)
								.addClass("subCatTitle");
							var innerDiv2 = $("<div>");
							innerDiv2
								.appendTo(innerDiv_super)
								.attr("id", "innerDiv2_" + data2.index + "_" + j);
							
							// For each, query the material titles that are in that category
							var materialsRequest = $.getJSON('<%=getMaterialTitles%>', {"index" : j});
							materialsRequest.done(function(data3) {
								var innerDiv_super2 = 
									document.getElementById("innerDiv2_" + data2.index + "_" + data3.index);
								for (var k in data3.objects) {
									var matForm = $("<form>");
									matForm
										.attr("method", "POST")
										.attr("name", "matForm_" + k)
										.attr("action", '<%=materialURL%>')
										.appendTo(innerDiv_super2);
									var mat = $("<button>");
									mat
										.text(data3.objects[k].title)
										.attr("mat_id",data3.objects[k].id)
										.attr("id", "mat_" + k)
										.attr("k",k)
										.attr("type","submit")
										.addClass("matTitle")
										.button()
										.appendTo(matForm);
									$("<p>").appendTo(innerDiv_super2);
								}
							});
						}
					}
					$("#innerDiv1_" + data2.index)
						.accordion({
							collapsible : true, 
							heightStyle: "content", 
							active: false});
				});
			}
		}
		$("#categoriesAccordion")
			.accordion({
				collapsible : true, 
				heightStyle: "content", 
				active: false});
		
		addAddLink($("#categories"), "addForm1", "<%=addURL.toString()%>");
	});
	
	var request2 = jQuery.getJSON('<%=getTopQuestions%>');
	request2.done(function(data) {
		console.log(data);
		var questionsDiv = document.getElementById("questions");
		var linkDiv = $("<div>");
		linkDiv
			.attr("id", "questionLinkDiv")
			.appendTo(questionsDiv);
		for (var i in data) {
			var questionsSpan = $("<span>");
			questionsSpan
				.addClass("questionsSpan")
				.appendTo(linkDiv);
			var questionForm = $("<form>");
			questionForm
				.addClass("questionForm")
				.attr("name", "questionForm_" + i)
				.attr("method","POST")
				.attr("action","<%=subQuestionsURL.toString()%>")
				.appendTo(questionsSpan);
			var questionLink = $("<a>");
			var hrefStr = "javascript:document.forms['questionForm_" + i + "'].submit()"; 
			questionLink
				.addClass("questionLink")
				.attr("href",hrefStr)
				.attr("id","question_" + i)
				.attr("name", "title")
				.html(data[i].title)
				.appendTo(questionForm);
			var questionId = $("<input>");
			questionId
				.attr("type","hidden")
				.attr("name", "question_id")
				.attr("value", data[i].id)
				.appendTo(questionForm);
		};
		
		addAddLink(questionsDiv, "addForm2", "<%=addURL2.toString()%>");
	});
});

function addAddLink(div, name, url) {
	
	var addSpan = $("<span>");
	addSpan
		.addClass("questionsSpan")
		.appendTo(div);
	var addForm = $("<form>");
	addForm
		.addClass("questionForm")
		.attr("name", name)
		.attr("method","POST")
		.attr("action",url)
		.appendTo(addSpan);
	var addLink = $("<a>");
	var linkStr = "javascript:document.forms['" + name + "'].submit()";
	addLink
		.addClass("questionLink")
		.attr("href",linkStr)
		.attr("id",name)
		.attr("name", "title")
		.html("Add new...")
		.appendTo(addForm);
}

</script>
</head>
<body>
<div class="trainingBody">
<div class="CHPTitle title" align="center">Community Health Portal</div>
<div class="subTitle title" align="center">Learning Materials</div>
<!--
<div class="search" align="center">
<form name="<portlet:namespace/>fm" method="POST" action="">
<input type="text" name="<portlet:namespace/>searchParameters" />
<input type="submit" value="Search" id="searchMaterials"/>
  
<script type="text/javascript">
$(function() {
	$("input").button();
	});
</script>
<label for="searchMaterials"></label>
</form>
</div>
-->
<div id="categories">
<span id="categoriesTitle" class="title">Categories for training materials:</span>
<div class="accordionUI">
<div id="categoriesAccordion"></div>
<p/>
</div>
</div>
<div id="questions">
<span id="questionsTitle" class="title">What symptoms does the patient have?</span>
</div>
</div>

</body>
</html>