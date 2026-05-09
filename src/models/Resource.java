package models;

public class Resource {
    private int resourceId;
    private String name;
    private String category;
    private int quantity;
    private String unit;
    private String location;
    private String status;
    private String donatedBy;

    public Resource(int resourceId, String name, String category, int quantity,
                    String unit, String location, String status, String donatedBy) {
        this.resourceId = resourceId;
        this.name       = name;
        this.category   = category;
        this.quantity   = quantity;
        this.unit       = unit;
        this.location   = location;
        this.status     = status;
        this.donatedBy  = donatedBy;
    }

    public Resource(String name, String category, int quantity, String unit,
                    String location, String status, String donatedBy) {
        this(0, name, category, quantity, unit, location, status, donatedBy);
    }

    public int    getResourceId() { return resourceId; }
    public String getName()       { return name; }
    public String getCategory()   { return category; }
    public int    getQuantity()   { return quantity; }
    public String getUnit()       { return unit; }
    public String getLocation()   { return location; }
    public String getStatus()     { return status; }
    public String getDonatedBy()  { return donatedBy; }

    public void setName(String n)       { this.name = n; }
    public void setCategory(String c)   { this.category = c; }
    public void setQuantity(int q)      { this.quantity = q; }
    public void setUnit(String u)       { this.unit = u; }
    public void setLocation(String l)   { this.location = l; }
    public void setStatus(String s)     { this.status = s; }
    public void setDonatedBy(String d)  { this.donatedBy = d; }
}
