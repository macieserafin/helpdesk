package macieserafin.pl.helpdesk.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

import static macieserafin.pl.helpdesk.contract.ApiContract.EMAIL_MAX_LENGTH;
import static macieserafin.pl.helpdesk.contract.ApiContract.PASSWORD_MAX_LENGTH;
import static macieserafin.pl.helpdesk.contract.ApiContract.PASSWORD_MIN_LENGTH;
import static macieserafin.pl.helpdesk.contract.ApiContract.ROLE_NAME_MAX_LENGTH;
import static macieserafin.pl.helpdesk.contract.ApiContract.USERNAME_MAX_LENGTH;
import static macieserafin.pl.helpdesk.contract.ApiContract.USERNAME_MIN_LENGTH;

public class UpdateUserRequest {
    @Size(min = USERNAME_MIN_LENGTH, max = USERNAME_MAX_LENGTH, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Username can contain only letters, numbers, dots, underscores and hyphens")
    private String username;

    @Email(message = "Email must be valid")
    @Size(max = EMAIL_MAX_LENGTH, message = "Email must not exceed 100 characters")
    @Pattern(regexp = "^.*\\S.*$", message = "Email is required")
    private String email;

    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH, message = "Password must be between 6 and 100 characters")
    private String password;

    private Boolean enabled;

    private List<@Pattern(regexp = "^.*\\S.*$", message = "Role name is required") @Size(max = ROLE_NAME_MAX_LENGTH, message = "Role name must not exceed 30 characters") String> roles;

    @Valid
    private UserProfileRequest profile;

    public UpdateUserRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public UserProfileRequest getProfile() {
        return profile;
    }

    public void setProfile(UserProfileRequest profile) {
        this.profile = profile;
    }
}
