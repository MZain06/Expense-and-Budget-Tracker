package com.zain.expenses.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.zain.expenses.transaction.Direction;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class DemoDataTests {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);

    @Test
    void coversTheLastTwelveMonthsWithNothingAfterToday() {
        List<DemoTransaction> transactions = DemoData.lastTwelveMonths(TODAY);
        List<String> categories =
                Stream.concat(DemoData.CATEGORIES.stream(), DemoData.INCOME_CATEGORIES.stream())
                        .toList();

        assertThat(transactions)
                .allSatisfy(
                        t -> {
                            assertThat(t.date()).isBetween(LocalDate.of(2025, 11, 1), TODAY);
                            assertThat(t.amount()).isPositive();
                            assertThat(t.amount().scale()).isEqualTo(2);
                            assertThat(categories).contains(t.category());
                        });
        long months = transactions.stream().map(t -> YearMonth.from(t.date())).distinct().count();
        assertThat(months).isEqualTo(12);
    }

    @Test
    void paysASalaryEveryMonthAndOnlyIncomeComesInThere() {
        List<DemoTransaction> transactions = DemoData.lastTwelveMonths(TODAY);

        assertThat(transactions)
                .filteredOn(t -> DemoData.INCOME_CATEGORIES.contains(t.category()))
                .allSatisfy(t -> assertThat(t.direction()).isEqualTo(Direction.IN));
        long paydays = transactions.stream().filter(t -> t.category().equals("Salary")).count();
        assertThat(paydays).isEqualTo(12);
    }

    @Test
    void refundsGiveBackAnEarlierPurchaseFromTheSameMonth() {
        List<DemoTransaction> transactions = DemoData.lastTwelveMonths(TODAY);
        List<DemoTransaction> refunds =
                transactions.stream()
                        .filter(t -> t.direction() == Direction.IN)
                        .filter(t -> DemoData.CATEGORIES.contains(t.category()))
                        .toList();

        assertThat(refunds)
                .isNotEmpty()
                .allSatisfy(
                        refund ->
                                assertThat(transactions)
                                        .anySatisfy(
                                                purchase -> {
                                                    assertThat(purchase.direction())
                                                            .isEqualTo(Direction.OUT);
                                                    assertThat(purchase.category())
                                                            .isEqualTo(refund.category());
                                                    assertThat(purchase.amount())
                                                            .isEqualTo(refund.amount());
                                                    assertThat(purchase.date())
                                                            .isBefore(refund.date());
                                                    assertThat(YearMonth.from(purchase.date()))
                                                            .isEqualTo(
                                                                    YearMonth.from(refund.date()));
                                                }));
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
