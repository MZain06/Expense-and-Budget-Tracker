package com.zain.expenses.insights;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zain.expenses.transaction.CategorySpending;
import com.zain.expenses.transaction.DailySpending;
import com.zain.expenses.transaction.TransactionRepository;
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

/** Unit tests for the insights maths, with "today" fixed at 16 October 2026. */
@ExtendWith(MockitoExtension.class)
class InsightsServiceTests {

    private static final Long USER_ID = 1L;
    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);
    private static final YearMonth SEPTEMBER = YearMonth.of(2026, 9);

    @Mock private TransactionRepository transactions;

    private InsightsService service;

    @BeforeEach
    void createServiceWithAFixedClock() {
        Clock midOctober = Clock.fixed(Instant.parse("2026-10-16T12:00:00Z"), ZoneOffset.UTC);
        service = new InsightsService(transactions, midOctober);
    }

    @Test
    void trendCoversTwelveMonthsOldestFirstWithEmptyMonthsAsZero() {
        givenDailySpending(
                day("2025-11-03", "10.00"),
                day("2026-09-28", "40.00"),
                day("2026-10-03", "12.50"),
                day("2026-10-15", "2.80"));

        List<MonthTotal> trend = service.insights(USER_ID, OCTOBER).trend();

        assertThat(trend).hasSize(12);
        assertThat(trend.get(0).month()).isEqualTo("2025-11");
        assertThat(trend.get(0).spent()).isEqualByComparingTo("10.00");
        assertThat(trend.get(1).spent()).isEqualByComparingTo("0.00");
        assertThat(trend.get(10).spent()).isEqualByComparingTo("40.00");
        assertThat(trend.get(11).month()).isEqualTo("2026-10");
        assertThat(trend.get(11).spent()).isEqualByComparingTo("15.30");
    }

    @Test
    void projectsTheCurrentMonthFromItsDailyAverage() {
        givenDailySpending(day("2026-10-01", "650.00"), day("2026-10-10", "132.88"));

        Insights insights = service.insights(USER_ID, OCTOBER);

        // 782.88 over the 16 days so far, then 31 days at that pace.
        assertThat(insights.spent()).isEqualByComparingTo("782.88");
        assertThat(insights.averagePerDay()).isEqualByComparingTo("48.93");
        assertThat(insights.projectedTotal()).isEqualByComparingTo("1516.83");
    }

    @Test
    void aFinishedMonthHasNoProjectionAndAveragesOverEveryDay() {
        givenDailySpending(day("2026-09-05", "100.00"), day("2026-09-20", "200.00"));

        Insights insights = service.insights(USER_ID, SEPTEMBER);

        assertThat(insights.averagePerDay()).isEqualByComparingTo("10.00");
        assertThat(insights.projectedTotal()).isNull();
    }

    @Test
    void comparesTheMonthSoFarWithTheSameDaysLastMonth() {
        givenDailySpending(
                day("2026-09-10", "20.00"),
                day("2026-09-20", "100.00"),
                day("2026-10-05", "35.00"));

        Insights insights = service.insights(USER_ID, OCTOBER);

        assertThat(insights.spentLastMonth()).isEqualByComparingTo("20.00");
        verify(transactions)
                .spendingByCategory(USER_ID, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 16));
    }

    @Test
    void averagesTheThreeMonthsBeforeThisOne() {
        givenDailySpending(
                day("2026-06-15", "999.00"),
                day("2026-07-02", "300.00"),
                day("2026-08-30", "150.00"));

        Insights insights = service.insights(USER_ID, OCTOBER);

        // July, August and September (which had nothing): (300 + 150 + 0) / 3.
        assertThat(insights.threeMonthAverage()).isEqualByComparingTo("150.00");
    }

    @Test
    void ranksTheBiggestChangesAndLeavesOutCategoriesThatDidNotMove() {
        when(transactions.spendingByCategory(eq(USER_ID), eq(OCTOBER.atDay(1)), any()))
                .thenReturn(
                        List.of(
                                spending(1L, "Bills", "650.00"),
                                spending(2L, "Entertainment", "10.00"),
                                spending(3L, "Food", "175.50"),
                                spending(7L, "Transport", "2.80")));
        when(transactions.spendingByCategory(eq(USER_ID), eq(SEPTEMBER.atDay(1)), any()))
                .thenReturn(
                        List.of(
                                spending(1L, "Bills", "650.00"),
                                spending(3L, "Food", "55.10"),
                                spending(4L, "Health", "5.00"),
                                spending(7L, "Transport", "32.80")));

        List<CategoryChange> changes = service.insights(USER_ID, OCTOBER).biggestChanges();

        assertThat(changes)
                .extracting(CategoryChange::categoryName)
                .containsExactly("Food", "Transport", "Entertainment");
        assertThat(changes.get(0).change()).isEqualByComparingTo("120.40");
        assertThat(changes.get(1).change()).isEqualByComparingTo("-30.00");
    }

    private void givenDailySpending(DailySpending... days) {
        when(transactions.spendingByDay(any(), any(), any())).thenReturn(List.of(days));
    }

    private static DailySpending day(String date, String amount) {
        return new DailySpending(LocalDate.parse(date), new BigDecimal(amount));
    }

    private static CategorySpending spending(Long id, String name, String amount) {
        return new CategorySpending(id, name, new BigDecimal(amount));
    }
}
