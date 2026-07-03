package com.nexstar.portal.payroll.repository;

import com.nexstar.portal.payroll.entity.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PayrollRecordRepository extends JpaRepository<PayrollRecord, UUID> {
    List<PayrollRecord> findByDeletedFalseOrderByCreatedAtDesc();
    List<PayrollRecord> findByEmployee_IdAndDeletedFalseOrderByCreatedAtDesc(UUID employeeId);
    List<PayrollRecord> findByEmployee_IdAndPayrollPeriodAndDeletedFalseOrderByCreatedAtDesc(UUID employeeId, String payrollPeriod);
    List<PayrollRecord> findByPayrollPeriodAndDeletedFalseOrderByCreatedAtDesc(String payrollPeriod);
}
