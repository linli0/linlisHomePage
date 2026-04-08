@echo off
chcp 65001 > nul
echo ☕ CoffeeCookie'sHomePage 启动脚本
echo =================================
echo.

REM 检查 Java
java -version > nul 2>&1
if errorlevel 1 (
    echo ❌ 未检测到 Java，请先安装 JDK 17+
    exit /b 1
)

REM 检查 Java 版本 >= 17
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VERSION=%%v
set JAVA_VERSION=%JAVA_VERSION:"=%
for /f "tokens=1,2 delims=." %%a in ("%JAVA_VERSION%") do (
    set JAVA_MAJOR=%%a
    set JAVA_MINOR=%%b
)

if %JAVA_MAJOR% LSS 17 (
    echo ❌ Java 版本过低，当前版本: %JAVA_MAJOR%，需要 17+
    exit /b 1
)

echo ✅ Java 已安装 (版本: %JAVA_VERSION%)

REM 检查 Maven
call mvn -version > nul 2>&1
if errorlevel 1 (
    echo ❌ 未检测到 Maven，请先安装 Maven 3.8+
    exit /b 1
)

echo ✅ Maven 已安装

REM 安装前端依赖并构建
if not exist "frontend\dist" (
    echo 📦 安装前端依赖...
    cd frontend
    if not exist "node_modules" (
        call npm install
        if errorlevel 1 (
            echo ❌ 前端依赖安装失败
            cd ..
            exit /b 1
        )
    )
    echo 🔨 构建前端...
    call npm run build
    if errorlevel 1 (
        echo ❌ 前端构建失败
        cd ..
        exit /b 1
    )
    cd ..
)

echo.
echo 🚀 启动 Spring Boot 后端...
echo.
echo 📊 访问地址：
echo    本地: http://localhost:8080
echo    API:  http://localhost:8080/api
echo.
echo 🔑 登录方式：
echo    用户名: admin
echo    密码: admin123
echo.
echo 按 Ctrl+C 停止服务
echo.

cd backend
call mvn spring-boot:run
cd ..