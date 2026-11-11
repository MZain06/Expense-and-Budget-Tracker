package com.zain.expenses.transaction;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** How much came in under one income category over a period. */
public record CategoryIncome(Long categoryId, String categoryName, BigDecimal received) {

    static List<CategoryIncome> netOf(List<CategoryTotal> totals) {
        Map<Long, CategoryIncome> byCategory = new LinkedHashMap<>();
        for (CategoryTotal total : totals) {
            CategoryIncome part =
                    new CategoryIncome(
                            total.categoryId(), total.categoryName(), total.towards(Direction.IN));
            byCategory.merge(total.categoryId(), part, CategoryIncome::plus);
        }
        return List.copyOf(byCategory.values());
    }

    private CategoryIncome plus(CategoryIncome other) {
        return new CategoryIncome(categoryId, categoryName, received.add(other.received));
    }
}
