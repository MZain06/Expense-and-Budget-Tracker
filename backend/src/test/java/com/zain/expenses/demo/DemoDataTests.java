package com.zain.expenses.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;

class DemoDataTests {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);

    @Test
    void coversTheLastTwelveMonthsWithNothingAfterToday() {
        List<DemoTransaction> transactions = DemoData.lastTwelveMonths(TODAY);

        assertThat(transactions)
                .allSatisfy(
                        t -> {
                            assertThat(t.date()).isBetween(LocalDate.of(2025, 11, 1), TODAY);
                            assertThat(t.amount()).isPositive();
                            assertThat(t.amount().scale()).isEqualTo(2);
                            assertThat(DemoData.CATEGORIES).contains(t.category());
                        });
        long months = transactions.stream().map(t -> YearMonth.from(t.date())).distinct().count();
        assertThat(months).isEqualTo(12);
    }

    @Test
    void onlyBudgetsForItsOwnCategories() {
        assertThat(DemoData.CATEGORIES).containsAll(DemoData.MONTHLY_BUDGETS.keySet());
    }

    @Test
    void givesTheSameDataEveryTime() {
        assertThat(DemoData.lastTwelveMonths(TODAY)).isEqualTo(DemoData.lastTwelveMonths(TODAY));
    }
}
