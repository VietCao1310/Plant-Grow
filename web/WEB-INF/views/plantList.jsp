<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.text.SimpleDateFormat, java.net.URLEncoder, model.PotPlant, util.Web" %>
<%
    List<PotPlant> rows = (List<PotPlant>) request.getAttribute("rows");
    int cur = ((Integer) request.getAttribute("page")).intValue();
    int pages = ((Integer) request.getAttribute("pages")).intValue();
    int total = ((Integer) request.getAttribute("total")).intValue();
    String kw = (String) request.getAttribute("kw");
    boolean canEdit = ((Boolean) request.getAttribute("canEdit")).booleanValue();
    String ctx = request.getContextPath();
    SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quan ly cay</title>
    <link rel="stylesheet" href="<%= ctx %>/css/app.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/nav.jsp"/>
<div class="wrap">
    <h2>Quan ly cay (<%= total %>)</h2>

    <form method="get" action="<%= ctx %>/plants" class="filter">
        <input type="text" name="kw" value="<%= Web.esc(kw) %>" placeholder="Ma, ten hoac loai cay">
        <button type="submit">Tim</button>
        <% if (canEdit) { %>
        <a href="<%= ctx %>/plants/create">+ Them cay</a>
        <% } %>
    </form>

    <table class="grid">
        <tr>
            <th>#</th><th>Ma</th><th>Ten</th><th>Loai</th><th>Chau</th>
            <th>Ngay gieo</th><th>Hoat dong</th><% if (canEdit) { %><th></th><% } %>
        </tr>
        <% for (PotPlant r : rows) { %>
        <tr>
            <td><%= r.getPlantId() %></td>
            <td><%= Web.esc(r.getCode()) %></td>
            <td><%= Web.esc(r.getName()) %></td>
            <td><%= Web.esc(r.getSpecies()) %></td>
            <td><%= r.getPotNo() == null ? "-" : r.getPotNo().toString() %></td>
            <td><%= r.getPlantedAt() == null ? "-" : fmt.format(r.getPlantedAt()) %></td>
            <td><%= r.isActive() ? "Co" : "Khong" %></td>
            <% if (canEdit) { %>
            <td>
                <a href="<%= ctx %>/plants/edit?id=<%= r.getPlantId() %>">Sua</a>

                <form method="post" action="<%= ctx %>/plants/move" style="display:inline">
                    <input type="hidden" name="id" value="<%= r.getPlantId() %>">
                    <select name="potNo">
                        <option value="1" <%= (r.getPotNo() != null && r.getPotNo() == 2) ? "selected" : "" %>>Chau 1</option>
                        <option value="2" <%= (r.getPotNo() == null || r.getPotNo() == 1) ? "selected" : "" %>>Chau 2</option>
                    </select>
                    <button type="submit">Chuyen</button>
                </form>

                <form method="post" action="<%= ctx %>/plants/delete" style="display:inline"
                      onsubmit="return confirm('Xoa cay nay?');">
                    <input type="hidden" name="id" value="<%= r.getPlantId() %>">
                    <button type="submit">Xoa</button>
                </form>
            </td>
            <% } %>
        </tr>
        <% } %>
        <% if (rows.isEmpty()) { %>
        <tr><td colspan="8">Khong co cay nao.</td></tr>
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