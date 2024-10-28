package com.example.controller.task;

import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import com.example.service.TaskService;
import com.example.utils.Either;
import io.micronaut.http.annotation.*;
import io.micronaut.http.MediaType;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/tasks")
@Tag(name = "Tasks", description = "Operaciones de gestión de tareas")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Post
    @Operation(summary = "Crear una nueva tarea")
    @Consumes(MediaType.APPLICATION_JSON)
    public HttpResponse<Void> newTask(@Body TaskRequest taskRequest, Principal principal) {
        TaskDto dto = new TaskDto();
        dto.setName(taskRequest.getName());
        dto.setDescription(taskRequest.getDescription());
        dto.setDueDate(taskRequest.getDueDate());
        dto.setUserName(principal.getName());

        Optional<TaskService.TaskError> response = taskService.newTask(dto);

        if (response.isPresent()) {
            switch (response.get()) {
                case BAD_REQUEST:
                case USER_NOT_EXIST:
                    return HttpResponse.badRequest();
                case UNEXPECTED_ERROR:
                    return HttpResponse.serverError();
            }
        }
        return HttpResponse.ok();
    }

    @Get
    @Operation(summary = "Obtener todas las tareas",
        description = "Devuelve los detalles de tareas filtrando por completadas y fecha de cierre.")
    public HttpResponse<Page<TaskResponse>> getTasks(@QueryValue Optional<Boolean> completed,
                                                     @QueryValue Optional<LocalDate> dueDate,
                                                     Pageable pageable) {
        Either<TaskService.TaskError, Page<TaskResponse>> response = taskService.getTasks(completed, dueDate, pageable);

        if (response.isLeft()) {
            return HttpResponse.serverError();
        }
        return HttpResponse.ok(response.getRight());
    }

    @Get("/{id}")
    @Operation(summary = "Obtener una tarea por ID",
        description = "Devuelve los detalles de una tarea específica.")
    public HttpResponse<TaskResponse> getTask(@QueryValue Long id) {
        Either<TaskService.TaskError, TaskResponse> response = taskService.getTask(id);

        if (response.isLeft()) {
            switch (response.getLeft()) {
                case NO_TASK_EXIST:
                    return HttpResponse.noContent();
                case UNEXPECTED_ERROR:
                    return HttpResponse.serverError();
            }
        }

        return HttpResponse.ok().body(response.getRight());
    }

    @Put("/{id}")
    @Operation(summary = "Actualiza una tarea por ID",
        description = "Actualiza los detalles de una tarea específica.")
    public HttpResponse<Void> updateTask(@QueryValue Long id,
                                         @Body UpdateTaskRequest updateTaskRequest,
                                         Principal principal) {
        TaskDto dto = new TaskDto();
        dto.setId(id);
        dto.setName(updateTaskRequest.getName());
        dto.setDescription(updateTaskRequest.getDescription());
        dto.setDueDate(updateTaskRequest.getDueDate());
        dto.setUserName(principal.getName());

        Optional<TaskService.TaskError> response = taskService.updateTask(dto);

        if (response.isPresent()) {
            switch (response.get()) {
                case UNAUTHORIZES_USER:
                    return HttpResponse.unauthorized();
                case NO_TASK_EXIST:
                    return HttpResponse.noContent();
                case UNEXPECTED_ERROR:
                    return HttpResponse.serverError();
            }
        }
        return HttpResponse.ok();
    }

    @Delete("/{id}")
    @Operation(summary = "Borra una tarea por ID")
    public HttpResponse<Void> deleteTask(@QueryValue Long id, Principal principal) {
        Optional<TaskService.TaskError> response = taskService.deleteTask(id, principal.getName());

        if (response.isPresent()) {
            switch (response.get()) {
                case UNAUTHORIZES_USER:
                    return HttpResponse.unauthorized();
                case NO_TASK_EXIST:
                    return HttpResponse.noContent();
                case UNEXPECTED_ERROR:
                    return HttpResponse.serverError();
            }
        }
        return HttpResponse.ok();
    }

    @Patch("/{id}/complete")
    @Operation(summary = "Completa una tarea por ID")
    public HttpResponse<Void> completeTask(@QueryValue Long id, Principal principal) {
        Optional<TaskService.TaskError> response = taskService.completeTask(id, principal.getName());

        if (response.isPresent()) {
            switch (response.get()) {
                case UNAUTHORIZES_USER:
                    return HttpResponse.unauthorized();
                case NO_TASK_EXIST:
                    return HttpResponse.noContent();
                case UNEXPECTED_ERROR:
                    return HttpResponse.serverError();
            }
        }
        return HttpResponse.ok();
    }
}
