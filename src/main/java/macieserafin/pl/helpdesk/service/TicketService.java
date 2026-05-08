package macieserafin.pl.helpdesk.service;

import macieserafin.pl.helpdesk.dto.TicketResponse;
import macieserafin.pl.helpdesk.model.entity.Category;
import macieserafin.pl.helpdesk.model.entity.Ticket;
import macieserafin.pl.helpdesk.model.enums.TicketPriority;
import macieserafin.pl.helpdesk.model.enums.TicketStatus;
import macieserafin.pl.helpdesk.repository.CategoryRepository;
import macieserafin.pl.helpdesk.repository.TicketRepository;
import macieserafin.pl.helpdesk.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public TicketService(TicketRepository ticketRepository,
                         UserRepository userRepository,
                         CategoryRepository categoryRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToTicketResponse)
                .toList();
    }

    private TicketResponse mapToTicketResponse(Ticket ticket) {
        String assignedTo = ticket.getAssignedTo() == null ? null : ticket.getAssignedTo().getUsername();

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCreatedBy().getUsername(),
                assignedTo,
                ticket.getCategory().getName(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getResolvedAt(),
                ticket.getClosedAt()
        );
    }

    @Transactional
    public void createTicketIfMissing(String title, String description, TicketStatus status, TicketPriority priority,
                                      String createdByUsername, String categoryName) {
        if (!ticketRepository.existsByTitle(title)) {
            var createdBy = userRepository.findByUsername(createdByUsername)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + createdByUsername));

            Category category = categoryRepository.findByName(categoryName)
                    .orElseGet(() -> categoryRepository.save(new Category(categoryName)));

            Ticket ticket = new Ticket(
                    title,
                    description,
                    status,
                    priority,
                    createdBy,
                    category
            );

            ticketRepository.save(ticket);
        }
    }
}
