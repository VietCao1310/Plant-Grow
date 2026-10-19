<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%
    int countAll = ((Integer) request.getAttribute("countAll")).intValue();
    int countReal = ((Integer) request.getAttribute("countReal")).intValue();
    int countSample = ((Integer) request.getAttribute("countSample")).intValue();
    String ctx = request.getContextPath();
    String[] labels = {"OK", "OVERWATER", "NO_UPTAKE", "DRY_STRESS", "LEAK"};
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Xuat CSV</title>
    <link rel="stylesheet" href="<%= ctx %>/css/app.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/nav.jsp"/>
<div class="wrap">
    <h2>Xuat so lieu ra tep CSV</h2>

    <table class="grid">
        <tr><th>Nhom du lieu</th><th>So phien</th></tr>
        <tr><td>Tat ca</td><td><b><%= countAll %></b></td></tr>
        <tr><td>Du lieu that (thiet bi va nhap tay)</td><td><%= countReal %></td></tr>
        <tr><td>Du lieu mau (is_sample = 1)</td><td><%= countSample %></td></tr>
    </table>

    <p>Moi dong la mot phien, gom cac so do, nhan hien tai, <b>nguon cua nhan</b> (RULE hoac REVIEWER),
       ly do va nguoi sua, va <b>rule_label</b> la nhan goc do bo quy tac gan.</p>

    <form method="get" action="<%= ctx %>/export/sessions">
        <p>
            <label>Pham vi</label><br>
            <select name="scope">
                <option value="all">Tat ca (<%= countAll %>)</option>
                <option value="real">Chi du lieu that (<%= countReal %>)</option>
                <option value="sample">Chi du lieu mau (<%= countSample %>)</option>
            </select>
        </p>
        <p>
            <label>Nhan</label><br>
            <select name="label">
                <option value="">-- Moi nhan --</option>
                <% for (String l : labels) { %>
                <option value="<%= l %>"><%= l %></option>
                <% } %>
            </select>
        </p>
        <button type="submit">Tai tep CSV</button>
    </form>

    <p class="muted">Tep co dau BOM UTF-8 nen Excel hien dung chu co dau. Mo bang Python thi dung
       encoding='utf-8-sig'. Ban sao luu cuoi moi buoi thu so lieu nen chon "Chi du lieu that".</p>
</div>
</body>
</html>