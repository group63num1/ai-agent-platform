<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
    <div class="w-full max-w-md px-6 py-8">
      <el-card class="shadow-xl border-0" body-class="p-8">
        <template #header>
          <div class="text-center">
            <h1 class="text-2xl font-bold text-gray-800">用户登录</h1>
            <p class="text-sm text-gray-500 mt-2">请输入您的账号和密码</p>
            <div class="mt-3 text-xs text-gray-400">
              <p>测试账号：</p>
              <p>管理员 - admin / 123456</p>
              <p>普通用户 - user / 123456</p>
            </div>
          </div>
        </template>

        <el-alert
          v-if="errorMessage"
          :title="errorMessage"
          type="error"
          :closable="true"
          @close="errorMessage = ''"
          class="mb-6"
          show-icon
        />

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          label-position="top"
          @submit.prevent="handleLogin"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名或邮箱"
              size="large"
              :prefix-icon="User"
              clearable
              :disabled="loading"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              clearable
              :disabled="loading"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              :disabled="loading"
              class="w-full"
              @click="handleLogin"
            >
              <span v-if="!loading">登录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>

          <el-form-item>
            <div class="text-center w-full">
              <el-button
                type="info"
                link
                @click="showRegisterDialog = true"
              >
                还没有账号？立即注册
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 注册对话框 -->
    <el-dialog
      v-model="showRegisterDialog"
      title="用户注册"
      width="500px"
      :close-on-click-modal="false"
      @close="handleRegisterDialogClose"
    >
      <el-alert
        v-if="registerErrorMessage"
        :title="registerErrorMessage"
        type="error"
        :closable="true"
        @close="registerErrorMessage = ''"
        class="mb-4"
        show-icon
      />

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-position="top"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名（3-50个字符）"
            size="large"
            :prefix-icon="User"
            clearable
            :disabled="registerLoading"
          />
        </el-form-item>

        <el-form-item label="邮箱或手机号" prop="emailOrPhone">
          <div class="flex gap-2">
            <el-input
              v-model="registerForm.emailOrPhone"
              placeholder="请输入邮箱地址或手机号（必填）"
              size="large"
              :prefix-icon="emailOrPhoneIcon"
              clearable
              :disabled="registerLoading"
              class="flex-1"
              @blur="handleEmailOrPhoneBlur"
              @input="handleEmailOrPhoneInput"
            />
            <el-button
              type="primary"
              :disabled="!registerForm.emailOrPhone || !isValidEmailOrPhone(registerForm.emailOrPhone) || codeCountdown > 0 || registerLoading"
              :loading="sendingCode"
              @click="handleSendCode"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}秒` : '发送验证码' }}
            </el-button>
          </div>
          <div class="text-xs text-gray-500 mt-1">
            <span v-if="registerForm.emailOrPhone && isValidEmail(registerForm.emailOrPhone)" class="text-green-600">
              ✓ 已识别为邮箱
            </span>
            <span v-else-if="registerForm.emailOrPhone && isValidPhone(registerForm.emailOrPhone)" class="text-green-600">
              ✓ 已识别为手机号
            </span>
            <span v-else-if="registerForm.emailOrPhone" class="text-orange-500">
              请输入有效的邮箱地址或11位手机号
            </span>
            <span v-else class="text-gray-400">
              请输入邮箱地址或11位手机号
            </span>
          </div>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（至少6位，包含字母和数字）"
            size="large"
            :prefix-icon="Lock"
            show-password
            clearable
            :disabled="registerLoading"
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            clearable
            :disabled="registerLoading"
          />
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input
            v-model="registerForm.nickname"
            placeholder="请输入昵称（可选）"
            size="large"
            :prefix-icon="UserFilled"
            clearable
            :disabled="registerLoading"
          />
        </el-form-item>


        <el-form-item label="验证码" prop="verificationCode">
          <el-input
            v-model="registerForm.verificationCode"
            placeholder="请输入验证码（6位数字）"
            size="large"
            :prefix-icon="MessageBox"
            clearable
            :disabled="registerLoading"
            maxlength="6"
          />
          <div class="text-xs text-gray-500 mt-1">
            请先输入邮箱或手机号，然后点击"发送验证码"按钮获取验证码
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleRegisterDialogClose" :disabled="registerLoading">
            取消
          </el-button>
          <el-button
            type="primary"
            :loading="registerLoading"
            @click="handleRegister"
          >
            <span v-if="!registerLoading">注册</span>
            <span v-else>注册中...</span>
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, MessageBox, UserFilled, Cellphone } from '@element-plus/icons-vue'
import { login, register, sendEmailRegisterCode, sendSmsRegisterCode } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const registerFormRef = ref(null)

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
})
const loading = ref(false)
const errorMessage = ref('')

// 注册表单
const showRegisterDialog = ref(false)
const registerForm = reactive({
  username: '',
  emailOrPhone: '',  // 统一的邮箱或手机号输入
  password: '',
  confirmPassword: '',
  nickname: '',
  verificationCode: ''
})
const registerLoading = ref(false)
const registerErrorMessage = ref('')

// 验证码相关
const sendingCode = ref(false)
const codeCountdown = ref(0)
let countdownTimer = null

// 登录表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 注册表单验证规则
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度至少6位'))
  } else if (!/^(?=.*[A-Za-z])(?=.*\d)/.test(value)) {
    callback(new Error('密码必须包含字母和数字'))
  } else {
    callback()
  }
}

// 验证邮箱或手机号
const validateEmailOrPhone = (rule, value, callback) => {
  if (!value || value.trim() === '') {
    callback(new Error('请输入邮箱或手机号'))
    return
  }
  
  const trimmedValue = value.trim()
  const isEmail = isValidEmail(trimmedValue)
  const isPhone = isValidPhone(trimmedValue)
  
  if (!isEmail && !isPhone) {
    callback(new Error('请输入有效的邮箱地址或11位手机号'))
  } else {
    callback()
  }
}

// 验证验证码
const validateVerificationCode = (rule, value, callback) => {
  if (!value || value.trim() === '') {
    callback(new Error('请输入验证码'))
  } else if (!/^\d{6}$/.test(value)) {
    callback(new Error('验证码必须是6位数字'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在3-50个字符之间', trigger: 'blur' }
  ],
  emailOrPhone: [
    { required: true, validator: validateEmailOrPhone, trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  nickname: [],
  verificationCode: [
    { required: true, validator: validateVerificationCode, trigger: 'blur' }
  ]
}

// 登录处理
const handleLogin = async () => {
  if (!loginFormRef.value) return
  try {
    await loginFormRef.value.validate()
  } catch (error) {
    return
  }
  errorMessage.value = ''
  loading.value = true
  try {
    const response = await login({
      username: loginForm.username.trim(),
      password: loginForm.password
    })
    if (!response || !response.token) {
      throw new Error('登录失败：未收到有效的 token')
    }
    userStore.setUser({
      userId: response.userId,
      username: response.username,
      token: response.token
    })
    ElMessage.success('登录成功')
    router.push('/home')
  } catch (error) {
    errorMessage.value = error.message || '登录失败，请检查用户名和密码'
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

// 验证邮箱格式
const isValidEmail = (email) => {
  if (!email) return false
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

// 验证手机号格式
const isValidPhone = (phone) => {
  if (!phone) return false
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(phone)
}

// 验证邮箱或手机号
const isValidEmailOrPhone = (value) => {
  if (!value) return false
  return isValidEmail(value) || isValidPhone(value)
}

// 判断输入的是邮箱还是手机号
const getEmailOrPhoneType = (value) => {
  if (!value) return null
  if (isValidEmail(value)) return 'email'
  if (isValidPhone(value)) return 'phone'
  return null
}

// 获取邮箱或手机号的图标（使用computed确保响应式更新）
const emailOrPhoneIcon = computed(() => {
  const value = registerForm.emailOrPhone?.trim()
  if (!value) return MessageBox
  if (isValidEmail(value)) return MessageBox
  if (isValidPhone(value)) return Cellphone
  return MessageBox
})

// 邮箱或手机号输入框输入事件
const handleEmailOrPhoneInput = () => {
  // 实时验证，但不显示错误（只在失焦时显示）
}

// 邮箱或手机号输入框失焦事件
const handleEmailOrPhoneBlur = () => {
  // 触发验证
  if (registerFormRef.value) {
    registerFormRef.value.validateField('emailOrPhone')
  }
}

// 发送验证码（统一处理邮箱和手机号）
const handleSendCode = async () => {
  const value = registerForm.emailOrPhone?.trim()
  if (!value) {
    ElMessage.warning('请输入邮箱或手机号')
    return
  }

  const type = getEmailOrPhoneType(value)
  if (!type) {
    ElMessage.warning('请输入有效的邮箱地址或11位手机号')
    return
  }

  sendingCode.value = true
  try {
    if (type === 'email') {
      await sendEmailRegisterCode(value)
      // 开发模式提示
      ElMessage({
        message: '验证码已发送（开发模式：请查看后端控制台输出）',
        type: 'warning',
        duration: 5000,
        showClose: true
      })
      console.warn('⚠️ 开发模式提示：邮箱验证码已生成，请查看后端控制台输出获取验证码')
    } else {
      await sendSmsRegisterCode(value)
      // 开发模式提示
      ElMessage({
        message: '验证码已发送（开发模式：请查看后端控制台输出）',
        type: 'warning',
        duration: 5000,
        showClose: true
      })
      console.warn('⚠️ 开发模式提示：手机验证码已生成，请查看后端控制台输出获取验证码')
    }
    // 开始倒计时
    startCountdown()
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败，请稍后重试')
    console.error('发送验证码失败:', error)
  } finally {
    sendingCode.value = false
  }
}

// 验证码倒计时
const startCountdown = () => {
  codeCountdown.value = 60
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
  countdownTimer = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

// 注册处理
const handleRegister = async () => {
  if (!registerFormRef.value) return
  try {
    await registerFormRef.value.validate()
  } catch (error) {
    return
  }

  // 验证邮箱或手机号
  const emailOrPhone = registerForm.emailOrPhone?.trim()
  if (!emailOrPhone) {
    registerErrorMessage.value = '请输入邮箱或手机号'
    return
  }

  const type = getEmailOrPhoneType(emailOrPhone)
  if (!type) {
    registerErrorMessage.value = '请输入有效的邮箱地址或11位手机号'
    return
  }

  // 验证验证码
  if (!registerForm.verificationCode || !/^\d{6}$/.test(registerForm.verificationCode)) {
    registerErrorMessage.value = '请输入6位数字验证码'
    return
  }

  registerErrorMessage.value = ''
  registerLoading.value = true

  try {
    const registerData = {
      username: registerForm.username.trim(),
      email: type === 'email' ? emailOrPhone : undefined,
      phone: type === 'phone' ? emailOrPhone : undefined,
      verificationCode: registerForm.verificationCode.trim(),
      password: registerForm.password,
      nickname: registerForm.nickname.trim() || undefined
    }

    const response = await register(registerData)
    
    ElMessage.success('注册成功！请登录')
    
    // 关闭注册对话框
    showRegisterDialog.value = false
    
    // 清空注册表单
    resetRegisterForm()
    
    // 自动填充登录表单的用户名
    loginForm.username = registerData.username
  } catch (error) {
    registerErrorMessage.value = error.message || '注册失败，请检查输入信息'
    console.error('注册失败:', error)
  } finally {
    registerLoading.value = false
  }
}

// 重置注册表单
const resetRegisterForm = () => {
  registerForm.username = ''
  registerForm.emailOrPhone = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
  registerForm.nickname = ''
  registerForm.verificationCode = ''
  registerErrorMessage.value = ''
  
  // 清除倒计时
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  codeCountdown.value = 0
  
  if (registerFormRef.value) {
    registerFormRef.value.clearValidate()
  }
}

// 组件卸载时清除定时器
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})

// 关闭注册对话框
const handleRegisterDialogClose = () => {
  showRegisterDialog.value = false
  resetRegisterForm()
}
</script>

<style scoped>
:deep(.el-card__header) {
  padding: 24px 24px 0;
  border-bottom: none;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
  margin-bottom: 8px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

.flex {
  display: flex;
}

.gap-2 {
  gap: 8px;
}

.flex-1 {
  flex: 1;
}
</style>

