package com.zain.expenses.transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdAndOccurredOnBetweenOrderByOccurredOnDesc(
            Long userId, LocalDate from, LocalDate to);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    @Query(
            """
            select t from Transaction t join fetch t.category
            where t.user.id = :userId
              and t.occurredOn between :firstDay and :lastDay
            order by t.occurredOn desc, t.id desc
            """)
    List<Transaction> findForMonth(
            @Param("userId") Long userId,
            @Param("firstDay") LocalDate firstDay,
            @Param("lastDay") LocalDate lastDay);

    @Query(
            """
            select t from Transaction t join fetch t.category
            where t.user.id = :userId
              and t.category.id = :categoryId
              and t.occurredOn between :firstDay and :lastDay
            order by t.occurredOn desc, t.id desc
            """)
    List<Transaction> findForMonthAndCategory(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("firstDay") LocalDate firstDay,
            @Param("lastDay") LocalDate lastDay);

    /** Totals a user's spending per category, in one grouped query. */
    @Query(
            """
            select new com.zain.expenses.transaction.CategorySpending(c.id, c.name, sum(t.amount))
            from Transaction t join t.category c
            where t.user.id = :userId
              and t.occurredOn between :firstDay and :lastDay
            group by c.id, c.name
            """)
    List<CategorySpending> spendingByCategory(
            @Param("userId") Long userId,
            @Param("firstDay") LocalDate firstDay,
            @Param("lastDay") LocalDate lastDay);

    /** Totals a user's spending per day. Days with nothing spent don't appear. */
    @Query(
            """
            select new com.zain.expenses.transaction.DailySpending(t.occurredOn, sum(t.amount))
            from Transaction t
            where t.user.id = :userId
              and t.occurredOn between :firstDay and :lastDay
            group by t.occurredOn
            """)
    List<DailySpending> spendingByDay(
            @Param("userId") Long userId,
            @Param("firstDay") LocalDate firstDay,
            @Param("lastDay") LocalDate lastDay);
}
