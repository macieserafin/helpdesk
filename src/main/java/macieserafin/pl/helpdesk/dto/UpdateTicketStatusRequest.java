package macieserafin.pl.helpdesk.dto;

import macieserafin.pl.helpdesk.model.enums.TicketStatus;

public class UpdateTicketStatusRequest {
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
