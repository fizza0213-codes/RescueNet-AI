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

## Optional: Enable AI Chatbot
Get free API key from [aistudio.google.com](https://aistudio.google.com)
and add it in `src/chatbot/AIChatbot.java`
<img width="478" height="584" alt="image" src="https://github.com/user-attachments/assets/aab97eff-7c81-41c1-aa4b-b1daa72c1fa8" />
<img width="465" height="569" alt="image" src="https://github.com/user-attachments/assets/5245102b-105c-4605-8a6b-64db4f18b7d4" />
<img width="1600" height="862" alt="image" src="https://github.com/user-attachments/assets/d3b54aa3-0d1f-482f-aaf6-2afed3606c34" />
<img width="1283" height="764" alt="image" src="https://github.com/user-attachments/assets/03c48e0a-2303-42c3-bef9-43f93f6fb264" />
<img width="1293" height="783" alt="image" src="https://github.com/user-attachments/assets/73ce60a4-37b4-4b83-9a84-d488ee2b7433" />
<img width="1285" height="778" alt="image" src="https://github.com/user-attachments/assets/5109db1d-c760-46c4-9a23-81f567e4d785" />
<img width="1283" height="763" alt="image" src="https://github.com/user-attachments/assets/d4eae122-6aaa-48e2-b94d-a14bf7aa90ab" />
<img width="847" height="671" alt="image" src="https://github.com/user-attachments/assets/64676027-1a78-4823-9ffd-0ffaa3eb65f7" />
<img width="862" height="677" alt="image" src="https://github.com/user-attachments/assets/0c6f18f7-7981-4851-a1fa-3925bdab0bd1" />
<img width="1081" height="670" alt="image" src="https://github.com/user-attachments/assets/67c433c3-d6e6-4c26-a800-02633a5e4e55" />
<img width="1100" height="644" alt="image" src="https://github.com/user-attachments/assets/63bfdcb9-a750-4d3b-9e71-4cabcc7deec9" />
<img width="985" height="607" alt="image" src="https://github.com/user-attachments/assets/4eb9e3a6-585c-4bff-8570-922b442add7b" />
<img width="1600" height="828" alt="image" src="https://github.com/user-attachments/assets/ffe88557-360d-4215-8334-61ce59c4fd47" />
<img width="1571" height="794" alt="image" src="https://github.com/user-attachments/assets/356d0aeb-f44a-4353-9d32-6bc8c6ae8c85" />
<img width="1600" height="857" alt="image" src="https://github.com/user-attachments/assets/5cc80d78-4709-4741-be93-11bbfe57df15" />
<img width="1600" height="860" alt="image" src="https://github.com/user-attachments/assets/fb1d3b6c-d49c-44c7-9eb3-c7d926b1eb32" />
<img width="1593" height="865" alt="image" src="https://github.com/user-attachments/assets/19f15776-e22e-4dca-97fb-f847916fcdd6" />
<img width="1600" height="858" alt="image" src="https://github.com/user-attachments/assets/c0ebcaf4-b51e-459f-bbab-456e1b27694a" />
<img width="1600" height="871" alt="image" src="https://github.com/user-attachments/assets/e61dcc2d-1409-4e14-a0e7-b7b0e074bbb4" />
<img width="1600" height="864" alt="image" src="https://github.com/user-attachments/assets/0eccd24e-fd42-4c31-886a-4a53e6c71893" />




















