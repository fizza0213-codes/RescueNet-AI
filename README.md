# 🆘 RescueNet AI — Disaster Emergency Management System

A Java Swing desktop application for managing disaster relief operations.
Uses a Binary Search Tree (BST) engine for fast victim lookup.

## Features
- 👥 Victim Registry with BST-powered search
- 🤖 AI Chatbot (Gemini API + offline rule-based)
- 🏠 Shelter Management
- 🚑 Rescue Team Coordination
- 📦 Resource Inventory
- 📊 Reports & Analytics
- 👤 Role-based access (Admin / Officer / Citizen)

## Tech Stack
- Java 17+ (Swing GUI)
- MySQL 8.0+
- Binary Search Tree (custom implementation)
- Gemini AI API (optional)

## Setup & Run

### Prerequisites
- JDK 17+
- MySQL 8.0+
- IntelliJ IDEA

### Database Setup
```sql
CREATE DATABASE rescuenet_db;
```
Then import `db/rescuenet_db.sql`

### Configure DB credentials
Edit `src/database/DBConnection.java`:
```java
private static final String USER = "root";
private static final String PASS = "your_password";
```

### Add MySQL Driver
Add `lib/mysql-connector-j-9.6.0.jar` to project dependencies in IntelliJ.

### Default Login
| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| officer1 | pass123 | OFFICER |
| citizen1 | pass123 | CITIZEN |











