<template>
  <section class="section-grid">
    <div class="section-header">
      <div>
        <h2>菜单管理</h2>
        <p>配置导航菜单、路由路径与权限标识。</p>
      </div>
      <button type="button" @click="openForm()">新增菜单</button>
    </div>

    <p v-if="localError" class="error-line">{{ localError }}</p>

    <div class="table tree-table-wrap">
      <div class="table-row table-head cols-menu">
        <span>名称</span><span>路径</span><span>权限标识</span><span>类型</span><span>排序</span><span>操作</span>
      </div>
      <TreeTableNode
        v-for="node in menuTree"
        :key="node.id"
        :node="node"
        children-key="children"
        row-key="id"
        row-class="cols-menu"
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
          <span>{{ node.path || '-' }}</span>
          <span class="mono">{{ node.permission || '-' }}</span>
          <span>{{ node.menuType }}</span>
          <span>{{ node.sortOrder }}</span>
        </template>
        <template #actions="{ node }">
          <button type="button" @click="openForm(node)">编辑</button>
          <button type="button" class="danger" @click="removeItem(node)">删除</button>
        </template>
      </TreeTableNode>
      <div v-if="!menuTree.length" class="table-empty">暂无数据</div>
    </div>

    <div v-if="formVisible" class="modal-mask" @click.self="formVisible = false">
      <form class="modal-card" @submit.prevent="submitForm">
        <h3>{{ formTitle }}</h3>
        <label>上级菜单</label>
        <TreePicker
          v-model="form.parentId"
          :nodes="menuTree"
          children-key="children"
          :exclude-id="editingId"
          root-label="无（顶级）"
        />
        <label>名称<input v-model="form.name" required /></label>
        <label>路由路径<input v-model="form.path" placeholder="如 system-users" /></label>
        <label>权限标识<input v-model="form.permission" placeholder="如 system:user:list" /></label>
        <label>类型
          <select v-model="form.menuType"><option value="M">目录</option><option value="C">菜单</option><option value="F">按钮</option></select>
        </label>
        <label>图标<input v-model="form.icon" /></label>
        <label>排序<input v-model.number="form.sortOrder" type="number" /></label>
        <label>可见<input type="checkbox" v-model="form.visible" /></label>
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
const menuTree = ref([]);
const expandedIds = ref(new Set());
const formVisible = ref(false);
const formTitle = ref('');
const editingId = ref(null);
const form = ref(emptyForm());

onMounted(() => loadData());

watch(menuTree, (nodes) => {
  expandedIds.value = new Set(flattenTree(nodes).map((n) => n.id));
}, { immediate: true, deep: true });

function emptyForm() {
  return { parentId: null, name: '', path: '', permission: '', menuType: 'C', icon: '', sortOrder: 0, visible: true, status: 1 };
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
    menuTree.value = await request('/system/menus/tree');
  } catch (error) {
    localError.value = error.message;
  }
}

function openForm(item) {
  editingId.value = item?.id || null;
  formTitle.value = item ? '编辑菜单' : '新增菜单';
  form.value = item
    ? { parentId: item.parentId, name: item.name, path: item.path || '', permission: item.permission || '', menuType: item.menuType, icon: item.icon || '', sortOrder: item.sortOrder, visible: item.visible, status: item.status }
    : emptyForm();
  formVisible.value = true;
}

async function submitForm() {
  localError.value = '';
  try {
    if (editingId.value) await request(`/system/menus/${editingId.value}`, { method: 'PUT', body: form.value });
    else await request('/system/menus', { method: 'POST', body: form.value });
    formVisible.value = false;
    await loadData();
  } catch (error) {
    localError.value = error.message;
  }
}

async function removeItem(item) {
  if (!window.confirm(`删除菜单 ${item.name}？`)) return;
  try {
    await request(`/system/menus/${item.id}`, { method: 'DELETE' });
    await loadData();
  } catch (error) {
    localError.value = error.message;
  }
}
</script>
