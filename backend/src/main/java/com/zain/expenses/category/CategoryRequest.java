package com.zain.expenses.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * The body for creating or updating a category. Without a kind, a new category is for spending and
 * an existing one keeps the kind it has.
 */
public record CategoryRequest(@NotBlank @Size(max = 50) String name, CategoryKind kind) {}
