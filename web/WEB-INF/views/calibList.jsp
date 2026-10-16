<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.text.SimpleDateFormat, java.net.URLEncoder, model.PotPumpCalib, util.Web" %>
<%
    List<PotPumpCalib> rows = (List<PotPumpCalib>) request.getAttribute("rows");
    int cur = ((Integer) request.getAttribute("page")).intValue();
    int pages = ((Integer) request.getAttribute("pages")).intValue();
    int total = ((Integer) request.getAttribute("total")).intValue();
    String kw = (String) request.getAttribute("kw");
    boolean canEdit = ((Boolean) request.getAttribute("canEdit")).booleanValue();
    String error = (String) request.getAttribute("error");
    String ctx = request.getContextPath();
    SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // values typed in the form before an error, so the person does not have to type them again
    String typedPot = (String) request.getAttribute("typedPot");
    String typedSeconds = (String) request.getAttribute("typedSeconds");
    String typedMl = (String) request.getAttribute("typedMl");
    String typedNote = (String) request.getAttribute("typedNote");
    if (typedSeconds == null || typedSeconds.isEmpty()) typedSeconds = "10";
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Hieu chuan bom</title>
    <link rel="stylesheet" href="<%= ctx %>/css/app.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/nav.jsp"/>
<div class="wrap">
    <h2>Hieu chuan bom</h2>

    <% if (canEdit) { %>
    <h3>Nhap ket qua dong nuoc</h3>
    <p>Cho bom chay vao coc dong, nhap so giay bom va so ml dong duoc. He thong tu tinh ml/giay.</p>

    <% if (error != null) { %>
    <div class="error" style="color:red; margin-bottom:10px;"><%= Web.esc(error) %></div>
    <% } %>

    <form method="post" action="<%= ctx %>/calibs/create">
        <p>
            <label>Bom cua chau</label><br>
            <select name="potNo" required>
                <option value="" <%= (typedPot == null || typedPot.isEmpty()) ? "selected" : "" %>>-- Chon chau --</option>
                <option value="1" <%= "1".equals(typedPot) ? "selected" : "" %>>Chau 1</option>
                <option value="2" <%= "2".equals(typedPot) ? "selected" : "" %>>Chau 2</option>
            </select>
        </p>
        <p>
            <label>So giay bom (tu 1 den 60)</label><br>
            <input type="number" name="seconds" min="1" max="60" step="0.1" required
                   value="<%= Web.esc(typedSeconds) %>">
        </p>
        <p>
            <label>So ml dong duoc trong coc</label><br>
            <input type="number" name="measuredMl" min="0.1" max="2000" step="0.1" required
                   value="<%= Web.esc(typedMl) %>">
        </p>
        <p>
            <label>Ghi chu</label><br>
            <input type="text" name="note" maxlength="400" size="50"
                   value="<%= Web.esc(typedNote) %>">
        </p>
        <button type="submit">Luu hieu chuan</button>
    </form>
    <hr>
    <% } %>

    <h3>Lich su hieu chuan (<%= total %>)</h3>

    <form method="get" action="<%= ctx %>/calibs" class="filter">
        <input type="text" name="kw" value="<%= Web.esc(kw) %>" placeholder="Ma hoac ten">
        <button type="submit">Tim</button>
    </form>

    <table class="grid">
        <tr>
            <th>#</th><th>Ma</th><th>Chau</th><th>ml/giay</th><th>Nguoi hieu chuan</th>
            <th>Thoi diem</th><th>Ghi chu</th><th>Hoat dong</th>
            <% if (canEdit) { %><th></th><% } %>
        </tr>
        <% for (PotPumpCalib r : rows) { %>
        <tr>
            <td><%= r.getPumpCalibId() %></td>
            <td><%= Web.esc(r.getCode()) %></td>
            <td><%= r.getPotNo() %></td>
            <td><b><%= r.getMlPerSecond() %></b></td>
            <td><%= r.getCalibratedByName() == null ? "-" : Web.esc(r.getCalibratedByName()) %></td>
            <td><%= r.getCalibratedAt() == null ? "-" : fmt.format(r.getCalibratedAt()) %></td>
            <td><%= Web.esc(r.getNote()) %></td>
            <td><%= r.isActive() ? "Co" : "Khong" %></td>
            <% if (canEdit) { %>
            <td>
                <a href="<%= ctx %>/calibs/edit?id=<%= r.getPumpCalibId() %>">Sua</a>

                <form method="post" action="<%= ctx %>/calibs/delete" style="display:inline"
                      onsubmit="return confirm('Xoa ban hieu chuan nay?');">
                    <input type="hidden" name="id" value="<%= r.getPumpCalibId() %>">
                    <button type="submit">Xoa</button>
                </form>
            </td>
            <% } %>
        </tr>
        <% } %>
        <% if (rows.isEmpty()) { %>
        <tr><td colspan="9">Chua co ban hieu chuan nao.</td></tr>
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