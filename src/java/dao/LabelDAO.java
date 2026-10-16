package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.PotLabel;
import util.DBContext;

/**
 * Data Access Object for Pot_Label. Inserts both automatic rule labels and
 * reviewer corrections, and reads the label history of one session.
 */
public class LabelDAO {

    /**
     * Appends a label row for a session (rows are never overwritten).
     * Used by IngestServlet (source RULE) and LabelCorrectServlet (source REVIEWER).
     *
     * @return the new label_id, or 0 when the insert failed
     */
    public int insert(int sessionId, String labelCode, String source, String reason, Integer labeledBy) {
        String sql = "INSERT INTO Pot_Label(session_id, label_code, source, reason, labeled_by) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sessionId);
            ps.setString(2, labelCode);
            ps.setString(3, source);
            ps.setString(4, reason);
            if (labeledBy != null) ps.setInt(5, labeledBy.intValue());
            else ps.setNull(5, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** All labels of one session, NEWEST FIRST: the first row is the current label. */
    public List<PotLabel> findBySession(int sessionId) {
        List<PotLabel> out = new ArrayList<PotLabel>();
        String sql = "SELECT l.label_id, l.session_id, l.label_code, l.source, l.reason, "
                   + "l.labeled_by, u.full_name AS by_name, l.labeled_at "
                   + "FROM Pot_Label l LEFT JOIN AppUser u ON u.user_id = l.labeled_by "
                   + "WHERE l.session_id = ? ORDER BY l.label_id DESC";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PotLabel o = new PotLabel();
                    o.setLabelId(rs.getInt("label_id"));
                    o.setSessionId(rs.getInt("session_id"));
                    o.setLabelCode(rs.getString("label_code"));
                    o.setSource(rs.getString("source"));
                    o.setReason(rs.getString("reason"));
                    int by = rs.getInt("labeled_by");
                    o.setLabeledBy(rs.wasNull() ? null : Integer.valueOf(by));
                    o.setLabeledByName(rs.getString("by_name"));
                    o.setLabeledAt(rs.getTimestamp("labeled_at"));
                    out.add(o);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }
}