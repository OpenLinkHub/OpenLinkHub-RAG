package com.openlinkhub.rag.admin.system.repository;

import com.openlinkhub.rag.admin.system.entity.SysMenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysMenuRepository extends JpaRepository<SysMenuEntity, Long> {

    List<SysMenuEntity> findAllByOrderBySortOrderAscIdAsc();

    boolean existsByParentId(Long parentId);
}
