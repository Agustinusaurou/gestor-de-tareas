package com.example.controller.task;

import com.example.entities.Task;

import java.time.LocalDate;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Introspected
@Serdeable
public class TaskResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean completed;
    private LocalDate dueDate;

    public TaskResponse() {
    }

    public TaskResponse(Task task) {
        this.id = task.getId();
        this.name = task.getName();
        this.description = task.getDescription();
        this.completed = task.getCompleted();
        this.dueDate = task.getDueDate();
    }

    public TaskResponse(Long id, String name, String description, Boolean completed, LocalDate dueDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.completed = completed;
        this.dueDate = dueDate;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
