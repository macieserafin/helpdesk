package macieserafin.pl.helpdesk.repository;

import macieserafin.pl.helpdesk.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    Optional<Category> findByNameIgnoreCase(String name);

    Optional<Category> findByNameIgnoreCaseAndActiveTrue(String name);

    List<Category> findAllByOrderByNameAsc();

    List<Category> findByActiveTrueOrderByNameAsc();
}
