package macieserafin.pl.helpdesk.dto;

import java.time.LocalDateTime;

public class AttachmentResponse {
    private Long id;
    private Long ticketId;
    private Long commentId;
    private String uploadedBy;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private LocalDateTime uploadedAt;

    public AttachmentResponse() {
    }

    public AttachmentResponse(Long id, Long ticketId, Long commentId, String uploadedBy, String fileName,
                              String contentType, Long fileSize, LocalDateTime uploadedAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.commentId = commentId;
        this.uploadedBy = uploadedBy;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
