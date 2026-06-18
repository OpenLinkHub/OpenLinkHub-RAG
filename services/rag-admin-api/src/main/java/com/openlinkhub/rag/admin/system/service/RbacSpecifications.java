package com.openlinkhub.rag.admin.system.service;

import com.openlinkhub.rag.admin.common.StringUtils;
import com.openlinkhub.rag.admin.system.entity.SysRoleEntity;
import com.openlinkhub.rag.admin.system.entity.SysUserEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class RbacSpecifications {

    private RbacSpecifications() {
    }

    public static Specification<SysUserEntity> userKeyword(String keyword) {
        return (root, query, builder) -> {
            if (StringUtils.isBlank(keyword)) {
                return builder.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.like(builder.lower(root.get("username")), pattern));
            predicates.add(builder.like(builder.lower(root.get("displayName")), pattern));
            predicates.add(builder.like(builder.lower(root.get("email")), pattern));
            predicates.add(builder.like(builder.lower(root.get("phone")), pattern));
            return builder.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<SysRoleEntity> roleKeyword(String keyword) {
        return (root, query, builder) -> {
            if (StringUtils.isBlank(keyword)) {
                return builder.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return builder.or(
                    builder.like(builder.lower(root.get("code")), pattern),
                    builder.like(builder.lower(root.get("name")), pattern),
                    builder.like(builder.lower(root.get("remark")), pattern)
            );
        };
    }
}
