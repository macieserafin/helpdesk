package macieserafin.pl.helpdesk.dto;

import java.util.List;

public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private boolean enabled;
    private List<String> roles;
    private UserProfileResponse profile;

    public UserResponse(Long id, String username, String email, boolean enabled, List<String> roles,
                        UserProfileResponse profile) {
        this.id = id;
        this.username = username;
        this.email = email;
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
