<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.net.URLEncoder, model.PotWaterPlan, util.Web" %>
<%
    List<PotWaterPlan> rows = (List<PotWaterPlan>) request.getAttribute("rows");
    int cur = ((Integer) request.getAttribute("page")).intValue();
    int pages = ((Integer) request.getAttribute("pages")).intValue();
    int total = ((Integer) request.getAttribute("total")).intValue();
    String kw = (String) request.getAttribute("kw");
    boolean canEdit = ((Boolean) request.getAttribute("canEdit")).booleanValue();
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Lich tuoi</title>
    <link rel="stylesheet" href="<%= ctx %>/css/app.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/nav.jsp"/>
<div class="wrap">
    <h2>Quan ly lich tuoi (<%= total %>)</h2>

    <form method="get" action="<%= ctx %>/plans" class="filter">
        <input type="text" name="kw" value="<%= Web.esc(kw) %>" placeholder="Ma hoac ten lich">
        <button type="submit">Tim</button>
        <% if (canEdit) { %>
        <a href="<%= ctx %>/plans/create">+ Them lich tuoi</a>
        <% } %>
    </form>

    <table class="grid">
        <tr>
            <th>#</th><th>Ma</th><th>Ten</th><th>Chau</th><th>Nuoc / lan (ml)</th>
            <th>Cach nhau (gio)</th><th>Nguong do am</th><th>Tuoi tu dong</th>
            <% if (canEdit) { %><th></th><% } %>
        </tr>
        <% for (PotWaterPlan r : rows) { %>
        <tr>
            <td><%= r.getWaterPlanId() %></td>
            <td><%= Web.esc(r.getCode()) %></td>
            <td><%= Web.esc(r.getName()) %></td>
            <td><%= r.getPotNo() %></td>
            <td><%= r.getWaterTargetMl() %></td>
            <td><%= r.getIntervalHours() %></td>
            <td><%= r.getTriggerMoist() %></td>
            <td><b><%= r.isActive() ? "BAT" : "TAT" %></b></td>
            <% if (canEdit) { %>
            <td>
                <a href="<%= ctx %>/plans/edit?id=<%= r.getWaterPlanId() %>">Sua</a>

                <form method="post" action="<%= ctx %>/plans/toggle" style="display:inline">
                    <input type="hidden" name="id" value="<%= r.getWaterPlanId() %>">
                    <button type="submit"><%= r.isActive() ? "Tat" : "Bat" %> tu dong</button>
                </form>

                <form method="post" action="<%= ctx %>/plans/delete" style="display:inline"
                      onsubmit="return confirm('Xoa lich tuoi nay?');">
                    <input type="hidden" name="id" value="<%= r.getWaterPlanId() %>">
                    <button type="submit">Xoa</button>
                </form>
            </td>
            <% } %>
        </tr>
        <% } %>
        <% if (rows.isEmpty()) { %>
        <tr><td colspan="9">Khong co lich tuoi nao.</td></tr>
        <% } %>
    </table>

    <div class="pager">
        <% for (int p = 1; p <= pages; p++) {
               if (p == cur) { %><b><%= p %></b> <% }
               else { %><a href="?page=<%= p %>&kw=<%= URLEncoder.encode(kw, "UTF-8") %>"><%= p %></a> <% }
           } %>
    </div>
</div>
</body>
</html>