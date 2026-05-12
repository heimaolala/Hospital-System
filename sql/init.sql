CREATE DATABASE IF NOT EXISTS hospital_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE hospital_system;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_card VARCHAR(18) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    age INT,
    gender TINYINT,
    address VARCHAR(255),
    phone VARCHAR(20),
    role TINYINT NOT NULL,
    status TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login DATETIME,
    must_change_pwd TINYINT DEFAULT 0,
    INDEX idx_users_role_status (role, status),
    UNIQUE INDEX idx_users_id_card (id_card)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS doctor_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    hospital VARCHAR(100),
    department VARCHAR(100),
    title VARCHAR(50),
    specialty TEXT,
    INDEX idx_doctor_dept (department),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS medical_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    content TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_medical_patient (patient_id),
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    department VARCHAR(100) NOT NULL,
    schedule_date DATE NOT NULL,
    time_slot TINYINT NOT NULL,
    total_quota INT NOT NULL,
    remaining_quota INT NOT NULL,
    status TINYINT DEFAULT 0,
    is_recurring TINYINT DEFAULT 0,
    recur_pattern VARCHAR(50),
    version INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_schedules_doctor_date (doctor_id, schedule_date),
    INDEX idx_schedules_date_status (schedule_date, status),
    INDEX idx_schedules_date_dept (schedule_date, department, status),
    INDEX idx_schedules_date_dept_slot (schedule_date, department, time_slot, status),
    FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS registrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    department VARCHAR(100) NOT NULL,
    registration_date DATE NOT NULL,
    time_slot TINYINT NOT NULL,
    status TINYINT DEFAULT 0,
    payment_status TINYINT DEFAULT 0,
    cancel_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_reg_patient_date (patient_id, registration_date),
    INDEX idx_reg_doctor_date (doctor_id, registration_date),
    INDEX idx_reg_schedule (schedule_id),
    UNIQUE INDEX idx_reg_unique (patient_id, schedule_id),
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    target_type VARCHAR(50),
    target_id BIGINT,
    action VARCHAR(50),
    old_value JSON,
    new_value JSON,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_operator (operator_id, created_at),
    INDEX idx_audit_target (target_type, target_id),
    FOREIGN KEY (operator_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS schedule_audits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT NOT NULL,
    admin_id BIGINT NOT NULL,
    action TINYINT NOT NULL,
    comment TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_schedule_audit (schedule_id),
    FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO users (id_card, password_hash, name, role, status, must_change_pwd)
VALUES ('admin', 'admin', 'Admin', 2, 1, 1)
ON DUPLICATE KEY UPDATE id=id;
