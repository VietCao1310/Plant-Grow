<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.text.SimpleDateFormat, java.net.URLEncoder, model.PotSession, model.AppUser, util.Web" %>
<%
    List<PotSession> rows = (List<PotSession>) request.getAttribute("rows");
    int cur = ((Integer) request.getAttribute("page")).intValue();
    int pages = ((Integer) request.getAttribute("pages")).intValue();
    int total = ((Integer) request.getAttribute("total")).intValue();
    String label = (String) request.getAttribute("label");
    if (label == null) label = "";
    String ctx = request.getContextPath();
    SimpleDateFormat fmt = new SimpleDateFormat("dd/MM HH:mm:ss");
    String[] labels = {"OK", "OVERWATER", "NO_UPTAKE", "DRY_STRESS", "LEAK"};

    // These flags only decide which buttons are drawn. AuthFilter is what really blocks the paths.
    AppUser me = (AppUser) session.getAttribute("user");
    String role = me == null ? "" : me.getRoleCode();
    boolean canCreate = "ADMIN".equals(role);
    boolean canEdit = "ADMIN".equals(role) || "GROWER".equals(role) || "TECHNICIAN".equals(role);
    boolean canDelete = "ADMIN".equals(role);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Danh sach phien</title>
    <link rel="stylesheet" href="<%= ctx %>/css/app.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/nav.jsp"/>
<div class="wrap">
    <h2>Danh sach phien (<%= total %>)</h2>

    <form method="get" action="<%= ctx %>/sessions" class="filter">
        <select name="label">
            <option value="">-- All labels --</option>
            <% for (String l : labels) { %>
            <option value="<%= l %>" <%= l.equals(label) ? "selected" : "" %>><%= l %></option>
            <% } %>
        </select>
        <button type="submit">Filter</button>
        <% if (canCreate) { %>
        <a href="<%= ctx %>/session/create">+ Them phien</a>
        <% } %>
    </form>

    <table class="grid">
        <tr>
            <th>#</th><th>Measured at</th><th>Pot no</th><th>Weight before</th>
            <th>Weight after</th><th>Water ml</th><th>Label</th><th>Source</th><th></th>
        </tr>
        <% for (PotSession r : rows) { %>
        <tr class="lb-<%= Web.esc(r.getLabelCode()) %>">
            <td><%= r.getSessionId() %></td>
            <td><%= fmt.format(r.getMeasuredAt()) %></td>
            <td><%= r.getPotNo() %></td>
            <td><%= r.getWeightBefore() %></td>
            <td><%= r.getWeightAfter() %></td>
            <td><%= r.getWaterMl() %></td>
            <td><span class="tag"><%= Web.esc(r.getLabelCode()) %></span></td>
            <td><%= r.isSample() ? "sample" : (r.getDeviceSeq() < 0 ? "manual" : "device") %></td>
            <td>
                <a href="<%= ctx %>/session/detail?id=<%= r.getSessionId() %>">Detail</a>
                <% if (canEdit) { %>
                <a href="<%= ctx %>/session/edit?id=<%= r.getSessionId() %>">Sua</a>
                <% } %>
                <% if (canDelete) { %>
                <form method="post" action="<%= ctx %>/session/delete" style="display:inline"
                      onsubmit="return confirm('Xoa phien nay cung nhan va canh bao cua no?');">
                    <input type="hidden" name="id" value="<%= r.getSessionId() %>">
                    <button type="submit">Xoa</button>
                </form>
                <% } %>
            </td>
        </tr>
        <% } %>
        <% if (rows.isEmpty()) { %>
        <tr><td colspan="9">Khong co phien nao.</td></tr>
        <% } %>
    </table>

    <div class="pager">
        <% for (int p = 1; p <= pages; p++) {
               if (p == cur) { %><b><%= p %></b> <% }
               else { %><a href="?page=<%= p %>&label=<%= URLEncoder.encode(label, "UTF-8") %>"><%= p %></a> <% }
           } %>
    </div>
</div>
</body>
</html>