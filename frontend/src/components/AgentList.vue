<template>
  <div>
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4" v-loading="loading">
      <el-card
        v-for="agent in list"
        :key="agent.id"
        class="relative cursor-pointer hover:shadow-md transition-shadow"
        @click="goDetail(agent)"
      >
        <div class="flex items-start justify-between mb-2">
          <h3 class="text-base font-semibold truncate pr-6">{{ agent.name }}</h3>
          <el-tag size="small" :type="agent.status==='published' ? 'success' : 'info'">{{ agent.status || 'draft' }}</el-tag>
        </div>
        <p class="text-sm text-gray-600 line-clamp-3 min-h-[3.6em]">{{ agent.description || '无简介' }}</p>

        <!-- 右下角三点菜单 -->
        <div class="absolute bottom-2 right-2" @click.stop>
          <el-dropdown @command="cmd => onCommand(agent, cmd)">
            <span class="el-dropdown-link">
              <el-button text :icon="MoreFilled" />
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="delete">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-card>
    </div>
  </div>
  
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAgentsStore } from '@/stores/agents'
import { computed } from 'vue'
import { ElMessageBox } from 'element-plus'
import { MoreFilled } from '@element-plus/icons-vue'

const router = useRouter()
const store = useAgentsStore()

const list = computed(() => store.list)
const loading = computed(() => store.loadingList || store.publishing || store.removing)

function goDetail(row) {
  // 列表点击跳转到工作台进行修改
  router.push({ name: 'agentStudio', params: { id: row.id } })
}

function onCommand(row, cmd) {
  if (cmd === 'delete') onRemove(row)
}

async function onRemove(row) {
  if (row.status === 'published') {
    await ElMessageBox.alert('已发布的智能体不允许删除。\n如需隐藏可改为归档或禁用状态。', '无法删除已发布智能体', {
      type: 'warning',
      confirmButtonText: '知道了'
    })
    return
  }
  await store.remove(row.id)
}
</script>
