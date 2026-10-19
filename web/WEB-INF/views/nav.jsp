<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.AppUser, util.Web" %>
<%
    AppUser me = (AppUser) session.getAttribute("user");
    String ctx = request.getContextPath();
    String flash = (String) session.getAttribute("flash");
    if (flash != null) session.removeAttribute("flash");
%>
<div class="topbar">
    <div class="brand">PlantGrow</div>
    <% if (me != null) { %>
    <div class="who">
        <%= Web.esc(me.getFullName()) %>
        <span class="tag"><%= Web.esc(me.getRoleCode()) %></span>
        <a class="out" href="<%= ctx %>/logout">Logout</a>
    </div>
    <% } %>
</div>
<% if (me != null) { %>
<div class="menu">
    <a href="<%= ctx %>/dashboard">Bang dieu khien</a>
    <a href="<%= ctx %>/sessions">Nhat ky tuoi</a>
    <a href="<%= ctx %>/chart">Bieu do chau</a>
    <a href="<%= ctx %>/plants">Quan ly cay</a>
    <a href="<%= ctx %>/plans">Lich tuoi</a>
    <a href="<%= ctx %>/calibs">Hieu chuan bom</a>
    <a href="<%= ctx %>/alerts">Canh bao</a>
    <a href="<%= ctx %>/export">Xuat CSV</a>
    <a href="<%= ctx %>/report">Bao cao</a>
    <% if ("ADMIN".equals(me.getRoleCode())) { %><a href="<%= ctx %>/admin/users">Nguoi dung</a><% } %>
</div>
<% } %>
<% if (flash != null) { %><div class="flash"><%= Web.esc(flash) %></div><% } %>