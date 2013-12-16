<%@ include file="/jsp/init.jsp" %>

<portlet:renderURL var="trainingURL">
        <portlet:param name="jspPage" value="/jsp/trainingFront.jsp"/>
</portlet:renderURL>
<html>
<body>
<div class="buttonLink" align="center">
<br/><a class="link" href="<%= trainingURL %>">On-the-job training</a>
</div>

<script type="text/javascript">
$(function() {
    $( "input[type=submit], a[class=link], button" )
      .button();
      });
</script>
</body>
</html>