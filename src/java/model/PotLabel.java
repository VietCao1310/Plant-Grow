package model;

import java.sql.Timestamp;

/** One row of Pot_Label. A session keeps every label it ever had, the newest row is the current one. */
public class PotLabel {

    private int labelId;
    private int sessionId;
    private String labelCode;       // OK, OVERWATER, NO_UPTAKE, DRY_STRESS, LEAK
    private String source;          // RULE or REVIEWER
    private String reason;          // filled when a reviewer corrected the label
    private Integer labeledBy;      // AppUser.user_id, null when the rule engine set it
    private String labeledByName;   // joined from AppUser.full_name, for display only
    private Timestamp labeledAt;

    public int getLabelId() { return labelId; }
    public void setLabelId(int v) { this.labelId = v; }
    public int getSessionId() { return sessionId; }
    public void setSessionId(int v) { this.sessionId = v; }
    public String getLabelCode() { return labelCode; }
    public void setLabelCode(String v) { this.labelCode = v; }
    public String getSource() { return source; }
    public void setSource(String v) { this.source = v; }
    public String getReason() { return reason; }
    public void setReason(String v) { this.reason = v; }
    public Integer getLabeledBy() { return labeledBy; }
    public void setLabeledBy(Integer v) { this.labeledBy = v; }
    public String getLabeledByName() { return labeledByName; }
    public void setLabeledByName(String v) { this.labeledByName = v; }
    public Timestamp getLabeledAt() { return labeledAt; }
    public void setLabeledAt(Timestamp v) { this.labeledAt = v; }
}