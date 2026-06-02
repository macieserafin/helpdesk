package macieserafin.pl.helpdesk.dto;

import jakarta.validation.constraints.Size;

import static macieserafin.pl.helpdesk.contract.ApiContract.CATEGORY_DESCRIPTION_MAX_LENGTH;
import static macieserafin.pl.helpdesk.contract.ApiContract.CATEGORY_NAME_MAX_LENGTH;

public class UpdateCategoryRequest {
    @Size(max = CATEGORY_NAME_MAX_LENGTH, message = "Category name must not exceed 100 characters")
    private String name;

    @Size(max = CATEGORY_DESCRIPTION_MAX_LENGTH, message = "Description must not exceed 1000 characters")
    private String description;

    private Boolean active;

    public UpdateCategoryRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
