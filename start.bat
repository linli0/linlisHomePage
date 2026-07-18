@echo off
chcp 65001 >nul
cd /d "%~dp0"

where python >nul 2>&1
if errorlevel 1 (
  where py >nul 2>&1
  if errorlevel 1 (
    echo [错误] 未检测到 Python，请安装 Python 3.12+
    pause
    exit /b 1
  )
  py -3 "%~dp0scripts\dev_launcher.py"
  exit /b %ERRORLEVEL%
)

python "%~dp0scripts\dev_launcher.py"
exit /b %ERRORLEVEL%
