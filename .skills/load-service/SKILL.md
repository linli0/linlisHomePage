# Load Service Skill

> 自动部署本地前后台服务并启动 Cloudflare Tunnel 穿透内网

**Version:** 1.0.0
**Category:** Deployment

---

## 功能描述

快速本地部署 CoffeeCookie's HomePage 项目：
1. 检查并启动 Spring Boot 后端（端口 8080）
2. 验证前端构建状态
3. 启动 Cloudflare Tunnel 穿透内网
4. 输出公网访问地址

---

## 使用方法

```bash
# 通过 AI 助手调用
用户: "load-service"
AI: 自动执行部署流程
```

---

## 前置条件

- **Java 17+**: `java -version`
- **Maven 3.8+**: `mvn -version`
- **Node.js 20+**: `node -version`
- **Cloudflared**: 通过 `winget install Cloudflare.cloudflared` 安装

---

## 执行流程

### 1. 环境检查
```bash
# 检查 Java 版本
java -version

# 检查 Maven
mvn -version

# 检查 Node.js
node -version
```

### 2. 前端构建
```bash
cd frontend

# 安装依赖（如需要）
if [ ! -d "node_modules" ]; then
    npm install
fi

# 构建前端
npm run build
```

### 3. 启动后端
```bash
cd backend

# 后台启动 Spring Boot
nohup mvn spring-boot:run > ../backend.log 2>&1 &

# 等待服务启动
sleep 20

# 测试服务
curl -s http://localhost:8080/api/gold-price/current?currency=USD
```

### 4. 启动 Cloudflare Tunnel
```bash
# 查找 cloudflared
CLOUDFLARED_PATH="/c/Users/Windows11/AppData/Local/Microsoft/WinGet/Packages/Cloudflare.cloudflared_Microsoft.Winget.Source_8wekyb3d8bbwe/cloudflared.exe"

# 后台启动 Tunnel
nohup $CLOUDFLARED_PATH tunnel --url http://localhost:8080 > tunnel.log 2>&1 &

# 等待并获取 URL
sleep 15
cat tunnel.log | grep "trycloudflare.com"
```

### 5. 输出结果
```
✅ 部署成功！

📊 服务状态:
  - 后端: http://localhost:8080 ✓
  - 前端: 已构建 ✓
  - Tunnel: 运行中 ✓

🌐 公网访问地址: https://xxx-trycloudflare.com

🔑 登录凭据:
  - 用户名: admin
  - 密码: admin123

📝 查看日志:
  - 后端: tail -f backend.log
  - Tunnel: tail -f tunnel.log

🛑 停止服务:
  - pkill -f "mvn spring-boot:run"
  - pkill -f "cloudflared"
```

---

## 故障排除

### 问题：Java 版本过低
```bash
# 安装 JDK 17+
winget install Oracle.JDK.17
```

### 问题：Maven 未安装
```bash
# 安装 Maven
winget install Apache.Maven
```

### 问题：后端启动失败
```bash
# 查看错误日志
cat backend.log

# 清理并重新构建
cd backend && mvn clean package -DskipTests
java -jar target/homepage-backend-1.0.0.jar
```

### 问题：Cloudflare Tunnel 无法访问
```bash
# 检查本地服务是否运行
curl http://localhost:8080/api/gold-price/current

# 查看隧道日志
cat tunnel.log

# 重新创建隧道
pkill -f "cloudflared"
nohup cloudflared tunnel --url http://localhost:8080 > tunnel.log 2>&1 &
```

---

## 注意事项

⚠️ **Quick Tunnel 限制**:
- URL 会随重启变化，非永久地址
- 无 SLA 保障，仅适合测试
- 生产环境请使用 Cloudflare 账户创建命名 Tunnel

🔒 **安全建议**:
- 不要将 Tunnel URL 分享给不可信人员
- 生产环境建议使用自定义域名
- 定期更换 JWT 密钥

---

## 高级配置

### 使用自定义域名
```bash
# 1. 创建命名 Tunnel
cloudflared tunnel create homepage

# 2. 配置 config.yml
tunnel: <TUNNEL-ID>
credentials-file: ~/.cloudflared/<TUNNEL-ID>.json

ingress:
  - hostname: yourdomain.com
    service: http://localhost:8080
  - service: http_status:404

# 3. 运行
cloudflared tunnel run homepage
```

### 环境变量配置
```bash
# .env 文件
JWT_SECRET=your-secret-key
MYSQL_PASSWORD=your-password
OLLAMA_BASE_URL=http://localhost:11434
```

---

## 更新日志

**v1.0.0** (2026-04-08)
- 初始版本
- 支持自动部署前后端
- 支持 Cloudflare Quick Tunnel
- 支持一键启动和停止