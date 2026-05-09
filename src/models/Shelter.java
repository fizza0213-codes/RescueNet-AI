package models;

public class Shelter {
    private int shelterId;
    private String name;
    private String location;
    private String city;
    private int capacity;
    private int occupied;
    private String status;
    private String contact;
    private String inCharge;
    private boolean hasMedical;
    private boolean hasFood;
    private boolean hasWater;
    private String notes;

    public Shelter(int shelterId, String name, String location, String city,
                   int capacity, int occupied, String status, String contact,
                   String inCharge, boolean hasMedical, boolean hasFood, boolean hasWater, String notes) {
        this.shelterId  = shelterId;
        this.name       = name;
        this.location   = location;
        this.city       = city;
        this.capacity   = capacity;
        this.occupied   = occupied;
        this.status     = status;
        this.contact    = contact;
        this.inCharge   = inCharge;
        this.hasMedical = hasMedical;
        this.hasFood    = hasFood;
        this.hasWater   = hasWater;
        this.notes      = notes;
    }

    public Shelter(String name, String location, String city, int capacity,
                   int occupied, String status, String contact, String inCharge,
                   boolean hasMedical, boolean hasFood, boolean hasWater, String notes) {
        this(0, name, location, city, capacity, occupied, status, contact, inCharge, hasMedical, hasFood, hasWater, notes);
    }

    public int    getShelterId()  { return shelterId; }
    public String getName()       { return name; }
    public String getLocation()   { return location; }
    public String getCity()       { return city; }
    public int    getCapacity()   { return capacity; }
    public int    getOccupied()   { return occupied; }
    public String getStatus()     { return status; }
    public String getContact()    { return contact; }
    public String getInCharge()   { return inCharge; }
    public boolean hasMedical()   { return hasMedical; }
    public boolean hasFood()      { return hasFood; }
    public boolean hasWater()     { return hasWater; }
    public String getNotes()      { return notes; }

    public int getAvailable()     { return Math.max(0, capacity - occupied); }
    public double getOccupancyPct() { return capacity > 0 ? (occupied * 100.0 / capacity) : 0; }

    public String getAmenitiesStr() {
        StringBuilder sb = new StringBuilder();
        if (hasMedical) sb.append("🏥Med ");
        if (hasFood)    sb.append("🍲Food ");
        if (hasWater)   sb.append("💧Water");
        return sb.toString().trim();
    }

    public void setOccupied(int o) { this.occupied = o; }
    public void setStatus(String s) { this.status = s; }
    public void setName(String n) { this.name = n; }
    public void setLocation(String l) { this.location = l; }
    public void setCity(String c) { this.city = c; }
    public void setCapacity(int c) { this.capacity = c; }
    public void setContact(String c) { this.contact = c; }
    public void setInCharge(String i) { this.inCharge = i; }
    public void setHasMedical(boolean b) { this.hasMedical = b; }
    public void setHasFood(boolean b) { this.hasFood = b; }
    public void setHasWater(boolean b) { this.hasWater = b; }
    public void setNotes(String n) { this.notes = n; }
}
