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

echo ✅ Node.js 已安装

REM 安装依赖（如果不存在）
if not exist "node_modules" (
    echo 📦 安装后端依赖...
    npm install
)

if not exist "frontend-dist" (
    echo 📦 安装前端依赖...
    cd frontend
    if not exist "node_modules" (
        npm install
    )
    echo 🔨 构建前端...
    npm run build
    cd ..
    xcopy /E /I /Y frontend\dist frontend-dist
)

echo.
echo 🚀 启动服务器...
echo.
echo 📊 访问地址：
echo    本地: http://localhost:8080
echo    API:  http://localhost:8080/api
echo.
echo 🔑 默认账号：
echo    管理员: admin / admin123
echo    普通用户: user / user123
echo.
echo 按 Ctrl+C 停止服务
echo.

node server.js
