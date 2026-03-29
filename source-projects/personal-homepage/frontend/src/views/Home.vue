<template>
  <div class="home">
    <!-- Hero Section -->
    <section class="hero">
      <div class="hero-content">
        <el-avatar :size="120" :src="profile?.avatar || defaultAvatar" class="avatar" />
        <h1 class="name">{{ profile?.name || 'Developer' }}</h1>
        <p class="title">{{ profile?.title || 'Full Stack Developer' }}</p>
        <div class="social-links">
          <el-button v-if="profile?.github" circle :icon="Link" @click="openLink(profile.github)" />
          <el-button v-if="profile?.email" circle :icon="Message" @click="openLink(`mailto:${profile.email}`)" />
        </div>
      </div>
    </section>

    <!-- About Section -->
    <section class="section">
      <h2 class="section-title">
        <el-icon><User /></el-icon>
        关于我
      </h2>
      <p class="about-text">{{ profile?.bio || '热爱编程，专注于 Web 开发。喜欢探索新技术，分享学习心得。' }}</p>
    </section>

    <!-- Skills Section -->
    <section class="section">
      <h2 class="section-title">
        <el-icon><Star /></el-icon>
        技能栈
      </h2>
      <div class="skills-container">
        <div v-for="(skills, category) in profile?.skillsByCategory" :key="category" class="skill-category">
          <h3 class="category-name">{{ category }}</h3>
          <div class="skill-list">
            <div v-for="skill in skills" :key="skill.id" class="skill-item">
              <span class="skill-name">{{ skill.name }}</span>
              <el-progress 
                :percentage="skill.proficiencyLevel" 
                :color="progressColors"
                :show-text="false"
                :stroke-width="8"
              />
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Projects Section -->
    <section class="section">
      <h2 class="section-title">
        <el-icon><Collection /></el-icon>
        项目经历
      </h2>
      <el-timeline>
        <el-timeline-item
          v-for="project in profile?.projects"
          :key="project.id"
          :timestamp="formatProjectDate(project)"
          placement="top"
        >
          <el-card class="project-card">
            <template #header>
              <div class="project-header">
                <span class="project-name">{{ project.name }}</span>
                <el-tag v-if="project.isFeatured" type="warning" size="small">精选</el-tag>
              </div>
            </template>
            <p class="project-desc">{{ project.description }}</p>
            <div class="project-tech" v-if="project.technologies">
              <el-tag v-for="tech in project.technologies.slice(0, 5)" :key="tech" size="small" effect="plain">
                {{ tech }}
              </el-tag>
            </div>
            <div class="project-links">
              <el-button v-if="project.githubUrl" link type="primary" @click="openLink(project.githubUrl)">
                <el-icon><Link /></el-icon> GitHub
              </el-button>
              <el-button v-if="project.url" link type="primary" @click="openLink(project.url)">
                <el-icon><View /></el-icon> 预览
              </el-button>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </section>

    <!-- Contact Section -->
    <section class="section">
      <h2 class="section-title">
        <el-icon><Message /></el-icon>
        联系方式
      </h2>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" v-for="contact in contacts" :key="contact.type">
          <el-card class="contact-card" shadow="hover" @click="openLink(contact.link)">
            <el-icon :size="32" class="contact-icon">
              <component :is="contact.icon" />
            </el-icon>
            <h4>{{ contact.label }}</h4>
            <p>{{ contact.value }}</p>
          </el-card>
        </el-col>
      </el-row>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { User, Star, Collection, Message, Link, View } from '@element-plus/icons-vue'
import { getProfile } from '@/api/profile'
import type { Profile } from '@/types'

const profile = ref<Profile>()
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const progressColors = [
  { color: '#f56c6c', percentage: 20 },
  { color: '#e6a23c', percentage: 40 },
  { color: '#5cb87a', percentage: 60 },
  { color: '#1989fa', percentage: 80 },
  { color: '#6f7ad3', percentage: 100 }
]

const contacts = ref([
  { type: 'github', label: 'GitHub', value: 'View Profile', icon: 'Link', link: '#' },
  { type: 'email', label: 'Email', value: 'Send Email', icon: 'Message', link: '#' }
])

function openLink(url: string) {
  window.open(url, '_blank')
}

function formatProjectDate(project: any) {
  const start = project.startDate || 'Unknown'
  const end = project.endDate || 'Present'
  return `${start} - ${end}`
}

async function loadProfile() {
  try {
    profile.value = await getProfile()
    if (profile.value) {
      contacts.value = [
        { 
          type: 'github', 
          label: 'GitHub', 
          value: profile.value.github || 'Not set', 
          icon: 'Link', 
          link: profile.value.github || '#' 
        },
        { 
          type: 'email', 
          label: 'Email', 
          value: profile.value.email || 'Not set', 
          icon: 'Message', 
          link: profile.value.email ? `mailto:${profile.value.email}` : '#' 
        }
      ]
    }
  } catch (error) {
    console.error('Failed to load profile:', error)
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped lang="scss">
.home {
  padding: 20px 0;
}

.hero {
  text-align: center;
  padding: 60px 20px;
  background: linear-gradient(135deg, var(--el-color-primary-light-9) 0%, var(--el-bg-color) 100%);
  border-radius: 16px;
  margin-bottom: 40px;
}

.hero-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.avatar {
  border: 4px solid var(--el-color-primary);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.name {
  font-size: 36px;
  font-weight: bold;
  color: var(--el-text-color-primary);
  margin: 0;
}

.title {
  font-size: 18px;
  color: var(--el-text-color-secondary);
  margin: 0;
}

.social-links {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.section {
  margin-bottom: 40px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 24px;
  margin-bottom: 20px;
  color: var(--el-text-color-primary);
}

.about-text {
  font-size: 16px;
  line-height: 1.8;
  color: var(--el-text-color-regular);
}

.skills-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.skill-category {
  background: var(--el-fill-color-light);
  padding: 20px;
  border-radius: 12px;
}

.category-name {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 16px;
  color: var(--el-color-primary);
}

.skill-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skill-item {
  .skill-name {
    display: block;
    margin-bottom: 4px;
    font-size: 14px;
    color: var(--el-text-color-regular);
  }
}

.project-card {
  margin-bottom: 8px;
  
  .project-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .project-name {
    font-weight: bold;
    font-size: 16px;
  }
  
  .project-desc {
    color: var(--el-text-color-secondary);
    margin-bottom: 12px;
    line-height: 1.6;
  }
  
  .project-tech {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 12px;
  }
  
  .project-links {
    display: flex;
    gap: 16px;
  }
}

.contact-card {
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s;
  
  &:hover {
    transform: translateY(-4px);
  }
  
  .contact-icon {
    color: var(--el-color-primary);
    margin-bottom: 12px;
  }
  
  h4 {
    margin: 0 0 8px 0;
    font-size: 16px;
  }
  
  p {
    margin: 0;
    color: var(--el-text-color-secondary);
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  .hero {
    padding: 40px 20px;
  }
  
  .name {
    font-size: 28px;
  }
  
  .skills-container {
    grid-template-columns: 1fr;
  }
}
</style>
