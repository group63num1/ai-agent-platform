import { createRouter, createWebHistory } from 'vue-router'

const Login = () => import('@/views/LoginView.vue')
const Home = () => import('@/views/HomeView.vue')
const Dashboard = () => import('@/views/DashboardView.vue')
const Apps = () => import('@/views/AppsView.vue')
const Profile = () => import('@/views/ProfileView.vue')
const Plugins = () => import('@/views/PluginsView.vue')
const Chat = () => import('@/views/ChatView.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: Login,
      meta: {
        title: '用户登录'
      }
    },
    {
      path: '/home',
      name: 'home',
      component: Home,
      meta: {
        title: '首页',
        requiresAuth: true
      },
      children: [
        {
          path: '',
          name: 'dashboard',
          component: Dashboard,
          meta: {
            title: '首页',
            requiresAuth: true
          }
        },
        {
          path: 'apps',
          name: 'apps',
          component: Apps,
          meta: {
            title: '应用列表',
            requiresAuth: true
          }
        },
        {
          path: 'profile',
          name: 'profile',
          component: Profile,
          meta: {
            title: '个人信息',
            requiresAuth: true
          }
        },
        {
          path: 'chat',
          name: 'chat',
          component: Chat,
          meta: {
            title: '智能客服助手',
            requiresAuth: true
          }
        },
        {
          path: 'plugins',
          name: 'plugins',
          component: Plugins,
          meta: {
            title: '插件管理',
            requiresAuth: true
          }
        }
      ]
    },
    // 兼容旧的 /apps 路径，重定向到 /home/apps
    {
      path: '/apps',
      redirect: '/home/apps'
    }
  ],
})

// 路由守卫：检查是否需要登录
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const isLoginPage = to.name === 'login' || to.path === '/'
  
  // 1. 未登录用户只能访问登录页
  if (!token) {
    // 如果访问的不是登录页，跳转到登录页
    if (!isLoginPage) {
      next({ 
        name: 'login', 
        query: { redirect: to.fullPath } 
      })
    } else {
      // 访问登录页，允许通过
      next()
    }
    return
  }
  
  // 2. 已登录用户访问登录页自动跳转首页
  if (isLoginPage && token) {
    next('/home')
    return
  }
  
  // 3. 已登录用户访问需要认证的页面，允许通过
  // Token失效的情况会在HTTP拦截器中处理（返回401时自动跳转登录页）
  next()
})

export default router
