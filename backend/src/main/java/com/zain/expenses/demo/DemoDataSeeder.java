package com.zain.expenses.demo;

import com.zain.expenses.budget.Budget;
import com.zain.expenses.budget.BudgetRepository;
import com.zain.expenses.category.Category;
import com.zain.expenses.category.CategoryRepository;
import com.zain.expenses.transaction.Transaction;
import com.zain.expenses.transaction.TransactionRepository;
import com.zain.expenses.user.User;
import com.zain.expenses.user.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** With the "demo" profile on, resets the demo account to a year of spending at every start. */
@Component
@Profile("demo")
class DemoDataSeeder implements ApplicationRunner {

    static final String EMAIL = "demo@example.com";
    static final String PASSWORD = "demo-password";

    private final UserRepository users;
    private final CategoryRepository categories;
    private final TransactionRepository transactions;
    private final BudgetRepository budgets;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;
    private final Clock clock;

    DemoDataSeeder(
            UserRepository users,
            CategoryRepository categories,
            TransactionRepository transactions,
            BudgetRepository budgets,
            PasswordEncoder passwordEncoder,
            EntityManager entityManager,
            Clock clock) {
        this.users = users;
        this.categories = categories;
        this.transactions = transactions;
        this.budgets = budgets;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Only the account's data is replaced, so a token issued before a restart still works.
        User user = users.findByEmail(EMAIL).orElseGet(this::createAccount);
        deleteAll("Transaction", user);
        deleteAll("Budget", user);
        deleteAll("Category", user);

        Map<String, Category> byName = new HashMap<>();
        for (String name : DemoData.CATEGORIES) {
            byName.put(name, categories.save(new Category(user, name)));
        }

        LocalDate today = LocalDate.now(clock);
        transactions.saveAll(
                DemoData.lastTwelveMonths(today).stream()
                        .map(
                                demo ->
                                        new Transaction(
                                                user,
                                                byName.get(demo.category()),
                                                demo.amount(),
                                                demo.date(),
                                                demo.note()))
                        .toList());

        YearMonth thisMonth = YearMonth.from(today);
        for (int monthsAgo = 0; monthsAgo < 3; monthsAgo++) {
            YearMonth month = thisMonth.minusMonths(monthsAgo);
            DemoData.MONTHLY_BUDGETS.forEach(
                    (category, limit) ->
                            budgets.save(new Budget(user, byName.get(category), month, limit)));
        }
    }

    private User createAccount() {
        return users.save(new User(EMAIL, passwordEncoder.encode(PASSWORD)));
    }

    private void deleteAll(String entity, User user) {
        entityManager
                .createQuery("delete from " + entity + " e where e.user = :user")
                .setParameter("user", user)
                .executeUpdate();
    }
}
