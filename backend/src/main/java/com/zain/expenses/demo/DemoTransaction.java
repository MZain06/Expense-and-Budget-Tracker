package com.zain.expenses.demo;

import java.math.BigDecimal;
import java.time.LocalDate;

record DemoTransaction(String category, BigDecimal amount, LocalDate date, String note) {}
