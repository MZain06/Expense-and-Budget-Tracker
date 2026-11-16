package com.zain.expenses.budget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zain.expenses.transaction.CategoryIncome;
import com.zain.expenses.transaction.CategorySpending;
import com.zain.expenses.transaction.DailySpending;
import com.zain.expenses.transaction.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for suggesting November's budgets from August to October. */
@ExtendWith(MockitoExtension.class)
class BudgetSuggestionServiceTests {

    private static final Long USER_ID = 1L;
    private static final YearMonth NOVEMBER = YearMonth.of(2026, 11);
    private static final LocalDate FROM = LocalDate.of(2026, 8, 1);
    private static final LocalDate TO = LocalDate.of(2026, 10, 31);

    @Mock private TransactionRepository transactions;

    private BudgetSuggestionService service;

    @BeforeEach
    void createService() {
        service = new BudgetSuggestionService(transactions);
    }

    @Test
    void suggestsWhatEachCategoryUsuallyCostsRoundedUpToTheNextFivePounds() {
        givenSpendingOn("2026-08-04", "2026-09-10", "2026-10-02");
        when(transactions.spendingByCategory(USER_ID, FROM, TO))
                .thenReturn(
                        List.of(
                                new CategorySpending(3L, "Food", new BigDecimal("1057.23")),
                                new CategorySpending(1L, "Bills", new BigDecimal("348.00")),
                                new CategorySpending(5L, "Shopping", new BigDecimal("-20.00"))));
        when(transactions.incomeByCategory(USER_ID, FROM, TO))
                .thenReturn(List.of(new CategoryIncome(8L, "Salary", new BigDecimal("4050.00"))));

        BudgetSuggestions suggestions = service.suggest(USER_ID, NOVEMBER);

        assertThat(suggestions.basedOn()).containsExactly("2026-08", "2026-09", "2026-10");
        // Shopping had more refunded than spent, so there's nothing to suggest for it.
        assertThat(suggestions.budgets())
                .extracting(SuggestedBudget::categoryName)
                .containsExactly("Bills", "Food");
        SuggestedBudget food = suggestions.budgets().get(1);
        assertThat(food.usualSpending()).isEqualByComparingTo("352.41");
        assertThat(food.limit()).isEqualByComparingTo("355.00");
        assertThat(suggestions.budgets().get(0).limit()).isEqualByComparingTo("120.00");
        assertThat(suggestions.total()).isEqualByComparingTo("475.00");
        assertThat(suggestions.usualSpending()).isEqualByComparingTo("461.74");
        assertThat(suggestions.usualIncome()).isEqualByComparingTo("1350.00");
    }

    @Test
    void averagesOnlyOverMonthsThatHadSpending() {
        givenSpendingOn("2026-10-02", "2026-10-20");
        when(transactions.spendingByCategory(USER_ID, FROM, TO))
                .thenReturn(List.of(new CategorySpending(3L, "Food", new BigDecimal("298.00"))));

        BudgetSuggestions suggestions = service.suggest(USER_ID, NOVEMBER);

        assertThat(suggestions.basedOn()).containsExactly("2026-10");
        assertThat(suggestions.budgets().get(0).limit()).isEqualByComparingTo("300.00");
        assertThat(suggestions.usualIncome()).isEqualByComparingTo("0.00");
    }

    @Test
    void suggestsNothingWithoutRecentSpending() {
        BudgetSuggestions suggestions = service.suggest(USER_ID, NOVEMBER);

        assertThat(suggestions.basedOn()).isEmpty();
        assertThat(suggestions.budgets()).isEmpty();
        verify(transactions, never()).spendingByCategory(any(), any(), any());
    }

    private void givenSpendingOn(String... days) {
        when(transactions.spendingByDay(USER_ID, FROM, TO))
                .thenReturn(
                        Arrays.stream(days)
                                .map(
                                        day ->
                                                new DailySpending(
                                                        LocalDate.parse(day),
                                                        new BigDecimal("10.00")))
                                .toList());
    }
}
