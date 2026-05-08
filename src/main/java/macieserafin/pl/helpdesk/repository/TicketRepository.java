package macieserafin.pl.helpdesk.repository;

import macieserafin.pl.helpdesk.model.entity.Ticket;
import macieserafin.pl.helpdesk.model.enums.TicketPriority;
import macieserafin.pl.helpdesk.model.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    boolean existsByTitle(String title);

    List<Ticket> findAllByOrderByCreatedAtDesc();

    List<Ticket> findByCreatedByIdOrderByCreatedAtDesc(Long createdById);

    List<Ticket> findByAssignedToIdOrderByCreatedAtDesc(Long assignedToId);

    List<Ticket> findByCategoryIdOrderByCreatedAtDesc(Long categoryId);

    List<Ticket> findByStatusOrderByCreatedAtDesc(TicketStatus status);

    List<Ticket> findByPriorityOrderByCreatedAtDesc(TicketPriority priority);
}
