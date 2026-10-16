package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.AppRole;
import model.AppUser;
import util.DBContext;

public class UserDAO {

    private static final String SELECT_BASE =
        "SELECT u.user_id, u.username, u.pass_hash, u.full_name, u.role_id, u.is_locked, "
        + "u.created_at, r.role_code, r.role_name "
        + "FROM AppUser u JOIN AppRole r ON r.role_id = u.role_id ";

    /** Used by the login. Returns null when not found. */
    public AppUser findByUsername(String username) {
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE u.username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AppUser findById(int id) {
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE u.user_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //read
    public List<AppUser> search(String keyword, int page, int size) {
        List<AppUser> out = new ArrayList<AppUser>();
        boolean hasKw = keyword != null && !keyword.trim().isEmpty();
        String sql = SELECT_BASE
            + (hasKw ? "WHERE u.username LIKE ? OR u.full_name LIKE ? " : "")
            + "ORDER BY u.user_id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
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
        String sql = "SELECT COUNT(*) FROM AppUser u "
            + (hasKw ? "WHERE u.username LIKE ? OR u.full_name LIKE ?" : "");
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

    //creat
    public int insert(AppUser u) {
        String sql = "INSERT INTO AppUser(username, pass_hash, full_name, role_id) VALUES (?, ?, ?, ?)";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassHash());
            ps.setString(3, u.getFullName());
            ps.setInt(4, u.getRoleId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601) return -1;
            e.printStackTrace();
        }
        return 0;
    }

    //update
    public boolean update(int id, String fullName, int roleId) {
        String sql = "UPDATE AppUser SET full_name = ?, role_id = ? WHERE user_id = ?";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setInt(2, roleId);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setLocked(int id, boolean locked) {
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement("UPDATE AppUser SET is_locked = ? WHERE user_id = ?")) {
            ps.setBoolean(1, locked);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** newHash must already be hashed with PasswordUtil.hash. */
    public boolean resetPassword(int id, String newHash) {
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement("UPDATE AppUser SET pass_hash = ? WHERE user_id = ?")) {
            ps.setString(1, newHash);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //delete
    public int delete(int id) {
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM AppUser WHERE user_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0 ? 1 : 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 547) return -1;
            e.printStackTrace();
        }
        return 0;
    }

    public List<AppRole> listRoles() {
        List<AppRole> out = new ArrayList<AppRole>();
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT role_id, role_code, role_name FROM AppRole ORDER BY role_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                AppRole r = new AppRole();
                r.setRoleId(rs.getInt("role_id"));
                r.setRoleCode(rs.getString("role_code"));
                r.setRoleName(rs.getString("role_name"));
                out.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    private AppUser map(ResultSet rs) throws SQLException {
        AppUser u = new AppUser();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPassHash(rs.getString("pass_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setRoleId(rs.getInt("role_id"));
        u.setLocked(rs.getBoolean("is_locked"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        u.setRoleCode(rs.getString("role_code"));
        u.setRoleName(rs.getString("role_name"));
        return u;
    }
}