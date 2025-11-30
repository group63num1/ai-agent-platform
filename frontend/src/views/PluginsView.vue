<template>
  <div class="plugins-page">
    <div class="page-header">
      <div>
        <h2 class="title">插件管理</h2>
        <p class="subtitle">管理系统扩展插件，启用或禁用特定功能，并支持导入开放平台配置</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleCreate">新建插件</el-button>
        <el-button type="success" :disabled="!currentPlugin" @click="handlePublish">发布</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="pluginList"
        highlight-current-row
        style="width: 100%"
        @current-change="handleCurrentChange"
      >
        <el-table-column label="插件名称" min-width="200">
          <template #default="{ row }">
            <div class="flex items-center gap-3">
              <el-avatar :size="36" :src="row.iconUrl">
                {{ row.name?.slice(0, 1)?.toUpperCase() }}
              </el-avatar>
              <div>
                <div class="font-semibold text-gray-800">{{ row.name }}</div>
                <div class="text-xs text-gray-500">{{ row.description }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="version" label="版本" width="120" />

        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="light">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="试运行" width="140">
          <template #default="{ row }">
            <el-tag
              :type="row.testStatus === 'passed' ? 'success' : row.testStatus === 'failed' ? 'danger' : 'warning'"
            >
              {{ testStatusText(row.testStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="启用" width="120">
          <template #default="{ row }">
            <el-switch
              :model-value="row.enabled"
              :disabled="row.testStatus !== 'passed'"
              active-text="ON"
              inactive-text="OFF"
              @change="(val) => handleToggle(row, val)"
            />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row)">配置</el-button>
            <el-button type="info" link @click="openDetail(row, true)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">卸载</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑 -->
    <el-dialog
      v-model="createDialog.visible"
      :title="createDialog.mode === 'create' ? '新建插件' : '编辑插件'"
      width="800px"
      class="plugin-dialog"
    >
      <el-alert
        type="info"
        show-icon
        class="mb-4"
        title="可直接填写字段或导入 OpenAPI JSON（如 coze 模板）自动生成工具配置"
      />
      <div class="mb-4">
        <el-upload
          action="#"
          :auto-upload="false"
          :show-file-list="false"
          accept=".json"
          :on-change="handleImport"
        >
          <el-button :icon="Upload"> 导入 JSON</el-button>
        </el-upload>
      </div>
      <el-form :model="pluginForm" label-width="110px" :rules="pluginRules" ref="pluginFormRef">
        <el-form-item label="插件名称" prop="name">
          <el-input v-model="pluginForm.name" maxlength="30" />
        </el-form-item>
        <el-form-item label="一句话介绍">
          <el-input v-model="pluginForm.intro" maxlength="50" />
        </el-form-item>
        <el-form-item label="插件描述">
          <el-input type="textarea" v-model="pluginForm.description" rows="3" />
        </el-form-item>
        <el-form-item label="插件图标">
          <el-input v-model="pluginForm.iconUrl" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="pluginForm.pluginType">
            <el-radio-button label="cloud">云端插件</el-radio-button>
            <el-radio-button label="edge">端插件</el-radio-button>
            <el-radio-button label="mcp">MCP</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="创建方式">
          <el-select v-model="pluginForm.createMode">
            <el-option label="基于已有服务创建" value="existing" />
            <el-option label="在 IDE 中创建" value="ide" />
          </el-select>
        </el-form-item>
        <el-form-item label="插件 URL">
          <el-input v-model="pluginForm.pluginUrl" placeholder="https://example.com" />
        </el-form-item>
        <el-form-item label="授权方式">
          <el-select v-model="pluginForm.authType">
            <el-option label="不需要授权" value="none" />
            <el-option label="Bearer Token" value="bearer" />
            <el-option label="API Key" value="apiKey" />
          </el-select>
        </el-form-item>
        <el-form-item label="Headers">
          <div class="header-list">
            <div class="header-row" v-for="(header, index) in pluginForm.headers" :key="index">
              <el-input v-model="header.key" placeholder="Key" />
              <el-input v-model="header.value" placeholder="Value" />
              <el-button link type="danger" @click="removeHeader(index)">删除</el-button>
            </div>
            <el-button link :icon="Plus" @click="addHeader">新增Header</el-button>
          </div>
        </el-form-item>
        <el-divider>工具列表</el-divider>
        <el-empty v-if="!pluginForm.tools.length" description="请添加至少一个工具" />
        <el-collapse v-else>
          <el-collapse-item v-for="(tool, idx) in pluginForm.tools" :key="idx" :name="idx">
            <template #title>
              <span class="font-medium text-sm">{{ tool.name || `工具 ${idx + 1}` }}</span>
            </template>
            <el-form-item label="名称">
              <el-input v-model="tool.name" />
            </el-form-item>
            <el-form-item label="描述">
              <el-input type="textarea" v-model="tool.description" />
            </el-form-item>
            <el-form-item label="方法 / 路径">
              <div class="flex gap-2 w-full">
                <el-select v-model="tool.method" class="w-1/3">
                  <el-option label="GET" value="GET" />
                  <el-option label="POST" value="POST" />
                  <el-option label="PUT" value="PUT" />
                  <el-option label="DELETE" value="DELETE" />
                </el-select>
                <el-input v-model="tool.endpoint" placeholder="/plugin/control" />
              </div>
            </el-form-item>
            <el-form-item label="输入参数">
              <el-table :data="tool.inputParams" size="small" border style="width: 100%">
                <el-table-column prop="name" label="名称">
                  <template #default="{ row }">
                    <el-input v-model="row.name" size="small" />
                  </template>
                </el-table-column>
                <el-table-column prop="type" label="类型" width="120">
                  <template #default="{ row }">
                    <el-select v-model="row.type" size="small">
                      <el-option label="string" value="string" />
                      <el-option label="integer" value="integer" />
                      <el-option label="number" value="number" />
                      <el-option label="object" value="object" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column prop="location" label="位置" width="120">
                  <template #default="{ row }">
                    <el-select v-model="row.location" size="small">
                      <el-option label="query" value="query" />
                      <el-option label="body" value="body" />
                      <el-option label="path" value="path" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column prop="required" label="必填" width="90">
                  <template #default="{ row }">
                    <el-switch v-model="row.required" size="small" />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="80">
                  <template #default="{ $index }">
                    <el-button link type="danger" size="small" @click="removeParam(tool, 'input', $index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-button link :icon="Plus" class="mt-2" @click="addParam(tool, 'input')">新增输入</el-button>
            </el-form-item>
            <el-form-item label="输出参数">
              <el-table :data="tool.outputParams" size="small" border>
                <el-table-column prop="name" label="名称">
                  <template #default="{ row }">
                    <el-input v-model="row.name" size="small" />
                  </template>
                </el-table-column>
                <el-table-column prop="type" label="类型" width="120">
                  <template #default="{ row }">
                    <el-select v-model="row.type" size="small">
                      <el-option label="string" value="string" />
                      <el-option label="integer" value="integer" />
                      <el-option label="number" value="number" />
                      <el-option label="object" value="object" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="80">
                  <template #default="{ $index }">
                    <el-button link type="danger" size="small" @click="removeParam(tool, 'output', $index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-button link :icon="Plus" class="mt-2" @click="addParam(tool, 'output')">新增输出</el-button>
            </el-form-item>
            <div class="flex justify-end">
              <el-button type="danger" link @click="removeTool(idx)">移除此工具</el-button>
            </div>
          </el-collapse-item>
        </el-collapse>
        <div class="mt-4">
          <el-button type="primary" link :icon="Plus" @click="addTool">新增工具</el-button>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="createDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitPluginForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-drawer v-model="detailDrawer.visible" size="50%" :with-header="false">
      <div v-if="detailDrawer.data" class="detail-container">
        <div class="detail-header">
          <div class="flex items-center gap-3">
            <el-avatar :size="48" :src="detailDrawer.data.iconUrl">
              {{ detailDrawer.data.name?.slice(0, 1)?.toUpperCase() }}
            </el-avatar>
            <div>
              <h3 class="text-xl font-semibold">{{ detailDrawer.data.name }}</h3>
              <p class="text-gray-500 text-sm">{{ detailDrawer.data.description }}</p>
            </div>
          </div>
          <div class="flex gap-2">
            <el-button type="primary" @click="openCreateFromDetail">编辑</el-button>
            <el-button @click="detailDrawer.visible = false">关闭</el-button>
          </div>
        </div>
        <el-divider />
        <div class="detail-section">
          <h4>基本信息</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="版本">{{ detailDrawer.data.version }}</el-descriptions-item>
            <el-descriptions-item label="类型">{{ detailDrawer.data.pluginType }}</el-descriptions-item>
            <el-descriptions-item label="创建方式">{{ detailDrawer.data.createMode }}</el-descriptions-item>
            <el-descriptions-item label="授权">{{ detailDrawer.data.authType || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="detail-section">
          <h4>工具列表</h4>
          <el-collapse>
            <el-collapse-item v-for="tool in detailDrawer.data.tools" :key="tool.id" :name="tool.id">
              <template #title>
                <div class="flex items-center gap-2">
                  <span class="font-medium">{{ tool.name }}</span>
                  <el-tag size="small">{{ tool.method }} {{ tool.endpoint }}</el-tag>
                  <el-tag
                    size="small"
                    :type="tool.testStatus === 'passed' ? 'success' : tool.testStatus === 'failed' ? 'danger' : 'warning'"
                  >
                    {{ testStatusText(tool.testStatus) }}
                  </el-tag>
                </div>
              </template>
              <p class="mb-2 text-gray-600">{{ tool.description }}</p>
              <h5 class="font-medium mb-2">输入参数</h5>
              <el-table :data="tool.inputParams" size="small" border>
                <el-table-column prop="name" label="名称" />
                <el-table-column prop="type" label="类型" width="120" />
                <el-table-column prop="location" label="位置" width="120" />
                <el-table-column prop="required" label="必填" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.required ? 'danger' : 'info'" size="small">
                      {{ row.required ? '是' : '否' }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
              <h5 class="font-medium mt-4 mb-2">输出参数</h5>
              <el-table :data="tool.outputParams" size="small" border>
                <el-table-column prop="name" label="名称" />
                <el-table-column prop="type" label="类型" width="120" />
              </el-table>
              <div class="mt-4 text-right">
                <el-button type="success" @click="openTestDialog(tool)">试运行</el-button>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>
      <el-empty v-else description="加载中..." />
    </el-drawer>

    <!-- 试运行 -->
    <el-dialog v-model="testDialog.visible" title="试运行" width="500px">
      <div v-if="testDialog.tool">
        <p class="text-sm text-gray-600 mb-3">
          {{ testDialog.tool.description || '请填写示例数据后提交测试' }}
        </p>
        <el-form :model="testDialog.form" label-width="120px">
          <el-form-item
            v-for="param in testDialog.tool.inputParams"
            :key="param.name"
            :label="`${param.name}${param.required ? ' *' : ''}`"
          >
            <el-input v-model="testDialog.form[param.name]" :placeholder="param.description" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="testDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="testDialog.loading" @click="submitTest">运行</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import {
  fetchPlugins,
  fetchPluginDetail,
  createPlugin,
  updatePlugin,
  deletePlugin,
  togglePlugin,
  publishPlugin,
  testPlugin,
  importPluginTemplate
} from '@/api/plugins'

const pluginList = ref([])
const loading = ref(false)
const currentPlugin = ref(null)

const createDialog = reactive({
  visible: false,
  mode: 'create'
})

const pluginForm = reactive(emptyPlugin())
const pluginFormRef = ref()

const pluginRules = {
  name: [{ required: true, message: '请输入插件名称', trigger: 'blur' }]
}

const detailDrawer = reactive({
  visible: false,
  data: null,
  editable: false
})

const testDialog = reactive({
  visible: false,
  tool: null,
  form: {},
  loading: false
})

function emptyPlugin() {
  return {
    name: '',
    intro: '',
    description: '',
    iconUrl: '',
    pluginType: 'cloud',
    createMode: 'existing',
    version: '1.0.0',
    pluginUrl: '',
    authType: 'none',
    specJson: '',
    headers: [],
    tools: []
  }
}

const statusTagType = (status) => {
  if (status === 'enabled') return 'success'
  if (status === 'disabled') return 'info'
  return 'warning'
}

const statusText = (status) => {
  const map = { draft: '草稿', enabled: '启用', disabled: '停用' }
  return map[status] || status
}

const testStatusText = (status) => {
  const map = { pending: '未测试', passed: '已通过', failed: '失败' }
  return map[status] || status
}

const handleCurrentChange = (row) => {
  currentPlugin.value = row
}

const handleCreate = () => {
  createDialog.mode = 'create'
  Object.assign(pluginForm, emptyPlugin())
  pluginForm.headers = []
  pluginForm.tools = []
  createDialog.visible = true
}

const addHeader = () => {
  pluginForm.headers.push({ key: '', value: '' })
}

const removeHeader = (idx) => {
  pluginForm.headers.splice(idx, 1)
}

const addTool = () => {
  pluginForm.tools.push({
    name: '',
    description: '',
    method: 'GET',
    endpoint: '',
    inputParams: [],
    outputParams: []
  })
}

const removeTool = (idx) => {
  pluginForm.tools.splice(idx, 1)
}

const addParam = (tool, type) => {
  const target = type === 'input' ? tool.inputParams : tool.outputParams
  target.push({
    name: '',
    description: '',
    type: 'string',
    location: type === 'input' ? 'body' : 'response',
    required: false
  })
}

const removeParam = (tool, type, idx) => {
  const target = type === 'input' ? tool.inputParams : tool.outputParams
  target.splice(idx, 1)
}

const handleImport = async (file) => {
  try {
    const data = await importPluginTemplate(file.raw)
    Object.assign(pluginForm, emptyPlugin(), data.template)
    createDialog.visible = true
    ElMessage.success('导入成功，已自动填充表单')
  } catch (error) {
    ElMessage.error(error.message || '导入失败')
  }
}

const submitPluginForm = () => {
  pluginFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      if (!pluginForm.tools.length) {
        ElMessage.warning('请至少添加一个工具')
        return
      }
      const payload = JSON.parse(JSON.stringify(pluginForm))
      if (createDialog.mode === 'create') {
        await createPlugin(payload)
        ElMessage.success('创建成功')
      } else {
        await updatePlugin(pluginForm.id, payload)
        ElMessage.success('更新成功')
      }
      createDialog.visible = false
      await loadPlugins()
    } catch (error) {
      ElMessage.error(error.message || '保存失败')
    }
  })
}

const loadPlugins = async () => {
  loading.value = true
  try {
    pluginList.value = await fetchPlugins()
    if (currentPlugin.value) {
      currentPlugin.value = pluginList.value.find((item) => item.id === currentPlugin.value.id) || null
    }
  } catch (error) {
    ElMessage.error(error.message || '加载插件失败')
  } finally {
    loading.value = false
  }
}

const handleToggle = async (row, enable) => {
  try {
    await togglePlugin(row.id, enable)
    ElMessage.success(enable ? '已启用插件' : '已停用插件')
    await loadPlugins()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定卸载插件「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deletePlugin(row.id)
      ElMessage.success('已卸载')
      await loadPlugins()
    } catch (error) {
      ElMessage.error(error.message || '卸载失败')
    }
  }).catch(() => {})
}

const openDetail = async (row, editable = false) => {
  try {
    detailDrawer.data = await fetchPluginDetail(row.id)
    detailDrawer.visible = true
    detailDrawer.editable = editable
    if (editable) {
      openCreateFromDetail()
    }
  } catch (error) {
    ElMessage.error(error.message || '加载详情失败')
  }
}

const openCreateFromDetail = () => {
  if (!detailDrawer.data) return
  createDialog.mode = 'edit'
  Object.assign(pluginForm, JSON.parse(JSON.stringify(detailDrawer.data)))
  createDialog.visible = true
}

const handlePublish = async () => {
  if (!currentPlugin.value) {
    ElMessage.warning('请先选择一个插件')
    return
  }
  try {
    await publishPlugin(currentPlugin.value.id)
    ElMessage.success('发布成功')
    await loadPlugins()
  } catch (error) {
    ElMessage.error(error.message || '发布失败')
  }
}

const openTestDialog = (tool) => {
  testDialog.tool = tool
  testDialog.form = {}
  tool.inputParams?.forEach((param) => {
    testDialog.form[param.name] = param.example || ''
  })
  testDialog.visible = true
}

const submitTest = async () => {
  if (!detailDrawer.data || !testDialog.tool) return
  testDialog.loading = true
  try {
    const payload = {
      pluginId: detailDrawer.data.id,
      toolId: testDialog.tool.id,
      inputs: testDialog.form
    }
    const result = await testPlugin(payload)
    if (result.success) {
      ElMessage.success(result.message || '试运行成功')
      testDialog.visible = false
      await loadPlugins()
      if (detailDrawer.visible) {
        detailDrawer.data = await fetchPluginDetail(detailDrawer.data.id)
      }
    } else {
      ElMessage.error(result.message || '试运行失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '试运行失败')
  } finally {
    testDialog.loading = false
  }
}

onMounted(() => {
  loadPlugins()
})
</script>

<style scoped>
.plugins-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.title {
  font-size: 20px;
  margin-bottom: 4px;
}
.subtitle {
  color: #6b7280;
  font-size: 13px;
}
.header-actions {
  display: flex;
  gap: 12px;
}
.header-list {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.header-row {
  display: grid;
  grid-template-columns: 1fr 1fr auto;
  gap: 8px;
}
.detail-container {
  padding: 24px;
}
.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.detail-section {
  margin-top: 24px;
}
</style>

