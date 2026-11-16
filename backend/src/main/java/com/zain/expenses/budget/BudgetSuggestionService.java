package com.zain.expenses.budget;

import static java.util.Comparator.comparing;

import com.zain.expenses.transaction.CategoryIncome;
import com.zain.expenses.transaction.CategorySpending;
import com.zain.expenses.transaction.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BudgetSuggestionService {

    static final int MONTHS_LOOKED_AT = 3;

    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal STEP = new BigDecimal("5");

    private final TransactionRepository transactions;

    public BudgetSuggestionService(TransactionRepository transactions) {
        this.transactions = transactions;
    }

    /**
     * Suggests a limit for each spending category from the three months before {@code month}: what
     * it usually costs, rounded up to the next £5.
     */
    @Transactional(readOnly = true)
    public BudgetSuggestions suggest(Long userId, YearMonth month) {
        LocalDate from = month.minusMonths(MONTHS_LOOKED_AT).atDay(1);
        LocalDate to = month.minusMonths(1).atEndOfMonth();

        // A user who started recently has fewer months to go on, so only those months count.
        List<String> basedOn =
                transactions.spendingByDay(userId, from, to).stream()
                        .map(day -> YearMonth.from(day.day()).toString())
                        .distinct()
                        .sorted()
                        .toList();
        if (basedOn.isEmpty()) {
            return new BudgetSuggestions(basedOn, ZERO, ZERO, ZERO, List.of());
        }
        int months = basedOn.size();

        List<CategorySpending> spending = transactions.spendingByCategory(userId, from, to);
        List<SuggestedBudget> budgets =
                spending.stream()
                        .filter(category -> category.spent().signum() > 0)
                        .map(
                                category -> {
                                    BigDecimal usual = average(category.spent(), months);
                                    return new SuggestedBudget(
                                            category.categoryId(),
                                            category.categoryName(),
                                            usual,
                                            roundUpToFive(usual));
                                })
                        .sorted(comparing(SuggestedBudget::categoryName))
                        .toList();
        BigDecimal income =
                sum(
                        transactions.incomeByCategory(userId, from, to).stream()
                                .map(CategoryIncome::received));

        return new BudgetSuggestions(
                basedOn,
                average(sum(spending.stream().map(CategorySpending::spent)), months),
                average(income, months),
                sum(budgets.stream().map(SuggestedBudget::limit)),
                budgets);
    }

    private static BigDecimal average(BigDecimal total, int months) {
        return total.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal roundUpToFive(BigDecimal amount) {
        return amount.divide(STEP, 0, RoundingMode.CEILING).multiply(STEP).setScale(2);
    }

    private static BigDecimal sum(Stream<BigDecimal> amounts) {
        return amounts.reduce(ZERO, BigDecimal::add);
    }
}
