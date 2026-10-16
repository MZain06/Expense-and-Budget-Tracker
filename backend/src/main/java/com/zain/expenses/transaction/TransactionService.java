package com.zain.expenses.transaction;

import com.zain.expenses.category.Category;
import com.zain.expenses.category.CategoryRepository;
import com.zain.expenses.user.UserRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactions;
    private final CategoryRepository categories;
    private final UserRepository users;
    private final Clock clock;

    public TransactionService(
            TransactionRepository transactions,
            CategoryRepository categories,
            UserRepository users,
            Clock clock) {
        this.transactions = transactions;
        this.categories = categories;
        this.users = users;
        this.clock = clock;
    }

    /** Lists one month of the user's transactions, newest first. Defaults to this month. */
    @Transactional(readOnly = true)
    public List<TransactionResponse> list(Long userId, YearMonth month, Long categoryId) {
        YearMonth period = month != null ? month : YearMonth.now(clock);
        LocalDate firstDay = period.atDay(1);
        LocalDate lastDay = period.atEndOfMonth();
        List<Transaction> found =
                categoryId == null
                        ? transactions.findForMonth(userId, firstDay, lastDay)
                        : transactions.findForMonthAndCategory(
                                userId, categoryId, firstDay, lastDay);
        return found.stream().map(TransactionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse get(Long userId, Long id) {
        return TransactionResponse.from(findOwnedTransaction(userId, id));
    }

    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest request) {
        Category category = findOwnedCategory(userId, request.categoryId());
        Transaction transaction =
                new Transaction(
                        users.getReferenceById(userId),
                        category,
                        request.amount(),
                        request.occurredOn(),
                        clean(request.note()));
        return TransactionResponse.from(transactions.save(transaction));
    }

    @Transactional
    public TransactionResponse update(Long userId, Long id, TransactionRequest request) {
        Transaction transaction = findOwnedTransaction(userId, id);
        Category category = findOwnedCategory(userId, request.categoryId());
        transaction.update(category, request.amount(), request.occurredOn(), clean(request.note()));
        return TransactionResponse.from(transaction);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        transactions.delete(findOwnedTransaction(userId, id));
    }

    private Transaction findOwnedTransaction(Long userId, Long id) {
        return transactions
                .findByIdAndUserId(id, userId)
                .orElseThrow(TransactionNotFoundException::new);
    }

    private Category findOwnedCategory(Long userId, Long categoryId) {
        return categories
                .findByIdAndUserId(categoryId, userId)
                .orElseThrow(UnknownCategoryException::new);
    }

    private static String clean(String note) {
        return note == null || note.isBlank() ? null : note.strip();
    }
}
