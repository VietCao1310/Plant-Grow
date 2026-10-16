package filter;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import model.AppUser;

/**
 * One filter guards every path. Rules are checked top to bottom and the FIRST
 * match wins, so a narrow rule (/session/delete) must stay above a wide one
 * (/sessions). A path that matches no rule is refused (default deny).
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    private static final String ADMIN = "ADMIN";
    private static final String GROWER = "GROWER";
    private static final String TECH = "TECHNICIAN";
    private static final String ANALYST = "ANALYST";
    private static final String VIEWER = "VIEWER";

    private static final String[] ALL = {ADMIN, GROWER, TECH, ANALYST, VIEWER};
    private static final String[] STAFF = {ADMIN, GROWER, TECH};   // may change master data

    /** Reachable without logging in. The device endpoint checks its own API key. */
    private static final String[] PUBLIC = {"/login", "/logout", "/api/", "/css/", "/js/", "/images/"};

    private static final Map<String, Set<String>> RULES = new LinkedHashMap<String, Set<String>>();

    private static void rule(String prefix, String... roles) {
        RULES.put(prefix, new HashSet<String>(Arrays.asList(roles)));
    }

    static {
        // ---- Pot_Session : C ADMIN | R all | U ADMIN GROWER TECH | D ADMIN
        rule("/session/create", ADMIN);
        rule("/session/edit",   ADMIN, GROWER, TECH);
        rule("/session/update", ADMIN, GROWER, TECH);
        rule("/session/delete", ADMIN);
        rule("/session/detail", ALL);
        rule("/sessions",       ALL);

        // ---- Pot_Label : C and U for ADMIN and ANALYST only, others read
        rule("/label/correct",  ADMIN, ANALYST);

        // ---- Pot_Alert : C D ADMIN | U all but VIEWER | R all
        rule("/alerts/create",  ADMIN);
        rule("/alerts/delete",  ADMIN);
        rule("/alerts/update",  ADMIN, GROWER, TECH, ANALYST);
        rule("/alerts",         ALL);

        // ---- Plant, WaterPlan, PumpCalib : C R U D for ADMIN GROWER TECH, others read
        for (String res : new String[]{"/plants", "/plans", "/calibs"}) {
            for (String act : new String[]{"create", "edit", "update", "delete", "move", "toggle"}) {
                rule(res + "/" + act, STAFF);
            }
            rule(res, ALL);
        }

        // ---- Users and device administration : ADMIN only
        rule("/admin", ADMIN);

        // ---- Read-only screens
        rule("/dashboard", ALL);
        rule("/chart",     ALL);
        rule("/export",    ALL);
        rule("/report",    ALL);
    }

    private static boolean matches(String path, String prefix) {
        if (prefix.endsWith("/")) return path.startsWith(prefix);
        return path.equals(prefix) || path.startsWith(prefix + "/");
    }

    @Override
    public void doFilter(ServletRequest rq, ServletResponse rp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) rq;
        HttpServletResponse resp = (HttpServletResponse) rp;

        // servletPath + pathInfo is already decoded and cleaned by the container
        String path = req.getServletPath();
        if (req.getPathInfo() != null) path += req.getPathInfo();

        for (String p : PUBLIC) {
            if (matches(path, p)) {
                chain.doFilter(rq, rp);
                return;
            }
        }

        AppUser me = null;
        HttpSession s = req.getSession(false);
        if (s != null) me = (AppUser) s.getAttribute("user");
        if (me == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // the home page only redirects to /sessions, so a login is enough
        if (path.equals("/") || path.equals("/index.jsp")) {
            chain.doFilter(rq, rp);
            return;
        }

        for (Map.Entry<String, Set<String>> e : RULES.entrySet()) {
            if (matches(path, e.getKey())) {
                if (e.getValue().contains(me.getRoleCode())) {
                    chain.doFilter(rq, rp);
                } else {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                            "Vai tro " + me.getRoleCode() + " khong duoc vao trang nay");
                }
                return;
            }
        }

        // no rule matched: refuse
        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Duong dan khong duoc cap quyen");
    }

    @Override public void init(FilterConfig c) { }
    @Override public void destroy() { }
}