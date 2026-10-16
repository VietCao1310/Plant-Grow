package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.PotWaterPlan;
import util.DBContext;

public class PotWaterPlanDAO {

    private static final String SELECT_BASE =
        "SELECT water_plan_id, code, name, pot_no, water_target_ml, interval_hours, "
        + "trigger_moist, note, is_active, created_at FROM Pot_WaterPlan ";

    /** READ: one page of rows. keyword is matched against code and name. */
    public List<PotWaterPlan> search(String keyword, int page, int size) {
        List<PotWaterPlan> out = new ArrayList<PotWaterPlan>();
        boolean hasKw = keyword != null && !keyword.trim().isEmpty();
        String sql = SELECT_BASE
            + (hasKw ? "WHERE code LIKE ? OR name LIKE ? " : "")
            + "ORDER BY pot_no, water_plan_id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
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
        String sql = "SELECT COUNT(*) FROM Pot_WaterPlan "
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

    public PotWaterPlan findById(int id) {
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE water_plan_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** CREATE */
    public int insert(PotWaterPlan o) {
        String sql = "INSERT INTO Pot_WaterPlan(code, name, pot_no, water_target_ml, interval_hours, "
                   + "trigger_moist, note, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fill(ps, o);
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

    /** UPDATE */
    public int update(PotWaterPlan o) {
        String sql = "UPDATE Pot_WaterPlan SET code = ?, name = ?, pot_no = ?, water_target_ml = ?, "
                   + "interval_hours = ?, trigger_moist = ?, note = ?, is_active = ? "
                   + "WHERE water_plan_id = ?";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            fill(ps, o);
            ps.setInt(9, o.getWaterPlanId());
            return ps.executeUpdate() > 0 ? 1 : 0;
        } catch (SQLException e) {
            if (isDuplicate(e)) return -1;
            e.printStackTrace();
        }
        return 0;
    }

    /** DELETE */
    public boolean delete(int id) {
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM Pot_WaterPlan WHERE water_plan_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** The "automatic watering on/off" button: flips is_active in one statement. */
    public boolean toggleActive(int id) {
        String sql = "UPDATE Pot_WaterPlan SET is_active = CASE WHEN is_active = 1 THEN 0 ELSE 1 END "
                   + "WHERE water_plan_id = ?";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------- helpers ----------

    /** Binds the 8 editable columns, in the same order for INSERT and UPDATE. */
    private void fill(PreparedStatement ps, PotWaterPlan o) throws SQLException {
        ps.setString(1, o.getCode());
        ps.setString(2, o.getName());
        ps.setInt(3, o.getPotNo());
        ps.setDouble(4, o.getWaterTargetMl());
        ps.setInt(5, o.getIntervalHours());
        ps.setInt(6, o.getTriggerMoist());
        ps.setString(7, o.getNote());
        ps.setBoolean(8, o.isActive());
    }

    /** SQL Server error 2627 / 2601 = unique key or index violated. */
    private boolean isDuplicate(SQLException e) {
        return e.getErrorCode() == 2627 || e.getErrorCode() == 2601;
    }

    private PotWaterPlan map(ResultSet rs) throws SQLException {
        PotWaterPlan o = new PotWaterPlan();
        o.setWaterPlanId(rs.getInt("water_plan_id"));
        o.setCode(rs.getString("code"));
        o.setName(rs.getString("name"));
        o.setPotNo(rs.getInt("pot_no"));
        o.setWaterTargetMl(rs.getDouble("water_target_ml"));
        o.setIntervalHours(rs.getInt("interval_hours"));
        o.setTriggerMoist(rs.getInt("trigger_moist"));
        o.setNote(rs.getString("note"));
        o.setActive(rs.getBoolean("is_active"));
        o.setCreatedAt(rs.getTimestamp("created_at"));
        return o;
    }
}