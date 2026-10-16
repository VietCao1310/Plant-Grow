<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.text.SimpleDateFormat, model.PotSession, model.PotLabel, util.Web" %>
<%
    PotSession s = (PotSession) request.getAttribute("s");
    List<PotLabel> history = (List<PotLabel>) request.getAttribute("history");
    String seriesJs = (String) request.getAttribute("seriesJs");
    int seriesSize = ((Integer) request.getAttribute("seriesSize")).intValue();
    int chartPoints = ((Integer) request.getAttribute("chartPoints")).intValue();
    boolean canCorrect = ((Boolean) request.getAttribute("canCorrect")).booleanValue();
    String ctx = request.getContextPath();
    SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    String current = history.isEmpty() ? "-" : history.get(0).getLabelCode();
    double gained = s.getWeightAfter() - s.getWeightBefore();
    double recomputed = s.getWaterMl() > 0 ? gained / s.getWaterMl() : 0;

    // Two timestamps: the board's and the server's. A big gap means the packet was sent late.
    long lagSec = (s.getIngestedAt().getTime() - s.getMeasuredAt().getTime()) / 1000L;
    String lagText = s.isSample() ? "-" : (lagSec > 120 ? "Gui bu (tre " + lagSec + " giay)" : "Gui ngay");

    String[] labels = {"OK", "OVERWATER", "NO_UPTAKE", "DRY_STRESS", "LEAK"};
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Chi tiet phien <%= s.getSessionId() %></title>
    <link rel="stylesheet" href="<%= ctx %>/css/app.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/nav.jsp"/>
<div class="wrap">
    <p><a href="<%= ctx %>/sessions">&laquo; Ve danh sach phien</a></p>
    <h2>Chi tiet phien #<%= s.getSessionId() %>
        <span class="tag"><%= Web.esc(current) %></span></h2>

    <table class="grid">
        <tr><th>Thong tin</th><th>Gia tri</th></tr>
        <tr><td>Chau</td><td><%= s.getPotNo() %></td></tr>
        <tr><td>Nguon du lieu</td><td><%= s.isSample() ? "sample (du lieu mau)" : "device (thiet bi)" %></td></tr>
        <tr><td>So thu tu goi tin (device_seq)</td><td><%= s.getDeviceSeq() %></td></tr>
        <tr><td>Thoi diem do (bo mach ghi)</td><td><%= fmt.format(s.getMeasuredAt()) %></td></tr>
        <tr><td>Thoi diem may chu nhan</td><td><%= fmt.format(s.getIngestedAt()) %></td></tr>
        <tr><td>Duong truyen</td><td><%= lagText %></td></tr>
        <tr><td>Khoi luong truoc tuoi (g)</td><td><%= String.format("%.2f", s.getWeightBefore()) %></td></tr>
        <tr><td>Khoi luong sau tuoi (g)</td><td><%= String.format("%.2f", s.getWeightAfter()) %></td></tr>
        <tr><td>Khoi luong tang them (g)</td><td><%= String.format("%.2f", gained) %></td></tr>
        <tr><td>Luong nuoc bom (ml)</td><td><%= String.format("%.2f", s.getWaterMl()) %></td></tr>
        <tr><td>He so giu nuoc (do duoc luu)</td><td><b><%= String.format("%.3f", s.getRetainRatio()) %></b></td></tr>
        <tr><td>He so giu nuoc (tinh lai: tang them / nuoc bom)</td><td><%= String.format("%.3f", recomputed) %></td></tr>
        <tr><td>Do am dat truoc / sau</td><td><%= s.getMoistBefore() %> &rarr; <%= s.getMoistAfter() %></td></tr>
        <tr><td>Cuong do anh sang</td><td><%= s.getLightLux() %></td></tr>
        <tr><td>Toc do kho (g/gio)</td><td><%= String.format("%.3f", s.getDryRate()) %></td></tr>
    </table>

    <h3>He so giu nuoc cua chau <%= s.getPotNo() %> (<%= chartPoints %> phien gan nhat toi phien nay)</h3>
    <% if (seriesSize == 0) { %>
    <p>Chua co du lieu de ve.</p>
    <% } else { %>
    <canvas id="chart" width="680" height="240"></canvas>
    <p class="muted">Cham do la phien dang xem. Duong di xuong dot ngot cho thay nhung lan tuoi thua.</p>
    <script src="<%= ctx %>/js/app.js"></script>
    <script>drawSeries('chart', <%= seriesJs %>, 'retain_ratio');</script>
    <% } %>

    <h3>Lich su nhan (moi nhat o tren)</h3>
    <table class="grid">
        <tr><th>Nhan</th><th>Nguon</th><th>Nguoi gan</th><th>Ly do</th><th>Luc</th></tr>
        <% for (PotLabel l : history) { %>
        <tr>
            <td><span class="tag"><%= Web.esc(l.getLabelCode()) %></span></td>
            <td><%= "REVIEWER".equals(l.getSource()) ? "Nguoi duyet" : "Quy tac tu dong" %></td>
            <td><%= l.getLabeledByName() == null ? "-" : Web.esc(l.getLabeledByName()) %></td>
            <td><%= Web.esc(l.getReason()) %></td>
            <td><%= fmt.format(l.getLabeledAt()) %></td>
        </tr>
        <% } %>
        <% if (history.isEmpty()) { %>
        <tr><td colspan="5">Phien nay chua co nhan.</td></tr>
        <% } %>
    </table>

    <% if (canCorrect) { %>
    <h3>Sua nhan</h3>
    <p>Dong nhan cu duoc giu lai, nhan moi duoc them vao lich su. Bat buoc ghi ly do.</p>
    <form method="post" action="<%= ctx %>/label/correct">
        <input type="hidden" name="sessionId" value="<%= s.getSessionId() %>">
        <p>
            <label>Nhan moi</label><br>
            <select name="labelCode" required>
                <% for (String l : labels) { %>
                <option value="<%= l %>" <%= l.equals(current) ? "selected" : "" %>><%= l %></option>
                <% } %>
            </select>
        </p>
        <p>
            <label>Ly do (it nhat 5 ky tu)</label><br>
            <textarea name="reason" rows="3" cols="50" minlength="5" maxlength="400" required></textarea>
        </p>
        <button type="submit">Luu nhan moi</button>
    </form>
    <% } %>
</div>
</body>
</html>