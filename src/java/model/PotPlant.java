package model;

import java.sql.Timestamp;

/** One row of Pot_Plant. potNo is Integer because the column can be NULL (plant not on a pot). */
public class PotPlant {

    private int plantId;
    private String code;
    private String name;
    private String species;
    private Integer potNo;        // 1 or 2, or null
    private Timestamp plantedAt;
    private String note;
    private boolean active;
    private Timestamp createdAt;

    public int getPlantId() { return plantId; }
    public void setPlantId(int v) { this.plantId = v; }
    public String getCode() { return code; }
    public void setCode(String v) { this.code = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getSpecies() { return species; }
    public void setSpecies(String v) { this.species = v; }
    public Integer getPotNo() { return potNo; }
    public void setPotNo(Integer v) { this.potNo = v; }
    public Timestamp getPlantedAt() { return plantedAt; }
    public void setPlantedAt(Timestamp v) { this.plantedAt = v; }
    public String getNote() { return note; }
    public void setNote(String v) { this.note = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { this.active = v; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp v) { this.createdAt = v; }
}