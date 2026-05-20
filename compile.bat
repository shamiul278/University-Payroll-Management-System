@echo off
echo ============================================
echo  University Payroll Management System
echo  Build Script (Oracle 10g)
echo ============================================

set LIB=lib\ojdbc14.jar
set SRC=src
set OUT=out

if not exist "%LIB%" (
    echo WARNING: Oracle JDBC driver not found at %LIB%
    echo Copy Oracle 10g ojdbc14.jar into the lib folder before running the app.
)

if exist %OUT% (
    echo Cleaning old .class files...
    rmdir /s /q %OUT%
)
mkdir %OUT%

echo.
echo Compiling Java sources...
javac -cp "%LIB%" -d "%OUT%" -sourcepath "%SRC%" ^
  %SRC%\upms\db\DBConnection.java ^
  %SRC%\upms\model\Department.java ^
  %SRC%\upms\model\Employee.java ^
  %SRC%\upms\model\User.java ^
  %SRC%\upms\model\Salary.java ^
  %SRC%\upms\model\Attendance.java ^
  %SRC%\upms\model\Payroll.java ^
  %SRC%\upms\model\Bonus.java ^
  %SRC%\upms\model\Deduction.java ^
  %SRC%\upms\dao\DepartmentDAO.java ^
  %SRC%\upms\dao\EmployeeDAO.java ^
  %SRC%\upms\dao\UserDAO.java ^
  %SRC%\upms\dao\SalaryDAO.java ^
  %SRC%\upms\dao\AttendanceDAO.java ^
  %SRC%\upms\dao\PayrollDAO.java ^
  %SRC%\upms\dao\BonusDAO.java ^
  %SRC%\upms\dao\DeductionDAO.java ^
  %SRC%\upms\ui\util\Theme.java ^
  %SRC%\upms\ui\panels\EmployeePanel.java ^
  %SRC%\upms\ui\panels\DepartmentPanel.java ^
  %SRC%\upms\ui\panels\SalaryPanel.java ^
  %SRC%\upms\ui\panels\AttendancePanel.java ^
  %SRC%\upms\ui\panels\PayrollPanel.java ^
  %SRC%\upms\ui\panels\BonusPanel.java ^
  %SRC%\upms\ui\panels\DeductionPanel.java ^
  %SRC%\upms\ui\panels\UserMgmtPanel.java ^
  %SRC%\upms\ui\panels\ReportPanel.java ^
  %SRC%\upms\ui\LoginFrame.java ^
  %SRC%\upms\ui\AdminDashboard.java ^
  %SRC%\upms\ui\AccountantDashboard.java ^
  %SRC%\upms\Main.java

if %ERRORLEVEL% == 0 (
    echo.
    echo Compilation successful!
    echo.
    if exist "%LIB%" (
        echo Running UPMS...
        java -cp "%OUT%;%LIB%" upms.Main
    ) else (
        echo Skipping run because %LIB% is missing.
    )
) else (
    echo.
    echo Compilation FAILED. Check errors above.
    echo Make sure ojdbc14.jar is in the lib\ folder.
    pause
)
