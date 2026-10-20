<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.util.Locale, dao.ReportDAO, util.Web" %>
<%!
    private String f(double v, int digits) {
        return String.format(Locale.US, "%." + digits + "f", v);
    }

    private String meaning(String label) {
        if ("OK".equals(label)) return "Binh thuong";
        if ("OVERWATER".equals(label)) return "Tuoi thua";
        if ("NO_UPTAKE".equals(label)) return "Dau do nghi hong";
        if ("DRY_STRESS".equals(label)) return "Thieu nuoc";
        if ("LEAK".equals(label)) return "Nghi ro nuoc";
        return label;
    }

    private String labelColor(String label) {
        if ("OK".equals(label)) return "#2e9e5b";
        if ("OVERWATER".equals(label)) return "#1a6fb5";
        if ("NO_UPTAKE".equals(label)) return "#d33";
        if ("DRY_STRESS".equals(label)) return "#e08a00";
        if ("LEAK".equals(label)) return "#7a4fb5";
        return "#999";
    }

    private String sevColor(String sev) {
        if ("CRITICAL".equals(sev)) return "#d33";
        if ("WARN".equals(sev)) return "#f5c542";
        return "#2e9e5b";
    }
%>
<%
    int pot = ((Integer) request.getAttribute("pot")).intValue();
    List<ReportDAO.WaterSummary> waterList = (List<ReportDAO.WaterSummary>) request.getAttribute("waterList");
    List<ReportDAO.LabelStat> labelStats = (List<ReportDAO.LabelStat>) request.getAttribute("labelStats");
    ReportDAO.CorrectionStat cs = (ReportDAO.CorrectionStat) request.getAttribute("correctionStat");
    List<ReportDAO.AlertStat> alertStats = (List<ReportDAO.AlertStat>) request.getAttribute("alertStats");
    String xsJs = (String) request.getAttribute("xsJs");
    String ysJs = (String) request.getAttribute("ysJs");
    int scatterCount = ((Integer) request.getAttribute("scatterCount")).intValue();
    String ctx = request.getContextPath();

    // totals over both pots
    int totalWaterings = 0;
    double totalWater = 0, totalRetained = 0;
    for (ReportDAO.WaterSummary w : waterList) {
        totalWaterings += w.waterings;
        totalWater += w.waterTotal;
        totalRetained += w.retainedTotal;
    }
    double totalWaste = Math.max(0, totalWater - totalRetained);
    double wastePct = totalWater > 0 ? 100.0 * totalWaste / totalWater : 0;

    // alert totals
    int aTotal = 0, aAcked = 0, aRejected = 0, aOpen = 0;
    for (ReportDAO.AlertStat a : alertStats) {
        aTotal += a.total;
        aAcked += a.acked;
        aRejected += a.rejected;
        aOpen += a.open;
    }
    int aHandled = aAcked + aRejected;
    double confirmRate = aHandled > 0 ? 100.0 * aAcked / aHandled : 0;
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Bao cao tong hop</title>
    <link rel="stylesheet" href="<%= ctx %>/css/app.css">
    <style>
        .bar-wrap { background: #eceef2; border-radius: 3px; height: 14px; min-width: 120px; }
        .bar { height: 14px; border-radius: 3px; }
        .dot { display: inline-block; width: 10px; height: 10px; border-radius: 50%; margin-right: 5px; }
        .toolbar { display: flex; gap: 8px; margin: 6px 0 14px; }
        @media print {
            .topbar, .menu, .toolbar, .tabs, .flash { display: none; }
            body { background: #fff; }
            .wrap { max-width: none; margin: 0; }
        }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/views/nav.jsp"/>
<div class="wrap">
    <h2>Bao cao tong hop</h2>
    <div class="toolbar">
        <button type="button" onclick="window.print()">In / luu PDF</button>
        <a class="btn ghost" href="<%= ctx %>/export">Xuat CSV</a>
    </div>

    <% if (totalWaterings == 0 && labelStats.isEmpty()) { %>
    <p>Chua co du lieu de lap bao cao.</p>
    <% } else { %>

    <!--Overview cards -->
    <div class="cards">
        <div class="card">
            <div class="num"><%= totalWaterings %></div>
            <div class="cap">Tong so phien tuoi</div>
        </div>
        <div class="card">
            <div class="num"><%= f(totalWater, 0) %> ml</div>
            <div class="cap">Tong luong nuoc da bom</div>
        </div>
        <div class="card">
            <div class="num"><%= f(totalRetained, 0) %> ml</div>
            <div class="cap">Luong nuoc thuc su duoc giu lai</div>
        </div>
        <div class="card">
            <div class="num" style="color:#d33;"><%= f(totalWaste, 0) %> ml</div>
            <div class="cap">Phan lang phi (<%= f(wastePct, 1) %>% luong bom)</div>
        </div>
    </div>

    <!--1 Water per pot -->
    <h3>1. He so giu nuoc va luong nuoc theo tung chau</h3>
    <table class="grid">
        <tr>
            <th>Chau</th>
            <th class="c">So lan tuoi</th>
            <th class="c">Nuoc da bom (ml)</th>
            <th class="c">Nuoc giu lai (ml)</th>
            <th class="c">Lang phi (ml)</th>
            <th class="c">He so giu nuoc TB</th>
            <th class="c">Toc do kho TB (g/gio)</th>
        </tr>
        <% for (ReportDAO.WaterSummary w : waterList) {
               double waste = Math.max(0, w.waterTotal - w.retainedTotal); %>
        <tr>
            <td>Chau <%= w.potNo %></td>
            <td class="c"><%= w.waterings %></td>
            <td class="c"><%= f(w.waterTotal, 1) %></td>
            <td class="c"><%= f(w.retainedTotal, 1) %></td>
            <td class="c"><%= f(waste, 1) %></td>
            <td class="c"><b><%= f(w.avgRetainRatio, 3) %></b></td>
            <td class="c"><%= f(w.avgDryRate, 2) %></td>
        </tr>
        <% } %>
    </table>
    <p class="muted">Nuoc giu lai = tong phan khoi luong chau tang them sau moi lan tuoi (1 ml nuoc ~ 1 g).
       Lang phi = nuoc da bom tru nuoc giu lai.</p>

    <!--2 Label distribution -->
    <h3>2. Phan bo nhan ket qua</h3>
    <table class="grid">
        <tr>
            <th>Nhan</th>
            <th>Y nghia</th>
            <th class="c">So phien</th>
            <th class="c">Ti le</th>
            <th style="width:30%;"></th>
        </tr>
        <% for (ReportDAO.LabelStat l : labelStats) { %>
        <tr>
            <td><span class="dot" style="background:<%= labelColor(l.labelCode) %>;"></span><%= Web.esc(l.labelCode) %></td>
            <td><%= meaning(l.labelCode) %></td>
            <td class="c"><%= l.count %></td>
            <td class="c"><%= f(l.percent, 2) %>%</td>
            <td>
                <div class="bar-wrap">
                    <div class="bar" style="width:<%= Math.min(100, l.percent) %>%; background:<%= labelColor(l.labelCode) %>;"></div>
                </div>
            </td>
        </tr>
        <% } %>
    </table>
    <p class="muted">Ti le tinh tren tong so dong trong bang nhan (gom ca dong do nguoi duyet sua).</p>

    <!-- 3 Correction stat -->
    <h3>3. Chat luong cua bo quy tac tu dong</h3>
    <div class="cards">
        <div class="card">
            <div class="num"><%= cs.editedCount %></div>
            <div class="cap">Phien bi nguoi duyet sua nhan</div>
        </div>
        <div class="card">
            <div class="num"><%= cs.totalCount %></div>
            <div class="cap">Tong so phien</div>
        </div>
        <div class="card">
            <div class="num"><%= f(cs.editRate, 2) %>%</div>
            <div class="cap">Ti le sua (cang thap, quy tac cang dung)</div>
        </div>
    </div>

    <!--4 Alerts -->
    <h3>4. Canh bao theo tung loai</h3>
    <% if (alertStats.isEmpty()) { %>
    <p>Chua co canh bao nao.</p>
    <% } else { %>
    <table class="grid">
        <tr>
            <th>Luat</th>
            <th>Muc do</th>
            <th class="c">Tong</th>
            <th class="c">Da xac nhan</th>
            <th class="c">Da tu choi</th>
            <th class="c">Chua xu ly</th>
        </tr>
        <% for (ReportDAO.AlertStat a : alertStats) { %>
        <tr>
            <td><%= Web.esc(a.ruleCode) %></td>
            <td><span class="dot" style="background:<%= sevColor(a.severity) %>;"></span><%= Web.esc(a.severity) %></td>
            <td class="c"><%= a.total %></td>
            <td class="c"><%= a.acked %></td>
            <td class="c"><%= a.rejected %></td>
            <td class="c"><%= a.open %></td>
        </tr>
        <% } %>
        <tr class="group">
            <td colspan="2">Tong cong</td>
            <td class="c"><%= aTotal %></td>
            <td class="c"><%= aAcked %></td>
            <td class="c"><%= aRejected %></td>
            <td class="c"><%= aOpen %></td>
        </tr>
    </table>
    <p>Ti le canh bao duoc nguoi trong xac nhan la dung:
       <b><%= f(confirmRate, 1) %>%</b>
       (<%= aAcked %> xac nhan / <%= aHandled %> da xu ly, chua tinh <%= aOpen %> canh bao con mo).</p>
    <% } %>

    <!--   5. Scatter -->
    <h3>5. Toc do kho theo cuong do anh sang</h3>
    <div class="tabs">
        <a href="<%= ctx %>/report" class="<%= pot == 0 ? "on" : "" %>">Ca hai chau</a>
        <a href="<%= ctx %>/report?pot=1" class="<%= pot == 1 ? "on" : "" %>">Chau 1</a>
        <a href="<%= ctx %>/report?pot=2" class="<%= pot == 2 ? "on" : "" %>">Chau 2</a>
    </div>
    <% if (scatterCount == 0) { %>
    <p>Khong co du lieu anh sang va toc do kho.</p>
    <% } else { %>
    <canvas id="scatter" width="900" height="360" style="max-width:100%;"></canvas>
    <p class="muted"><%= scatterCount %> phien gan nhat. Moi cham la mot phien; cho nao cham chong len nhau thi mau dam hon.
       Neu anh sang cang manh ma toc do kho cang cao thi cay dang thoat hoi nuoc nhanh hon duoi nang.</p>
    <script src="<%= ctx %>/js/app.js"></script>
    <script>
        drawScatter('scatter', <%= xsJs %>, <%= ysJs %>, 'Cuong do anh sang (light_lux)', 'Toc do kho (g/gio)');
    </script>
    <% } %>

    <% } %>
</div>
</body>
</html>