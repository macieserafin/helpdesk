package macieserafin.pl.helpdesk.dto;

public class RegisterUserRequest {
    private String username;
    private String email;
    private String password;
    private UserProfileRequest profile;

    public RegisterUserRequest() {
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

    public UserProfileRequest getProfile() {
        return profile;
    }

    public void setProfile(UserProfileRequest profile) {
        this.profile = profile;
    }
}
