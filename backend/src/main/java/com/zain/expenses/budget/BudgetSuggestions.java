package com.zain.expenses.budget;

import java.math.BigDecimal;
import java.util.List;

/**
 * Budgets suggested from recent spending. basedOn lists the months used, which are the ones with
 * spending among the three before the budget month. usualSpending and usualIncome are monthly
 * averages over those months, and total adds up the suggested limits.
 */
public record BudgetSuggestions(
        List<String> basedOn,
        BigDecimal usualSpending,
        BigDecimal usualIncome,
        BigDecimal total,
        List<SuggestedBudget> budgets) {}
