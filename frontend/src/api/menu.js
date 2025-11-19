import { http } from '@/utils/http'

/**
 * 获取当前用户的菜单列表
 * 根据用户权限返回不同的菜单
 * @returns {Promise} 返回菜单列表
 */
export function getMenus() {
  return http.get('/v1/menus')
}

