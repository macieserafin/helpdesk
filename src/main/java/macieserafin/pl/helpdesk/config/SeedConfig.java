package macieserafin.pl.helpdesk.config;

import macieserafin.pl.helpdesk.model.enums.TicketPriority;
import macieserafin.pl.helpdesk.model.enums.TicketStatus;
import macieserafin.pl.helpdesk.service.TicketService;
import macieserafin.pl.helpdesk.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class SeedConfig {

    @Bean
    @Order(1)
    CommandLineRunner seedUsers(UserService userService) {
        return args -> {
            userService.createUserIfMissing("user", "user@example.com", "user123", "USER");
            userService.createUserIfMissing("agent", "agent@example.com", "agent123", "AGENT");
            userService.createUserIfMissing("admin", "admin@example.com", "admin123", "ADMIN");
        };
    }

    @Bean
    @Order(2)
    CommandLineRunner seedTicket(TicketService ticketService) {
        return args -> {
            ticketService.createTicketIfMissing(
                    "Problem z logowaniem",
                    "Nie moge sie zalogowac naprawcie gnoje.",
                    TicketStatus.OPEN,
                    TicketPriority.HIGH,
                    "user",
                    "Konto"
            );

            ticketService.createTicketIfMissing(
                    "Brak dostepu do raportow",
                    "Agent nie widzi zakladki z raportami mimo przypisanej roli.",
                    TicketStatus.IN_PROGRESS,
                    TicketPriority.MEDIUM,
                    "agent",
                    "Uprawnienia"
            );

            ticketService.createTicketIfMissing(
                    "Nie dziala zalacznik",
                    "Podczas dodawania zalacznika formularz zwraca blad serwera.",
                    TicketStatus.OPEN,
                    TicketPriority.CRITICAL,
                    "admin",
                    "Zalaczniki"
            );
        };
    }
}
