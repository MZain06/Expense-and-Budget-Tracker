package com.zain.expenses.summary;

import static java.util.Comparator.comparing;

import com.zain.expenses.budget.Budget;
import com.zain.expenses.budget.BudgetRepository;
import com.zain.expenses.transaction.CategorySpending;
import com.zain.expenses.transaction.TransactionRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.YearMonth;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SummaryService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final TransactionRepository transactions;
    private final BudgetRepository budgets;
    private final Clock clock;

    public SummaryService(
            TransactionRepository transactions, BudgetRepository budgets, Clock clock) {
        this.transactions = transactions;
        this.budgets = budgets;
        this.clock = clock;
    }

    /** Spending against budget for each category in one month. Defaults to this month. */
    @Transactional(readOnly = true)
    public MonthlySummary summarise(Long userId, YearMonth month) {
        YearMonth period = month != null ? month : YearMonth.now(clock);

        Map<Long, String> names = new HashMap<>();
        Map<Long, BigDecimal> spent = new HashMap<>();
        for (CategorySpending spending :
                transactions.spendingByCategory(userId, period.atDay(1), period.atEndOfMonth())) {
            names.put(spending.categoryId(), spending.categoryName());
            spent.put(spending.categoryId(), spending.spent());
        }

        Map<Long, BigDecimal> limits = new HashMap<>();
        for (Budget budget : budgets.findForMonth(userId, period.atDay(1))) {
            names.put(budget.getCategory().getId(), budget.getCategory().getName());
            limits.put(budget.getCategory().getId(), budget.getLimitAmount());
        }

        // Biggest spending first; ties (like several budgets with nothing spent) go alphabetically.
        List<CategorySummary> categories =
                names.keySet().stream()
                        .map(
                                id ->
                                        CategorySummary.of(
                                                id,
                                                names.get(id),
                                                spent.getOrDefault(id, ZERO),
                                                limits.get(id)))
                        .sorted(
                                comparing(CategorySummary::spent)
                                        .reversed()
                                        .thenComparing(CategorySummary::categoryName))
                        .toList();
        return new MonthlySummary(total(spent.values()), total(limits.values()), categories);
    }

    private static BigDecimal total(Collection<BigDecimal> amounts) {
        return amounts.stream().reduce(ZERO, BigDecimal::add);
    }
}
