package macieserafin.pl.helpdesk.repository;

import macieserafin.pl.helpdesk.model.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByTicketId(Long ticketId);

    List<Attachment> findByCommentId(Long commentId);
}
