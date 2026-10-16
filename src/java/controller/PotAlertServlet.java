package controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dao.AlertDAO;
import dao.PotSessionDAO;
import model.AppUser;
import model.PotAlert;

/*
  Alerts
   GET  /alerts          list, filter by status and severity, server side paging
   POST /alerts/update   a person confirms (ACKED), rejects (REJECTED) or reopens (OPEN) an alert
   POST /alerts/create   ADMIN adds an alert by hand
   POST /alerts/delete   ADMIN deletes an alert
 AuthFilter decides which role may reach which path, this class does not check roles.
 */
@WebServlet(name = "PotAlertServlet", urlPatterns = {"/alerts", "/alerts/*"})
public class PotAlertServlet extends HttpServlet {

    private static final int PAGE_SIZE = 15;
    private static final List<String> STATUSES = Arrays.asList("OPEN", "ACKED", "REJECTED");
    private static final List<String> SEVERITIES = Arrays.asList("INFO", "WARN", "CRITICAL");

    private final AlertDAO dao = new AlertDAO();
    private final PotSessionDAO sessions = new PotSessionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!action(req).isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String status = pick(req.getParameter("status"), STATUSES);
        String severity = pick(req.getParameter("severity"), SEVERITIES);
        int page = Math.max(1, intParam(req, "page"));

        int total = dao.count(status, severity);
        int pages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
        if (page > pages) page = pages;
        List<PotAlert> rows = dao.search(status, severity, page, PAGE_SIZE);

        AppUser me = (AppUser) req.getSession().getAttribute("user");
        String role = me == null ? "" : me.getRoleCode();

        req.setAttribute("rows", rows);
        req.setAttribute("page", Integer.valueOf(page));
        req.setAttribute("pages", Integer.valueOf(pages));
        req.setAttribute("total", Integer.valueOf(total));
        req.setAttribute("status", status);
        req.setAttribute("severity", severity);
        req.setAttribute("openCount", Integer.valueOf(dao.count("OPEN", "")));
        req.setAttribute("canHandle", Boolean.valueOf("ADMIN".equals(role) || "GROWER".equals(role)
                || "TECHNICIAN".equals(role) || "ANALYST".equals(role)));
        req.setAttribute("canAdmin", Boolean.valueOf("ADMIN".equals(role)));
        req.getRequestDispatcher("/WEB-INF/views/alertList.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = action(req);
        if (action.equals("update")) {
            update(req);
        } else if (action.equals("create")) {
            create(req);
        } else if (action.equals("delete")) {
            boolean ok = dao.delete(intParam(req, "id"));
            flash(req, ok ? "Da xoa canh bao" : "Khong xoa duoc canh bao");
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        resp.sendRedirect(backUrl(req));
    }

    private void update(HttpServletRequest req) {
        PotAlert a = dao.findById(intParam(req, "id"));
        if (a == null) {
            flash(req, "Khong tim thay canh bao");
            return;
        }
        String status = trim(req.getParameter("status"));
        String note = trim(req.getParameter("note"));

        if (!STATUSES.contains(status)) {
            flash(req, "Trang thai khong hop le");
        } else if ("REJECTED".equals(status) && note.length() < 5) {
            flash(req, "Phai ghi ly do khi tu choi canh bao (it nhat 5 ky tu)");
        } else if (note.length() > 400) {
            flash(req, "Ghi chu toi da 400 ky tu");
        } else {
            AppUser me = (AppUser) req.getSession().getAttribute("user");
            boolean reopen = "OPEN".equals(status);
            Integer by = reopen ? null : Integer.valueOf(me.getUserId());
            String n = (reopen || note.isEmpty()) ? null : note;
            boolean ok = dao.handle(a.getAlertId(), status, by, n);
            flash(req, ok ? "Da cap nhat canh bao #" + a.getAlertId() + ": " + status
                          : "Khong cap nhat duoc canh bao");
        }
    }

    private void create(HttpServletRequest req) {
        String rule = trim(req.getParameter("ruleCode"));
        String severity = trim(req.getParameter("severity"));
        String message = trim(req.getParameter("message"));
        String sidText = trim(req.getParameter("sessionId"));

        if (rule.isEmpty() || rule.length() > 32) {
            flash(req, "Ma luat bat buoc, toi da 32 ky tu");
            return;
        }
        if (!SEVERITIES.contains(severity)) {
            flash(req, "Muc do khong hop le");
            return;
        }
        if (message.isEmpty() || message.length() > 400) {
            flash(req, "Noi dung bat buoc, toi da 400 ky tu");
            return;
        }
        Integer sessionId = null;
        if (!sidText.isEmpty()) {
            try {
                sessionId = Integer.valueOf(Integer.parseInt(sidText));
            } catch (NumberFormatException e) {
                flash(req, "Ma phien phai la so");
                return;
            }
            if (sessions.findById(sessionId.intValue()) == null) {
                flash(req, "Khong co phien so " + sessionId);
                return;
            }
        }
        int id = dao.createManual(sessionId, rule, severity, message);
        flash(req, id > 0 ? "Da them canh bao #" + id : "Khong them duoc canh bao");
    }

    /*
      Where to go after a POST: the list again with the same filters and page.
      The values are checked here, so nothing typed by the user is copied into the address.
     */
    private String backUrl(HttpServletRequest req) {
        StringBuilder sb = new StringBuilder(req.getContextPath()).append("/alerts?page=")
                .append(Math.max(1, intParam(req, "page")));
        String status = pick(req.getParameter("status"), STATUSES);
        String severity = pick(req.getParameter("severity"), SEVERITIES);
        if (!status.isEmpty()) sb.append("&status=").append(status);
        if (!severity.isEmpty()) sb.append("&severity=").append(severity);
        return sb.toString();
    }

    // Returns value when it is one of the allowed values, otherwise "" (no filter)
    private String pick(String value, List<String> allowed) {
        return value != null && allowed.contains(value.trim()) ? value.trim() : "";
    }

    // "/alerts" gives "", "/alerts/update" gives "update"
    private String action(HttpServletRequest req) {
        String p = req.getPathInfo();
        if (p == null || p.equals("/")) return "";
        return p.substring(1);
    }

    private int intParam(HttpServletRequest req, String name) {
        try {
            return Integer.parseInt(req.getParameter(name).trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private void flash(HttpServletRequest req, String msg) {
        req.getSession().setAttribute("flash", msg);   // nav.jsp shows it once, then removes it
    }
}