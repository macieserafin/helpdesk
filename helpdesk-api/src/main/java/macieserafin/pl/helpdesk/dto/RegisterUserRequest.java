package macieserafin.pl.helpdesk.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static macieserafin.pl.helpdesk.contract.ApiContract.EMAIL_MAX_LENGTH;
import static macieserafin.pl.helpdesk.contract.ApiContract.PASSWORD_MAX_LENGTH;
import static macieserafin.pl.helpdesk.contract.ApiContract.PASSWORD_MIN_LENGTH;
import static macieserafin.pl.helpdesk.contract.ApiContract.LOGIN_IDENTIFIER_MAX_LENGTH;
import static macieserafin.pl.helpdesk.contract.ApiContract.LOGIN_IDENTIFIER_MIN_LENGTH;

public class RegisterUserRequest {
    @NotBlank(message = "Login identifier is required")
    @Size(min = LOGIN_IDENTIFIER_MIN_LENGTH, max = LOGIN_IDENTIFIER_MAX_LENGTH, message = "Login identifier must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Login identifier can contain only letters, numbers, dots, underscores and hyphens")
    private String loginIdentifier;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = EMAIL_MAX_LENGTH, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH, message = "Password must be between 6 and 100 characters")
    private String password;

    @Valid
    private UserProfileRequest profile;

    public RegisterUserRequest() {
    }

    public String getLoginIdentifier() {
        return loginIdentifier;
    }

    public void setLoginIdentifier(String loginIdentifier) {
        this.loginIdentifier = loginIdentifier;
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

    public UserProfileRequest getProfile() {
        return profile;
    }

    public void setProfile(UserProfileRequest profile) {
        this.profile = profile;
    }
}
