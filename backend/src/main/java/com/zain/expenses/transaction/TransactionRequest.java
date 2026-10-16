package com.zain.expenses.transaction;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/** The body for creating or updating a transaction. */
public record TransactionRequest(
        @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @NotNull Long categoryId,
        @NotNull LocalDate occurredOn,
        @Size(max = 280) String note) {}
