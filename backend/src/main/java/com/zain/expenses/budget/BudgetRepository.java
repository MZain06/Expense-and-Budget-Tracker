package com.zain.expenses.budget;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    /** All of a user's budgets for one month, in category-name order. */
    @Query(
            """
            select b from Budget b join fetch b.category
            where b.user.id = :userId and b.month = :month
            order by b.category.name
            """)
    List<Budget> findForMonth(@Param("userId") Long userId, @Param("month") LocalDate month);

    Optional<Budget> findByUserIdAndCategoryIdAndMonth(
            Long userId, Long categoryId, LocalDate month);
}
