package macieserafin.pl.helpdesk.service;

import macieserafin.pl.helpdesk.dto.CreateUserRequest;
import macieserafin.pl.helpdesk.dto.RegisterUserRequest;
import macieserafin.pl.helpdesk.dto.UpdateUserEnabledRequest;
import macieserafin.pl.helpdesk.dto.UpdateUserRequest;
import macieserafin.pl.helpdesk.dto.UserProfileRequest;
import macieserafin.pl.helpdesk.dto.UserProfileResponse;
import macieserafin.pl.helpdesk.dto.UserResponse;
import macieserafin.pl.helpdesk.model.entity.Role;
import macieserafin.pl.helpdesk.model.entity.User;
import macieserafin.pl.helpdesk.model.entity.UserProfile;
import macieserafin.pl.helpdesk.repository.RoleRepository;
import macieserafin.pl.helpdesk.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    public Optional<User> findByLoginIdentifier(String loginIdentifier) {
        return userRepository.findByLoginIdentifier(loginIdentifier);
    }

    @Transactional(readOnly = true)
    public Optional<User> findForAuthentication(String loginIdentifierOrEmail) {
        if (!hasText(loginIdentifierOrEmail)) {
            return Optional.empty();
        }

        String value = loginIdentifierOrEmail.trim();
        Optional<User> byLoginIdentifier = userRepository.findByLoginIdentifier(value);
        if (byLoginIdentifier.isPresent()) {
            return byLoginIdentifier;
        }

        return userRepository.findByEmailIgnoreCase(value)
                .filter(this::canLoginWithEmail);
    }

    @Transactional(readOnly = true)
    public UserResponse findCurrentUser(String loginIdentifier) {
        return mapToUserResponse(findUser(loginIdentifier));
    }

    @Transactional(readOnly = true)
    public UserResponse findUserById(Long id) {
        return mapToUserResponse(findUser(id));
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        String loginIdentifier = requireText(request.getLoginIdentifier(), "Login identifier is required");
        String email = requireText(request.getEmail(), "Email is required");
        String password = requireText(request.getPassword(), "Password is required");

        if (userRepository.existsByLoginIdentifier(loginIdentifier)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Login identifier already exists: " + loginIdentifier);
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists: " + email);
        }

        User user = new User(loginIdentifier, email, passwordEncoder.encode(password));
        user.setEnabled(request.getEnabled() == null || request.getEnabled());
        setRoles(user, request.getRoles());
        user.setProfile(mapToUserProfile(request.getProfile()));

        return mapToUserResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse registerUser(RegisterUserRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setLoginIdentifier(request.getLoginIdentifier());
        createUserRequest.setEmail(request.getEmail());
        createUserRequest.setPassword(request.getPassword());
        createUserRequest.setEnabled(true);
        createUserRequest.setRoles(List.of("USER"));
        createUserRequest.setProfile(request.getProfile());

        return createUser(createUserRequest);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        User user = findUser(id);

        if (request.getLoginIdentifier() != null) {
            String loginIdentifier = requireText(request.getLoginIdentifier(), "Login identifier is required");
            userRepository.findByLoginIdentifier(loginIdentifier)
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Login identifier already exists: " + loginIdentifier);
                    });
            user.setLoginIdentifier(loginIdentifier);
        }

        if (request.getEmail() != null) {
            String email = requireText(request.getEmail(), "Email is required");
            userRepository.findByEmailIgnoreCase(email)
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists: " + email);
                    });
            user.setEmail(email);
        }

        if (request.getPassword() != null) {
            String password = requireText(request.getPassword(), "Password is required");
            user.setPasswordHash(passwordEncoder.encode(password));
        }

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        if (request.getRoles() != null) {
            setRoles(user, request.getRoles());
        }

        if (request.getProfile() != null) {
            updateProfile(user, request.getProfile());
        }

        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateCurrentUserProfile(String loginIdentifier, UserProfileRequest request) {
        User user = findUser(loginIdentifier);
        updateProfile(user, request);

        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateEnabled(Long id, UpdateUserEnabledRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        if (request.getEnabled() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enabled is required");
        }

        User user = findUser(id);
        user.setEnabled(request.getEnabled());

        return mapToUserResponse(user);
    }

    @Transactional
    public void createUserIfMissing(String loginIdentifier, String email, String rawPassword, String roleName,
                                    UserProfile profile) {
        Optional<User> existingUser = userRepository.findByLoginIdentifier(loginIdentifier);
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
                loginIdentifier,
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
                user.getLoginIdentifier(),
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

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
    }

    private User findUser(String loginIdentifier) {
        return userRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found: " + loginIdentifier));
    }

    private boolean canLoginWithEmail(User user) {
        return hasRole(user, "USER") && !hasRole(user, "AGENT") && !hasRole(user, "ADMIN");
    }

    private boolean hasRole(User user, String roleName) {
        return user.getRoles().stream().anyMatch(role -> role.getName().equals(roleName));
    }

    private void setRoles(User user, List<String> roleNames) {
        Set<Role> roles = normalizeRoleNames(roleNames)
                .stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseGet(() -> roleRepository.save(new Role(roleName))))
                .collect(Collectors.toSet());

        user.getRoles().forEach(role -> role.getUsers().remove(user));
        user.getRoles().clear();
        roles.forEach(user::addRole);
    }

    private Set<String> normalizeRoleNames(List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return Set.of("USER");
        }

        Set<String> normalizedRoleNames = roleNames.stream()
                .map(this::normalizeRoleName)
                .collect(Collectors.toSet());

        if (normalizedRoleNames.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one role is required");
        }

        return normalizedRoleNames;
    }

    private String normalizeRoleName(String roleName) {
        String value = requireText(roleName, "Role name is required").toUpperCase();
        if (value.startsWith("ROLE_")) {
            value = value.substring("ROLE_".length());
        }

        return value;
    }

    private UserProfile mapToUserProfile(UserProfileRequest request) {
        if (request == null) {
            return null;
        }

        return new UserProfile(
                trimToNull(request.getFirstName()),
                trimToNull(request.getLastName()),
                trimToNull(request.getPhoneNumber()),
                trimToNull(request.getCity()),
                trimToNull(request.getStreetAddress()),
                trimToNull(request.getPostalCode())
        );
    }

    private void updateProfile(User user, UserProfileRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile is required");
        }

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile(null, null, null, null, null, null);
            user.setProfile(profile);
        }

        if (request.getFirstName() != null) {
            profile.setFirstName(trimToNull(request.getFirstName()));
        }
        if (request.getLastName() != null) {
            profile.setLastName(trimToNull(request.getLastName()));
        }
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(trimToNull(request.getPhoneNumber()));
        }
        if (request.getCity() != null) {
            profile.setCity(trimToNull(request.getCity()));
        }
        if (request.getStreetAddress() != null) {
            profile.setStreetAddress(trimToNull(request.getStreetAddress()));
        }
        if (request.getPostalCode() != null) {
            profile.setPostalCode(trimToNull(request.getPostalCode()));
        }
    }

    private String requireText(String value, String message) {
        if (!hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        return value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trimToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }
}
