package com.zain.expenses.transaction;

import com.zain.expenses.category.CategoryKind;
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

    /** Totals the money in and the money out of each category of one kind. */
    @Query(
            """
            select new com.zain.expenses.transaction.CategoryTotal(
                c.id, c.name, t.direction, sum(t.amount))
            from Transaction t join t.category c
            where t.user.id = :userId
              and c.kind = :kind
              and t.occurredOn between :firstDay and :lastDay
            group by c.id, c.name, t.direction
            """)
    List<CategoryTotal> totalsByCategory(
            @Param("userId") Long userId,
            @Param("kind") CategoryKind kind,
            @Param("firstDay") LocalDate firstDay,
            @Param("lastDay") LocalDate lastDay);

    /** Totals the money in and the money out on each day, for categories of one kind. */
    @Query(
            """
            select new com.zain.expenses.transaction.DailyTotal(
                t.occurredOn, t.direction, sum(t.amount))
            from Transaction t join t.category c
            where t.user.id = :userId
              and c.kind = :kind
              and t.occurredOn between :firstDay and :lastDay
            group by t.occurredOn, t.direction
            """)
    List<DailyTotal> totalsByDay(
            @Param("userId") Long userId,
            @Param("kind") CategoryKind kind,
            @Param("firstDay") LocalDate firstDay,
            @Param("lastDay") LocalDate lastDay);

    /** Spending per category: what went out, less any refunds. */
    default List<CategorySpending> spendingByCategory(
            Long userId, LocalDate firstDay, LocalDate lastDay) {
        return CategorySpending.netOf(
                totalsByCategory(userId, CategoryKind.SPENDING, firstDay, lastDay));
    }

    /** Money received per income category. */
    default List<CategoryIncome> incomeByCategory(
            Long userId, LocalDate firstDay, LocalDate lastDay) {
        return CategoryIncome.netOf(
                totalsByCategory(userId, CategoryKind.INCOME, firstDay, lastDay));
    }

    /** Spending per day, less any refunds. Days with nothing spent don't appear. */
    default List<DailySpending> spendingByDay(Long userId, LocalDate firstDay, LocalDate lastDay) {
        return DailySpending.netOf(totalsByDay(userId, CategoryKind.SPENDING, firstDay, lastDay));
    }
}
