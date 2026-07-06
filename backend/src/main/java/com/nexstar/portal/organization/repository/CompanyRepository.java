package com.nexstar.portal.organization.repository;

import com.nexstar.portal.organization.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    Optional<Company> findFirstByDeletedFalse();
    Optional<Company> findByIdAndDeletedFalse(UUID id);
}
