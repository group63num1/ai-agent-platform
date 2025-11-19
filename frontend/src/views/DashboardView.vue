<template>
  <div class="dashboard-container">
    <el-card class="welcome-card mb-6" shadow="hover">
      <div class="flex items-center gap-4">
        <el-avatar :size="60" class="border-2 border-blue-200">
          <span class="text-2xl">{{ userStore.user?.username?.[0] || 'U' }}</span>
        </el-avatar>
        <div>
          <h2 class="text-xl font-bold text-gray-800">
            欢迎回来，{{ userStore.user?.username || '用户' }}！
          </h2>
          <p class="text-sm text-gray-500 mt-1">今天是 {{ currentDate }}</p>
        </div>
      </div>
    </el-card>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
      <el-card 
        class="quick-action-card cursor-pointer hover:shadow-lg transition-shadow"
        shadow="hover"
        @click="router.push('/home/apps')"
      >
        <div class="flex items-center gap-4">
          <el-icon :size="40" class="text-blue-500">
            <Grid />
          </el-icon>
          <div>
            <h3 class="font-semibold text-gray-800">应用管理</h3>
            <p class="text-sm text-gray-500">查看和管理应用</p>
          </div>
        </div>
      </el-card>

      <el-card 
        class="quick-action-card cursor-pointer hover:shadow-lg transition-shadow"
        shadow="hover"
        @click="router.push('/home/profile')"
      >
        <div class="flex items-center gap-4">
          <el-icon :size="40" class="text-purple-500">
            <User />
          </el-icon>
          <div>
            <h3 class="font-semibold text-gray-800">个人信息</h3>
            <p class="text-sm text-gray-500">查看和编辑个人信息</p>
          </div>
        </div>
      </el-card>
    </div>

    <el-card shadow="hover">
      <template #header>
        <h3 class="text-lg font-semibold text-gray-800">系统信息</h3>
      </template>
      <div class="space-y-3">
        <div class="flex items-center justify-between">
          <span class="text-gray-600">系统名称</span>
          <span class="font-medium text-gray-800">用户权限管理系统</span>
        </div>
        <div class="flex items-center justify-between">
          <span class="text-gray-600">当前用户</span>
          <span class="font-medium text-gray-800">{{ userStore.user?.username || '未知' }}</span>
        </div>
        <div class="flex items-center justify-between">
          <span class="text-gray-600">登录状态</span>
          <el-tag type="success">已登录</el-tag>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Grid, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const currentDate = computed(() => {
  const date = new Date()
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const weekday = weekdays[date.getDay()]
  return `${year}年${month}月${day}日 ${weekday}`
})
</script>

<style scoped>
.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card {
  border-radius: 12px;
}

.quick-action-card {
  border-radius: 12px;
  transition: all 0.3s ease;
}

.quick-action-card:hover {
  transform: translateY(-2px);
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>

