package com.zain.expenses.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

/** The money that moved one way on one day. */
public record DailyTotal(LocalDate day, Direction direction, BigDecimal amount) {

    /** The amount, made negative when the money went the opposite way to {@code usual}. */
    BigDecimal towards(Direction usual) {
        return direction == usual ? amount : amount.negate();
    }
}
