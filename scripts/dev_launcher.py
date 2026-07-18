#!/usr/bin/env python3
"""One-click local launcher for FastAPI (8000) + Vite (3000)."""

from __future__ import annotations

import os
import shutil
import subprocess
import sys
import time
import urllib.error
import urllib.request
import webbrowser
from pathlib import Path

def project_root() -> Path:
    # PyInstaller onefile: exe 放在仓库根目录
    if getattr(sys, "frozen", False):
        return Path(sys.executable).resolve().parent
    return Path(__file__).resolve().parents[1]


ROOT = project_root()
API_DIR = ROOT / "api"
FRONTEND_DIR = ROOT / "frontend"
VENV_PYTHON = API_DIR / ".venv" / "Scripts" / "python.exe"
VENV_UVICORN = API_DIR / ".venv" / "Scripts" / "uvicorn.exe"
WEB_URL = "http://localhost:3000"
API_HEALTH = "http://localhost:8000/api/health"


def die(msg: str, code: int = 1) -> None:
    print(f"[错误] {msg}")
    input("按回车退出...")
    sys.exit(code)


def ensure_tools() -> None:
    if shutil.which("python") is None and shutil.which("py") is None:
        die("未检测到 Python，请安装 Python 3.12+")
    if shutil.which("node") is None:
        die("未检测到 Node.js，请安装 Node.js 18+")
    if shutil.which("npm") is None:
        die("未检测到 npm")


def ensure_venv() -> None:
    if VENV_PYTHON.is_file() and VENV_UVICORN.is_file():
        print("[1/3] Python 虚拟环境已就绪")
        return

    print("[1/3] 创建 Python 虚拟环境并安装依赖...")
    py = shutil.which("py") or shutil.which("python")
    assert py
    subprocess.check_call([py, "-m", "venv", str(API_DIR / ".venv")], cwd=ROOT)
    pip = API_DIR / ".venv" / "Scripts" / "pip.exe"
    subprocess.check_call([str(pip), "install", "-r", str(API_DIR / "requirements.txt")], cwd=API_DIR)


def ensure_node_modules() -> None:
    if (FRONTEND_DIR / "node_modules").is_dir():
        print("[2/3] 前端依赖已就绪")
        return
    print("[2/3] 安装前端依赖...")
    subprocess.check_call("npm install", cwd=FRONTEND_DIR, shell=True)


def start_process(args: list[str], cwd: Path, *, shell: bool = False) -> subprocess.Popen:
    creationflags = 0
    if sys.platform == "win32":
        creationflags = subprocess.CREATE_NEW_CONSOLE  # type: ignore[attr-defined]
    return subprocess.Popen(
        args if not shell else subprocess.list2cmdline(args),
        cwd=str(cwd),
        creationflags=creationflags,
        shell=shell,
    )


def wait_url(url: str, timeout: float = 60.0) -> bool:
    deadline = time.time() + timeout
    while time.time() < deadline:
        try:
            with urllib.request.urlopen(url, timeout=2) as resp:
                if 200 <= resp.status < 500:
                    return True
        except (urllib.error.URLError, TimeoutError, OSError):
            time.sleep(1)
    return False


def main() -> None:
    os.chdir(ROOT)
    print("============================================")
    print("  CoffeeCookies Homepage 一键启动")
    print("  FastAPI (8000) + Vite (3000)")
    print("============================================")
    print(f"项目目录: {ROOT}")
    print()

    ensure_tools()
    ensure_venv()
    ensure_node_modules()

    print("[3/3] 启动服务...")
    api = start_process(
        [str(VENV_UVICORN), "app.main:app", "--reload", "--host", "0.0.0.0", "--port", "8000"],
        API_DIR,
    )
    # Windows 上 npm 通常是 npm.cmd，需要 shell
    web = start_process(
        ["npm", "run", "dev"],
        FRONTEND_DIR,
        shell=sys.platform == "win32",
    )

    print("等待服务就绪...")
    api_ok = wait_url(API_HEALTH, timeout=45)
    web_ok = wait_url(WEB_URL, timeout=60)

    if web_ok:
        webbrowser.open(WEB_URL)
    elif api_ok:
        webbrowser.open(API_HEALTH)

    print()
    print("已启动:")
    print(f"  前端  {WEB_URL}           {'OK' if web_ok else '启动中/失败'}")
    print(f"  API   {API_HEALTH} {'OK' if api_ok else '启动中/失败'}")
    print()
    print("关闭本窗口不会自动停服务；请运行 stop.bat，或关闭弹出的控制台窗口。")
    print()
    input("按回车退出启动器（服务继续运行）...")
    # Keep child consoles alive; do not kill on exit.
    _ = (api, web)


if __name__ == "__main__":
    try:
        main()
    except subprocess.CalledProcessError as exc:
        die(f"命令执行失败: {exc}")
    except KeyboardInterrupt:
        sys.exit(130)
