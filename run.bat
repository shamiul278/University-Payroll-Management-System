@echo off
set LIB=lib\ojdbc14.jar

if not exist "%LIB%" (
    echo Oracle JDBC driver not found at %LIB%
    echo Copy Oracle 10g ojdbc14.jar into the lib folder, then run this file again.
    pause
    exit /b 1
)

java -cp "out;%LIB%" upms.Main
