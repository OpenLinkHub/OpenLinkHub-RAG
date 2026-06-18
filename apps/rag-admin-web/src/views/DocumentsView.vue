<template>
  <section class="section-grid">
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

    <p v-if="errorMessage" class="error-line">{{ errorMessage }}</p>

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

    <div v-if="statusCounts" class="status-counts">
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

    <div v-if="pagination.total_pages > 1" class="pagination-bar">
      <button type="button" :disabled="!pagination.has_prev" @click="changePage(pagination.page - 1)">上一页</button>
      <span>第 {{ pagination.page }} / {{ pagination.total_pages }} 页，共 {{ pagination.total_count }} 条</span>
      <button type="button" :disabled="!pagination.has_next" @click="changePage(pagination.page + 1)">下一页</button>
    </div>

    <details v-if="lastActionResult" class="json-details">
      <summary>最近操作结果</summary>
      <pre class="json-panel">{{ lastActionResult }}</pre>
    </details>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { request } from '../api/request.js';

const errorMessage = ref('');
const knowledgeBases = ref([]);
const selectedKnowledgeBaseId = ref('');
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

const currentKbId = computed(() => selectedKnowledgeBaseId.value || knowledgeBases.value[0]?.id || 'kb-default');

onMounted(async () => {
  knowledgeBases.value = await request('/knowledge-bases');
  selectedKnowledgeBaseId.value = knowledgeBases.value[0]?.id || '';
  await refreshDocumentsView();
});

watch(selectedKnowledgeBaseId, async () => {
  docPage.value = 1;
  await refreshDocumentsView();
});

async function refreshDocumentsView() {
  if (!currentKbId.value) return;
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
</script>
