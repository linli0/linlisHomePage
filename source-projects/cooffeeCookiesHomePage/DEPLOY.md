# 部署到 GitHub Pages

## 步骤

### 1. 在 GitHub 创建仓库
访问 https://github.com/new
- 仓库名: `cooffeeCookiesHomePage`
- 选择 Public
- 点击 Create repository

### 2. 推送代码到 GitHub

```bash
# 添加远程仓库
git remote add origin https://github.com/你的用户名/cooffeeCookiesHomePage.git

# 推送代码
git add .
git commit -m "Initial commit"
git push -u origin main
```

### 3. 启用 GitHub Pages

1. 打开仓库页面
2. 点击 Settings → Pages
3. Source 选择 "Deploy from a branch"
4. Branch 选择 "main"，文件夹选 "/ (root)"
5. 点击 Save

### 4. 访问网站

等待 1-2 分钟后，访问：
```
https://你的用户名.github.io/cooffeeCookiesHomePage
```

---

# 部署到 Vercel（更简单）

## 步骤

### 1. 安装 Vercel CLI
```bash
npm i -g vercel
```

### 2. 登录并部署
```bash
cd C:/Users/Windows11/cooffeeCookiesHomePage
vercel
```

### 3. 按提示操作
- 登录 Vercel 账号
- 选择项目目录
- 确认部署设置

### 4. 完成
Vercel 会自动分配域名，如 `cooffeeCookiesHomePage-xxxxx.vercel.app`

---

# 部署到 Netlify

## 步骤

### 1. 安装 Netlify CLI
```bash
npm i -g netlify-cli
```

### 2. 登录并部署
```bash
netlify login
netlify deploy --prod --dir=src
```

### 3. 完成
Netlify 会分配临时域名，可在后台自定义。
