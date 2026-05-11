package macieserafin.pl.helpdesk.dto;

public class CreateCommentRequest {
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
