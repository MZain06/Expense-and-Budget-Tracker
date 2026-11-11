package com.zain.expenses.transaction;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** How much a user spent in one category over a period, less any refunds. */
public record CategorySpending(Long categoryId, String categoryName, BigDecimal spent) {

    static List<CategorySpending> netOf(List<CategoryTotal> totals) {
        Map<Long, CategorySpending> byCategory = new LinkedHashMap<>();
        for (CategoryTotal total : totals) {
            CategorySpending part =
                    new CategorySpending(
                            total.categoryId(), total.categoryName(), total.towards(Direction.OUT));
            byCategory.merge(total.categoryId(), part, CategorySpending::plus);
        }
        return List.copyOf(byCategory.values());
    }

    private CategorySpending plus(CategorySpending other) {
        return new CategorySpending(categoryId, categoryName, spent.add(other.spent));
    }
}
