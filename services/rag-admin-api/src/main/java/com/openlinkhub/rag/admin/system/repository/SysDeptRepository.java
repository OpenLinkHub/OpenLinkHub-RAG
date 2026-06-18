package com.openlinkhub.rag.admin.system.repository;

import com.openlinkhub.rag.admin.system.entity.SysDeptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SysDeptRepository extends JpaRepository<SysDeptEntity, Long> {

    Optional<SysDeptEntity> findByCode(String code);

    List<SysDeptEntity> findAllByOrderBySortOrderAscIdAsc();

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByParentId(Long parentId);
}
