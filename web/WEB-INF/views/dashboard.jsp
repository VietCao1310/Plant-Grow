<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.text.SimpleDateFormat, model.PotSession, model.PotAlert, util.Web" %>
<%!
    private String color(String sev) {
        if ("CRITICAL".equals(sev)) return "#d33";
        if ("WARN".equals(sev)) return "#f5c542";
        if ("INFO".equals(sev)) return "#2e9e5b";
        return "#999";
    }

    private String textColor(String sev) {
        return "WARN".equals(sev) ? "#000" : "#fff";
    }

    /** The meaning of a label in plain words. */
    private String meaning(String label) {
        if ("OK".equals(label)) return "Binh thuong";
        if ("OVERWATER".equals(label)) return "Tuoi thua";
        if ("NO_UPTAKE".equals(label)) return "Dau do nghi hong";
        if ("DRY_STRESS".equals(label)) return "Thieu nuoc";
        if ("LEAK".equals(label)) return "Nghi ro nuoc";
        return "Chua co nhan";
    }

    /** "5 phut truoc", "3 gio truoc", "2 ngay truoc". */
    private String ago(java.sql.Timestamp t) {
        long min = (System.currentTimeMillis() - t.getTime()) / 60000L;
        if (min < 1) return "vua xong";
        if (min < 60) return min + " phut truoc";
        if (min < 48 * 60) return (min / 60) + " gio truoc";
        return (min / 1440) + " ngay truoc";
    }
%>
<%
    PotSession[] pots = {(PotSession) request.getAttribute("pot1"), (PotSession) request.getAttribute("pot2")};
    String[] sevs = {(String) request.getAttribute("sev1"), (String) request.getAttribute("sev2")};
    Integer light = (Integer) request.getAttribute("light");
    List<PotAlert> alerts = (List<PotAlert>) request.getAttribute("alerts");
    int openCount = ((Integer) request.getAttribute("openCount")).intValue();
    String ctx = request.getContextPath();
    SimpleDateFormat fmt = new SimpleDateFormat("dd/MM HH:mm");
    SimpleDateFormat full = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta http-equiv="refresh" content="30">
    <title>Bang dieu khien</title>
    <link rel="stylesheet" href="<%= ctx %>/css/app.css">
</head>
<body>
<jsp:include page="/WEB-INF/views/nav.jsp"/>
<div class="wrap">
    <h2>Bang dieu khien</h2>
    <p class="muted">Cap nhat luc <%= full.format(new java.util.Date()) %> (tu dong lam moi moi 30 giay).
       So lieu moi chau la cua lan tuoi gan nhat, sau khi nuoc tham.</p>

    <div style="display:flex; flex-wrap:wrap; gap:16px; margin-bottom:20px;">
        <% for (int i = 0; i < 2; i++) {
               PotSession p = pots[i];
               String sev = sevs[i]; %>
        <div style="flex:1; min-width:260px; border:3px solid <%= color(sev) %>; border-radius:8px; padding:14px;">
            <div style="display:flex; justify-content:space-between; align-items:center;">
                <h3 style="margin:0;">Chau <%= i + 1 %></h3>
                <span style="background:<%= color(sev) %>; color:<%= textColor(sev) %>; padding:3px 10px; border-radius:4px;">
                    <%= p == null ? "Chua co du lieu" : Web.esc(p.getLabelCode()) + " - " + meaning(p.getLabelCode()) %>
                </span>
            </div>
            <% if (p == null) { %>
            <p>Chau nay chua co lan tuoi nao.</p>
            <% } else { %>
            <p style="font-size:30px; margin:10px 0 2px;"><b><%= String.format("%.1f", p.getWeightAfter()) %> g</b></p>
            <p class="muted" style="margin-top:0;">Khoi luong chau</p>
            <table class="grid">
                <tr><td>Do am dat</td><td><b><%= p.getMoistAfter() %></b></td></tr>
                <tr><td>Lan tuoi gan nhat</td>
                    <td><%= fmt.format(p.getMeasuredAt()) %> (<%= ago(p.getMeasuredAt()) %>)</td></tr>
                <tr><td>Luong nuoc / he so giu nuoc</td>
                    <td><%= String.format("%.0f", p.getWaterMl()) %> ml / <%= String.format("%.3f", p.getRetainRatio()) %></td></tr>
                <tr><td>Toc do kho</td><td><%= String.format("%.2f", p.getDryRate()) %> g/gio</td></tr>
            </table>
            <p><a href="<%= ctx %>/session/detail?id=<%= p.getSessionId() %>">Xem chi tiet lan tuoi nay</a></p>
            <% } %>
        </div>
        <% } %>

        <div style="min-width:180px; border:3px solid #1a6fb5; border-radius:8px; padding:14px;">
            <h3 style="margin:0;">Anh sang</h3>
            <p style="font-size:30px; margin:10px 0 2px;"><b><%= light == null ? "-" : light.toString() %></b></p>
            <p class="muted" style="margin-top:0;">Cuong do chung cua khay (don vi quy uoc)</p>
        </div>
    </div>

    <h3>Canh bao chua xu ly (<%= openCount %>)</h3>
    <table class="grid">
        <tr><th>Luc</th><th>Muc do</th><th>Luat</th><th>Chau</th><th>Noi dung</th><th>Phien</th></tr>
        <% for (PotAlert a : alerts) { %>
        <tr>
            <td><%= fmt.format(a.getCreatedAt()) %></td>
            <td><span style="background:<%= color("WARN".equals(a.getSeverity()) ? "WARN" : ("CRITICAL".equals(a.getSeverity()) ? "CRITICAL" : "NONE")) %>;
                             color:<%= textColor(a.getSeverity()) %>; padding:2px 8px; border-radius:3px;">
                <%= Web.esc(a.getSeverity()) %></span></td>
            <td><%= Web.esc(a.getRuleCode()) %></td>
            <td><%= a.getPotNo() == null ? "-" : a.getPotNo().toString() %></td>
            <td><%= Web.esc(a.getMessage()) %></td>
            <td>
                <% if (a.getSessionId() != null) { %>
                <a href="<%= ctx %>/session/detail?id=<%= a.getSessionId() %>">#<%= a.getSessionId() %></a>
                <% } else { %>-<% } %>
            </td>
        </tr>
        <% } %>
        <% if (alerts.isEmpty()) { %>
        <tr><td colspan="6">Khong co canh bao nao dang cho xu ly.</td></tr>
        <% } %>
    </table>
    <p><a href="<%= ctx %>/alerts">Xem tat ca canh bao &raquo;</a></p>
</div>
</body>
</html>