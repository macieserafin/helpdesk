package macieserafin.pl.helpdesk.config;

import macieserafin.pl.helpdesk.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserSeedConfig {

    @Bean
    CommandLineRunner seedUsers(UserService userService) {
        return args -> {
            userService.createUserIfMissing("user", "user@example.com", "user123", "USER");
            userService.createUserIfMissing("agent", "agent@example.com", "agent123", "AGENT");
            userService.createUserIfMissing("admin", "admin@example.com", "admin123", "ADMIN");
        };
    }
}
