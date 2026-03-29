<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <el-icon size="28"><HomeFilled /></el-icon>
          <span class="logo-text">My Home</span>
        </div>
        
        <div class="nav-menu" :class="{ 'is-mobile': isMobile, 'is-open': mobileMenuOpen }">
          <el-menu
            :default-active="$route.path"
            mode="horizontal"
            :ellipsis="false"
            router
            :class="{ 'mobile-menu': isMobile }"
          >
            <el-menu-item index="/">首页</el-menu-item>
            <el-menu-item index="/blog">博客</el-menu-item>
            <el-menu-item index="/files">文件</el-menu-item>
            <el-menu-item index="/tools">工具箱</el-menu-item>
            <el-menu-item index="/kimi">Kimi AI</el-menu-item>
          </el-menu>
          
          <div class="mobile-close" v-if="isMobile" @click="mobileMenuOpen = false">
            <el-icon><Close /></el-icon>
          </div>
        </div>
        
        <div class="header-actions">
          <el-button
            circle
            :icon="themeStore.isDark ? Sunny : Moon"
            @click="themeStore.toggleTheme"
          />
          <el-button
            v-if="isMobile"
            circle
            :icon="Menu"
            @click="mobileMenuOpen = true"
          />
        </div>
      </div>
    </el-header>
    
    <el-main class="main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>
    
    <el-footer class="footer">
      <div class="footer-content">
        <p>&copy; {{ new Date().getFullYear() }} Personal Homepage. Built with Spring Boot & Vue 3.</p>
      </div>
    </el-footer>
  </el-container>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { HomeFilled, Moon, Sunny, Menu, Close } from '@element-plus/icons-vue'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
const isMobile = ref(false)
const mobileMenuOpen = ref(false)

function checkMobile() {
  isMobile.value = window.innerWidth < 768
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped lang="scss">
.layout {
  min-height: 100vh;
}

.header {
  background-color: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color);
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--el-color-primary);
  
  .logo-text {
    font-size: 20px;
    font-weight: bold;
  }
}

.nav-menu {
  flex: 1;
  display: flex;
  justify-content: center;
  
  :deep(.el-menu) {
    border-bottom: none;
    background: transparent;
  }
  
  &.is-mobile {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: var(--el-bg-color);
    z-index: 200;
    flex-direction: column;
    justify-content: flex-start;
    padding-top: 60px;
    transform: translateX(-100%);
    transition: transform 0.3s;
    
    &.is-open {
      transform: translateX(0);
    }
    
    .mobile-menu {
      flex-direction: column;
      width: 100%;
      border-right: none;
      
      :deep(.el-menu-item) {
        height: 50px;
        line-height: 50px;
        font-size: 16px;
      }
    }
  }
}

.mobile-close {
  position: absolute;
  top: 15px;
  right: 20px;
  font-size: 24px;
  cursor: pointer;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.main {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
  min-height: calc(100vh - 120px);
}

.footer {
  background-color: var(--el-bg-color);
  border-top: 1px solid var(--el-border-color);
  padding: 20px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
  color: var(--el-text-color-secondary);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
