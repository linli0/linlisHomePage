# Load Service Skill

> 自动部署本地前后台服务并启动 Cloudflare Tunnel 穿透内网

**Version:** 3.0.0
**Category:** Deployment

---

## 功能描述

通过 `load-service.ps1` 脚本一键部署 CoffeeCookie's HomePage：
1. 读取 `.config/service-config.yml` 获取部署配置
2. 检查环境依赖
3. 构建前端 + 启动 Spring Boot 后端
4. 启动 Cloudflare Tunnel（已有域名方案）
5. 输出固定公网访问地址

---

## 使用方法

### 方式一：脚本执行（推荐）

```powershell
# 完整部署（构建 + 后端 + Tunnel）
.\.skills\load-service\load-service.ps1

# 跳过前端构建（已构建过）
.\.skills\load-service\load-service.ps1 -SkipBuild

# 跳过 Tunnel（仅本地访问）
.\.skills\load-service\load-service.ps1 -SkipTunnel

# 组合使用
.\.skills\load-service\load-service.ps1 -SkipBuild -SkipTunnel
```

### 方式二：AI 助手调用

```bash
# 告诉 AI 助手
"load-service"

# AI 助手将执行
.\.skills\load-service\load-service.ps1
```

---

## 脚本参数

| 参数 | 说明 |
|------|------|
| `-SkipBuild` | 跳过前端构建步骤 |
| `-SkipTunnel` | 跳过 Cloudflare Tunnel 启动 |
| `-Dev` | 预留：开发模式 |

---

## 配置文件

所有部署参数均从 `.config/service-config.yml` 读取，脚本不硬编码任何端口、域名或命令。

### 配置文件结构

```yaml
service:           # 服务信息（名称、端口、域名）
build:             # 前后端构建命令
start:             # 后端启动 + Tunnel 启动方式
access:            # 本地/公网访问地址、默认账号
env_check:         # 必须检查的工具列表
```

### Tunnel 启动方式

`start.tunnel.method` 决定 Tunnel 策略：

| method | 说明 | 公网地址 |
|--------|------|----------|
| `existing-domain` | 复用已有 tunnel + 配置文件 | 固定域名（如 www.coffeecookie.online） |
| `quick-tunnel` | 临时 Quick Tunnel | 随机 trycloudflare.com 地址 |

当前配置为 **`existing-domain`**，公网地址固定为 `https://www.coffeecookie.online`。

---

## 前置条件

从 `service-config.yml` 的 `env_check.required` 读取，当前要求：
- **Java 17+**: `java -version`
- **Maven 3.8+**: `mvn -version`
- **Node.js 20+**: `node --version`
- **Cloudflared**: `cloudflared --version`

---

## 执行流程

脚本 `load-service.ps1` 自动执行以下步骤：

### Step 1: 读取配置
解析 `.config/service-config.yml`，提取端口、域名、Tunnel 方式等参数。

### Step 2: 环境检查
逐项检查 `env_check.required` 中的工具，缺失则终止并提示安装。

### Step 3: 前端构建
- 检查 `node_modules` 是否存在，不存在则 `npm install`
- 执行 `npm run build`，产物输出到 `frontend/dist/`

### Step 4: 启动后端
- 后台启动 `mvn spring-boot:run`
- 健康检查：轮询 `health_check.url` 直到服务就绪（超时 120s）

### Step 5: 启动 Cloudflare Tunnel
- `existing-domain`：使用配置文件启动，固定域名
- `quick-tunnel`：生成临时 URL

### Step 6: 输出结果
显示服务状态、公网地址、登录凭据、日志和停止命令。

---

## 故障排除

### 问题：Java 版本过低
```powershell
winget install Oracle.JDK.17
```

### 问题：Maven 未安装
```powershell
winget install Apache.Maven
```

### 问题：后端启动失败
```powershell
Get-Content backend-error.log -Tail 50
cd backend; mvn clean package -DskipTests; java -jar target/homepage-backend-1.0.0.jar
```

### 问题：Tunnel 无法访问
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/gold-price/latest"
Test-Path ".config/tunnel/opencode-config.yml"
```

### 问题：域名无法解析
- 确认 Cloudflare Dashboard 中 `www` 的 CNAME 记录指向 `my-tunnel.cfargotunnel.com`
- 确认 tunnel 凭证文件存在：`~/.cloudflared/d2f7ff06-9e7d-4b52-aa52-08ce100d2f7e.json`

---

## 停止服务

```powershell
Stop-Process -Name "java" -Force
Stop-Process -Name "cloudflared" -Force
```

---

## 注意事项

🔒 **已有域名方案优势**:
- 公网地址固定，不随重启变化
- 可配置 DNS、SSL 证书等
- 适合生产环境

⚠️ **Tunnel 配置变更**:
- 修改 tunnel 配置需同步更新 `.config/tunnel/opencode-config.yml`
- 新增子域名路由需在 Cloudflare Dashboard 添加 DNS CNAME 记录
- ingress 最后一条必须是 catch-all（`- service: http_status:404`）

🔐 **安全建议**:
- `.config/` 目录已在 `.gitignore` 中排除，不会被提交
- 生产环境必须设置 `JWT_SECRET` 环境变量
- 定期更换 JWT 密钥

---

## 更新日志

**v3.0.0** (2026-04-09)
- 引入 `load-service.ps1` 脚本，一键执行部署
- 支持 `-SkipBuild`、`-SkipTunnel` 参数
- 脚本自动解析 `service-config.yml`，无需 AI 逐步执行

**v2.0.0** (2026-04-09)
- 从 Quick Tunnel 切换到已有域名方案
- 引入 `service-config.yml` 作为部署配置中心

**v1.0.0** (2026-04-08)
- 初始版本，支持 Cloudflare Quick Tunnel
