package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dao.ExportDAO;
import util.Csv;

/**
 * CSV export (MVC2: this class writes a file, never HTML).
 *   GET /export            page with the choices and the number of rows
 *   GET /export/sessions   downloads the CSV, optional ?scope=all|real|sample and ?label=OK|...
 * Read only, so AuthFilter lets every role in.
 */
@WebServlet(name = "ExportServlet", urlPatterns = {"/export", "/export/sessions"})
public class ExportServlet extends HttpServlet {

    private static final List<String> LABELS =
            Arrays.asList("OK", "OVERWATER", "NO_UPTAKE", "DRY_STRESS", "LEAK");
    private static final List<String> SCOPES = Arrays.asList("all", "real", "sample");

    private static final String[] HEADER = {
        "session_id", "device_code", "device_seq", "pot_no", "measured_at", "ingested_at",
        "weight_before", "weight_after", "water_ml", "retain_ratio", "moist_before", "moist_after",
        "light_lux", "dry_rate", "source", "label_code", "label_source", "label_reason",
        "label_by", "label_at", "rule_label"
    };

    private final ExportDAO dao = new ExportDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("/export/sessions".equals(req.getServletPath())) {
            download(req, resp);
            return;
        }
        req.setAttribute("countAll", Integer.valueOf(dao.count("", "all")));
        req.setAttribute("countReal", Integer.valueOf(dao.count("", "real")));
        req.setAttribute("countSample", Integer.valueOf(dao.count("", "sample")));
        req.getRequestDispatcher("/WEB-INF/views/export.jsp").forward(req, resp);
    }

    private void download(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String label = req.getParameter("label");
        if (label == null || !LABELS.contains(label.trim())) label = "";
        String scope = req.getParameter("scope");
        if (scope == null || !SCOPES.contains(scope.trim())) scope = "all";
        label = label.trim();
        scope = scope.trim();

        String name = "sessions_" + scope + (label.isEmpty() ? "" : "_" + label) + "_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv";
        resp.setContentType("text/csv");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");

        final PrintWriter out = resp.getWriter();
        final SimpleDateFormat ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        out.write('\uFEFF');                                  // BOM, so Excel reads UTF-8 correctly
        out.write(String.join(",", HEADER) + "\r\n");
        try {
            dao.eachSession(label, scope, rs -> out.write(line(rs, ts)));
        } catch (SQLException e) {
            e.printStackTrace();
            if (!resp.isCommitted()) {
                resp.reset();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Khong xuat duoc du lieu");
                return;
            }
        }
        out.flush();
    }

    /** One CSV line, in the same order as HEADER. */
    private String line(ResultSet rs, SimpleDateFormat ts) throws SQLException {
        boolean sample = rs.getBoolean("is_sample");
        int seq = rs.getInt("device_seq");
        String source = sample ? "sample" : (seq < 0 ? "manual" : "device");

        String[] c = {
            Csv.cell(rs.getString("session_id")),
            Csv.cell(rs.getString("device_code")),
            Csv.cell(rs.getString("device_seq")),
            Csv.cell(rs.getString("pot_no")),
            Csv.cell(time(rs.getTimestamp("measured_at"), ts)),
            Csv.cell(time(rs.getTimestamp("ingested_at"), ts)),
            Csv.cell(rs.getString("weight_before")),
            Csv.cell(rs.getString("weight_after")),
            Csv.cell(rs.getString("water_ml")),
            Csv.cell(rs.getString("retain_ratio")),
            Csv.cell(rs.getString("moist_before")),
            Csv.cell(rs.getString("moist_after")),
            Csv.cell(rs.getString("light_lux")),
            Csv.cell(rs.getString("dry_rate")),
            source,
            Csv.cell(rs.getString("label_code")),
            Csv.cell(rs.getString("label_source")),
            Csv.text(rs.getString("label_reason")),
            Csv.text(rs.getString("label_by")),
            Csv.cell(time(rs.getTimestamp("labeled_at"), ts)),
            Csv.cell(rs.getString("rule_label"))
        };
        return String.join(",", c) + "\r\n";
    }

    private String time(Timestamp t, SimpleDateFormat ts) {
        return t == null ? "" : ts.format(t);
    }
}