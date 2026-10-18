package com.zain.expenses.category;

import com.zain.expenses.common.ApiException;
import org.springframework.http.HttpStatus;

public class DuplicateCategoryException extends ApiException {

    public DuplicateCategoryException(String name) {
        super(HttpStatus.CONFLICT, "You already have a category called " + name);
    }
}
