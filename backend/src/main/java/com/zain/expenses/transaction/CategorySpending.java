package com.zain.expenses.transaction;

import java.math.BigDecimal;

/** How much a user spent in one category over a period. */
public record CategorySpending(Long categoryId, String categoryName, BigDecimal spent) {}
