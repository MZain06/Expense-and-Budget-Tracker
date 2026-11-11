package com.zain.expenses.demo;

import com.zain.expenses.transaction.Direction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

final class DemoData {

    static final List<String> CATEGORIES =
            List.of("Bills", "Entertainment", "Food", "Health", "Other", "Shopping", "Transport");

    static final List<String> INCOME_CATEGORIES = List.of("Other income", "Salary");

    static final Map<String, BigDecimal> MONTHLY_BUDGETS =
            Map.of(
                    "Bills", new BigDecimal("140.00"),
                    "Entertainment", new BigDecimal("45.00"),
                    "Food", new BigDecimal("380.00"),
                    "Health", new BigDecimal("20.00"),
                    "Shopping", new BigDecimal("120.00"),
                    "Transport", new BigDecimal("100.00"));

    private DemoData() {}

    static List<DemoTransaction> lastTwelveMonths(LocalDate today) {
        YearMonth thisMonth = YearMonth.from(today);
        List<DemoTransaction> transactions = new ArrayList<>();
        for (int monthsAgo = 11; monthsAgo >= 0; monthsAgo--) {
            for (DemoTransaction transaction : month(thisMonth.minusMonths(monthsAgo))) {
                if (!transaction.date().isAfter(today)) {
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }

    static List<DemoTransaction> month(YearMonth month) {
        Month name = month.getMonth();
        boolean winter = name == Month.DECEMBER || name == Month.JANUARY || name == Month.FEBRUARY;
        boolean festive = name == Month.NOVEMBER || name == Month.DECEMBER;
        MonthBuilder builder = new MonthBuilder(month);

        builder.fixed("Bills", winter ? 96 : 68, 1, "Energy bill");
        builder.fixed("Health", 24.99, 2, "Gym membership");
        builder.fixed("Bills", 30, 5, "Broadband");
        builder.fixed("Bills", 18, 12, "Phone contract");
        builder.fixed("Entertainment", 11.99, 15, "Spotify");
        builder.fixed("Entertainment", 10.99, 20, "Netflix");

        builder.weekly("Food", 38, 72, "Tesco", "Sainsbury's", "Aldi");
        builder.onWeekdays("Food", 0.3, 3, 14, "Pret", "Greggs", "Costa");
        builder.onWeekdays("Transport", 0.55, 2.8, 7.5, "TfL", "Bus fare");
        builder.some("Food", 1, 3, 15, 32, "Deliveroo", "Just Eat");
        builder.some("Transport", 0, 2, 18, 46, "Trainline");
        builder.some("Entertainment", 0, 3, 9, 45, "Cinema", "Steam", "Gig tickets");
        builder.some("Shopping", 1, 3, 12, 85, "Amazon", "Uniqlo", "ASOS");
        if (festive) {
            builder.some("Shopping", 4, 6, 20, 70, "Christmas presents");
        }
        builder.some("Health", 0, 1, 4, 16, "Boots");
        builder.some("Other", 0, 2, 6, 35, "Haircut", "Gift", "Post Office");

        // Money in comes last, so the spending above stays the same as before it was added.
        builder.received("Salary", 1350, 1, "Salary");
        builder.someReceived("Other income", 0, 2, 8, 35, "Vinted sale", "Cashback");
        builder.refundOne("Shopping", 0.3);
        return builder.transactions;
    }

    private static final class MonthBuilder {

        private final YearMonth month;
        private final Random random;
        private final List<DemoTransaction> transactions = new ArrayList<>();

        MonthBuilder(YearMonth month) {
            this.month = month;
            this.random = new Random(month.getYear() * 100L + month.getMonthValue());
        }

        void fixed(String category, double amount, int day, String note) {
            add(category, Direction.OUT, pounds(amount), dayOfMonth(day), note);
        }

        void received(String category, double amount, int day, String note) {
            add(category, Direction.IN, pounds(amount), dayOfMonth(day), note);
        }

        void weekly(String category, double min, double max, String... notes) {
            for (int day = 1 + random.nextInt(7); day <= month.lengthOfMonth(); day += 7) {
                add(
                        category,
                        Direction.OUT,
                        pounds(between(min, max)),
                        month.atDay(day),
                        pick(notes));
            }
        }

        void onWeekdays(String category, double chance, double min, double max, String... notes) {
            for (int day = 1; day <= month.lengthOfMonth(); day++) {
                DayOfWeek weekday = month.atDay(day).getDayOfWeek();
                boolean weekend = weekday == DayOfWeek.SATURDAY || weekday == DayOfWeek.SUNDAY;
                if (!weekend && random.nextDouble() < chance) {
                    add(
                            category,
                            Direction.OUT,
                            pounds(between(min, max)),
                            month.atDay(day),
                            pick(notes));
                }
            }
        }

        void some(String category, int fewest, int most, double min, double max, String... notes) {
            several(category, Direction.OUT, fewest, most, min, max, notes);
        }

        void someReceived(
                String category, int fewest, int most, double min, double max, String... notes) {
            several(category, Direction.IN, fewest, most, min, max, notes);
        }

        /** Sometimes sends back one of this month's purchases a few days after it was made. */
        void refundOne(String category, double chance) {
            List<DemoTransaction> purchases =
                    transactions.stream()
                            .filter(t -> t.category().equals(category))
                            .filter(t -> t.direction() == Direction.OUT)
                            .toList();
            if (purchases.isEmpty() || random.nextDouble() >= chance) {
                return;
            }
            DemoTransaction purchase = purchases.get(random.nextInt(purchases.size()));
            LocalDate refunded = purchase.date().plusDays(3 + random.nextInt(8));
            if (YearMonth.from(refunded).equals(month)) {
                add(
                        category,
                        Direction.IN,
                        purchase.amount(),
                        refunded,
                        purchase.note() + " refund");
            }
        }

        private void several(
                String category,
                Direction direction,
                int fewest,
                int most,
                double min,
                double max,
                String... notes) {
            int count = fewest + random.nextInt(most - fewest + 1);
            for (int i = 0; i < count; i++) {
                int day = 1 + random.nextInt(month.lengthOfMonth());
                add(category, direction, pounds(between(min, max)), month.atDay(day), pick(notes));
            }
        }

        private LocalDate dayOfMonth(int day) {
            return month.atDay(Math.min(day, month.lengthOfMonth()));
        }

        private double between(double min, double max) {
            return min + random.nextDouble() * (max - min);
        }

        private String pick(String... notes) {
            return notes[random.nextInt(notes.length)];
        }

        private void add(
                String category,
                Direction direction,
                BigDecimal amount,
                LocalDate date,
                String note) {
            transactions.add(new DemoTransaction(category, direction, amount, date, note));
        }

        private static BigDecimal pounds(double amount) {
            return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
        }
    }
}
