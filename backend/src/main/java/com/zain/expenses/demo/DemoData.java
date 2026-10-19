package com.zain.expenses.demo;

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
            add(category, amount, month.atDay(Math.min(day, month.lengthOfMonth())), note);
        }

        void weekly(String category, double min, double max, String... notes) {
            for (int day = 1 + random.nextInt(7); day <= month.lengthOfMonth(); day += 7) {
                add(category, between(min, max), month.atDay(day), pick(notes));
            }
        }

        void onWeekdays(String category, double chance, double min, double max, String... notes) {
            for (int day = 1; day <= month.lengthOfMonth(); day++) {
                DayOfWeek weekday = month.atDay(day).getDayOfWeek();
                boolean weekend = weekday == DayOfWeek.SATURDAY || weekday == DayOfWeek.SUNDAY;
                if (!weekend && random.nextDouble() < chance) {
                    add(category, between(min, max), month.atDay(day), pick(notes));
                }
            }
        }

        void some(String category, int fewest, int most, double min, double max, String... notes) {
            int count = fewest + random.nextInt(most - fewest + 1);
            for (int i = 0; i < count; i++) {
                int day = 1 + random.nextInt(month.lengthOfMonth());
                add(category, between(min, max), month.atDay(day), pick(notes));
            }
        }

        private double between(double min, double max) {
            return min + random.nextDouble() * (max - min);
        }

        private String pick(String... notes) {
            return notes[random.nextInt(notes.length)];
        }

        private void add(String category, double amount, LocalDate date, String note) {
            BigDecimal pounds = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
            transactions.add(new DemoTransaction(category, pounds, date, note));
        }
    }
}
