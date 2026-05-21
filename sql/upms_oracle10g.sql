-- --------------------------------------------------------
-- Tables
-- --------------------------------------------------------

CREATE TABLE attendance (
  attendance_id VARCHAR2(10) NOT NULL,
  date_         DATE NOT NULL,
  status        VARCHAR2(10) DEFAULT NULL,
  emp_id        VARCHAR2(10) DEFAULT NULL,
  CONSTRAINT pk_attendance PRIMARY KEY (attendance_id),
  CONSTRAINT chk_attendance_status CHECK (status IN ('Present', 'Absent', 'Leave'))
);

CREATE TABLE bonus (
  bonus_id VARCHAR2(10) NOT NULL,
  amount   NUMBER(10,2) NOT NULL,
  "TYPE"   VARCHAR2(50) DEFAULT NULL,
  CONSTRAINT pk_bonus PRIMARY KEY (bonus_id),
  CONSTRAINT chk_bonus_type CHECK ("TYPE" IN ('Performance', 'Festival'))
);

CREATE TABLE deduction (
  deduction_id VARCHAR2(10) NOT NULL,
  amount       NUMBER(10,2) NOT NULL,
  reason       VARCHAR2(100) DEFAULT NULL,
  CONSTRAINT pk_deduction PRIMARY KEY (deduction_id),
  CONSTRAINT chk_deduction_reason CHECK (reason IN ('Tax', 'Loan Repayment', 'Absence Penalty'))
);

CREATE TABLE department (
  dept_id    VARCHAR2(10) NOT NULL,
  dept_name  VARCHAR2(100) NOT NULL,
  building   VARCHAR2(100) DEFAULT NULL,
  contact_no VARCHAR2(15) DEFAULT NULL,
  CONSTRAINT pk_department PRIMARY KEY (dept_id)
);

CREATE TABLE employee (
  emp_id          VARCHAR2(10) NOT NULL,
  name            VARCHAR2(100) NOT NULL,
  designation     VARCHAR2(100) DEFAULT NULL,
  email           VARCHAR2(100) NOT NULL,
  phone           VARCHAR2(15) DEFAULT NULL,
  join_date       DATE DEFAULT NULL,
  employment_type VARCHAR2(20) DEFAULT NULL,
  dept_id         VARCHAR2(10) DEFAULT NULL,
  CONSTRAINT pk_employee PRIMARY KEY (emp_id),
  CONSTRAINT uq_emp_email UNIQUE (email),
  CONSTRAINT chk_employee_type CHECK (employment_type IN ('Full-Time', 'Part-Time', 'Contractual'))
);

CREATE TABLE payroll (
  payroll_id     VARCHAR2(10) NOT NULL,
  month_         VARCHAR2(20) NOT NULL,
  generated_date DATE NOT NULL,
  payment_status VARCHAR2(20) DEFAULT 'Pending' NOT NULL,
  total_salary   NUMBER(10,2) DEFAULT 0.00 NOT NULL,
  emp_id         VARCHAR2(10) DEFAULT NULL,
  CONSTRAINT pk_payroll PRIMARY KEY (payroll_id),
  CONSTRAINT chk_payment_status CHECK (payment_status IN ('Pending', 'Processed', 'Disbursed'))
);

CREATE TABLE payroll_bonus (
  payroll_id VARCHAR2(10) NOT NULL,
  bonus_id   VARCHAR2(10) NOT NULL,
  CONSTRAINT pk_payroll_bonus PRIMARY KEY (payroll_id, bonus_id)
);

CREATE TABLE payroll_deduction (
  payroll_id   VARCHAR2(10) NOT NULL,
  deduction_id VARCHAR2(10) NOT NULL,
  CONSTRAINT pk_payroll_deduction PRIMARY KEY (payroll_id, deduction_id)
);

CREATE TABLE salary (
  salary_id    VARCHAR2(10) NOT NULL,
  basic_salary NUMBER(10,2) NOT NULL,
  allowance    NUMBER(10,2) DEFAULT 0.00 NOT NULL,
  emp_id       VARCHAR2(10) DEFAULT NULL,
  CONSTRAINT pk_salary PRIMARY KEY (salary_id),
  CONSTRAINT uq_salary_emp UNIQUE (emp_id)
);

CREATE TABLE users (
  user_id  VARCHAR2(10) NOT NULL,
  username VARCHAR2(50) NOT NULL,
  password VARCHAR2(100) NOT NULL,
  role     VARCHAR2(20) DEFAULT NULL,
  emp_id   VARCHAR2(10) DEFAULT NULL,
  CONSTRAINT pk_users PRIMARY KEY (user_id),
  CONSTRAINT uq_username UNIQUE (username),
  CONSTRAINT chk_user_role CHECK (role IN ('Administrator', 'Accountant'))
);

-- --------------------------------------------------------
-- Data
-- --------------------------------------------------------

INSERT INTO department (dept_id, dept_name, building, contact_no)
VALUES ('D01', 'CSE', 'Annex 1', '01700000001');
INSERT INTO department (dept_id, dept_name, building, contact_no)
VALUES ('D02', 'EEE', 'Annex 2', '01700000002');
INSERT INTO department (dept_id, dept_name, building, contact_no)
VALUES ('D03', 'IPE', 'Annex 3', '01700000003');
INSERT INTO department (dept_id, dept_name, building, contact_no)
VALUES ('D04', 'LLB', 'Annex 4', '01700000004');
INSERT INTO department (dept_id, dept_name, building, contact_no)
VALUES ('D05', 'sadsa', 'dasdsa', '5156156');

INSERT INTO employee (emp_id, name, designation, email, phone, join_date, employment_type, dept_id)
VALUES ('E01', 'Atif Al Muhit', 'Lead Developer', 'atif@aiub.edu', '01811111111', TO_DATE('2026-05-18', 'YYYY-MM-DD'), 'Full-Time', 'D01');
INSERT INTO employee (emp_id, name, designation, email, phone, join_date, employment_type, dept_id)
VALUES ('E02', 'Sakib Hasan', 'Accountant', 'sakib@aiub.edu', '01822222222', TO_DATE('2026-05-18', 'YYYY-MM-DD'), 'Full-Time', 'D02');
INSERT INTO employee (emp_id, name, designation, email, phone, join_date, employment_type, dept_id)
VALUES ('E03', 'Sadman Sakib', 'Software Engineer', 'sadman@aiub.edu', '01833333333', TO_DATE('2026-05-18', 'YYYY-MM-DD'), 'Full-Time', 'D01');
INSERT INTO employee (emp_id, name, designation, email, phone, join_date, employment_type, dept_id)
VALUES ('E04', 'Shamiul Islam', 'Software Engineer', 'shamiul746@aiub.edu', '01844444444', TO_DATE('2026-05-18', 'YYYY-MM-DD'), 'Full-Time', 'D01');
INSERT INTO employee (emp_id, name, designation, email, phone, join_date, employment_type, dept_id)
VALUES ('E05', 'MD Xion Bin Faruk', 'Software Engineer', 'faruk@aiub.edu', '0185555555', TO_DATE('2026-05-18', 'YYYY-MM-DD'), 'Full-Time', 'D01');

INSERT INTO attendance (attendance_id, date_, status, emp_id)
VALUES ('A01', TO_DATE('2026-05-18', 'YYYY-MM-DD'), 'Present', 'E01');
INSERT INTO attendance (attendance_id, date_, status, emp_id)
VALUES ('A02', TO_DATE('2026-05-18', 'YYYY-MM-DD'), 'Present', 'E02');
INSERT INTO attendance (attendance_id, date_, status, emp_id)
VALUES ('A03', TO_DATE('2026-05-18', 'YYYY-MM-DD'), 'Absent', 'E03');
INSERT INTO attendance (attendance_id, date_, status, emp_id)
VALUES ('A04', TO_DATE('2026-05-18', 'YYYY-MM-DD'), 'Leave', 'E04');
INSERT INTO attendance (attendance_id, date_, status, emp_id)
VALUES ('A05', TO_DATE('2026-05-18', 'YYYY-MM-DD'), 'Present', 'E05');

INSERT INTO bonus (bonus_id, amount, "TYPE")
VALUES ('B01', 5000.00, 'Performance');
INSERT INTO bonus (bonus_id, amount, "TYPE")
VALUES ('B02', 3000.00, 'Festival');

INSERT INTO deduction (deduction_id, amount, reason)
VALUES ('DD1', 2000.00, 'Tax');
INSERT INTO deduction (deduction_id, amount, reason)
VALUES ('DD2', 1500.00, 'Absence Penalty');

INSERT INTO payroll (payroll_id, month_, generated_date, payment_status, total_salary, emp_id)
VALUES ('P01', 'April 2026', TO_DATE('2026-05-18', 'YYYY-MM-DD'), 'Pending', 63000.00, 'E01');
INSERT INTO payroll (payroll_id, month_, generated_date, payment_status, total_salary, emp_id)
VALUES ('P02', 'April 2026', TO_DATE('2026-05-18', 'YYYY-MM-DD'), 'Processed', 54500.00, 'E02');

INSERT INTO payroll_bonus (payroll_id, bonus_id)
VALUES ('P01', 'B01');
INSERT INTO payroll_bonus (payroll_id, bonus_id)
VALUES ('P02', 'B02');

INSERT INTO payroll_deduction (payroll_id, deduction_id)
VALUES ('P01', 'DD1');
INSERT INTO payroll_deduction (payroll_id, deduction_id)
VALUES ('P02', 'DD2');

INSERT INTO salary (salary_id, basic_salary, allowance, emp_id)
VALUES ('S01', 50000.00, 10000.00, 'E01');
INSERT INTO salary (salary_id, basic_salary, allowance, emp_id)
VALUES ('S02', 45000.00, 8000.00, 'E02');
INSERT INTO salary (salary_id, basic_salary, allowance, emp_id)
VALUES ('S03', 48000.00, 9000.00, 'E03');
INSERT INTO salary (salary_id, basic_salary, allowance, emp_id)
VALUES ('S04', 47000.00, 8500.00, 'E04');
INSERT INTO salary (salary_id, basic_salary, allowance, emp_id)
VALUES ('S05', 46000.00, 8000.00, 'E05');

INSERT INTO users (user_id, username, password, role, emp_id)
VALUES ('U01', 'admin', 'admin123', 'Administrator', 'E01');
INSERT INTO users (user_id, username, password, role, emp_id)
VALUES ('U02', 'accountant', 'acc123', 'Accountant', 'E02');

COMMIT;

-- --------------------------------------------------------
-- Indexes
-- --------------------------------------------------------

CREATE INDEX idx_attendance_emp ON attendance (emp_id);
CREATE INDEX idx_attendance_date ON attendance (date_);
CREATE INDEX idx_employee_dept ON employee (dept_id);
CREATE INDEX idx_payroll_emp ON payroll (emp_id);
CREATE INDEX idx_payroll_month ON payroll (month_);
CREATE INDEX idx_payroll_status ON payroll (payment_status);
CREATE INDEX idx_pb_bonus ON payroll_bonus (bonus_id);
CREATE INDEX idx_pd_deduction ON payroll_deduction (deduction_id);
CREATE INDEX idx_user_emp ON users (emp_id);

-- --------------------------------------------------------
-- Foreign key constraints
-- --------------------------------------------------------

ALTER TABLE attendance
  ADD CONSTRAINT fk_attend_emp
  FOREIGN KEY (emp_id) REFERENCES employee (emp_id) ON DELETE CASCADE;

ALTER TABLE employee
  ADD CONSTRAINT fk_emp_dept
  FOREIGN KEY (dept_id) REFERENCES department (dept_id) ON DELETE SET NULL;

ALTER TABLE payroll
  ADD CONSTRAINT fk_payroll_emp
  FOREIGN KEY (emp_id) REFERENCES employee (emp_id) ON DELETE SET NULL;

ALTER TABLE payroll_bonus
  ADD CONSTRAINT fk_pb_bonus
  FOREIGN KEY (bonus_id) REFERENCES bonus (bonus_id) ON DELETE CASCADE;

ALTER TABLE payroll_bonus
  ADD CONSTRAINT fk_pb_payroll
  FOREIGN KEY (payroll_id) REFERENCES payroll (payroll_id) ON DELETE CASCADE;

ALTER TABLE payroll_deduction
  ADD CONSTRAINT fk_pd_deduction
  FOREIGN KEY (deduction_id) REFERENCES deduction (deduction_id) ON DELETE CASCADE;

ALTER TABLE payroll_deduction
  ADD CONSTRAINT fk_pd_payroll
  FOREIGN KEY (payroll_id) REFERENCES payroll (payroll_id) ON DELETE CASCADE;

ALTER TABLE salary
  ADD CONSTRAINT fk_salary_emp
  FOREIGN KEY (emp_id) REFERENCES employee (emp_id) ON DELETE CASCADE;

ALTER TABLE users
  ADD CONSTRAINT fk_user_emp
  FOREIGN KEY (emp_id) REFERENCES employee (emp_id) ON DELETE SET NULL;

-- --------------------------------------------------------
-- Triggers
-- --------------------------------------------------------

CREATE OR REPLACE TRIGGER trg_bonus_au
AFTER UPDATE ON bonus
FOR EACH ROW
BEGIN
  UPDATE payroll p
  SET    p.total_salary = NVL(p.total_salary, 0) + (:NEW.amount - :OLD.amount)
  WHERE  EXISTS (
           SELECT 1
           FROM   payroll_bonus pb
           WHERE  pb.payroll_id = p.payroll_id
           AND    pb.bonus_id = :NEW.bonus_id
         );
END;
/
SHOW ERRORS TRIGGER trg_bonus_au;

CREATE OR REPLACE TRIGGER trg_deduction_au
AFTER UPDATE ON deduction
FOR EACH ROW
BEGIN
  UPDATE payroll p
  SET    p.total_salary = NVL(p.total_salary, 0) - (:NEW.amount - :OLD.amount)
  WHERE  EXISTS (
           SELECT 1
           FROM   payroll_deduction pd
           WHERE  pd.payroll_id = p.payroll_id
           AND    pd.deduction_id = :NEW.deduction_id
         );
END;
/
SHOW ERRORS TRIGGER trg_deduction_au;

CREATE OR REPLACE TRIGGER trg_payroll_bi
BEFORE INSERT ON payroll
FOR EACH ROW
DECLARE
  v_base NUMBER(10,2) := 0;
BEGIN
  SELECT NVL(MAX(NVL(basic_salary, 0) + NVL(allowance, 0)), 0)
  INTO   v_base
  FROM   salary
  WHERE  emp_id = :NEW.emp_id;

  :NEW.generated_date := NVL(:NEW.generated_date, TRUNC(SYSDATE));
  :NEW.payment_status := NVL(:NEW.payment_status, 'Pending');
  :NEW.total_salary   := v_base;
END;
/
SHOW ERRORS TRIGGER trg_payroll_bi;

CREATE OR REPLACE TRIGGER trg_pb_ad
AFTER DELETE ON payroll_bonus
FOR EACH ROW
BEGIN
  UPDATE payroll p
  SET    p.total_salary = NVL(p.total_salary, 0)
                          - NVL((SELECT b.amount
                                 FROM   bonus b
                                 WHERE  b.bonus_id = :OLD.bonus_id), 0)
  WHERE  p.payroll_id = :OLD.payroll_id;
END;
/
SHOW ERRORS TRIGGER trg_pb_ad;

CREATE OR REPLACE TRIGGER trg_pb_ai
AFTER INSERT ON payroll_bonus
FOR EACH ROW
BEGIN
  UPDATE payroll p
  SET    p.total_salary = NVL(p.total_salary, 0)
                          + NVL((SELECT b.amount
                                 FROM   bonus b
                                 WHERE  b.bonus_id = :NEW.bonus_id), 0)
  WHERE  p.payroll_id = :NEW.payroll_id;
END;
/
SHOW ERRORS TRIGGER trg_pb_ai;

CREATE OR REPLACE TRIGGER trg_pd_ad
AFTER DELETE ON payroll_deduction
FOR EACH ROW
BEGIN
  UPDATE payroll p
  SET    p.total_salary = NVL(p.total_salary, 0)
                          + NVL((SELECT d.amount
                                 FROM   deduction d
                                 WHERE  d.deduction_id = :OLD.deduction_id), 0)
  WHERE  p.payroll_id = :OLD.payroll_id;
END;
/
SHOW ERRORS TRIGGER trg_pd_ad;

CREATE OR REPLACE TRIGGER trg_pd_ai
AFTER INSERT ON payroll_deduction
FOR EACH ROW
BEGIN
  UPDATE payroll p
  SET    p.total_salary = NVL(p.total_salary, 0)
                          - NVL((SELECT d.amount
                                 FROM   deduction d
                                 WHERE  d.deduction_id = :NEW.deduction_id), 0)
  WHERE  p.payroll_id = :NEW.payroll_id;
END;
/
SHOW ERRORS TRIGGER trg_pd_ai;

CREATE OR REPLACE TRIGGER trg_salary_ad
AFTER DELETE ON salary
FOR EACH ROW
BEGIN
  UPDATE payroll
  SET    total_salary = NVL(total_salary, 0)
                        - (NVL(:OLD.basic_salary, 0) + NVL(:OLD.allowance, 0))
  WHERE  emp_id = :OLD.emp_id;
END;
/
SHOW ERRORS TRIGGER trg_salary_ad;

CREATE OR REPLACE TRIGGER trg_salary_au
AFTER UPDATE ON salary
FOR EACH ROW
BEGIN
  IF NVL(:NEW.emp_id, '#NULL#') = NVL(:OLD.emp_id, '#NULL#') THEN
    UPDATE payroll
    SET    total_salary = NVL(total_salary, 0)
                          + (NVL(:NEW.basic_salary, 0) + NVL(:NEW.allowance, 0))
                          - (NVL(:OLD.basic_salary, 0) + NVL(:OLD.allowance, 0))
    WHERE  emp_id = :NEW.emp_id;
  ELSE
    UPDATE payroll
    SET    total_salary = NVL(total_salary, 0)
                          - (NVL(:OLD.basic_salary, 0) + NVL(:OLD.allowance, 0))
    WHERE  emp_id = :OLD.emp_id;

    UPDATE payroll
    SET    total_salary = NVL(total_salary, 0)
                          + (NVL(:NEW.basic_salary, 0) + NVL(:NEW.allowance, 0))
    WHERE  emp_id = :NEW.emp_id;
  END IF;
END;
/
SHOW ERRORS TRIGGER trg_salary_au;

-- --------------------------------------------------------
-- Procedures
-- --------------------------------------------------------

CREATE OR REPLACE PROCEDURE sp_generate_payroll (
  p_payroll_id    IN  VARCHAR2,
  p_month         IN  VARCHAR2,
  p_emp_id        IN  VARCHAR2,
  p_bonus_id      IN  VARCHAR2,
  p_deduction_id  IN  VARCHAR2,
  p_total_salary  OUT NUMBER
) AS
  v_salary_count NUMBER := 0;
BEGIN
  SELECT COUNT(*)
  INTO   v_salary_count
  FROM   salary
  WHERE  emp_id = p_emp_id;

  IF v_salary_count = 0 THEN
    RAISE_APPLICATION_ERROR(-20001, 'Cannot generate payroll: employee salary is not configured');
  END IF;

  -- trg_payroll_bi sets the base salary total.
  INSERT INTO payroll (payroll_id, month_, generated_date, payment_status, total_salary, emp_id)
  VALUES (p_payroll_id, p_month, TRUNC(SYSDATE), 'Pending', 0, p_emp_id);

  -- trg_pb_ai adds the bonus amount.
  IF p_bonus_id IS NOT NULL THEN
    INSERT INTO payroll_bonus (payroll_id, bonus_id)
    VALUES (p_payroll_id, p_bonus_id);
  END IF;

  -- trg_pd_ai subtracts the deduction amount.
  IF p_deduction_id IS NOT NULL THEN
    INSERT INTO payroll_deduction (payroll_id, deduction_id)
    VALUES (p_payroll_id, p_deduction_id);
  END IF;

  SELECT total_salary
  INTO   p_total_salary
  FROM   payroll
  WHERE  payroll_id = p_payroll_id;
END;
/
SHOW ERRORS PROCEDURE sp_generate_payroll;

CREATE OR REPLACE PROCEDURE sp_recalculate_payroll_total (
  p_payroll_id IN VARCHAR2
) AS
BEGIN
  UPDATE payroll p
  SET    p.total_salary =
           NVL((
             SELECT s.basic_salary + s.allowance
             FROM   salary s
             WHERE  s.emp_id = p.emp_id
           ), 0)
         + NVL((
             SELECT SUM(b.amount)
             FROM   payroll_bonus pb, bonus b
             WHERE  b.bonus_id = pb.bonus_id
             AND    pb.payroll_id = p.payroll_id
           ), 0)
         - NVL((
             SELECT SUM(d.amount)
             FROM   payroll_deduction pd, deduction d
             WHERE  d.deduction_id = pd.deduction_id
             AND    pd.payroll_id = p.payroll_id
           ), 0)
  WHERE  p.payroll_id = p_payroll_id;
END;
/
SHOW ERRORS PROCEDURE sp_recalculate_payroll_total;

COMMIT;
