package macieserafin.pl.helpdesk.service;

import macieserafin.pl.helpdesk.dto.CommentResponse;
import macieserafin.pl.helpdesk.dto.CreateCommentRequest;
import macieserafin.pl.helpdesk.dto.CreateTicketRequest;
import macieserafin.pl.helpdesk.dto.TicketHistoryResponse;
import macieserafin.pl.helpdesk.dto.TicketResponse;
import macieserafin.pl.helpdesk.model.entity.Comment;
import macieserafin.pl.helpdesk.model.entity.Category;
import macieserafin.pl.helpdesk.model.entity.Ticket;
import macieserafin.pl.helpdesk.model.entity.TicketHistory;
import macieserafin.pl.helpdesk.model.entity.User;
import macieserafin.pl.helpdesk.model.enums.TicketHistoryActionType;
import macieserafin.pl.helpdesk.model.enums.TicketPriority;
import macieserafin.pl.helpdesk.model.enums.TicketStatus;
import macieserafin.pl.helpdesk.repository.CategoryRepository;
import macieserafin.pl.helpdesk.repository.CommentRepository;
import macieserafin.pl.helpdesk.repository.TicketHistoryRepository;
import macieserafin.pl.helpdesk.repository.TicketRepository;
import macieserafin.pl.helpdesk.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CommentRepository commentRepository;
    private final TicketHistoryRepository ticketHistoryRepository;

    public TicketService(TicketRepository ticketRepository,
                         UserRepository userRepository,
                         CategoryRepository categoryRepository,
                         CommentRepository commentRepository,
                         TicketHistoryRepository ticketHistoryRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.commentRepository = commentRepository;
        this.ticketHistoryRepository = ticketHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToTicketResponse)
                .toList();
    }

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, String username) {
        User createdBy = findUser(username);
        Category category = categoryRepository.findByName(requireText(request.getCategory(), "Category is required"))
                .orElseGet(() -> categoryRepository.save(new Category(request.getCategory().trim())));
        TicketPriority priority = request.getPriority() == null ? TicketPriority.MEDIUM : request.getPriority();

        Ticket ticket = new Ticket(
                requireText(request.getTitle(), "Title is required"),
                requireText(request.getDescription(), "Description is required"),
                TicketStatus.OPEN,
                priority,
                createdBy,
                category
        );

        Ticket savedTicket = ticketRepository.save(ticket);
        saveHistory(savedTicket, createdBy, TicketHistoryActionType.TICKET_CREATED, null, TicketStatus.OPEN,
                null, null, null, null, "Ticket created");

        return mapToTicketResponse(savedTicket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getMyTickets(String username) {
        User user = findUser(username);

        return ticketRepository.findByCreatedByIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToTicketResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicket(Long id, String username) {
        User user = findUser(username);
        Ticket ticket = findTicket(id);
        checkCanViewTicket(user, ticket);

        return mapToTicketResponse(ticket);
    }

    @Transactional
    public TicketResponse assignTicket(Long id, String username) {
        User agent = findUser(username);
        checkHasAnyRole(agent, "AGENT", "ADMIN");

        Ticket ticket = findTicket(id);
        User oldAssignedTo = ticket.getAssignedTo();
        TicketStatus oldStatus = ticket.getStatus();

        ticket.setAssignedTo(agent);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        saveHistory(ticket, agent, TicketHistoryActionType.ASSIGNED_CHANGED, oldStatus, TicketStatus.IN_PROGRESS,
                null, null, oldAssignedTo, agent, "Ticket assigned");

        return mapToTicketResponse(ticket);
    }

    @Transactional
    public TicketResponse updateStatus(Long id, TicketStatus newStatus, String username) {
        if (newStatus == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
        }

        User actor = findUser(username);
        Ticket ticket = findTicket(id);
        TicketStatus oldStatus = ticket.getStatus();

        if (oldStatus == newStatus) {
            return mapToTicketResponse(ticket);
        }

        checkCanChangeStatus(actor, ticket, newStatus);

        ticket.setStatus(newStatus);
        if (newStatus == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }
        if (newStatus == TicketStatus.CLOSED) {
            ticket.setClosedAt(LocalDateTime.now());
        }

        saveHistory(ticket, actor, actionForStatus(newStatus), oldStatus, newStatus,
                null, null, null, null, "Status changed");

        return mapToTicketResponse(ticket);
    }

    @Transactional
    public CommentResponse addComment(Long ticketId, CreateCommentRequest request, String username) {
        User author = findUser(username);
        Ticket ticket = findTicket(ticketId);
        checkCanViewTicket(author, ticket);

        if (request.isInternal() && !hasAnyRole(author, "AGENT", "ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only agents and admins can add internal comments");
        }

        Comment comment = new Comment(
                ticket,
                author,
                requireText(request.getContent(), "Comment content is required"),
                request.isInternal()
        );

        Comment savedComment = commentRepository.save(comment);
        saveHistory(ticket, author, TicketHistoryActionType.COMMENT_ADDED, null, null,
                null, null, null, null, "Comment added");

        return mapToCommentResponse(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long ticketId, String username) {
        User user = findUser(username);
        Ticket ticket = findTicket(ticketId);
        checkCanViewTicket(user, ticket);
        boolean staff = hasAnyRole(user, "AGENT", "ADMIN");

        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .filter(comment -> staff || !comment.isInternal())
                .map(this::mapToCommentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketHistoryResponse> getHistory(Long ticketId, String username) {
        User user = findUser(username);
        Ticket ticket = findTicket(ticketId);
        checkCanViewTicket(user, ticket);

        return ticketHistoryRepository.findByTicketIdOrderByChangedAtAsc(ticketId)
                .stream()
                .map(this::mapToTicketHistoryResponse)
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

    private CommentResponse mapToCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTicket().getId(),
                comment.getAuthor().getUsername(),
                comment.getContent(),
                comment.isInternal(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    private TicketHistoryResponse mapToTicketHistoryResponse(TicketHistory history) {
        String oldAssignedTo = history.getOldAssignedTo() == null ? null : history.getOldAssignedTo().getUsername();
        String newAssignedTo = history.getNewAssignedTo() == null ? null : history.getNewAssignedTo().getUsername();

        return new TicketHistoryResponse(
                history.getId(),
                history.getTicket().getId(),
                history.getChangedBy().getUsername(),
                history.getActionType(),
                history.getOldStatus(),
                history.getNewStatus(),
                history.getOldPriority(),
                history.getNewPriority(),
                oldAssignedTo,
                newAssignedTo,
                history.getNote(),
                history.getChangedAt()
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

    private void checkCanChangeStatus(User actor, Ticket ticket, TicketStatus newStatus) {
        if (newStatus == TicketStatus.CLOSED) {
            if (ticket.getStatus() != TicketStatus.RESOLVED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only resolved tickets can be closed");
            }
            if (!isTicketOwner(actor, ticket) && !hasRole(actor, "ADMIN")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ticket owner or admin can close ticket");
            }
            return;
        }

        checkHasAnyRole(actor, "AGENT", "ADMIN");
    }

    private TicketHistoryActionType actionForStatus(TicketStatus status) {
        if (status == TicketStatus.RESOLVED) {
            return TicketHistoryActionType.TICKET_RESOLVED;
        }
        if (status == TicketStatus.CLOSED) {
            return TicketHistoryActionType.TICKET_CLOSED;
        }
        return TicketHistoryActionType.STATUS_CHANGED;
    }

    private void saveHistory(Ticket ticket, User changedBy, TicketHistoryActionType actionType,
                             TicketStatus oldStatus, TicketStatus newStatus,
                             TicketPriority oldPriority, TicketPriority newPriority,
                             User oldAssignedTo, User newAssignedTo, String note) {
        TicketHistory history = new TicketHistory(ticket, changedBy, actionType);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setOldPriority(oldPriority);
        history.setNewPriority(newPriority);
        history.setOldAssignedTo(oldAssignedTo);
        history.setNewAssignedTo(newAssignedTo);
        history.setNote(note);

        ticketHistoryRepository.save(history);
    }

    private Ticket findTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found: " + id));
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + username));
    }

    private void checkCanViewTicket(User user, Ticket ticket) {
        if (isTicketOwner(user, ticket) || isAssignedAgent(user, ticket) || hasAnyRole(user, "AGENT", "ADMIN")) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No access to ticket");
    }

    private void checkHasAnyRole(User user, String... roles) {
        if (!hasAnyRole(user, roles)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Missing role");
        }
    }

    private boolean hasAnyRole(User user, String... roles) {
        for (String role : roles) {
            if (hasRole(user, role)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasRole(User user, String role) {
        return user.getRoles()
                .stream()
                .anyMatch(userRole -> userRole.getName().equals(role));
    }

    private boolean isTicketOwner(User user, Ticket ticket) {
        return ticket.getCreatedBy().getId().equals(user.getId());
    }

    private boolean isAssignedAgent(User user, Ticket ticket) {
        return ticket.getAssignedTo() != null
                && ticket.getAssignedTo().getId().equals(user.getId());
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        return value.trim();
    }
}
