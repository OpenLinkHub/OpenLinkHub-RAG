<template>
  <section class="section-grid">
    <div class="section-header">
      <div>
        <h2>部门管理</h2>
        <p>维护组织部门树，用于用户归属与权限划分。</p>
      </div>
      <button type="button" @click="openForm()">新增部门</button>
    </div>

    <p v-if="localError" class="error-line">{{ localError }}</p>

    <div class="table tree-table-wrap">
      <div class="table-row table-head cols-dept">
        <span>名称</span><span>编码</span><span>排序</span><span>状态</span><span>操作</span>
      </div>
      <TreeTableNode
        v-for="node in deptTree"
        :key="node.id"
        :node="node"
        children-key="children"
        row-key="id"
        row-class="cols-dept"
        :depth="0"
        :expanded-ids="expandedIds"
        @toggle="toggleExpand"
      >
        <template #row="{ node, depth, hasChildren, expanded, toggle }">
          <span class="tree-label-cell" :style="{ paddingLeft: `${depth * 16}px` }">
            <button v-if="hasChildren" type="button" class="tree-toggle" @click="toggle">{{ expanded ? '▾' : '▸' }}</button>
            <span v-else class="tree-toggle-placeholder" />
            <span>{{ node.name }}</span>
          </span>
          <span>{{ node.code }}</span>
          <span>{{ node.sortOrder }}</span>
          <span>{{ node.status === 1 ? '启用' : '禁用' }}</span>
        </template>
        <template #actions="{ node }">
          <button type="button" @click="openForm(node)">编辑</button>
          <button type="button" class="danger" :disabled="node.id === 1" @click="removeItem(node)">删除</button>
        </template>
      </TreeTableNode>
      <div v-if="!deptTree.length" class="table-empty">暂无数据</div>
    </div>

    <div v-if="formVisible" class="modal-mask" @click.self="formVisible = false">
      <form class="modal-card" @submit.prevent="submitForm">
        <h3>{{ formTitle }}</h3>
        <label>上级部门</label>
        <TreePicker
          v-model="form.parentId"
          :nodes="deptTree"
          children-key="children"
          :exclude-id="editingId"
          root-label="无（顶级）"
        />
        <label>名称<input v-model="form.name" required /></label>
        <label>编码<input v-model="form.code" required /></label>
        <label>排序<input v-model.number="form.sortOrder" type="number" /></label>
        <label>状态
          <select v-model.number="form.status"><option :value="1">启用</option><option :value="0">禁用</option></select>
        </label>
        <div class="modal-actions">
          <button type="button" @click="formVisible = false">取消</button>
          <button type="submit">保存</button>
        </div>
      </form>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue';
import TreePicker from '../../components/tree/TreePicker.vue';
import TreeTableNode from '../../components/tree/TreeTableNode.vue';
import { flattenTree } from '../../utils/system.js';
import { request } from '../../api/request.js';

const localError = ref('');
const deptTree = ref([]);
const expandedIds = ref(new Set());
const formVisible = ref(false);
const formTitle = ref('');
const editingId = ref(null);
const form = ref(emptyForm());

onMounted(() => loadData());

watch(deptTree, (nodes) => {
  expandedIds.value = new Set(flattenTree(nodes, 'children').map((n) => n.id));
}, { immediate: true, deep: true });

function emptyForm() {
  return { parentId: null, name: '', code: '', sortOrder: 0, status: 1 };
}

function toggleExpand(id) {
  const next = new Set(expandedIds.value);
  if (next.has(id)) next.delete(id);
  else next.add(id);
  expandedIds.value = next;
}

async function loadData() {
  localError.value = '';
  try {
    deptTree.value = await request('/system/depts/tree');
  } catch (error) {
    localError.value = error.message;
  }
}

function openForm(item) {
  editingId.value = item?.id || null;
  formTitle.value = item ? '编辑部门' : '新增部门';
  form.value = item
    ? { parentId: item.parentId, name: item.name, code: item.code, sortOrder: item.sortOrder, status: item.status }
    : emptyForm();
  formVisible.value = true;
}

async function submitForm() {
  localError.value = '';
  try {
    if (editingId.value) await request(`/system/depts/${editingId.value}`, { method: 'PUT', body: form.value });
    else await request('/system/depts', { method: 'POST', body: form.value });
    formVisible.value = false;
    await loadData();
  } catch (error) {
    localError.value = error.message;
  }
}

async function removeItem(item) {
  if (!window.confirm(`删除部门 ${item.name}？`)) return;
  try {
    await request(`/system/depts/${item.id}`, { method: 'DELETE' });
    await loadData();
  } catch (error) {
    localError.value = error.message;
  }
}
</script>
