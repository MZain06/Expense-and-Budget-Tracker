package com.zain.expenses.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** How much a user spent on one day, less any refunds. */
public record DailySpending(LocalDate day, BigDecimal spent) {

    static List<DailySpending> netOf(List<DailyTotal> totals) {
        Map<LocalDate, BigDecimal> byDay = new TreeMap<>();
        for (DailyTotal total : totals) {
            byDay.merge(total.day(), total.towards(Direction.OUT), BigDecimal::add);
        }
        return byDay.entrySet().stream()
                .map(day -> new DailySpending(day.getKey(), day.getValue()))
                .toList();
    }
}
