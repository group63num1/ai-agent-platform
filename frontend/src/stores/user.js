import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 用户信息
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || '')

  /**
   * 设置用户信息
   * @param {Object} userData - 用户数据
   * @param {number} userData.userId - 用户ID
   * @param {string} userData.username - 用户名
   * @param {string} userData.token - JWT Token
   */
  const setUser = (userData) => {
    user.value = {
      userId: userData.userId,
      username: userData.username
    }
    token.value = userData.token
    
    // 将 token 存储到 localStorage
    localStorage.setItem('token', userData.token)
    localStorage.setItem('userId', userData.userId.toString())
    localStorage.setItem('username', userData.username)
    
    console.log('用户信息已保存:', {
      userId: userData.userId,
      username: userData.username,
      tokenStored: !!localStorage.getItem('token')
    })
  }

  /**
   * 清除用户信息
   */
  const clearUser = () => {
    user.value = null
    token.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
  }

  /**
   * 初始化用户信息（从本地存储恢复）
   */
  const initUser = () => {
    const storedToken = localStorage.getItem('token')
    const storedUserId = localStorage.getItem('userId')
    const storedUsername = localStorage.getItem('username')

    if (storedToken && storedUserId && storedUsername) {
      user.value = {
        userId: parseInt(storedUserId),
        username: storedUsername
      }
      token.value = storedToken
    }
  }

  /**
   * 检查是否已登录
   * @returns {boolean}
   */
  const isLoggedIn = () => {
    return !!token.value && !!user.value
  }

  return {
    user,
    token,
    setUser,
    clearUser,
    initUser,
    isLoggedIn
  }
})

