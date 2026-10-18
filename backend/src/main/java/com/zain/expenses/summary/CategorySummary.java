package com.zain.expenses.summary;

import java.math.BigDecimal;

/**
 * One category's spending against its budget. budget and remaining are null when no budget is set.
 */
public record CategorySummary(
        Long categoryId,
        String categoryName,
        BigDecimal spent,
        BigDecimal budget,
        BigDecimal remaining) {

    static CategorySummary of(
            Long categoryId, String categoryName, BigDecimal spent, BigDecimal budget) {
        BigDecimal remaining = budget == null ? null : budget.subtract(spent);
        return new CategorySummary(categoryId, categoryName, spent, budget, remaining);
    }
}
