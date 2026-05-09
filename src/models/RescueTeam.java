package models;

public class RescueTeam {
    private int teamId;
    private String teamName;
    private String leader;
    private String leaderPhone;
    private int members;
    private String vehicle;
    private String vehicleNo;
    private String specialization;
    private String status;
    private String currentLocation;

    public RescueTeam(int teamId, String teamName, String leader, String leaderPhone,
                      int members, String vehicle, String vehicleNo,
                      String specialization, String status, String currentLocation) {
        this.teamId          = teamId;
        this.teamName        = teamName;
        this.leader          = leader;
        this.leaderPhone     = leaderPhone;
        this.members         = members;
        this.vehicle         = vehicle;
        this.vehicleNo       = vehicleNo;
        this.specialization  = specialization;
        this.status          = status;
        this.currentLocation = currentLocation;
    }

    public RescueTeam(String teamName, String leader, String leaderPhone,
                      int members, String vehicle, String vehicleNo,
                      String specialization, String status, String currentLocation) {
        this(0, teamName, leader, leaderPhone, members, vehicle, vehicleNo, specialization, status, currentLocation);
    }

    public int    getTeamId()          { return teamId; }
    public String getTeamName()        { return teamName; }
    public String getLeader()          { return leader; }
    public String getLeaderPhone()     { return leaderPhone; }
    public int    getMembers()         { return members; }
    public String getVehicle()         { return vehicle; }
    public String getVehicleNo()       { return vehicleNo; }
    public String getSpecialization()  { return specialization; }
    public String getStatus()          { return status; }
    public String getCurrentLocation() { return currentLocation; }

    public void setStatus(String s)           { this.status = s; }
    public void setCurrentLocation(String l)  { this.currentLocation = l; }
    public void setTeamName(String n)         { this.teamName = n; }
    public void setLeader(String l)           { this.leader = l; }
    public void setLeaderPhone(String p)      { this.leaderPhone = p; }
    public void setMembers(int m)             { this.members = m; }
    public void setVehicle(String v)          { this.vehicle = v; }
    public void setVehicleNo(String v)        { this.vehicleNo = v; }
    public void setSpecialization(String s)   { this.specialization = s; }
}
