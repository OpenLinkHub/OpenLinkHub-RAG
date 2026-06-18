<template>
  <main class="admin-shell">
    <aside class="sidebar">
      <div class="brand">
        <span>RAG</span>
        <div>
          <strong>OpenLinkHub</strong>
          <p>Knowledge Console</p>
        </div>
      </div>

      <RouterLink
        v-for="item in mainNavItems"
        :key="item.name"
        :to="{ name: item.name }"
        class="nav-item"
        active-class="active"
      >
        {{ item.label }}
      </RouterLink>

      <div class="nav-group">
        <p class="nav-group-title">系统设置</p>
        <RouterLink
          v-for="item in systemNavItems"
          :key="item.name"
          :to="{ name: item.name }"
          class="nav-item nav-subitem"
          active-class="active"
        >
          {{ item.label }}
        </RouterLink>
      </div>

      <RouterLink :to="{ name: 'system-status' }" class="nav-item" active-class="active">
        系统状态
      </RouterLink>
    </aside>

    <section class="workspace">
      <header class="topbar">
        <div>
          <p class="eyebrow">LightRAG Management Layer</p>
          <h1>{{ pageTitle }}</h1>
        </div>
        <div class="user-block">
          <span>{{ user?.displayName }}</span>
          <button type="button" @click="handleLogout">退出</button>
        </div>
      </header>

      <section class="content">
        <RouterView />
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed } from 'vue';
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router';
import { useAuth } from '../composables/useAuth.js';

const route = useRoute();
const router = useRouter();
const { user, logout } = useAuth();

const mainNavItems = [
  { name: 'dashboard', label: '仪表盘' },
  { name: 'knowledge', label: '知识库' },
  { name: 'documents', label: '文档管理' },
  { name: 'query', label: '查询测试' },
  { name: 'models', label: '模型配置' }
];

const systemNavItems = [
  { name: 'system-users', label: '用户管理' },
  { name: 'system-roles', label: '角色管理' },
  { name: 'system-menus', label: '菜单管理' },
  { name: 'system-depts', label: '部门管理' }
];

const pageTitle = computed(() => route.meta.title || 'RAG Admin');

function handleLogout() {
  logout();
  router.push({ name: 'login' });
}
</script>
