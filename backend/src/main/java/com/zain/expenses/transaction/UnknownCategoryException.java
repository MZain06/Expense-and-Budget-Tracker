package com.zain.expenses.transaction;

import com.zain.expenses.common.ApiException;
import org.springframework.http.HttpStatus;

public class UnknownCategoryException extends ApiException {

    public UnknownCategoryException() {
        super(HttpStatus.BAD_REQUEST, "Category not found");
    }
}
