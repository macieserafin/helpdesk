package macieserafin.pl.helpdesk.service;

import macieserafin.pl.helpdesk.model.Role;
import macieserafin.pl.helpdesk.model.User;

import java.util.Optional;

public interface UserService {

    Optional<User> findByUsername(String username);

    void createUserIfMissing(String username, String rawPassword, Role role);
}
