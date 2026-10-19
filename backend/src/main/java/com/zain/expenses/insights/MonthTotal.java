package com.zain.expenses.insights;

import java.math.BigDecimal;

/** Total spending in one month. The month is written like "2026-10". */
public record MonthTotal(String month, BigDecimal spent) {}
