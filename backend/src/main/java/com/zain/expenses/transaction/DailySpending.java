package com.zain.expenses.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

/** How much a user spent on one day. */
public record DailySpending(LocalDate day, BigDecimal spent) {}
