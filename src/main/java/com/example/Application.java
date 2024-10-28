package com.example;

import com.example.controller.task.TaskRequest;
import io.micronaut.runtime.Micronaut;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.*;
import io.swagger.v3.oas.annotations.servers.Server;
import io.micronaut.core.annotation.Introspected;
import javax.persistence.Entity;
import io.micronaut.serde.annotation.SerdeImport;

@OpenAPIDefinition(
    info = @Info(
        title = "Gestor de tareas",
        version = "1.0",
        description = "Gestor de tareas"
    )
)
@SerdeImport(TaskRequest.class)
@Introspected(packages = "com.example.entities", includedAnnotations = Entity.class)
public class Application {

    public static void main(String[] args) {
        Micronaut.build(args)
            .packages("com.example")
            .start();
    }

    public Application(ObjectMapper mapper) {
        mapper.findAndRegisterModules();
    }
}
