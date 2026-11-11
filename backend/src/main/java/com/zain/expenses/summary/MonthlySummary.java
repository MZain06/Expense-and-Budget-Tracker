package com.zain.expenses.summary;

import com.zain.expenses.transaction.CategoryIncome;
import java.math.BigDecimal;
import java.util.List;

/**
 * A month at a glance. totalBudget adds up every budget set for the month. Spending categories with
 * no spending and no budget are left out of categories, and income categories with nothing received
 * are left out of income.
 */
public record MonthlySummary(
        BigDecimal totalSpent,
        BigDecimal totalBudget,
        BigDecimal totalIncome,
        List<CategorySummary> categories,
        List<CategoryIncome> income) {}
