package com.nexstar.portal.employee.repository;

import com.nexstar.portal.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmployeeIdAndDeletedFalse(String employeeId);

    Optional<Employee> findByUserIdAndDeletedFalse(UUID userId);

    Optional<Employee> findByIdAndDeletedFalse(UUID id);

    boolean existsByEmployeeIdAndDeletedFalse(String employeeId);

    @Query("""
            SELECT e FROM Employee e
            LEFT JOIN FETCH e.department
            LEFT JOIN FETCH e.designation
            LEFT JOIN FETCH e.team
            LEFT JOIN FETCH e.user
            WHERE e.id = :id AND e.deleted = false
            """)
    Optional<Employee> findByIdWithDetails(UUID id);

    @Query("""
            SELECT e FROM Employee e
            LEFT JOIN FETCH e.department
            LEFT JOIN FETCH e.designation
            LEFT JOIN FETCH e.team
            LEFT JOIN FETCH e.user
            WHERE e.deleted = false
            ORDER BY e.createdAt DESC
            """)
    Page<Employee> findAllActive(Pageable pageable);

    @Query("""
            SELECT e FROM Employee e
            WHERE e.department.id = :departmentId AND e.deleted = false
            """)
    Page<Employee> findByDepartmentId(UUID departmentId, Pageable pageable);

    @Query("""
            SELECT e FROM Employee e
            WHERE e.reportingManager.id = :managerId AND e.deleted = false
            """)
    Page<Employee> findByReportingManager(UUID managerId, Pageable pageable);
}
