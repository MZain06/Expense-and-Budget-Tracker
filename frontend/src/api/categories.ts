import type { Category } from "../types/api";
import { apiFetch } from "./client";

export function fetchCategories(): Promise<Category[]> {
  return apiFetch<Category[]>("/categories");
}

export function createCategory(name: string): Promise<Category> {
  return apiFetch<Category>("/categories", {
    method: "POST",
    body: JSON.stringify({ name }),
  });
}

export function renameCategory(id: number, name: string): Promise<Category> {
  return apiFetch<Category>(`/categories/${id}`, {
    method: "PUT",
    body: JSON.stringify({ name }),
  });
}

export function deleteCategory(id: number): Promise<void> {
  return apiFetch<void>(`/categories/${id}`, { method: "DELETE" });
}
