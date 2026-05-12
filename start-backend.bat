@echo off
set JAVA_HOME=%USERPROFILE%\.jdks\jdk-21
set MAVEN_HOME=%USERPROFILE%\.m2\apache-maven-3.9.8
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo.
echo === Hospital Registration System ===
echo.
echo Starting backend server...
echo.
cd /d "%~dp0hospital-server"
call mvn spring-boot:run -DskipTests
pause
