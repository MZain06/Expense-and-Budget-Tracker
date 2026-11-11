package com.zain.expenses.transaction;

import java.math.BigDecimal;

/** The money that moved one way in one category over a period. */
public record CategoryTotal(
        Long categoryId, String categoryName, Direction direction, BigDecimal amount) {

    /** The amount, made negative when the money went the opposite way to {@code usual}. */
    BigDecimal towards(Direction usual) {
        return direction == usual ? amount : amount.negate();
    }
}
