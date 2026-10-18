package com.zain.expenses.budget;

import com.zain.expenses.category.Category;
import com.zain.expenses.category.CategoryNotFoundException;
import com.zain.expenses.category.CategoryRepository;
import com.zain.expenses.user.UserRepository;
import java.time.Clock;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BudgetService {

    private final BudgetRepository budgets;
    private final CategoryRepository categories;
    private final UserRepository users;
    private final Clock clock;

    public BudgetService(
            BudgetRepository budgets,
            CategoryRepository categories,
            UserRepository users,
            Clock clock) {
        this.budgets = budgets;
        this.categories = categories;
        this.users = users;
        this.clock = clock;
    }

    /** Lists one month's budgets in category order. Defaults to this month. */
    @Transactional(readOnly = true)
    public List<BudgetResponse> list(Long userId, YearMonth month) {
        YearMonth period = month != null ? month : YearMonth.now(clock);
        return budgets.findForMonth(userId, period.atDay(1)).stream()
                .map(BudgetResponse::from)
                .toList();
    }

    /** Sets a category's limit for a month: creates the budget, or replaces its old limit. */
    @Transactional
    public BudgetResponse set(
            Long userId, YearMonth month, Long categoryId, BudgetRequest request) {
        Category category = findOwnedCategory(userId, categoryId);
        Optional<Budget> existing =
                budgets.findByUserIdAndCategoryIdAndMonth(userId, categoryId, month.atDay(1));
        if (existing.isPresent()) {
            existing.get().changeLimit(request.limit());
            return BudgetResponse.from(existing.get());
        }
        Budget created =
                budgets.save(
                        new Budget(
                                users.getReferenceById(userId), category, month, request.limit()));
        return BudgetResponse.from(created);
    }

    /** Removes a category's budget for a month. Removing one that isn't set does nothing. */
    @Transactional
    public void remove(Long userId, YearMonth month, Long categoryId) {
        findOwnedCategory(userId, categoryId);
        budgets.findByUserIdAndCategoryIdAndMonth(userId, categoryId, month.atDay(1))
                .ifPresent(budgets::delete);
    }

    /** Copies last month's budgets into this month, skipping categories that already have one. */
    @Transactional
    public List<BudgetResponse> copyFromPreviousMonth(Long userId, YearMonth month) {
        Set<Long> alreadySet =
                budgets.findForMonth(userId, month.atDay(1)).stream()
                        .map(budget -> budget.getCategory().getId())
                        .collect(Collectors.toSet());
        List<Budget> copies =
                budgets.findForMonth(userId, month.minusMonths(1).atDay(1)).stream()
                        .filter(previous -> !alreadySet.contains(previous.getCategory().getId()))
                        .map(
                                previous ->
                                        new Budget(
                                                users.getReferenceById(userId),
                                                previous.getCategory(),
                                                month,
                                                previous.getLimitAmount()))
                        .toList();
        budgets.saveAll(copies);
        return list(userId, month);
    }

    private Category findOwnedCategory(Long userId, Long categoryId) {
        return categories
                .findByIdAndUserId(categoryId, userId)
                .orElseThrow(CategoryNotFoundException::new);
    }
}
