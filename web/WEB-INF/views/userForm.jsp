<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, model.AppUser, model.AppRole, util.Web" %>
<%
    AppUser u = (AppUser) request.getAttribute("u");
    boolean editing = ((Boolean) request.getAttribute("editing")).booleanValue();
    boolean isSelf = ((Boolean) request.getAttribute("isSelf")).booleanValue();
    String error = (String) request.getAttribute("error");
    List<AppRole> roles = (List<AppRole>) request.getAttribute("roles");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><%= editing ? "Sua tai khoan" : "Them tai khoan" %></title>
    <link rel="stylesheet" href="<%= ctx %>/css/app.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/nav.jsp"/>
<div class="wrap">
    <h2><%= editing ? "Sua tai khoan " + Web.esc(u.getUsername()) : "Them tai khoan moi" %></h2>

    <% if (error != null) { %>
    <div class="error" style="color:red; margin-bottom:10px;"><%= Web.esc(error) %></div>
    <% } %>

    <form method="post" action="<%= ctx %>/admin/users/<%= editing ? "update" : "create" %>">
        <% if (editing) { %>
        <input type="hidden" name="id" value="<%= u.getUserId() %>">
        <% } else { %>
        <p>
            <label>Ten dang nhap (3 den 64 ky tu: chu, so, _ . -)</label><br>
            <input type="text" name="username" maxlength="64" required value="<%= Web.esc(u.getUsername()) %>">
        </p>
        <% } %>

        <p>
            <label>Ho ten</label><br>
            <input type="text" name="fullName" maxlength="150" required size="40"
                   value="<%= Web.esc(u.getFullName()) %>">
        </p>
        <p>
            <label>Vai tro</label><br>
            <select name="roleId" required <%= isSelf ? "disabled" : "" %>>
                <% if (!editing) { %><option value="">-- Chon vai tro --</option><% } %>
                <% for (AppRole r : roles) { %>
                <option value="<%= r.getRoleId() %>" <%= r.getRoleId() == u.getRoleId() ? "selected" : "" %>>
                    <%= Web.esc(r.getRoleCode()) %>
                </option>
                <% } %>
            </select>
            <% if (isSelf) { %>
            <input type="hidden" name="roleId" value="<%= u.getRoleId() %>">
            <small>(khong doi duoc vai tro cua chinh minh)</small>
            <% } %>
        </p>

        <% if (!editing) { %>
        <p>
            <label>Mat khau (6 den 64 ky tu)</label><br>
            <input type="password" name="password" minlength="6" maxlength="64" required>
        </p>
        <p>
            <label>Nhap lai mat khau</label><br>
            <input type="password" name="password2" minlength="6" maxlength="64" required>
        </p>
        <% } %>

        <button type="submit"><%= editing ? "Luu thay doi" : "Tao tai khoan" %></button>
        <a href="<%= ctx %>/admin/users">Huy</a>
    </form>

    <% if (editing) { %>
    <hr>
    <h3>Dat lai mat khau</h3>
    <form method="post" action="<%= ctx %>/admin/users/reset">
        <input type="hidden" name="id" value="<%= u.getUserId() %>">
        <p>
            <label>Mat khau moi (6 den 64 ky tu)</label><br>
            <input type="password" name="password" minlength="6" maxlength="64" required>
        </p>
        <p>
            <label>Nhap lai mat khau moi</label><br>
            <input type="password" name="password2" minlength="6" maxlength="64" required>
        </p>
        <button type="submit">Dat lai mat khau</button>
    </form>
    <% } %>
</div>
</body>
</html>