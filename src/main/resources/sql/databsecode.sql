-- ====================================================================
-- UPMS DATABASE MASTER SCHEMA SCRIPT (ORACLE 10g / 11g COMPATIBLE)
-- ====================================================================

-- --------------------------------------------------------------------
-- STEP 1: DROP OLD TABLES & SEQUENCES (CLEANUP BEFORE CREATION)
-- --------------------------------------------------------------------
DROP TABLE payroll_records CASCADE CONSTRAINTS;
DROP TABLE payroll_cycles CASCADE CONSTRAINTS;
DROP TABLE attendance_logs CASCADE CONSTRAINTS;
DROP TABLE salary_configurations CASCADE CONSTRAINTS;
DROP TABLE employees CASCADE CONSTRAINTS;

DROP SEQUENCE emp_seq;
DROP SEQUENCE salary_seq;
DROP SEQUENCE attendance_seq;
DROP SEQUENCE cycle_seq;
DROP SEQUENCE record_seq;

-- --------------------------------------------------------------------
-- STEP 2: CREATE CORE EMPLOYEES TABLE & SEQUENCE
-- --------------------------------------------------------------------
CREATE TABLE employees (
                           emp_id VARCHAR2(20) PRIMARY KEY,
                           full_name VARCHAR2(100) NOT NULL,
                           designation VARCHAR2(50),
                           department VARCHAR2(50),
                           email VARCHAR2(100) UNIQUE NOT NULL,
                           phone VARCHAR2(20),
                           join_date DATE DEFAULT SYSDATE,
                           base_salary NUMBER(10,2) DEFAULT 0,
                           attendance_pct NUMBER(5,2) DEFAULT 0.0
);

CREATE SEQUENCE emp_seq START WITH 10024 INCREMENT BY 1;

-- Trigger to auto-generate Employee ID (e.g., EMP-10024)
CREATE OR REPLACE TRIGGER trg_emp_id
BEFORE INSERT ON employees
FOR EACH ROW
BEGIN
    IF :NEW.emp_id IS NULL THEN
SELECT 'EMP-' || emp_seq.NEXTVAL INTO :NEW.emp_id FROM DUAL;
END IF;
END;
/

-- --------------------------------------------------------------------
-- STEP 3: CREATE SALARY CONFIGURATIONS TABLE & SEQUENCE
-- --------------------------------------------------------------------
CREATE TABLE salary_configurations (
                                       config_id NUMBER PRIMARY KEY,
                                       emp_id VARCHAR2(20) REFERENCES employees(emp_id),
                                       basic_salary NUMBER(10,2) NOT NULL,
                                       allowance NUMBER(10,2) DEFAULT 0,
                                       total_package NUMBER(10,2),
                                       effective_date DATE,
                                       status VARCHAR2(20) DEFAULT 'ACTIVE'
);

CREATE SEQUENCE salary_seq START WITH 1 INCREMENT BY 1;

-- Smart Trigger for Salary ID, Total Package and 15% Fiscal Policy
CREATE OR REPLACE TRIGGER trg_salary_config
BEFORE INSERT ON salary_configurations
FOR EACH ROW
DECLARE
v_old_salary NUMBER(10,2);
BEGIN
    -- Auto Generate ID
    IF :NEW.config_id IS NULL THEN
SELECT salary_seq.NEXTVAL INTO :NEW.config_id FROM DUAL;
END IF;

    -- Auto Calculate Total Package
    :NEW.total_package := :NEW.basic_salary + NVL(:NEW.allowance, 0);

    -- Fiscal Policy Rule
BEGIN
SELECT NVL(base_salary, 0) INTO v_old_salary
FROM employees
WHERE emp_id = :NEW.emp_id;

IF v_old_salary = 0 THEN
            :NEW.status := 'ACTIVE';
        ELSIF :NEW.basic_salary > (v_old_salary * 1.15) THEN
            :NEW.status := 'PENDING';
ELSE
            :NEW.status := 'ACTIVE';
END IF;
EXCEPTION
        WHEN NO_DATA_FOUND THEN
            :NEW.status := 'ACTIVE';
END;
END;
/

-- --------------------------------------------------------------------
-- STEP 4: CREATE ATTENDANCE LOGS TABLE & SEQUENCE
-- --------------------------------------------------------------------
CREATE TABLE attendance_logs (
                                 attendance_id NUMBER PRIMARY KEY,
                                 emp_id VARCHAR2(20) REFERENCES employees(emp_id),
                                 attendance_date DATE DEFAULT TRUNC(SYSDATE),
                                 status VARCHAR2(20) CHECK (status IN ('PRESENT', 'ABSENT', 'LEAVE', 'PENDING')),
                                 CONSTRAINT uniq_emp_date UNIQUE (emp_id, attendance_date)
);

CREATE SEQUENCE attendance_seq START WITH 1 INCREMENT BY 1;

-- --------------------------------------------------------------------
-- STEP 5: CREATE PAYROLL CYCLES & RECORDS TABLES
-- --------------------------------------------------------------------
CREATE TABLE payroll_cycles (
                                cycle_id NUMBER PRIMARY KEY,
                                month_year VARCHAR2(20) NOT NULL,
                                total_gross NUMBER(12,2) DEFAULT 0,
                                total_deductions NUMBER(12,2) DEFAULT 0,
                                net_payout NUMBER(12,2) DEFAULT 0,
                                status VARCHAR2(20) DEFAULT 'DRAFT',
                                created_date DATE DEFAULT SYSDATE
);

CREATE SEQUENCE cycle_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE payroll_records (
                                 record_id NUMBER PRIMARY KEY,
                                 cycle_id NUMBER REFERENCES payroll_cycles(cycle_id),
                                 emp_id VARCHAR2(20) REFERENCES employees(emp_id),
                                 base_allowance NUMBER(10,2) DEFAULT 0,
                                 bonus NUMBER(10,2) DEFAULT 0,
                                 absence_penalty NUMBER(10,2) DEFAULT 0,
                                 net_salary NUMBER(10,2) DEFAULT 0
);

CREATE SEQUENCE record_seq START WITH 1 INCREMENT BY 1;

-- --------------------------------------------------------------------
-- STEP 6: SEED DUMMY DATA FOR INITIAL DEMO
-- --------------------------------------------------------------------
INSERT INTO employees (full_name, designation, department, email, join_date)
VALUES ('Adrian Sterling', 'Senior Lecturer', 'Computer Science', 'a.sterling@upms.edu', TO_DATE('12-OCT-2021', 'DD-MON-YYYY'));

INSERT INTO employees (full_name, designation, department, email, join_date)
VALUES ('Elena Kovic', 'Head of Dept', 'Mathematics', 'e.kovic@upms.edu', TO_DATE('05-JAN-2019', 'DD-MON-YYYY'));

INSERT INTO employees (full_name, designation, department, email, join_date)
VALUES ('Marcus Webb', 'Associate Professor', 'Social Sciences', 'm.webb@upms.edu', TO_DATE('22-AUG-2022', 'DD-MON-YYYY'));

COMMIT;
-- ====================================================================
-- END OF SCRIPT
-- ====================================================================