import { createRouter, createWebHistory } from 'vue-router'

const Login = () => import('@/views/LoginView.vue')
const Home = () => import('@/views/HomeView.vue')
const Dashboard = () => import('@/views/DashboardView.vue')
const Profile = () => import('@/views/ProfileView.vue')
const Plugins = () => import('@/views/PluginsView.vue')
const Chat = () => import('@/views/ChatView.vue')
const Agents = () => import('@/views/AgentsView.vue')
const AgentDetail = () => import('@/views/AgentDetailView.vue')
const AgentStudio = () => import('@/views/AgentStudioView.vue')
const KnowledgeBases = () => import('@/views/KnowledgeBasesView.vue')
const KnowledgeBaseDetail = () => import('@/views/KnowledgeBaseDetailView.vue')
const Products = () => import('@/views/ProductsView.vue')
const ProductChat = () => import('@/views/ProductChatView.vue')

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
        // 兼容旧的 /home/apps 子路由：移除应用模块后，重定向到首页
        {
          path: 'apps',
          redirect: '/home'
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
        ,
        {
          path: 'agents',
          name: 'agents',
          component: Agents,
          meta: {
            title: '智能体管理',
            requiresAuth: true
          }
        },
        {
          path: 'products',
          name: 'products',
          component: Products,
          meta: {
            title: '产品管理',
            requiresAuth: true
          }
        },
        {
          path: 'products/:id/chat',
          name: 'productChat',
          component: ProductChat,
          meta: {
            title: '产品聊天',
            requiresAuth: true
          }
        },
        {
          path: 'agents/:id/studio',
          name: 'agentStudio',
          component: AgentStudio,
          meta: {
            title: '智能体工作台',
            requiresAuth: true
          }
        },
        {
          path: 'agents/:id',
          name: 'agentDetail',
          component: AgentDetail,
          meta: {
            title: '智能体详情',
            requiresAuth: true
          }
        },
        {
          path: 'knowledge-bases',
          name: 'knowledgeBases',
          component: KnowledgeBases,
          meta: {
            title: '知识库管理',
            requiresAuth: true
          }
        },
        {
          path: 'knowledge-bases/:id',
          name: 'knowledgeBaseDetail',
          component: KnowledgeBaseDetail,
          meta: {
            title: '知识库详情',
            requiresAuth: true
          }
        }
      ]
    },
    // 兼容旧的 /apps 路径：移除应用模块后，统一重定向到首页
    {
      path: '/apps',
      redirect: '/home'
    }
  ],
})

// 路由守卫：检查是否需要登录
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const isLoginPage = to.name === 'login' || to.path === '/'
  // 检查是否为应用首次启动的初始导航（from.name 在首次导航时通常为 undefined/null）
  const isInitialNavigation = !from || !from.name
  
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
  
  // 2. 应用启动时（首次导航）仅在无 token 情况下强制跳转到登录页
  if (isInitialNavigation && !isLoginPage && !token) {
    next({ name: 'login', query: { redirect: to.fullPath } })
    return
  }

  // 3. 已登录用户访问登录页自动跳转首页
  if (isLoginPage && token) {
    next('/home')
    return
  }
  
  // 3. 已登录用户访问需要认证的页面，允许通过
  // Token失效的情况会在HTTP拦截器中处理（返回401时自动跳转登录页）
  next()
})

export default router
