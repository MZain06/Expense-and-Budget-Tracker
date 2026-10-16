package com.zain.expenses.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zain.expenses.category.Category;
import com.zain.expenses.category.CategoryRepository;
import com.zain.expenses.user.User;
import com.zain.expenses.user.UserRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Fast unit tests: no Spring, no database — the repositories are Mockito mocks. */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTests {

    private static final Long USER_ID = 1L;

    @Mock private TransactionRepository transactions;
    @Mock private CategoryRepository categories;
    @Mock private UserRepository users;

    private TransactionService service;

    @BeforeEach
    void createServiceWithAFixedClock() {
        Clock midOctober = Clock.fixed(Instant.parse("2026-10-16T12:00:00Z"), ZoneOffset.UTC);
        service = new TransactionService(transactions, categories, users, midOctober);
    }

    @Test
    void listDefaultsToTheCurrentMonth() {
        service.list(USER_ID, null, null);

        verify(transactions)
                .findForMonth(USER_ID, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
    }

    @Test
    void listUsesTheCategoryFilterWhenOneIsGiven() {
        service.list(USER_ID, YearMonth.of(2026, 2), 7L);

        verify(transactions)
                .findForMonthAndCategory(
                        USER_ID, 7L, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28));
    }

    @Test
    void createStoresABlankNoteAsNull() {
        User user = new User("zain@example.com", "hash");
        Category food = new Category(user, "Food");
        when(categories.findByIdAndUserId(3L, USER_ID)).thenReturn(Optional.of(food));
        when(users.getReferenceById(USER_ID)).thenReturn(user);
        when(transactions.save(any(Transaction.class))).thenAnswer(call -> call.getArgument(0));

        TransactionResponse created =
                service.create(
                        USER_ID,
                        new TransactionRequest(
                                new BigDecimal("12.50"), 3L, LocalDate.of(2026, 10, 3), "   "));

        assertThat(created.note()).isNull();
        assertThat(created.categoryName()).isEqualTo("Food");
    }

    @Test
    void createRefusesACategoryTheUserDoesNotOwn() {
        when(categories.findByIdAndUserId(99L, USER_ID)).thenReturn(Optional.empty());
        TransactionRequest request =
                new TransactionRequest(
                        new BigDecimal("5.00"), 99L, LocalDate.of(2026, 10, 3), null);

        assertThatThrownBy(() -> service.create(USER_ID, request))
                .isInstanceOf(UnknownCategoryException.class);
        verify(transactions, never()).save(any());
    }

    @Test
    void deletingATransactionTheUserDoesNotOwnIsNotFound() {
        when(transactions.findByIdAndUserId(5L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(USER_ID, 5L))
                .isInstanceOf(TransactionNotFoundException.class);
        verify(transactions, never()).delete(any());
    }
}
