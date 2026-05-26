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
set "EXIT_CODE=%errorlevel%"
if "%EXIT_CODE%"=="0" call :normalizeGeneratedProjectPermissions %*
exit /b %EXIT_CODE%

:normalizeGeneratedProjectPermissions
setlocal EnableExtensions EnableDelayedExpansion
if /i not "%~1"=="generate" (
    endlocal
    exit /b 0
)
set "__name="
set "__artifact_id="
set "__output_dir=."
shift /1
:parseGenerateArgs
if "%~1"=="" goto :normalizeGeneratedProjectPermissionsDone
set "__arg=%~1"
if /i "!__arg!"=="--name" (
    shift /1
    set "__name=%~1"
    goto :parseGenerateArgsNext
)
if /i "!__arg:~0,7!"=="--name=" (
    set "__name=!__arg:~7!"
    goto :parseGenerateArgsNext
)
if /i "!__arg!"=="--artifact-id" (
    shift /1
    set "__artifact_id=%~1"
    goto :parseGenerateArgsNext
)
if /i "!__arg:~0,14!"=="--artifact-id=" (
    set "__artifact_id=!__arg:~14!"
    goto :parseGenerateArgsNext
)
if /i "!__arg!"=="--output-dir" (
    shift /1
    set "__output_dir=%~1"
    goto :parseGenerateArgsNext
)
if /i "!__arg:~0,13!"=="--output-dir=" (
    set "__output_dir=!__arg:~13!"
    goto :parseGenerateArgsNext
)
:parseGenerateArgsNext
shift /1
goto :parseGenerateArgs

:normalizeGeneratedProjectPermissionsDone
if not defined __artifact_id set "__artifact_id=!__name!"
if not defined __artifact_id (
    endlocal
    exit /b 0
)
set "__project_dir=!__output_dir!\!__artifact_id!"
call :grantCurrentUserFullControl "!__project_dir!"
endlocal
exit /b 0

:grantCurrentUserFullControl
set "__path=%~f1"
if not exist "%__path%" exit /b 0
attrib -R "%__path%\*" /S /D >nul 2>nul
where icacls >nul 2>nul
if errorlevel 1 exit /b 0
set "__current_user="
for /f "delims=" %%U in ('whoami 2^>nul') do set "__current_user=%%U"
if not defined __current_user exit /b 0
icacls "%__path%" /inheritance:e /grant:r "%__current_user%:(OI)(CI)F" /T /C >nul 2>nul
exit /b 0
