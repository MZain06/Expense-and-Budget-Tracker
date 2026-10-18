package com.zain.expenses.budget;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/** The body for setting a budget. A limit of zero means "spend nothing on this". */
public record BudgetRequest(
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal limit) {}
