@echo off
setlocal EnableExtensions DisableDelayedExpansion

chcp 65001 >nul

set "SCRIPT_DIR=%~dp0"
set "JAR_FILE=%SCRIPT_DIR%scaffold4j-cli\target\scaffold4j-cli-1.0.0-SNAPSHOT.jar"
set "DEFAULT_OUTPUT_DIR=%USERPROFILE%\scaffold4j-projects"
if not defined S4J_DEBUG set "S4J_DEBUG=0"

echo.
echo ================================================
echo        scaffold4j Windows Project Bootstrap
echo ================================================
echo.

call :checkJava || exit /b 1
call :ensureJar || exit /b 1
call :debug "Bootstrap checks completed"

echo Press Enter to accept defaults shown in brackets.
echo Use comma-separated values for multi-select options.
echo.

call :promptDefault PROJECT_NAME "Project name" "my-ai-app"
call :promptDefault BASE_PACKAGE "Base package" "com.example.ai"
call :promptDefault GROUP_ID "Maven groupId" "%BASE_PACKAGE%"
call :promptDefault ARTIFACT_ID "Maven artifactId" "%PROJECT_NAME%"
call :promptDefault VERSION "Version" "1.0.0-SNAPSHOT"
call :promptDefault OUTPUT_DIR "Output directory" "%DEFAULT_OUTPUT_DIR%"

echo.
echo Build options
call :promptDefault JAVA_VERSION "Java version, valid: 17 or 21" "17"
call :promptDefault SPRING_BOOT_VERSION "Spring Boot version" "3.5.0"

echo.
echo AI framework options: spring-ai, spring-ai-alibaba, langchain4j, both
call :promptDefault AI_FRAMEWORK "AI framework" "spring-ai"

echo.
echo LLM providers: openai, ollama, anthropic, deepseek, zhipuai, vertex-ai, azure-openai, bedrock, qwen, moonshot, doubao
call :promptDefault LLM_PROVIDERS "LLM providers" "openai"

echo.
echo Protocols: rest, mcp, a2a, acp
call :promptDefault PROTOCOLS "Protocols" "rest"

echo.
echo Features: memory, rag, sse, websocket
call :promptDefault FEATURES "Features" ""

echo.
echo Vector stores: pgvector, milvus, chroma, pinecone, elasticsearch, redis, weaviate, qdrant, simple
call :promptDefault VECTOR_STORE "Vector store" "milvus"
call :debug "Base project options collected"

echo.
call :promptDefault NACOS_ENABLED "Enable Nacos? true/false" "false"
set "NACOS_FLAG="
if /i "%NACOS_ENABLED%"=="true" (
    set "NACOS_FLAG=--nacos"
    call :promptDefault NACOS_ADDR "Nacos server address" "localhost:8848"
    call :promptDefault NACOS_NAMESPACE "Nacos namespace" ""
) else (
    set "NACOS_ADDR=localhost:8848"
    set "NACOS_NAMESPACE="
)

echo.
echo Database types: mysql, postgresql, h2
call :promptDefault DB_TYPE "Database type" "mysql"
call :promptDefault ORM "ORM framework, valid: mybatis-plus or jpa" "mybatis-plus"
set "DB_PORT_DEFAULT=0"
if /i "%DB_TYPE%"=="mysql" set "DB_PORT_DEFAULT=3306"
if /i "%DB_TYPE%"=="postgresql" set "DB_PORT_DEFAULT=5432"
if /i not "%DB_TYPE%"=="h2" (
    call :promptDefault DB_HOST "Database host" "localhost"
    call :promptDefault DB_PORT "Database port" "%DB_PORT_DEFAULT%"
    call :promptDefault DB_NAME "Database name" "%PROJECT_NAME%"
    call :promptDefault DB_USERNAME "Database username" "root"
    call :promptDefault DB_PASSWORD "Database password" "root"
) else (
    set "DB_HOST=localhost"
    set "DB_PORT=0"
    set "DB_NAME=%PROJECT_NAME%"
    set "DB_USERNAME=root"
    set "DB_PASSWORD=root"
)
call :debug "Database options collected"

call :collectCacheAndMq
call :debug "Cache and MQ options collected"

if "%BASE_PACKAGE%"=="" set "BASE_PACKAGE=com.example.ai"
if "%AI_FRAMEWORK%"=="" set "AI_FRAMEWORK=spring-ai"
if "%LLM_PROVIDERS%"=="" set "LLM_PROVIDERS=openai"
if "%PROTOCOLS%"=="" set "PROTOCOLS=rest"
if "%VECTOR_STORE%"=="" set "VECTOR_STORE=pgvector"
if "%ORM%"=="" set "ORM=mybatis-plus"

echo.
echo Generating project...
echo.
call :prepareOutputDirectory "%OUTPUT_DIR%" || exit /b 1
call :debug "Generating project with direct CLI arguments"

if /i "%MQ_TYPE%"=="none" (
    java -jar "%JAR_FILE%" generate --name "%PROJECT_NAME%" --package "%BASE_PACKAGE%" --group-id "%GROUP_ID%" --artifact-id "%ARTIFACT_ID%" --version "%VERSION%" --java-version "%JAVA_VERSION%" --spring-boot-version "%SPRING_BOOT_VERSION%" --ai-framework "%AI_FRAMEWORK%" --llm-providers "%LLM_PROVIDERS%" --protocols "%PROTOCOLS%" --features "%FEATURES%" --vector-store "%VECTOR_STORE%" %NACOS_FLAG% --nacos-addr "%NACOS_ADDR%" --nacos-namespace "%NACOS_NAMESPACE%" --db-type "%DB_TYPE%" --db-host "%DB_HOST%" --db-port "%DB_PORT%" --db-name "%DB_NAME%" --db-username "%DB_USERNAME%" --db-password "%DB_PASSWORD%" --orm "%ORM%" --cache-type "%CACHE_TYPE%" --redis-host "%REDIS_HOST%" --redis-port "%REDIS_PORT%" --redis-password "%REDIS_PASSWORD%" --redis-database "%REDIS_DATABASE%" --mq-type "%MQ_TYPE%" --output-dir "%OUTPUT_DIR%"
) else (
    java -jar "%JAR_FILE%" generate --name "%PROJECT_NAME%" --package "%BASE_PACKAGE%" --group-id "%GROUP_ID%" --artifact-id "%ARTIFACT_ID%" --version "%VERSION%" --java-version "%JAVA_VERSION%" --spring-boot-version "%SPRING_BOOT_VERSION%" --ai-framework "%AI_FRAMEWORK%" --llm-providers "%LLM_PROVIDERS%" --protocols "%PROTOCOLS%" --features "%FEATURES%" --vector-store "%VECTOR_STORE%" %NACOS_FLAG% --nacos-addr "%NACOS_ADDR%" --nacos-namespace "%NACOS_NAMESPACE%" --db-type "%DB_TYPE%" --db-host "%DB_HOST%" --db-port "%DB_PORT%" --db-name "%DB_NAME%" --db-username "%DB_USERNAME%" --db-password "%DB_PASSWORD%" --orm "%ORM%" --cache-type "%CACHE_TYPE%" --redis-host "%REDIS_HOST%" --redis-port "%REDIS_PORT%" --redis-password "%REDIS_PASSWORD%" --redis-database "%REDIS_DATABASE%" --mq-type "%MQ_TYPE%" --mq-host "%MQ_HOST%" --mq-port "%MQ_PORT%" --mq-username "%MQ_USERNAME%" --mq-password "%MQ_PASSWORD%" --mq-virtual-host "%MQ_VIRTUAL_HOST%" --mq-group "%MQ_GROUP%" --output-dir "%OUTPUT_DIR%"
)
set "EXIT_CODE=%errorlevel%"
call :debug "Generation finished with exit code %EXIT_CODE%"
echo.
if "%EXIT_CODE%"=="0" (
    call :grantCurrentUserFullControl "%OUTPUT_DIR%\%ARTIFACT_ID%"
    echo Done. Next steps:
    echo   cd "%OUTPUT_DIR%\%ARTIFACT_ID%"
    echo   mvnw.cmd -pl %ARTIFACT_ID%-bootstrap spring-boot:run
) else (
    echo Generation failed with exit code %EXIT_CODE%.
)
exit /b %EXIT_CODE%

:prepareOutputDirectory
set "__path=%~f1"
if not exist "%__path%" (
    mkdir "%__path%" 2>nul
    if errorlevel 1 (
        echo [ERROR] Failed to create output directory: %__path%
        echo Please choose a user-writable directory such as %%USERPROFILE%%\scaffold4j-projects.
        exit /b 1
    )
)
call :grantCurrentUserFullControl "%__path%"
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

:checkJava
where java >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Java was not found in PATH.
    echo Please install JDK 17+ and set JAVA_HOME.
    exit /b 1
)
exit /b 0

:ensureJar
where mvn >nul 2>nul
if errorlevel 1 (
    if exist "%JAR_FILE%" (
        echo Maven was not found in PATH. Using existing CLI jar.
        exit /b 0
    )
    echo [ERROR] Maven was not found in PATH and CLI jar does not exist.
    echo Please install Maven 3.9+ or copy scaffold4j-cli\target\scaffold4j-cli-1.0.0-SNAPSHOT.jar into this directory.
    exit /b 1
)
echo Building/updating scaffold4j CLI...
call mvn -q -f "%SCRIPT_DIR%pom.xml" clean package -DskipTests
if errorlevel 1 exit /b %errorlevel%
if not exist "%JAR_FILE%" (
    echo [ERROR] CLI jar was not created: %JAR_FILE%
    exit /b 1
)
exit /b 0

:promptDefault
set "__var=%~1"
set "__label=%~2"
set "__default=%~3"
set "__value="
if "%__default%"=="" (
    set /p "__value=%__label%: "
) else (
    set /p "__value=%__label% [%__default%]: "
)
if "%__value%"=="" set "__value=%__default%"
set "%__var%=%__value%"
exit /b 0

:collectCacheAndMq
if defined __S4J_CACHE_MQ_COLLECTED (
    call :debug "Skipping duplicate cache/mq collection"
    exit /b 0
)
set "__S4J_CACHE_MQ_COLLECTED=1"
call :debug "Entering cache/mq collection"
echo.
echo Cache types: redis, caffeine, none
call :promptDefault CACHE_TYPE "Cache type" "none"
if /i "%CACHE_TYPE%"=="redis" (
    call :promptDefault REDIS_HOST "Redis host" "localhost"
    call :promptDefault REDIS_PORT "Redis port" "6379"
    call :promptDefault REDIS_PASSWORD "Redis password" ""
    call :promptDefault REDIS_DATABASE "Redis database" "0"
) else (
    set "REDIS_HOST=localhost"
    set "REDIS_PORT=6379"
    set "REDIS_PASSWORD="
    set "REDIS_DATABASE=0"
)
call :debug "Cache options collected"

echo.
echo MQ types: rabbitmq, rocketmq, kafka, none
call :promptDefault MQ_TYPE "Message queue type" "none"
call :normalizeMqType
call :configureMq
call :debug "MQ options collected"
exit /b 0

:configureMq
if /i "%MQ_TYPE%"=="none" (
    set "MQ_HOST=localhost"
    set "MQ_PORT=0"
    set "MQ_USERNAME="
    set "MQ_PASSWORD="
    set "MQ_VIRTUAL_HOST="
    set "MQ_GROUP="
    echo MQ type is none. Skipping all MQ configuration prompts.
    exit /b 0
)
set "MQ_PORT_DEFAULT=0"
if /i "%MQ_TYPE%"=="rabbitmq" set "MQ_PORT_DEFAULT=5672"
if /i "%MQ_TYPE%"=="rocketmq" set "MQ_PORT_DEFAULT=9876"
if /i "%MQ_TYPE%"=="kafka" set "MQ_PORT_DEFAULT=9092"
call :promptDefault MQ_HOST "MQ host" "localhost"
call :promptDefault MQ_PORT "MQ port" "%MQ_PORT_DEFAULT%"
call :promptDefault MQ_USERNAME "MQ username" "guest"
call :promptDefault MQ_PASSWORD "MQ password" "guest"
if /i "%MQ_TYPE%"=="rabbitmq" (
    call :promptDefault MQ_VIRTUAL_HOST "RabbitMQ virtual host" "/"
) else (
    set "MQ_VIRTUAL_HOST=/"
)
call :promptDefault MQ_GROUP "Consumer group" "scaffold4j-consumer"
exit /b 0

:normalizeMqType
set "MQ_TYPE=%MQ_TYPE: =%"
if /i "%MQ_TYPE%"=="rabbitmq" exit /b 0
if /i "%MQ_TYPE%"=="rocketmq" exit /b 0
if /i "%MQ_TYPE%"=="kafka" exit /b 0
if /i "%MQ_TYPE%"=="none" exit /b 0
echo Unknown MQ type "%MQ_TYPE%". Using none.
set "MQ_TYPE=none"
exit /b 0

:debug
if /i not "%S4J_DEBUG%"=="1" exit /b 0
echo [DEBUG] %~1
exit /b 0
