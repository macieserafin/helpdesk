package macieserafin.pl.helpdesk.config;

import macieserafin.pl.helpdesk.model.entity.UserProfile;
import macieserafin.pl.helpdesk.model.enums.TicketPriority;
import macieserafin.pl.helpdesk.model.enums.TicketStatus;
import macieserafin.pl.helpdesk.service.CategoryService;
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
    CommandLineRunner seedCategories(CategoryService categoryService) {
        return args -> {
            categoryService.createCategoryIfMissing("Konto", "Logowanie, dane konta i ustawienia profilu uzytkownika.");
            categoryService.createCategoryIfMissing("Uprawnienia", "Dostepy, role i widocznosc funkcji w aplikacji.");
            categoryService.createCategoryIfMissing("Zalaczniki", "Dodawanie, pobieranie i obsluga plikow przy zgloszeniach.");
            categoryService.createCategoryIfMissing("Aplikacja", "Bledy interfejsu, formularzy i nieprawidlowe dzialanie panelu.");
            categoryService.createCategoryIfMissing("Inne", "Zgloszenia, ktore nie pasuja do pozostalych kategorii.");
        };
    }

    /*
    @Bean
    @Order(3)
    CommandLineRunner seedTicket(TicketService ticketService) {
        return args -> {
            ticketService.createTicketIfMissing(
                    "Nie moge zalogowac sie do panelu",
                    "Po wpisaniu poprawnych danych logowania system wraca do ekranu logowania bez komunikatu o bledzie.",
                    TicketStatus.OPEN,
                    TicketPriority.UNASSIGNED,
                    "user",
                    "Konto"
            );

            ticketService.createTicketIfMissing(
                    "Brak dostepu do raportow",
                    "Nie widze zakladki z raportami, chociaz moje konto powinno miec dostep do tej sekcji.",
                    TicketStatus.OPEN,
                    TicketPriority.UNASSIGNED,
                    "user",
                    "Uprawnienia"
            );

            ticketService.createTicketIfMissing(
                    "Nie moge dodac zalacznika",
                    "Podczas dodawania pliku do zgloszenia formularz zwraca blad i nie zapisuje zalacznika.",
                    TicketStatus.OPEN,
                    TicketPriority.UNASSIGNED,
                    "user",
                    "Zalaczniki"
            );

            ticketService.createTicketIfMissing(
                    "Formularz profilu nie zapisuje zmian",
                    "Po aktualizacji danych profilu i zapisaniu formularza poprzednie wartosci nadal sa widoczne w panelu.",
                    TicketStatus.OPEN,
                    TicketPriority.UNASSIGNED,
                    "user",
                    "Aplikacja"
            );
        };
    }*/
}
