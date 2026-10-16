package controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dao.PotPlantDAO;
import model.AppUser;
import model.PotPlant;

/**
 * Plant management (MVC2: no HTML here, only read the request, call the DAO, forward or redirect).
 *   GET  /plants           list, with keyword search and server side paging
 *   GET  /plants/create    empty form
 *   GET  /plants/edit?id=  form filled with one plant
 *   POST /plants/create    insert
 *   POST /plants/update    update
 *   POST /plants/delete    delete
 *   POST /plants/move      put the plant on another pot
 * AuthFilter decides which role may reach which path, this class does not check roles.
 */
@WebServlet(name = "PotPlantServlet", urlPatterns = {"/plants", "/plants/*"})
public class PotPlantServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;
    private final PotPlantDAO dao = new PotPlantDAO();

    // ---------------------------------------------------------------- GET

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = action(req);
        if (action.isEmpty()) {
            list(req, resp);
        } else if (action.equals("create")) {
            showForm(req, resp, new PotPlant(), false, null);
        } else if (action.equals("edit")) {
            PotPlant o = dao.findById(intParam(req, "id"));
            if (o == null) {
                flash(req, "Khong tim thay cay");
                resp.sendRedirect(req.getContextPath() + "/plants");
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
            flash(req, ok ? "Da xoa cay" : "Khong xoa duoc cay");
            resp.sendRedirect(req.getContextPath() + "/plants");
        } else if (action.equals("move")) {
            int pot = intParam(req, "potNo");
            boolean ok = (pot == 1 || pot == 2) && dao.movePot(intParam(req, "id"), pot);
            flash(req, ok ? "Da chuyen cay sang chau " + pot : "Khong chuyen duoc cay");
            resp.sendRedirect(req.getContextPath() + "/plants");
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
        List<PotPlant> rows = dao.search(kw, page, PAGE_SIZE);

        req.setAttribute("rows", rows);
        req.setAttribute("page", Integer.valueOf(page));
        req.setAttribute("pages", Integer.valueOf(pages));
        req.setAttribute("total", Integer.valueOf(total));
        req.setAttribute("kw", kw == null ? "" : kw);
        req.setAttribute("canEdit", Boolean.valueOf(canEdit(req)));
        req.getRequestDispatcher("/WEB-INF/views/plantList.jsp").forward(req, resp);
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp,
                          PotPlant plant, boolean editing, String error)
            throws ServletException, IOException {
        req.setAttribute("plant", plant);
        req.setAttribute("editing", Boolean.valueOf(editing));
        req.setAttribute("error", error);
        req.getRequestDispatcher("/WEB-INF/views/plantForm.jsp").forward(req, resp);
    }

    private void save(HttpServletRequest req, HttpServletResponse resp, boolean creating)
            throws ServletException, IOException {
        PotPlant o = new PotPlant();
        if (!creating) {
            o.setPlantId(intParam(req, "id"));
            if (dao.findById(o.getPlantId()) == null) {
                flash(req, "Khong tim thay cay");
                resp.sendRedirect(req.getContextPath() + "/plants");
                return;
            }
        }

        String error = fill(req, o);
        if (error == null) {
            int r = creating ? dao.insert(o) : dao.update(o);
            boolean ok = creating ? r > 0 : r == 1;
            if (r == -1) error = "Ma cay da ton tai";
            else if (!ok) error = "Khong luu duoc, thu lai";
        }

        if (error != null) {                       // show the form again with what was typed
            showForm(req, resp, o, !creating, error);
            return;
        }
        flash(req, creating ? "Da them cay" : "Da cap nhat cay");
        resp.sendRedirect(req.getContextPath() + "/plants");
    }

    // ------------------------------------------------------------ helpers

    /**
     * Copies the form fields into o and returns the first error message, or null when valid.
     * Every field is copied first, so the form can be shown again with the typed values.
     */
    private String fill(HttpServletRequest req, PotPlant o) {
        o.setCode(trim(req.getParameter("code")));
        o.setName(trim(req.getParameter("name")));
        String species = trim(req.getParameter("species"));
        String note = trim(req.getParameter("note"));
        o.setSpecies(species.isEmpty() ? null : species);
        o.setNote(note.isEmpty() ? null : note);
        o.setActive(req.getParameter("active") != null);

        String potErr = null;
        String pot = trim(req.getParameter("potNo"));
        if (pot.isEmpty()) {
            o.setPotNo(null);
        } else if (pot.equals("1") || pot.equals("2")) {
            o.setPotNo(Integer.valueOf(pot));
        } else {
            potErr = "Chau chi duoc la 1 hoac 2";
        }

        String dateErr = null;
        String d = trim(req.getParameter("plantedAt"));
        if (d.isEmpty()) {
            o.setPlantedAt(null);
        } else {
            try {
                o.setPlantedAt(Timestamp.valueOf(LocalDate.parse(d).atStartOfDay()));
            } catch (DateTimeParseException e) {
                dateErr = "Ngay gieo khong hop le";
            }
        }

        if (o.getCode().isEmpty() || o.getCode().length() > 32) return "Ma cay bat buoc, toi da 32 ky tu";
        if (o.getName().isEmpty() || o.getName().length() > 150) return "Ten cay bat buoc, toi da 150 ky tu";
        if (species.length() > 100) return "Ten loai toi da 100 ky tu";
        if (note.length() > 400) return "Ghi chu toi da 400 ky tu";
        if (potErr != null) return potErr;
        return dateErr;
    }

    /** "/plants" gives "", "/plants/edit" gives "edit". */
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