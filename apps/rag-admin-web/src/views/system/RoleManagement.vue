<template>
  <section class="section-grid">
    <div class="section-header">
      <div>
        <h2>角色管理</h2>
        <p>定义角色编码、名称与菜单权限范围。</p>
      </div>
      <button type="button" @click="openForm()">新增角色</button>
    </div>

    <ListToolbar
      v-model:keyword="keyword"
      label="搜索角色"
      placeholder="编码 / 名称 / 备注"
      @search="search"
      @reset="reset"
    />

    <p v-if="localError || error" class="error-line">{{ localError || error }}</p>

    <div class="table">
      <div class="table-row table-head cols-role">
        <span>编码</span><span>名称</span><span>菜单数</span><span>状态</span><span>操作</span>
      </div>
      <div v-if="loading" class="table-empty">加载中...</div>
      <div v-else-if="!items.length" class="table-empty">暂无数据</div>
      <div v-for="item in items" :key="item.id" class="table-row cols-role">
        <span>{{ item.code }}</span>
        <span>{{ item.name }}</span>
        <span>{{ item.menuIds?.length || 0 }}</span>
        <span>{{ item.status === 1 ? '启用' : '禁用' }}</span>
        <span class="row-actions">
          <button type="button" @click="openForm(item)">编辑</button>
          <button type="button" class="danger" :disabled="item.code === 'SUPER_ADMIN'" @click="removeItem(item)">删除</button>
        </span>
      </div>
    </div>

    <ListPagination
      :page="page"
      :page-size="pageSize"
      :total="total"
      :total-pages="totalPages"
      @change-page="changePage"
      @change-page-size="changePageSize"
    />

    <div v-if="formVisible" class="modal-mask" @click.self="formVisible = false">
      <form class="modal-card modal-card-wide" @submit.prevent="submitForm">
        <h3>{{ formTitle }}</h3>
        <label>编码<input v-model="form.code" required :disabled="editingId && form.code === 'SUPER_ADMIN'" /></label>
        <label>名称<input v-model="form.name" required /></label>
        <label>备注<input v-model="form.remark" /></label>
        <label>状态
          <select v-model.number="form.status"><option :value="1">启用</option><option :value="0">禁用</option></select>
        </label>
        <label>菜单权限（多选）</label>
        <select v-model="form.menuIds" multiple size="8" class="tree-multi-select">
          <option v-for="m in flatMenus" :key="m.id" :value="m.id">{{ '—'.repeat(m.depth) }}{{ m.name }}</option>
        </select>
        <div class="modal-actions">
          <button type="button" @click="formVisible = false">取消</button>
          <button type="submit">保存</button>
        </div>
      </form>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import ListToolbar from '../../components/list/ListToolbar.vue';
import ListPagination from '../../components/list/ListPagination.vue';
import { usePagedList } from '../../composables/usePagedList.js';
import { flattenTree, normalizeMulti } from '../../utils/system.js';
import { request } from '../../api/request.js';

const localError = ref('');
const menuTree = ref([]);
const formVisible = ref(false);
const formTitle = ref('');
const editingId = ref(null);
const form = ref(emptyForm());

const flatMenus = computed(() => flattenTree(menuTree.value));

const {
  items,
  keyword,
  page,
  pageSize,
  total,
  totalPages,
  loading,
  error,
  load,
  search,
  reset,
  changePage,
  changePageSize
} = usePagedList((query) => request('/system/roles', { query }), 10);

onMounted(async () => {
  await Promise.all([load(), loadMenus()]);
});

function emptyForm() {
  return { code: '', name: '', remark: '', status: 1, menuIds: [] };
}

async function loadMenus() {
  menuTree.value = await request('/system/menus/tree');
}

function openForm(item) {
  editingId.value = item?.id || null;
  formTitle.value = item ? '编辑角色' : '新增角色';
  form.value = item
    ? { code: item.code, name: item.name, remark: item.remark || '', status: item.status, menuIds: [...(item.menuIds || [])] }
    : emptyForm();
  formVisible.value = true;
}

async function submitForm() {
  localError.value = '';
  try {
    const body = { ...form.value, menuIds: normalizeMulti(form.value.menuIds) };
    if (editingId.value) await request(`/system/roles/${editingId.value}`, { method: 'PUT', body });
    else await request('/system/roles', { method: 'POST', body });
    formVisible.value = false;
    await load();
  } catch (err) {
    localError.value = err.message;
  }
}

async function removeItem(item) {
  if (!window.confirm(`删除角色 ${item.name}？`)) return;
  try {
    await request(`/system/roles/${item.id}`, { method: 'DELETE' });
    await load();
  } catch (err) {
    localError.value = err.message;
  }
}
</script>
