package controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dao.PotPumpCalibDAO;
import model.AppUser;
import model.PotPumpCalib;

/**
 * Pump calibration (MVC2: no HTML here).
 *   GET  /calibs           history list, with the "new measurement" form on top
 *   GET  /calibs/edit?id=  form to fix a typing mistake on one row
 *   POST /calibs/create    a new measurement: seconds + ml collected, the rate is computed here
 *   POST /calibs/update    fix a row
 *   POST /calibs/delete    delete a row
 * AuthFilter decides which role may reach which path, this class does not check roles.
 */
@WebServlet(name = "PotPumpCalibServlet", urlPatterns = {"/calibs", "/calibs/*"})
public class PotPumpCalibServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;

    // Accepted ranges. Change them here if the team agrees on other limits.
    private static final double SEC_MIN = 1, SEC_MAX = 60;         // pumping time of one test
    private static final double ML_MIN = 0.1, ML_MAX = 2000;       // water collected in the cup
    private static final double RATE_MIN = 0.05, RATE_MAX = 100;   // resulting ml per second

    private final PotPumpCalibDAO dao = new PotPumpCalibDAO();

    // ---------------------------------------------------------------- GET

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = action(req);
        if (action.isEmpty()) {
            list(req, resp, null);
        } else if (action.equals("edit")) {
            PotPumpCalib o = dao.findById(intParam(req, "id"));
            if (o == null) {
                flash(req, "Khong tim thay ban hieu chuan");
                resp.sendRedirect(req.getContextPath() + "/calibs");
                return;
            }
            showEditForm(req, resp, o, null);
        } else if (action.equals("create")) {
            resp.sendRedirect(req.getContextPath() + "/calibs");   // the create form is on the list page
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
        if (action.equals("create")) {
            create(req, resp);
        } else if (action.equals("update")) {
            update(req, resp);
        } else if (action.equals("delete")) {
            boolean ok = dao.delete(intParam(req, "id"));
            flash(req, ok ? "Da xoa ban hieu chuan" : "Khong xoa duoc ban hieu chuan");
            resp.sendRedirect(req.getContextPath() + "/calibs");
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // ------------------------------------------------------------ actions

    /** Shows the history. error is shown above the form, and the typed values stay in the request. */
    private void list(HttpServletRequest req, HttpServletResponse resp, String error)
            throws ServletException, IOException {
        String kw = req.getParameter("kw");
        int page = Math.max(1, intParam(req, "page"));

        int total = dao.count(kw);
        int pages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
        if (page > pages) page = pages;
        List<PotPumpCalib> rows = dao.search(kw, page, PAGE_SIZE);

        req.setAttribute("rows", rows);
        req.setAttribute("page", Integer.valueOf(page));
        req.setAttribute("pages", Integer.valueOf(pages));
        req.setAttribute("total", Integer.valueOf(total));
        req.setAttribute("kw", kw == null ? "" : kw);
        req.setAttribute("canEdit", Boolean.valueOf(canEdit(req)));
        req.setAttribute("error", error);
        req.getRequestDispatcher("/WEB-INF/views/calibList.jsp").forward(req, resp);
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp,
                              PotPumpCalib o, String error)
            throws ServletException, IOException {
        req.setAttribute("calib", o);
        req.setAttribute("error", error);
        req.getRequestDispatcher("/WEB-INF/views/calibForm.jsp").forward(req, resp);
    }

    /** A new measurement: the person types seconds and ml, the rate and the code are made here. */
    private void create(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pot = trim(req.getParameter("potNo"));
        String secText = trim(req.getParameter("seconds"));
        String mlText = trim(req.getParameter("measuredMl"));
        String note = trim(req.getParameter("note"));

        // keep what was typed, so the form on the list page can be filled in again
        req.setAttribute("typedPot", pot);
        req.setAttribute("typedSeconds", secText);
        req.setAttribute("typedMl", mlText);
        req.setAttribute("typedNote", note);

        String error = null;
        Double sec = number(secText);
        Double ml = number(mlText);
        double rate = 0;

        if (!pot.equals("1") && !pot.equals("2")) {
            error = "Phai chon bom cua chau 1 hoac 2";
        } else if (sec == null || sec < SEC_MIN || sec > SEC_MAX) {
            error = "So giay bom phai tu " + (int) SEC_MIN + " den " + (int) SEC_MAX;
        } else if (ml == null || ml < ML_MIN || ml > ML_MAX) {
            error = "So ml dong duoc phai lon hon 0 va toi da " + (int) ML_MAX;
        } else if (note.length() > 400) {
            error = "Ghi chu toi da 400 ky tu";
        } else {
            rate = round3(ml / sec);
            if (rate < RATE_MIN || rate > RATE_MAX) {
                error = "Ket qua " + rate + " ml/giay khong hop ly, kiem tra lai so giay va so ml";
            }
        }
        if (error != null) {
            list(req, resp, error);
            return;
        }

        Date now = new Date();
        AppUser me = (AppUser) req.getSession().getAttribute("user");

        PotPumpCalib o = new PotPumpCalib();
        o.setCode("CALIB-P" + pot + "-" + new SimpleDateFormat("yyyyMMddHHmmss").format(now));
        o.setName("Hieu chuan bom chau " + pot + " luc " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(now));
        o.setPotNo(Integer.parseInt(pot));
        o.setMlPerSecond(rate);
        o.setCalibratedBy(me == null ? null : Integer.valueOf(me.getUserId()));
        o.setNote(note.isEmpty() ? null : note);
        o.setActive(true);

        int r = dao.insert(o);
        if (r == -1) {
            list(req, resp, "Vua co mot ban hieu chuan cung giay, thu lai sau it giay");
            return;
        }
        if (r <= 0) {
            list(req, resp, "Khong luu duoc, thu lai");
            return;
        }
        flash(req, "Da luu hieu chuan bom chau " + pot + ": " + rate + " ml/giay");
        resp.sendRedirect(req.getContextPath() + "/calibs");
    }

    /** Fixes a typing mistake. The person and the time of the measurement stay as they were. */
    private void update(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = intParam(req, "id");
        if (dao.findById(id) == null) {
            flash(req, "Khong tim thay ban hieu chuan");
            resp.sendRedirect(req.getContextPath() + "/calibs");
            return;
        }

        PotPumpCalib o = new PotPumpCalib();
        o.setPumpCalibId(id);
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

        Double rate = number(req.getParameter("mlPerSecond"));
        if (rate == null || rate < RATE_MIN || rate > RATE_MAX) {
            o.setMlPerSecond(10);
            if (err == null) err = "Toc do bom phai tu " + RATE_MIN + " den " + (int) RATE_MAX + " ml/giay";
        } else {
            o.setMlPerSecond(round3(rate));
        }

        if (o.getCode().isEmpty() || o.getCode().length() > 32) err = "Ma bat buoc, toi da 32 ky tu";
        else if (o.getName().isEmpty() || o.getName().length() > 150) err = "Ten bat buoc, toi da 150 ky tu";
        else if (note.length() > 400) err = "Ghi chu toi da 400 ky tu";

        if (err == null) {
            int r = dao.update(o);
            if (r == -1) err = "Ma da ton tai";
            else if (r != 1) err = "Khong luu duoc, thu lai";
        }
        if (err != null) {
            showEditForm(req, resp, o, err);
            return;
        }
        flash(req, "Da cap nhat ban hieu chuan");
        resp.sendRedirect(req.getContextPath() + "/calibs");
    }

    // ------------------------------------------------------------ helpers

    private double round3(double v) {
        return new BigDecimal(v).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

    /** Reads a number such as 10 or 10.5 (a comma is accepted too). Returns null if it is not a number. */
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

    /** "/calibs" gives "", "/calibs/edit" gives "edit". */
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