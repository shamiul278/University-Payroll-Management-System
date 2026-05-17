-- ============================================================
--  University Payroll Management System
--  Database Schema — MySQL / XAMPP
--  Run in phpMyAdmin or: mysql -u root < sql/create_tables.sql
-- ============================================================

-- Create and select the database
CREATE DATABASE IF NOT EXISTS upms
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
USE upms;

-- ============================================================
--  Tear-down (safe re-run): triggers → procedures → tables
-- ============================================================

DROP TRIGGER IF EXISTS trg_payroll_before_insert;
DROP TRIGGER IF EXISTS trg_salary_after_update;
DROP TRIGGER IF EXISTS trg_salary_after_delete;
DROP TRIGGER IF EXISTS trg_payroll_bonus_after_insert;
DROP TRIGGER IF EXISTS trg_payroll_bonus_after_delete;
DROP TRIGGER IF EXISTS trg_bonus_after_update;
DROP TRIGGER IF EXISTS trg_payroll_deduction_after_insert;
DROP TRIGGER IF EXISTS trg_payroll_deduction_after_delete;
DROP TRIGGER IF EXISTS trg_deduction_after_update;

DROP PROCEDURE IF EXISTS sp_generate_payroll;
DROP PROCEDURE IF EXISTS sp_recalculate_payroll_total;

DROP TABLE IF EXISTS Payroll_Deduction;
DROP TABLE IF EXISTS Payroll_Bonus;
DROP TABLE IF EXISTS Deduction;
DROP TABLE IF EXISTS Bonus;
DROP TABLE IF EXISTS Payroll;
DROP TABLE IF EXISTS Attendance;
DROP TABLE IF EXISTS Salary;
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS Employee;
DROP TABLE IF EXISTS Department;

-- ============================================================
--  Tables
-- ============================================================

-- 1. Department
CREATE TABLE Department (
    dept_id    VARCHAR(10)  NOT NULL,
    dept_name  VARCHAR(100) NOT NULL,
    building   VARCHAR(100),
    contact_no VARCHAR(15),
    CONSTRAINT pk_department PRIMARY KEY (dept_id)
);

-- 2. Employee
CREATE TABLE Employee (
    emp_id          VARCHAR(10)  NOT NULL,
    name            VARCHAR(100) NOT NULL,
    designation     VARCHAR(100),
    email           VARCHAR(100) NOT NULL,
    phone           VARCHAR(15),
    join_date       DATE,
    employment_type VARCHAR(20)
        CHECK (employment_type IN ('Full-Time', 'Part-Time', 'Contractual')),
    dept_id         VARCHAR(10),
    CONSTRAINT pk_employee    PRIMARY KEY (emp_id),
    CONSTRAINT uq_emp_email   UNIQUE      (email),
    CONSTRAINT fk_emp_dept    FOREIGN KEY (dept_id)
        REFERENCES Department (dept_id) ON DELETE SET NULL
);

-- 3. Users (login accounts)
CREATE TABLE Users (
    user_id  VARCHAR(10)  NOT NULL,
    username VARCHAR(50)  NOT NULL,
    password VARCHAR(100) NOT NULL,
    role     VARCHAR(20)
        CHECK (role IN ('Administrator', 'Accountant')),
    emp_id   VARCHAR(10),
    CONSTRAINT pk_users      PRIMARY KEY (user_id),
    CONSTRAINT uq_username   UNIQUE      (username),
    CONSTRAINT fk_user_emp   FOREIGN KEY (emp_id)
        REFERENCES Employee (emp_id) ON DELETE SET NULL
);

-- 4. Salary  (one record per employee — enforced by UNIQUE on emp_id)
CREATE TABLE Salary (
    salary_id    VARCHAR(10)   NOT NULL,
    basic_salary DECIMAL(10,2) NOT NULL,
    allowance    DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    emp_id       VARCHAR(10),
    CONSTRAINT pk_salary     PRIMARY KEY (salary_id),
    CONSTRAINT uq_salary_emp UNIQUE      (emp_id),
    CONSTRAINT fk_salary_emp FOREIGN KEY (emp_id)
        REFERENCES Employee (emp_id) ON DELETE CASCADE
);

-- 5. Attendance  (date_ avoids the reserved word DATE)
CREATE TABLE Attendance (
    attendance_id VARCHAR(10) NOT NULL,
    date_         DATE        NOT NULL,
    status        VARCHAR(10)
        CHECK (status IN ('Present', 'Absent', 'Leave')),
    emp_id        VARCHAR(10),
    CONSTRAINT pk_attendance  PRIMARY KEY (attendance_id),
    CONSTRAINT fk_attend_emp  FOREIGN KEY (emp_id)
        REFERENCES Employee (emp_id) ON DELETE CASCADE
);

-- 6. Payroll  (month_ avoids the built-in function name MONTH)
CREATE TABLE Payroll (
    payroll_id     VARCHAR(10)   NOT NULL,
    month_         VARCHAR(20)   NOT NULL,
    generated_date DATE          NOT NULL,
    payment_status VARCHAR(20)   NOT NULL DEFAULT 'Pending'
        CHECK (payment_status IN ('Pending', 'Processed', 'Disbursed')),
    total_salary   DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    emp_id         VARCHAR(10),
    CONSTRAINT pk_payroll     PRIMARY KEY (payroll_id),
    CONSTRAINT fk_payroll_emp FOREIGN KEY (emp_id)
        REFERENCES Employee (emp_id) ON DELETE SET NULL
);

-- 7. Bonus
CREATE TABLE Bonus (
    bonus_id VARCHAR(10)   NOT NULL,
    amount   DECIMAL(10,2) NOT NULL,
    type     VARCHAR(50)
        CHECK (type IN ('Performance', 'Festival')),
    CONSTRAINT pk_bonus PRIMARY KEY (bonus_id)
);

-- 8. Deduction
CREATE TABLE Deduction (
    deduction_id VARCHAR(10)   NOT NULL,
    amount       DECIMAL(10,2) NOT NULL,
    reason       VARCHAR(100)
        CHECK (reason IN ('Tax', 'Loan Repayment', 'Absence Penalty')),
    CONSTRAINT pk_deduction PRIMARY KEY (deduction_id)
);

-- 9. Payroll_Bonus  (junction: many bonuses per payroll)
CREATE TABLE Payroll_Bonus (
    payroll_id VARCHAR(10) NOT NULL,
    bonus_id   VARCHAR(10) NOT NULL,
    CONSTRAINT pk_payroll_bonus PRIMARY KEY (payroll_id, bonus_id),
    CONSTRAINT fk_pb_payroll    FOREIGN KEY (payroll_id)
        REFERENCES Payroll (payroll_id) ON DELETE CASCADE,
    CONSTRAINT fk_pb_bonus      FOREIGN KEY (bonus_id)
        REFERENCES Bonus (bonus_id) ON DELETE CASCADE
);

-- 10. Payroll_Deduction  (junction: many deductions per payroll)
CREATE TABLE Payroll_Deduction (
    payroll_id   VARCHAR(10) NOT NULL,
    deduction_id VARCHAR(10) NOT NULL,
    CONSTRAINT pk_payroll_deduction PRIMARY KEY (payroll_id, deduction_id),
    CONSTRAINT fk_pd_payroll        FOREIGN KEY (payroll_id)
        REFERENCES Payroll (payroll_id) ON DELETE CASCADE,
    CONSTRAINT fk_pd_deduction      FOREIGN KEY (deduction_id)
        REFERENCES Deduction (deduction_id) ON DELETE CASCADE
);

-- ============================================================
--  Indexes  (FK columns used in WHERE / JOIN — MySQL does not
--  auto-index FKs unless they are part of the primary key)
-- ============================================================

CREATE INDEX idx_employee_dept     ON Employee         (dept_id);
CREATE INDEX idx_salary_emp        ON Salary           (emp_id);
CREATE INDEX idx_attendance_emp    ON Attendance        (emp_id);
CREATE INDEX idx_attendance_date   ON Attendance        (date_);
CREATE INDEX idx_payroll_emp       ON Payroll           (emp_id);
CREATE INDEX idx_payroll_month     ON Payroll           (month_);
CREATE INDEX idx_payroll_status    ON Payroll           (payment_status);
CREATE INDEX idx_pb_bonus          ON Payroll_Bonus     (bonus_id);
CREATE INDEX idx_pd_deduction      ON Payroll_Deduction (deduction_id);

-- ============================================================
--  Stored Procedures and Triggers
-- ============================================================

DELIMITER //

-- ------------------------------------------------------------
--  sp_recalculate_payroll_total
--  Rebuilds one payroll record's total from scratch.
--  Useful after bulk data corrections or manual edits.
-- ------------------------------------------------------------
CREATE PROCEDURE sp_recalculate_payroll_total(IN p_payroll_id VARCHAR(10))
BEGIN
    UPDATE Payroll p
    LEFT  JOIN Salary s ON s.emp_id = p.emp_id
    SET p.total_salary =
          COALESCE(s.basic_salary, 0) + COALESCE(s.allowance, 0)
        + COALESCE((
              SELECT SUM(b.amount)
              FROM   Payroll_Bonus pb
              JOIN   Bonus b ON b.bonus_id = pb.bonus_id
              WHERE  pb.payroll_id = p.payroll_id
          ), 0)
        - COALESCE((
              SELECT SUM(d.amount)
              FROM   Payroll_Deduction pd
              JOIN   Deduction d ON d.deduction_id = pd.deduction_id
              WHERE  pd.payroll_id = p.payroll_id
          ), 0)
    WHERE p.payroll_id = p_payroll_id;
END//

-- ------------------------------------------------------------
--  sp_generate_payroll
--  Called by PayrollDAO.generatePayroll() via CallableStatement.
--  Inserts the payroll row, links bonus and deduction if given,
--  then returns the trigger-computed total.
-- ------------------------------------------------------------
CREATE PROCEDURE sp_generate_payroll(
    IN  p_payroll_id   VARCHAR(10),
    IN  p_month        VARCHAR(20),
    IN  p_emp_id       VARCHAR(10),
    IN  p_bonus_id     VARCHAR(10),
    IN  p_deduction_id VARCHAR(10),
    OUT p_total_salary DECIMAL(10,2)
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM Salary WHERE emp_id = p_emp_id) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Cannot generate payroll: employee salary is not configured';
    END IF;

    -- trg_payroll_before_insert fires here and sets the base salary total
    INSERT INTO Payroll (payroll_id, month_, generated_date, payment_status, total_salary, emp_id)
    VALUES (p_payroll_id, p_month, CURDATE(), 'Pending', 0, p_emp_id);

    -- trg_payroll_bonus_after_insert fires here and adds bonus amount
    IF p_bonus_id IS NOT NULL AND p_bonus_id <> '' THEN
        INSERT INTO Payroll_Bonus (payroll_id, bonus_id)
        VALUES (p_payroll_id, p_bonus_id);
    END IF;

    -- trg_payroll_deduction_after_insert fires here and subtracts deduction amount
    IF p_deduction_id IS NOT NULL AND p_deduction_id <> '' THEN
        INSERT INTO Payroll_Deduction (payroll_id, deduction_id)
        VALUES (p_payroll_id, p_deduction_id);
    END IF;

    SELECT total_salary INTO p_total_salary
    FROM   Payroll
    WHERE  payroll_id = p_payroll_id;
END//

-- ------------------------------------------------------------
--  trg_payroll_before_insert
--  Seeds total_salary with the employee's gross pay (basic +
--  allowance) before bonuses and deductions are linked.
-- ------------------------------------------------------------
CREATE TRIGGER trg_payroll_before_insert
BEFORE INSERT ON Payroll
FOR EACH ROW
BEGIN
    DECLARE v_base DECIMAL(10,2) DEFAULT 0.00;

    SELECT COALESCE(basic_salary + allowance, 0)
    INTO   v_base
    FROM   Salary
    WHERE  emp_id = NEW.emp_id
    LIMIT  1;

    SET NEW.generated_date = COALESCE(NEW.generated_date, CURDATE());
    SET NEW.payment_status = COALESCE(NEW.payment_status, 'Pending');
    SET NEW.total_salary   = v_base;
END//

-- ------------------------------------------------------------
--  Salary change triggers — keep every existing payroll for the
--  same employee in sync when the salary record is updated or
--  deleted.  The after-insert trigger is intentionally omitted:
--  the before-insert payroll trigger already seeds the total
--  when a payroll row is first created.
-- ------------------------------------------------------------
CREATE TRIGGER trg_salary_after_update
AFTER UPDATE ON Salary
FOR EACH ROW
BEGIN
    IF NEW.emp_id = OLD.emp_id THEN
        UPDATE Payroll
        SET    total_salary = COALESCE(total_salary, 0)
                              + (NEW.basic_salary + NEW.allowance)
                              - (OLD.basic_salary + OLD.allowance)
        WHERE  emp_id = NEW.emp_id;
    ELSE
        UPDATE Payroll
        SET    total_salary = COALESCE(total_salary, 0)
                              - (OLD.basic_salary + OLD.allowance)
        WHERE  emp_id = OLD.emp_id;

        UPDATE Payroll
        SET    total_salary = COALESCE(total_salary, 0)
                              + (NEW.basic_salary + NEW.allowance)
        WHERE  emp_id = NEW.emp_id;
    END IF;
END//

CREATE TRIGGER trg_salary_after_delete
AFTER DELETE ON Salary
FOR EACH ROW
BEGIN
    UPDATE Payroll
    SET    total_salary = COALESCE(total_salary, 0)
                          - (OLD.basic_salary + OLD.allowance)
    WHERE  emp_id = OLD.emp_id;
END//

-- ------------------------------------------------------------
--  Bonus link triggers — add / remove bonus amount from payroll
-- ------------------------------------------------------------
CREATE TRIGGER trg_payroll_bonus_after_insert
AFTER INSERT ON Payroll_Bonus
FOR EACH ROW
BEGIN
    UPDATE Payroll p
    JOIN   Bonus   b ON b.bonus_id = NEW.bonus_id
    SET    p.total_salary = COALESCE(p.total_salary, 0) + b.amount
    WHERE  p.payroll_id = NEW.payroll_id;
END//

CREATE TRIGGER trg_payroll_bonus_after_delete
AFTER DELETE ON Payroll_Bonus
FOR EACH ROW
BEGIN
    UPDATE Payroll p
    JOIN   Bonus   b ON b.bonus_id = OLD.bonus_id
    SET    p.total_salary = COALESCE(p.total_salary, 0) - b.amount
    WHERE  p.payroll_id = OLD.payroll_id;
END//

-- Propagate bonus amount edits to all linked payrolls
CREATE TRIGGER trg_bonus_after_update
AFTER UPDATE ON Bonus
FOR EACH ROW
BEGIN
    UPDATE Payroll       p
    JOIN   Payroll_Bonus pb ON pb.payroll_id = p.payroll_id
    SET    p.total_salary = COALESCE(p.total_salary, 0)
                            + (NEW.amount - OLD.amount)
    WHERE  pb.bonus_id = NEW.bonus_id;
END//

-- ------------------------------------------------------------
--  Deduction link triggers — subtract / restore deduction amount
-- ------------------------------------------------------------
CREATE TRIGGER trg_payroll_deduction_after_insert
AFTER INSERT ON Payroll_Deduction
FOR EACH ROW
BEGIN
    UPDATE Payroll    p
    JOIN   Deduction  d ON d.deduction_id = NEW.deduction_id
    SET    p.total_salary = COALESCE(p.total_salary, 0) - d.amount
    WHERE  p.payroll_id = NEW.payroll_id;
END//

CREATE TRIGGER trg_payroll_deduction_after_delete
AFTER DELETE ON Payroll_Deduction
FOR EACH ROW
BEGIN
    UPDATE Payroll    p
    JOIN   Deduction  d ON d.deduction_id = OLD.deduction_id
    SET    p.total_salary = COALESCE(p.total_salary, 0) + d.amount
    WHERE  p.payroll_id = OLD.payroll_id;
END//

-- Propagate deduction amount edits to all linked payrolls
CREATE TRIGGER trg_deduction_after_update
AFTER UPDATE ON Deduction
FOR EACH ROW
BEGIN
    UPDATE Payroll           p
    JOIN   Payroll_Deduction pd ON pd.payroll_id = p.payroll_id
    SET    p.total_salary = COALESCE(p.total_salary, 0)
                            - (NEW.amount - OLD.amount)
    WHERE  pd.deduction_id = NEW.deduction_id;
END//

DELIMITER ;
