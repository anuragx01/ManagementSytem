package com.nexstar.portal.payroll.service;

import com.nexstar.portal.common.exception.BusinessException;
import com.nexstar.portal.common.exception.ResourceNotFoundException;
import com.nexstar.portal.employee.entity.Employee;
import com.nexstar.portal.employee.repository.EmployeeRepository;
import com.nexstar.portal.payroll.dto.PayrollRecordDto;
import com.nexstar.portal.payroll.entity.PayrollRecord;
import com.nexstar.portal.payroll.repository.PayrollRecordRepository;
import com.nexstar.portal.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRecordRepository payrollRecordRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<PayrollRecordDto> listPayroll(UUID employeeId, String payrollPeriod, UserPrincipal currentUser) {
        if (hasRole(currentUser, "EMPLOYEE") && !hasAnyRole(currentUser, "SUPER_ADMIN", "HR")) {
            Employee employee = employeeByUser(currentUser.getId());
            return recordsFor(employee.getId(), payrollPeriod).stream().map(this::toDto).toList();
        }
        if (employeeId != null) return recordsFor(employeeId, payrollPeriod).stream().map(this::toDto).toList();
        if (payrollPeriod != null && !payrollPeriod.isBlank()) {
            return payrollRecordRepository.findByPayrollPeriodAndDeletedFalseOrderByCreatedAtDesc(payrollPeriod)
                    .stream().map(this::toDto).toList();
        }
        return payrollRecordRepository.findByDeletedFalseOrderByCreatedAtDesc().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PayrollRecordDto> myPayroll(UserPrincipal currentUser) {
        Employee employee = employeeByUser(currentUser.getId());
        return payrollRecordRepository.findByEmployee_IdAndDeletedFalseOrderByCreatedAtDesc(employee.getId())
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public PayrollRecordDto createPayroll(PayrollRecordDto dto) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", dto.getEmployeeId()));
        PayrollRecord record = PayrollRecord.builder()
                .employee(employee)
                .payrollPeriod(dto.getPayrollPeriod())
                .basicSalary(value(dto.getBasicSalary()))
                .allowances(value(dto.getAllowances()))
                .deductions(value(dto.getDeductions()))
                .paymentStatus(dto.getPaymentStatus() != null ? dto.getPaymentStatus() : PayrollRecord.PaymentStatus.PENDING)
                .processedDate(dto.getProcessedDate())
                .notes(dto.getNotes())
                .build();
        record.setNetSalary(net(record));
        return toDto(payrollRecordRepository.save(record));
    }

    @Transactional
    public PayrollRecordDto updatePayroll(UUID id, PayrollRecordDto dto) {
        PayrollRecord record = payrollRecordRepository.findById(id)
                .filter(item -> !item.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record", "id", id));
        if (dto.getEmployeeId() != null) {
            record.setEmployee(employeeRepository.findByIdAndDeletedFalse(dto.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", dto.getEmployeeId())));
        }
        if (dto.getPayrollPeriod() != null) record.setPayrollPeriod(dto.getPayrollPeriod());
        if (dto.getBasicSalary() != null) record.setBasicSalary(dto.getBasicSalary());
        if (dto.getAllowances() != null) record.setAllowances(dto.getAllowances());
        if (dto.getDeductions() != null) record.setDeductions(dto.getDeductions());
        if (dto.getPaymentStatus() != null) record.setPaymentStatus(dto.getPaymentStatus());
        if (dto.getProcessedDate() != null) record.setProcessedDate(dto.getProcessedDate());
        if (dto.getNotes() != null) record.setNotes(dto.getNotes());
        record.setNetSalary(net(record));
        return toDto(payrollRecordRepository.save(record));
    }

    @Transactional
    public void deletePayroll(UUID id) {
        PayrollRecord record = payrollRecordRepository.findById(id)
                .filter(item -> !item.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record", "id", id));
        record.setDeleted(true);
        payrollRecordRepository.save(record);
    }

    private List<PayrollRecord> recordsFor(UUID employeeId, String payrollPeriod) {
        if (payrollPeriod != null && !payrollPeriod.isBlank()) {
            return payrollRecordRepository.findByEmployee_IdAndPayrollPeriodAndDeletedFalseOrderByCreatedAtDesc(employeeId, payrollPeriod);
        }
        return payrollRecordRepository.findByEmployee_IdAndDeletedFalseOrderByCreatedAtDesc(employeeId);
    }

    private Employee employeeByUser(UUID userId) {
        return employeeRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException("Employee profile not found.", HttpStatus.NOT_FOUND));
    }

    private boolean hasRole(UserPrincipal user, String role) {
        return user.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(("ROLE_" + role)::equals);
    }

    private boolean hasAnyRole(UserPrincipal user, String... roles) {
        for (String role : roles) {
            if (hasRole(user, role)) return true;
        }
        return false;
    }

    private BigDecimal value(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal net(PayrollRecord record) {
        return value(record.getBasicSalary()).add(value(record.getAllowances())).subtract(value(record.getDeductions()));
    }

    private PayrollRecordDto toDto(PayrollRecord record) {
        Employee employee = record.getEmployee();
        String employeeName = employee != null && employee.getUser() != null
                ? employee.getUser().getFirstName() + " " + employee.getUser().getLastName()
                : null;
        return PayrollRecordDto.builder()
                .id(record.getId())
                .employeeId(employee != null ? employee.getId() : null)
                .employeeCode(employee != null ? employee.getEmployeeId() : null)
                .employeeName(employeeName)
                .departmentName(employee != null && employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .payrollPeriod(record.getPayrollPeriod())
                .basicSalary(record.getBasicSalary())
                .allowances(record.getAllowances())
                .deductions(record.getDeductions())
                .netSalary(record.getNetSalary())
                .paymentStatus(record.getPaymentStatus())
                .processedDate(record.getProcessedDate() != null ? record.getProcessedDate() : LocalDate.now())
                .notes(record.getNotes())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
