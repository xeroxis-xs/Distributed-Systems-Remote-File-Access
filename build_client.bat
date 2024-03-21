@echo off
cls

rem Compile C++ files
g++ -o client\Main.exe client\Client.cpp client\Handler.cpp utils\Marshaller.cpp utils\UserInputReader.cpp utils\ConsoleUI.cpp client\Main.cpp -lws2_32

rem Check if compilation was successful
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed.
    exit /b %ERRORLEVEL%
)

rem Run the compiled executable
client\Main.exe %*
