package macieserafin.pl.helpdesk.repository;

import macieserafin.pl.helpdesk.model.entity.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {

    List<TicketHistory> findByTicketIdOrderByChangedAtAsc(Long ticketId);

    List<TicketHistory> findByTicketIdOrderByChangedAtDesc(Long ticketId);

    List<TicketHistory> findByChangedByIdOrderByChangedAtDesc(Long changedById);
}
