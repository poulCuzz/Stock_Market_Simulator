<%@ page import="pl.coderslab.Companies" %>
<%@ page import="pl.coderslab.User" %>
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
    <title>Stock Market Simulator Application</title>
</head>
<body>
<%
    User user = (User)request.getAttribute("user");
    String userNick = user.getNick();
%>


<h2>Welcome in your portfolio <%= userNick %></h2><br/>
<strong></strong>
    <div>
        <label><strong>Value of all your stocks:</strong><lobel></lobel></label><a>&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp&nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp</a><label><strong>Profit/Loss:</strong></label>

    </div>

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
    </table>
</c:forEach>
</body>
</html>
