@echo off
chcp 65001 > nul
echo CoffeeCookies Homepage - FastAPI + Vue
echo =====================================
echo.

python --version > nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Python 3.12+
    exit /b 1
)

node --version > nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Node.js
    exit /b 1
)

if not exist api\.venv (
    echo 创建 Python 虚拟环境...
    python -m venv api\.venv
    call api\.venv\Scripts\pip install -r api\requirements.txt
)

if not exist frontend\node_modules (
    echo 安装前端依赖...
    cd frontend
    call npm install
    cd ..
)

echo 启动 FastAPI (8000) 与 Vite (3000)...
start "FastAPI" cmd /k "cd api && ..\\api\\.venv\\Scripts\\uvicorn app.main:app --reload --host 0.0.0.0 --port 8000"
cd frontend
call npm run dev
