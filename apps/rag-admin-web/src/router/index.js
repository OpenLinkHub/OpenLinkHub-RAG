import { createRouter, createWebHistory } from 'vue-router';
import { useAuth } from '../composables/useAuth.js';

const AdminLayout = () => import('../layouts/AdminLayout.vue');
const LoginView = () => import('../views/LoginView.vue');
const DashboardView = () => import('../views/DashboardView.vue');
const KnowledgeView = () => import('../views/KnowledgeView.vue');
const DocumentsView = () => import('../views/DocumentsView.vue');
const QueryView = () => import('../views/QueryView.vue');
const ModelsView = () => import('../views/ModelsView.vue');
const SystemStatusView = () => import('../views/SystemStatusView.vue');
const UserManagement = () => import('../views/system/UserManagement.vue');
const RoleManagement = () => import('../views/system/RoleManagement.vue');
const MenuManagement = () => import('../views/system/MenuManagement.vue');
const DeptManagement = () => import('../views/system/DeptManagement.vue');

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { guest: true }
  },
  {
    path: '/',
    component: AdminLayout,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: { name: 'dashboard' } },
      { path: 'dashboard', name: 'dashboard', component: DashboardView, meta: { title: '仪表盘' } },
      { path: 'knowledge', name: 'knowledge', component: KnowledgeView, meta: { title: '知识库' } },
      { path: 'documents', name: 'documents', component: DocumentsView, meta: { title: '文档管理' } },
      { path: 'query', name: 'query', component: QueryView, meta: { title: '查询测试' } },
      { path: 'models', name: 'models', component: ModelsView, meta: { title: '模型配置' } },
      { path: 'system/users', name: 'system-users', component: UserManagement, meta: { title: '用户管理' } },
      { path: 'system/roles', name: 'system-roles', component: RoleManagement, meta: { title: '角色管理' } },
      { path: 'system/menus', name: 'system-menus', component: MenuManagement, meta: { title: '菜单管理' } },
      { path: 'system/depts', name: 'system-depts', component: DeptManagement, meta: { title: '部门管理' } },
      { path: 'system/status', name: 'system-status', component: SystemStatusView, meta: { title: '系统状态' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: { name: 'dashboard' } }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach(async (to) => {
  const { isAuthenticated, user, fetchUser, logout } = useAuth();

  if (to.meta.requiresAuth && !isAuthenticated()) {
    return { name: 'login', query: { redirect: to.fullPath } };
  }

  if (to.meta.guest && isAuthenticated()) {
    return { name: 'dashboard' };
  }

  if (to.meta.requiresAuth && isAuthenticated() && !user.value) {
    try {
      await fetchUser();
    } catch {
      logout();
      return { name: 'login', query: { redirect: to.fullPath } };
    }
  }

  return true;
});

export default router;
