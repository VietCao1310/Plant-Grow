package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dao.AlertDAO;
import dao.PotSessionDAO;
import model.PotSession;
import service.PotRuleEngine;

/*
  The home page after login (MVC2: no HTML here). Read only, so every role may open it.
  It reads the newest session of each pot, the light of the newest one, and the open alerts.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    private static final int ALERTS_SHOWN = 8;

    private final PotSessionDAO sessions = new PotSessionDAO();
    private final AlertDAO alerts = new AlertDAO();
    private final PotRuleEngine engine = new PotRuleEngine();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        PotSession p1 = sessions.findLatestByPot(1);
        PotSession p2 = sessions.findLatestByPot(2);

        // the light sensor is shared by the whole tray: take it from whichever session is newer
        PotSession newest = p1;
        if (p2 != null && (newest == null || p2.getMeasuredAt().after(newest.getMeasuredAt()))) {
            newest = p2;
        }

        req.setAttribute("pot1", p1);
        req.setAttribute("pot2", p2);
        // the colour of a card comes from the same severity table the alerts use
        req.setAttribute("sev1", p1 == null ? "NONE" : engine.severityOf(p1.getLabelCode()));
        req.setAttribute("sev2", p2 == null ? "NONE" : engine.severityOf(p2.getLabelCode()));
        req.setAttribute("light", newest == null ? null : Integer.valueOf(newest.getLightLux()));
        req.setAttribute("alerts", alerts.search("OPEN", "", 1, ALERTS_SHOWN));
        req.setAttribute("openCount", Integer.valueOf(alerts.count("OPEN", "")));
        req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
    }
}