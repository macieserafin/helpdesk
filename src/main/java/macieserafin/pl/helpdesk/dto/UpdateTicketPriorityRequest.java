package macieserafin.pl.helpdesk.dto;

import macieserafin.pl.helpdesk.model.enums.TicketPriority;

public class UpdateTicketPriorityRequest {
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
