package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.PotPumpCalib;
import util.DBContext;

public class PotPumpCalibDAO {

    /** The person's name comes from AppUser, so a LEFT JOIN keeps rows whose user is unknown. */
    private static final String SELECT_BASE =
        "SELECT c.pump_calib_id, c.code, c.name, c.pot_no, c.ml_per_second, c.calibrated_by, "
        + "u.full_name AS by_name, c.calibrated_at, c.note, c.is_active, c.created_at "
        + "FROM Pot_PumpCalib c LEFT JOIN AppUser u ON u.user_id = c.calibrated_by ";

    /** READ: one page of the calibration history, newest first. */
    public List<PotPumpCalib> search(String keyword, int page, int size) {
        List<PotPumpCalib> out = new ArrayList<PotPumpCalib>();
        boolean hasKw = keyword != null && !keyword.trim().isEmpty();
        String sql = SELECT_BASE
            + (hasKw ? "WHERE c.code LIKE ? OR c.name LIKE ? " : "")
            + "ORDER BY c.calibrated_at DESC, c.pump_calib_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = 1;
            if (hasKw) {
                String k = "%" + keyword.trim() + "%";
                ps.setString(i++, k);
                ps.setString(i++, k);
            }
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

    public int count(String keyword) {
        boolean hasKw = keyword != null && !keyword.trim().isEmpty();
        String sql = "SELECT COUNT(*) FROM Pot_PumpCalib "
            + (hasKw ? "WHERE code LIKE ? OR name LIKE ?" : "");
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            if (hasKw) {
                String k = "%" + keyword.trim() + "%";
                ps.setString(1, k);
                ps.setString(2, k);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public PotPumpCalib findById(int id) {
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE c.pump_calib_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** CREATE. Returns the new key, -1 when the code already exists, 0 on any other failure. */
    public int insert(PotPumpCalib o) {
        String sql = "INSERT INTO Pot_PumpCalib(code, name, pot_no, ml_per_second, calibrated_by, note, is_active) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, o.getCode());
            ps.setString(2, o.getName());
            ps.setInt(3, o.getPotNo());
            ps.setDouble(4, o.getMlPerSecond());
            if (o.getCalibratedBy() == null) ps.setNull(5, Types.INTEGER);
            else ps.setInt(5, o.getCalibratedBy().intValue());
            ps.setString(6, o.getNote());
            ps.setBoolean(7, o.isActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            if (isDuplicate(e)) return -1;
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * UPDATE, meant for fixing a typing mistake. It does not touch calibrated_by and
     * calibrated_at: a new measurement should be a new row. Returns -1 on a duplicate code.
     */
    public int update(PotPumpCalib o) {
        String sql = "UPDATE Pot_PumpCalib SET code = ?, name = ?, pot_no = ?, ml_per_second = ?, "
                   + "note = ?, is_active = ? WHERE pump_calib_id = ?";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, o.getCode());
            ps.setString(2, o.getName());
            ps.setInt(3, o.getPotNo());
            ps.setDouble(4, o.getMlPerSecond());
            ps.setString(5, o.getNote());
            ps.setBoolean(6, o.isActive());
            ps.setInt(7, o.getPumpCalibId());
            return ps.executeUpdate() > 0 ? 1 : 0;
        } catch (SQLException e) {
            if (isDuplicate(e)) return -1;
            e.printStackTrace();
        }
        return 0;
    }

    /** DELETE. No other table points at Pot_PumpCalib, so a plain delete is enough. */
    public boolean delete(int id) {
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM Pot_PumpCalib WHERE pump_calib_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------- helpers ----------

    /** SQL Server error 2627 / 2601 = unique key or index violated. */
    private boolean isDuplicate(SQLException e) {
        return e.getErrorCode() == 2627 || e.getErrorCode() == 2601;
    }

    private PotPumpCalib map(ResultSet rs) throws SQLException {
        PotPumpCalib o = new PotPumpCalib();
        o.setPumpCalibId(rs.getInt("pump_calib_id"));
        o.setCode(rs.getString("code"));
        o.setName(rs.getString("name"));
        o.setPotNo(rs.getInt("pot_no"));
        o.setMlPerSecond(rs.getDouble("ml_per_second"));
        int by = rs.getInt("calibrated_by");
        o.setCalibratedBy(rs.wasNull() ? null : Integer.valueOf(by));
        o.setCalibratedByName(rs.getString("by_name"));
        o.setCalibratedAt(rs.getTimestamp("calibrated_at"));
        o.setNote(rs.getString("note"));
        o.setActive(rs.getBoolean("is_active"));
        o.setCreatedAt(rs.getTimestamp("created_at"));
        return o;
    }
}