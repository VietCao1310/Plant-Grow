<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.text.SimpleDateFormat, model.PotSession, util.Web" %>
<%
    PotSession s = (PotSession) request.getAttribute("s");
    boolean editing = ((Boolean) request.getAttribute("editing")).booleanValue();
    String error = (String) request.getAttribute("error");
    String ctx = request.getContextPath();

    // A brand new form (no error yet) starts with typical values from the subject instead of zeros.
    boolean fresh = !editing && error == null;
    int pot = fresh ? 1 : s.getPotNo();
    String wb = fresh ? "450" : String.valueOf(s.getWeightBefore());
    String wa = fresh ? "500" : String.valueOf(s.getWeightAfter());
    String ml = fresh ? "50" : String.valueOf(s.getWaterMl());
    String mb = fresh ? "350" : String.valueOf(s.getMoistBefore());
    String ma = fresh ? "700" : String.valueOf(s.getMoistAfter());
    String lux = fresh ? "500" : String.valueOf(s.getLightLux());
    String dry = fresh ? "2.5" : String.valueOf(s.getDryRate());

    String whenInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(s.getMeasuredAt());
    String whenShown = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(s.getMeasuredAt());
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><%= editing ? "Sua phien" : "Them phien" %></title>
    <link rel="stylesheet" href="<%= ctx %>/css/app.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/nav.jsp"/>
<div class="wrap">
    <h2><%= editing ? "Sua phien #" + s.getSessionId() : "Them phien nhap tay" %></h2>
    <p>He so giu nuoc khong nhap tay: he thong tinh bang (khoi luong sau - khoi luong truoc) / luong nuoc bom.
       Sau khi luu, nhan cua phien duoc tinh lai bang bo quy tac.</p>
    <% if (!editing) { %>
    <p class="muted">Phien nhap tay co so thu tu am va hien la "manual" trong danh sach.
       Du lieu thiet bi that nen den tu thiet bi.</p>
    <% } %>

    <% if (error != null) { %>
    <div class="error" style="color:red; margin-bottom:10px;"><%= Web.esc(error) %></div>
    <% } %>

    <form method="post" action="<%= ctx %>/session/<%= editing ? "update" : "create" %>">
        <% if (editing) { %>
        <input type="hidden" name="id" value="<%= s.getSessionId() %>">
        <p><b>Thoi diem do:</b> <%= whenShown %> (khong doi duoc)</p>
        <% } else { %>
        <p>
            <label>Thoi diem do</label><br>
            <input type="datetime-local" name="measuredAt" required value="<%= whenInput %>">
        </p>
        <% } %>

        <p>
            <label>Chau</label><br>
            <select name="potNo" required>
                <option value="1" <%= pot == 1 ? "selected" : "" %>>Chau 1</option>
                <option value="2" <%= pot == 2 ? "selected" : "" %>>Chau 2</option>
            </select>
        </p>
        <p>
            <label>Khoi luong truoc tuoi (g, tu 0 den 1000)</label><br>
            <input type="number" name="weightBefore" min="0" max="1000" step="0.01" required value="<%= wb %>">
        </p>
        <p>
            <label>Khoi luong sau tuoi va tham (g, tu 0 den 1000)</label><br>
            <input type="number" name="weightAfter" min="0" max="1000" step="0.01" required value="<%= wa %>">
        </p>
        <p>
            <label>Luong nuoc bom (ml, tu 0 den 250)</label><br>
            <input type="number" name="waterMl" min="0" max="250" step="0.01" required value="<%= ml %>">
        </p>
        <p>
            <label>Do am dat truoc tuoi (so nguyen, tu 0 den 1000)</label><br>
            <input type="number" name="moistBefore" min="0" max="1000" step="1" required value="<%= mb %>">
        </p>
        <p>
            <label>Do am dat sau tuoi (so nguyen, tu 0 den 1000)</label><br>
            <input type="number" name="moistAfter" min="0" max="1000" step="1" required value="<%= ma %>">
        </p>
        <p>
            <label>Cuong do anh sang (so nguyen, tu 0 den 10000)</label><br>
            <input type="number" name="lightLux" min="0" max="10000" step="1" required value="<%= lux %>">
        </p>
        <p>
            <label>Toc do kho (g/gio, tu 0 den 100)</label><br>
            <input type="number" name="dryRate" min="0" max="100" step="0.001" required value="<%= dry %>">
        </p>

        <button type="submit"><%= editing ? "Luu thay doi" : "Them phien" %></button>
        <a href="<%= editing ? ctx + "/session/detail?id=" + s.getSessionId() : ctx + "/sessions" %>">Huy</a>
    </form>
</div>
</body>
</html>