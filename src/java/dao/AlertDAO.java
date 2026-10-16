package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.PotAlert;
import util.DBContext;

public class AlertDAO {

    private static final String SELECT_BASE =
        "SELECT a.alert_id, a.session_id, a.rule_code, a.severity, a.message, a.status, "
        + "a.handled_by, u.full_name AS by_name, a.handled_note, a.created_at, s.pot_no "
        + "FROM Pot_Alert a "
        + "LEFT JOIN AppUser u ON u.user_id = a.handled_by "
        + "LEFT JOIN Pot_Session s ON s.session_id = a.session_id ";

    //Insert
    public boolean insert(int sessionId, String ruleCode, String severity, String message) {
        String sql = "INSERT INTO Pot_Alert(session_id, rule_code, severity, message, status) VALUES (?, ?, ?, ?, 'OPEN')";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setString(2, ruleCode);
            ps.setString(3, severity);
            ps.setString(4, message);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //Creat by admin
    public int createManual(Integer sessionId, String ruleCode, String severity, String message) {
        String sql = "INSERT INTO Pot_Alert(session_id, rule_code, severity, message, status) VALUES (?, ?, ?, ?, 'OPEN')";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (sessionId == null) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, sessionId.intValue());
            ps.setString(2, ruleCode);
            ps.setString(3, severity);
            ps.setString(4, message);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //Read
    public List<PotAlert> search(String status, String severity, int page, int size) {
        List<PotAlert> out = new ArrayList<PotAlert>();
        boolean hasStatus = status != null && !status.isEmpty();
        boolean hasSev = severity != null && !severity.isEmpty();
        String sql = SELECT_BASE + "WHERE 1 = 1 "
            + (hasStatus ? "AND a.status = ? " : "")
            + (hasSev ? "AND a.severity = ? " : "")
            + "ORDER BY CASE WHEN a.status = 'OPEN' THEN 0 ELSE 1 END, a.created_at DESC, a.alert_id DESC "
            + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = 1;
            if (hasStatus) ps.setString(i++, status);
            if (hasSev) ps.setString(i++, severity);
            ps.setInt(i++, (page - 1) * size);
            ps.setInt(i++, size);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public int count(String status, String severity) {
        boolean hasStatus = status != null && !status.isEmpty();
        boolean hasSev = severity != null && !severity.isEmpty();
        String sql = "SELECT COUNT(*) FROM Pot_Alert a WHERE 1 = 1 "
            + (hasStatus ? "AND a.status = ? " : "")
            + (hasSev ? "AND a.severity = ? " : "");
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = 1;
            if (hasStatus) ps.setString(i++, status);
            if (hasSev) ps.setString(i++, severity);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public PotAlert findById(int id) {
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE a.alert_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Update
    public boolean handle(int id, String status, Integer userId, String note) {
        String sql = "UPDATE Pot_Alert SET status = ?, handled_by = ?, handled_note = ? WHERE alert_id = ?";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, status);
            if (userId == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, userId.intValue());
            ps.setString(3, note);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

   // Delete
    public boolean delete(int id) {
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM Pot_Alert WHERE alert_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private PotAlert map(ResultSet rs) throws SQLException {
        PotAlert o = new PotAlert();
        o.setAlertId(rs.getInt("alert_id"));
        int sid = rs.getInt("session_id");
        o.setSessionId(rs.wasNull() ? null : Integer.valueOf(sid));
        o.setRuleCode(rs.getString("rule_code"));
        o.setSeverity(rs.getString("severity"));
        o.setMessage(rs.getString("message"));
        o.setStatus(rs.getString("status"));
        int by = rs.getInt("handled_by");
        o.setHandledBy(rs.wasNull() ? null : Integer.valueOf(by));
        o.setHandledByName(rs.getString("by_name"));
        o.setHandledNote(rs.getString("handled_note"));
        o.setCreatedAt(rs.getTimestamp("created_at"));
        int pot = rs.getInt("pot_no");
        o.setPotNo(rs.wasNull() ? null : Integer.valueOf(pot));
        return o;
    }
}