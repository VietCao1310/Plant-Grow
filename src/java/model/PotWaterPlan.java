package model;

import java.sql.Timestamp;

/** One row of Pot_WaterPlan: how much, how often and at which moisture a pot is watered. */
public class PotWaterPlan {

    private int waterPlanId;
    private String code;
    private String name;
    private int potNo;              // 1 or 2, never null in this table
    private double waterTargetMl;   // water_target_ml
    private int intervalHours;      // interval_hours
    private int triggerMoist;       // trigger_moist
    private String note;
    private boolean active;         // is_active = automatic watering on or off
    private Timestamp createdAt;

    public int getWaterPlanId() { return waterPlanId; }
    public void setWaterPlanId(int v) { this.waterPlanId = v; }
    public String getCode() { return code; }
    public void setCode(String v) { this.code = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public int getPotNo() { return potNo; }
    public void setPotNo(int v) { this.potNo = v; }
    public double getWaterTargetMl() { return waterTargetMl; }
    public void setWaterTargetMl(double v) { this.waterTargetMl = v; }
    public int getIntervalHours() { return intervalHours; }
    public void setIntervalHours(int v) { this.intervalHours = v; }
    public int getTriggerMoist() { return triggerMoist; }
    public void setTriggerMoist(int v) { this.triggerMoist = v; }
    public String getNote() { return note; }
    public void setNote(String v) { this.note = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { this.active = v; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp v) { this.createdAt = v; }
}