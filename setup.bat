@echo off
chcp 65001 >nul 2>&1
title Hospital System - Docker Setup

echo ============================================
echo   Hospital Registration System
echo   Docker One-Click Deploy
echo ============================================
echo.

:: Check Docker
echo [1/3] Checking Docker...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Docker not found. Please install Docker Desktop first:
    echo   https://www.docker.com/products/docker-desktop/
    echo.
    echo After installation, restart your computer and run this script again.
    pause
    exit /b 1
)
echo [OK] Docker is ready

:: Detect docker compose command
docker compose version >nul 2>&1
if %errorlevel% equ 0 (
    set COMPOSE_CMD=docker compose
) else (
    docker-compose --version >nul 2>&1
    if %errorlevel% neq 0 (
        echo [ERROR] Docker Compose not available. Please update Docker Desktop.
        pause
        exit /b 1
    )
    set COMPOSE_CMD=docker-compose
)

:: Build and start
echo [2/3] Building and starting services (first time may take 5-10 minutes)...
%COMPOSE_CMD% up -d --build
if %errorlevel% neq 0 (
    echo [ERROR] Failed to start services. Check the error messages above.
    pause
    exit /b 1
)

:: Wait for services
echo.
echo [3/3] Waiting for services to be ready...

:wait_frontend
timeout /t 3 /nobreak >nul
curl -s http://localhost >nul 2>&1
if %errorlevel% neq 0 goto wait_frontend

:: Read admin password from Docker MySQL
for /f "tokens=*" %%i in ('docker exec hospital-mysql mysql -u root -proot hospital_system -N -B -e "SELECT password_hash FROM users WHERE id_card='admin'" 2^>nul') do set ADMIN_PWD=%%i
if "%ADMIN_PWD%"=="" set ADMIN_PWD=admin

:: Done
echo.
echo ============================================
echo   Deploy complete!
echo.
echo   URL: http://localhost
echo.
echo   Admin account: admin
echo   Admin password: %ADMIN_PWD%
echo.
echo   First login will force password change.
echo ============================================
start http://localhost

echo.
echo Useful commands:
echo   docker compose logs       - View logs
echo   docker compose down       - Stop services (keep data)
echo   docker compose down -v    - Stop and clear all data
echo   docker compose restart    - Restart services
echo.
pause
