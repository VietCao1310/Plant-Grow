package controller;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dao.PotWaterPlanDAO;
import model.AppUser;
import model.PotWaterPlan;

/**
 * Watering schedule management (MVC2: no HTML here).
 *   GET  /plans           list, with keyword search and server side paging
 *   GET  /plans/create    empty form
 *   GET  /plans/edit?id=  form filled with one schedule
 *   POST /plans/create    insert
 *   POST /plans/update    update
 *   POST /plans/delete    delete
 *   POST /plans/toggle    turn automatic watering of this schedule on or off
 * AuthFilter decides which role may reach which path, this class does not check roles.
 */
@WebServlet(name = "PotWaterPlanServlet", urlPatterns = {"/plans", "/plans/*"})
public class PotWaterPlanServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;

    // Accepted ranges for the three numbers. Change them here if the team agrees on other limits.
    private static final double ML_MIN = 1, ML_MAX = 250;
    private static final int HOURS_MIN = 1, HOURS_MAX = 168;      // up to one week
    private static final int MOIST_MIN = 0, MOIST_MAX = 1000;

    private final PotWaterPlanDAO dao = new PotWaterPlanDAO();

    // ---------------------------------------------------------------- GET

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = action(req);
        if (action.isEmpty()) {
            list(req, resp);
        } else if (action.equals("create")) {
            showForm(req, resp, new PotWaterPlan(), false, null);
        } else if (action.equals("edit")) {
            PotWaterPlan o = dao.findById(intParam(req, "id"));
            if (o == null) {
                flash(req, "Khong tim thay lich tuoi");
                resp.sendRedirect(req.getContextPath() + "/plans");
                return;
            }
            showForm(req, resp, o, true, null);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // --------------------------------------------------------------- POST

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = action(req);
        if (action.equals("create") || action.equals("update")) {
            save(req, resp, action.equals("create"));
        } else if (action.equals("delete")) {
            boolean ok = dao.delete(intParam(req, "id"));
            flash(req, ok ? "Da xoa lich tuoi" : "Khong xoa duoc lich tuoi");
            resp.sendRedirect(req.getContextPath() + "/plans");
        } else if (action.equals("toggle")) {
            boolean ok = dao.toggleActive(intParam(req, "id"));
            flash(req, ok ? "Da doi trang thai tuoi tu dong" : "Khong doi duoc trang thai");
            resp.sendRedirect(req.getContextPath() + "/plans");
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // ------------------------------------------------------------ actions

    private void list(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String kw = req.getParameter("kw");
        int page = Math.max(1, intParam(req, "page"));

        int total = dao.count(kw);
        int pages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
        if (page > pages) page = pages;
        List<PotWaterPlan> rows = dao.search(kw, page, PAGE_SIZE);

        req.setAttribute("rows", rows);
        req.setAttribute("page", Integer.valueOf(page));
        req.setAttribute("pages", Integer.valueOf(pages));
        req.setAttribute("total", Integer.valueOf(total));
        req.setAttribute("kw", kw == null ? "" : kw);
        req.setAttribute("canEdit", Boolean.valueOf(canEdit(req)));
        req.getRequestDispatcher("/WEB-INF/views/planList.jsp").forward(req, resp);
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp,
                          PotWaterPlan plan, boolean editing, String error)
            throws ServletException, IOException {
        req.setAttribute("plan", plan);
        req.setAttribute("editing", Boolean.valueOf(editing));
        req.setAttribute("error", error);
        req.getRequestDispatcher("/WEB-INF/views/planForm.jsp").forward(req, resp);
    }

    private void save(HttpServletRequest req, HttpServletResponse resp, boolean creating)
            throws ServletException, IOException {
        PotWaterPlan o = new PotWaterPlan();
        if (!creating) {
            o.setWaterPlanId(intParam(req, "id"));
            if (dao.findById(o.getWaterPlanId()) == null) {
                flash(req, "Khong tim thay lich tuoi");
                resp.sendRedirect(req.getContextPath() + "/plans");
                return;
            }
        }

        String error = fill(req, o);
        if (error == null) {
            int r = creating ? dao.insert(o) : dao.update(o);
            boolean ok = creating ? r > 0 : r == 1;
            if (r == -1) error = "Ma lich tuoi da ton tai";
            else if (!ok) error = "Khong luu duoc, thu lai";
        }

        if (error != null) {                       // show the form again with what was typed
            showForm(req, resp, o, !creating, error);
            return;
        }
        flash(req, creating ? "Da them lich tuoi" : "Da cap nhat lich tuoi");
        resp.sendRedirect(req.getContextPath() + "/plans");
    }

    // ------------------------------------------------------------ helpers

    /**
     * Copies the form fields into o and returns the first error message, or null when valid.
     * A field that cannot be read keeps a safe default, so the form can still be shown again.
     */
    private String fill(HttpServletRequest req, PotWaterPlan o) {
        o.setCode(trim(req.getParameter("code")));
        o.setName(trim(req.getParameter("name")));
        String note = trim(req.getParameter("note"));
        o.setNote(note.isEmpty() ? null : note);
        o.setActive(req.getParameter("active") != null);

        String err = null;

        String pot = trim(req.getParameter("potNo"));
        if (pot.equals("1") || pot.equals("2")) {
            o.setPotNo(Integer.parseInt(pot));
        } else {
            o.setPotNo(1);
            err = "Phai chon chau 1 hoac 2";
        }

        Double ml = number(req.getParameter("waterTargetMl"));
        if (ml == null || ml < ML_MIN || ml > ML_MAX) {
            o.setWaterTargetMl(50);
            if (err == null) err = "Luong nuoc moi lan phai tu " + (int) ML_MIN + " den " + (int) ML_MAX + " ml";
        } else {
            o.setWaterTargetMl(ml);
        }

        Double hours = number(req.getParameter("intervalHours"));
        if (hours == null || hours != Math.rint(hours) || hours < HOURS_MIN || hours > HOURS_MAX) {
            o.setIntervalHours(12);
            if (err == null) err = "Khoang cach giua hai lan tuoi phai la so gio nguyen tu " + HOURS_MIN + " den " + HOURS_MAX;
        } else {
            o.setIntervalHours(hours.intValue());
        }

        Double moist = number(req.getParameter("triggerMoist"));
        if (moist == null || moist != Math.rint(moist) || moist < MOIST_MIN || moist > MOIST_MAX) {
            o.setTriggerMoist(250);
            if (err == null) err = "Nguong do am phai la so nguyen tu " + MOIST_MIN + " den " + MOIST_MAX;
        } else {
            o.setTriggerMoist(moist.intValue());
        }

        if (o.getCode().isEmpty() || o.getCode().length() > 32) return "Ma lich tuoi bat buoc, toi da 32 ky tu";
        if (o.getName().isEmpty() || o.getName().length() > 150) return "Ten lich tuoi bat buoc, toi da 150 ky tu";
        if (note.length() > 400) return "Ghi chu toi da 400 ky tu";
        return err;
    }

    /** Reads a number such as 50 or 50.5 (a comma is accepted too). Returns null if it is not a number. */
    private Double number(String s) {
        if (s == null) return null;
        try {
            double d = Double.parseDouble(s.trim().replace(',', '.'));
            if (Double.isNaN(d) || Double.isInfinite(d)) return null;
            return Double.valueOf(d);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** "/plans" gives "", "/plans/edit" gives "edit". */
    private String action(HttpServletRequest req) {
        String p = req.getPathInfo();
        if (p == null || p.equals("/")) return "";
        return p.substring(1);
    }

    private boolean canEdit(HttpServletRequest req) {
        AppUser me = (AppUser) req.getSession().getAttribute("user");
        if (me == null) return false;
        String r = me.getRoleCode();
        return "ADMIN".equals(r) || "GROWER".equals(r) || "TECHNICIAN".equals(r);
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