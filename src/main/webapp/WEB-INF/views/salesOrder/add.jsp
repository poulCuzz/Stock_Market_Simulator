<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%--
  Created by IntelliJ IDEA.
  User: pawel
  Date: 05.07.2022
  Time: 22:35
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<%--@elvariable id="salesOrder" type="pl.coderslab.SalesOrders"--%>
<form:form method="post" modelAttribute="salesOrder">
    <form:input type="number" path="volumen" id="volumen" step="1" min="1" /><label for="volumen">volume</label><br/>
    <form:errors path="volumen"/>
    <form:input path="priceLimit" id="limit" type="number" min="1"/><label for="limit">priceLimit</label><br/>
    <form:errors path="priceLimit"/>
    <form:hidden path="user" />
    <form:hidden path="company"/>
    <input type="submit" value="Save">
</form:form>
<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger" role="alert">${errorMessage}</div>
</c:if>
</body>
</html>
