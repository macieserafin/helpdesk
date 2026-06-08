package macieserafin.pl.helpdesk.service;

import macieserafin.pl.helpdesk.dto.AttachmentDownload;
import macieserafin.pl.helpdesk.dto.AttachmentResponse;
import macieserafin.pl.helpdesk.dto.CommentResponse;
import macieserafin.pl.helpdesk.dto.CreateCommentRequest;
import macieserafin.pl.helpdesk.dto.CreateTicketRequest;
import macieserafin.pl.helpdesk.dto.TicketFilterRequest;
import macieserafin.pl.helpdesk.dto.TicketHistoryResponse;
import macieserafin.pl.helpdesk.dto.UpdateTicketPriorityRequest;
import macieserafin.pl.helpdesk.dto.UpdateTicketRequest;
import macieserafin.pl.helpdesk.dto.TicketResponse;
import macieserafin.pl.helpdesk.dto.UserDashboardActivityResponse;
import macieserafin.pl.helpdesk.dto.UserDashboardResponse;
import macieserafin.pl.helpdesk.model.entity.Attachment;
import macieserafin.pl.helpdesk.model.entity.Comment;
import macieserafin.pl.helpdesk.model.entity.Category;
import macieserafin.pl.helpdesk.model.entity.Ticket;
import macieserafin.pl.helpdesk.model.entity.TicketHistory;
import macieserafin.pl.helpdesk.model.entity.User;
import macieserafin.pl.helpdesk.model.enums.TicketHistoryActionType;
import macieserafin.pl.helpdesk.model.enums.TicketPriority;
import macieserafin.pl.helpdesk.model.enums.TicketStatus;
import macieserafin.pl.helpdesk.repository.AttachmentRepository;
import macieserafin.pl.helpdesk.repository.CategoryRepository;
import macieserafin.pl.helpdesk.repository.CommentRepository;
import macieserafin.pl.helpdesk.repository.TicketHistoryRepository;
import macieserafin.pl.helpdesk.repository.TicketRepository;
import macieserafin.pl.helpdesk.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class TicketService {
    private static final Set<TicketStatus> TERMINAL_STATUSES = EnumSet.of(
            TicketStatus.CLOSED,
            TicketStatus.REJECTED,
            TicketStatus.CANCELLED
    );

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_STATUS_TRANSITIONS = Map.of(
            TicketStatus.OPEN, EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.REJECTED, TicketStatus.CANCELLED),
            TicketStatus.IN_PROGRESS, EnumSet.of(TicketStatus.WAITING_FOR_USER, TicketStatus.RESOLVED,
                    TicketStatus.REJECTED, TicketStatus.CANCELLED),
            TicketStatus.WAITING_FOR_USER, EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED,
                    TicketStatus.CANCELLED),
            TicketStatus.RESOLVED, EnumSet.of(TicketStatus.CLOSED, TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED),
            TicketStatus.CLOSED, EnumSet.noneOf(TicketStatus.class),
            TicketStatus.REJECTED, EnumSet.noneOf(TicketStatus.class),
            TicketStatus.CANCELLED, EnumSet.noneOf(TicketStatus.class)
    );

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final Path attachmentStorageRoot;

    public TicketService(TicketRepository ticketRepository,
                         UserRepository userRepository,
                         CategoryRepository categoryRepository,
                         CommentRepository commentRepository,
                         AttachmentRepository attachmentRepository,
                         TicketHistoryRepository ticketHistoryRepository,
                         @Value("${app.attachments.storage-dir:uploads/attachments}") String attachmentStorageDir) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.commentRepository = commentRepository;
        this.attachmentRepository = attachmentRepository;
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.attachmentStorageRoot = Paths.get(attachmentStorageDir).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> getTickets(TicketFilterRequest filter, Pageable pageable) {
        return ticketRepository.findAll(buildTicketSpecification(filter, null), pageable)
                .map(this::mapToTicketResponse);
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> getAgentTickets(String loginIdentifier, TicketFilterRequest filter, Pageable pageable) {
        User agent = findUser(loginIdentifier);
        checkHasAnyRole(agent, "AGENT", "ADMIN");

        return ticketRepository.findAll(buildTicketSpecification(filter, null), pageable)
                .map(this::mapToTicketResponse);
    }

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, String loginIdentifier) {
        User createdBy = findUser(loginIdentifier);
        checkCanCreateTicket(createdBy);

        Category category = findActiveCategory(request.getCategory());

        Ticket ticket = new Ticket(
                requireText(request.getTitle(), "Title is required"),
                requireText(request.getDescription(), "Description is required"),
                TicketStatus.OPEN,
                TicketPriority.UNASSIGNED,
                createdBy,
                category
        );

        Ticket savedTicket = ticketRepository.save(ticket);
        saveHistory(savedTicket, createdBy, TicketHistoryActionType.TICKET_CREATED, null, TicketStatus.OPEN,
                null, null, null, null, "Ticket created");

        return mapToTicketResponse(savedTicket);
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> getMyTickets(String loginIdentifier, TicketFilterRequest filter, Pageable pageable) {
        User user = findUser(loginIdentifier);

        return ticketRepository.findAll(buildTicketSpecification(filter, user.getId()), pageable)
                .map(this::mapToTicketResponse);
    }

    @Transactional(readOnly = true)
    public UserDashboardResponse getUserDashboard(String loginIdentifier) {
        User user = findUser(loginIdentifier);
        List<Ticket> tickets = ticketRepository.findByCreatedByIdOrderByCreatedAtDesc(user.getId());
        Map<TicketStatus, Long> statusBreakdown = buildStatusBreakdown(tickets);
        List<Ticket> latestTickets = tickets.stream()
                .sorted(Comparator.comparing(this::lastActivityAt).reversed())
                .limit(5)
                .toList();
        List<Ticket> requiresUserAction = tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.WAITING_FOR_USER
                        || ticket.getStatus() == TicketStatus.RESOLVED)
                .sorted(Comparator.comparing(this::lastActivityAt).reversed())
                .limit(5)
                .toList();

        LocalDateTime lastUpdatedAt = tickets.stream()
                .map(this::lastActivityAt)
                .max(Comparator.naturalOrder())
                .orElse(null);

        return new UserDashboardResponse(
                tickets.size(),
                countStatus(tickets, TicketStatus.OPEN, TicketStatus.IN_PROGRESS, TicketStatus.WAITING_FOR_USER),
                countStatus(tickets, TicketStatus.OPEN, TicketStatus.IN_PROGRESS),
                countStatus(tickets, TicketStatus.WAITING_FOR_USER),
                countStatus(tickets, TicketStatus.RESOLVED),
                countStatus(tickets, TicketStatus.CLOSED),
                lastUpdatedAt,
                statusBreakdown,
                latestTickets.stream().map(this::mapToTicketResponse).toList(),
                requiresUserAction.stream().map(this::mapToTicketResponse).toList(),
                recentUserActivity(tickets)
        );
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicket(Long id, String loginIdentifier) {
        User user = findUser(loginIdentifier);
        Ticket ticket = findTicket(id);
        checkCanViewTicket(user, ticket);

        return mapToTicketResponse(ticket);
    }

    @Transactional
    public TicketResponse updateTicket(Long id, UpdateTicketRequest request, String loginIdentifier) {
        User actor = findUser(loginIdentifier);
        Ticket ticket = findTicket(id);
        checkCanEditTicket(actor, ticket);

        String newTitle = requireText(request.getTitle(), "Title is required");
        String newDescription = requireText(request.getDescription(), "Description is required");
        Category newCategory = findActiveCategory(request.getCategory());

        if (!ticket.getTitle().equals(newTitle)) {
            String oldTitle = ticket.getTitle();
            ticket.setTitle(newTitle);
            saveHistory(ticket, actor, TicketHistoryActionType.TICKET_UPDATED,
                    null, null, null, null, null, null,
                    "Title changed: " + oldTitle + " -> " + newTitle);
        }

        if (!ticket.getDescription().equals(newDescription)) {
            ticket.setDescription(newDescription);
            saveHistory(ticket, actor, TicketHistoryActionType.TICKET_UPDATED,
                    null, null, null, null, null, null, "Description changed");
        }

        if (!ticket.getCategory().getId().equals(newCategory.getId())) {
            String oldCategory = ticket.getCategory().getName();
            ticket.setCategory(newCategory);
            saveHistory(ticket, actor, TicketHistoryActionType.TICKET_UPDATED,
                    null, null, null, null, null, null,
                    "Category changed: " + oldCategory + " -> " + newCategory.getName());
        }

        return mapToTicketResponse(ticket);
    }

    @Transactional
    public TicketResponse updatePriority(Long id, UpdateTicketPriorityRequest request, String loginIdentifier) {
        if (request.getPriority() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Priority is required");
        }
        if (request.getPriority() == TicketPriority.UNASSIGNED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Priority must be assigned");
        }

        User agent = findUser(loginIdentifier);
        checkCanAssignPriority(agent);

        Ticket ticket = findTicket(id);
        TicketPriority oldPriority = ticket.getPriority();
        TicketPriority newPriority = request.getPriority();

        if (oldPriority == newPriority) {
            return mapToTicketResponse(ticket);
        }

        ticket.setPriority(newPriority);
        saveHistory(ticket, agent, TicketHistoryActionType.PRIORITY_CHANGED, null, null,
                oldPriority, newPriority, null, null, "Priority changed");

        return mapToTicketResponse(ticket);
    }

    @Transactional
    public TicketResponse assignTicket(Long id, String loginIdentifier) {
        User agent = findUser(loginIdentifier);
        checkCanTakeTicket(agent);

        Ticket ticket = findTicket(id);
        User oldAssignedTo = ticket.getAssignedTo();
        TicketStatus oldStatus = ticket.getStatus();

        if (isTerminalStatus(oldStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Terminal tickets cannot be assigned");
        }

        if (oldAssignedTo != null && oldAssignedTo.getId().equals(agent.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket is already assigned to this agent");
        }

        ticket.setAssignedTo(agent);
        TicketStatus newStatus = oldStatus == TicketStatus.OPEN ? TicketStatus.IN_PROGRESS : oldStatus;
        ticket.setStatus(newStatus);
        saveHistory(ticket, agent, TicketHistoryActionType.ASSIGNED_CHANGED,
                oldStatus == newStatus ? null : oldStatus,
                oldStatus == newStatus ? null : newStatus,
                null, null, oldAssignedTo, agent, "Ticket assigned");

        return mapToTicketResponse(ticket);
    }

    @Transactional
    public TicketResponse updateStatus(Long id, TicketStatus newStatus, String loginIdentifier) {
        if (newStatus == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
        }

        User actor = findUser(loginIdentifier);
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
        if (oldStatus == TicketStatus.RESOLVED && newStatus == TicketStatus.IN_PROGRESS) {
            ticket.setResolvedAt(null);
        }
        if (newStatus == TicketStatus.CLOSED) {
            ticket.setClosedAt(LocalDateTime.now());
        }

        saveHistory(ticket, actor, actionForStatus(newStatus), oldStatus, newStatus,
                null, null, null, null, "Status changed");

        return mapToTicketResponse(ticket);
    }

    @Transactional
    public CommentResponse addComment(Long ticketId, CreateCommentRequest request, String loginIdentifier) {
        User author = findUser(loginIdentifier);
        Ticket ticket = findTicket(ticketId);
        checkCanAddComment(author, ticket, request.isInternal());

        Comment comment = new Comment(
                ticket,
                author,
                requireText(request.getContent(), "Comment content is required"),
                request.isInternal()
        );

        Comment savedComment = commentRepository.save(comment);
        TicketStatus oldStatus = ticket.getStatus();
        TicketStatus newStatus = statusAfterComment(ticket, author, request.isInternal());
        if (oldStatus != newStatus) {
            ticket.setStatus(newStatus);
        }

        saveHistory(ticket, author, TicketHistoryActionType.COMMENT_ADDED,
                oldStatus == newStatus ? null : oldStatus,
                oldStatus == newStatus ? null : newStatus,
                null, null, null, null, "Comment added");

        return mapToCommentResponse(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long ticketId, String loginIdentifier) {
        User user = findUser(loginIdentifier);
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
    public List<TicketHistoryResponse> getHistory(Long ticketId, String loginIdentifier) {
        User user = findUser(loginIdentifier);
        Ticket ticket = findTicket(ticketId);
        checkCanViewTicket(user, ticket);

        return ticketHistoryRepository.findByTicketIdOrderByChangedAtAsc(ticketId)
                .stream()
                .map(this::mapToTicketHistoryResponse)
                .toList();
    }

    @Transactional
    public AttachmentResponse addAttachment(Long ticketId, Long commentId, MultipartFile file, String loginIdentifier) {
        User uploadedBy = findUser(loginIdentifier);
        Ticket ticket = findTicket(ticketId);
        checkCanViewTicket(uploadedBy, ticket);
        validateAttachmentFile(file);

        Comment comment = null;
        if (commentId != null) {
            comment = findComment(commentId);
            if (!comment.getTicket().getId().equals(ticket.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment does not belong to ticket");
            }
            if (comment.isInternal() && !isStaff(uploadedBy)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Only agents and admins can attach files to internal comments");
            }
        }

        String originalFileName = sanitizeFileName(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + "-" + originalFileName;
        Path ticketDirectory = attachmentStorageRoot.resolve(ticket.getId().toString()).normalize();
        Path storedFilePath = ticketDirectory.resolve(storedFileName).normalize();

        if (!storedFilePath.startsWith(ticketDirectory)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name");
        }

        try {
            Files.createDirectories(ticketDirectory);
            Files.copy(file.getInputStream(), storedFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not store attachment", exception);
        }

        Attachment attachment = new Attachment(ticket, uploadedBy, originalFileName, storedFilePath.toString());
        attachment.setComment(comment);
        attachment.setContentType(resolveContentType(file));
        attachment.setFileSize(file.getSize());

        Attachment savedAttachment = attachmentRepository.save(attachment);
        saveHistory(ticket, uploadedBy, TicketHistoryActionType.ATTACHMENT_ADDED,
                null, null, null, null, null, null, "Attachment added: " + originalFileName);

        return mapToAttachmentResponse(savedAttachment);
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachments(Long ticketId, String loginIdentifier) {
        User user = findUser(loginIdentifier);
        Ticket ticket = findTicket(ticketId);
        checkCanViewTicket(user, ticket);
        boolean staff = isStaff(user);

        return attachmentRepository.findByTicketIdOrderByUploadedAtAsc(ticketId)
                .stream()
                .filter(attachment -> staff
                        || attachment.getComment() == null
                        || !attachment.getComment().isInternal())
                .map(this::mapToAttachmentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AttachmentDownload downloadAttachment(Long ticketId, Long attachmentId, String loginIdentifier) {
        User user = findUser(loginIdentifier);
        Ticket ticket = findTicket(ticketId);
        checkCanViewTicket(user, ticket);

        Attachment attachment = findAttachment(ticketId, attachmentId);
        if (attachment.getComment() != null && attachment.getComment().isInternal() && !isStaff(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No access to internal attachment");
        }

        Path filePath = Paths.get(attachment.getFilePath()).toAbsolutePath().normalize();
        if (!filePath.startsWith(attachmentStorageRoot) || !Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment file not found");
        }

        Resource resource = new FileSystemResource(filePath);

        return new AttachmentDownload(resource, attachment.getFileName(),
                resolveContentType(attachment), attachment.getFileSize());
    }

    @Transactional
    public void deleteAttachment(Long ticketId, Long attachmentId, String loginIdentifier) {
        User user = findUser(loginIdentifier);
        Ticket ticket = findTicket(ticketId);
        checkCanViewTicket(user, ticket);

        Attachment attachment = findAttachment(ticketId, attachmentId);
        if (!attachment.getUploadedBy().getId().equals(user.getId()) && !hasRole(user, "ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only uploader or admin can delete attachment");
        }

        Path filePath = Paths.get(attachment.getFilePath()).toAbsolutePath().normalize();
        attachmentRepository.delete(attachment);
        saveHistory(ticket, user, TicketHistoryActionType.ATTACHMENT_DELETED,
                null, null, null, null, null, null, "Attachment deleted: " + attachment.getFileName());

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not delete attachment file", exception);
        }
    }

    private TicketResponse mapToTicketResponse(Ticket ticket) {
        String assignedTo = ticket.getAssignedTo() == null ? null : ticket.getAssignedTo().getLoginIdentifier();

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCreatedBy().getLoginIdentifier(),
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
                comment.getAuthor().getLoginIdentifier(),
                comment.getContent(),
                comment.isInternal(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    private TicketHistoryResponse mapToTicketHistoryResponse(TicketHistory history) {
        String oldAssignedTo = history.getOldAssignedTo() == null ? null : history.getOldAssignedTo().getLoginIdentifier();
        String newAssignedTo = history.getNewAssignedTo() == null ? null : history.getNewAssignedTo().getLoginIdentifier();

        return new TicketHistoryResponse(
                history.getId(),
                history.getTicket().getId(),
                history.getChangedBy().getLoginIdentifier(),
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

    private AttachmentResponse mapToAttachmentResponse(Attachment attachment) {
        Long commentId = attachment.getComment() == null ? null : attachment.getComment().getId();

        return new AttachmentResponse(
                attachment.getId(),
                attachment.getTicket().getId(),
                commentId,
                attachment.getUploadedBy().getLoginIdentifier(),
                attachment.getFileName(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getUploadedAt()
        );
    }

    private Map<TicketStatus, Long> buildStatusBreakdown(List<Ticket> tickets) {
        Map<TicketStatus, Long> statusBreakdown = new EnumMap<>(TicketStatus.class);
        for (TicketStatus status : TicketStatus.values()) {
            statusBreakdown.put(status, 0L);
        }
        tickets.forEach(ticket -> statusBreakdown.compute(ticket.getStatus(), (status, count) -> count == null ? 1L : count + 1));

        return statusBreakdown;
    }

    private long countStatus(List<Ticket> tickets, TicketStatus... statuses) {
        Set<TicketStatus> expectedStatuses = EnumSet.noneOf(TicketStatus.class);
        expectedStatuses.addAll(List.of(statuses));

        return tickets.stream()
                .filter(ticket -> expectedStatuses.contains(ticket.getStatus()))
                .count();
    }

    private List<UserDashboardActivityResponse> recentUserActivity(List<Ticket> tickets) {
        List<UserDashboardActivityResponse> activities = new ArrayList<>();

        for (Ticket ticket : tickets) {
            ticket.getComments()
                    .stream()
                    .filter(comment -> !comment.isInternal())
                    .forEach(comment -> activities.add(new UserDashboardActivityResponse(
                            ticket.getId(),
                            ticket.getTitle(),
                            "COMMENT",
                            comment.getAuthor().getLoginIdentifier(),
                            "Komentarz: " + truncate(comment.getContent(), 140),
                            comment.getCreatedAt()
                    )));

            ticket.getHistoryEntries()
                    .stream()
                    .filter(history -> history.getActionType() != TicketHistoryActionType.COMMENT_ADDED)
                    .filter(history -> history.getActionType() != TicketHistoryActionType.ATTACHMENT_ADDED)
                    .filter(history -> history.getActionType() != TicketHistoryActionType.ATTACHMENT_DELETED)
                    .forEach(history -> activities.add(new UserDashboardActivityResponse(
                            ticket.getId(),
                            ticket.getTitle(),
                            "HISTORY",
                            history.getChangedBy().getLoginIdentifier(),
                            activityDescription(history),
                            history.getChangedAt()
                    )));

            ticket.getAttachments()
                    .stream()
                    .filter(this::isPublicAttachment)
                    .forEach(attachment -> activities.add(new UserDashboardActivityResponse(
                            ticket.getId(),
                            ticket.getTitle(),
                            "ATTACHMENT",
                            attachment.getUploadedBy().getLoginIdentifier(),
                            "Dodano załącznik: " + attachment.getFileName(),
                            attachment.getUploadedAt()
                    )));
        }

        return activities.stream()
                .sorted(Comparator.comparing(UserDashboardActivityResponse::getOccurredAt).reversed())
                .limit(8)
                .toList();
    }

    private LocalDateTime lastActivityAt(Ticket ticket) {
        LocalDateTime lastActivityAt = ticket.getUpdatedAt() == null ? ticket.getCreatedAt() : ticket.getUpdatedAt();

        for (Comment comment : ticket.getComments()) {
            if (!comment.isInternal() && comment.getCreatedAt().isAfter(lastActivityAt)) {
                lastActivityAt = comment.getCreatedAt();
            }
        }
        for (TicketHistory history : ticket.getHistoryEntries()) {
            if (history.getChangedAt().isAfter(lastActivityAt)) {
                lastActivityAt = history.getChangedAt();
            }
        }
        for (Attachment attachment : ticket.getAttachments()) {
            if (isPublicAttachment(attachment) && attachment.getUploadedAt().isAfter(lastActivityAt)) {
                lastActivityAt = attachment.getUploadedAt();
            }
        }

        return lastActivityAt;
    }

    private boolean isPublicAttachment(Attachment attachment) {
        return attachment.getComment() == null || !attachment.getComment().isInternal();
    }

    private String activityDescription(TicketHistory history) {
        if (history.getOldStatus() != null && history.getNewStatus() != null) {
            return "Zmieniono status: " + statusLabel(history.getOldStatus()) + " -> "
                    + statusLabel(history.getNewStatus());
        }
        if (history.getActionType() == TicketHistoryActionType.TICKET_CREATED) {
            return "Utworzono zgłoszenie";
        }
        if (history.getActionType() == TicketHistoryActionType.TICKET_RESOLVED) {
            return "Oznaczono zgłoszenie jako rozwiązane";
        }
        if (history.getActionType() == TicketHistoryActionType.TICKET_CLOSED) {
            return "Zamknięto zgłoszenie";
        }
        if (history.getNote() != null && !history.getNote().isBlank()) {
            return history.getNote();
        }

        return history.getActionType().name();
    }

    private String statusLabel(TicketStatus status) {
        return switch (status) {
            case OPEN -> "Otwarte";
            case IN_PROGRESS -> "W toku";
            case WAITING_FOR_USER -> "Czeka na użytkownika";
            case RESOLVED -> "Rozwiązane";
            case CLOSED -> "Zamknięte";
            case REJECTED -> "Odrzucone";
            case CANCELLED -> "Anulowane";
        };
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }

        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }

        return normalized.substring(0, maxLength - 3) + "...";
    }

    @Transactional
    public TicketResponse createTicketIfMissing(String title, String description, TicketStatus status,
                                                TicketPriority priority, String createdByLoginIdentifier,
                                                String categoryName) {
        return ticketRepository.findByTitle(title)
                .map(this::mapToTicketResponse)
                .orElseGet(() -> createSeedTicket(title, description, status, priority,
                        createdByLoginIdentifier, categoryName));
    }

    @Transactional(readOnly = true)
    public boolean ticketExistsByTitle(String title) {
        return ticketRepository.existsByTitle(title);
    }

    private TicketResponse createSeedTicket(String title, String description, TicketStatus status,
                                            TicketPriority priority, String createdByLoginIdentifier,
                                            String categoryName) {
        var createdBy = userRepository.findByLoginIdentifier(createdByLoginIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + createdByLoginIdentifier));
        checkCanCreateTicket(createdBy);

        Category category = categoryRepository.findByNameIgnoreCaseAndActiveTrue(
                        requireText(categoryName, "Category is required"))
                .orElseThrow(() -> new IllegalArgumentException("Category not found or inactive: " + categoryName));

        Ticket ticket = new Ticket(
                title,
                description,
                status,
                priority,
                createdBy,
                category
        );

        Ticket savedTicket = ticketRepository.save(ticket);
        saveHistory(savedTicket, createdBy, TicketHistoryActionType.TICKET_CREATED, null, status,
                null, priority, null, null, "Seed ticket created");

        return mapToTicketResponse(savedTicket);
    }

    private void checkCanChangeStatus(User actor, Ticket ticket, TicketStatus newStatus) {
        TicketStatus oldStatus = ticket.getStatus();
        if (!isAllowedStatusTransition(oldStatus, newStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Status transition " + oldStatus + " -> " + newStatus + " is not allowed");
        }

        if (hasRole(actor, "ADMIN")) {
            return;
        }

        if (isCustomer(actor)) {
            if (!isTicketOwner(actor, ticket)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ticket owner can change ticket status");
            }
            if (newStatus == TicketStatus.CANCELLED || newStatus == TicketStatus.CLOSED) {
                return;
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Users can only cancel or close their own tickets");
        }

        if (hasRole(actor, "AGENT") && newStatus != TicketStatus.CLOSED) {
            return;
        }

        if (newStatus == TicketStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ticket owner or admin can close ticket");
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Missing role");
    }

    private void checkCanEditTicket(User actor, Ticket ticket) {
        if (isTerminalStatus(ticket.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Terminal tickets cannot be edited");
        }
        if (isTicketOwner(actor, ticket) || hasRole(actor, "ADMIN")) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No access to edit ticket");
    }

    private void checkCanCreateTicket(User user) {
        if (isCustomer(user)) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only users can create tickets");
    }

    private void checkCanTakeTicket(User user) {
        checkHasAnyRole(user, "AGENT");
    }

    private void checkCanAssignPriority(User user) {
        checkHasAnyRole(user, "AGENT", "ADMIN");
    }

    private void checkCanAddComment(User author, Ticket ticket, boolean internal) {
        checkCanViewTicket(author, ticket);

        if (internal && !isStaff(author)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only agents and admins can add internal comments");
        }
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

    private TicketStatus statusAfterComment(Ticket ticket, User author, boolean internal) {
        if (internal || isTerminalStatus(ticket.getStatus())) {
            return ticket.getStatus();
        }
        if (isStaff(author) && isAllowedStatusTransition(ticket.getStatus(), TicketStatus.WAITING_FOR_USER)) {
            return TicketStatus.WAITING_FOR_USER;
        }
        if (isTicketOwner(author, ticket)
                && isAllowedStatusTransition(ticket.getStatus(), TicketStatus.IN_PROGRESS)) {
            return TicketStatus.IN_PROGRESS;
        }

        return ticket.getStatus();
    }

    private boolean isTerminalStatus(TicketStatus status) {
        return TERMINAL_STATUSES.contains(status);
    }

    private boolean isAllowedStatusTransition(TicketStatus oldStatus, TicketStatus newStatus) {
        return ALLOWED_STATUS_TRANSITIONS.getOrDefault(oldStatus, Set.of()).contains(newStatus);
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

    private Comment findComment(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found: " + id));
    }

    private Attachment findAttachment(Long ticketId, Long attachmentId) {
        return attachmentRepository.findByIdAndTicketId(attachmentId, ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Attachment not found: " + attachmentId));
    }

    private User findUser(String loginIdentifier) {
        return userRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found: " + loginIdentifier));
    }

    private Specification<Ticket> buildTicketSpecification(TicketFilterRequest filter, Long createdById) {
        validateDateRange(filter);

        Specification<Ticket> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (createdById != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("createdBy").get("id"), createdById));
        }

        if (filter == null) {
            return specification;
        }

        if (filter.getStatus() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), filter.getStatus()));
        }

        if (filter.getPriority() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("priority"), filter.getPriority()));
        }

        if (hasText(filter.getCategory())) {
            String category = filter.getCategory().trim().toLowerCase();
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(criteriaBuilder.lower(root.get("category").get("name")), category));
        }

        if (hasText(filter.getAgent())) {
            String agent = filter.getAgent().trim().toLowerCase();
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(criteriaBuilder.lower(root.get("assignedTo").get("loginIdentifier")), agent));
        }

        if (filter.getCreatedFrom() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedFrom()));
        }

        if (filter.getCreatedTo() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedTo()));
        }

        return specification;
    }

    private void validateDateRange(TicketFilterRequest filter) {
        if (filter != null
                && filter.getCreatedFrom() != null
                && filter.getCreatedTo() != null
                && filter.getCreatedFrom().isAfter(filter.getCreatedTo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "createdFrom must be before createdTo");
        }
    }

    private Category findActiveCategory(String categoryName) {
        String name = requireText(categoryName, "Category is required");

        return categoryRepository.findByNameIgnoreCaseAndActiveTrue(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Category not found or inactive: " + name));
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

    private boolean isCustomer(User user) {
        return hasRole(user, "USER") && !isStaff(user);
    }

    private boolean isStaff(User user) {
        return hasAnyRole(user, "AGENT", "ADMIN");
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void validateAttachmentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attachment file is required");
        }
    }

    private String sanitizeFileName(String fileName) {
        String cleanedFileName = StringUtils.cleanPath(fileName == null ? "" : fileName);
        String normalizedFileName = Paths.get(cleanedFileName).getFileName().toString();

        if (!hasText(normalizedFileName) || normalizedFileName.contains("..")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name");
        }

        return normalizedFileName;
    }

    private String resolveContentType(MultipartFile file) {
        return hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream";
    }

    private String resolveContentType(Attachment attachment) {
        return hasText(attachment.getContentType()) ? attachment.getContentType() : "application/octet-stream";
    }
}
