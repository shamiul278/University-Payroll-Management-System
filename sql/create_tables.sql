-- University Payroll Management System
-- Table Creation Script for Oracle 10g

-- Drop tables in reverse dependency order (if they exist)
BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE Payroll_Deduction CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE Payroll_Bonus CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE Deduction CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE Bonus CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE Payroll CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE Attendance CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE Salary CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE Users CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE Employee CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE Department CASCADE CONSTRAINTS';
EXCEPTION WHEN OTHERS THEN NULL; END;
/

-- 1. Department Table
CREATE TABLE Department (
    dept_id     VARCHAR2(10) PRIMARY KEY,
    dept_name   VARCHAR2(100) NOT NULL,
    building    VARCHAR2(100),
    contact_no  VARCHAR2(15)
);

-- 2. Employee Table
CREATE TABLE Employee (
    emp_id          VARCHAR2(10) PRIMARY KEY,
    name            VARCHAR2(100) NOT NULL,
    designation     VARCHAR2(100),
    email           VARCHAR2(100) UNIQUE,
    phone           VARCHAR2(15),
    join_date       DATE,
    employment_type VARCHAR2(20) CHECK (employment_type IN ('Full-Time', 'Part-Time', 'Contractual')),
    dept_id         VARCHAR2(10),
    CONSTRAINT fk_emp_dept FOREIGN KEY (dept_id) REFERENCES Department(dept_id)
);

-- 3. Users Table
CREATE TABLE Users (
    user_id  VARCHAR2(10) PRIMARY KEY,
    username VARCHAR2(50) NOT NULL UNIQUE,
    password VARCHAR2(100) NOT NULL,
    role     VARCHAR2(20) CHECK (role IN ('Administrator', 'Accountant')),
    emp_id   VARCHAR2(10),
    CONSTRAINT fk_user_emp FOREIGN KEY (emp_id) REFERENCES Employee(emp_id)
);

-- 4. Salary Table
CREATE TABLE Salary (
    salary_id    VARCHAR2(10) PRIMARY KEY,
    basic_salary NUMBER(10, 2) NOT NULL,
    allowance    NUMBER(10, 2) DEFAULT 0,
    emp_id       VARCHAR2(10),
    CONSTRAINT fk_salary_emp FOREIGN KEY (emp_id) REFERENCES Employee(emp_id)
);

-- 5. Attendance Table
CREATE TABLE Attendance (
    attendance_id VARCHAR2(10) PRIMARY KEY,
    date_         DATE NOT NULL,
    status        VARCHAR2(10) CHECK (status IN ('Present', 'Absent', 'Leave')),
    emp_id        VARCHAR2(10),
    CONSTRAINT fk_attend_emp FOREIGN KEY (emp_id) REFERENCES Employee(emp_id)
);

-- 6. Payroll Table
CREATE TABLE Payroll (
    payroll_id     VARCHAR2(10) PRIMARY KEY,
    month_         VARCHAR2(20) NOT NULL,
    generated_date DATE NOT NULL,
    payment_status VARCHAR2(20) CHECK (payment_status IN ('Pending', 'Processed', 'Disbursed')),
    total_salary   NUMBER(10, 2),
    emp_id         VARCHAR2(10),
    CONSTRAINT fk_payroll_emp FOREIGN KEY (emp_id) REFERENCES Employee(emp_id)
);

-- 7. Bonus Table
CREATE TABLE Bonus (
    bonus_id VARCHAR2(10) PRIMARY KEY,
    amount   NUMBER(10, 2) NOT NULL,
    type     VARCHAR2(50) CHECK (type IN ('Performance', 'Festival'))
);

-- 8. Deduction Table
CREATE TABLE Deduction (
    deduction_id VARCHAR2(10) PRIMARY KEY,
    amount       NUMBER(10, 2) NOT NULL,
    reason       VARCHAR2(100) CHECK (reason IN ('Tax', 'Loan Repayment', 'Absence Penalty'))
);

-- 9. Payroll_Bonus Junction Table
CREATE TABLE Payroll_Bonus (
    payroll_id VARCHAR2(10),
    bonus_id   VARCHAR2(10),
    CONSTRAINT pk_payroll_bonus PRIMARY KEY (payroll_id, bonus_id),
    CONSTRAINT fk_pb_payroll FOREIGN KEY (payroll_id) REFERENCES Payroll(payroll_id),
    CONSTRAINT fk_pb_bonus   FOREIGN KEY (bonus_id)   REFERENCES Bonus(bonus_id)
);

-- 10. Payroll_Deduction Junction Table
CREATE TABLE Payroll_Deduction (
    payroll_id   VARCHAR2(10),
    deduction_id VARCHAR2(10),
    CONSTRAINT pk_payroll_deduction PRIMARY KEY (payroll_id, deduction_id),
    CONSTRAINT fk_pd_payroll    FOREIGN KEY (payroll_id)   REFERENCES Payroll(payroll_id),
    CONSTRAINT fk_pd_deduction  FOREIGN KEY (deduction_id) REFERENCES Deduction(deduction_id)
);

COMMIT;
