package com.zain.expenses.transaction;

import com.zain.expenses.common.ApiException;
import org.springframework.http.HttpStatus;

public class TransactionNotFoundException extends ApiException {

    public TransactionNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Transaction not found");
    }
}
