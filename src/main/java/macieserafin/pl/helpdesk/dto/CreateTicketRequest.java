package macieserafin.pl.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static macieserafin.pl.helpdesk.contract.ApiContract.TICKET_CATEGORY_MAX_LENGTH;
import static macieserafin.pl.helpdesk.contract.ApiContract.TICKET_DESCRIPTION_MAX_LENGTH;
import static macieserafin.pl.helpdesk.contract.ApiContract.TICKET_TITLE_MAX_LENGTH;

public class CreateTicketRequest {
    @NotBlank(message = "Title is required")
    @Size(max = TICKET_TITLE_MAX_LENGTH, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = TICKET_DESCRIPTION_MAX_LENGTH, message = "Description must not exceed 4000 characters")
    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = TICKET_CATEGORY_MAX_LENGTH, message = "Category must not exceed 100 characters")
    private String category;

    public CreateTicketRequest() {
    }

    public CreateTicketRequest(String title, String description, String category) {
        this.title = title;
        this.description = description;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
