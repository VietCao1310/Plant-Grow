package controller;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dao.LabelDAO;
import dao.PotSessionDAO;
import model.AppUser;
import model.PotLabel;
import model.PotSession;

/**
 * One session in detail: the readings, the label history and a retain_ratio chart of the same pot.
 *   GET /session/detail?id=
 * AuthFilter lets every role in. Only ADMIN and ANALYST get the correction form,
 * and /label/correct is guarded again by the filter, so hiding the form is not the only protection.
 */
@WebServlet(name = "SessionDetailServlet", urlPatterns = {"/session/detail"})
public class SessionDetailServlet extends HttpServlet {

    private static final int CHART_POINTS = 30;

    private final PotSessionDAO sessions = new PotSessionDAO();
    private final LabelDAO labels = new LabelDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int id = -1;
        try {
            id = Integer.parseInt(req.getParameter("id").trim());
        } catch (Exception ignored) { }

        PotSession s = id < 1 ? null : sessions.findById(id);
        if (s == null) {
            req.getSession().setAttribute("flash", "Khong tim thay phien");
            resp.sendRedirect(req.getContextPath() + "/sessions");
            return;
        }

        List<PotLabel> history = labels.findBySession(id);
        List<Double> series = sessions.retainSeries(s.getPotNo(), s.getMeasuredAt(), CHART_POINTS);

        // plain numbers separated by commas, safe to print straight into a JavaScript array
        StringBuilder js = new StringBuilder("[");
        for (int i = 0; i < series.size(); i++) {
            if (i > 0) js.append(',');
            js.append(series.get(i).doubleValue());
        }
        js.append(']');

        AppUser me = (AppUser) req.getSession().getAttribute("user");
        boolean canCorrect = me != null
                && ("ADMIN".equals(me.getRoleCode()) || "ANALYST".equals(me.getRoleCode()));

        req.setAttribute("s", s);
        req.setAttribute("history", history);
        req.setAttribute("seriesJs", js.toString());
        req.setAttribute("seriesSize", Integer.valueOf(series.size()));
        req.setAttribute("chartPoints", Integer.valueOf(CHART_POINTS));
        req.setAttribute("canCorrect", Boolean.valueOf(canCorrect));
        req.getRequestDispatcher("/WEB-INF/views/sessionDetail.jsp").forward(req, resp);
    }
}