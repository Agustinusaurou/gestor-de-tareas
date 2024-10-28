package com.example.repository;

import com.example.entities.Task;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Page;
import java.time.LocalDate;

@Repository
public interface TaskRepository extends CrudRepository<Task, Long>{
    Page<Task> findByCompletedAndDueDateBefore(Boolean completed, LocalDate dueDate, Pageable pageable);

    Page<Task> findByCompleted(Boolean completed, Pageable pageable);

    Page<Task> findByDueDateBefore(LocalDate dueDate, Pageable pageable);

    Page<Task> findAll(Pageable pageable);
}
