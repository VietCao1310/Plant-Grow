package controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dao.LabelDAO;
import dao.PotSessionDAO;
import model.AppUser;

/**
 * Lets a reviewer correct a label. The old label row is kept, a new one is
 * appended with source REVIEWER, so the history of every correction survives
 * and the report can count how often the rule engine was wrong.
 * AuthFilter only lets ADMIN and ANALYST reach this path.
 */
@WebServlet("/label/correct")
public class LabelCorrectServlet extends HttpServlet {

    private static final List<String> LABELS =
            Arrays.asList("OK", "OVERWATER", "NO_UPTAKE", "DRY_STRESS", "LEAK");

    private final LabelDAO labels = new LabelDAO();
    private final PotSessionDAO sessions = new PotSessionDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String ctx = req.getContextPath();
        AppUser me = (AppUser) req.getSession().getAttribute("user");

        int sessionId = -1;
        try {
            sessionId = Integer.parseInt(req.getParameter("sessionId").trim());
        } catch (Exception ignored) { }
        if (sessionId < 1 || sessions.findById(sessionId) == null) {
            flash(req, "Khong tim thay phien");
            resp.sendRedirect(ctx + "/sessions");
            return;
        }
        String back = ctx + "/session/detail?id=" + sessionId;

        String newLabel = req.getParameter("labelCode") == null ? "" : req.getParameter("labelCode").trim();
        String reason = req.getParameter("reason") == null ? "" : req.getParameter("reason").trim();

        if (!LABELS.contains(newLabel)) {
            flash(req, "Nhan khong hop le");
        } else if (reason.length() < 5) {
            flash(req, "Phai ghi ly do sua nhan (it nhat 5 ky tu)");
        } else if (reason.length() > 400) {
            flash(req, "Ly do toi da 400 ky tu");
        } else if (labels.insert(sessionId, newLabel, "REVIEWER", reason,
                Integer.valueOf(me.getUserId())) == 0) {
            flash(req, "Khong luu duoc nhan, thu lai");
        } else {
            flash(req, "Da sua nhan thanh " + newLabel);
        }
        resp.sendRedirect(back);
    }

    private void flash(HttpServletRequest req, String msg) {
        req.getSession().setAttribute("flash", msg);   // nav.jsp shows it once
    }
}