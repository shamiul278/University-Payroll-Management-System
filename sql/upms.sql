-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 19, 2026 at 07:39 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `upms`
--

DELIMITER $$
--
-- Procedures
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_generate_payroll` (IN `p_payroll_id` VARCHAR(10), IN `p_month` VARCHAR(20), IN `p_emp_id` VARCHAR(10), IN `p_bonus_id` VARCHAR(10), IN `p_deduction_id` VARCHAR(10), OUT `p_total_salary` DECIMAL(10,2))   BEGIN
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
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_recalculate_payroll_total` (IN `p_payroll_id` VARCHAR(10))   BEGIN
    UPDATE Payroll p
    LEFT JOIN Salary s ON s.emp_id = p.emp_id
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
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `attendance`
--

CREATE TABLE `attendance` (
  `attendance_id` varchar(10) NOT NULL,
  `date_` date NOT NULL,
  `status` varchar(10) DEFAULT NULL CHECK (`status` in ('Present','Absent','Leave')),
  `emp_id` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `attendance`
--

INSERT INTO `attendance` (`attendance_id`, `date_`, `status`, `emp_id`) VALUES
('A01', '2026-05-18', 'Present', 'E01'),
('A02', '2026-05-18', 'Present', 'E02'),
('A03', '2026-05-18', 'Absent', 'E03'),
('A04', '2026-05-18', 'Leave', 'E04'),
('A05', '2026-05-18', 'Present', 'E05');

-- --------------------------------------------------------

--
-- Table structure for table `bonus`
--

CREATE TABLE `bonus` (
  `bonus_id` varchar(10) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `type` varchar(50) DEFAULT NULL CHECK (`type` in ('Performance','Festival'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bonus`
--

INSERT INTO `bonus` (`bonus_id`, `amount`, `type`) VALUES
('B01', 5000.00, 'Performance'),
('B02', 3000.00, 'Festival');

--
-- Triggers `bonus`
--
DELIMITER $$
CREATE TRIGGER `trg_bonus_after_update` AFTER UPDATE ON `bonus` FOR EACH ROW BEGIN
    UPDATE Payroll       p
    JOIN   Payroll_Bonus pb ON pb.payroll_id = p.payroll_id
    SET    p.total_salary = COALESCE(p.total_salary, 0)
                            + (NEW.amount - OLD.amount)
    WHERE  pb.bonus_id = NEW.bonus_id;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `deduction`
--

CREATE TABLE `deduction` (
  `deduction_id` varchar(10) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `reason` varchar(100) DEFAULT NULL CHECK (`reason` in ('Tax','Loan Repayment','Absence Penalty'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `deduction`
--

INSERT INTO `deduction` (`deduction_id`, `amount`, `reason`) VALUES
('DD1', 2000.00, 'Tax'),
('DD2', 1500.00, 'Absence Penalty');

--
-- Triggers `deduction`
--
DELIMITER $$
CREATE TRIGGER `trg_deduction_after_update` AFTER UPDATE ON `deduction` FOR EACH ROW BEGIN
    UPDATE Payroll           p
    JOIN   Payroll_Deduction pd ON pd.payroll_id = p.payroll_id
    SET    p.total_salary = COALESCE(p.total_salary, 0)
                            - (NEW.amount - OLD.amount)
    WHERE  pd.deduction_id = NEW.deduction_id;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `department`
--

CREATE TABLE `department` (
  `dept_id` varchar(10) NOT NULL,
  `dept_name` varchar(100) NOT NULL,
  `building` varchar(100) DEFAULT NULL,
  `contact_no` varchar(15) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `department`
--

INSERT INTO `department` (`dept_id`, `dept_name`, `building`, `contact_no`) VALUES
('D01', 'CSE', 'Annex 1', '01700000001'),
('D02', 'EEE', 'Annex 2', '01700000002'),
('D03', 'IPE', 'Annex 3', '01700000003'),
('D04', 'LLB', 'Annex 4', '01700000004'),
('D05', 'sadsa', 'dasdsa', '5156156');

-- --------------------------------------------------------

--
-- Table structure for table `employee`
--

CREATE TABLE `employee` (
  `emp_id` varchar(10) NOT NULL,
  `name` varchar(100) NOT NULL,
  `designation` varchar(100) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(15) DEFAULT NULL,
  `join_date` date DEFAULT NULL,
  `employment_type` varchar(20) DEFAULT NULL CHECK (`employment_type` in ('Full-Time','Part-Time','Contractual')),
  `dept_id` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `employee`
--

INSERT INTO `employee` (`emp_id`, `name`, `designation`, `email`, `phone`, `join_date`, `employment_type`, `dept_id`) VALUES
('E01', 'Atif Al Muhit', 'Lead Developer', 'atif@aiub.edu', '01811111111', '2026-05-18', 'Full-Time', 'D01'),
('E02', 'Sakib Hasan', 'Accountant', 'sakib@aiub.edu', '01822222222', '2026-05-18', 'Full-Time', 'D02'),
('E03', 'Sadman Sakib', 'Software Engineer', 'sadman@aiub.edu', '01833333333', '2026-05-18', 'Full-Time', 'D01'),
('E04', 'Shamiul Islam', 'Software Engineer', 'shamiul746@aiub.edu', '01844444444', '2026-05-18', 'Full-Time', 'D01'),
('E05', 'MD Xion Bin Faruk', 'Software Engineer', 'faruk@aiub.edu', '0185555555', '2026-05-18', 'Full-Time', 'D01');

-- --------------------------------------------------------

--
-- Table structure for table `payroll`
--

CREATE TABLE `payroll` (
  `payroll_id` varchar(10) NOT NULL,
  `month_` varchar(20) NOT NULL,
  `generated_date` date NOT NULL,
  `payment_status` varchar(20) NOT NULL DEFAULT 'Pending' CHECK (`payment_status` in ('Pending','Processed','Disbursed')),
  `total_salary` decimal(10,2) NOT NULL DEFAULT 0.00,
  `emp_id` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `payroll`
--

INSERT INTO `payroll` (`payroll_id`, `month_`, `generated_date`, `payment_status`, `total_salary`, `emp_id`) VALUES
('P01', 'April 2026', '2026-05-18', 'Pending', 63000.00, 'E01'),
('P02', 'April 2026', '2026-05-18', 'Processed', 54500.00, 'E02');

--
-- Triggers `payroll`
--
DELIMITER $$
CREATE TRIGGER `trg_payroll_before_insert` BEFORE INSERT ON `payroll` FOR EACH ROW BEGIN
    DECLARE v_base DECIMAL(10,2) DEFAULT 0.00;

    SELECT COALESCE(basic_salary + allowance, 0)
    INTO   v_base
    FROM   Salary
    WHERE  emp_id = NEW.emp_id
    LIMIT  1;

    SET NEW.generated_date = COALESCE(NEW.generated_date, CURDATE());
    SET NEW.payment_status = COALESCE(NEW.payment_status, 'Pending');
    SET NEW.total_salary   = v_base;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `payroll_bonus`
--

CREATE TABLE `payroll_bonus` (
  `payroll_id` varchar(10) NOT NULL,
  `bonus_id` varchar(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `payroll_bonus`
--

INSERT INTO `payroll_bonus` (`payroll_id`, `bonus_id`) VALUES
('P01', 'B01'),
('P02', 'B02');

--
-- Triggers `payroll_bonus`
--
DELIMITER $$
CREATE TRIGGER `trg_payroll_bonus_after_delete` AFTER DELETE ON `payroll_bonus` FOR EACH ROW BEGIN
    UPDATE Payroll p
    JOIN   Bonus   b ON b.bonus_id = OLD.bonus_id
    SET    p.total_salary = COALESCE(p.total_salary, 0) - b.amount
    WHERE  p.payroll_id = OLD.payroll_id;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `trg_payroll_bonus_after_insert` AFTER INSERT ON `payroll_bonus` FOR EACH ROW BEGIN
    UPDATE Payroll p
    JOIN   Bonus   b ON b.bonus_id = NEW.bonus_id
    SET    p.total_salary = COALESCE(p.total_salary, 0) + b.amount
    WHERE  p.payroll_id = NEW.payroll_id;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `payroll_deduction`
--

CREATE TABLE `payroll_deduction` (
  `payroll_id` varchar(10) NOT NULL,
  `deduction_id` varchar(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `payroll_deduction`
--

INSERT INTO `payroll_deduction` (`payroll_id`, `deduction_id`) VALUES
('P01', 'DD1'),
('P02', 'DD2');

--
-- Triggers `payroll_deduction`
--
DELIMITER $$
CREATE TRIGGER `trg_payroll_deduction_after_delete` AFTER DELETE ON `payroll_deduction` FOR EACH ROW BEGIN
    UPDATE Payroll    p
    JOIN   Deduction  d ON d.deduction_id = OLD.deduction_id
    SET    p.total_salary = COALESCE(p.total_salary, 0) + d.amount
    WHERE  p.payroll_id = OLD.payroll_id;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `trg_payroll_deduction_after_insert` AFTER INSERT ON `payroll_deduction` FOR EACH ROW BEGIN
    UPDATE Payroll    p
    JOIN   Deduction  d ON d.deduction_id = NEW.deduction_id
    SET    p.total_salary = COALESCE(p.total_salary, 0) - d.amount
    WHERE  p.payroll_id = NEW.payroll_id;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `salary`
--

CREATE TABLE `salary` (
  `salary_id` varchar(10) NOT NULL,
  `basic_salary` decimal(10,2) NOT NULL,
  `allowance` decimal(10,2) NOT NULL DEFAULT 0.00,
  `emp_id` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `salary`
--

INSERT INTO `salary` (`salary_id`, `basic_salary`, `allowance`, `emp_id`) VALUES
('S01', 50000.00, 10000.00, 'E01'),
('S02', 45000.00, 8000.00, 'E02'),
('S03', 48000.00, 9000.00, 'E03'),
('S04', 47000.00, 8500.00, 'E04'),
('S05', 46000.00, 8000.00, 'E05');

--
-- Triggers `salary`
--
DELIMITER $$
CREATE TRIGGER `trg_salary_after_delete` AFTER DELETE ON `salary` FOR EACH ROW BEGIN
    UPDATE Payroll
    SET    total_salary = COALESCE(total_salary, 0)
                          - (OLD.basic_salary + OLD.allowance)
    WHERE  emp_id = OLD.emp_id;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `trg_salary_after_update` AFTER UPDATE ON `salary` FOR EACH ROW BEGIN
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
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` varchar(10) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `role` varchar(20) DEFAULT NULL CHECK (`role` in ('Administrator','Accountant')),
  `emp_id` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `password`, `role`, `emp_id`) VALUES
('U01', 'admin', 'admin123', 'Administrator', 'E01'),
('U02', 'accountant', 'acc123', 'Accountant', 'E02');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `attendance`
--
ALTER TABLE `attendance`
  ADD PRIMARY KEY (`attendance_id`),
  ADD KEY `idx_attendance_emp` (`emp_id`),
  ADD KEY `idx_attendance_date` (`date_`);

--
-- Indexes for table `bonus`
--
ALTER TABLE `bonus`
  ADD PRIMARY KEY (`bonus_id`);

--
-- Indexes for table `deduction`
--
ALTER TABLE `deduction`
  ADD PRIMARY KEY (`deduction_id`);

--
-- Indexes for table `department`
--
ALTER TABLE `department`
  ADD PRIMARY KEY (`dept_id`);

--
-- Indexes for table `employee`
--
ALTER TABLE `employee`
  ADD PRIMARY KEY (`emp_id`),
  ADD UNIQUE KEY `uq_emp_email` (`email`),
  ADD KEY `idx_employee_dept` (`dept_id`);

--
-- Indexes for table `payroll`
--
ALTER TABLE `payroll`
  ADD PRIMARY KEY (`payroll_id`),
  ADD KEY `idx_payroll_emp` (`emp_id`),
  ADD KEY `idx_payroll_month` (`month_`),
  ADD KEY `idx_payroll_status` (`payment_status`);

--
-- Indexes for table `payroll_bonus`
--
ALTER TABLE `payroll_bonus`
  ADD PRIMARY KEY (`payroll_id`,`bonus_id`),
  ADD KEY `idx_pb_bonus` (`bonus_id`);

--
-- Indexes for table `payroll_deduction`
--
ALTER TABLE `payroll_deduction`
  ADD PRIMARY KEY (`payroll_id`,`deduction_id`),
  ADD KEY `idx_pd_deduction` (`deduction_id`);

--
-- Indexes for table `salary`
--
ALTER TABLE `salary`
  ADD PRIMARY KEY (`salary_id`),
  ADD UNIQUE KEY `uq_salary_emp` (`emp_id`),
  ADD KEY `idx_salary_emp` (`emp_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `uq_username` (`username`),
  ADD KEY `fk_user_emp` (`emp_id`);

--
-- Constraints for dumped tables
--

--
-- Constraints for table `attendance`
--
ALTER TABLE `attendance`
  ADD CONSTRAINT `fk_attend_emp` FOREIGN KEY (`emp_id`) REFERENCES `employee` (`emp_id`) ON DELETE CASCADE;

--
-- Constraints for table `employee`
--
ALTER TABLE `employee`
  ADD CONSTRAINT `fk_emp_dept` FOREIGN KEY (`dept_id`) REFERENCES `department` (`dept_id`) ON DELETE SET NULL;

--
-- Constraints for table `payroll`
--
ALTER TABLE `payroll`
  ADD CONSTRAINT `fk_payroll_emp` FOREIGN KEY (`emp_id`) REFERENCES `employee` (`emp_id`) ON DELETE SET NULL;

--
-- Constraints for table `payroll_bonus`
--
ALTER TABLE `payroll_bonus`
  ADD CONSTRAINT `fk_pb_bonus` FOREIGN KEY (`bonus_id`) REFERENCES `bonus` (`bonus_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_pb_payroll` FOREIGN KEY (`payroll_id`) REFERENCES `payroll` (`payroll_id`) ON DELETE CASCADE;

--
-- Constraints for table `payroll_deduction`
--
ALTER TABLE `payroll_deduction`
  ADD CONSTRAINT `fk_pd_deduction` FOREIGN KEY (`deduction_id`) REFERENCES `deduction` (`deduction_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_pd_payroll` FOREIGN KEY (`payroll_id`) REFERENCES `payroll` (`payroll_id`) ON DELETE CASCADE;

--
-- Constraints for table `salary`
--
ALTER TABLE `salary`
  ADD CONSTRAINT `fk_salary_emp` FOREIGN KEY (`emp_id`) REFERENCES `employee` (`emp_id`) ON DELETE CASCADE;

--
-- Constraints for table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `fk_user_emp` FOREIGN KEY (`emp_id`) REFERENCES `employee` (`emp_id`) ON DELETE SET NULL;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
