package macieserafin.pl.helpdesk.dto;

import macieserafin.pl.helpdesk.model.enums.TicketHistoryActionType;
import macieserafin.pl.helpdesk.model.enums.TicketPriority;
import macieserafin.pl.helpdesk.model.enums.TicketStatus;

import java.time.LocalDateTime;

public class TicketHistoryResponse {
    private Long id;
    private Long ticketId;
    private String changedBy;
    private TicketHistoryActionType actionType;
    private TicketStatus oldStatus;
    private TicketStatus newStatus;
    private TicketPriority oldPriority;
    private TicketPriority newPriority;
    private String oldAssignedTo;
    private String newAssignedTo;
    private String note;
    private LocalDateTime changedAt;

    public TicketHistoryResponse() {
    }

    public TicketHistoryResponse(Long id, Long ticketId, String changedBy, TicketHistoryActionType actionType,
                                 TicketStatus oldStatus, TicketStatus newStatus, TicketPriority oldPriority,
                                 TicketPriority newPriority, String oldAssignedTo, String newAssignedTo,
                                 String note, LocalDateTime changedAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.changedBy = changedBy;
        this.actionType = actionType;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.oldPriority = oldPriority;
        this.newPriority = newPriority;
        this.oldAssignedTo = oldAssignedTo;
        this.newAssignedTo = newAssignedTo;
        this.note = note;
        this.changedAt = changedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
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

    public String getOldAssignedTo() {
        return oldAssignedTo;
    }

    public void setOldAssignedTo(String oldAssignedTo) {
        this.oldAssignedTo = oldAssignedTo;
    }

    public String getNewAssignedTo() {
        return newAssignedTo;
    }

    public void setNewAssignedTo(String newAssignedTo) {
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
