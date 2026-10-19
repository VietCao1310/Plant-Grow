package controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dao.PotSessionDAO;
import model.PotSession;

/**
 * The chart of one pot (MVC2): weight, soil moisture and light on one time axis, and a vertical
 * mark for every watering. Read only, so AuthFilter lets every role in.
 *   GET /chart?pot=1|2&range=6|24|72|168|720|0   (range is in hours, 0 = everything)
 *
 * Pot_Session only stores watering sessions, so every session gives two points: the reading before
 * watering (placed 10 minutes before measured_at: the pump runs, then the pot soaks and drains for
 * 10 minutes) and the reading after watering (at measured_at, taken after the soak).
 */
@WebServlet(name = "ChartServlet", urlPatterns = {"/chart"})
public class ChartServlet extends HttpServlet {

    private static final int MAX_SESSIONS = 600;
    private static final long SOAK_MS = 10L * 60L * 1000L;
    private static final List<Integer> RANGES = Arrays.asList(6, 24, 72, 168, 720, 0);
    private static final int DEFAULT_HOURS = 0;

    private final PotSessionDAO dao = new PotSessionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int pot = intParam(req, "pot");
        if (pot != 1 && pot != 2) pot = 1;
        int hours = intParam(req, "range");
        if (!RANGES.contains(Integer.valueOf(hours))) hours = DEFAULT_HOURS;

        Timestamp from = hours == 0 ? null
                : new Timestamp(System.currentTimeMillis() - hours * 3600L * 1000L);

        // ask for one more than we show, to know whether the list was cut
        List<PotSession> list = dao.forChart(pot, from, MAX_SESSIONS + 1);
        boolean truncated = list.size() > MAX_SESSIONS;
        if (truncated) list.remove(0);                      // the list is oldest first: drop the oldest

        List<double[]> weight = new ArrayList<double[]>();
        List<double[]> moist = new ArrayList<double[]>();
        List<double[]> light = new ArrayList<double[]>();
        List<Long> marks = new ArrayList<Long>();
        for (PotSession s : list) {
            long t = s.getMeasuredAt().getTime();
            long t0 = t - SOAK_MS;
            weight.add(new double[]{t0, s.getWeightBefore()});
            weight.add(new double[]{t, s.getWeightAfter()});
            moist.add(new double[]{t0, s.getMoistBefore()});
            moist.add(new double[]{t, s.getMoistAfter()});
            light.add(new double[]{t, s.getLightLux()});
            marks.add(Long.valueOf(t0));                    // the pump starts here
        }
        Comparator<double[]> byTime = new Comparator<double[]>() {
            @Override
            public int compare(double[] a, double[] b) {
                return Double.compare(a[0], b[0]);
            }
        };
        Collections.sort(weight, byTime);                   // sessions closer than 10 minutes overlap
        Collections.sort(moist, byTime);
        Collections.sort(light, byTime);

        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        req.setAttribute("pot", Integer.valueOf(pot));
        req.setAttribute("hours", Integer.valueOf(hours));
        req.setAttribute("count", Integer.valueOf(list.size()));
        req.setAttribute("truncated", Boolean.valueOf(truncated));
        req.setAttribute("maxSessions", Integer.valueOf(MAX_SESSIONS));
        req.setAttribute("firstText", list.isEmpty() ? null : fmt.format(list.get(0).getMeasuredAt()));
        req.setAttribute("lastText", list.isEmpty() ? null : fmt.format(list.get(list.size() - 1).getMeasuredAt()));
        req.setAttribute("weightJs", toJs(weight));
        req.setAttribute("moistJs", toJs(moist));
        req.setAttribute("lightJs", toJs(light));
        req.setAttribute("marksJs", marksJs(marks));
        req.getRequestDispatcher("/WEB-INF/views/chart.jsp").forward(req, resp);
    }

    /** [[time,value],[time,value],...] with plain numbers only, safe to print into JavaScript. */
    private String toJs(List<double[]> pts) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < pts.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append('[').append((long) pts.get(i)[0]).append(',').append(pts.get(i)[1]).append(']');
        }
        return sb.append(']').toString();
    }

    private String marksJs(List<Long> marks) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < marks.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(marks.get(i).longValue());
        }
        return sb.append(']').toString();
    }

    private int intParam(HttpServletRequest req, String name) {
        try {
            return Integer.parseInt(req.getParameter(name).trim());
        } catch (Exception e) {
            return -1;
        }
    }
}