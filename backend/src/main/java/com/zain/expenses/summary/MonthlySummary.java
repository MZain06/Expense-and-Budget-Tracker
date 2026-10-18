package com.zain.expenses.summary;

import java.math.BigDecimal;
import java.util.List;

/**
 * A month at a glance. totalBudget adds up every budget set for the month. Categories with no
 * spending and no budget are left out of the list.
 */
public record MonthlySummary(
        BigDecimal totalSpent, BigDecimal totalBudget, List<CategorySummary> categories) {}
