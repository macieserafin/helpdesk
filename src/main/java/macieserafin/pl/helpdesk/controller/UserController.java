package macieserafin.pl.helpdesk.controller;

import macieserafin.pl.helpdesk.dto.UserResponse;
import macieserafin.pl.helpdesk.model.entity.User;
import macieserafin.pl.helpdesk.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
//@RequestMapping
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin/users")
    public List<UserResponse> getUsers() {
        return userService.findAllUsers();
    }
}
