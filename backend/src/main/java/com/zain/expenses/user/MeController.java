package com.zain.expenses.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    /** Returns the logged-in user, read straight from their verified token. */
    @GetMapping("/api/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return new UserResponse(Long.valueOf(jwt.getSubject()), jwt.getClaimAsString("email"));
    }
}
