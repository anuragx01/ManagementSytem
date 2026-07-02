package com.nexstar.portal.authentication.repository;

import com.nexstar.portal.authentication.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByNameAndDeletedFalse(String name);

    boolean existsByNameAndDeletedFalse(String name);
}
