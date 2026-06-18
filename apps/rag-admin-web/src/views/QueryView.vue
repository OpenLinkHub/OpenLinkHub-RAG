<template>
  <section class="section-grid">
    <div class="section-header">
      <div>
        <h2>查询测试</h2>
        <p>通过管理后端代理 `/query/stream`，验证当前知识库问答效果。</p>
      </div>
      <button type="button" @click="queryAnswer = ''">清空回答</button>
    </div>

    <p v-if="errorMessage" class="error-line">{{ errorMessage }}</p>

    <div class="toolbar">
      <select v-model="selectedKnowledgeBaseId" class="kb-select">
        <option v-for="kb in knowledgeBases" :key="kb.id" :value="kb.id">{{ kb.name }}</option>
      </select>
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
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { API_BASE } from '../api/config.js';
import { request } from '../api/request.js';
import { useAuth } from '../composables/useAuth.js';

const { token } = useAuth();

const errorMessage = ref('');
const busy = ref(false);
const knowledgeBases = ref([]);
const selectedKnowledgeBaseId = ref('');
const queryText = ref('');
const queryAnswer = ref('');
const queryReferences = ref([]);

const currentKbId = computed(() => selectedKnowledgeBaseId.value || knowledgeBases.value[0]?.id || 'kb-default');

onMounted(async () => {
  knowledgeBases.value = await request('/knowledge-bases');
  selectedKnowledgeBaseId.value = knowledgeBases.value[0]?.id || '';
});

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
</script>
