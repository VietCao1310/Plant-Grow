<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.text.SimpleDateFormat, java.net.URLEncoder, model.AppUser, util.Web" %>
<%
    List<AppUser> rows = (List<AppUser>) request.getAttribute("rows");
    int cur = ((Integer) request.getAttribute("page")).intValue();
    int pages = ((Integer) request.getAttribute("pages")).intValue();
    int total = ((Integer) request.getAttribute("total")).intValue();
    String kw = (String) request.getAttribute("kw");
    int meId = ((Integer) request.getAttribute("meId")).intValue();
    String ctx = request.getContextPath();
    SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quan tri nguoi dung</title>
    <link rel="stylesheet" href="<%= ctx %>/css/app.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/nav.jsp"/>
<div class="wrap">
    <h2>Quan tri nguoi dung (<%= total %>)</h2>

    <form method="get" action="<%= ctx %>/admin/users" class="filter">
        <input type="text" name="kw" value="<%= Web.esc(kw) %>" placeholder="Ten dang nhap hoac ho ten">
        <button type="submit">Tim</button>
        <a href="<%= ctx %>/admin/users/create">+ Them tai khoan</a>
    </form>

    <table class="grid">
        <tr>
            <th>#</th><th>Ten dang nhap</th><th>Ho ten</th><th>Vai tro</th>
            <th>Trang thai</th><th>Ngay tao</th><th></th>
        </tr>
        <% for (AppUser r : rows) { boolean self = r.getUserId() == meId; %>
        <tr>
            <td><%= r.getUserId() %></td>
            <td><%= Web.esc(r.getUsername()) %><%= self ? " (ban)" : "" %></td>
            <td><%= Web.esc(r.getFullName()) %></td>
            <td><span class="tag"><%= Web.esc(r.getRoleCode()) %></span></td>
            <td><b><%= r.isLocked() ? "DA KHOA" : "Hoat dong" %></b></td>
            <td><%= r.getCreatedAt() == null ? "-" : fmt.format(r.getCreatedAt()) %></td>
            <td>
                <a href="<%= ctx %>/admin/users/edit?id=<%= r.getUserId() %>">Sua / Dat lai mat khau</a>
                <% if (!self) { %>
                <form method="post" action="<%= ctx %>/admin/users/lock" style="display:inline">
                    <input type="hidden" name="id" value="<%= r.getUserId() %>">
                    <button type="submit"><%= r.isLocked() ? "Mo khoa" : "Khoa" %></button>
                </form>
                <form method="post" action="<%= ctx %>/admin/users/delete" style="display:inline"
                      onsubmit="return confirm('Xoa tai khoan nay?');">
                    <input type="hidden" name="id" value="<%= r.getUserId() %>">
                    <button type="submit">Xoa</button>
                </form>
                <% } %>
            </td>
        </tr>
        <% } %>
        <% if (rows.isEmpty()) { %>
        <tr><td colspan="7">Khong co tai khoan nao.</td></tr>
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