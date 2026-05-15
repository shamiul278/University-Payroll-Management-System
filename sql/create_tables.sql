-- University Payroll Management System
-- MySQL / XAMPP Version

-- Create and use the database
CREATE DATABASE IF NOT EXISTS upms;
USE upms;

-- Drop tables in reverse dependency order
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

-- 1. Department
CREATE TABLE Department (
    dept_id    VARCHAR(10)  PRIMARY KEY,
    dept_name  VARCHAR(100) NOT NULL,
    building   VARCHAR(100),
    contact_no VARCHAR(15)
);

-- 2. Employee
CREATE TABLE Employee (
    emp_id          VARCHAR(10)  PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    designation     VARCHAR(100),
    email           VARCHAR(100) UNIQUE,
    phone           VARCHAR(15),
    join_date       DATE,
    employment_type VARCHAR(20)  CHECK (employment_type IN ('Full-Time','Part-Time','Contractual')),
    dept_id         VARCHAR(10),
    CONSTRAINT fk_emp_dept FOREIGN KEY (dept_id) REFERENCES Department(dept_id)
);

-- 3. Users
CREATE TABLE Users (
    user_id  VARCHAR(10)  PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role     VARCHAR(20)  CHECK (role IN ('Administrator','Accountant')),
    emp_id   VARCHAR(10),
    CONSTRAINT fk_user_emp FOREIGN KEY (emp_id) REFERENCES Employee(emp_id)
);

-- 4. Salary
CREATE TABLE Salary (
    salary_id    VARCHAR(10)    PRIMARY KEY,
    basic_salary DECIMAL(10,2)  NOT NULL,
    allowance    DECIMAL(10,2)  DEFAULT 0,
    emp_id       VARCHAR(10),
    CONSTRAINT fk_salary_emp FOREIGN KEY (emp_id) REFERENCES Employee(emp_id)
);

-- 5. Attendance  (date_ avoids MySQL reserved word DATE)
CREATE TABLE Attendance (
    attendance_id VARCHAR(10) PRIMARY KEY,
    date_         DATE        NOT NULL,
    status        VARCHAR(10) CHECK (status IN ('Present','Absent','Leave')),
    emp_id        VARCHAR(10),
    CONSTRAINT fk_attend_emp FOREIGN KEY (emp_id) REFERENCES Employee(emp_id)
);

-- 6. Payroll  (month_ avoids MySQL function name MONTH)
CREATE TABLE Payroll (
    payroll_id     VARCHAR(10)   PRIMARY KEY,
    month_         VARCHAR(20)   NOT NULL,
    generated_date DATE          NOT NULL,
    payment_status VARCHAR(20)   CHECK (payment_status IN ('Pending','Processed','Disbursed')),
    total_salary   DECIMAL(10,2),
    emp_id         VARCHAR(10),
    CONSTRAINT fk_payroll_emp FOREIGN KEY (emp_id) REFERENCES Employee(emp_id)
);

-- 7. Bonus
CREATE TABLE Bonus (
    bonus_id VARCHAR(10)   PRIMARY KEY,
    amount   DECIMAL(10,2) NOT NULL,
    type     VARCHAR(50)   CHECK (type IN ('Performance','Festival'))
);

-- 8. Deduction
CREATE TABLE Deduction (
    deduction_id VARCHAR(10)   PRIMARY KEY,
    amount       DECIMAL(10,2) NOT NULL,
    reason       VARCHAR(100)  CHECK (reason IN ('Tax','Loan Repayment','Absence Penalty'))
);

-- 9. Payroll_Bonus
CREATE TABLE Payroll_Bonus (
    payroll_id VARCHAR(10),
    bonus_id   VARCHAR(10),
    CONSTRAINT pk_payroll_bonus PRIMARY KEY (payroll_id, bonus_id),
    CONSTRAINT fk_pb_payroll FOREIGN KEY (payroll_id) REFERENCES Payroll(payroll_id),
    CONSTRAINT fk_pb_bonus   FOREIGN KEY (bonus_id)   REFERENCES Bonus(bonus_id)
);

-- 10. Payroll_Deduction
CREATE TABLE Payroll_Deduction (
    payroll_id   VARCHAR(10),
    deduction_id VARCHAR(10),
    CONSTRAINT pk_payroll_deduction PRIMARY KEY (payroll_id, deduction_id),
    CONSTRAINT fk_pd_payroll   FOREIGN KEY (payroll_id)   REFERENCES Payroll(payroll_id),
    CONSTRAINT fk_pd_deduction FOREIGN KEY (deduction_id) REFERENCES Deduction(deduction_id)
);
