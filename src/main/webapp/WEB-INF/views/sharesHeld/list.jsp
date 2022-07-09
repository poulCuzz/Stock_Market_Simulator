
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%--
  Created by IntelliJ IDEA.
  User: pawel
  Date: 05.07.2022
  Time: 21:44
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>


    <h2>Hello! Here's your stocks:</h2>

<c:forEach var="bo" items="${sharesHeld}"><%--@elvariable id="sharesHeld" type="pl.coderslab.SharesHeld"--%>
    <table border="2">
        <tr>
            <th>date and time</th>
            <th>company name</th>
            <th>volume</th>
            <th>purchase price</th>
            <th>market value all</th>
            <th>purchase price for all</th>
            <th>profit/loss</th>
        </tr>

        <tr>
            <td>${bo.dateAndTime}</td>
            <td>${bo.company.getName()}</td>
            <td>${bo.volume}</td>
            <td>${bo.purchasePrice}</td>
            <td>${bo.valueAll}</td><br/>
            <td>${bo.purchasePriceAll}</td><br/>
            <td>${bo.profitOrLoss}</td><br/>
        </tr>

        <form action="http://localhost:8080/sellorder/add/${bo.user.id}/${bo.company.id}">
            <input class="d-none d-sm-inline-block btn btn-sm btn-primary shadow-sm" type="submit" value="issue a sales order" />
        </form>
<%--        <jsp:useBean id="sharesHeld" scope="request" type="pl.coderslab.SharesHeld"/>--%>
<%--       <a> <c:url  value="/sellorder/add/${sharesHeld.user.id}/${sharesHeld.company.id}"/> ">issue a sales order</a>--%>
    </table>
</c:forEach>
</body>
</html>
