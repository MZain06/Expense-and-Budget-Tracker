package com.zain.expenses.budget;

import java.math.BigDecimal;

public record BudgetResponse(Long categoryId, String categoryName, BigDecimal limit) {

    public static BudgetResponse from(Budget budget) {
        return new BudgetResponse(
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getLimitAmount());
    }
}
