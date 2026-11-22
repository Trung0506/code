@echo off
chcp 65001 >nul
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
dir /s /b src\*.java > sources.txt
javac -source 8 -target 8 -encoding UTF-8 -d out @sources.txt
if errorlevel 1 exit /b 1
echo Main-Class: main.Main> manifest.txt
jar cfm LibraryApp.jar manifest.txt -C out .
del sources.txt manifest.txt
echo Build xong: LibraryApp.jar
