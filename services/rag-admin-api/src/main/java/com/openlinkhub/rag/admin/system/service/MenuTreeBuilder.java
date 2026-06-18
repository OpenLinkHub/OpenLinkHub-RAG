package com.openlinkhub.rag.admin.system.service;

import com.openlinkhub.rag.admin.system.dto.MenuTreeNode;
import com.openlinkhub.rag.admin.system.entity.SysMenuEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class MenuTreeBuilder {

    private MenuTreeBuilder() {
    }

    public static List<MenuTreeNode> build(List<SysMenuEntity> menus, boolean includeInvisible) {
        Map<Long, MenuTreeNode> nodes = new HashMap<>();
        for (SysMenuEntity menu : menus) {
            if (!includeInvisible && !menu.isVisible()) {
                continue;
            }
            nodes.put(menu.getId(), toNode(menu));
        }
        Set<Long> linkedParents = menus.stream()
                .map(SysMenuEntity::getParentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<MenuTreeNode> roots = new ArrayList<>();
        for (SysMenuEntity menu : menus) {
            if (!includeInvisible && !menu.isVisible()) {
                continue;
            }
            MenuTreeNode node = nodes.get(menu.getId());
            if (menu.getParentId() == null || !nodes.containsKey(menu.getParentId())) {
                roots.add(node);
                continue;
            }
            nodes.get(menu.getParentId()).getChildren().add(node);
        }
        sortTree(roots);
        return roots.stream()
                .sorted(Comparator.comparingInt(MenuTreeNode::getSortOrder).thenComparing(MenuTreeNode::getId))
                .collect(Collectors.toList());
    }

    private static void sortTree(List<MenuTreeNode> nodes) {
        nodes.sort(Comparator.comparingInt(MenuTreeNode::getSortOrder).thenComparing(MenuTreeNode::getId));
        for (MenuTreeNode node : nodes) {
            sortTree(node.getChildren());
        }
    }

    private static MenuTreeNode toNode(SysMenuEntity menu) {
        return new MenuTreeNode(
                menu.getId(),
                menu.getParentId(),
                menu.getName(),
                menu.getPath(),
                menu.getPermission(),
                menu.getMenuType(),
                menu.getIcon(),
                menu.getSortOrder(),
                menu.isVisible(),
                menu.getStatus()
        );
    }
}
