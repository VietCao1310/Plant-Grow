package controller;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dao.UserDAO;
import model.AppRole;
import model.AppUser;
import util.PasswordUtil;

/**
 * Account administration (MVC2: no HTML here). AuthFilter lets only ADMIN under /admin.
 *   GET  /admin/users          list, keyword search, server side paging
 *   GET  /admin/users/create   empty form
 *   GET  /admin/users/edit     form with one account (name, role) and the reset password form
 *   POST /admin/users/create   insert, the password is hashed first
 *   POST /admin/users/update   change the name and the role
 *   POST /admin/users/lock     lock or unlock
 *   POST /admin/users/reset    set a new password
 *   POST /admin/users/delete   delete
 * An admin can not lock, delete or change the role of his own account.
 */
@WebServlet(name = "UserAdminServlet", urlPatterns = {"/admin/users", "/admin/users/*"})
public class UserAdminServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;
    private static final int PASS_MIN = 6, PASS_MAX = 64;
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_.-]{3,64}");

    private final UserDAO dao = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = action(req);
        if (action.isEmpty()) {
            list(req, resp);
        } else if (action.equals("create")) {
            showForm(req, resp, new AppUser(), false, null);
        } else if (action.equals("edit")) {
            AppUser u = dao.findById(intParam(req, "id"));
            if (u == null) {
                flash(req, "Khong tim thay tai khoan");
                resp.sendRedirect(req.getContextPath() + "/admin/users");
                return;
            }
            showForm(req, resp, u, true, null);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = action(req);
        String list = req.getContextPath() + "/admin/users";
        if (action.equals("create")) {
            create(req, resp);
        } else if (action.equals("update")) {
            update(req, resp);
        } else if (action.equals("lock")) {
            lock(req);
            resp.sendRedirect(list);
        } else if (action.equals("reset")) {
            int id = intParam(req, "id");
            boolean ok = reset(req, id);
            resp.sendRedirect(ok ? list : list + "/edit?id=" + id);
        } else if (action.equals("delete")) {
            delete(req);
            resp.sendRedirect(list);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void list(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String kw = req.getParameter("kw");
        int page = Math.max(1, intParam(req, "page"));
        int total = dao.count(kw);
        int pages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
        if (page > pages) page = pages;

        req.setAttribute("rows", dao.search(kw, page, PAGE_SIZE));
        req.setAttribute("page", Integer.valueOf(page));
        req.setAttribute("pages", Integer.valueOf(pages));
        req.setAttribute("total", Integer.valueOf(total));
        req.setAttribute("kw", kw == null ? "" : kw);
        req.setAttribute("meId", Integer.valueOf(me(req).getUserId()));
        req.getRequestDispatcher("/WEB-INF/views/userList.jsp").forward(req, resp);
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp,
                          AppUser u, boolean editing, String error)
            throws ServletException, IOException {
        req.setAttribute("u", u);
        req.setAttribute("editing", Boolean.valueOf(editing));
        req.setAttribute("error", error);
        req.setAttribute("roles", dao.listRoles());
        req.setAttribute("isSelf", Boolean.valueOf(editing && u.getUserId() == me(req).getUserId()));
        req.getRequestDispatcher("/WEB-INF/views/userForm.jsp").forward(req, resp);
    }

    private void create(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        AppUser u = new AppUser();
        u.setUsername(trim(req.getParameter("username")));
        u.setFullName(trim(req.getParameter("fullName")));
        u.setRoleId(intParam(req, "roleId"));
        String pw = req.getParameter("password") == null ? "" : req.getParameter("password");
        String pw2 = req.getParameter("password2") == null ? "" : req.getParameter("password2");

        String error = null;
        if (!USERNAME.matcher(u.getUsername()).matches()) {
            error = "Ten dang nhap 3 den 64 ky tu, chi gom chu, so, dau gach duoi, cham, gach ngang";
        } else if (u.getFullName().isEmpty() || u.getFullName().length() > 150) {
            error = "Ho ten bat buoc, toi da 150 ky tu";
        } else if (!roleExists(u.getRoleId())) {
            error = "Phai chon vai tro";
        } else if ((error = checkPassword(pw, pw2)) != null) {
            // error already set
        } else {
            u.setPassHash(PasswordUtil.hash(pw));
            int id = dao.insert(u);
            if (id == -1) error = "Ten dang nhap da ton tai";
            else if (id == 0) error = "Khong tao duoc tai khoan, thu lai";
        }
        if (error != null) {
            u.setPassHash(null);
            showForm(req, resp, u, false, error);
            return;
        }
        flash(req, "Da tao tai khoan " + u.getUsername());
        resp.sendRedirect(req.getContextPath() + "/admin/users");
    }

    private void update(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        AppUser u = dao.findById(intParam(req, "id"));
        if (u == null) {
            flash(req, "Khong tim thay tai khoan");
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }
        AppUser me = me(req);
        boolean self = u.getUserId() == me.getUserId();

        String fullName = trim(req.getParameter("fullName"));
        int roleId = intParam(req, "roleId");

        String error = null;
        if (fullName.isEmpty() || fullName.length() > 150) {
            error = "Ho ten bat buoc, toi da 150 ky tu";
        } else if (!roleExists(roleId)) {
            error = "Phai chon vai tro";
        } else if (self && roleId != u.getRoleId()) {
            error = "Khong duoc doi vai tro cua chinh minh";
        } else if (!dao.update(u.getUserId(), fullName, roleId)) {
            error = "Khong luu duoc, thu lai";
        }
        if (error != null) {
            u.setFullName(fullName);
            showForm(req, resp, u, true, error);
            return;
        }
        if (self) me.setFullName(fullName);      // the name shown at the top of every page
        flash(req, "Da cap nhat tai khoan " + u.getUsername());
        resp.sendRedirect(req.getContextPath() + "/admin/users");
    }

    private void lock(HttpServletRequest req) {
        AppUser u = dao.findById(intParam(req, "id"));
        if (u == null) {
            flash(req, "Khong tim thay tai khoan");
        } else if (u.getUserId() == me(req).getUserId()) {
            flash(req, "Khong the khoa chinh minh");
        } else {
            boolean toLocked = !u.isLocked();
            boolean ok = dao.setLocked(u.getUserId(), toLocked);
            flash(req, ok ? (toLocked ? "Da khoa tai khoan " : "Da mo khoa tai khoan ") + u.getUsername()
                          : "Khong doi duoc trang thai tai khoan");
        }
    }

    /** Returns true when the password was changed. */
    private boolean reset(HttpServletRequest req, int id) {
        AppUser u = dao.findById(id);
        if (u == null) {
            flash(req, "Khong tim thay tai khoan");
            return true;                       // nothing to go back to, show the list
        }
        String pw = req.getParameter("password") == null ? "" : req.getParameter("password");
        String pw2 = req.getParameter("password2") == null ? "" : req.getParameter("password2");
        String error = checkPassword(pw, pw2);
        if (error != null) {
            flash(req, error);
            return false;
        }
        boolean ok = dao.resetPassword(u.getUserId(), PasswordUtil.hash(pw));
        flash(req, ok ? "Da dat lai mat khau cho " + u.getUsername() : "Khong dat lai duoc mat khau");
        return ok;
    }

    private void delete(HttpServletRequest req) {
        AppUser u = dao.findById(intParam(req, "id"));
        if (u == null) {
            flash(req, "Khong tim thay tai khoan");
        } else if (u.getUserId() == me(req).getUserId()) {
            flash(req, "Khong the xoa chinh minh");
        } else {
            int r = dao.delete(u.getUserId());
            if (r == 1) flash(req, "Da xoa tai khoan " + u.getUsername());
            else if (r == -1) flash(req, "Tai khoan " + u.getUsername()
                    + " da co du lieu lien quan (nhan, canh bao, hieu chuan). Hay khoa tai khoan thay vi xoa.");
            else flash(req, "Khong xoa duoc tai khoan");
        }
    }

    /** Returns an error message, or null when the two passwords match and are long enough. */
    private String checkPassword(String pw, String pw2) {
        if (pw.length() < PASS_MIN || pw.length() > PASS_MAX) {
            return "Mat khau phai tu " + PASS_MIN + " den " + PASS_MAX + " ky tu";
        }
        if (!pw.equals(pw2)) {
            return "Hai o mat khau khong giong nhau";
        }
        return null;
    }

    private boolean roleExists(int roleId) {
        List<AppRole> roles = dao.listRoles();
        for (AppRole r : roles) {
            if (r.getRoleId() == roleId) return true;
        }
        return false;
    }

    private AppUser me(HttpServletRequest req) {
        return (AppUser) req.getSession().getAttribute("user");
    }

    /** "/admin/users" gives "", "/admin/users/edit" gives "edit". */
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