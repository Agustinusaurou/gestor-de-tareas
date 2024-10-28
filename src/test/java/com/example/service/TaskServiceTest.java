package com.example.service;

import com.example.controller.task.TaskDto;
import com.example.controller.task.TaskResponse;
import com.example.entities.Task;
import com.example.entities.User;
import com.example.repository.TaskRepository;
import com.example.repository.UserRepository;
import com.example.utils.CustomPage;
import com.example.utils.CustomPageable;
import com.example.utils.Either;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.assertj.core.api.Assertions.assertThat;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@MicronautTest
class TaskServiceTest {
    private TaskRepository taskRepositoryMock;
    private UserRepository userRepositoryMock;
    @InjectMocks
    private TaskService sut;

    @BeforeEach
    void setup() {
        userRepositoryMock = mock(UserRepository.class);
        taskRepositoryMock = mock(TaskRepository.class);
        sut = new TaskService(taskRepositoryMock, userRepositoryMock);
    }

    @Nested
    @DisplayName("newTask")
    class NewTaskTest {
        @Test
        void when_userRepository_findByUserName_fail_then_return_UNEXPECTED_ERROR() {
            TaskDto dto = new TaskDto();
            dto.setName("NAME");
            dto.setDescription("DESCRIPTION");
            dto.setDueDate(LocalDate.now());
            dto.setUserName("USERNAME");

            when(userRepositoryMock.findByUserName(anyString())).thenThrow(RuntimeException.class);

            Optional<TaskService.TaskError> actual = sut.newTask(dto);

            verify(userRepositoryMock).findByUserName("USERNAME");
            assertThat(actual.isPresent()).isTrue();
            assertThat(actual.get()).isEqualTo(TaskService.TaskError.UNEXPECTED_ERROR);
        }

        @Test
        void when_userRepository_findByUserName_return_empty_optional_then_return_USER_NOT_EXIST() {
            TaskDto dto = new TaskDto();
            dto.setName("NAME");
            dto.setDescription("DESCRIPTION");
            dto.setDueDate(LocalDate.now());
            dto.setUserName("USERNAME");

            when(userRepositoryMock.findByUserName(anyString())).thenReturn(Optional.empty());

            Optional<TaskService.TaskError> actual = sut.newTask(dto);

            verify(userRepositoryMock).findByUserName("USERNAME");
            assertThat(actual.isPresent()).isTrue();
            assertThat(actual.get()).isEqualTo(TaskService.TaskError.USER_NOT_EXIST);
        }

        @Test
        void when_dto_name_is_null_then_return_BAD_REQUEST() {
            TaskDto dto = new TaskDto();
            dto.setDescription("DESCRIPTION");
            dto.setDueDate(LocalDate.now());
            dto.setUserName("USERNAME");

            Optional<TaskService.TaskError> actual = sut.newTask(dto);

            assertThat(actual.isPresent()).isTrue();
            assertThat(actual.get()).isEqualTo(TaskService.TaskError.BAD_REQUEST);
        }

        @Test
        void when_dto_name_is_empty_then_return_BAD_REQUEST() {
            TaskDto dto = new TaskDto();
            dto.setName("");
            dto.setDescription("DESCRIPTION");
            dto.setDueDate(LocalDate.now());
            dto.setUserName("USERNAME");

            Optional<TaskService.TaskError> actual = sut.newTask(dto);

            assertThat(actual.isPresent()).isTrue();
            assertThat(actual.get()).isEqualTo(TaskService.TaskError.BAD_REQUEST);
        }

        @Test
        void when_taskRepository_save_fail_then_return_UNEXPECTED_ERROR() {
            TaskDto dto = new TaskDto();
            dto.setName("NAME");
            dto.setDescription("DESCRIPTION");
            dto.setDueDate(LocalDate.now());
            dto.setUserName("USERNAME");

            User user = new User();

            when(userRepositoryMock.findByUserName(anyString())).thenReturn(Optional.of(user));
            when(taskRepositoryMock.save(any(Task.class))).thenThrow(RuntimeException.class);

            Optional<TaskService.TaskError> actual = sut.newTask(dto);

            verify(userRepositoryMock).findByUserName("USERNAME");
            verify(taskRepositoryMock).save(any(Task.class));
            assertThat(actual.isPresent()).isTrue();
            assertThat(actual.get()).isEqualTo(TaskService.TaskError.UNEXPECTED_ERROR);
        }

        @Test
        void when_task_save_then_return_empty_optional() {
            TaskDto dto = new TaskDto();
            dto.setName("NAME");
            dto.setDescription("DESCRIPTION");
            dto.setDueDate(LocalDate.now());
            dto.setUserName("USERNAME");

            User user = new User();
            Task task = new Task();

            when(userRepositoryMock.findByUserName(anyString())).thenReturn(Optional.of(user));
            when(taskRepositoryMock.save(any(Task.class))).thenReturn(task);

            Optional<TaskService.TaskError> actual = sut.newTask(dto);

            verify(userRepositoryMock).findByUserName("USERNAME");
            verify(taskRepositoryMock).save(any(Task.class));
            assertThat(actual.isPresent()).isFalse();
        }
    }

    @Nested
    @DisplayName("getTasks")
    class GetTasksTest {
        @Test
        void when_taskRepository_findByCompletedAndDueDateBefore_fail_then_return_Either_left_UNEXPECTED_ERROR() {
            Optional<Boolean> completed = Optional.of(true);
            Optional<LocalDate> dueDate = Optional.of(LocalDate.now());
            Pageable pageable = new CustomPageable(0, 1);

            when(taskRepositoryMock.findByCompletedAndDueDateBefore(any(), any(), any())).thenThrow(RuntimeException.class);

            Either<TaskService.TaskError, Page<TaskResponse>> actual = sut.getTasks(completed, dueDate, pageable);

            verify(taskRepositoryMock).findByCompletedAndDueDateBefore(completed.get(), dueDate.get(), pageable);
            assertThat(actual.isLeft()).isTrue();
            assertThat(actual.getLeft()).isEqualTo(TaskService.TaskError.UNEXPECTED_ERROR);
        }

        @Test
        void when_taskRepository_findByCompleted_fail_then_return_Either_left_UNEXPECTED_ERROR() {
            Optional<Boolean> completed = Optional.of(true);
            Optional<LocalDate> dueDate = Optional.empty();
            Pageable pageable = new CustomPageable(0, 1);

            when(taskRepositoryMock.findByCompleted(any(), any())).thenThrow(RuntimeException.class);

            Either<TaskService.TaskError, Page<TaskResponse>> actual = sut.getTasks(completed, dueDate, pageable);

            verify(taskRepositoryMock).findByCompleted(completed.get(), pageable);
            assertThat(actual.isLeft()).isTrue();
            assertThat(actual.getLeft()).isEqualTo(TaskService.TaskError.UNEXPECTED_ERROR);
        }

        @Test
        void when_taskRepository_findByDueDateBefore_fail_then_return_Either_left_UNEXPECTED_ERROR() {
            Optional<Boolean> completed = Optional.empty();
            Optional<LocalDate> dueDate = Optional.of(LocalDate.now());
            Pageable pageable = new CustomPageable(0, 1);

            when(taskRepositoryMock.findByDueDateBefore(any(), any())).thenThrow(RuntimeException.class);

            Either<TaskService.TaskError, Page<TaskResponse>> actual = sut.getTasks(completed, dueDate, pageable);

            verify(taskRepositoryMock).findByDueDateBefore(dueDate.get(), pageable);
            assertThat(actual.isLeft()).isTrue();
            assertThat(actual.getLeft()).isEqualTo(TaskService.TaskError.UNEXPECTED_ERROR);
        }

        @Test
        void when_taskRepository_findAll_fail_then_return_Either_left_UNEXPECTED_ERROR() {
            Optional<Boolean> completed = Optional.empty();
            Optional<LocalDate> dueDate = Optional.empty();
            Pageable pageable = new CustomPageable(0, 1);

            when(taskRepositoryMock.findAll(any())).thenThrow(RuntimeException.class);

            Either<TaskService.TaskError, Page<TaskResponse>> actual = sut.getTasks(completed, dueDate, pageable);

            verify(taskRepositoryMock).findAll(pageable);
            assertThat(actual.isLeft()).isTrue();
            assertThat(actual.getLeft()).isEqualTo(TaskService.TaskError.UNEXPECTED_ERROR);
        }

        @Test
        void when_taskRepository_findAll_return_tasks_then_return_Either_right_with_Page_TaskResponse() {
            Optional<Boolean> completed = Optional.empty();
            Optional<LocalDate> dueDate = Optional.empty();
            Pageable pageable = new CustomPageable(0, 1);

            String name = "NAME";
            String description = "DESCRIPTION";
            LocalDate localDate = LocalDate.now();
            Task task = new Task(name, description, localDate, new User());
            List<Task> taskResponses = Collections.singletonList(task);
            Page<Task> pageTasks = new CustomPage(taskResponses, pageable, taskResponses.size());

            when(taskRepositoryMock.findAll(any())).thenReturn(pageTasks);

            Either<TaskService.TaskError, Page<TaskResponse>> actual = sut.getTasks(completed, dueDate, pageable);

            verify(taskRepositoryMock).findAll(pageable);
            assertThat(actual.isRight()).isTrue();
            assertThat(actual.getRight().getContent()).hasSize(1);
            assertThat(actual.getRight().getContent().get(0).getName()).isEqualTo(name);
            assertThat(actual.getRight().getContent().get(0).getDescription()).isEqualTo(description);
            assertThat(actual.getRight().getContent().get(0).getDueDate()).isEqualTo(localDate);
        }
    }

    @Nested
    @DisplayName("getTask")
    class GetTaskTest {
        @Test
        void when_taskRepository_findById_fail_then_return_Either_left_UNEXPECTED_ERROR() {
            when(taskRepositoryMock.findById(any())).thenThrow(RuntimeException.class);

            Either<TaskService.TaskError, TaskResponse> actual = sut.getTask(1L);

            verify(taskRepositoryMock).findById(1L);
            assertThat(actual.isLeft()).isTrue();
            assertThat(actual.getLeft()).isEqualTo(TaskService.TaskError.UNEXPECTED_ERROR);
        }

        @Test
        void when_taskRepository_findById_return_optional_empty_then_return_Either_left_NO_TASK_EXIST() {
            when(taskRepositoryMock.findById(any())).thenReturn(Optional.empty());

            Either<TaskService.TaskError, TaskResponse> actual = sut.getTask(1L);

            verify(taskRepositoryMock).findById(1L);
            assertThat(actual.isLeft()).isTrue();
            assertThat(actual.getLeft()).isEqualTo(TaskService.TaskError.NO_TASK_EXIST);
        }

        @Test
        void when_taskRepository_findById_return_optional_with_task_then_return_Either_right_TaskResponse() {
            String name = "NAME";
            String description = "DESCRIPTION";
            LocalDate localDate = LocalDate.now();
            Task task = new Task(name, description, localDate, new User());

            when(taskRepositoryMock.findById(any())).thenReturn(Optional.of(task));

            Either<TaskService.TaskError, TaskResponse> actual = sut.getTask(1L);

            verify(taskRepositoryMock).findById(1L);
            assertThat(actual.isRight()).isTrue();
            assertThat(actual.getRight().getName()).isEqualTo(name);
            assertThat(actual.getRight().getDescription()).isEqualTo(description);
            assertThat(actual.getRight().getDueDate()).isEqualTo(localDate);
        }
    }

    @Nested
    @DisplayName("updateTask")
    class UpdateTaskTest {
        @Test
        void when_taskRepository_findById_fail_then_return_Optional_of_UNEXPECTED_ERROR() {
            TaskDto dto = new TaskDto();
            dto.setId(1L);
            dto.setName("NAME");
            dto.setDescription("DESCRIPTION");
            dto.setDueDate(LocalDate.now());
            dto.setUserName("USERNAME");

            when(taskRepositoryMock.findById(any())).thenThrow(RuntimeException.class);

            Optional<TaskService.TaskError> actual = sut.updateTask(dto);

            verify(taskRepositoryMock).findById(1L);
            assertThat(actual.isPresent()).isTrue();
            assertThat(actual.get()).isEqualTo(TaskService.TaskError.UNEXPECTED_ERROR);
        }

        @Test
        void when_taskRepository_findById_return_optional_empty_then_return_Optional_of_NO_TASK_EXIST() {
            TaskDto dto = new TaskDto();
            dto.setId(1L);
            dto.setName("NAME");
            dto.setDescription("DESCRIPTION");
            dto.setDueDate(LocalDate.now());
            dto.setUserName("USERNAME");

            when(taskRepositoryMock.findById(any())).thenReturn(Optional.empty());

            Optional<TaskService.TaskError> actual = sut.updateTask(dto);

            verify(taskRepositoryMock).findById(1L);
            assertThat(actual.isPresent()).isTrue();
            assertThat(actual.get()).isEqualTo(TaskService.TaskError.NO_TASK_EXIST);
        }

        @Test
        void when_username_is_not_equals_to_the_task_user_username_then_return_Optional_of_UNAUTHORIZES_USER() {
            TaskDto dto = new TaskDto();
            dto.setId(1L);
            dto.setName("NAME");
            dto.setDescription("DESCRIPTION");
            dto.setDueDate(LocalDate.now());
            dto.setUserName("USERNAME");

            User user = new User();
            user.setUserName("OTRO");
            String name = "NAME";
            String description = "DESCRIPTION";
            LocalDate localDate = LocalDate.now();
            Task task = new Task(name, description, localDate, user);

            when(taskRepositoryMock.findById(any())).thenReturn(Optional.of(task));

            Optional<TaskService.TaskError> actual = sut.updateTask(dto);

            verify(taskRepositoryMock).findById(1L);
            assertThat(actual.isPresent()).isTrue();
            assertThat(actual.get()).isEqualTo(TaskService.TaskError.UNAUTHORIZES_USER);
        }

        @Test
        void when_taskRepositoryMock_save_fail_then_return_Optional_of_UNEXPECTED_ERROR() {
            TaskDto dto = new TaskDto();
            dto.setId(1L);
            dto.setName("NAME");
            dto.setDescription("DESCRIPTION");
            dto.setDueDate(LocalDate.now());
            dto.setUserName("USERNAME");

            User user = new User();
            user.setUserName("USERNAME");
            String name = "NAME";
            String description = "DESCRIPTION";
            LocalDate localDate = LocalDate.now();
            Task task = new Task(name, description, localDate, user);

            when(taskRepositoryMock.findById(any())).thenReturn(Optional.of(task));
            when(taskRepositoryMock.save(any(Task.class))).thenThrow(RuntimeException.class);

            Optional<TaskService.TaskError> actual = sut.updateTask(dto);

            verify(taskRepositoryMock).findById(1L);
            assertThat(actual.isPresent()).isTrue();
            assertThat(actual.get()).isEqualTo(TaskService.TaskError.UNEXPECTED_ERROR);
        }

        @Test
        void when_username_is_equals_to_the_task_user_username_then_return_Optional_empty() {
            TaskDto dto = new TaskDto();
            dto.setId(1L);
            dto.setName("NAME");
            dto.setDescription("DESCRIPTION");
            dto.setDueDate(LocalDate.now());
            dto.setUserName("USERNAME");

            User user = new User();
            user.setUserName("USERNAME");
            String name = "NAME";
            String description = "DESCRIPTION";
            LocalDate localDate = LocalDate.now();
            Task task = new Task(name, description, localDate, user);

            when(taskRepositoryMock.findById(any())).thenReturn(Optional.of(task));
            when(taskRepositoryMock.save(any(Task.class))).thenReturn(task);

            Optional<TaskService.TaskError> actual = sut.updateTask(dto);

            verify(taskRepositoryMock).findById(1L);
            verify(taskRepositoryMock).save(task);
            assertThat(actual.isPresent()).isFalse();
        }
    }

    @Nested
    @DisplayName("deleteTask")
    class DeleteTaskTest {
        @Test
        void when_taskRepository_findById_fail_then_return_Optional_of_UNEXPECTED_ERROR() {
            Long id = 1L;
            String userName = "USERNAME";

            when(taskRepositoryMock.findById(any())).thenThrow(RuntimeException.class);

            Optional<TaskService.TaskError> actual = sut.deleteTask(id, userName);

            verify(taskRepositoryMock).findById(1L);
            assertThat(actual.isPresent()).isTrue();
            assertThat(actual.get()).isEqualTo(TaskService.TaskError.UNEXPECTED_ERROR);
        }

        @Test
        void when_taskRepository_findById_return_optional_empty_then_return_Optional_of_NO_TASK_EXIST() {
            Long id = 1L;
            String userName = "USERNAME";

            when(taskRepositoryMock.findById(any())).thenReturn(Optional.empty());

            Optional<TaskService.TaskError> actual = sut.deleteTask(id, userName);

            verify(taskRepositoryMock).findById(1L);
            assertThat(actual.isPresent()).isTrue();
            assertThat(actual.get()).isEqualTo(TaskService.TaskError.NO_TASK_EXIST);
        }

        @Test
        void when_username_is_not_equals_to_the_task_user_username_then_return_Optional_of_UNAUTHORIZES_USER() {
            Long id = 1L;
            String userName = "USERNAME";

            User user = new User();
            user.setUserName("OTRO");
            String name = "NAME";
            String description = "DESCRIPTION";
            LocalDate localDate = LocalDate.now();
            Task task = new Task(name, description, localDate, user);

            when(taskRepositoryMock.findById(any())).thenReturn(Optional.of(task));

            Optional<TaskService.TaskError> actual = sut.deleteTask(id, userName);

            verify(taskRepositoryMock).findById(1L);
            assertThat(actual.isPresent()).isTrue();
            assertThat(actual.get()).isEqualTo(TaskService.TaskError.UNAUTHORIZES_USER);
        }

        @Test
        void when_taskRepository_delete_fail_then_return_Optional_of_UNEXPECTED_ERROR() {
            Long id = 1L;
            String userName = "USERNAME";

            User user = new User();
            user.setUserName("USERNAME");
            String name = "NAME";
            String description = "DESCRIPTION";
            LocalDate localDate = LocalDate.now();
            Task task = new Task(name, description, localDate, user);

            when(taskRepositoryMock.findById(any())).thenReturn(Optional.of(task));
            doThrow(RuntimeException.class).when(taskRepositoryMock).delete(any());

            Optional<TaskService.TaskError> actual = sut.deleteTask(id, userName);

            verify(taskRepositoryMock).findById(1L);
            assertThat(actual.isPresent()).isTrue();
            assertThat(actual.get()).isEqualTo(TaskService.TaskError.UNEXPECTED_ERROR);
        }

        @Test
        void when_task_delete_then_return_Optional_empty() {
            Long id = 1L;
            String userName = "USERNAME";

            User user = new User();
            user.setUserName("USERNAME");
            String name = "NAME";
            String description = "DESCRIPTION";
            LocalDate localDate = LocalDate.now();
            Task task = new Task(name, description, localDate, user);

            when(taskRepositoryMock.findById(any())).thenReturn(Optional.of(task));
            doNothing().when(taskRepositoryMock).delete(any());

            Optional<TaskService.TaskError> actual = sut.deleteTask(id, userName);

            verify(taskRepositoryMock).findById(1L);
            verify(taskRepositoryMock).delete(task);
            assertThat(actual.isPresent()).isFalse();
        }
    }

    @Nested
    @DisplayName("completeTask")
    class CompleteTaskTest {
        @Test
        void when_taskRepository_findById_fail_then_return_Optional_of_UNEXPECTED_ERROR() {
            Long id = 1L;
            String userName = "USERNAME";

            when(taskRepositoryMock.findById(any())).thenThrow(RuntimeException.class);

            Optional<TaskService.TaskError> actual = sut.completeTask(id, userName);

            verify(taskRepositoryMock).findById(1L);
            assertThat(actual.isPresent()).isTrue();
            assertThat(actual.get()).isEqualTo(TaskService.TaskError.UNEXPECTED_ERROR);
        }

        @Test
        void when_taskRepository_findById_return_optional_empty_then_return_Optional_of_NO_TASK_EXIST() {
            Long id = 1L;
            String userName = "USERNAME";

            when(taskRepositoryMock.findById(any())).thenReturn(Optional.empty());

            Optional<TaskService.TaskError> actual = sut.completeTask(id, userName);

            verify(taskRepositoryMock).findById(1L);
            assertThat(actual.isPresent()).isTrue();
            assertThat(actual.get()).isEqualTo(TaskService.TaskError.NO_TASK_EXIST);
        }

        @Test
        void when_username_is_not_equals_to_the_task_user_username_then_return_Optional_of_UNAUTHORIZES_USER() {
            Long id = 1L;
            String userName = "USERNAME";

            User user = new User();
            user.setUserName("OTRO");
            String name = "NAME";
            String description = "DESCRIPTION";
            LocalDate localDate = LocalDate.now();
            Task task = new Task(name, description, localDate, user);

            when(taskRepositoryMock.findById(any())).thenReturn(Optional.of(task));

            Optional<TaskService.TaskError> actual = sut.completeTask(id, userName);

            verify(taskRepositoryMock).findById(1L);
            assertThat(actual.isPresent()).isTrue();
            assertThat(actual.get()).isEqualTo(TaskService.TaskError.UNAUTHORIZES_USER);
        }

        @Test
        void when_taskRepository_save_fail_then_return_Optional_of_UNEXPECTED_ERROR() {
            Long id = 1L;
            String userName = "USERNAME";

            User user = new User();
            user.setUserName("USERNAME");
            String name = "NAME";
            String description = "DESCRIPTION";
            LocalDate localDate = LocalDate.now();
            Task task = new Task(name, description, localDate, user);

            when(taskRepositoryMock.findById(any())).thenReturn(Optional.of(task));
            when(taskRepositoryMock.save(any())).thenThrow(RuntimeException.class);

            Optional<TaskService.TaskError> actual = sut.completeTask(id, userName);

            verify(taskRepositoryMock).findById(1L);
            assertThat(actual.isPresent()).isTrue();
            assertThat(actual.get()).isEqualTo(TaskService.TaskError.UNEXPECTED_ERROR);
        }

        @Test
        void when_complete_task_then_return_Optional_empty() {
            Long id = 1L;
            String userName = "USERNAME";

            User user = new User();
            user.setUserName("USERNAME");
            String name = "NAME";
            String description = "DESCRIPTION";
            LocalDate localDate = LocalDate.now();
            Task task = new Task(name, description, localDate, user);

            when(taskRepositoryMock.findById(any())).thenReturn(Optional.of(task));
            when(taskRepositoryMock.save(any())).thenReturn(task);

            Optional<TaskService.TaskError> actual = sut.completeTask(id, userName);

            verify(taskRepositoryMock).findById(1L);
            verify(taskRepositoryMock).save(task);
            assertThat(actual.isPresent()).isFalse();
        }
    }
}
