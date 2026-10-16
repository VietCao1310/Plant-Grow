<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.PotWaterPlan, util.Web" %>
<%
    PotWaterPlan p = (PotWaterPlan) request.getAttribute("plan");
    boolean editing = ((Boolean) request.getAttribute("editing")).booleanValue();
    String error = (String) request.getAttribute("error");
    String ctx = request.getContextPath();

    // A brand new form (no error yet) starts with sensible defaults instead of zeros.
    boolean fresh = !editing && error == null;
    String ml = fresh ? "50" : String.valueOf(p.getWaterTargetMl());
    String hours = fresh ? "12" : String.valueOf(p.getIntervalHours());
    String moist = fresh ? "250" : String.valueOf(p.getTriggerMoist());
    boolean active = fresh ? true : p.isActive();
    int pot = p.getPotNo();   // 0 on a fresh form
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><%= editing ? "Sua lich tuoi" : "Them lich tuoi" %></title>
    <link rel="stylesheet" href="<%= ctx %>/css/app.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/nav.jsp"/>
<div class="wrap">
    <h2><%= editing ? "Sua lich tuoi" : "Them lich tuoi moi" %></h2>

    <% if (error != null) { %>
    <div class="error" style="color:red; margin-bottom:10px;"><%= Web.esc(error) %></div>
    <% } %>

    <form method="post" action="<%= ctx %>/plans/<%= editing ? "update" : "create" %>">
        <% if (editing) { %>
        <input type="hidden" name="id" value="<%= p.getWaterPlanId() %>">
        <% } %>

        <p>
            <label>Ma lich (bat buoc, khong trung)</label><br>
            <input type="text" name="code" maxlength="32" required
                   value="<%= Web.esc(p.getCode()) %>">
        </p>
        <p>
            <label>Ten lich (bat buoc)</label><br>
            <input type="text" name="name" maxlength="150" required
                   value="<%= Web.esc(p.getName()) %>">
        </p>
        <p>
            <label>Chau</label><br>
            <select name="potNo" required>
                <option value="" <%= pot == 0 ? "selected" : "" %>>-- Chon chau --</option>
                <option value="1" <%= pot == 1 ? "selected" : "" %>>Chau 1</option>
                <option value="2" <%= pot == 2 ? "selected" : "" %>>Chau 2</option>
            </select>
        </p>
        <p>
            <label>Luong nuoc moi lan (ml, tu 1 den 250)</label><br>
            <input type="number" name="waterTargetMl" min="1" max="250" step="0.1" required
                   value="<%= ml %>">
        </p>
        <p>
            <label>Khoang cach giua hai lan tuoi (gio, tu 1 den 168)</label><br>
            <input type="number" name="intervalHours" min="1" max="168" step="1" required
                   value="<%= hours %>">
        </p>
        <p>
            <label>Nguong do am kich hoat tuoi tu dong (tu 0 den 1000)</label><br>
            <input type="number" name="triggerMoist" min="0" max="1000" step="1" required
                   value="<%= moist %>">
        </p>
        <p>
            <label>Ghi chu</label><br>
            <textarea name="note" rows="3" cols="40" maxlength="400"><%= Web.esc(p.getNote()) %></textarea>
        </p>
        <p>
            <label><input type="checkbox" name="active" <%= active ? "checked" : "" %>> Bat tuoi tu dong</label>
        </p>

        <button type="submit"><%= editing ? "Luu thay doi" : "Them lich tuoi" %></button>
        <a href="<%= ctx %>/plans">Huy</a>
    </form>
</div>
</body>
</html>