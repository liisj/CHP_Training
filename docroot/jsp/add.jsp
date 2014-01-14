<%@ include file="/jsp/init.jsp" %>

<portlet:resourceURL id="getCategories" var="getCategories">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:resourceURL>
<portlet:resourceURL id="getSubCategories" var="getSubCategories">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:resourceURL>
<portlet:actionURL name="sendMaterialsForm" var="sendFormURL">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
	<portlet:param name="jspPage" value="/jsp/trainingFront.jsp"></portlet:param>
</portlet:actionURL>

<html>
<head>
<script type="text/javascript">
$(document).ready(function() {
	
	// Fill categories dropdown menu
	
	var url = '<%=getCategories%>';
	$("#statusgif").show();
	
	var request = jQuery.getJSON(url);
	request.done(function(data){
		$("#statusgif").hide();
		console.log(data);
		if (data.error != null) {
			createDialog("notification","#error-message","ui-icon ui-icon-alert","Fetching categories failed", data.details);
		}
		else {
			var dropdown = document.getElementById("topCategories");
			var catIDs = ""; 
			var keys = Object.keys(data);
			for (var i in keys) {							// Add categories to dropdown menu
				var key = parseInt(keys[i]);
				if (data[key] != null) {
					var categoryOpt = $("<option>");
					categoryOpt
						.text(data[key])
						.appendTo(dropdown);
					catIDs += key + ",";
				}
			}
			$("#topCategories").attr("catIDs", catIDs);
		}
	});
	
	$("#saveBtn").button();
	//$("#picture_1").button();
});

$(document).on("change","#topCategories",function() {
	
	var catMenu = document.getElementById("topCategories");
	var catIDs = catMenu.getAttribute("catIDs").split(",");
	var category_id = catIDs[catMenu.selectedIndex - 1];
	
	var request = $.getJSON('<%=getSubCategories%>', {"id": category_id});
	request.done(function(data) {
		
		$("<p>").appendTo($("#dropdowns"));
		
		var label = $("<label>");
		label
			.attr("for", "subCategories")
			.text("Sub-category:")
			.appendTo($("#dropdowns"));
		$("<p>").appendTo($("#dropdowns"));
		
		var dropdown = $("<select>");
		dropdown
			.appendTo($("#dropdowns"))
			.attr("id", "subCategories");
		$("<option>").appendTo(dropdown);
		catIDs2 = "";
		for (var i in data.objects) {
			var option = $("<option>");
			option
				.appendTo(dropdown)
				.text(data.objects[i].name);
			catIDs2 += data.objects[i].id + ",";
		}
		$("#subCategories").attr("catIDs", catIDs2);
	});

});

$(document).on("click","#saveBtn", function() {
	document.forms['addForm'].submit();
});
</script>
</head>
<body>
	<div class="trainingBody">
		<div class="contentBody">
			<span class="title">Add new materials</span>
			<p></p>
			<form name="addForm" method="POST" action='<%=sendFormURL.toString()%>' enctype="multipart/form-data">
				<div id="formDiv">
					<label for="materialTitle">Title: </label>
					<p></p>
					<input type="text" id="materialTitle"></input>
					<p></p>
					<div id="dropdowns">	
						<label>Pick category:</label>
						<p></p>
						<select id="topCategories">
							<option></option>
						</select>
						<p></p>
					</div>
					<label for="textarea">Text: </label>
					<p></p>
					<textarea id="textarea" rows="20" cols="80"></textarea>
					<p></p>
					<label>Add picture:</label>
					<p></p>
					<input type="file" name="picture_1"></input>
					<p></p>
					<span id="saveBtn">Save</span>
				</div>
			</form>
		</div>
	</div>
</body>
</html>