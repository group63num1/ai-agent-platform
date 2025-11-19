import { http } from '@/utils/http'

/**
 * 获取当前用户的个人信息
 * @returns {Promise} 返回用户信息
 */
export function getUserProfile() {
  return http.get('/v1/user/profile')
}

/**
 * 更新当前用户的个人信息
 * @param {Object} data - 用户信息数据
 * @param {string} data.nickname - 昵称
 * @param {string} data.phone - 手机号
 * @param {string} data.bio - 个人简介
 * @param {string} data.avatarUrl - 头像URL
 * @returns {Promise} 返回更新后的用户信息
 */
export function updateUserProfile(data) {
  return http.put('/v1/user/profile', data)
}

/**
 * 注销当前用户账户
 * @returns {Promise} 返回注销结果
 */
export function deleteUserProfile() {
  return http.delete('/v1/user/profile')
}

