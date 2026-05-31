package macieserafin.pl.helpdesk.controller;

import jakarta.validation.Valid;
import macieserafin.pl.helpdesk.dto.CreateUserRequest;
import macieserafin.pl.helpdesk.dto.UpdateUserEnabledRequest;
import macieserafin.pl.helpdesk.dto.UpdateUserRequest;
import macieserafin.pl.helpdesk.dto.UserProfileRequest;
import macieserafin.pl.helpdesk.dto.UserResponse;
import macieserafin.pl.helpdesk.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //zwraca zalogowanego usera
    @GetMapping("/users/me")
    public UserResponse getCurrentUser(Principal principal) {
        return userService.findCurrentUser(principal.getName());
    }

    //aktualizuje profil zalogowanego usera
    @PatchMapping("/users/me/profile")
    public UserResponse updateCurrentUserProfile(@Valid @RequestBody UserProfileRequest request, Principal principal) {
        return userService.updateCurrentUserProfile(principal.getName(), request);
    }

    //zwraca liste wszsytkich userow
    @GetMapping("/admin/users")
    public List<UserResponse> getUsers() {
        return userService.findAllUsers();
    }

    //zwraca dane usera po id
    @GetMapping("/admin/users/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.findUserById(id);
    }

    //tworzy nowego usera
    @PostMapping("/admin/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    //aktualizuje dane usera po ID
    @PatchMapping("/admin/users/{id}")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }

    //wlacza lub wylacza konto usera
    @PatchMapping("/admin/users/{id}/enabled")
    public UserResponse updateEnabled(@PathVariable Long id, @Valid @RequestBody UpdateUserEnabledRequest request) {
        return userService.updateEnabled(id, request);
    }
}
