package com.example.controller.user;

import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.authentication.AuthenticationResponse;
import com.example.service.UserService;
import io.micronaut.http.annotation.*;
import io.micronaut.http.MediaType;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import java.util.Optional;

@Controller("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Post
    @Secured(SecurityRule.IS_ANONYMOUS)
    @Consumes(MediaType.APPLICATION_JSON)
    public HttpResponse<Void> newUser(@Body UserRequest userRequest) {
        UserDto dto = new UserDto();
        dto.setUserName(userRequest.getUserName());
        dto.setPassword(userRequest.getPassword());

        Optional<UserService.UserError> response = userService.createNewUser(dto);
        if (response.isPresent()) {
            switch (response.get()) {
                case BAD_REQUEST:
                    return HttpResponse.badRequest();
                case UNEXPECTED_ERROR:
                    return HttpResponse.serverError();
            }
        }
        return HttpResponse.ok();
    }

    @Post("/login")
    @Secured(SecurityRule.IS_ANONYMOUS)
    @Consumes(MediaType.APPLICATION_JSON)
    public HttpResponse<String> login(@Body UserRequest userRequest) {
        try {
            String token = userService.authenticateUser(userRequest.getUserName(), userRequest.getPassword());
            return HttpResponse.ok(token);
        } catch (RuntimeException e) {
            return HttpResponse.unauthorized();
        }
    }
}
