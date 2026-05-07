package macieserafin.pl.helpdesk.config;

import macieserafin.pl.helpdesk.model.Role;
import macieserafin.pl.helpdesk.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserSeedConfig {

    @Bean
    CommandLineRunner seedUsers(UserService userService) {
        return args -> {
            userService.createUserIfMissing("user", "user123", Role.USER);
            userService.createUserIfMissing("agent", "agent123", Role.AGENT);
            userService.createUserIfMissing("admin", "admin123", Role.ADMIN);
        };
    }
}
