#!/bin/bash

echo "============================================"
echo "   CoffeeCookies Homepage 启动脚本"
echo "   Vue 3 + FastAPI 轻量化架构"
echo "============================================"
echo ""

MODE="${1:-docker}"

if [ "$MODE" = "dev" ]; then
    echo "[dev] 启动本地开发环境..."
    mkdir -p data api/data
  if [ ! -d frontend/node_modules ]; then
    echo "[dev] 安装前端依赖..."
    (cd frontend && npm install)
  fi
  if [ ! -d api/.venv ]; then
    echo "[dev] 创建 Python 虚拟环境..."
    python3 -m venv api/.venv
    api/.venv/bin/pip install -r api/requirements.txt
  fi
  echo "[dev] 启动 FastAPI (8000) 与 Vite (3000)..."
  trap 'kill 0' EXIT
  (cd api && ../api/.venv/bin/uvicorn app.main:app --reload --host 0.0.0.0 --port 8000) &
  (cd frontend && npm run dev) &
  wait
  exit 0
fi

# Docker mode
if ! docker info > /dev/null 2>&1; then
    echo "[错误] Docker 未运行，请先启动 Docker"
    echo "       或使用本地开发: ./start.sh dev"
    exit 1
fi

if [ ! -f .env ]; then
    echo "[信息] 创建 .env 文件..."
    cp .env.example .env
fi

mkdir -p data

echo "[1/3] 正在构建 Docker 镜像..."
docker compose build

if [ $? -ne 0 ]; then
    echo "[错误] 构建失败"
    exit 1
fi

echo "[2/3] 正在启动服务..."
docker compose up -d

if [ $? -ne 0 ]; then
    echo "[错误] 启动失败"
    exit 1
fi

echo "[3/3] 等待服务启动..."
sleep 5

echo ""
echo "============================================"
echo "   服务启动成功！"
echo "============================================"
echo ""
echo "访问地址:"
echo "  - 前端: http://localhost"
echo "  - API: http://localhost:8000"
echo "  - API 文档: http://localhost:8000/docs"
echo ""
echo "默认账号:"
echo "  - 管理员: admin / admin123"
echo "  - 普通用户: user / user123"
echo ""
echo "常用命令:"
echo "  - 本地开发: ./start.sh dev"
echo "  - 查看日志: docker compose logs -f"
echo "  - 停止服务: docker compose down"
echo "  - API 测试: cd api && PYTHONPATH=. pytest"
echo ""
