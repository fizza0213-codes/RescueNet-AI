package models;

import java.sql.Timestamp;

public class User {
    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private boolean active;
    private Timestamp lastLogin;
    private Timestamp createdAt;

    public User(int userId, String username, String password,
                String fullName, String email, String phone, String role) {
        this.userId   = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email    = email;
        this.phone    = phone;
        this.role     = role;
        this.active   = true;
    }

    // Constructor for new user registration (no userId yet)
    public User(String username, String password, String fullName,
                String email, String phone, String role) {
        this(0, username, password, fullName, email, phone, role);
    }

    public int    getUserId()    { return userId; }
    public String getUsername()  { return username; }
    public String getPassword()  { return password; }
    public String getFullName()  { return fullName; }
    public String getEmail()     { return email; }
    public String getPhone()     { return phone; }
    public String getRole()      { return role; }
    public boolean isActive()    { return active; }
    public Timestamp getLastLogin()  { return lastLogin; }
    public Timestamp getCreatedAt()  { return createdAt; }

    public void setActive(boolean a)       { this.active = a; }
    public void setLastLogin(Timestamp t)  { this.lastLogin = t; }
    public void setCreatedAt(Timestamp t)  { this.createdAt = t; }
    public void setPassword(String p)      { this.password = p; }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s", fullName, username, role);
    }
}
