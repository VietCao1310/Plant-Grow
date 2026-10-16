package model;

public class AppRole {

    private int roleId;
    private String roleCode;   // ADMIN, GROWER, TECHNICIAN, ANALYST, VIEWER
    private String roleName;

    public int getRoleId() { return roleId; }
    public void setRoleId(int v) { this.roleId = v; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String v) { this.roleCode = v; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String v) { this.roleName = v; }
}