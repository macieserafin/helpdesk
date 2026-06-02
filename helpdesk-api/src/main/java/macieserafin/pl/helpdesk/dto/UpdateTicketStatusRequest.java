package macieserafin.pl.helpdesk.dto;

import jakarta.validation.constraints.NotNull;
import macieserafin.pl.helpdesk.model.enums.TicketStatus;

public class UpdateTicketStatusRequest {
    @NotNull(message = "Status is required")
    private TicketStatus status;

    public UpdateTicketStatusRequest() {
    }

    public UpdateTicketStatusRequest(TicketStatus status) {
        this.status = status;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }
}
