package macieserafin.pl.helpdesk.controller;

import jakarta.validation.Valid;
import macieserafin.pl.helpdesk.dto.CommentResponse;
import macieserafin.pl.helpdesk.dto.CreateCommentRequest;
import macieserafin.pl.helpdesk.dto.CreateTicketRequest;
import macieserafin.pl.helpdesk.dto.PageResponse;
import macieserafin.pl.helpdesk.dto.TicketFilterRequest;
import macieserafin.pl.helpdesk.dto.TicketHistoryResponse;
import macieserafin.pl.helpdesk.dto.TicketResponse;
import macieserafin.pl.helpdesk.dto.UpdateTicketPriorityRequest;
import macieserafin.pl.helpdesk.dto.UpdateTicketStatusRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import macieserafin.pl.helpdesk.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

    //pobieranuie konkretnego zgloszenia po ID
    @GetMapping("/tickets/{id}")
    public TicketResponse getTicket(@PathVariable Long id, Principal principal) {
        return ticketService.getTicket(id, principal.getName());
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
}
