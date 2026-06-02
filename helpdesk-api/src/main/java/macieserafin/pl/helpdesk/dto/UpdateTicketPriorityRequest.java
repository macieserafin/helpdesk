package macieserafin.pl.helpdesk.dto;

import jakarta.validation.constraints.NotNull;
import macieserafin.pl.helpdesk.model.enums.TicketPriority;

public class UpdateTicketPriorityRequest {
    @NotNull(message = "Priority is required")
    private TicketPriority priority;

    public UpdateTicketPriorityRequest() {
    }

    public UpdateTicketPriorityRequest(TicketPriority priority) {
        this.priority = priority;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }
}
