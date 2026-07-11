#!/bin/bash

echo "============================================"
echo "   CoffeeCookies Homepage 启动脚本"
echo "============================================"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "[错误] Docker 未运行，请先启动 Docker"
    exit 1
fi

# Check if .env file exists
if [ ! -f .env ]; then
    echo "[信息] 创建 .env 文件..."
    cp .env.example .env
fi

echo "[1/3] 正在构建 Docker 镜像..."
docker-compose build

if [ $? -ne 0 ]; then
    echo "[错误] 构建失败"
    exit 1
fi

echo "[2/3] 正在启动服务..."
docker-compose up -d

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
echo "  - 后端 API: http://localhost:8080"
echo "  - H2 控制台: http://localhost:8080/h2-console"
echo ""
echo "默认账号:"
echo "  - 管理员: admin / admin123"
echo "  - 普通用户: user / user123"
echo ""
echo "常用命令:"
echo "  - 查看日志: docker-compose logs -f"
echo "  - 停止服务: docker-compose down"
echo "  - 重启服务: docker-compose restart"
echo ""
