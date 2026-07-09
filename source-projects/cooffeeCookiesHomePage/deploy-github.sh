#!/bin/bash
# 部署脚本 for GitHub Pages

echo "🚀 部署到 GitHub Pages..."

# 1. 创建 GitHub 仓库（手动在 GitHub 上创建）
echo "1. 请先在 GitHub 创建仓库，命名为: cooffeeCookiesHomePage"
echo "   访问: https://github.com/new"
read -p "按 Enter 继续..."

# 2. 添加远程仓库
git remote add origin https://github.com/$(git config user.name)/cooffeeCookiesHomePage.git 2>/dev/null || true

# 3. 提交代码
git add .
git commit -m "Initial commit" || true
git push -u origin main || git push -u origin master

# 4. 创建 gh-pages 分支
git checkout -b gh-pages
git push origin gh-pages

echo "✅ 部署完成！"
echo "🌐 访问地址: https://$(git config user.name).github.io/cooffeeCookiesHomePage"
