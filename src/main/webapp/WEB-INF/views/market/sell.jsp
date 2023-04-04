<%--<%@ page import="pl.coderslab.User" %>--%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<%--
  Created by IntelliJ IDEA.
  User: pawel
  Date: 06.07.2022
  Time: 20:51
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<%--<%--%>
<%--  User user = (User)request.getAttribute("user");--%>
<%--  String userNick = user.getNick();--%>
<%--%>--%>
<%--@elvariable id="buyOrders" type="pl.coderslab.BuyOrders"--%>
<form:form method="post" modelAttribute="buyOrders">
  <form:hidden path="company.id"/>
  <form:errors path="company.id"/>
<%--  <form:hidden path="user.id"/>--%>
  <form:errors path="user.id"/>
<%--  <input type="number" name="yourId" min="1" step="1" required/><label>your Id</label><br/>--%>
  <form:input path="user.id" type="number"/><label>your Id</label><br/>
  <form:input path="volumen" type="number"/><label>volume</label><br/>
  <form:errors path="volumen"/>
  <form:input path="priceLimit" type="number"/><label>price Limit</label>
  <form:errors path="priceLimit"/>
    <input type="submit" name="submit" value="sell">
</form:form>
<c:if test="${not empty errorMessage}">
  <div class="alert alert-danger" role="alert">${errorMessage}</div>
</c:if>
</body>
</html>
