<template>
  <div class="layout-container h-screen flex flex-col bg-gray-50">
    <header class="top-navbar bg-white shadow-sm border-b border-gray-200">
      <div class="flex items-center justify-between h-16 px-6">
        <div class="flex items-center gap-4">
          <h1 class="text-xl font-bold text-gray-800">用户权限管理系统</h1>
        </div>
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-2 text-gray-600">
            <el-icon><User /></el-icon>
            <span class="text-sm font-medium">{{ userStore.user?.username || '未知用户' }}</span>
          </div>
          <el-button
            type="danger"
            size="small"
            :icon="SwitchButton"
            @click="handleLogout"
          >
            退出登录
          </el-button>
        </div>
      </div>
    </header>

    <div class="flex flex-1 overflow-hidden">
      <aside class="sidebar bg-white border-r border-gray-200 w-64 flex-shrink-0 overflow-y-auto">
        <div v-if="menuLoading" class="p-4">
          <el-skeleton :rows="5" animated />
        </div>
        <el-menu
          v-else
          :default-active="activeMenu"
          :collapse="false"
          router
          class="border-none"
        >
          <template v-for="menu in menuList" :key="menu.path">
            <el-menu-item
              v-if="!menu.children || menu.children.length === 0"
              :index="menu.path"
              @click="handleMenuClick(menu)"
            >
              <el-icon v-if="menu.icon">
                <component :is="menu.icon" />
              </el-icon>
              <template #title>{{ menu.title }}</template>
            </el-menu-item>
            <el-sub-menu v-else-if="menu.children && menu.children.length > 0" :index="menu.path">
              <template #title>
                <el-icon v-if="menu.icon">
                  <component :is="menu.icon" />
                </el-icon>
                <span>{{ menu.title }}</span>
              </template>
              <el-menu-item
                v-for="child in menu.children"
                :key="child.path"
                :index="child.path"
                @click="handleMenuClick(child)"
              >
                <template #title>{{ child.title }}</template>
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </aside>

      <main class="main-content flex-1 overflow-y-auto p-6">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  User, 
  SwitchButton, 
  House, 
  Grid, 
  UserFilled, 
  Lock, 
  OfficeBuilding, 
  Setting 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { logout } from '@/api/auth'
import { getMenus } from '@/api/menu'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const menuLoading = ref(false)
const activeMenu = computed(() => route.path)
const iconMap = {
  House,
  Grid,
  User,
  UserFilled,
  Lock,
  OfficeBuilding,
  Setting
}
const menuList = ref([])

const getIconComponent = (iconName) => {
  if (!iconName) return null
  return iconMap[iconName] || null
}

const transformMenuData = (menus) => {
  return menus
    .filter(menu => {
      // 过滤掉团队管理菜单
      return menu.title !== '团队管理' && menu.path !== '/home/teams'
    })
    .map(menu => ({
      title: menu.title,
      path: menu.path,
      icon: getIconComponent(menu.icon),
      permission: menu.permission,
      sortOrder: menu.sortOrder || 0,
      children: menu.children ? transformMenuData(menu.children) : []
    }))
    .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
}

const loadMenus = async () => {
  menuLoading.value = true
  try {
    const menus = await getMenus();
    const transformed = transformMenuData(menus);

    // // 插件管理菜单始终存在
    // const hasPluginMenu = transformed.some(item => item.path === '/home/plugins');
    // if (!hasPluginMenu) {
    //   transformed.splice(1, 0, {
    //     title: '插件管理',
    //     path: '/home/plugins',
    //     icon: iconMap.Setting
    //   });
    // }

    // // 知识库管理菜单始终存在
    // const hasKBMenu = transformed.some(item => item.path === '/home/knowledge-bases');
    // if (!hasKBMenu) {
    //   transformed.splice(1, 0, {
    //     title: '知识库管理',
    //     path: '/home/knowledge-bases',
    //     icon: iconMap.Grid
    //   });
    // }

    // // 确保包含智能体管理菜单（后端未返回时补充）
    // const hasAgentsMenu = transformed.some(item => item.path === '/home/agents');
    // if (!hasAgentsMenu) {
    //   transformed.splice(1, 0, {
    //     title: '智能体管理',
    //     path: '/home/agents',
    //     icon: iconMap.Grid
    //   });
    // }

    menuList.value = transformed;
  } catch (error) {
    console.error('加载菜单失败:', error)
    ElMessage.error('加载菜单失败: ' + (error.message || '未知错误'))
    menuList.value = [
      { title: '首页', path: '/home', icon: iconMap.House },
      { title: '应用管理', path: '/home/apps', icon: iconMap.Grid },
      { title: '知识库管理', path: '/home/knowledge-bases', icon: iconMap.Grid },
      { title: '插件管理', path: '/home/plugins', icon: iconMap.Setting },
      { title: '智能体管理', path: '/home/agents', icon: iconMap.Grid },
      { title: '个人信息', path: '/home/profile', icon: iconMap.User }
    ]
  } finally {
    menuLoading.value = false
  }
}

const handleMenuClick = (menu) => {
  if (menu.path) {
    router.push(menu.path)
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    try {
      await logout()
    } catch (error) {
      console.warn('登出接口调用失败（将清除本地信息）:', error)
    }
    userStore.clearUser()
    router.push('/')
    ElMessage.success('已退出登录')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出登录失败:', error)
      ElMessage.error('退出登录失败')
    }
  }
}

onMounted(() => {
  if (route.path === '/' && userStore.isLoggedIn()) {
    router.push('/home')
  }
  if (userStore.isLoggedIn()) {
    loadMenus()
  }
})
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.top-navbar {
  z-index: 1000;
}

.sidebar {
  height: calc(100vh - 64px);
}

.main-content {
  background-color: #f5f7fa;
}

:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
}

:deep(.el-sub-menu__title) {
  height: 48px;
  line-height: 48px;
}
</style>

