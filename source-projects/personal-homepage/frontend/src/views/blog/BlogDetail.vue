<template>
  <div class="blog-detail">
    <el-row :gutter="24">
      <!-- 主内容区 -->
      <el-col :xs="24" :sm="24" :md="18">
        <div v-if="loading" class="loading-wrapper">
          <el-skeleton :rows="10" animated />
        </div>
        
        <template v-else-if="article">
          <!-- 文章卡片 -->
          <el-card class="article-card" shadow="never">
            <!-- 标题区 -->
            <div class="article-header">
              <h1 class="article-title">{{ article.title }}</h1>
              
              <div class="article-meta">
                <div class="author-info">
                  <el-avatar :size="40" :src="defaultAvatar" />
                  <div class="author-detail">
                    <span class="author-name">{{ article.author || '匿名作者' }}</span>
                    <span class="publish-time">
                      <el-icon><Calendar /></el-icon>
                      发布于 {{ formatDate(article.createdAt) }}
                    </span>
                  </div>
                </div>
                
                <div class="article-stats">
                  <span class="stat-item">
                    <el-icon><View /></el-icon>
                    {{ article.viewCount }} 阅读
                  </span>
                  <span class="stat-item">
                    <el-icon><ChatDotRound /></el-icon>
                    {{ article.commentCount }} 评论
                  </span>
                </div>
              </div>
              
              <!-- 标签 -->
              <div v-if="article.tags?.length" class="article-tags">
                <el-tag
                  v-for="tag in article.tags"
                  :key="tag.id"
                  size="small"
                  :color="tag.color"
                  effect="dark"
                  class="tag-item"
                >
                  {{ tag.name }}
                </el-tag>
              </div>
            </div>
            
            <!-- 封面图 -->
            <div v-if="article.coverImage" class="article-cover">
              <el-image :src="article.coverImage" fit="cover" />
            </div>
            
            <!-- 文章内容 -->
            <div class="article-content markdown-body" v-html="renderedContent"></div>
          </el-card>
          
          <!-- 上一篇/下一篇导航 -->
          <div class="article-nav">
            <el-card
              v-if="prevArticle"
              class="nav-card prev"
              shadow="hover"
              @click="goToArticle(prevArticle.id)"
            >
              <div class="nav-label">
                <el-icon><ArrowLeft /></el-icon>
                上一篇
              </div>
              <div class="nav-title">{{ prevArticle.title }}</div>
            </el-card>
            <div v-else class="nav-placeholder"></div>
            
            <el-card
              v-if="nextArticle"
              class="nav-card next"
              shadow="hover"
              @click="goToArticle(nextArticle.id)"
            >
              <div class="nav-label">
                下一篇
                <el-icon><ArrowRight /></el-icon>
              </div>
              <div class="nav-title">{{ nextArticle.title }}</div>
            </el-card>
            <div v-else class="nav-placeholder"></div>
          </div>
          
          <!-- 评论区 -->
          <el-card class="comments-card" shadow="never">
            <template #header>
              <div class="comments-header">
                <span>评论 ({{ comments.length }})</span>
              </div>
            </template>
            
            <!-- 评论表单 -->
            <div class="comment-form">
              <h4>发表评论</h4>
              <el-form :model="commentForm" label-position="top">
                <el-row :gutter="16">
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="昵称">
                      <el-input
                        v-model="commentForm.author"
                        placeholder="请输入昵称"
                        maxlength="20"
                        show-word-limit
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="邮箱">
                      <el-input
                        v-model="commentForm.email"
                        placeholder="请输入邮箱（不会公开）"
                        type="email"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-form-item label="评论内容">
                  <el-input
                    v-model="commentForm.content"
                    type="textarea"
                    :rows="4"
                    placeholder="写下你的想法..."
                    maxlength="500"
                    show-word-limit
                  />
                </el-form-item>
                <el-form-item>
                  <el-button
                    type="primary"
                    :loading="submitting"
                    @click="submitComment"
                  >
                    发表评论
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
            
            <el-divider />
            
            <!-- 评论列表 -->
            <div v-if="commentsLoading" class="comments-loading">
              <el-skeleton :rows="3" animated />
            </div>
            <div v-else-if="comments.length === 0" class="no-comments">
              <el-empty description="暂无评论，快来抢沙发吧！" />
            </div>
            <div v-else class="comments-list">
              <div
                v-for="comment in comments"
                :key="comment.id"
                class="comment-item"
              >
                <div class="comment-avatar">
                  <el-avatar :size="40">{{ comment.author?.charAt(0) || '?' }}</el-avatar>
                </div>
                <div class="comment-body">
                  <div class="comment-header">
                    <span class="comment-author">{{ comment.author }}</span>
                    <span class="comment-time">{{ formatDateTime(comment.createdAt) }}</span>
                  </div>
                  <p class="comment-content">{{ comment.content }}</p>
                </div>
              </div>
            </div>
          </el-card>
        </template>
        
        <div v-else class="not-found">
          <el-empty description="文章不存在或已被删除">
            <el-button type="primary" @click="goBack">返回博客列表</el-button>
          </el-empty>
        </div>
      </el-col>
      
      <!-- 侧边栏 -->
      <el-col :xs="24" :sm="24" :md="6">
        <div class="detail-sidebar">
          <!-- 目录 -->
          <el-card class="sidebar-card" shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon><List /></el-icon>
                <span>文章目录</span>
              </div>
            </template>
            <div class="toc-content">
              <div v-if="toc.length === 0" class="toc-empty">
                暂无目录
              </div>
              <div
                v-for="(item, index) in toc"
                :key="index"
                :class="['toc-item', `toc-level-${item.level}`]"
                @click="scrollToHeading(item.id)"
              >
                {{ item.text }}
              </div>
            </div>
          </el-card>
          
          <!-- 相关文章 -->
          <el-card class="sidebar-card" shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon><Link /></el-icon>
                <span>相关文章</span>
              </div>
            </template>
            <div v-if="relatedArticles.length === 0" class="related-empty">
              暂无相关文章
            </div>
            <div v-else class="related-list">
              <div
                v-for="item in relatedArticles"
                :key="item.id"
                class="related-item"
                @click="goToArticle(item.id)"
              >
                <span class="related-title">{{ item.title }}</span>
                <span class="related-date">{{ formatDate(item.createdAt) }}</span>
              </div>
            </div>
          </el-card>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Calendar, View, ChatDotRound, ArrowLeft, ArrowRight,
  List, Link
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getArticle, getArticles } from '@/api/article'
import { getCommentsByArticle, createComment } from '@/api/comment'
import type { Article, Comment } from '@/types'

const route = useRoute()
const router = useRouter()
const articleId = computed(() => parseInt(route.params.id as string))

// 状态
const loading = ref(false)
const article = ref<Article | null>(null)
const comments = ref<Comment[]>([])
const commentsLoading = ref(false)
const submitting = ref(false)
const prevArticle = ref<Article | null>(null)
const nextArticle = ref<Article | null>(null)
const relatedArticles = ref<Article[]>([])
const toc = ref<Array<{ id: string; text: string; level: number }>>([])

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 评论表单
const commentForm = ref({
  author: '',
  email: '',
  content: ''
})

// 简单的 Markdown 渲染（实际项目中可以使用 markdown-it 等库）
const renderedContent = computed(() => {
  if (!article.value?.content) return ''
  // 简单的 HTML 转义和换行处理
  return article.value.content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\n/g, '<br>')
})

// 加载文章详情
async function loadArticle() {
  loading.value = true
  try {
    article.value = await getArticle(articleId.value)
    // 生成目录
    generateToc()
    // 加载相关文章
    loadRelatedArticles()
  } catch (error) {
    console.error('Failed to load article:', error)
    article.value = null
  } finally {
    loading.value = false
  }
}

// 加载评论
async function loadComments() {
  commentsLoading.value = true
  try {
    comments.value = await getCommentsByArticle(articleId.value)
  } catch (error) {
    console.error('Failed to load comments:', error)
    comments.value = []
  } finally {
    commentsLoading.value = false
  }
}

// 加载上一篇/下一篇
async function loadAdjacentArticles() {
  try {
    const result = await getArticles({ page: 0, size: 100 })
    const articles = result.content
    const currentIndex = articles.findIndex(a => a.id === articleId.value)
    
    if (currentIndex > 0) {
      prevArticle.value = articles[currentIndex - 1]
    } else {
      prevArticle.value = null
    }
    
    if (currentIndex < articles.length - 1) {
      nextArticle.value = articles[currentIndex + 1]
    } else {
      nextArticle.value = null
    }
  } catch (error) {
    console.error('Failed to load adjacent articles:', error)
  }
}

// 加载相关文章
async function loadRelatedArticles() {
  try {
    const result = await getArticles({ page: 0, size: 5 })
    relatedArticles.value = result.content.filter(a => a.id !== articleId.value).slice(0, 4)
  } catch (error) {
    console.error('Failed to load related articles:', error)
  }
}

// 生成目录
function generateToc() {
  // 实际项目中可以解析文章内容中的标题
  // 这里简单演示
  toc.value = []
}

// 提交评论
async function submitComment() {
  if (!commentForm.value.author.trim()) {
    ElMessage.warning('请输入昵称')
    return
  }
  if (!commentForm.value.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  submitting.value = true
  try {
    await createComment(articleId.value, {
      author: commentForm.value.author,
      email: commentForm.value.email,
      content: commentForm.value.content
    })
    ElMessage.success('评论发表成功')
    commentForm.value.content = ''
    loadComments()
  } catch (error) {
    console.error('Failed to submit comment:', error)
    ElMessage.error('评论发表失败')
  } finally {
    submitting.value = false
  }
}

// 跳转到文章
function goToArticle(id: number) {
  router.push(`/blog/${id}`)
}

// 返回博客列表
function goBack() {
  router.push('/blog')
}

// 滚动到指定标题
function scrollToHeading(id: string) {
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}

// 格式化日期
function formatDate(dateStr: string): string {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 格式化日期时间
function formatDateTime(dateStr: string): string {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadArticle()
  loadComments()
  loadAdjacentArticles()
})
</script>

<style scoped lang="scss">
.blog-detail {
  padding: 20px 0;
}

.loading-wrapper {
  padding: 40px 0;
}

.article-card {
  margin-bottom: 20px;
  
  :deep(.el-card__body) {
    padding: 24px;
  }
}

.article-header {
  margin-bottom: 24px;
}

.article-title {
  margin: 0 0 16px 0;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--el-text-color-primary);
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.publish-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.article-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.article-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag-item {
  border: none;
}

.article-cover {
  margin: 0 -24px 24px;
  
  .el-image {
    width: 100%;
    max-height: 400px;
    
    :deep(img) {
      width: 100%;
      object-fit: cover;
    }
  }
}

.article-content {
  font-size: 16px;
  line-height: 1.8;
  color: var(--el-text-color-regular);
  
  :deep(p) {
    margin: 16px 0;
  }
  
  :deep(h2), :deep(h3), :deep(h4) {
    margin: 24px 0 16px;
    color: var(--el-text-color-primary);
  }
  
  :deep(code) {
    background: var(--el-fill-color);
    padding: 2px 6px;
    border-radius: 4px;
    font-family: monospace;
  }
  
  :deep(pre) {
    background: var(--el-fill-color-dark);
    padding: 16px;
    border-radius: 8px;
    overflow-x: auto;
    
    code {
      background: transparent;
      padding: 0;
    }
  }
  
  :deep(blockquote) {
    border-left: 4px solid var(--el-color-primary);
    margin: 16px 0;
    padding: 8px 16px;
    background: var(--el-fill-color-light);
    border-radius: 0 4px 4px 0;
  }
}

.article-nav {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.nav-card {
  flex: 1;
  cursor: pointer;
  transition: transform 0.2s;
  
  &:hover {
    transform: translateY(-2px);
  }
  
  :deep(.el-card__body) {
    padding: 16px;
  }
  
  &.next {
    text-align: right;
  }
}

.nav-placeholder {
  flex: 1;
}

.nav-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
  
  .next & {
    justify-content: flex-end;
  }
}

.nav-title {
  font-weight: 600;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.comments-card {
  :deep(.el-card__header) {
    padding: 16px 24px;
  }
  
  :deep(.el-card__body) {
    padding: 24px;
  }
}

.comments-header {
  font-weight: 600;
  font-size: 16px;
}

.comment-form {
  margin-bottom: 24px;
  
  h4 {
    margin: 0 0 16px 0;
    font-size: 16px;
  }
}

.comments-loading {
  padding: 20px 0;
}

.no-comments {
  padding: 40px 0;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.comment-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.comment-content {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: var(--el-text-color-regular);
}

.not-found {
  padding: 80px 0;
}

.detail-sidebar {
  position: sticky;
  top: 80px;
}

.sidebar-card {
  margin-bottom: 16px;
  
  :deep(.el-card__header) {
    padding: 12px 16px;
  }
  
  :deep(.el-card__body) {
    padding: 12px;
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.toc-content {
  font-size: 14px;
}

.toc-empty {
  color: var(--el-text-color-secondary);
  text-align: center;
  padding: 16px 0;
}

.toc-item {
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 4px;
  color: var(--el-text-color-regular);
  transition: all 0.2s;
  
  &:hover {
    background: var(--el-fill-color-light);
    color: var(--el-color-primary);
  }
  
  &.toc-level-2 {
    padding-left: 24px;
  }
  
  &.toc-level-3 {
    padding-left: 36px;
  }
}

.related-empty {
  color: var(--el-text-color-secondary);
  text-align: center;
  padding: 16px 0;
}

.related-list {
  display: flex;
  flex-direction: column;
}

.related-item {
  padding: 12px;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.2s;
  
  &:hover {
    background: var(--el-fill-color-light);
  }
}

.related-title {
  display: block;
  font-size: 14px;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.related-date {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

@media (max-width: 992px) {
  .detail-sidebar {
    position: static;
    margin-top: 24px;
  }
  
  .article-nav {
    flex-direction: column;
  }
  
  .nav-placeholder {
    display: none;
  }
}

@media (max-width: 768px) {
  .article-title {
    font-size: 22px;
  }
  
  .article-meta {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .article-stats {
    width: 100%;
    justify-content: flex-start;
  }
  
  .article-card :deep(.el-card__body) {
    padding: 16px;
  }
  
  .article-cover {
    margin: 0 -16px 16px;
  }
}
</style>
