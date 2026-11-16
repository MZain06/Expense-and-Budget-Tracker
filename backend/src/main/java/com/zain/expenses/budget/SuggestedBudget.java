package com.zain.expenses.budget;

import java.math.BigDecimal;

/** A suggested monthly limit for one spending category, from what it usually costs. */
public record SuggestedBudget(
        Long categoryId, String categoryName, BigDecimal usualSpending, BigDecimal limit) {}
