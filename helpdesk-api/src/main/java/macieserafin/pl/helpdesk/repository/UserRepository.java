package macieserafin.pl.helpdesk.repository;

import macieserafin.pl.helpdesk.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginIdentifier(String loginIdentifier);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByLoginIdentifier(String loginIdentifier);

    boolean existsByEmailIgnoreCase(String email);
}
