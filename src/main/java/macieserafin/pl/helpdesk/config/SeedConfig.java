package macieserafin.pl.helpdesk.config;

import macieserafin.pl.helpdesk.model.entity.UserProfile;
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
            userService.createUserIfMissing(
                    "user",
                    "user@example.com",
                    "user123",
                    "USER",
                    new UserProfile("Jan", "Kowalski", "+48 500 100 100", "Warszawa", "Marszalkowska 10", "00-001")
            );
            userService.createUserIfMissing(
                    "agent",
                    "agent@example.com",
                    "agent123",
                    "AGENT",
                    new UserProfile("Anna", "Nowak", "+48 500 200 200", "Krakow", "Dluga 5", "31-147")
            );
            userService.createUserIfMissing(
                    "admin",
                    "admin@example.com",
                    "admin123",
                    "ADMIN",
                    new UserProfile("Piotr", "Zielinski", "+48 500 300 300", "Gdansk", "Grunwaldzka 20", "80-236")
            );
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
                    "Nie widze zakladki z raportami mimo aktywnego konta.",
                    TicketStatus.IN_PROGRESS,
                    TicketPriority.MEDIUM,
                    "user",
                    "Uprawnienia"
            );

            ticketService.createTicketIfMissing(
                    "Nie dziala zalacznik",
                    "Podczas dodawania zalacznika formularz zwraca blad serwera.",
                    TicketStatus.OPEN,
                    TicketPriority.CRITICAL,
                    "user",
                    "Zalaczniki"
            );
        };
    }
}
