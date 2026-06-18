<template>
  <section class="section-grid">
    <div class="section-header">
      <div>
        <h2>用户管理</h2>
        <p>管理系统登录账号、所属部门与角色分配。</p>
      </div>
      <button type="button" @click="openForm()">新增用户</button>
    </div>

    <ListToolbar
      v-model:keyword="keyword"
      label="搜索用户"
      placeholder="用户名 / 姓名 / 邮箱 / 手机"
      @search="search"
      @reset="reset"
    />

    <p v-if="localError || error" class="error-line">{{ localError || error }}</p>

    <div class="table">
      <div class="table-row table-head cols-user">
        <span>用户名</span><span>姓名</span><span>部门</span><span>角色</span><span>状态</span><span>操作</span>
      </div>
      <div v-if="loading" class="table-empty">加载中...</div>
      <div v-else-if="!items.length" class="table-empty">暂无数据</div>
      <div v-for="item in items" :key="item.id" class="table-row cols-user">
        <span>{{ item.username }}</span>
        <span>{{ item.displayName }}</span>
        <span>{{ item.deptName || '-' }}</span>
        <span>{{ (item.roleNames || []).join(', ') }}</span>
        <span>{{ item.status === 1 ? '启用' : '禁用' }}</span>
        <span class="row-actions">
          <button type="button" @click="openForm(item)">编辑</button>
          <button type="button" class="danger" @click="removeItem(item)">删除</button>
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
      <form class="modal-card" @submit.prevent="submitForm">
        <h3>{{ formTitle }}</h3>
        <label>用户名<input v-model="form.username" required /></label>
        <label>密码<input v-model="form.password" type="password" :required="!editingId" placeholder="留空则不修改" /></label>
        <label>姓名<input v-model="form.displayName" required /></label>
        <label>部门</label>
        <TreePicker v-model="form.deptId" :nodes="deptTree" children-key="children" root-label="无" />
        <label>邮箱<input v-model="form.email" /></label>
        <label>手机<input v-model="form.phone" /></label>
        <label>状态
          <select v-model.number="form.status"><option :value="1">启用</option><option :value="0">禁用</option></select>
        </label>
        <label>角色（多选）
          <select v-model="form.roleIds" multiple size="4">
            <option v-for="r in roleOptions" :key="r.id" :value="r.id">{{ r.name }}</option>
          </select>
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
import { onMounted, ref } from 'vue';
import ListToolbar from '../../components/list/ListToolbar.vue';
import ListPagination from '../../components/list/ListPagination.vue';
import TreePicker from '../../components/tree/TreePicker.vue';
import { usePagedList } from '../../composables/usePagedList.js';
import { normalizeMulti } from '../../utils/system.js';
import { request } from '../../api/request.js';

const localError = ref('');
const roleOptions = ref([]);
const deptTree = ref([]);
const formVisible = ref(false);
const formTitle = ref('');
const editingId = ref(null);
const form = ref(emptyForm());

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
} = usePagedList((query) => request('/system/users', { query }), 10);

onMounted(async () => {
  await Promise.all([load(), loadFormOptions()]);
});

function emptyForm() {
  return { username: '', password: '', displayName: '', deptId: null, email: '', phone: '', status: 1, roleIds: [] };
}

async function loadFormOptions() {
  const [roles, depts] = await Promise.all([
    request('/system/roles/options'),
    request('/system/depts/tree')
  ]);
  roleOptions.value = roles;
  deptTree.value = depts;
}

function openForm(item) {
  editingId.value = item?.id || null;
  formTitle.value = item ? '编辑用户' : '新增用户';
  form.value = item
    ? { username: item.username, password: '', displayName: item.displayName, deptId: item.deptId, email: item.email || '', phone: item.phone || '', status: item.status, roleIds: [...(item.roleIds || [])] }
    : emptyForm();
  formVisible.value = true;
}

async function submitForm() {
  localError.value = '';
  try {
    const body = { ...form.value, roleIds: normalizeMulti(form.value.roleIds) };
    if (editingId.value) await request(`/system/users/${editingId.value}`, { method: 'PUT', body });
    else await request('/system/users', { method: 'POST', body });
    formVisible.value = false;
    await load();
  } catch (err) {
    localError.value = err.message;
  }
}

async function removeItem(item) {
  if (!window.confirm(`删除用户 ${item.username}？`)) return;
  try {
    await request(`/system/users/${item.id}`, { method: 'DELETE' });
    await load();
  } catch (err) {
    localError.value = err.message;
  }
}
</script>
