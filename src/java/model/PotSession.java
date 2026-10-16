package model;

import java.sql.Timestamp;

public class PotSession {

    private int sessionId;
    private int deviceId;
    private int deviceSeq;
    private Timestamp measuredAt;
    private Timestamp ingestedAt;
    private boolean sample;
    private String labelCode;    // joined from the label table, latest label wins
    private int potNo;           // column pot_no
    private double weightBefore; // column weight_before
    private double weightAfter;  // column weight_after
    private double waterMl;      // column water_ml
    private double retainRatio;  // column retain_ratio
    private int moistBefore;     // column moist_before
    private int moistAfter;      // column moist_after
    private int lightLux;        // column light_lux
    private double dryRate;      // column dry_rate

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int v) {
        this.sessionId = v;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int v) {
        this.deviceId = v;
    }

    public int getDeviceSeq() {
        return deviceSeq;
    }

    public void setDeviceSeq(int v) {
        this.deviceSeq = v;
    }

    public Timestamp getMeasuredAt() {
        return measuredAt;
    }

    public void setMeasuredAt(Timestamp v) {
        this.measuredAt = v;
    }

    public Timestamp getIngestedAt() {
        return ingestedAt;
    }

    public void setIngestedAt(Timestamp v) {
        this.ingestedAt = v;
    }

    public boolean isSample() {
        return sample;
    }

    public void setSample(boolean v) {
        this.sample = v;
    }

    public String getLabelCode() {
        return labelCode;
    }

    public void setLabelCode(String v) {
        this.labelCode = v;
    }

    public int getPotNo() {
        return potNo;
    }

    public void setPotNo(int v) {
        this.potNo = v;
    }

    public double getWeightBefore() {
        return weightBefore;
    }

    public void setWeightBefore(double v) {
        this.weightBefore = v;
    }

    public double getWeightAfter() {
        return weightAfter;
    }

    public void setWeightAfter(double v) {
        this.weightAfter = v;
    }

    public double getWaterMl() {
        return waterMl;
    }

    public void setWaterMl(double v) {
        this.waterMl = v;
    }

    public double getRetainRatio() {
        return retainRatio;
    }

    public void setRetainRatio(double v) {
        this.retainRatio = v;
    }

    public int getMoistBefore() {
        return moistBefore;
    }

    public void setMoistBefore(int v) {
        this.moistBefore = v;
    }

    public int getMoistAfter() {
        return moistAfter;
    }

    public void setMoistAfter(int v) {
        this.moistAfter = v;
    }

    public int getLightLux() {
        return lightLux;
    }

    public void setLightLux(int v) {
        this.lightLux = v;
    }

    public double getDryRate() {
        return dryRate;
    }

    public void setDryRate(double v) {
        this.dryRate = v;
    }
}
