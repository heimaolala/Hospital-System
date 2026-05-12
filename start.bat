@echo off
chcp 65001 >nul 2>&1
title Hospital System - Starting...

echo ============================================
echo   Hospital Registration System
echo ============================================
echo.

:: Check MySQL
echo [1/4] Checking MySQL...
mysqladmin -u root -p123456 ping >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARN] MySQL not running, trying to start...
    net start MySQL80 >nul 2>&1
    if %errorlevel% neq 0 (
        echo [ERROR] Cannot start MySQL. Please start it manually.
        pause
        exit /b 1
    )
)
echo [OK] MySQL is ready

:: Init database
echo [2/4] Initializing database...
mysql -u root -p123456 < "%~dp0sql\init.sql" 2>nul
echo [OK] Database ready

:: Start backend
echo [3/4] Starting backend (port 8080)...
set JAVA_HOME=%USERPROFILE%\.jdks\jdk-21
start "Hospital-Backend" cmd /c "cd /d %~dp0hospital-server && %USERPROFILE%\.m2\apache-maven-3.9.8\bin\mvn spring-boot:run -DskipTests"

:: Wait for backend
echo Waiting for backend to start...
:wait_backend
timeout /t 2 /nobreak >nul
curl -s http://localhost:8080/api/health >nul 2>&1
if %errorlevel% neq 0 goto wait_backend
echo [OK] Backend started

:: Start frontend
echo [4/4] Starting frontend (port 5173)...
start "Hospital-Frontend" cmd /c "cd /d %~dp0hospital-web && npm run dev"

:: Wait for frontend
echo Waiting for frontend to start...
:wait_frontend
timeout /t 2 /nobreak >nul
curl -s http://localhost:5173 >nul 2>&1
if %errorlevel% neq 0 goto wait_frontend
echo [OK] Frontend started

:: Read admin password from database
for /f "tokens=*" %%i in ('mysql -u root -p123456 hospital_system -N -B -e "SELECT password_hash FROM users WHERE id_card='admin'" 2^>nul') do set ADMIN_PWD=%%i
if "%ADMIN_PWD%"=="" set ADMIN_PWD=admin

:: Done
echo.
echo ============================================
echo   All services started!
echo.
echo   Frontend: http://localhost:5173
echo   Backend:  http://localhost:8080
echo.
echo   Admin account: admin
echo   Admin password: %ADMIN_PWD%
echo.
echo   First login will force password change.
echo ============================================
start http://localhost:5173

echo.
echo Press any key to close this window (services keep running)...
pause >nul
