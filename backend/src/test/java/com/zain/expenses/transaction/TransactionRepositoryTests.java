package com.zain.expenses.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.zain.expenses.TestcontainersConfiguration;
import com.zain.expenses.category.Category;
import com.zain.expenses.category.CategoryRepository;
import com.zain.expenses.user.User;
import com.zain.expenses.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class TransactionRepositoryTests {

    @Autowired private UserRepository users;
    @Autowired private CategoryRepository categories;
    @Autowired private TransactionRepository transactions;

    @Test
    void findsOnlyTransactionsInsideTheDateRange() {
        User user = users.save(new User("zain@example.com", "not-a-real-hash"));
        Category food = categories.save(new Category(user, "Food"));
        transactions.save(
                new Transaction(
                        user, food, new BigDecimal("12.50"), LocalDate.of(2026, 10, 3), "Lunch"));
        transactions.save(
                new Transaction(
                        user, food, new BigDecimal("40.00"), LocalDate.of(2026, 9, 28), "Shop"));

        List<Transaction> october =
                transactions.findByUserIdAndOccurredOnBetweenOrderByOccurredOnDesc(
                        user.getId(), LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));

        assertThat(october).hasSize(1);
        assertThat(october.get(0).getAmount()).isEqualByComparingTo("12.50");
    }

    @Test
    void doesNotReturnAnotherUsersTransaction() {
        User alice = users.save(new User("alice@example.com", "hash"));
        User bob = users.save(new User("bob@example.com", "hash"));
        Category food = categories.save(new Category(alice, "Food"));
        Transaction lunch =
                transactions.save(
                        new Transaction(
                                alice,
                                food,
                                new BigDecimal("9.99"),
                                LocalDate.of(2026, 10, 5),
                                null));

        assertThat(transactions.findByIdAndUserId(lunch.getId(), bob.getId())).isEmpty();
        assertThat(transactions.findByIdAndUserId(lunch.getId(), alice.getId())).isPresent();
    }

    @Test
    void databaseRejectsNonPositiveAmounts() {
        User user = users.save(new User("carol@example.com", "hash"));
        Category food = categories.save(new Category(user, "Food"));

        assertThatThrownBy(
                        () ->
                                transactions.saveAndFlush(
                                        new Transaction(
                                                user,
                                                food,
                                                new BigDecimal("-5.00"),
                                                LocalDate.of(2026, 10, 5),
                                                null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
