
<template>
  <div class="h-full flex flex-col relative overflow-hidden bg-gray-50">
    <!-- 顶部导航栏 -->
    <header class="flex items-center justify-between px-4 py-3 border-b bg-white z-30 shadow-sm relative">
      <div class="flex items-center gap-3">
        <el-button text @click="router.back" icon="Back">返回</el-button>
        <div class="flex flex-col">
          <div class="flex items-center gap-2">
            <el-input v-model="meta.name" placeholder="未命名工作流" size="small" class="w-64 font-medium" />
            <el-tag v-if="meta.status === 'draft'" size="small" type="info">草稿</el-tag>
          </div>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <span class="text-xs text-gray-400 mr-2">Tips: 拖拽左侧组件至画布 | 选中连线按 Delete 删除</span>
        <el-button @click="seedDefault" size="small">重置画布</el-button>
        <el-button type="primary" :loading="store.saving" @click="handleSave" size="small" icon="Check">保存</el-button>
        <el-button :loading="store.running" @click="handleRun" size="small" icon="VideoPlay">运行</el-button>
      </div>
    </header>

    <!-- 主工作区 -->
    <div class="flex-1 relative overflow-hidden flex">
      
      <!-- 左侧面板：组件库 (拖拽源) -->
      <aside class="w-64 bg-white border-r border-gray-200 flex flex-col z-20 shrink-0">
        <div class="p-3 border-b border-gray-100">
          <span class="font-semibold text-gray-700">组件库</span>
        </div>
        <div class="flex-1 overflow-y-auto p-3 space-y-6">
          
          <!-- 智能体节点分类 -->
          <!-- <div>
            <h3 class="text-xs font-bold text-gray-400 uppercase mb-3 tracking-wider flex items-center gap-1">
              <el-icon><User /></el-icon> 基础智能体
            </h3>
            <div 
              class="palette-item group"
              draggable="true"
              @dragstart="(e) => onDragStart(e, { type: 'agent-simple', label: '普通智能体', sub: 'LLM 对话处理' })"
            >
              <div class="flex items-center justify-between">
                <span class="font-medium text-gray-700 text-sm">普通智能体</span>
                <el-icon class="text-gray-400"><Rank /></el-icon>
              </div>
              <div class="text-xs text-gray-400 mt-1">处理通用对话任务</div>
            </div>
          </div>

            <!-- 运行结果弹窗 -->
            <el-dialog
              v-model="showRunResult"
              title="运行结果"
              width="720px"
              :close-on-click-modal="false"
            >
              <div v-if="runResult" class="space-y-4">
                <div class="flex flex-wrap gap-3 text-sm text-gray-600">
                  <el-tag size="small" type="info">Workflow: {{ runResult.workflowId || 'N/A' }}</el-tag>
                  <el-tag size="small" type="success">Session: {{ runResult.sessionId || 'N/A' }}</el-tag>
                </div>

                <div>
                  <div class="text-xs font-semibold text-gray-500 mb-2">最终输出</div>
                  <el-input
                    type="textarea"
                    :rows="4"
                    v-model="runResult.output"
                    readonly
                    class="text-sm"
                  />
                </div>

                <div>
                  <div class="text-xs font-semibold text-gray-500 mb-2">节点输出</div>
                  <el-timeline>
                    <el-timeline-item
                      v-for="(node, idx) in runResult.nodeResults || []"
                      :key="idx"
                      :timestamp="node.agentId"
                      placement="top"
                    >
                      <div class="bg-gray-50 border border-gray-100 rounded p-3 text-sm text-gray-700 whitespace-pre-line">
                        {{ node.output || '无输出' }}
                      </div>
                    </el-timeline-item>
                  </el-timeline>
                </div>
              </div>
              <template #footer>
                <span class="dialog-footer">
                  <el-button @click="showRunResult = false">关闭</el-button>
                </span>
              </template>
            </el-dialog>

          <div>
            <h3 class="text-xs font-bold text-gray-400 uppercase mb-3 tracking-wider flex items-center gap-1">
              <el-icon><Collection /></el-icon> 知识增强
            </h3>
            <div 
              class="palette-item group"
              draggable="true"
              @dragstart="(e) => onDragStart(e, { type: 'agent-knowledge', label: '知识库智能体', sub: 'RAG 检索增强' })"
            >
              <div class="flex items-center justify-between">
                <span class="font-medium text-gray-700 text-sm">知识库智能体</span>
                <el-icon class="text-gray-400"><Rank /></el-icon>
              </div>
              <div class="text-xs text-gray-400 mt-1">基于文档回答问题</div>
            </div>
          </div>

          <div>
            <h3 class="text-xs font-bold text-gray-400 uppercase mb-3 tracking-wider flex items-center gap-1">
              <el-icon><Connection /></el-icon> 外部能力
            </h3>
            <div 
              class="palette-item group"
              draggable="true"
              @dragstart="(e) => onDragStart(e, { type: 'agent-plugin', label: '插件智能体', sub: '调用外部工具' })"
            >
              <div class="flex items-center justify-between">
                <span class="font-medium text-gray-700 text-sm">插件智能体</span>
                <el-icon class="text-gray-400"><Rank /></el-icon>
              </div>
              <div class="text-xs text-gray-400 mt-1">执行搜索/计算/API</div>
            </div>
          </div> -->

          <!-- 已发布 Agent 列表（后端真实数据，可直接拖入画布） -->
          <div>
            <h3 class="text-xs font-bold text-gray-400 uppercase mb-3 tracking-wider flex items-center gap-1">
              <el-icon><UserFilled /></el-icon> 已发布 Agent
              <el-tag v-if="agentsStore.loadingList" size="small" effect="plain">加载中...</el-tag>
            </h3>
            <template v-if="publishedAgents.length">
              <div 
                v-for="agent in publishedAgents" 
                :key="agent.id"
                class="palette-item group"
                draggable="true"
                @dragstart="(e) => onDragStart(e, { type: 'agent-simple', label: agent.name || agent.id, sub: agent.description || '已发布 Agent', agentId: agent.id })"
              >
                <div class="flex items-center justify-between">
                  <span class="font-medium text-gray-700 text-sm truncate">{{ agent.name || agent.id }}</span>
                  <el-icon class="text-gray-400"><Rank /></el-icon>
                </div>
                <div class="text-xs text-gray-400 mt-1 line-clamp-2">{{ agent.description || '已发布 Agent，可直接绑定' }}</div>
              </div>
            </template>
            <div v-else class="text-xs text-gray-400 bg-gray-50 border border-dashed border-gray-200 rounded p-3">
              {{ agentsStore.loadingList ? '正在加载已发布 Agent...' : '暂无已发布 Agent，请先在「智能体」页面发布后再使用' }}
            </div>
          </div>

        </div>
      </aside>

      <!-- 画布区域 (Canvas) -->
      <div
        ref="canvasContainerRef"
        class="flex-1 relative overflow-auto bg-canvas"
        @dragover.prevent
        @drop="onDrop"
        @mousedown="handleCanvasMouseDown"
        @mousemove="handleGlobalMouseMove"
        @mouseup="handleGlobalMouseUp"
        @click="clearSelection"
      >
        <!-- 实际内容容器 -->
        <div 
          class="relative transform-origin-tl"
          :style="{ width: canvasSize.width + 'px', height: canvasSize.height + 'px' }"
        >
          <!-- SVG 连线层 -->
          <svg class="absolute inset-0 pointer-events-none w-full h-full overflow-visible">
            <defs>
              <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
                <polygon points="0 0, 10 3.5, 0 7" fill="#9ca3af" />
              </marker>
              <marker id="arrowhead-active" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
                <polygon points="0 0, 10 3.5, 0 7" fill="#6366f1" />
              </marker>
              <marker id="arrowhead-selected" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
                <polygon points="0 0, 10 3.5, 0 7" fill="#ef4444" />
              </marker>
            </defs>

            <!-- 已有连线 -->
            <g v-for="edge in edges" :key="edge.id">
              <path
                :d="linkPath(edge.from, edge.to)"
                stroke="transparent"
                stroke-width="20"
                fill="none"
                class="cursor-pointer pointer-events-auto"
                @click.stop="selectEdge(edge.id)"
                @mouseover="hoverEdgeId = edge.id"
                @mouseleave="hoverEdgeId = null"
              />
              <path
                :d="linkPath(edge.from, edge.to)"
                fill="none"
                :stroke="edgeColor(edge)"
                :stroke-width="edge.id === selectedEdgeId ? 2.5 : 2"
                :marker-end="markerUrl(edge)"
                stroke-linecap="round"
                class="transition-colors duration-200"
              />
            </g>

            <!-- 拖拽连线预览 -->
            <path
              v-if="dragState.isLinking"
              :d="tempLinkPath"
              stroke="#6366f1"
              stroke-width="2"
              stroke-dasharray="5,5"
              fill="none"
              class="opacity-60"
            />
          </svg>

          <!-- 节点组件 -->
          <div
            v-for="node in nodes"
            :key="node.id"
            class="node-card group"
            :class="{ 
              'ring-2 ring-indigo-500 shadow-indigo-100': node.id === selectedNodeId
            }"
            :style="{
              transform: `translate(${node.x}px, ${node.y}px)`,
              width: NODE_WIDTH + 'px'
            }"
            @mousedown.stop="(e) => startDragNode(e, node)"
            @click.stop="selectNode(node.id)"
          >
            <!-- 节点头部 -->
            <div class="flex items-center gap-2 mb-2">
              <span class="node-icon" :class="badgeClass(node.type)">
                <el-icon v-if="node.type === 'start'"><VideoPlay /></el-icon>
                <el-icon v-else-if="node.type === 'end'"><SwitchButton /></el-icon>
                <el-icon v-else-if="node.type === 'agent-knowledge'"><Collection /></el-icon>
                <el-icon v-else-if="node.type === 'agent-plugin'"><Connection /></el-icon>
                <el-icon v-else><User /></el-icon>
              </span>
              <span class="font-bold text-gray-700 text-sm truncate flex-1">{{ node.label }}</span>
              
              <!-- 更多菜单 -->
              <el-dropdown trigger="click" size="small">
                <span class="text-gray-300 hover:text-gray-600 cursor-pointer p-1 rounded transition-colors" @click.stop>
                  <el-icon><MoreFilled /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="duplicateNode(node)" icon="CopyDocument">复制</el-dropdown-item>
                    <el-dropdown-item v-if="!isSystemNode(node)" @click="removeNode(node.id)" icon="Delete" class="text-red-500">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>

            <!-- 节点内容摘要 -->
            <div class="text-xs text-gray-500 leading-relaxed line-clamp-2 h-8">
              {{ node.description || '暂无描述' }}
            </div>

            <!-- 输入端口 -->
            <div 
              v-if="node.type !== 'start'"
              class="port-container port-left"
              data-port-type="in"
              :data-node-id="node.id"
              @mouseup="handlePortMouseUp($event, node)"
            >
              <div class="port-point" :class="{'bg-indigo-500 scale-125 ring-2 ring-indigo-200': dragState.isLinking}"></div>
              <span class="port-label">In</span>
            </div>

            <!-- 输出端口 -->
            <div 
              v-if="node.type !== 'end'"
              class="port-container port-right"
              @mousedown.stop.prevent="(e) => startLinkDrag(e, node)"
            >
              <span class="port-label">Out</span>
              <div class="port-point transition-transform hover:scale-125 hover:bg-indigo-500"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 悬浮右侧面板：属性检查器 -->
      <div 
        class="absolute right-4 top-4 w-80 bg-white/95 backdrop-blur shadow-xl rounded-xl border border-gray-200 z-20 flex flex-col max-h-[calc(100%-2rem)] transition-all duration-300"
        :class="{ 'translate-x-[120%]': !showInspector }"
      >
        <div class="p-3 border-b bg-gray-50/50 rounded-t-xl flex justify-between items-center">
          <span class="font-semibold text-gray-700">
            {{ selectedNode ? '节点配置' : '全局设置' }}
          </span>
          <el-button size="small" text circle icon="ArrowRight" @click="showInspector = false" />
        </div>
        
        <div class="p-4 overflow-y-auto space-y-6 custom-scrollbar flex-1">
          <!-- 节点属性模式 -->
          <template v-if="selectedNode">
            
            <!-- 基础信息 -->
            <div class="space-y-3 pb-4 border-b border-gray-100">
              <div>
                <label class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1.5 block">节点名称</label>
                <el-input v-model="selectedNode.label" placeholder="节点名称" />
              </div>
              <div class="flex items-center justify-between text-xs text-gray-500 bg-gray-50 p-2 rounded">
                <span>类型</span>
                <span class="font-medium text-indigo-600">{{ getTypeName(selectedNode.type) }}</span>
              </div>
            </div>

            <!-- 动态属性配置 (核心逻辑) -->
            
            <!-- CASE 1: Start 节点 (Input) -->
            <template v-if="selectedNode.type === 'start'">
              <div>
                <label class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1.5 block">
                  <el-icon class="mr-1 relative top-[1px]"><Right /></el-icon> 默认输入
                </label>
                <div class="text-sm text-gray-700 bg-gray-50 border border-gray-200 rounded p-2.5">
                  用户提示词 (User Prompt)
                </div>
                <p class="text-xs text-gray-400 mt-1">工作流的起始输入，通常为用户发送的消息。</p>
              </div>
            </template>

            <!-- CASE 2: End 节点 (Output) -->
            <template v-else-if="selectedNode.type === 'end'">
              <div>
                <label class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1.5 block">
                  <el-icon class="mr-1 relative top-[1px]"><Back /></el-icon> 默认输出
                </label>
                <div class="text-sm text-gray-700 bg-gray-50 border border-gray-200 rounded p-2.5">
                  工作流最终结果 (Result)
                </div>
                <p class="text-xs text-gray-400 mt-1">汇聚上游数据，作为整个工作流的返回值。</p>
              </div>
            </template>

            <!-- CASE 3: 智能体节点 (Common) -->
            <template v-else>
              <!-- 默认输入 (只读) -->
              <div>
                <label class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1.5 block">
                  <el-icon class="mr-1 relative top-[1px]"><Right /></el-icon> 系统输入
                </label>
                <div class="text-sm text-gray-600 bg-gray-50 border border-gray-200 rounded p-2.5">
                  🔥 上一个节点的输出
                </div>
              </div>

              <!-- 用户输入 (提示词/参数) -->
              <div>
                <label class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1.5 block">
                  <el-icon class="mr-1 relative top-[1px]"><Edit /></el-icon> 用户输入 / 提示词
                </label>
                <el-input
                  v-model="selectedNode.meta.userPrompt"
                  type="textarea"
                  :rows="5"
                  resize="none"
                  placeholder="在此输入给智能体的提示词..."
                  class="text-sm"
                />
                <p class="text-xs text-gray-400 mt-1.5">
                  <span v-if="selectedNode.type === 'agent-knowledge'">用于在知识库中进行检索和回答。</span>
                  <span v-else-if="selectedNode.type === 'agent-plugin'">用于指导插件如何执行任务。</span>
                  <span v-else>用于指导大模型生成回复。</span>
                </p>
              </div>

              <!-- 绑定后端 Agent -->
              <div class="pt-2 border-t border-gray-100">
                <label class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1.5 block">选择已发布 Agent</label>
                <el-select
                  v-model="selectedNode.meta.agentId"
                  placeholder="选择后端已发布的 Agent"
                  filterable
                  size="small"
                  class="w-full"
                >
                  <el-option
                    v-for="agent in publishedAgents"
                    :key="agent.id"
                    :label="agent.name || agent.id"
                    :value="agent.id"
                  />
                </el-select>
                <p class="text-xs text-amber-500 mt-1" v-if="!selectedNode.meta.agentId">保存前请绑定真实 Agent，否则后端会返回 400。</p>
              </div>

              <!-- 关联资源选择 (仅示意) -->
              <div v-if="selectedNode.type === 'agent-knowledge'" class="pt-2 border-t border-gray-100">
                <label class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1.5 block">关联知识库</label>
                <el-select v-model="selectedNode.meta.knowledgeBaseId" placeholder="选择知识库" size="small" class="w-full">
                  <el-option label="公司产品文档" value="kb-1" />
                  <el-option label="技术支持手册" value="kb-2" />
                </el-select>
              </div>

              <div v-if="selectedNode.type === 'agent-plugin'" class="pt-2 border-t border-gray-100">
                <label class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1.5 block">选择插件</label>
                <el-select v-model="selectedNode.meta.pluginId" placeholder="选择插件工具" size="small" class="w-full">
                  <el-option label="Google 搜索" value="pl-search" />
                  <el-option label="Python 解释器" value="pl-code" />
                </el-select>
              </div>
            </template>

          </template>

          <!-- 全局设置模式 -->
          <template v-else>
            <div>
              <label class="text-xs font-semibold text-gray-500 mb-1 block">工作流名称</label>
              <el-input v-model="meta.name" placeholder="Workflow Name" />
            </div>
            <div>
              <label class="text-xs font-semibold text-gray-500 mb-1 block">详细说明</label>
              <el-input v-model="meta.description" type="textarea" :rows="4" placeholder="描述此工作流的用途..." />
            </div>
            <div>
              <label class="text-xs font-semibold text-gray-500 mb-1 block">触发条件</label>
              <el-select v-model="meta.triggerType" class="w-full">
                <el-option label="手动触发 (Manual)" value="manual" />
                <el-option label="定时任务 (Schedule)" value="schedule" />
                <el-option label="Webhook 调用" value="webhook" />
              </el-select>
            </div>
            <div class="text-xs text-indigo-500 bg-indigo-50 p-3 rounded border border-indigo-100">
              <el-icon class="mr-1"><InfoFilled /></el-icon>
              点击画布空白处可切换回全局设置。
            </div>
          </template>
        </div>
      </div>

       <!-- 悬浮右侧面板开关 -->
      <div v-if="!showInspector" class="absolute right-4 top-4 z-20">
        <el-button type="primary" circle icon="Setting" @click="showInspector = true" shadow="always" />
      </div>

    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Back, VideoPlay, Check, Rank, Setting, ArrowRight,
  User, Tools, SwitchButton, MoreFilled, InfoFilled, CopyDocument, Delete,
  Collection, Connection, Right, Back as IconBack, Edit, UserFilled
} from '@element-plus/icons-vue'
import { useWorkflowsStore } from '@/stores/workflows'
import { useAgentsStore } from '@/stores/agents'

// --- 常量与状态 ---
const NODE_WIDTH = 240
const router = useRouter()
const route = useRoute()
const store = useWorkflowsStore()
const agentsStore = useAgentsStore()

const canvasContainerRef = ref(null)
const showInspector = ref(true)

// 数据核心
const nodes = ref([])
const edges = ref([])
const selectedNodeId = ref(null)
const selectedEdgeId = ref(null)
const hoverEdgeId = ref(null)

// 拖拽与交互状态
const dragState = reactive({
  isDraggingNode: false,
  dragNodeId: null,
  nodeOffsetX: 0,
  nodeOffsetY: 0,
  isLinking: false,
  linkStartId: null,
  mousePos: { x: 0, y: 0 }
})

// 元数据
const meta = reactive({
  name: '',
  description: '',
  triggerType: 'manual',
  status: 'draft',
})

const showRunResult = ref(false)
const runResult = ref(null)

const isNew = computed(() => !route.params.id)
const selectedNode = computed(() => nodes.value.find(n => n.id === selectedNodeId.value))
const publishedAgents = computed(() => (agentsStore.list || []).filter(a => a.status === 'published'))

// --- 计算属性 ---

const canvasSize = computed(() => {
  let maxX = 800
  let maxY = 600
  nodes.value.forEach(n => {
    if (n.x + NODE_WIDTH > maxX) maxX = n.x + NODE_WIDTH
    if (n.y + 200 > maxY) maxY = n.y + 200
  })
  return { width: maxX + 400, height: maxY + 400 }
})

// --- 基础工具函数 ---

function uid(prefix = 'n') {
  return `${prefix}-${Math.random().toString(36).slice(2, 8)}`
}

function isSystemNode(node) {
  return node.type === 'start' || node.type === 'end'
}

function getTypeName(type) {
  const map = { 
    'start': 'Input 节点', 
    'end': 'Output 节点', 
    'agent-simple': '普通智能体', 
    'agent-knowledge': '知识库智能体', 
    'agent-plugin': '插件智能体' 
  }
  return map[type] || '未知节点'
}

function badgeClass(type) {
  if (type === 'start') return 'bg-blue-100 text-blue-600'
  if (type === 'end') return 'bg-gray-100 text-gray-600'
  if (type === 'agent-simple') return 'bg-emerald-100 text-emerald-600'
  if (type === 'agent-knowledge') return 'bg-orange-100 text-orange-600'
  if (type === 'agent-plugin') return 'bg-purple-100 text-purple-600'
  return 'bg-gray-100 text-gray-500'
}

// 获取节点端口坐标
function getPortPos(nodeId, direction) {
  const node = nodes.value.find(n => n.id === nodeId)
  if (!node) return { x: 0, y: 0 }
  const h = 50 
  if (direction === 'in') {
    return { x: node.x, y: node.y + h } // 左侧
  } else {
    return { x: node.x + NODE_WIDTH, y: node.y + h } // 右侧
  }
}

// 连线路径
function linkPath(fromId, toId) {
  const start = getPortPos(fromId, 'out')
  const end = getPortPos(toId, 'in')
  return buildBezier(start, end)
}

const tempLinkPath = computed(() => {
  if (!dragState.isLinking || !dragState.linkStartId) return ''
  const start = getPortPos(dragState.linkStartId, 'out')
  const end = dragState.mousePos
  return buildBezier(start, end)
})

function buildBezier(pt1, pt2) {
  const dx = Math.abs(pt1.x - pt2.x)
  let controlOffset = Math.max(dx * 0.5, 60)
  if (pt2.x < pt1.x + 20) {
    controlOffset = Math.max(150, Math.abs(pt1.y - pt2.y) * 0.8)
  }
  const cp1 = { x: pt1.x + controlOffset, y: pt1.y }
  const cp2 = { x: pt2.x - controlOffset, y: pt2.y }
  return `M ${pt1.x} ${pt1.y} C ${cp1.x} ${cp1.y} ${cp2.x} ${cp2.y} ${pt2.x} ${pt2.y}`
}

function edgeColor(edge) {
  if (edge.id === selectedEdgeId.value) return '#ef4444'
  if (edge.id === hoverEdgeId.value) return '#818cf8'
  return '#9ca3af'
}

function markerUrl(edge) {
  if (edge.id === selectedEdgeId.value) return 'url(#arrowhead-selected)'
  if (edge.id === hoverEdgeId.value) return 'url(#arrowhead-active)'
  return 'url(#arrowhead)'
}

// --- 拖拽添加新节点 (Drag & Drop Logic) ---

function onDragStart(evt, item) {
  // 传递节点元数据
  evt.dataTransfer.setData('application/json', JSON.stringify(item))
  evt.dataTransfer.effectAllowed = 'copy'
}

function onDrop(evt) {
  const raw = evt.dataTransfer.getData('application/json')
  if (!raw) return
  
  const item = JSON.parse(raw)
  const container = canvasContainerRef.value
  const rect = container.getBoundingClientRect()
  
  // 计算放置位置：鼠标相对视口坐标 - 容器左上角 + 滚动偏移 - 节点中心修正
  const x = evt.clientX - rect.left + container.scrollLeft - NODE_WIDTH / 2
  const y = evt.clientY - rect.top + container.scrollTop - 40 // 40是高度的一半左右

  const node = {
    id: uid('node'),
    type: item.type,
    label: item.label,
    description: item.sub,
    x: Math.max(0, x), // 防止拖出左上边界
    y: Math.max(0, y),
    meta: { 
      userPrompt: '', // 用户提示词字段
      knowledgeBaseId: '', 
      pluginId: '',
      agentId: item.agentId || '' // 绑定后端已发布 Agent（拖拽真实 Agent 时自动填充）
    }
  }
  nodes.value.push(node)
  selectedNodeId.value = node.id
  showInspector.value = true
}

// --- 普通操作逻辑 ---

function selectNode(id) {
  selectedNodeId.value = id
  selectedEdgeId.value = null
  showInspector.value = true
}

function selectEdge(id) {
  selectedEdgeId.value = id
  selectedNodeId.value = null
}

function clearSelection() {
  selectedNodeId.value = null
  selectedEdgeId.value = null
  showInspector.value = false
}

function deleteSelected() {
  if (selectedNodeId.value) {
    const node = nodes.value.find(n => n.id === selectedNodeId.value)
    if (node && !isSystemNode(node)) {
      removeNode(node.id)
    }
  }
  if (selectedEdgeId.value) {
    edges.value = edges.value.filter(e => e.id !== selectedEdgeId.value)
    selectedEdgeId.value = null
  }
}

function removeNode(id) {
  nodes.value = nodes.value.filter(n => n.id !== id)
  edges.value = edges.value.filter(e => e.from !== id && e.to !== id)
  if (selectedNodeId.value === id) selectedNodeId.value = null
}

function duplicateNode(node) {
  const copy = JSON.parse(JSON.stringify(node))
  copy.id = uid('node')
  copy.x += 30
  copy.y += 30
  nodes.value.push(copy)
  selectedNodeId.value = copy.id
}

// --- 鼠标交互 (Move/Link/Drag) ---

function getCanvasPos(evt) {
  const container = canvasContainerRef.value
  const rect = container.getBoundingClientRect()
  return {
    x: evt.clientX - rect.left + container.scrollLeft,
    y: evt.clientY - rect.top + container.scrollTop
  }
}

function startDragNode(evt, node) {
  dragState.isDraggingNode = true
  dragState.dragNodeId = node.id
  const pos = getCanvasPos(evt)
  dragState.nodeOffsetX = pos.x - node.x
  dragState.nodeOffsetY = pos.y - node.y
}

function startLinkDrag(evt, node) {
  dragState.isLinking = true
  dragState.linkStartId = node.id
  const pos = getCanvasPos(evt)
  dragState.mousePos = pos
}

function handleCanvasMouseDown(evt) {
  // 画布平移功能预留
}

function handleGlobalMouseMove(evt) {
  const pos = getCanvasPos(evt)

  if (dragState.isDraggingNode && dragState.dragNodeId) {
    const node = nodes.value.find(n => n.id === dragState.dragNodeId)
    if (node) {
      node.x = Math.max(0, pos.x - dragState.nodeOffsetX)
      node.y = Math.max(0, pos.y - dragState.nodeOffsetY)
    }
  }

  if (dragState.isLinking) {
    dragState.mousePos = pos
  }
}

function handlePortMouseUp(evt, targetNode) {
  if (dragState.isLinking && dragState.linkStartId) {
    const fromId = dragState.linkStartId
    const toId = targetNode.id
    if (fromId !== toId) {
      const exists = edges.value.some(e => e.from === fromId && e.to === toId)
      if (!exists) {
        edges.value.push({ id: uid('edge'), from: fromId, to: toId })
      } else {
        ElMessage.warning('连接已存在')
      }
    }
  }
}

function handleGlobalMouseUp() {
  dragState.isDraggingNode = false
  dragState.dragNodeId = null
  dragState.isLinking = false
  dragState.linkStartId = null
}

function onKeyDown(evt) {
  if (['Backspace', 'Delete'].includes(evt.key)) {
    if (['INPUT', 'TEXTAREA'].includes(evt.target.tagName)) return
    deleteSelected()
  }
}

// --- 初始化与保存 ---

function seedDefault() {
  nodes.value = [
    { id: 'start', type: 'start', label: 'Start', description: 'Input', x: 50, y: 300, meta: {} },
    { id: 'end', type: 'end', label: 'End', description: 'Output', x: 600, y: 300, meta: {} }
  ]
  edges.value = [] // 默认无连线
  meta.name = ''
  meta.description = ''
  selectedNodeId.value = null
  selectedEdgeId.value = null
}

async function loadWorkflow(id) {
  await store.fetchDetail(id)
  const wf = store.current
  if (!wf) return
  meta.name = wf.name || ''
  meta.description = wf.intro || ''
  meta.triggerType = wf.triggerType || 'manual'
  
  if (wf.graphData) {
    try {
      const graph = typeof wf.graphData === 'string' ? JSON.parse(wf.graphData) : wf.graphData
      nodes.value = graph.nodes || []
      edges.value = graph.edges || []
    } catch (e) {
      console.error('Parse graph error', e)
      seedDefault()
    }
  } else if (Array.isArray(wf.agentIds)) {
    // 后端仅返回 agentIds 时，用简单线性布局复原
    const baseX = 150
    const baseY = 250
    const gap = 220
    nodes.value = [
      { id: 'start', type: 'start', label: 'Start', description: 'Input', x: 30, y: baseY, meta: {} },
      ...wf.agentIds.map((aid, idx) => ({
        id: `node-${idx + 1}`,
        type: 'agent-simple',
        label: `Agent ${idx + 1}`,
        description: '已发布 Agent',
        x: baseX + gap * idx,
        y: baseY,
        meta: { agentId: aid, userPrompt: '', knowledgeBaseId: '', pluginId: '' }
      })),
      { id: 'end', type: 'end', label: 'End', description: 'Output', x: baseX + gap * (wf.agentIds.length || 1), y: baseY, meta: {} }
    ]
    edges.value = wf.agentIds.map((_, idx) => ({
      id: `edge-${idx + 1}`,
      from: idx === 0 ? 'start' : `node-${idx}`,
      to: `node-${idx + 1}`
    })).concat([{
      id: 'edge-end',
      from: wf.agentIds.length ? `node-${wf.agentIds.length}` : 'start',
      to: 'end'
    }])
  } else {
    // 旧数据兼容：略
    seedDefault()
  }
}

// 序列化：实际提交给后端的业务逻辑数组
function serializeSteps() {
  // 简单拓扑排序：找出除了 Start 以外的节点顺序
  // 真实情况：Start -> Next -> Next ... -> End
  // 这里简化：按 X 轴排序返回节点列表，后端按此顺序执行
  return nodes.value
    .filter(n => n.type !== 'start' && n.type !== 'end')
    .sort((a,b) => a.x - b.x)
    .map(n => ({
      id: n.id,
      type: n.type, // 'agent-simple', 'agent-knowledge', etc
      meta: n.meta // 包含 userPrompt, pluginId 等
    }))
}

async function handleSave() {
  if (!meta.name) return ElMessage.warning('请输入工作流名称')
  const orderedSteps = serializeSteps()
  const agentIds = orderedSteps.map(s => s.meta.agentId).filter(Boolean)
  const nodeInputs = orderedSteps.map(s => s.meta.userPrompt || '')
  if (agentIds.length !== orderedSteps.length) {
    return ElMessage.warning('请为所有节点绑定已发布的 Agent')
  }

  // 先确保工作流实体存在
  let workflowId = route.params.id
  if (isNew.value) {
    const created = await store.create({ name: meta.name, intro: meta.description })
    workflowId = created.id
    router.replace({ name: 'workflowBuilder', params: { id: workflowId } })
  }

  // 保存节点顺序、提示词和画布结构，便于运行时复用
  const graphData = { nodes: nodes.value, edges: edges.value }
  await store.update(workflowId, { agentIds, nodeInputs, graphData })
  ElMessage.success('保存成功')
}

async function handleRun() {
  if (!route.params.id && isNew.value) {
    return ElMessage.warning('请先保存工作流后再运行')
  }

  const orderedSteps = serializeSteps()
  const agentIds = orderedSteps.map(s => s.meta.agentId).filter(Boolean)
  if (!agentIds.length) {
    return ElMessage.warning('请先添加并绑定 Agent 后再运行')
  }

  const { value: inputValue } = await ElMessageBox.prompt('请输入运行时初始输入', '运行工作流', {
    confirmButtonText: '运行',
    cancelButtonText: '取消',
    inputPlaceholder: '例如：帮我总结最新的产品更新',
    inputValue: ''
  }).catch(() => ({ value: null }))

  if (inputValue === null || inputValue === undefined) return

  const nodeInputs = orderedSteps.map(step => step.meta.userPrompt || '')
  const graphData = { nodes: nodes.value, edges: edges.value }
  const workflowId = route.params.id
  // 这里还是没有传图信息
  // const resp = await store.run(workflowId, { input: inputValue, nodeInputs, graphData })
  const resp = await store.run(workflowId, { input: inputValue, nodeInputs, graphData })
  runResult.value = resp
  showRunResult.value = true
  ElMessage.success('运行完成')
}

onMounted(async () => {
  window.addEventListener('keydown', onKeyDown)
  await agentsStore.fetchList({ status: 'published' })
  if (route.params.id) {
    await loadWorkflow(route.params.id)
  } else {
    seedDefault()
  }
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKeyDown)
})
</script>

<style scoped>
/* 网点背景 */
.bg-canvas {
  background-color: #f8fafc;
  background-image: 
    linear-gradient(#e2e8f0 1px, transparent 1px),
    linear-gradient(90deg, #e2e8f0 1px, transparent 1px);
  background-size: 20px 20px;
}

/* 节点卡片样式 */
.node-card {
  position: absolute;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  user-select: none;
  cursor: grab;
  color: #334155;
  transition: box-shadow 0.2s, border-color 0.2s;
  z-index: 10;
}
.node-card:active {
  cursor: grabbing;
}
.node-card:hover {
  border-color: #cbd5e1;
}

.node-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  font-size: 14px;
}

/* 端口容器 */
.port-container {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  gap: 4px;
  z-index: 20;
}
.port-left {
  left: -8px; 
  padding: 10px; 
  margin-left: -10px;
}
.port-right {
  right: -8px;
  padding: 10px;
  margin-right: -10px;
  cursor: crosshair;
}

/* 实际的小圆点 */
.port-point {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #cbd5e1;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px #94a3b8;
  transition: all 0.2s;
}

.port-label {
  font-size: 9px;
  color: #94a3b8;
  opacity: 0;
  transition: opacity 0.2s;
  pointer-events: none;
  font-weight: 600;
}
.node-card:hover .port-label {
  opacity: 1;
}

/* 组件库项目 */
.palette-item {
  background: white;
  border: 1px solid #f1f5f9;
  border-radius: 8px;
  padding: 12px;
  cursor: grab;
  transition: all 0.2s;
  user-select: none;
}
.palette-item:active {
  cursor: grabbing;
}
.palette-item:hover {
  background: #f8fafc;
  border-color: #6366f1;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(99,102,241,0.1);
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #e2e8f0; 
  border-radius: 4px;
}
</style>
