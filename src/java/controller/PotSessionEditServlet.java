package controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dao.AlertDAO;
import dao.DeviceDAO;
import dao.LabelDAO;
import dao.PotSessionDAO;
import model.PotLabel;
import model.PotSession;
import service.PotRuleEngine;

/**
 * Create, edit and delete a session by hand (MVC2: no HTML here).
 *   GET  /session/create   empty form
 *   GET  /session/edit     form filled with one session
 *   POST /session/create   insert, then label it with the rule engine
 *   POST /session/update   update, then re-run the rule engine
 *   POST /session/delete   delete the session with its labels and alerts
 * AuthFilter decides which role may reach which path, this class does not check roles.
 */
@WebServlet(name = "PotSessionEditServlet",
        urlPatterns = {"/session/create", "/session/edit", "/session/update", "/session/delete"})
public class PotSessionEditServlet extends HttpServlet {

    // Accepted ranges. Change them here if the team agrees on other limits.
    private static final double WEIGHT_MAX = 1000;   // the load cell is 1 kg
    private static final double WATER_MAX = 250;
    private static final double MOIST_MAX = 1000;
    private static final double LUX_MAX = 10000;
    private static final double DRY_MAX = 100;

    private final PotSessionDAO dao = new PotSessionDAO();
    private final DeviceDAO devices = new DeviceDAO();
    private final LabelDAO labels = new LabelDAO();
    private final AlertDAO alerts = new AlertDAO();
    private final PotRuleEngine engine = new PotRuleEngine();

    // ---------------------------------------------------------------- GET

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/session/create".equals(path)) {
            PotSession o = new PotSession();
            o.setMeasuredAt(new Timestamp(System.currentTimeMillis()));
            showForm(req, resp, o, false, null);
        } else if ("/session/edit".equals(path)) {
            PotSession o = dao.findById(intParam(req, "id"));
            if (o == null) {
                flash(req, "Khong tim thay phien");
                resp.sendRedirect(req.getContextPath() + "/sessions");
                return;
            }
            showForm(req, resp, o, true, null);
        } else {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    // --------------------------------------------------------------- POST

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getServletPath();
        if ("/session/create".equals(path)) {
            create(req, resp);
        } else if ("/session/update".equals(path)) {
            update(req, resp);
        } else if ("/session/delete".equals(path)) {
            boolean ok = dao.delete(intParam(req, "id"));
            flash(req, ok ? "Da xoa phien" : "Khong xoa duoc phien");
            resp.sendRedirect(req.getContextPath() + "/sessions");
        } else {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    // ------------------------------------------------------------ actions

    private void showForm(HttpServletRequest req, HttpServletResponse resp,
                          PotSession o, boolean editing, String error)
            throws ServletException, IOException {
        req.setAttribute("s", o);
        req.setAttribute("editing", Boolean.valueOf(editing));
        req.setAttribute("error", error);
        req.getRequestDispatcher("/WEB-INF/views/sessionForm.jsp").forward(req, resp);
    }

    private void create(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        PotSession o = new PotSession();
        String error = fill(req, o);

        Timestamp when = parseWhen(req.getParameter("measuredAt"));
        o.setMeasuredAt(when != null ? when : new Timestamp(System.currentTimeMillis()));
        if (when == null && error == null) error = "Thoi diem do khong hop le";

        Integer deviceId = devices.findFirstActiveId();
        if (deviceId == null && error == null) error = "Chua co thiet bi nao dang hoat dong";

        int id = 0;
        if (error == null) {
            id = -1;
            for (int attempt = 0; attempt < 3 && id == -1; attempt++) {   // -1 = sequence number taken
                o.setDeviceId(deviceId.intValue());
                o.setDeviceSeq(dao.nextManualSeq(deviceId.intValue()));
                id = dao.insert(o);
            }
            if (id <= 0) error = "Khong luu duoc phien, thu lai";
        }
        if (error != null) {
            showForm(req, resp, o, false, error);
            return;
        }

        // label it exactly like a packet from the device would be labelled
        String label = engine.classify(o);
        labels.insert(id, label, "RULE", "Phien nhap tay tren web", null);
        String sev = engine.severityOf(label);
        if (!"INFO".equals(sev)) {
            alerts.insert(id, label, sev, engine.messageOf(label));
        }
        flash(req, "Da them phien, nhan gan: " + label);
        resp.sendRedirect(req.getContextPath() + "/session/detail?id=" + id);
    }

    private void update(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = intParam(req, "id");
        PotSession old = dao.findById(id);
        if (old == null) {
            flash(req, "Khong tim thay phien");
            resp.sendRedirect(req.getContextPath() + "/sessions");
            return;
        }

        PotSession o = new PotSession();
        o.setSessionId(id);
        o.setMeasuredAt(old.getMeasuredAt());      // the time of a measurement does not change
        String error = fill(req, o);
        if (error == null && !dao.update(o)) error = "Khong luu duoc, thu lai";
        if (error != null) {
            showForm(req, resp, o, true, error);
            return;
        }

        // the readings changed, so ask the rule engine again; keep the history, only append
        List<PotLabel> hist = labels.findBySession(id);
        String current = hist.isEmpty() ? "" : hist.get(0).getLabelCode();
        String fresh = engine.classify(o);
        if (!fresh.equals(current)) {
            labels.insert(id, fresh, "RULE", "Tinh lai sau khi sua so do", null);
            flash(req, "Da cap nhat phien, nhan duoc tinh lai thanh " + fresh);
        } else {
            flash(req, "Da cap nhat phien, nhan giu nguyen: " + current);
        }
        resp.sendRedirect(req.getContextPath() + "/session/detail?id=" + id);
    }

    // ------------------------------------------------------------ helpers

    /**
     * Copies the readings from the form into o and returns the first error, or null when valid.
     * retain_ratio is not typed: it is (weight after - weight before) / water, as the subject defines it.
     * A bad field keeps a default value so the form can be shown again.
     */
    private String fill(HttpServletRequest req, PotSession o) {
        String err = null;

        String pot = trim(req.getParameter("potNo"));
        if (pot.equals("1") || pot.equals("2")) {
            o.setPotNo(Integer.parseInt(pot));
        } else {
            o.setPotNo(1);
            err = "Phai chon chau 1 hoac 2";
        }

        double wb = num(req.getParameter("weightBefore"), 0, WEIGHT_MAX);
        if (Double.isNaN(wb)) { wb = 450; err = first(err, "Khoi luong truoc tuoi phai tu 0 den " + (int) WEIGHT_MAX + " g"); }
        double wa = num(req.getParameter("weightAfter"), 0, WEIGHT_MAX);
        if (Double.isNaN(wa)) { wa = 500; err = first(err, "Khoi luong sau tuoi phai tu 0 den " + (int) WEIGHT_MAX + " g"); }
        double ml = num(req.getParameter("waterMl"), 0, WATER_MAX);
        if (Double.isNaN(ml)) { ml = 50; err = first(err, "Luong nuoc bom phai tu 0 den " + (int) WATER_MAX + " ml"); }
        double mb = num(req.getParameter("moistBefore"), 0, MOIST_MAX);
        if (Double.isNaN(mb) || mb != Math.rint(mb)) { mb = 350; err = first(err, "Do am truoc tuoi phai la so nguyen tu 0 den " + (int) MOIST_MAX); }
        double ma = num(req.getParameter("moistAfter"), 0, MOIST_MAX);
        if (Double.isNaN(ma) || ma != Math.rint(ma)) { ma = 700; err = first(err, "Do am sau tuoi phai la so nguyen tu 0 den " + (int) MOIST_MAX); }
        double lux = num(req.getParameter("lightLux"), 0, LUX_MAX);
        if (Double.isNaN(lux) || lux != Math.rint(lux)) { lux = 500; err = first(err, "Cuong do anh sang phai la so nguyen tu 0 den " + (int) LUX_MAX); }
        double dry = num(req.getParameter("dryRate"), 0, DRY_MAX);
        if (Double.isNaN(dry)) { dry = 2.5; err = first(err, "Toc do kho phai tu 0 den " + (int) DRY_MAX + " g/gio"); }

        o.setWeightBefore(wb);
        o.setWeightAfter(wa);
        o.setWaterMl(ml);
        o.setMoistBefore((int) mb);
        o.setMoistAfter((int) ma);
        o.setLightLux((int) lux);
        o.setDryRate(dry);

        double ratio = ml > 0 ? (wa - wb) / ml : 0;
        ratio = Math.max(-999, Math.min(999, ratio));            // the column holds DECIMAL(9,3)
        o.setRetainRatio(new BigDecimal(ratio).setScale(3, RoundingMode.HALF_UP).doubleValue());
        return err;
    }

    private String first(String a, String b) {
        return a != null ? a : b;
    }

    /** A number in [min, max] (a comma is accepted as the decimal mark), or NaN when invalid. */
    private double num(String s, double min, double max) {
        if (s == null) return Double.NaN;
        try {
            double d = Double.parseDouble(s.trim().replace(',', '.'));
            if (Double.isNaN(d) || Double.isInfinite(d) || d < min || d > max) return Double.NaN;
            return d;
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    /** Reads the value of an <input type="datetime-local">, such as 2026-10-07T10:30. Null if invalid. */
    private Timestamp parseWhen(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Timestamp.valueOf(LocalDateTime.parse(s.trim()));
        } catch (DateTimeParseException e) {
            return null;
        }
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