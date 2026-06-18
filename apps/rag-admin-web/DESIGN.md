# RAG Admin Web — 列表与树形 UI 设计规范

本文件定义管理端列表页、树形页的复用模式，新增页面应遵循以保持风格一致。

## 1. 页面结构

```text
section.section-grid
  ├── div.section-header          # 标题 + 描述 + 主操作（新增）
  ├── ListToolbar（分页列表专用）   # 搜索 + 重置
  ├── p.error-line（可选）
  ├── div.table | div.tree-table    # 数据区
  ├── ListPagination（分页列表专用）
  └── div.modal-mask（表单弹层）
```

- **section-header**：左侧 `h2` + `p` 说明，右侧主按钮（如「新增用户」）。
- **错误信息**：统一使用 `p.error-line`，置于工具栏与表格之间。

## 2. 分页列表（用户、角色及后续平铺列表）

### 2.1 组件

| 组件 | 路径 | 用途 |
|------|------|------|
| `ListToolbar` | `components/list/ListToolbar.vue` | 关键词搜索 |
| `ListPagination` | `components/list/ListPagination.vue` | 翻页与统计 |
| `usePagedList` | `composables/usePagedList.js` | 分页/搜索状态与加载 |

### 2.2 API 约定

请求：`GET /api/admin/...?page=1&pageSize=10&keyword=xxx`

响应 `data` 结构：

```json
{
  "items": [],
  "total": 100,
  "page": 1,
  "pageSize": 10,
  "totalPages": 10
}
```

### 2.3 交互约定

- 默认 `pageSize = 10`，可选 10 / 20 / 50。
- 点击「搜索」或回车触发查询，`page` 重置为 1。
- 点击「重置」清空关键词并重新加载。
- 删除/新增/编辑成功后调用 `reload()` 保持当前页（若当前页无数据则回退一页）。

### 2.4 表格样式

- 外层：`div.table`
- 表头行：`div.table-row.table-head.cols-{name}`
- 数据行：`div.table-row.cols-{name}`
- 列宽在 `styles.css` 用 `.cols-user`、`.cols-role` 等定义。

## 3. 树形列表（菜单、部门）

### 3.1 组件

| 组件 | 路径 | 用途 |
|------|------|------|
| `TreeTable` | `components/tree/TreeTable.vue` | 树形表格展示 |
| `TreePicker` | `components/tree/TreePicker.vue` | 表单中选择上级节点 |

### 3.2 数据约定

- 菜单：`GET /system/menus/tree`，子节点字段 `children`
- 部门：`GET /system/depts/tree`，子节点字段 `children`

### 3.3 TreeTable 用法

```vue
<TreeTable :nodes="menuTree" children-key="children" row-key="id">
  <template #head>
    <span>名称</span><span>路径</span>...
  </template>
  <template #row="{ node }">
    <span>{{ node.name }}</span><span>{{ node.path || '-' }}</span>...
  </template>
  <template #actions="{ node }">
    <button @click="openForm(node)">编辑</button>
  </template>
</TreeTable>
```

- 树行缩进由组件内部 `depth` 控制，使用 `.tree-indent` 样式。
- 有子节点时显示展开/折叠按钮。

### 3.4 TreePicker 用法

```vue
<TreePicker
  v-model="form.parentId"
  :nodes="deptTree"
  children-key="children"
  :exclude-id="editingId"
  root-label="无（顶级）"
/>
```

- `exclude-id`：编辑时排除自身及其子孙，避免循环引用。
- 单选，值为节点 `id` 或 `null`（顶级）。

## 4. 表单弹层

- 遮罩：`div.modal-mask`，点击遮罩关闭。
- 卡片：`form.modal-card`，底部 `div.modal-actions`（取消 / 保存）。
- 字段：`label` 包裹 `input` / `select` / `TreePicker`。

## 5. 路由与页面

每个功能页使用独立路由，支持浏览器刷新后停留在当前页。

### 5.1 路由表

| 路径 | 名称 | 页面 |
|------|------|------|
| `/login` | `login` | 登录 |
| `/dashboard` | `dashboard` | 仪表盘 |
| `/knowledge` | `knowledge` | 知识库 |
| `/documents` | `documents` | 文档管理 |
| `/query` | `query` | 查询测试 |
| `/models` | `models` | 模型配置 |
| `/system/users` | `system-users` | 用户管理 |
| `/system/roles` | `system-roles` | 角色管理 |
| `/system/menus` | `system-menus` | 菜单管理 |
| `/system/depts` | `system-depts` | 部门管理 |
| `/system/status` | `system-status` | 系统状态 |

- 配置：`src/router/index.js`
- 布局：`src/layouts/AdminLayout.vue`（侧栏 + `<RouterView />`）
- 新增页面：在 `router/index.js` 注册子路由，并在 `AdminLayout` 导航中增加 `RouterLink`

### 5.2 认证

- 未登录访问受保护路由 → 跳转 `/login?redirect=原路径`
- 登录成功后 `router.replace(redirect)` 回到原页面
- API 请求统一使用 `src/api/request.js`

## 6. 请求封装

`request(path, { query: { page, pageSize, keyword } })` 自动拼接查询参数（见 `src/api/request.js`）。

下拉选项（不分页）使用独立接口，例如 `GET /system/roles/options`。

## 7. 新增列表页检查清单

- [ ] 使用 `section-grid` + `section-header`
- [ ] 分页列表：`ListToolbar` + `ListPagination` + `usePagedList`
- [ ] 树形列表：`TreeTable` + 表单内 `TreePicker`
- [ ] 表格列宽 class 已加入 `styles.css`
- [ ] 后端返回 `PageResult` 或树形 `children` 结构
