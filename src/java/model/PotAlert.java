package model;

import java.sql.Timestamp;

public class PotAlert {

    private int alertId;
    private Integer sessionId;       // null for an alert typed in by an admin without a session
    private String ruleCode;
    private String severity;         // INFO, WARN, CRITICAL
    private String message;
    private String status;           // OPEN, ACKED, REJECTED
    private Integer handledBy;
    private String handledByName;    // joined from AppUser.full_name, for display only
    private String handledNote;
    private Timestamp createdAt;
    private Integer potNo;           // joined from Pot_Session, null when there is no session

    public int getAlertId() { return alertId; }
    public void setAlertId(int v) { this.alertId = v; }
    public Integer getSessionId() { return sessionId; }
    public void setSessionId(Integer v) { this.sessionId = v; }
    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String v) { this.ruleCode = v; }
    public String getSeverity() { return severity; }
    public void setSeverity(String v) { this.severity = v; }
    public String getMessage() { return message; }
    public void setMessage(String v) { this.message = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Integer getHandledBy() { return handledBy; }
    public void setHandledBy(Integer v) { this.handledBy = v; }
    public String getHandledByName() { return handledByName; }
    public void setHandledByName(String v) { this.handledByName = v; }
    public String getHandledNote() { return handledNote; }
    public void setHandledNote(String v) { this.handledNote = v; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp v) { this.createdAt = v; }
    public Integer getPotNo() { return potNo; }
    public void setPotNo(Integer v) { this.potNo = v; }
}