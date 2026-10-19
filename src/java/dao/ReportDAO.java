package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import util.DBContext;

public class ReportDAO {

    public static class WaterSummary {
        public int potNo;
        public int waterings;
        public double waterTotal;
        public double retainedTotal;
        public double avgRetainRatio;
        public double avgDryRate;
    }

    public static class LabelStat {
        public String labelCode;
        public int count;
        public double percent;
    }

    public static class CorrectionStat {
        public int editedCount;
        public int totalCount;
        public double editRate;
    }

    public static class AlertStat {
        public String ruleCode;
        public String severity;
        public int total;
        public int acked;
        public int rejected;
        public int open;
    }

    public static class ScatterData {
        public List<Double> xs = new ArrayList<>();
        public List<Double> ys = new ArrayList<>();
    }

    public static class HourlyStat {
        public int hour;
        public int count;
    }

    public List<WaterSummary> getWaterSummaries() {
        List<WaterSummary> list = new ArrayList<>();
        String sql = "SELECT pot_no, COUNT(*) AS total_waterings, "
                   + "ISNULL(SUM(water_ml), 0) AS total_water_ml, "
                   + "ISNULL(SUM(CASE WHEN weight_after > weight_before THEN (weight_after - weight_before) ELSE 0 END), 0) AS total_retained_ml, "
                   + "ISNULL(AVG(retain_ratio), 0) AS avg_retain_ratio, "
                   + "ISNULL(AVG(dry_rate), 0) AS avg_dry_rate "
                   + "FROM Pot_Session "
                   + "WHERE pot_no IN (1, 2) "
                   + "GROUP BY pot_no ORDER BY pot_no";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                WaterSummary s = new WaterSummary();
                s.potNo = rs.getInt("pot_no");
                s.waterings = rs.getInt("total_waterings");
                s.waterTotal = rs.getDouble("total_water_ml");
                s.retainedTotal = rs.getDouble("total_retained_ml");
                s.avgRetainRatio = rs.getDouble("avg_retain_ratio");
                s.avgDryRate = rs.getDouble("avg_dry_rate");
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<LabelStat> getLabelStats() {
        List<LabelStat> list = new ArrayList<>();
        String sql = "SELECT l.label_code, COUNT(*) AS so_phien, "
                   + "CAST(100.0 * COUNT(*) / NULLIF((SELECT COUNT(*) FROM Pot_Label), 0) AS DECIMAL(5,2)) AS ti_le "
                   + "FROM Pot_Label l "
                   + "GROUP BY l.label_code ORDER BY so_phien DESC";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LabelStat s = new LabelStat();
                s.labelCode = rs.getString("label_code");
                s.count = rs.getInt("so_phien");
                s.percent = rs.getDouble("ti_le");
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public CorrectionStat getCorrectionStat() {
        CorrectionStat s = new CorrectionStat();
        String sql = "SELECT SUM(CASE WHEN nguon = N'REVIEWER' THEN 1 ELSE 0 END) AS so_phien_bi_sua, "
                   + "COUNT(*) AS tong_phien, "
                   + "CAST(100.0 * SUM(CASE WHEN nguon = N'REVIEWER' THEN 1 ELSE 0 END) "
                   + "     / NULLIF(COUNT(*), 0) AS DECIMAL(5,2)) AS ti_le_sua "
                   + "FROM ( "
                   + "    SELECT s.session_id, "
                   + "           (SELECT TOP 1 l.source FROM Pot_Label l "
                   + "            WHERE l.session_id = s.session_id ORDER BY l.label_id DESC) AS nguon "
                   + "    FROM Pot_Session s "
                   + ") q";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                s.editedCount = rs.getInt("so_phien_bi_sua");
                s.totalCount = rs.getInt("tong_phien");
                s.editRate = rs.getDouble("ti_le_sua");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return s;
    }

    public List<AlertStat> getAlertStats() {
        List<AlertStat> list = new ArrayList<>();
        String sql = "SELECT rule_code, severity, COUNT(*) AS tong_so, "
                   + "SUM(CASE WHEN status = 'ACKED' THEN 1 ELSE 0 END) AS da_xac_nhan, "
                   + "SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) AS da_tu_choi, "
                   + "SUM(CASE WHEN status = 'OPEN' THEN 1 ELSE 0 END) AS chua_xu_ly "
                   + "FROM Pot_Alert "
                   + "GROUP BY rule_code, severity ORDER BY tong_so DESC";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                AlertStat s = new AlertStat();
                s.ruleCode = rs.getString("rule_code");
                s.severity = rs.getString("severity");
                s.total = rs.getInt("tong_so");
                s.acked = rs.getInt("da_xac_nhan");
                s.rejected = rs.getInt("da_tu_choi");
                s.open = rs.getInt("chua_xu_ly");
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ScatterData getScatterData(Integer potNo) {
        ScatterData data = new ScatterData();
        String sql = "SELECT TOP 400 light_lux, dry_rate FROM Pot_Session "
                   + "WHERE light_lux IS NOT NULL AND dry_rate IS NOT NULL "
                   + (potNo != null && potNo > 0 ? "AND pot_no = ? " : "")
                   + "ORDER BY session_id DESC";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            if (potNo != null && potNo > 0) {
                ps.setInt(1, potNo.intValue());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.xs.add(Double.valueOf(rs.getDouble("light_lux")));
                    data.ys.add(Double.valueOf(rs.getDouble("dry_rate")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public List<HourlyStat> getHourlyStats() {
        List<HourlyStat> list = new ArrayList<>();
        String sql = "SELECT DATEPART(HOUR, s.measured_at) AS gio, COUNT(*) AS so_phien "
                   + "FROM Pot_Session s "
                   + "GROUP BY DATEPART(HOUR, s.measured_at) ORDER BY gio";
        try (Connection cn = DBContext.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                HourlyStat s = new HourlyStat();
                s.hour = rs.getInt("gio");
                s.count = rs.getInt("so_phien");
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
