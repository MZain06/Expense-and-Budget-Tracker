package com.zain.expenses.user;

/** What the API returns about a user. Never includes the password hash. */
public record UserResponse(Long id, String email) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail());
    }
}
