package com.zain.expenses.budget;

import com.zain.expenses.security.CurrentUserId;
import jakarta.validation.Valid;
import java.time.YearMonth;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public List<BudgetResponse> list(
            @CurrentUserId Long userId,
            @RequestParam(name = "month", required = false) YearMonth month) {
        return budgetService.list(userId, month);
    }

    @PutMapping("/{month}/{categoryId}")
    public BudgetResponse set(
            @CurrentUserId Long userId,
            @PathVariable("month") YearMonth month,
            @PathVariable("categoryId") Long categoryId,
            @Valid @RequestBody BudgetRequest request) {
        return budgetService.set(userId, month, categoryId, request);
    }

    @DeleteMapping("/{month}/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(
            @CurrentUserId Long userId,
            @PathVariable("month") YearMonth month,
            @PathVariable("categoryId") Long categoryId) {
        budgetService.remove(userId, month, categoryId);
    }

    @PostMapping("/{month}/copy-from-previous")
    public List<BudgetResponse> copyFromPreviousMonth(
            @CurrentUserId Long userId, @PathVariable("month") YearMonth month) {
        return budgetService.copyFromPreviousMonth(userId, month);
    }
}
