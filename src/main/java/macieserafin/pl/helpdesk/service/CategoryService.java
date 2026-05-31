package macieserafin.pl.helpdesk.service;

import macieserafin.pl.helpdesk.dto.CategoryResponse;
import macieserafin.pl.helpdesk.dto.CreateCategoryRequest;
import macieserafin.pl.helpdesk.dto.UpdateCategoryRequest;
import macieserafin.pl.helpdesk.model.entity.Category;
import macieserafin.pl.helpdesk.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {
        return mapToResponse(findCategory(id));
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        String name = requireText(request.getName(), "Category name is required");
        ensureNameAvailable(name, null);

        Category category = new Category(name);
        category.setDescription(trimToNull(request.getDescription()));
        category.setActive(true);

        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        Category category = findCategory(id);

        if (request.getName() != null) {
            String name = requireText(request.getName(), "Category name is required");
            ensureNameAvailable(name, id);
            category.setName(name);
        }

        if (request.getDescription() != null) {
            category.setDescription(trimToNull(request.getDescription()));
        }

        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        return mapToResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = findCategory(id);
        category.setActive(false);
    }

    @Transactional
    public void createCategoryIfMissing(String name, String description) {
        String normalizedName = requireText(name, "Category name is required");

        Category category = categoryRepository.findByNameIgnoreCase(normalizedName)
                .orElseGet(() -> categoryRepository.save(new Category(normalizedName)));
        category.setDescription(trimToNull(description));
        category.setActive(true);
    }

    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.isActive(),
                category.getCreatedAt()
        );
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found: " + id));
    }

    private void ensureNameAvailable(String name, Long currentCategoryId) {
        categoryRepository.findByNameIgnoreCase(name)
                .filter(existing -> currentCategoryId == null || !existing.getId().equals(currentCategoryId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists: " + name);
                });
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        return value.trim();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
