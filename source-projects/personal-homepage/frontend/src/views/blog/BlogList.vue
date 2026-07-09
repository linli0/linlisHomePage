<template>
  <div class="blog-list">
    <!-- 搜索栏 -->
    <div class="search-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索文章..."
        size="large"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <el-row :gutter="24">
      <!-- 侧边栏 -->
      <el-col :xs="24" :sm="24" :md="6" :lg="5">
        <div class="sidebar">
          <!-- 分类筛选 -->
          <el-card class="sidebar-card" shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon><Folder /></el-icon>
                <span>文章分类</span>
              </div>
            </template>
            <el-menu
              :default-active="String(selectedCategory)"
              class="category-menu"
              @select="handleCategorySelect"
            >
              <el-menu-item index="0">
                <span>全部文章</span>
                <el-tag size="small" type="info" class="count-tag">
                  {{ totalArticles }}
                </el-tag>
              </el-menu-item>
              <el-menu-item
                v-for="category in categories"
                :key="category.id"
                :index="String(category.id)"
              >
                <span>{{ category.name }}</span>
                <el-tag size="small" type="info" class="count-tag">
                  {{ category.articleCount }}
                </el-tag>
              </el-menu-item>
            </el-menu>
          </el-card>

          <!-- 标签云 -->
          <el-card class="sidebar-card" shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon><CollectionTag /></el-icon>
                <span>热门标签</span>
              </div>
            </template>
            <div class="tag-cloud">
              <el-check-tag
                v-for="tag in tags"
                :key="tag.id"
                :checked="selectedTag === tag.id"
                :style="getTagStyle(tag)"
                @change="handleTagSelect(tag.id)"
                class="tag-item"
              >
                {{ tag.name }}
              </el-check-tag>
            </div>
          </el-card>
        </div>
      </el-col>

      <!-- 文章列表 -->
      <el-col :xs="24" :sm="24" :md="18" :lg="19">
        <div class="article-section">
          <!-- 筛选提示 -->
          <div v-if="selectedCategory || selectedTag || searchKeyword" class="filter-bar">
            <span class="filter-text">筛选条件:</span>
            <el-tag
              v-if="selectedCategory"
              closable
              @close="clearCategoryFilter"
              class="filter-tag"
            >
              分类: {{ getCategoryName(selectedCategory) }}
            </el-tag>
            <el-tag
              v-if="selectedTag"
              closable
              @close="clearTagFilter"
              class="filter-tag"
            >
              标签: {{ getTagName(selectedTag) }}
            </el-tag>
            <el-tag
              v-if="searchKeyword"
              closable
              @close="clearSearchFilter"
              class="filter-tag"
            >
              搜索: {{ searchKeyword }}
            </el-tag>
          </div>

          <!-- 加载状态 -->
          <div v-if="loading" class="loading-wrapper">
            <el-skeleton :rows="5" animated />
            <el-skeleton :rows="5" animated />
          </div>

          <!-- 文章卡片列表 -->
          <template v-else>
            <div v-if="articles.length === 0" class="empty-state">
              <el-empty description="暂无文章" />
            </div>
            <div v-else class="article-list">
              <el-card
                v-for="article in articles"
                :key="article.id"
                class="article-card"
                shadow="hover"
                @click="goToArticle(article.id)"
              >
                <div class="article-content">
                  <!-- 封面图 -->
                  <div v-if="article.coverImage" class="article-cover">
                    <el-image
                      :src="article.coverImage"
                      fit="cover"
                      :preview-src-list="[]"
                    />
                  </div>
                  
                  <div class="article-info">
                    <!-- 标题 -->
                    <h3 class="article-title">{{ article.title }}</h3>
                    
                    <!-- 摘要 -->
                    <p class="article-summary">{{ article.summary }}</p>
                    
                    <!-- 元信息 -->
                    <div class="article-meta">
                      <span class="meta-item">
                        <el-icon><Calendar /></el-icon>
                        {{ formatDate(article.createdAt) }}
                      </span>
                      <span class="meta-item">
                        <el-icon><View /></el-icon>
                        {{ article.viewCount }} 阅读
                      </span>
                      <span class="meta-item">
                        <el-icon><ChatDotRound /></el-icon>
                        {{ article.commentCount }} 评论
                      </span>
                      <el-tag
                        v-if="article.category"
                        size="small"
                        effect="plain"
                        class="category-tag"
                      >
                        {{ article.category.name }}
                      </el-tag>
                    </div>
                    
                    <!-- 标签 -->
                    <div v-if="article.tags?.length" class="article-tags">
                      <el-tag
                        v-for="tag in article.tags.slice(0, 3)"
                        :key="tag.id"
                        size="small"
                        :color="tag.color"
                        effect="dark"
                      >
                        {{ tag.name }}
                      </el-tag>
                    </div>
                  </div>
                </div>
              </el-card>
            </div>

            <!-- 分页 -->
            <div v-if="totalPages > 1" class="pagination-wrapper">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :total="totalElements"
                :page-sizes="[10, 20, 30, 50]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handlePageChange"
              />
            </div>
          </template>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, Folder, CollectionTag, Calendar, View, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getArticles, searchArticles } from '@/api/article'
import { getCategories } from '@/api/category'
import { getTagsWithCount } from '@/api/tag'
import type { Article, Category, Tag } from '@/types'

const router = useRouter()
const route = useRoute()

// 状态
const loading = ref(false)
const articles = ref<Article[]>([])
const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])
const totalArticles = ref(0)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const totalElements = ref(0)
const totalPages = ref(0)

// 筛选
const selectedCategory = ref<number | null>(null)
const selectedTag = ref<number | null>(null)
const searchKeyword = ref('')

// 加载文章列表
async function loadArticles() {
  loading.value = true
  try {
    let result
    if (searchKeyword.value) {
      result = await searchArticles({
        keyword: searchKeyword.value,
        page: currentPage.value - 1,
        size: pageSize.value
      })
    } else {
      result = await getArticles({
        page: currentPage.value - 1,
        size: pageSize.value,
        categoryId: selectedCategory.value || undefined,
        tagId: selectedTag.value || undefined
      })
    }
    articles.value = result.content
    totalElements.value = result.totalElements
    totalPages.value = result.totalPages
  } catch (error) {
    console.error('Failed to load articles:', error)
    ElMessage.error('加载文章失败')
  } finally {
    loading.value = false
  }
}

// 加载分类
async function loadCategories() {
  try {
    categories.value = await getCategories()
    totalArticles.value = categories.value.reduce((sum, cat) => sum + (cat.articleCount || 0), 0)
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

// 加载标签
async function loadTags() {
  try {
    tags.value = await getTagsWithCount()
  } catch (error) {
    console.error('Failed to load tags:', error)
  }
}

// 分类选择
function handleCategorySelect(index: string) {
  const categoryId = parseInt(index)
  selectedCategory.value = categoryId === 0 ? null : categoryId
  currentPage.value = 1
  loadArticles()
  updateQueryParams()
}

// 标签选择
function handleTagSelect(tagId: number) {
  selectedTag.value = selectedTag.value === tagId ? null : tagId
  currentPage.value = 1
  loadArticles()
  updateQueryParams()
}

// 搜索
function handleSearch() {
  currentPage.value = 1
  loadArticles()
  updateQueryParams()
}

// 清除筛选
function clearCategoryFilter() {
  selectedCategory.value = null
  loadArticles()
  updateQueryParams()
}

function clearTagFilter() {
  selectedTag.value = null
  loadArticles()
  updateQueryParams()
}

function clearSearchFilter() {
  searchKeyword.value = ''
  loadArticles()
  updateQueryParams()
}

// 分页
function handleSizeChange(size: number) {
  pageSize.value = size
  currentPage.value = 1
  loadArticles()
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 跳转到文章详情
function goToArticle(id: number) {
  router.push(`/blog/${id}`)
}

// 获取分类名
function getCategoryName(id: number | null): string {
  if (!id) return '全部'
  const category = categories.value.find(c => c.id === id)
  return category?.name || '未知'
}

// 获取标签名
function getTagName(id: number | null): string {
  if (!id) return ''
  const tag = tags.value.find(t => t.id === id)
  return tag?.name || '未知'
}

// 标签样式
function getTagStyle(tag: Tag): Record<string, string> {
  const isSelected = selectedTag.value === tag.id
  return {
    backgroundColor: isSelected ? tag.color : 'var(--el-fill-color)',
    color: isSelected ? '#fff' : 'var(--el-text-color-regular)',
    borderColor: tag.color
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

// 更新 URL 查询参数
function updateQueryParams() {
  const query: Record<string, string> = {}
  if (selectedCategory.value) query.category = String(selectedCategory.value)
  if (selectedTag.value) query.tag = String(selectedTag.value)
  if (searchKeyword.value) query.search = searchKeyword.value
  if (currentPage.value > 1) query.page = String(currentPage.value)
  
  router.replace({ query })
}

// 从 URL 解析查询参数
function parseQueryParams() {
  const { category, tag, search, page } = route.query
  if (category) selectedCategory.value = parseInt(category as string)
  if (tag) selectedTag.value = parseInt(tag as string)
  if (search) searchKeyword.value = search as string
  if (page) currentPage.value = parseInt(page as string)
}

// 监听路由变化
watch(() => route.query, () => {
  parseQueryParams()
  loadArticles()
}, { immediate: false })

onMounted(() => {
  parseQueryParams()
  loadCategories()
  loadTags()
  loadArticles()
})
</script>

<style scoped lang="scss">
.blog-list {
  padding: 20px 0;
}

.search-section {
  margin-bottom: 24px;
  max-width: 600px;
}

.sidebar {
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

.category-menu {
  border-right: none;
  
  :deep(.el-menu-item) {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 40px;
    line-height: 40px;
    padding: 0 12px;
    border-radius: 4px;
    margin-bottom: 4px;
    
    &.is-active {
      background-color: var(--el-color-primary-light-9);
    }
  }
}

.count-tag {
  margin-left: 8px;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
  
  &:hover {
    opacity: 0.8;
  }
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.filter-text {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.filter-tag {
  margin-right: 0;
}

.loading-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.empty-state {
  padding: 60px 0;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.article-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--el-box-shadow-light);
  }
  
  :deep(.el-card__body) {
    padding: 16px;
  }
}

.article-content {
  display: flex;
  gap: 16px;
}

.article-cover {
  flex-shrink: 0;
  width: 180px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  
  .el-image {
    width: 100%;
    height: 100%;
  }
  
  :deep(img) {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.article-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.article-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-summary {
  margin: 0;
  font-size: 14px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.category-tag {
  margin-left: auto;
}

.article-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination-wrapper {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .sidebar {
    position: static;
    margin-bottom: 24px;
  }
  
  .article-content {
    flex-direction: column;
  }
  
  .article-cover {
    width: 100%;
    height: 160px;
  }
  
  .article-title {
    white-space: normal;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
  
  .category-tag {
    margin-left: 0;
  }
}
</style>
