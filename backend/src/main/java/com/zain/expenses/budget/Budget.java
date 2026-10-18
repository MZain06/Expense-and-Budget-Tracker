package com.zain.expenses.budget;

import com.zain.expenses.category.Category;
import com.zain.expenses.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/** A spending limit for one category in one month. */
@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // The table says "on delete cascade": deleting a category deletes its budgets.
    // @OnDelete tells Hibernate the database does this, so it doesn't try to stop it.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Category category;

    /** Always the first day of the month the budget is for. */
    @Column(nullable = false)
    private LocalDate month;

    @Column(name = "limit_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal limitAmount;

    protected Budget() {
        // required by JPA
    }

    public Budget(User user, Category category, YearMonth month, BigDecimal limitAmount) {
        this.user = user;
        this.category = category;
        this.month = month.atDay(1);
        this.limitAmount = limitAmount;
    }

    public void changeLimit(BigDecimal newLimit) {
        this.limitAmount = newLimit;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Category getCategory() {
        return category;
    }

    public YearMonth getMonth() {
        return YearMonth.from(month);
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }
}
