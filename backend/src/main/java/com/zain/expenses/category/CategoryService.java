package com.zain.expenses.category;

import com.zain.expenses.user.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categories;
    private final UserRepository users;

    public CategoryService(CategoryRepository categories, UserRepository users) {
        this.categories = categories;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list(Long userId) {
        return categories.findByUserIdOrderByNameAsc(userId).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest request) {
        String name = tidy(request.name());
        if (categories.existsByUserIdAndNameIgnoreCase(userId, name)) {
            throw new DuplicateCategoryException(name);
        }
        Category category = categories.save(new Category(users.getReferenceById(userId), name));
        return CategoryResponse.from(category);
    }

    @Transactional
    public CategoryResponse rename(Long userId, Long id, CategoryRequest request) {
        Category category = findOwnedCategory(userId, id);
        String name = tidy(request.name());
        // Another category with the same name is a clash; changing the case of this one isn't.
        if (categories.existsByUserIdAndNameIgnoreCaseAndIdNot(userId, name, id)) {
            throw new DuplicateCategoryException(name);
        }
        category.rename(name);
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Category category = findOwnedCategory(userId, id);
        long transactionCount = categories.countTransactions(id);
        if (transactionCount > 0) {
            throw new CategoryInUseException(category.getName(), transactionCount);
        }
        categories.delete(category);
    }

    private Category findOwnedCategory(Long userId, Long id) {
        return categories.findByIdAndUserId(id, userId).orElseThrow(CategoryNotFoundException::new);
    }

    // Trims the name and squashes repeated spaces: "  Eating   out " becomes "Eating out".
    private static String tidy(String name) {
        return name.strip().replaceAll("\\s+", " ");
    }
}
