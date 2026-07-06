package com.nexstar.portal.tasks.service;

import com.nexstar.portal.authentication.entity.User;
import com.nexstar.portal.authentication.repository.UserRepository;
import com.nexstar.portal.common.exception.ResourceNotFoundException;
import com.nexstar.portal.employee.entity.Employee;
import com.nexstar.portal.employee.repository.EmployeeRepository;
import com.nexstar.portal.projects.entity.Project;
import com.nexstar.portal.projects.repository.ProjectRepository;
import com.nexstar.portal.projects.repository.SprintRepository;
import com.nexstar.portal.security.UserPrincipal;
import com.nexstar.portal.tasks.dto.CreateTaskRequest;
import com.nexstar.portal.tasks.dto.TaskResponse;
import com.nexstar.portal.tasks.dto.UpdateTaskRequest;
import com.nexstar.portal.tasks.entity.Task;
import com.nexstar.portal.tasks.entity.TaskActivity;
import com.nexstar.portal.tasks.repository.TaskActivityRepository;
import com.nexstar.portal.tasks.repository.TaskChecklistRepository;
import com.nexstar.portal.tasks.repository.TaskCommentRepository;
import com.nexstar.portal.tasks.repository.TaskRepository;
import com.nexstar.portal.tasks.repository.TaskWatcherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskCommentRepository taskCommentRepository;

    @Mock
    private TaskChecklistRepository taskChecklistRepository;

    @Mock
    private TaskWatcherRepository taskWatcherRepository;

    @Mock
    private TaskActivityRepository taskActivityRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private UserPrincipal currentUser;
    private User actorUser;
    private Employee employee;
    private Project project;
    private UUID userId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        projectId = UUID.fromString("00000000-0000-0000-0000-000000000002");

        currentUser = mock(UserPrincipal.class);
        when(currentUser.getId()).thenReturn(userId);

        actorUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@company.com")
                .password("encoded")
                .build();

        employee = Employee.builder()
                .employeeId("EMP00001")
                .user(actorUser)
                .build();

        project = Project.builder()
                .name("Test Project")
                .key("PROJ")
                .build();
    }

    private void stubCommonTaskResponseDeps(UUID taskId) {
        when(taskChecklistRepository.findByTaskIdAndDeletedFalseOrderByPosition(any()))
                .thenReturn(Collections.emptyList());
        when(taskWatcherRepository.countByTaskId(any())).thenReturn(0L);
        when(taskCommentRepository.findByTaskIdAndDeletedFalseOrderByCreatedAtAsc(any()))
                .thenReturn(Collections.emptyList());
    }

    @Test
    void createTask_generatesTaskKeyFromProjectKey() {
        // Arrange
        when(projectRepository.findByIdAndDeletedFalse(projectId)).thenReturn(Optional.of(project));
        when(taskRepository.countByProjectId(projectId)).thenReturn(5L);
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(actorUser));
        when(taskActivityRepository.save(any(TaskActivity.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        when(taskRepository.save(taskCaptor.capture())).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            stubCommonTaskResponseDeps(null);
            return t;
        });

        CreateTaskRequest request = CreateTaskRequest.builder()
                .projectId(projectId)
                .title("Implement feature X")
                .build();

        // Act
        TaskResponse response = taskService.createTask(request, currentUser);

        // Assert
        Task savedTask = taskCaptor.getValue();
        assertThat(savedTask.getTaskKey()).isEqualTo("PROJ-6");
    }

    @Test
    void updateTask_statusChange_logsActivity() {
        // Arrange
        UUID taskId = UUID.fromString("00000000-0000-0000-0000-000000000010");

        Task existingTask = Task.builder()
                .project(project)
                .taskKey("PROJ-1")
                .title("Some task")
                .status(Task.TaskStatus.TODO)
                .priority(Task.TaskPriority.MEDIUM)
                .build();

        when(taskRepository.findByIdAndDeletedFalse(taskId)).thenReturn(Optional.of(existingTask));
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(actorUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<TaskActivity> activityCaptor = ArgumentCaptor.forClass(TaskActivity.class);
        when(taskActivityRepository.save(activityCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        stubCommonTaskResponseDeps(taskId);

        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .status(Task.TaskStatus.IN_PROGRESS)
                .build();

        // Act
        TaskResponse response = taskService.updateTask(taskId, request, currentUser);

        // Assert
        verify(taskActivityRepository).save(any(TaskActivity.class));
        TaskActivity loggedActivity = activityCaptor.getValue();
        assertThat(loggedActivity.getAction()).isEqualTo("STATUS_CHANGED");
    }

    @Test
    void getTask_whenNotFound_throwsException() {
        // Arrange
        UUID unknownId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        when(taskRepository.findByIdAndDeletedFalse(unknownId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.getTask(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
