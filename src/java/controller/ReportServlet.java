package controller;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dao.ReportDAO;

@WebServlet(name = "ReportServlet", urlPatterns = {"/report"})
public class ReportServlet extends HttpServlet {

    private final ReportDAO dao = new ReportDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int pot = 0;
        try {
            pot = Integer.parseInt(req.getParameter("pot"));
        } catch (Exception ignored) { }
        if (pot != 1 && pot != 2) pot = 0;

        List<ReportDAO.WaterSummary> waterList = dao.getWaterSummaries();
        List<ReportDAO.LabelStat> labelStats = dao.getLabelStats();
        ReportDAO.CorrectionStat correctionStat = dao.getCorrectionStat();
        List<ReportDAO.AlertStat> alertStats = dao.getAlertStats();
        ReportDAO.ScatterData scatter = dao.getScatterData(pot);

        StringBuilder xsJs = new StringBuilder("[");
        for (int i = 0; i < scatter.xs.size(); i++) {
            if (i > 0) xsJs.append(',');
            xsJs.append(scatter.xs.get(i));
        }
        xsJs.append(']');

        StringBuilder ysJs = new StringBuilder("[");
        for (int i = 0; i < scatter.ys.size(); i++) {
            if (i > 0) ysJs.append(',');
            ysJs.append(scatter.ys.get(i));
        }
        ysJs.append(']');

        req.setAttribute("pot", Integer.valueOf(pot));
        req.setAttribute("waterList", waterList);
        req.setAttribute("labelStats", labelStats);
        req.setAttribute("correctionStat", correctionStat);
        req.setAttribute("alertStats", alertStats);
        req.setAttribute("xsJs", xsJs.toString());
        req.setAttribute("ysJs", ysJs.toString());
        req.setAttribute("scatterCount", Integer.valueOf(scatter.xs.size()));

        req.getRequestDispatcher("/WEB-INF/views/report.jsp").forward(req, resp);
    }
}
