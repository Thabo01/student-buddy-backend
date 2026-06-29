package com.studentbuddy.service;

import com.studentbuddy.dto.TaskRequest;
import com.studentbuddy.dto.TaskResponse;
import com.studentbuddy.exception.ResourceNotFoundException;
import com.studentbuddy.model.Task;
import com.studentbuddy.model.TaskStatus;
import com.studentbuddy.model.User;
import com.studentbuddy.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public List<TaskResponse> listFor(User user) {
        return taskRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getFor(User user, Long id) {
        return TaskResponse.from(requireOwned(user, id));
    }

    @Transactional
    public TaskResponse create(User user, TaskRequest request) {
        Task task = new Task();
        task.setUser(user);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        // Default to TODO when the client doesn't specify a status.
        applyStatus(task, request.status() != null ? request.status() : TaskStatus.TODO);
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse update(User user, Long id, TaskRequest request) {
        Task task = requireOwned(user, id);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        if (request.status() != null) {
            applyStatus(task, request.status());
        }
        return TaskResponse.from(taskRepository.save(task));
    }

    /**
     * Moves a task to a new status (used when dragging a Kanban card). Keeps
     * {@code completedAt} consistent with the DONE state.
     */
    @Transactional
    public TaskResponse changeStatus(User user, Long id, TaskStatus newStatus) {
        Task task = requireOwned(user, id);
        applyStatus(task, newStatus);
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void delete(User user, Long id) {
        taskRepository.delete(requireOwned(user, id));
    }

    /** Centralises the rule that completedAt is set iff the task is DONE. */
    private void applyStatus(Task task, TaskStatus status) {
        task.setStatus(status);
        if (status == TaskStatus.DONE) {
            if (task.getCompletedAt() == null) {
                task.setCompletedAt(Instant.now());
            }
        } else {
            task.setCompletedAt(null);
        }
    }

    private Task requireOwned(User user, Long id) {
        return taskRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> ResourceNotFoundException.of("Task", id));
    }


    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
}