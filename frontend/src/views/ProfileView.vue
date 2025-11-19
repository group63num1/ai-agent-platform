<template>
  <div class="profile-container">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-800">个人信息</h1>
      <p class="text-sm text-gray-500 mt-1">查看和管理您的个人信息</p>
    </div>

    <el-skeleton v-if="loading" :rows="8" animated />

    <el-alert
      v-else-if="error"
      :title="error"
      type="error"
      :closable="true"
      @close="error = ''"
      show-icon
      class="mb-6"
    />

    <el-card v-else shadow="hover" class="profile-card">
      <template #header>
        <div class="flex items-center justify-between">
          <h2 class="text-lg font-semibold text-gray-800">基本信息</h2>
          <el-button
            type="primary"
            :icon="Edit"
            @click="showEditDialog = true"
          >
            编辑信息
          </el-button>
        </div>
      </template>

      <div class="profile-content">
        <div class="flex items-start gap-6 mb-8 pb-8 border-b border-gray-200">
          <div class="avatar-section">
            <el-avatar
              :size="100"
              :src="userInfo.avatarUrl"
              class="border-2 border-gray-200"
            >
              <span class="text-3xl">{{ userInfo.nickname?.[0] || userInfo.username?.[0] || 'U' }}</span>
            </el-avatar>
          </div>

          <div class="flex-1">
            <h3 class="text-xl font-bold text-gray-800 mb-2">
              {{ userInfo.nickname || userInfo.username || '未设置昵称' }}
            </h3>
            <p class="text-sm text-gray-500 mb-4">{{ userInfo.username }}</p>
            <div class="flex items-center gap-4">
              <el-tag v-if="userInfo.status === 1" type="success">正常</el-tag>
              <el-tag v-else-if="userInfo.status === 0" type="danger">已禁用</el-tag>
              <el-tag v-else-if="userInfo.status === 2" type="warning">已锁定</el-tag>
            </div>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div class="info-item">
            <label class="info-label">用户名</label>
            <div class="info-value">{{ userInfo.username || '未设置' }}</div>
          </div>

          <div class="info-item">
            <label class="info-label">
              邮箱
              <el-tag v-if="userInfo.emailVerified === 1" type="success" size="small" class="ml-2">已验证</el-tag>
              <el-tag v-else type="info" size="small" class="ml-2">未验证</el-tag>
            </label>
            <div class="info-value">{{ userInfo.email || '未设置' }}</div>
          </div>

          <div class="info-item">
            <label class="info-label">
              手机号
              <el-tag v-if="userInfo.phoneVerified === 1" type="success" size="small" class="ml-2">已验证</el-tag>
              <el-tag v-else-if="userInfo.phone" type="info" size="small" class="ml-2">未验证</el-tag>
            </label>
            <div class="info-value">{{ userInfo.phone || '未设置' }}</div>
          </div>

          <div class="info-item">
            <label class="info-label">昵称</label>
            <div class="info-value">{{ userInfo.nickname || '未设置' }}</div>
          </div>

          <div class="info-item md:col-span-2">
            <label class="info-label">个人简介</label>
            <div class="info-value">{{ userInfo.bio || '暂无简介' }}</div>
          </div>

          <div class="info-item md:col-span-2" v-if="userInfo.roles && userInfo.roles.length > 0">
            <label class="info-label">角色</label>
            <div class="flex flex-wrap gap-2">
              <el-tag
                v-for="role in userInfo.roles"
                :key="role.id"
                type="primary"
              >
                {{ role.name || role.code }}
              </el-tag>
            </div>
          </div>

          <div class="info-item">
            <label class="info-label">最后登录时间</label>
            <div class="info-value">
              {{ userInfo.lastLoginAt ? formatDateTime(userInfo.lastLoginAt) : '从未登录' }}
            </div>
          </div>

          <div class="info-item">
            <label class="info-label">最后登录IP</label>
            <div class="info-value">{{ userInfo.lastLoginIp || '未知' }}</div>
          </div>

          <div class="info-item md:col-span-2">
            <label class="info-label">注册时间</label>
            <div class="info-value">
              {{ userInfo.createdAt ? formatDateTime(userInfo.createdAt) : '未知' }}
            </div>
          </div>
        </div>

        <div class="mt-8 pt-6 border-t border-gray-200">
          <div class="flex items-center justify-between">
            <el-button
              type="danger"
              :icon="Delete"
              @click="handleDeleteAccount"
              :loading="deleteAccountLoading"
            >
              注销账户
            </el-button>
            <div class="flex items-center gap-4">
              <el-button
                type="warning"
                :icon="Setting"
                @click="handleAdminAction"
                :loading="adminActionLoading"
              >
                管理员操作
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 编辑信息对话框 -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑个人信息"
      width="600px"
      :close-on-click-modal="false"
      @close="handleEditDialogClose"
    >
      <el-alert
        v-if="editErrorMessage"
        :title="editErrorMessage"
        type="error"
        :closable="true"
        @close="editErrorMessage = ''"
        class="mb-4"
        show-icon
      />

      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-position="top"
      >
        <el-form-item label="昵称" prop="nickname">
          <el-input
            v-model="editForm.nickname"
            placeholder="请输入昵称"
            size="large"
            clearable
            :disabled="editLoading"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="editForm.phone"
            placeholder="请输入手机号"
            size="large"
            clearable
            :disabled="editLoading"
            maxlength="20"
          />
        </el-form-item>

        <el-form-item label="个人简介" prop="bio">
          <el-input
            v-model="editForm.bio"
            type="textarea"
            :rows="4"
            placeholder="请输入个人简介"
            :disabled="editLoading"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="头像URL" prop="avatarUrl">
          <el-input
            v-model="editForm.avatarUrl"
            placeholder="请输入头像URL（可选）"
            size="large"
            clearable
            :disabled="editLoading"
          />
          <div class="mt-2 text-xs text-gray-500">
            提示：您也可以先上传头像获取URL
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleEditDialogClose" :disabled="editLoading">
            取消
          </el-button>
          <el-button
            type="primary"
            :loading="editLoading"
            @click="handleUpdateProfile"
          >
            <span v-if="!editLoading">保存</span>
            <span v-else>保存中...</span>
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, Edit, Delete } from '@element-plus/icons-vue'
import { getUserProfile, updateUserProfile, deleteUserProfile } from '@/api/user'
import { deleteUser } from '@/api/admin'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const error = ref('')
const adminActionLoading = ref(false)
const deleteAccountLoading = ref(false)

// 编辑相关
const showEditDialog = ref(false)
const editFormRef = ref(null)
const editForm = ref({
  nickname: '',
  phone: '',
  bio: '',
  avatarUrl: ''
})
const editLoading = ref(false)
const editErrorMessage = ref('')

const userInfo = ref({
  id: null,
  username: '',
  email: '',
  phone: '',
  nickname: '',
  avatarUrl: '',
  bio: '',
  status: 1,
  emailVerified: 0,
  phoneVerified: 0,
  lastLoginAt: null,
  lastLoginIp: '',
  createdAt: null,
  roles: []
})

// 编辑表单验证规则
const editRules = {
  nickname: [
    { max: 100, message: '昵称长度不能超过100个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$|^$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  bio: [
    { max: 500, message: '个人简介长度不能超过500个字符', trigger: 'blur' }
  ],
  avatarUrl: [
    { max: 500, message: '头像URL长度不能超过500个字符', trigger: 'blur' }
  ]
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '未知'
  if (typeof dateTime === 'string') {
    const date = new Date(dateTime)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  }
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const loadUserProfile = async () => {
  loading.value = true
  error.value = ''
  try {
    const data = await getUserProfile()
    userInfo.value = {
      id: data.id,
      username: data.username || '',
      email: data.email || '',
      phone: data.phone || '',
      nickname: data.nickname || '',
      avatarUrl: data.avatarUrl || '',
      bio: data.bio || '',
      status: data.status || 1,
      emailVerified: data.emailVerified || 0,
      phoneVerified: data.phoneVerified || 0,
      lastLoginAt: data.lastLoginAt,
      lastLoginIp: data.lastLoginIp || '',
      createdAt: data.createdAt,
      roles: data.roles || []
    }
  } catch (err) {
    console.error('加载个人信息失败:', err)
    error.value = err.message || '加载个人信息失败，请稍后重试'
    ElMessage.error(error.value)
  } finally {
    loading.value = false
  }
}

// 打开编辑对话框时初始化表单数据
const initEditForm = () => {
  editForm.value = {
    nickname: userInfo.value.nickname || '',
    phone: userInfo.value.phone || '',
    bio: userInfo.value.bio || '',
    avatarUrl: userInfo.value.avatarUrl || ''
  }
  editErrorMessage.value = ''
}

// 更新个人信息
const handleUpdateProfile = async () => {
  if (!editFormRef.value) return
  try {
    await editFormRef.value.validate()
  } catch (error) {
    return
  }

  editErrorMessage.value = ''
  editLoading.value = true

  try {
    const updateData = {
      nickname: editForm.value.nickname.trim() || null,
      phone: editForm.value.phone.trim() || null,
      bio: editForm.value.bio.trim() || null,
      avatarUrl: editForm.value.avatarUrl.trim() || null
    }

    const updatedUser = await updateUserProfile(updateData)
    
    // 更新本地用户信息
    userInfo.value = {
      ...userInfo.value,
      nickname: updatedUser.nickname || userInfo.value.nickname,
      phone: updatedUser.phone || userInfo.value.phone,
      bio: updatedUser.bio || userInfo.value.bio,
      avatarUrl: updatedUser.avatarUrl || userInfo.value.avatarUrl
    }
    
    ElMessage.success('个人信息更新成功')
    showEditDialog.value = false
  } catch (error) {
    editErrorMessage.value = error.message || '更新个人信息失败，请检查输入信息'
    console.error('更新个人信息失败:', error)
  } finally {
    editLoading.value = false
  }
}

// 关闭编辑对话框
const handleEditDialogClose = () => {
  showEditDialog.value = false
  if (editFormRef.value) {
    editFormRef.value.clearValidate()
  }
}

// 监听编辑对话框打开
watch(showEditDialog, (newVal) => {
  if (newVal) {
    initEditForm()
  }
})

// 注销账户
const handleDeleteAccount = async () => {
  try {
    await ElMessageBox.confirm(
      '注销账户是不可逆的操作，您的账户将从数据库中彻底删除，所有数据将无法恢复。\n\n注意：如果您是团队负责人，请先转移团队负责人身份或删除相关团队。',
      '确认注销账户',
      {
        confirmButtonText: '确认注销',
        cancelButtonText: '取消',
        type: 'error',
        dangerouslyUseHTMLString: false,
        confirmButtonClass: 'el-button--danger'
      }
    )

    // 二次确认
    await ElMessageBox.confirm(
      '请再次确认：注销后您的账户将从数据库中彻底删除，所有数据将被永久删除且无法恢复。',
      '最后确认',
      {
        confirmButtonText: '我确定要注销',
        cancelButtonText: '取消',
        type: 'error',
        confirmButtonClass: 'el-button--danger'
      }
    )

    deleteAccountLoading.value = true
    try {
      await deleteUserProfile()
      ElMessage.success('账户注销成功')
      
      // 清除用户信息和Token
      userStore.clearUser()
      
      // 延迟跳转，让用户看到成功提示
      setTimeout(() => {
        router.push('/')
      }, 1000)
    } catch (err) {
      ElMessage.error(err.message || '注销账户失败，请稍后重试')
      console.error('注销账户失败:', err)
    } finally {
      deleteAccountLoading.value = false
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('注销账户操作失败:', error)
    }
  }
}

const handleAdminAction = async () => {
  try {
    await ElMessageBox.confirm(
      '此操作需要管理员权限，确定要继续吗？',
      '管理员操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    adminActionLoading.value = true
    try {
      await deleteUser(1)
      ElMessage.success('操作成功')
    } catch (err) {
      if (err.code === 403 || err.response?.status === 403) {
        ElMessage.error('权限不足：您没有执行此操作的权限')
        console.warn('权限不足，403错误:', err)
      } else {
        ElMessage.error(err.message || '操作失败')
        console.error('操作失败:', err)
      }
    } finally {
      adminActionLoading.value = false
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('管理员操作失败:', error)
    }
  }
}

onMounted(() => {
  loadUserProfile()
})
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
}

.profile-card {
  border-radius: 12px;
}

.profile-content {
  padding: 8px;
}

.avatar-section {
  flex-shrink: 0;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-label {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  display: flex;
  align-items: center;
}

.info-value {
  font-size: 16px;
  color: #303133;
  word-break: break-word;
}

:deep(.el-card__header) {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-card__body) {
  padding: 24px;
}
</style>

