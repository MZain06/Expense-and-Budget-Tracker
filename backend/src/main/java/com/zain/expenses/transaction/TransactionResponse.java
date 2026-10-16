package com.zain.expenses.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        LocalDate occurredOn,
        String note,
        Long categoryId,
        String categoryName) {

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getOccurredOn(),
                transaction.getNote(),
                transaction.getCategory().getId(),
                transaction.getCategory().getName());
    }
}
