package com.zain.expenses.insights;

import java.math.BigDecimal;

/** How one category's spending moved between last month and this one. */
public record CategoryChange(
        Long categoryId,
        String categoryName,
        BigDecimal thisMonth,
        BigDecimal lastMonth,
        BigDecimal change) {}
