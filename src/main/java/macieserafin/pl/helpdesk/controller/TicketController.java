package macieserafin.pl.helpdesk.controller;

import jakarta.validation.Valid;
import macieserafin.pl.helpdesk.dto.AttachmentDownload;
import macieserafin.pl.helpdesk.dto.AttachmentResponse;
import macieserafin.pl.helpdesk.dto.CommentResponse;
import macieserafin.pl.helpdesk.dto.CreateCommentRequest;
import macieserafin.pl.helpdesk.dto.CreateTicketRequest;
import macieserafin.pl.helpdesk.dto.PageResponse;
import macieserafin.pl.helpdesk.dto.TicketFilterRequest;
import macieserafin.pl.helpdesk.dto.TicketHistoryResponse;
import macieserafin.pl.helpdesk.dto.TicketResponse;
import macieserafin.pl.helpdesk.dto.UpdateTicketPriorityRequest;
import macieserafin.pl.helpdesk.dto.UpdateTicketRequest;
import macieserafin.pl.helpdesk.dto.UpdateTicketStatusRequest;
import macieserafin.pl.helpdesk.model.enums.TicketPriority;
import macieserafin.pl.helpdesk.model.enums.TicketStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import macieserafin.pl.helpdesk.service.TicketService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    //pobieranie wszystkich zgloszen przez admina
    @GetMapping("/admin/tickets")
    public PageResponse<TicketResponse> getTickets(TicketFilterRequest filter,
                                                   @PageableDefault(size = 20, sort = "createdAt",
                                                           direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.of(ticketService.getTickets(filter, pageable));
    }

    //pobieranie kolejki zgloszen przez agenta
    @GetMapping("/agent/tickets")
    public PageResponse<TicketResponse> getAgentTickets(TicketFilterRequest filter,
                                                        @PageableDefault(size = 20, sort = "createdAt",
                                                                direction = Sort.Direction.DESC) Pageable pageable,
                                                        Principal principal) {
        return PageResponse.of(ticketService.getAgentTickets(principal.getName(), filter, pageable));
    }

    //utworzenie nowego zgloszenia
    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse createTicket(@Valid @RequestBody CreateTicketRequest request, Principal principal) {
        return ticketService.createTicket(request, principal.getName());
    }

    //pobieranie zgloszen zalogowanego uzytkownika
    @GetMapping("/tickets/me")
    public PageResponse<TicketResponse> getMyTickets(TicketFilterRequest filter,
                                                     @PageableDefault(size = 20, sort = "createdAt",
                                                             direction = Sort.Direction.DESC) Pageable pageable,
                                                     Principal principal) {
        return PageResponse.of(ticketService.getMyTickets(principal.getName(), filter, pageable));
    }

    @GetMapping("/tickets/statuses")
    public List<TicketStatus> getTicketStatuses() {
        return List.of(TicketStatus.values());
    }

    @GetMapping("/tickets/priorities")
    public List<TicketPriority> getTicketPriorities() {
        return List.of(TicketPriority.values());
    }

    @GetMapping("/agent/tickets/assignable-priorities")
    public List<TicketPriority> getAssignableTicketPriorities() {
        return List.of(TicketPriority.LOW, TicketPriority.MEDIUM, TicketPriority.HIGH, TicketPriority.CRITICAL);
    }

    //pobieranuie konkretnego zgloszenia po ID
    @GetMapping("/tickets/{id}")
    public TicketResponse getTicket(@PathVariable Long id, Principal principal) {
        return ticketService.getTicket(id, principal.getName());
    }

    //edycja danych ticketa
    @PatchMapping("/tickets/{id}")
    public TicketResponse updateTicket(@PathVariable Long id,
                                       @Valid @RequestBody UpdateTicketRequest request,
                                       Principal principal) {
        return ticketService.updateTicket(id, request, principal.getName());
    }

    //przypisanie zgloszenia do agenta
    @PatchMapping("/agent/tickets/{id}/assign")
    public TicketResponse assignTicket(@PathVariable Long id, Principal principal) {
        return ticketService.assignTicket(id, principal.getName());
    }

    //ustawienie priorytetu zgloszenia przez agenta
    @PatchMapping("/agent/tickets/{id}/priority")
    public TicketResponse updateTicketPriority(@PathVariable Long id,
                                               @Valid @RequestBody UpdateTicketPriorityRequest request,
                                               Principal principal) {
        return ticketService.updatePriority(id, request, principal.getName());
    }

    //zmiana statusu zgloszenia przez agenta
    @PatchMapping("/agent/tickets/{id}/status")
    public TicketResponse updateTicketStatus(@PathVariable Long id,
                                             @Valid @RequestBody UpdateTicketStatusRequest request,
                                             Principal principal) {
        return ticketService.updateStatus(id, request.getStatus(), principal.getName());
    }

    @PatchMapping("/tickets/{id}/status")
    public TicketResponse updateTicketStatusForCurrentUser(@PathVariable Long id,
                                                           @Valid @RequestBody UpdateTicketStatusRequest request,
                                                           Principal principal) {
        return ticketService.updateStatus(id, request.getStatus(), principal.getName());
    }

    //dodanie komentarza do ticketa
    @PostMapping("/tickets/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(@PathVariable Long id,
                                      @Valid @RequestBody CreateCommentRequest request,
                                      Principal principal) {
        return ticketService.addComment(id, request, principal.getName());
    }

    //pobieranie komentarzy ticketa
    @GetMapping("/tickets/{id}/comments")
    public List<CommentResponse> getComments(@PathVariable Long id, Principal principal) {
        return ticketService.getComments(id, principal.getName());
    }

    //pobieranie histori ticketa
    @GetMapping("/tickets/{id}/history")
    public List<TicketHistoryResponse> getHistory(@PathVariable Long id, Principal principal) {
        return ticketService.getHistory(id, principal.getName());
    }

    @PostMapping(value = "/tickets/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentResponse addAttachment(@PathVariable Long id,
                                            @RequestParam("file") MultipartFile file,
                                            @RequestParam(required = false) Long commentId,
                                            Principal principal) {
        return ticketService.addAttachment(id, commentId, file, principal.getName());
    }

    @GetMapping("/tickets/{id}/attachments")
    public List<AttachmentResponse> getAttachments(@PathVariable Long id, Principal principal) {
        return ticketService.getAttachments(id, principal.getName());
    }

    @GetMapping("/tickets/{ticketId}/attachments/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long ticketId,
                                                       @PathVariable Long attachmentId,
                                                       Principal principal) {
        AttachmentDownload download = ticketService.downloadAttachment(ticketId, attachmentId, principal.getName());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .contentLength(download.fileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(download.fileName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(download.resource());
    }

    @DeleteMapping("/tickets/{ticketId}/attachments/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAttachment(@PathVariable Long ticketId,
                                 @PathVariable Long attachmentId,
                                 Principal principal) {
        ticketService.deleteAttachment(ticketId, attachmentId, principal.getName());
    }
}
