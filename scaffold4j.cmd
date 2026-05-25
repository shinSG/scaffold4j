@echo off
setlocal EnableExtensions

set "SCRIPT_DIR=%~dp0"
set "JAR_FILE=%SCRIPT_DIR%scaffold4j-cli\target\scaffold4j-cli-1.0.0-SNAPSHOT.jar"

where mvn >nul 2>nul
if errorlevel 1 (
    if exist "%JAR_FILE%" (
        echo Maven was not found in PATH. Using existing CLI jar.
        goto :runCli
    )
    echo [ERROR] Maven was not found in PATH and CLI jar does not exist.
    echo Please install Maven 3.9+ or copy scaffold4j-cli\target\scaffold4j-cli-1.0.0-SNAPSHOT.jar into this directory.
    exit /b 1
)
echo ^>^>^> Building/updating scaffold4j...
call mvn -q -f "%SCRIPT_DIR%pom.xml" clean package -DskipTests
if errorlevel 1 exit /b %errorlevel%
if not exist "%JAR_FILE%" (
    echo [ERROR] CLI jar was not created: %JAR_FILE%
    exit /b 1
)
echo.

:runCli
where java >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Java was not found in PATH.
    echo Please install JDK 17+ and set JAVA_HOME, then run this command again.
    exit /b 1
)

java -jar "%JAR_FILE%" %*
exit /b %errorlevel%
