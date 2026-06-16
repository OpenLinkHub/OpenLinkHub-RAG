<template>
  <main v-if="!token" class="login-shell">
    <section class="login-panel">
      <p class="eyebrow">OpenLinkHub RAG Admin</p>
      <h1>知识库管理系统</h1>
      <form @submit.prevent="login">
        <label>
          用户名
          <input v-model="loginForm.username" autocomplete="username" />
        </label>
        <label>
          密码
          <input v-model="loginForm.password" type="password" autocomplete="current-password" />
        </label>
        <button type="submit" :disabled="busy">登录</button>
      </form>
      <p class="hint">默认账号：admin / admin123</p>
      <p v-if="errorMessage" class="error-line">{{ errorMessage }}</p>
    </section>
  </main>

  <main v-else class="admin-shell">
    <aside class="sidebar">
      <div class="brand">
        <span>RAG</span>
        <div>
          <strong>OpenLinkHub</strong>
          <p>Knowledge Console</p>
        </div>
      </div>
      <button
        v-for="item in navItems"
        :key="item.key"
        class="nav-item"
        :class="{ active: activeView === item.key }"
        type="button"
        @click="activeView = item.key"
      >
        {{ item.label }}
      </button>
    </aside>

    <section class="workspace">
      <header class="topbar">
        <div>
          <p class="eyebrow">LightRAG Management Layer</p>
          <h1>{{ activeTitle }}</h1>
        </div>
        <div class="user-block">
          <span>{{ user?.displayName }}</span>
          <button type="button" @click="logout">退出</button>
        </div>
      </header>

      <section class="content">
        <div v-if="errorMessage" class="error-line">{{ errorMessage }}</div>

        <section v-if="activeView === 'dashboard'" class="dashboard-grid">
          <article class="metric-card">
            <p>知识库</p>
            <strong>{{ knowledgeBases.length }}</strong>
            <span>已登记 LightRAG 管理对象</span>
          </article>
          <article class="metric-card">
            <p>Endpoint</p>
            <strong>{{ endpoints.length }}</strong>
            <span>LightRAG 运行实例</span>
          </article>
          <article class="metric-card">
            <p>模型配置</p>
            <strong>{{ modelProfiles.length }}</strong>
            <span>可绑定配置档案</span>
          </article>
          <article class="metric-card">
            <p>当前用户</p>
            <strong>{{ user?.roles?.join(', ') }}</strong>
            <span>{{ user?.permissions?.length || 0 }} permissions</span>
          </article>
        </section>

        <section v-if="activeView === 'knowledge'" class="section-grid">
          <div class="section-header">
            <div>
              <h2>知识库</h2>
              <p>知识库绑定 LightRAG endpoint，用于授权、文档管理和查询测试。</p>
            </div>
            <button type="button" @click="createKnowledgeBase">创建样例知识库</button>
          </div>
          <div class="table">
            <div class="table-row table-head">
              <span>名称</span>
              <span>编码</span>
              <span>Endpoint</span>
              <span>查询模式</span>
              <span>模型状态</span>
            </div>
            <button
              v-for="kb in knowledgeBases"
              :key="kb.id"
              class="table-row selectable"
              :class="{ selected: selectedKnowledgeBaseId === kb.id }"
              type="button"
              @click="selectedKnowledgeBaseId = kb.id"
            >
              <span>{{ kb.name }}</span>
              <span>{{ kb.code }}</span>
              <span>{{ endpointName(kb.endpointId) }}</span>
              <span>{{ kb.defaultQueryMode }}</span>
              <span>{{ kb.pendingRestart ? '待重启' : '已生效' }}</span>
            </button>
          </div>
        </section>

        <section v-if="activeView === 'documents'" class="section-grid">
          <div class="section-header">
            <div>
              <h2>文档管理</h2>
              <p>对接 LightRAG Documents API：上传、导入、分页查询、流水线监控与批量操作。</p>
            </div>
            <div class="toolbar">
              <select v-model="selectedKnowledgeBaseId" class="kb-select">
                <option v-for="kb in knowledgeBases" :key="kb.id" :value="kb.id">{{ kb.name }}</option>
              </select>
              <button type="button" @click="refreshDocumentsView">刷新</button>
            </div>
          </div>

          <article class="pipeline-card">
            <div class="pipeline-head">
              <h3>处理流水线</h3>
              <span class="status-pill" :class="pipelineStatus.busy ? 'busy' : 'idle'">
                {{ pipelineStatus.busy ? '处理中' : '空闲' }}
              </span>
            </div>
            <div class="pipeline-grid">
              <div>
                <p>任务</p>
                <strong>{{ pipelineStatus.job_name || '-' }}</strong>
              </div>
              <div>
                <p>文档数</p>
                <strong>{{ pipelineStatus.docs ?? 0 }}</strong>
              </div>
              <div>
                <p>批次</p>
                <strong>{{ pipelineStatus.cur_batch ?? 0 }} / {{ pipelineStatus.batchs ?? 0 }}</strong>
              </div>
              <div>
                <p>最新消息</p>
                <strong class="pipeline-message">{{ pipelineStatus.latest_message || '暂无' }}</strong>
              </div>
            </div>
            <div class="action-row">
              <button type="button" @click="scanDocuments">扫描新文档</button>
              <button type="button" @click="reprocessFailed">重试失败</button>
              <button type="button" @click="cancelPipeline">取消流水线</button>
              <button type="button" @click="clearCache">清理缓存</button>
              <button type="button" class="danger" @click="clearAllDocuments">清空全部</button>
            </div>
          </article>

          <div class="status-counts" v-if="statusCounts">
            <button
              v-for="(count, status) in statusCounts"
              :key="status"
              type="button"
              class="status-chip"
              :class="{ active: docStatusFilter === status }"
              @click="toggleStatusFilter(status)"
            >
              {{ status }} <strong>{{ count }}</strong>
            </button>
            <button
              v-if="docStatusFilter"
              type="button"
              class="status-chip clear"
              @click="docStatusFilter = ''; loadDocuments()"
            >
              清除筛选
            </button>
          </div>

          <div class="split-panel">
            <div class="form-card">
              <h3>文本导入</h3>
              <textarea v-model="textDocument" placeholder="输入要写入知识库的文本..." />
              <button type="button" @click="insertText">写入 LightRAG</button>
              <p v-if="lastTrackId" class="hint">最近 track_id: {{ lastTrackId }}</p>
            </div>
            <div class="form-card">
              <h3>文件上传</h3>
              <input type="file" @change="onFileChange" />
              <button type="button" :disabled="!selectedFile" @click="uploadFile">上传文件</button>
              <div class="track-lookup">
                <input v-model="trackIdInput" placeholder="输入 track_id 查询进度" />
                <button type="button" :disabled="!trackIdInput.trim()" @click="lookupTrackStatus">查询</button>
              </div>
            </div>
          </div>

          <div class="table doc-table">
            <div class="table-row table-head">
              <span>文件</span>
              <span>状态</span>
              <span>摘要</span>
              <span>分块</span>
              <span>更新时间</span>
              <span>操作</span>
            </div>
            <div v-if="!documents.length" class="table-empty">暂无文档，请上传文件或导入文本。</div>
            <div v-for="doc in documents" :key="doc.id" class="table-row">
              <span class="mono" :title="doc.file_path">{{ doc.file_path }}</span>
              <span><span class="status-pill small" :class="docStatusClass(doc.status)">{{ doc.status }}</span></span>
              <span class="ellipsis" :title="doc.content_summary">{{ doc.content_summary }}</span>
              <span>{{ doc.chunks_count ?? '-' }}</span>
              <span>{{ formatTime(doc.updated_at) }}</span>
              <span class="row-actions">
                <button type="button" class="danger" @click="deleteDocument(doc)">删除</button>
              </span>
            </div>
          </div>

          <div class="pagination-bar" v-if="pagination.total_pages > 1">
            <button type="button" :disabled="!pagination.has_prev" @click="changePage(pagination.page - 1)">上一页</button>
            <span>第 {{ pagination.page }} / {{ pagination.total_pages }} 页，共 {{ pagination.total_count }} 条</span>
            <button type="button" :disabled="!pagination.has_next" @click="changePage(pagination.page + 1)">下一页</button>
          </div>

          <details v-if="lastActionResult" class="json-details">
            <summary>最近操作结果</summary>
            <pre class="json-panel">{{ lastActionResult }}</pre>
          </details>
        </section>

        <section v-if="activeView === 'query'" class="section-grid">
          <div class="section-header">
            <div>
              <h2>查询测试</h2>
              <p>通过管理后端代理 `/query/stream`，验证当前知识库问答效果。</p>
            </div>
            <button type="button" @click="queryAnswer = ''">清空回答</button>
          </div>
          <div class="query-panel">
            <textarea v-model="queryText" placeholder="输入测试问题..." />
            <button type="button" :disabled="!queryText.trim() || busy" @click="runStreamQuery">流式查询</button>
          </div>
          <article class="answer-panel">
            <p>{{ queryAnswer || '回答会流式显示在这里。' }}</p>
            <details v-if="queryReferences.length">
              <summary>查看引用资料</summary>
              <pre>{{ JSON.stringify(queryReferences, null, 2) }}</pre>
            </details>
          </article>
        </section>

        <section v-if="activeView === 'models'" class="section-grid">
          <div class="section-header">
            <div>
              <h2>模型配置</h2>
              <p>MVP 保存配置档案并绑定知识库，切换后提示重启 LightRAG。</p>
            </div>
            <button type="button" @click="switchDefaultModel">绑定默认配置</button>
          </div>
          <div class="table model-table">
            <div class="table-row table-head">
              <span>名称</span>
              <span>LLM</span>
              <span>Embedding</span>
              <span>状态</span>
            </div>
            <div v-for="profile in modelProfiles" :key="profile.id" class="table-row">
              <span>{{ profile.name }}</span>
              <span>{{ profile.llmBinding }} / {{ profile.llmModel }}</span>
              <span>{{ profile.embeddingBinding }} / {{ profile.embeddingModel }}</span>
              <span>{{ profile.status }}</span>
            </div>
          </div>
        </section>

        <section v-if="activeView === 'system'" class="section-grid">
          <div class="section-header">
            <div>
              <h2>系统状态</h2>
              <p>检查管理系统登记的 LightRAG endpoint 健康状态。</p>
            </div>
          </div>
          <div class="endpoint-grid">
            <article v-for="endpoint in endpoints" :key="endpoint.id" class="endpoint-card">
              <h3>{{ endpoint.name }}</h3>
              <p>{{ endpoint.baseUrl }}</p>
              <button type="button" @click="checkHealth(endpoint.id)">健康检查</button>
            </article>
          </div>
          <pre class="json-panel">{{ healthJson }}</pre>
        </section>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';

const API_BASE = import.meta.env.VITE_RAG_ADMIN_API_URL || 'http://localhost:8091/api/admin';

const token = ref(localStorage.getItem('rag_admin_token') || '');
const user = ref(null);
const busy = ref(false);
const errorMessage = ref('');
const activeView = ref('dashboard');
const loginForm = ref({ username: 'admin', password: 'admin123' });
const knowledgeBases = ref([]);
const endpoints = ref([]);
const modelProfiles = ref([]);
const selectedKnowledgeBaseId = ref('kb-default');
const selectedFile = ref(null);
const textDocument = ref('');
const documents = ref([]);
const pagination = ref({
  page: 1,
  page_size: 20,
  total_count: 0,
  total_pages: 1,
  has_next: false,
  has_prev: false
});
const statusCounts = ref(null);
const pipelineStatus = ref({});
const docStatusFilter = ref('');
const docPage = ref(1);
const lastTrackId = ref('');
const trackIdInput = ref('');
const lastActionResult = ref('');
const healthJson = ref('');
const queryText = ref('');
const queryAnswer = ref('');
const queryReferences = ref([]);

const navItems = [
  { key: 'dashboard', label: '仪表盘' },
  { key: 'knowledge', label: '知识库' },
  { key: 'documents', label: '文档管理' },
  { key: 'query', label: '查询测试' },
  { key: 'models', label: '模型配置' },
  { key: 'system', label: '系统状态' }
];
const activeTitle = computed(() => navItems.find((item) => item.key === activeView.value)?.label || 'RAG Admin');
const currentKbId = computed(() => selectedKnowledgeBaseId.value || knowledgeBases.value[0]?.id || 'kb-default');

onMounted(async () => {
  if (token.value) {
    await bootstrap();
  }
});

watch(activeView, async (view) => {
  if (view === 'documents' && token.value) {
    await refreshDocumentsView();
  }
});

watch(selectedKnowledgeBaseId, async () => {
  if (activeView.value === 'documents' && token.value) {
    docPage.value = 1;
    await refreshDocumentsView();
  }
});

async function login() {
  busy.value = true;
  errorMessage.value = '';
  try {
    const result = await request('/auth/login', {
      method: 'POST',
      body: loginForm.value,
      auth: false
    });
    token.value = result.token;
    user.value = result.user;
    localStorage.setItem('rag_admin_token', token.value);
    await bootstrap();
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    busy.value = false;
  }
}

function logout() {
  token.value = '';
  user.value = null;
  localStorage.removeItem('rag_admin_token');
}

async function bootstrap() {
  user.value = await request('/auth/me');
  await Promise.all([loadKnowledgeBases(), loadEndpoints(), loadModelProfiles()]);
}

async function loadKnowledgeBases() {
  knowledgeBases.value = await request('/knowledge-bases');
  selectedKnowledgeBaseId.value = selectedKnowledgeBaseId.value || knowledgeBases.value[0]?.id;
}

async function loadEndpoints() {
  endpoints.value = await request('/endpoints');
}

async function loadModelProfiles() {
  modelProfiles.value = await request('/model-profiles');
}

async function createKnowledgeBase() {
  const timestamp = Date.now().toString().slice(-5);
  await request('/knowledge-bases', {
    method: 'POST',
    body: {
      name: `样例知识库 ${timestamp}`,
      code: `sample-${timestamp}`,
      endpointId: endpoints.value[0]?.id || 'endpoint-local',
      defaultQueryMode: 'mix'
    }
  });
  await loadKnowledgeBases();
}

async function refreshDocumentsView() {
  await Promise.all([loadDocuments(), loadPipelineStatus(), loadStatusCounts()]);
}

async function loadDocuments() {
  const body = {
    page: docPage.value,
    page_size: pagination.value.page_size,
    sort_field: 'updated_at',
    sort_direction: 'desc'
  };
  if (docStatusFilter.value) {
    body.status_filters = [docStatusFilter.value];
  }
  const result = await request(`/knowledge-bases/${currentKbId.value}/documents`, {
    method: 'POST',
    body
  });
  documents.value = result.documents || [];
  pagination.value = result.pagination || pagination.value;
  if (result.status_counts) {
    statusCounts.value = result.status_counts;
  }
}

async function loadPipelineStatus() {
  pipelineStatus.value = await request(`/knowledge-bases/${currentKbId.value}/pipeline-status`);
}

async function loadStatusCounts() {
  const result = await request(`/knowledge-bases/${currentKbId.value}/documents/status-counts`);
  statusCounts.value = result.status_counts || result;
}

function toggleStatusFilter(status) {
  docStatusFilter.value = docStatusFilter.value === status ? '' : status;
  docPage.value = 1;
  loadDocuments();
}

function changePage(page) {
  docPage.value = page;
  loadDocuments();
}

function docStatusClass(status) {
  const normalized = String(status || '').toLowerCase();
  if (normalized === 'processed') return 'processed';
  if (normalized === 'failed') return 'failed';
  if (['pending', 'parsing', 'analyzing', 'processing', 'preprocessed'].includes(normalized)) return 'processing';
  return 'idle';
}

function formatTime(value) {
  if (!value) return '-';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
}

function setActionResult(result) {
  lastActionResult.value = JSON.stringify(result, null, 2);
}

async function runDocumentAction(label, path, options = {}) {
  errorMessage.value = '';
  try {
    const result = await request(path, options);
    setActionResult(result);
    if (result.track_id) {
      lastTrackId.value = result.track_id;
      trackIdInput.value = result.track_id;
    }
    await refreshDocumentsView();
    return result;
  } catch (error) {
    errorMessage.value = `${label}失败: ${error.message}`;
    throw error;
  }
}

function onFileChange(event) {
  selectedFile.value = event.target.files?.[0] || null;
}

async function uploadFile() {
  if (!selectedFile.value) return;
  const form = new FormData();
  form.append('file', selectedFile.value);
  await runDocumentAction('上传', `/knowledge-bases/${currentKbId.value}/documents/upload`, {
    method: 'POST',
    body: form
  });
  selectedFile.value = null;
}

async function insertText() {
  if (!textDocument.value.trim()) return;
  await runDocumentAction('文本导入', `/knowledge-bases/${currentKbId.value}/documents/text`, {
    method: 'POST',
    body: {
      text: textDocument.value,
      file_source: `manual-${Date.now()}.txt`
    }
  });
  textDocument.value = '';
}

async function scanDocuments() {
  await runDocumentAction('扫描', `/knowledge-bases/${currentKbId.value}/documents/scan`, { method: 'POST' });
}

async function reprocessFailed() {
  await runDocumentAction('重试失败文档', `/knowledge-bases/${currentKbId.value}/documents/reprocess-failed`, { method: 'POST' });
}

async function cancelPipeline() {
  await runDocumentAction('取消流水线', `/knowledge-bases/${currentKbId.value}/documents/cancel-pipeline`, { method: 'POST' });
}

async function clearCache() {
  await runDocumentAction('清理缓存', `/knowledge-bases/${currentKbId.value}/documents/clear-cache`, { method: 'POST' });
}

async function clearAllDocuments() {
  if (!window.confirm('确定清空当前知识库的全部文档？此操作不可撤销。')) return;
  await runDocumentAction('清空文档', `/knowledge-bases/${currentKbId.value}/documents`, { method: 'DELETE' });
}

async function lookupTrackStatus() {
  if (!trackIdInput.value.trim()) return;
  await runDocumentAction('查询进度', `/knowledge-bases/${currentKbId.value}/documents/track-status/${encodeURIComponent(trackIdInput.value.trim())}`);
}

async function deleteDocument(doc) {
  if (!window.confirm(`确定删除文档 ${doc.file_path || doc.id}？`)) return;
  const deleteFile = window.confirm('是否同时删除上传目录中的源文件？');
  await runDocumentAction('删除文档', `/knowledge-bases/${currentKbId.value}/documents/${doc.id}?deleteFile=${deleteFile}`, {
    method: 'DELETE'
  });
}

async function runStreamQuery() {
  busy.value = true;
  queryAnswer.value = '';
  queryReferences.value = [];
  errorMessage.value = '';
  try {
    const response = await fetch(`${API_BASE}/knowledge-bases/${currentKbId.value}/query/stream`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token.value}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        query: queryText.value,
        mode: 'mix',
        include_references: true,
        response_type: 'Multiple Paragraphs'
      })
    });
    if (!response.ok) {
      throw new Error(`Query failed: HTTP ${response.status}`);
    }
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';
    while (true) {
      const { value, done } = await reader.read();
      if (done) break;
      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split('\n');
      buffer = lines.pop() || '';
      for (const line of lines) {
        handleNdjsonLine(line);
      }
    }
    if (buffer.trim()) {
      handleNdjsonLine(buffer);
    }
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    busy.value = false;
  }
}

function handleNdjsonLine(line) {
  if (!line.trim()) return;
  const data = JSON.parse(line);
  if (data.references) {
    queryReferences.value = data.references;
  }
  if (data.response) {
    queryAnswer.value += data.response;
  }
  if (data.error) {
    errorMessage.value = data.error;
  }
}

async function switchDefaultModel() {
  await request(`/knowledge-bases/${currentKbId.value}/model-profile`, {
    method: 'POST',
    body: { modelProfileId: modelProfiles.value[0]?.id || 'model-default' }
  });
  await loadKnowledgeBases();
}

async function checkHealth(endpointId) {
  healthJson.value = JSON.stringify(await request(`/system/endpoints/${endpointId}/health`), null, 2);
}

function endpointName(endpointId) {
  return endpoints.value.find((endpoint) => endpoint.id === endpointId)?.name || endpointId;
}

async function request(path, options = {}) {
  const headers = {};
  const isForm = options.body instanceof FormData;
  if (!isForm) {
    headers['Content-Type'] = 'application/json';
  }
  if (options.auth !== false && token.value) {
    headers.Authorization = `Bearer ${token.value}`;
  }
  const response = await fetch(`${API_BASE}${path}`, {
    method: options.method || 'GET',
    headers,
    body: options.body ? (isForm ? options.body : JSON.stringify(options.body)) : undefined
  });
  const payload = await response.json();
  if (!response.ok || payload.success === false) {
    throw new Error(payload.message || `HTTP ${response.status}`);
  }
  return payload.data;
}
</script>
