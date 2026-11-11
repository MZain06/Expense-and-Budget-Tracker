package com.zain.expenses.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for how money in and money out add up. */
class NetTotalsTests {

    private static final LocalDate OCTOBER_3 = LocalDate.of(2026, 10, 3);
    private static final LocalDate OCTOBER_7 = LocalDate.of(2026, 10, 7);

    @Test
    void refundsComeOffACategorysSpending() {
        List<CategorySpending> spending =
                CategorySpending.netOf(
                        List.of(
                                total(3L, "Shopping", Direction.OUT, "50.00"),
                                total(1L, "Food", Direction.OUT, "12.50"),
                                total(3L, "Shopping", Direction.IN, "20.00")));

        assertThat(spending)
                .extracting(CategorySpending::categoryName)
                .containsExactly("Shopping", "Food");
        assertThat(spending.get(0).spent()).isEqualByComparingTo("30.00");
        assertThat(spending.get(1).spent()).isEqualByComparingTo("12.50");
    }

    @Test
    void moneyPaidBackComesOffIncome() {
        List<CategoryIncome> income =
                CategoryIncome.netOf(
                        List.of(
                                total(8L, "Salary", Direction.IN, "1350.00"),
                                total(8L, "Salary", Direction.OUT, "50.00")));

        assertThat(income).hasSize(1);
        assertThat(income.get(0).received()).isEqualByComparingTo("1300.00");
    }

    @Test
    void spendingPerDayIsInDateOrderWithRefundsTakenOff() {
        List<DailySpending> days =
                DailySpending.netOf(
                        List.of(
                                new DailyTotal(OCTOBER_7, Direction.IN, new BigDecimal("20.00")),
                                new DailyTotal(OCTOBER_3, Direction.OUT, new BigDecimal("50.00")),
                                new DailyTotal(OCTOBER_3, Direction.IN, new BigDecimal("5.00"))));

        assertThat(days).extracting(DailySpending::day).containsExactly(OCTOBER_3, OCTOBER_7);
        assertThat(days.get(0).spent()).isEqualByComparingTo("45.00");
        assertThat(days.get(1).spent()).isEqualByComparingTo("-20.00");
    }

    private static CategoryTotal total(
            Long categoryId, String name, Direction direction, String amount) {
        return new CategoryTotal(categoryId, name, direction, new BigDecimal(amount));
    }
}
