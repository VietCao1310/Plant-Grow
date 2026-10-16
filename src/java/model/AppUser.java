package model;

import java.sql.Timestamp;

/** One row of AppUser joined with its role. Columns match PotDB exactly. */
public class AppUser {

    private int userId;
    private String username;
    private String passHash;
    private String fullName;
    private int roleId;
    private boolean locked;
    private Timestamp createdAt;
    private String roleCode;   // AppRole.role_code
    private String roleName;   // AppRole.role_name

    public int getUserId() { return userId; }
    public void setUserId(int v) { this.userId = v; }
    public String getUsername() { return username; }
    public void setUsername(String v) { this.username = v; }
    public String getPassHash() { return passHash; }
    public void setPassHash(String v) { this.passHash = v; }
    public String getFullName() { return fullName; }
    public void setFullName(String v) { this.fullName = v; }
    public int getRoleId() { return roleId; }
    public void setRoleId(int v) { this.roleId = v; }
    public boolean isLocked() { return locked; }
    public void setLocked(boolean v) { this.locked = v; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp v) { this.createdAt = v; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String v) { this.roleCode = v; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String v) { this.roleName = v; }
}