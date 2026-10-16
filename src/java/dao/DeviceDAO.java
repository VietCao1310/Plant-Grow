package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import util.DBContext;

public class DeviceDAO {

    /**
     * Finds device ID by its code and API key.
     */
    public Integer findIdByCodeAndKey(String code, String apiKey) {
        String sql = "SELECT device_id FROM Device WHERE device_code = ? AND api_key = ? AND is_active = 1";
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            cn = DBContext.getConnection();
            ps = cn.prepareStatement(sql);
            ps.setString(1, code);
            ps.setString(2, apiKey);
            rs = ps.executeQuery();
            if (rs.next()) {
                return Integer.valueOf(rs.getInt("device_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs, ps, cn);
        }
        return null;
    }

    /**
     * Logs rejected packets from the board.
     */
    public void logRejected(String rawBody, String deviceCode, String reason) {
        String sql = "INSERT INTO RejectedPacket(raw_body, device_code, reason) VALUES (?, ?, ?)";
        Connection cn = null;
        PreparedStatement ps = null;
        try {
            cn = DBContext.getConnection();
            ps = cn.prepareStatement(sql);
            ps.setString(1, rawBody);
            ps.setString(2, deviceCode);
            ps.setString(3, reason);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(null, ps, cn);
        }
    }

    /**
     * Updates the last_seen timestamp of a device.
     */
    public boolean touch(int deviceId) {
        String sql = "UPDATE Device SET last_seen = SYSDATETIME() WHERE device_id = ?";
        Connection cn = null;
        PreparedStatement ps = null;
        try {
            cn = DBContext.getConnection();
            ps = cn.prepareStatement(sql);
            ps.setInt(1, deviceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(null, ps, cn);
        }
        return false;
    }

    // The first active device, used as the owner of a session typed in by hand. Null if none. */
    public Integer findFirstActiveId() {
        String sql = "SELECT TOP 1 device_id FROM Device WHERE is_active = 1 ORDER BY device_id";
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            cn = DBContext.getConnection();
            ps = cn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return Integer.valueOf(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs, ps, cn);
        }
        return null;
    }
    
    
    private void close(ResultSet rs, PreparedStatement ps, Connection cn) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ignored) {
        }
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (SQLException ignored) {
        }
        try {
            if (cn != null) {
                cn.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
