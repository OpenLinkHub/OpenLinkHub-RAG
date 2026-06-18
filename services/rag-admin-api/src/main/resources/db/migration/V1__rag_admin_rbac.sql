CREATE SCHEMA IF NOT EXISTS rag_admin;

CREATE TABLE rag_admin.sys_dept (
    id          BIGSERIAL PRIMARY KEY,
    parent_id   BIGINT REFERENCES rag_admin.sys_dept (id),
    name        VARCHAR(100) NOT NULL,
    code        VARCHAR(64)  NOT NULL UNIQUE,
    sort_order  INT          NOT NULL DEFAULT 0,
    status      SMALLINT     NOT NULL DEFAULT 1,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE rag_admin.sys_menu (
    id          BIGSERIAL PRIMARY KEY,
    parent_id   BIGINT REFERENCES rag_admin.sys_menu (id),
    name        VARCHAR(100) NOT NULL,
    path        VARCHAR(200),
    permission  VARCHAR(100),
    menu_type   VARCHAR(1)   NOT NULL DEFAULT 'C',
    icon        VARCHAR(64),
    sort_order  INT          NOT NULL DEFAULT 0,
    visible     BOOLEAN      NOT NULL DEFAULT TRUE,
    status      SMALLINT     NOT NULL DEFAULT 1,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE rag_admin.sys_role (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    status      SMALLINT     NOT NULL DEFAULT 1,
    remark      VARCHAR(255),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE rag_admin.sys_user (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(64)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    dept_id       BIGINT REFERENCES rag_admin.sys_dept (id),
    email         VARCHAR(120),
    phone         VARCHAR(32),
    status        SMALLINT     NOT NULL DEFAULT 1,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE rag_admin.sys_user_role (
    user_id BIGINT NOT NULL REFERENCES rag_admin.sys_user (id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES rag_admin.sys_role (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE rag_admin.sys_role_menu (
    role_id BIGINT NOT NULL REFERENCES rag_admin.sys_role (id) ON DELETE CASCADE,
    menu_id BIGINT NOT NULL REFERENCES rag_admin.sys_menu (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, menu_id)
);

INSERT INTO rag_admin.sys_dept (id, parent_id, name, code, sort_order, status)
VALUES (1, NULL, 'OpenLinkHub', 'ROOT', 0, 1);

SELECT setval('rag_admin.sys_dept_id_seq', 1);

INSERT INTO rag_admin.sys_role (id, code, name, status, remark) VALUES
(1, 'SUPER_ADMIN', '超级管理员', 1, '拥有全部权限'),
(2, 'KB_OPERATOR', '知识库运营', 1, '知识库与文档日常操作');

SELECT setval('rag_admin.sys_role_id_seq', 2);

INSERT INTO rag_admin.sys_menu (id, parent_id, name, path, permission, menu_type, icon, sort_order, visible, status) VALUES
(1,  NULL, '系统设置', NULL, NULL, 'M', 'settings', 90, TRUE, 1),
(2,  1,    '用户管理', 'system-users', 'system:user:list', 'C', 'users', 1, TRUE, 1),
(3,  1,    '角色管理', 'system-roles', 'system:role:list', 'C', 'shield', 2, TRUE, 1),
(4,  1,    '菜单管理', 'system-menus', 'system:menu:list', 'C', 'menu', 3, TRUE, 1),
(5,  1,    '部门管理', 'system-depts', 'system:dept:list', 'C', 'building', 4, TRUE, 1),
(10, NULL, '仪表盘', 'dashboard', 'dashboard:view', 'C', 'dashboard', 1, TRUE, 1),
(11, NULL, '知识库', 'knowledge', 'kb:list', 'C', 'database', 10, TRUE, 1),
(12, NULL, '文档管理', 'documents', 'doc:list', 'C', 'file', 20, TRUE, 1),
(13, NULL, '查询测试', 'query', 'query:execute', 'C', 'search', 30, TRUE, 1),
(14, NULL, '模型配置', 'models', 'model:list', 'C', 'cpu', 40, TRUE, 1),
(15, NULL, '系统状态', 'system', 'system:status:view', 'C', 'activity', 50, TRUE, 1),
(21, 2,    '用户新增', NULL, 'system:user:create', 'F', NULL, 1, FALSE, 1),
(22, 2,    '用户编辑', NULL, 'system:user:update', 'F', NULL, 2, FALSE, 1),
(23, 2,    '用户删除', NULL, 'system:user:delete', 'F', NULL, 3, FALSE, 1),
(24, 3,    '角色新增', NULL, 'system:role:create', 'F', NULL, 1, FALSE, 1),
(25, 3,    '角色编辑', NULL, 'system:role:update', 'F', NULL, 2, FALSE, 1),
(26, 3,    '角色删除', NULL, 'system:role:delete', 'F', NULL, 3, FALSE, 1),
(27, 4,    '菜单新增', NULL, 'system:menu:create', 'F', NULL, 1, FALSE, 1),
(28, 4,    '菜单编辑', NULL, 'system:menu:update', 'F', NULL, 2, FALSE, 1),
(29, 4,    '菜单删除', NULL, 'system:menu:delete', 'F', NULL, 3, FALSE, 1),
(30, 5,    '部门新增', NULL, 'system:dept:create', 'F', NULL, 1, FALSE, 1),
(31, 5,    '部门编辑', NULL, 'system:dept:update', 'F', NULL, 2, FALSE, 1),
(32, 5,    '部门删除', NULL, 'system:dept:delete', 'F', NULL, 3, FALSE, 1),
(41, 11,   '创建知识库', NULL, 'kb:create', 'F', NULL, 1, FALSE, 1),
(42, 12,   '上传文档', NULL, 'doc:upload', 'F', NULL, 1, FALSE, 1),
(43, 12,   '删除文档', NULL, 'doc:delete', 'F', NULL, 2, FALSE, 1),
(44, 12,   '重处理文档', NULL, 'doc:reprocess', 'F', NULL, 3, FALSE, 1),
(45, 14,   '模型管理', NULL, 'model:manage', 'F', NULL, 1, FALSE, 1);

SELECT setval('rag_admin.sys_menu_id_seq', 45);

INSERT INTO rag_admin.sys_role_menu (role_id, menu_id)
SELECT 1, id FROM rag_admin.sys_menu;

INSERT INTO rag_admin.sys_role_menu (role_id, menu_id)
SELECT 2, id FROM rag_admin.sys_menu
WHERE permission IS NULL
   OR permission IN (
       'dashboard:view', 'kb:list', 'kb:create', 'doc:list', 'doc:upload',
       'doc:delete', 'doc:reprocess', 'query:execute', 'model:list', 'model:manage',
       'system:status:view'
   )
   OR id IN (10, 11, 12, 13, 14, 15, 41, 42, 43, 44, 45);
