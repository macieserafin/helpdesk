package macieserafin.pl.helpdesk.config;

import macieserafin.pl.helpdesk.dto.CreateCommentRequest;
import macieserafin.pl.helpdesk.dto.TicketResponse;
import macieserafin.pl.helpdesk.dto.UpdateTicketPriorityRequest;
import macieserafin.pl.helpdesk.model.entity.UserProfile;
import macieserafin.pl.helpdesk.model.enums.TicketPriority;
import macieserafin.pl.helpdesk.model.enums.TicketStatus;
import macieserafin.pl.helpdesk.service.CategoryService;
import macieserafin.pl.helpdesk.service.TicketService;
import macieserafin.pl.helpdesk.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class SeedConfig {

    private final boolean demoSeedEnabled;
    private final boolean requireCustomPasswords;
    private final String userPassword;
    private final String agentPassword;
    private final String adminPassword;

    public SeedConfig(
            @Value("${app.demo.seed.enabled:true}") boolean demoSeedEnabled,
            @Value("${app.demo.seed.require-custom-passwords:false}") boolean requireCustomPasswords,
            @Value("${app.demo.seed.user-password:user123}") String userPassword,
            @Value("${app.demo.seed.agent-password:agent123}") String agentPassword,
            @Value("${app.demo.seed.admin-password:admin123}") String adminPassword) {
        this.demoSeedEnabled = demoSeedEnabled;
        this.requireCustomPasswords = requireCustomPasswords;
        this.userPassword = userPassword;
        this.agentPassword = agentPassword;
        this.adminPassword = adminPassword;
    }

    @Bean
    @Order(1)
    CommandLineRunner seedUsers(UserService userService) {
        return args -> {
            if (!demoSeedEnabled) {
                return;
            }
            validateDemoPasswords();

            userService.createUserIfMissing(
                    "user",
                    "user@example.com",
                    userPassword,
                    "USER",
                    new UserProfile("Jan", "Kowalski", "+48 500 100 100", "Warszawa", "Marszalkowska 10", "00-001")
            );
            userService.createUserIfMissing(
                    "agent",
                    "agent@example.com",
                    agentPassword,
                    "AGENT",
                    new UserProfile("Anna", "Nowak", "+48 500 200 200", "Krakow", "Dluga 5", "31-147")
            );
            userService.createUserIfMissing(
                    "admin",
                    "admin@example.com",
                    adminPassword,
                    "ADMIN",
                    new UserProfile("Piotr", "Zielinski", "+48 500 300 300", "Gdansk", "Grunwaldzka 20", "80-236")
            );
        };
    }

    @Bean
    @Order(2)
    CommandLineRunner seedCategories(CategoryService categoryService) {
        return args -> {
            if (!demoSeedEnabled) {
                return;
            }

            categoryService.createCategoryIfMissing("Konto", "Logowanie, dane konta i ustawienia profilu użytkownika.");
            categoryService.createCategoryIfMissing("Uprawnienia", "Dostępy, role i widoczność funkcji w aplikacji.");
            categoryService.createCategoryIfMissing("Załączniki", "Dodawanie, pobieranie i obsługa plików przy zgłoszeniach.");
            categoryService.createCategoryIfMissing("Aplikacja", "Błędy interfejsu, formularzy i nieprawidłowe działanie panelu.");
            categoryService.createCategoryIfMissing("Inne", "Zgłoszenia, które nie pasują do pozostałych kategorii.");
        };
    }

    @Bean
    @Order(3)
    CommandLineRunner seedTickets(TicketService ticketService) {
        return args -> {
            if (!demoSeedEnabled) {
                return;
            }

            seedOpenHighPriorityTicket(ticketService);
            seedAssignedInProgressTicket(ticketService);
            seedWaitingForUserTicket(ticketService);
            seedResolvedTicket(ticketService);
            seedClosedTicket(ticketService);
            seedRejectedTicket(ticketService);
            seedCancelledTicket(ticketService);
        };
    }

    private void validateDemoPasswords() {
        if (!requireCustomPasswords) {
            return;
        }

        validateCustomPassword("APP_DEMO_SEED_USER_PASSWORD", userPassword, "user123");
        validateCustomPassword("APP_DEMO_SEED_AGENT_PASSWORD", agentPassword, "agent123");
        validateCustomPassword("APP_DEMO_SEED_ADMIN_PASSWORD", adminPassword, "admin123");
    }

    private void validateCustomPassword(String variableName, String password, String unsafeDefault) {
        if (password == null || password.isBlank()) {
            throw new IllegalStateException(variableName + " is required when APP_DEMO_SEED_REQUIRE_CUSTOM_PASSWORDS=true");
        }
        if (password.equals(unsafeDefault) || password.length() < 12) {
            throw new IllegalStateException(variableName + " must be custom and at least 12 characters long");
        }
    }

    private void seedOpenHighPriorityTicket(TicketService ticketService) {
        TicketResponse ticket = createSeedTicket(
                ticketService,
                "Nie mogę zalogować się do panelu",
                "Po wpisaniu poprawnych danych logowania system wraca do ekranu logowania bez komunikatu o błędzie.",
                "Konto"
        );
        if (ticket == null) {
            return;
        }

        ticketService.updatePriority(ticket.getId(), new UpdateTicketPriorityRequest(TicketPriority.HIGH), "agent");
    }

    private void seedAssignedInProgressTicket(TicketService ticketService) {
        TicketResponse ticket = createSeedTicket(
                ticketService,
                "Brak dostępu do raportów",
                "Nie widzę zakładki z raportami, chociaż moje konto powinno mieć dostęp do tej sekcji.",
                "Uprawnienia"
        );
        if (ticket == null) {
            return;
        }

        ticketService.assignTicket(ticket.getId(), "agent");
        ticketService.updatePriority(ticket.getId(), new UpdateTicketPriorityRequest(TicketPriority.MEDIUM), "agent");
        ticketService.addComment(ticket.getId(),
                new CreateCommentRequest("Sprawdzam role i historię zmian uprawnień dla tego konta.", true),
                "agent");
    }

    private void seedWaitingForUserTicket(TicketService ticketService) {
        TicketResponse ticket = createSeedTicket(
                ticketService,
                "Nie mogę dodać załącznika",
                "Podczas dodawania pliku do zgłoszenia formularz zwraca błąd i nie zapisuje załącznika.",
                "Załączniki"
        );
        if (ticket == null) {
            return;
        }

        ticketService.assignTicket(ticket.getId(), "agent");
        ticketService.updatePriority(ticket.getId(), new UpdateTicketPriorityRequest(TicketPriority.HIGH), "agent");
        ticketService.addComment(ticket.getId(),
                new CreateCommentRequest("Proszę dosłać nazwę pliku, rozmiar i typ załącznika, który powoduje błąd.", false),
                "agent");
    }

    private void seedResolvedTicket(TicketService ticketService) {
        TicketResponse ticket = createSeedTicket(
                ticketService,
                "Formularz profilu nie zapisuje zmian",
                "Po aktualizacji danych profilu i zapisaniu formularza poprzednie wartości nadal są widoczne w panelu.",
                "Aplikacja"
        );
        if (ticket == null) {
            return;
        }

        ticketService.assignTicket(ticket.getId(), "agent");
        ticketService.updatePriority(ticket.getId(), new UpdateTicketPriorityRequest(TicketPriority.LOW), "agent");
        ticketService.addComment(ticket.getId(),
                new CreateCommentRequest("Czy problem dotyczy telefonu, adresu czy wszystkich danych profilu?", false),
                "agent");
        ticketService.addComment(ticket.getId(),
                new CreateCommentRequest("Problem dotyczy numeru telefonu i miasta.", false),
                "user");
        ticketService.updateStatus(ticket.getId(), TicketStatus.RESOLVED, "agent");
        ticketService.addComment(ticket.getId(),
                new CreateCommentRequest("Poprawka została wdrożona. Proszę potwierdzić, czy profil zapisuje się poprawnie.", false),
                "agent");
    }

    private void seedClosedTicket(TicketService ticketService) {
        TicketResponse ticket = createSeedTicket(
                ticketService,
                "Reset hasła do konta testowego",
                "Nie mogę odzyskać hasła do konta testowego używanego podczas prezentacji.",
                "Konto"
        );
        if (ticket == null) {
            return;
        }

        ticketService.assignTicket(ticket.getId(), "agent");
        ticketService.updatePriority(ticket.getId(), new UpdateTicketPriorityRequest(TicketPriority.LOW), "agent");
        ticketService.addComment(ticket.getId(),
                new CreateCommentRequest("Reset został przygotowany. Proszę potwierdzić, że konto testowe jest właściwe.", false),
                "agent");
        ticketService.addComment(ticket.getId(),
                new CreateCommentRequest("Potwierdzam, chodzi o konto do prezentacji.", false),
                "user");
        ticketService.updateStatus(ticket.getId(), TicketStatus.RESOLVED, "agent");
        ticketService.updateStatus(ticket.getId(), TicketStatus.CLOSED, "user");
    }

    private void seedRejectedTicket(TicketService ticketService) {
        TicketResponse ticket = createSeedTicket(
                ticketService,
                "Prośba o dostęp do panelu administracyjnego",
                "Chciałbym dostać pełny dostęp administracyjny do środowiska produkcyjnego.",
                "Uprawnienia"
        );
        if (ticket == null) {
            return;
        }

        ticketService.updatePriority(ticket.getId(), new UpdateTicketPriorityRequest(TicketPriority.CRITICAL), "agent");
        ticketService.updateStatus(ticket.getId(), TicketStatus.REJECTED, "agent");
        ticketService.addComment(ticket.getId(),
                new CreateCommentRequest("Zgłoszenie odrzucone: dostęp administracyjny wymaga osobnej ścieżki akceptacji.", false),
                "agent");
    }

    private void seedCancelledTicket(TicketService ticketService) {
        TicketResponse ticket = createSeedTicket(
                ticketService,
                "Duplikat zgłoszenia o problemie z logowaniem",
                "To zgłoszenie jest duplikatem wcześniejszej sprawy dotyczącej logowania.",
                "Inne"
        );
        if (ticket == null) {
            return;
        }

        ticketService.addComment(ticket.getId(),
                new CreateCommentRequest("Zamykam jako duplikat, główna sprawa jest już zgłoszona.", false),
                "user");
        ticketService.updateStatus(ticket.getId(), TicketStatus.CANCELLED, "user");
    }

    private TicketResponse createSeedTicket(TicketService ticketService, String title, String description,
                                            String categoryName) {
        if (ticketService.ticketExistsByTitle(title)) {
            return null;
        }

        return ticketService.createTicketIfMissing(
                title,
                description,
                TicketStatus.OPEN,
                TicketPriority.UNASSIGNED,
                "user",
                categoryName
        );
    }
}
