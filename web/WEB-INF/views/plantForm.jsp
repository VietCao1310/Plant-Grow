<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.text.SimpleDateFormat, model.PotPlant, util.Web" %>
<%
    PotPlant p = (PotPlant) request.getAttribute("plant");
    boolean editing = ((Boolean) request.getAttribute("editing")).booleanValue();
    String error = (String) request.getAttribute("error");
    String ctx = request.getContextPath();

    String planted = p.getPlantedAt() == null ? ""
            : new SimpleDateFormat("yyyy-MM-dd").format(p.getPlantedAt());
    Integer pot = p.getPotNo();

    // A brand new form (no error yet) starts with "active" ticked.
    boolean active = (!editing && error == null) ? true : p.isActive();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><%= editing ? "Sua cay" : "Them cay" %></title>
    <link rel="stylesheet" href="<%= ctx %>/css/app.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/nav.jsp"/>
<div class="wrap">
    <h2><%= editing ? "Sua cay" : "Them cay moi" %></h2>

    <% if (error != null) { %>
    <div class="error" style="color:red; margin-bottom:10px;"><%= Web.esc(error) %></div>
    <% } %>

    <form method="post" action="<%= ctx %>/plants/<%= editing ? "update" : "create" %>">
        <% if (editing) { %>
        <input type="hidden" name="id" value="<%= p.getPlantId() %>">
        <% } %>

        <p>
            <label>Ma cay (bat buoc, khong trung)</label><br>
            <input type="text" name="code" maxlength="32" required
                   value="<%= Web.esc(p.getCode()) %>">
        </p>
        <p>
            <label>Ten cay (bat buoc)</label><br>
            <input type="text" name="name" maxlength="150" required
                   value="<%= Web.esc(p.getName()) %>">
        </p>
        <p>
            <label>Ten loai</label><br>
            <input type="text" name="species" maxlength="100"
                   value="<%= Web.esc(p.getSpecies()) %>">
        </p>
        <p>
            <label>Dang dat o chau</label><br>
            <select name="potNo">
                <option value="" <%= pot == null ? "selected" : "" %>>-- Chua dat --</option>
                <option value="1" <%= (pot != null && pot == 1) ? "selected" : "" %>>Chau 1</option>
                <option value="2" <%= (pot != null && pot == 2) ? "selected" : "" %>>Chau 2</option>
            </select>
        </p>
        <p>
            <label>Ngay gieo</label><br>
            <input type="date" name="plantedAt" value="<%= planted %>">
        </p>
        <p>
            <label>Ghi chu</label><br>
            <textarea name="note" rows="3" cols="40" maxlength="400"><%= Web.esc(p.getNote()) %></textarea>
        </p>
        <p>
            <label><input type="checkbox" name="active" <%= active ? "checked" : "" %>> Dang hoat dong</label>
        </p>

        <button type="submit"><%= editing ? "Luu thay doi" : "Them cay" %></button>
        <a href="<%= ctx %>/plants">Huy</a>
    </form>
</div>
</body>
</html>