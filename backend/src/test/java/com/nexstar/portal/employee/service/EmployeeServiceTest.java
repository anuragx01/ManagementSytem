package com.nexstar.portal.employee.service;

import com.nexstar.portal.authentication.entity.Role;
import com.nexstar.portal.authentication.entity.User;
import com.nexstar.portal.authentication.repository.RoleRepository;
import com.nexstar.portal.authentication.repository.UserRepository;
import com.nexstar.portal.common.exception.BusinessException;
import com.nexstar.portal.common.exception.ResourceNotFoundException;
import com.nexstar.portal.employee.dto.CreateEmployeeRequest;
import com.nexstar.portal.employee.dto.EmployeeResponse;
import com.nexstar.portal.employee.entity.Employee;
import com.nexstar.portal.employee.repository.EmergencyContactRepository;
import com.nexstar.portal.employee.repository.EmployeeDocumentRepository;
import com.nexstar.portal.employee.repository.EmployeeRepository;
import com.nexstar.portal.employee.repository.EmployeeSkillRepository;
import com.nexstar.portal.employee.repository.EmploymentHistoryRepository;
import com.nexstar.portal.organization.entity.Department;
import com.nexstar.portal.organization.repository.BranchRepository;
import com.nexstar.portal.organization.repository.DesignationRepository;
import com.nexstar.portal.organization.repository.DepartmentRepository;
import com.nexstar.portal.organization.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmergencyContactRepository emergencyContactRepository;

    @Mock
    private EmployeeDocumentRepository documentRepository;

    @Mock
    private EmployeeSkillRepository skillRepository;

    @Mock
    private EmploymentHistoryRepository historyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private DesignationRepository designationRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeService employeeService;

    private Role employeeRole;
    private Department department;

    @BeforeEach
    void setUp() {
        employeeRole = Role.builder()
                .name("EMPLOYEE")
                .build();

        department = Department.builder()
                .name("Engineering")
                .build();
    }

    @Test
    void createEmployee_generatesEmployeeIdCorrectly() {
        // Arrange
        UUID deptId = UUID.fromString("00000000-0000-0000-0000-000000000010");

        when(userRepository.existsByEmailAndDeletedFalse("john.doe@company.com")).thenReturn(false);
        when(roleRepository.findByNameAndDeletedFalse("EMPLOYEE")).thenReturn(Optional.of(employeeRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(employeeRepository.count()).thenReturn(0L);
        when(employeeRepository.existsByEmployeeIdAndDeletedFalse("EMP00001")).thenReturn(false);
        when(departmentRepository.findByIdAndDeletedFalse(deptId)).thenReturn(Optional.of(department));

        ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
        when(employeeRepository.save(employeeCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@company.com");
        request.setDateOfJoining(LocalDate.of(2026, 1, 1));
        request.setDepartmentId(deptId);

        // Act
        EmployeeResponse response = employeeService.createEmployee(request);

        // Assert
        Employee saved = employeeCaptor.getValue();
        assertThat(saved.getEmployeeId()).isEqualTo("EMP00001");
    }

    @Test
    void createEmployee_withDuplicateEmail_throwsException() {
        // Arrange
        when(userRepository.existsByEmailAndDeletedFalse("existing@company.com")).thenReturn(true);

        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("existing@company.com");
        request.setDateOfJoining(LocalDate.of(2026, 1, 1));

        // Act & Assert
        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    void getEmployee_whenNotFound_throwsNotFoundException() {
        // Arrange
        UUID unknownId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        when(employeeRepository.findByIdWithDetails(unknownId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> employeeService.getEmployee(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
