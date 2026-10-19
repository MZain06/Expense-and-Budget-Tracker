package com.zain.expenses.insights;

import java.math.BigDecimal;
import java.util.List;

/**
 * Patterns in one month's spending.
 *
 * <p>While a month is in progress, "last month" means the same days of last month, so the
 * comparison is like for like: the 1st to the 16th of this month against the 1st to the 16th of
 * last month. projectedTotal is null unless the month is in progress.
 */
public record Insights(
        BigDecimal spent,
        BigDecimal spentLastMonth,
        BigDecimal averagePerDay,
        BigDecimal projectedTotal,
        BigDecimal threeMonthAverage,
        List<MonthTotal> trend,
        List<CategoryChange> biggestChanges) {}
