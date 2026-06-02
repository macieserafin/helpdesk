package macieserafin.pl.helpdesk.dto;

import org.springframework.core.io.Resource;

public record AttachmentDownload(
        Resource resource,
        String fileName,
        String contentType,
        Long fileSize
) {
}
