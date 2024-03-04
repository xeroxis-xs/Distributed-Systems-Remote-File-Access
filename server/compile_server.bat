@echo off
echo Compiling Server.cpp...
g++ Server.cpp -o Server.exe -lWs2_32

if %errorlevel% neq 0 (
    echo Compilation failed.
    pause
    exit /b %errorlevel%
)

echo Compilation successful.
echo Running Server.exe...
Server.exe
pause