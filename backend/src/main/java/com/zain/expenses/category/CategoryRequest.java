package com.zain.expenses.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** The body for creating or renaming a category. */
public record CategoryRequest(@NotBlank @Size(max = 50) String name) {}
