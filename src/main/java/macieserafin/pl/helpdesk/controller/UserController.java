package macieserafin.pl.helpdesk.controller;

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
@RequestMapping
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //zwraca zalogowanego usera
    @GetMapping("/api/users/me")
    public UserResponse getCurrentUser(Principal principal) {
        return userService.findCurrentUser(principal.getName());
    }

    //aktualizuje profil zalogowanego usera
    @PatchMapping("/api/users/me/profile")
    public UserResponse updateCurrentUserProfile(@RequestBody UserProfileRequest request, Principal principal) {
        return userService.updateCurrentUserProfile(principal.getName(), request);
    }

    //zwraca liste wszsytkich userow
    @GetMapping("/api/admin/users")
    public List<UserResponse> getUsers() {
        return userService.findAllUsers();
    }

    //zwraca dane usera po id
    @GetMapping("/api/admin/users/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.findUserById(id);
    }

    //tworzy nowego usera
    @PostMapping("/api/admin/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    //aktualizuje dane usera po ID
    @PatchMapping("/api/admin/users/{id}")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }

    //wlacza lub wylacza konto usera
    @PatchMapping("/api/admin/users/{id}/enabled")
    public UserResponse updateEnabled(@PathVariable Long id, @RequestBody UpdateUserEnabledRequest request) {
        return userService.updateEnabled(id, request);
    }
}
