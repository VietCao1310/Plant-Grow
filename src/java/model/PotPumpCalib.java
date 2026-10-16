package model;

import java.sql.Timestamp;

/** One row of Pot_PumpCalib: how many ml per second a pump delivers, measured on a given day. */
public class PotPumpCalib {

    private int pumpCalibId;
    private String code;
    private String name;
    private int potNo;                  // 1 or 2: the pump of that pot
    private double mlPerSecond;         // ml_per_second
    private Integer calibratedBy;       // AppUser.user_id, can be null
    private String calibratedByName;    // joined from AppUser.full_name, for display only
    private Timestamp calibratedAt;
    private String note;
    private boolean active;
    private Timestamp createdAt;

    public int getPumpCalibId() { return pumpCalibId; }
    public void setPumpCalibId(int v) { this.pumpCalibId = v; }
    public String getCode() { return code; }
    public void setCode(String v) { this.code = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public int getPotNo() { return potNo; }
    public void setPotNo(int v) { this.potNo = v; }
    public double getMlPerSecond() { return mlPerSecond; }
    public void setMlPerSecond(double v) { this.mlPerSecond = v; }
    public Integer getCalibratedBy() { return calibratedBy; }
    public void setCalibratedBy(Integer v) { this.calibratedBy = v; }
    public String getCalibratedByName() { return calibratedByName; }
    public void setCalibratedByName(String v) { this.calibratedByName = v; }
    public Timestamp getCalibratedAt() { return calibratedAt; }
    public void setCalibratedAt(Timestamp v) { this.calibratedAt = v; }
    public String getNote() { return note; }
    public void setNote(String v) { this.note = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { this.active = v; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp v) { this.createdAt = v; }
}