<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.text.SimpleDateFormat, model.PotPumpCalib, util.Web" %>
<%
    PotPumpCalib c = (PotPumpCalib) request.getAttribute("calib");
    String error = (String) request.getAttribute("error");
    String ctx = request.getContextPath();
    int pot = c.getPotNo();
    String when = c.getCalibratedAt() == null ? "-"
            : new SimpleDateFormat("dd/MM/yyyy HH:mm").format(c.getCalibratedAt());
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Sua ban hieu chuan</title>
    <link rel="stylesheet" href="<%= ctx %>/css/app.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/nav.jsp"/>
<div class="wrap">
    <h2>Sua ban hieu chuan</h2>
    <p>Chi dung de chua loi go nham. Nguoi va thoi diem hieu chuan (<%= when %>) giu nguyen.
       Neu do lai bom, hay nhap mot ban hieu chuan moi o trang danh sach.</p>

    <% if (error != null) { %>
    <div class="error" style="color:red; margin-bottom:10px;"><%= Web.esc(error) %></div>
    <% } %>

    <form method="post" action="<%= ctx %>/calibs/update">
        <input type="hidden" name="id" value="<%= c.getPumpCalibId() %>">

        <p>
            <label>Ma (bat buoc, khong trung)</label><br>
            <input type="text" name="code" maxlength="32" required
                   value="<%= Web.esc(c.getCode()) %>">
        </p>
        <p>
            <label>Ten (bat buoc)</label><br>
            <input type="text" name="name" maxlength="150" required size="50"
                   value="<%= Web.esc(c.getName()) %>">
        </p>
        <p>
            <label>Bom cua chau</label><br>
            <select name="potNo" required>
                <option value="1" <%= pot == 1 ? "selected" : "" %>>Chau 1</option>
                <option value="2" <%= pot == 2 ? "selected" : "" %>>Chau 2</option>
            </select>
        </p>
        <p>
            <label>Toc do bom (ml/giay, tu 0.05 den 100)</label><br>
            <input type="number" name="mlPerSecond" min="0.05" max="100" step="0.001" required
                   value="<%= c.getMlPerSecond() %>">
        </p>
        <p>
            <label>Ghi chu</label><br>
            <textarea name="note" rows="3" cols="40" maxlength="400"><%= Web.esc(c.getNote()) %></textarea>
        </p>
        <p>
            <label><input type="checkbox" name="active" <%= c.isActive() ? "checked" : "" %>> Dang hoat dong</label>
        </p>

        <button type="submit">Luu thay doi</button>
        <a href="<%= ctx %>/calibs">Huy</a>
    </form>
</div>
</body>
</html>