package macieserafin.pl.helpdesk.controller;

import macieserafin.pl.helpdesk.dto.TicketResponse;
import macieserafin.pl.helpdesk.service.TicketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tickets")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public List<TicketResponse> getTickets() {
        return ticketService.getTickets();
    }
}
