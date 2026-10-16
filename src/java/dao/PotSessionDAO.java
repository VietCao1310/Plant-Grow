package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.PotSession;
import util.DBContext;

public class PotSessionDAO {

    private static final String SELECT_BASE
            = "SELECT s.session_id, s.device_id, s.device_seq, s.measured_at, s.ingested_at, "
            + "s.is_sample, s.pot_no, s.weight_before, s.weight_after, s.water_ml, s.retain_ratio, s.moist_before, "
            + "s.moist_after, s.light_lux, s.dry_rate, "
            + "(SELECT TOP 1 l.label_code FROM Pot_Label l "
            + " WHERE l.session_id = s.session_id ORDER BY l.label_id DESC) AS label_code "
            + "FROM Pot_Session s ";

    /**
     * READ, one page of rows with optional filters.
     */
    public List<PotSession> search(String label, Integer deviceId, int page, int size) {
        List<PotSession> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder(SELECT_BASE).append("WHERE 1 = 1 ");
        if (label != null && !label.isEmpty()) {
            sb.append("AND EXISTS (SELECT 1 FROM Pot_Label l2 WHERE l2.session_id = s.session_id ")
                    .append("AND l2.label_code = ? AND l2.label_id = ")
                    .append("(SELECT MAX(l3.label_id) FROM Pot_Label l3 WHERE l3.session_id = s.session_id)) ");
        }
        if (deviceId != null) {
            sb.append("AND s.device_id = ? ");
        }
        sb.append("ORDER BY s.measured_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            cn = DBContext.getConnection();
            ps = cn.prepareStatement(sb.toString());
            int i = 1;
            if (label != null && !label.isEmpty()) {
                ps.setString(i++, label);
            }
            if (deviceId != null) {
                ps.setInt(i++, deviceId.intValue());
            }
            ps.setInt(i++, (page - 1) * size);
            ps.setInt(i++, size);
            rs = ps.executeQuery();
            while (rs.next()) {
                out.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs, ps, cn);
        }
        return out;
    }

    public int count(String label, Integer deviceId) {
        StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM Pot_Session s WHERE 1 = 1 ");
        if (label != null && !label.isEmpty()) {
            // same rule as search(): only the LATEST label of a session counts
            sb.append("AND (SELECT TOP 1 l.label_code FROM Pot_Label l ")
              .append("WHERE l.session_id = s.session_id ORDER BY l.label_id DESC) = ? ");
        }
        if (deviceId != null) sb.append("AND s.device_id = ? ");

        Connection cn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            cn = DBContext.getConnection();
            ps = cn.prepareStatement(sb.toString());
            int i = 1;
            if (label != null && !label.isEmpty()) ps.setString(i++, label);
            if (deviceId != null) ps.setInt(i++, deviceId.intValue());
            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs, ps, cn);
        }
        return 0;
    }

    public PotSession findById(int id) {
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            cn = DBContext.getConnection();
            ps = cn.prepareStatement(SELECT_BASE + "WHERE s.session_id = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs, ps, cn);
        }
        return null;
    }

    /*
     CREATE
     */
    public int insert(PotSession o) {
        String sql = "INSERT INTO Pot_Session(device_id, device_seq, measured_at, pot_no, weight_before, "
                + "weight_after, water_ml, retain_ratio, moist_before, moist_after, light_lux, dry_rate) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection cn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            cn = DBContext.getConnection();
            ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, o.getDeviceId());
            ps.setInt(2, o.getDeviceSeq());
            ps.setTimestamp(3, o.getMeasuredAt());
            ps.setInt(4, o.getPotNo());
            ps.setDouble(5, o.getWeightBefore());
            ps.setDouble(6, o.getWeightAfter());
            ps.setDouble(7, o.getWaterMl());
            ps.setDouble(8, o.getRetainRatio());
            ps.setInt(9, o.getMoistBefore());
            ps.setInt(10, o.getMoistAfter());
            ps.setInt(11, o.getLightLux());
            ps.setDouble(12, o.getDryRate());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLIntegrityConstraintViolationException dup) {
            return -1; // the unique key rejected a resent packet
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UQ_Pot_Session_seq")) {
                return -1;
            }
            e.printStackTrace();
        } finally {
            close(rs, ps, cn);
        }
        return 0;
    }

    /*
      UPDATE
     */
    public boolean update(PotSession o) {
        String sql = "UPDATE Pot_Session SET pot_no = ?, weight_before = ?, weight_after = ?, water_ml = ?, "
                + "retain_ratio = ?, moist_before = ?, moist_after = ?, light_lux = ?, dry_rate = ? WHERE session_id = ?";
        Connection cn = null;
        PreparedStatement ps = null;
        try {
            cn = DBContext.getConnection();
            ps = cn.prepareStatement(sql);
            ps.setInt(1, o.getPotNo());
            ps.setDouble(2, o.getWeightBefore());
            ps.setDouble(3, o.getWeightAfter());
            ps.setDouble(4, o.getWaterMl());
            ps.setDouble(5, o.getRetainRatio());
            ps.setInt(6, o.getMoistBefore());
            ps.setInt(7, o.getMoistAfter());
            ps.setInt(8, o.getLightLux());
            ps.setDouble(9, o.getDryRate());
            ps.setInt(10, o.getSessionId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(null, ps, cn);
        }
        return false;
    }

    /*
      DELETE
     */
    public boolean delete(int id) {
        Connection cn = null;
        try {
            cn = DBContext.getConnection();
            cn.setAutoCommit(false);
            exec(cn, "DELETE FROM Pot_Alert WHERE session_id = ?", id);
            exec(cn, "DELETE FROM Pot_Label WHERE session_id = ?", id);
            exec(cn, "DELETE FROM Pot_Session WHERE session_id = ?", id);
            cn.commit();
            return true;
        } catch (SQLException e) {
            try {
                if (cn != null) {
                    cn.rollback();
                }
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
        } finally {
            try {
                if (cn != null) {
                    cn.close();
                }
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    private void exec(Connection cn, String sql, int id) throws SQLException {
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    private PotSession map(ResultSet rs) throws SQLException {
        PotSession o = new PotSession();
        o.setSessionId(rs.getInt("session_id"));
        o.setDeviceId(rs.getInt("device_id"));
        o.setDeviceSeq(rs.getInt("device_seq"));
        o.setMeasuredAt(rs.getTimestamp("measured_at"));
        o.setIngestedAt(rs.getTimestamp("ingested_at"));
        o.setSample(rs.getBoolean("is_sample"));
        o.setLabelCode(rs.getString("label_code"));
        o.setPotNo(rs.getInt("pot_no"));
        o.setWeightBefore(rs.getDouble("weight_before"));
        o.setWeightAfter(rs.getDouble("weight_after"));
        o.setWaterMl(rs.getDouble("water_ml"));
        o.setRetainRatio(rs.getDouble("retain_ratio"));
        o.setMoistBefore(rs.getInt("moist_before"));
        o.setMoistAfter(rs.getInt("moist_after"));
        o.setLightLux(rs.getInt("light_lux"));
        o.setDryRate(rs.getDouble("dry_rate"));
        return o;
    }

    /**
     the retain_ratio of the latest `limit` sessions of one pot, up to and including
     the given time, OLDEST FIRST so the chart reads left to right.
    */
    public List<Double> retainSeries(int potNo, Timestamp upTo, int limit) {
        List<Double> out = new ArrayList<Double>();
        String sql = "SELECT TOP (?) retain_ratio FROM Pot_Session "
                   + "WHERE pot_no = ? AND measured_at <= ? AND retain_ratio IS NOT NULL "
                   + "ORDER BY measured_at DESC, session_id DESC";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, potNo);
            ps.setTimestamp(3, upTo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(Double.valueOf(rs.getDouble(1)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        java.util.Collections.reverse(out);
        return out;
    }
    
     /*
     Sequence number for a session typed in by hand: -1, -2, -3 ... The board counts upwards
     from 1, so a manual row can never collide with a packet from the device.
     */
    public int nextManualSeq(int deviceId) {
        String sql = "SELECT ISNULL(MIN(device_seq), 0) - 1 FROM Pot_Session "
                   + "WHERE device_id = ? AND device_seq < 0";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, deviceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
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
    
    
    // The most recent session of one pot (with its current label), or null when the pot has none. 
    public PotSession findLatestByPot(int potNo) {
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BASE
                     + "WHERE s.pot_no = ? ORDER BY s.measured_at DESC, s.session_id DESC")) {
            ps.setInt(1, potNo);
            ps.setMaxRows(1);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
