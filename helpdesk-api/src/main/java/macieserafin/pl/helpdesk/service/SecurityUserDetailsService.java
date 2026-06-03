package macieserafin.pl.helpdesk.service;

import macieserafin.pl.helpdesk.model.entity.Role;
import macieserafin.pl.helpdesk.model.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecurityUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public SecurityUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginIdentifierOrEmail) throws UsernameNotFoundException {
        return userService.findForAuthentication(loginIdentifierOrEmail)
                .map(this::toUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + loginIdentifierOrEmail));
    }

    private UserDetails toUserDetails(User user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getLoginIdentifier())
                .password(user.getPasswordHash())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .toArray(String[]::new))
                .disabled(!user.isEnabled())
                .build();
    }
}
