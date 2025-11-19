import { http } from '@/utils/http'

/**
 * 模拟管理员操作：删除用户
 * 仅管理员可以访问
 * @param {number} userId - 用户ID
 * @returns {Promise} 返回操作结果
 */
export function deleteUser(userId) {
  return http.post('/v1/admin/delete-user', null, {
    params: { userId }
  })
}

