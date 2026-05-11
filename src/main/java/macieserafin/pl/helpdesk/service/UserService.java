package macieserafin.pl.helpdesk.service;

import macieserafin.pl.helpdesk.dto.UserResponse;
import macieserafin.pl.helpdesk.dto.UserProfileResponse;
import macieserafin.pl.helpdesk.model.entity.Role;
import macieserafin.pl.helpdesk.model.entity.User;
import macieserafin.pl.helpdesk.model.entity.UserProfile;
import macieserafin.pl.helpdesk.repository.RoleRepository;
import macieserafin.pl.helpdesk.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void createUserIfMissing(String username, String email, String rawPassword, String roleName,
                                    UserProfile profile) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (user.getProfile() == null && profile != null) {
                user.setProfile(profile);
            }

            return;
        }

        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));

        User user = new User(
                username,
                email,
                passwordEncoder.encode(rawPassword)
        );

        user.addRole(role);
        user.setProfile(profile);

        userRepository.save(user);
    }

    private UserResponse mapToUserResponse(User user) {
        List<String> roleNames = user.getRoles()
                .stream()
                .map(Role::getName)
                .toList();

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                roleNames,
                mapToUserProfileResponse(user.getProfile())
        );
    }

    private UserProfileResponse mapToUserProfileResponse(UserProfile profile) {
        if (profile == null) {
            return null;
        }

        return new UserProfileResponse(
                profile.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getPhoneNumber(),
                profile.getCity(),
                profile.getStreetAddress(),
                profile.getPostalCode()
        );
    }


    @Transactional(readOnly = true)
    public List<UserResponse> findAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }
}
