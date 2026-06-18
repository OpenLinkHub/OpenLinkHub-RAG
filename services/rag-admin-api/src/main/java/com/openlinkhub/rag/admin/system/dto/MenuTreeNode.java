package com.openlinkhub.rag.admin.system.dto;

import java.util.ArrayList;
import java.util.List;

public final class MenuTreeNode {

    private final Long id;
    private final Long parentId;
    private final String name;
    private final String path;
    private final String permission;
    private final String menuType;
    private final String icon;
    private final int sortOrder;
    private final boolean visible;
    private final short status;
    private final List<MenuTreeNode> children = new ArrayList<>();

    public MenuTreeNode(
            Long id,
            Long parentId,
            String name,
            String path,
            String permission,
            String menuType,
            String icon,
            int sortOrder,
            boolean visible,
            short status
    ) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.path = path;
        this.permission = permission;
        this.menuType = menuType;
        this.icon = icon;
        this.sortOrder = sortOrder;
        this.visible = visible;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getPermission() {
        return permission;
    }

    public String getMenuType() {
        return menuType;
    }

    public String getIcon() {
        return icon;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public boolean isVisible() {
        return visible;
    }

    public short getStatus() {
        return status;
    }

    public List<MenuTreeNode> getChildren() {
        return children;
    }
}
