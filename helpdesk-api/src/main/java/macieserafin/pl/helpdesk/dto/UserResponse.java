package macieserafin.pl.helpdesk.dto;

import java.util.List;

public class UserResponse {
    private Long id;
    private String loginIdentifier;
    private String email;
    private boolean emailVerified;
    private boolean enabled;
    private List<String> roles;
    private UserProfileResponse profile;

    public UserResponse(Long id, String loginIdentifier, String email, boolean emailVerified, boolean enabled,
                        List<String> roles, UserProfileResponse profile) {
        this.id = id;
        this.loginIdentifier = loginIdentifier;
        this.email = email;
        this.emailVerified = emailVerified;
        this.enabled = enabled;
        this.roles = roles;
        this.profile = profile;

    }

    public UserResponse(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public UserProfileResponse getProfile() {
        return profile;
    }

    public void setProfile(UserProfileResponse profile) {
        this.profile = profile;
    }
}
