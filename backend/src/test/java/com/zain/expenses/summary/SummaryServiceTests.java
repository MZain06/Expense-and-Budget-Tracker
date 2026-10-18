package com.zain.expenses.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zain.expenses.budget.Budget;
import com.zain.expenses.budget.BudgetRepository;
import com.zain.expenses.category.Category;
import com.zain.expenses.transaction.CategorySpending;
import com.zain.expenses.transaction.TransactionRepository;
import com.zain.expenses.user.User;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** Fast unit tests for the summary maths: no Spring, no database. */
@ExtendWith(MockitoExtension.class)
class SummaryServiceTests {

    private static final Long USER_ID = 1L;
    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);

    @Mock private TransactionRepository transactions;
    @Mock private BudgetRepository budgets;

    private SummaryService service;
    private final User user = new User("zain@example.com", "hash");

    @BeforeEach
    void createServiceWithAFixedClock() {
        Clock midOctober = Clock.fixed(Instant.parse("2026-10-16T12:00:00Z"), ZoneOffset.UTC);
        service = new SummaryService(transactions, budgets, midOctober);
    }

    @Test
    void combinesSpendingAndBudgetsForEachCategory() {
        when(transactions.spendingByCategory(USER_ID, OCTOBER.atDay(1), OCTOBER.atEndOfMonth()))
                .thenReturn(
                        List.of(
                                new CategorySpending(1L, "Bills", new BigDecimal("650.00")),
                                new CategorySpending(3L, "Food", new BigDecimal("55.10"))));
        when(budgets.findForMonth(USER_ID, OCTOBER.atDay(1)))
                .thenReturn(
                        List.of(budget(3L, "Food", "200.00"), budget(7L, "Transport", "50.00")));

        MonthlySummary summary = service.summarise(USER_ID, OCTOBER);

        assertThat(summary.totalSpent()).isEqualByComparingTo("705.10");
        assertThat(summary.totalBudget()).isEqualByComparingTo("250.00");
        assertThat(summary.categories())
                .extracting(CategorySummary::categoryName)
                .containsExactly("Bills", "Food", "Transport");

        CategorySummary bills = summary.categories().get(0);
        assertThat(bills.budget()).isNull();
        assertThat(bills.remaining()).isNull();

        CategorySummary food = summary.categories().get(1);
        assertThat(food.spent()).isEqualByComparingTo("55.10");
        assertThat(food.remaining()).isEqualByComparingTo("144.90");

        CategorySummary transport = summary.categories().get(2);
        assertThat(transport.spent()).isEqualByComparingTo("0.00");
        assertThat(transport.remaining()).isEqualByComparingTo("50.00");
    }

    @Test
    void remainingGoesNegativeWhenACategoryIsOverBudget() {
        when(transactions.spendingByCategory(any(), any(), any()))
                .thenReturn(List.of(new CategorySpending(3L, "Food", new BigDecimal("230.00"))));
        when(budgets.findForMonth(any(), any())).thenReturn(List.of(budget(3L, "Food", "200.00")));

        CategorySummary food = service.summarise(USER_ID, OCTOBER).categories().get(0);

        assertThat(food.remaining()).isEqualByComparingTo("-30.00");
    }

    @Test
    void aMonthWithNothingInItHasZeroTotals() {
        // Unstubbed Mockito mocks return empty lists, so this month has no spending or budgets.
        MonthlySummary summary = service.summarise(USER_ID, OCTOBER);

        assertThat(summary.totalSpent()).isEqualByComparingTo("0.00");
        assertThat(summary.totalBudget()).isEqualByComparingTo("0.00");
        assertThat(summary.categories()).isEmpty();
    }

    @Test
    void defaultsToTheCurrentMonth() {
        service.summarise(USER_ID, null);

        verify(transactions)
                .spendingByCategory(USER_ID, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        verify(budgets).findForMonth(USER_ID, LocalDate.of(2026, 10, 1));
    }

    private Budget budget(long categoryId, String categoryName, String limit) {
        Category category = new Category(user, categoryName);
        // Ids normally come from the database, which a unit test doesn't have, so set one directly.
        ReflectionTestUtils.setField(category, "id", categoryId);
        return new Budget(user, category, OCTOBER, new BigDecimal(limit));
    }
}
