package macieserafin.pl.helpdesk.dto;

import java.time.LocalDateTime;

public class UserDashboardActivityResponse {
    private Long ticketId;
    private String ticketTitle;
    private String type;
    private String actor;
    private String description;
    private LocalDateTime occurredAt;

    public UserDashboardActivityResponse() {
    }

    public UserDashboardActivityResponse(Long ticketId, String ticketTitle, String type, String actor,
                                         String description, LocalDateTime occurredAt) {
        this.ticketId = ticketId;
        this.ticketTitle = ticketTitle;
        this.type = type;
        this.actor = actor;
        this.description = description;
        this.occurredAt = occurredAt;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getTicketTitle() {
        return ticketTitle;
    }

    public void setTicketTitle(String ticketTitle) {
        this.ticketTitle = ticketTitle;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}
