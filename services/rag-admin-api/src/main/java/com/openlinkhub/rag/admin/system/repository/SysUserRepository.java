package com.openlinkhub.rag.admin.system.repository;

import com.openlinkhub.rag.admin.system.entity.SysUserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUserEntity, Long>, JpaSpecificationExecutor<SysUserEntity> {

    @EntityGraph(attributePaths = {"roles", "roles.menus", "dept"})
    Optional<SysUserEntity> findWithRolesByUsername(String username);

    @EntityGraph(attributePaths = {"roles", "roles.menus", "dept"})
    Optional<SysUserEntity> findWithRolesById(Long id);

    List<SysUserEntity> findAllByOrderByIdAsc();

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = {"roles", "dept"})
    Page<SysUserEntity> findAll(Specification<SysUserEntity> spec, Pageable pageable);
}
