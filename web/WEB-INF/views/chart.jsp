<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="util.Web" %>
<%
    int pot = ((Integer) request.getAttribute("pot")).intValue();
    int hours = ((Integer) request.getAttribute("hours")).intValue();
    int count = ((Integer) request.getAttribute("count")).intValue();
    boolean truncated = ((Boolean) request.getAttribute("truncated")).booleanValue();
    int maxSessions = ((Integer) request.getAttribute("maxSessions")).intValue();
    String firstText = (String) request.getAttribute("firstText");
    String lastText = (String) request.getAttribute("lastText");
    String weightJs = (String) request.getAttribute("weightJs");
    String moistJs = (String) request.getAttribute("moistJs");
    String lightJs = (String) request.getAttribute("lightJs");
    String marksJs = (String) request.getAttribute("marksJs");
    String ctx = request.getContextPath();

    int[] ranges = {6, 24, 72, 168, 720, 0};
    String[] rangeNames = {"6 gio qua", "24 gio qua", "3 ngay qua", "7 ngay qua", "30 ngay qua", "Tat ca"};
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Bieu do chau <%= pot %></title>
    <link rel="stylesheet" href="<%= ctx %>/css/app.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/nav.jsp"/>
<div class="wrap">
    <h2>Bieu do theo chau</h2>

    <form method="get" action="<%= ctx %>/chart" class="filter">
        <select name="pot">
            <option value="1" <%= pot == 1 ? "selected" : "" %>>Chau 1</option>
            <option value="2" <%= pot == 2 ? "selected" : "" %>>Chau 2</option>
        </select>
        <select name="range">
            <% for (int i = 0; i < ranges.length; i++) { %>
            <option value="<%= ranges[i] %>" <%= ranges[i] == hours ? "selected" : "" %>><%= rangeNames[i] %></option>
            <% } %>
        </select>
        <button type="submit">Xem</button>
    </form>

    <% if (count == 0) { %>
    <p>Chau <%= pot %> khong co lan tuoi nao trong khoang thoi gian nay.</p>
    <% } else { %>
    <p><b><%= count %></b> lan tuoi, tu <%= Web.esc(firstText) %> den <%= Web.esc(lastText) %>.
       <% if (truncated) { %>
       <br><b>Chi ve <%= maxSessions %> lan tuoi gan nhat</b> de bieu do con doc duoc, hay chon khoang thoi gian ngan hon.
       <% } %></p>

    <canvas id="potchart" width="900" height="380" style="max-width:100%;"></canvas>
    <div id="potlegend" style="margin:8px 0;"></div>

    <p class="muted">
        Moi vach dung dut net la mot lan tuoi (luc bom bat dau). Ba duong dung chung truc thoi gian
        nhung moi duong co thang do rieng, khoang gia tri cua tung duong ghi o chu thich.<br>
        Cach doc: khoi luong nhay len khi tuoi, roi di xuong giua hai lan tuoi. Phan di xuong
        chinh la luong nuoc cay da dung va nuoc bay hoi.
    </p>

    <script src="<%= ctx %>/js/app.js"></script>
    <script>
        drawPotChart('potchart', 'potlegend', [
            {name: 'Khoi luong (g)', color: '#1a6fb5', points: <%= weightJs %>},
            {name: 'Do am dat', color: '#2e9e5b', points: <%= moistJs %>},
            {name: 'Anh sang', color: '#e08a00', points: <%= lightJs %>}
        ], <%= marksJs %>);
    </script>
    <% } %>
</div>
</body>
</html>