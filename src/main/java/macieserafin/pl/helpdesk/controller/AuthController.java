package macieserafin.pl.helpdesk.controller;

import macieserafin.pl.helpdesk.dto.RegisterUserRequest;
import macieserafin.pl.helpdesk.dto.UserResponse;
import macieserafin.pl.helpdesk.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody RegisterUserRequest request) {
        return userService.registerUser(request);
    }
}
