package com.nexstar.portal.tasks.service;

import com.nexstar.portal.authentication.entity.User;
import com.nexstar.portal.authentication.repository.UserRepository;
import com.nexstar.portal.common.exception.BusinessException;
import com.nexstar.portal.common.exception.ResourceNotFoundException;
import com.nexstar.portal.common.response.PageResponse;
import com.nexstar.portal.employee.entity.Employee;
import com.nexstar.portal.employee.repository.EmployeeRepository;
import com.nexstar.portal.projects.entity.Project;
import com.nexstar.portal.projects.entity.Sprint;
import com.nexstar.portal.projects.repository.ProjectRepository;
import com.nexstar.portal.projects.repository.SprintRepository;
import com.nexstar.portal.security.UserPrincipal;
import com.nexstar.portal.tasks.dto.*;
import com.nexstar.portal.tasks.entity.*;
import com.nexstar.portal.tasks.repository.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskChecklistRepository taskChecklistRepository;
    private final TaskWatcherRepository taskWatcherRepository;
    private final TaskActivityRepository taskActivityRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    // ── Tasks ─────────────────────────────────────────────────────────────────

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, UserPrincipal currentUser) {
        Project project = projectRepository.findByIdAndDeletedFalse(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        Sprint sprint = null;
        if (request.getSprintId() != null) {
            sprint = sprintRepository.findByIdAndDeletedFalse(request.getSprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", request.getSprintId()));
        }

        Task parent = null;
        if (request.getParentId() != null) {
            parent = taskRepository.findByIdAndDeletedFalse(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task", "id", request.getParentId()));
        }

        Employee assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = employeeRepository.findByIdAndDeletedFalse(request.getAssigneeId()).orElse(null);
        }

        Employee reporter = null;
        if (request.getReporterId() != null) {
            reporter = employeeRepository.findByIdAndDeletedFalse(request.getReporterId()).orElse(null);
        }

        // Generate task key: PROJECT_KEY-N
        long taskCount = taskRepository.countByProjectId(project.getId()) + 1;
        String taskKey = project.getKey() + "-" + taskCount;

        Task task = Task.builder()
                .project(project)
                .sprint(sprint)
                .parent(parent)
                .taskKey(taskKey)
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType() != null ? request.getType() : Task.TaskType.TASK)
                .priority(request.getPriority() != null ? request.getPriority() : Task.TaskPriority.MEDIUM)
                .status(request.getStatus() != null ? request.getStatus() : Task.TaskStatus.BACKLOG)
                .assignee(assignee)
                .reporter(reporter)
                .dueDate(request.getDueDate())
                .storyPoints(request.getStoryPoints())
                .estimatedHours(request.getEstimatedHours())
                .build();

        task = taskRepository.save(task);

        // Log activity
        User actor = userRepository.findByIdAndDeletedFalse(currentUser.getId()).orElse(null);
        logActivity(task, actor, "CREATED", null, task.getStatus().name(), "Task created");

        return toTaskResponse(task);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(UUID id) {
        Task task = taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        return toTaskResponse(task);
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> searchTasks(TaskFilterRequest filter) {
        Sort sort = filter.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();
        PageRequest pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<Task> spec = buildSpecification(filter);
        Page<Task> page = taskRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toTaskResponse));
    }

    @Transactional
    public TaskResponse updateTask(UUID id, UpdateTaskRequest request, UserPrincipal currentUser) {
        Task task = taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        User actor = userRepository.findByIdAndDeletedFalse(currentUser.getId()).orElse(null);

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            task.setType(request.getType());
        }
        if (request.getPriority() != null) {
            String oldPriority = task.getPriority().name();
            task.setPriority(request.getPriority());
            if (!oldPriority.equals(request.getPriority().name())) {
                logActivity(task, actor, "PRIORITY_CHANGED", oldPriority, request.getPriority().name(), null);
            }
        }
        if (request.getStatus() != null) {
            String oldStatus = task.getStatus().name();
            task.setStatus(request.getStatus());
            if (!oldStatus.equals(request.getStatus().name())) {
                logActivity(task, actor, "STATUS_CHANGED", oldStatus, request.getStatus().name(), null);
            }
        }
        if (request.getAssigneeId() != null) {
            Employee newAssignee = employeeRepository.findByIdAndDeletedFalse(request.getAssigneeId()).orElse(null);
            String oldAssignee = task.getAssignee() != null ? task.getAssignee().getId().toString() : "none";
            String newAssigneeName = newAssignee != null ? request.getAssigneeId().toString() : "none";
            task.setAssignee(newAssignee);
            logActivity(task, actor, "ASSIGNEE_CHANGED", oldAssignee, newAssigneeName, null);
        }
        if (request.getReporterId() != null) {
            Employee newReporter = employeeRepository.findByIdAndDeletedFalse(request.getReporterId()).orElse(null);
            task.setReporter(newReporter);
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getStoryPoints() != null) {
            task.setStoryPoints(request.getStoryPoints());
        }
        if (request.getEstimatedHours() != null) {
            task.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getActualHours() != null) {
            task.setActualHours(request.getActualHours());
        }
        if (request.getPosition() != null) {
            task.setPosition(request.getPosition());
        }
        if (request.getIsRecurring() != null) {
            task.setRecurring(request.getIsRecurring());
        }
        if (request.getSprintId() != null) {
            Sprint sprint = sprintRepository.findByIdAndDeletedFalse(request.getSprintId()).orElse(null);
            task.setSprint(sprint);
        }
        if (request.getParentId() != null) {
            Task parentTask = taskRepository.findByIdAndDeletedFalse(request.getParentId()).orElse(null);
            task.setParent(parentTask);
        }

        return toTaskResponse(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(UUID id) {
        Task task = taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        task.setDeleted(true);
        taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public KanbanResponse getKanban(UUID projectId, UUID sprintId) {
        List<Task> tasks = taskRepository.findByProjectIdAndSprintIdAndDeletedFalse(projectId, sprintId);

        Map<String, List<TaskResponse>> columns = new LinkedHashMap<>();
        for (Task.TaskStatus status : Task.TaskStatus.values()) {
            columns.put(status.name(), new ArrayList<>());
        }

        for (Task task : tasks) {
            String statusKey = task.getStatus().name();
            columns.get(statusKey).add(toTaskResponse(task));
        }

        // Sort each column by position
        columns.values().forEach(list ->
                list.sort(Comparator.comparingInt(TaskResponse::getPosition)));

        return KanbanResponse.builder().columns(columns).build();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getBacklog(UUID projectId) {
        return taskRepository.findByProjectIdAndSprintIdIsNullAndDeletedFalse(projectId)
                .stream().map(this::toTaskResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getSubtasks(UUID parentId) {
        return taskRepository.findByParentIdAndDeletedFalse(parentId)
                .stream().map(this::toTaskResponse).collect(Collectors.toList());
    }

    // ── Comments ──────────────────────────────────────────────────────────────

    @Transactional
    public TaskCommentDto addComment(UUID taskId, String content, UserPrincipal currentUser) {
        Task task = taskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        User author = userRepository.findByIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        TaskComment comment = TaskComment.builder()
                .task(task)
                .author(author)
                .content(content)
                .build();

        return toCommentDto(taskCommentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<TaskCommentDto> getComments(UUID taskId) {
        return taskCommentRepository.findByTaskIdAndDeletedFalseOrderByCreatedAtAsc(taskId)
                .stream().map(this::toCommentDto).collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(UUID commentId, UserPrincipal currentUser) {
        TaskComment comment = taskCommentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new BusinessException("You can only delete your own comments", HttpStatus.FORBIDDEN);
        }

        comment.setDeleted(true);
        taskCommentRepository.save(comment);
    }

    // ── Checklists ────────────────────────────────────────────────────────────

    @Transactional
    public List<TaskChecklistDto> updateChecklist(UUID taskId, List<TaskChecklistDto> items) {
        Task task = taskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        // Soft-delete existing checklists
        taskChecklistRepository.softDeleteByTaskId(taskId);

        // Save new checklists
        List<TaskChecklist> saved = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            TaskChecklistDto dto = items.get(i);
            TaskChecklist checklist = TaskChecklist.builder()
                    .task(task)
                    .title(dto.getTitle())
                    .completed(dto.isCompleted())
                    .position(dto.getPosition() > 0 ? dto.getPosition() : i)
                    .build();
            saved.add(taskChecklistRepository.save(checklist));
        }

        return saved.stream().map(this::toChecklistDto).collect(Collectors.toList());
    }

    @Transactional
    public TaskChecklistDto toggleChecklistItem(UUID checklistId) {
        TaskChecklist checklist = taskChecklistRepository.findByIdAndDeletedFalse(checklistId)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist item", "id", checklistId));
        checklist.setCompleted(!checklist.isCompleted());
        return toChecklistDto(taskChecklistRepository.save(checklist));
    }

    // ── Watchers ──────────────────────────────────────────────────────────────

    @Transactional
    public void addWatcher(UUID taskId, UserPrincipal currentUser) {
        Task task = taskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        User user = userRepository.findByIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        if (taskWatcherRepository.existsByTaskIdAndUserId(taskId, currentUser.getId())) {
            throw new BusinessException("You are already watching this task");
        }

        TaskWatcherId watcherId = new TaskWatcherId(taskId, currentUser.getId());
        TaskWatcher watcher = TaskWatcher.builder()
                .id(watcherId)
                .task(task)
                .user(user)
                .addedAt(LocalDateTime.now())
                .build();
        taskWatcherRepository.save(watcher);
    }

    @Transactional
    public void removeWatcher(UUID taskId, UserPrincipal currentUser) {
        taskWatcherRepository.deleteByTaskIdAndUserId(taskId, currentUser.getId());
    }

    // ── Activity ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<TaskActivityDto> getActivity(UUID taskId, int page) {
        Page<TaskActivity> activities = taskActivityRepository.findByTaskIdOrderByCreatedAtDesc(
                taskId, PageRequest.of(page, 20));
        return PageResponse.of(activities.map(this::toActivityDto));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void logActivity(Task task, User actor, String action, String from, String to, String detail) {
        TaskActivity activity = TaskActivity.builder()
                .task(task)
                .actor(actor)
                .action(action)
                .from(from)
                .to(to)
                .detail(detail)
                .build();
        taskActivityRepository.save(activity);
    }

    private Specification<Task> buildSpecification(TaskFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (filter.getProjectId() != null) {
                predicates.add(cb.equal(root.get("project").get("id"), filter.getProjectId()));
            }
            if (filter.getSprintId() != null) {
                predicates.add(cb.equal(root.get("sprint").get("id"), filter.getSprintId()));
            }
            if (filter.getAssigneeId() != null) {
                predicates.add(cb.equal(root.get("assignee").get("id"), filter.getAssigneeId()));
            }
            if (filter.getType() != null) {
                predicates.add(cb.equal(root.get("type"), filter.getType()));
            }
            if (filter.getPriority() != null) {
                predicates.add(cb.equal(root.get("priority"), filter.getPriority()));
            }
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }
            if (StringUtils.hasText(filter.getSearch())) {
                predicates.add(cb.like(cb.lower(root.get("title")),
                        "%" + filter.getSearch().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private TaskResponse toTaskResponse(Task t) {
        List<TaskChecklist> checklists = taskChecklistRepository.findByTaskIdAndDeletedFalseOrderByPosition(t.getId());
        int checklistTotal = checklists.size();
        int checklistDone = (int) checklists.stream().filter(TaskChecklist::isCompleted).count();
        long watcherCount = taskWatcherRepository.countByTaskId(t.getId());
        long commentCount = taskCommentRepository.findByTaskIdAndDeletedFalseOrderByCreatedAtAsc(t.getId()).size();

        String assigneeName = null;
        if (t.getAssignee() != null && t.getAssignee().getUser() != null) {
            assigneeName = t.getAssignee().getUser().getFirstName() + " " + t.getAssignee().getUser().getLastName();
        }
        String reporterName = null;
        if (t.getReporter() != null && t.getReporter().getUser() != null) {
            reporterName = t.getReporter().getUser().getFirstName() + " " + t.getReporter().getUser().getLastName();
        }

        return TaskResponse.builder()
                .id(t.getId())
                .projectId(t.getProject() != null ? t.getProject().getId() : null)
                .projectName(t.getProject() != null ? t.getProject().getName() : null)
                .projectKey(t.getProject() != null ? t.getProject().getKey() : null)
                .sprintId(t.getSprint() != null ? t.getSprint().getId() : null)
                .sprintName(t.getSprint() != null ? t.getSprint().getName() : null)
                .parentId(t.getParent() != null ? t.getParent().getId() : null)
                .parentTaskKey(t.getParent() != null ? t.getParent().getTaskKey() : null)
                .taskKey(t.getTaskKey())
                .title(t.getTitle())
                .description(t.getDescription())
                .type(t.getType())
                .priority(t.getPriority())
                .status(t.getStatus())
                .assigneeId(t.getAssignee() != null ? t.getAssignee().getId() : null)
                .assigneeName(assigneeName)
                .reporterId(t.getReporter() != null ? t.getReporter().getId() : null)
                .reporterName(reporterName)
                .dueDate(t.getDueDate())
                .storyPoints(t.getStoryPoints())
                .estimatedHours(t.getEstimatedHours())
                .actualHours(t.getActualHours())
                .position(t.getPosition())
                .isRecurring(t.isRecurring())
                .commentCount(commentCount)
                .checklistTotal(checklistTotal)
                .checklistDone(checklistDone)
                .watcherCount(watcherCount)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private TaskCommentDto toCommentDto(TaskComment c) {
        String authorName = null;
        String authorAvatar = null;
        if (c.getAuthor() != null) {
            authorName = c.getAuthor().getFirstName() + " " + c.getAuthor().getLastName();
            authorAvatar = c.getAuthor().getProfilePictureUrl();
        }
        return TaskCommentDto.builder()
                .id(c.getId())
                .taskId(c.getTask() != null ? c.getTask().getId() : null)
                .authorId(c.getAuthor() != null ? c.getAuthor().getId() : null)
                .authorName(authorName)
                .authorAvatar(authorAvatar)
                .content(c.getContent())
                .edited(c.isEdited())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private TaskChecklistDto toChecklistDto(TaskChecklist cl) {
        return TaskChecklistDto.builder()
                .id(cl.getId())
                .taskId(cl.getTask() != null ? cl.getTask().getId() : null)
                .title(cl.getTitle())
                .completed(cl.isCompleted())
                .position(cl.getPosition())
                .build();
    }

    private TaskActivityDto toActivityDto(TaskActivity a) {
        String actorName = null;
        if (a.getActor() != null) {
            actorName = a.getActor().getFirstName() + " " + a.getActor().getLastName();
        }
        return TaskActivityDto.builder()
                .id(a.getId())
                .taskId(a.getTask() != null ? a.getTask().getId() : null)
                .actorId(a.getActor() != null ? a.getActor().getId() : null)
                .actorName(actorName)
                .action(a.getAction())
                .from(a.getFrom())
                .to(a.getTo())
                .detail(a.getDetail())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
