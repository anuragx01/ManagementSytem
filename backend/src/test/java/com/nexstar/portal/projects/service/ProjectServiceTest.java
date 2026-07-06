package com.nexstar.portal.projects.service;

import com.nexstar.portal.authentication.entity.User;
import com.nexstar.portal.common.exception.BusinessException;
import com.nexstar.portal.employee.entity.Employee;
import com.nexstar.portal.employee.repository.EmployeeRepository;
import com.nexstar.portal.organization.entity.Company;
import com.nexstar.portal.organization.repository.CompanyRepository;
import com.nexstar.portal.projects.dto.CreateProjectRequest;
import com.nexstar.portal.projects.dto.ProjectResponse;
import com.nexstar.portal.projects.dto.SprintDto;
import com.nexstar.portal.projects.entity.Milestone;
import com.nexstar.portal.projects.entity.Project;
import com.nexstar.portal.projects.entity.ProjectMember;
import com.nexstar.portal.projects.entity.Sprint;
import com.nexstar.portal.projects.repository.MilestoneRepository;
import com.nexstar.portal.projects.repository.ProjectLabelRepository;
import com.nexstar.portal.projects.repository.ProjectMemberRepository;
import com.nexstar.portal.projects.repository.ProjectRepository;
import com.nexstar.portal.projects.repository.SprintRepository;
import com.nexstar.portal.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private MilestoneRepository milestoneRepository;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private ProjectLabelRepository projectLabelRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private ProjectService projectService;

    private UserPrincipal currentUser;
    private Company company;
    private Employee owner;
    private Project project;
    private UUID userId;
    private UUID companyId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        companyId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        projectId = UUID.fromString("00000000-0000-0000-0000-000000000003");

        currentUser = mock(UserPrincipal.class);
        when(currentUser.getId()).thenReturn(userId);

        company = Company.builder()
                .name("Test Company")
                .build();

        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@company.com")
                .password("encoded")
                .build();

        owner = Employee.builder()
                .employeeId("EMP00001")
                .user(user)
                .build();

        project = Project.builder()
                .name("Test Project")
                .key("PROJ")
                .company(company)
                .owner(owner)
                .build();
    }

    @Test
    void createProject_addsCreatorAsOwner() {
        // Arrange
        when(companyRepository.findByIdAndDeletedFalse(companyId)).thenReturn(Optional.of(company));
        when(projectRepository.existsByKeyAndCompanyIdAndDeletedFalse("PROJ", companyId)).thenReturn(false);
        when(employeeRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(owner));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));
        when(projectMemberRepository.countByProjectIdAndDeletedFalse(any())).thenReturn(1L);

        ArgumentCaptor<ProjectMember> memberCaptor = ArgumentCaptor.forClass(ProjectMember.class);
        when(projectMemberRepository.save(memberCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("Test Project")
                .key("proj")
                .companyId(companyId)
                .startDate(LocalDate.of(2026, 7, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .build();

        // Act
        ProjectResponse response = projectService.createProject(request, currentUser);

        // Assert
        verify(projectMemberRepository).save(any(ProjectMember.class));
        assertThat(memberCaptor.getValue().getRole()).isEqualTo(ProjectMember.MemberRole.OWNER);
    }

    @Test
    void createProject_withDuplicateKey_throwsException() {
        // Arrange
        when(companyRepository.findByIdAndDeletedFalse(companyId)).thenReturn(Optional.of(company));
        when(projectRepository.existsByKeyAndCompanyIdAndDeletedFalse("PROJ", companyId)).thenReturn(true);

        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("Duplicate Project")
                .key("proj")
                .companyId(companyId)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> projectService.createProject(request, currentUser))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void startSprint_whenAnotherActive_throwsException() {
        // Arrange
        UUID sprintId = UUID.fromString("00000000-0000-0000-0000-000000000010");
        UUID activeSprintId = UUID.fromString("00000000-0000-0000-0000-000000000011");

        Sprint activeSprint = Sprint.builder()
                .project(project)
                .name("Active Sprint 1")
                .status(Sprint.SprintStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 14))
                .build();

        Sprint plannedSprint = Sprint.builder()
                .project(project)
                .name("Planned Sprint 2")
                .status(Sprint.SprintStatus.PLANNED)
                .startDate(LocalDate.of(2026, 6, 15))
                .endDate(LocalDate.of(2026, 6, 28))
                .build();

        when(sprintRepository.findByIdAndDeletedFalse(sprintId)).thenReturn(Optional.of(plannedSprint));
        when(sprintRepository.findActiveSprintByProjectId(any())).thenReturn(Optional.of(activeSprint));

        // Act & Assert
        assertThatThrownBy(() -> projectService.startSprint(sprintId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("active sprint");
    }

    @Test
    void startSprint_whenNoActiveSprint_setsStatusActive() {
        // Arrange
        UUID sprintId = UUID.fromString("00000000-0000-0000-0000-000000000010");

        Sprint plannedSprint = Sprint.builder()
                .project(project)
                .name("Sprint 1")
                .status(Sprint.SprintStatus.PLANNED)
                .startDate(LocalDate.of(2026, 7, 1))
                .endDate(LocalDate.of(2026, 7, 14))
                .build();

        when(sprintRepository.findByIdAndDeletedFalse(sprintId)).thenReturn(Optional.of(plannedSprint));
        when(sprintRepository.findActiveSprintByProjectId(any())).thenReturn(Optional.empty());

        ArgumentCaptor<Sprint> sprintCaptor = ArgumentCaptor.forClass(Sprint.class);
        when(sprintRepository.save(sprintCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SprintDto result = projectService.startSprint(sprintId);

        // Assert
        assertThat(sprintCaptor.getValue().getStatus()).isEqualTo(Sprint.SprintStatus.ACTIVE);
    }
}
