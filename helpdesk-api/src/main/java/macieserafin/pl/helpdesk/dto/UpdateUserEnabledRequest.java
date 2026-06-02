package macieserafin.pl.helpdesk.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateUserEnabledRequest {
    @NotNull(message = "Enabled is required")
    private Boolean enabled;

    public UpdateUserEnabledRequest() {
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
