@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"

echo 正在停止本机 8000 / 3000 端口上的服务...

for %%P in (8000 3000) do (
  for /f "tokens=5" %%A in ('netstat -ano ^| findstr ":%%P" ^| findstr LISTENING') do (
    echo 结束 PID %%A ^(port %%P^)
    taskkill /PID %%A /F >nul 2>&1
  )
)

echo 完成。
timeout /t 2 >nul
