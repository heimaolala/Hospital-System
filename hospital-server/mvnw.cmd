@echo off
setlocal enabledelayedexpansion

set "DIR=%~dp0"
set "MAVEN_VERSION=3.9.9"
set "DIST_DIR=%USERPROFILE%\.m2\wrapper\dists"
set "MAVEN_HOME=%DIST_DIR%\apache-maven-%MAVEN_VERSION%"
set "MAVEN_ZIP=%DIST_DIR%\maven-%MAVEN_VERSION%.zip"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo [INFO] Downloading Maven %MAVEN_VERSION% ...
    if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"
    if not exist "%MAVEN_ZIP%" (
        powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://dlcdn.apache.org/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip' -OutFile '%MAVEN_ZIP%'"
    )
    echo [INFO] Extracting Maven...
    powershell -Command "Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%DIST_DIR%' -Force"
)
set "PATH=%MAVEN_HOME%\bin;%PATH%"
call mvn -f "%DIR%pom.xml" %*
