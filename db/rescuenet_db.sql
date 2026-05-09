-- ============================================================
--   RescueNet AI - Complete Database Schema v2.0
--   Run: mysql -u root -p < rescuenet_db.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS rescuenet_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rescuenet_db;

-- ── USERS ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    user_id    INT PRIMARY KEY AUTO_INCREMENT,
    username   VARCHAR(50)  UNIQUE NOT NULL,
    password   VARCHAR(255) NOT NULL,
    full_name  VARCHAR(100),
    email      VARCHAR(100),
    phone      VARCHAR(20),
    role       ENUM('ADMIN','OFFICER','CITIZEN') DEFAULT 'CITIZEN',
    active     BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── VICTIMS ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS victims (
    victim_id      INT PRIMARY KEY AUTO_INCREMENT,
    name           VARCHAR(100) NOT NULL,
    cnic           VARCHAR(20),
    age            INT DEFAULT 0,
    gender         VARCHAR(10) DEFAULT 'Unknown',
    disaster_type  VARCHAR(50),
    severity_level INT DEFAULT 1,
    status         VARCHAR(20) DEFAULT 'ACTIVE',
    location       VARCHAR(150),
    contact        VARCHAR(50),
    next_of_kin    VARCHAR(100),
    notes          TEXT,
    assigned_shelter INT NULL,
    assigned_team  INT NULL,
    registered_by  INT NULL,
    registered_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ── SHELTERS ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS shelters (
    shelter_id  INT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    location    VARCHAR(150),
    city        VARCHAR(50),
    capacity    INT DEFAULT 0,
    occupied    INT DEFAULT 0,
    status      VARCHAR(20) DEFAULT 'OPEN',
    contact     VARCHAR(50),
    in_charge   VARCHAR(100),
    has_medical BOOLEAN DEFAULT FALSE,
    has_food    BOOLEAN DEFAULT TRUE,
    has_water   BOOLEAN DEFAULT TRUE,
    notes       TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── RESCUE TEAMS ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS rescue_teams (
    team_id     INT PRIMARY KEY AUTO_INCREMENT,
    team_name   VARCHAR(100) NOT NULL,
    leader      VARCHAR(100),
    leader_phone VARCHAR(20),
    members     INT DEFAULT 0,
    vehicle     VARCHAR(50),
    vehicle_no  VARCHAR(20),
    specialization VARCHAR(50),
    status      VARCHAR(20) DEFAULT 'AVAILABLE',
    current_location VARCHAR(150),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── RESOURCES ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS resources (
    resource_id   INT PRIMARY KEY AUTO_INCREMENT,
    name          VARCHAR(100) NOT NULL,
    category      VARCHAR(20) DEFAULT 'OTHER',
    quantity      INT DEFAULT 0,
    unit          VARCHAR(20),
    location      VARCHAR(150),
    status        VARCHAR(20) DEFAULT 'AVAILABLE',
    expiry_date   DATE NULL,
    donated_by    VARCHAR(100),
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ── CHATBOT HISTORY ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS chatbot_history (
    chat_id     INT PRIMARY KEY AUTO_INCREMENT,
    user_id     INT NULL,
    session_id  VARCHAR(50),
    user_query  TEXT NOT NULL,
    ai_response TEXT NOT NULL,
    response_type VARCHAR(20) DEFAULT 'RULE_BASED',
    timestamp   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── INCIDENTS ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS incidents (
    incident_id   INT PRIMARY KEY AUTO_INCREMENT,
    title         VARCHAR(150) NOT NULL,
    type          VARCHAR(50),
    severity      VARCHAR(10) DEFAULT 'MEDIUM',
    location      VARCHAR(150),
    city          VARCHAR(50),
    description   TEXT,
    status        VARCHAR(20) DEFAULT 'ACTIVE',
    reported_by   INT NULL,
    reported_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at   TIMESTAMP NULL
);

-- ── ACTIVITY LOG ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS activity_log (
    log_id      INT PRIMARY KEY AUTO_INCREMENT,
    user_id     INT NULL,
    action_type VARCHAR(50),
    description TEXT,
    entity_id   INT NULL,
    timestamp   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── SAMPLE DATA ──────────────────────────────────────────────
INSERT IGNORE INTO users(username,password,full_name,email,phone,role) VALUES
  ('admin','admin123','System Administrator','admin@rescuenet.pk','051-9999001','ADMIN'),
  ('officer1','pass123','Ahmed Khan','ahmed@rescuenet.pk','0300-1234567','OFFICER'),
  ('citizen1','pass123','Muhammad Ali','ali@email.com','0321-3456789','CITIZEN');

INSERT IGNORE INTO shelters(name,location,city,capacity,occupied,status,contact,in_charge,has_medical,has_food,has_water) VALUES
  ('Lahore Relief Camp A','Data Darbar Road, Gulberg','Lahore',500,320,'OPEN','042-99200001','Col. (R) Tariq Hussain',TRUE,TRUE,TRUE),
  ('Karachi Emergency Shelter','Burns Road, Saddar','Karachi',300,300,'FULL','021-99200002','Ms. Fatima Zaidi',TRUE,TRUE,TRUE),
  ('Islamabad Relief Centre','Sector G-10, I-8 Markaz','Islamabad',200,80,'OPEN','051-99200003','Dr. Imran Shah',TRUE,TRUE,TRUE),
  ('Peshawar Flood Camp','Ring Road, Hayatabad','Peshawar',400,150,'OPEN','091-99200004','Brig. (R) Ali Raza',FALSE,TRUE,TRUE),
  ('Multan Relief Point','Qasim Bagh Stadium','Multan',250,90,'OPEN','061-99200005','Ms. Ayesha Nawaz',TRUE,TRUE,TRUE);

INSERT IGNORE INTO rescue_teams(team_name,leader,leader_phone,members,vehicle,vehicle_no,specialization,status,current_location) VALUES
  ('Team Alpha','Sgt. Khalid Mehmood','0333-1111001',8,'Rescue Truck','PJC-001','Flood Rescue','AVAILABLE','Lahore HQ'),
  ('Team Bravo','Sgt. Nadia Iqbal','0333-1111002',6,'Ambulance','PJC-002','Medical Aid','ON_MISSION','Model Town, Lahore'),
  ('Team Charlie','Lt. Usman Ghani','0333-1111003',10,'Helicopter','N/A','Search & Rescue','AVAILABLE','Islamabad Base'),
  ('Team Delta','Sgt. Rabia Aslam','0333-1111004',7,'Speed Boat','PJC-004','Water Rescue','AVAILABLE','Karachi Port'),
  ('Team Echo','Sgt. Bilal Chaudhry','0333-1111005',9,'Fire Tender','PJC-005','Fire Fighting','OFF_DUTY','Peshawar Station');

INSERT IGNORE INTO resources(name,category,quantity,unit,location,status) VALUES
  ('First Aid Kits','MEDICAL',450,'units','Lahore Warehouse','AVAILABLE'),
  ('Rice Bags 50kg','FOOD',800,'bags','Karachi Store','AVAILABLE'),
  ('Bottled Water 1.5L','WATER',5000,'bottles','Islamabad Depot','AVAILABLE'),
  ('Blankets','CLOTHING',1200,'pieces','Peshawar Store','AVAILABLE'),
  ('Rescue Ropes','EQUIPMENT',80,'rolls','Lahore HQ','AVAILABLE'),
  ('Generators','EQUIPMENT',15,'units','Islamabad Depot','AVAILABLE'),
  ('Tents (Family)','EQUIPMENT',300,'units','Karachi Store','AVAILABLE'),
  ('Medicines Pack','MEDICAL',600,'packs','Multan Store','AVAILABLE');

INSERT IGNORE INTO incidents(title,type,severity,location,city,description,status) VALUES
  ('Chenab River Flooding','Flood','CRITICAL','Jhang District','Jhang','Major flooding affecting 50,000 residents along Chenab river banks','ACTIVE'),
  ('Earthquake Aftershocks','Earthquake','HIGH','Swat Valley','Swat','Series of aftershocks following M5.8 earthquake, buildings damaged','ACTIVE'),
  ('Urban Fire Outbreak','Fire','MEDIUM','Lyari Industrial Area','Karachi','Factory fire spread to 3 adjacent structures, evacuations underway','CONTAINED');

INSERT IGNORE INTO victims(name,cnic,age,gender,disaster_type,severity_level,status,location,contact,next_of_kin,notes) VALUES
  ('Muhammad Arif','35202-1234567-1',42,'Male','Flood',8,'ACTIVE','Jhang City, Near River Bank','0300-1111001','Fatima Arif','Critical condition, needs medical attention'),
  ('Amna Bibi','35202-2345678-2',28,'Female','Flood',6,'RECOVERING','Jhang Relief Camp','0301-2222002','Tariq Mahmood','Recovering at camp, needs medication'),
  ('Salman Khan','42101-3456789-3',35,'Male','Earthquake',9,'ACTIVE','Swat Valley, House No 5','0321-3333003','Rizwan Khan','Trapped under debris, urgent rescue needed'),
  ('Zainab Hussain','42101-4567890-4',19,'Female','Earthquake',7,'RESCUED','Swat City Hospital','0311-4444004','Hussain Ali','Rescued, under medical care'),
  ('Ali Raza','42201-5678901-5',55,'Male','Fire',5,'ACTIVE','Lyari Industrial Area','0333-5555005','Sara Raza','Minor burns, needs shelter');
