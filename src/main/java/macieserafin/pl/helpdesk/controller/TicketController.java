package macieserafin.pl.helpdesk.controller;

import macieserafin.pl.helpdesk.dto.CommentResponse;
import macieserafin.pl.helpdesk.dto.CreateCommentRequest;
import macieserafin.pl.helpdesk.dto.CreateTicketRequest;
import macieserafin.pl.helpdesk.dto.TicketHistoryResponse;
import macieserafin.pl.helpdesk.dto.TicketResponse;
import macieserafin.pl.helpdesk.dto.UpdateTicketStatusRequest;
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
@RequestMapping
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    //pobieranie wszystkich zgloszen przez admina
    @GetMapping("/api/admin/tickets")
    public List<TicketResponse> getTickets() {
        return ticketService.getTickets();
    }

    //pobieranie kolejki zgloszen przez agenta
    @GetMapping("/api/agent/tickets")
    public List<TicketResponse> getAgentTickets(Principal principal) {
        return ticketService.getAgentTickets(principal.getName());
    }

    //utworzenie nowego zgloszenia
    @PostMapping("/api/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse createTicket(@RequestBody CreateTicketRequest request, Principal principal) {
        return ticketService.createTicket(request, principal.getName());
    }

    //pobieranie zgloszen zalogowanego uzytkownika
    @GetMapping("/api/tickets/me")
    public List<TicketResponse> getMyTickets(Principal principal) {
        return ticketService.getMyTickets(principal.getName());
    }

    //pobieranuie konkretnego zgloszenia po ID
    @GetMapping("/api/tickets/{id}")
    public TicketResponse getTicket(@PathVariable Long id, Principal principal) {
        return ticketService.getTicket(id, principal.getName());
    }

    //przypisanie zgloszenia do agenta
    @PatchMapping("/api/agent/tickets/{id}/assign")
    public TicketResponse assignTicket(@PathVariable Long id, Principal principal) {
        return ticketService.assignTicket(id, principal.getName());
    }

    //zmiana statusu zgloszenia przez agenta
    @PatchMapping("/api/agent/tickets/{id}/status")
    public TicketResponse updateTicketStatus(@PathVariable Long id,
                                             @RequestBody UpdateTicketStatusRequest request,
                                             Principal principal) {
        return ticketService.updateStatus(id, request.getStatus(), principal.getName());
    }

    //dodanie komentarza do ticketa
    @PostMapping("/api/tickets/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(@PathVariable Long id,
                                      @RequestBody CreateCommentRequest request,
                                      Principal principal) {
        return ticketService.addComment(id, request, principal.getName());
    }

    //pobieranie komentarzy ticketa
    @GetMapping("/api/tickets/{id}/comments")
    public List<CommentResponse> getComments(@PathVariable Long id, Principal principal) {
        return ticketService.getComments(id, principal.getName());
    }

    //pobieranie histori ticketa
    @GetMapping("/api/tickets/{id}/history")
    public List<TicketHistoryResponse> getHistory(@PathVariable Long id, Principal principal) {
        return ticketService.getHistory(id, principal.getName());
    }
}
