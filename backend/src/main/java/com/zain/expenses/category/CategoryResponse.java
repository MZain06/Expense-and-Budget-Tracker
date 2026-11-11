package com.zain.expenses.category;

public record CategoryResponse(Long id, String name, CategoryKind kind) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getKind());
    }
}
