package com.zain.expenses.budget;

import com.zain.expenses.common.ApiException;
import org.springframework.http.HttpStatus;

public class IncomeCategoryBudgetException extends ApiException {

    public IncomeCategoryBudgetException(String name) {
        super(HttpStatus.BAD_REQUEST, name + " is an income category, so it can't have a budget");
    }
}
