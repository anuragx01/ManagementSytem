package com.nexstar.portal.projects.service;

import com.nexstar.portal.common.exception.BusinessException;
import com.nexstar.portal.common.exception.ResourceNotFoundException;
import com.nexstar.portal.common.response.PageResponse;
import com.nexstar.portal.employee.entity.Employee;
import com.nexstar.portal.employee.repository.EmployeeRepository;
import com.nexstar.portal.organization.entity.Company;
import com.nexstar.portal.organization.repository.CompanyRepository;
import com.nexstar.portal.projects.dto.*;
import com.nexstar.portal.projects.entity.*;
import com.nexstar.portal.projects.repository.*;
import com.nexstar.portal.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final MilestoneRepository milestoneRepository;
    private final SprintRepository sprintRepository;
    private final ProjectLabelRepository projectLabelRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;

    // ── Projects ──────────────────────────────────────────────────────────────

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, UserPrincipal currentUser) {
        Company company = companyRepository.findByIdAndDeletedFalse(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", request.getCompanyId()));

        String key = request.getKey().toUpperCase();
        if (projectRepository.existsByKeyAndCompanyIdAndDeletedFalse(key, request.getCompanyId())) {
            throw new BusinessException("Project with key '" + key + "' already exists in this company");
        }

        Employee owner = employeeRepository.findByUserIdAndDeletedFalse(currentUser.getId())
                .orElse(null);

        Project project = Project.builder()
                .name(request.getName())
                .key(key)
                .description(request.getDescription())
                .company(company)
                .owner(owner)
                .type(request.getType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isPrivate(request.getIsPrivate() != null && request.getIsPrivate())
                .build();

        project = projectRepository.save(project);

        // Add creator as OWNER member
        if (owner != null) {
            ProjectMember member = ProjectMember.builder()
                    .project(project)
                    .employee(owner)
                    .role(ProjectMember.MemberRole.OWNER)
                    .joinedAt(LocalDateTime.now())
                    .build();
            projectMemberRepository.save(member);
        }

        return toProjectResponse(project);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(UUID id) {
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        return toProjectResponse(project);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> getProjects(UUID companyId, int page, int size) {
        Page<Project> projects = projectRepository.findByCompanyIdAndDeletedFalse(
                companyId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PageResponse.of(projects.map(this::toProjectResponse));
    }

    @Transactional
    public ProjectResponse updateProject(UUID id, CreateProjectRequest request) {
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        String key = request.getKey().toUpperCase();
        if (!key.equals(project.getKey()) &&
                projectRepository.existsByKeyAndCompanyIdAndDeletedFalse(key, project.getCompany().getId())) {
            throw new BusinessException("Project with key '" + key + "' already exists in this company");
        }

        project.setName(request.getName());
        project.setKey(key);
        project.setDescription(request.getDescription());
        project.setType(request.getType());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        if (request.getIsPrivate() != null) {
            project.setPrivate(request.getIsPrivate());
        }

        return toProjectResponse(projectRepository.save(project));
    }

    @Transactional
    public void archiveProject(UUID id) {
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        project.setArchived(true);
        projectRepository.save(project);
    }

    // ── Members ───────────────────────────────────────────────────────────────

    @Transactional
    public ProjectMemberDto addMember(UUID projectId, UUID employeeId, ProjectMember.MemberRole role) {
        Project project = projectRepository.findByIdAndDeletedFalse(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        Employee employee = employeeRepository.findByIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        if (projectMemberRepository.existsByProjectIdAndEmployeeIdAndDeletedFalse(projectId, employeeId)) {
            throw new BusinessException("Employee is already a member of this project");
        }

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .employee(employee)
                .role(role != null ? role : ProjectMember.MemberRole.DEVELOPER)
                .joinedAt(LocalDateTime.now())
                .build();

        return toMemberDto(projectMemberRepository.save(member));
    }

    @Transactional
    public void removeMember(UUID projectId, UUID employeeId) {
        if (!projectRepository.findByIdAndDeletedFalse(projectId).isPresent()) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        projectMemberRepository.deleteByProjectIdAndEmployeeId(projectId, employeeId);
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberDto> getMembers(UUID projectId) {
        return projectMemberRepository.findByProjectIdAndDeletedFalse(projectId)
                .stream().map(this::toMemberDto).collect(Collectors.toList());
    }

    // ── Milestones ────────────────────────────────────────────────────────────

    @Transactional
    public MilestoneDto createMilestone(MilestoneDto dto) {
        Project project = projectRepository.findByIdAndDeletedFalse(dto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", dto.getProjectId()));

        Milestone milestone = Milestone.builder()
                .project(project)
                .name(dto.getName())
                .description(dto.getDescription())
                .startDate(dto.getStartDate())
                .dueDate(dto.getDueDate())
                .status(dto.getStatus() != null ? dto.getStatus() : Milestone.MilestoneStatus.UPCOMING)
                .completionPercentage(dto.getCompletionPercentage())
                .build();

        return toMilestoneDto(milestoneRepository.save(milestone));
    }

    @Transactional(readOnly = true)
    public List<MilestoneDto> getMilestones(UUID projectId) {
        return milestoneRepository.findByProjectIdAndDeletedFalse(projectId)
                .stream().map(this::toMilestoneDto).collect(Collectors.toList());
    }

    @Transactional
    public MilestoneDto updateMilestone(UUID id, MilestoneDto dto) {
        Milestone milestone = milestoneRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone", "id", id));

        milestone.setName(dto.getName());
        milestone.setDescription(dto.getDescription());
        milestone.setStartDate(dto.getStartDate());
        milestone.setDueDate(dto.getDueDate());
        if (dto.getStatus() != null) {
            milestone.setStatus(dto.getStatus());
        }
        milestone.setCompletionPercentage(dto.getCompletionPercentage());

        return toMilestoneDto(milestoneRepository.save(milestone));
    }

    // ── Sprints ───────────────────────────────────────────────────────────────

    @Transactional
    public SprintDto createSprint(SprintDto dto) {
        Project project = projectRepository.findByIdAndDeletedFalse(dto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", dto.getProjectId()));

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BusinessException("Sprint end date cannot be before start date");
        }

        Milestone milestone = null;
        if (dto.getMilestoneId() != null) {
            milestone = milestoneRepository.findByIdAndDeletedFalse(dto.getMilestoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Milestone", "id", dto.getMilestoneId()));
        }

        Sprint sprint = Sprint.builder()
                .project(project)
                .milestone(milestone)
                .name(dto.getName())
                .goal(dto.getGoal())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(Sprint.SprintStatus.PLANNED)
                .build();

        return toSprintDto(sprintRepository.save(sprint));
    }

    @Transactional
    public SprintDto startSprint(UUID sprintId) {
        Sprint sprint = sprintRepository.findByIdAndDeletedFalse(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", sprintId));

        if (sprint.getStatus() != Sprint.SprintStatus.PLANNED) {
            throw new BusinessException("Only a PLANNED sprint can be started");
        }

        // Only one active sprint per project
        sprintRepository.findActiveSprintByProjectId(sprint.getProject().getId())
                .ifPresent(activeSprint -> {
                    throw new BusinessException("Project already has an active sprint: " + activeSprint.getName());
                });

        sprint.setStatus(Sprint.SprintStatus.ACTIVE);
        return toSprintDto(sprintRepository.save(sprint));
    }

    @Transactional
    public SprintDto completeSprint(UUID sprintId) {
        Sprint sprint = sprintRepository.findByIdAndDeletedFalse(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", sprintId));

        if (sprint.getStatus() != Sprint.SprintStatus.ACTIVE) {
            throw new BusinessException("Only an ACTIVE sprint can be completed");
        }

        sprint.setStatus(Sprint.SprintStatus.COMPLETED);
        return toSprintDto(sprintRepository.save(sprint));
    }

    @Transactional(readOnly = true)
    public List<SprintDto> getSprints(UUID projectId) {
        return sprintRepository.findByProjectIdAndDeletedFalse(projectId)
                .stream().map(this::toSprintDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SprintDto getActiveSprint(UUID projectId) {
        return sprintRepository.findActiveSprintByProjectId(projectId)
                .map(this::toSprintDto)
                .orElse(null);
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private ProjectResponse toProjectResponse(Project p) {
        long memberCount = projectMemberRepository.countByProjectIdAndDeletedFalse(p.getId());
        return ProjectResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .key(p.getKey())
                .description(p.getDescription())
                .companyId(p.getCompany() != null ? p.getCompany().getId() : null)
                .companyName(p.getCompany() != null ? p.getCompany().getName() : null)
                .ownerId(p.getOwner() != null ? p.getOwner().getId() : null)
                .ownerName(p.getOwner() != null && p.getOwner().getUser() != null
                        ? p.getOwner().getUser().getFirstName() + " " + p.getOwner().getUser().getLastName()
                        : null)
                .status(p.getStatus())
                .type(p.getType())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .logoUrl(p.getLogoUrl())
                .isPrivate(p.isPrivate())
                .archived(p.isArchived())
                .memberCount(memberCount)
                .createdAt(p.getCreatedAt())
                .build();
    }

    private ProjectMemberDto toMemberDto(ProjectMember m) {
        String empName = null;
        if (m.getEmployee() != null && m.getEmployee().getUser() != null) {
            empName = m.getEmployee().getUser().getFirstName() + " " + m.getEmployee().getUser().getLastName();
        } else if (m.getEmployee() != null) {
            empName = m.getEmployee().getEmployeeId();
        }
        return ProjectMemberDto.builder()
                .id(m.getId())
                .projectId(m.getProject() != null ? m.getProject().getId() : null)
                .employeeId(m.getEmployee() != null ? m.getEmployee().getId() : null)
                .employeeName(empName)
                .role(m.getRole())
                .joinedAt(m.getJoinedAt())
                .build();
    }

    private MilestoneDto toMilestoneDto(Milestone m) {
        return MilestoneDto.builder()
                .id(m.getId())
                .projectId(m.getProject() != null ? m.getProject().getId() : null)
                .name(m.getName())
                .description(m.getDescription())
                .startDate(m.getStartDate())
                .dueDate(m.getDueDate())
                .status(m.getStatus())
                .completionPercentage(m.getCompletionPercentage())
                .build();
    }

    private SprintDto toSprintDto(Sprint s) {
        return SprintDto.builder()
                .id(s.getId())
                .projectId(s.getProject() != null ? s.getProject().getId() : null)
                .milestoneId(s.getMilestone() != null ? s.getMilestone().getId() : null)
                .name(s.getName())
                .goal(s.getGoal())
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .status(s.getStatus())
                .velocity(s.getVelocity())
                .build();
    }
}
