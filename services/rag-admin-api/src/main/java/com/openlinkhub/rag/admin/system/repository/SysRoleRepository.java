package com.openlinkhub.rag.admin.system.repository;

import com.openlinkhub.rag.admin.system.entity.SysRoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface SysRoleRepository extends JpaRepository<SysRoleEntity, Long>, JpaSpecificationExecutor<SysRoleEntity> {

    Optional<SysRoleEntity> findByCode(String code);

    @EntityGraph(attributePaths = "menus")
    Optional<SysRoleEntity> findWithMenusById(Long id);

    List<SysRoleEntity> findAllByOrderByIdAsc();

    boolean existsByCodeAndIdNot(String code, Long id);

    @EntityGraph(attributePaths = "menus")
    Page<SysRoleEntity> findAll(Specification<SysRoleEntity> spec, Pageable pageable);
}
