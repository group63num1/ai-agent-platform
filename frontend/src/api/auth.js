import { http } from '@/utils/http'

/**
 * 用户登录
 * @param {Object} data - 登录数据
 * @param {string} data.username - 用户名或邮箱
 * @param {string} data.password - 密码
 * @returns {Promise} 返回登录响应数据
 */
export function login(data) {
  return http.post('/v1/auth/login', data)
}

/**
 * 用户注册
 * @param {Object} data - 注册数据
 * @returns {Promise} 返回注册响应数据
 */
export function register(data) {
  return http.post('/v1/auth/register', data)
}

/**
 * 用户登出
 * @returns {Promise} 返回登出响应数据
 */
export function logout() {
  return http.post('/v1/auth/logout')
}

/**
 * 发送邮箱注册验证码
 * @param {string} email - 邮箱地址
 * @returns {Promise} 返回发送结果
 */
export function sendEmailRegisterCode(email) {
  return http.post('/v1/verification/email/register', null, {
    params: { email }
  })
}

/**
 * 发送手机号注册验证码
 * @param {string} phone - 手机号
 * @returns {Promise} 返回发送结果
 */
export function sendSmsRegisterCode(phone) {
  return http.post('/v1/verification/sms/register', null, {
    params: { phone }
  })
}