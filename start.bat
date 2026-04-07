@echo off
chcp 65001 > nul
echo ☕ CoffeeCookie'sHomePage 启动脚本
echo =================================
echo.

REM 检查 Node.js
node --version > nul 2>&1
if errorlevel 1 (
    echo ❌ 未检测到 Node.js，请先安装 Node.js 18+
    exit /b 1
)

REM 检查 Node.js 版本 >= 18
for /f "tokens=1 delims=." %%v in ('node -e "process.stdout.write(process.versions.node.split('.')[0])"') do set NODE_MAJOR=%%v
if %NODE_MAJOR% LSS 18 (
    echo ❌ Node.js 版本过低，当前版本: %NODE_MAJOR%，需要 18+
    exit /b 1
)

echo ✅ Node.js 已安装

REM 安装后端依赖
if not exist "node_modules" (
    echo 📦 安装后端依赖...
    call npm install
    if errorlevel 1 (
        echo ❌ 后端依赖安装失败
        exit /b 1
    )
)

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
echo 🚀 启动服务器...
echo.
echo 📊 访问地址：
echo    本地: http://localhost:8080
echo    API:  http://localhost:8080/api
echo.
echo 🔑 登录方式：
echo    只需输入访问密码（默认: admin）
echo.
echo 按 Ctrl+C 停止服务
echo.

node server.js
