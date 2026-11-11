package com.zain.expenses.demo;

import com.zain.expenses.transaction.Direction;
import java.math.BigDecimal;
import java.time.LocalDate;

record DemoTransaction(
        String category, Direction direction, BigDecimal amount, LocalDate date, String note) {}
