package com.zain.expenses.auth;

import com.zain.expenses.common.ApiException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyRegisteredException extends ApiException {

    public EmailAlreadyRegisteredException() {
        super(HttpStatus.CONFLICT, "An account with this email already exists");
    }
}
