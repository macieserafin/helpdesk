package macieserafin.pl.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static macieserafin.pl.helpdesk.contract.ApiContract.COMMENT_CONTENT_MAX_LENGTH;

public class CreateCommentRequest {
    @NotBlank(message = "Comment content is required")
    @Size(max = COMMENT_CONTENT_MAX_LENGTH, message = "Comment content must not exceed 2000 characters")
    private String content;

    private boolean internal;

    public CreateCommentRequest() {
    }

    public CreateCommentRequest(String content, boolean internal) {
        this.content = content;
        this.internal = internal;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }
}
