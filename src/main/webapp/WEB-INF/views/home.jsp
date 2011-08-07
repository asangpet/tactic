<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
<head>
	<title>Home</title>
</head>
<body>
<h1>
	Hello world!  
</h1>

<P>  The time on the server is ${serverTime}. </P>

<ul>
<c:forEach items="${response}" var="resp">
<li><c:out value="${resp}"/></li>	
</c:forEach>
</ul>

</body>
</html>
