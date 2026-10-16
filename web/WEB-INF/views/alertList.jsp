<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.text.SimpleDateFormat, model.PotAlert, util.Web" %>
<%!
    /** A small coloured badge: yellow for WARN, red for CRITICAL, grey for INFO. */
    private String badge(String severity) {
        String bg = "#999", fg = "#fff";
        if ("WARN".equals(severity)) { bg = "#f5c542"; fg = "#000"; }
        if ("CRITICAL".equals(severity)) { bg = "#d33"; }
        return "<span style=\"background:" + bg + ";color:" + fg
                + ";padding:2px 8px;border-radius:3px;\">" + util.Web.esc(severity) + "</span>";
    }
%>
<%
    List<PotAlert> rows = (List<PotAlert>) request.getAttribute("rows");
    int cur = ((Integer) request.getAttribute("page")).intValue();
    int pages = ((Integer) request.getAttribute("pages")).intValue();
    int total = ((Integer) request.getAttribute("total")).intValue();
    int openCount = ((Integer) request.getAttribute("openCount")).intValue();
    String status = (String) request.getAttribute("status");
    String severity = (String) request.getAttribute("severity");
    boolean canHandle = ((Boolean) request.getAttribute("canHandle")).booleanValue();
    boolean canAdmin = ((Boolean) request.getAttribute("canAdmin")).booleanValue();
    String ctx = request.getContextPath();
    SimpleDateFormat fmt = new SimpleDateFormat("dd/MM HH:mm:ss");
    String[] statuses = {"OPEN", "ACKED", "REJECTED"};
    String[] severities = {"INFO", "WARN", "CRITICAL"};
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Canh bao</title>
    <link rel="stylesheet" href="<%= ctx %>/css/app.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/nav.jsp"/>
<div class="wrap">
    <h2>Canh bao (<%= total %>) &nbsp; <small>Chua xu ly: <b><%= openCount %></b></small></h2>

    <form method="get" action="<%= ctx %>/alerts" class="filter">
        <select name="status">
            <option value="">-- Moi trang thai --</option>
            <% for (String st : statuses) { %>
            <option value="<%= st %>" <%= st.equals(status) ? "selected" : "" %>><%= st %></option>
            <% } %>
        </select>
        <select name="severity">
            <option value="">-- Moi muc do --</option>
            <% for (String sv : severities) { %>
            <option value="<%= sv %>" <%= sv.equals(severity) ? "selected" : "" %>><%= sv %></option>
            <% } %>
        </select>
        <button type="submit">Loc</button>
    </form>

    <table class="grid">
        <tr>
            <th>#</th><th>Luc</th><th>Muc do</th><th>Luat</th><th>Chau</th><th>Phien</th>
            <th>Noi dung</th><th>Trang thai</th><th>Nguoi xu ly</th>
            <% if (canHandle || canAdmin) { %><th>Xu ly</th><% } %>
        </tr>
        <% for (PotAlert a : rows) { %>
        <tr>
            <td><%= a.getAlertId() %></td>
            <td><%= fmt.format(a.getCreatedAt()) %></td>
            <td><%= badge(a.getSeverity()) %></td>
            <td><%= Web.esc(a.getRuleCode()) %></td>
            <td><%= a.getPotNo() == null ? "-" : a.getPotNo().toString() %></td>
            <td>
                <% if (a.getSessionId() != null) { %>
                <a href="<%= ctx %>/session/detail?id=<%= a.getSessionId() %>">#<%= a.getSessionId() %></a>
                <% } else { %>-<% } %>
            </td>
            <td><%= Web.esc(a.getMessage()) %></td>
            <td><b><%= Web.esc(a.getStatus()) %></b></td>
            <td>
                <%= a.getHandledByName() == null ? "-" : Web.esc(a.getHandledByName()) %>
                <% if (a.getHandledNote() != null) { %><br><i><%= Web.esc(a.getHandledNote()) %></i><% } %>
            </td>
            <% if (canHandle || canAdmin) { %>
            <td>
                <% if (canHandle) { %>
                <form method="post" action="<%= ctx %>/alerts/update">
                    <input type="hidden" name="id" value="<%= a.getAlertId() %>">
                    <input type="hidden" name="page" value="<%= cur %>">
                    <input type="hidden" name="status_filter" value="">
                    <input type="hidden" name="severity_filter" value="">
                    <select name="status">
                        <option value="ACKED">Xac nhan (dung)</option>
                        <option value="REJECTED">Tu choi (bao nham)</option>
                        <option value="OPEN">Mo lai</option>
                    </select>
                    <input type="text" name="note" maxlength="400" placeholder="Ghi chu / ly do">
                    <button type="submit">Luu</button>
                </form>
                <% } %>
                <% if (canAdmin) { %>
                <form method="post" action="<%= ctx %>/alerts/delete" style="display:inline"
                      onsubmit="return confirm('Xoa canh bao nay?');">
                    <input type="hidden" name="id" value="<%= a.getAlertId() %>">
                    <input type="hidden" name="page" value="<%= cur %>">
                    <button type="submit">Xoa</button>
                </form>
                <% } %>
            </td>
            <% } %>
        </tr>
        <% } %>
        <% if (rows.isEmpty()) { %>
        <tr><td colspan="10">Khong co canh bao nao.</td></tr>
        <% } %>
    </table>

    <div class="pager">
        <% for (int p = 1; p <= pages; p++) {
               if (p == cur) { %><b><%= p %></b> <% }
               else { %><a href="?page=<%= p %>&status=<%= status %>&severity=<%= severity %>"><%= p %></a> <% }
           } %>
    </div>

    <% if (canAdmin) { %>
    <hr>
    <h3>Them canh bao thu cong (ADMIN)</h3>
    <form method="post" action="<%= ctx %>/alerts/create">
        <p>
            <label>Ma luat</label><br>
            <input type="text" name="ruleCode" maxlength="32" required placeholder="vi du OVERWATER">
        </p>
        <p>
            <label>Muc do</label><br>
            <select name="severity" required>
                <% for (String sv : severities) { %>
                <option value="<%= sv %>" <%= "WARN".equals(sv) ? "selected" : "" %>><%= sv %></option>
                <% } %>
            </select>
        </p>
        <p>
            <label>Noi dung</label><br>
            <input type="text" name="message" maxlength="400" size="60" required>
        </p>
        <p>
            <label>Ma phien (khong bat buoc)</label><br>
            <input type="number" name="sessionId" min="1">
        </p>
        <button type="submit">Them canh bao</button>
    </form>
    <% } %>
</div>
</body>
</html>