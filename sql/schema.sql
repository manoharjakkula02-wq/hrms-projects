CREATE DATABASE IF NOT EXISTS hrms_db;
USE hrms_db;

CREATE TABLE IF NOT EXISTS organizations (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    address         VARCHAR(255) NOT NULL,
    pan             VARCHAR(10)  NOT NULL,
    gst             VARCHAR(15)  NOT NULL,
    department      VARCHAR(100) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,   -- used for login
    password_hash   VARCHAR(255) NOT NULL,
    employee_count  INT NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS employees (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    org_id       INT NOT NULL,
    name         VARCHAR(120) NOT NULL,
    email        VARCHAR(150) NOT NULL,
    phone        VARCHAR(20),
    designation  VARCHAR(100),
    department   VARCHAR(100),
    joining_date DATE NOT NULL,
    UNIQUE KEY uq_emp_email (org_id, email),
    FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS attendance (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    org_id      INT NOT NULL,
    employee_id INT NOT NULL,
    att_date    DATE NOT NULL,
    status      ENUM('PRESENT','ABSENT','LEAVE','HALF_DAY') NOT NULL,
    UNIQUE KEY uq_att (employee_id, att_date),
    FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS salary (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    org_id      INT NOT NULL,
    employee_id INT NOT NULL,
    salary_month CHAR(7) NOT NULL,                  -- YYYY-MM
    basic       DECIMAL(12,2) NOT NULL,
    hra         DECIMAL(12,2) NOT NULL DEFAULT 0,
    allowances  DECIMAL(12,2) NOT NULL DEFAULT 0,
    deductions  DECIMAL(12,2) NOT NULL DEFAULT 0,
    net_salary  DECIMAL(12,2) GENERATED ALWAYS AS (basic + hra + allowances - deductions) STORED,
    UNIQUE KEY uq_sal (employee_id, salary_month),
    FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS notices (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    org_id      INT NOT NULL,
    type        ENUM('NOTICE','HOLIDAY') NOT NULL,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    event_date  DATE NOT NULL,
    FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE
) ENGINE=InnoDB;
