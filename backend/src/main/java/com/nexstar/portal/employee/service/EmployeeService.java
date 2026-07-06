package com.nexstar.portal.employee.service;

import com.nexstar.portal.authentication.entity.Role;
import com.nexstar.portal.authentication.entity.User;
import com.nexstar.portal.authentication.repository.RoleRepository;
import com.nexstar.portal.authentication.repository.UserRepository;
import com.nexstar.portal.common.exception.BusinessException;
import com.nexstar.portal.common.exception.ResourceNotFoundException;
import com.nexstar.portal.common.response.PageResponse;
import com.nexstar.portal.employee.dto.*;
import com.nexstar.portal.employee.entity.*;
import com.nexstar.portal.employee.repository.*;
import com.nexstar.portal.organization.entity.*;
import com.nexstar.portal.organization.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final EmployeeDocumentRepository documentRepository;
    private final EmployeeSkillRepository skillRepository;
    private final EmploymentHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final DesignationRepository designationRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new BusinessException("Email already registered");
        }

        Role employeeRole = roleRepository.findByNameAndDeletedFalse("EMPLOYEE")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "EMPLOYEE"));

        // Create user account with temp password
        String tempPassword = UUID.randomUUID().toString().substring(0, 12) + "Aa1!";
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(tempPassword))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .emailVerified(true)
                .active(true)
                .build();
        user.getRoles().add(employeeRole);
        userRepository.save(user);

        // Generate employee ID
        String empId = StringUtils.hasText(request.getEmployeeId())
                ? request.getEmployeeId()
                : generateEmployeeId();

        if (employeeRepository.existsByEmployeeIdAndDeletedFalse(empId)) {
            throw new BusinessException("Employee ID already exists: " + empId);
        }

        Employee employee = Employee.builder()
                .user(user)
                .employeeId(empId)
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .nationality(request.getNationality())
                .maritalStatus(request.getMaritalStatus())
                .bloodGroup(request.getBloodGroup())
                .personalEmail(request.getPersonalEmail())
                .phoneNumber(request.getPhoneNumber())
                .alternatePhone(request.getAlternatePhone())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .dateOfJoining(request.getDateOfJoining())
                .employmentType(request.getEmploymentType())
                .workLocation(request.getWorkLocation())
                .status(Employee.EmployeeStatus.ACTIVE)
                .build();

        if (request.getDepartmentId() != null) {
            employee.setDepartment(departmentRepository.findByIdAndDeletedFalse(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId())));
        }
        if (request.getTeamId() != null) {
            employee.setTeam(teamRepository.findByIdAndDeletedFalse(request.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team", "id", request.getTeamId())));
        }
        if (request.getDesignationId() != null) {
            employee.setDesignation(designationRepository.findByIdAndDeletedFalse(request.getDesignationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", request.getDesignationId())));
        }
        if (request.getBranchId() != null) {
            employee.setBranch(branchRepository.findByIdAndDeletedFalse(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId())));
        }
        if (request.getReportingManagerId() != null) {
            employee.setReportingManager(employeeRepository.findByIdAndDeletedFalse(request.getReportingManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", request.getReportingManagerId())));
        }

        return toResponse(employeeRepository.save(employee));
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployee(UUID id) {
        Employee employee = employeeRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return toResponse(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByUserId(UUID userId) {
        Employee employee = employeeRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found for user"));
        return toResponse(employee);
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> searchEmployees(EmployeeFilterRequest filter) {
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(filter.getSortDir())
                        ? Sort.Direction.DESC : Sort.Direction.ASC,
                filter.getSortBy()
        );
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<Employee> spec = buildSpec(filter);
        Page<Employee> page = employeeRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Transactional
    public EmployeeResponse updateEmployee(UUID id, CreateEmployeeRequest request) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        // Update user
        User user = employee.getUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        userRepository.save(user);

        // Update personal info
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setGender(request.getGender());
        employee.setNationality(request.getNationality());
        employee.setMaritalStatus(request.getMaritalStatus());
        employee.setBloodGroup(request.getBloodGroup());
        employee.setPersonalEmail(request.getPersonalEmail());
        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setAlternatePhone(request.getAlternatePhone());
        employee.setAddress(request.getAddress());
        employee.setCity(request.getCity());
        employee.setState(request.getState());
        employee.setCountry(request.getCountry());
        employee.setPostalCode(request.getPostalCode());
        employee.setWorkLocation(request.getWorkLocation());

        if (request.getDepartmentId() != null) {
            employee.setDepartment(departmentRepository.findByIdAndDeletedFalse(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId())));
        }
        if (request.getTeamId() != null) {
            employee.setTeam(teamRepository.findByIdAndDeletedFalse(request.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team", "id", request.getTeamId())));
        }
        if (request.getDesignationId() != null) {
            employee.setDesignation(designationRepository.findByIdAndDeletedFalse(request.getDesignationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", request.getDesignationId())));
        }
        if (request.getReportingManagerId() != null) {
            employee.setReportingManager(employeeRepository.findByIdAndDeletedFalse(request.getReportingManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", request.getReportingManagerId())));
        }

        return toResponse(employeeRepository.save(employee));
    }

    @Transactional
    public void terminateEmployee(UUID id, String reason) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        employee.setStatus(Employee.EmployeeStatus.TERMINATED);
        employee.getUser().setActive(false);
        employeeRepository.save(employee);
    }

    // ── Emergency Contacts ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<EmergencyContact> getEmergencyContacts(UUID employeeId) {
        assertEmployeeExists(employeeId);
        return emergencyContactRepository.findByEmployeeIdAndDeletedFalse(employeeId);
    }

    @Transactional
    public EmergencyContact addEmergencyContact(UUID employeeId, EmergencyContact contact) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        contact.setEmployee(employee);
        return emergencyContactRepository.save(contact);
    }

    @Transactional
    public void deleteEmergencyContact(UUID id) {
        EmergencyContact contact = emergencyContactRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency Contact", "id", id));
        contact.setDeleted(true);
        emergencyContactRepository.save(contact);
    }

    // ── Skills ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<EmployeeSkill> getSkills(UUID employeeId) {
        assertEmployeeExists(employeeId);
        return skillRepository.findByEmployeeIdAndDeletedFalse(employeeId);
    }

    @Transactional
    public EmployeeSkill addSkill(UUID employeeId, EmployeeSkill skill) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        skill.setEmployee(employee);
        return skillRepository.save(skill);
    }

    @Transactional
    public void deleteSkill(UUID id) {
        EmployeeSkill skill = skillRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", id));
        skill.setDeleted(true);
        skillRepository.save(skill);
    }

    // ── Employment History ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<EmploymentHistory> getEmploymentHistory(UUID employeeId) {
        assertEmployeeExists(employeeId);
        return historyRepository.findByEmployeeIdAndDeletedFalseOrderByStartDateDesc(employeeId);
    }

    @Transactional
    public EmploymentHistory addEmploymentHistory(UUID employeeId, EmploymentHistory history) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        history.setEmployee(employee);
        return historyRepository.save(history);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void assertEmployeeExists(UUID id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee", "id", id);
        }
    }

    private String generateEmployeeId() {
        long count = employeeRepository.count() + 1;
        return "EMP" + String.format("%05d", count);
    }

    private Specification<Employee> buildSpec(EmployeeFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (StringUtils.hasText(filter.getSearch())) {
                String pattern = "%" + filter.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("user").get("firstName")), pattern),
                        cb.like(cb.lower(root.get("user").get("lastName")), pattern),
                        cb.like(cb.lower(root.get("user").get("email")), pattern),
                        cb.like(cb.lower(root.get("employeeId")), pattern)
                ));
            }
            if (filter.getDepartmentId() != null) {
                predicates.add(cb.equal(root.get("department").get("id"), filter.getDepartmentId()));
            }
            if (filter.getTeamId() != null) {
                predicates.add(cb.equal(root.get("team").get("id"), filter.getTeamId()));
            }
            if (filter.getDesignationId() != null) {
                predicates.add(cb.equal(root.get("designation").get("id"), filter.getDesignationId()));
            }
            if (filter.getBranchId() != null) {
                predicates.add(cb.equal(root.get("branch").get("id"), filter.getBranchId()));
            }
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }
            if (filter.getEmploymentType() != null) {
                predicates.add(cb.equal(root.get("employmentType"), filter.getEmploymentType()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public EmployeeResponse toResponse(Employee e) {
        return EmployeeResponse.builder()
                .id(e.getId())
                .employeeId(e.getEmployeeId())
                .userId(e.getUser() != null ? e.getUser().getId() : null)
                .firstName(e.getUser() != null ? e.getUser().getFirstName() : null)
                .lastName(e.getUser() != null ? e.getUser().getLastName() : null)
                .email(e.getUser() != null ? e.getUser().getEmail() : null)
                .profilePictureUrl(e.getProfilePictureUrl())
                .dateOfBirth(e.getDateOfBirth())
                .gender(e.getGender())
                .nationality(e.getNationality())
                .maritalStatus(e.getMaritalStatus())
                .bloodGroup(e.getBloodGroup())
                .personalEmail(e.getPersonalEmail())
                .phoneNumber(e.getPhoneNumber())
                .address(e.getAddress())
                .city(e.getCity())
                .state(e.getState())
                .country(e.getCountry())
                .departmentId(e.getDepartment() != null ? e.getDepartment().getId() : null)
                .departmentName(e.getDepartment() != null ? e.getDepartment().getName() : null)
                .teamId(e.getTeam() != null ? e.getTeam().getId() : null)
                .teamName(e.getTeam() != null ? e.getTeam().getName() : null)
                .designationId(e.getDesignation() != null ? e.getDesignation().getId() : null)
                .designationName(e.getDesignation() != null ? e.getDesignation().getName() : null)
                .branchId(e.getBranch() != null ? e.getBranch().getId() : null)
                .branchName(e.getBranch() != null ? e.getBranch().getName() : null)
                .reportingManagerId(e.getReportingManager() != null ? e.getReportingManager().getId() : null)
                .reportingManagerName(e.getReportingManager() != null
                        ? e.getReportingManager().getUser().getFirstName() + " " + e.getReportingManager().getUser().getLastName()
                        : null)
                .dateOfJoining(e.getDateOfJoining())
                .status(e.getStatus())
                .employmentType(e.getEmploymentType())
                .workLocation(e.getWorkLocation())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
