package dao;

import java.io.IOException;
import java.sql.*;
import util.DBContext;

/*
 Reads sessions for the CSV export. The rows are handed over one at a time to a handler,
 so a big table never has to sit in memory.
 */
public class ExportDAO {

    //Called once per row while the ResultSet is open.
    public interface RowHandler {
        void accept(ResultSet rs) throws SQLException, IOException;
    }

    private static final String FROM =
        "FROM Pot_Session s "
        + "JOIN Device d ON d.device_id = s.device_id "
        + "OUTER APPLY (SELECT TOP 1 l.label_code, l.source, l.reason, l.labeled_by, l.labeled_at "
        + "             FROM Pot_Label l WHERE l.session_id = s.session_id "
        + "             ORDER BY l.label_id DESC) cur "
        + "LEFT JOIN AppUser cu ON cu.user_id = cur.labeled_by ";

    private static final String SELECT =
        "SELECT s.session_id, d.device_code, s.device_seq, s.pot_no, s.measured_at, s.ingested_at, "
        + "s.weight_before, s.weight_after, s.water_ml, s.retain_ratio, s.moist_before, s.moist_after, "
        + "s.light_lux, s.dry_rate, s.is_sample, "
        + "cur.label_code, cur.source AS label_source, cur.reason AS label_reason, "
        + "cu.full_name AS label_by, cur.labeled_at, "
        + "(SELECT TOP 1 r.label_code FROM Pot_Label r "
        + " WHERE r.session_id = s.session_id AND r.source = 'RULE' ORDER BY r.label_id DESC) AS rule_label ";

    /*
     scope: "all", "real" (is_sample = 0) or "sample" (is_sample = 1).
     label: empty for every label, or one label code.
     */
    private String where(String label, String scope) {
        StringBuilder sb = new StringBuilder("WHERE 1 = 1 ");
        if ("real".equals(scope)) sb.append("AND s.is_sample = 0 ");
        else if ("sample".equals(scope)) sb.append("AND s.is_sample = 1 ");
        if (label != null && !label.isEmpty()) sb.append("AND cur.label_code = ? ");
        return sb.toString();
    }

    public void eachSession(String label, String scope, RowHandler handler)
            throws SQLException, IOException {
        String sql = SELECT + FROM + where(label, scope) + "ORDER BY s.measured_at, s.session_id";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            if (label != null && !label.isEmpty()) ps.setString(1, label);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) handler.accept(rs);
            }
        }
    }

    // How many rows the same filters would export
    public int count(String label, String scope) {
        String sql = "SELECT COUNT(*) " + FROM + where(label, scope);
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            if (label != null && !label.isEmpty()) ps.setString(1, label);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}