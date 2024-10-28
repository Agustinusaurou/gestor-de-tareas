package com.example.service;

import com.example.controller.task.TaskDto;
import com.example.controller.task.TaskResponse;
import com.example.entities.Task;
import com.example.entities.User;
import com.example.repository.TaskRepository;
import com.example.repository.UserRepository;
import com.example.utils.Either;
import jakarta.inject.Singleton;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import io.micronaut.transaction.annotation.Transactional;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import javax.swing.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Inject
    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Optional<TaskError> newTask(TaskDto dto) {
        if (dto.getName() == null || dto.getName().isEmpty()) {
            return Optional.of(TaskError.BAD_REQUEST);
        }

        Optional<User> optionalUser;
        try {
            optionalUser = userRepository.findByUserName(dto.getUserName());
        } catch (Exception e) {
        log.error("Unexpected exception trying to find User", e);
        return Optional.of(TaskError.UNEXPECTED_ERROR);
        }
        if (!optionalUser.isPresent()) {
            return Optional.of(TaskError.USER_NOT_EXIST);
        }

        Task task = new Task(
            dto.getName(),
            dto.getDescription(),
            dto.getDueDate(),
            optionalUser.get()
        );
        try {
            taskRepository.save(task);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected exception trying to save new Task", e);
            return Optional.of(TaskError.UNEXPECTED_ERROR);
        }
    }

    public Either<TaskError, Page<TaskResponse>> getTasks(Optional<Boolean> completed,
                                                          Optional<LocalDate> dueDate,
                                                          Pageable pageable) {
        try{
            Page<Task> tasks;
            if (completed.isPresent() && dueDate.isPresent()) {
                tasks =  taskRepository.findByCompletedAndDueDateBefore(completed.get(), dueDate.get(), pageable);
            } else if (completed.isPresent()) {
                tasks =  taskRepository.findByCompleted(completed.get(), pageable);
            } else if (dueDate.isPresent()) {
                tasks =  taskRepository.findByDueDateBefore(dueDate.get(), pageable);
            } else {
                tasks = taskRepository.findAll(pageable);
            }

            Page<TaskResponse> tasksResponses = tasks.map(task -> new TaskResponse(task));
            return Either.right(tasksResponses);
        } catch (Exception e) {
            log.error("Unexpected exception trying to find all Tasks", e);
            return Either.left(TaskError.UNEXPECTED_ERROR);
        }
    }

    public Either<TaskError, TaskResponse> getTask(Long id) {
        try{
            Optional<Task> optionalTask = taskRepository.findById(id);

            if (!optionalTask.isPresent()) {
                return Either.left(TaskError.NO_TASK_EXIST);
            }
            TaskResponse response = new TaskResponse(optionalTask.get());

            return Either.right(response);
        } catch (Exception e) {
            log.error("Unexpected exception trying to find Task with id '{}'", id, e);
            return Either.left(TaskError.UNEXPECTED_ERROR);
        }
    }

    @Transactional
    public Optional<TaskError> updateTask(TaskDto dto) {
        try{
            Optional<Task> optionalTask = taskRepository.findById(dto.getId());

            if (!optionalTask.isPresent()) {
                return Optional.of(TaskError.NO_TASK_EXIST);
            }
            Task task = optionalTask.get();

            if (!dto.getUserName().equals(task.getUser().getUserName())) {
                return Optional.of(TaskError.UNAUTHORIZES_USER);
            }

            updateData(task, dto);
            taskRepository.save(task);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected exception trying to update Task with id '{}'", dto.getId(), e);
            return Optional.of(TaskError.UNEXPECTED_ERROR);
        }
    }

    @Transactional
    public Optional<TaskError> deleteTask(Long id, String userName) {
        try{
            Optional<Task> optionalTask = taskRepository.findById(id);

            if (!optionalTask.isPresent()) {
                return Optional.of(TaskError.NO_TASK_EXIST);
            }

            Task task = optionalTask.get();

            if (!userName.equals(task.getUser().getUserName())) {
                return Optional.of(TaskError.UNAUTHORIZES_USER);
            }

            taskRepository.delete(task);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected exception trying to delete Task with id '{}'", id, e);
            return Optional.of(TaskError.UNEXPECTED_ERROR);
        }
    }

    @Transactional
    public Optional<TaskError> completeTask(Long id, String userName) {
        try{
            Optional<Task> optionalTask = taskRepository.findById(id);

            if (!optionalTask.isPresent()) {
                return Optional.of(TaskError.NO_TASK_EXIST);
            }

            Task task = optionalTask.get();

            if (!userName.equals(task.getUser().getUserName())) {
                return Optional.of(TaskError.UNAUTHORIZES_USER);
            }

            task.setCompleted(true);
            taskRepository.save(task);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected exception trying to complete Task with id '{}'", id, e);
            return Optional.of(TaskError.UNEXPECTED_ERROR);
        }
    }

    private void updateData(Task task, TaskDto dto) {
        if (dto.getName() != null && !dto.getName().isEmpty()) {
            task.setName(dto.getName());
        }
        if (dto.getDescription() != null && !dto.getDescription().isEmpty()) {
            task.setDescription(dto.getDescription());
        }
        if (dto.getDueDate() != null) {
            task.setDueDate(dto.getDueDate());
        }
    }

    public enum TaskError {
        BAD_REQUEST,
        USER_NOT_EXIST,
        UNAUTHORIZES_USER,
        NO_TASK_EXIST,
        UNEXPECTED_ERROR
    }
}


