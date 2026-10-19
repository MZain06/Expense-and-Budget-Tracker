package com.zain.expenses.insights;

import static java.util.Comparator.comparing;

import com.zain.expenses.transaction.CategorySpending;
import com.zain.expenses.transaction.DailySpending;
import com.zain.expenses.transaction.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InsightsService {

    static final int TREND_MONTHS = 12;
    static final int MAX_CHANGES = 3;

    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final TransactionRepository transactions;
    private final Clock clock;

    public InsightsService(TransactionRepository transactions, Clock clock) {
        this.transactions = transactions;
        this.clock = clock;
    }

    /** Works out the insights for one month. Defaults to this month. */
    @Transactional(readOnly = true)
    public Insights insights(Long userId, YearMonth month) {
        LocalDate today = LocalDate.now(clock);
        YearMonth period = month != null ? month : YearMonth.from(today);
        boolean inProgress = period.equals(YearMonth.from(today));
        int daysCounted = inProgress ? today.getDayOfMonth() : period.lengthOfMonth();

        YearMonth previous = period.minusMonths(1);
        LocalDate previousEnd =
                inProgress
                        ? previous.atDay(Math.min(daysCounted, previous.lengthOfMonth()))
                        : previous.atEndOfMonth();

        // One query covers the whole 12-month window; Java adds the days up into months.
        YearMonth firstMonth = period.minusMonths(TREND_MONTHS - 1);
        List<DailySpending> days =
                transactions.spendingByDay(userId, firstMonth.atDay(1), period.atEndOfMonth());
        Map<YearMonth, BigDecimal> monthTotals = new HashMap<>();
        BigDecimal spentLastMonth = ZERO;
        for (DailySpending day : days) {
            monthTotals.merge(YearMonth.from(day.day()), day.spent(), BigDecimal::add);
            if (YearMonth.from(day.day()).equals(previous) && !day.day().isAfter(previousEnd)) {
                spentLastMonth = spentLastMonth.add(day.spent());
            }
        }

        List<MonthTotal> trend = new ArrayList<>();
        for (YearMonth m = firstMonth; !m.isAfter(period); m = m.plusMonths(1)) {
            trend.add(new MonthTotal(m.toString(), monthTotals.getOrDefault(m, ZERO)));
        }

        BigDecimal spent = monthTotals.getOrDefault(period, ZERO);
        BigDecimal averagePerDay = divide(spent, daysCounted);
        BigDecimal projectedTotal = null;
        if (inProgress) {
            // Carry on at this month's pace until the last day.
            BigDecimal wholeMonth = spent.multiply(BigDecimal.valueOf(period.lengthOfMonth()));
            projectedTotal = divide(wholeMonth, daysCounted);
        }
        BigDecimal lastThreeMonths =
                monthTotals
                        .getOrDefault(period.minusMonths(1), ZERO)
                        .add(monthTotals.getOrDefault(period.minusMonths(2), ZERO))
                        .add(monthTotals.getOrDefault(period.minusMonths(3), ZERO));

        List<CategoryChange> biggestChanges =
                biggestChanges(
                        transactions.spendingByCategory(
                                userId, period.atDay(1), period.atEndOfMonth()),
                        transactions.spendingByCategory(userId, previous.atDay(1), previousEnd));

        return new Insights(
                spent,
                spentLastMonth,
                averagePerDay,
                projectedTotal,
                divide(lastThreeMonths, 3),
                trend,
                biggestChanges);
    }

    /** The categories whose spending moved most, biggest move first, ignoring any that didn't. */
    private static List<CategoryChange> biggestChanges(
            List<CategorySpending> thisMonth, List<CategorySpending> lastMonth) {
        Map<Long, String> names = new HashMap<>();
        Map<Long, BigDecimal> now = new HashMap<>();
        Map<Long, BigDecimal> before = new HashMap<>();
        for (CategorySpending spending : thisMonth) {
            names.put(spending.categoryId(), spending.categoryName());
            now.put(spending.categoryId(), spending.spent());
        }
        for (CategorySpending spending : lastMonth) {
            names.put(spending.categoryId(), spending.categoryName());
            before.put(spending.categoryId(), spending.spent());
        }
        return names.keySet().stream()
                .map(
                        id -> {
                            BigDecimal current = now.getOrDefault(id, ZERO);
                            BigDecimal earlier = before.getOrDefault(id, ZERO);
                            return new CategoryChange(
                                    id, names.get(id), current, earlier, current.subtract(earlier));
                        })
                .filter(change -> change.change().signum() != 0)
                .sorted(
                        comparing((CategoryChange change) -> change.change().abs())
                                .reversed()
                                .thenComparing(CategoryChange::categoryName))
                .limit(MAX_CHANGES)
                .toList();
    }

    private static BigDecimal divide(BigDecimal amount, int by) {
        return amount.divide(BigDecimal.valueOf(by), 2, RoundingMode.HALF_UP);
    }
}
