package com.studentbuddy.service;

import com.studentbuddy.dto.TaskRequest;
import com.studentbuddy.dto.TaskResponse;
import com.studentbuddy.exception.ResourceNotFoundException;
import com.studentbuddy.model.Task;
import com.studentbuddy.model.TaskStatus;
import com.studentbuddy.model.User;
import com.studentbuddy.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
    }

    @Test
    void create_defaultsToTodoWhenStatusOmitted() {
        TaskRequest request = new TaskRequest("Write essay", null, null, null);
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse response = taskService.create(user, request);

        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
        assertThat(response.completedAt()).isNull();
    }

    @Test
    void changeStatus_toDone_setsCompletedAt() {
        Task task = new Task();
        task.setId(2L);
        task.setUser(user);
        task.setStatus(TaskStatus.IN_PROGRESS);
        when(taskRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse response = taskService.changeStatus(user, 2L, TaskStatus.DONE);

        assertThat(response.status()).isEqualTo(TaskStatus.DONE);
        assertThat(response.completedAt()).isNotNull();
    }

    @Test
    void changeStatus_awayFromDone_clearsCompletedAt() {
        Task task = new Task();
        task.setId(2L);
        task.setUser(user);
        task.setStatus(TaskStatus.DONE);
        task.setCompletedAt(Instant.now());
        when(taskRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse response = taskService.changeStatus(user, 2L, TaskStatus.TODO);

        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
        assertThat(response.completedAt()).isNull();
    }

    @Test
    void getFor_whenNotOwned_throwsNotFound() {
        when(taskRepository.findByIdAndUserId(7L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getFor(user, 7L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
