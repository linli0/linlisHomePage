@echo off
chcp 65001 >nul
cd /d "%~dp0.."

echo 打包一键启动器 CoffeeHomepage.exe ...
echo 需要已安装 Python；首次会安装 PyInstaller。
echo.

where python >nul 2>&1
if errorlevel 1 (
  where py >nul 2>&1
  if errorlevel 1 (
    echo [错误] 未检测到 Python
    pause
    exit /b 1
  )
  set PY=py -3
) else (
  set PY=python
)

%PY% -m pip install -q pyinstaller
if errorlevel 1 (
  echo [错误] 安装 PyInstaller 失败
  pause
  exit /b 1
)

%PY% -m PyInstaller --noconfirm --clean --onefile --console --name CoffeeHomepage --distpath . --workpath build\launcher --specpath build\launcher scripts\dev_launcher.py
if errorlevel 1 (
  echo [错误] 打包失败
  pause
  exit /b 1
)

echo.
echo 完成: %CD%\CoffeeHomepage.exe
echo 双击即可启动本地服务（仍需本机已安装 Python / Node.js）。
echo.
pause
