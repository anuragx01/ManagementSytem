package com.nexstar.portal.organization.repository;

import com.nexstar.portal.organization.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, UUID> {
    List<Holiday> findByCompanyIdAndDeletedFalse(UUID companyId);
    List<Holiday> findByCompanyIdAndDateBetweenAndDeletedFalse(UUID companyId, LocalDate from, LocalDate to);
    Optional<Holiday> findByIdAndDeletedFalse(UUID id);
}
