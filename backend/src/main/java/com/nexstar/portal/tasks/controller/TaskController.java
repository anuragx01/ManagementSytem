package com.nexstar.portal.tasks.controller;

import com.nexstar.portal.common.response.ApiResponse;
import com.nexstar.portal.common.response.PageResponse;
import com.nexstar.portal.security.UserPrincipal;
import com.nexstar.portal.tasks.dto.*;
import com.nexstar.portal.tasks.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management, comments, checklists, and activity feed")
public class TaskController {

    private final TaskService taskService;

    // ── Tasks ─────────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('TEAM_LEAD')")
    @Operation(summary = "Create a new task")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Task created", taskService.createTask(request, currentUser)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR') or hasRole('MANAGER') or hasRole('TEAM_LEAD') or hasRole('EMPLOYEE')")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTask(id, currentUser)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR') or hasRole('MANAGER') or hasRole('TEAM_LEAD') or hasRole('EMPLOYEE')")
    @Operation(summary = "Search and filter tasks")
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> searchTasks(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) UUID sprintId,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        TaskFilterRequest filter = TaskFilterRequest.builder()
                .projectId(projectId)
                .sprintId(sprintId)
                .assigneeId(assigneeId)
                .type(type != null ? com.nexstar.portal.tasks.entity.Task.TaskType.valueOf(type) : null)
                .priority(priority != null ? com.nexstar.portal.tasks.entity.Task.TaskPriority.valueOf(priority) : null)
                .status(status != null ? com.nexstar.portal.tasks.entity.Task.TaskStatus.valueOf(status) : null)
                .search(search)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();

        return ResponseEntity.ok(ApiResponse.success(taskService.searchTasks(filter, currentUser)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('TEAM_LEAD') or hasRole('EMPLOYEE')")
    @Operation(summary = "Update a task (partial update)")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable UUID id,
            @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Task updated", taskService.updateTask(id, request, currentUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Delete a task (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.noContent("Task deleted"));
    }

    @GetMapping("/kanban")
    @Operation(summary = "Get kanban board for a project sprint")
    public ResponseEntity<ApiResponse<KanbanResponse>> getKanban(
            @RequestParam UUID projectId,
            @RequestParam UUID sprintId) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getKanban(projectId, sprintId)));
    }

    @GetMapping("/backlog")
    @Operation(summary = "Get backlog tasks (not in any sprint)")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getBacklog(@RequestParam UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getBacklog(projectId)));
    }

    @GetMapping("/{id}/subtasks")
    @Operation(summary = "Get subtasks of a task")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getSubtasks(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getSubtasks(id)));
    }

    // ── Comments ──────────────────────────────────────────────────────────────

    @PostMapping("/{taskId}/comments")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('TEAM_LEAD') or hasRole('EMPLOYEE')")
    @Operation(summary = "Add a comment to a task")
    public ResponseEntity<ApiResponse<TaskCommentDto>> addComment(
            @PathVariable UUID taskId,
            @RequestParam String content,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Comment added", taskService.addComment(taskId, content, currentUser)));
    }

    @GetMapping("/{taskId}/comments")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR') or hasRole('MANAGER') or hasRole('TEAM_LEAD') or hasRole('EMPLOYEE')")
    @Operation(summary = "Get all comments for a task")
    public ResponseEntity<ApiResponse<List<TaskCommentDto>>> getComments(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getComments(taskId, currentUser)));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Delete a comment")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        taskService.deleteComment(commentId, currentUser);
        return ResponseEntity.ok(ApiResponse.noContent("Comment deleted"));
    }

    // ── Checklists ────────────────────────────────────────────────────────────

    @PutMapping("/{taskId}/checklists")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('TEAM_LEAD') or hasRole('EMPLOYEE')")
    @Operation(summary = "Replace all checklist items for a task")
    public ResponseEntity<ApiResponse<List<TaskChecklistDto>>> updateChecklist(
            @PathVariable UUID taskId,
            @RequestBody List<TaskChecklistDto> items) {
        return ResponseEntity.ok(ApiResponse.success("Checklist updated", taskService.updateChecklist(taskId, items)));
    }

    @PostMapping("/checklists/{checklistId}/toggle")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('TEAM_LEAD') or hasRole('EMPLOYEE')")
    @Operation(summary = "Toggle a checklist item completion")
    public ResponseEntity<ApiResponse<TaskChecklistDto>> toggleChecklistItem(
            @PathVariable UUID checklistId) {
        return ResponseEntity.ok(ApiResponse.success(taskService.toggleChecklistItem(checklistId)));
    }

    // ── Watchers ──────────────────────────────────────────────────────────────

    @PostMapping("/{taskId}/watch")
    @Operation(summary = "Watch a task")
    public ResponseEntity<ApiResponse<Void>> addWatcher(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        taskService.addWatcher(taskId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Now watching task", null));
    }

    @DeleteMapping("/{taskId}/watch")
    @Operation(summary = "Unwatch a task")
    public ResponseEntity<ApiResponse<Void>> removeWatcher(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        taskService.removeWatcher(taskId, currentUser);
        return ResponseEntity.ok(ApiResponse.noContent("Stopped watching task"));
    }

    // ── Activity ──────────────────────────────────────────────────────────────

    @GetMapping("/{taskId}/activity")
    @Operation(summary = "Get activity log for a task")
    public ResponseEntity<ApiResponse<PageResponse<TaskActivityDto>>> getActivity(
            @PathVariable UUID taskId,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getActivity(taskId, page)));
    }
}
