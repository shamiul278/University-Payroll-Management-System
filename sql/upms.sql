-- University Payroll Management System
-- Oracle 10g schema and sample data
--
-- Run this script while connected as:
--   Project / 12345678
--
-- Example:
--   sqlplus Project/12345678@XE @sql\upms.sql

SET DEFINE OFF

BEGIN
    FOR obj IN (
        SELECT table_name
        FROM user_tables
        WHERE table_name IN (
            'PAYROLL_DEDUCTION', 'PAYROLL_BONUS', 'USERS', 'PAYROLL',
            'SALARY', 'ATTENDANCE', 'EMPLOYEE', 'DEPARTMENT',
            'DEDUCTION', 'BONUS'
        )
    ) LOOP
        EXECUTE IMMEDIATE 'DROP TABLE ' || obj.table_name || ' CASCADE CONSTRAINTS PURGE';
    END LOOP;
END;
/

BEGIN
    FOR obj IN (
        SELECT object_name
        FROM user_objects
        WHERE object_type = 'PROCEDURE'
          AND object_name IN ('SP_GENERATE_PAYROLL', 'SP_RECALCULATE_PAYROLL_TOTAL')
    ) LOOP
        EXECUTE IMMEDIATE 'DROP PROCEDURE ' || obj.object_name;
    END LOOP;
END;
/

CREATE TABLE Department (
    dept_id    VARCHAR2(10)  NOT NULL,
    dept_name  VARCHAR2(100) NOT NULL,
    building   VARCHAR2(100),
    contact_no VARCHAR2(15),
    CONSTRAINT pk_department PRIMARY KEY (dept_id)
);

CREATE TABLE Employee (
    emp_id          VARCHAR2(10)  NOT NULL,
    name            VARCHAR2(100) NOT NULL,
    designation     VARCHAR2(100),
    email           VARCHAR2(100) NOT NULL,
    phone           VARCHAR2(15),
    join_date       DATE,
    employment_type VARCHAR2(20),
    dept_id         VARCHAR2(10),
    CONSTRAINT pk_employee PRIMARY KEY (emp_id),
    CONSTRAINT uq_emp_email UNIQUE (email),
    CONSTRAINT chk_emp_type CHECK (employment_type IN ('Full-Time', 'Part-Time', 'Contractual')),
    CONSTRAINT fk_emp_dept FOREIGN KEY (dept_id)
        REFERENCES Department (dept_id) ON DELETE SET NULL
);

CREATE TABLE Attendance (
    attendance_id VARCHAR2(10) NOT NULL,
    date_         DATE         NOT NULL,
    status        VARCHAR2(10),
    emp_id        VARCHAR2(10),
    CONSTRAINT pk_attendance PRIMARY KEY (attendance_id),
    CONSTRAINT chk_att_status CHECK (status IN ('Present', 'Absent', 'Leave')),
    CONSTRAINT fk_attend_emp FOREIGN KEY (emp_id)
        REFERENCES Employee (emp_id) ON DELETE CASCADE
);

CREATE TABLE Bonus (
    bonus_id   VARCHAR2(10) NOT NULL,
    amount     NUMBER(10,2) NOT NULL,
    bonus_type VARCHAR2(50),
    CONSTRAINT pk_bonus PRIMARY KEY (bonus_id),
    CONSTRAINT chk_bonus_type CHECK (bonus_type IN ('Performance', 'Festival'))
);

CREATE TABLE Deduction (
    deduction_id VARCHAR2(10)  NOT NULL,
    amount       NUMBER(10,2)  NOT NULL,
    reason       VARCHAR2(100),
    CONSTRAINT pk_deduction PRIMARY KEY (deduction_id),
    CONSTRAINT chk_deduction_reason CHECK (reason IN ('Tax', 'Loan Repayment', 'Absence Penalty'))
);

CREATE TABLE Salary (
    salary_id    VARCHAR2(10) NOT NULL,
    basic_salary NUMBER(10,2) NOT NULL,
    allowance    NUMBER(10,2) DEFAULT 0 NOT NULL,
    emp_id       VARCHAR2(10),
    CONSTRAINT pk_salary PRIMARY KEY (salary_id),
    CONSTRAINT uq_salary_emp UNIQUE (emp_id),
    CONSTRAINT fk_salary_emp FOREIGN KEY (emp_id)
        REFERENCES Employee (emp_id) ON DELETE CASCADE
);

CREATE TABLE Payroll (
    payroll_id     VARCHAR2(10) NOT NULL,
    month_         VARCHAR2(20) NOT NULL,
    generated_date DATE         NOT NULL,
    payment_status VARCHAR2(20) DEFAULT 'Pending' NOT NULL,
    total_salary   NUMBER(10,2) DEFAULT 0 NOT NULL,
    emp_id         VARCHAR2(10),
    CONSTRAINT pk_payroll PRIMARY KEY (payroll_id),
    CONSTRAINT chk_payment_status CHECK (payment_status IN ('Pending', 'Processed', 'Disbursed')),
    CONSTRAINT fk_payroll_emp FOREIGN KEY (emp_id)
        REFERENCES Employee (emp_id) ON DELETE SET NULL
);

CREATE TABLE Payroll_Bonus (
    payroll_id VARCHAR2(10) NOT NULL,
    bonus_id   VARCHAR2(10) NOT NULL,
    CONSTRAINT pk_payroll_bonus PRIMARY KEY (payroll_id, bonus_id),
    CONSTRAINT fk_pb_payroll FOREIGN KEY (payroll_id)
        REFERENCES Payroll (payroll_id) ON DELETE CASCADE,
    CONSTRAINT fk_pb_bonus FOREIGN KEY (bonus_id)
        REFERENCES Bonus (bonus_id) ON DELETE CASCADE
);

CREATE TABLE Payroll_Deduction (
    payroll_id   VARCHAR2(10) NOT NULL,
    deduction_id VARCHAR2(10) NOT NULL,
    CONSTRAINT pk_payroll_deduction PRIMARY KEY (payroll_id, deduction_id),
    CONSTRAINT fk_pd_payroll FOREIGN KEY (payroll_id)
        REFERENCES Payroll (payroll_id) ON DELETE CASCADE,
    CONSTRAINT fk_pd_deduction FOREIGN KEY (deduction_id)
        REFERENCES Deduction (deduction_id) ON DELETE CASCADE
);

CREATE TABLE Users (
    user_id  VARCHAR2(10)  NOT NULL,
    username VARCHAR2(50)  NOT NULL,
    password VARCHAR2(100) NOT NULL,
    role     VARCHAR2(20),
    emp_id   VARCHAR2(10),
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_username UNIQUE (username),
    CONSTRAINT chk_user_role CHECK (role IN ('Administrator', 'Accountant')),
    CONSTRAINT fk_user_emp FOREIGN KEY (emp_id)
        REFERENCES Employee (emp_id) ON DELETE SET NULL
);

CREATE INDEX idx_attendance_emp ON Attendance (emp_id);
CREATE INDEX idx_attendance_date ON Attendance (date_);
CREATE INDEX idx_employee_dept ON Employee (dept_id);
CREATE INDEX idx_payroll_emp ON Payroll (emp_id);
CREATE INDEX idx_payroll_month ON Payroll (month_);
CREATE INDEX idx_payroll_status ON Payroll (payment_status);
CREATE INDEX idx_pb_bonus ON Payroll_Bonus (bonus_id);
CREATE INDEX idx_pd_deduction ON Payroll_Deduction (deduction_id);
CREATE INDEX idx_user_emp ON Users (emp_id);

INSERT INTO Department (dept_id, dept_name, building, contact_no) VALUES ('D01', 'CSE', 'Annex 1', '01700000001');
INSERT INTO Department (dept_id, dept_name, building, contact_no) VALUES ('D02', 'EEE', 'Annex 2', '01700000002');
INSERT INTO Department (dept_id, dept_name, building, contact_no) VALUES ('D03', 'IPE', 'Annex 3', '01700000003');
INSERT INTO Department (dept_id, dept_name, building, contact_no) VALUES ('D04', 'LLB', 'Annex 4', '01700000004');
INSERT INTO Department (dept_id, dept_name, building, contact_no) VALUES ('D05', 'sadsa', 'dasdsa', '5156156');

INSERT INTO Employee (emp_id, name, designation, email, phone, join_date, employment_type, dept_id)
VALUES ('E01', 'Atif Al Muhit', 'Lead Developer', 'atif@aiub.edu', '01811111111', DATE '2026-05-18', 'Full-Time', 'D01');
INSERT INTO Employee (emp_id, name, designation, email, phone, join_date, employment_type, dept_id)
VALUES ('E02', 'Sakib Hasan', 'Accountant', 'sakib@aiub.edu', '01822222222', DATE '2026-05-18', 'Full-Time', 'D02');
INSERT INTO Employee (emp_id, name, designation, email, phone, join_date, employment_type, dept_id)
VALUES ('E03', 'Sadman Sakib', 'Software Engineer', 'sadman@aiub.edu', '01833333333', DATE '2026-05-18', 'Full-Time', 'D01');
INSERT INTO Employee (emp_id, name, designation, email, phone, join_date, employment_type, dept_id)
VALUES ('E04', 'Shamiul Islam', 'Software Engineer', 'shamiul746@aiub.edu', '01844444444', DATE '2026-05-18', 'Full-Time', 'D01');
INSERT INTO Employee (emp_id, name, designation, email, phone, join_date, employment_type, dept_id)
VALUES ('E05', 'MD Xion Bin Faruk', 'Software Engineer', 'faruk@aiub.edu', '0185555555', DATE '2026-05-18', 'Full-Time', 'D01');

INSERT INTO Attendance (attendance_id, date_, status, emp_id) VALUES ('A01', DATE '2026-05-18', 'Present', 'E01');
INSERT INTO Attendance (attendance_id, date_, status, emp_id) VALUES ('A02', DATE '2026-05-18', 'Present', 'E02');
INSERT INTO Attendance (attendance_id, date_, status, emp_id) VALUES ('A03', DATE '2026-05-18', 'Absent', 'E03');
INSERT INTO Attendance (attendance_id, date_, status, emp_id) VALUES ('A04', DATE '2026-05-18', 'Leave', 'E04');
INSERT INTO Attendance (attendance_id, date_, status, emp_id) VALUES ('A05', DATE '2026-05-18', 'Present', 'E05');

INSERT INTO Bonus (bonus_id, amount, bonus_type) VALUES ('B01', 5000.00, 'Performance');
INSERT INTO Bonus (bonus_id, amount, bonus_type) VALUES ('B02', 3000.00, 'Festival');

INSERT INTO Deduction (deduction_id, amount, reason) VALUES ('DD1', 2000.00, 'Tax');
INSERT INTO Deduction (deduction_id, amount, reason) VALUES ('DD2', 1500.00, 'Absence Penalty');

INSERT INTO Salary (salary_id, basic_salary, allowance, emp_id) VALUES ('S01', 50000.00, 10000.00, 'E01');
INSERT INTO Salary (salary_id, basic_salary, allowance, emp_id) VALUES ('S02', 45000.00, 8000.00, 'E02');
INSERT INTO Salary (salary_id, basic_salary, allowance, emp_id) VALUES ('S03', 48000.00, 9000.00, 'E03');
INSERT INTO Salary (salary_id, basic_salary, allowance, emp_id) VALUES ('S04', 47000.00, 8500.00, 'E04');
INSERT INTO Salary (salary_id, basic_salary, allowance, emp_id) VALUES ('S05', 46000.00, 8000.00, 'E05');

INSERT INTO Payroll (payroll_id, month_, generated_date, payment_status, total_salary, emp_id)
VALUES ('P01', 'April 2026', DATE '2026-05-18', 'Pending', 63000.00, 'E01');
INSERT INTO Payroll (payroll_id, month_, generated_date, payment_status, total_salary, emp_id)
VALUES ('P02', 'April 2026', DATE '2026-05-18', 'Processed', 54500.00, 'E02');

INSERT INTO Payroll_Bonus (payroll_id, bonus_id) VALUES ('P01', 'B01');
INSERT INTO Payroll_Bonus (payroll_id, bonus_id) VALUES ('P02', 'B02');

INSERT INTO Payroll_Deduction (payroll_id, deduction_id) VALUES ('P01', 'DD1');
INSERT INTO Payroll_Deduction (payroll_id, deduction_id) VALUES ('P02', 'DD2');

INSERT INTO Users (user_id, username, password, role, emp_id) VALUES ('U01', 'admin', 'admin123', 'Administrator', 'E01');
INSERT INTO Users (user_id, username, password, role, emp_id) VALUES ('U02', 'accountant', 'acc123', 'Accountant', 'E02');

CREATE OR REPLACE PROCEDURE sp_recalculate_payroll_total (
    p_payroll_id IN VARCHAR2
) AS
BEGIN
    UPDATE Payroll p
    SET p.total_salary =
          NVL((
              SELECT s.basic_salary + s.allowance
              FROM Salary s
              WHERE s.emp_id = p.emp_id
          ), 0)
        + NVL((
              SELECT SUM(b.amount)
              FROM Payroll_Bonus pb
              JOIN Bonus b ON b.bonus_id = pb.bonus_id
              WHERE pb.payroll_id = p.payroll_id
          ), 0)
        - NVL((
              SELECT SUM(d.amount)
              FROM Payroll_Deduction pd
              JOIN Deduction d ON d.deduction_id = pd.deduction_id
              WHERE pd.payroll_id = p.payroll_id
          ), 0)
    WHERE p.payroll_id = p_payroll_id;
END;
/

CREATE OR REPLACE PROCEDURE sp_generate_payroll (
    p_payroll_id   IN  VARCHAR2,
    p_month        IN  VARCHAR2,
    p_emp_id       IN  VARCHAR2,
    p_bonus_id     IN  VARCHAR2,
    p_deduction_id IN  VARCHAR2,
    p_total_salary OUT NUMBER
) AS
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_count
    FROM Salary
    WHERE emp_id = p_emp_id;

    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Cannot generate payroll: employee salary is not configured');
    END IF;

    INSERT INTO Payroll (payroll_id, month_, generated_date, payment_status, total_salary, emp_id)
    VALUES (p_payroll_id, p_month, SYSDATE, 'Pending', 0, p_emp_id);

    IF p_bonus_id IS NOT NULL AND TRIM(p_bonus_id) IS NOT NULL THEN
        INSERT INTO Payroll_Bonus (payroll_id, bonus_id)
        VALUES (p_payroll_id, p_bonus_id);
    END IF;

    IF p_deduction_id IS NOT NULL AND TRIM(p_deduction_id) IS NOT NULL THEN
        INSERT INTO Payroll_Deduction (payroll_id, deduction_id)
        VALUES (p_payroll_id, p_deduction_id);
    END IF;

    SELECT total_salary
    INTO p_total_salary
    FROM Payroll
    WHERE payroll_id = p_payroll_id;
END;
/

CREATE OR REPLACE TRIGGER trg_payroll_bi
BEFORE INSERT ON Payroll
FOR EACH ROW
DECLARE
    v_base NUMBER(10,2) := 0;
BEGIN
    SELECT NVL(MAX(basic_salary + allowance), 0)
    INTO v_base
    FROM Salary
    WHERE emp_id = :NEW.emp_id;

    :NEW.generated_date := NVL(:NEW.generated_date, SYSDATE);
    :NEW.payment_status := NVL(:NEW.payment_status, 'Pending');
    :NEW.total_salary := v_base;
END;
/

CREATE OR REPLACE TRIGGER trg_pb_ai
AFTER INSERT ON Payroll_Bonus
FOR EACH ROW
BEGIN
    UPDATE Payroll p
    SET p.total_salary = NVL(p.total_salary, 0)
        + NVL((SELECT b.amount FROM Bonus b WHERE b.bonus_id = :NEW.bonus_id), 0)
    WHERE p.payroll_id = :NEW.payroll_id;
END;
/

CREATE OR REPLACE TRIGGER trg_pb_ad
AFTER DELETE ON Payroll_Bonus
FOR EACH ROW
BEGIN
    UPDATE Payroll p
    SET p.total_salary = NVL(p.total_salary, 0)
        - NVL((SELECT b.amount FROM Bonus b WHERE b.bonus_id = :OLD.bonus_id), 0)
    WHERE p.payroll_id = :OLD.payroll_id;
END;
/

CREATE OR REPLACE TRIGGER trg_pd_ai
AFTER INSERT ON Payroll_Deduction
FOR EACH ROW
BEGIN
    UPDATE Payroll p
    SET p.total_salary = NVL(p.total_salary, 0)
        - NVL((SELECT d.amount FROM Deduction d WHERE d.deduction_id = :NEW.deduction_id), 0)
    WHERE p.payroll_id = :NEW.payroll_id;
END;
/

CREATE OR REPLACE TRIGGER trg_pd_ad
AFTER DELETE ON Payroll_Deduction
FOR EACH ROW
BEGIN
    UPDATE Payroll p
    SET p.total_salary = NVL(p.total_salary, 0)
        + NVL((SELECT d.amount FROM Deduction d WHERE d.deduction_id = :OLD.deduction_id), 0)
    WHERE p.payroll_id = :OLD.payroll_id;
END;
/

CREATE OR REPLACE TRIGGER trg_bonus_au
AFTER UPDATE ON Bonus
FOR EACH ROW
BEGIN
    UPDATE Payroll p
    SET p.total_salary = NVL(p.total_salary, 0) + (:NEW.amount - :OLD.amount)
    WHERE EXISTS (
        SELECT 1
        FROM Payroll_Bonus pb
        WHERE pb.payroll_id = p.payroll_id
          AND pb.bonus_id = :NEW.bonus_id
    );
END;
/

CREATE OR REPLACE TRIGGER trg_deduction_au
AFTER UPDATE ON Deduction
FOR EACH ROW
BEGIN
    UPDATE Payroll p
    SET p.total_salary = NVL(p.total_salary, 0) - (:NEW.amount - :OLD.amount)
    WHERE EXISTS (
        SELECT 1
        FROM Payroll_Deduction pd
        WHERE pd.payroll_id = p.payroll_id
          AND pd.deduction_id = :NEW.deduction_id
    );
END;
/

CREATE OR REPLACE TRIGGER trg_salary_ad
AFTER DELETE ON Salary
FOR EACH ROW
BEGIN
    UPDATE Payroll
    SET total_salary = NVL(total_salary, 0) - (:OLD.basic_salary + :OLD.allowance)
    WHERE emp_id = :OLD.emp_id;
END;
/

CREATE OR REPLACE TRIGGER trg_salary_au
AFTER UPDATE ON Salary
FOR EACH ROW
BEGIN
    IF NVL(:NEW.emp_id, '#') = NVL(:OLD.emp_id, '#') THEN
        UPDATE Payroll
        SET total_salary = NVL(total_salary, 0)
            + (:NEW.basic_salary + :NEW.allowance)
            - (:OLD.basic_salary + :OLD.allowance)
        WHERE emp_id = :NEW.emp_id;
    ELSE
        UPDATE Payroll
        SET total_salary = NVL(total_salary, 0)
            - (:OLD.basic_salary + :OLD.allowance)
        WHERE emp_id = :OLD.emp_id;

        UPDATE Payroll
        SET total_salary = NVL(total_salary, 0)
            + (:NEW.basic_salary + :NEW.allowance)
        WHERE emp_id = :NEW.emp_id;
    END IF;
END;
/

COMMIT;

PROMPT UPMS Oracle 10g schema loaded successfully.
