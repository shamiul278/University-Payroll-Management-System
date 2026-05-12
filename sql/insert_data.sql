-- University Payroll Management System
-- Sample Data Insertion Script

-- Departments
INSERT INTO Department VALUES ('D01', 'CSE', 'Annex 1', '01700000001');
INSERT INTO Department VALUES ('D02', 'EEE', 'Annex 2', '01700000002');
INSERT INTO Department VALUES ('D03', 'IPE', 'Annex 3', '01700000003');
INSERT INTO Department VALUES ('D04', 'LLB', 'Annex 4', '01700000004');
INSERT INTO Department VALUES ('D05', 'BBA', 'Annex 5', '01700000005');

-- Employees
INSERT INTO Employee VALUES ('E01','Atif Al Muhit','Lead Developer','atif@aiub.edu','01811111111',SYSDATE,'Full-Time','D01');
INSERT INTO Employee VALUES ('E02','Sakib Hasan','Accountant','sakib@aiub.edu','01822222222',SYSDATE,'Full-Time','D02');
INSERT INTO Employee VALUES ('E03','Sadman Sakib','Software Engineer','sadman@aiub.edu','01833333333',SYSDATE,'Full-Time','D01');
INSERT INTO Employee VALUES ('E04','Shamiul Islam','Software Engineer','shamiul@aiub.edu','01844444444',SYSDATE,'Full-Time','D01');
INSERT INTO Employee VALUES ('E05','MD Xion Bin Faruk','Software Engineer','faruk@aiub.edu','01855555555',SYSDATE,'Full-Time','D01');

-- Users (Admin and Accountant)
INSERT INTO Users VALUES ('U01','admin','admin123','Administrator','E01');
INSERT INTO Users VALUES ('U02','accountant','acc123','Accountant','E02');

-- Salaries
INSERT INTO Salary VALUES ('S01', 50000, 10000, 'E01');
INSERT INTO Salary VALUES ('S02', 45000, 8000,  'E02');
INSERT INTO Salary VALUES ('S03', 48000, 9000,  'E03');
INSERT INTO Salary VALUES ('S04', 47000, 8500,  'E04');
INSERT INTO Salary VALUES ('S05', 46000, 8000,  'E05');

-- Attendance
INSERT INTO Attendance VALUES ('A01', SYSDATE, 'Present', 'E01');
INSERT INTO Attendance VALUES ('A02', SYSDATE, 'Present', 'E02');
INSERT INTO Attendance VALUES ('A03', SYSDATE, 'Absent',  'E03');
INSERT INTO Attendance VALUES ('A04', SYSDATE, 'Leave',   'E04');
INSERT INTO Attendance VALUES ('A05', SYSDATE, 'Present', 'E05');

-- Bonuses
INSERT INTO Bonus VALUES ('B01', 5000, 'Performance');
INSERT INTO Bonus VALUES ('B02', 3000, 'Festival');

-- Deductions
INSERT INTO Deduction VALUES ('DD1', 2000, 'Tax');
INSERT INTO Deduction VALUES ('DD2', 1500, 'Absence Penalty');

-- Payroll
INSERT INTO Payroll VALUES ('P01','April 2026',SYSDATE,'Pending',60000,'E01');
INSERT INTO Payroll VALUES ('P02','April 2026',SYSDATE,'Processed',52000,'E02');

-- Payroll_Bonus linkages
INSERT INTO Payroll_Bonus VALUES ('P01','B01');
INSERT INTO Payroll_Bonus VALUES ('P02','B02');

-- Payroll_Deduction linkages
INSERT INTO Payroll_Deduction VALUES ('P01','DD1');
INSERT INTO Payroll_Deduction VALUES ('P02','DD2');

COMMIT;
