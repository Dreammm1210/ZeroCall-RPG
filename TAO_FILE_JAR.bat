@echo off
setlocal
cd /d "%~dp0"
title ZERO CALL RPG v0.2.0 - Fabric 1.21.11
echo =========================================
echo ZERO CALL RPG v0.2.0 - BUILD .JAR
 echo 8 CLASS - BUFF - QUAI - DUNGEON, NO QUESTS
 echo =========================================
echo Can Windows + JDK 21 + Internet
where java >nul 2>&1 || goto :fail
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-mod.ps1"
if errorlevel 1 goto :fail
set "MODJAR=build\libs\zerocall-rpg-fabric-1.21.11-0.2.0.jar"
if not exist "%MODJAR%" goto :fail
if not exist JAR_DE_CAI mkdir JAR_DE_CAI
copy /Y "%MODJAR%" "JAR_DE_CAI\" >nul
if errorlevel 1 goto :fail
echo.
echo THANH CONG: JAR_DE_CAI\zerocall-rpg-fabric-1.21.11-0.2.0.jar
echo Khong dung file -sources.jar
pause
exit /b 0
:fail
echo BUILD THAT BAI. Chup lai log loi va gui de sua.
pause
exit /b 1
