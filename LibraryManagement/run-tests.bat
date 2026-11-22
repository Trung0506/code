@echo off
chcp 65001 >nul
cd /d "%~dp0"
if exist out-test rmdir /s /q out-test
mkdir out-test
javac -encoding UTF-8 -cp out -d out-test test\ServiceTest.java test\ManagementTest.java
if errorlevel 1 exit /b 1
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp out;out-test ServiceTest
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp out;out-test ManagementTest
