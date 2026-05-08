package macieserafin.pl.helpdesk.repository;

import macieserafin.pl.helpdesk.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    List<Comment> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
