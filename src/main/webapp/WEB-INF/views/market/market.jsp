<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  Created by IntelliJ IDEA.
  User: pawel
  Date: 05.07.2022
  Time: 19:54
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<h2>Stock Market</h2><br/>
<h4>******************Buy_Orders*****************</h4>
<c:forEach var="bo" items="${buyOrders}">
    <table border="1">
        <tr>
            <th>nick</th>
            <th>company</th>
            <th>volume</th>
            <th>priceLimit</th>
        </tr>

        <tr>
            <td>${bo.user.getNick()}</td>
            <td>${bo.company.getName()}</td>
            <td>${bo.volumen}</td>
            <td>${bo.priceLimit}</td>
            <td><button onclick="location.href='http://localhost:8080/sell/${bo.user.id}/${bo.company.id}/${bo.volumen}/${bo.priceLimit}'" type="button">
                Sell</button></td>
            <td><button onclick="location.href='http://localhost:8080/buyorder/edit/${bo.user.id}/${bo.company.id}'" type="button">
                Edit</button></td>
            <td><button onclick="location.href='http://localhost:8080/buyorder/delete/${bo.user.id}/${bo.company.id}'" type="button">
                Delete</button></td>
        </tr><br/>

    </table>
</c:forEach>
<h4>******************Sales_Orders*****************</h4>
<c:forEach var="bo" items="${salesOrders}">
    <table border="1">
        <tr>
            <th>nick</th>
            <th>company</th>
            <th>volume</th>
            <th>priceLimit</th>
        </tr>

        <tr>
            <td>${bo.user.getNick()}</td>
            <td>${bo.company.getName()}</td>
            <td>${bo.volumen}</td>
            <td>${bo.priceLimit}</td>
            <td><button onclick="location.href='http://localhost:8080/buy/${bo.user.id}/${bo.company.id}/${bo.volumen}/${bo.priceLimit}'" type="button">
                Buy</button></td>
            <td><button onclick="location.href='http://localhost:8080/sellorder/edit/${bo.user.id}/${bo.company.id}'" type="button">
                Edit</button></td>
            <td><button onclick="location.href='http://localhost:8080/sellorder/delete/${bo.user.id}/${bo.company.id}'" type="button">
                Delete</button></td>
        </tr><br/>
    </table>
</c:forEach>

</table>

</body>
</html>
