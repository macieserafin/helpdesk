package macieserafin.pl.helpdesk.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import macieserafin.pl.helpdesk.model.enums.TicketHistoryActionType;
import macieserafin.pl.helpdesk.model.enums.TicketPriority;
import macieserafin.pl.helpdesk.model.enums.TicketStatus;

import java.time.LocalDateTime;

import static macieserafin.pl.helpdesk.contract.ApiContract.TICKET_ENUM_MAX_LENGTH;

@Entity
@Table(name = "ticket_history")
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_id", nullable = false)
    private User changedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 50, nullable = false)
    private TicketHistoryActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = TICKET_ENUM_MAX_LENGTH)
    private TicketStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = TICKET_ENUM_MAX_LENGTH)
    private TicketStatus newStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_priority", length = TICKET_ENUM_MAX_LENGTH)
    private TicketPriority oldPriority;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_priority", length = TICKET_ENUM_MAX_LENGTH)
    private TicketPriority newPriority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_assigned_to_id")
    private User oldAssignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_assigned_to_id")
    private User newAssignedTo;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    protected TicketHistory() {
    }

    public TicketHistory(Ticket ticket, User changedBy, TicketHistoryActionType actionType) {
        this.ticket = ticket;
        this.changedBy = changedBy;
        this.actionType = actionType;
    }

    @PrePersist
    void prePersist() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public User getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(User changedBy) {
        this.changedBy = changedBy;
    }

    public TicketHistoryActionType getActionType() {
        return actionType;
    }

    public void setActionType(TicketHistoryActionType actionType) {
        this.actionType = actionType;
    }

    public TicketStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(TicketStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public TicketStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(TicketStatus newStatus) {
        this.newStatus = newStatus;
    }

    public TicketPriority getOldPriority() {
        return oldPriority;
    }

    public void setOldPriority(TicketPriority oldPriority) {
        this.oldPriority = oldPriority;
    }

    public TicketPriority getNewPriority() {
        return newPriority;
    }

    public void setNewPriority(TicketPriority newPriority) {
        this.newPriority = newPriority;
    }

    public User getOldAssignedTo() {
        return oldAssignedTo;
    }

    public void setOldAssignedTo(User oldAssignedTo) {
        this.oldAssignedTo = oldAssignedTo;
    }

    public User getNewAssignedTo() {
        return newAssignedTo;
    }

    public void setNewAssignedTo(User newAssignedTo) {
        this.newAssignedTo = newAssignedTo;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
