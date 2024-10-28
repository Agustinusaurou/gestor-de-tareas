package com.example;

import io.micronaut.http.annotation.*;

@Controller("/gestor-de-tareas")
public class GestorDeTareasController {

    @Get(uri = "/", produces = "text/plain")
    public String index() {
        return "Example Response";
    }
}
