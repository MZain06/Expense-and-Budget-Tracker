package com.zain.expenses.category;

import com.zain.expenses.common.ApiException;
import org.springframework.http.HttpStatus;

public class CategoryInUseException extends ApiException {

    public CategoryInUseException(String name, long transactionCount) {
        super(HttpStatus.CONFLICT, message(name, transactionCount));
    }

    private static String message(String name, long transactionCount) {
        if (transactionCount == 1) {
            return name + " still has 1 transaction. Move or delete it first.";
        }
        return "%s still has %d transactions. Move or delete them first."
                .formatted(name, transactionCount);
    }
}
