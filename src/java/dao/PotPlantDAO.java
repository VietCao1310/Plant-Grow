package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.PotPlant;
import util.DBContext;

public class PotPlantDAO {

    private static final String SELECT_BASE =
        "SELECT plant_id, code, name, species, pot_no, planted_at, note, is_active, created_at "
        + "FROM Pot_Plant ";

    /** READ: one page of rows. keyword is matched against code, name and species. */
    public List<PotPlant> search(String keyword, int page, int size) {
        List<PotPlant> out = new ArrayList<PotPlant>();
        boolean hasKw = keyword != null && !keyword.trim().isEmpty();
        String sql = SELECT_BASE
            + (hasKw ? "WHERE code LIKE ? OR name LIKE ? OR species LIKE ? " : "")
            + "ORDER BY plant_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = 1;
            if (hasKw) {
                String k = "%" + keyword.trim() + "%";
                ps.setString(i++, k);
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
        String sql = "SELECT COUNT(*) FROM Pot_Plant "
            + (hasKw ? "WHERE code LIKE ? OR name LIKE ? OR species LIKE ?" : "");
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            if (hasKw) {
                String k = "%" + keyword.trim() + "%";
                ps.setString(1, k);
                ps.setString(2, k);
                ps.setString(3, k);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public PotPlant findById(int id) {
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE plant_id = ?")) {
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
    public int insert(PotPlant o) {
        String sql = "INSERT INTO Pot_Plant(code, name, species, pot_no, planted_at, note, is_active) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
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

    /** UPDATE. Returns -1 on a duplicate code, 1 when a row changed, 0 otherwise. */
    public int update(PotPlant o) {
        String sql = "UPDATE Pot_Plant SET code = ?, name = ?, species = ?, pot_no = ?, "
                   + "planted_at = ?, note = ?, is_active = ? WHERE plant_id = ?";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            fill(ps, o);
            ps.setInt(8, o.getPlantId());
            return ps.executeUpdate() > 0 ? 1 : 0;
        } catch (SQLException e) {
            if (isDuplicate(e)) return -1;
            e.printStackTrace();
        }
        return 0;
    }

    /** DELETE. No other table points at Pot_Plant, so a plain delete is enough. */
    public boolean delete(int id) {
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM Pot_Plant WHERE plant_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** The "move plant to another pot" button. potNo must be 1 or 2 (the table has a CHECK). */
    public boolean movePot(int id, int potNo) {
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement("UPDATE Pot_Plant SET pot_no = ? WHERE plant_id = ?")) {
            ps.setInt(1, potNo);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------- helpers ----------

    /** Binds the 7 editable columns, in the same order for INSERT and UPDATE. */
    private void fill(PreparedStatement ps, PotPlant o) throws SQLException {
        ps.setString(1, o.getCode());
        ps.setString(2, o.getName());
        ps.setString(3, o.getSpecies());
        if (o.getPotNo() == null) ps.setNull(4, Types.INTEGER);
        else ps.setInt(4, o.getPotNo().intValue());
        ps.setTimestamp(5, o.getPlantedAt());       // null is fine, the column allows it
        ps.setString(6, o.getNote());
        ps.setBoolean(7, o.isActive());
    }

    /** SQL Server error 2627 / 2601 = unique key or index violated. */
    private boolean isDuplicate(SQLException e) {
        return e.getErrorCode() == 2627 || e.getErrorCode() == 2601;
    }

    private PotPlant map(ResultSet rs) throws SQLException {
        PotPlant o = new PotPlant();
        o.setPlantId(rs.getInt("plant_id"));
        o.setCode(rs.getString("code"));
        o.setName(rs.getString("name"));
        o.setSpecies(rs.getString("species"));
        int pot = rs.getInt("pot_no");
        o.setPotNo(rs.wasNull() ? null : Integer.valueOf(pot));
        o.setPlantedAt(rs.getTimestamp("planted_at"));
        o.setNote(rs.getString("note"));
        o.setActive(rs.getBoolean("is_active"));
        o.setCreatedAt(rs.getTimestamp("created_at"));
        return o;
    }
}