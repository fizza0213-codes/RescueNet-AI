package models;

import java.sql.Timestamp;

public class Victim {
    private int victimId;
    private String name;
    private String cnic;
    private int age;
    private String gender;
    private String disasterType;
    private int severityLevel;
    private String status;
    private String location;
    private String contact;
    private String nextOfKin;
    private String notes;
    private Integer assignedShelterId;
    private Integer assignedTeamId;
    private Integer registeredBy;
    private Timestamp registeredAt;

    // Full constructor
    public Victim(int victimId, String name, String cnic, int age, String gender,
                  String disasterType, int severityLevel, String status,
                  String location, String contact, String nextOfKin, String notes) {
        this.victimId = victimId;
        this.name = name;
        this.cnic = cnic;
        this.age = age;
        this.gender = gender;
        this.disasterType = disasterType;
        this.severityLevel = severityLevel;
        this.status = status;
        this.location = location;
        this.contact = contact;
        this.nextOfKin = nextOfKin;
        this.notes = notes;
    }

    // Compact constructor for BST-only use
    public Victim(int victimId, String name, String disasterType,
                  int severityLevel, String status, String location, String contact) {
        this(victimId, name, "", 0, "Unknown", disasterType, severityLevel, status, location, contact, "", "");
    }

    // New victim (no ID yet — DB will auto-assign)
    public Victim(String name, String cnic, int age, String gender,
                  String disasterType, int severityLevel, String status,
                  String location, String contact, String nextOfKin, String notes) {
        this(0, name, cnic, age, gender, disasterType, severityLevel, status, location, contact, nextOfKin, notes);
    }

    public int getVictimId()           { return victimId; }
    public String getName()            { return name; }
    public String getCnic()            { return cnic; }
    public int getAge()                { return age; }
    public String getGender()          { return gender; }
    public String getDisasterType()    { return disasterType; }
    public int getSeverityLevel()      { return severityLevel; }
    public String getStatus()          { return status; }
    public String getLocation()        { return location; }
    public String getContact()         { return contact; }
    public String getNextOfKin()       { return nextOfKin; }
    public String getNotes()           { return notes; }
    public Integer getAssignedShelterId() { return assignedShelterId; }
    public Integer getAssignedTeamId()    { return assignedTeamId; }
    public Integer getRegisteredBy()      { return registeredBy; }
    public Timestamp getRegisteredAt() { return registeredAt; }

    public void setVictimId(int id)    { this.victimId = id; }
    public void setStatus(String s)    { this.status = s; }
    public void setName(String n)      { this.name = n; }
    public void setCnic(String c)      { this.cnic = c; }
    public void setAge(int a)          { this.age = a; }
    public void setGender(String g)    { this.gender = g; }
    public void setDisasterType(String d)  { this.disasterType = d; }
    public void setSeverityLevel(int s)    { this.severityLevel = s; }
    public void setLocation(String l)      { this.location = l; }
    public void setContact(String c)       { this.contact = c; }
    public void setNextOfKin(String n)     { this.nextOfKin = n; }
    public void setNotes(String n)         { this.notes = n; }
    public void setAssignedShelterId(Integer id) { this.assignedShelterId = id; }
    public void setAssignedTeamId(Integer id)    { this.assignedTeamId = id; }
    public void setRegisteredBy(Integer id)      { this.registeredBy = id; }
    public void setRegisteredAt(Timestamp t)     { this.registeredAt = t; }

    public String getSeverityLabel() {
        if (severityLevel >= 9) return "CRITICAL";
        if (severityLevel >= 7) return "SEVERE";
        if (severityLevel >= 4) return "MODERATE";
        return "MILD";
    }

    public String getRegisteredAtStr() {
        if (registeredAt == null) return "N/A";
        return registeredAt.toString().substring(0, 16); // "yyyy-MM-dd HH:mm"
    }

    @Override
    public String toString() {
        return String.format("ID:%d | %s | %s | Severity:%d(%s) | %s | %s",
                victimId, name, disasterType, severityLevel, getSeverityLabel(), status, location);
    }
}
