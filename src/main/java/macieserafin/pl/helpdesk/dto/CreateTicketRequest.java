package macieserafin.pl.helpdesk.dto;

import macieserafin.pl.helpdesk.model.enums.TicketPriority;

public class CreateTicketRequest {
    private String title;
    private String description;
    private TicketPriority priority;
    private String category;

    public CreateTicketRequest() {
    }

    public CreateTicketRequest(String title, String description, TicketPriority priority, String category) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
